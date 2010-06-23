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
* List of last known locations
*
*/
package com.nokia.tracebuilder.engine;

import java.util.ArrayList;

/**
 * List of last known  locations
 * 
 */
public final class LastKnownLocationList extends LocationListBase {

	/**
	 * Location list listeners
	 */
	private ArrayList<LastKnownLocationListListener> listeners = new ArrayList<LastKnownLocationListListener>();

	/**
	 * Adds a location to last known  locations list
	 * 
	 * @param loc
	 *            the location
	 */
	public void addLocation(LastKnownLocation loc) {
		add(loc);
		loc.setLocationList(this);
		fireLocationAdded(loc);
	}

	/**
	 * Adds a location list listener to this object
	 * 
	 * @param listener
	 *            the listener interface
	 */
	public void addLocationListListener(LastKnownLocationListListener listener) {
		listeners.add(listener);
	}

	/**
	 * Removes the location list listener
	 * 
	 * @param listener
	 *            the listener interface
	 */
	public void removeLocationListListener(
			LastKnownLocationListListener listener) {
		listeners.remove(listener);
	}

	/**
	 * Creates locationAdded event to location list listeners
	 * 
	 * @param location
	 *            the location that was added
	 */
	private void fireLocationAdded(LastKnownLocation location) {
		for (LastKnownLocationListListener l : listeners) {
			l.locationAdded(location);
		}
	}

	/**
	 * Creates locationRemoved event to location list listeners
	 * 
	 * @param location
	 *            the location that was added
	 */
	void fireLocationRemoved(LastKnownLocation location) {
		for (LastKnownLocationListListener l : listeners) {
			l.locationRemoved(location);
		}
	}

}