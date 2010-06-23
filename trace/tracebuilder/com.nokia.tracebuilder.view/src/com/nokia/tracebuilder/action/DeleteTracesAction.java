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
* Action to remove multiple traces
*
*/
package com.nokia.tracebuilder.action;

import com.nokia.tracebuilder.engine.TraceBuilderErrorMessages;
import com.nokia.tracebuilder.engine.TraceBuilderGlobals;
import com.nokia.tracebuilder.engine.TraceBuilderHelp;
import com.nokia.tracebuilder.engine.TraceBuilderErrorCodes.TraceBuilderErrorCode;
import com.nokia.tracebuilder.model.TraceBuilderException;

/**
 * Action to remove multiple traces
 * 
 */
final class DeleteTracesAction extends TraceBuilderAction {

	/**
	 * Constructor
	 */
	DeleteTracesAction() {
		setText(Messages.getString("DeleteTracesAction.Title")); //$NON-NLS-1$
		setToolTipText(Messages.getString("DeleteTracesAction.Tooltip")); //$NON-NLS-1$
		setDefaultProperties(ActionIDs.DELETE_TRACES_ACTION_ID,
				TraceBuilderHelp.DELETE_TRACES_HELP_CONTEXT);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.action.TraceBuilderAction#doRun()
	 */
	@Override
	protected void doRun() throws TraceBuilderException {
		TraceBuilderGlobals.getTraceBuilder().deleteMultipleTraces();
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.action.IAction#run()
	 */
	@Override
	public void run() {
		try {
			doRun();
		} catch (TraceBuilderException e) {
			if (e.getErrorCode() == TraceBuilderErrorCode.NO_TRACES_TO_DELETE) {
				String msg = TraceBuilderErrorMessages.getErrorMessage(
						TraceBuilderErrorCode.NO_TRACES_TO_DELETE, null);
				TraceBuilderGlobals.getEvents().postInfoMessage(msg, null);
			} else {
				TraceBuilderGlobals.getEvents().postError(e);
			}
		}
	}
}
