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
 * Connection Helper class
 *
 */
package com.nokia.traceviewer.engine;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.preference.IPreferenceNode;
import org.eclipse.jface.preference.IPreferencePage;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.jface.preference.PreferenceManager;
import org.eclipse.jface.preference.PreferenceNode;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.PlatformUI;

import com.nokia.carbide.remoteconnections.RemoteConnectionsActivator;
import com.nokia.carbide.remoteconnections.interfaces.IClientServiceSiteUI2;
import com.nokia.carbide.remoteconnections.interfaces.IConnection;
import com.nokia.carbide.remoteconnections.interfaces.IService;
import com.nokia.carbide.remoteconnections.interfaces.IConnectionsManager.ISelectedConnectionInfo;
import com.nokia.traceviewer.TraceViewerPlugin;
import com.nokia.traceviewer.action.ConnectAction;
import com.nokia.traceviewer.engine.preferences.PreferenceConstants;
import com.nokia.traceviewer.engine.preferences.TraceViewerConnectionPreferencesPage;

/**
 * Connection Helper class
 */
public class ConnectionHelper {

	/**
	 * Current connection identifier
	 */
	public static String CURRENT_CONNECTION_ID = "com.nokia.carbide.remoteConnections.currentConnection"; //$NON-NLS-1$

	/**
	 * Client service UI
	 */
	private static IClientServiceSiteUI2 clientServiceUI = getClientServiceUI();

	/**
	 * Tells if "Current connection" is selected from TraceViewer preferences
	 */
	public static boolean isCurrentConnectionSelected;

	static {

		// Current connection is selected if ID is CURRENT_CONNECTION_ID or
		// empty
		String currConn = TraceViewerPlugin.getDefault().getPreferenceStore()
				.getString(PreferenceConstants.SELECTED_CONNECTION_ID);
		isCurrentConnectionSelected = currConn.equals(CURRENT_CONNECTION_ID)
				|| currConn.equals(""); //$NON-NLS-1$
	}

	/**
	 * The current connection ensured
	 */
	public static IConnection currentEnsuredConnection;

	/**
	 * Gets tracing service
	 * 
	 * @return tracing service
	 */
	public static IService getTracingService() {
		IService service = RemoteConnectionsActivator
				.getConnectionTypeProvider().findServiceByID(
						"com.nokia.carbide.trk.support.service.TracingService"); //$NON-NLS-1$	
		return service;
	}

	/**
	 * Gets selected connection object
	 * 
	 * @return selected connection object
	 */
	public static IConnection getSelectedConnection() {
		String selectedConnId = clientServiceUI.getSelectedConnection();

		IConnection conn = null;

		// Current connection selected
		if (selectedConnId != null
				&& selectedConnId.equals(CURRENT_CONNECTION_ID)) {
			isCurrentConnectionSelected = true;
		} else {
			isCurrentConnectionSelected = false;
			conn = getConnectionWithID(selectedConnId);

			// If connection doesn't exist, create ClientUI and select the
			// connection
			if (conn == null) {
				createClientUI();

				// Select connection with ID
				selectedConnId = TraceViewerPlugin.getDefault()
						.getPreferenceStore().getString(
								PreferenceConstants.SELECTED_CONNECTION_ID);
				clientServiceUI.selectConnection(selectedConnId);
				conn = getConnectionWithID(selectedConnId);
			}
		}

		return conn;
	}

	/**
	 * Gets connection with current ID
	 * 
	 * @return connection
	 */
	public static IConnection getConnectionWithCurrentID() {
		String selectedConnId = TraceViewerPlugin.getDefault()
				.getPreferenceStore().getString(
						PreferenceConstants.SELECTED_CONNECTION_ID);
		IConnection ret = getConnectionWithID(selectedConnId);

		return ret;
	}

	/**
	 * Gets connection with ID
	 * 
	 * @param id
	 *            the ID
	 * @return connection
	 */
	public static IConnection getConnectionWithID(String id) {
		IConnection ret = RemoteConnectionsActivator.getConnectionsManager()
				.findConnection(id);

		return ret;
	}

	/**
	 * Gets client service UI
	 * 
	 * @return client service UI
	 */
	public static IClientServiceSiteUI2 getClientServiceUI() {
		IClientServiceSiteUI2 ui = null;
		IService service = getTracingService();

		if (service != null) {
			ui = RemoteConnectionsActivator.getConnectionsManager()
					.getClientSiteUI2(service);
		}
		return ui;
	}

	/**
	 * Select connection from client UI with ID
	 * 
	 * @param id
	 *            the ID
	 */
	public static void selectConnectionFromUIWithID(String id) {
		if (clientServiceUI != null) {
			clientServiceUI.selectConnection(id);
		}
	}

	/**
	 * Creates client service UI
	 * 
	 * @param composite
	 *            composite
	 * @return composite or null if client service UI couldn't be constructed
	 */
	public static Composite createClientServiceUI(Composite composite) {
		Composite ret = null;
		if (clientServiceUI != null) {
			clientServiceUI.createComposite(composite);
			ret = composite;
		}
		return ret;
	}

	/**
	 * Creates Client UI
	 */
	private static void createClientUI() {
		PreferenceManager mgr = new PreferenceManager();

		// Create connection preference page
		IPreferencePage connectionPage = new TraceViewerConnectionPreferencesPage();
		IPreferenceNode connectionNode = new PreferenceNode("1", connectionPage); //$NON-NLS-1$
		mgr.addToRoot(connectionNode);

		// Create the dialog
		PreferenceDialog dialog = new PreferenceDialog(PlatformUI
				.getWorkbench().getDisplay().getActiveShell(), mgr);
		dialog.create();
	}

	/**
	 * Saves connection settings to preference store
	 * 
	 * @param ensureConnection
	 *            if true, connection is ensured. It means that if the saved ID
	 *            is not a ID of a "real" connection, we must find a real
	 *            connection for this virtual ID
	 * @return found IConnection object
	 */
	public static IConnection saveConnectionSettingsToPreferenceStore(
			boolean ensureConnection) {

		IPreferenceStore store = TraceViewerPlugin.getDefault()
				.getPreferenceStore();

		IConnection conn = null;

		if (ensureConnection) {
			String selectedConnId = clientServiceUI.getSelectedConnection();
			if (selectedConnId == null && isCurrentConnectionSelected) {
				selectedConnId = CURRENT_CONNECTION_ID;
			} else if (selectedConnId == null) {
				selectedConnId = store
						.getString(PreferenceConstants.SELECTED_CONNECTION_ID);
			}

			try {
				ISelectedConnectionInfo ret = RemoteConnectionsActivator
						.getConnectionsManager().ensureConnection(
								selectedConnId, getTracingService());
				conn = ret.getConnection();
			} catch (CoreException e) {
				e.printStackTrace();
			}

		} else {
			conn = ConnectionHelper.getSelectedConnection();
		}

		// Save the currently ensured connection object
		currentEnsuredConnection = conn;

		if (conn != null
				&& PlatformUI.getWorkbench().getDisplay().getActiveShell() != null
				&& !PlatformUI.getWorkbench().getDisplay().getActiveShell()
						.isDisposed()) {

			String connectionTypeId = conn.getConnectionType().getIdentifier();
			boolean isMusti = connectionTypeId
					.equals("com.nokia.carbide.trk.support.connection.TCPIPConnectionType"); //$NON-NLS-1$

			boolean isPlatsim = connectionTypeId
					.equals("com.nokia.carbide.trk.support.connection.PlatSimConnectionType"); //$NON-NLS-1$

			// TCP / IP connection
			if (isMusti || isPlatsim) {
				String address = conn.getSettings().get("ipAddress"); //$NON-NLS-1$
				String port = conn.getSettings().get("port"); //$NON-NLS-1$
				int channel = PreferenceConstants.TCPIP_DEFAULT_CHANNEL;
				if (isMusti) {
					try {
						channel = Integer.parseInt(conn.getSettings().get(
								"mustiChannel")); //$NON-NLS-1$
					} catch (NumberFormatException e) {
						channel = 1;
					}
				}

				// Save Connection type, IP address, port number and channel
				store.setValue(PreferenceConstants.CONNECTION_TYPE,
						PreferenceConstants.CONNECTION_TYPE_TCPIP);
				store.setValue(PreferenceConstants.IP_ADDRESS, address);
				store.setValue(PreferenceConstants.TCPIP_PORT, port);
				store.setValue(PreferenceConstants.TCPIP_CHANNEL, channel);

				// USB connection
			} else if (connectionTypeId
					.equals("com.nokia.carbide.trk.support.connection.USBConnectionType")) { //$NON-NLS-1$
				String portNumStr = conn.getSettings().get("port"); //$NON-NLS-1$
				int portNum = Integer.parseInt(portNumStr);

				// Save Connection type and port number
				store.setValue(PreferenceConstants.CONNECTION_TYPE,
						PreferenceConstants.CONNECTION_TYPE_USB_SERIAL);
				store
						.setValue(PreferenceConstants.USB_SERIAL_COM_PORT,
								portNum);

			}

			// Save selected connection name. Only save the identifier if we
			// have not selected the "current connection"
			store.setValue(PreferenceConstants.SELECTED_CONNECTION_NAME, conn
					.getDisplayName());

			if (!isCurrentConnectionSelected) {
				store.setValue(PreferenceConstants.SELECTED_CONNECTION_ID, conn
						.getIdentifier());
			}

			// Current connection selected
		} else if (isCurrentConnectionSelected) {
			store.setValue(PreferenceConstants.CONNECTION_TYPE,
					CURRENT_CONNECTION_ID);
			store.setValue(PreferenceConstants.SELECTED_CONNECTION_ID,
					CURRENT_CONNECTION_ID);
			store.setValue(PreferenceConstants.SELECTED_CONNECTION_NAME,
					CURRENT_CONNECTION_ID);

			// Couldn't find connection
		} else {
			store.setValue(PreferenceConstants.CONNECTION_TYPE, ""); //$NON-NLS-1$

			// Resetting selected connection identifier
			store.setValue(PreferenceConstants.SELECTED_CONNECTION_ID, ""); //$NON-NLS-1$
			store.setValue(PreferenceConstants.SELECTED_CONNECTION_NAME, ""); //$NON-NLS-1$
		}

		// Change connection button tooltip
		ConnectAction action = (ConnectAction) TraceViewerGlobals
				.getTraceViewer().getView().getActionFactory()
				.getConnectAction();
		action.changeConnectToolTip();

		return conn;
	}
}
