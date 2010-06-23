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
* Callback for add group property dialog
*
*/
package com.nokia.tracebuilder.engine.propertydialog;

import com.nokia.tracebuilder.engine.TraceBuilderGlobals;
import com.nokia.tracebuilder.engine.TraceObjectPropertyDialog;
import com.nokia.tracebuilder.model.TraceBuilderException;
import com.nokia.tracebuilder.model.TraceGroup;
import com.nokia.tracebuilder.model.TraceModel;
import com.nokia.tracebuilder.model.TraceModelExtension;

/**
 * Callback for add group property dialog
 * 
 */
final class CreateGroupCallback extends PropertyDialogCallback {

	/**
	 * Constructor
	 * 
	 * @param model
	 *            the trace model
	 */
	CreateGroupCallback(TraceModel model) {
		super(model);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.engine.PropertyDialogManagerCallback#
	 *      okSelected(com.nokia.tracebuilder.engine.TraceObjectPropertyDialog)
	 */
	public void okSelected(TraceObjectPropertyDialog dialog)
			throws TraceBuilderException {
		int id = dialog.getID();
		String name = dialog.getName();
		model.getVerifier().checkTraceGroupProperties(model, null, id, name);
		TraceModelExtension[] extensions = createExtensions(null, dialog);
		TraceGroup group = model.getFactory().createTraceGroup(id, name,
				extensions);
		TraceBuilderGlobals.getTraceBuilder().traceObjectSelected(group, true, false);
	}

}
