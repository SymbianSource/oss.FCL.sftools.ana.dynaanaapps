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
* Wrapper for list of traces
*
*/
package com.nokia.tracebuilder.view;

import java.util.Iterator;

import com.nokia.tracebuilder.model.Trace;
import com.nokia.tracebuilder.model.TraceGroup;

/**
 * Wrapper for list of traces
 * 
 */
final class TraceListWrapper extends TraceObjectListWrapper {

	/**
	 * Constructor.
	 * 
	 * @param group
	 *            the owning trace group
	 * @param parent
	 *            the parent wrapper
	 * @param updater
	 *            the update notifier
	 */
	TraceListWrapper(TraceGroup group, WrapperBase parent,
			WrapperUpdater updater) {
		super(parent, updater);
		Iterator<Trace> itr = group.getTraces();
		while (itr.hasNext()) {
			addTrace(itr.next());
		}
	}

	/**
	 * Adds a new trace to this list
	 * 
	 * @param trace
	 *            the new trace
	 * @return the wrapper to be refreshed
	 */
	WrapperBase addTrace(Trace trace) {
		add(new TraceWrapper(trace, this, getUpdater()));
		return this;
	}

	/**
	 * Removes a trace from this list.
	 * 
	 * @param trace
	 *            the trace to be removed
	 * @return the wrapper to be refreshed
	 */
	WrapperBase removeTrace(Trace trace) {
		remove(trace.getExtension(TraceWrapper.class));
		return this;
	}
}