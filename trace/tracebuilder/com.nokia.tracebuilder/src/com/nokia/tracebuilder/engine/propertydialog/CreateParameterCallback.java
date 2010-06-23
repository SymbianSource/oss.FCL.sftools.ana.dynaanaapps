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
* Callback for add parameter property dialog
*
*/
package com.nokia.tracebuilder.engine.propertydialog;

import com.nokia.tracebuilder.engine.TraceBuilderGlobals;
import com.nokia.tracebuilder.engine.TraceObjectPropertyDialog;
import com.nokia.tracebuilder.model.Trace;
import com.nokia.tracebuilder.model.TraceBuilderException;
import com.nokia.tracebuilder.model.TraceModel;
import com.nokia.tracebuilder.model.TraceModelExtension;
import com.nokia.tracebuilder.model.TraceParameter;

/**
 * Callback for add parameter property dialog
 * 
 */
final class CreateParameterCallback extends PropertyDialogCallback {

	/**
	 * Trace parameter is returned after creation
	 */
	private TraceParameter parameter;

	/**
	 * Owning trace
	 */
	private Trace trace;

	/**
	 * Constructor
	 * 
	 * @param model
	 *            the trace model
	 * @param trace
	 *            the trace where parameter is added
	 */
	CreateParameterCallback(TraceModel model, Trace trace) {
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
		String type = dialog.getValue();
		model.getVerifier().checkTraceParameterProperties(trace, null, id,
				name, type);
		TraceModelExtension[] extensions = createExtensions(trace, dialog);
		parameter = model.getFactory().createTraceParameter(trace, id, name,
				type, extensions);
		TraceBuilderGlobals.getTraceBuilder().traceObjectSelected(parameter,
				true, false);
	}

	/**
	 * Gets the parameter that was created
	 * 
	 * @return the new parameter
	 */
	TraceParameter getParameter() {
		return parameter;
	}

}