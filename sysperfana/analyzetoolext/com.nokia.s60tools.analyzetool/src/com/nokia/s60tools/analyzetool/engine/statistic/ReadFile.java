/*
 * Copyright (c) 2008-2009 Nokia Corporation and/or its subsidiary(-ies).
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

package com.nokia.s60tools.analyzetool.engine.statistic;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.AbstractList;

import com.nokia.s60tools.analyzetool.engine.ParseAnalyzeData;

/**
 * Reads thru trace file and generates statistic info
 * @author kihe
 *
 */
public class ReadFile {

	/** Parser */
	ParseAnalyzeData parser;

	/**
	 * Constructor
	 */
	public ReadFile()
	{
		parser = new ParseAnalyzeData(false, true);
	}


	/**
	 * Reads thru file
	 * @param path File location
	 * @return True if no errors occurs when reading file otherwise False
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
			java.io.File file = new java.io.File(path);
			if (!file.exists()) {
				return false;
			}

			// get input
			fis = new FileInputStream(file);
			input = new BufferedReader(new InputStreamReader(fis,"UTF-8"));

			// get first line of data file
			String line = null;
			// go thru file
			while ((line = input.readLine()) != null) {
				parser.parse(line);
			}
			fis.close();
			input.close();
		} catch (FileNotFoundException ex) {
			retValue = false;
			ex.printStackTrace();
		} catch (OutOfMemoryError oome){
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
				if( fis != null ) {
					fis.close();
				}
			}catch(Exception e) {
				e.printStackTrace();
			}
		}
		return retValue;
	}

	/**
	 * Returns statistic
	 * @return Statistic
	 */
	public AbstractList<ProcessInfo> getStatistic()
	{
		return parser.getStatistic();
	}

	/**
	 * Finish the file reading.
	 */
	public void finish()
	{
		parser.finish();

	}
}
