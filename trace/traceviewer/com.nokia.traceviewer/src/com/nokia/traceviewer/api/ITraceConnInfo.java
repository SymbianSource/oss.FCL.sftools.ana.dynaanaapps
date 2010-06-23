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

import com.nokia.carbide.remoteconnections.interfaces.IConnectionType;

/**
 * Connection info object for client getting information about the user
 * connection.
 */
public interface ITraceConnInfo {

	/**
	 * Gets connection type.
	 * 
	 * @return connection type
	 */
	public IConnectionType getConnectionType();

	/**
	 * Gets display name.
	 * 
	 * @return display name
	 */
	public String getDisplayName();

	/**
	 * Gets connection identifier.
	 * 
	 * @return connection identifier
	 */
	public String getIdentifier();

}