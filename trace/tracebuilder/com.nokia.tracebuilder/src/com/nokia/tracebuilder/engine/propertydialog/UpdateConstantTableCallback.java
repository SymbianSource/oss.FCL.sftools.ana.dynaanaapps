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
* Property dialog callback to modify constant tables
*
*/
package com.nokia.tracebuilder.engine.propertydialog;

import com.nokia.tracebuilder.engine.TraceObjectPropertyDialog;
import com.nokia.tracebuilder.model.TraceBuilderException;
import com.nokia.tracebuilder.model.TraceConstantTable;
import com.nokia.tracebuilder.model.TraceModel;

/**
 * Property dialog callback to modify constant tables
 * 
 */
final class UpdateConstantTableCallback extends PropertyDialogCallback {

	/**
	 * The constant table to be updated
	 */
	private TraceConstantTable table;

	/**
	 * Constructor
	 * 
	 * @param model
	 *            the trace model
	 * @param table
	 *            the constant table to be updated
	 */
	UpdateConstantTableCallback(TraceModel model, TraceConstantTable table) {
		super(model);
		this.table = table;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.engine.PropertyDialogManagerCallback#
	 *      okSelected(com.nokia.tracebuilder.engine.TraceObjectPropertyDialog)
	 */
	public void okSelected(TraceObjectPropertyDialog dialog)
			throws TraceBuilderException {
		String name = dialog.getName();
		int id = dialog.getID();
		table.getModel().getVerifier().checkConstantTableProperties(
				table.getModel(), table, id, name);
		table.setName(name);
	}

}
