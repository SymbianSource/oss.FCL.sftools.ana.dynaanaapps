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
 * Handler for open log command
 *
 */
package com.nokia.traceviewer.action;

import java.io.File;
import java.net.URL;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.PlatformUI;

import com.nokia.traceviewer.TraceViewerHelpContextIDs;
import com.nokia.traceviewer.TraceViewerPlugin;
import com.nokia.traceviewer.engine.TraceProvider;
import com.nokia.traceviewer.engine.TraceViewerGlobals;
import com.nokia.traceviewer.engine.preferences.PreferenceConstants;

/**
 * Handler for open log command
 * 
 */
final class LogOpenLogAction extends TraceViewerAction {

	/**
	 * Filters in plain text open file dialog
	 */
	private static final String[] FILTER_NAMES = { Messages
			.getString("LogOpenLogAction.FilterNames") }; //$NON-NLS-1$

	/**
	 * Extension filters in plain text open file dialog
	 */
	private static final String[] FILTER_EXTS = { "*.bin;*.txt", "*.*" }; //$NON-NLS-1$ //$NON-NLS-2$

	/**
	 * Image for the action
	 */
	private static ImageDescriptor image;

	static {
		URL url = null;
		url = TraceViewerPlugin.getDefault().getBundle().getEntry(
				"/icons/logopen.gif"); //$NON-NLS-1$
		image = ImageDescriptor.createFromURL(url);
	}

	/**
	 * Constructor
	 */
	LogOpenLogAction() {
		setText(Messages.getString("LogOpenLogAction.Title")); //$NON-NLS-1$
		setToolTipText(Messages.getString("LogOpenLogAction.Tooltip")); //$NON-NLS-1$
		setImageDescriptor(image);
		setActionDefinitionId("com.nokia.traceviewer.command.open_log"); //$NON-NLS-1$

		// Set help
		PlatformUI.getWorkbench().getHelpSystem().setHelp(this,
				TraceViewerHelpContextIDs.LOGGING);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.traceviewer.action.TraceViewerAction#doRun()
	 */
	@Override
	protected void doRun() {
		TraceViewerGlobals.postUiEvent("LogOpenLogAction", "1"); //$NON-NLS-1$ //$NON-NLS-2$

		// Bring up a file selection dialog
		String[] files = TraceViewerActionUtils.openFileDialog(FILTER_NAMES,
				FILTER_EXTS, null, null, false, true);

		// Check if a file was selected
		if (files != null && files.length > 0) {
			String fileName = files[0];

			// Check if file exists or give an error
			File file = new File(fileName);
			if (file.exists()) {

				boolean binary = true;

				// Check extension
				if (file.getName().endsWith(".txt")) { //$NON-NLS-1$
					binary = false;
				} else {

					// Check if format seems to match
					checkIfDataFormatMatches(fileName);
				}

				// Tell the Logger to open the file
				TraceViewerGlobals.getTraceViewer().getDataProcessorAccess()
						.getLogger().openLogFile(fileName, binary);

				// Update view
				TraceViewerGlobals.getTraceViewer().getView().updateViewName();
			} else {
				String msg = Messages
						.getString("LogOpenLogAction.FileDoesntExist"); //$NON-NLS-1$
				TraceViewerGlobals.getTraceViewer().getDialogs()
						.showErrorMessage(msg);
			}
		}
		TraceViewerGlobals.postUiEvent("LogOpenLogAction", "0"); //$NON-NLS-1$ //$NON-NLS-2$
	}

	/**
	 * Checks if the data format matches and asks user to change it if not
	 * 
	 * @param fileName
	 *            file name
	 */
	private void checkIfDataFormatMatches(String fileName) {
		boolean dataMatches = TraceViewerGlobals.getTraceProvider()
				.checkIfFileFormatMatches(fileName);

		if (!dataMatches) {

			// Try with other TraceProviders
			for (int i = 0; i < TraceViewerGlobals.getListOfTraceProviders()
					.size(); i++) {
				TraceProvider provider = TraceViewerGlobals
						.getListOfTraceProviders().get(i);

				// Don't try with the one that is in use
				if (!provider.getName().equals(
						TraceViewerGlobals.getTraceProvider().getName())) {

					// Check if data format seems to match
					if (provider.checkIfFileFormatMatches(fileName)) {
						String msg1 = Messages
								.getString("LogOpenLogAction.ChangeDataFormatMsg1"); //$NON-NLS-1$
						String msg2 = Messages
								.getString("LogOpenLogAction.ChangeDataFormatMsg2"); //$NON-NLS-1$

						// Ask user if he wants to change data format
						boolean change = TraceViewerGlobals.getTraceViewer()
								.getDialogs().showConfirmationDialog(
										msg1 + provider.getName() + msg2);

						// Change data format
						if (change) {
							TraceViewerPlugin.getDefault().getPreferenceStore()
									.setValue(PreferenceConstants.DATA_FORMAT,
											provider.getName());
							TraceViewerGlobals
									.setTraceProvider(provider, false);
							break;
						}
					}
				}
			}
		}
	}
}
