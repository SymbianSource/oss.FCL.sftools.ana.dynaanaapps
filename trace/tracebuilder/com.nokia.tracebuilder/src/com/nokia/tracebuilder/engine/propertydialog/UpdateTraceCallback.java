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
* Callback for update trace property dialog
*
*/
package com.nokia.tracebuilder.engine.propertydialog;

import com.nokia.tracebuilder.engine.TraceObjectPropertyDialog;
import com.nokia.tracebuilder.model.Trace;
import com.nokia.tracebuilder.model.TraceBuilderException;
import com.nokia.tracebuilder.model.TraceModel;

/**
 * Callback for update trace property dialog
 * 
 */
final class UpdateTraceCallback extends PropertyDialogCallback {

	/**
	 * Trace to be updated
	 */
	private Trace trace;

	/**
	 * Constructor
	 * 
	 * @param model
	 *            the trace model
	 * @param trace
	 *            the trace to be updated
	 */
	UpdateTraceCallback(TraceModel model, Trace trace) {
		super(model);
		this.trace = trace;
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
		String value = dialog.getValue();
		trace.getModel().getVerifier().checkTraceProperties(trace.getGroup(),
				trace, id, name, value);
		trace.setTrace(value);
		trace.setID(id);
		trace.setName(name);
	}

}