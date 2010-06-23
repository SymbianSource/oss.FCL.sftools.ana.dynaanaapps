/*
* Copyright (c) 2008-2010 Nokia Corporation and/or its subsidiary(-ies). 
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
* Function entry-exit template for instrumenter
*
*/
package com.nokia.tracebuilder.engine.rules;

import java.util.List;

import com.nokia.tracebuilder.engine.TraceBuilderGlobals;
import com.nokia.tracebuilder.model.TraceModelExtension;
import com.nokia.tracebuilder.project.GroupNameHandlerBase;

/**
 * Function entry-exit template for instrumenter
 * 
 */
final class EntryExitInstrumenterTemplate extends InstrumenterTemplateBase
		implements ExitTracePropertyBuilder {

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.engine.TraceObjectPropertyDialogTemplate#getTitle()
	 */
	public String getTitle() {
		return RuleUtils.getEntryTemplateTitle(RuleUtils.TYPE_ENTRY_EXIT);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.engine.TraceObjectPropertyDialogTemplate#getName()
	 */
	public String getName() {
		return RuleUtils.createEntryTraceNameFormat(RuleUtils.TYPE_ENTRY_EXIT);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.engine.TraceObjectPropertyDialogTemplate#getValue()
	 */
	public String getValue() {
		return RuleUtils.TEXT_FORMAT_BASE;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.engine.TraceObjectPropertyDialogTemplate#
	 *      createExtensions(java.util.List)
	 */
	public void createExtensions(List<TraceModelExtension> list) {
		list.add(new StartOfFunctionLocationRule());
		list.add(new EntryTraceRule());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.engine.rules.ExitTracePropertyBuilder#
	 *      createExitName(java.lang.String)
	 */
	public String createExitName(String entryName) {
		return RuleUtils.createExitName(entryName, RuleUtils.TYPE_ENTRY_EXIT);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.engine.rules.ExitTracePropertyBuilder#
	 *      createExitText(java.lang.String)
	 */
	public String createExitText(String entryText) {
		return entryText;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.engine.TracePropertyDialogTemplate#getGroupName()
	 */
	public String getGroupName() {
		// Entry-exit traces go to flow group by default
		GroupNameHandlerBase groupNameHandler = TraceBuilderGlobals.getGroupNameHandler();
		return groupNameHandler.getDefaultGroups()[groupNameHandler.getFlowGroupIdIndex()];
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.engine.rules.InstrumenterTemplateBase#isValueEnabled()
	 */
	@Override
	public boolean isValueEnabled() {
		return false;
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.engine.rules.InstrumenterTemplateBase#isTargetEnabled()
	 */
	@Override
	public boolean isTargetEnabled() {
		return false;
	}

}
