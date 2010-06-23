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
* Extension that can notify listeners about updates
*
*/
package com.nokia.tracebuilder.model;

/**
 * Extension that can notify listeners about updates
 * 
 */
public interface TraceModelUpdatableExtension extends TraceModelExtension {

	/**
	 * Adds an update listener to this extension
	 * 
	 * @param listener
	 *            the listener
	 */
	public void addUpdateListener(TraceModelExtensionUpdateListener listener);

	/**
	 * Removes an update listener from this extension
	 * 
	 * @param listener
	 *            the listener
	 */
	public void removeUpdateListener(TraceModelExtensionUpdateListener listener);

}
