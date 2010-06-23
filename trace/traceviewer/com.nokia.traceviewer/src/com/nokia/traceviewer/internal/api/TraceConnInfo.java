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

import com.nokia.carbide.remoteconnections.interfaces.IConnection;
import com.nokia.carbide.remoteconnections.interfaces.IConnectionFactory;
import com.nokia.carbide.remoteconnections.interfaces.IConnectionType;
import com.nokia.traceviewer.api.ITraceConnInfo;
import com.nokia.traceviewer.api.TraceViewerAPI2;

/**
 * Connection information object that wraps <code>IConnection</code> enabling
 * only getters for accessing connection specific information. This information
 * object is used via <code>ITraceConnInfo</code> interface that is used by
 * <code>TraceViewerAPI2</code>.
 * 
 * @see IConnection
 * @see ITraceConnInfo
 * @see TraceViewerAPI2
 */
public class TraceConnInfo implements ITraceConnInfo {

	/**
	 * Connection that is wrapped behind read-only interface.
	 */
	private final IConnection connection;

	/**
	 * Constructor
	 * 
	 * @param connection
	 *            connection to get info from.
	 */
	public TraceConnInfo(IConnection connection) {
		this.connection = connection;
	}

	/**
	 * Returns a null connection type in case no connection types set.
	 * 
	 * @return null connection type in case no connection types set
	 */
	private IConnectionType getNullConnectionType() {
		return new IConnectionType() {
			/*
			 * (non-Javadoc)
			 * 
			 * @see
			 * com.nokia.carbide.remoteconnections.interfaces.IConnectionType
			 * #getIdentifier()
			 */
			public String getIdentifier() {
				return ""; //$NON-NLS-1$
			}

			/*
			 * (non-Javadoc)
			 * 
			 * @see
			 * com.nokia.carbide.remoteconnections.interfaces.IConnectionType
			 * #getHelpContext()
			 */
			public String getHelpContext() {
				return ""; //$NON-NLS-1$
			}

			/*
			 * (non-Javadoc)
			 * 
			 * @see
			 * com.nokia.carbide.remoteconnections.interfaces.IConnectionType
			 * #getDisplayName()
			 */
			public String getDisplayName() {
				return Messages.TraceConnInfo_UnknownConnection;
			}

			/*
			 * (non-Javadoc)
			 * 
			 * @see
			 * com.nokia.carbide.remoteconnections.interfaces.IConnectionType
			 * #getDescription()
			 */
			public String getDescription() {
				return Messages.TraceConnInfo_UnknownConnectionRemote;
			}

			/*
			 * (non-Javadoc)
			 * 
			 * @see
			 * com.nokia.carbide.remoteconnections.interfaces.IConnectionType
			 * #getConnectionFactory()
			 */
			public IConnectionFactory getConnectionFactory() {
				return null;
			}
		};
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.traceviewer.api.ITraceConnInfo#getConnectionType()
	 */
	public IConnectionType getConnectionType() {
		if (connection == null) {
			return getNullConnectionType();
		}
		return connection.getConnectionType();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.traceviewer.api.ITraceConnInfo#getDisplayName()
	 */
	public String getDisplayName() {
		if (connection == null) {
			return getNullConnectionType().getDisplayName();
		}
		return connection.getDisplayName();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.traceviewer.api.ITraceConnInfo#getIdentifier()
	 */
	public String getIdentifier() {
		if (connection == null) {
			return getNullConnectionType().getIdentifier();
		}
		return connection.getIdentifier();
	}

}
