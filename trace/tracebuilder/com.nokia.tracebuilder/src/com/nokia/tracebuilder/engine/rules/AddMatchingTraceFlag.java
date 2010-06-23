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
* Dialog flag to add mathicg exit trace to entry trace
*
*/
package com.nokia.tracebuilder.engine.rules;

import java.util.List;

import com.nokia.tracebuilder.engine.SourceContextManager;
import com.nokia.tracebuilder.engine.TraceBuilderGlobals;
import com.nokia.tracebuilder.engine.TraceObjectPropertyDialogTemplate;
import com.nokia.tracebuilder.model.TraceModelExtension;

/**
 * Dialog flag to add matching exit trace to entry trace. This creates an
 * AutoAddMatchingTraceRule to the trace(s) created by the dialog.
 * 
 */
final class AddMatchingTraceFlag extends DialogDynamicFlagBase {

	/**
	 * Title shown in UI
	 */
	private static final String UI_TITLE = Messages
			.getString("AddMatchingTraceFlag.Title"); //$NON-NLS-1$

	/**
	 * The template that was selected to create the entry trace
	 */
	private TraceObjectPropertyDialogTemplate entryTemplate;

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.engine.TraceObjectPropertyDialogFlag#
	 *      createExtensions(java.util.List)
	 */
	public void createExtensions(List<TraceModelExtension> extList) {
		extList.add(new AutoAddMatchingTraceRule(entryTemplate));
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
		boolean changed = false;
		if (template instanceof EntryExitTraceTemplate
				|| template instanceof EntryExitInstrumenterTemplate
				|| template instanceof PerformanceEventTemplate) {
			if (entryTemplate != template) {
				changed = true;
				entryTemplate = template;
			}
		} else {
			if (entryTemplate != null) {
				changed = true;
				entryTemplate = null;
			}
		}
		return changed;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.engine.rules.DialogDynamicFlagBase#isAvailable()
	 */
	@Override
	public boolean isAvailable() {
		// Currently this is never available
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.engine.rules.DialogDynamicFlagBase#isAlwaysEnabled()
	 */
	@Override
	public boolean isAlwaysEnabled() {
		return (entryTemplate instanceof EntryExitTraceTemplate)
				|| (entryTemplate instanceof EntryExitInstrumenterTemplate)
				|| (entryTemplate instanceof PerformanceEventTemplate);
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
		return manager.isInstrumenting() || manager.getContext() != null;
	}

}
