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
* Handler for add parameter command
*
*/
package com.nokia.tracebuilder.action;

import com.nokia.tracebuilder.engine.LastKnownLocation;
import com.nokia.tracebuilder.engine.TraceBuilderGlobals;
import com.nokia.tracebuilder.engine.TraceBuilderHelp;
import com.nokia.tracebuilder.engine.TraceLocation;
import com.nokia.tracebuilder.model.Trace;
import com.nokia.tracebuilder.model.TraceBuilderException;
import com.nokia.tracebuilder.model.TraceObject;
import com.nokia.tracebuilder.rules.ReadOnlyObjectRule;
import com.nokia.tracebuilder.rules.TraceParameterRestrictionRule;

/**
 * Handler for add parameter command
 * 
 */
final class AddParameterAction extends TraceBuilderAction {

	/**
	 * Constructor
	 */
	AddParameterAction() {
		setText(Messages.getString("AddParameterAction.Title")); //$NON-NLS-1$
		setToolTipText(Messages.getString("AddParameterAction.Tooltip")); //$NON-NLS-1$
		setDefaultProperties(ActionIDs.ADD_PARAMETER_ACTION_ID,
				TraceBuilderHelp.ADD_PARAMETER_HELP_CONTEXT);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.action.TraceBuilderAction#doRun()
	 */
	@Override
	protected void doRun() throws TraceBuilderException {
		TraceBuilderGlobals.getTraceBuilder().addParameter();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.action.TraceBuilderAction#getEnabledFlag(java.lang.Object)
	 */
	@Override
	protected boolean getEnabledFlag(Object selectedObject) {
		boolean retval;
		selectedObject = locationToTrace(selectedObject);
		// Currently only traces can have parameters
		if (selectedObject instanceof Trace) {
			ReadOnlyObjectRule readOnly = ((Trace) selectedObject)
					.getExtension(ReadOnlyObjectRule.class);
			TraceParameterRestrictionRule restriction = ((Trace) selectedObject)
					.getExtension(TraceParameterRestrictionRule.class);
			if (readOnly != null
					|| (restriction != null && !restriction.canAddParameters())) {
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
	 * @see com.nokia.tracebuilder.action.TraceBuilderAction#isInPopupMenu(java.lang.Object)
	 */
	@Override
	protected boolean isInPopupMenu(Object selectedObject) {
		Object parent = locationToTrace(selectedObject);
		return isEnabled() && (parent instanceof TraceObject)
				&& !(selectedObject instanceof TraceLocation)
				&& !(selectedObject instanceof LastKnownLocation);
	}

}