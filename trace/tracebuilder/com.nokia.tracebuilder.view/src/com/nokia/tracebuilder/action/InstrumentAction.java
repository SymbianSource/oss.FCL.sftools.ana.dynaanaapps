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
* Handler for instrument code command
*
*/
package com.nokia.tracebuilder.action;

import com.nokia.tracebuilder.engine.TraceBuilderGlobals;
import com.nokia.tracebuilder.engine.TraceBuilderHelp;
import com.nokia.tracebuilder.model.TraceBuilderException;

/**
 * Handler for instrument code command
 * 
 */
final class InstrumentAction extends TraceBuilderAction {

	/**
	 * Constructor
	 */
	InstrumentAction() {
		setText(Messages.getString("InstrumentAction.Title")); //$NON-NLS-1$
		setToolTipText(Messages.getString("InstrumentAction.Tooltip")); //$NON-NLS-1$
		setDefaultProperties(ActionIDs.INSTRUMENTER_ACTION_ID,
				TraceBuilderHelp.INSTRUMENTER_HELP_CONTEXT);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.action.TraceBuilderAction#doRun()
	 */
	@Override
	protected void doRun() throws TraceBuilderException {
		TraceBuilderGlobals.getTraceBuilder().startInstrumenter();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.action.TraceBuilderAction#getEnabledFlag(java.lang.Object)
	 */
	@Override
	protected boolean getEnabledFlag(Object selectedObject) {
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.action.TraceBuilderAction#isInPopupMenu(java.lang.Object)
	 */
	@Override
	protected boolean isInPopupMenu(Object selectedObject) {
		return false;
	}

}
