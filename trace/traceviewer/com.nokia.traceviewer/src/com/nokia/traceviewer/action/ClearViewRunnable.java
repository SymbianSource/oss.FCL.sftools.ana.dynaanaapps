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
 * Executes clear view
 *
 */
package com.nokia.traceviewer.action;

import com.nokia.traceviewer.engine.TraceViewerGlobals;

/**
 * Executes clear view
 * 
 */
public class ClearViewRunnable implements Runnable {

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Runnable#run()
	 */
	public void run() {

		// Pause connection and datareader
		TraceViewerGlobals.getTraceViewer().getDataReaderAccess()
				.getCurrentDataReader().shutdown();

		boolean logFileOpened = TraceViewerGlobals.getTraceViewer()
				.getDataProcessorAccess().getLogger().isLogFileOpened();

		// Clear everything
		TraceViewerGlobals.getTraceViewer().clearAllData();

		// Create new main reader
		if (!logFileOpened) {
			TraceViewerGlobals.getTraceViewer().getDataReaderAccess()
					.createMainDataReader();
		}

		// Set pause image to pause action
		((PauseAction) TraceViewerGlobals.getTraceViewer().getView()
				.getActionFactory().getPauseAction()).setPauseImage(true);

		// Update view name
		TraceViewerGlobals.getTraceViewer().getView().updateViewName();
	}
}
