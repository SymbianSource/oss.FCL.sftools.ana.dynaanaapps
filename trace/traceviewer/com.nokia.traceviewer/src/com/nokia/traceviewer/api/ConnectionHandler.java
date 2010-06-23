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
 * Connection handler handles the connecting and disconnecting
 *
 */
package com.nokia.traceviewer.api;

import java.util.List;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.widgets.Display;

import com.nokia.traceviewer.TraceViewerPlugin;
import com.nokia.traceviewer.action.ConnectAction;
import com.nokia.traceviewer.api.TraceViewerAPI.TVAPIError;
import com.nokia.traceviewer.engine.TraceProvider;
import com.nokia.traceviewer.engine.TraceViewerGlobals;
import com.nokia.traceviewer.engine.preferences.PreferenceConstants;
import com.nokia.traceviewer.internal.api.TraceViewerAPI2Impl;

/**
 * Connection handler handles the connecting and disconnecting
 * 
 */
final class ConnectionHandler {

	/**
	 * Old connection method
	 */
	private String oldConnectionMethod;

	/**
	 * Old connection parameters
	 */
	private String[] oldParameters;

	/**
	 * Old connection ID that was selected.
	 */
	private String oldSelectedConnectionID;

	/**
	 * Connect using current settings defined in TraceViewer's preferences
	 * 
	 * @return error code from TraceViewerAPI
	 */
	public TVAPIError connect() {
		TVAPIError errorcode = TVAPIError.NONE;

		// Check if already connected
		if (isAlreadyConnected()) {
			errorcode = TVAPIError.ALREADY_CONNECTED;

		} else {

			// If view exists and we are in UI thread, do the connecting through
			// the view action
			if (TraceViewerGlobals.getTraceViewer().getView() != null
					&& Display.getCurrent() != null) {
				ConnectAction action = ((ConnectAction) (TraceViewerGlobals
						.getTraceViewer().getView().getActionFactory()
						.getConnectAction()));

				errorcode = action.connect(false, true);

				// Connect through engine
			} else {
				changeDataFormat();
				boolean success = TraceViewerGlobals.getTraceViewer().connect();

				if (!success) {
					errorcode = TVAPIError.INVALID_CONNECTION_SETTINGS;
				}

				// If succeeded and view exists, change button
				if (success
						&& TraceViewerGlobals.getTraceViewer().getView() != null) {
					ConnectAction action = ((ConnectAction) (TraceViewerGlobals
							.getTraceViewer().getView().getActionFactory()
							.getConnectAction()));
					action.changeToDisconnectAction();
				}
			}
		}
		return errorcode;
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
	public TVAPIError connect(int connectionMethod, String[] parameters) {
		TVAPIError errorcode = TVAPIError.NONE;

		// Check if already connected
		if (isAlreadyConnected()) {
			errorcode = TVAPIError.ALREADY_CONNECTED;

		} else {
			// Save old connection preferences
			saveConnectionPreferences();

			// Set new connection preferences
			boolean parametersOk = setNewConnectionPreferences(
					connectionMethod, parameters);

			if (parametersOk) {

				// If view exists and we are in UI thread, do the connecting
				// through the view action
				if (TraceViewerGlobals.getTraceViewer().getView() != null
						&& Display.getCurrent() != null) {
					ConnectAction action = ((ConnectAction) (TraceViewerGlobals
							.getTraceViewer().getView().getActionFactory()
							.getConnectAction()));
					errorcode = action.connect(false, false);

					// Connect through engine
				} else {
					changeDataFormat();
					boolean success = TraceViewerGlobals.getTraceViewer()
							.connect();

					if (success) {
						errorcode = TVAPIError.NONE;
					} else {
						errorcode = TVAPIError.INVALID_CONNECTION_SETTINGS;
					}
				}

				// Connecting failed
				if (errorcode != TVAPIError.NONE) {

					// Set old preferences back
					setOldPreferencesBack();

					// If succeeded and view exists, change button
				} else if (TraceViewerGlobals.getTraceViewer().getView() != null) {
					ConnectAction action = ((ConnectAction) (TraceViewerGlobals
							.getTraceViewer().getView().getActionFactory()
							.getConnectAction()));
					action.changeToDisconnectAction();
				}

			} else {
				// Set old preferences back
				setOldPreferencesBack();

				errorcode = TVAPIError.INVALID_CONNECTION_PARAMETERS;
			}
		}
		return errorcode;
	}

	/**
	 * Changes Data Format
	 */
	private void changeDataFormat() {

		// Get the TraceViewer preferenceStore
		IPreferenceStore store = TraceViewerPlugin.getDefault()
				.getPreferenceStore();

		// If there are more than one TraceProvider
		if (TraceViewerGlobals.getListOfTraceProviders().size() > 1) {

			String selectedConnectionType = store
					.getString(PreferenceConstants.CONNECTION_TYPE);

			// If the currently selected TraceProvider doesn't has selected
			// connection as preferred, try to find one that does
			if (!TraceViewerGlobals.getTraceProvider()
					.getPreferredConnectionType()
					.equals(selectedConnectionType)) {

				List<TraceProvider> traceProviders = TraceViewerGlobals
						.getListOfTraceProviders();

				// Go through list of TraceProviders
				for (int i = 0; i < traceProviders.size(); i++) {
					TraceProvider newProvider = traceProviders.get(i);
					if (newProvider.getPreferredConnectionType().equals(
							selectedConnectionType)) {
						store.setValue(PreferenceConstants.DATA_FORMAT,
								newProvider.getName());

						// Set new Trace provider
						TraceViewerGlobals.setTraceProvider(newProvider, true);
						break;
					}
				}
			}
		}
	}

	/**
	 * Sets new connection preferences
	 * 
	 * @param connectionMethod
	 *            connection method
	 * @param parameters
	 *            connection parameters
	 * @return true if succeeded, false if failed
	 */
	private boolean setNewConnectionPreferences(int connectionMethod,
			String[] parameters) {

		boolean succeeded = true;

		// Get the TraceViewer preferenceStore
		IPreferenceStore store = TraceViewerPlugin.getDefault()
				.getPreferenceStore();

		try {
			String connectionMethodStr = null;

			// TCP / IP
			if (connectionMethod == TraceViewerAPI.TVAPI_CONNECTION_TCP) {
				connectionMethodStr = PreferenceConstants.CONNECTION_TYPE_TCPIP;

				// There must be at least 2 parameters
				if (parameters != null && parameters.length > 1) {

					// Set IP address
					store.setValue(PreferenceConstants.IP_ADDRESS,
							parameters[0]);

					// Set port number
					store.setValue(PreferenceConstants.TCPIP_PORT,
							parameters[1]);

					// Check if there is channel specified
					if (parameters.length > 2) {

						// Set channel number
						store.setValue(PreferenceConstants.TCPIP_CHANNEL,
								parameters[2]);
					}

				} else {
					succeeded = false;
				}

				// USB Serial
			} else if (connectionMethod == TraceViewerAPI.TVAPI_CONNECTION_USB_SERIAL) {
				connectionMethodStr = PreferenceConstants.CONNECTION_TYPE_USB_SERIAL;

				// There must be at least 1 parameters
				if (parameters != null && parameters.length > 0) {

					// Set com port
					store.setValue(PreferenceConstants.USB_SERIAL_COM_PORT,
							parameters[0]);

				} else {
					succeeded = false;
				}
			}

			// Set the connection method
			if (succeeded) {
				store.setValue(PreferenceConstants.CONNECTION_TYPE,
						connectionMethodStr);

				// Old connection ID is not valid, since connection settings
				// have been changed.
				store.setValue(PreferenceConstants.SELECTED_CONNECTION_ID, ""); //$NON-NLS-1$
			}
		} catch (Exception e) {
			succeeded = false;
		}

		return succeeded;
	}

	/**
	 * Saves old connection preferences
	 */
	private void saveConnectionPreferences() {

		// Get the TraceViewer preferenceStore
		IPreferenceStore store = TraceViewerPlugin.getDefault()
				.getPreferenceStore();

		oldConnectionMethod = store
				.getString(PreferenceConstants.CONNECTION_TYPE);

		oldSelectedConnectionID = store
				.getString(PreferenceConstants.SELECTED_CONNECTION_ID);

		// TCP / IP connection
		if (oldConnectionMethod
				.equals(PreferenceConstants.CONNECTION_TYPE_TCPIP)) {
			oldParameters = new String[2];
			oldParameters[0] = store.getString(PreferenceConstants.IP_ADDRESS);
			oldParameters[1] = store.getString(PreferenceConstants.TCPIP_PORT);

			// USB Serial connection
		} else if (oldConnectionMethod
				.equals(PreferenceConstants.CONNECTION_TYPE_USB_SERIAL)) {
			oldParameters = new String[1];
			oldParameters[0] = store
					.getString(PreferenceConstants.USB_SERIAL_COM_PORT);
		}
	}

	/**
	 * Sets old connection preferences back to preference store
	 */
	private void setOldPreferencesBack() {

		// Get the TraceViewer preferenceStore
		IPreferenceStore store = TraceViewerPlugin.getDefault()
				.getPreferenceStore();

		// Set the connection method
		store
				.setValue(PreferenceConstants.CONNECTION_TYPE,
						oldConnectionMethod);
		// Set the selected connection ID
		store.setValue(PreferenceConstants.SELECTED_CONNECTION_ID,
				oldSelectedConnectionID);

		// TCP / IP connection
		if (oldConnectionMethod
				.equals(PreferenceConstants.CONNECTION_TYPE_TCPIP)) {
			store.setValue(PreferenceConstants.IP_ADDRESS, oldParameters[0]);
			store.setValue(PreferenceConstants.TCPIP_PORT, oldParameters[1]);

			// USB Serial connection
		} else if (oldConnectionMethod
				.equals(PreferenceConstants.CONNECTION_TYPE_USB_SERIAL)) {
			store.setValue(PreferenceConstants.USB_SERIAL_COM_PORT,
					oldParameters[0]);
		}

		// Resetting old connection settings
		oldConnectionMethod = null;
		oldParameters = null;
	}

	/**
	 * Checks if there are old connection preferences to restore.
	 * 
	 * @return <code>true</code> if are settings to restore, otherwise
	 *         <code>false</code>.
	 */
	public boolean hasRestorableConnectionPreferences() {
		return (oldConnectionMethod != null && oldParameters != null);
	}

	/**
	 * Disconnects from the target
	 * 
	 * @return error code from TraceViewerAPI
	 */
	public TVAPIError disconnect() {
		TVAPIError errorcode = TVAPIError.NONE;

		// Check if not connected
		if (!isAlreadyConnected()) {
			errorcode = TVAPIError.NOT_CONNECTED;

		} else {
			// Doing real disconnect only if client has been connected with
			// custom connection preferences
			// or there are no more registered clients left using the
			// connection.
			if (hasRestorableConnectionPreferences()
					|| !TraceViewerAPI2Impl.hasRegisteredClients()) {

				boolean disconnect;
				// If view exists and we are in UI thread, do the disconnecting
				// through the view action
				if (TraceViewerGlobals.getTraceViewer().getView() != null
						&& Display.getCurrent() != null) {
					ConnectAction action = ((ConnectAction) (TraceViewerGlobals
							.getTraceViewer().getView().getActionFactory()
							.getConnectAction()));
					disconnect = action.disconnect();

					// Disconnect through engine
				} else {
					disconnect = TraceViewerGlobals.getTraceViewer()
							.disconnect();
				}

				// Disconnection failed
				if (!disconnect) {
					errorcode = TVAPIError.DISCONNECTING_FAILED;

					// Disconnecting succeeded and view exists, change button
				} else if (TraceViewerGlobals.getTraceViewer().getView() != null) {
					ConnectAction action = ((ConnectAction) (TraceViewerGlobals
							.getTraceViewer().getView().getActionFactory()
							.getConnectAction()));
					action.changeToConnectAction();
				}
			}
		}

		// In case custom connection parameters were used in last connect...
		if (hasRestorableConnectionPreferences()) {
			// ... we are restoring the old settings back
			setOldPreferencesBack();
		}

		return errorcode;
	}

	/**
	 * Checks if the connection is already connected
	 * 
	 * @return true if already connected
	 */
	private boolean isAlreadyConnected() {
		boolean alreadyConnected = false;

		// Check if already connected
		if (TraceViewerGlobals.getTraceViewer().getConnection() != null) {
			if (TraceViewerGlobals.getTraceViewer().getConnection()
					.isConnected()) {
				alreadyConnected = true;
			}
		}
		return alreadyConnected;
	}
}
