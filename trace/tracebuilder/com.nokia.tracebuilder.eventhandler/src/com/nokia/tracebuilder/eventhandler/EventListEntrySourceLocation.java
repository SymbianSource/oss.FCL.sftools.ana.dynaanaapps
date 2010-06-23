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

import com.nokia.trace.eventview.EventListEntryString;
import com.nokia.trace.eventview.TraceEventList;
import com.nokia.tracebuilder.source.SourceLocation;
import com.nokia.tracebuilder.source.SourceLocationListener;

/**
 * Event list entry, which contains a reference to SourceLocation
 * 
 */
class EventListEntrySourceLocation extends EventListEntryString implements
		SourceLocationListener {

	/**
	 * The location of this entry
	 */
	protected SourceLocation location;

	/**
	 * Event list
	 */
	protected TraceEventList eventList;

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
	public EventListEntrySourceLocation(int type, String description,
			TraceEventList eventList, SourceLocation location) {
		// getSourceName is overridden in this class -> Pass null to superclass
		super(type, description, null);
		this.eventList = eventList;
		this.location = location;
		location.addLocationListener(this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.trace.eventview.EventListEntryString#setDescription(java.lang.String)
	 */
	@Override
	protected void setDescription(String description) {
		super.setDescription(description);
		eventList.updateEntry(this);
	}

	/**
	 * Gets the location
	 * 
	 * @return the location
	 */
	public SourceLocation getLocation() {
		return location;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.trace.eventview.EventListEntryString#hasSource()
	 */
	@Override
	protected boolean hasSource() {
		return location != null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.source.SourceLocationListener#
	 *      locationDeleted(com.nokia.tracebuilder.source.SourceLocation)
	 */
	public void locationDeleted(SourceLocation location) {
		eventList.removeEntry(this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.source.SourceLocationListener#
	 *      locationChanged(com.nokia.tracebuilder.source.SourceLocation)
	 */
	public void locationChanged(SourceLocation location) {
		eventList.updateEntry(this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.trace.eventview.EventListEntry#reset()
	 */
	@Override
	protected void reset() {
		location.removeLocationListener(this);
		// This will cause a locationDeleted event -> Listener is removed first
		location.dereference();
		super.reset();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.trace.eventview.EventListEntryString#getSource()
	 */
	@Override
	protected Object getSource() {
		return location;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.trace.eventview.EventListEntryString#getSourceName()
	 */
	@Override
	protected String getSourceName() {
		StringBuffer buffer = new StringBuffer();
		buffer.append(location.getFileName());
		buffer.append(Messages
				.getString("EventListEntrySourceLocation.FileLineSeparator")); //$NON-NLS-1$
		buffer.append(location.getLineNumber());
		return buffer.toString();
	}

}
