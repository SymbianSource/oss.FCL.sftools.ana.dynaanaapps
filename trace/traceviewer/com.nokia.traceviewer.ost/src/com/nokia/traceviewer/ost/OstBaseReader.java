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
 * Base class for OST Readers
 *
 */
package com.nokia.traceviewer.ost;

import java.io.IOException;

import com.nokia.traceviewer.engine.BaseDataReader;
import com.nokia.traceviewer.engine.TraceProperties;

/**
 * Base class for OST Readers
 * 
 */
public abstract class OstBaseReader extends BaseDataReader implements OstConsts {

	/**
	 * Splits data from byte buffer into messages
	 */
	protected OstMessageProcessor messageProcessor;

	/**
	 * Tells if this reader is paused or not
	 */
	protected boolean paused;

	/**
	 * Main loop running boolean
	 */
	protected boolean running;

	/**
	 * Byte array to store trace data get from buffer
	 */
	private byte[] traceData;

	/**
	 * Constructor
	 */
	OstBaseReader() {
		// BaseDataReader constructor
		super();
		traceData = new byte[MAX_MESSAGE_SIZE];
	}

	/**
	 * Parses a OST Simple Trace
	 * 
	 * @param msgStart
	 *            start of message
	 * @param headerLen
	 *            length of header
	 */
	boolean processOstSimpleTrace(int msgStart, int headerLen) {
		boolean valid = true;
		int componentId = 0;
		int groupId = 0;
		int traceId = 0;

		// Get component id
		componentId |= receiveBuffer.getInt(msgStart + headerLen
				+ OST_SIMPLE_TRACE_COMPONENTID_OFFSET);

		// Get group id
		groupId |= receiveBuffer.getShort(msgStart + headerLen
				+ OST_SIMPLE_TRACE_GROUPID_OFFSET)
				& SHORT_MASK;

		// Get trace id
		traceId |= receiveBuffer.getShort(msgStart + headerLen
				+ OST_SIMPLE_TRACE_TRACEID_OFFSET)
				& SHORT_MASK;

		trace.binaryTrace = true;
		trace.traceString = null;

		// Set trace information
		trace.information.setComponentId(componentId);
		trace.information.setGroupId(groupId);
		trace.information.setTraceId(traceId);

		int bTraceHeaderStart = msgStart + headerLen
				+ OST_SIMPLE_TRACE_TRACEID_OFFSET + TRACE_ID_LENGTH;

		// Parse BTrace variables
		parseBTraceVariables(receiveBuffer, bTraceHeaderStart, trace);

		// Handle possible multipart trace
		if (trace.bTraceInformation.getMultiPart() != 0) {
			valid = handleMultiPart(trace, trace.bTraceInformation
					.getMultiPart(), bTraceHeaderStart);
		}

		return valid;
	}

	/**
	 * Parses a OST Ascii Trace
	 * 
	 * @param msgStart
	 *            start of message
	 * @param msgLen
	 *            length of message
	 * @param headerLen
	 *            length of OST header
	 */
	void processOstAsciiTrace(int msgStart, int msgLen, int headerLen) {

		// Data length
		int dataLength = msgLen - headerLen - OST_ASCII_TRACE_TIMESTAMP_LENGTH;
		if (dataLength < 0) {
			dataLength = 0;
		}
		int position = receiveBuffer.position();
		int limit = receiveBuffer.limit();
		receiveBuffer.position(msgStart + headerLen
				+ OST_ASCII_TRACE_TIMESTAMP_LENGTH);
		receiveBuffer.limit(msgStart + headerLen
				+ OST_ASCII_TRACE_TIMESTAMP_LENGTH + dataLength);

		receiveBuffer.get(traceData, 0, dataLength);

		// Remove line breaks and ASCII end characters from the buffer
		boolean stringEndsRemoved = false;
		for (int i = dataLength - 1; i >= 0; i--) {
			byte b = traceData[i];
			if (!stringEndsRemoved && b == '\0') {
				dataLength--;
			} else if (b == '\n' || b == '\r' || b == '\0') {
				traceData[i] = ' ';
			} else {
				stringEndsRemoved = true;
			}
		}

		trace.traceString = new String(traceData, 0, dataLength);
		trace.dataStart = msgStart + headerLen
				+ OST_ASCII_TRACE_TIMESTAMP_LENGTH;

		receiveBuffer.limit(limit);
		receiveBuffer.position(position);

		trace.binaryTrace = false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seecom.nokia.traceviewer.engine.BaseDataReader#
	 * setProtocolSpecificStuffToMultiPartTrace(byte[],
	 * com.nokia.traceviewer.engine.TraceProperties)
	 */
	@Override
	protected byte[] setProtocolSpecificStuffToMultiPartTrace(byte[] byteArr,
			TraceProperties trace) {
		int totalMessageLength = byteArr.length;
		int versionNr = byteArr[OST_VERSION_OFFSET];

		// OST version 0.1 or 0.0
		if (versionNr == OST_V01 || versionNr == OST_V00) {
			totalMessageLength -= OST_V01_HEADER_LENGTH;
			byteArr[OST_V01_LENGTH_OFFSET_2] = (byte) (totalMessageLength & BYTE_MASK);
			byteArr[OST_V01_LENGTH_OFFSET_1] = (byte) ((totalMessageLength >> 8) & BYTE_MASK);

			// OST version 0.5 or 1.0
		} else if (versionNr == OST_V05 || versionNr == OST_V10) {
			int messageLengthWithoutHeader = totalMessageLength
					- OST_V05_HEADER_LENGTH;

			// Length can fit to one byte
			if (messageLengthWithoutHeader <= 0xFF) {
				byteArr[OST_V05_LENGTH_OFFSET] = (byte) messageLengthWithoutHeader;

				// Use extended length
			} else {
				byteArr[OST_V05_LENGTH_OFFSET] = 0;
				totalMessageLength += OST_V05_EXT_LENGTH_LENGTH;

				// Array needs to be extended by 4 bytes
				byte[] newArray = new byte[totalMessageLength];
				System.arraycopy(byteArr, 0, newArray, 0,
						OST_V05_EXT_LENGTH_LENGTH);

				newArray[OST_V05_EXT_LENGTH_OFFSET] = (byte) (messageLengthWithoutHeader & BYTE_MASK);
				newArray[OST_V05_EXT_LENGTH_OFFSET + 1] = (byte) ((messageLengthWithoutHeader >> 8) & BYTE_MASK);
				newArray[OST_V05_EXT_LENGTH_OFFSET + 2] = (byte) ((messageLengthWithoutHeader >> 16) & BYTE_MASK);
				newArray[OST_V05_EXT_LENGTH_OFFSET + 3] = (byte) ((messageLengthWithoutHeader >> 24) & BYTE_MASK);

				// Copy rest of the array to offset 8
				System.arraycopy(byteArr, OST_V05_EXT_LENGTH_LENGTH, newArray,
						OST_V05_EXT_LENGTH_OFFSET + OST_V05_EXT_LENGTH_LENGTH,
						totalMessageLength - OST_V05_EXT_LENGTH_OFFSET
								- OST_V05_EXT_LENGTH_LENGTH);

				byteArr = newArray;

				// Data start must be moved by 4 bytes
				trace.dataStart += OST_V05_EXT_LENGTH_LENGTH;
				trace.messageLength += OST_V05_EXT_LENGTH_LENGTH;
			}
		}
		return byteArr;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.traceviewer.engine.DataReader#isPaused()
	 */
	public boolean isPaused() {
		return paused;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.traceviewer.engine.DataReader#pause(boolean)
	 */
	public void pause(boolean pause) {
		// Default implementation
		paused = pause;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.traceviewer.engine.DataReader#shutdown()
	 */
	public void shutdown() {
		running = false;

		// Close the message processor
		if (messageProcessor != null) {
			messageProcessor.close();
		}

		try {
			if (sourceChannel != null) {
				sourceChannel.close();
				sourceChannel = null;
			}
		} catch (IOException e) {
		}
	}
}
