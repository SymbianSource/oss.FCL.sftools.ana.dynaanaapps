/*
* Copyright (c) 2008 Nokia Corporation and/or its subsidiary(-ies). 
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

package com.nokia.s60tools.crashanalyser.files;

import java.io.File;
import java.util.Date;
import java.util.List;

import org.xml.sax.helpers.DefaultHandler;
import com.nokia.s60tools.crashanalyser.data.*;
import com.nokia.s60tools.crashanalyser.containers.Thread;
import java.text.DateFormat;

/**
 * This is a base class for all Crash Analyser file types. 
 *
 */
public abstract class CrashAnalyserFile extends DefaultHandler {

	// File extensions
	public static final String OUTPUT_FILE_EXTENSION = "crashxml";
	public static final String MOBILECRASH_FILE_EXTENSION = "bin";
	public static final String D_EXC_FILE_EXTENSION = "txt";
	public static final String TRACE_EXTENSION = "trace";
	public static final String ELF_CORE_DUMP_FILE_EXTENSION = "elf";
	public static final String SUMMARY_FILE_EXTENSION = "xml";
	public static final String EMULATOR_PANIC_EXTENSION = "panicxml";
	
	// base data for all crash files
	protected String filePath = "";
	protected String time = "";
	protected String threadName = "";
	protected String panicCategory = "";
	protected String fileName = "";
	protected String created = "";
	protected String description = "";
	protected String shortDescription = "";
	protected String romId = "";
	protected String panicCode = "";
	protected int totalThreadCount = -1;
	protected int processCount = -1;

	protected ErrorLibrary errorLibrary;

	// Thread if this is for thread information only.
	protected Thread threadInfo = null;

	/**
	 * Constructor
	 * @param crashFilePath crash file path
	 * @param library error library
	 */
	protected CrashAnalyserFile(String crashFilePath, ErrorLibrary library) {
		filePath = crashFilePath;
		errorLibrary = library;
	}
	
	public abstract String getFileType(); 
	
	public String getTime() {
		return time;
	}
	
	public String getThreadName() {
		return threadName;
	}
	
	public int getTotalThreadCount() {
		return totalThreadCount;
	}
	
	public abstract List<Thread> getThreads();
	
	public int getProcessCount() {
		return processCount;
	}

	public String getPanicCategory() {
		return panicCategory;
	}
	
	public String getFileName() {
		return fileName;
	}
	
	public String getPanicCode() {
		return panicCode;
	}
	
	public String getCreated() {
		return created;
	}
	
	public String getDescription() {
		return description;
	}
	
	public String getShortDescription() {
		return shortDescription;
	}
	
	public String getRomId() {
		return romId;
	}
	
	public String getFilePath() {
		return filePath; 
	}
	
	public ErrorLibrary getErrorLibrary() {
		return errorLibrary;
	}
	
	public Thread getThread() {
		return threadInfo;
	}

	/**
	 * Read file name and last modified time
	 */
	protected void doRead() {
		File f = new File(filePath);
		if (f.exists() && f.isFile()) {
			Date d = new Date(f.lastModified());
			created = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.MEDIUM).format(d);
			fileName = f.getName();
		} else {
			filePath = "";
		}
	}
	
	/**
	 * Returns the absolute path for a crash file. Given folder can only
	 * contain one crash file of given extension.
	 * @param folder from where the file is searched
	 * @param extension extension of the file
	 * @return absolute path for a wanted crash file.
	 */
	protected static String findFile(String folder, String extension) {
		String ext = extension;
		if (!extension.startsWith("."))
			ext = "." + extension;
		
		File cFolder = new File(folder);
		// given folder is directory and is exists
		if (cFolder.isDirectory() && cFolder.exists()) {
			File[] files = cFolder.listFiles();
			if (files != null) {
				// go through all files
				for (int i = 0; i < files.length; i++) {
					File file = files[i];
					// return the file which has the given extension
					if (file.getName().endsWith(ext)) {
						return file.getAbsolutePath();
					}
				}
			}
		}
		
		return null;
	}
}
