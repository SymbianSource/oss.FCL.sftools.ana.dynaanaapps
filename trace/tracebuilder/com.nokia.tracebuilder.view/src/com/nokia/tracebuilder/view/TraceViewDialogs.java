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
* Dialog interface implementation
*
*/
package com.nokia.tracebuilder.view;

import java.io.File;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;

import com.nokia.tracebuilder.engine.TraceBuilderDialogs;

/**
 * Dialog interface implementation
 * 
 */
final class TraceViewDialogs implements TraceBuilderDialogs {

	/**
	 * Trace view
	 */
	private TraceView view;

	/**
	 * Constructor
	 * 
	 * @param view
	 *            trace view
	 */
	TraceViewDialogs(TraceView view) {
		this.view = view;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.engine.TraceBuilderDialogs#
	 *      showConfirmationQuery(com.nokia.tracebuilder.engine.TraceBuilderDialogs.QueryDialogParameters)
	 */
	public int showConfirmationQuery(QueryDialogParameters parameters) {
		Shell shell = view.getShell();
		int ret = CANCEL;
		if (shell != null) {
			if (parameters instanceof DirectoryDialogQueryParameters) {
				ret = showDirectoryQuery((DirectoryDialogQueryParameters) parameters);
			} else if (parameters instanceof FileDialogQueryParameters) {
				ret = showFileQuery((FileDialogQueryParameters) parameters);
			} else if (parameters instanceof ExtendedQueryParameters) {
				ret = showExtendedQueryDialog(parameters, shell);
			} else {
				if (MessageDialog.openQuestion(shell, Messages
						.getString("TraceView.TraceBuilder"), //$NON-NLS-1$
						TraceViewMessages.getConfirmationQueryText(parameters))) {
					ret = OK;
				}
			}
		}
		return ret;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.engine.TraceBuilderDialogs#
	 *      showErrorMessage(java.lang.String)
	 */
	public void showErrorMessage(String message) {
		Shell shell = view.getShell();
		if (shell != null) {
			shell.getDisplay().asyncExec(
					new MessageDialogRunnable(shell, message));
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.engine.TraceBuilderDialogs#
	 *      showCheckList(com.nokia.tracebuilder.engine.TraceBuilderDialogs.CheckListDialogType,
	 *      java.util.List, java.util.List)
	 */
	public int showCheckList(CheckListDialogParameters parameters) {
		Shell shell = view.getShell();
		int ret = CANCEL;
		if (shell != null) {
			CheckListSelectionDialog dlg = new CheckListSelectionDialog(shell,
					parameters);
			int result = dlg.open();
			if (result == IDialogConstants.OK_ID
					|| result == IDialogConstants.YES_ID) {
				ret = OK;
			}
		}
		return ret;
	}

	/**
	 * Shows a directory query
	 * 
	 * @param parameters
	 *            the query parameters
	 * @return OK / CANCEL
	 */
	private int showDirectoryQuery(DirectoryDialogQueryParameters parameters) {
		Shell shell = view.getShell();
		int ret = CANCEL;
		if (shell != null) {
			DirectoryDialog dd = new DirectoryDialog(shell);
			dd.setMessage(TraceViewMessages
					.getConfirmationQueryText(parameters));
			String res = dd.open();
			if (res != null) {
				parameters.path = res;
				ret = OK;
			}
		}
		return ret;
	}

	/**
	 * Shows file selection dialog
	 * 
	 * @param parameters
	 *            the parameters
	 * @return OK / CANCEL
	 */
	private int showFileQuery(FileDialogQueryParameters parameters) {
		Shell shell = view.getShell();
		int ret = CANCEL;
		if (shell != null) {
			FileDialog fd = new FileDialog(shell, SWT.SINGLE | SWT.OPEN);
			File f = new File(parameters.path);
			if (f.isDirectory()) {
				fd.setFilterPath(f.getPath());
			} else {
				fd.setFilterPath(f.getParent());
			}
			if (parameters.filters != null) {
				fd.setFilterNames(parameters.filterTitles);
				fd.setFilterExtensions(parameters.filters);
			}
			fd.setText(TraceViewMessages.getConfirmationQueryText(parameters));
			String res = fd.open();
			if (res != null) {
				parameters.path = res;
				ret = OK;
			}
		}
		return ret;
	}

	/**
	 * Shows a query dialog, where the buttons have been defined by client
	 * 
	 * @param parameters
	 *            the dialog parameters
	 * @param shell
	 *            the parent shell
	 * @return the index of the selected button
	 */
	private int showExtendedQueryDialog(QueryDialogParameters parameters,
			Shell shell) {
		MessageDialog md = new MessageDialog(
				shell,
				Messages.getString("TraceView.TraceBuilder"), //$NON-NLS-1$
				null, ((ExtendedQueryParameters) parameters).message,
				MessageDialog.QUESTION,
				((ExtendedQueryParameters) parameters).buttonTitles, 0);
		// Returns the index of the button defined in extended query parameters
		return md.open();
	}

}
