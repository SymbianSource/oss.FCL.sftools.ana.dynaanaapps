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
 * Connection Creator based on preferences
 *
 */
package com.nokia.traceviewer.engine;

import org.eclipse.jface.preference.IPreferenceStore;

import com.nokia.traceviewer.TraceViewerPlugin;
import com.nokia.traceviewer.engine.preferences.PreferenceConstants;

/**
 * Creates and returns a connection based on preferences
 * 
 */
public final class ConnectionCreator {

	/**
	 * Trace file path
	 */
	private final String traceFilePath;

	/**
	 * Constructor
	 * 
	 * @param traceFilePath
	 *            trace file path
	 */
	public ConnectionCreator(String traceFilePath) {
		this.traceFilePath = traceFilePath;
	}

	/**
	 * Creates and returns a connection
	 * 
	 * @return created connection
	 */
	public Connection getConnection() {
		Connection connection = null;
		IPreferenceStore store = TraceViewerPlugin.getDefault()
				.getPreferenceStore();

		// TCP connection
		if (store.getString(PreferenceConstants.CONNECTION_TYPE).equals(
				PreferenceConstants.CONNECTION_TYPE_TCPIP)) {

			connection = createTcpConnection(store);

			// USB Serial connection
		} else if (store.getString(PreferenceConstants.CONNECTION_TYPE).equals(
				PreferenceConstants.CONNECTION_TYPE_USB_SERIAL)) {
			connection = createUsbSerialConnection(store);
		}

		return connection;
	}

	/**
	 * Creates TCP connection
	 * 
	 * @param store
	 *            preference store
	 * @return created connection
	 */
	private Connection createTcpConnection(IPreferenceStore store) {
		Connection connection;

		// Close the old file from file handler
		TraceViewerGlobals.getTraceViewer().getFileHandler().closeFile();

		// Set parameters to array
		String[] parameters = new String[3];
		parameters[0] = store.getString(PreferenceConstants.IP_ADDRESS);
		parameters[1] = store.getString(PreferenceConstants.TCPIP_PORT);
		parameters[2] = store.getString(PreferenceConstants.TCPIP_CHANNEL);

		// Create connection
		connection = TraceViewerGlobals.getTraceProvider()
				.getConnectionHandler();
		connection.createConnection(PreferenceConstants.CONNECTION_TYPE_TCPIP,
				parameters, traceFilePath);

		// Set TCF connection as file handler
		TraceViewerGlobals.getTraceViewer().setFileHandler(
				(TraceFileHandler) connection);

		return connection;
	}

	/**
	 * Creates Serial connection
	 * 
	 * @param store
	 *            preference store
	 * @return created connection
	 */
	private Connection createUsbSerialConnection(IPreferenceStore store) {
		Connection connection = null;

		// Close the old file from file handler
		TraceViewerGlobals.getTraceViewer().getFileHandler().closeFile();

		// Set parameters to array
		String[] parameters = new String[1];
		parameters[0] = store
				.getString(PreferenceConstants.USB_SERIAL_COM_PORT);

		// Create connection
		connection = TraceViewerGlobals.getTraceProvider()
				.getConnectionHandler();
		connection.createConnection(
				PreferenceConstants.CONNECTION_TYPE_USB_SERIAL, parameters,
				traceFilePath);

		// Set TCF connection as file handler
		TraceViewerGlobals.getTraceViewer().setFileHandler(
				(TraceFileHandler) connection);

		return connection;
	}
}
