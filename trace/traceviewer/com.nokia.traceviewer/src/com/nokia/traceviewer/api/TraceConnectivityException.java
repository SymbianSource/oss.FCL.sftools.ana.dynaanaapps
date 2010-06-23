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

import com.nokia.traceviewer.api.TraceViewerAPI.TVAPIError;
import com.nokia.traceviewer.internal.api.TraceViewerAPI2Impl;

/**
 * Exception that is thrown when <code>TraceViewerAPI2</code> connectivity
 * operations fail.
 * 
 * @see TraceViewerAPI2Impl
 */
public class TraceConnectivityException extends Exception {

	/**
	 * Serial version UID.
	 */
	private static final long serialVersionUID = 6945640644336014817L;
	/**
	 * Trace viewer error due to which connect failed.
	 */
	private final TVAPIError error;

	/**
	 * Constructor
	 * 
	 * @param error
	 *            trace viewer error due to which connect failed
	 */
	public TraceConnectivityException(TVAPIError error) {
		this.error = error;
	}

	/**
	 * Gets trace viewer error due to which connect failed.
	 * 
	 * @return trace viewer error due to which connect failed
	 */
	public TVAPIError getError() {
		return error;
	}

}
