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

import com.nokia.s60tools.crashanalyser.data.*;

/**
 * This class represent an undecoded crash file such as MobileCrash.bin 
 *
 */
public class UndecodedFile extends CrashAnalyserFile {

	/**
	 * Constructor
	 * @param filePath file path to this crash file
	 * @param library error library
	 */
	protected UndecodedFile(String filePath, ErrorLibrary library) {
		super(filePath, library);
	}
	
	/**
	 * Returns the file type of this crash file.
	 * @return E.g "MobileCrash", "D_EXC",
	 */
	public String getFileType() {
		if (fileName.endsWith("."+CrashAnalyserFile.MOBILECRASH_FILE_EXTENSION) ||
			fileName.endsWith("."+CrashAnalyserFile.TRACE_EXTENSION))
			return "MobileCrash";
		else
			return "D_EXC";
	}
	
	/**
	 * Reads crash file
	 * @param folder folder which contains one binary crash file which will be read
	 * @return read crash file
	 */
	public static UndecodedFile read(String folder) {
		String undecodedFile = findFile(folder, CrashAnalyserFile.MOBILECRASH_FILE_EXTENSION);
		if (undecodedFile == null) {
			undecodedFile = findFile(folder, CrashAnalyserFile.D_EXC_FILE_EXTENSION);
		}
		if (undecodedFile == null) {
			undecodedFile = findFile(folder, CrashAnalyserFile.TRACE_EXTENSION);
		}
		
//		if (undecodedFile == null) {
//			undecodedFile = findFile(folder, CrashAnalyserFile.ELF_CORE_DUMP_FILE_EXTENSION); // core dump file will be support in later version
//		}
		
		// undecoded file doesn't exist
		if (undecodedFile == null)
			return null;
		
		UndecodedFile file = new UndecodedFile(undecodedFile, null);
		file.doRead();
		return file;
	}
}
