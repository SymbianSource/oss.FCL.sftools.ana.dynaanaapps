/*
 * Copyright (c) 2009 Nokia Corporation and/or its subsidiary(-ies).
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
 */

package com.nokia.s60tools.memspy.model;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.regex.Pattern;

import com.nokia.s60tools.memspy.containers.ThreadInfo;
import com.nokia.s60tools.memspy.export.ITraceDataProcessor;
import com.nokia.s60tools.memspy.interfaces.IMemSpyTraceListener.LauncherErrorType;
import com.nokia.s60tools.memspy.util.MemSpyConsole;
import com.nokia.s60tools.util.debug.DbgUtility;

/**
 * Monitors data received from TraceCore and acts based on it.
 */
public final class TraceCoreDataHandler implements ITraceDataProcessor {

	// Count of lines
	private int lineCount;

	// boolean value that is used when parsing thread list
	private boolean lastWasName;

	// Array, where found thread names and id's are written
	private ArrayList<ThreadInfo> threadArray = null;

	// ThreadInfo-object where info is collected
	private ThreadInfo threadInfo;

	// boolean value that is set to true, when trace-lines are lost
	private boolean dumpedTraces;

	// boolean value that is set to false, when heap type is not symbian OS
	// Rheap
	private boolean heapTypeCorrect;

	// boolean value that is true when some information is written to file
	private boolean writeFile;

	// boolean value that true when file is open
	private boolean fileOpen;

	// Writer that writes to file
	private PrintWriter plainOutput;

	// Writer that writes to file
	private PrintWriter swmtHeadDumpOutput = null;

	// boolean value that is true when some memspy operations are on-going.
	private boolean logging;

	// Trace engine.
	private TraceCoreEngine engine;

	// Strings
	private final static String LAUNCHER_READY = "<MEMSPY_LAUNCHER_READY>";
	private final static String MEMSPY_LAUNCHER_VERSION_PREFIX = "<MEMSPY_LAUNCHER_DATAVERSION=";// e.g.
	// <MEMSPY_LAUNCHER_VERSION=1>
	private final static String END_TAG = ">";
	private final static String LAUNCHER_COLON = "::";
	private final static String LAUNCHER_THREAD_ID = "Thread Id";
	private final static String LAUNCHER_TYPE = "Type:";
	private final static String LAUNCHER_SYMBIAN_OS_RHEAP = "Symbian OS RHeap";
	private final static String LAUNCHER_SYMBIAN_OS_RHYBRIDHEAP = "Symbian OS RHybridHeap";
	private final static String LAUNCHER_HEAP_DUMP_START = "<MEMSPY_HEAP_DATA";
	private final static String LAUNCHER_HEAP_DUMP_END = "</MEMSPY_HEAP_DATA";
	// Heap info's will act as start point for new Head dump, when Head Dumps
	// are received during SWMT logging
	// E.g. following Heap info is received:
	// HeapData - mc_isiserver::Main - HEAP INFO FOR THREAD 'mc_isiserver::Main'
	private final static String LAUNCHER_HEAP_INFO_FOR_THREAD = "HEAP INFO FOR THREAD";
	private final static String LAUNCHER_HEAPDATA = "HeapData -";
	private final static String LAUNCHER_ERROR = "<MEMSPY_LAUNCHER_ERROR>";
	private final static String DUMPED_TC_TRACES = "* Dumped Traces";
	private final static String MEMSPY_PROGRESS = "<MEMSPY_PROGRESS>";

	/**
	 * <code>SYSTEM WIDE MEMORY TRACKER<code> tag
	 */
	public final static String LAUNCHER_SWMT_LOG_START = "<SYSTEM WIDE MEMORY TRACKER>";

	private final static String LAUNCHER_SWMT_LOG_END = "</SYSTEM WIDE MEMORY TRACKER>";
	private final static String LAUNCHER_SWMTDATA = "[SMT ";
	private final static String LAUNCHER_CATEGORY_NOT_SUPPORTED = "<MEMSPY_LAUNCHER_CATEGORY_NOT_SUPPORTED>";

	private ThreadInfo swmtHeadDumpThreadInfo;

	/**
	 * Constructor.
	 * 
	 * @param engine
	 *            engine that uses handler
	 */
	public TraceCoreDataHandler(TraceCoreEngine engine) {
		this.engine = engine;
		lastWasName = false;
		heapTypeCorrect = false;
		writeFile = false;
		lineCount = 0;
	}

	/**
	 * Method that is called when trace logging is started.
	 * 
	 * @param fName
	 *            Name of the file, where needed information is printed.
	 * @param fileOpen
	 *            boolean value that is true when file needs to be opened.
	 */
	public boolean startLogging(String fName, boolean fileOpen) {
		logging = true;
		lineCount = 0;

		DbgUtility
				.println(
						DbgUtility.PRIORITY_OPERATION,
						"TraceCoreDataHandler.startLogging/fName=" + fName + ", fileOpen=" + fileOpen); //$NON-NLS-1$ //$NON-NLS-2$

		// If file needs to be opened, open it.
		this.fileOpen = fileOpen;
		if (fileOpen) {
			try {
				plainOutput = new PrintWriter(new FileWriter(fName));
			} catch (IOException e) {

				engine.launcherError(LauncherErrorType.FILE);
				return false;
			}

		}
		return true;
	}

	/**
	 * Stops logging and closes file if needed.
	 */
	public void stopLogging() {
		DbgUtility.println(DbgUtility.PRIORITY_OPERATION,
				"TraceCoreDataHandler.stopLogging"); //$NON-NLS-1$				
		this.logging = false;
		if (this.fileOpen) {
			plainOutput.flush();
			plainOutput.close();
		}
		stopSWMTHeadDumpLogging();
	}

	/**
	 * Flush and Close swmtHeadDumpOutput
	 */
	private void stopSWMTHeadDumpLogging() {
		if (swmtHeadDumpOutput != null) {
			swmtHeadDumpOutput.flush();
			swmtHeadDumpOutput.close();
		}
	}

	/**
	 * Open FileWriter for swmtHeadDumpOutput
	 */
	private void startSWMTHeadDumpLogging() {
		try {
			File file = new File(swmtHeadDumpThreadInfo.getThreadFilePath());
			File path = file.getParentFile();
			if (!path.exists()) {
				path.mkdirs();
			}
			file.createNewFile();
			if (swmtHeadDumpThreadInfo != null) {
				swmtHeadDumpOutput = new PrintWriter(new FileWriter(
						swmtHeadDumpThreadInfo.getThreadFilePath()));
			}
		} catch (IOException e) {
			engine.launcherError(LauncherErrorType.FILE);
		}
	}

	/**
	 * Processes trace data that is received. This method is called every time
	 * trace data is received when logging is on.
	 * 
	 * @param traceLineStr
	 *            trace data line
	 */
	public void processDataLine(String traceLineStr) {

		if (logging) {

			if (traceLineStr != null) {

				if (isMemSpyRelatedLine(traceLineStr)) {
					lineCount++;
				}
				// Reset timer every 10 MemSpy related lines.
				if (lineCount > 10) {
					engine.restartErrorTimer();
					lineCount = 0;
				}

				// If Line contains launcher error message
				if (traceLineStr.contains(LAUNCHER_ERROR)) {
					handleLauncherErrorLine(traceLineStr);
				}

				// If line contains message of dumped traces
				else if (traceLineStr.contains(DUMPED_TC_TRACES)) {
					handleDumpTCTracesLine();
				}

				// If line contains confirmation that Launcher is ready to
				// receive new command
				else if (traceLineStr.contains(LAUNCHER_READY)) {
					handleLauncherReadyLine();
				} else if (traceLineStr
						.contains(MEMSPY_LAUNCHER_VERSION_PREFIX)) {
					handleLauncherVersionLine(traceLineStr);
				}
				// If launcher sends an progress message, restarting error timer
				// so the timer wont reset progress
				// this is done because of long taking progress was causing time
				// outs.
				else if (traceLineStr.contains(MEMSPY_PROGRESS)) {
					engine.restartErrorTimer();
				}

				// If receiving heap dump
				else if (engine.getFirstTask() == TraceCoreEngine.MEMSPY_GET_HEAP_DUMP) {
					handleHeadDumpLine(traceLineStr);
				}

				// If receiving SWMT log
				else if (engine.getFirstTask() == TraceCoreEngine.MEMSPY_SWMT_UPDATE
						|| engine.getFirstTask() == TraceCoreEngine.MEMSPY_SWMT_RESET) {
					handleSWMTLine(traceLineStr);
				}

				// If receiving thread info
				else if (engine.getFirstTask() == TraceCoreEngine.MEMSPY_THREAD_INFO) {
					handleThreadInfoLine(traceLineStr);
				}

				// Setting SWMT category low bits
				else if (engine.getFirstTask() == TraceCoreEngine.MEMSPY_SET_CATEGORIES_LOW) {
					handleCategoriesLowLine(traceLineStr);
				}

				// Setting SWMT category high bits
				else if (engine.getFirstTask() == TraceCoreEngine.MEMSPY_SET_CATEGORIES_HIGH) {
					handleCategoriesHighLine(traceLineStr);
				}

				// If Receiving thread list
				else if (engine.getFirstTask() == TraceCoreEngine.MEMSPY_THREAD_LIST) {
					handleThreadListLine(traceLineStr);
				}

			}
		}

	}

	/**
	 * Checks that line has something to do with MemSpy related data.
	 * 
	 * @param str
	 *            line sting
	 * @return <code>true</code> if MemSpy related data, otherwise
	 *         <code>false</code>.
	 */
	private boolean isMemSpyRelatedLine(String str) {
		return str.contains("HeapData") || str.contains("MemSpy");
	}

	private void handleLauncherVersionLine(String str) {
		DbgUtility
				.println(DbgUtility.PRIORITY_LOOP,
						"TraceCoreDataHandler.processData/MEMSPY_LAUNCHER_VERSION_PREFIX"); //$NON-NLS-1$
		String version = cutString(MEMSPY_LAUNCHER_VERSION_PREFIX, END_TAG, str);
		engine.setMemSpyLauncherVersion(version);
	}

	private void handleLauncherReadyLine() {
		DbgUtility.println(DbgUtility.PRIORITY_OPERATION,
				"TraceCoreDataHandler.processData/LAUNCHER_READY"); //$NON-NLS-1$
		engine.memSpyReady();
	}

	private void handleDumpTCTracesLine() {
		DbgUtility.println(DbgUtility.PRIORITY_OPERATION,
				"TraceCoreDataHandler.processData/DUMPED_TC_TRACES"); //$NON-NLS-1$
		this.dumpedTraces = true;
	}

	private void handleLauncherErrorLine(String str) {
		DbgUtility.println(DbgUtility.PRIORITY_OPERATION,
				"TraceCoreDataHandler.processData/LAUNCHER_ERROR"); //$NON-NLS-1$
		if (str.contains("'")) {
			str = str.substring(str.indexOf("'") + 1);
			if (str.contains("'")) {
				String error = str.substring(0, str.indexOf("'"));
				LauncherErrorType type = getErrorById(error);
				engine.launcherError(type);
			}
		} else {
			// If no error code present, then parsing custom error message and
			// sending information to console
			String additionalErrorInfo = "";
			// Getting error message string portion
			String[] splitArr = str.split(Pattern.quote(":"));
			if (splitArr.length == 2) {
				additionalErrorInfo = additionalErrorInfo + splitArr[1].trim();
			}
			// Passing launcher error forwards generic launcher error
			engine.launcherError(LauncherErrorType.GENERAL_LAUNCHER_ERROR,
					additionalErrorInfo);
		}
	}

	private void handleThreadListLine(String str) {
		// If line contains "::" create new ThreadInfo-object
		if (str.contains(LAUNCHER_COLON)) {

			threadInfo = new ThreadInfo();
			threadInfo.setThreadName(str);
			lastWasName = true;

		}

		// Save threadID into latest ThreadInfo-object and add info into thread
		// list
		if (str.contains(LAUNCHER_THREAD_ID)) {
			if (lastWasName) {
				String threadID = str.substring(str.indexOf(LAUNCHER_THREAD_ID)
						+ LAUNCHER_THREAD_ID.length());
				threadID = threadID.trim();
				threadInfo.setThreadID(threadID);
				threadArray.add(threadInfo);
				DbgUtility
						.println(
								DbgUtility.PRIORITY_OPERATION,
								"TraceCoreDataHandler.processData/LAUNCHER_THREAD_ID/id=" + threadInfo.getThreadID() + ", name=" + threadInfo.getThreadName()); //$NON-NLS-1$ //$NON-NLS-2$
				lastWasName = false;
			}
		}
	}

	private void handleCategoriesHighLine(String str) {
		DbgUtility
				.println(
						DbgUtility.PRIORITY_OPERATION,
						"TraceCoreDataHandler.processData/MEMSPY_SET_CATEGORIES_HIGH:  " + str); //$NON-NLS-1$
	}

	private void handleCategoriesLowLine(String str) {
		DbgUtility
				.println(
						DbgUtility.PRIORITY_OPERATION,
						"TraceCoreDataHandler.processData/MEMSPY_SET_CATEGORIES_LOW:  " + str); //$NON-NLS-1$
		if (str.contains(LAUNCHER_CATEGORY_NOT_SUPPORTED)) {
			// LAUNCHER_CATEGORY_NOT_SUPPORTED error
			engine.launcherError(LauncherErrorType.CATEGORIES_NOT_SUPPORTED);
		}
	}

	private void handleThreadInfoLine(String str) {
		// Check for threads heap type
		if (str.contains(LAUNCHER_TYPE)) {
			if (str.contains(LAUNCHER_SYMBIAN_OS_RHEAP)
					|| str.contains(LAUNCHER_SYMBIAN_OS_RHYBRIDHEAP)) {
				DbgUtility.println(DbgUtility.PRIORITY_OPERATION,
						"TraceCoreDataHandler.processData/MEMSPY_THREAD_INFO"); //$NON-NLS-1$
				heapTypeCorrect = true;
			}
		}
	}

	private void handleSWMTLine(String str) {
		// Check for start tag
		if (str.contains(LAUNCHER_SWMT_LOG_START)) {
			DbgUtility.println(DbgUtility.PRIORITY_OPERATION,
					"TraceCoreDataHandler.processData/LAUNCHER_SWMT_LOG_START"); //$NON-NLS-1$
			this.writeFile = true;
			engine.restartErrorTimer(); // Resetting error time instantly when
			// getting start event of the logging
		}

		// If writing to file
		if (this.writeFile && str.contains(LAUNCHER_SWMTDATA)) {
			this.writeLine(str);
		}

		// If we receive a Heap Dump line during SWMT logging
		if (this.writeFile
				&& (str.contains(LAUNCHER_HEAPDATA)
						|| str.contains(LAUNCHER_HEAP_DUMP_START) || str
						.contains(LAUNCHER_HEAP_DUMP_END))) {
			handleHeapDumpDuringSWMTLogging(str);
		}

		// Check for end tag
		if (str.contains(LAUNCHER_SWMT_LOG_END)) {
			DbgUtility.println(DbgUtility.PRIORITY_OPERATION,
					"TraceCoreDataHandler.processData/LAUNCHER_SWMT_LOG_END"); //$NON-NLS-1$
			this.writeFile = false;
			engine.restartErrorTimer(); // Resetting error time instantly when
			// getting end event of the logging
		}
	}

	/**
	 * Handling a Heap Dump line during SWMT logging
	 * 
	 * @param str
	 */
	private void handleHeapDumpDuringSWMTLogging(String str) {

		// If we receiving a start point of Dump..
		if (str.contains(LAUNCHER_HEAP_DUMP_START)) {
			DbgUtility.println(DbgUtility.PRIORITY_LOOP,
					"TraceCoreDataHandler.processData/Start SWMT Head Dump"); //$NON-NLS-1$
			swmtHeadDumpThreadInfo = new ThreadInfo();
			if (threadArray == null) {
				setThreadArray(new ArrayList<ThreadInfo>());
			}
		}
		// If we receiving a Thread name of Dump, we create a new file for dump,
		// and start dumping it
		else if (str.contains(LAUNCHER_HEAP_INFO_FOR_THREAD)) {

			String threadName = getThreadNameFromInfo(str);
			swmtHeadDumpThreadInfo.setThreadName(threadName);
			DbgUtility
					.println(
							DbgUtility.PRIORITY_LOOP,
							"TraceCoreDataHandler.processData/SWMT Head Dump Thread name received: " + threadName); //$NON-NLS-1$

			// String threadName, String threadID, String threadFilePath, Date
			// date, HeapDumpType type
			// Get file name for heap dump from engine
			String filePath = MemSpyFileOperations.getFileNameForTempHeapDump(
					swmtHeadDumpThreadInfo.getThreadName(),
					swmtHeadDumpThreadInfo.getDate());
			swmtHeadDumpThreadInfo.setThreadFilePath(filePath);
			startSWMTHeadDumpLogging();
			writeSWMTHeadDumpLine(str);

			DbgUtility
					.println(
							DbgUtility.PRIORITY_LOOP,
							"TraceCoreDataHandler.processData/SWMT Head Dump Thread file created: " + filePath); //$NON-NLS-1$			

			// Heap info's will act as start point for new Head dump, when Head
			// Dumps are received during SWMT logging
			// E.g. following Heap info is received:
			// HeapData - mc_isiserver::Main - HEAP INFO FOR THREAD
			// 'mc_isiserver::Main'
		}

		// If we receiving a end point of Dump...
		else if (str.contains(LAUNCHER_HEAP_DUMP_END)) {
			DbgUtility.println(DbgUtility.PRIORITY_LOOP,
					"TraceCoreDataHandler.processData/End SWMT Head Dump"); //$NON-NLS-1$
			threadArray.add(swmtHeadDumpThreadInfo);
			stopSWMTHeadDumpLogging();
		}

		// Else we receiving a dump line, and writing it to the Dump file, not
		// to SWMT file
		else if (swmtHeadDumpThreadInfo != null && swmtHeadDumpOutput != null) {
			writeSWMTHeadDumpLine(str);
		} else {
			DbgUtility
					.println(
							DbgUtility.PRIORITY_LOOP,
							"TraceCoreDataHandler.processData/LAUNCHER_HEAPDATA & MEMSPY_SWMT_UPDATE unknown line occured: "
									+ str);
		}
	}

	/**
	 * Get Thread name from Heap Info line
	 * 
	 * @param str
	 * @return Thread name
	 */
	private String getThreadNameFromInfo(String str) {
		String name = str
				.substring((str.indexOf(LAUNCHER_HEAP_INFO_FOR_THREAD) + LAUNCHER_HEAP_INFO_FOR_THREAD
						.length()));
		String separator = "'";
		name = name.substring((name.indexOf(separator) + separator.length()),
				name.lastIndexOf(separator)).trim();
		return name;
	}

	private void handleHeadDumpLine(String str) {
		// Check for heap end tag
		if (str.contains(LAUNCHER_HEAP_DUMP_END)) {
			DbgUtility
					.println(DbgUtility.PRIORITY_OPERATION,
							"TraceCoreDataHandler.processData/MEMSPY_GET_HEAP_DUMP/LAUNCHER_HEAP_DUMP_END"); //$NON-NLS-1$
			this.writeFile = false;
		}

		// If writing to file
		if (this.writeFile && str.contains(LAUNCHER_HEAPDATA)) {
			this.writeLine(str);
		}

		// Check for heap start tag
		if (str.contains(LAUNCHER_HEAP_DUMP_START)) {
			DbgUtility
					.println(
							DbgUtility.PRIORITY_OPERATION,
							"TraceCoreDataHandler.processData/MEMSPY_GET_HEAP_DUMP/LAUNCHER_HEAP_DUMP_START"); //$NON-NLS-1$
			this.writeFile = true;
		}
	}

	/**
	 * Cut string from startTag to endTag
	 * 
	 * @param startTag
	 * @param endTag
	 * @param str
	 * @return cutted string or str given if start and end tags does not found
	 *         from given str
	 */
	private static String cutString(String startTag, String endTag, String str) {
		if (!str.contains(startTag) && !str.contains(endTag)) {
			return str;
		}
		String ret = str.substring(str.indexOf(startTag) + startTag.length());
		ret = ret.substring(0, ret.indexOf(endTag));
		return ret;
	}

	/**
	 * Writes one line into opened file.
	 * 
	 * @param line
	 *            , Line that is written to file
	 */
	private void writeLine(String line) {
		plainOutput.write(line + "\n");
	}

	/**
	 * Writes one line into opened file.
	 * 
	 * @param line
	 *            , Line that is written to file
	 */
	private void writeSWMTHeadDumpLine(String line) {
		swmtHeadDumpOutput.write(line + "\n");
	}

	//
	// Getters and setters for member variables
	//

	/**
	 * Set thread array
	 * 
	 * @param threadArray
	 */
	public void setThreadArray(ArrayList<ThreadInfo> threadArray) {
		this.threadArray = threadArray;
	}

	/**
	 * Check if {@link TraceCoreDataHandler#LAUNCHER_COLON} was found in line
	 * 
	 * @param lastWasName
	 */
	public void setLastWasName(boolean lastWasName) {
		this.lastWasName = lastWasName;
	}

	/**
	 * Check if heap type was correct
	 * 
	 * @return <code>true</code> if heap type was correct, <code>false</code>
	 *         otherwise.
	 */
	public boolean isHeapTypeCorrect() {
		return heapTypeCorrect;
	}

	/**
	 * Set heap type as correct
	 * 
	 * @param heapTypeCorrect
	 */
	public void setHeapTypeCorrect(boolean heapTypeCorrect) {
		this.heapTypeCorrect = heapTypeCorrect;
	}

	/**
	 * Set dump traces
	 * 
	 * @param dumpedTraces
	 */
	public void setDumpedTraces(boolean dumpedTraces) {
		this.dumpedTraces = dumpedTraces;
	}

	/**
	 * Check if trace was containing
	 * {@link TraceCoreDataHandler#DUMPED_TC_TRACES} lines
	 * 
	 * @return <code>true</code> if dumped lines was found, <code>false</code>
	 *         otherwise.
	 */
	public boolean isDumpedTraces() {
		return dumpedTraces;
	}

	/**
	 * Returns {@link LauncherErrorType} by its ordinal
	 * 
	 * @param errorCodeAsString
	 *            Error number as string
	 * @return {@link LauncherErrorType} or {@link LauncherErrorType#ACTIVATION}
	 *         if no matching item found.
	 */
	private LauncherErrorType getErrorById(String errorCodeAsString) {

		try {
			int errorCode = Integer.parseInt(errorCodeAsString);
			LauncherErrorType[] values = LauncherErrorType.values();
			for (int i = 0; i < values.length; i++) {
				if (errorCode == values[i].ordinal()) {
					return values[i];
				}
			}
		} catch (NumberFormatException e) {
			// If occurs, it's an internal error, MemSpy S60 side is giving id
			// in wrong format.
			e.printStackTrace();
			MemSpyConsole.getInstance().printStackTrace(e);
		}

		return LauncherErrorType.ACTIVATION;
	}

	/**
	 * Get imported SWMT Heap Dumps
	 * 
	 * @return imported SWMT Heap Dumps
	 */
	public ArrayList<ThreadInfo> getImportedSWMTHeaps() {
		return threadArray;
	}

}
