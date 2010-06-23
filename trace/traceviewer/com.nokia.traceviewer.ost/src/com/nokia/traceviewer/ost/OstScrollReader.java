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
 * OST ScrollReader class
 *
 */
package com.nokia.traceviewer.ost;

import java.io.IOException;
import java.nio.ByteBuffer;

import com.nokia.traceviewer.engine.DataScrollReader;
import com.nokia.traceviewer.engine.MediaCallback;
import com.nokia.traceviewer.engine.TraceConfiguration;
import com.nokia.traceviewer.engine.TraceProperties;
import com.nokia.traceviewer.engine.TraceViewerGlobals;

/**
 * OST ScrollReader class
 */
public class OstScrollReader extends OstBaseReader implements DataScrollReader,
		OstMessageCallback {

	/**
	 * Number of blocks to process when reader is block-reader
	 */
	private int numberOfBlocks = 1;

	/**
	 * Starting offset when reader is block-reader
	 */
	private int startReadingTracesFrom;

	/**
	 * Number of traces read when reader is block-reader
	 */
	private int numberOfTracesRead;

	/**
	 * Number of bytes received
	 */
	private int bytesRead;

	/**
	 * Number of bytes in previous message
	 */
	private int previousMessageBytes;

	/**
	 * Is this reader blocking
	 */
	private boolean blocking;

	/**
	 * Constructor
	 * 
	 * @param mediaCallback
	 *            callback
	 * @param configuration
	 *            Trace Configuration
	 */
	public OstScrollReader(MediaCallback mediaCallback,
			TraceConfiguration configuration) {
		// OstBaseReader constructor
		super();

		filePath = TraceViewerGlobals.getTraceViewer().getDataReaderAccess()
				.getCurrentDataReader().getFilePath();
		createFileChannel();

		this.mediaCallback = mediaCallback;
		receiveBuffer = ByteBuffer.allocateDirect(RECEIVE_BUFFER_SIZE);
		messageProcessor = new OstMessageProcessor(this, receiveBuffer);
		traceConfiguration = configuration;
		trace = new TraceProperties(configuration);
		trace.byteBuffer = receiveBuffer;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Thread#run()
	 */
	@Override
	public synchronized void run() {
		try {
			running = true;
			receiveBuffer.position(0);
			while (running) {
				// Get bytes in buffer before read operation
				if (receiveBuffer.position() != 0) {
					previousMessageBytes = receiveBuffer.position();
				} else {
					previousMessageBytes = 0;
				}

				// Reads data to buffer
				bytesRead = sourceChannel.read(receiveBuffer);
				if (bytesRead > 0) {

					// If data is found, the processor is used to split
					// thedata into OST messages. processMessage is
					// called for each message found from the buffer and
					// the buffer is re-positioned for next read
					// operation
					messageProcessor.processBuffer();
				} else {
					putToSleep();
				}
			}
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.traceviewer.ost.OstMessageCallback#processMessage(int,
	 * int, int)
	 */
	public boolean processMessage(int msgStart, int msgLen, int headerLen,
			int headerVersion) throws IOException {
		boolean valid = true;
		boolean returnValue = true;
		int protocolId = 0;

		// Create new TraceProperties
		trace = new TraceProperties(traceConfiguration);

		// Save buffer and message and data offsets
		trace.byteBuffer = receiveBuffer;
		trace.messageStart = msgStart;
		trace.messageLength = msgLen;

		// Get protocol ID offset
		int protocolIdOffset = 0;
		if (headerVersion == OST_V01) {
			protocolIdOffset = OST_V01_PROTOCOLID_OFFSET;
		} else if (headerVersion == OST_V05) {
			protocolIdOffset = OST_V05_PROTOCOLID_OFFSET;
		} else if (headerVersion == OST_V10) {
			protocolIdOffset = OST_V10_PROTOCOLID_OFFSET;
		} else if (headerVersion == OST_V00) {
			protocolIdOffset = OST_V00_PROTOCOLID_OFFSET;
		} else {
			// Unsupported header version
			System.out.println("Unsupported header version: " + headerVersion); //$NON-NLS-1$
		}

		// Read protocol ID
		protocolId |= receiveBuffer.get(msgStart + protocolIdOffset)
				& BYTE_MASK;

		// OST Ascii Trace (0x02)
		if (protocolId == OST_ASCII_TRACE_ID && msgLen >= headerLen) {

			// Get the timestamp
			trace.timestamp = messageProcessor.parseTimeStampToNanosecs(
					msgStart, headerLen + OST_ASCII_TRACE_TIMESTAMP_OFFSET);

			// Process the trace
			processOstAsciiTrace(msgStart, msgLen, headerLen);

			// OST Simple Application Trace Protocol (0x03)
		} else if (protocolId == OST_SIMPLE_TRACE_ID) {

			// Get the timestamp
			trace.timestamp = messageProcessor.parseTimeStampToNanosecs(
					msgStart, headerLen + OST_SIMPLE_TRACE_TIMESTAMP_OFFSET);

			// Process the trace
			valid = processOstSimpleTrace(msgStart, headerLen);

			// Unknown protocol ID
		} else {
			trace.binaryTrace = true;
			valid = false;
		}

		// Trace is valid
		if (valid) {
			returnValue = processScrolledTrace(msgStart, msgLen, returnValue);

		}
		return returnValue;
	}

	/**
	 * Processes scrolled trace
	 * 
	 * @param msgStart
	 *            message start offset
	 * @param msgLen
	 *            message length
	 * @param returnValue
	 *            implication of success of processing trace
	 * @return implication of success of processing trace
	 * @throws IOException
	 */
	public boolean processScrolledTrace(int msgStart, int msgLen,
			boolean returnValue) throws IOException {

		// In case of multi part trace, set trace properties now so
		// that DataProcessor can use it properly
		if (trace.bTraceInformation.getMultiPart() != 0) {
			int initialHeaderSize = trace.dataStart - trace.messageStart;
			trace.messageStart = 0;
			trace.messageLength = trace.byteBuffer.capacity();
			trace.dataStart = initialHeaderSize;
			trace.dataLength = trace.messageLength - initialHeaderSize;
		}

		numberOfTracesRead++;
		trace.traceNumber = startReadingTracesFrom + numberOfTracesRead;

		// Not processed full block of traces yet, keep processing
		if (!lastTrace(msgStart, msgLen)) {

			trace.lastTrace = false;
			mediaCallback.processTrace(trace);

			// Last trace
		} else {
			trace.lastTrace = true;
			blocking = true;
			numberOfTracesRead = 0;
			mediaCallback.processTrace(trace);

			putToSleep();
			returnValue = false;
		}
		return returnValue;
	}

	/**
	 * Tells if this trace is the last one to process
	 * 
	 * @param msgStart
	 *            message start offset
	 * @param msgLen
	 *            message length
	 * @return true if this is the last trace
	 * @throws IOException
	 */
	public boolean lastTrace(int msgStart, int msgLen) throws IOException {
		return sourceChannelPosition(msgStart, msgLen) >= sourceChannel.size()
				|| lastTraceToShow() || isBlockSizeRead();
	}

	/**
	 * Checks if we are now read the whole block what we want
	 * 
	 * @return true if block is fully read
	 */
	private boolean isBlockSizeRead() {
		boolean isBlockRead = false;
		if (numberOfTracesRead == TraceViewerGlobals.blockSize * numberOfBlocks) {
			isBlockRead = true;
		}
		return isBlockRead;
	}

	/**
	 * Tells if this is the last trace we want to show during pause because
	 * there propably is more traces in the file
	 * 
	 * @return true if this is the last trace to show during pause
	 */
	public boolean lastTraceToShow() {
		boolean lastTraceToShow = (startReadingTracesFrom + numberOfTracesRead
				+ 1 > TraceViewerGlobals.getTraceViewer().getDataReaderAccess()
				.getCurrentDataReader().getTraceCount());

		return lastTraceToShow;
	}

	/**
	 * Gets position of source channel after this trace
	 * 
	 * @param msgStart
	 *            message start offset
	 * @param msgLen
	 *            message length
	 * @return source channel position after this trace
	 * @throws IOException
	 */
	public long sourceChannelPosition(int msgStart, int msgLen)
			throws IOException {
		return sourceChannel.position() - bytesRead + msgStart + msgLen
				- previousMessageBytes;
	}

	/**
	 * Puts this thread to sleep
	 */
	public void putToSleep() {
		try {
			// Null variables if this reader is only for blocks
			if (TraceViewerGlobals.getTraceViewer().getStateHolder()
					.isScrolling()) {
				receiveBuffer.position(receiveBuffer.capacity());
				numberOfTracesRead = 0;
			}
			if (blocking) {
				this.notifyAll();
				this.wait();
			}
		} catch (InterruptedException e) {
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.traceviewer.engine.DataScrollReader#setBlockReader(int,
	 * int, boolean)
	 */
	public void setBlockReader(int numberOfBlocks, int startingTrace,
			boolean blocking) {
		this.numberOfBlocks = numberOfBlocks;
		this.startReadingTracesFrom = startingTrace;
		this.blocking = blocking;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.traceviewer.engine.DataReader#getTracePositionInFile()
	 */
	public long getTracePositionInFile() {
		return 0;
	}
}
