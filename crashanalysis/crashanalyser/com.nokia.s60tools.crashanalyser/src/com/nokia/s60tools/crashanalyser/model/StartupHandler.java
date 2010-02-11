/*
* Copyright (c) 2008 Nokia Corporation and/or its subsidiary(-ies). 
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

package com.nokia.s60tools.crashanalyser.model;

import org.eclipse.ui.IStartup;

import com.nokia.s60tools.crashanalyser.plugin.CrashAnalyserPlugin;

/**
 * Startup handler is called when Carbide is started. We need to start
 * emulator and/or trace listeners according to preferences. 
 *
 */
public class StartupHandler implements IStartup {

	public void earlyStartup() {
		CrashAnalyserPlugin.startTraceListener();
		CrashAnalyserPlugin.startEmulatorListener();
	}	

}
