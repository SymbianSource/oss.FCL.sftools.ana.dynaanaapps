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
 * Handler for new binary log command
 *
 */
package com.nokia.traceviewer.action;

import java.net.URL;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.PlatformUI;

import com.nokia.traceviewer.TraceViewerHelpContextIDs;
import com.nokia.traceviewer.TraceViewerPlugin;
import com.nokia.traceviewer.engine.TraceViewerGlobals;

/**
 * Handler for new binary log command
 * 
 */
final class LogNewBinaryAction extends TraceViewerAction {

	/**
	 * Filters in save file dialog
	 */
	private static final String[] FILTER_NAMES = { Messages
			.getString("LogNewBinaryAction.FilterNames") }; //$NON-NLS-1$

	/**
	 * Extension filters in save file dialog
	 */
	private static final String[] FILTER_EXTS = { "*.bin", "*.*" }; //$NON-NLS-1$ //$NON-NLS-2$

	/**
	 * Image for the action
	 */
	private static ImageDescriptor image;

	static {
		URL url = TraceViewerPlugin.getDefault().getBundle().getEntry(
				"/icons/log.gif"); //$NON-NLS-1$
		image = ImageDescriptor.createFromURL(url);
	}

	/**
	 * Constructor
	 */
	LogNewBinaryAction() {
		setText(Messages.getString("LogNewBinaryAction.Title")); //$NON-NLS-1$
		setToolTipText(Messages.getString("LogNewBinaryAction.Tooltip")); //$NON-NLS-1$
		setImageDescriptor(image);
		setActionDefinitionId("com.nokia.traceviewer.command.new_binary_log"); //$NON-NLS-1$

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
		TraceViewerGlobals.postUiEvent("LogNewBinaryAction", "1"); //$NON-NLS-1$ //$NON-NLS-2$

		// Bring up a file save dialog
		String[] files = TraceViewerActionUtils.openFileDialog(FILTER_NAMES,
				FILTER_EXTS, null, null, false, false);

		// Check if a file name was given
		if (files != null && files.length > 0) {
			String fileName = files[0];

			// Start logging
			TraceViewerGlobals.getTraceViewer().getDataProcessorAccess()
					.getLogger().startBinaryLogging(fileName);
		}

		TraceViewerGlobals.postUiEvent("LogNewBinaryAction", "0"); //$NON-NLS-1$ //$NON-NLS-2$
	}

}
