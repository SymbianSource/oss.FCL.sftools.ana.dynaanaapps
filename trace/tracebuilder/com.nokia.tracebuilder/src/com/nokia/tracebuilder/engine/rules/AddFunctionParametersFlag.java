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
* Dialog flag to add function parameter to trace
*
*/
package com.nokia.tracebuilder.engine.rules;

import java.util.ArrayList;
import java.util.List;

import com.nokia.tracebuilder.engine.SourceContextManager;
import com.nokia.tracebuilder.engine.TraceBuilderGlobals;
import com.nokia.tracebuilder.engine.TraceObjectPropertyDialogTemplate;
import com.nokia.tracebuilder.model.TraceModelExtension;
import com.nokia.tracebuilder.source.SourceContext;
import com.nokia.tracebuilder.source.SourceParameter;
import com.nokia.tracebuilder.source.SourceParserException;

/**
 * Dialog flag to add function parameter to trace. This creates a
 * AutoAddFunctionParametersRule to the trace(s) created by the dialog.
 * 
 */
final class AddFunctionParametersFlag extends DialogDynamicFlagBase {

	/**
	 * Title shown in UI
	 */
	private static final String UI_TITLE = Messages
			.getString("AddFunctionParametersFlag.Title"); //$NON-NLS-1$

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.engine.TraceObjectPropertyDialogFlag#
	 *      createExtensions(java.util.List)
	 */
	public void createExtensions(List<TraceModelExtension> extList) {
		extList.add(new AutoAddFunctionParametersRule());
		extList.add(new ComplexHeaderRuleImpl());
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
	 * @see com.nokia.tracebuilder.engine.rules.DialogFlagBase#isVisible()
	 */
	@Override
	public boolean isVisible() {
		SourceContextManager manager = TraceBuilderGlobals
				.getSourceContextManager();
		boolean retval = false;
		if (manager.isInstrumenting()) {
			retval = true;
		} else {
			// If not instrumenting, the current context must
			// exist and have parameters
			SourceContext context = manager.getContext();
			if (context != null) {
				ArrayList<SourceParameter> list = new ArrayList<SourceParameter>();
				try {
					context.parseParameters(list);
					if (list.size() > 0) {
						retval = true;
					}
				} catch (SourceParserException e) {
				}
			}
		}
		return retval;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.engine.rules.DialogDynamicFlagBase#
	 *      templateChanged(com.nokia.tracebuilder.engine.TraceObjectPropertyDialogTemplate)
	 */
	@Override
	public boolean templateChanged(TraceObjectPropertyDialogTemplate template) {
		boolean changed = false;
		if (template == null || template instanceof EntryExitTraceTemplate
				|| template instanceof EntryExitInstrumenterTemplate) {
			if (!isAvailable()) {
				changed = true;
				setAvailable(true);
			}
		} else {
			if (isAvailable()) {
				changed = true;
				setAvailable(false);
			}
		}
		return changed;
	}

}
