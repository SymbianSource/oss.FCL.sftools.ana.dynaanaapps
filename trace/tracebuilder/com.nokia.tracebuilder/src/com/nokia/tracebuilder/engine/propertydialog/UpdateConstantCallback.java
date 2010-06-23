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
* Callback for update constant property dialog
*
*/
package com.nokia.tracebuilder.engine.propertydialog;

import com.nokia.tracebuilder.engine.TraceObjectPropertyDialog;
import com.nokia.tracebuilder.model.TraceBuilderException;
import com.nokia.tracebuilder.model.TraceConstantTableEntry;
import com.nokia.tracebuilder.model.TraceModel;

/**
 * Callback for update constant property dialog
 * 
 */
final class UpdateConstantCallback extends PropertyDialogCallback {

	/**
	 * The constant table entry to be updated
	 */
	private TraceConstantTableEntry entry;

	/**
	 * Constructor
	 * 
	 * @param model
	 *            the trace model
	 * @param entry
	 *            the entry to be updated
	 */
	UpdateConstantCallback(TraceModel model, TraceConstantTableEntry entry) {
		super(model);
		this.entry = entry;
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
		entry.getModel().getVerifier().checkConstantProperties(
				entry.getTable(), entry, id, name);
		entry.setID(id);
		entry.setName(name);
	}

}