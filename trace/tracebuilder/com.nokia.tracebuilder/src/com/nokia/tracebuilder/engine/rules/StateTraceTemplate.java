/*
* Copyright (c) 2009-2010 Nokia Corporation and/or its subsidiary(-ies). 
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
* Template for State trace
*
*/
package com.nokia.tracebuilder.engine.rules;

import java.util.List;

import com.nokia.tracebuilder.engine.TraceBuilderGlobals;
import com.nokia.tracebuilder.model.TraceModelExtension;
import com.nokia.tracebuilder.project.GroupNameHandlerBase;

/**
 * Template for State trace
 * 
 */
public final class StateTraceTemplate extends ContextTemplateBase {

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.engine.TraceObjectPropertyDialogTemplate#getTitle()
	 */
	public String getTitle() {
		return RuleUtils.getEntryTemplateTitle(RuleUtils.TYPE_STATE_TRACE);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.engine.TracePropertyDialogTemplate#getGroupName()
	 */
	public String getGroupName() {
		GroupNameHandlerBase groupNameHandler = TraceBuilderGlobals.getGroupNameHandler();
		return groupNameHandler.getDefaultGroups()[groupNameHandler.getStateGroupIdIndex()];
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.engine.TraceObjectPropertyDialogTemplate#
	 *      createExtensions(java.util.List)
	 */
	public void createExtensions(List<TraceModelExtension> list) {
		list.add(new StateTraceRule());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.engine.rules.ContextTemplateBase#getTraceNameFormat()
	 */
	@Override
	protected String getTraceNameFormat() {
		return RuleUtils.createEntryTraceNameFormat(RuleUtils.TYPE_STATE_TRACE);
	}

	/* (non-Javadoc)
	 * @see com.nokia.tracebuilder.engine.rules.ContextTemplateBase#getTraceTextFormat()
	 */
	@Override
	protected String getTraceTextFormat() {
		return ""; //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see com.nokia.tracebuilder.engine.rules.ContextTemplateBase#isIDEnabled()
	 */
	@Override
	public boolean isIDEnabled() {
		return true;
	}

	/* (non-Javadoc)
	 * @see com.nokia.tracebuilder.engine.rules.ContextTemplateBase#isNameEnabled()
	 */
	@Override
	public boolean isNameEnabled() {
		return true;
	}
	
	/* (non-Javadoc)
	 * @see com.nokia.tracebuilder.engine.rules.ContextTemplateBase#isTargetEnabled()
	 */
	@Override
	public boolean isTargetEnabled() {
		return false;
	}
	
	/* (non-Javadoc)
	 * @see com.nokia.tracebuilder.engine.rules.ContextTemplateBase#isValueEnabled()
	 */
	@Override
	public boolean isValueEnabled() {
		return false;
	}
}
