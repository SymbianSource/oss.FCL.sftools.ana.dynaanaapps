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
* List of location lists
*
*/
package com.nokia.tracebuilder.view;

import java.util.ArrayList;
import java.util.Iterator;

import com.nokia.tracebuilder.engine.TraceLocationList;
import com.nokia.tracebuilder.model.TraceModel;

/**
 * List of location lists
 * 
 */
final class TraceLocationListsWrapper extends ListWrapper {

	/**
	 * Location lists that are not visible
	 */
	private ArrayList<TraceLocationListWrapper> hiddenWrappers = new ArrayList<TraceLocationListWrapper>();

	/**
	 * Constructor
	 * 
	 * @param model
	 *            the trace model
	 * @param parent
	 *            the parent wrapper
	 * @param updater
	 *            the updater
	 */
	TraceLocationListsWrapper(TraceModel model, WrapperBase parent,
			WrapperUpdater updater) {
		super(parent, updater);
		Iterator<TraceLocationList> itr = model
				.getExtensions(TraceLocationList.class);
		while (itr.hasNext()) {
			addLocationList(itr.next());
		}
	}

	/**
	 * Adds a location list to this wrapper
	 * 
	 * @param list
	 *            the list
	 * @return the wrapper to be updated
	 */
	WrapperBase addLocationList(TraceLocationList list) {
		TraceLocationListWrapper wrapper = new TraceLocationListWrapper(list,
				this, getUpdater());
		if (list.hasLocations()) {
			add(wrapper);
		} else {
			hiddenWrappers.add(wrapper);
		}
		return this;
	}

	/**
	 * Removes a location list from this wrapper
	 * 
	 * @param list
	 *            the list to be removed
	 * @return the wrapper to be updated
	 */
	WrapperBase removeLocationList(TraceLocationList list) {
		Iterator<WrapperBase> itr = getVisibleWrappers();
		WrapperBase found = null;
		while (itr.hasNext() && found == null) {
			TraceLocationListWrapper wrapper = (TraceLocationListWrapper) itr
					.next();
			if (wrapper.getLocationList() == list) {
				found = wrapper;
			}
		}
		if (found == null) {
			for (TraceLocationListWrapper wrapper : hiddenWrappers) {
				if (wrapper.getLocationList() == list) {
					found = wrapper;
					break;
				}
			}
		}
		if (found != null) {
			remove(found);
		}
		return this;
	}

	/**
	 * Shows the given location list wrapper
	 * 
	 * @param wrapper
	 *            the wrapper to be shown
	 * @return the wrapper to be updated
	 */
	WrapperBase showLocationList(TraceLocationListWrapper wrapper) {
		hiddenWrappers.remove(wrapper);
		add(wrapper);
		WrapperBase retval;
		TraceModelWrapper parent = (TraceModelWrapper) getParent();
		if (!parent.contains(this)) {
			parent.add(this);
			retval = parent;
		} else {
			retval = this;
		}
		return retval;
	}

	/**
	 * Hides the given location list wrapper
	 * 
	 * @param wrapper
	 *            the wrapper to be hidden
	 * @return the wrapper to be updated
	 */
	WrapperBase hideLocationList(TraceLocationListWrapper wrapper) {
		hiddenWrappers.add(wrapper);
		hide(wrapper);
		WrapperBase retval;
		TraceModelWrapper parent = (TraceModelWrapper) getParent();
		if (!hasChildren()) {
			parent.hide(this);
			retval = parent;
		} else {
			retval = this;
		}
		return retval;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.view.TraceObjectWrapper#delete()
	 */
	@Override
	void delete() {
		Iterator<TraceLocationListWrapper> itr = hiddenWrappers.iterator();
		while (itr.hasNext()) {
			itr.next().delete();
		}
		super.delete();
	}

}
