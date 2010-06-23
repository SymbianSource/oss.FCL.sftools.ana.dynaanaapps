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
 * Handler for save binary log command
 *
 */
package com.nokia.traceviewer.action;

import java.io.File;
import java.net.URL;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.PlatformUI;

import com.nokia.traceviewer.TraceViewerHelpContextIDs;
import com.nokia.traceviewer.TraceViewerPlugin;
import com.nokia.traceviewer.dialog.ProgressBarDialog;
import com.nokia.traceviewer.engine.TraceViewerGlobals;
import com.nokia.traceviewer.engine.TraceViewerDialogInterface.Dialog;

/**
 * Handler for save binary log command
 * 
 */
public final class LogSaveBinaryAction extends TraceViewerAction implements
		CopyFileProgressCallback {

	/**
	 * Divide long values with this to get integers
	 */
	private static final int LONG_DIVIDER = 10000;

	/**
	 * Filters in save file dialog
	 */
	private static final String[] FILTER_NAMES = { Messages
			.getString("LogSaveBinaryAction.FilterNames") }; //$NON-NLS-1$

	/**
	 * Extension filters in save file dialog
	 */
	private static final String[] FILTER_EXTS = { "*.bin", "*.*" }; //$NON-NLS-1$ //$NON-NLS-2$

	/**
	 * Image for the action
	 */
	private static ImageDescriptor image;

	/**
	 * Progressbar
	 */
	private ProgressBarDialog progressBar;

	/**
	 * Callback for copy file
	 */
	private final CopyFileProgressCallback callback;

	/**
	 * Saving file boolean
	 */
	private boolean savingFile;

	/**
	 * Update count
	 */
	private int updateCount;

	static {
		URL url = TraceViewerPlugin.getDefault().getBundle().getEntry(
				"/icons/logsavebinary.gif"); //$NON-NLS-1$
		image = ImageDescriptor.createFromURL(url);
	}

	/**
	 * Constructor
	 */
	LogSaveBinaryAction() {
		setText(Messages.getString("LogSaveBinaryAction.Title")); //$NON-NLS-1$
		setToolTipText(Messages.getString("LogSaveBinaryAction.Tooltip")); //$NON-NLS-1$
		setImageDescriptor(image);
		setActionDefinitionId("com.nokia.traceviewer.command.save_binary_log"); //$NON-NLS-1$

		// Set help
		PlatformUI.getWorkbench().getHelpSystem().setHelp(this,
				TraceViewerHelpContextIDs.LOGGING);

		callback = this;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.traceviewer.action.TraceViewerAction#doRun()
	 */
	@Override
	protected void doRun() {
		TraceViewerGlobals.postUiEvent("LogSaveBinaryAction", "1"); //$NON-NLS-1$ //$NON-NLS-2$

		// Bring up a file save dialog
		String[] files = TraceViewerActionUtils.openFileDialog(FILTER_NAMES,
				FILTER_EXTS, null, null, false, false);

		// Check if a file name was given
		if (files != null && files.length > 0) {
			String fileName = files[0];
			final File targetFile = new File(fileName);
			final File sourceFile = getSourceFile();

			// Create progressbar
			updateCount = 0;
			progressBar = (ProgressBarDialog) TraceViewerGlobals
					.getTraceViewer().getDialogs().createDialog(
							Dialog.PROGRESSBAR);

			// Create new thread
			new Thread() {

				/*
				 * (non-Javadoc)
				 * 
				 * @see java.lang.Thread#run()
				 */
				@Override
				public void run() {
					try {
						// Start copying the file
						TraceViewerActionUtils.copyFile(sourceFile, targetFile,
								callback, TraceViewerGlobals.getTraceViewer()
										.getDataReaderAccess()
										.getCurrentDataReader()
										.getFileStartOffset());
					} catch (Exception e) {
						// Close the progressBar if still open
						TraceViewerGlobals.getTraceViewer().getView()
								.closeProgressBar(progressBar);
					}
				}

			}.start();

			savingFile = true;
			progressBar.open((int) (sourceFile.length() / LONG_DIVIDER),
					Messages.getString("LogSaveBinaryAction.SavingText")); //$NON-NLS-1$
			savingFile = false;

			// Export possible comments
			TraceViewerGlobals.getTraceViewer().getDataProcessorAccess()
					.getTraceCommentHandler().exportTraceComments(fileName);
		}

		TraceViewerGlobals.postUiEvent("LogSaveBinaryAction", "0"); //$NON-NLS-1$ //$NON-NLS-2$
	}

	/**
	 * Gets source file
	 * 
	 * @return source file to copy
	 */
	private File getSourceFile() {
		String currentFile = TraceViewerGlobals.getTraceViewer()
				.getDataReaderAccess().getCurrentDataReader().getFilePath();

		File file = new File(currentFile);
		return file;
	}

	/**
	 * Is saving file
	 * 
	 * @return true if saving file is going on
	 */
	public boolean isSavingFile() {
		return savingFile;
	}

	/**
	 * Set saving file
	 * 
	 * @param savingFile
	 *            new saving file status
	 */
	public void setSavingFile(boolean savingFile) {
		this.savingFile = savingFile;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.nokia.traceviewer.action.CopyFileProgressCallback#notifyFilePosition
	 * (long)
	 */
	public void notifyFilePosition(long filePosition) {

		// Only update every third time to get more speed
		if (progressBar != null && updateCount++ % 3 == 0) {
			progressBar.updateProgressBar((int) (filePosition / LONG_DIVIDER));
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.nokia.traceviewer.action.CopyFileProgressCallback#cancelCopying()
	 */
	public boolean cancelCopying() {
		return !savingFile;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.traceviewer.action.CopyFileProgressCallback#copyingReady()
	 */
	public void copyingFinished() {
		TraceViewerGlobals.getTraceViewer().getView().closeProgressBar(
				progressBar);
	}
}
