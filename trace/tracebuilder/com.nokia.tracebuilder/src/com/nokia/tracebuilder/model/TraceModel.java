/*
* Copyright (c) 2008-2010 Nokia Corporation and/or its subsidiary(-ies). 
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
* Trace model is a collection of trace groups
*
*/
package com.nokia.tracebuilder.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.Properties;

import com.nokia.tracebuilder.engine.TraceBuilderGlobals;
import com.nokia.tracebuilder.project.FormattingUtils;
import com.nokia.tracebuilder.project.GroupNameHandlerBase;
import com.nokia.tracebuilder.source.SourceConstants;

/**
 * Trace model is a collection of trace groups. Listener interfaces can be
 * attached to a trace model to receive change notifications when traces are
 * added, removed or modified.
 * 
 */
public class TraceModel extends TraceObject implements Iterable<TraceGroup> {

	/**
	 * Group property prefix
	 */
	public final String GROUP_PROPERTY_PREFIX = "[GROUP]"; //$NON-NLS-1$

	/**
	 * Trace property prefix
	 */
	public final String TRACE_PROPERTY_PREFIX = "[TRACE]"; //$NON-NLS-1$

	/**
	 * Obsolete property prefix
	 */
	public final String OBSOLETE_PROPERTY_PREFIX = "[[OBSOLETE]]"; //$NON-NLS-1$

	/**
	 * Group Id prefix
	 */
	public final String GROUP_ID_PREFIX = "["; //$NON-NLS-1$

	/**
	 * Group Id suffix
	 */
	public final String GROUP_ID_SUFFIX = "]"; //$NON-NLS-1$	

	/**
	 * Factory object for creating other trace objects
	 */
	private TraceObjectFactory factory;

	/**
	 * Property verifier interface
	 */
	private TraceObjectPropertyVerifier verifier;

	/**
	 * List of trace groups
	 */
	private ArrayList<TraceGroup> groups = new ArrayList<TraceGroup>();

	/**
	 * List of traces, sorted by name
	 */
	private ArrayList<Trace> tracesByName = new ArrayList<Trace>();

	/**
	 * List of model listeners
	 */
	private ArrayList<TraceModelListener> modelListeners = new ArrayList<TraceModelListener>();

	/**
	 * List of extension listeners
	 */
	private ArrayList<TraceModelExtensionListener> extensionListeners = new ArrayList<TraceModelExtensionListener>();

	/**
	 * List of reset listeners
	 */
	private ArrayList<TraceModelResetListener> resetListeners = new ArrayList<TraceModelResetListener>();

	/**
	 * Processing listeners
	 */
	private ArrayList<TraceProcessingListener> processingListeners = new ArrayList<TraceProcessingListener>();

	/**
	 * List of constant tables
	 */
	private ArrayList<TraceConstantTable> constantTables = new ArrayList<TraceConstantTable>();

	/**
	 * Validity flag
	 */
	private boolean valid;

	/**
	 * Number of nested calls to startProcessing
	 */
	private int isProcessing;

	/**
	 * Model was changed during processing
	 */
	private boolean modelChangedDuringProcessing;

	/**
	 * Hex radix
	 */
	public int HEX_RADIX = 16; // CodForChk_Dis_Magic

	/**
	 * Number of user defined groups
	 */	
	private int numberOfUserDefinedGroups = 0;
	
	/**
	 * Constructor
	 * 
	 * @param factory
	 *            the rule factory
	 * @param verifier
	 *            the property verifier
	 */
	public TraceModel(TraceObjectRuleFactory factory,
			TraceObjectPropertyVerifier verifier) {
		// Stored for callback purposes
		setModel(this);
		this.factory = new TraceObjectFactory(this, factory);
		this.verifier = verifier;
	}

	/**
	 * Adds a new trace model listener to this model
	 * 
	 * @param listener
	 *            the new listener
	 */
	public void addModelListener(TraceModelListener listener) {
		modelListeners.add(listener);
	}

	/**
	 * Removes a trace model listener. Does nothing if the listener is not found
	 * 
	 * @param listener
	 *            the listener to be removed
	 */
	public void removeModelListener(TraceModelListener listener) {
		modelListeners.remove(listener);
	}

	/**
	 * Adds a new trace model extension listener to this model
	 * 
	 * @param listener
	 *            the new listener
	 */
	public void addExtensionListener(TraceModelExtensionListener listener) {
		extensionListeners.add(listener);
	}

	/**
	 * Removes a trace model extension listener. Does nothing if the listener is
	 * not found
	 * 
	 * @param listener
	 *            the listener to be removed
	 */
	public void removeExtensionListener(TraceModelExtensionListener listener) {
		extensionListeners.remove(listener);
	}

	/**
	 * Adds a new trace model reset listener to this model
	 * 
	 * @param listener
	 *            the new listener
	 */
	public void addResetListener(TraceModelResetListener listener) {
		resetListeners.add(listener);
	}

	/**
	 * Removes a trace model reset listener. Does nothing if the listener is not
	 * found
	 * 
	 * @param listener
	 *            the listener to be removed
	 */
	public void removeResetListener(TraceModelResetListener listener) {
		resetListeners.remove(listener);
	}

	/**
	 * Adds a new trace model listener to this model
	 * 
	 * @param listener
	 *            the new listener
	 */
	public void addProcessingListener(TraceProcessingListener listener) {
		processingListeners.add(listener);
	}

	/**
	 * Removes a processing listener. Does nothing if the listener is not found
	 * 
	 * @param listener
	 *            the listener to be removed
	 */
	public void removeProcessingListener(TraceProcessingListener listener) {
		processingListeners.remove(listener);
	}

	/**
	 * Returns highest group ID + 1. Can be used to create an unique ID for a
	 * new trace group.
	 * 
	 * @return the next trace group ID
	 * @throws TraceBuilderException
	 */
	public int getNextGroupID() throws TraceBuilderException {
		// Get current max group id in model
		int maxGroupIdInModel = 0;
		for (TraceGroup group : groups) {
			int groupIdInModel = group.getID();
			if (groupIdInModel > maxGroupIdInModel) {
				maxGroupIdInModel = groupIdInModel;
			}
		}
		int nextGroupId = maxGroupIdInModel + 1;

		return nextGroupId;
	}

	/**
	 * Returns highest constant table ID + 1. Can be used to create an unique ID
	 * for a new constant table.
	 * 
	 * @return the next constant table ID
	 */
	public int getNextConstantTableID() {
		int max = 0;
		for (TraceConstantTable table : constantTables) {
			int id = table.getID();
			if (id > max) {
				max = id;
			}
		}
		return max + 1;
	}

	/**
	 * Removes a trace group from this model. Create groupRemoved event to model
	 * listeners
	 * 
	 * @param group
	 *            the group to be removed
	 */
	public void removeGroup(TraceGroup group) {
		if (groups.remove(group)) {
			notifyObjectRemoved(this, group);
			group.reset();
			if (!isPredefinedGroup(group.getName())) {
				decreaseNumberOfUserDefinedGroups();
			}
		}
	}

	/**
	 * Determines if this model contains any trace groups
	 * 
	 * @return true if there are trace groups
	 */
	public boolean hasGroups() {
		return !groups.isEmpty();
	}

	/**
	 * Gets the number of trace groups
	 * 
	 * @return trace group count
	 */
	public int getGroupCount() {
		return groups.size();
	}

	/**
	 * Returns the trace groups of this model
	 * 
	 * @return the iterator over the groups
	 */
	public Iterator<TraceGroup> getGroups() {
		return groups.iterator();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Iterable#iterator()
	 */
	public Iterator<TraceGroup> iterator() {
		return groups.iterator();
	}

	/**
	 * Removes all trace groups and parameters from this model. Extensions are
	 * not removed. Notifies the reset listeners with modelResetting and
	 * modelReset
	 * 
	 * @see TraceModelResetListener#modelResetting
	 * @see TraceModelResetListener#modelReset
	 */
	@Override
	public void reset() {
		notifyModelResetting();
		// Properties are removed, other extensions are left
		removeExtensions(TraceObjectPropertyList.class);
		groups.clear();
		tracesByName.clear();
		constantTables.clear();
		numberOfUserDefinedGroups = 0;
		super.reset();
		notifyModelReset();
	}

	/**
	 * Gets the group which has given ID
	 * 
	 * @param id
	 *            the id
	 * @return group or null
	 */
	public TraceGroup findGroupByID(int id) {
		TraceGroup retval = null;
		for (TraceGroup group : groups) {
			if (group.getID() == id) {
				retval = group;
				break;
			}
		}
		return retval;
	}

	/**
	 * Locates a trace group which has the given name.
	 * 
	 * @param name
	 *            the name of the trace group
	 * @return the group or null if not found
	 * @see TraceObject#getName
	 */
	public TraceGroup findGroupByName(String name) {
		TraceGroup retval = null;
		for (TraceGroup group : groups) {
			if (group.getName().equals(name)) {
				retval = group;
				break;
			}
		}
		return retval;
	}

	/**
	 * Locates a trace which has the given name.
	 * 
	 * @param name
	 *            the name of the trace
	 * @return the trace or null if not found
	 * @see TraceObject#getName
	 */
	public Trace findTraceByName(String name) {
		Trace retval;
		int index = Collections.binarySearch(tracesByName, name,
				TraceObjectUtils.traceToNameComparator);
		if (index >= 0) {
			retval = tracesByName.get(index);
		} else {
			retval = null;
		}
		return retval;
	}

	/**
	 * Returns the group at given index
	 * 
	 * @param index
	 *            the group index
	 * @return the group
	 */
	public TraceGroup getGroupAt(int index) {
		return groups.get(index);
	}

	/**
	 * Removes a constant table from this model. Creates objectRemoved event to
	 * model listeners
	 * 
	 * @see TraceModelListener#objectRemoved(TraceObject, TraceObject)
	 * @param table
	 *            the table to be removed
	 */
	public void removeConstantTable(TraceConstantTable table) {
		if (constantTables.remove(table)) {
			notifyObjectRemoved(this, table);
			table.reset();
		}
	}

	/**
	 * Gets the constant tables of this model
	 * 
	 * @return the tables iterator
	 */
	public Iterator<TraceConstantTable> getConstantTables() {
		return constantTables.iterator();
	}

	/**
	 * Gets a constant table by ID
	 * 
	 * @param id
	 *            the ID
	 * @return the table or null
	 */
	public TraceConstantTable findConstantTableByID(int id) {
		TraceConstantTable retval = null;
		for (TraceConstantTable table : constantTables) {
			if (table.getID() == id) {
				retval = table;
				break;
			}
		}
		return retval;
	}

	/**
	 * Gets a constant table by name
	 * 
	 * @param tableName
	 *            the name
	 * @return the table or null
	 */
	public TraceConstantTable findConstantTableByName(String tableName) {
		TraceConstantTable retval = null;
		for (TraceConstantTable table : constantTables) {
			if (table.getName().equals(tableName)) {
				retval = table;
				break;
			}
		}
		return retval;
	}

	/**
	 * Checks if this model contains constant tables
	 * 
	 * @return true if there are constant tables
	 */
	public boolean hasConstantTables() {
		return !constantTables.isEmpty();
	}

	/**
	 * Returns the constant table at given index
	 * 
	 * @param index
	 *            the group index
	 * @return the group
	 */
	public TraceConstantTable getConstantTableAt(int index) {
		return constantTables.get(index);
	}

	/**
	 * Adds a new trace group to this model. Creates objectAdded event to model
	 * listeners. This is only intended to be called from TraceGroup
	 * constructor, so this is not public.
	 * 
	 * @see TraceModelListener#objectAdded(TraceObject, TraceObject)
	 * @param group
	 *            the group to be added
	 */
	void addGroup(TraceGroup group) {
		groups.add(group);
		notifyObjectAdded(this, group);
	}

	/**
	 * Adds a constant table to this model. Created objectAdded event to model
	 * listeners. This is only intended to be called from constant table
	 * constructor, so this is not public
	 * 
	 * @see TraceModelListener#objectAdded(TraceObject, TraceObject)
	 * @param table
	 *            the constant table
	 */
	void addConstantTable(TraceConstantTable table) {
		constantTables.add(table);
		notifyObjectAdded(this, table);
	}

	/**
	 * Fires propertiesUpdated event. Called from trace objects when their
	 * properties change.
	 * 
	 * @see TraceModelListener#propertyUpdated(TraceObject, int)
	 * @param source
	 *            the object that changed
	 * @param property
	 *            the property that changed
	 */
	void notifyPropertyUpdated(TraceObject source, int property) {
		if (source instanceof Trace) {
			if (property == TraceModelListener.NAME) {
				Collections.sort(tracesByName,
						TraceObjectUtils.traceObjectNameComparator);
			}
			((Trace) source).getGroup().tracePropertyUpdated(source, property);
		}
		for (TraceModelListener l : modelListeners) {
			l.propertyUpdated(source, property);
		}
		modelChangedDuringProcessing = true;
	}

	/**
	 * Fires objectAdded event to listeners
	 * 
	 * @see TraceModelListener#objectAdded(TraceObject, TraceObject)
	 * @param owner
	 *            the owner object
	 * @param object
	 *            the object that was added
	 */
	void notifyObjectAdded(TraceObject owner, TraceObject object) {
		// List is sorted when the name is set to the trace
		if (object instanceof Trace) {
			tracesByName.add((Trace) object);
		}
		for (TraceModelListener l : modelListeners) {
			l.objectAdded(owner, object);
		}
		modelChangedDuringProcessing = true;
	}

	/**
	 * Fires objectRemoved event to listeners
	 * 
	 * @see TraceModelListener#objectRemoved(TraceObject, TraceObject)
	 * @param owner
	 *            the owner object
	 * @param object
	 *            the object that was removed
	 */
	void notifyObjectRemoved(TraceObject owner, TraceObject object) {
		if (object instanceof Trace) {
			int index = Collections.binarySearch(tracesByName, (Trace) object,
					TraceObjectUtils.traceObjectNameComparator);
			tracesByName.remove(index);
		}
		notifyOnDelete(object);
		for (TraceModelListener l : modelListeners) {
			l.objectRemoved(owner, object);
		}
		modelChangedDuringProcessing = true;
	}

	/**
	 * Notifies that an object creation is complete
	 * 
	 * @see TraceModelListener#objectCreationComplete(TraceObject)
	 * @param object
	 *            the object
	 */
	void notifyObjectCreationComplete(TraceObject object) {
		for (TraceModelListener l : modelListeners) {
			l.objectCreationComplete(object);
		}
	}

	/**
	 * Fires modelResetting event to all listeners
	 * 
	 * @see TraceModelResetListener#modelResetting()
	 */
	private void notifyModelResetting() {
		for (TraceModelResetListener l : resetListeners) {
			l.modelResetting();
		}
	}

	/**
	 * Fires modelReset event to all listeners
	 * 
	 * @see TraceModelResetListener#modelReset()
	 */
	private void notifyModelReset() {
		for (TraceModelResetListener l : resetListeners) {
			l.modelReset();
		}
	}

	/**
	 * Fires extensionAdded event. Called from TraceObject when extension is
	 * added to it
	 * 
	 * @see TraceModelExtensionListener#extensionAdded(TraceObject,
	 *      TraceModelExtension)
	 * @param object
	 *            the trace object
	 * @param extension
	 *            the new extension
	 */
	void notifyExtensionAdded(TraceObject object, TraceModelExtension extension) {
		for (TraceModelExtensionListener l : extensionListeners) {
			l.extensionAdded(object, extension);
		}
		modelChangedDuringProcessing = true;
	}

	/**
	 * Fires extensionRemoved event. Called from TraceObject when extension is
	 * removed from it
	 * 
	 * @see TraceModelExtensionListener#extensionRemoved(TraceObject,
	 *      TraceModelExtension)
	 * @param object
	 *            the object
	 * @param extension
	 *            the removed extension
	 */
	void notifyExtensionRemoved(TraceObject object,
			TraceModelExtension extension) {
		for (TraceModelExtensionListener l : extensionListeners) {
			l.extensionRemoved(object, extension);
		}
		modelChangedDuringProcessing = true;
	}

	/**
	 * Gets the validity flag of this model
	 * 
	 * @return the validity flag
	 */
	public boolean isValid() {
		return valid;
	}

	/**
	 * Sets the validity flag
	 * 
	 * @param valid
	 *            new flag value
	 */
	public void setValid(boolean valid) {
		if (valid != this.valid) {
			this.valid = valid;
			for (TraceModelResetListener l : resetListeners) {
				l.modelValid(valid);
			}
		}
	}

	/**
	 * Gets the trace object factory
	 * 
	 * @return the factory
	 */
	public TraceObjectFactory getFactory() {
		return factory;
	}

	/**
	 * Gets the object verifier interface. The verifier should be used before
	 * updating object properties
	 * 
	 * @return the verifier
	 */
	public TraceObjectPropertyVerifier getVerifier() {
		return verifier;
	}

	/**
	 * Notifies the process listeners that a process the results in multiple
	 * listener updates is about to start
	 */
	public void startProcessing() {
		isProcessing++;
		if (isProcessing == 1) {
			modelChangedDuringProcessing = false;
			for (TraceProcessingListener l : processingListeners) {
				l.processingStarted();
			}
		}
	}

	/**
	 * Notifies the process listeners that a process the results in multiple
	 * listener updates has finished
	 */
	public void processingComplete() {
		isProcessing--;
		if (isProcessing == 0) {
			for (TraceProcessingListener l : processingListeners) {
				l.processingComplete(modelChangedDuringProcessing);
			}
			modelChangedDuringProcessing = false;
		}
	}

	/**
	 * Checks the state of the processing flag
	 * 
	 * @return the processing flag
	 */
	public boolean isProcessing() {
		return isProcessing > 0;
	}

	/**
	 * Checks if model has traces
	 * 
	 * @return true if there's traces, false if not
	 */
	public boolean hasTraces() {
		boolean retval = false;
		for (TraceGroup group : groups) {
			if (group.hasTraces()) {
				retval = true;
				break;
			}
		}
		return retval;
	}

	/**
	 * Gets group ID from properties
	 * 
	 * @param properties
	 *            the properties
	 * @param group
	 *            the group
	 * @return the group ID
	 * @throws TraceBuilderException
	 */
	public int getGroupID(Properties properties, TraceGroup group)
			throws TraceBuilderException {
		String value = properties.getProperty(GROUP_PROPERTY_PREFIX
				+ group.getName());
		int id;
		if (value == null) {
			// Not found, assign a proper ID
			id = getNewIdForGroup(group);

		} else {
			try {
				id = Integer.decode(value).intValue();
				TraceGroup traceGroup = findGroupByID(id);
				if (traceGroup != null && !traceGroup.equals(group)) {
					// Id already in use, assign a proper ID
					id = getNewIdForGroup(group);
				}
			} catch (NumberFormatException e) {
				// Corrupted, assign a proper ID
				id = getNewIdForGroup(group);
			}
		}
		group.internalSetID(id);
		return id;
	}

	/**
	 * Gets a new ID for this group
	 * 
	 * @param group
	 *            the group
	 * @return a new ID for this group
	 * @throws TraceBuilderException
	 */
	private int getNewIdForGroup(TraceGroup group) throws TraceBuilderException {
		int id = FormattingUtils.getGroupID(group.getModel(), group.getName());
		return id;
	}

	/**
	 * Saves trace and group identifiers to given properties.
	 * 
	 * @param properties
	 *            the properties to use
	 */
	public void saveIDs(Properties properties) {
		properties.clear();
		StringBuffer sb = new StringBuffer();
		for (TraceGroup group : this) {
			int groupId = group.getID();
			properties.setProperty(GROUP_PROPERTY_PREFIX + group.getName(),
					SourceConstants.HEX_PREFIX + Integer.toHexString(groupId));
			for (Trace trace : group) {
				int traceId = trace.getID();
				properties.setProperty(TRACE_PROPERTY_PREFIX
						+ createTraceName(sb, group, trace),
						SourceConstants.HEX_PREFIX
								+ Integer.toHexString(traceId));
			}
		}
	}

	/**
	 * Merges the group and trace names together
	 * 
	 * @param sb
	 *            the buffer where name is stored
	 * @param group
	 *            group
	 * @param trace
	 *            trace
	 * @return the trace name
	 */
	public String createTraceName(StringBuffer sb, TraceGroup group, Trace trace) {
		sb.setLength(0);
		sb.append(group.getName());
		sb.append(GROUP_ID_PREFIX);
		int groupId = group.getID();
		String groupIdString = SourceConstants.HEX_PREFIX
				+ Integer.toString(groupId, HEX_RADIX).toUpperCase();
		sb.append(groupIdString);
		sb.append(GROUP_ID_SUFFIX);
		sb.append(SourceConstants.UNDERSCORE);
		sb.append(trace.getName());
		return sb.toString();
	}

	/**
	 * 
	 * Get number of user defined groups
	 * 
	 * @return the number of user defined groups
	 */
	public int getNumberOfUserdefinedGroups() {
		return numberOfUserDefinedGroups;
	}
	
	/**
	 * 
	 * Increase number of user defined groups
	 */
	public void increaseNumberOfUserDefinedGroups() {
		numberOfUserDefinedGroups++;
	}
	
	/**
	 * 
	 * Decrease number of user defined groups
	 */
	public void decreaseNumberOfUserDefinedGroups() {
		numberOfUserDefinedGroups--;
	}

	/**
	 * Check is group predefined group
	 * @param name of the group
	 * @return true if group is predefined group, otherwise false
	 */
	public boolean isPredefinedGroup(String name) {
		boolean isPredefinedGroup = false;
		GroupNameHandlerBase groupNameHandler = TraceBuilderGlobals.getGroupNameHandler();
		String[] defaultGroups = groupNameHandler.getDefaultGroups();
		for (int i = 0; i < defaultGroups.length; i++) {
			if (name.equals(defaultGroups[i])) {
				isPredefinedGroup = true;
				break;
			}
		}
		return isPredefinedGroup;
	}	
}
