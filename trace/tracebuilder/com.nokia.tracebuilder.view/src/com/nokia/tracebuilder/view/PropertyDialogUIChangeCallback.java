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
* Callback from property dialog UI
*
*/
package com.nokia.tracebuilder.view;

import com.nokia.tracebuilder.engine.TraceObjectPropertyDialogTemplate;

/**
 * Callback from property dialog UI
 * 
 */
interface PropertyDialogUIChangeCallback {

	/**
	 * Called when template changes
	 * 
	 * @param template
	 *            the template
	 */
	void templateChanged(TraceObjectPropertyDialogTemplate template);

	/**
	 * Target change notification
	 * 
	 * @param target
	 *            new target
	 */
	void targetChanged(String target);

	/**
	 * Called when a text field changes
	 */
	void fieldChanged();

	/**
	 * Called when parameter type changes
	 * 
	 * @param type
	 *            the new type
	 */
	void typeChanged(String type);

}