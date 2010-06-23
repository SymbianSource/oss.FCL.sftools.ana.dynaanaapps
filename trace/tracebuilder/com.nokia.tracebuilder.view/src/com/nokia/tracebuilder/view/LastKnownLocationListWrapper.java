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
* Wrapper class for last known location list
*
*/
package com.nokia.tracebuilder.view;

import java.util.Iterator;

import com.nokia.tracebuilder.engine.LocationProperties;
import com.nokia.tracebuilder.engine.LastKnownLocation;
import com.nokia.tracebuilder.engine.LastKnownLocationList;
import com.nokia.tracebuilder.engine.LastKnownLocationListListener;

/**
 * Wrapper class for last known location list
 * 
 */
final class LastKnownLocationListWrapper extends ListWrapper implements
		LastKnownLocationListListener {

	/**
	 * Location list
	 */
	private LastKnownLocationList locationList;

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
	LastKnownLocationListWrapper(LastKnownLocationList list,
			WrapperBase parent, WrapperUpdater updater) {
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
			LastKnownLocationWrapper wrapper = new LastKnownLocationWrapper(
					(LastKnownLocation) loc, this, getUpdater());
			add(wrapper);
		}
	}

	/**
	 * Gets the location list
	 * 
	 * @return the location list
	 */
	LastKnownLocationList getLocationList() {
		return locationList;
	}

	/**
	 * Updates the location list
	 * 
	 * @param list
	 *            the new location list
	 */
	void updateLocationList(LastKnownLocationList list) {
		locationList.removeLocationListListener(this);
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
		locationList.removeLocationListListener(this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.engine.LastKnownLocationListListener#
	 *      locationAdded(com.nokia.tracebuilder.engine.LastKnownLocation)
	 */
	public void locationAdded(LastKnownLocation location) {
		LastKnownLocationWrapper wrapper = new LastKnownLocationWrapper(
				location, this, getUpdater());
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
			TraceWrapper parent = (TraceWrapper) getParent();
			parent.showLastKnownLocationList();
			getUpdater().queueUpdate(parent);
		} else {
			getUpdater().queueUpdate(this);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.engine.LastKnownLocationListListener#
	 *      locationRemoved(com.nokia.tracebuilder.engine.LastKnownLocation)
	 */
	public void locationRemoved(LastKnownLocation location) {
		LastKnownLocationWrapper wrapper = (LastKnownLocationWrapper) location
				.getViewReference();
		remove(wrapper);
		if (!hasChildren()) {
			TraceWrapper parent = (TraceWrapper) getParent();
			parent.hideLastKnownLocationList();
			getUpdater().queueUpdate(parent);
		} else {
			getUpdater().queueUpdate(this);
		}
	}

}
