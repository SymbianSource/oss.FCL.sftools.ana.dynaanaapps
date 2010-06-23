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
* Dialog flag to add a this pointer parameter to trace
*
*/
package com.nokia.tracebuilder.engine.rules;

import java.util.List;

import com.nokia.tracebuilder.engine.SourceContextManager;
import com.nokia.tracebuilder.engine.TraceBuilderGlobals;
import com.nokia.tracebuilder.engine.TraceObjectPropertyDialogFlag;
import com.nokia.tracebuilder.engine.TraceObjectPropertyDialogTemplate;
import com.nokia.tracebuilder.model.TraceModelExtension;
import com.nokia.tracebuilder.source.SourceContext;

/**
 * Dialog flag to add a this pointer parameter to trace. This creates a
 * AutoAddThisPtrRule to the trace(s) created by the dialog.
 * 
 */
final class AddThisPtrFlag extends DialogDynamicFlagBase {

	/**
	 * Title shown in UI
	 */
	private static final String UI_TITLE = Messages
			.getString("AddThisPtrFlag.Title"); //$NON-NLS-1$

	/**
	 * Currently selected template
	 */
	private TraceObjectPropertyDialogTemplate template;

	/**
	 * Add function parameters flag enabled
	 */
	private boolean addFunctionParametersFlagEnabled;

	/**
	 * Add return parameter flag enabled
	 */
	private boolean addReturnParameterFlagEnabled;

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.engine.TraceObjectPropertyDialogFlag#
	 *      createExtensions(java.util.List)
	 */
	public void createExtensions(List<TraceModelExtension> extList) {
		extList.add(new AutoAddThisPtrRule());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.engine.TraceObjectPropertyDialogFlag#getText()
	 */
	public String getText() {
		return UI_TITLE;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.engine.rules.DialogDynamicFlagBase#
	 *      templateChanged(com.nokia.tracebuilder.engine.TraceObjectPropertyDialogTemplate)
	 */
	@Override
	public boolean templateChanged(TraceObjectPropertyDialogTemplate template) {
		this.template = template;
		return checkAvailability();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.engine.rules.DialogDynamicFlagBase#
	 *      flagChanged(com.nokia.tracebuilder.engine.TraceObjectPropertyDialogFlag)
	 */
	@Override
	public boolean flagChanged(TraceObjectPropertyDialogFlag flag) {
		boolean changed = false;
		if (flag instanceof AddFunctionParametersFlag) {
			addFunctionParametersFlagEnabled = flag.isEnabled();
			checkAvailability();
			changed = true;
		} else if (flag instanceof AddReturnParameterFlag) {
			addReturnParameterFlagEnabled = flag.isEnabled();
			checkAvailability();
			changed = true;
		}

		return changed;
	}

	/**
	 * Checks the availability of this flag
	 * 
	 * @return the flag
	 */
	private boolean checkAvailability() {
		boolean changed = false;
		if (template instanceof PerformanceEventTemplate) {
			setAvailable(false);
			setEnabled(false);
			changed = true;
		} else if ((template instanceof EntryExitTraceTemplate || template instanceof EntryExitInstrumenterTemplate)
				&& (addFunctionParametersFlagEnabled || addReturnParameterFlagEnabled)) {
			setAvailable(false);
			setEnabled(true);
			changed = true;
		} else {
			if (!isAvailable()) {
				setAvailable(true);
				changed = true;
			}
		}
		return changed;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.engine.rules.DialogDynamicFlagBase#isAlwaysEnabled()
	 */
	@Override
	public boolean isAlwaysEnabled() {
		// Auto-enabled with entry-exit traces if
		// function parameters flag is set
		boolean entry = (template instanceof EntryExitTraceTemplate)
				|| (template instanceof EntryExitInstrumenterTemplate);
		return isVisible()
				&& (addFunctionParametersFlagEnabled || addReturnParameterFlagEnabled)
				&& entry;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.engine.rules.DialogFlagBase#isVisible()
	 */
	@Override
	public boolean isVisible() {
		SourceContextManager manager = TraceBuilderGlobals
				.getSourceContextManager();
		boolean retval;
		if (manager.isInstrumenting()) {
			retval = true;
		} else {
			SourceContext context = manager.getContext();
			if (context != null && !RuleUtils.isStaticFunction(context)) {
				retval = true;
			} else {
				retval = false;
			}
		}
		return retval;
	}
}
