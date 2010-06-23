/*
* Copyright (c) 2009 Nokia Corporation and/or its subsidiary(-ies).
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



package com.nokia.s60tools.traceanalyser.model;

import org.eclipse.ui.IStartup;

import com.nokia.s60tools.traceanalyser.plugin.TraceAnalyserPlugin;

/**
 * Startup handler is called when Carbide is started.
 * Engine is started when carbide starts.
 *
 */

public class StartupHandler implements IStartup {

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.IStartup#earlyStartup()
	 */
	public void earlyStartup() {
		TraceAnalyserPlugin.startEngine();
	}	

}
