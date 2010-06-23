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
 * Logger DataProcessor
 *
 */
package com.nokia.traceviewer.engine.dataprocessor;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Iterator;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

import com.nokia.traceviewer.action.TraceViewerActionUtils;
import com.nokia.traceviewer.engine.TraceProperties;
import com.nokia.traceviewer.engine.TraceViewerGlobals;
import com.nokia.traceviewer.engine.TraceViewerUtils;

/**
 * Logger DataProcessor
 * 
 */
public final class Logger implements DataProcessor {

	/**
	 * Equals char
	 */
	private static final char EQUALS_CHAR = '=';

	/**
	 * Parameter char
	 */
	private static final char PARAMETER_CHAR = 'p';

	/**
	 * Machine readable time tag
	 */
	private static final String TAG_TIME = "time="; //$NON-NLS-1$

	/**
	 * Machine readable CPU ID tag
	 */
	private static final String TAG_CPU_ID = "cpuID="; //$NON-NLS-1$

	/**
	 * Machine readable Thread ID tag
	 */
	private static final String TAG_THREAD_ID = "threadID="; //$NON-NLS-1$

	/**
	 * Component ID tag
	 */
	private static final String TAG_COMPONENT_ID = "compID="; //$NON-NLS-1$

	/**
	 * Group ID tag
	 */
	private static final String TAG_GROUP_ID = "groupID="; //$NON-NLS-1$

	/**
	 * Trace ID tag
	 */
	private static final String TAG_TRACE_ID = "traceID="; //$NON-NLS-1$

	/**
	 * Trace text tag
	 */
	private static final String TAG_TRACE_TEXT = "trace="; //$NON-NLS-1$

	/**
	 * Data tag
	 */
	private static final String DATA_TAG = "data="; //$NON-NLS-1$

	/**
	 * Comment tag
	 */
	private static final String COMMENT_TAG = "comment="; //$NON-NLS-1$

	/**
	 * Empty string
	 */
	private static final String EMPTY = ""; //$NON-NLS-1$

	/**
	 * Start bracket tag
	 */
	private static final char TAG_START_BRACKET = '[';

	/**
	 * End bracket tag
	 */
	private static final char TAG_END_BRACKET = ']';

	/**
	 * Hyphen char
	 */
	private static final char HYPHEN_CHAR = '-';

	/**
	 * Underscore char
	 */
	private static final char UNDERSCORE_CHAR = '_';

	/**
	 * Semicolon char
	 */
	private static final char SEMICOLON_CHAR = ';';

	/**
	 * End line character \r
	 */
	private static final char ENDLINE_R = '\r';

	/**
	 * End line character \n
	 */
	private static final char ENDLINE_N = '\n';

	/**
	 * Tabulator character
	 */
	private static final char TABULATOR = '\t';

	/**
	 * Writer to be used for writing
	 */
	private PrintWriter plainOutput;

	/**
	 * Filechannel where binary log is written
	 */
	private FileChannel binaryOutput;

	/**
	 * Logging in plain text
	 */
	private boolean plainLogging;

	/**
	 * Logging in binary
	 */
	private boolean binaryLogging;

	/**
	 * Path to plain text log file
	 */
	private String plainLogPath;

	/**
	 * Path to binary log file
	 */
	private String binaryLogPath;

	/**
	 * Omitting timestamp or not
	 */
	private boolean omitTimestamps;

	/**
	 * Machine readable log file
	 */
	private boolean machineReadable;

	/**
	 * Tells if a log file is opened
	 */
	private boolean logFileOpened;

	/**
	 * Opened log file path
	 */
	private String openedLogFilePath;

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.nokia.traceviewer.engine.DataProcessor#processData(com.nokia.traceviewer
	 * .engine.TraceProperties)
	 */
	public void processData(TraceProperties properties) {
		if (!properties.traceConfiguration.isFilteredOut()
				&& !properties.traceConfiguration.isScrolledTrace()
				&& !properties.traceConfiguration.isTriggeredOut()) {

			// Logging as plain text
			if (plainLogging) {

				String trace = createPlainTextTrace(properties);

				// Write the trace
				if (trace != null) {
					plainOutput.write(trace);
				}
			}

			// Binary logging
			if (binaryLogging) {
				try {
					if (properties.byteBuffer != null && binaryOutput != null) {

						// Write all parts of multipart trace
						if (properties.bTraceInformation
								.getMultiPartTraceParts() != null) {
							Iterator<byte[]> i = properties.bTraceInformation
									.getMultiPartTraceParts().getTraceParts()
									.iterator();

							while (i.hasNext()) {
								byte[] byteArr = i.next();
								binaryOutput.write(ByteBuffer.wrap(byteArr));
							}
						} else {

							// Write the message to file
							int position = properties.byteBuffer.position();
							int limit = properties.byteBuffer.limit();
							properties.byteBuffer.limit(properties.messageStart
									+ properties.messageLength);
							properties.byteBuffer
									.position(properties.messageStart);

							binaryOutput.write(properties.byteBuffer);
							properties.byteBuffer.limit(limit);
							properties.byteBuffer.position(position);
						}
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * Create plain text trace
	 * 
	 * @param properties
	 *            trace properties
	 * @return plain text trace string or null if this trace should not be
	 *         logged
	 */
	private String createPlainTextTrace(TraceProperties properties) {
		String ret = null;
		StringBuffer trace = null;

		// Normal ASCII trace log
		if (!machineReadable && !properties.binaryTrace) {

			// If omitting timestamps, don't write them
			if (omitTimestamps || properties.timestampString == null) {
				int traceLen = properties.traceString.length() + 1;
				trace = new StringBuffer(traceLen);
				if (properties.bTraceInformation.isTraceMissing()) {
					trace.append(TraceViewerActionUtils.TRACES_DROPPED_MSG);
				}
				trace.append(properties.traceString);
				trace.append(ENDLINE_N);

				// Write timestamp
			} else {
				StringBuffer timeFromPreviousSB = TraceViewerGlobals
						.getTraceViewer().getDataProcessorAccess()
						.getTimestampParser().getTimeFromPreviousString(
								properties.timeFromPreviousTrace);
				int traceLen = properties.timestampString.length() + 1
						+ timeFromPreviousSB.length()
						+ properties.traceString.length() + 1;
				trace = new StringBuffer(traceLen);
				if (properties.bTraceInformation.isTraceMissing()) {
					trace.append(TraceViewerActionUtils.TRACES_DROPPED_MSG);
				}
				trace.append(properties.timestampString);
				trace.append(timeFromPreviousSB);
				trace.append(TABULATOR);
				trace.append(properties.traceString);
				trace.append(ENDLINE_N);
			}

			// Machine readable log file
		} else if (machineReadable) {

			// Create buffer three times as big as original trace text
			if (properties.traceString != null) {
				trace = new StringBuffer(properties.traceString.length() * 3);
			} else {
				trace = new StringBuffer();
			}

			// Get trace variables
			int cid = properties.information.getComponentId();
			int gid = properties.information.getGroupId();
			int tid = properties.information.getTraceId();

			// Timestamp
			trace.append(TAG_TIME);
			if (properties.timestampString != null) {
				trace.append(properties.timestampString);
			} else {
				trace.append(properties.timestamp);
			}
			trace.append(SEMICOLON_CHAR);

			// CPU ID
			trace.append(TAG_CPU_ID);
			if (properties.bTraceInformation.getCpuId() != -1) {
				trace.append(properties.bTraceInformation.getCpuId());
			} else {
				trace.append(HYPHEN_CHAR);
			}
			trace.append(SEMICOLON_CHAR);

			// Thread ID
			trace.append(TAG_THREAD_ID);
			if (properties.bTraceInformation.getThreadId() != 0) {
				trace.append(properties.bTraceInformation.getThreadId());
			} else {
				trace.append(HYPHEN_CHAR);
			}
			trace.append(SEMICOLON_CHAR);

			// Get Component, Group and Trace names
			String[] names = TraceViewerGlobals.getDecodeProvider()
					.getComponentGroupTraceName(cid, gid, tid);

			// Component ID
			String componentName = names[0];
			if (componentName == null) {
				componentName = EMPTY;
			}
			trace.append(TAG_COMPONENT_ID);
			trace
					.append(componentName.replace(SEMICOLON_CHAR,
							UNDERSCORE_CHAR));
			trace.append(TAG_START_BRACKET);
			trace.append(cid);
			trace.append(TAG_END_BRACKET);
			trace.append(SEMICOLON_CHAR);

			// Group ID
			String groupName = names[1];
			if (groupName == null) {
				groupName = EMPTY;
			}
			trace.append(TAG_GROUP_ID);
			trace.append(groupName.replace(SEMICOLON_CHAR, UNDERSCORE_CHAR));
			trace.append(TAG_START_BRACKET);
			trace.append(gid);
			trace.append(TAG_END_BRACKET);
			trace.append(SEMICOLON_CHAR);

			// Trace ID
			String traceName = names[2];
			if (traceName == null) {
				traceName = EMPTY;
			}
			trace.append(TAG_TRACE_ID);
			trace.append(traceName.replace(SEMICOLON_CHAR, UNDERSCORE_CHAR));
			trace.append(TAG_START_BRACKET);
			trace.append(tid);
			trace.append(TAG_END_BRACKET);
			trace.append(SEMICOLON_CHAR);

			// Parameters
			for (int i = 0; i < properties.parameters.size(); i++) {
				trace.append(PARAMETER_CHAR);
				trace.append(i + 1);
				trace.append(EQUALS_CHAR);
				trace.append(properties.parameters.get(i).replace(
						SEMICOLON_CHAR, UNDERSCORE_CHAR));
				trace.append(SEMICOLON_CHAR);
			}

			// Trace comment
			if (properties.traceComment != null) {
				trace.append(COMMENT_TAG);
				trace.append(properties.traceComment);
				trace.append(SEMICOLON_CHAR);
			}

			// Trace string
			if (properties.traceString != null) {
				trace.append(TAG_TRACE_TEXT);
				trace.append(properties.traceString.replace(SEMICOLON_CHAR,
						UNDERSCORE_CHAR));
				trace.append(SEMICOLON_CHAR);
				// Trace as hex
			} else {
				// Get data as hex
				String hexTrace = TraceViewerUtils.getTraceAsHexString(
						properties.byteBuffer, properties.dataStart,
						properties.dataLength, false);
				trace.append(DATA_TAG);
				trace.append(hexTrace);
				trace.append(SEMICOLON_CHAR);

			}

			trace.append(ENDLINE_R);
			trace.append(ENDLINE_N);
		}

		if (trace != null) {
			ret = trace.toString();
		}

		return ret;
	}

	/**
	 * Constructor
	 */
	public Logger() {
		plainLogging = false;
		binaryLogging = false;
		omitTimestamps = false;
		plainLogPath = EMPTY;
		binaryLogPath = EMPTY;
	}

	/**
	 * Flushes data
	 */
	public void flush() {
		if (plainOutput != null) {
			plainOutput.flush();
		}
	}

	/**
	 * Gets plain text logging status
	 * 
	 * @return status of plain text logging
	 */
	public boolean isPlainLogging() {
		return plainLogging;
	}

	/**
	 * Gets plain text log file path
	 * 
	 * @return plainLogPath
	 */
	public String getPlainLogPath() {
		return plainLogPath;
	}

	/**
	 * Gets binary log file path
	 * 
	 * @return binaryLogPath
	 */
	public String getBinaryLogPath() {
		return binaryLogPath;
	}

	/**
	 * Gets logging status
	 * 
	 * @return status of binary logging
	 */
	public boolean isBinLogging() {
		return binaryLogging;
	}

	/**
	 * Starts plain text logging
	 * 
	 * @param filename
	 *            file name where to save
	 * @param omitTimestamp
	 *            true if timestamps should be omitted
	 * @param machineReadable
	 *            true if log file should be machine readable
	 * @return status of starting of logging
	 */
	public boolean startPlainTextLogging(String filename,
			boolean omitTimestamp, boolean machineReadable) {
		plainLogPath = filename;
		this.omitTimestamps = omitTimestamp;
		this.machineReadable = machineReadable;

		// Build file channel
		boolean success = buildPlainFileWriter();
		plainLogging = true;
		return success;
	}

	/**
	 * Stops plain text logging
	 * 
	 * @return status of stopPlainTextLogging
	 */
	public boolean stopPlainTextLogging() {
		plainOutput.close();
		plainLogging = false;

		// Export possible comments
		TraceViewerGlobals.getTraceViewer().getDataProcessorAccess()
				.getTraceCommentHandler().exportTraceComments(plainLogPath);

		return true;
	}

	/**
	 * Start binary logging
	 * 
	 * @param filename
	 *            file name
	 * @return status of binary logging
	 */
	public boolean startBinaryLogging(String filename) {
		boolean success = false;
		try {
			binaryLogPath = filename;

			// Build file channel
			FileOutputStream outputFile = new FileOutputStream(binaryLogPath);

			binaryOutput = outputFile.getChannel();
			binaryLogging = true;
			success = true;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		return success;
	}

	/**
	 * Stops binary logging
	 * 
	 * @return status of stopBinLogging
	 */
	public boolean stopBinLogging() {
		binaryLogging = false;
		try {
			binaryOutput.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		// Export possible comments
		TraceViewerGlobals.getTraceViewer().getDataProcessorAccess()
				.getTraceCommentHandler().exportTraceComments(binaryLogPath);

		return true;
	}

	/**
	 * Opens log file
	 * 
	 * @param path
	 *            path to log file
	 * @param binary
	 *            binary file
	 */
	public void openLogFile(String path, boolean binary) {
		TraceViewerGlobals.getTraceViewer().getDataReaderAccess().openLogFile(
				path, binary);
		openedLogFilePath = path;
	}

	/**
	 * Builds the plain file writer
	 * 
	 * @return status of building plain text file
	 */
	public boolean buildPlainFileWriter() {
		boolean success = false;

		try {
			plainOutput = new PrintWriter(new FileWriter(plainLogPath));
			success = true;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return success;
	}

	/**
	 * Tells if log file is opened
	 * 
	 * @return true if log file is opened
	 */
	public boolean isLogFileOpened() {
		return logFileOpened;
	}

	/**
	 * Sets the log file opened variable
	 * 
	 * @param logFileOpened
	 *            status of log file opened
	 */
	public void setLogFileOpened(boolean logFileOpened) {
		this.logFileOpened = logFileOpened;

		if (!logFileOpened) {
			openedLogFilePath = null;
		}
	}

	/**
	 * Gets currently opened log file path
	 * 
	 * @return currently opened log file path or null if no log file is open
	 */
	public String getOpenedLogFilePath() {
		return openedLogFilePath;
	}

	/**
	 * Gets currently opened log file name
	 * 
	 * @return currently opened log file name or null if no log file is open
	 */
	public String getOpenedLogFileName() {
		String fileName = null;

		if (openedLogFilePath != null) {
			IPath filePath = new Path(openedLogFilePath);
			fileName = filePath.lastSegment();
		}

		return fileName;
	}
}