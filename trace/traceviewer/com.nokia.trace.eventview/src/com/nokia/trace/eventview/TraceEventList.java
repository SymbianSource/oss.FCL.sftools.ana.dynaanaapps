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
 * Interface to the events list of the view
 *
 */
package com.nokia.trace.eventview;

import java.util.Iterator;

import org.eclipse.jface.viewers.ISelectionChangedListener;

/**
 * Interface to the events list of the view. An instance of this interface is
 * available via {@link TraceEventView#getEventList()
 * TraceEventView.getEventList}
 * 
 */
public interface TraceEventList extends Iterable<EventListEntry> {

	/**
	 * Adds an entry to this list
	 * 
	 * @param entry
	 *            the entry to be added
	 */
	public void addEntry(EventListEntry entry);

	/**
	 * Removes an entry from this list
	 * 
	 * @param entry
	 *            the entry to be removed
	 */
	public void removeEntry(EventListEntry entry);

	/**
	 * Updates an existing entry
	 * 
	 * @param entry
	 *            the entry to be updated
	 */
	public void updateEntry(EventListEntry entry);

	/**
	 * Runs an asynchronous operation using the event view shell
	 * 
	 * @param runnable
	 *            the runnable
	 */
	public void asyncExec(Runnable runnable);

	/**
	 * Adds an event handler, which gets notified when events arrive to view
	 * 
	 * @param handler
	 *            the event handler
	 */
	public void addEventHandler(TraceEventHandler handler);

	/**
	 * Removes an event handler
	 * 
	 * @param handler
	 *            the event handler
	 */
	public void removeEventHandler(TraceEventHandler handler);

	/**
	 * Removes all events which have a source of given type
	 * 
	 * @param type
	 *            the type
	 */
	public void removeAll(Class<?> type);

	/**
	 * Gets all entries which have the given source
	 * 
	 * @param source
	 *            the source
	 * @return the entries with given source
	 */
	public Iterator<EventListEntry> getEntries(Object source);

	/**
	 * Adds a selection listener to the UI
	 * 
	 * @param listener
	 *            the selection listener
	 */
	public void addSelectionListener(ISelectionChangedListener listener);

	/**
	 * Removes a selection listener
	 * 
	 * @param listener
	 *            the selection listener
	 */
	public void removeSelectionListener(ISelectionChangedListener listener);

	/**
	 * Is viewer enabled
	 * 
	 * @return true if viewer is enable, otherwise false
	 */
	public boolean isViewerEnabled();
}
