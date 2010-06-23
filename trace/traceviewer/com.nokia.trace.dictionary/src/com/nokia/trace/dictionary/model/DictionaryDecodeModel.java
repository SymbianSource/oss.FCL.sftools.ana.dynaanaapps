/*
 * Copyright (c) 2007-2010 Nokia Corporation and/or its subsidiary(-ies). 
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
 * Dictionary Decode Model
 *
 */
package com.nokia.trace.dictionary.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import com.nokia.trace.dictionary.model.decodeparameters.ConstantParameter;
import com.nokia.trace.dictionary.model.decodeparameters.DecodeParameter;
import com.nokia.trace.dictionary.model.decodeparameters.FillerParameter;

/**
 * Dictionary Decode Model
 * 
 */
public class DictionaryDecodeModel {

	/**
	 * List of Components
	 */
	private ArrayList<TraceComponent> components;

	/**
	 * List of decode parameters except constantParameters
	 */
	private ArrayList<DecodeParameter> decodeParameters;

	/**
	 * List of constant decode parameters
	 */
	private ArrayList<ConstantParameter> constantParameters;

	/**
	 * List of locations
	 */
	private ArrayList<Location> locations;

	/**
	 * List of trace data objects
	 */
	private ArrayList<TraceData> traceDatas;

	/**
	 * List of metadata for this Dictionary
	 */
	private ArrayList<Options> metaData;

	/**
	 * Factory for creating decode objects
	 */
	private DecodeObjectFactory factory;

	/**
	 * Indicates is the model valid
	 */
	private boolean valid = true;

	/**
	 * Filler parameter
	 */
	private DecodeParameter fillerParameter;

	/**
	 * Constructor
	 */
	public DictionaryDecodeModel() {
		// Create lists
		components = new ArrayList<TraceComponent>();
		decodeParameters = new ArrayList<DecodeParameter>();
		constantParameters = new ArrayList<ConstantParameter>();
		locations = new ArrayList<Location>();
		traceDatas = new ArrayList<TraceData>();
		metaData = new ArrayList<Options>();

		// Create decodeobject factory
		factory = new DecodeObjectFactory();

		// Create filler parameter
		fillerParameter = new FillerParameter("Filler", false); //$NON-NLS-1$
	}

	/**
	 * Gets components to be used in creating activation information
	 * 
	 * @return list of components
	 */
	public ArrayList<TraceComponent> getActivationInformation() {
		return components;
	}

	/**
	 * Gets valid status
	 * 
	 * @return the valid
	 */
	public boolean isValid() {
		return valid;
	}

	/**
	 * Sets valid status
	 * 
	 * @param valid
	 *            the valid to set
	 */
	public void setValid(boolean valid) {
		this.valid = valid;
	}

	/**
	 * Adds component to the list to right position
	 * 
	 * @param component
	 *            component to add
	 * @return null if adding was ok, old component if component with the same
	 *         id already exists
	 */
	public TraceComponent addComponent(TraceComponent component) {
		TraceComponent returnComponent = null;
		int pos = Collections.binarySearch(components, component,
				new Comparator<TraceComponent>() {

					public int compare(TraceComponent o1, TraceComponent o2) {
						int id1 = o1.getId();
						int id2 = o2.getId();
						return id1 > id2 ? 1 : id1 < id2 ? -1 : 0;
					}

				});
		if (pos < 0) {
			components.add(-pos - 1, component);
		} else {
			returnComponent = components.get(pos);
		}
		return returnComponent;
	}

	/**
	 * Gets component for specific id
	 * 
	 * @param id
	 *            id of the component
	 * @return the component or null if not found
	 */
	public TraceComponent getComponent(int id) {
		int pos = Collections.binarySearch(components, Integer.valueOf(id),
				new Comparator<Object>() {

					public int compare(Object o1, Object o2) {
						int id1 = ((TraceComponent) o1).getId();
						int id2 = ((Integer) o2).intValue();
						return id1 > id2 ? 1 : id1 < id2 ? -1 : 0;
					}

				});
		TraceComponent component = null;
		if (pos >= 0) {
			component = components.get(pos);
		}
		return component;

	}

	/**
	 * Removes component from the model
	 * 
	 * @param id
	 *            id of the component
	 * @return true if component was found and removed
	 */
	public boolean removeComponent(int id) {
		boolean removed = false;
		int pos = Collections.binarySearch(components, Integer.valueOf(id),
				new Comparator<Object>() {

					public int compare(Object o1, Object o2) {
						int id1 = ((TraceComponent) o1).getId();
						int id2 = ((Integer) o2).intValue();
						return id1 > id2 ? 1 : id1 < id2 ? -1 : 0;
					}

				});
		if (pos >= 0) {
			components.remove(pos);
			removed = true;
		}
		return removed;

	}

	/**
	 * Adds decode parameter to the list to right position
	 * 
	 * @param parameter
	 *            parameter
	 * @return null if insertion went fine and old parameter if parameter with
	 *         same type already exists. Parameter must be then checked for case
	 *         of collision in decode files.
	 */
	public DecodeParameter addDecodeParameter(DecodeParameter parameter) {
		DecodeParameter returnParameter = null;
		int pos = Collections.binarySearch(decodeParameters, parameter,
				new Comparator<DecodeParameter>() {

					public int compare(DecodeParameter o1, DecodeParameter o2) {
						int val;
						if (o1 == null || o2 == null) {
							val = -1;
						} else {
							val = o1.getType().compareTo(o2.getType());
						}
						return val > 0 ? 1 : val < 0 ? -1 : 0;
					}
				});
		if (pos < 0) {
			decodeParameters.add(-pos - 1, parameter);
		} else {
			returnParameter = decodeParameters.get(pos);
		}
		return returnParameter;
	}

	/**
	 * Gets decode parameter with type
	 * 
	 * @param type
	 *            type of the decode parameter
	 * @return decode parameter or null if not found
	 */
	public DecodeParameter getDecodeParameter(String type) {
		int pos = Collections.binarySearch(decodeParameters, type,
				new Comparator<Object>() {

					public int compare(Object o1, Object o2) {
						int val = ((DecodeParameter) o1).getType().compareTo(
								(String) o2);
						return val > 0 ? 1 : val < 0 ? -1 : 0;
					}
				});

		DecodeParameter parameter = null;
		if (pos >= 0) {
			parameter = decodeParameters.get(pos);
		}
		return parameter;
	}

	/**
	 * Adds constant parameter to the list to right position
	 * 
	 * @param parameter
	 */
	public void addConstantParameter(ConstantParameter parameter) {
		int pos = Collections.binarySearch(constantParameters, parameter,
				new Comparator<ConstantParameter>() {

					public int compare(ConstantParameter o1,
							ConstantParameter o2) {
						int val;
						if (o1 == null || o2 == null) {
							val = -1;
						} else {
							val = o1.getType().compareTo(o2.getType());
						}
						return val > 0 ? 1 : val < 0 ? -1 : 0;
					}
				});
		if (pos < 0) {
			constantParameters.add(-pos - 1, parameter);
		}
	}

	/**
	 * Gets constant parameter with name(text)
	 * 
	 * @param name
	 *            name of the constant parameter
	 * @return constant parameter or null if not found
	 */
	public ConstantParameter getConstantParameter(String name) {
		int pos = Collections.binarySearch(constantParameters, name,
				new Comparator<Object>() {

					public int compare(Object o1, Object o2) {
						int val = ((ConstantParameter) o1).getType().compareTo(
								(String) o2);
						return val > 0 ? 1 : val < 0 ? -1 : 0;
					}
				});
		ConstantParameter parameter = null;
		if (pos >= 0) {
			parameter = constantParameters.get(pos);
		}
		return parameter;
	}

	/**
	 * Adds location to the list to right position
	 * 
	 * @param location
	 */
	public void addLocation(Location location) {
		int pos = Collections.binarySearch(locations, location,
				new Comparator<Location>() {

					public int compare(Location o1, Location o2) {
						int id1 = o1.getId();
						int id2 = o2.getId();
						return id1 > id2 ? 1 : id1 < id2 ? -1 : 0;
					}

				});
		if (pos < 0) {
			locations.add(-pos - 1, location);
		}

	}

	/**
	 * Gets location for specific id
	 * 
	 * @param id
	 *            id of the location
	 * @return the location or null if not found
	 */
	public Location getLocation(int id) {
		int pos = Collections.binarySearch(locations, Integer.valueOf(id),
				new Comparator<Object>() {

					public int compare(Object o1, Object o2) {
						int id1 = ((Location) o1).getId();
						int id2 = ((Integer) o2).intValue();
						return id1 > id2 ? 1 : id1 < id2 ? -1 : 0;
					}

				});
		Location location = null;
		if (pos >= 0) {
			location = locations.get(pos);
		}
		return location;

	}

	/**
	 * Adds trace data to the list to right position
	 * 
	 * @param traceData
	 *            trace data
	 */
	public void addTraceData(TraceData traceData) {
		int pos = Collections.binarySearch(traceDatas, traceData,
				new Comparator<TraceData>() {

					public int compare(TraceData o1, TraceData o2) {
						int id1 = o1.getId();
						int id2 = o2.getId();
						return id1 > id2 ? 1 : id1 < id2 ? -1 : 0;
					}

				});
		if (pos < 0) {
			traceDatas.add(-pos - 1, traceData);
		}

	}

	/**
	 * Gets trace data for specific id
	 * 
	 * @param id
	 *            id of the trace data
	 * @return the trace data or null if not found
	 */
	public TraceData getTraceData(int id) {
		int pos = Collections.binarySearch(traceDatas, Integer.valueOf(id),
				new Comparator<Object>() {

					public int compare(Object o1, Object o2) {
						int id1 = ((TraceData) o1).getId();
						int id2 = ((Integer) o2).intValue();
						return id1 > id2 ? 1 : id1 < id2 ? -1 : 0;
					}

				});
		TraceData traceData = null;
		if (pos >= 0) {
			traceData = traceDatas.get(pos);
		}
		return traceData;
	}

	/**
	 * Gets the metadata
	 * 
	 * @return the metaData
	 */
	public ArrayList<Options> getMetaData() {
		return metaData;
	}

	/**
	 * Gets DecodeObjectFactory
	 * 
	 * @return the factory
	 */
	public DecodeObjectFactory getFactory() {
		return factory;
	}

	/**
	 * Gets the filler parameter
	 * 
	 * @return the fillerParameter
	 */
	public DecodeParameter getFillerParameter() {
		return fillerParameter;
	}

	/**
	 * Clears whole model
	 */
	public void clearModel() {
		components.clear();
		decodeParameters.clear();
		constantParameters.clear();
		locations.clear();
		traceDatas.clear();
		metaData.clear();
		valid = true;
	}

	/**
	 * Clear unneeded arrays from the model after it's complete
	 */
	public void clearAfterModelIsReady() {
		locations.clear();
		traceDatas.clear();
	}

	/**
	 * Gets group ID with component ID and group name
	 * 
	 * @param componentId
	 *            component ID
	 * @param groupName
	 *            group name
	 * @return group ID or -1 if not found
	 */
	public int getGroupIdWithName(int componentId, String groupName) {
		int groupId = -1;

		// Find the correct component
		TraceComponent component = getComponent(componentId);
		if (component != null) {

			// Go through groups
			ArrayList<TraceGroup> groups = component.getGroups();
			for (int i = 0; i < groups.size(); i++) {

				// Check if the name equals
				if (groups.get(i).getName().equals(groupName)) {
					groupId = groups.get(i).getId();
					break;
				}
			}
		}
		return groupId;
	}
}
