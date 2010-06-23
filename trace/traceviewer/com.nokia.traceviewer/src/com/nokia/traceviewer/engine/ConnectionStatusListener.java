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
 * Connection status listener
 *
 */
package com.nokia.traceviewer.engine;

import org.eclipse.swt.widgets.Display;

import com.nokia.carbide.remoteconnections.internal.api.IConnection2.IConnectionStatus;
import com.nokia.carbide.remoteconnections.internal.api.IConnection2.IConnectionStatusChangedListener;
import com.nokia.carbide.remoteconnections.internal.api.IConnection2.IConnectionStatus.EConnectionStatus;

/**
 * Connection status listener
 */
public class ConnectionStatusListener implements
		IConnectionStatusChangedListener {

	/*
	 * (non-Javadoc)
	 * 
	 * @seecom.nokia.carbide.remoteconnections.internal.api.IConnection2.
	 * IConnectionStatusChangedListener
	 * #statusChanged(com.nokia.carbide.remoteconnections
	 * .internal.api.IConnection2.IConnectionStatus)
	 */
	public void statusChanged(IConnectionStatus status) {

		// Connection disconnected
		if (status.getEConnectionStatus().equals(
				EConnectionStatus.IN_USE_DISCONNECTED)) {
			if (TraceViewerGlobals.getTraceViewer().getConnection() != null
					&& TraceViewerGlobals.getTraceViewer().getConnection()
							.isConnected()) {
				runDisconnect();
			}
		}
	}

	/**
	 * Runs disconnect
	 */
	private void runDisconnect() {

		// Ensure UI thread
		if (Display.getCurrent() != null) {
			TraceViewerGlobals.getTraceViewer().getView().getActionFactory()
					.getConnectAction().run();
		} else {
			Display.getDefault().syncExec(new Runnable() {

				/*
				 * (non-Javadoc)
				 * 
				 * @see java.lang.Runnable#run()
				 */
				public void run() {
					TraceViewerGlobals.getTraceViewer().getView()
							.getActionFactory().getConnectAction().run();
				}

			});
		}
	}
}
