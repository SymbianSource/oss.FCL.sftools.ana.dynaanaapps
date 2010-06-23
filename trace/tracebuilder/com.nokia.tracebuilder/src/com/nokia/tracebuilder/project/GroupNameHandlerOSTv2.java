/*
 * Copyright (c) 2010 Nokia Corporation and/or its subsidiary(-ies). 
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
 * Default names for OSTv2 trace groups
 *
 */
package com.nokia.tracebuilder.project;

/**
 * Default names for trace groups
 * 
 */
public final class GroupNameHandlerOSTv2 extends GroupNameHandlerBase {

	/**
	 * Default names for OSTv2 groups.
	 */
	private static String[] DEFAULT_GROUPS = { "", //$NON-NLS-1$
			"TRACE_FATAL", //$NON-NLS-1$
			"TRACE_ERROR", //$NON-NLS-1$
			"TRACE_WARNING", //$NON-NLS-1$
			"TRACE_BORDER", //$NON-NLS-1$
			"TRACE_NORMAL", //$NON-NLS-1$
			"TRACE_STATE", //$NON-NLS-1$
			"TRACE_INTERNALS", //$NON-NLS-1$
			"TRACE_DUMP", //$NON-NLS-1$
			"TRACE_FLOW", //$NON-NLS-1$
			"TRACE_PERFORMANCE", //$NON-NLS-1$
			"TRACE_ADHOC", //$NON-NLS-1$
			"TRACE_EXTENSION", //$NON-NLS-1$
			"TRACE_TESTING1", //$NON-NLS-1$
			"TRACE_TESTING2" //$NON-NLS-1$
	};

	/**
	 * Start of OSTv2 user-defined groups
	 */
	private static int USER_GROUP_ID_FIRST = 222; // CodForChk_Dis_Magic

	/**
	 * End of OSTv2 user-defined groups
	 */
	private static int USER_GROUP_ID_LAST = 253; // CodForChk_Dis_Magic
	
	/**
	 * Max group ID
	 */
	public static final int MAX_GROUP_ID = 255; // CodForChk_Dis_Magic

	/**
	 * Index of OSTv2 state group
	 */
	private static int STATE_GROUP_ID_INDEX = 6; // CodForChk_Dis_Magic

	/**
	 * Index of OSTv2 performance group
	 */
	private static int PERFORMANCE_GROUP_ID_INDEX = 10; // CodForChk_Dis_Magic

	/**
	 * Index of OSTv2 flow group
	 */
	private static int FLOW_GROUP_ID_INDEX = 9; // CodForChk_Dis_Magic

	/**
	 * Index of OSTv2 normal group
	 */
	private static int NORMAL_GROUP_ID_INDEX = 5; // CodForChk_Dis_Magic

	/**
	 * OSTv2 version text
	 */
	private static final String OST_VERSION_2_X_X = "2.x.x"; //$NON-NLS-1$

	
	/**
	 * Constructor
	 */
	public GroupNameHandlerOSTv2() {
		defaultGroups = DEFAULT_GROUPS;
		userGroupIdFirst = USER_GROUP_ID_FIRST;
		userGroupIdLast = USER_GROUP_ID_LAST;
		stateGroupIdIndex = STATE_GROUP_ID_INDEX;
		performanceGroupIdIndex = PERFORMANCE_GROUP_ID_INDEX;
		flowGroupIdIndex = FLOW_GROUP_ID_INDEX;
		normalGroupIdIndex = NORMAL_GROUP_ID_INDEX;
		usedOstVersion = OST_VERSION_2_X_X;
		maxGroupId = MAX_GROUP_ID;
	}
}
