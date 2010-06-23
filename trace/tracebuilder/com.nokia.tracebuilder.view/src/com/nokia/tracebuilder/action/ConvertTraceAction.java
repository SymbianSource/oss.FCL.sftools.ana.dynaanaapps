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
* Handler for convert trace command
*
*/
package com.nokia.tracebuilder.action;

import com.nokia.tracebuilder.engine.TraceBuilderGlobals;
import com.nokia.tracebuilder.engine.TraceBuilderHelp;
import com.nokia.tracebuilder.engine.TraceLocation;
import com.nokia.tracebuilder.engine.TraceBuilderErrorCodes.TraceBuilderErrorCode;
import com.nokia.tracebuilder.model.TraceBuilderException;
import com.nokia.tracebuilder.project.TraceProjectAPI;

/**
 * Handler for convert trace command
 * 
 */
final class ConvertTraceAction extends TraceBuilderAction {

	/**
	 * Constructor
	 */
	ConvertTraceAction() {
		setText(Messages.getString("ConvertTraceAction.Title")); //$NON-NLS-1$
		setToolTipText(Messages.getString("ConvertTraceAction.Tooltip")); //$NON-NLS-1$
		setDefaultProperties(ActionIDs.CONVERT_TRACE_ACTION_ID,
				TraceBuilderHelp.CONVERT_TRACE_HELP_CONTEXT);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.action.TraceBuilderAction#doRun()
	 */
	@Override
	protected void doRun() throws TraceBuilderException {
		TraceBuilderGlobals.getTraceBuilder().convertTrace();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.action.TraceBuilderAction#getEnabledFlag(java.lang.Object)
	 */
	@Override
	protected boolean getEnabledFlag(Object selectedObject) {
		boolean retval = false;
		if (selectedObject instanceof TraceLocation) {
			TraceLocation loc = (TraceLocation) selectedObject;
			if (loc.getValidityCode() == TraceBuilderErrorCode.TRACE_NEEDS_CONVERSION) {
				TraceProjectAPI api = TraceBuilderGlobals.getTraceModel()
						.getExtension(TraceProjectAPI.class);
				setText(Messages.getString("ConvertTraceAction.Title") //$NON-NLS-1$
						+ api.getTitle());
				retval = true;
			}
		}
		return retval;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.action.TraceBuilderAction#isInMenu()
	 */
	@Override
	protected boolean isInMenu() {
		return false;
	}

}
