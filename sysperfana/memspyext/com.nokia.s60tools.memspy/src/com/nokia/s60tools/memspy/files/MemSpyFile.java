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

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import org.xml.sax.helpers.DefaultHandler;

import com.nokia.s60tools.memspy.model.MemSpyFileOperations;

/**
 * This is a base class for all MemSpy file types. 
 *
 */
public class MemSpyFile extends DefaultHandler{

	/* files path */
	protected String filePath = "";
	
	/* files name */
	protected String fileName = "";
	
	/* time code from file */
	protected String time = "";

	/* date time code from file */
	private Date dateTime = null;
	
	/* type of the file  */
	protected String fileType = "";
	
	/**
	 * MemSpyFile.
	 * constructor
	 * @param filePath Path of file
	 */
	public MemSpyFile( String filePath ){
		this.filePath = filePath;
	}
	
	/**
	 * doRead.
	 * Read's file variables from file that is defined in filePath
	 */
	public void doRead(){
		File f = new File(filePath);
		
		//if file exists
		if (f.exists() && f.isFile()) {
			
			//Get date
			Date date = new Date(f.lastModified());
			setDateTime(date);
			SimpleDateFormat formatter = new SimpleDateFormat ( MemSpyFileOperations.DATEFORMAT );
			time = formatter.format(date);
			
			// get File name
			fileName = f.getName();
		} else {
			fileName = "";
		}
	}
	
	/**
	 * findFiles.
	 * Finds all files with defined extension.
	 * @param folder folder where from files are searched
	 * @param extension file extension
	 * @return arraylist that contains found file names
	 */
	protected static ArrayList<String> findFiles(String folder, String extension) {
		
		ArrayList<String> retVal = new ArrayList<String>();
		if ( !extension.startsWith(".") ){
			extension = "." + extension;
		}
		File cFolder = new File(folder);
		if (cFolder.isDirectory() && cFolder.exists()) {
			File[] files = cFolder.listFiles();
			if (files != null) {
				for (int i = 0; i < files.length; i++) {
					File file = files[i];
					if (file.getName().endsWith(extension)) {
						retVal.add( file.getAbsolutePath() );
					}
				}
			}
		}
		
		return retVal;
	}
	
	/**
	 * findFile
	 * Finds file with defined extension.
	 * @param folder folder where file is searched
	 * @param extension file extension
	 * @return fileName
	 */
	protected static String findFile(String folder, String extension) {
		if ( !extension.startsWith(".") ){
			extension = "." + extension;
		}
		File cFolder = new File(folder);
		if (cFolder.isDirectory() && cFolder.exists()) {
			File[] files = cFolder.listFiles();
			if (files != null) {
				for (int i = 0; i < files.length; i++) {
					File file = files[i];
					if (file.getName().endsWith(extension)) {
						return file.getAbsolutePath();
					}
				}
			}
		}
		
		return null;
	}
	
	
	
	/**
	 * Get file name
	 * @return file name
	 */
	public String getFileName() {
		return fileName;
	}
	/**
	 * Set file name
	 * @param fileName
	 */
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
	/**
	 * Get time when log file was created
	 * @return time
	 */
	public String getTime() {
		return time;
	}
	/**
	 * Set time when log file was created
	 * @param time
	 */
	public void setTime(String time) {
		this.time = time;
	}
	/**
	 * Get type of log file
	 * @return type
	 */
	public String getFileType() {
		return fileType;
	}
	/**
	 * Set type of log file
	 * @param fileType
	 */
	public void setFileType(String fileType) {
		this.fileType = fileType;
	}

	/**
	 * Get absolute file path
	 * @return file path
	 */
	public String getFilePath() {
		return filePath;
	}

	/**
	 * Set date time
	 * @param dateTime the dateTime to set
	 */
	private void setDateTime(Date dateTime) {
		this.dateTime = dateTime;
	}

	/**
	 * Get date time read from file
	 * @return the dateTime
	 */
	public Date getDateTime() {
		return dateTime;
	} 
	
	

}
