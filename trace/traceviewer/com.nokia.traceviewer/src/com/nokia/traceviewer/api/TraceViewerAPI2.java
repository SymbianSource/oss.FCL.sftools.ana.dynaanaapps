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
 */

package com.nokia.traceviewer.api;

import java.util.ArrayList;

import com.nokia.traceviewer.api.TraceViewerAPI.TVAPIError;
import com.nokia.traceviewer.engine.TraceInformation;
import com.nokia.traceviewer.engine.TraceMetaData;
import com.nokia.traceviewer.engine.activation.TraceActivationComponentItem;
import com.nokia.traceviewer.engine.dataprocessor.DataProcessor;
import com.nokia.traceviewer.internal.api.TraceViewerAPI2Impl;

/**
 * Public API access point to TraceViewer API functions that offers the
 * following enhancements to the original <code>TraceViewerAPI</code>
 * implementation: - notification services for client on connection status
 * changes. - keeps count of active clients, and disconnects data source only
 * when there is no more active clients.
 * 
 * Public API calls are further delegated to internal implementation of the API
 * in <code>TraceViewerAPIImpl2</code> class.
 * 
 * Other API services that are not connectivity related are just delegated to
 * original <code>TraceViewerAPI</code> implementation.
 * 
 * @see TraceViewerAPI2Impl
 * @see TraceViewerAPI
 */
public class TraceViewerAPI2 {

	/**
	 * Connect using current settings defined in TraceViewer's preferences.
	 * Client is registered successfully in case no exceptions are thrown.
	 * 
	 * @param client
	 *            client requesting the connection.
	 * @return connection info about the connected service.
	 * @throws TraceConnectivityException
	 * @see TraceConnectivityException
	 */
	public static ITraceConnInfo connect(ITraceConnClient client)
			throws TraceConnectivityException {
		return TraceViewerAPI2Impl.connect(client);
	}

	/**
	 * Connects using current settings defined in TraceViewer's preferences
	 * Client is disconnected and unregistered successfully for notifications in
	 * case no exceptions are thrown.
	 * 
	 * @param client
	 *            client requesting the connection.
	 * @throws TraceConnectivityException
	 * @see TraceConnectivityException
	 */
	public static void disconnect(ITraceConnClient client)
			throws TraceConnectivityException {
		TraceViewerAPI2Impl.disconnect(client);
	}

	/**
	 * @see com.nokia.traceviewer.api.TraceViewerAPI#activateTraces(int, int[],
	 *      boolean)
	 * 
	 * @param componentId
	 *            component ID
	 * @param groupIds
	 *            group ID array
	 * @param activate
	 *            if true, activate. If false, deactivate
	 * @return error code from TVAPIError
	 * @throws TraceConnectivityException
	 */
	public static TVAPIError activateTraces(int componentId, int[] groupIds,
			boolean activate) throws TraceConnectivityException {
		TVAPIError error = TraceViewerAPI.activateTraces(componentId, groupIds,
				activate);
		if (error == TVAPIError.NOT_CONNECTED) {
			throw new TraceConnectivityException(error);
		}
		return error;
	}

	/**
	 * @see com.nokia.traceviewer.api.TraceViewerAPI#activateTraces(int,
	 *      java.lang.String[], boolean)
	 * 
	 * @param componentId
	 *            component ID
	 * @param groupNames
	 *            array of group names
	 * @param activate
	 *            if true, activate. If false, deactivate.
	 * @return error code from TraceViewerAPI
	 * 
	 * @throws TraceConnectivityException
	 */
	public static TVAPIError activateTraces(int componentId,
			String[] groupNames, boolean activate)
			throws TraceConnectivityException {
		TVAPIError error = TraceViewerAPI.activateTraces(componentId,
				groupNames, activate);
		if (error == TVAPIError.NOT_CONNECTED) {
			throw new TraceConnectivityException(error);
		}
		return error;
	}

	/**
	 * @see com.nokia.traceviewer.api.TraceViewerAPI#addDataProcessor(com.nokia.traceviewer.engine.dataprocessor.DataProcessor,
	 *      com.nokia.traceviewer.api.DPLocation, int)
	 * 
	 * @param dataProcessor
	 *            the dataprocessor to be added
	 * @param location
	 *            location in the list
	 * @param priority
	 *            priority of the dataprocessor. If two dataprocessors are in
	 *            the same location in the dataprocessor list, the one with
	 *            higher priority will be run first
	 * @return error code from TraceViewerAPI
	 */
	public static TVAPIError addDataProcessor(DataProcessor dataProcessor,
			DPLocation location, int priority) {
		return TraceViewerAPI.addDataProcessor(dataProcessor, location,
				priority);
	}

	/**
	 * @see com.nokia.traceviewer.api.TraceViewerAPI#getDictionaryComponents()
	 * 
	 * @return list of components from all loaded Dictionaries. List can be
	 *         empty.
	 */
	public static ArrayList<TraceActivationComponentItem> getDictionaryComponents() {
		return TraceViewerAPI.getDictionaryComponents();
	}

	/**
	 * @see com.nokia.traceviewer.api.TraceViewerAPI#getTraceComponentName(int)
	 * 
	 * @param componentId
	 *            component ID
	 * @return component name or null if not found or no Decode files are loaded
	 */
	public static String getTraceComponentName(int componentId) {
		return TraceViewerAPI.getTraceComponentName(componentId);
	}

	/**
	 * @see com.nokia.traceviewer.api.TraceViewerAPI#getTraceGroupId(int,
	 *      java.lang.String)
	 * 
	 * @param componentId
	 *            component ID
	 * @param groupName
	 *            the group name
	 * @return trace group ID or -1 if group ID not found or -2 if no Decode
	 *         files are loaded
	 */
	public static int getTraceGroupId(int componentId, String groupName) {
		return TraceViewerAPI.getTraceGroupId(componentId, groupName);
	}

	/**
	 * @see com.nokia.traceviewer.api.TraceViewerAPI#getTraceGroupName(int, int)
	 * 
	 * @param componentId
	 *            component ID
	 * @param groupId
	 *            group ID
	 * @return group name or null if not found or no Decode files are loaded
	 */
	public static String getTraceGroupName(int componentId, int groupId) {
		return TraceViewerAPI.getTraceGroupName(componentId, groupId);
	}

	/**
	 * @see com.nokia.traceviewer.api.TraceViewerAPI#getTraceName(int, int, int)
	 * 
	 * @param componentId
	 *            component ID
	 * @param groupId
	 *            group ID
	 * @param traceId
	 *            trace ID
	 * @return trace name or null if not found or no Decode files are loaded
	 */
	public static String getTraceName(int componentId, int groupId, int traceId) {
		return TraceViewerAPI.getTraceName(componentId, groupId, traceId);
	}

	/**
	 * @see com.nokia.traceviewer.api.TraceViewerAPI#getTraceMetaData(com.nokia.traceviewer.engine.TraceInformation)
	 * 
	 * @param information
	 *            TraceInformation to be used when finding correct metadata
	 * @return trace metadata or null if nothing is found
	 */
	public static TraceMetaData getTraceMetaData(TraceInformation information) {
		return TraceViewerAPI.getTraceMetaData(information);
	}

	/**
	 * @see com.nokia.traceviewer.api.TraceViewerAPI#loadDecodeFile(java.lang.String,
	 *      boolean)
	 * 
	 * @param decodeFilePath
	 *            absolute path to the decode file. Path must be in correct
	 *            format for any operating system (Windows, Linux)
	 * @param deleteExistingModel
	 *            if true, old decode model is removed before this decode file
	 *            is loaded
	 * @return error code from TraceViewerAPI
	 */
	public static TVAPIError loadDecodeFile(String decodeFilePath,
			boolean deleteExistingModel) {
		return TraceViewerAPI.loadDecodeFile(decodeFilePath,
				deleteExistingModel);
	}

	/**
	 * @see com.nokia.traceviewer.api.TraceViewerAPI#openLogFile(java.lang.String)
	 * 
	 * @param filePath
	 *            file path to open. If file path ends with .txt, file is opened
	 *            as ASCII log. If filePath is null and TraceViewer view is
	 *            open, a file selection dialog is opened.
	 * @return error code from TraceViewerAPI
	 */
	public static TVAPIError openLogFile(String filePath) {
		return TraceViewerAPI.openLogFile(filePath);
	}

	/**
	 * @see com.nokia.traceviewer.api.TraceViewerAPI#removeDataProcessor(com.nokia.traceviewer.engine.dataprocessor.DataProcessor)
	 * @param dataProcessor
	 *            the DataProcessor to be removed
	 * @return error code from TraceViewerAPI
	 */
	public static TVAPIError removeDataProcessor(DataProcessor dataProcessor) {
		return TraceViewerAPI.removeDataProcessor(dataProcessor);
	}

	/**
	 * @see com.nokia.traceviewer.api.TraceViewerAPI#saveCurrentTracesToLog(java.lang.String)
	 * @param filePath
	 *            file path where to save the log. If null and TraceViewer view
	 *            is visible, a file selection dialog is shown
	 * @return error code from TraceViewerAPI
	 */
	public static TVAPIError saveCurrentTracesToLog(String filePath) {
		return TraceViewerAPI.saveCurrentTracesToLog(filePath);
	}

	/**
	 * @see com.nokia.traceviewer.api.TraceViewerAPI#sendDataToDeviceWithHeader(byte
	 *      [], boolean)
	 * 
	 * @param msg
	 *            byte array to be sent
	 * @param changeMediaType
	 *            If true, changes media type to the header when sending the
	 *            message. Otherwise leaves the message as it is.
	 * @return error code from TraceViewerAPI
	 * @throws TraceConnectivityException
	 */
	public static TVAPIError sendDataToDeviceWithHeader(byte[] msg,
			boolean changeMediaType) throws TraceConnectivityException {
		TVAPIError error = TraceViewerAPI.sendDataToDeviceWithHeader(msg,
				changeMediaType);
		if (error == TVAPIError.NOT_CONNECTED) {
			throw new TraceConnectivityException(error);
		}
		return error;
	}

	/**
	 * @see com.nokia.traceviewer.api.TraceViewerAPI#sendDataToDeviceWithoutHeader
	 *      (byte[], byte)
	 * 
	 * @param msg
	 *            byte array to be sent
	 * @param messageId
	 *            messageID to add to the header. Might not be needed, depends
	 *            on the protocol.
	 * @return error code from TraceViewerAPI
	 * @throws TraceConnectivityException
	 */
	public static TVAPIError sendDataToDeviceWithoutHeader(byte[] msg,
			byte messageId) throws TraceConnectivityException {
		TVAPIError error = TraceViewerAPI.sendDataToDeviceWithoutHeader(msg,
				messageId);
		if (error == TVAPIError.NOT_CONNECTED) {
			throw new TraceConnectivityException(error);
		}
		return error;
	}

	/**
	 * @see com.nokia.traceviewer.api.TraceViewerAPI#stopViewUpdate(boolean)
	 * 
	 * @param stop
	 *            if true, stops the view update. If false, restarts the update.
	 */
	public static void stopViewUpdate(boolean stop) {
		TraceViewerAPI.stopViewUpdate(stop);
	}

	/**
	 * @see com.nokia.traceviewer.api.TraceViewerAPI#syncToTimestamp(java.lang.String,
	 *      java.lang.String)
	 * 
	 * @param startTimestamp
	 *            start timestamp in the format of hh:mm:ss.SSS
	 * @param endTimestamp
	 *            end timestamp in the format of hh:mm:ss.SSS or null if only
	 *            start timestamp is searched for
	 * @return error code from TraceViewerAPI
	 */
	public static TVAPIError syncToTimestamp(String startTimestamp,
			String endTimestamp) {
		return TraceViewerAPI.syncToTimestamp(startTimestamp, endTimestamp);
	}

	/**
	 * @see com.nokia.traceviewer.api.TraceViewerAPI#syncToTrace(int, int)
	 * 
	 * @param startTrace
	 *            start trace number
	 * @param endTrace
	 *            end trace number of 0 if only start trace is searched for
	 * @return error code from TraceViewerAPI
	 */
	public static TVAPIError syncToTrace(int startTrace, int endTrace) {
		return TraceViewerAPI.syncToTrace(startTrace, endTrace);
	}

}
