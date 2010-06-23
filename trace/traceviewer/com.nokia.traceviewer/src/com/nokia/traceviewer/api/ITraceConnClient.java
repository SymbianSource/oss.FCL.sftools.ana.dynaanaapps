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

package com.nokia.traceviewer.api;

/**
 * This is interface that each client has to implement in order to get
 * notifications about connection status changes
 */
public interface ITraceConnClient {

	/**
	 * Registered connection is re-established after connection break.
	 * 
	 * @param connectionInfo
	 *            connection info for the newly connected connection
	 */
	void notifyConnection(ITraceConnInfo connectionInfo);

	/**
	 * Registered connection is disconnected.
	 */
	void notifyDisconnection();

	/**
	 * Connection preferences has been changed and the new settings will be
	 * taken into use when trace connection is restarted next time i.e. in next
	 * <code>notifyConnection()</code>. Interface implementor can already notify
	 * user in this point if connection preferences are not acceptable ones.
	 * 
	 * @param connectionInfo
	 *            connection info for the newly set connection preference
	 */
	void notifyConnPrefsChanged(ITraceConnInfo connectionInfo);

}
