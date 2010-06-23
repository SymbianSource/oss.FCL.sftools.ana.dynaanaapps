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
 * Handler for connect command
 *
 */
package com.nokia.traceviewer.action;

import java.net.URL;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;

import com.nokia.carbide.remoteconnections.RemoteConnectionsActivator;
import com.nokia.carbide.remoteconnections.interfaces.IConnection;
import com.nokia.traceviewer.TraceViewerHelpContextIDs;
import com.nokia.traceviewer.TraceViewerPlugin;
import com.nokia.traceviewer.api.TraceViewerAPI.TVAPIError;
import com.nokia.traceviewer.engine.ConnectionHelper;
import com.nokia.traceviewer.engine.TraceProvider;
import com.nokia.traceviewer.engine.TraceViewerGlobals;
import com.nokia.traceviewer.engine.TraceViewerDialogInterface.TVPreferencePage;
import com.nokia.traceviewer.engine.preferences.PreferenceConstants;

/**
 * Handler for connect command
 */
public final class ConnectAction extends TraceViewerAction {

	/**
	 * Connect image
	 */
	private static ImageDescriptor connectImage;

	/**
	 * Disconnect image
	 */
	private static ImageDescriptor disConnectImage;

	/**
	 * When set to false, don't show protocol change dialog anymore
	 */
	private boolean showProtocolChangeDialog = true;

	/**
	 * Last operation's error code
	 */
	private TVAPIError operationErrorCode;

	/**
	 * Show errors when connecting
	 */
	private boolean showErrors = true;

	/**
	 * When set to false, don't check if given preferences match the one's in
	 * Remote connections
	 */
	private boolean checkPreferences = true;

	/**
	 * Check preferences error
	 */
	private static final String CHECK_PREFERENCES_ERROR = com.nokia.traceviewer.action.Messages
			.getString("ConnectAction.ConnectionPreferencesError"); //$NON-NLS-1$

	/**
	 * Connection not supported error
	 */
	private static final String CONNECTION_NOT_SUPPORTED_ERROR = Messages
			.getString("ConnectAction.ConnectionNotSupported"); //$NON-NLS-1$

	static {
		URL url = null;
		URL url2 = null;
		url = TraceViewerPlugin.getDefault().getBundle().getEntry(
				"/icons/connect.gif"); //$NON-NLS-1$
		connectImage = ImageDescriptor.createFromURL(url);
		url2 = TraceViewerPlugin.getDefault().getBundle().getEntry(
				"/icons/disconnect.gif"); //$NON-NLS-1$
		disConnectImage = ImageDescriptor.createFromURL(url2);
	}

	/**
	 * Constructor
	 */
	ConnectAction() {

		// Check which action to create
		if (TraceViewerGlobals.getTraceViewer().getConnection() != null
				&& TraceViewerGlobals.getTraceViewer().getConnection()
						.isConnected()) {
			changeToDisconnectAction();
		} else {
			changeToConnectAction();
		}

		// Set help
		PlatformUI.getWorkbench().getHelpSystem().setHelp(this,
				TraceViewerHelpContextIDs.ACTIONS);
	}

	/**
	 * Connects to the target
	 * 
	 * @param showErrors
	 *            if true, show errors if there are any
	 * @param checkPreferences
	 *            if true, check if preferences match the ones in the selected
	 *            Remote Connection
	 * @return TVAPI error code
	 */
	public TVAPIError connect(boolean showErrors, boolean checkPreferences) {
		this.showErrors = showErrors;
		this.checkPreferences = checkPreferences;

		// Call doRun instead of run() to not care if TraceProvider is
		// registered yet or not.
		doRun();
		this.showErrors = true;
		this.checkPreferences = true;

		return operationErrorCode;
	}

	/**
	 * Disconnects from the target
	 * 
	 * @return true if disconnection succeeded, false otherwise
	 */
	public boolean disconnect() {

		// Call doRun instead of run() to not care if TraceProvider is
		// registered yet or not
		doRun();
		boolean succeed = false;
		if (operationErrorCode == TVAPIError.NONE) {
			succeed = true;
		}
		return succeed;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.traceviewer.action.TraceViewerAction#doRun()
	 */
	@Override
	protected void doRun() {
		TraceViewerGlobals.postUiEvent("ConnectButton", "1"); //$NON-NLS-1$ //$NON-NLS-2$
		TVAPIError errorCode;

		boolean connected = (TraceViewerGlobals.getTraceViewer()
				.getConnection() != null && TraceViewerGlobals.getTraceViewer()
				.getConnection().isConnected());
		// Connect
		if (!connected) {
			errorCode = doConnect();
			// Disconnect
		} else {
			errorCode = doDisconnect();
		}
		operationErrorCode = errorCode;
		TraceViewerGlobals.postUiEvent("ConnectButton", "0"); //$NON-NLS-1$ //$NON-NLS-2$
	}

	/**
	 * Do connect. Must be called from an UI thread!
	 * 
	 * @return TVAPI error code
	 */
	private TVAPIError doConnect() {
		TVAPIError errorCode = TVAPIError.INVALID_CONNECTION_PARAMETERS;

		boolean canConnect = true;

		// Check that a connection preferences are set. If not, open preferences
		// page. Doesn't open preferences page if "current connection" is
		// selected.
		canConnect = checkConnectionPreferences();

		// Ensure connection before connecting
		ConnectionHelper.saveConnectionSettingsToPreferenceStore(true);

		// Check used protocol if user want's to change it
		if (canConnect) {
			canConnect = checkUsedProtocol();

			// If the main DataReader isn't active, ask to create it again
			// and erase old data
			if (canConnect
					&& TraceViewerGlobals.getTraceViewer()
							.getDataProcessorAccess().getLogger()
							.isLogFileOpened()) {
				canConnect = handleCreatingNewDataReader();
			}
		}

		// Try to connect
		if (canConnect) {
			boolean success = TraceViewerGlobals.getTraceViewer().connect();
			if (success) {
				changeToDisconnectAction();
				errorCode = TVAPIError.NONE;

			} else if (!success && showErrors) {
				TraceViewerGlobals.getTraceViewer().getDialogs()
						.showErrorMessage(CHECK_PREFERENCES_ERROR);
			}

			// User canceled the connection
		} else {
			errorCode = TVAPIError.USER_CANCELED;
		}
		return errorCode;
	}

	/**
	 * Checks connection preferences
	 * 
	 * @return true if connection is selected, false otherwise
	 */
	private boolean checkConnectionPreferences() {
		boolean connectionSettingsDefined = true;
		IPreferenceStore store = TraceViewerPlugin.getDefault()
				.getPreferenceStore();

		// Open preferences page
		if (store.getString(PreferenceConstants.CONNECTION_TYPE).equals("") || !preferencesMatchSelectedConnection(store)) { //$NON-NLS-1$
			connectionSettingsDefined = TraceViewerGlobals.getTraceViewer()
					.getDialogs().openPreferencePage(
							TVPreferencePage.CONNECTION);

			// If user clicked OK, check that some connection is selected
			if (connectionSettingsDefined) {
				if (store.getString(PreferenceConstants.CONNECTION_TYPE)
						.equals("")) { //$NON-NLS-1$
					connectionSettingsDefined = false;
				}
			}
		}
		return connectionSettingsDefined;
	}

	/**
	 * Checks that set preferences match selected connections preferences
	 * 
	 * @param store
	 *            preference store
	 * @return true if set preferences match the parameters in selected Remote
	 *         Connection
	 */
	private boolean preferencesMatchSelectedConnection(IPreferenceStore store) {
		boolean matches = false;
		if (!checkPreferences || ConnectionHelper.isCurrentConnectionSelected) {
			matches = true;
		} else {
			String selectedIdentifier = store
					.getString(PreferenceConstants.SELECTED_CONNECTION_ID);

			// Get and compare the parameters from currently selected connection
			Iterator<IConnection> connections = RemoteConnectionsActivator
					.getConnectionsManager().getConnections().iterator();
			while (connections.hasNext()) {
				IConnection conn = connections.next();

				// First find the right connection with the identifier
				if (conn.getIdentifier().equals(selectedIdentifier)) {
					String connectionTypeId = conn.getConnectionType()
							.getIdentifier();
					String connectionType = store
							.getString(PreferenceConstants.CONNECTION_TYPE);
					boolean isMusti = connectionTypeId
							.equals("com.nokia.carbide.trk.support.connection.TCPIPConnectionType"); //$NON-NLS-1$

					boolean isPlatsim = connectionTypeId
							.equals("com.nokia.carbide.trk.support.connection.PlatSimConnectionType"); //$NON-NLS-1$

					// TCP / IP connection
					if (isMusti || isPlatsim) {
						String address = conn.getSettings().get("ipAddress"); //$NON-NLS-1$
						String port = conn.getSettings().get("port"); //$NON-NLS-1$

						// Check that connection type, address and port match
						if (connectionType
								.equals(PreferenceConstants.CONNECTION_TYPE_TCPIP)
								&& address
										.equals(store
												.getString(PreferenceConstants.IP_ADDRESS))
								&& port
										.equals(store
												.getString(PreferenceConstants.TCPIP_PORT))) {
							matches = true;
							break;
						}

						// USB connection
					} else if (connectionTypeId
							.equals("com.nokia.carbide.trk.support.connection.USBConnectionType")) { //$NON-NLS-1$
						String portNumStr = conn.getSettings().get("port"); //$NON-NLS-1$
						if (connectionType
								.equals(PreferenceConstants.CONNECTION_TYPE_USB_SERIAL)
								&& portNumStr
										.equals(store
												.getString(PreferenceConstants.USB_SERIAL_COM_PORT))) {
							matches = true;
							break;
						}
					}
				}
			}
		}
		return matches;
	}

	/**
	 * Do disconnect
	 * 
	 * @return TVAPI error code
	 */
	private TVAPIError doDisconnect() {
		TVAPIError errorCode;
		boolean success = TraceViewerGlobals.getTraceViewer().disconnect();
		if (success) {
			errorCode = TVAPIError.NONE;
			changeToConnectAction();

		} else {
			// Disconnecting failed error
			errorCode = TVAPIError.DISCONNECTING_FAILED;
		}
		return errorCode;
	}

	/**
	 * Checks used media protocol
	 * 
	 * @return true if TraceViewer can still continue connecting. False if
	 *         currently selected TraceProvider doesn't support connecting.
	 */
	private boolean checkUsedProtocol() {
		boolean canConnect = true;

		// First check there are more than one TraceProviders
		if (TraceViewerGlobals.getListOfTraceProviders().size() > 1
				&& showProtocolChangeDialog) {

			IPreferenceStore store = TraceViewerPlugin.getDefault()
					.getPreferenceStore();
			String selectedConnectionType = store
					.getString(PreferenceConstants.CONNECTION_TYPE);

			// Check if current TraceProvider has selected connection type as
			// preferred
			String preferredConnectionType = TraceViewerGlobals
					.getTraceProvider().getPreferredConnectionType();

			// If current TraceProvider returns not supported as preferred
			// connection type,
			if (preferredConnectionType
					.equals(PreferenceConstants.CONNECTION_TYPE_NOT_SUPPORTED)) {
				canConnect = false;
			}

			// Connection type doesn't match, find other TraceProvider which has
			// the selected connection type as preferred
			if (!preferredConnectionType.equals(selectedConnectionType)) {
				List<TraceProvider> traceProviders = TraceViewerGlobals
						.getListOfTraceProviders();

				// Go through list of TraceProviders
				for (int i = 0; i < traceProviders.size(); i++) {
					TraceProvider newProvider = traceProviders.get(i);
					if (newProvider.getPreferredConnectionType().equals(
							selectedConnectionType)) {

						// Create human readable change message
						String selectedConnectionHuman = Messages
								.getString("ConnectAction.USBSerialMsg"); //$NON-NLS-1$
						if (selectedConnectionType
								.equals(PreferenceConstants.CONNECTION_TYPE_TCPIP)) {
							selectedConnectionHuman = Messages
									.getString("ConnectAction.TCPIPMsg"); //$NON-NLS-1$
						}
						String changeMsgStart = Messages
								.getString("ConnectAction.ChangeDataFormatMsg1"); //$NON-NLS-1$
						String changeMsgEnd = Messages
								.getString("ConnectAction.ChangeDataFormatMsg2"); //$NON-NLS-1$

						String confirmationMsg = selectedConnectionHuman
								+ changeMsgStart + newProvider.getName()
								+ changeMsgEnd;

						// Ask the user if the TraceProvider should be changed
						boolean ret = TraceViewerGlobals.getTraceViewer()
								.getDialogs().showConfirmationDialog(
										confirmationMsg);

						// Change TraceProvider
						if (ret) {
							store.setValue(PreferenceConstants.DATA_FORMAT,
									newProvider.getName());
							TraceViewerGlobals.getTraceViewer().clearAllData();
							TraceViewerGlobals.setTraceProvider(newProvider,
									true);
							canConnect = true;
						} else {
							showProtocolChangeDialog = false;
						}
						break;
					}
				}
			}
		}

		// Current TraceProvider doesn't support connecting
		if (!canConnect && showErrors) {
			TraceViewerGlobals.getTraceViewer().getDialogs().showErrorMessage(
					TraceViewerGlobals.getTraceProvider().getName()
							+ CONNECTION_NOT_SUPPORTED_ERROR);
		}

		return canConnect;
	}

	/**
	 * Handles creation of new main DataReader
	 * 
	 * @return true if new main DataReader was created
	 */
	private boolean handleCreatingNewDataReader() {
		boolean canConnect;
		String confirmationMsg = Messages
				.getString("ConnectAction.EraseLogConfirmation"); //$NON-NLS-1$;

		boolean ret = TraceViewerGlobals.getTraceViewer().getDialogs()
				.showConfirmationDialog(confirmationMsg);

		// Ok
		if (ret) {
			// Create new main DataReader
			// Unpause if paused
			if (TraceViewerGlobals.getTraceViewer().getDataReaderAccess()
					.getMainDataReader().isPaused()) {
				TraceViewerGlobals.getTraceViewer().getView()
						.getActionFactory().getPauseAction().run();
			}
			TraceViewerGlobals.getTraceViewer().clearAllData();
			TraceViewerGlobals.getTraceViewer().getView().updateViewName();
			canConnect = true;
		} else {
			canConnect = false;
		}
		return canConnect;
	}

	/**
	 * Changes action to connect action
	 */
	public void changeToConnectAction() {

		if (Display.getCurrent() != null) {
			setImageDescriptor(connectImage);
			setText(Messages.getString("ConnectAction.Title")); //$NON-NLS-1$
			changeConnectToolTip();
		} else {
			Display.getDefault().syncExec(new Runnable() {

				/*
				 * (non-Javadoc)
				 * 
				 * @see java.lang.Runnable#run()
				 */
				public void run() {
					setImageDescriptor(connectImage);
					setText(Messages.getString("ConnectAction.Title")); //$NON-NLS-1$
					changeConnectToolTip();
				}

			});
		}
	}

	/**
	 * Changes action to disconnect action
	 */
	public void changeToDisconnectAction() {
		if (Display.getCurrent() != null) {
			setImageDescriptor(disConnectImage);
			setText(Messages.getString("DisconnectAction.Title")); //$NON-NLS-1$
			changeConnectToolTip();
		} else {
			Display.getDefault().syncExec(new Runnable() {

				/*
				 * (non-Javadoc)
				 * 
				 * @see java.lang.Runnable#run()
				 */
				public void run() {
					setImageDescriptor(disConnectImage);
					setText(Messages.getString("DisconnectAction.Title")); //$NON-NLS-1$
					changeConnectToolTip();
				}

			});
		}
	}

	/**
	 * Changes connect button tooltip if needed
	 */
	public void changeConnectToolTip() {
		IPreferenceStore store = TraceViewerPlugin.getDefault()
				.getPreferenceStore();

		boolean connected = (TraceViewerGlobals.getTraceViewer()
				.getConnection() != null && TraceViewerGlobals.getTraceViewer()
				.getConnection().isConnected());

		// Start with either connect or disconnect
		String toolTipText;
		if (!connected) {
			toolTipText = Messages.getString("ConnectAction.Tooltip"); //$NON-NLS-1$ 
		} else {
			toolTipText = Messages.getString("DisconnectAction.Tooltip"); //$NON-NLS-1$ 
		}

		// Construct the tooltip for connect action
		String connName = store
				.getString(PreferenceConstants.SELECTED_CONNECTION_NAME);

		// "Current connection" selected or already connected
		if (ConnectionHelper.isCurrentConnectionSelected || connected) {
			String curText = Messages.getString("ConnectAction.TooltipCurr"); //$NON-NLS-1$
			IConnection currentConnection = RemoteConnectionsActivator
					.getConnectionsManager().getCurrentConnection();
			if (currentConnection != null) {
				curText = currentConnection.getDisplayName();
			} else if (connected) {
				curText = connName;
			}
			toolTipText = toolTipText + curText;
		}

		// No connection selected
		else if (connName.equals("")) { //$NON-NLS-1$
			toolTipText = toolTipText
					+ Messages.getString("ConnectAction.Tooltip2"); //$NON-NLS-1$

			// Static connection selected
		} else {
			toolTipText = toolTipText + connName;
		}
		setToolTipText(toolTipText);
	}
}
