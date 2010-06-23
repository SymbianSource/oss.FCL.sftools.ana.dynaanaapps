/*
* Copyright (c) 2007 Nokia Corporation and/or its subsidiary(-ies). 
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
* Async runnable which shows an message dialog to user
*
*/
package com.nokia.tracebuilder.view;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;

/**
 * Async runnable which shows an message dialog to user
 * 
 */
class MessageDialogRunnable implements Runnable {

	/**
	 * Dialog message
	 */
	private String message;

	/**
	 * Dialog shell
	 */
	private Shell shell;

	/**
	 * Creates a new message dialog runnable
	 * 
	 * @param shell
	 *            the dialog shell
	 * @param message
	 *            the message to be shown
	 */
	MessageDialogRunnable(Shell shell, String message) {
		this.shell = shell;
		this.message = message;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Runnable#run()
	 */
	public void run() {
		if (!shell.isDisposed()) {
			MessageDialog.openError(shell, Messages
					.getString("TraceView.TraceBuilder"), //$NON-NLS-1$
					message);
		}
	}

}
