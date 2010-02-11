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

package com.nokia.s60tools.crashanalyser.model;

import com.nokia.s60tools.crashanalyser.data.*;
import java.util.*;

/**
 * This class is used to save data entered in Wizard pages. This is
 * then passed to DecodingEngine so that it knows what to process.
 *
 */
public class DecodingData {

	/**
	 * Constructor
	 */
	public DecodingData() {		
		// No implementation needed
	}
	
	/**
	 * Decoder Engine contains a list of files which user selected with
	 * Wizard. However, user might only want to actually decode couple of those
	 * files. So this index list contains indexes of those files which are to be
	 * decoded. 
	 */
	public List<Integer> crashFileIndexes = null;
	
	/**
	 * Contains list of provided symbol file locations or null
	 * if no symbol files were provided.
	 */
	public String[] symbolFiles = null;
	
	/**
	 * path to mapfiles.zip or empty if not provided
	 */
	public String mapFilesZip = "";

	/**
	 * path to map files or empty if not provided
	 */
	public String mapFilesFolder = "";
	
	/**
	 * Contains list of provided image file locations or null
	 * if no symbol files were provided.
	 */
	public String[] imageFiles = null;
	
	/**
	 * Whether an html file should be generated from input files
	 */
	public boolean html = false;
	
	/**
	 * Whether a text file should be generated from input files
	 */
	public boolean text = false;
	
	/**
	 * Folder where html and text files are stored. If empty is passed
	 * (and html and/or text is true), input file's folder will be used.
	 * If null is passed, nothing is created even if html or text would be true.
	 */
	public String htmlTextOutputFolder = "";
	
	/**
	 * Defines whether we are importing new files or we are re-decoding
	 * existing files
	 */
	public boolean importingFiles = true;
	
	/**
	 * Error Library
	 */
	public ErrorLibrary errorLibrary = null;
	
}
