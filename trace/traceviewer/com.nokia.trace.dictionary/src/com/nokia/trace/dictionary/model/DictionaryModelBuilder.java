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
 * Dictionary Model Builder
 *
 */
package com.nokia.trace.dictionary.model;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;

import com.nokia.trace.dictionary.TraceDictionaryEngine;
import com.nokia.trace.dictionary.model.handlers.DictionaryHandler;
import com.nokia.trace.eventrouter.TraceEvent;

/**
 * Dictionary Model Builder
 */
public class DictionaryModelBuilder {

	/**
	 * Allowed extension for files
	 */
	private static final String ALLOWED_EXTENSION = ".xml"; //$NON-NLS-1$

	/**
	 * Category for events
	 */
	private final static String EVENT_CATEGORY = "Dictionary Model Builder"; //$NON-NLS-1$

	/**
	 * Sleeping time when waiting model to be ready
	 */
	private static final long WAIT_FOR_MODEL_READY_TIME = 5;

	/**
	 * Model to build
	 */
	private DictionaryDecodeModel model;

	/**
	 * Current file opening
	 */
	private static String currentFile;

	/**
	 * Metadata handlers
	 */
	private static ArrayList<DictionaryHandler> metadataHandlers = new ArrayList<DictionaryHandler>();

	/**
	 * Constructor
	 * 
	 * @param model
	 *            the trace model
	 */
	public DictionaryModelBuilder(DictionaryDecodeModel model) {
		this.model = model;
	}

	/**
	 * Opens decode file
	 * 
	 * @param filePath
	 *            file path. Must end with .xml to be loaded
	 * @param fileStream
	 *            file stream. If null, file stream is created by opening a file
	 *            from filePath variable
	 * @param createNew
	 *            true if creating new model, false if not
	 */
	public void openDecodeFile(String filePath, InputStream fileStream,
			boolean createNew) {
		// If there if parsing going on, wait for it to finish
		waitForPreviousParse();

		// Set current file and check the extension
		currentFile = filePath;
		if (!filePath.trim().toLowerCase().endsWith(ALLOWED_EXTENSION)) {
			postErrorMessage(Messages
					.getString("DictionaryModelBuilder.FileSkipped"), //$NON-NLS-1$
					filePath);
		} else {

			// If file stream doesn't exist, create if from the file
			if (fileStream == null) {
				fileStream = createFileStream(filePath);
			}

			// File stream must now exist or quit
			if (fileStream != null) {

				// Creating new model so erase old one first
				if (createNew) {
					model.clearModel();
				}

				// Model is not valid anymore
				model.setValid(false);

				// Create new thread for parsing
				DictionaryModelParserThread parseThread = new DictionaryModelParserThread(
						fileStream, filePath, model, metadataHandlers);

				// Start parsing
				parseThread.start();
			}
		}
	}

	/**
	 * Wait for previous parse
	 */
	private void waitForPreviousParse() {
		try {
			while (!model.isValid()) {

				// Model not valid, wait
				Thread.sleep(WAIT_FOR_MODEL_READY_TIME);
			}
		} catch (InterruptedException e) {
		}
	}

	/**
	 * Creates file stream from file path
	 * 
	 * @param filePath
	 *            file path
	 * @return file input stream
	 */
	private InputStream createFileStream(String filePath) {
		BufferedInputStream bin = null;
		try {
			FileInputStream fin = new FileInputStream(filePath);
			bin = new BufferedInputStream(fin);
		} catch (FileNotFoundException e) {
			postErrorMessage(e.toString(), filePath);
			model.setValid(true);
		}
		return bin;
	}

	/**
	 * Post error message
	 * 
	 * @param string
	 *            error message
	 * @param fileName
	 *            file name
	 */
	private void postErrorMessage(String string, String fileName) {
		TraceEvent event = new TraceEvent(TraceEvent.ERROR, string);
		event.setCategory(EVENT_CATEGORY);
		event.setSource(fileName);
		TraceDictionaryEngine.postEvent(event);
	}

	/**
	 * Gets the file path of the dictionary file that is currently being parsed
	 * 
	 * @return the file path of the dictionary file that is currently beging
	 *         parsed
	 */
	public static String getCurrentFile() {
		return currentFile;
	}

	/**
	 * Adds a metadata handler
	 * 
	 * @param metadataHandler
	 *            the handler to be added
	 */
	public static void addMetadataHandler(DictionaryHandler metadataHandler) {
		metadataHandlers.add(metadataHandler);
	}
}
