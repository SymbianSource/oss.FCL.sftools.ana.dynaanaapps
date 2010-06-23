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
 * Copy Selection Data Fetcher gets data from file to the clipboard
 *
 */
package com.nokia.traceviewer.action;

import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.ui.PlatformUI;

import com.nokia.traceviewer.engine.DataReader;
import com.nokia.traceviewer.engine.DataScrollReader;
import com.nokia.traceviewer.engine.MediaCallback;
import com.nokia.traceviewer.engine.TraceProperties;
import com.nokia.traceviewer.engine.TraceViewerGlobals;
import com.nokia.traceviewer.view.listener.SelectionProperties;

/**
 * Copies selected data to the clipboard
 * 
 */
public final class CopySelectionDataFetcher implements MediaCallback {

	/**
	 * Max number of traces to copy to clipboard
	 */
	private static int MAX_TRACES_TO_COPY = 10000;

	/**
	 * String to attach to end of the clipboard if tried to copy more than
	 * MAX_TRACES_TO_COPY traces
	 */
	private static String MAX_STRING = Messages
			.getString("CopySelectionDataFetcher.MaxTracesLine1") //$NON-NLS-1$
			+ " " //$NON-NLS-1$
			+ MAX_TRACES_TO_COPY + " " //$NON-NLS-1$
			+ Messages.getString("CopySelectionDataFetcher.MaxTracesLine2"); //$NON-NLS-1$

	/**
	 * Buffer to gather data to
	 */
	private final StringBuffer dataBuffer;

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
	 * System Clipboard
	 */
	private final Clipboard clipboard;

	/**
	 * Own data reader reading the file
	 */
	private DataScrollReader dataReader;

	/**
	 * If already processing, don't start new process
	 */
	private boolean processing;

	/**
	 * Constructor
	 */
	public CopySelectionDataFetcher() {
		traceCount = 0;
		dataBuffer = new StringBuffer();
		clipboard = new Clipboard(PlatformUI.getWorkbench().getDisplay());
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

		// Process traces we want to clipboard
		if (traceCount > tracesToDrop
				&& traceCount - tracesToDrop <= tracesToGet) {

			// Null timestamp from first trace
			if (traceCount == tracesToDrop + 1) {
				TraceViewerGlobals.getTraceViewer().getDataProcessorAccess()
						.getTimestampParser().nullPreviousTimestamp();
			}

			// Parse timestamp
			TraceViewerGlobals.getTraceViewer().getDataProcessorAccess()
					.getTimestampParser().processData(properties);

			// If binary trace, decode it
			if (properties.binaryTrace) {
				TraceViewerGlobals.getTraceViewer().getDataProcessorAccess()
						.getDecoder().processData(properties);
			}

			// Append trace to buffer
			appendTraceToBuffer(properties);
		}

		// Last trace
		if (properties.lastTrace) {
			// Lines has been received, if there are MAX_TRACES_TO_COPY
			// Also add a note to user in the end of the data
			if (tracesToGet == MAX_TRACES_TO_COPY) {
				dataBuffer.append(MAX_STRING);
			}

			// Copy data to clipboard
			copyBufferToClipboard();
		}
	}

	/**
	 * Appends trace to buffer
	 * 
	 * @param trace
	 *            trace properties
	 */
	private void appendTraceToBuffer(TraceProperties trace) {

		// Check how many characters to skip and take before starting the first
		// and last trace
		int skip = 0;
		int take = 0;
		if (SelectionProperties.firstClickedLine <= SelectionProperties.lastClickedLine) {
			skip = SelectionProperties.firstClickedLineCaretOffset;
			take = SelectionProperties.lastClickedLineCaretOffset;
		} else {
			skip = SelectionProperties.lastClickedLineCaretOffset;
			take = SelectionProperties.firstClickedLineCaretOffset;
		}

		// First trace
		if (traceCount == tracesToDrop + 1) {

			StringBuffer str = new StringBuffer();
			addTraceTextToBuffer(trace, str);

			if (str.length() > skip) {
				str.delete(0, skip);
			} else {
				str.setLength(0);
			}

			dataBuffer.append(str);
			dataBuffer.append('\r');
			dataBuffer.append('\n');

			// Last trace
		} else if (traceCount - tracesToDrop == tracesToGet) {

			StringBuffer str = new StringBuffer();
			addTraceTextToBuffer(trace, str);

			if (str.length() > take) {
				str.setLength(take);
			}
			dataBuffer.append(str);
			if (traceCount - tracesToDrop == MAX_TRACES_TO_COPY) {
				dataBuffer.append('\r');
				dataBuffer.append('\n');
			}

			// Middle trace
		} else {
			addTraceTextToBuffer(trace, dataBuffer);
			dataBuffer.append('\r');
			dataBuffer.append('\n');
		}
	}

	/**
	 * Adds trace text to string buffer
	 * 
	 * @param trace
	 *            trace to be added
	 * @param str
	 *            string buffer
	 */
	private void addTraceTextToBuffer(TraceProperties trace, StringBuffer str) {

		// Traces missing
		if (trace.bTraceInformation.isTraceMissing()) {
			str.append(TraceViewerActionUtils.TRACES_DROPPED_MSG);
		}

		// Insert timestamp
		if (trace.timestampString != null) {
			str.append(trace.timestampString);
			str.append(TraceViewerGlobals.getTraceViewer()
					.getDataProcessorAccess().getTimestampParser()
					.getTimeFromPreviousString(trace.timeFromPreviousTrace));
			str.append('\t');
		}

		// Insert trace string
		if (trace.traceString != null) {
			str.append(trace.traceString);
			if (trace.traceComment != null) {
				str.append(TraceViewerActionUtils.COMMENT_PREFIX);
				str.append(trace.traceComment);
			}
		}
	}

	/**
	 * Copies dataBuffer to clipboard
	 */
	private void copyBufferToClipboard() {
		// Copy text to clipboard
		PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
			public void run() {
				try {
					String data = dataBuffer.toString();
					clipboard.setContents(new Object[] { data },
							new Transfer[] { TextTransfer.getInstance() });
				} finally {
					// Free the memory
					dataBuffer.setLength(0);

					processing = false;
				}
			}
		});

	}

	/**
	 * Starts gathering data
	 */
	public void startGatheringData() {
		if (!processing) {
			processing = true;
			dataBuffer.setLength(0);
			traceCount = 0;

			int fromTrace;
			int toTrace;

			if (SelectionProperties.firstClickedLine < SelectionProperties.lastClickedLine) {
				fromTrace = SelectionProperties.firstClickedLine;
				toTrace = SelectionProperties.lastClickedLine;
			} else {
				fromTrace = SelectionProperties.lastClickedLine;
				toTrace = SelectionProperties.firstClickedLine;
			}

			// Get the file position having given offset
			int index = TraceViewerGlobals.getTraceViewer()
					.getDataReaderAccess().getCurrentDataReader().getFileMap()
					.getIndexFromOffset(fromTrace);

			long pos = TraceViewerGlobals.getTraceViewer()
					.getDataReaderAccess().getCurrentDataReader().getFileMap()
					.getItem(index).longValue();
			int startTrace = index * TraceViewerGlobals.blockSize;

			// Calculate how many traces to drop from start
			tracesToDrop = fromTrace - startTrace;
			tracesToGet = toTrace - fromTrace + 1;
			if (tracesToGet > MAX_TRACES_TO_COPY) {
				tracesToGet = MAX_TRACES_TO_COPY;
			}

			// Number of blocks
			int numberOfBlocks = (tracesToGet / TraceViewerGlobals.blockSize) + 1;
			if (numberOfBlocks * TraceViewerGlobals.blockSize - tracesToDrop < tracesToGet) {
				numberOfBlocks++;
			}

			// Create own data reader if it doesn't exist and start it
			dataReader = TraceViewerGlobals.getTraceViewer()
					.getDataReaderAccess().startOwnDataReader(dataReader, this,
							numberOfBlocks, pos, startTrace, false);
		}
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
