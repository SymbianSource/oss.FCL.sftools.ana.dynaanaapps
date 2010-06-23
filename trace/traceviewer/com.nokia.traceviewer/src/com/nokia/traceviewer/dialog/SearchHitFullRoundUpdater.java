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
 * Search hit full round updater class
 *
 */
package com.nokia.traceviewer.dialog;

import com.nokia.traceviewer.engine.TraceViewerGlobals;

/**
 * Handles search hitting full round
 */
public class SearchHitFullRoundUpdater implements Runnable {

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Runnable#run()
	 */
	public void run() {
		// Show text not found dialog to the user
		String textNotFound = Messages
				.getString("SearchHitFullRoundUpdater.TextNotFound"); //$NON-NLS-1$
		TraceViewerGlobals.getTraceViewer().getDialogs()
				.showInformationMessage(textNotFound);

		// Enable search button
		TraceViewerGlobals.getTraceViewer().getDataProcessorAccess()
				.getSearchProcessor().getSearchDialog().enableSearchButton();
	}

}
