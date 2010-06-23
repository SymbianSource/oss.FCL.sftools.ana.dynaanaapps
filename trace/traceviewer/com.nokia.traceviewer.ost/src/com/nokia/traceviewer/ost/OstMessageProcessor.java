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
 * OST Message Processor 
 *
 */
package com.nokia.traceviewer.ost;

import java.io.IOException;
import java.nio.ByteBuffer;

import com.nokia.traceviewer.engine.TraceViewerConst;

/**
 * OST Message Processor
 * 
 */
public class OstMessageProcessor implements OstConsts, TraceViewerConst {

	/**
	 * Buffer where data is located
	 */
	private final ByteBuffer receiveBuffer;

	/**
	 * Temporary buffer for data
	 */
	private final ByteBuffer tempBuffer;

	/**
	 * Callback class
	 */
	private final OstMessageCallback callback;

	/**
	 * If true, stop processing the messages
	 */
	private boolean stopped;

	/**
	 * @param callback
	 *            callback where messages are returned
	 * @param receiveBuffer
	 *            buffer where data is located
	 */
	public OstMessageProcessor(OstMessageCallback callback,
			ByteBuffer receiveBuffer) {
		this.callback = callback;
		this.receiveBuffer = receiveBuffer;
		tempBuffer = ByteBuffer.allocateDirect(MAX_MESSAGE_SIZE);
	}

	/**
	 * Processes buffer to messages
	 * 
	 * @throws IOException
	 */
	public void processBuffer() throws IOException {

		boolean messageFound = false;
		boolean processorResult = false;
		int msgLength = 0;
		int versionNr = 0;
		int msgStart = 0;
		// Parses all complete messages
		int position = receiveBuffer.position();
		do {
			messageFound = false;
			// Reads next message length if there is enough data
			if (position - msgStart > OST_V01_HEADER_LENGTH) {
				versionNr = 0;
				msgLength = 0;

				// Get OST version and check the base header length
				versionNr |= (receiveBuffer.get(msgStart + OST_VERSION_OFFSET) & BYTE_MASK);

				int headerLength = 0;

				// OST version 0.1
				if (versionNr == OST_V01) {
					headerLength = OST_V01_HEADER_LENGTH;

					// Get message length
					msgLength |= (receiveBuffer.getShort(msgStart
							+ OST_V01_LENGTH_OFFSET_1) & SHORT_MASK)
							+ headerLength;

					// OST version 0.0
				} else if (versionNr == OST_V00) {
					headerLength = OST_V00_HEADER_LENGTH;

					// Get message length
					msgLength |= (receiveBuffer.getShort(msgStart
							+ OST_V01_LENGTH_OFFSET_1) & SHORT_MASK)
							+ headerLength;

					// OST version 0.5 or 1.0
				} else if (versionNr == OST_V05 || versionNr == OST_V10) {
					headerLength = OST_V05_HEADER_LENGTH;

					// Get message length
					msgLength |= (receiveBuffer.get(msgStart
							+ OST_V05_LENGTH_OFFSET) & BYTE_MASK)
							+ headerLength;

					// If length is only the header length, there are 4 bytes
					// extended length in Little Endian (LE)
					if (msgLength == headerLength) {
						headerLength += OST_V05_EXT_LENGTH_LENGTH;
						msgLength = receiveBuffer.getInt(msgStart
								+ OST_V05_EXT_LENGTH_OFFSET);

						// Reverse because of Little Endian
						msgLength = Integer.reverseBytes(msgLength);
						msgLength += headerLength;
					}
				} else {
					// Unsupported OST version.
					System.out
							.println("Unsupported OST version: 0x" + Integer.toHexString(versionNr)); //$NON-NLS-1$
				}

				// Parses message if it is completely in buffer
				if (position >= msgStart + msgLength) {
					processorResult = callback.processMessage(msgStart,
							msgLength, headerLength, versionNr);
					msgStart += msgLength;
					messageFound = true;
				}
			}
		} while (messageFound && processorResult && !stopped);

		if (!stopped) {
			// Writes the remaining data into beginning of receive
			// buffer. The next socket read operation will append data
			// into buffer
			int remainderLength = position - msgStart;
			if (remainderLength > 0 && processorResult) {
				receiveBuffer.position(msgStart);
				receiveBuffer.limit(msgStart + remainderLength);
				tempBuffer.position(0);
				tempBuffer.limit(MAX_MESSAGE_SIZE);
				tempBuffer.put(receiveBuffer);

				receiveBuffer.position(0);
				receiveBuffer.limit(RECEIVE_BUFFER_SIZE);
				tempBuffer.position(0);
				tempBuffer.limit(remainderLength);

				receiveBuffer.put(tempBuffer);
				receiveBuffer.position(remainderLength);
			} else {
				receiveBuffer.position(0);
				receiveBuffer.limit(RECEIVE_BUFFER_SIZE);
			}
		}
	}

	/**
	 * Reads the 8 byte timestamp from the data buffer
	 * 
	 * @param msgStart
	 *            message start offset
	 * @param timestampOffset
	 *            offset to the timestamp in the message
	 * @return the timestamp
	 */
	public long parseTimeStampToNanosecs(int msgStart, int timestampOffset) {
		long timestamp = 0;
		try {
			timestamp = receiveBuffer.getLong(msgStart + timestampOffset)
					& TIMESTAMP_MASK;
		} catch (Throwable t) {
			t.printStackTrace();
			// Couldn't get timestamp, return MIN_VALUE to set trace as non
			// valid
			timestamp = Long.MIN_VALUE;
		}
		return timestamp;
	}

	/**
	 * Close the processor
	 */
	void close() {
		stopped = true;
	}
}
