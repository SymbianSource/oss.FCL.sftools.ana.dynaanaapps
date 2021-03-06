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
* Listener for last known locations list
*
*/
package com.nokia.tracebuilder.engine;

/**
 * Listener for last known locations list
 * 
 */
public interface LastKnownLocationListListener {

	/**
	 * Location was added to this list
	 * 
	 * @param location
	 *            the new location
	 */
	public void locationAdded(LastKnownLocation location);

	/**
	 * Location was removed from this list
	 * 
	 * @param location
	 *            the location that was removed
	 */
	public void locationRemoved(LastKnownLocation location);

}
