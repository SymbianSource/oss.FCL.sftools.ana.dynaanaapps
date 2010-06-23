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
* Constant table entry deletion query processor
*
*/
package com.nokia.tracebuilder.engine.propertydialog;

import com.nokia.tracebuilder.engine.TraceBuilderDialogs.DeleteObjectQueryParameters;
import com.nokia.tracebuilder.engine.TraceBuilderDialogs.QueryDialogParameters;
import com.nokia.tracebuilder.engine.TraceBuilderDialogs.QueryDialogType;
import com.nokia.tracebuilder.model.TraceBuilderException;
import com.nokia.tracebuilder.model.TraceConstantTableEntry;
import com.nokia.tracebuilder.model.TraceObject;

/**
 * Constant table entry deletion query processor
 * 
 */
public final class DeleteConstantCallback extends DeleteObjectCallback {

	/**
	 * Constructor
	 * 
	 * @param entry
	 *            the constant table entry to be removed
	 */
	public DeleteConstantCallback(TraceConstantTableEntry entry) {
		super(entry);
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
		DeleteObjectQueryParameters params = new DeleteObjectQueryParameters();
		TraceConstantTableEntry entry = (TraceConstantTableEntry) object;
		params.objectName = entry.getName();
		params.ownerName = entry.getTable().getName();
		params.dialogType = QueryDialogType.DELETE_CONSTANT;
		return params;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.engine.propertydialog.DeleteObjectCallback#
	 *      deleteObject(com.nokia.tracebuilder.model.TraceObject,
	 *      com.nokia.tracebuilder.engine.propertydialog.DeleteObjectCallback.QueryDialogProperties)
	 */
	@Override
	protected void deleteObject(TraceObject object,
			QueryDialogParameters queryResults) {
		TraceConstantTableEntry entry = (TraceConstantTableEntry) object;
		entry.getTable().removeEntry(entry);
	}

}
