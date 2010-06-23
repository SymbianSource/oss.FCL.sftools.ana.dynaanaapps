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
* Handler for add constant command
*
*/
package com.nokia.tracebuilder.action;

import com.nokia.tracebuilder.engine.TraceBuilderGlobals;
import com.nokia.tracebuilder.engine.TraceBuilderHelp;
import com.nokia.tracebuilder.model.TraceBuilderException;
import com.nokia.tracebuilder.model.TraceConstantTable;

/**
 * Handler for add constant command
 * 
 */
final class AddConstantAction extends TraceBuilderAction {

	/**
	 * Constructor
	 */
	AddConstantAction() {
		setText(Messages.getString("AddConstantAction.Title")); //$NON-NLS-1$
		setToolTipText(Messages.getString("AddConstantAction.Tooltip")); //$NON-NLS-1$
		setDefaultProperties(ActionIDs.ADD_CONSTANT_ACTION_ID,
				TraceBuilderHelp.ADD_ENUM_HELP_CONTEXT);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.action.TraceBuilderAction#doRun()
	 */
	@Override
	protected void doRun() throws TraceBuilderException {
		TraceBuilderGlobals.getTraceBuilder().addConstant();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.nokia.tracebuilder.action.TraceBuilderAction#getEnabledFlag(java.
	 * lang.Object)
	 */
	@Override
	protected boolean getEnabledFlag(Object selectedObject) {
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.nokia.tracebuilder.action.TraceBuilderAction#isInPopupMenu(java.lang
	 * .Object)
	 */
	@Override
	protected boolean isInPopupMenu(Object selectedObject) {
		return isEnabled() && (selectedObject instanceof TraceConstantTable);
	}
}
