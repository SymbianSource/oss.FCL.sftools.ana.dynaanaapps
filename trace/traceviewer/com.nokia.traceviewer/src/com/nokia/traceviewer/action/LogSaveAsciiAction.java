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
 * Handler for save ascii log command
 *
 */
package com.nokia.traceviewer.action;

import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.PlatformUI;

import com.nokia.traceviewer.TraceViewerHelpContextIDs;
import com.nokia.traceviewer.TraceViewerPlugin;
import com.nokia.traceviewer.dialog.ProgressBarDialog;
import com.nokia.traceviewer.engine.DataProcessorAccess;
import com.nokia.traceviewer.engine.TraceProperties;
import com.nokia.traceviewer.engine.TraceViewerGlobals;
import com.nokia.traceviewer.engine.TraceViewerDialogInterface.Dialog;

/**
 * Handler for save ascii log command
 */
public final class LogSaveAsciiAction extends TraceViewerAction {

	/**
	 * Number of traces to process once
	 */
	private static final int NUMBER_OF_TRACES_TO_PROCESS = 400;

	/**
	 * Filters in save file dialog
	 */
	private static final String[] FILTER_NAMES = { Messages
			.getString("LogSaveAsciiAction.FilterNames") }; //$NON-NLS-1$

	/**
	 * Extension filters in save file dialog
	 */
	private static final String[] FILTER_EXTS = { "*.txt", "*.*" }; //$NON-NLS-1$ //$NON-NLS-2$

	/**
	 * Image for the action
	 */
	private static ImageDescriptor image;

	/**
	 * Progressbar
	 */
	private ProgressBarDialog progressBar;

	/**
	 * Saving file boolean
	 */
	private boolean savingFile;

	/**
	 * Writer to be used for writing
	 */
	private PrintWriter plainOutput;

	/**
	 * End line character \n
	 */
	private static final char ENDLINE_N = '\n';

	/**
	 * Tabulator character
	 */
	private static final char TABULATOR = '\t';

	static {
		URL url = TraceViewerPlugin.getDefault().getBundle().getEntry(
				"/icons/logsaveascii.gif"); //$NON-NLS-1$
		image = ImageDescriptor.createFromURL(url);
	}

	/**
	 * Constructor
	 */
	LogSaveAsciiAction() {
		setText(Messages.getString("LogSaveAsciiAction.Title")); //$NON-NLS-1$
		setToolTipText(Messages.getString("LogSaveAsciiAction.Tooltip")); //$NON-NLS-1$
		setImageDescriptor(image);
		setActionDefinitionId("com.nokia.traceviewer.command.save_ascii_log"); //$NON-NLS-1$

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
		TraceViewerGlobals.postUiEvent("LogSaveAsciiAction", "1"); //$NON-NLS-1$ //$NON-NLS-2$

		// Bring up a file save dialog
		String[] files = TraceViewerActionUtils.openFileDialog(FILTER_NAMES,
				FILTER_EXTS, null, null, false, false);

		// Check if a file name was given
		if (files != null && files.length > 0) {
			String fileName = files[0];
			final int numberOfTraces = TraceViewerGlobals.getTraceViewer()
					.getDataReaderAccess().getMainDataReader().getTraceCount();

			// Create file writer
			boolean writerOk = buildPlainFileWriter(fileName);

			if (writerOk) {

				// Create progressbar
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
							int traceNumberToProcess = 0;

							// Get block of traces from the binary file
							while (traceNumberToProcess < numberOfTraces) {

								// From 0 to 399.
								List<TraceProperties> traces = TraceViewerGlobals
										.getTraceViewer()
										.getTraces(
												traceNumberToProcess,
												traceNumberToProcess
														+ NUMBER_OF_TRACES_TO_PROCESS
														- 1);
								Iterator<TraceProperties> i = traces.iterator();

								// Iterate through the block of traces
								while (i.hasNext()) {
									TraceProperties trace = i.next();
									String processedTrace = processTrace(trace);

									if (processedTrace != null) {
										plainOutput.write(processedTrace);
									}
								}

								// Increase the counter value and update
								// progressbar
								traceNumberToProcess += NUMBER_OF_TRACES_TO_PROCESS;
								notifyFilePosition(traceNumberToProcess);
							}

						} catch (Exception e) {
							e.printStackTrace();

							// Close the progressbar and the file writer
						} finally {
							TraceViewerGlobals.getTraceViewer().getView()
									.closeProgressBar(progressBar);
							plainOutput.flush();
							plainOutput.close();
						}
					}

				}.start();

				savingFile = true;
				progressBar.open(numberOfTraces, Messages
						.getString("LogSaveAsciiAction.SavingText")); //$NON-NLS-1$
				savingFile = false;

				// Export possible comments
				TraceViewerGlobals.getTraceViewer().getDataProcessorAccess()
						.getTraceCommentHandler().exportTraceComments(fileName);

			} else {
				System.out
						.println(Messages
								.getString("LogSaveAsciiAction.CannotCreateFileWriter")); //$NON-NLS-1$
			}
		}

		TraceViewerGlobals.postUiEvent("LogSaveAsciiAction", "0"); //$NON-NLS-1$ //$NON-NLS-2$
	}

	/**
	 * Processes trace
	 * 
	 * @param properties
	 *            trace properties
	 * @return the processed trace
	 */
	protected String processTrace(TraceProperties properties) {
		String ret = null;
		StringBuffer trace = null;

		DataProcessorAccess dpa = TraceViewerGlobals.getTraceViewer()
				.getDataProcessorAccess();

		// If binary trace, decode it
		if (properties.binaryTrace) {
			dpa.getDecoder().processData(properties);
		}

		// Null timestamp from first trace
		if (properties.traceNumber == 1) {
			TraceViewerGlobals.getTraceViewer().getDataProcessorAccess()
					.getTimestampParser().nullPreviousTimestamp();
		}

		// Parse timestamp
		dpa.getTimestampParser().processData(properties);

		// Normal ASCII trace log
		if (!properties.binaryTrace) {

			// No timestamp
			if (properties.timestampString == null) {
				int traceLen = properties.traceString.length() + 1;
				trace = new StringBuffer(traceLen);
				trace.append(properties.traceString);
				trace.append(ENDLINE_N);

				// With timestamp
			} else {
				StringBuffer timeFromPreviousSB = TraceViewerGlobals
						.getTraceViewer().getDataProcessorAccess()
						.getTimestampParser().getTimeFromPreviousString(
								properties.timeFromPreviousTrace);
				int traceLen = properties.timestampString.length() + 1
						+ timeFromPreviousSB.length()
						+ properties.traceString.length() + 1;
				trace = new StringBuffer(traceLen);
				trace.append(properties.timestampString);
				trace.append(timeFromPreviousSB);
				trace.append(TABULATOR);
				trace.append(properties.traceString);
				trace.append(ENDLINE_N);
			}
			ret = trace.toString();
		}
		return ret;
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
	 * Update progress bar with file position
	 * 
	 * @param traceNumber
	 *            trace number
	 */
	public void notifyFilePosition(int traceNumber) {

		// Only update every third time to get more speed
		if (progressBar != null) {
			progressBar.updateProgressBar(traceNumber);
		}
	}

	/**
	 * Builds the plain file writer
	 * 
	 * @param filePath
	 *            file path to save the log
	 * @return status of building plain text file
	 */
	public boolean buildPlainFileWriter(String filePath) {
		boolean success = false;

		try {
			plainOutput = new PrintWriter(new FileWriter(filePath));
			success = true;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return success;
	}
}
