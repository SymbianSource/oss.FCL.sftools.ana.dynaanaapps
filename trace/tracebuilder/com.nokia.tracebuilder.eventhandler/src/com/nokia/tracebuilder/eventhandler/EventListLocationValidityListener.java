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
* Location validity listener
*
*/
package com.nokia.tracebuilder.eventhandler;

import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

import com.nokia.trace.eventview.TraceEventList;
import com.nokia.tracebuilder.engine.TraceLocation;
import com.nokia.tracebuilder.engine.TraceLocationListener;
import com.nokia.tracebuilder.engine.TraceBuilderErrorCodes.TraceBuilderErrorCode;
import com.nokia.tracebuilder.source.SourceLocation;

/**
 * Monitors the validity of a source location and creates an entry to the view
 * when the location becomes invalid. The entry removes itself when the location
 * becomes invalid.
 * 
 */
final class EventListLocationValidityListener implements TraceLocationListener {

	/**
	 * TraceEventView ID
	 */
	private static final String TRACEVENTVIEW_ID = "com.nokia.trace.eventview.TraceEventView"; //$NON-NLS-1$
	
	/**
	 * Event list
	 */
	private TraceEventList eventList;

	/**
	 * Constructor
	 * 
	 * @param eventList
	 *            the event list
	 */
	EventListLocationValidityListener(TraceEventList eventList) {
		this.eventList = eventList;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.source.SourceLocationListener#
	 *      locationChanged(com.nokia.tracebuilder.source.SourceLocation)
	 */
	public void locationChanged(SourceLocation location) {
		// Processed by the entry
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.source.SourceLocationListener#
	 *      locationDeleted(com.nokia.tracebuilder.source.SourceLocation)
	 */
	public void locationDeleted(SourceLocation location) {
		// The entry removes itself from the view
		location.removeLocationListener(this);
		((TraceLocation) location).getProperties().setEventViewReference(null);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.engine.TraceLocationListener#locationContentChanged()
	 */
	public void locationContentChanged(TraceLocation location) {
		// Processed by the entry
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.engine.TraceLocationListener#locationValidityChanged()
	 */
	public void locationValidityChanged(TraceLocation location) {
		// The entry removes itself from the view when location becomes valid.
		// This listener remains in the location and creates a new view
		// entry if location becomes invalid again
		if (location.getValidityCode() != TraceBuilderErrorCode.OK) {
			if (!eventList.isViewerEnabled()) {
				try {
					PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().showView(TRACEVENTVIEW_ID);
				} catch (PartInitException e) {

				}
			}
			eventList.asyncExec(new InvalidLocationEntryAdder(eventList,
					location));
		}
	}
}
