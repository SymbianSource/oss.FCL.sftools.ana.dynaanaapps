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
* Wrapper for a list of trace groups
*
*/
package com.nokia.tracebuilder.view;

import com.nokia.tracebuilder.model.TraceGroup;
import com.nokia.tracebuilder.model.TraceModel;

/**
 * Wrapper for a list of trace groups
 * 
 */
final class TraceGroupListWrapper extends TraceObjectListWrapper {

	/**
	 * Creates a new wrapper
	 * 
	 * @param model
	 *            trace model
	 * @param parent
	 *            parent wrapper
	 * @param updater
	 *            the update notifier
	 */
	TraceGroupListWrapper(TraceModel model, WrapperBase parent,
			WrapperUpdater updater) {
		super(parent, updater);
		for (TraceGroup group : model) {
			addGroup(group);
		}
	}

	/**
	 * Adds a new trace group
	 * 
	 * @param group
	 *            the group to be added
	 * @return the wrapper which needs to be refreshed
	 */
	WrapperBase addGroup(TraceGroup group) {
		add(new TraceGroupWrapper(group, this, getUpdater()));
		return this;
	}

	/**
	 * Removes a trace group
	 * 
	 * @param group
	 *            the group to be removed
	 * @return the wrapper which needs to be refreshed
	 */
	WrapperBase removeGroup(TraceGroup group) {
		remove(group.getExtension(TraceGroupWrapper.class));
		return this;
	}
}