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
 * Access point to TraceViewer API functions
 *
 */
package com.nokia.traceviewer.api;

import java.util.ArrayList;

import com.nokia.traceviewer.engine.TraceInformation;
import com.nokia.traceviewer.engine.TraceMetaData;
import com.nokia.traceviewer.engine.TraceViewerGlobals;
import com.nokia.traceviewer.engine.activation.TraceActivationComponentItem;
import com.nokia.traceviewer.engine.dataprocessor.DataProcessor;

/**
 * Access point to TraceViewer API functions
 */
public final class TraceViewerAPI {

	/**
	 * Enum indicating the error codes of TraceViewer API operations
	 */
	public enum TVAPIError {

		/**
		 * No error, operation succeeded.
		 */
		NONE,

		/**
		 * Not connected error. Is given when trying to operate with the
		 * connection when it is not open.
		 */
		NOT_CONNECTED,

		/**
		 * Disconnecting failed error.
		 */
		DISCONNECTING_FAILED,

		/**
		 * Invalid connection parameters error. Parameters are not what they
		 * should be for this connection method.
		 */
		INVALID_CONNECTION_PARAMETERS,

		/**
		 * Already connected error. Connection is already open.
		 */
		ALREADY_CONNECTED,

		/**
		 * Cannot open the connection port
		 */
		CANNOT_OPEN,

		/**
		 * Connection port already open
		 */
		ALREADY_OPEN,

		/**
		 * Invalid connection handle
		 */
		INVALID_CONNECTION_HANDLE,

		/**
		 * Timeout when connecting
		 */
		TIMEOUT,

		/**
		 * Tracefile is already open
		 */
		TRACEFILE_ALREADY_OPEN,

		/**
		 * Tracefile doesn't exist
		 */
		TRACEFILE_DOESNT_EXIST,

		/**
		 * No tracefile open where to write
		 */
		NO_TRACEFILE_OPEN,

		/**
		 * Cannot write to the tracefile
		 */
		CANNOT_WRITE_TO_TRACEFILE,

		/**
		 * Media seems not to be open
		 */
		MEDIA_NOT_OPEN,

		/**
		 * Invalid connection settings
		 */
		INVALID_CONNECTION_SETTINGS,

		/**
		 * Cannot find trace router application
		 */
		CANNOT_FIND_TRACEROUTER,

		/**
		 * Cannot create trace router process
		 */
		CANNOT_CREATE_TRACEROUTER,

		/**
		 * Media busy
		 */
		MEDIA_BUSY,

		/**
		 * User canceled connection
		 */
		USER_CANCELED,

		/**
		 * Unknown error
		 */
		UNKNOWN_ERROR,

		/**
		 * DataProcessor already added error. DataProcessor is already in the
		 * DataProcessor list.
		 */
		DATAPROCESSOR_ALREADY_ADDED,

		/**
		 * DataProcessor could not be found error. DataProcessor was not in the
		 * DataProcessor list.
		 */
		DATAPROCESSOR_NOT_FOUND,

		/**
		 * Decode Provider plugin is missing
		 */
		DECODE_PROVIDER_PLUGIN_MISSING,

		/**
		 * No Decode files loaded error.
		 */
		DECODE_FILE_NOT_LOADED,

		/**
		 * Some groups couldn't be found from the decode model. They are
		 * propably missing from the loaded decode files.
		 */
		SOME_GROUPS_NOT_FOUND_FROM_DECODE_MODEL,

		/**
		 * File doesn't exist
		 */
		FILE_DOES_NOT_EXIST,

		/**
		 * TraceViewer view not open
		 */
		TRACE_VIEW_NOT_OPEN;
	}

	/**
	 * TCP / IP connection method value
	 */
	public static final int TVAPI_CONNECTION_TCP = 1;

	/**
	 * USB Serial connection method value
	 */
	public static final int TVAPI_CONNECTION_USB_SERIAL = 2;

	/**
	 * DataProcessor adder object
	 */
	private static DataProcessorAdder dpAdder;

	/**
	 * Data sender object
	 */
	private static DataSender dataSender;

	/**
	 * Connection handler object
	 */
	private static ConnectionHandler connectionHandler;

	/**
	 * Decode handler object
	 */
	private static DecodeHandler decodeHandler;

	/**
	 * View handler object
	 */
	private static ViewHandler viewHandler;

	/**
	 * Log handler object
	 */
	private static LogHandler logHandler;

	/**
	 * Activates traces using group ID's
	 * 
	 * @param componentId
	 *            component ID
	 * @param groupIds
	 *            array of group IDs
	 * @param activate
	 *            if true, activate. If false, deactivate.
	 * @return error code from TraceViewerAPI
	 */
	public static TVAPIError activateTraces(int componentId, int[] groupIds,
			boolean activate) {

		// Create the sender if it doesn't exist
		if (dataSender == null) {
			dataSender = new DataSender();
		}

		// Send the data and check error status
		TVAPIError errorCode = dataSender.activateTraces(componentId, groupIds,
				activate);

		return errorCode;

	}

	/**
	 * Activates traces using group names
	 * 
	 * @param componentId
	 *            component ID
	 * @param groupNames
	 *            array of group names
	 * @param activate
	 *            if true, activate. If false, deactivate.
	 * @return error code from TraceViewerAPI
	 */
	public static TVAPIError activateTraces(int componentId,
			String[] groupNames, boolean activate) {

		// Create the sender if it doesn't exist
		if (dataSender == null) {
			dataSender = new DataSender();
		}

		// Send the data and check error status
		TVAPIError errorCode = dataSender.activateTraces(componentId,
				groupNames, activate);

		return errorCode;

	}

	/**
	 * Adds own DataProcessor to a specific location in dataprocessor list.
	 * DataProcessors registered through this method will receive all traces
	 * that are coming from the device.
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

		// Create the adder if it doesn't exist
		if (dpAdder == null) {
			dpAdder = new DataProcessorAdder();
		}

		// Add the processor and check error status
		TVAPIError errorCode = dpAdder.addDataProcessor(dataProcessor,
				location, priority);

		return errorCode;
	}

	/**
	 * Connect using current settings defined in TraceViewer's preferences
	 * 
	 * @return error code from TraceViewerAPI
	 */
	public static TVAPIError connect() {

		// Create the connection handler if it doesn't exist
		if (connectionHandler == null) {
			connectionHandler = new ConnectionHandler();
		}

		// Try to connect
		TVAPIError errorCode = connectionHandler.connect();

		return errorCode;
	}

	/**
	 * Connect using given parameters. Possible connection methods can be found
	 * from TraceViewerAPI constants. Parameters should be given in this order:
	 * When using TCP connection, parameters are IP address port number and
	 * channel number in this order. When using USB serial connection, the only
	 * parameter is the COM port number. This function will also set given
	 * parameters to TraceViewer's connection preferences if the connecting
	 * succeeds.
	 * 
	 * @param connectionMethod
	 *            the connection method to use
	 * @param parameters
	 *            array of parameters
	 * @return error code from TraceViewerAPI
	 */
	public static TVAPIError connect(int connectionMethod, String[] parameters) {

		// Create the connection handler if it doesn't exist
		if (connectionHandler == null) {
			connectionHandler = new ConnectionHandler();
		}

		// Try to connect
		TVAPIError errorCode = connectionHandler.connect(connectionMethod,
				parameters);

		return errorCode;
	}

	/**
	 * Disconnects from the target
	 * 
	 * @return error code from TraceViewerAPI
	 */
	public static TVAPIError disconnect() {

		// Create the connection handler if it doesn't exist
		if (connectionHandler == null) {
			connectionHandler = new ConnectionHandler();
		}

		// Try to disconnect
		TVAPIError errorCode = connectionHandler.disconnect();

		return errorCode;
	}

	/**
	 * Gets components from all loaded Dictionaries
	 * 
	 * @return list of components from all loaded Dictionaries. List can be
	 *         empty.
	 */
	public static ArrayList<TraceActivationComponentItem> getDictionaryComponents() {

		// Create the decode handler if it doesn't exist
		if (decodeHandler == null) {
			decodeHandler = new DecodeHandler();
		}

		// Get the component list
		ArrayList<TraceActivationComponentItem> components = decodeHandler
				.getDictionaryComponents();

		return components;
	}

	/**
	 * Gets trace component name from the Decode model with a component ID
	 * 
	 * @param componentId
	 *            component ID
	 * @return component name or null if not found or no Decode files are loaded
	 */
	public static String getTraceComponentName(int componentId) {

		// Create the decode handler if it doesn't exist
		if (decodeHandler == null) {
			decodeHandler = new DecodeHandler();
		}

		// Get the component name
		String componentName = decodeHandler.getTraceComponentName(componentId);

		return componentName;
	}

	/**
	 * Gets trace group ID from the Decode model with a group name
	 * 
	 * @param componentId
	 *            component ID
	 * @param groupName
	 *            the group name
	 * @return trace group ID or -1 if group ID not found or -2 if no Decode
	 *         files are loaded
	 */
	public static int getTraceGroupId(int componentId, String groupName) {

		// Create the decode handler if it doesn't exist
		if (decodeHandler == null) {
			decodeHandler = new DecodeHandler();
		}

		// Get the group ID
		int groupId = decodeHandler.getTraceGroupId(componentId, groupName);

		return groupId;
	}

	/**
	 * Gets trace group name from the Decode model with a component and group
	 * IDs
	 * 
	 * @param componentId
	 *            component ID
	 * @param groupId
	 *            group ID
	 * @return group name or null if not found or no Decode files are loaded
	 */
	public static String getTraceGroupName(int componentId, int groupId) {

		// Create the decode handler if it doesn't exist
		if (decodeHandler == null) {
			decodeHandler = new DecodeHandler();
		}

		// Get the group name
		String groupName = decodeHandler
				.getTraceGroupName(componentId, groupId);

		return groupName;
	}

	/**
	 * Gets trace name from the Decode model with a component, group and trace
	 * IDs
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

		// Create the decode handler if it doesn't exist
		if (decodeHandler == null) {
			decodeHandler = new DecodeHandler();
		}

		// Get the trace name
		String traceName = decodeHandler.getTraceName(componentId, groupId,
				traceId);

		return traceName;
	}

	/**
	 * Gets trace metadata
	 * 
	 * @param information
	 *            TraceInformation to be used when finding correct metadata
	 * @return trace metadata or null if nothing is found
	 */
	public static TraceMetaData getTraceMetaData(TraceInformation information) {
		TraceMetaData metaData = null;
		if (information != null && information.isDefined()
				&& TraceViewerGlobals.getDecodeProvider() != null) {
			metaData = TraceViewerGlobals.getDecodeProvider().getTraceMetaData(
					information);
		}

		return metaData;
	}

	/**
	 * Loads decode file to the decode model. User must remember that loading
	 * the decode file can take some time and this function will block until the
	 * model is loaded or a maximum of 5 seconds.
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

		// Create the decode handler if it doesn't exist
		if (decodeHandler == null) {
			decodeHandler = new DecodeHandler();
		}

		// Load the decode file
		TVAPIError errorCode = decodeHandler.loadDecodeFile(decodeFilePath,
				deleteExistingModel);

		return errorCode;
	}

	/**
	 * Opens log file to TraceViewer. If filePath is null and TraceViewer view
	 * is not open, error will be returned.
	 * 
	 * @param filePath
	 *            file path to open. If file path ends with .txt, file is opened
	 *            as ASCII log. If filePath is null and TraceViewer view is
	 *            open, a file selection dialog is opened.
	 * @return error code from TraceViewerAPI
	 */
	public static TVAPIError openLogFile(String filePath) {

		// Create the handler if it doesn't exist
		if (logHandler == null) {
			logHandler = new LogHandler();
		}

		// Save current traces to a log file
		TVAPIError errorCode = logHandler.openLogFile(filePath);

		return errorCode;
	}

	/**
	 * Removes this DataProcessor from the list of DataProcessors.
	 * 
	 * @param dataProcessor
	 *            the DataProcessor to be removed
	 * @return error code from TraceViewerAPI
	 */
	public static TVAPIError removeDataProcessor(DataProcessor dataProcessor) {

		// Create the adder if it doesn't exist
		if (dpAdder == null) {
			dpAdder = new DataProcessorAdder();
		}

		// Remove the Data Processor
		TVAPIError errorCode = dpAdder.removeDataProcessor(dataProcessor);

		return errorCode;
	}

	/**
	 * Saves current traces shown in TraceViewer view to a Binary log. If
	 * TraceViewer view is visible, a progress bar about the saving will be
	 * shown for the user. If TraceViewer view is not visible, saving the file
	 * will be done with the calling thread. Note that saving the file can take
	 * a long time!
	 * 
	 * @param filePath
	 *            file path where to save the log. If null and TraceViewer view
	 *            is visible, a file selection dialog is shown
	 * @return error code from TraceViewerAPI
	 */
	public static TVAPIError saveCurrentTracesToLog(String filePath) {

		// Create the handler if it doesn't exist
		if (logHandler == null) {
			logHandler = new LogHandler();
		}

		// Save current traces to a log file
		TVAPIError errorCode = logHandler.saveCurrentTracesToLog(filePath);

		return errorCode;
	}

	/**
	 * Sends raw data to the device with header
	 * 
	 * @param msg
	 *            byte array to be sent
	 * @param changeMediaType
	 *            If true, changes media type to the header when sending the
	 *            message. Otherwise leaves the message as it is.
	 * @return error code from TraceViewerAPI
	 */
	public static TVAPIError sendDataToDeviceWithHeader(byte[] msg,
			boolean changeMediaType) {

		// Create the sender if it doesn't exist
		if (dataSender == null) {
			dataSender = new DataSender();
		}

		// Send the data and check error status
		byte b = 0x00;
		TVAPIError errorCode = dataSender.sendRawData(msg, false, b,
				changeMediaType);

		return errorCode;
	}

	/**
	 * Sends raw data to the device without header
	 * 
	 * @param msg
	 *            byte array to be sent
	 * @param messageId
	 *            messageID to add to the header. Might not be needed, depends
	 *            on the protocol.
	 * @return error code from TraceViewerAPI
	 */
	public static TVAPIError sendDataToDeviceWithoutHeader(byte[] msg,
			byte messageId) {

		// Create the sender if it doesn't exist
		if (dataSender == null) {
			dataSender = new DataSender();
		}

		// Send the data and check error status
		TVAPIError errorCode = dataSender.sendRawData(msg, true, messageId,
				false);

		return errorCode;
	}

	/**
	 * Stops or restarts the TraceViewer view update
	 * 
	 * @param stop
	 *            if true, stops the view update. If false, restarts the update.
	 */
	public static void stopViewUpdate(boolean stop) {

		// Create the handler if it doesn't exist
		if (viewHandler == null) {
			viewHandler = new ViewHandler();
		}

		// Ask handler to stop / restart view update
		viewHandler.stopViewUpdate(stop);
	}

	/**
	 * Syncs to timestamp in the TraceViewer view. If both start and end
	 * timestamps are given, the range is selected. This function assumes that
	 * the traces in the TraceViewer view are in chronological order. Also,
	 * endTimestamp must always be "bigger" than startTimestamp
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

		// Create the handler if it doesn't exist
		if (viewHandler == null) {
			viewHandler = new ViewHandler();
		}

		// Try to sync and check error status
		TVAPIError errorCode = viewHandler.syncToTimestamp(startTimestamp,
				endTimestamp);

		return errorCode;
	}

	/**
	 * Syncs to trace in the TraceViewer view
	 * 
	 * @param startTrace
	 *            start trace number
	 * @param endTrace
	 *            end trace number of 0 if only start trace is searched for
	 * @return error code from TraceViewerAPI
	 */
	public static TVAPIError syncToTrace(int startTrace, int endTrace) {

		// Create the handler if it doesn't exist
		if (viewHandler == null) {
			viewHandler = new ViewHandler();
		}

		// Try to sync and check error status
		TVAPIError errorCode = viewHandler.syncToTrace(startTrace, endTrace);

		return errorCode;
	}
}
