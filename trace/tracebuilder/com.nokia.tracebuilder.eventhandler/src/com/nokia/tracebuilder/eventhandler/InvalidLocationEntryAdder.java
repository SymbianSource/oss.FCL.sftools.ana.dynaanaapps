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
 * Asynchronously adds an invalid location to the events list
 *
 */
package com.nokia.tracebuilder.eventhandler;

import java.util.Iterator;

import com.nokia.trace.eventrouter.TraceEvent;
import com.nokia.trace.eventview.EventListEntry;
import com.nokia.trace.eventview.TraceEventList;
import com.nokia.trace.eventview.TraceEventView;
import com.nokia.tracebuilder.engine.TraceLocation;
import com.nokia.tracebuilder.engine.TraceBuilderErrorCodes.TraceBuilderErrorCode;

/**
 * Asynchronously adds an invalid location to the events list
 * 
 */
final class InvalidLocationEntryAdder implements Runnable {

	/**
	 * Event list
	 */
	private TraceEventList eventList;

	/**
	 * The location to be added to the list
	 */
	private TraceLocation location;

	/**
	 * Constructor
	 * 
	 * @param eventList
	 *            the event list
	 * @param location
	 *            the location to be added
	 */
	InvalidLocationEntryAdder(TraceEventList eventList, TraceLocation location) {
		this.eventList = eventList;
		this.location = location;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Runnable#run()
	 */
	public void run() {
		// The location might changed to valid while waiting for async operation
		// to start
		if (!location.isDeleted()
				&& location.getValidityCode() != TraceBuilderErrorCode.OK) {
			boolean existingFound = false;
			Iterator<EventListEntry> itr = eventList.getEntries(location);
			while (itr.hasNext()) {
				EventListEntry e = itr.next();
				if (e instanceof EventListEntryInvalidTraceLocation) {
					existingFound = true;
				}
			}
			if (!existingFound) {
				// The entry is deleted when trace location is deleted
				// or when it becomes valid
				TraceBuilderErrorCode validityCode = location.getValidityCode();
				int errorType = TraceEvent.ERROR;

				// In case of "Trace needs to be converted to correct API"
				// validity code change error type to info
				if (validityCode
						.equals(TraceBuilderErrorCode.TRACE_NEEDS_CONVERSION)) {
					errorType = TraceEvent.INFO;
				}
				
				// Add new event to TraceEventView
				EventListEntryInvalidTraceLocation entry = new EventListEntryInvalidTraceLocation(
						errorType, eventList, location);
				eventList.addEntry(entry);
				
				// Inform TraceEventView that it's context has changed
				if (TraceEventView.contentChangeListener != null) {
					TraceEventView.contentChangeListener.contentChanged();
				}
				
				// Adds a reference to the location. The location is
				// dereferenced when removed from the list
				location.reference();
			}
		}
	}
}
