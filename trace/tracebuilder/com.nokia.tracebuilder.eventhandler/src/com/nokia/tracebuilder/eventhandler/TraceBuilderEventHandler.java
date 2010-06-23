/*
* Copyright (c) 2007 Nokia Corporation and/or its subsidiary(-ies). 
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
* Content provider for the event view
*
*/
package com.nokia.tracebuilder.eventhandler;

import java.util.Iterator;

import com.nokia.trace.eventrouter.TraceEvent;
import com.nokia.trace.eventview.EventListEntry;
import com.nokia.trace.eventview.EventListEntryString;
import com.nokia.trace.eventview.TraceEventHandler;
import com.nokia.trace.eventview.TraceEventList;
import com.nokia.trace.eventview.TraceEventView;
import com.nokia.tracebuilder.engine.LastKnownLocationList;
import com.nokia.tracebuilder.engine.TraceBuilderGlobals;
import com.nokia.tracebuilder.engine.TraceLocationList;
import com.nokia.tracebuilder.model.Trace;
import com.nokia.tracebuilder.model.TraceConstantTable;
import com.nokia.tracebuilder.model.TraceConstantTableEntry;
import com.nokia.tracebuilder.model.TraceGroup;
import com.nokia.tracebuilder.model.TraceModel;
import com.nokia.tracebuilder.model.TraceModelExtension;
import com.nokia.tracebuilder.model.TraceModelExtensionListener;
import com.nokia.tracebuilder.model.TraceModelListener;
import com.nokia.tracebuilder.model.TraceModelResetListener;
import com.nokia.tracebuilder.model.TraceObject;
import com.nokia.tracebuilder.model.TraceParameter;
import com.nokia.tracebuilder.rules.HiddenTraceObjectRule;
import com.nokia.tracebuilder.source.SourceLocation;

/**
 * Content provider for the event view
 * 
 */
public final class TraceBuilderEventHandler implements TraceModelListener,
		TraceModelResetListener, TraceEventHandler, TraceModelExtensionListener {

	/**
	 * Event list from event view plug-in
	 */
	private TraceEventList eventList;

	/**
	 * Location list listener
	 */
	private EventListLocationListListener locationListListener;

	/**
	 * Selection listener for the event view
	 */
	private EventListSelectionListener selectionListener;

	/**
	 * Constructor
	 */
	TraceBuilderEventHandler() {
		selectionListener = new EventListSelectionListener();
		eventList = TraceEventView.getEventList();
		// Adds listeners to model and event list
		TraceBuilderGlobals.getTraceModel().addModelListener(this);
		TraceBuilderGlobals.getTraceModel().addResetListener(this);
		TraceBuilderGlobals.getTraceModel().addExtensionListener(this);
		eventList.addEventHandler(this);
		eventList.addSelectionListener(selectionListener);
		locationListListener = new EventListLocationListListener(this,
				eventList);
		// The model may contain multiple location lists, one for each parser
		Iterator<TraceLocationList> lists = TraceBuilderGlobals.getTraceModel()
				.getExtensions(TraceLocationList.class);
		while (lists.hasNext()) {
			lists.next().addLocationListListener(locationListListener);
		}
	}

	/**
	 * Shutdown
	 */
	void shutdown() {
		// Removes the listeners from model and event list
		TraceBuilderGlobals.getTraceModel().getExtension(
				TraceLocationList.class).removeLocationListListener(
				locationListListener);
		TraceBuilderGlobals.getTraceModel().removeModelListener(this);
		TraceBuilderGlobals.getTraceModel().removeResetListener(this);
		TraceBuilderGlobals.getTraceModel().removeExtensionListener(this);
		eventList.removeEventHandler(this);
		eventList.removeSelectionListener(selectionListener);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.model.TraceModelListener#
	 *      objectAdded(com.nokia.tracebuilder.model.TraceObject,
	 *      com.nokia.tracebuilder.model.TraceObject)
	 */
	public void objectAdded(TraceObject owner, TraceObject object) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.model.TraceModelListener#
	 *      objectCreationComplete(com.nokia.tracebuilder.model.TraceObject)
	 */
	public void objectCreationComplete(TraceObject object) {
		if (object.getModel().isValid()) {
			HiddenTraceObjectRule hiddenRule = object
					.getExtension(HiddenTraceObjectRule.class);
			if (hiddenRule == null) {
				String event = getAddEventTitle(object);
				if (event != null) {
					addObjectCreationEvent(object, event);
				}
			}
		}
	}

	/**
	 * Adds an object creation event
	 * 
	 * @param object
	 *            the object
	 * @param event
	 *            the event title
	 */
	private void addObjectCreationEvent(TraceObject object, String event) {
		// If the auto-converter is running, creation events are not
		// logged. However, an existing trace might have been
		// re-created due to save operation and thus the new trace
		// is linked to that
		if (!TraceBuilderGlobals.getSourceContextManager().isConverting()) {
			EventListEntry entry = new EventListEntryTraceObject(
					TraceEvent.INFO, event, object);
			entry.setCategory(TraceBuilderGlobals.getEvents()
					.getEventCategory());
			eventList.addEntry(entry);
		} else {
			for (EventListEntry entry : eventList) {
				if (entry instanceof EventListEntryTraceObject) {
					EventListEntryTraceObject objEntry = (EventListEntryTraceObject) entry;
					if (objEntry.getObject() == null) {
						if (objEntry.getSourceName().equals(object.getName())) {
							objEntry.setObject(object);
							break;
						}
					}
				}
			}
		}
	}

	/**
	 * Gets the creation event title for given object
	 * 
	 * @param object
	 *            the object
	 * @return the event title
	 */
	private String getAddEventTitle(TraceObject object) {
		String event = null;
		if (object instanceof Trace) {
			event = Messages
					.getString("EventListContentProvider.TraceAddedEvent"); //$NON-NLS-1$
		} else if (object instanceof TraceGroup) {
			event = Messages
					.getString("EventListContentProvider.GroupAddedEvent"); //$NON-NLS-1$
		} else if (object instanceof TraceParameter) {
			event = Messages
					.getString("EventListContentProvider.ParameterAddedEvent"); //$NON-NLS-1$
		} else if (object instanceof TraceConstantTable) {
			event = Messages
					.getString("EventListContentProvider.ConstantTableAddedEvent"); //$NON-NLS-1$
		} else if (object instanceof TraceConstantTableEntry) {
			event = Messages
					.getString("EventListContentProvider.ConstantTableEntryAddedEvent"); //$NON-NLS-1$
		}
		return event;
	}

	/**
	 * Gets the creation event title for given object
	 * 
	 * @param object
	 *            the object
	 * @return the event title
	 */
	private String getRemoveEventTitle(TraceObject object) {
		String event = null;
		if (object instanceof Trace) {
			event = Messages
					.getString("EventListContentProvider.TraceRemovedEvent"); //$NON-NLS-1$
		} else if (object instanceof TraceGroup) {
			event = Messages
					.getString("EventListContentProvider.GroupRemovedEvent"); //$NON-NLS-1$
		} else if (object instanceof TraceParameter) {
			event = Messages
					.getString("EventListContentProvider.ParameterRemovedEvent"); //$NON-NLS-1$
		} else if (object instanceof TraceConstantTable) {
			event = Messages
					.getString("EventListContentProvider.ConstantTableRemovedEvent"); //$NON-NLS-1$
		} else if (object instanceof TraceConstantTableEntry) {
			event = Messages
					.getString("EventListContentProvider.ConstantTableEntryRemovedEvent"); //$NON-NLS-1$
		}
		return event;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.model.TraceModelListener#
	 *      objectRemoved(com.nokia.tracebuilder.model.TraceObject,
	 *      com.nokia.tracebuilder.model.TraceObject)
	 */
	public void objectRemoved(TraceObject owner, TraceObject object) {
		// If the auto-converter is running, creation events are not logged
		if (!TraceBuilderGlobals.getSourceContextManager().isConverting()) {
			HiddenTraceObjectRule hiddenRule = object
					.getExtension(HiddenTraceObjectRule.class);
			if (hiddenRule == null) {
				EventListEntry entry = new EventListEntryString(
						TraceEvent.INFO, getRemoveEventTitle(object), object
								.getName());
				entry.setCategory(TraceBuilderGlobals.getEvents()
						.getEventCategory());
				eventList.addEntry(entry);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.model.TraceModelListener#
	 *      propertyUpdated(com.nokia.tracebuilder.model.TraceObject, int)
	 */
	public void propertyUpdated(TraceObject object, int property) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.model.TraceModelResetListener#modelReset()
	 */
	public void modelReset() {
		eventList.removeAll(SourceLocation.class);
		for (EventListEntry entry : eventList) {
			if (entry instanceof EventListEntryTraceObject) {
				((EventListEntryTraceObject) entry).objectDeleted();
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.model.TraceModelResetListener#modelResetting()
	 */
	public void modelResetting() {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.model.TraceModelResetListener#modelValid(boolean)
	 */
	public void modelValid(boolean valid) {
		if (valid) {
			Iterator<TraceGroup> groups = TraceBuilderGlobals.getTraceModel()
					.getGroups();
			while (groups.hasNext()) {
				Iterator<Trace> traces = groups.next().getTraces();
				while (traces.hasNext()) {
					Trace trace = traces.next();
					checkTraceValidity(trace);
				}
			}
		}
	}

	/**
	 * Checks that trace is referenced in source files
	 * 
	 * @param trace
	 *            the trace
	 */
	void checkTraceValidity(Trace trace) {
		TraceLocationList list = trace.getExtension(TraceLocationList.class);
		LastKnownLocationList plist = trace
				.getExtension(LastKnownLocationList.class);
		int locCount = 0;
		if (list != null) {
			locCount = list.getLocationCount();
		}
		if (plist != null) {
			locCount += plist.getLocationCount();
		}
		EventListEntryLocationCountError entry = trace
				.getExtension(EventListEntryLocationCountError.class);
		if (locCount == 0 || locCount > 1) {
			if (entry == null) {
				entry = new EventListEntryLocationCountError(eventList, trace);
				eventList.addEntry(entry);
			} else {
				entry.update();
			}
		} else {
			if (entry != null) {
				eventList.removeEntry(entry);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.trace.eventview.TraceEventHandler#
	 *      handleEvent(com.nokia.trace.eventrouter.TraceEvent)
	 */
	public boolean handleEvent(TraceEvent event) {
		boolean retval = false;
		Object o = event.getSource();
		if (o instanceof SourceLocation || o instanceof TraceObject) {
			eventList.asyncExec(new EventAdder(eventList, event));
			retval = true;
		}
		return retval;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.model.TraceModelExtensionListener#
	 *      extensionAdded(com.nokia.tracebuilder.model.TraceObject,
	 *      com.nokia.tracebuilder.model.TraceModelExtension)
	 */
	public void extensionAdded(TraceObject object, TraceModelExtension extension) {
		if (object instanceof Trace || object instanceof TraceModel) {
			if (extension instanceof TraceLocationList) {
				TraceLocationList list = (TraceLocationList) extension;
				list.addLocationListListener(locationListListener);
			} else if (extension instanceof LastKnownLocationList) {
				LastKnownLocationList list = (LastKnownLocationList) extension;
				list.addLocationListListener(locationListListener);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.model.TraceModelExtensionListener#
	 *      extensionRemoved(com.nokia.tracebuilder.model.TraceObject,
	 *      com.nokia.tracebuilder.model.TraceModelExtension)
	 */
	public void extensionRemoved(TraceObject object,
			TraceModelExtension extension) {
		if (object instanceof Trace || object instanceof TraceModel) {
			if (extension instanceof TraceLocationList) {
				TraceLocationList list = (TraceLocationList) extension;
				list.removeLocationListListener(locationListListener);
			} else if (extension instanceof LastKnownLocationList) {
				LastKnownLocationList list = (LastKnownLocationList) extension;
				list.removeLocationListListener(locationListListener);
			}
		}
	}

}