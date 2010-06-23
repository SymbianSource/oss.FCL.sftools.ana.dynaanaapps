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
 * External Filter Processor
 *
 */
package com.nokia.traceviewer.engine.dataprocessor;

import java.io.IOException;
import java.util.ArrayList;

import com.nokia.traceviewer.TraceViewerPlugin;
import com.nokia.traceviewer.engine.TraceProperties;
import com.nokia.traceviewer.engine.TraceViewerGlobals;
import com.nokia.traceviewer.engine.preferences.PreferenceConstants;

/**
 * External Filter Processor handles starting and stopping external filter
 * application
 * 
 */
public final class ExternalFilterProcessor {

	/**
	 * Quotation mark char
	 */
	private static final char QUOTATION_MARK = '"';

	/**
	 * The process running the current filter application
	 */
	private Process process;

	/**
	 * Data sender
	 */
	private ExternalFilterDataWriter writer;

	/**
	 * Data reader
	 */
	private ExternalFilterDataReader reader;

	/**
	 * Start external application
	 * 
	 * @return true if process was started
	 */
	public boolean startExternalApplication() {
		boolean success = true;
		String command = TraceViewerPlugin.getDefault().getPreferenceStore()
				.getString(PreferenceConstants.EXTERNAL_FILTER_COMMAND);

		// Split the command
		String[] args = splitCommand(command);

		if (process != null) {
			process.destroy();
			process = null;
		}
		if (reader != null) {
			reader.shutDown();
			reader = null;
		}

		if (args.length > 0) {

			// Create the process
			try {
				ProcessBuilder bb = new ProcessBuilder(args);
				process = bb.start();

				// Create data fetcher and start it
				reader = new ExternalFilterDataReader(process.getInputStream());
				reader.start();

				// Create filtered file
				createFilteredFile();

			} catch (IOException e) {
				e.printStackTrace();
				success = false;
			}
		} else {
			success = false;
		}
		return success;
	}

	/**
	 * Creates filtered file
	 */
	private void createFilteredFile() {
		// Create filtered file and open the progressbar
		TraceViewerGlobals.getTraceViewer().getDataProcessorAccess()
				.getFilterProcessor().getFilterDialog().applyFilters(
						TraceViewerGlobals.getTraceViewer()
								.getDataReaderAccess().getMainDataReader()
								.getTraceCount());

	}

	/**
	 * Splits the command into String array
	 * 
	 * @param command
	 *            command
	 * @return String array containing the splitted command
	 */
	private String[] splitCommand(String command) {
		boolean containsQuotationMark = false;
		ArrayList<String> commands = new ArrayList<String>();

		// Split the command with space
		String[] args = command.split("\\s"); //$NON-NLS-1$

		// Go through tokens
		for (int i = 0; i < args.length; i++) {
			String arg = args[i];

			// No quotation marks going on
			if (!containsQuotationMark) {
				if (containsQuotationMark(arg)) {
					containsQuotationMark = true;
				}
				commands.add(arg);

				// Previous token has contained quotation mark
			} else {
				if (containsQuotationMark(arg)) {
					containsQuotationMark = false;
				}
				int idx = commands.size() - 1;
				String prev = commands.get(idx);
				String newStr = prev + ' ' + arg;
				commands.set(idx, newStr);
			}
		}

		// Move the commands from the arraylist to array
		String[] retArr = new String[commands.size()];
		for (int i = 0; i < retArr.length; i++) {

			// Remove quotation marks, ProcessBuilder doesn't want them
			retArr[i] = commands.get(i).replace("\"", ""); //$NON-NLS-1$ //$NON-NLS-2$
		}

		return retArr;
	}

	/**
	 * Checks if given argument contains odd number of quotation marks
	 * 
	 * @param arg
	 *            the argument
	 * @return true if arg contains odd number of quotation marks
	 */
	private boolean containsQuotationMark(String arg) {
		boolean contains = false;
		int noOfQuotationMarks = 0;

		// Calculate quotation marks
		for (int i = 0; i < arg.length(); i++) {
			if (arg.charAt(i) == QUOTATION_MARK) {
				noOfQuotationMarks++;
			}
		}

		// Check that number of quotation marks is odd number
		if (noOfQuotationMarks % 2 != 0) {
			contains = true;
		}

		return contains;
	}

	/**
	 * Stops external application
	 */
	public void stopExternalApplication() {
		if (process != null) {
			process.destroy();
			process = null;
		}
		if (reader != null) {
			reader.shutDown();
			reader = null;
		}
		if (writer != null) {
			writer.shutDown();
			writer = null;
		}

		// Remove filters and open progressbar
		TraceViewerGlobals.getTraceViewer().getDataProcessorAccess()
				.getFilterProcessor().getFilterDialog().removeFilters(
						TraceViewerGlobals.getTraceViewer()
								.getDataReaderAccess().getMainDataReader()
								.getTraceCount());
	}

	/**
	 * Gets process
	 * 
	 * @return the process
	 */
	public Process getProcess() {
		return process;
	}

	/**
	 * Writes trace to process outputstream
	 * 
	 * @param properties
	 *            trace properties
	 */
	public void writeTraceToProcess(TraceProperties properties) {
		if (writer == null) {
			writer = new ExternalFilterDataWriter(process.getOutputStream());
		}
		writer.writeTrace(properties);
	}
}
