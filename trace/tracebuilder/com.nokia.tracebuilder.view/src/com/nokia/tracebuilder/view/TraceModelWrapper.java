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
* Wrapper for the TraceModel object
*
*/
package com.nokia.tracebuilder.view;

import java.util.Iterator;

import com.nokia.tracebuilder.engine.TraceLocationList;
import com.nokia.tracebuilder.model.TraceConstantTable;
import com.nokia.tracebuilder.model.TraceGroup;
import com.nokia.tracebuilder.model.TraceModel;

/**
 * Wrapper for the TraceModel object
 * 
 */
final class TraceModelWrapper extends TraceObjectWrapper {

	/**
	 * Trace groups list
	 */
	private TraceGroupListWrapper traceGroupListWrapper;

	/**
	 * Constant tables list
	 */
	private ConstantTableListWrapper traceConstantTableListWrapper;

	/**
	 * List of location lists
	 */
	private TraceLocationListsWrapper traceLocationListsWrapper;

	/**
	 * Constructor
	 * 
	 * @param model
	 *            the trace model
	 * @param parent
	 *            parent wrapper
	 * @param updater
	 *            the update notifier
	 */
	TraceModelWrapper(TraceModel model, WrapperBase parent,
			WrapperUpdater updater) {
		super(model, parent, updater);
		addGroups();
		addTables();
		addLocationLists();
	}

	/**
	 * Adds a new trace group
	 * 
	 * @param group
	 *            the trace group
	 * @return the wrapper which needs to be refreshed
	 */
	WrapperBase addGroup(TraceGroup group) {
		WrapperBase wrapper;
		if (traceGroupListWrapper.hasChildren()) {
			wrapper = traceGroupListWrapper.addGroup(group);
		} else {
			traceGroupListWrapper.addGroup(group);
			add(traceGroupListWrapper);
			wrapper = this;
		}
		return wrapper;
	}

	/**
	 * Removes a trace group
	 * 
	 * @param group
	 *            the group to be removed
	 * @return the wrapper which needs to be refreshed
	 */
	WrapperBase removeGroup(TraceGroup group) {
		WrapperBase wrapper = traceGroupListWrapper.removeGroup(group);
		if (!traceGroupListWrapper.hasChildren()) {
			hide(traceGroupListWrapper);
			wrapper = this;
		}
		return wrapper;
	}

	/**
	 * Adds a new constant table
	 * 
	 * @param table
	 *            the constant table
	 * @return the wrapper which needs to be refreshed
	 */
	WrapperBase addConstantTable(TraceConstantTable table) {
		WrapperBase wrapper;
		if (traceConstantTableListWrapper.hasChildren()) {
			wrapper = traceConstantTableListWrapper.addConstantTable(table);
		} else {
			traceConstantTableListWrapper.addConstantTable(table);
			add(traceConstantTableListWrapper);
			wrapper = this;
		}
		return wrapper;
	}

	/**
	 * Removes a constant table
	 * 
	 * @param table
	 *            the constant table to be removed
	 * @return the wrapper which needs to be refreshed
	 */
	WrapperBase removeConstantTable(TraceConstantTable table) {
		WrapperBase wrapper = traceConstantTableListWrapper
				.removeConstantTable(table);
		if (!traceConstantTableListWrapper.hasChildren()) {
			hide(traceConstantTableListWrapper);
			wrapper = this;
		}
		return wrapper;
	}

	/**
	 * Adds a location list
	 * 
	 * @param list
	 *            the location list
	 * @return the wrapper to be updated
	 */
	WrapperBase addLocationList(TraceLocationList list) {
		WrapperBase retval = traceLocationListsWrapper.addLocationList(list);
		if (!contains(traceLocationListsWrapper) && list.hasLocations()) {
			add(traceLocationListsWrapper);
			retval = this;
		}
		return retval;
	}

	/**
	 * Removes a location list
	 * 
	 * @param list
	 *            the list to be removed
	 * @return the wrapper to be updated
	 */
	WrapperBase removeLocationList(TraceLocationList list) {
		WrapperBase retval = traceLocationListsWrapper.removeLocationList(list);
		if (!hasEntries((TraceModel) getTraceObject())) {
			hide(traceLocationListsWrapper);
			retval = this;
		}
		return retval;
	}

	/**
	 * Called when model is reset
	 */
	void modelReset() {
		// Locations lists may be hidden
		if (!contains(traceLocationListsWrapper)) {
			traceLocationListsWrapper.delete();
		}
		clear();
		// New list objects need to be created since clear deletes them
		addGroups();
		addTables();
		addExtensions();
		addProperties();
		addLocationLists();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.view.TraceObjectWrapper#delete()
	 */
	@Override
	void delete() {
		if (!contains(traceLocationListsWrapper)) {
			traceLocationListsWrapper.delete();
		}
		if (!contains(traceConstantTableListWrapper)) {
			traceConstantTableListWrapper.delete();
		}
		if (!contains(traceGroupListWrapper)) {
			traceGroupListWrapper.delete();
		}
		super.delete();
	}

	/**
	 * Adds the trace groups list
	 */
	private void addGroups() {
		TraceModel model = (TraceModel) getTraceObject();
		traceGroupListWrapper = new TraceGroupListWrapper(model, this,
				getUpdater());
		if (model.hasGroups()) {
			add(traceGroupListWrapper);
		}
	}

	/**
	 * Adds the constant tables list
	 */
	private void addTables() {
		TraceModel model = (TraceModel) getTraceObject();
		traceConstantTableListWrapper = new ConstantTableListWrapper(model,
				this, getUpdater());
		if (model.hasConstantTables()) {
			add(traceConstantTableListWrapper);
		}
	}

	/**
	 * Adds the locations lists list
	 */
	private void addLocationLists() {
		TraceModel model = (TraceModel) getTraceObject();
		traceLocationListsWrapper = new TraceLocationListsWrapper(model, this,
				getUpdater());
		if (hasEntries(model)) {
			add(traceLocationListsWrapper);
		}
	}

	/**
	 * Checks if the sub-lists have entries
	 * 
	 * @param model
	 *            the model
	 * @return true if there are entries, false if not
	 */
	private boolean hasEntries(TraceModel model) {
		Iterator<TraceLocationList> itr = model
				.getExtensions(TraceLocationList.class);
		boolean hasEntries = false;
		while (itr.hasNext() && !hasEntries) {
			hasEntries = itr.next().hasLocations();
		}
		return hasEntries;
	}

}
