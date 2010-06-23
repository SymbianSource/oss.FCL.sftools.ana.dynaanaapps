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
 * Trace Fetcher gets traces from file to a arraylist and returns it
 *
 */
package com.nokia.traceviewer.engine;

import java.util.ArrayList;
import java.util.List;

import com.nokia.traceviewer.engine.DataReader;
import com.nokia.traceviewer.engine.MediaCallback;
import com.nokia.traceviewer.engine.TraceProperties;
import com.nokia.traceviewer.engine.TraceViewerGlobals;

/**
 * Trace Fetcher gets traces from file to a arraylist and returns it
 */
public final class TraceFetcher implements MediaCallback {

	/**
	 * Max number of traces to get
	 */
	private static int MAX_TRACES_TO_GET = TraceViewerGlobals.blockSize * 5;

	/**
	 * Array to gather traces to
	 */
	private List<TraceProperties> traceArray;

	/**
	 * Tells how many traces to drop from the beginning
	 */
	private int tracesToDrop;

	/**
	 * Tells how many traces to get totally
	 */
	private int tracesToGet;

	/**
	 * Trace count by this far
	 */
	private int traceCount;

	/**
	 * Data reader
	 */
	private DataScrollReader dataReader;

	/**
	 * Constructor
	 */
	public TraceFetcher() {
		traceCount = 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.nokia.traceviewer.engine.MediaCallback#processTrace(com.nokia.traceviewer
	 * .engine.TraceProperties)
	 */
	public void processTrace(TraceProperties properties) {
		traceCount++;
		int tracesSaved = traceCount - tracesToDrop;

		// Save traces we want to trace array
		if (traceCount > tracesToDrop && tracesSaved <= tracesToGet) {
			traceArray.add(properties);
		}
	}

	/**
	 * Starts gathering traces
	 * 
	 * @param start
	 *            start trace
	 * @param end
	 *            end trace
	 * @return the trace array or null if there is no current data reader
	 */
	public List<TraceProperties> startGatheringData(int start, int end) {
		traceArray = null;

		// Check that current data reader exists
		DataReader currentDataReader = TraceViewerGlobals.getTraceViewer()
				.getDataReaderAccess().getCurrentDataReader();

		if (currentDataReader != null) {

			// Get the file position having given offset
			int index = currentDataReader.getFileMap()
					.getIndexFromOffset(start);
			long pos = currentDataReader.getFileMap().getItem(index)
					.longValue();
			int startTrace = index * TraceViewerGlobals.blockSize;

			// Calculate how many traces to drop from start
			traceCount = 0;
			tracesToDrop = start - startTrace;
			tracesToGet = end - start + 1;
			if (tracesToGet > MAX_TRACES_TO_GET) {
				tracesToGet = MAX_TRACES_TO_GET;
			}

			// Number of blocks
			int numberOfBlocks = (tracesToGet / TraceViewerGlobals.blockSize);

			// If modulo is not zero, we will need one block more
			if (tracesToGet % TraceViewerGlobals.blockSize != 0) {
				numberOfBlocks++;
			}

			// If We are dropping traces from the first block we are reading, we
			// might need to take one block more
			if (numberOfBlocks * TraceViewerGlobals.blockSize - tracesToDrop < tracesToGet) {
				numberOfBlocks++;
			}

			traceArray = new ArrayList<TraceProperties>();

			// Create own data reader if it doesn't exist and start it. Put it
			// to blocking mode.
			dataReader = TraceViewerGlobals.getTraceViewer()
					.getDataReaderAccess().startOwnDataReader(dataReader, this,
							numberOfBlocks, pos, startTrace, true);
		}
		return traceArray;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.nokia.traceviewer.engine.MediaCallback#endOfFile(com.nokia.traceviewer
	 * .engine.DataReader)
	 */
	public void endOfFile(DataReader reader) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.traceviewer.engine.MediaCallback#dataHandleChanged()
	 */
	public void dataHandleChanged() {
		if (dataReader != null) {
			dataReader.shutdown();
			dataReader = null;
		}
	}

}
