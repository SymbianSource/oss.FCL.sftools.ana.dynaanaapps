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
* Asynchronously adds an event to the events list
*
*/
package com.nokia.tracebuilder.eventhandler;

import com.nokia.trace.eventrouter.TraceEvent;
import com.nokia.trace.eventview.EventListEntry;
import com.nokia.trace.eventview.TraceEventList;
import com.nokia.tracebuilder.engine.TraceLocation;
import com.nokia.tracebuilder.model.TraceObject;
import com.nokia.tracebuilder.source.SourceLocation;

/**
 * Asynchronously adds an event to the events list
 * 
 */
final class EventAdder implements Runnable {

	/**
	 * Event to be added
	 */
	private TraceEvent event;

	/**
	 * The event list
	 */
	private TraceEventList eventList;

	/**
	 * Constructor
	 * 
	 * @param eventList
	 *            the event list
	 * @param event
	 *            the event to be added
	 */
	EventAdder(TraceEventList eventList, TraceEvent event) {
		this.event = event;
		this.eventList = eventList;
		Object o = event.getSource();
		// Adds a reference so the location does not get removed from the source
		if (o instanceof SourceLocation) {
			((SourceLocation) o).reference();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Runnable#run()
	 */
	public void run() {
		Object o = event.getSource();
		EventListEntry entry = null;
		if (o instanceof TraceLocation) {
			// Location delete flag needs to be checked, since this is
			// called asynchronously. The location might have been deleted
			TraceLocation location = (TraceLocation) o;
			if (!location.isDeleted()) {
				entry = new EventListEntryTraceLocation(event.getType(), event
						.getDescription(), eventList, location);
			}
		} else if (o instanceof TraceObject) {
			TraceObject object = (TraceObject) o;
			entry = new EventListEntryTraceObject(event.getType(), event
					.getDescription(), object);
		} else if (o instanceof SourceLocation) {
			SourceLocation location = (SourceLocation) o;
			if (!location.isDeleted()) {
				entry = new EventListEntrySourceLocation(event.getType(), event
						.getDescription(), eventList, location);
			}
		}
		if (entry != null) {
			entry.setCategory(event.getCategory());
			eventList.addEntry(entry);
		}
	}

}
