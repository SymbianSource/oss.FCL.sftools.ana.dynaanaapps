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
 * Engine of the OST Trace Provider
 *
 */
package com.nokia.traceviewer.ost;

import java.nio.channels.ByteChannel;

import com.nokia.traceviewer.engine.Connection;
import com.nokia.traceviewer.engine.DataReader;
import com.nokia.traceviewer.engine.DataWriter;
import com.nokia.traceviewer.engine.MediaCallback;
import com.nokia.traceviewer.engine.TraceConfiguration;
import com.nokia.traceviewer.engine.TraceProvider;

/**
 * OST engine
 */
public class OstEngine implements TraceProvider, OstConsts {

	/**
	 * Connection handler
	 */
	private OstConnectionHandler connectionHandler;

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.traceviewer.engine.TraceProvider#activate(boolean,
	 * boolean, int, int[])
	 */
	public void activate(boolean activate, boolean overWrite, int componentId,
			int[] groupIds) {
		OstMessageSender sender = new OstMessageSender();
		sender.sendActivationMessage(activate, componentId, groupIds);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.nokia.traceviewer.engine.TraceProvider#checkIfFileFormatMatches(java
	 * .lang.String)
	 */
	public boolean checkIfFileFormatMatches(String filePath) {
		OstDataMatcherChecker checker = new OstDataMatcherChecker();
		boolean matches = checker.checkIfFileFormatMatches(filePath);
		return matches;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.nokia.traceviewer.engine.TraceProvider#createDataReader(com.nokia
	 * .traceviewer.engine.MediaCallback,
	 * com.nokia.traceviewer.engine.TraceConfiguration)
	 */
	public DataReader createDataReader(MediaCallback callback,
			TraceConfiguration conf) {

		DataReader dataReader = new OstReader(callback, conf);
		return dataReader;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.nokia.traceviewer.engine.TraceProvider#createDataWriter(java.nio.
	 * channels.ByteChannel)
	 */
	public DataWriter createDataWriter(ByteChannel writeChannel) {
		DataWriter newWriter = new OstWriter(writeChannel);
		return newWriter;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.traceviewer.engine.TraceProvider#getConnectionHandler()
	 */
	public Connection getConnectionHandler() {
		if (connectionHandler == null) {
			connectionHandler = new OstConnectionHandler();
		}
		return connectionHandler;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.traceviewer.engine.TraceProvider#getName()
	 */
	public String getName() {
		return "OST - Open System Trace"; //$NON-NLS-1$
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.nokia.traceviewer.engine.TraceProvider#getPreferredConnectionType()
	 */
	public String getPreferredConnectionType() {
		return com.nokia.traceviewer.engine.preferences.PreferenceConstants.CONNECTION_TYPE_USB_SERIAL;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.nokia.traceviewer.engine.TraceProvider#sendActivationmessage(byte[],
	 * byte)
	 */
	public void sendMessage(byte[] message, boolean addHeader, byte messageId,
			boolean changeMediaType) {
		OstMessageSender sender = new OstMessageSender();
		sender.sendMessage(message, addHeader, messageId);
	}
}
