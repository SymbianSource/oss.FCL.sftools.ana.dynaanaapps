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
 * Default names for trace groups
 *
 */
package com.nokia.tracebuilder.project;

/**
 * Default names for trace groups
 * 
 */
public class GroupNameHandlerBase {

	/**
	 * Array of default groups
	 */
	protected String[] defaultGroups;
	
	/**
	 * First user defined group ID
	 */
	protected int userGroupIdFirst;
	
	/**
	 * Last user defined group ID
	 */
	protected int userGroupIdLast;
	
	/**
	 * Index of TRACE_STATE group
	 */
	protected int stateGroupIdIndex;
	
	/**
	 * Index of TRACE_PERFORMANCE group
	 */
	protected int performanceGroupIdIndex;
	
	/**
	 * Index of TRACE_FLOW group
	 */
	protected int flowGroupIdIndex;
	
	/**
	 * Index of TRACE_NORMAL group
	 */
	protected int normalGroupIdIndex;
	
	/**
	 * OST version in use
	 */
	protected String usedOstVersion;
	
	
	/**
	 * Max group ID
	 */
	protected int maxGroupId;
	
	/**
	 * Constructor
	 */
	public GroupNameHandlerBase() {

	}

	/**
	 * Get list of default groups
	 * 
	 * @return list of default groups
	 */
	public String[] getDefaultGroups() {
		return defaultGroups;
	}

	/**
	 * Get first user defined group ID
	 * 
	 * @return first user defined group ID
	 */
	public int getUserGroupIdFirst() {
		return userGroupIdFirst;
	}

	/**
	 * Get last user defined group ID
	 * 
	 * @return last user defined group ID
	 */
	public int getUserGroupIdLast() {
		return userGroupIdLast;
	}

	/**
	 * Get state group ID index
	 * 
	 * @return state group ID index
	 */
	public int getStateGroupIdIndex() {
		return stateGroupIdIndex;
	}

	/**
	 * Get performance group ID index
	 * 
	 * @return performance group ID index
	 */
	public int getPerformanceGroupIdIndex() {
		return performanceGroupIdIndex;
	}

	/**
	 * Get flow group ID index
	 * 
	 * @return flow group ID index
	 */
	public int getFlowGroupIdIndex() {
		return flowGroupIdIndex;
	}

	/**
	 * Get normal group ID index
	 * 
	 * @return normal group ID index
	 */
	public int getNormalGroupIdIndex() {
		return normalGroupIdIndex;
	}

	/**
	 * Get max number of user defined groups
	 * 
	 * @return normal group ID index
	 */
	public int getMaxNumberOfUserDefinedGroups() {
		return userGroupIdLast - userGroupIdFirst + 1;
	}
	
	/**
	 * Get max group ID
	 * 
	 * @return max group ID
	 */
	public int getMaxGroupId() {
		return maxGroupId;
	}
}
