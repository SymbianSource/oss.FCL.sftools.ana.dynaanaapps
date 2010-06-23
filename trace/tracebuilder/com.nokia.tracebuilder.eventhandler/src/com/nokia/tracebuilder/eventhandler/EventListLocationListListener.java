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
* Content provider for the event view
*
*/
package com.nokia.tracebuilder.eventhandler;

import com.nokia.trace.eventview.TraceEventList;
import com.nokia.tracebuilder.engine.LastKnownLocation;
import com.nokia.tracebuilder.engine.LastKnownLocationListListener;
import com.nokia.tracebuilder.engine.TraceLocation;
import com.nokia.tracebuilder.engine.TraceLocationListListener;
import com.nokia.tracebuilder.model.Trace;

/**
 * Location list listener for event view collects invalid locations to the event
 * list
 * 
 */
final class EventListLocationListListener implements TraceLocationListListener,
		LastKnownLocationListListener {

	/**
	 * Event handler
	 */
	private TraceBuilderEventHandler eventHandler;

	/**
	 * Location validity listener
	 */
	private EventListLocationValidityListener validityListener;

	/**
	 * Constructor
	 * 
	 * @param eventHandler
	 *            the event handler
	 * @param eventList
	 *            the event list
	 */
	EventListLocationListListener(TraceBuilderEventHandler eventHandler,
			TraceEventList eventList) {
		this.eventHandler = eventHandler;
		validityListener = new EventListLocationValidityListener(eventList);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.engine.TraceLocationListListener#
	 *      locationAdded(com.nokia.tracebuilder.engine.TraceLocation)
	 */
	public void locationAdded(TraceLocation location) {
		// If a location is moved from list to another, it will already
		// contain a validity listener
		Object existing = location.getProperties().getEventViewReference();
		if (existing == null) {
			location.addLocationListener(validityListener);
			location.getProperties().setEventViewReference(validityListener);
		}
		// If the trace contains a location warning extension, it is updated
		Trace trace = location.getTrace();
		if (trace != null) {
			eventHandler.checkTraceValidity(trace);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.engine.TraceLocationListListener#
	 *      locationRemoved(com.nokia.tracebuilder.engine.TraceLocation)
	 */
	public void locationRemoved(TraceLocation location) {
		// The listener is not removed here, since this is also called when
		// the location is moved from one location list to another.
		// The listener removes itself from the location when the location
		// is actually deleted
		Trace trace = location.getTrace();
		if (trace != null) {
			eventHandler.checkTraceValidity(trace);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.engine.LastKnownLocationListListener#
	 *      locationAdded(com.nokia.tracebuilder.engine.LastKnownLocation)
	 */
	public void locationAdded(LastKnownLocation location) {
		// Last known locations cannot be added without removing normal
		// locations -> Trace cannot have a HasNoLocations entry here
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.engine.LastKnownLocationListListener#
	 *      locationRemoved(com.nokia.tracebuilder.engine.LastKnownLocation)
	 */
	public void locationRemoved(LastKnownLocation location) {
		// The listener is not removed here, since this is also called when
		// the location is moved from one location list to another.
		// The listener removes itself from the location when the location
		// is actually deleted
		Trace trace = location.getTrace();
		if (trace != null) {
			eventHandler.checkTraceValidity(trace);
		}
	}

}