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
 * Thread for opening decode files
 *
 */
package com.nokia.traceviewer.action;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import com.nokia.traceviewer.dialog.ProgressBarDialog;
import com.nokia.traceviewer.engine.DecodeProvider;
import com.nokia.traceviewer.engine.TraceViewerGlobals;

/**
 * Thread for opening decode files
 */
public class OpenDecodeFileThread extends Thread {

	/**
	 * Model valid waiting time
	 */
	private static final long MODEL_VALID_WAITING_TIME = 100;

	/**
	 * Model valid maximum waiting time
	 */
	private static final long MODEL_VALID_MAX_WAITING_TIME = 5000;

	/**
	 * Progress Bar
	 */
	private final ProgressBarDialog progressBarDialog;

	/**
	 * Create new model or append
	 */
	private final boolean createNew;

	/**
	 * Files to open
	 */
	private final String[] files;

	/**
	 * Number of files already processed
	 */
	private int filesProcessed;

	/**
	 * Number of total files to be processed
	 */
	private int totalFiles;

	/**
	 * Constructor
	 * 
	 * @param files
	 *            array of files to open
	 * @param createNew
	 *            indicates if creating new model or appending
	 * @param progressBarDialog
	 *            progressbar to update
	 */
	public OpenDecodeFileThread(String[] files, boolean createNew,
			ProgressBarDialog progressBarDialog) {
		this.files = files;
		this.createNew = createNew;
		this.progressBarDialog = progressBarDialog;
		totalFiles = files.length;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Thread#run()
	 */
	@Override
	public void run() {

		// Process opening main decode file, create new model
		processOpeningDecodeFile(files[0], createNew);

		// Process remaining decode files as append files
		for (int i = 1; i < files.length; i++) {

			// Start processing
			processOpeningDecodeFile(files[i], false);
		}
	}

	/**
	 * Opens decode file
	 * 
	 * @param filePath
	 *            decode file path
	 * @param createNew
	 *            if true, delete old model before appending the new decode file
	 */
	private void processOpeningDecodeFile(String filePath, boolean createNew) {
		DecodeProvider decoder = TraceViewerGlobals.getDecodeProvider();
		if (decoder != null) {
			boolean createNewModel = createNew;

			// Check if the file is a ZIP file
			File file = new File(filePath);

			// Check if the file is a ZIP file
			try {
				ZipFile zipFile = new ZipFile(file);
				Enumeration<? extends ZipEntry> entries = zipFile.entries();

				totalFiles += zipFile.size() - 1;
				progressBarDialog.setMax(totalFiles);

				// Add ZIP file to list of opened startup files
				((OpenDecodeFileAction) TraceViewerGlobals.getTraceViewer()
						.getView().getActionFactory().getOpenDecodeFileAction())
						.addOpenInStartupDictionary(filePath);

				// Loop through elements
				while (entries.hasMoreElements()) {
					processZipEntry(createNewModel, decoder, zipFile, entries);
					createNewModel = false;
				}

			} catch (IOException e) {

				// Not a ZIP file, open as normal decode file
				decoder.openDecodeFile(filePath, null, createNewModel);

				filesProcessed++;
				updateProgressBar();
			}
		}
	}

	/**
	 * Processes ZIP entry
	 * 
	 * @param createNew
	 *            if true, delete old model before appending the new decode file
	 * @param decoder
	 *            decode provider
	 * @param zipFile
	 *            ZIP file
	 * @param entries
	 *            entries enumeration
	 * @throws IOException
	 */
	private void processZipEntry(boolean createNew, DecodeProvider decoder,
			ZipFile zipFile, Enumeration<? extends ZipEntry> entries)
			throws IOException {
		ZipEntry entry = entries.nextElement();

		if (!entry.isDirectory()) {
			InputStream inputStream = zipFile.getInputStream(entry);

			// Open the decode file entry from the ZIP
			decoder.openDecodeFile(entry.getName(), inputStream, createNew);

			filesProcessed++;

			// Directories decrease the amount of total files
		} else {
			totalFiles--;
			progressBarDialog.setMax(totalFiles);
		}

		updateProgressBar();
	}

	/**
	 * Updates or closes progress bar
	 */
	private void updateProgressBar() {
		// All files are processed, close progress bar but only after the model
		// is valid
		if (filesProcessed == totalFiles) {

			// Wait until model is ready and loaded or maximum of five
			// seconds
			long startTime = System.currentTimeMillis();
			while (!TraceViewerGlobals.getDecodeProvider()
					.isModelLoadedAndValid()
					&& startTime + MODEL_VALID_MAX_WAITING_TIME > System
							.currentTimeMillis()) {
				try {
					Thread.sleep(MODEL_VALID_WAITING_TIME);
				} catch (InterruptedException e) {
				}
			}

			// Close the progress bar
			TraceViewerGlobals.getTraceViewer().getView().closeProgressBar(
					progressBarDialog);
		} else {
			progressBarDialog.updateProgressBar(filesProcessed);
		}
	}

	/**
	 * Calculates number of files inside given files
	 * 
	 * @return number of files
	 */
	public int calculateNumberOfFiles() {
		int number = 0;

		for (int i = 0; i < files.length; i++) {

			// Check if the file is a ZIP file
			File file = new File(files[i]);

			// Check if the file is a ZIP file
			try {
				ZipFile zipFile = new ZipFile(file);
				number += zipFile.size() - 1;

			} catch (IOException e) {
				number++;
			}
		}

		return number;
	}
}
