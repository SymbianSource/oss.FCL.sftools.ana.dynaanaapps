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
* Dialog flag to add a return value parameter to trace
*
*/
package com.nokia.tracebuilder.engine.rules;

import java.util.List;

import com.nokia.tracebuilder.engine.SourceContextManager;
import com.nokia.tracebuilder.engine.TraceBuilderGlobals;
import com.nokia.tracebuilder.engine.TraceObjectPropertyDialogTemplate;
import com.nokia.tracebuilder.model.TraceModelExtension;
import com.nokia.tracebuilder.source.SourceContext;

/**
 * Dialog flag to add a return value parameter to trace. This creates a
 * AutoAddReturnParameterRule to the trace(s) created by the dialog.
 * 
 */
final class AddReturnParameterFlag extends DialogDynamicFlagBase {

	/**
	 * Title shown in UI
	 */
	private static final String UI_TITLE = Messages
			.getString("AddReturnParameterFlag.Title"); //$NON-NLS-1$

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.engine.TraceObjectPropertyDialogFlag#
	 *      createExtensions(java.util.List)
	 */
	public void createExtensions(List<TraceModelExtension> extList) {
		extList.add(new AutoAddReturnParameterRule());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.engine.TraceObjectPropertyDialogFlag#
	 *      getText()
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
		boolean changed = false;
		if (template instanceof EntryExitInstrumenterTemplate
				|| template instanceof EntryExitTraceTemplate) {
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.engine.rules.DialogFlagBase#isVisible()
	 */
	@Override
	public boolean isVisible() {
		SourceContextManager manager = TraceBuilderGlobals
				.getSourceContextManager();
		SourceContext context = manager.getContext();
		return manager.isInstrumenting()
				|| (context != null && !context.isVoid());
	}
}
