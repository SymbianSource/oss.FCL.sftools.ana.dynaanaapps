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

import org.eclipse.osgi.util.NLS;

@SuppressWarnings("all")
public final class Messages extends NLS {
	private static final String BUNDLE_NAME = "com.nokia.traceviewer.internal.api.messages"; //$NON-NLS-1$
	public static String TraceConnInfo_UnknownConnection;
	public static String TraceConnInfo_UnknownConnectionRemote;
	public static String TraceViewerAPI2Impl_UnregisterNotRegisteredClient_ErrMsg;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
