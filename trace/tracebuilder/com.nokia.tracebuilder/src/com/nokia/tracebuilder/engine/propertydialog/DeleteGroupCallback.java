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
* Group deletion query processor
*
*/
package com.nokia.tracebuilder.engine.propertydialog;

import com.nokia.tracebuilder.engine.TraceBuilderDialogs.DeleteObjectQueryParameters;
import com.nokia.tracebuilder.engine.TraceBuilderDialogs.QueryDialogParameters;
import com.nokia.tracebuilder.engine.TraceBuilderDialogs.QueryDialogType;
import com.nokia.tracebuilder.engine.source.SourceEngine;
import com.nokia.tracebuilder.model.Trace;
import com.nokia.tracebuilder.model.TraceGroup;
import com.nokia.tracebuilder.model.TraceObject;

/**
 * Group deletion query processor
 * 
 */
public final class DeleteGroupCallback extends DeleteTraceFromSourceCallback {

	/**
	 * Constructor
	 * 
	 * @param group
	 *            the group to be deleted
	 * @param sourceEngine
	 *            the source engine for trace removal
	 */
	public DeleteGroupCallback(TraceGroup group, SourceEngine sourceEngine) {
		super(group, sourceEngine);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.engine.propertydialog.DeleteObjectCallback#
	 *      buildQuery(com.nokia.tracebuilder.model.TraceObject)
	 */
	@Override
	protected QueryDialogParameters buildQuery(TraceObject object) {
		DeleteObjectQueryParameters params = new DeleteObjectQueryParameters();
		params.objectName = object.getName();
		params.dialogType = QueryDialogType.DELETE_GROUP;
		return params;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.engine.propertydialog.DeleteObjectCallback#deleteObject(
	 *      com.nokia.tracebuilder.model.TraceObject,
	 *      com.nokia.tracebuilder.engine.TraceBuilderDialogs.QueryDialogParameters)
	 */
	@Override
	protected void deleteObject(TraceObject object,
			QueryDialogParameters queryResults) {
		TraceGroup group = (TraceGroup) object;
		for (Trace trace : group) {
			removeTraceFromSource(trace);
		}
		group.getModel().removeGroup(group);
	}

}
