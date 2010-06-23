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
 * Content provider for the event view
 *
 */
package com.nokia.trace.eventview;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.widgets.Table;
import org.eclipse.ui.PlatformUI;

import com.nokia.trace.eventrouter.TraceEvent;
import com.nokia.trace.eventrouter.TraceEventListener;
import com.nokia.trace.eventrouter.TraceEventRouter;

/**
 * Content provider for the event view
 * 
 */
final class EventListContentProvider implements IStructuredContentProvider,
		TraceEventListener, TraceEventList {

	/**
	 * ID of the event view
	 */
	private static final String EVENTVIEW_ID = "com.nokia.trace.eventview.TraceEventView"; //$NON-NLS-1$

	/**
	 * The table view
	 */
	private TableViewer viewer;

	/**
	 * The list of viewer elements
	 */
	private ArrayList<EventListEntry> elements = new ArrayList<EventListEntry>();

	/**
	 * Event handlers
	 */
	private ArrayList<TraceEventHandler> eventHandlers = new ArrayList<TraceEventHandler>();

	/**
	 * Selection listeners
	 */
	private ArrayList<ISelectionChangedListener> selectionListeners;

	/**
	 * Constructor
	 * 
	 */
	EventListContentProvider() {
		TraceEventRouter router = TraceEventRouter.getInstance();
		if (router != null) {
			router.addEventListener(this);
		}
	}

	/**
	 * Removes the event listener from router
	 */
	void removeListener() {
		TraceEventRouter router = TraceEventRouter.getInstance();
		if (router != null) {
			router.removeEventListener(this);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.trace.eventview.TraceEventList#
	 * addEntry(com.nokia.trace.eventview.EventListEntry)
	 */
	public void addEntry(EventListEntry entry) {
		elements.add(entry);
		if (viewer != null) {
			viewer.add(entry);
			viewer.reveal(entry);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.trace.eventview.TraceEventList#
	 * removeEntry(com.nokia.trace.eventview.EventListEntry)
	 */
	public void removeEntry(EventListEntry entry) {
		boolean removed = elements.remove(entry);
		if (removed && viewer != null) {
			viewer.remove(entry);
		}
		entry.reset();
	}

	/**
	 * Removes all entries
	 */
	void removeAllEntries() {
		if (viewer != null) {
			viewer.getTable().removeAll();
		}
		for (EventListEntry entry : elements) {
			entry.reset();
		}
		elements.clear();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.trace.eventview.TraceEventList#
	 * updateEntry(com.nokia.trace.eventview.EventListEntry)
	 */
	public void updateEntry(EventListEntry entry) {
		if (viewer != null) {
			viewer.update(entry, null);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jface.viewers.IStructuredContentProvider#getElements(java
	 * .lang.Object)
	 */
	public Object[] getElements(Object inputElement) {
		return elements.toArray();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.IContentProvider#dispose()
	 */
	public void dispose() {
		viewer = null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.IContentProvider#
	 * inputChanged(org.eclipse.jface.viewers.Viewer, java.lang.Object,
	 * java.lang.Object)
	 */
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		boolean changed = false;
		if (this.viewer != viewer) {
			changed = true;
		}
		if (changed) {
			this.viewer = (TableViewer) viewer;
			if (selectionListeners != null) {
				for (int i = 0; i < selectionListeners.size(); i++) {
					viewer.addSelectionChangedListener(selectionListeners
							.get(i));
				}
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.trace.eventrouter.TraceEventListener#
	 * processEvent(com.nokia.trace.eventrouter.TraceEvent)
	 */
	public void processEvent(TraceEvent event) {
		boolean handled = false;
		for (int i = 0; i < eventHandlers.size(); i++) {
			handled = eventHandlers.get(i).handleEvent(event);
			if (handled) {
				i = eventHandlers.size();
			}
		}
		if (handled) {
			// If event was handled by one of the view extensions, the focus
			// needs to be switched to the view
			setFocus();
		} else {
			if (event.getSource() == null
					|| event.getSource() instanceof String
					|| event.getSource() instanceof Throwable) {
				setFocus();
				asyncExec(new EventAdder(this, event));
			}
		}
	}

	/**
	 * Sets focus to the event view
	 */
	private void setFocus() {
		if (!isViewerEnabled()) {
			PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
				public void run() {
					try {
						PlatformUI.getWorkbench().getActiveWorkbenchWindow()
								.getActivePage().showView(EVENTVIEW_ID);
					} catch (Exception e) {
					}
				}
			});
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.nokia.trace.eventview.TraceEventList#asyncExec(java.lang.Runnable)
	 */
	public void asyncExec(Runnable runnable) {
		if (viewer != null) {
			Table table = viewer.getTable();
			if (!table.isDisposed()) {
				table.getDisplay().asyncExec(runnable);
			}

			// EventAdder events can be inserted even when not in UI thread
		} else if (runnable instanceof EventAdder) {
			runnable.run();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.trace.eventview.TraceEventList#
	 * addEventHandler(com.nokia.trace.eventview.TraceEventHandler)
	 */
	public void addEventHandler(TraceEventHandler handler) {
		eventHandlers.add(handler);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.trace.eventview.TraceEventList#
	 * removeEventHandler(com.nokia.trace.eventview.TraceEventHandler)
	 */
	public void removeEventHandler(TraceEventHandler handler) {
		eventHandlers.remove(handler);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.trace.eventview.TraceEventList#removeAll(java.lang.Class)
	 */
	public void removeAll(Class<?> type) {
		for (int i = 0; i < elements.size(); i++) {
			EventListEntry entry = elements.get(i);
			if (entry.hasSource()
					&& type.isAssignableFrom(entry.getSource().getClass())) {
				elements.remove(i);
				i--;
			}
		}
		if (viewer != null) {
			viewer.refresh();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.trace.eventview.TraceEventList#
	 * addSelectionListener(org.eclipse.jface.viewers.ISelectionChangedListener)
	 */
	public void addSelectionListener(ISelectionChangedListener listener) {
		if (viewer != null) {
			viewer.addSelectionChangedListener(listener);
		}
		if (selectionListeners == null) {
			selectionListeners = new ArrayList<ISelectionChangedListener>();
		}
		selectionListeners.add(listener);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.trace.eventview.TraceEventList#
	 * removeSelectionListener(org
	 * .eclipse.jface.viewers.ISelectionChangedListener)
	 */
	public void removeSelectionListener(ISelectionChangedListener listener) {
		if (viewer != null) {
			viewer.removeSelectionChangedListener(listener);
		}
		if (selectionListeners != null) {
			selectionListeners.remove(listener);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Iterable#iterator()
	 */
	public Iterator<EventListEntry> iterator() {
		return elements.iterator();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.nokia.trace.eventview.TraceEventList#getEntries(java.lang.Object)
	 */
	public Iterator<EventListEntry> getEntries(Object source) {
		List<EventListEntry> entries = null;
		for (EventListEntry entry : elements) {
			if (entry.getSource() == source) {
				if (entries == null) {
					entries = new ArrayList<EventListEntry>();
				}
				entries.add(entry);
			}
		}
		if (entries == null) {
			entries = Collections.emptyList();
		}
		return entries.iterator();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.trace.eventview.TraceEventList#isViewerEnabled()
	 */
	public boolean isViewerEnabled() {
		boolean retVal = true;
		if (viewer == null) {
			retVal = false;
		}
		return retVal;
	}
}