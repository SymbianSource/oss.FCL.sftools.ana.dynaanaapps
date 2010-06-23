/*
* Copyright (c) 2008 Nokia Corporation and/or its subsidiary(-ies). 
* All rights reserved.
* This component and the accompanying materials are made available
* under the terms of "Eclipse Public License v1.0"
* which accompanies this distribution, and is available
* at the URL "http://www.eclipse.org/legal/epl-v10.html".
*
* Initial Contributors:
* Nokia Corporation - initial contribution.
*
* Contributors:
*
* Description:
*
* Maps trace locations into traces and vice versa
*
*/
package com.nokia.tracebuilder.engine;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import com.nokia.tracebuilder.engine.source.SourceProperties;
import com.nokia.tracebuilder.model.Trace;
import com.nokia.tracebuilder.model.TraceGroup;
import com.nokia.tracebuilder.model.TraceModel;
import com.nokia.tracebuilder.model.TraceObject;
import com.nokia.tracebuilder.source.SourceContext;

/**
 * Maps trace locations into traces and vice versa.
 * 
 */
public final class TraceLocationMap {

	/**
	 * List of unrelated traces
	 */
	private TraceLocationList unrelated = new TraceLocationList();

	/**
	 * Parser groups
	 */
	private HashMap<String, TraceLocationList> parserGroups = new HashMap<String, TraceLocationList>();

	/**
	 * The trace model
	 */
	private TraceModel model;

	/**
	 * Global list of locations, used for verification purposes with
	 * GLOBAL_LOCATION_ASSERTS configuration flag
	 */
	private ArrayList<TraceLocation> globalList;

	/**
	 * Flag which is set when "Source missing" error is shown. Prevents it to be
	 * shown again
	 */
	private boolean missingSourceErrorShown;

	/**
	 * Creates a location mapper
	 * 
	 * @param model
	 *            the trace model
	 */
	public TraceLocationMap(TraceModel model) {
		if (TraceBuilderConfiguration.GLOBAL_LOCATION_ASSERTS) {
			globalList = new ArrayList<TraceLocation>();
		}
		this.model = model;
		model.addModelListener(new LocationMapModelListener(this));
		model.addExtension(unrelated);
	}

	/**
	 * Adds the locations from the source file to the map
	 * 
	 * @param source
	 *            properties of the source to be added
	 */
	public void addSource(SourceProperties source) {
		for (TraceLocation location : source) {
			if (TraceBuilderConfiguration.GLOBAL_LOCATION_ASSERTS) {
				if (globalList.contains(location)) {
					TraceBuilderGlobals.getEvents().postAssertionFailed(
							"Location already in global list", //$NON-NLS-1$
							location.getConvertedName());
				} else {
					globalList.add(location);
				}
			}
			// Generates locationAdded event via TraceLocationListListener
			addNewLocationToTrace(location);
		}
		removeFromStorage(source);
	}

	/**
	 * Updates the locations based on the new source properties
	 * 
	 * @param source
	 *            the source properties of the updated source
	 */
	void updateSource(SourceProperties source) {
		for (TraceLocation location : source) {
			if (location.isDeleted()) {
				// Generates locationRemoved event via TraceLocationListListener
				locationDeleted(location);
			} else if (location.isContentChanged()) {
				locationContentChanged(location);
			} else {
				// Generates locationChanged events if offset or length has
				// changed
				location.notifyLocationChanged();
			}
		}
		checkGlobalValidity();
	}

	/**
	 * Removes the locations of the closed source properties
	 * 
	 * @param source
	 *            the source properties of the removed source
	 * @param path
	 *            the file path as string
	 * @param name
	 *            the file name
	 */
	public void removeSource(SourceProperties source, String path,
			String name) {
		missingSourceErrorShown = false;
		for (TraceLocation location : source) {
			if (TraceBuilderConfiguration.GLOBAL_LOCATION_ASSERTS) {
				if (!globalList.remove(location)) {
					TraceBuilderGlobals.getEvents().postAssertionFailed(
							"Location not in global list", //$NON-NLS-1$
							location.getConvertedName());
				}
			}
			TraceLocationList list = location.getLocationList();
			if (list != null) {
				addLastKnownLocation(source, location, list, path, name);
				// Generates locationRemoved event via TraceLocationListListener
				list.removeLocation(location);
			} else {
				if (TraceBuilderConfiguration.ASSERTIONS_ENABLED) {
					TraceBuilderGlobals.getEvents().postAssertionFailed(
							"Unassociated location on remove", //$NON-NLS-1$
							location.getConvertedName());
				}
			}
		}
	}	

	/**
	 * Converts a location to a last known  location
	 * 
	 * @param source
	 *            the source file
	 * @param location
	 *            the location
	 * @param list
	 *            the location list
	 * @param path
	 *            the file path as string
	 * @param name
	 *            the file name
	 */
	public void addLastKnownLocation(SourceProperties source,
			TraceLocation location, TraceLocationList list, String path, String name) {
		TraceObject owner = list.getOwner();
		if (owner instanceof Trace) {
			LastKnownLocationList pll = owner
					.getExtension(LastKnownLocationList.class);
			if (pll == null) {
				pll = new LastKnownLocationList();
				owner.addExtension(pll);
			}
			if (path == null) {
				path = source.getFilePath();
			}
			if (name == null) {
				name = source.getFileName();
			}			
			if (path != null && name != null) {
				if (new File(path + name).exists()) {
					String cname = null;
					String mname = null;
					SourceContext context = source.getSourceEditor()
							.getContext(location.getOffset());
					if (context != null) {
						cname = context.getClassName();
						mname = context.getFunctionName();
					}
					LastKnownLocation loc = new LastKnownLocation(path, name,
							location.getLineNumber(), cname, mname);
					pll.addLocation(loc);
				} else {
					showSourceRemovedError(name);
				}
			}
		}
	}	
	
	/**
	 * Shows source with traces removed error
	 * 
	 * @param name
	 *            the source name
	 */
	private void showSourceRemovedError(String name) {
		// TODO: Add an error code for this and move message there
		if (!missingSourceErrorShown) {
			missingSourceErrorShown = true;
			String msg = Messages
					.getString("TraceLocationMap.SourceWithTraceRemoved"); //$NON-NLS-1$
			TraceBuilderGlobals.getEvents().postErrorMessage(msg, name, true);
		}
	}

	/**
	 * Removes locations from last known storage
	 * 
	 * @param source
	 *            the source file
	 */
	private void removeFromStorage(SourceProperties source) {
		File f = new File(source.getFilePath() + source.getFileName());
		Iterator<TraceGroup> groups = model.getGroups();
		while (groups.hasNext()) {
			Iterator<Trace> traces = groups.next().getTraces();
			while (traces.hasNext()) {
				LastKnownLocationList list = traces.next().getExtension(
						LastKnownLocationList.class);
				if (list != null) {
					Iterator<LocationProperties> locs = list.iterator();
					while (locs.hasNext()) {
						LastKnownLocation loc = (LastKnownLocation) locs
								.next();
						File locf = new File(loc.getFilePath()
								+ loc.getFileName());
						if (f.equals(locf)) {
							locs.remove();
							list.fireLocationRemoved(loc);
						}
					}
				}
			}
		}
	}

	/**
	 * Adds a location to trace or to the unrelated list if a trace cannot be
	 * found.
	 * 
	 * @param location
	 *            the location to be added
	 */
	private void addNewLocationToTrace(TraceLocation location) {
		TraceLocationList list;
		Trace trace = model.findTraceByName(location.getOriginalName());
		if (trace != null) {
			list = trace.getExtension(TraceLocationList.class);
			if (list == null) {
				list = new TraceLocationList();
				trace.addExtension(list);
			}
		} else {
			String name = location.getParserRule().getLocationParser()
					.getLocationGroup();
			if (name == null) {
				list = unrelated;
			} else {
				list = parserGroups.get(name);
				if (list == null) {
					list = new TraceLocationList(name);
					model.addExtension(list);
					parserGroups.put(name, list);
				}
			}
		}
		list.addLocation(location);
	}

	/**
	 * Checks that all locations are valid.
	 * Posts assertion failed events if not valid
	 */
	private void checkGlobalValidity() {
		if (TraceBuilderConfiguration.GLOBAL_LOCATION_ASSERTS) {
			boolean failure = false;
			for (int i = 0; i < globalList.size(); i++) {
				TraceLocation loc = globalList.get(i);
				if (loc.isDeleted()) {
					TraceBuilderGlobals.getEvents().postAssertionFailed(
							"Deleted location found", //$NON-NLS-1$
							loc.getConvertedName());
					failure = true;
				} else if (loc.getLocationList() == null) {
					TraceBuilderGlobals.getEvents().postAssertionFailed(
							"Unassociated location found", //$NON-NLS-1$
							loc.getConvertedName());
					failure = true;
				} else if (loc.getTag() == null) {
					TraceBuilderGlobals.getEvents().postAssertionFailed(
							"Untagged location found", loc.getConvertedName()); //$NON-NLS-1$
					failure = true;
				} else if (loc.getLength() <= loc.getTag().length()) {
					TraceBuilderGlobals.getEvents().postAssertionFailed(
							"Unassociated location found", //$NON-NLS-1$
							loc.getConvertedName());
					failure = true;
				}
			}
			if (failure) {
				TraceBuilderGlobals.getEvents().postCriticalAssertionFailed(
						"Invalid location(s) found", null); //$NON-NLS-1$
			}
		}
	}

	/**
	 * Processes a location which has been deleted
	 * 
	 * @param location
	 *            the location that was deleted
	 */
	private void locationDeleted(TraceLocation location) {
		if (TraceBuilderConfiguration.GLOBAL_LOCATION_ASSERTS) {
			if (!globalList.remove(location)) {
				TraceBuilderGlobals.getEvents().postAssertionFailed(
						"Location not in global list", //$NON-NLS-1$
						location.getConvertedName());
			}
		}
		TraceLocationList list = location.getLocationList();
		if (list != null) {
			list.removeLocation(location);
		} else {
			if (TraceBuilderConfiguration.ASSERTIONS_ENABLED) {
				TraceBuilderGlobals.getEvents().postCriticalAssertionFailed(
						"Unassociated location on delete", //$NON-NLS-1$
						location.getConvertedName());
			}
		}
	}

	/**
	 * Processes a location that has content changed flag
	 * 
	 * @param location
	 *            the location to be processed
	 */
	private void locationContentChanged(TraceLocation location) {
		// If location is new, it does not have a list
		if (location.getLocationList() == null) {
			if (TraceBuilderConfiguration.GLOBAL_LOCATION_ASSERTS) {
				if (globalList.contains(location)) {
					TraceBuilderGlobals.getEvents().postAssertionFailed(
							"Location already in global list", //$NON-NLS-1$
							location.getConvertedName());
				} else {
					globalList.add(location);
				}
			}
			// Generates locationAdded event via TraceLocationListListener
			addNewLocationToTrace(location);
		} else if (location.isNameChanged()) {
			// Generates locationRemoved to listeners of old list and
			// locationAdded to listeners of new list
			moveLocation(location);
			// Generates content and validity changed events
			location.notifyLocationChanged();
		} else if (location.isContentChanged()) {
			// Generates content and validity changed events
			location.notifyLocationChanged();
		}
	}

	/**
	 * Moves a location from trace to another. Does nothing if the target trace
	 * is same as source trace
	 * 
	 * @param location
	 *            the location to be moved
	 */
	private void moveLocation(TraceLocation location) {
		Trace trace = location.getTrace();
		Trace newTrace = model.findTraceByName(location.getOriginalName());
		// If the traces differ, this relocates the location
		// to different location array
		if (trace != newTrace) {
			TraceLocationList list = location.getLocationList();
			// Removes from existing list and adds the existing list to the
			// updates list
			list.removeLocation(location);
			// Adds to new list. If new list does not exist, it is created
			if (newTrace != null) {
				list = newTrace.getExtension(TraceLocationList.class);
				if (list == null) {
					list = new TraceLocationList();
					newTrace.addExtension(list);
				}
			} else {
				list = unrelated;
			}
			list.addLocation(location);
		}
	}

	/**
	 * Returns the list of unrelated trace objects.
	 * 
	 * @return list of unrelated traces
	 */
	TraceLocationList getUnrelatedTraces() {
		return unrelated;
	}

	/**
	 * Removes all location lists from the model
	 */
	public void clearAll() {
		model.removeExtension(unrelated);
		Iterator<TraceGroup> groups = model.getGroups();
		while (groups.hasNext()) {
			TraceGroup group = groups.next();
			Iterator<Trace> traces = group.getTraces();
			while (traces.hasNext()) {
				Trace trace = traces.next();
				TraceLocationList list = trace
						.getExtension(TraceLocationList.class);
				trace.removeExtension(list);
			}
		}
	}

	/**
	 * Moves the locations from trace to unrelated list
	 * 
	 * @param trace
	 *            the trace
	 */
	void moveToUnrelated(Trace trace) {
		TraceLocationList list = trace.getExtension(TraceLocationList.class);
		if (list != null) {
			trace.removeExtension(list);
			for (LocationProperties loc : list) {
				unrelated.addLocation((TraceLocation) loc);
			}
		}
	}

	/**
	 * Moves locations from unrelated to the given trace
	 * 
	 * @param trace
	 *            the trace
	 */
	void moveFromUnrelated(Trace trace) {
		String name = trace.getName();
		TraceLocationList list = null;
		Iterator<LocationProperties> itr = unrelated.iterator();
		while (itr.hasNext()) {
			TraceLocation location = (TraceLocation) itr.next();
			if (name.equals(location.getOriginalName())) {
				list = trace.getExtension(TraceLocationList.class);
				if (list == null) {
					list = new TraceLocationList();
					trace.addExtension(list);
				}
				// NOTE: This must replicate the behavior of
				// TraceLocationList.removeLocation
				itr.remove();
				unrelated.fireLocationRemoved(location);
				list.addLocation(location);
			}
		}
	}
}
