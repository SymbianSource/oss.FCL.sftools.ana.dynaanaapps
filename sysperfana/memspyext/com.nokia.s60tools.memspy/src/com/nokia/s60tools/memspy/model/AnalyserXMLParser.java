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
import java.util.ArrayList;


/**
 * class AnalyserXMLParser.
 * class that parses symbol and thread name information from debug configuration file.
 *
 */

public class AnalyserXMLParser {
	
	static final String DEBUG_META_DATA_START_TAG 	= "<category name=\"debug_meta_data\">";
	static final String DEBUG_META_DATA_END_TAG 	= "</category>";
	static final String THREAD_TAG 					= "<command name=\"thread\">";
	static final String FILE_NAME 					= "file name";
	static final String DIRECTORY_NAME				= "directory name";

	
	/**
	 * Add wanted lines from given input to XML generator and returns the generator.
	 * @param reader
	 * @return XML generator
	 */
	public static AnalyserXMLGenerator parseXML( BufferedReader reader ){
		AnalyserXMLGenerator retVal = new AnalyserXMLGenerator();
		
		boolean readingDebugMetaData = false;
		ArrayList<String> debugMetaDataFiles = new ArrayList<String>();
		String debugMetaDataDirectory = null;
		String threadName = "";
		
		try{
			String line = "";
			
			// Go thru file in loop and search for lines that contain debugMetaData -text. 
			// If text is found, save meta data file information into AnalyserXMLGenerator-object.
			
			while( reader.ready() ){

				line = reader.readLine();
				
				if( line == null){
					break;
				}
				
				// Search for debug meta data start and end tags.
				if( line.contains(DEBUG_META_DATA_START_TAG) ){
					readingDebugMetaData = true;
				}
				else if( line.contains(DEBUG_META_DATA_END_TAG) ){
					readingDebugMetaData = false;
				}
				
				// if between debug meta data start and end tags, search for debug meta data info.
				if( readingDebugMetaData ){
					if(line.contains(FILE_NAME)){
						debugMetaDataFiles.add( getMetaDataInfo(line) );
					}
					if(line.contains(DIRECTORY_NAME)){
						debugMetaDataDirectory = getMetaDataInfo(line);
					}
				}
				
				// search for thread name tag.
				if( line.contains(THREAD_TAG) ){
					threadName = getThreadName(line);
				}
			}
			
		}
		catch (Exception e){
			e.printStackTrace();
			return null; 		
		}
		
		if(threadName.equals("") ){
			return null;
		}
		
		// check that thread name and at least one symbol file was found.
		if( debugMetaDataFiles.size() != 0 ){
			// set found data into AnalyserXMLGenerator-object.
			retVal.setXMLDebugMetaDataFile(debugMetaDataFiles.toArray(new String [debugMetaDataFiles.size()]));			
		}
		
		if( !threadName.equals("")){
			retVal.setXMLDebugMetaDataDirectory(debugMetaDataDirectory);
		}
		retVal.setXMLThreadName(threadName);
		
		return retVal;
		
	}
	
	/**
	 * getMetaDataInfo.
	 * @param line String that is parsed
	 * @return string between quotation marks
	 */
	private static String getMetaDataInfo( String line ) {
		int firstIndex = line.indexOf("\"") + 1 ;
		int secondIndex = line.indexOf(("\""), firstIndex );
		return line.substring(firstIndex, secondIndex);
	}

	
	/**
	 * getThreadName.
	 * @param line String that is parsed
	 * @return string between threas tags
	 */
	private static String getThreadName(String line){
		int firstIndex = line.indexOf("=\"thread\">") + 10;
		int secondIndex = line.indexOf("</command>"); 
		return line.substring(firstIndex, secondIndex);
	}
	
}
