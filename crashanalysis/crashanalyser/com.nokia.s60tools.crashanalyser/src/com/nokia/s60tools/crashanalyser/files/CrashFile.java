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

import com.nokia.s60tools.crashanalyser.containers.Thread;
import com.nokia.s60tools.crashanalyser.data.*;
import java.io.*;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IEditorInput;

/**
 * This class represents a fully decoded crash file (.crashxml). 
 * It contains crash file's name, path, etc.
 * 
 * Note that this file extends SummaryFile, so all reading
 * is done SummaryFile class.
 * 
 */
public class CrashFile extends SummaryFile implements IEditorInput {
	/**
	 * Constructor
	 * @param filePath file path to this crash file
	 * @param library error library
	 */
	protected CrashFile(String filePath, ErrorLibrary library) {
		super(filePath, library);
	}
	
	/**
	 * Constructor
	 * @param filePath file path to this crash file
	 * @param library error library
	 */
	protected CrashFile(String filePath, ErrorLibrary library, Thread thread) {
		super(filePath, library, thread);
	}

	/**
	 * Returns the file type of this crash file.
	 * @return "Decoded File"
	 */
	public String getFileType() {
		return "Decoded File";
	}
	
	/**
	 * Reads crash file
	 * @param file crash file
	 * @param library error library
	 * @return read crash file
	 */
	public static CrashFile read(File file, ErrorLibrary library) {
		if (file == null || !file.exists() || !file.isFile())
			return null;
		
		CrashFile crashFile = new CrashFile(file.getAbsolutePath(), library);
		crashFile.doRead();
		return crashFile;
	}
	
	/**
	 * Reads crash file
	 * @param folder folder which contains one .crashxml file which will be read
	 * @param library error library
	 * @return read crash file
	 */
	public static CrashFile read(String folder, ErrorLibrary library) {
		String crashFile = findFile(folder, CrashAnalyserFile.OUTPUT_FILE_EXTENSION);
		
		if (crashFile == null)
			return null;
		
		CrashFile file = new CrashFile(crashFile, library);
		file.doRead();
		return file;
	}

	/**
	 * Reads crash file
	 * @param folder folder which contains one .crashxml file which will be read
	 * @param library error library
	 * @return read crash file
	 */
	public static CrashFile read(String folder, ErrorLibrary library, Thread thread) {
		String crashFile = findFile(folder, CrashAnalyserFile.OUTPUT_FILE_EXTENSION);
		
		if (crashFile == null)
			return null;
		
		CrashFile file = new CrashFile(crashFile, library, thread);
		file.doRead();
		return file;
	}
		
	@Override
	public ImageDescriptor getImageDescriptor() {
		return null;
	}

	@Override
	public String getToolTipText() {
		return "Crash Analyser File"; 
	}
}
