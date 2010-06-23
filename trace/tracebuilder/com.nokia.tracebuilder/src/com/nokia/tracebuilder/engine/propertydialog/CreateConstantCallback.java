/*
* Copyright (c) 2008 Nokia Corporation and/or its subsidiary(-ies). 
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
* Callback for add constant property dialog
*
*/
package com.nokia.tracebuilder.engine.propertydialog;

import com.nokia.tracebuilder.engine.TraceBuilderGlobals;
import com.nokia.tracebuilder.engine.TraceObjectPropertyDialog;
import com.nokia.tracebuilder.model.TraceBuilderException;
import com.nokia.tracebuilder.model.TraceConstantTable;
import com.nokia.tracebuilder.model.TraceConstantTableEntry;
import com.nokia.tracebuilder.model.TraceModel;
import com.nokia.tracebuilder.model.TraceModelExtension;

/**
 * Callback for add constant property dialog
 * 
 */
final class CreateConstantCallback extends PropertyDialogCallback {

	/**
	 * Constructor
	 * 
	 * @param model
	 *            the trace model
	 */
	CreateConstantCallback(TraceModel model) {
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
		String tableName = dialog.getTarget();
		try {
			model.startProcessing();
			TraceConstantTable table = model.findConstantTableByName(tableName);
			if (table == null) {
				// If table does not exist, it is created
				int tableId = model.getNextConstantTableID();
				model.getVerifier().checkConstantTableProperties(model, null,
						tableId, tableName);
				table = model.getFactory().createConstantTable(id, tableName, null);
			}
			model.getVerifier().checkConstantProperties(table, null, id, name);
			TraceModelExtension[] extensions = createExtensions(table, dialog);
			TraceConstantTableEntry entry = model.getFactory()
					.createConstantTableEntry(table, id, name, extensions);
			TraceBuilderGlobals.getTraceBuilder().traceObjectSelected(entry,
					true, false);
		} finally {
			model.processingComplete();
		}
	}
}