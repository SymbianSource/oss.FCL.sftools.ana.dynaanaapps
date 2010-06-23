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
* Interface to a flag shown in property dialog
*
*/
package com.nokia.tracebuilder.engine;

import java.util.List;

import com.nokia.tracebuilder.model.TraceModelExtension;

/**
 * Interface to a flag shown in property dialog
 * 
 */
public interface TraceObjectPropertyDialogFlag {

	/**
	 * Gets the flag state
	 * 
	 * @return the enabled state
	 */
	public boolean isEnabled();

	/**
	 * Sets the flag state
	 * 
	 * @param enabled
	 *            the flag state
	 */
	public void setEnabled(boolean enabled);

	/**
	 * Gets the text shown in UI
	 * 
	 * @return the text
	 */
	public String getText();

	/**
	 * Checks if this flag should be shown in UI
	 * 
	 * @return true if visible, false otherwise
	 */
	public boolean isVisible();

	/**
	 * Creates the extensions to be added to the trace
	 * 
	 * @param extList
	 *            the list where the extensions should be added
	 */
	public void createExtensions(List<TraceModelExtension> extList);

}
