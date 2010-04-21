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

/**
 * class HeapDumpFile.
 * This class represents one Heap Dump.
 */

public class HeapDumpFile extends MemSpyFile {
	
	/* path of XML-file*/
	private String xmlPath;
	
	/**
	 * HeapDumpFile.
	 * constructor
	 * @param filePath files path
	 * @param xmlPath xml files path
	 */
	public HeapDumpFile( String filePath, String xmlPath ) {
		super(filePath);
		this.xmlPath = xmlPath;
	}

	/**
	 * Read's file variables from file that is defined in filePath
	 * @param folder folder where files are searched
	 * @return New HeapDumpFile
	 */
	static public HeapDumpFile read( String folder ){
		
		String xmlPath = findFile(folder, "xml");
		String heapPath = findFile(folder, "txt");
		
		if ( xmlPath == null || heapPath == null  ){
			return null;	
		}
		HeapDumpFile heapFile = new HeapDumpFile( heapPath, xmlPath );
		heapFile.doRead();
		heapFile.setFileType("Heap Dump");
		return heapFile;
	}

	/**
	 * Get XML file path
	 * @return path of xml-file
	 */
	public String getXmlPath() {
		return xmlPath;
	}
	
	
}
