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
 * OST message sender class
 *
 */
package com.nokia.traceviewer.ost;

import com.nokia.traceviewer.engine.TraceViewerGlobals;

/**
 * OST message sender class
 * 
 */
public class OstMessageSender implements OstConsts {

	/**
	 * Sends message to the device
	 * 
	 * @param message
	 *            the message
	 * @param addHeader
	 *            if true, adds header to the message before sending
	 * @param messageId
	 *            message ID to be used in message
	 */
	public void sendMessage(byte[] message, boolean addHeader, byte messageId) {
		byte[] ostMessage;
		if (addHeader) {
			// Add TraceCore protocol subscriber header
			ostMessage = addTraceCoreProtocolHeader(message, messageId);
			messageId = OST_TRACECORE_PROTOCOL_ID;

			// Wrap the message to OST message
			ostMessage = wrapInOstMessage(ostMessage, messageId);
		} else {
			ostMessage = message;
		}

		// Write message to connection
		if (TraceViewerGlobals.getTraceViewer().getConnection() != null) {
			TraceViewerGlobals.getTraceViewer().getConnection().write(
					ostMessage);
		}
	}

	/**
	 * Sends activation message to the device
	 * 
	 * @param activate
	 *            if true, activate, otherwise deactivate
	 * @param componentId
	 *            component ID
	 * @param groupIds
	 *            array of group IDs
	 */
	public void sendActivationMessage(boolean activate, int componentId,
			int[] groupIds) {

		// Don't send anything if activating zero groups
		if (groupIds.length > 0) {

			// Create activation message
			byte[] activationMessage = createActivationMessage(activate,
					componentId, groupIds);

			// Wrap the message to OST message
			byte[] ostMessage = wrapInOstMessage(activationMessage,
					OST_TRACE_ACTIVATION_ID);

			// Write message to connection
			if (TraceViewerGlobals.getTraceViewer().getConnection() != null) {
				TraceViewerGlobals.getTraceViewer().getConnection().write(
						ostMessage);
			}
		}
	}

	/**
	 * Creates activation message
	 * 
	 * @param activate
	 *            if true, message is activation message. If false, message is
	 *            deactivation message.
	 * @param componentId
	 *            component ID
	 * @param groupIds
	 *            array of group ID's
	 * @return the activation message
	 */
	private byte[] createActivationMessage(boolean activate, int componentId,
			int[] groupIds) {
		int j = 0;
		byte[] message = null;

		// Calculate length of the message. The last "2" is from activation
		// status + filler
		int length = OST_TRACE_ACTIVATION_HEADER_LENGTH + COMPONENT_ID_LENGTH
				+ GROUP_ID_LENGTH * groupIds.length + 2;

		message = new byte[length];

		// Transaction ID
		message[OST_TRACE_ACTIVATION_TRANSID_OFFSET] = 0x01;

		// Message ID is set application status request
		message[OST_TRACE_ACTIVATION_MESSAGEID_OFFSET] = OST_TRACE_ACTIVATION_SET_STATUS_REQUEST;

		// Set next four bytes to be component id
		byte[] componentIdBytes = getIntAsFourBytesBigEndian(componentId);
		for (int k = 0; k < componentIdBytes.length; k++) {
			message[OST_TRACE_ACTIVATION_COMPONENTID_OFFSET + k] = componentIdBytes[k];
		}

		// Activate or deactivate
		if (activate) {
			message[OST_TRACE_ACTIVATION_STATUS_OFFSET] = OST_TRACE_ACTIVATION_ACTIVATE_INDICATOR;
		} else {
			message[OST_TRACE_ACTIVATION_STATUS_OFFSET] = OST_TRACE_ACTIVATION_DEACTIVATE_INDICATOR;
		}

		message[OST_TRACE_ACTIVATION_FILLER_OFFSET] = 0x00;

		// Iterate through groups
		for (j = 0; j < groupIds.length; j++) {

			// Set next 2 bytes to be group id
			byte[] groupIdBytes = getIntAsTwoBytesBigEndian(groupIds[j]);
			for (int k = 0; k < groupIdBytes.length; k++) {
				int offset = OST_TRACE_ACTIVATION_GROUPID_START_OFFSET + k
						+ (j * 2);
				message[offset] = groupIdBytes[k];
			}
		}

		return message;
	}

	/**
	 * Adds TraceCore protocol header to the message
	 * 
	 * @param message
	 *            message
	 * @param messageId
	 *            message (subscriber) ID to be used
	 * @return message with TraceCore protocol header added
	 */
	private byte[] addTraceCoreProtocolHeader(byte[] message, byte messageId) {
		byte[] ostMessage = new byte[message.length
				+ OST_TRACECORE_PROTOCOL_HEADER_LENGTH];

		// TraceCore protocol ID to be Subscriber ID
		ostMessage[OST_TRACECORE_PROTOCOL_ID_OFFSET] = OST_TRACECORE_SUBSCRIBER_PROTOCOL_ID;

		// Add original message ID as a subscriber ID
		ostMessage[OST_TRACECORE_SUBSCRIBER_ID_OFFSET] = messageId;

		// Two next bytes are reserved for future use
		ostMessage[OST_TRACECORE_SUBSCRIBER_ID_OFFSET + 1] = 0x00;
		ostMessage[OST_TRACECORE_SUBSCRIBER_ID_OFFSET + 2] = 0x00;

		// Copy the rest of the bytes to the new array
		System.arraycopy(message, 0, ostMessage,
				OST_TRACECORE_PROTOCOL_HEADER_LENGTH, message.length);

		return ostMessage;
	}

	/**
	 * Wraps given byte array to OST message. Header to be used is OST header
	 * version 0.1
	 * 
	 * @param message
	 *            message to wrap
	 * @param protocolId
	 *            protocol ID to be used in message
	 * @return message wrapped in OST message
	 */
	public byte[] wrapInOstMessage(byte[] message, byte protocolId) {
		byte[] ostMessage = new byte[message.length + OST_V01_HEADER_LENGTH];

		// Version
		ostMessage[OST_VERSION_OFFSET] = 0x01;

		// Protocol ID
		ostMessage[OST_V01_PROTOCOLID_OFFSET] = protocolId;

		// Length bytes
		byte[] length = getIntAsTwoBytesBigEndian(message.length);
		ostMessage[OST_V01_LENGTH_OFFSET_1] = length[0];
		ostMessage[OST_V01_LENGTH_OFFSET_2] = length[1];

		// Copy the rest of the bytes to the new array
		System.arraycopy(message, 0, ostMessage, OST_V01_HEADER_LENGTH,
				message.length);

		return ostMessage;
	}

	/**
	 * Gets int as two bytes in big endian
	 * 
	 * @param integer
	 *            integer to get as bytes
	 * @return byte array[2] containing given integer in big endian
	 */
	private byte[] getIntAsTwoBytesBigEndian(int integer) {
		byte[] byteArr = new byte[2];
		byteArr[0] = (byte) ((integer >> 8) & 0xFF);
		byteArr[1] = (byte) (integer & 0xFF);
		return byteArr;
	}

	/**
	 * Gets int as four bytes in big endian
	 * 
	 * @param integer
	 *            int
	 * @return four byte array in big endian
	 */
	private byte[] getIntAsFourBytesBigEndian(int integer) {
		byte[] byteArr = new byte[4];
		byteArr[0] = (byte) (integer >> 24);
		byteArr[1] = (byte) ((integer << 8) >> 24);
		byteArr[2] = (byte) ((integer << 16) >> 24);
		byteArr[3] = (byte) ((integer << 24) >> 24);
		return byteArr;
	}
}
