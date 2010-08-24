/*
 * Copyright (c) 2010 Nokia Corporation and/or its subsidiary(-ies).
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
 * Description:  Definitions for the class ReadFile
 *
 */
package com.nokia.s60tools.analyzetool.engine;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.nokia.s60tools.analyzetool.engine.statistic.AllocCallstack;
import com.nokia.s60tools.analyzetool.engine.statistic.BaseInfo;
import com.nokia.s60tools.analyzetool.engine.statistic.ProcessInfo;

/**
 * CallstackManager for reading of callstacks on demand by accessing the .dat
 * file after the initial parsing of the file has completed.
 * 
 */
public class DeferredCallstackManager implements ICallstackManager {

	/**
	 * Max number of lines to parse before giving up and forcing the callstack
	 * as complete. This helps to deal with corrupt files.
	 */
	private static final int BAIL_OUT_LIMIT = 100;

	/** Location of the .dat file */
	String fileLocation;

	/**
	 * list of processes for this file; this has to contain all DllLoads for
	 * each process
	 */
	private Map<Integer, ProcessInfo> processMap;

	/**
	 * Constructor
	 * 
	 * @param fileLocation
	 *            full path of the file to read callstacks from
	 */
	public DeferredCallstackManager(String fileLocation) {
		this.fileLocation = fileLocation;
	}

	/**
	 * Setter for all processes valid for this data file. This has to contain
	 * all DllInfo information for each of the processes
	 * 
	 * @param processes
	 *            List of processes for the file
	 */
	public void setProcesses(AbstractList<ProcessInfo> processes) {
		// make a copy
		if (processMap == null) {
			processMap = new HashMap<Integer, ProcessInfo>();
			for (ProcessInfo processInfo : processes) {
				processMap.put(processInfo.getProcessID(), processInfo);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seecom.nokia.s60tools.analyzetool.engine.statistic.ICallstackManager#
	 * readCallstack(com.nokia.s60tools.analyzetool.engine.statistic.BaseInfo)
	 */
	public List<AllocCallstack> readCallstack(BaseInfo baseInfo)
			throws IOException {
		// Returns a fully resolved callstack file in .dat format.
		// The baseInfo's file position points to the beginning of a record
		// where the callstack information starts

		if (!hasCallstack(baseInfo)) {
			return null;
		}

		ProcessInfo p = processMap.get(baseInfo.getProcessID());

		if (p == null) {
			// no matching process found
			return new ArrayList<AllocCallstack>();
		}

		RandomAccessFile fileReader = null;
		List<AllocCallstack> ret = null;

		try {
			// Open .dat file for reading
			fileReader = new RandomAccessFile(new File(fileLocation), "r"); //$NON-NLS-1$

			// Parsing the ELF header to read the Program Header Offset
			// 'e_phoff' value
			fileReader.seek(baseInfo.getFilePos());

			CallstackDataParser parser = new CallstackDataParser(baseInfo, p);
			int lineCnt = 0;
			String line;
			while ((line = fileReader.readLine()) != null) {
				if (parser.parseLine(line)) {
					parser.finaliseCallstack();
					ret = parser.getCallstack();
					break;
				}

				// let's count the lines we parse and bail out after <n> lines
				// in case the file is corrupt
				lineCnt++;
				if (lineCnt == BAIL_OUT_LIMIT) {
					parser.forceComplete();
					ret = parser.getCallstack();
					break;
				}
			}
		} finally {
			if (fileReader != null) {
				fileReader.close();
			}
		}
		return ret == null ? new ArrayList<AllocCallstack>() : ret;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seecom.nokia.s60tools.analyzetool.engine.statistic.ICallstackManager#
	 * hasCallstack(com.nokia.s60tools.analyzetool.engine.statistic.BaseInfo)
	 */
	public boolean hasCallstack(BaseInfo baseInfo) {
		return baseInfo.getFilePos() > -1;
	}
}
