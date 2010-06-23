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
* Callback for update group property dialog
*
*/
package com.nokia.tracebuilder.engine.propertydialog;

import com.nokia.tracebuilder.engine.TraceObjectPropertyDialog;
import com.nokia.tracebuilder.model.TraceBuilderException;
import com.nokia.tracebuilder.model.TraceGroup;
import com.nokia.tracebuilder.model.TraceModel;

/**
 * Callback for update group property dialog
 * 
 */
final class UpdateGroupCallback extends PropertyDialogCallback {

	/**
	 * The group to be updated
	 */
	private TraceGroup group;

	/**
	 * Constructor
	 * 
	 * @param model
	 *            the trace model
	 * @param group
	 *            the group to be updated
	 */
	UpdateGroupCallback(TraceModel model, TraceGroup group) {
		super(model);
		this.group = group;
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
		group.getModel().getVerifier().checkTraceGroupProperties(
				group.getModel(), group, id, name);
		group.setID(id);
		group.setName(name);
	}

}