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
 * Handler for close ascii log global command
 *
 */
package com.nokia.traceviewer.action;

import com.nokia.traceviewer.engine.TraceViewerGlobals;
import com.nokia.traceviewer.engine.dataprocessor.Logger;

/**
 * Handler for close ascii log global command
 */
public final class LogCloseAsciiGlobalAction extends TraceViewerGlobalAction {

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.traceviewer.action.TraceViewerGlobalAction#doRun()
	 */
	@Override
	protected void doRun() {
		// Get the logger DataProcessor
		Logger logger = TraceViewerGlobals.getTraceViewer()
				.getDataProcessorAccess().getLogger();

		boolean asciiLogging = logger.isPlainLogging();
		boolean logFileOpened = logger.isLogFileOpened();

		// ASCII logging must be on to be able to close it
		if (asciiLogging && !logFileOpened) {

			TraceViewerGlobals.getTraceViewer().getView().getActionFactory()
					.getLogCloseAsciiAction().run();
		}
	}

}
