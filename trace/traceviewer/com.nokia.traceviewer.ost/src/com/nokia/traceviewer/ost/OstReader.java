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
 * OST Reader class
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
 * OST Reader class
 */
public class OstReader extends OstBaseReader implements OstMessageCallback {

	/**
	 * Indicates not found
	 */
	private static final int NOT_DEFINED = -1;

	/**
	 * Number of bytes received
	 */
	private int bytesRead;

	/**
	 * Number of bytes in previous message
	 */
	private int previousMessageBytes;

	/**
	 * Trace number where pause was pressed
	 */
	private int pauseStopPoint = NOT_DEFINED;

	/**
	 * File position where pause is invoked
	 */
	private long pauseFilePosition;

	/**
	 * Constructor
	 * 
	 * @param mediaCallback
	 *            callback
	 * @param configuration
	 *            trace configuration used in this reader
	 */
	public OstReader(MediaCallback mediaCallback,
			TraceConfiguration configuration) {
		// OstBaseReader constructor
		super();

		this.mediaCallback = mediaCallback;
		receiveBuffer = ByteBuffer.allocateDirect(RECEIVE_BUFFER_SIZE);
		messageProcessor = new OstMessageProcessor(this, receiveBuffer);
		traceConfiguration = configuration;
		trace = new TraceProperties(configuration);
		trace.byteBuffer = receiveBuffer;

		running = true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Thread#run()
	 */
	@Override
	public void run() {
		// Create the file channel
		createFileChannel();
		setFilePosition(0);
		try {
			receiveBuffer.position(0);
			while (running) {
				// Read data if not paused or pause point is not reached
				if (!paused || pauseStopPoint > traceCount) {
					// Get bytes in buffer before read operation
					if (receiveBuffer.position() != 0) {
						previousMessageBytes = receiveBuffer.position();
					} else {
						previousMessageBytes = 0;
					}

					// Reads data to buffer
					bytesRead = sourceChannel.read(receiveBuffer);
					if (bytesRead < 0) {
						// If no data is found inform
						// callback for EOF, update view and sleep
						mediaCallback.endOfFile(this);
						Thread.sleep(SLEEP_TIME);
					} else {
						// If data is found, the processor is used to split the
						// data into OST messages. processMessage is called for
						// each message found from the buffer and the buffer is
						// re-positioned for next read operation
						messageProcessor.processBuffer();
					}
					// Paused, sleep
				} else {
					// Update view when pausing
					mediaCallback.endOfFile(this);
					Thread.sleep(PAUSE_TIME);
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
	 * int, int, int)
	 */
	public boolean processMessage(int msgStart, int msgLen, int headerLen,
			int headerVersion) {

		boolean valid = true;
		try {
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
				System.out
						.println("Unsupported header version: " + headerVersion); //$NON-NLS-1$
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
				trace.timestamp = messageProcessor
						.parseTimeStampToNanosecs(msgStart, headerLen
								+ OST_SIMPLE_TRACE_TIMESTAMP_OFFSET);

				// Process the trace
				valid = processOstSimpleTrace(msgStart, headerLen);

				// Unknown protocol ID
			} else {
				trace.binaryTrace = true;
				valid = false;
			}

			// Trace is valid, process it
			if (valid) {
				processNormalTrace();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		// Always return true to continue from the next message
		return true;
	}

	/**
	 * Processes normal trace (not scrolled)
	 */
	private void processNormalTrace() {
		try {
			if (sourceChannel != null && sourceChannel.isOpen()) {
				if ((!paused || pauseStopPoint > traceCount)) {
					traceCount++;
					trace.traceNumber = traceCount;

					// Update file map if first trace of trace block
					if (traceCount % TraceViewerGlobals.blockSize == 1) {
						long filePos = getTracePositionInFile();

						fileMap.insert(Long.valueOf(filePos));
					}

					// Check if last trace
					checkLastTrace();

					// In case of multi part trace, set trace properties now so
					// that DataProcessor can use it properly
					if (trace.bTraceInformation.getMultiPart() != 0) {
						int initialHeaderSize = trace.dataStart
								- trace.messageStart;
						trace.messageStart = 0;
						trace.messageLength = trace.byteBuffer.capacity();
						trace.dataStart = initialHeaderSize;
						trace.dataLength = trace.messageLength
								- initialHeaderSize;
					}

					// Process this trace
					mediaCallback.processTrace(trace);

					// Save file position when pausing
				} else if (pauseFilePosition == 0) {
					pauseFilePosition = getTracePositionInFile();
				}
			}
		} catch (IOException e) {
			// e.printStackTrace();
		}

	}

	/**
	 * Checks if last trace
	 * 
	 * @throws IOException
	 */
	private void checkLastTrace() throws IOException {
		long pos = getTracePositionInFile() + trace.messageLength;
		if (pos >= sourceChannel.size()) {
			trace.lastTrace = true;
		} else {
			trace.lastTrace = false;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.traceviewer.engine.DataReader#pause(boolean)
	 */
	@Override
	public void pause(boolean pause) {
		if (pause) {
			paused = true;
			pauseStopPoint = traceCount;
		} else {
			if (pauseFilePosition != 0) {
				setFilePosition(pauseFilePosition);
				pauseFilePosition = 0;
			}
			pauseStopPoint = NOT_DEFINED;
			paused = false;

		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.traceviewer.engine.DataReader#setFilePosition(long)
	 */
	@Override
	public void setFilePosition(long filePos) {
		try {
			receiveBuffer.position(0);
			if (sourceChannel != null) {
				if (filePos == 0) {
					sourceChannel.position(fileStartOffset);
				} else {
					sourceChannel.position(filePos);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		// If set to start, null everything
		if (filePos == 0) {
			pauseFilePosition = 0;
			traceCount = 0;
			fileMap.clearMap();
			bytesRead = 0;
			previousMessageBytes = 0;
		}
	}

	/**
	 * Gets trace position in file
	 * 
	 * @return trace position in file
	 */
	public long getTracePositionInFile() {
		long filePos = 0;
		try {
			filePos = sourceChannel.position() - bytesRead + trace.messageStart
					- previousMessageBytes;
		} catch (IOException e) {
		}
		return filePos;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.nokia.traceviewer.engine.BaseDataReader#createScrollReader(com.nokia
	 * .traceviewer.engine.MediaCallback,
	 * com.nokia.traceviewer.engine.TraceConfiguration)
	 */
	@Override
	public DataScrollReader createScrollReader(MediaCallback mediaCallback,
			TraceConfiguration conf) {
		return new OstScrollReader(mediaCallback, conf);
	}
}
