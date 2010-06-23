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
* Wrapper for one trace group
*
*/
package com.nokia.tracebuilder.view;

import com.nokia.tracebuilder.model.Trace;
import com.nokia.tracebuilder.model.TraceGroup;

/**
 * Wrapper for one trace group
 * 
 * @see com.nokia.tracebuilder.model.TraceGroup
 */
final class TraceGroupWrapper extends TraceObjectWrapper {

	/**
	 * List of traces
	 */
	private TraceListWrapper traceListWrapper;

	/**
	 * Wrapper constructor
	 * 
	 * @param group
	 *            the trace group
	 * @param parent
	 *            parent wrapper
	 * @param updater
	 *            the update notifier
	 */
	public TraceGroupWrapper(TraceGroup group, WrapperBase parent,
			WrapperUpdater updater) {
		super(group, parent, updater);
		traceListWrapper = new TraceListWrapper(group, this, updater);
		if (traceListWrapper.hasChildren()) {
			add(traceListWrapper);
		}
	}

	/**
	 * Adds a trace to this group
	 * 
	 * @param trace
	 *            the trace to be added
	 * @return the wrapper which needs to be refreshed
	 */
	public WrapperBase addTrace(Trace trace) {
		WrapperBase wrapper;
		if (traceListWrapper.hasChildren()) {
			wrapper = traceListWrapper.addTrace(trace);
		} else {
			traceListWrapper.addTrace(trace);
			add(traceListWrapper);
			wrapper = this;
		}
		return wrapper;
	}

	/**
	 * Removes a trace
	 * 
	 * @param trace
	 *            the trace to be removed
	 * @return the wrapper which needs to be refreshed
	 */
	public WrapperBase removeTrace(Trace trace) {
		WrapperBase wrapper = traceListWrapper.removeTrace(trace);
		if (!traceListWrapper.hasChildren()) {
			hide(traceListWrapper);
			wrapper = this;
		}
		return wrapper;
	}

}