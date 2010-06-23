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
 * Connection interface
 *
 */
package com.nokia.traceviewer.engine;

/**
 * Connection interface
 */
public interface Connection {

	/**
	 * Connect to media
	 * 
	 * @return true if connection succeeded, false otherwise
	 */
	public boolean connect();

	/**
	 * Creates a connection for given connection type. Types are taken from
	 * com.nokia.traceviewer.engine.preferences.PreferenceConstants.java
	 * 
	 * @param connectionType
	 *            connection type from
	 *            com.nokia.traceviewer.engine.preferences.PreferenceConstants
	 *            .java
	 * @param parameters
	 *            connection parameters. When using TCP connection, parameters
	 *            are IP address, port number and channel number in this order.
	 *            When using USB serial connection, the only parameter is the
	 *            COM port number.
	 * @param filePath
	 *            file path where the binary file is created
	 * @return true if connection was succesfully created, false otherwise
	 */
	public boolean createConnection(String connectionType, String[] parameters,
			String filePath);

	/**
	 * Disconnect from media
	 * 
	 * @return true if disconnect succeeded, false otherwise
	 */
	public boolean disconnect();

	/**
	 * Indicates the status of the connection
	 * 
	 * @return true if connection is up, false otherwise
	 */
	public boolean isConnected();

	/**
	 * Writes message to connection
	 * 
	 * @param byteArray
	 *            byte array containing data
	 * @return true if writing succeeded, false otherwise
	 */
	public boolean write(byte[] byteArray);
}
