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
 * Log handler handles operations related to log files 
 *
 */
package com.nokia.traceviewer.api;

import java.io.File;

import com.nokia.traceviewer.action.CopyFileProgressCallback;
import com.nokia.traceviewer.action.LogSaveBinaryAction;
import com.nokia.traceviewer.action.TraceViewerActionUtils;
import com.nokia.traceviewer.api.TraceViewerAPI.TVAPIError;
import com.nokia.traceviewer.dialog.ProgressBarDialog;
import com.nokia.traceviewer.engine.TraceViewerGlobals;
import com.nokia.traceviewer.engine.TraceViewerDialogInterface.Dialog;

/**
 * Log handler handles operations related to log files
 * 
 */
final class LogHandler implements CopyFileProgressCallback {

	/**
	 * Ascii file extension
	 */
	private static final String ASCII_FILE_EXTENSION = ".txt"; //$NON-NLS-1$

	/**
	 * Divide long values with this to get integers
	 */
	private static final int LONG_DIVIDER = 10000;

	/**
	 * Progressbar
	 */
	private ProgressBarDialog progressBar;

	/**
	 * Callback for copy file
	 */
	private final CopyFileProgressCallback callback;

	/**
	 * Update count
	 */
	private int updateCount;

	/**
	 * Saving file boolean
	 */
	private boolean savingFile;

	/**
	 * Constructor
	 */
	LogHandler() {
		callback = this;
	}

	/**
	 * Saves current traces shown in TraceViewer view to a Binary log. If
	 * TraceViewer view is visible, a progress bar about the saving will be
	 * shown for the user. If TraceViewer view is not visible, saving the file
	 * will be done with the calling thread. Note that saving the file can take
	 * a long time!
	 * 
	 * @param filePath
	 *            file path where to save the log. If null and TraceViewer view
	 *            is visible, a file selection dialog is shown
	 * @return error code from TraceViewerAPI
	 */
	public TVAPIError saveCurrentTracesToLog(String filePath) {
		TVAPIError ret = TVAPIError.NONE;

		progressBar = null;

		// Both the file and the TV view are null, return error
		if (filePath == null
				&& TraceViewerGlobals.getTraceViewer().getView() == null) {

			ret = TVAPIError.TRACE_VIEW_NOT_OPEN;

			// Else, we can do something
		} else {

			// File is null but TV view is available, use Save Binary log action
			if (filePath == null
					&& TraceViewerGlobals.getTraceViewer().getView() != null) {

				TraceViewerGlobals.getTraceViewer().getView()
						.getActionFactory().getLogSaveBinaryAction().run();

				// File is given
			} else {

				// Get source file and target files
				String currentFile = TraceViewerGlobals.getTraceViewer()
						.getDataReaderAccess().getCurrentDataReader()
						.getFilePath();

				final File sourceFile = new File(currentFile);
				final File targetFile = new File(filePath);

				// If TV view is available, create a progressbar
				if (TraceViewerGlobals.getTraceViewer().getView() != null) {
					progressBar = (ProgressBarDialog) TraceViewerGlobals
							.getTraceViewer().getDialogs().createDialog(
									Dialog.PROGRESSBAR);

					// Let's set the real action to think we are saving the log
					// file to enable possibility for the user to cancel the
					// saving and ProgressBarCloseHandler will notice the
					// current action
					LogSaveBinaryAction saveAction = (LogSaveBinaryAction) TraceViewerGlobals
							.getTraceViewer().getView().getActionFactory()
							.getLogSaveBinaryAction();

					// Create new thread because progressbar takes UI thread
					new Thread() {

						/*
						 * (non-Javadoc)
						 * 
						 * @see java.lang.Thread#run()
						 */
						@Override
						public void run() {
							startCopying(sourceFile, targetFile);
						}

					}.start();

					if (progressBar != null) {
						String msg = Messages
								.getString("LogHandler.SavingLogMsg"); //$NON-NLS-1$
						savingFile = true;
						saveAction.setSavingFile(true);
						progressBar
								.open(
										(int) (sourceFile.length() / LONG_DIVIDER),
										msg);
						savingFile = false;
						saveAction.setSavingFile(false);
					}

					// View not available, use the current thread
				} else {
					savingFile = true;
					startCopying(sourceFile, targetFile);
					savingFile = false;
				}

			}

		}

		return ret;
	}

	/**
	 * Starts copying
	 * 
	 * @param sourceFile
	 *            source file
	 * @param targetFile
	 *            target file
	 */
	private void startCopying(final File sourceFile, final File targetFile) {

		try {
			// Start copying the file
			TraceViewerActionUtils.copyFile(sourceFile, targetFile, callback,
					TraceViewerGlobals.getTraceViewer().getDataReaderAccess()
							.getCurrentDataReader().getFileStartOffset());
		} catch (Exception e) {

			// Close the progressBar if still open
			if (progressBar != null) {
				TraceViewerGlobals.getTraceViewer().getView().closeProgressBar(
						progressBar);
			}
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
	 * @see
	 * com.nokia.traceviewer.action.CopyFileProgressCallback#copyingFinished()
	 */
	public void copyingFinished() {
		if (progressBar != null) {
			TraceViewerGlobals.getTraceViewer().getView().closeProgressBar(
					progressBar);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.nokia.traceviewer.action.CopyFileProgressCallback#notifyFilePosition
	 * (long)
	 */
	public void notifyFilePosition(long filePosition) {
		// Only update every third time to save time
		if (progressBar != null && updateCount++ % 3 == 0) {
			progressBar.updateProgressBar((int) (filePosition / LONG_DIVIDER));
		}
	}

	/**
	 * Opens log file to TraceViewer. If filePath is null and TraceViewer view
	 * is not open, error will be returned.
	 * 
	 * @param filePath
	 *            file path to open. If file path ends with .txt, file is opened
	 *            as ASCII log. If filePath is null and TraceViewer view is
	 *            open, a file selection dialog is opened.
	 * @return error code from TraceViewerAPI
	 */
	public TVAPIError openLogFile(String filePath) {
		TVAPIError ret = TVAPIError.NONE;

		// File path is not given
		if (filePath == null) {

			// If view exists, use open log file action
			if (TraceViewerGlobals.getTraceViewer().getView() != null) {
				TraceViewerGlobals.getTraceViewer().getView()
						.getActionFactory().getLogOpenLogAction().run();
			} else {
				ret = TVAPIError.TRACE_VIEW_NOT_OPEN;
			}

			// File path is given, open the log file
		} else {

			File file = new File(filePath);

			// File must exist
			if (file.exists()) {

				// Check if the file is ASCII log
				if (filePath.trim().endsWith(ASCII_FILE_EXTENSION)) {
					TraceViewerGlobals.getTraceViewer()
							.getDataProcessorAccess().getLogger().openLogFile(
									filePath, false);

					// Binary log
				} else {
					TraceViewerGlobals.getTraceViewer()
							.getDataProcessorAccess().getLogger().openLogFile(
									filePath, true);
				}

				// File doesn't exist
			} else {
				ret = TVAPIError.FILE_DOES_NOT_EXIST;
			}
		}
		return ret;
	}

}
