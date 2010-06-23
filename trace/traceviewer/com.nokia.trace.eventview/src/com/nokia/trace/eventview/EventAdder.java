/*
 * Copyright (c) 2007-2010 Nokia Corporation and/or its subsidiary(-ies). 
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
package com.nokia.trace.eventview;

import com.nokia.trace.eventrouter.TraceEvent;

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
	 * The content provider
	 */
	private EventListContentProvider contentProvider;

	/**
	 * Constructor
	 * 
	 * @param contentProvider
	 *            the content provider
	 * @param event
	 *            the event to be added
	 */
	public EventAdder(EventListContentProvider contentProvider, TraceEvent event) {
		this.event = event;
		this.contentProvider = contentProvider;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Runnable#run()
	 */
	public void run() {
		Object o = event.getSource();
		EventListEntry entry = null;
		if (o instanceof Throwable) {
			entry = new EventListEntryThrowable(event.getType(), event
					.getDescription(), (Throwable) o);
		} else if (o instanceof String) {
			entry = new EventListEntryString(event.getType(), event
					.getDescription(), (String) o);
		} else {
			entry = new EventListEntryString(event.getType(), event
					.getDescription(), null);
		}
		entry.setCategory(event.getCategory());
		contentProvider.addEntry(entry);
		if (TraceEventView.contentChangeListener != null) {
			TraceEventView.contentChangeListener.contentChanged();
		}
	}

}
