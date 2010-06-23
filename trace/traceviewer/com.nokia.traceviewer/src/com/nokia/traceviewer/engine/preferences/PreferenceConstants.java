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
 * Constant definitions for plug-in preferences
 *
 */
package com.nokia.traceviewer.engine.preferences;

/**
 * Constant definitions for plug-in preferences
 */
public final class PreferenceConstants {

	/**
	 * Connection type preference name
	 */
	public static final String CONNECTION_TYPE = "connectionTypePreference"; //$NON-NLS-1$

	/**
	 * Selected connection ID
	 */
	public static final String SELECTED_CONNECTION_ID = "selectedConnectionId"; //$NON-NLS-1$

	/**
	 * Selected connection name
	 */
	public static final String SELECTED_CONNECTION_NAME = "selectedConnectionName"; //$NON-NLS-1$

	/**
	 * Timestamp accuracy preference name
	 */
	public static final String TIMESTAMP_ACCURACY = "timestampAccuracyPreference"; //$NON-NLS-1$

	/**
	 * Data format preference name
	 */
	public static final String DATA_FORMAT = "dataFormatPreference"; //$NON-NLS-1$

	/**
	 * IP address preference name
	 */
	public static final String IP_ADDRESS = "ipAddressPreference"; //$NON-NLS-1$

	/**
	 * TCP/IP port preference name
	 */
	public static final String TCPIP_PORT = "tcpIpPortPreference"; //$NON-NLS-1$

	/**
	 * TCP/IP channel preference name
	 */
	public static final String TCPIP_CHANNEL = "tcpIpChannelPreference"; //$NON-NLS-1$

	/**
	 * TCP/IP default channel
	 */
	public static final int TCPIP_DEFAULT_CHANNEL = 2;

	/**
	 * TCP connection type
	 */
	public static final String CONNECTION_TYPE_TCPIP = "tcpip"; //$NON-NLS-1$

	/**
	 * USB connection type
	 */
	public static final String CONNECTION_TYPE_USB = "usb"; //$NON-NLS-1$

	/**
	 * USB Serial connection type
	 */
	public static final String CONNECTION_TYPE_USB_SERIAL = "usbserial"; //$NON-NLS-1$

	/**
	 * Connection is not supported
	 */
	public static final String CONNECTION_TYPE_NOT_SUPPORTED = "notsupported"; //$NON-NLS-1$

	/**
	 * COM port number in USB serial connection
	 */
	public static final String USB_SERIAL_COM_PORT = "serialcomport"; //$NON-NLS-1$

	/**
	 * Default USB serial COM port
	 */
	public static final String DEFAULT_USB_SERIAL_COM_PORT = "COM1"; //$NON-NLS-1$

	/**
	 * Millisecond timestamp accuracy
	 */
	public static final String MILLISECOND_ACCURACY = "millisecond"; //$NON-NLS-1$

	/**
	 * Microsecond timestamp accuracy
	 */
	public static final String MICROSECOND_ACCURACY = "microsecond"; //$NON-NLS-1$

	/**
	 * Show undecoded traces as info text
	 */
	public static final String UNDECODED_INFO_TEXT = "undecodedInfoText"; //$NON-NLS-1$

	/**
	 * Show undecoded traces as hex
	 */
	public static final String UNDECODED_HEX = "undecodedHex"; //$NON-NLS-1$

	/**
	 * Show undecoded traces as ID's and data
	 */
	public static final String UNDECODED_ID_AND_DATA = "undecodedIdAndData"; //$NON-NLS-1$

	/**
	 * Configuration file
	 */
	public static final String CONFIGURATION_FILE = "configurationFile"; //$NON-NLS-1$

	/**
	 * Default configuration file
	 */
	public static final String DEFAULT_CONFIGURATION_FILE = Messages
			.getString("PreferenceConstants.DefaultConfigurationFile"); //$NON-NLS-1$

	/**
	 * Use external filter command checkbox
	 */
	public static final String EXTERNAL_FILTER_CHECKBOX = "externalFilterCheckbox"; //$NON-NLS-1$

	/**
	 * Show time from previous trace checkbox
	 */
	public static final String TIME_FROM_PREVIOUS_TRACE_CHECKBOX = "timeFromPreviousTraceCheckbox"; //$NON-NLS-1$

	/**
	 * Show component and group name checkbox
	 */
	public static final String SHOW_COMPONENT_GROUP_NAME_CHECKBOX = "showComponentGroupNameCheckbox"; //$NON-NLS-1$

	/**
	 * Show class and function name checkbox
	 */
	public static final String SHOW_CLASS_FUNCTION_NAME_CHECKBOX = "showClassFunctionNameCheckbox"; //$NON-NLS-1$

	/**
	 * Automatically reload changed Dictionaries checkbox
	 */
	public static final String AUTO_RELOAD_DICTIONARIES_CHECKBOX = "autoReloadChangedDictionariesCheckbox"; //$NON-NLS-1$

	/**
	 * Show BTrace variables checkbox
	 */
	public static final String SHOW_BTRACE_VARIABLES_CHECKBOX = "showBTraceVariablesCheckbox"; //$NON-NLS-1$

	/**
	 * Show binary traces type radio group
	 */
	public static final String SHOW_UNDECODED_TRACES_TYPE = "showBinaryTracesType"; //$NON-NLS-1$

	/**
	 * External filter command
	 */
	public static final String EXTERNAL_FILTER_COMMAND = "externalFilterCommand"; //$NON-NLS-1$

	/**
	 * Auto-connect to dynamic connections checkbox
	 */
	public static final String AUTO_CONNECT_DYNAMIC_CONNECTIONS_CHECKBOX = "autoConnectDynamicConnections"; //$NON-NLS-1$
}
