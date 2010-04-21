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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Date;

import com.nokia.s60tools.memspy.containers.ThreadInfo;
import com.nokia.s60tools.memspy.containers.ThreadInfo.HeapDumpType;

/**
 * Heap Dump parser that is used when parsing heap dump file.
 */
public class MemSpyLogParserEngine {

	// Writer that writes into file
	private PrintWriter plainOutput;

	private boolean fileOpen;

	/**
	 * 
	 * Checks that at least one heap info with binary data information is found from file given as parameter.
	 * Function also saves thread names from heap dump file into ArrayList what
	 * was given as parameter threadNames.
	 * 
	 * @param file
	 * @param threadNames
	 *            Arraylist where thread names are saved.
	 * @return true if threads containing binary data information were found from this file.
	 */
	public boolean isFileHeapDumpFile(File file,
			ArrayList<ThreadInfo> threadNames) {

		boolean heapFound = false;
		boolean heapDataFound = false;
		boolean heapDataErrorFound = false;

		try {
			String heapInfo = "HEAP INFO FOR THREAD";
			String heapData = "Heap Data";
			String heapDataError = "Heap error";

			BufferedReader reader = new BufferedReader(new FileReader(file));
			String line = "";

			// Go thru file in loop and search for lines that contain HEAP
			// INFO-text. If text is found, save thread names into array list
			// threadNames. If the thread doesn't contain any binary heap data information,
			// the thread is removed from this array list.

			while (reader.ready()) {
				line = reader.readLine();
				if (line.contains(heapInfo)) {

					if ((heapDataFound && heapDataErrorFound) || !heapDataFound) {
						if (threadNames.size() > 0) {
							threadNames.remove(threadNames.size() - 1);
						}
					}

					heapDataFound = false;
					heapDataErrorFound = false;

					if (line.contains("'")) {
						String name = line.substring(line.indexOf("'") + 1);
						if (name.contains("'")) {
							String thread = name
									.substring(0, name.indexOf("'"));
							ThreadInfo threadInfo = new ThreadInfo();
							threadInfo.setThreadName(thread);
							Date date = new Date();
							threadInfo.setDate(date);
							threadInfo.setType(HeapDumpType.FILE);
							String fName = MemSpyFileOperations
									.getFileNameForTempHeapDump(thread, date);
							threadInfo.setThreadFilePath(fName);
							threadNames.add(threadInfo);

							if (fileOpen) {
								plainOutput.flush();
								plainOutput.close();
							}
							if (!this.openFile(fName)) {
								return false;
							}
						}
					}
				}

				// Checking if the line contains binary heap data header
				if (line.contains(heapData)) {
					heapDataFound = true;
				}

				// Checking if the line contains binary heap data error
				if (line.contains(heapDataError)) {
					heapDataErrorFound = true;
				}

				if (fileOpen) {
					plainOutput.write(line + "\n");
				}
			}

			// Check for the last thread in file, because if the last thread is found and if it
			// contains heap data with error or no heap data at all, it wouldn't be removed
			// so it needs to be done here
			if ((heapDataFound && heapDataErrorFound) || !heapDataFound) {
				if (threadNames.size() > 0) {
					threadNames.remove(threadNames.size() - 1);
				}
			}

			if (threadNames.size() > 0) {
				heapFound = true;
			} else {
				heapFound = false;
			}

			if (fileOpen) {
				plainOutput.close();
				fileOpen = false;
			}
		} catch (Exception e) {
			return false;
		}
		return heapFound;
	}

	/**
	 * openFile creates new print writer into given path and saves it into
	 * member variable plainOutput
	 * 
	 * @param fName
	 *            file name
	 * @return true if file operation was successful
	 */
	private boolean openFile(String fName) {
		// If file needs to be opened, open it.

		try {
			plainOutput = new PrintWriter(new FileWriter(fName));
		} catch (IOException e) {
			return false;
		}
		this.fileOpen = true;
		return true;
	}

	/**
	 * Check if this file is a SWMT log file
	 * 
	 * @param file
	 * @return <code>true</code> if file contains a SWMT tag:
	 *         {@link TraceCoreDataHandler#LAUNCHER_SWMT_LOG_START}
	 *         <code>false</code> otherwise.
	 */
	static public boolean isFileSWMTLog(File file) {
		try {
			String SWMTTag = TraceCoreDataHandler.LAUNCHER_SWMT_LOG_START;
			BufferedReader reader = new BufferedReader(new FileReader(file));
			String line = "";
			int count = 0;

			// Go thru file in loop and check if first line of file contains
			// swmt-tag.

			while (reader.ready() && count < 200) {
				line = reader.readLine();
				count++;

				// if line contains tag, conclude that file is swmt-log
				if (line.contains(SWMTTag)) {
					return true;
				}

				// if line is other than empty, conclude that file is not
				// swmt-log
				if (line.length() > 0) {
					return false;
				}
			}

		} catch (Exception e) {
			return false;
		}

		// if first 200 lines were empty, conclude that file is not SWMT-log
		return false;
	}
}
