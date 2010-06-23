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
* Handler for "Remove from source" action
*
*/
package com.nokia.tracebuilder.action;

import com.nokia.tracebuilder.engine.TraceBuilderGlobals;
import com.nokia.tracebuilder.engine.TraceBuilderHelp;
import com.nokia.tracebuilder.engine.TraceLocation;
import com.nokia.tracebuilder.model.TraceBuilderException;

/**
 * Handler for "Remove from source" action
 * 
 */
final class RemoveSourceAction extends TraceBuilderAction {

	/**
	 * Constructor
	 */
	RemoveSourceAction() {
		setText(Messages.getString("RemoveSourceAction.Title")); //$NON-NLS-1$
		setToolTipText(Messages.getString("RemoveSourceAction.Tooltip")); //$NON-NLS-1$
		setDefaultProperties(null,
				TraceBuilderHelp.REMOVE_TRACE_HELP_CONTEXT);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.action.TraceBuilderAction#doRun()
	 */
	@Override
	protected void doRun() throws TraceBuilderException {
		TraceBuilderGlobals.getTraceBuilder().removeFromSource();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.action.TraceBuilderAction#getEnabledFlag(java.lang.Object)
	 */
	@Override
	protected boolean getEnabledFlag(Object selectedObject) {
		boolean retval;
		if (selectedObject instanceof TraceLocation) {
			retval = true;
		} else {
			retval = false;
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
