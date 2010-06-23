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
 * Process ProgressBar updater class
 *
 */
package com.nokia.traceviewer.view;

import com.nokia.traceviewer.dialog.ProgressBarDialog;

/**
 * Process ProgressBar Updater
 */
public class ProcessProgressBarUpdater implements Runnable {

	/**
	 * Progressbar to be closed
	 */
	private final ProgressBarDialog dialog;

	/**
	 * Constructor
	 * 
	 * @param dialog
	 */
	ProcessProgressBarUpdater(ProgressBarDialog dialog) {
		this.dialog = dialog;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Runnable#run()
	 */
	public void run() {
		// Close the progress bar
		if (dialog != null && dialog.getShell() != null
				&& !dialog.getShell().isDisposed()) {
			dialog.close();
		}
	}
}
