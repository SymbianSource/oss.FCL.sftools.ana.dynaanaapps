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
* Wrapper for a last known location
*
*/
package com.nokia.tracebuilder.view;

import com.nokia.tracebuilder.engine.LastKnownLocation;

/**
 * Wrapper for a last known location
 * 
 */
final class LastKnownLocationWrapper extends WrapperBase {

	/**
	 * The location
	 */
	private LastKnownLocation location;

	/**
	 * Constructor
	 * 
	 * @param location
	 *            the trace location
	 * @param parent
	 *            the parent wrapper
	 * @param updater
	 *            the update notifier
	 */
	LastKnownLocationWrapper(LastKnownLocation location, WrapperBase parent,
			WrapperUpdater updater) {
		super(parent, updater);
		this.location = location;
		location.setViewReference(this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.view.WrapperBase#getChildren()
	 */
	@Override
	Object[] getChildren() {
		return new Object[0];
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.view.WrapperBase#hasChildren()
	 */
	@Override
	boolean hasChildren() {
		return false;
	}

	/**
	 * Gets the location
	 * 
	 * @return location
	 */
	LastKnownLocation getLocation() {
		return location;
	}

}
