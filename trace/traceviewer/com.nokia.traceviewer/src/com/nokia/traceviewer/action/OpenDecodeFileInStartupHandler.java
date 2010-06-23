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
 * Handles opening previous decode files in startup of TraceViewer
 *
 */
package com.nokia.traceviewer.action;

import com.nokia.traceviewer.engine.TraceViewerGlobals;

/**
 * Handles opening previous decode files in startup of TraceViewer
 * 
 */
public class OpenDecodeFileInStartupHandler extends Thread {

	/**
	 * Run already
	 */
	private static boolean runAlready;

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Thread#run()
	 */
	@Override
	public void run() {
		if (!runAlready) {
			runAlready = true;

			OpenDecodeFileAction openDecodeFileAction = (OpenDecodeFileAction) TraceViewerGlobals
					.getTraceViewer().getView().getActionFactory()
					.getOpenDecodeFileAction();

			openDecodeFileAction.openPreviousDecodeFiles();
		}
	}
}
