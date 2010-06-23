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
* Remove all unrelated traces action
*
*/
package com.nokia.tracebuilder.action;

import com.nokia.tracebuilder.engine.TraceBuilderGlobals;
import com.nokia.tracebuilder.engine.TraceBuilderHelp;
import com.nokia.tracebuilder.engine.TraceLocationList;
import com.nokia.tracebuilder.model.Trace;
import com.nokia.tracebuilder.model.TraceBuilderException;

/**
 * Remove all unrelated traces action
 * 
 */
final class RemoveUnrelatedAction extends TraceBuilderAction {

	/**
	 * Constructor
	 */
	RemoveUnrelatedAction() {
		setText(Messages.getString("RemoveUnrelatedAction.Title")); //$NON-NLS-1$
		setToolTipText(Messages.getString("RemoveUnrelatedAction.Tooltip")); //$NON-NLS-1$
		setDefaultProperties(null,
				TraceBuilderHelp.REMOVE_UNRELATED_HELP_CONTEXT);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.action.TraceBuilderAction#doRun()
	 */
	@Override
	protected void doRun() throws TraceBuilderException {
		TraceBuilderGlobals.getTraceBuilder().removeUnrelatedFromSource();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.action.TraceBuilderAction#getEnabledFlag(java.lang.Object)
	 */
	@Override
	protected boolean getEnabledFlag(Object selectedObject) {
		boolean retval;
		if (selectedObject instanceof TraceLocationList) {
			if (((TraceLocationList) selectedObject).getOwner() instanceof Trace) {
				retval = false;
			} else if (((TraceLocationList) selectedObject).getListTitle() != null) {
				retval = false;
			} else {
				retval = true;
			}
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
