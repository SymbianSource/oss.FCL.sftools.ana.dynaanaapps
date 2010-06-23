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
* Utilities for trace formatting
*
*/
package com.nokia.tracebuilder.project;

import java.util.ArrayList;
import java.util.Iterator;

import com.nokia.tracebuilder.engine.TraceBuilderGlobals;
import com.nokia.tracebuilder.model.TraceBuilderException;
import com.nokia.tracebuilder.model.TraceGroup;
import com.nokia.tracebuilder.model.TraceModel;

/**
 * Utilities for trace formatting
 * 
 */
public final class FormattingUtils {

	/**
	 * Gets a group ID based on group name
	 * 
	 * @param model
	 *            the trace model
	 * @param name
	 *            the group name
	 * @return the group ID
	 * @throws TraceBuilderException
	 */
	public static int getGroupID(TraceModel model, String name)
			throws TraceBuilderException {
		int retval = 0;
		
		// First check that is group one of the default groups
		// Groups start from index 1
		GroupNameHandlerBase groupNameHandler = TraceBuilderGlobals.getGroupNameHandler();
		String[] defaultGroups = groupNameHandler.getDefaultGroups();
		for (int i = 1; i < defaultGroups.length && retval == 0; i++) {
			String defaultName = defaultGroups[i];
			if (name.equals(defaultName)) {
				retval = i;
			}
		}
		// If group was not one of the default groups then get next group Id from model
		if (retval == 0) {
			retval = model.getNextGroupID();
			if (retval < groupNameHandler.getUserGroupIdFirst()) {
				retval = groupNameHandler.getUserGroupIdFirst();
			}
		}
		return retval;
	}

	/**
	 * Gets a list of available group names
	 * 
	 * @param traceModel
	 *            the model
	 * @return the group names
	 */
	public static Iterator<String> getGroupNames(TraceModel traceModel) {
		ArrayList<String> list = new ArrayList<String>();
		GroupNameHandlerBase groupNameHandler = TraceBuilderGlobals.getGroupNameHandler();
		String[] defaultGroups = groupNameHandler.getDefaultGroups();
		for (int i = 1; i < defaultGroups.length; i++) {
			String defaultGroup = defaultGroups[i];
			list.add(defaultGroup);
		}
		Iterator<TraceGroup> groups = TraceBuilderGlobals.getTraceModel()
				.getGroups();
		while (groups.hasNext()) {
			String name = groups.next().getName();
			if (!list.contains(name)) {
				list.add(name);
			}
		}
		return list.iterator();
	}
}
