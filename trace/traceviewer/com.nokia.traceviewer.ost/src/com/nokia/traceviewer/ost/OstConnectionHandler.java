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
 * OST connection handler
 *
 */
package com.nokia.traceviewer.ost;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;

import com.nokia.tcf.api.ITCAPIConnection;
import com.nokia.tcf.api.ITCConnection;
import com.nokia.tcf.api.ITCMessage;
import com.nokia.tcf.api.ITCMessageIds;
import com.nokia.tcf.api.ITCMessageOptions;
import com.nokia.tcf.api.TCFClassFactory;
import com.nokia.traceviewer.engine.Connection;
import com.nokia.traceviewer.engine.TraceFileHandler;
import com.nokia.traceviewer.engine.TraceViewerGlobals;
import com.nokia.traceviewer.engine.preferences.PreferenceConstants;

/**
 * OST connection handler
 */
public class OstConnectionHandler implements Connection, TraceFileHandler,
		OstConsts {

	/**
	 * Trace activation category
	 */
	private static final String TRACE_ACTIVATION_CATEGORY = Messages
			.getString("OstConnectionHandler.TraceActivationCategory"); //$NON-NLS-1$

	/**
	 * API to Target Connection Framework
	 */
	protected ITCAPIConnection mapi;

	/**
	 * Connection
	 */
	protected ITCConnection inConnection;

	/**
	 * Message options
	 */
	protected ITCMessageOptions inMessageOptions;

	/**
	 * Message IDs
	 */
	protected ITCMessageIds inMessageIds;

	/**
	 * Connected boolean
	 */
	protected boolean connected;

	/**
	 * Status of the connection
	 */
	protected IStatus connStatus;

	/**
	 * File path
	 */
	private String filePath;

	/**
	 * Constructor
	 */
	OstConnectionHandler() {

		// Create API connection
		mapi = TCFClassFactory.createITCAPIConnection();
		inMessageIds = TCFClassFactory.createITCMessageIds();

		// Get message IDs
		byte messageIds[] = new byte[] { OST_ASCII_TRACE_ID,
				OST_SIMPLE_TRACE_ID };

		// Add message IDs
		for (int i = 0; i < messageIds.length; i++) {
			inMessageIds.addMessageId(Byte.valueOf(messageIds[i]));
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.traceviewer.engine.Connection#connect()
	 */
	public boolean connect() {
		if (inConnection != null) {

			// Try the connection
			IPath path = new Path(filePath);
			inMessageOptions = TCFClassFactory.createITCMessageOptions(path);
			inMessageOptions
					.setMessageDestination(ITCMessageOptions.DESTINATION_CLIENTFILE);
			connStatus = mapi.connect(inConnection, inMessageOptions,
					inMessageIds);

			if (connStatus.isOK()) {
				connected = true;
			} else {
				connected = false;
				mapi.disconnect();
			}
		} else {
			connected = false;
			mapi.disconnect();
		}

		return connected;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.nokia.traceviewer.engine.Connection#createConnection(java.lang.String
	 * , java.lang.String[], java.lang.String)
	 */
	public boolean createConnection(String connectionType, String[] parameters,
			String filePath) {
		this.filePath = filePath;
		boolean success = false;

		// TCP / IP
		if (connectionType.equals(PreferenceConstants.CONNECTION_TYPE_TCPIP)) {

			String address = parameters[0];
			String port = parameters[1];

			// Create Real TCP connection
			inConnection = TCFClassFactory.createITCRealTCPConnection(address,
					port);
			success = true;

			// USB Serial
		} else if (connectionType
				.equals(PreferenceConstants.CONNECTION_TYPE_USB_SERIAL)) {

			String comPort = parameters[0];

			// Create Virtual serial connection
			inConnection = TCFClassFactory
					.createITCVirtualSerialConnection(comPort);
			success = true;
		}
		return success;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.traceviewer.engine.Connection#disconnect()
	 */
	public boolean disconnect() {
		boolean succeeded = false;
		IStatus status = mapi.disconnect();
		if (status.isOK()) {
			connected = false;
			succeeded = true;
		}
		return succeeded;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.traceviewer.engine.Connection#isConnected()
	 */
	public boolean isConnected() {
		return connected;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.traceviewer.engine.Connection#write(byte[])
	 */
	public boolean write(byte[] byteArray) {
		boolean succeeded = false;
		if (connStatus.isOK() && connected) {

			// Try to send the data to the media
			ITCMessage msg = TCFClassFactory.createITCMessage(byteArray);
			IStatus status = mapi.sendMessage(msg);

			// Post error message
			if (status.getCode() != IStatus.OK) {
				TraceViewerGlobals.postErrorEvent(Messages
						.getString("OstConnectionHandler.CannotSendMsg") //$NON-NLS-1$
						+ status.getMessage(), TRACE_ACTIVATION_CATEGORY, null);
			} else {
				succeeded = true;
			}

			// Not connected, post error message
		} else {
			TraceViewerGlobals
					.postErrorEvent(
							Messages
									.getString("OstConnectionHandler.CannotSendMsgNotConnected"), //$NON-NLS-1$
							TRACE_ACTIVATION_CATEGORY, null);
		}
		return succeeded;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.traceviewer.engine.TraceFileHandler#clearFile()
	 */
	public void clearFile() {
		if (mapi != null) {
			mapi.clearMessageFile();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.traceviewer.engine.TraceFileHandler#closeFile()
	 */
	public void closeFile() {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.traceviewer.engine.TraceFileHandler#openFile()
	 */
	public void openFile() {
	}
}
