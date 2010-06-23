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
 */

package com.nokia.traceviewer.internal.api;

import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import com.nokia.carbide.remoteconnections.interfaces.IConnection;
import com.nokia.traceviewer.api.ITraceConnClient;
import com.nokia.traceviewer.api.ITraceConnInfo;
import com.nokia.traceviewer.api.TraceConnectivityException;
import com.nokia.traceviewer.api.TraceViewerAPI;
import com.nokia.traceviewer.api.TraceViewerAPI2;
import com.nokia.traceviewer.api.TraceViewerAPI.TVAPIError;
import com.nokia.traceviewer.engine.ConnectionHelper;

/**
 * Implementation if TraceViewer API functions that offers - notification
 * services for client on connection status changes. - keeps count of active
 * clients, and disconnects data source only when there is no more active
 * clients.
 */
public class TraceViewerAPI2Impl {

	/**
	 * Storage for currently registered clients.
	 */
	public static Set<ITraceConnClient> clients = Collections
			.synchronizedSet(new HashSet<ITraceConnClient>());

	/**
	 * Connect using current settings defined in TraceViewer's preferences.
	 * Client is registered successfully in case no exceptions are thrown.
	 * 
	 * @param client
	 *            client requesting the connection.
	 * @return connection info about the connected service.
	 * @throws TraceConnectivityException
	 */
	public static ITraceConnInfo connect(ITraceConnClient client)
			throws TraceConnectivityException {
		TVAPIError error = TraceViewerAPI.connect();
		if (error == TVAPIError.NONE || error == TVAPIError.ALREADY_CONNECTED) {
			// Register must have after connect attempt because it triggers
			// notifications to already registered clients
			registerClient(client);
		} else {
			throw new TraceConnectivityException(error);
		}
		IConnection connectionWithCurrentID = ConnectionHelper
				.getConnectionWithCurrentID();
		return new TraceConnInfo(connectionWithCurrentID);
	}

	/**
	 * Connects using current settings defined in TraceViewer's preferences
	 * Client is disconnected and unregistered successfully for notifications in
	 * case no exceptions are thrown.
	 * 
	 * @param client
	 *            client requesting the connection.
	 * @throws TraceConnectivityException
	 */
	public static void disconnect(ITraceConnClient client)
			throws TraceConnectivityException {
		// Client is unregistered in any case and must be done before
		// disconnect() is called
		// because ínside disconnect() there is callback back to
		// hasRegisteredClients() method.
		boolean clientWasRegistered = unregisterClient(client);
		// Real disconnect only, if there are no more other clients using the
		// connection
		if (!hasRegisteredClients()) {
			TVAPIError error = TraceViewerAPI.disconnect();
			if (error != TVAPIError.NONE) {
				throw new TraceConnectivityException(error);
			}
		}
		// clientWasRegistered is true if the client was registered before
		// unregister attempt
		if (!clientWasRegistered) {
			// Reporting also possible failures that are possible errors in
			// client code
			throw new RuntimeException(
					TraceViewerAPI2.class.getSimpleName()
							+ ": " //$NON-NLS-1$ 
							+ Messages.TraceViewerAPI2Impl_UnregisterNotRegisteredClient_ErrMsg
							+ ": " + client); //$NON-NLS-1$ 
		}
	}

	/**
	 * Checks if there are any registered clients left.
	 * 
	 * @return <code>true</code> if there are registered clients, otherwise
	 *         <code>false</code>.
	 */
	public static boolean hasRegisteredClients() {
		synchronized (clients) {
			return !clients.isEmpty();
		}
	}

	/**
	 * Registers client.
	 * 
	 * @param client
	 *            client to register
	 */
	private static void registerClient(ITraceConnClient client) {
		synchronized (clients) {
			clients.add(client);
		}
	}

	/**
	 * Unregisters client.
	 * 
	 * @param client
	 *            client to unregister
	 * @return true if the previously registered client was unregistered
	 *         successfully
	 */
	private static boolean unregisterClient(ITraceConnClient client) {
		synchronized (clients) {
			// remove returns true if the set contained the specified element.
			return clients.remove(client);
		}
	}

	/**
	 * Notifies all client about disconnect event
	 * 
	 * @param currentConnection
	 *            currently selected connection
	 */
	public static void notifyConnection(IConnection currentConnection) {
		synchronized (clients) {
			for (Iterator<ITraceConnClient> iterator = clients.iterator(); iterator
					.hasNext();) {
				ITraceConnClient client = iterator.next();
				client.notifyConnection(new TraceConnInfo(currentConnection));
			}
		}
	}

	/**
	 * Notifies all client about disconnect event
	 */
	public static void notifyDisconnection() {
		synchronized (clients) {
			for (Iterator<ITraceConnClient> iterator = clients.iterator(); iterator
					.hasNext();) {
				ITraceConnClient client = iterator.next();
				client.notifyDisconnection();
			}
		}
	}

	/**
	 * Notifies listeners that connection preferences has been changed.
	 * 
	 * @param connectionInfo
	 *            Connection info about the new connection preferences.
	 */
	public static void notifyConnPrefsChanged(IConnection connectionInfo) {
		synchronized (clients) {
			for (Iterator<ITraceConnClient> iterator = clients.iterator(); iterator
					.hasNext();) {
				ITraceConnClient client = iterator.next();
				client
						.notifyConnPrefsChanged(new TraceConnInfo(
								connectionInfo));
			}
		}
	}

	/**
	 * Gets count of currently registered clients. This method is meant only for
	 * unit testing purposes.
	 * 
	 * @return count of currently registered clients
	 */
	public static int getRegisteredClientsCount() {
		synchronized (clients) {
			return clients.size();
		}
	}

}
