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
* Event list entry, which contains a reference to TraceLocation
*
*/
package com.nokia.tracebuilder.eventhandler;

import org.eclipse.jface.action.IMenuManager;

import com.nokia.trace.eventview.TraceEventList;
import com.nokia.tracebuilder.action.TraceViewActions;
import com.nokia.tracebuilder.engine.TraceBuilderGlobals;
import com.nokia.tracebuilder.engine.TraceLocation;
import com.nokia.tracebuilder.engine.TraceLocationListener;

/**
 * Event list entry, which contains a reference to TraceLocation
 * 
 */
class EventListEntryTraceLocation extends EventListEntrySourceLocation
		implements TraceLocationListener {

	/**
	 * Constructor
	 * 
	 * @param type
	 *            event type
	 * @param description
	 *            event description
	 * @param eventList
	 *            the event list
	 * @param location
	 *            the event location
	 */
	public EventListEntryTraceLocation(int type, String description,
			TraceEventList eventList, TraceLocation location) {
		super(type, description, eventList, location);
	}

	/**
	 * Gets the location
	 * 
	 * @return the location
	 */
	public TraceLocation getTraceLocation() {
		return (TraceLocation) location;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.trace.eventview.EventListEntryString#hasSourceActions()
	 */
	@Override
	protected boolean hasSourceActions() {
		return hasSource();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.trace.eventview.EventListEntryString#
	 *      addSourceActions(org.eclipse.jface.action.IMenuManager)
	 */
	@Override
	protected void addSourceActions(IMenuManager manager) {
		((TraceViewActions) TraceBuilderGlobals.getActions())
				.fillContextMenu(manager);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.engine.TraceLocationListener#
	 *      locationContentChanged(com.nokia.tracebuilder.engine.TraceLocation)
	 */
	public void locationContentChanged(TraceLocation location) {
		eventList.updateEntry(this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.engine.TraceLocationListener#
	 *      locationValidityChanged(com.nokia.tracebuilder.engine.TraceLocation)
	 */
	public void locationValidityChanged(TraceLocation location) {
	}

}
