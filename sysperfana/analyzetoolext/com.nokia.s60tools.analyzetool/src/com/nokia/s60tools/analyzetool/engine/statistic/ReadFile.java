/*
 * Copyright (c) 2008-2010 Nokia Corporation and/or its subsidiary(-ies).
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
 * Description: Definitions for the class ReadFile
 *
 */

package com.nokia.s60tools.analyzetool.engine.statistic;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.AbstractList;

import com.nokia.s60tools.analyzetool.engine.ParseAnalyzeData;

/**
 * Reads thru trace file and generates statistic info.
 * 
 * @author kihe
 * 
 */
public class ReadFile {

	/** Parser. */
	private ParseAnalyzeData parser;

	/**
	 * Constructor.
	 */
	public ReadFile() {
	}

	/**
	 * Reads thru file.
	 * 
	 * @param path
	 *            file location
	 * @return true, if no errors occurs when reading file, otherwise false
	 */
	public boolean readFile(final String path) {
		// return value
		boolean retValue = true;

		// input
		BufferedReader input = null;
		FileInputStream fis = null;

		try {
			// check that file exists
			if (path == null || ("").equals(path)) {
				return false;
			}
			File file = new File(path);
			if (!file.exists()) {
				return false;
			}

			// get input
			fis = new FileInputStream(file);
			input = new BufferedReader(new InputStreamReader(fis, "UTF-8"));
			int linebreakSize = lineBreakSize(file); // important to determine
			// file position for deferred callstack reading

			// get first line of data file
			parser = new ParseAnalyzeData(false, true, true, linebreakSize);
			String line = null;
			// go thru file
			while ((line = input.readLine()) != null) {
				boolean success = parser.parse(line);
				if (!success) {
					return false;
				}
			}
			fis.close();
			input.close();
		} catch (FileNotFoundException ex) {
			retValue = false;
			ex.printStackTrace();
		} catch (OutOfMemoryError oome) {
			retValue = true;
			oome.printStackTrace();
		} catch (IOException ex) {
			retValue = false;
			ex.printStackTrace();
		} finally {
			try {
				if (input != null) {
					input.close();
				}
			} catch (IOException ex) {
				ex.printStackTrace();
			}

			try {
				if (fis != null) {
					fis.close();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return retValue;
	}

	/**
	 * Returns statistic
	 * 
	 * @return Statistic
	 */
	public AbstractList<ProcessInfo> getStatistic() {
		return parser.getStatistic();
	}

	/**
	 * Finish the file reading.
	 */
	public void finish() {
		parser.finish();
	}

	/**
	 * Returns true if callstack reading from file is done on demand; false if
	 * callstacks are made available during parsing phase.
	 * 
	 * @return true for deferred callstack reading
	 */
	public boolean hasDeferredCallstacks() {
		return parser.hasDeferredCallstacks();
	}

	/**
	 * Determines the size of line breaks in the given file. File produced on
	 * the device-side should have while Windows files have.
	 * 
	 * @param aFile
	 *            the file to check
	 * @return 1 or 2 for size of line break, or 0 if it cannot be determined
	 */
	private static int lineBreakSize(File aFile) {
		int ret = 0;
		try {
			BufferedReader br = new BufferedReader(new FileReader(aFile
					.getPath()));

			int ch;
			int cnt = 0;
			while ((ch = br.read()) >= 0) {
				cnt++;
				if (ch == '\r') {
					ret++;
				} else if (ch == '\n') {
					ret++;
					break;
				}
			}
		} catch (IOException e) {
			// do nothing, a return value of 0 will indicate some problem
		}
		return ret;
	}
}
