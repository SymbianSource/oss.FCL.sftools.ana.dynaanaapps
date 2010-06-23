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
 * TraceProvider provides traces to the TraceViewer engine
 *
 */
package com.nokia.traceviewer.engine;

import java.nio.channels.ByteChannel;

/**
 * TraceProvider interface
 */
public interface TraceProvider {

	/**
	 * Requests TraceProvider to send an activate message to the device using
	 * given component ID and array of group IDs.
	 * 
	 * @param activate
	 *            if true, given groups should be activated. If false, given
	 *            groups should be deactivated.
	 * @param overWrite
	 *            If true, all other groups not listed in groupsIds array are
	 *            deactivated. Has only effect if activate boolean is also true.
	 * @param componentId
	 *            component ID
	 * @param groupIds
	 *            array of group IDs
	 */
	public void activate(boolean activate, boolean overWrite, int componentId,
			int[] groupIds);

	/**
	 * Checks if file format seems to match to this TraceProvider
	 * 
	 * @param filePath
	 *            file path
	 * @return true if file format seems to match to this TraceProvider
	 */
	public boolean checkIfFileFormatMatches(String filePath);

	/**
	 * Creates and returns new data reader
	 * 
	 * @param callback
	 *            callback to return traces to
	 * @param conf
	 *            trace configuration to use in the reader
	 * @return new data reader
	 */
	public DataReader createDataReader(MediaCallback callback,
			TraceConfiguration conf);

	/**
	 * Creates and returns data writer
	 * 
	 * @param writeChannel
	 *            write channel to write to
	 * @return the newly created data writer
	 */
	public DataWriter createDataWriter(ByteChannel writeChannel);

	/**
	 * Gets connection handler for this TraceProvider
	 * 
	 * @return connection handler
	 */
	public Connection getConnectionHandler();

	/**
	 * Gets name of the TraceProvider
	 * 
	 * @return the name of the TraceProvider
	 */
	public String getName();

	/**
	 * Gets preferred connection type for this TraceProvider. Types are taken
	 * from com.nokia.traceviewer.engine.preferences.PreferenceConstants.java
	 * 
	 * @return preferred connection type
	 */
	public String getPreferredConnectionType();

	/**
	 * Sends message to the device. Adds header to the message with given
	 * message ID
	 * 
	 * @param message
	 *            the message
	 * @param addHeader
	 *            if true, protocol header should be added to the message before
	 *            sending
	 * @param messageId
	 *            message ID
	 * @param changeMediaType
	 *            if true, media type should be changed to the selected one from
	 *            the message. If addHeader is true, this is automatically done.
	 */
	public void sendMessage(byte[] message, boolean addHeader, byte messageId,
			boolean changeMediaType);
}
