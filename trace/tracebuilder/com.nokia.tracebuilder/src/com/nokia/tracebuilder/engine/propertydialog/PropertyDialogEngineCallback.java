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
* Control logic for property dialog
*
*/
package com.nokia.tracebuilder.engine.propertydialog;

import com.nokia.tracebuilder.engine.TraceObjectPropertyDialog;
import com.nokia.tracebuilder.model.TraceBuilderException;

/**
 * Callback for property dialog engine
 * 
 */
interface PropertyDialogEngineCallback {

	/**
	 * Called when OK is selected from the dialog. If this throws an exception,
	 * an error message is shown to user and the dialog stays open
	 * 
	 * @param dialog
	 *            the property dialog properties
	 * @throws TraceBuilderException
	 *             if OK processing fails
	 */
	void okSelected(TraceObjectPropertyDialog dialog)
			throws TraceBuilderException;
}