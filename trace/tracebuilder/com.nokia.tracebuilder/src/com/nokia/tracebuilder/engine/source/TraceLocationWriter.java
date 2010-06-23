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
* Updates a trace location into source code
*
*/
package com.nokia.tracebuilder.engine.source;

import java.util.Iterator;

import com.nokia.tracebuilder.engine.TraceLocation;
import com.nokia.tracebuilder.engine.TraceLocationProperties;
import com.nokia.tracebuilder.model.Trace;
import com.nokia.tracebuilder.source.SourceExcludedArea;
import com.nokia.tracebuilder.source.SourceParserException;

/**
 * Updates trace locations into source code
 * 
 */
class TraceLocationWriter {

	/**
	 * Prevents construction
	 */
	private TraceLocationWriter() {
	}

	/**
	 * Queues a location update operation
	 * 
	 * @param location
	 *            the location to be updated
	 */
	static void updateLocation(TraceLocation location) {
		TraceLocationUpdateWriter writer = new TraceLocationUpdateWriter(
				location);
		writer.update();
	}

	/**
	 * Queues a add location operation
	 * 
	 * @param properties
	 *            the source properties
	 * @param trace
	 *            the trace to be added to source
	 * @param offset
	 *            the offset where trace is added
	 * @throws SourceParserException
	 *             if source parser fails
	 */
	static void addLocation(SourceProperties properties, Trace trace, int offset)
			throws SourceParserException {
		TraceLocationCreator writer = new TraceLocationCreator(properties,
				trace, offset, null);
		writer.update();
	}

	/**
	 * Queues a replace location operation
	 * 
	 * @param properties
	 *            the source properties
	 * @param trace
	 *            the trace to be added to source
	 * @param replaced
	 *            the location which is replaced by the trace
	 * @throws SourceParserException
	 *             if source parser fails
	 */
	static void replaceLocation(SourceProperties properties, Trace trace,
			TraceLocation replaced) throws SourceParserException {
		TraceLocationProperties locprops = new TraceLocationProperties();
		Iterator<String> itr = replaced.getParameters();
		while (itr.hasNext()) {
			locprops.addParameterTag(itr.next());
		}
		TraceLocationCreator replacer = new TraceLocationCreator(properties,
				trace, TraceLocationRemover.findStartOffset(replaced), locprops);
		replacer.update();
	}

	/**
	 * Queues a remove location operation
	 * 
	 * @param location
	 *            the location to be removed
	 */
	static void removeLocation(TraceLocation location) {
		TraceLocationRemover remover = new TraceLocationRemover(location);
		remover.update();
	}

	/**
	 * Locates the comment related to given location
	 * 
	 * @param location
	 *            the location
	 * @return index to the start of comment
	 */
	static SourceExcludedArea findLocationComment(TraceLocation location) {
		SourceExcludedArea retval = null;
		if (location.getParserRule() != null) {
			retval = location.getParserRule().getLocationParser()
					.findLocationComment(location);
		}
		return retval;
	}

}
