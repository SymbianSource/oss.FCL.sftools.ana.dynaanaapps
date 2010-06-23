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
* An error that trace does not have or has more than one location
*
*/
package com.nokia.tracebuilder.eventhandler;

import com.nokia.trace.eventrouter.TraceEvent;
import com.nokia.trace.eventview.TraceEventList;
import com.nokia.tracebuilder.engine.TraceBuilderErrorMessages;
import com.nokia.tracebuilder.engine.TraceLocationList;
import com.nokia.tracebuilder.engine.TraceBuilderErrorCodes.TraceBuilderErrorCode;
import com.nokia.tracebuilder.model.Trace;

/**
 * An error that trace does not have or has more than one location
 * 
 */
class EventListEntryLocationCountError extends EventListEntryTraceObject {

	/**
	 * The event list
	 */
	private TraceEventList eventList;

	/**
	 * Constructor
	 * 
	 * @param eventList
	 *            the event list
	 * @param trace
	 *            the trace
	 */
	EventListEntryLocationCountError(TraceEventList eventList, Trace trace) {
		super(TraceEvent.ERROR, "", trace); //$NON-NLS-1$
		String category = Messages
				.getString("InvalidLocationEntryAdder.InvalidLocationEventCategory"); //$NON-NLS-1$
		setCategory(category);
		this.eventList = eventList;
		update();
	}

	/**
	 * Updates the description of this error
	 */
	void update() {
		TraceLocationList list = getObject().getExtension(
				TraceLocationList.class);
		String msg;
		if (list == null || list.getLocationCount() == 0) {
			msg = TraceBuilderErrorMessages.getErrorMessage(
					TraceBuilderErrorCode.TRACE_HAS_NO_LOCATIONS, null);
		} else {
			msg = TraceBuilderErrorMessages.getErrorMessage(
					TraceBuilderErrorCode.TRACE_HAS_MULTIPLE_LOCATIONS, null);
		}
		setDescription(msg);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.eventhandler.EventListEntryTraceObject#objectDeleted()
	 */
	@Override
	public void objectDeleted() {
		eventList.removeEntry(this);
	}
}