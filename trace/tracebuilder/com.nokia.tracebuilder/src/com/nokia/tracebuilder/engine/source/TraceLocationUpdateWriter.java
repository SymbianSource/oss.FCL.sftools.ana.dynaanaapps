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
* SourceEditorUpdater instance to replace an existing trace location
*
*/
package com.nokia.tracebuilder.engine.source;

import com.nokia.tracebuilder.engine.TraceLocation;
import com.nokia.tracebuilder.model.Trace;
import com.nokia.tracebuilder.source.SourceLocationBase;
import com.nokia.tracebuilder.source.SourceParserException;

/**
 * SourceEditorUpdater instance to replace an existing trace location
 * 
 */
class TraceLocationUpdateWriter extends SourceEditorUpdater {

	/**
	 * The trace location to be updated
	 */
	private TraceLocation location;

	/**
	 * Creates a new location writer for given location
	 * 
	 * @param location
	 *            the location to be written to source
	 */
	TraceLocationUpdateWriter(TraceLocation location) {
		super(location.getSource());
		this.location = location;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.engine.source.SourceEditorUpdater#runUpdate()
	 */
	@Override
	protected boolean runUpdate() throws SourceParserException {
		// If location has been deleted, it cannot be updated
		boolean updated;
		if (!location.isDeleted()) {
			Trace trace = location.getTrace();
			int start = TraceLocationRemover.removeLocation(location);
			location.getSource().internalInsertTrace(trace, start, location,
					location.getProperties());
			updated = true;
		} else {
			updated = false;
		}
		return updated;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.engine.source.SourceEditorUpdater#getPosition()
	 */
	@Override
	protected SourceLocationBase getPosition() {
		return location;
	}
}
