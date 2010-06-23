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
 * Handler for close and restart logging command
 *
 */
package com.nokia.traceviewer.action;

import java.net.URL;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.widgets.Display;

import com.nokia.traceviewer.TraceViewerPlugin;
import com.nokia.traceviewer.engine.TraceViewerGlobals;
import com.nokia.traceviewer.engine.dataprocessor.Logger;

/**
 * Handler for close and restart logging command
 */
public final class CloseAndRestartLoggingAction extends TraceViewerAction {

	/**
	 * Close log image
	 */
	private static ImageDescriptor closeLogImage;

	/**
	 * Restart logging image
	 */
	private static ImageDescriptor restartLoggingImage;

	/**
	 * File path of the log file
	 */
	private String filePath;

	static {
		URL url = TraceViewerPlugin.getDefault().getBundle().getEntry(
				"/icons/logclose.gif"); //$NON-NLS-1$
		closeLogImage = ImageDescriptor.createFromURL(url);
		url = TraceViewerPlugin.getDefault().getBundle().getEntry(
				"/icons/log.gif"); //$NON-NLS-1$
		restartLoggingImage = ImageDescriptor.createFromURL(url);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.traceviewer.action.TraceViewerAction#doRun()
	 */
	@Override
	protected void doRun() {
		TraceViewerGlobals.postUiEvent("CloseAndRestartLoggingButton", "1"); //$NON-NLS-1$ //$NON-NLS-2$
		Logger logger = TraceViewerGlobals.getTraceViewer()
				.getDataProcessorAccess().getLogger();

		// If logging, close it
		if (logger.isPlainLogging()) {
			logger.stopPlainTextLogging();
			changeToRestartLoggingAction();

			// Start new machine readable logging
		} else {
			logger.startPlainTextLogging(filePath, false, true);
			changeToStopLoggingAction();
		}
		TraceViewerGlobals.postUiEvent("CloseAndRestartLoggingButton", "0"); //$NON-NLS-1$ //$NON-NLS-2$
	}

	/**
	 * Changes action to stop logging
	 */
	public void changeToStopLoggingAction() {
		if (Display.getCurrent() != null) {
			setImageDescriptor(closeLogImage);
			setText(Messages
					.getString("CloseAndRestartLoggingAction.CloseLogText")); //$NON-NLS-1$
			setToolTipText(Messages
					.getString("CloseAndRestartLoggingAction.CloseLogToolTip")); //$NON-NLS-1$
		} else {
			Display.getDefault().syncExec(new Runnable() {

				/*
				 * (non-Javadoc)
				 * 
				 * @see java.lang.Runnable#run()
				 */
				public void run() {
					setImageDescriptor(closeLogImage);
					setText(Messages
							.getString("CloseAndRestartLoggingAction.CloseLogText")); //$NON-NLS-1$
					setToolTipText(Messages
							.getString("CloseAndRestartLoggingAction.CloseLogToolTip")); //$NON-NLS-1$
				}

			});
		}
	}

	/**
	 * Changes action to restart logging
	 */
	public void changeToRestartLoggingAction() {
		if (Display.getCurrent() != null) {
			setImageDescriptor(restartLoggingImage);
			setText(Messages
					.getString("CloseAndRestartLoggingAction.RetakeLogText")); //$NON-NLS-1$
			setToolTipText(Messages
					.getString("CloseAndRestartLoggingAction.RetakeLogToolTip")); //$NON-NLS-1$
		} else {
			Display.getDefault().syncExec(new Runnable() {

				/*
				 * (non-Javadoc)
				 * 
				 * @see java.lang.Runnable#run()
				 */
				public void run() {
					setImageDescriptor(restartLoggingImage);
					setText(Messages
							.getString("CloseAndRestartLoggingAction.RetakeLogText")); //$NON-NLS-1$
					setToolTipText(Messages
							.getString("CloseAndRestartLoggingAction.RetakeLogToolTip"));//$NON-NLS-1$
				}

			});
		}
	}

	/**
	 * Sets file path
	 * 
	 * @param filePath
	 *            file path
	 */
	public void setFilePath(String filePath) {
		this.filePath = filePath;

	}
}
