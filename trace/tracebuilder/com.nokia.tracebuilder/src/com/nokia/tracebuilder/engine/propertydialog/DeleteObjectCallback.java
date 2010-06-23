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
* Base class for trace object deletion
*
*/
package com.nokia.tracebuilder.engine.propertydialog;

import com.nokia.tracebuilder.engine.TraceBuilderDialogs;
import com.nokia.tracebuilder.engine.TraceBuilderGlobals;
import com.nokia.tracebuilder.engine.TraceBuilderDialogs.QueryDialogParameters;
import com.nokia.tracebuilder.model.TraceBuilderException;
import com.nokia.tracebuilder.model.TraceObject;

/**
 * Base class for trace object deletion
 * 
 */
public abstract class DeleteObjectCallback {

	/**
	 * The object to be deleted
	 */
	private TraceObject object;

	/**
	 * Constructor
	 * 
	 * @param object
	 *            the object to be deleted
	 */
	DeleteObjectCallback(TraceObject object) {
		this.object = object;
	}

	/**
	 * Deletes the object.
	 * 
	 * @return TraceBuilderDialogs.YES if object was deleted
	 * @throws TraceBuilderException
	 *             if object cannot be deleted
	 */
	public int delete() throws TraceBuilderException {
		int res = PropertyDialogCallback.showLocationConfirmationQuery(object);
		if (res == TraceBuilderDialogs.OK) {
			QueryDialogParameters props = buildQuery(object);
			res = TraceBuilderGlobals.getDialogs().showConfirmationQuery(props);
			if (res == TraceBuilderDialogs.OK) {
				deleteObject(object, props);
			}
		}
		return res;
	}

	/**
	 * Builds the query that is shown to the user before deleting
	 * 
	 * @param object
	 *            the object to be deleted
	 * @return the dialog parameters
	 * @throws TraceBuilderException
	 *             if the object cannot be deleted
	 */
	protected abstract QueryDialogParameters buildQuery(TraceObject object)
			throws TraceBuilderException;

	/**
	 * Called to delete the object if user selected YES to the query
	 * 
	 * @param object
	 *            the object to be deleted
	 * @param queryResults
	 *            the query results, modified by the UI
	 */
	protected abstract void deleteObject(TraceObject object,
			QueryDialogParameters queryResults);

}
