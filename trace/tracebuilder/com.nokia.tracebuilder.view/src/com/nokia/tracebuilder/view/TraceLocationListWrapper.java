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
* Wrapper for TraceLocationList
*
*/
package com.nokia.tracebuilder.view;

import java.util.Iterator;

import com.nokia.tracebuilder.engine.LocationProperties;
import com.nokia.tracebuilder.engine.TraceLocation;
import com.nokia.tracebuilder.engine.TraceLocationList;
import com.nokia.tracebuilder.engine.TraceLocationListListener;

/**
 * Wrapper for TraceLocationList
 * 
 */
final class TraceLocationListWrapper extends ListWrapper implements
		TraceLocationListListener {

	/**
	 * Location list
	 */
	private TraceLocationList locationList;

	/**
	 * Constructor
	 * 
	 * @param list
	 *            the location list
	 * @param parent
	 *            the parent wrapper
	 * @param updater
	 *            the update notifier
	 */
	TraceLocationListWrapper(TraceLocationList list, WrapperBase parent,
			WrapperUpdater updater) {
		super(parent, updater);
		this.locationList = list;
		createChildren();
		locationList.addLocationListListener(this);
	}

	/**
	 * Creates the location wrappers
	 */
	private void createChildren() {
		for (LocationProperties loc : locationList) {
			TraceLocationWrapper wrapper = new TraceLocationWrapper(
					(TraceLocation) loc, this, getUpdater());
			add(wrapper);
		}
	}

	/**
	 * Gets the location list
	 * 
	 * @return the location list
	 */
	TraceLocationList getLocationList() {
		return locationList;
	}

	/**
	 * Updates the location list
	 * 
	 * @param list
	 *            the new location list
	 */
	void updateLocationList(TraceLocationList list) {
		if (locationList != null) {
			locationList.removeLocationListListener(this);
		}
		clear();
		locationList = list;
		if (locationList != null) {
			locationList.addLocationListListener(this);
			createChildren();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.view.ListWrapper#delete()
	 */
	@Override
	void delete() {
		super.delete();
		if (locationList != null) {
			locationList.removeLocationListListener(this);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.engine.TraceLocationListListener#
	 *      locationAdded(com.nokia.tracebuilder.engine.TraceLocation)
	 */
	public void locationAdded(TraceLocation location) {
		TraceLocationWrapper wrapper = new TraceLocationWrapper(location, this,
				getUpdater());
		boolean inView = false;
		boolean hidden = false;
		if (!hasChildren()) {
			hidden = true;
		}
		// If one of the existing locations is shown in view, or there are no
		// existing locations the new one is also marked as shown in view
		if (isInView()) {
			if (hasChildren()) {
				Iterator<WrapperBase> itr = getVisibleWrappers();
				if (itr.next().isInView()) {
					inView = true;
				}
			} else {
				inView = true;
			}
		}
		add(wrapper);
		wrapper.setInView(inView);
		if (hidden) {
			WrapperBase parent = getParent();
			if (parent instanceof TraceWrapper) {
				((TraceWrapper) parent).showLocationList();
				getUpdater().queueUpdate(parent);
			} else if (parent instanceof TraceLocationListsWrapper) {
				WrapperBase update = ((TraceLocationListsWrapper) parent)
						.showLocationList(this);
				getUpdater().queueUpdate(update);
			}
		} else {
			getUpdater().queueUpdate(this);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.engine.TraceLocationListListener#
	 *      locationRemoved(com.nokia.tracebuilder.engine.TraceLocation)
	 */
	public void locationRemoved(TraceLocation location) {
		TraceLocationWrapper wrapper = (TraceLocationWrapper) location
				.getProperties().getViewReference();
		remove(wrapper);
		// Forces a full validity check
		if (!hasChildren()) {
			WrapperBase parent = getParent();
			if (parent instanceof TraceWrapper) {
				((TraceWrapper) parent).hideLocationList();
				getUpdater().queueUpdate(parent);
			} else if (parent instanceof TraceLocationListsWrapper) {
				WrapperBase update = ((TraceLocationListsWrapper) parent)
						.hideLocationList(this);
				getUpdater().queueUpdate(update);
			}
		} else {
			getUpdater().queueUpdate(this);
		}
	}

}
