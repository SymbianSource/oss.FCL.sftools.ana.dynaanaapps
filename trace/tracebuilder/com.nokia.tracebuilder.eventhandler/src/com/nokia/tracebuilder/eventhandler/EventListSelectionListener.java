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
* Selection listener for the event view
*
*/
package com.nokia.tracebuilder.eventhandler;

import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;

import com.nokia.trace.eventview.EventListEntry;
import com.nokia.tracebuilder.engine.TraceBuilderGlobals;
import com.nokia.tracebuilder.engine.TraceLocation;
import com.nokia.tracebuilder.source.SourceLocation;

/**
 * Selection listener for the event view
 * 
 */
final class EventListSelectionListener implements ISelectionChangedListener {

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.ISelectionChangedListener#
	 *      selectionChanged(org.eclipse.jface.viewers.SelectionChangedEvent)
	 */
	public void selectionChanged(SelectionChangedEvent event) {
		IStructuredSelection selection = (IStructuredSelection) event
				.getSelection();
		if (!selection.isEmpty()) {
			EventListEntry entry = (EventListEntry) selection.getFirstElement();
			if (entry instanceof EventListEntryTraceObject) {
				TraceBuilderGlobals.getTraceBuilder().traceObjectSelected(
						((EventListEntryTraceObject) entry).getObject(), true, false);
			} else if (entry instanceof EventListEntryTraceLocation) {
				TraceLocation location = ((EventListEntryTraceLocation) entry)
						.getTraceLocation();
				if (!location.isDeleted()) {
					TraceBuilderGlobals.getTraceBuilder().locationSelected(
							location.getLocationList(), location, true);
				}
			} else if (entry instanceof EventListEntrySourceLocation) {
				SourceLocation location = ((EventListEntrySourceLocation) entry)
						.getLocation();
				location.selectFromSource();
			}
		}
	}
}