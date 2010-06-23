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
* Wrapper for TraceLocation
*
*/
package com.nokia.tracebuilder.view;

import com.nokia.tracebuilder.engine.TraceLocation;
import com.nokia.tracebuilder.engine.TraceLocationListener;
import com.nokia.tracebuilder.source.SourceLocation;

/**
 * Wrapper for TraceLocation
 * 
 */
final class TraceLocationWrapper extends WrapperBase implements
		TraceLocationListener {

	/**
	 * The trace location
	 */
	private TraceLocation location;

	/**
	 * Filters out notifications where line was not changed
	 */
	private int lastNotifiedLineNumber;

	/**
	 * Constructor
	 * 
	 * @param location
	 *            the trace location
	 * @param parent
	 *            the parent wrapper
	 * @param updater
	 *            the update notifier
	 */
	TraceLocationWrapper(TraceLocation location, WrapperBase parent,
			WrapperUpdater updater) {
		super(parent, updater);
		this.location = location;
		location.getProperties().setViewReference(this);
		location.addLocationListener(this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.view.WrapperBase#getChildren()
	 */
	@Override
	Object[] getChildren() {
		return new Object[0];
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.view.WrapperBase#hasChildren()
	 */
	@Override
	boolean hasChildren() {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.view.WrapperBase#delete()
	 */
	@Override
	void delete() {
		super.delete();
		location.removeLocationListener(this);
		location.getProperties().setViewReference(null);
		location = null;
	}

	/**
	 * Gets the wrapped location
	 * 
	 * @return the location
	 */
	TraceLocation getLocation() {
		return location;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.engine.TraceLocationListener#
	 *      locationValidityChanged(com.nokia.tracebuilder.engine.TraceLocation)
	 */
	public void locationValidityChanged(TraceLocation location) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.source.SourceLocationListener#
	 *      locationDeleted(com.nokia.tracebuilder.source.SourceLocation)
	 */
	public void locationDeleted(SourceLocation location) {
		// Processed in location list wrapper when location is removed from
		// location list
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.engine.TraceLocationListener#locationContentChanged()
	 */
	public void locationContentChanged(TraceLocation location) {
		// Name update
		if (isInView()) {
			getUpdater().queueUpdate(this);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.source.SourceLocationListener#locationChanged()
	 */
	public void locationChanged(SourceLocation location) {
		int line = location.getLineNumber();
		if (line != lastNotifiedLineNumber) {
			lastNotifiedLineNumber = line;
			if (isInView()) {
				getUpdater().queueUpdate(this);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return location.getFileName() + ", " + location.getLineNumber(); //$NON-NLS-1$
	}

}
