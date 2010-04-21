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



package com.nokia.s60tools.memspy.files;

import java.util.ArrayList;

/**
 * class SWMTFile.
 * This class represents one System Wide Memory Tracking log.
 */

public class SWMTFile extends MemSpyFile {



	/**
	 * SWMTFile.
	 * Constructor
	 * @param filePath path of file
	 */
	private SWMTFile(String filePath) {
		super(filePath);
	}
	
	/**
	 * read.
	 * Reads searches swmt-file from given folder
	 * @param folder
	 * @return new SWMTFile
	 */
	static public SWMTFile read( String folder ){
	
		ArrayList<String> filePaths = findFiles(folder, "txt");
		String xmlFile = findFile(folder, "xml");

		
		if ( filePaths.size() == 0 || xmlFile != null ){
			return null;
		}
		SWMTFile swmtFile = new SWMTFile( filePaths.get(0) );
		swmtFile.doRead();
		swmtFile.setFileType("SWMT-Log");
		return swmtFile;
	}
	

}
