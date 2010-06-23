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
 * Connection changed listener
 *
 */
package com.nokia.traceviewer.engine;

import java.util.Collection;

import org.eclipse.swt.widgets.Display;

import com.nokia.carbide.remoteconnections.RemoteConnectionsActivator;
import com.nokia.carbide.remoteconnections.interfaces.IConnection;
import com.nokia.carbide.remoteconnections.interfaces.IConnectionTypeProvider;
import com.nokia.carbide.remoteconnections.interfaces.IService;
import com.nokia.carbide.remoteconnections.interfaces.IConnectionsManager.IConnectionListener;
import com.nokia.carbide.remoteconnections.internal.api.IConnection2;
import com.nokia.traceviewer.TraceViewerPlugin;
import com.nokia.traceviewer.action.ConnectAction;
import com.nokia.traceviewer.engine.preferences.PreferenceConstants;
import com.nokia.traceviewer.internal.api.TraceViewerAPI2Impl;

/**
 * Connection changed listener
 */
class ConnectionChangedListener implements IConnectionListener {

	/*
	 * (non-Javadoc)
	 * 
	 * @seecom.nokia.carbide.remoteconnections.interfaces.IConnectionsManager.
	 * IConnectionListener
	 * #connectionAdded(com.nokia.carbide.remoteconnections.interfaces
	 * .IConnection)
	 */
	public void connectionAdded(IConnection arg0) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seecom.nokia.carbide.remoteconnections.interfaces.IConnectionsManager.
	 * IConnectionListener
	 * #connectionRemoved(com.nokia.carbide.remoteconnections.
	 * interfaces.IConnection)
	 */
	public void connectionRemoved(IConnection arg0) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seecom.nokia.carbide.remoteconnections.interfaces.IConnectionsManager.
	 * IConnectionListener
	 * #currentConnectionSet(com.nokia.carbide.remoteconnections
	 * .interfaces.IConnection)
	 */
	public void currentConnectionSet(IConnection newConnection) {
		if (ConnectionHelper.isCurrentConnectionSelected
				&& newConnection != null
				&& supportsTracingService(newConnection)) {

			// Save new settings
			if (TraceViewerGlobals.getTraceViewer().getConnection() == null
					|| !TraceViewerGlobals.getTraceViewer().getConnection()
							.isConnected()) {
				saveNewSettings();

				// Notify about connection preferences changed to listeners
				TraceViewerAPI2Impl.notifyConnPrefsChanged(newConnection);

				// If auto connect is on
				if (TraceViewerPlugin
						.getDefault()
						.getPreferenceStore()
						.getBoolean(
								PreferenceConstants.AUTO_CONNECT_DYNAMIC_CONNECTIONS_CHECKBOX)) {

					// IConnection2 object, dynamic connection and TraceViewer
					// view exists
					if (newConnection instanceof IConnection2) {
						if (((IConnection2) newConnection).isDynamic()
								&& TraceViewerGlobals.getTraceViewer()
										.getView() != null) {
							connect();
						}
					}
				}
			}
		}
	}

	/**
	 * Saves new settings
	 */
	private void saveNewSettings() {
		// Change connect tooltip
		final ConnectAction action = (ConnectAction) TraceViewerGlobals
				.getTraceViewer().getView().getActionFactory()
				.getConnectAction();

		// UI thread
		if (Display.getCurrent() != null) {
			ConnectionHelper.saveConnectionSettingsToPreferenceStore(true);
			action.changeConnectToolTip();

			// Not in UI thread, sync
		} else {
			Display.getDefault().syncExec(new Runnable() {

				/*
				 * (non-Javadoc)
				 * 
				 * @see java.lang.Runnable#run()
				 */
				public void run() {
					ConnectionHelper
							.saveConnectionSettingsToPreferenceStore(true);
					action.changeConnectToolTip();
				}

			});
		}

	}

	/**
	 * Connects
	 */
	private void connect() {

		// Change connect tooltip
		final ConnectAction action = (ConnectAction) TraceViewerGlobals
				.getTraceViewer().getView().getActionFactory()
				.getConnectAction();

		// UI thread
		if (Display.getCurrent() != null) {
			action.run();

			// Not in UI thread, sync
		} else {
			Display.getDefault().syncExec(new Runnable() {

				/*
				 * (non-Javadoc)
				 * 
				 * @see java.lang.Runnable#run()
				 */
				public void run() {
					action.run();
				}

			});
		}
	}

	/**
	 * Checks if the connection supports tracing service
	 * 
	 * @param connection
	 *            connection
	 * @return true if the connection supports tracing service
	 */
	private boolean supportsTracingService(IConnection connection) {
		boolean supports = false;
		IConnectionTypeProvider provider = RemoteConnectionsActivator
				.getConnectionTypeProvider();
		Collection<IService> services = provider
				.getCompatibleServices(connection.getConnectionType());
		for (IService service : services) {
			if (service.getIdentifier().equals(
					"com.nokia.carbide.trk.support.service.TracingService")) { //$NON-NLS-1$	
				supports = true;
				break;
			}
		}

		return supports;
	}

}
