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
* Constant table deletion query processor
*
*/
package com.nokia.tracebuilder.engine.propertydialog;

import com.nokia.tracebuilder.engine.TraceBuilderDialogs.DeleteObjectQueryParameters;
import com.nokia.tracebuilder.engine.TraceBuilderDialogs.QueryDialogParameters;
import com.nokia.tracebuilder.engine.TraceBuilderDialogs.QueryDialogType;
import com.nokia.tracebuilder.model.TraceBuilderException;
import com.nokia.tracebuilder.model.TraceConstantTable;
import com.nokia.tracebuilder.model.TraceObject;

/**
 * Constant table deletion query processor
 * 
 */
public final class DeleteConstantTableCallback extends DeleteObjectCallback {

	/**
	 * Constructor
	 * 
	 * @param table
	 *            the table to be deleted
	 */
	public DeleteConstantTableCallback(TraceConstantTable table) {
		super(table);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.engine.propertydialog.DeleteObjectCallback#
	 *      buildQuery(com.nokia.tracebuilder.model.TraceObject)
	 */
	@Override
	protected QueryDialogParameters buildQuery(TraceObject object)
			throws TraceBuilderException {
		TraceConstantTable table = (TraceConstantTable) object;
		DeleteObjectQueryParameters params = new DeleteObjectQueryParameters();
		params.objectName = table.getName();
		params.dialogType = QueryDialogType.DELETE_CONSTANT_TABLE;
		return params;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.engine.propertydialog.DeleteObjectCallback#
	 *      deleteObject(com.nokia.tracebuilder.model.TraceObject,
	 *      com.nokia.tracebuilder.engine.TraceBuilderDialogs.QueryDialogParameters)
	 */
	@Override
	protected void deleteObject(TraceObject object,
			QueryDialogParameters queryResults) {
		TraceConstantTable table = (TraceConstantTable) object;
		table.getModel().removeConstantTable(table);
	}

}
