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

/**
 * Extends the property dialog flag interface by providing means to receive
 * template change notifications from the UI
 * 
 */
public interface TraceObjectPropertyDialogDynamicFlag extends
		TraceObjectPropertyDialogFlag {

	/**
	 * Called by the UI when user changes the template
	 * 
	 * @param template
	 *            the new template
	 * @return true if flag changed, false if not
	 */
	public boolean templateChanged(TraceObjectPropertyDialogTemplate template);

	/**
	 * Called by the UI when user changes a property flag
	 * 
	 * @param flag
	 *            the flag that was changed
	 * @return true if flag changed, false if not
	 */
	public boolean flagChanged(TraceObjectPropertyDialogFlag flag);

	/**
	 * Queried by the UI after template has changed. If this returns false, the
	 * UI checkbox is disabled and the flag value is set to isAlwaysEnabled
	 * 
	 * @return availability flag
	 */
	public boolean isAvailable();

	/**
	 * If this returns true, the flag value is set even if the checkbox is
	 * disabled
	 * 
	 * @return the always enabled flag
	 */
	public boolean isAlwaysEnabled();

}
