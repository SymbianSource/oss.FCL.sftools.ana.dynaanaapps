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
 * Search hit EOF updater class
 *
 */
package com.nokia.traceviewer.dialog;

import com.nokia.traceviewer.engine.TraceViewerGlobals;

/**
 * Handles search hitting EOF
 * 
 */
public class SearchHitEOFUpdater implements Runnable {

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Runnable#run()
	 */
	public void run() {
		// Show not found dialog to user and ask if continue
		String notFoundStr = Messages
				.getString("SearchHitEOFUpdater.TextNotFoundStr"); //$NON-NLS-1$
		boolean ret = TraceViewerGlobals.getTraceViewer().getDialogs()
				.showConfirmationDialog(notFoundStr);

		// Ok
		if (ret) {
			// Start new search from EOF
			TraceViewerGlobals.getTraceViewer().getDataProcessorAccess()
					.getSearchProcessor().getSearchDialog().endOfFile();
		} else {
			TraceViewerGlobals.getTraceViewer().getDataProcessorAccess()
					.getSearchProcessor().getSearchDialog()
					.enableSearchButton();
		}
	}

}
