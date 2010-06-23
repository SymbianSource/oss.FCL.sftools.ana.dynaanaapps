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
 * Handler for open decode file command
 *
 */
package com.nokia.traceviewer.action;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.PlatformUI;

import com.nokia.traceviewer.TraceViewerHelpContextIDs;
import com.nokia.traceviewer.TraceViewerPlugin;
import com.nokia.traceviewer.dialog.ProgressBarDialog;
import com.nokia.traceviewer.engine.TraceViewerGlobals;
import com.nokia.traceviewer.engine.TraceViewerUtils;
import com.nokia.traceviewer.engine.TraceViewerDialogInterface.Dialog;

/**
 * Handler for open decode file command
 * 
 */
public final class OpenDecodeFileAction extends TraceViewerAction {

	/**
	 * Maximum number of file paths to show in open dialog
	 */
	private static final int MAX_PATHS_IN_DIALOG = 10;

	/**
	 * Spaces to indent text in dialog
	 */
	private static final String SPACING = "    "; //$NON-NLS-1$

	/**
	 * Filter names in open file dialog
	 */
	static final String[] FILTER_NAMES = { Messages
			.getString("OpenDecodeFileAction.FilterNamesOST") }; //$NON-NLS-1$

	/**
	 * Filter extensions in open file dialog
	 */
	static final String[] FILTER_EXTS = { "*.xml;*.zip", "*.*" }; //$NON-NLS-1$ //$NON-NLS-2$

	/**
	 * Progress Bar
	 */
	private ProgressBarDialog progressBarDialog;

	/**
	 * Dictionary files to open in startup. Filled when reading general settings
	 * file when starting TraceViewer.
	 */
	private final List<String> openInStartupFiles = new ArrayList<String>();

	/**
	 * Process reason when building decode model
	 */
	private final String processReasonBuild = Messages
			.getString("OpenDecodeFileAction.BuildingModelString"); //$NON-NLS-1$

	/**
	 * Process reason when decoding traces
	 */
	private final String processReasonDecode = Messages
			.getString("OpenDecodeFileAction.DecodingTracesString"); //$NON-NLS-1$

	/**
	 * Tells that we are already decoding traces
	 */
	private boolean decodingTraces;

	/**
	 * Image for this action
	 */
	private static ImageDescriptor image;
	static {
		URL url = null;
		url = TraceViewerPlugin.getDefault().getBundle().getEntry(
				"/icons/opendecodefile.gif"); //$NON-NLS-1$
		image = ImageDescriptor.createFromURL(url);
	}

	/**
	 * Constructor
	 */
	OpenDecodeFileAction() {
		setText(Messages.getString("OpenDecodeFileAction.Title")); //$NON-NLS-1$
		setToolTipText(Messages.getString("OpenDecodeFileAction.Tooltip")); //$NON-NLS-1$
		setImageDescriptor(image);

		// Set help
		PlatformUI.getWorkbench().getHelpSystem().setHelp(this,
				TraceViewerHelpContextIDs.ACTIONS);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.traceviewer.action.TraceViewerAction#doRun()
	 */
	@Override
	protected void doRun() {
		TraceViewerGlobals.postUiEvent("OpenDecodeFileButton", "1"); //$NON-NLS-1$ //$NON-NLS-2$

		// DecodeProvider must be available
		if (TraceViewerGlobals.getDecodeProvider() != null) {

			// Pause the datareader if it's not paused already
			boolean wasPausedWhenEntered = TraceViewerGlobals.getTraceViewer()
					.getDataReaderAccess().getMainDataReader().isPaused();

			if (!wasPausedWhenEntered) {
				TraceViewerGlobals.getTraceViewer().getView()
						.getActionFactory().getPauseAction().run();
			}

			// Get files
			String[] files = TraceViewerActionUtils.openFileDialog(
					FILTER_NAMES, FILTER_EXTS, null, null, true, true);

			// Load files to model
			loadFilesToModel(files, true);

			// Unpause
			if (!wasPausedWhenEntered) {
				TraceViewerGlobals.getTraceViewer().getView()
						.getActionFactory().getPauseAction().run();
			}

		}
		TraceViewerGlobals.postUiEvent("OpenDecodeFileButton", "0"); //$NON-NLS-1$ //$NON-NLS-2$
	}

	/**
	 * Loads array of files to model
	 * 
	 * @param files
	 *            array of files
	 * @param createNewModel
	 *            true if we first erase possible old model
	 */
	public void loadFilesToModel(String[] files, boolean createNewModel) {
		if (files != null) {

			// Create progressbar
			progressBarDialog = (ProgressBarDialog) TraceViewerGlobals
					.getTraceViewer().getDialogs().createDialog(
							Dialog.PROGRESSBAR);

			// Create opener thread
			OpenDecodeFileThread openerThread = new OpenDecodeFileThread(files,
					createNewModel, progressBarDialog);

			// Get number of decode files inside given files array. This is
			// different from the amount of files in files array if some of the
			// files are ZIP files.
			int numberOfFiles = openerThread.calculateNumberOfFiles();

			// Start thread
			openerThread.start();

			// Open progress bar. This thread will stop here to wait for
			// progress bar to be closed. Closing is done in the opener thread.
			progressBarDialog.open(numberOfFiles, processReasonBuild);

			// Decode traces after files are loaded
			processDecodingTraces();

			// Set new files to be watched in case of changes
			ReloadDecodeFilesAction reloadDecodeFilesAction = (ReloadDecodeFilesAction) TraceViewerGlobals
					.getTraceViewer().getView().getActionFactory()
					.getReloadDecodeFilesAction();
			reloadDecodeFilesAction.updateFilesToBeWatched();
		}
	}

	/**
	 * Process decoding traces
	 */
	private void processDecodingTraces() {
		decodingTraces = true;

		// If data reader exists
		if (TraceViewerGlobals.getTraceViewer().getDataReaderAccess()
				.getMainDataReader() != null
				&& TraceViewerGlobals.getTraceViewer().getDataReaderAccess()
						.getMainDataReader().getTraceCount() > 0) {

			// Check if the read from start is needed
			if (TraceViewerUtils.isReadingFromStartNeeded()) {
				readTraceFileFromTheBeginning();

				// Else just refresh the current view
			} else {
				TraceViewerGlobals.getTraceViewer().getView()
						.refreshCurrentView();
			}

		}

		decodingTraces = false;
	}

	/**
	 * Reads the trace file from the beginning
	 */
	private void readTraceFileFromTheBeginning() {

		// Get line count before clearing views
		int lineCount = TraceViewerGlobals.getTraceViewer()
				.getDataReaderAccess().getMainDataReader().getTraceCount();

		// Get offset where to start showing traces again
		int startShowingTraceOffset = TraceViewerGlobals.getTraceViewer()
				.getView().getShowingTracesFrom()
				- TraceViewerGlobals.blockSize + 1;
		if (startShowingTraceOffset < 0) {
			startShowingTraceOffset = 0;
		}

		// Set traces not to be shown in the view unless we are
		// filtering
		if (!TraceViewerGlobals.getTraceViewer().getDataProcessorAccess()
				.getFilterProcessor().isFiltering()) {
			TraceViewerGlobals.getTraceViewer().getDataReaderAccess()
					.getMainDataReader().getTraceConfiguration().setShowInView(
							false);

			// Set point to decoder where to start showing traces again
			TraceViewerGlobals.getTraceViewer().getDataProcessorAccess()
					.getDecoder().setStartShowingTracesAgainOffset(
							startShowingTraceOffset);
		}

		// Re-read all the data from the trace file
		TraceViewerGlobals.getTraceViewer().readDataFileFromBeginning();

		// Open progress bar
		progressBarDialog = (ProgressBarDialog) TraceViewerGlobals
				.getTraceViewer().getDialogs().createDialog(Dialog.PROGRESSBAR);
		progressBarDialog.open(lineCount, processReasonDecode);
	}

	/**
	 * Tells if opening decode file
	 * 
	 * @return true if opening decode file
	 */
	public boolean isOpeningDecodeFile() {
		return (progressBarDialog != null
				&& progressBarDialog.getShell() != null
				&& !progressBarDialog.getShell().isDisposed() && !decodingTraces);
	}

	/**
	 * Tells if decoding traces
	 * 
	 * @return true if decoding traces
	 */
	public boolean isDecodingTraces() {
		return (progressBarDialog != null
				&& progressBarDialog.getShell() != null
				&& !progressBarDialog.getShell().isDisposed() && decodingTraces);
	}

	/**
	 * Get progressBar
	 * 
	 * @return the progressBar
	 */
	public ProgressBarDialog getProgressBarDialog() {
		return progressBarDialog;
	}

	/**
	 * Adds open in startup Dictionary
	 * 
	 * @param dictionary
	 *            new Dictionary
	 */
	public void addOpenInStartupDictionary(String dictionary) {
		if (!openInStartupFiles.contains(dictionary)) {
			openInStartupFiles.add(dictionary);
		}
	}

	/**
	 * Gets startup files
	 * 
	 * @return startup files list
	 */
	public List<String> getStartupFiles() {
		return openInStartupFiles;
	}

	/**
	 * Opens previously opened decode files
	 */
	public void openPreviousDecodeFiles() {
		if (!openInStartupFiles.isEmpty()) {
			PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable() {
				public void run() {

					// Construct info message
					StringBuilder infoStringSB = new StringBuilder();
					infoStringSB
							.append(Messages
									.getString("OpenDecodeFileAction.OpenPreviousFilesString")); //$NON-NLS-1$
					infoStringSB.append('\n');
					infoStringSB.append('\n');

					// Add decode files
					if (!openInStartupFiles.isEmpty()) {
						int i;
						for (i = 0; i < openInStartupFiles.size()
								&& i < MAX_PATHS_IN_DIALOG; i++) {
							IPath path = new Path(openInStartupFiles.get(i));
							infoStringSB.append(SPACING);
							infoStringSB.append(path.lastSegment());
							infoStringSB.append('\n');
						}

						if (i == MAX_PATHS_IN_DIALOG) {
							int more = openInStartupFiles.size() - i;
							String files = Messages
									.getString("OpenDecodeFileAction.Files"); //$NON-NLS-1$
							infoStringSB.append(SPACING);
							infoStringSB.append("+ "); //$NON-NLS-1$
							infoStringSB.append(more);
							infoStringSB.append(files);
							infoStringSB.append('\n');
						}
					}

					boolean answer = TraceViewerGlobals.getTraceViewer()
							.getDialogs().showConfirmationDialog(
									infoStringSB.toString());

					if (answer) {
						TraceViewerGlobals.postUiEvent(
								"OpenPreviousDecodeFilesYes", "1"); //$NON-NLS-1$ //$NON-NLS-2$

						// Create string array from arraylist and fill it
						String[] files = new String[openInStartupFiles.size()];
						for (int i = 0; i < openInStartupFiles.size(); i++) {
							files[i] = openInStartupFiles.get(i);
						}
						openInStartupFiles.clear();

						// Load files to new model
						loadFilesToModel(files, true);
						TraceViewerGlobals.postUiEvent(
								"OpenPreviousDecodeFilesYes", "0"); //$NON-NLS-1$ //$NON-NLS-2$
					} else {
						TraceViewerGlobals.postUiEvent(
								"OpenPreviousDecodeFilesNo", "1"); //$NON-NLS-1$ //$NON-NLS-2$

						openInStartupFiles.clear();

						TraceViewerGlobals.postUiEvent(
								"OpenPreviousDecodeFilesNo", "0"); //$NON-NLS-1$ //$NON-NLS-2$
					}
				}

			});
		}
	}
}
