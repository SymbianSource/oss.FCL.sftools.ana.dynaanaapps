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
 * Trace Comment Handler
 *
 */
package com.nokia.traceviewer.engine.dataprocessor;

import java.util.Map;
import java.util.TreeMap;

import com.nokia.traceviewer.action.TraceViewerActionUtils;
import com.nokia.traceviewer.engine.TraceProperties;
import com.nokia.traceviewer.engine.TraceViewerGlobals;
import com.nokia.traceviewer.engine.metafile.MetaFileXMLExporter;
import com.nokia.traceviewer.engine.metafile.MetaFileXMLImporter;

/**
 * Trace Comment Handler
 */
public class TraceCommentHandler implements DataProcessor {

	/**
	 * Postfix for comments file
	 */
	private static final String COMMENTS_FILE_POSTFIX = ".meta"; //$NON-NLS-1$

	/**
	 * Map containing the trace comments
	 */
	private final Map<Integer, String> commentMap;

	/**
	 * Constructor
	 */
	public TraceCommentHandler() {
		commentMap = new TreeMap<Integer, String>();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.nokia.traceviewer.engine.dataprocessor.DataProcessor#processData(
	 * com.nokia.traceviewer.engine.TraceProperties)
	 */
	public void processData(TraceProperties properties) {
		if (!commentMap.isEmpty()) {
			properties.traceComment = commentMap.get(Integer
					.valueOf(properties.traceNumber));
		}
	}

	/**
	 * Inserts new comment to trace
	 * 
	 * @param traceNumber
	 *            trace number
	 * @param comment
	 *            trace comment
	 */
	public void insertComment(int traceNumber, String comment) {
		if (!commentMap.containsKey(Integer.valueOf(traceNumber))) {
			commentMap.put(Integer.valueOf(traceNumber), comment);

			updateCommentsToViewAndFile();
		}
	}

	/**
	 * Update comment to the trace
	 * 
	 * @param traceNumber
	 *            trace number
	 * @param comment
	 *            trace comment
	 */
	public void updateComment(int traceNumber, String comment) {
		if (commentMap.containsKey(Integer.valueOf(traceNumber))) {
			commentMap.remove(Integer.valueOf(traceNumber));
			commentMap.put(Integer.valueOf(traceNumber), comment);

			updateCommentsToViewAndFile();
		}
	}

	/**
	 * Update comment to the trace
	 * 
	 * @param traceNumber
	 *            trace number
	 */
	public void removeComment(int traceNumber) {
		if (commentMap.containsKey(Integer.valueOf(traceNumber))) {
			commentMap.remove(Integer.valueOf(traceNumber));

			updateCommentsToViewAndFile();
		}
	}

	/**
	 * Get comments in line number order
	 * 
	 * @return map of the line numbers and comments
	 */
	public Map<Integer, String> getComments() {
		return commentMap;
	}

	/**
	 * Gets comment with a trace number
	 * 
	 * @param traceNumber
	 *            trace number
	 * @return comment for this trace number or null if not found
	 */
	public String getComment(int traceNumber) {
		String comment = null;
		if (commentMap.containsKey(Integer.valueOf(traceNumber))) {
			comment = commentMap.get(Integer.valueOf(traceNumber));
		}

		return comment;
	}

	/**
	 * Exports trace comments to a file
	 * 
	 * @param logFilePath
	 *            path where the log file was saved
	 */
	public void exportTraceComments(String logFilePath) {
		if (!commentMap.isEmpty()) {
			String commentLogPath = logFilePath + COMMENTS_FILE_POSTFIX;

			MetaFileXMLExporter exporter = new MetaFileXMLExporter(
					commentLogPath);
			exporter.export();
		}
	}

	/**
	 * Imports trace comments from a file
	 * 
	 * @param logFilePath
	 *            path where the log file was saved
	 */
	public void importTraceComments(String logFilePath) {
		String commentLogPath = logFilePath + COMMENTS_FILE_POSTFIX;

		MetaFileXMLImporter importer = new MetaFileXMLImporter(commentLogPath);
		importer.importData(commentMap);

		// Update property view
		updateCommentsToViewAndFile();
	}

	/**
	 * Updates comments to property view and possible file
	 */
	void updateCommentsToViewAndFile() {
		// Open property view if it doesn't exist and there's comments
		if (!commentMap.isEmpty()) {
			TraceViewerActionUtils.openPropertyView();
		}
		// Update comments
		if (TraceViewerGlobals.getTraceViewer().getPropertyView() != null) {
			TraceViewerGlobals.getTraceViewer().getPropertyView()
					.updateTraceComments();
		}

		// If log file is opened, update the comments file
		if (TraceViewerGlobals.getTraceViewer().getDataProcessorAccess()
				.getLogger().isLogFileOpened()) {
			String logFile = TraceViewerGlobals.getTraceViewer()
					.getDataProcessorAccess().getLogger()
					.getOpenedLogFilePath();

			if (logFile != null) {
				// Export the changed comments
				exportTraceComments(logFile);
			}
		}

		// Refresh current view
		if (TraceViewerGlobals.getTraceViewer().getView() != null) {
			TraceViewerGlobals.getTraceViewer().getView().refreshCurrentView();
		}
	}
}
