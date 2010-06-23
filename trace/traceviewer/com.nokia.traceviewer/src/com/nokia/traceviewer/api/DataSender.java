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
 * Data sender sends data to the device
 *
 */
package com.nokia.traceviewer.api;

import java.util.ArrayList;

import com.nokia.traceviewer.api.TraceViewerAPI.TVAPIError;
import com.nokia.traceviewer.engine.Connection;
import com.nokia.traceviewer.engine.TraceViewerGlobals;

/**
 * Data sender sends data to the device
 */
final class DataSender {

	/**
	 * Group ID not found error code
	 */
	private static final int GROUP_ID_NOT_FOUND = -1;

	/**
	 * Decode file not loaded error code
	 */
	private static final int DECODE_FILE_NOT_LOADED = -2;

	/**
	 * Sends raw data to the device
	 * 
	 * @param msg
	 *            array of bytes to send
	 * @param addHeader
	 *            if true, adds current protocol header to the message before
	 *            sending. If false, possible header is already included in the
	 *            data array
	 * @param messageId
	 *            messageID to add to the header. Might not be needed, depends
	 *            on the protocol.
	 * @param changeMediaType
	 *            if true, change media type to the header according to the
	 *            selected media
	 * @return error code from TraceViewerAPI
	 */
	public TVAPIError sendRawData(byte[] msg, boolean addHeader,
			byte messageId, boolean changeMediaType) {
		TVAPIError errorCode = TVAPIError.NONE;

		Connection connection = TraceViewerGlobals.getTraceViewer()
				.getConnection();

		// If connection exists and is connected, writing is propably ok
		if (connection != null && connection.isConnected()) {

			// Add header from TraceProvider
			if (addHeader) {
				TraceViewerGlobals.getTraceProvider().sendMessage(msg, true,
						messageId, false);

				// Else change the media type
			} else if (changeMediaType) {
				TraceViewerGlobals.getTraceProvider().sendMessage(msg, false,
						messageId, true);
				// Else just send as it is
			} else {
				connection.write(msg);
			}

		} else {
			errorCode = TVAPIError.NOT_CONNECTED;
		}
		return errorCode;
	}

	/**
	 * Activates given trace groups
	 * 
	 * @param componentId
	 *            component ID
	 * @param groupIds
	 *            array of group ID's
	 * @param activate
	 *            if true, activates given traces. If false, deactivates them
	 * @return error code from TraceViewerAPI
	 */
	public TVAPIError activateTraces(int componentId, int[] groupIds,
			boolean activate) {
		TVAPIError errorCode = TVAPIError.NONE;

		Connection connection = TraceViewerGlobals.getTraceViewer()
				.getConnection();

		// If connection exists and is connected, writing is propably ok
		if (connection != null && connection.isConnected()) {

			// Activate
			TraceViewerGlobals.getTraceProvider().activate(activate, false,
					componentId, groupIds);
		} else {
			errorCode = TVAPIError.NOT_CONNECTED;
		}

		return errorCode;
	}

	/**
	 * Activates given trace groups
	 * 
	 * @param componentId
	 *            component ID
	 * @param groupNames
	 *            array of group names
	 * @param activate
	 *            if true, activates given traces. If false, deactivates them
	 * @return error code from TraceViewerAPI
	 */
	public TVAPIError activateTraces(int componentId, String[] groupNames,
			boolean activate) {
		TVAPIError errorCode = TVAPIError.NONE;

		ArrayList<Integer> groupIds = new ArrayList<Integer>();

		// Get group ID's for the names
		for (int i = 0; i < groupNames.length; i++) {
			int groupId = TraceViewerAPI.getTraceGroupId(componentId,
					groupNames[i]);

			// Group not found from the decode model
			if (groupId == GROUP_ID_NOT_FOUND) {
				errorCode = TVAPIError.SOME_GROUPS_NOT_FOUND_FROM_DECODE_MODEL;
				groupIds.clear();
				break;

				// No decode file loaded
			} else if (groupId == DECODE_FILE_NOT_LOADED) {
				errorCode = TVAPIError.DECODE_FILE_NOT_LOADED;
				groupIds.clear();
				break;

				// Group ID ok
			} else {
				groupIds.add(Integer.valueOf(groupId));
			}
		}

		if (!groupIds.isEmpty()) {

			// Transfer the ID's to the array
			int[] groups = new int[groupIds.size()];
			for (int i = 0; i < groupIds.size(); i++) {
				groups[i] = groupIds.get(i).intValue();
			}

			// Use the other activate function
			errorCode = activateTraces(componentId, groups, activate);

		}

		return errorCode;
	}
}
