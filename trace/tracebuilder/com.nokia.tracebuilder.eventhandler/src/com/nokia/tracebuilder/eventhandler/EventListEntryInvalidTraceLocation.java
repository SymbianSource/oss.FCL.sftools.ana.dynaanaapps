/*
* Copyright (c) 2008 Nokia Corporation and/or its subsidiary(-ies). 
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
* An error that location is not valid
*
*/
package com.nokia.tracebuilder.eventhandler;

import com.nokia.trace.eventview.TraceEventList;
import com.nokia.tracebuilder.engine.TraceBuilderErrorMessages;
import com.nokia.tracebuilder.engine.TraceLocation;
import com.nokia.tracebuilder.engine.TraceBuilderErrorCodes.TraceBuilderErrorCode;

/**
 * An error that location is not valid
 * 
 */
final class EventListEntryInvalidTraceLocation extends
		EventListEntryTraceLocation {

	/**
	 * Constructor
	 * @param errorType 
	 *            the error type 				
	 * @param eventList
	 *            the event list
	 * @param location
	 *            the location
	 */
	EventListEntryInvalidTraceLocation(int errorType, TraceEventList eventList,
			TraceLocation location) {
		super(errorType, TraceBuilderErrorMessages.getErrorMessage(
				location.getValidityCode(), location.getValidityParameters()),
				eventList, location);
		setCategory(Messages
				.getString("InvalidLocationEntryAdder.InvalidLocationEventCategory")); //$NON-NLS-1$
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.eventhandler.EventListEntryTraceLocation#
	 *      locationValid(boolean)
	 */
	@Override
	public void locationValidityChanged(TraceLocation location) {
		TraceBuilderErrorCode code = location.getValidityCode();
		if (code == TraceBuilderErrorCode.OK) {
			eventList.removeEntry(this);
		} else {
			setDescription(TraceBuilderErrorMessages
			.getErrorMessage(code, location.getValidityParameters()));
			
		}
	}

}
