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


import java.io.File;
import java.util.Date;

import com.nokia.s60tools.memspy.files.HeapDumpFile;
import com.nokia.s60tools.memspy.files.SWMTFile;

/**
 * MemSpyBundle class bundles up one folder under MemSpy plugin's folder. I.e. one 
 * MemSpyFileBundle is one row in MainView. A Bundle can contain an Heap Dump file or
 * 
 * SWMT-log file. All of the file listed above are found e.g. from
 * c:\my_carbide_workspace\.metadata\.plugins\com.nokia.s60tools.memspy\ImportedFiles[bundle folder]
 * 
 * Bundle can also be a waiting bundle, so that 'Loading files. Please wait' row can be shown in MainView.
 *
 */
public class MemSpyFileBundle {

	public static final int INDEX_FILE_TYPE = 0;
	public static final int INDEX_FILE_NAME = 1;
	public static final int INDEX_TIME = 2;

	
	/* 
	 * Heap Dump file
	 */
	private HeapDumpFile heapDumpFile = null;
	
	/*
	 * SWMT-log file
	 */
	private SWMTFile swmtFile = null; 
		
	/**
	 * Bundle name for an empty or waiting bundle  
	 */
	private String bundleName = "";
	
	/**
	 * Folder where all bundle's files can be found from
	 */
	private String bundleFolder = "";
	
	/**
	 * If true, bundle is an empty or waiting bundle
	 */
	private boolean emptyFile = false;
	
	/**
	 * Used for creating an empty or waiting bundle
	 * @param empty If <code>true</code>, an empty bundle is created. If <code>false</code>, a waiting bundle is created. 
	 */
	public MemSpyFileBundle(boolean empty) {
		if (empty)
			bundleName = "No Crash Files Found.";
		else
			bundleName = "Loading files. Please wait.";
		emptyFile = true;
	}
	
	/**
	 * Creates a bundle from folder
	 * @param folder Bundle folder. Bundle's file will be read from here.
	 */
	public MemSpyFileBundle( String folder ) {
		bundleFolder = MemSpyFileOperations.addSlashToEnd(folder);
		
		heapDumpFile = HeapDumpFile.read( folder );
		swmtFile = SWMTFile.read( folder );
		
	}
	
	/**
	 * Create dummy bundle.
	 * @param folder where files are searched.
	 * @return returns new bundle.
	 */
	public static MemSpyFileBundle createDummyBundle(String folder) {
		return new MemSpyFileBundle(folder);
	}
	
	/**
	 * MainView can use this to get description for each column in the grid
	 * @param index index of the column
	 * @return value for asked column
	 */
	public String getText(int index) {
		String retval = "";
		switch (index) {
			case INDEX_TIME:
				retval = this.getTime();
				break;
				
			case INDEX_FILE_NAME:
				retval = this.getFileName();
				break;
			
			case INDEX_FILE_TYPE:
				retval = this.getFileType();
				break;
				
			default:
				break;
		}
		
		return retval;
	}
	
	/**
	 * Get file type.
	 * @return type of file.
	 */
	public String getFileType() {
		if (emptyFile)
			return bundleName;
		
		String retval = "";
		
		if ( this.heapDumpFile != null) {
			retval = heapDumpFile.getFileType();
		}
		
		if( swmtFile != null ){
			retval = this.swmtFile.getFileType();
		}
		
		return retval;
	}
	
	
	/**
	 * Returns the file name for this bundle. File name depends on what types
	 * of files this bundle contains (or if this bundle is an empty or waiting bundle).
	 *   
	 * @return the file name for this bundle.
	 */
	public String getFileName() {
		if (emptyFile)
			return bundleName;
		
		String retval = "";
		
		if ( this.heapDumpFile != null) {
			retval = heapDumpFile.getFileName();
		}
		else if( swmtFile != null ){
			retval = this.swmtFile.getFileName();
		}
		
		return retval;
	}
	
	/**
	 * Get the Cycle number from FileName.
	 * E.g. if file name is: "MemSpy SWMT-log 12.08.2009 08-40-22 Cycle 3" 
	 * will return '3'. 
	 *   
	 * @return the cycle number from file name for this bundle, or -1 if can't found.
	 */
	public int getCycleNumberFromFileName() {
		int retval = -1;
		if (emptyFile){
			return retval;
		}
				
		String fileName = "";
		
		if ( this.heapDumpFile != null) {
			fileName = heapDumpFile.getFileName();
		}
		else if( swmtFile != null ){
			fileName = this.swmtFile.getFileName();
		}
		
		if(fileName != null && fileName.indexOf(MemSpyFileOperations.CYCLE) != -1){
			int index = fileName.indexOf(MemSpyFileOperations.CYCLE);
			String cycle = fileName.substring((index + MemSpyFileOperations.CYCLE.length())).trim();
			cycle = cycle.substring(0, cycle.indexOf('.'));
			try {
				retval = Integer.parseInt(cycle);
			} catch (NumberFormatException e) {
				e.printStackTrace();
				retval = -1;
			}
		}
		
		return retval;
	}
	
	/**  
	 * Get Time
	 * @return time of file creation.
	 */
	public String getTime() {
		if (emptyFile)
			return "";
		String retval = "";

		if( heapDumpFile != null ){
			retval = this.heapDumpFile.getTime();
		}		
		else if( swmtFile != null ){
			retval = this.swmtFile.getTime();
		}
	
	
		return retval;
	}
	
	/**
	 * Get time as long
	 * @return time of file creation as long.
	 */
	public long getTimeAsLong(){
		long time = -1;
		Date date = null;
		if( heapDumpFile != null ){
			date = this.heapDumpFile.getDateTime();
		}		
		else if( swmtFile != null ){
			date = this.swmtFile.getDateTime();
		}
		
		if(date != null){
			time = date.getTime();
		}
		
		return time;
	}
	
	
	/**
	 * Get XML file.
	 * @return XML-files path
	 */
	public String getXMLFilePath() {
		if( heapDumpFile != null ){
			return this.heapDumpFile.getXmlPath();
		}
		else{
			return null;
		}
	}
	
	/**
	 * Get file path.
	 * @return Files complete path.
	 */
	public String getFilePath(){
		if (emptyFile)
			return "";
		String retval = "";

		if( heapDumpFile != null ){
			retval = this.heapDumpFile.getFilePath();
		}
		
		if( swmtFile != null ){
			retval = this.swmtFile.getFilePath();
		}
	
	
		return retval;
	}
	
	
	/**
	 * Returns whether this is an empty or waiting bundle.
	 * @return true if bundle is empty or waiting, false if not.
	 */
	public boolean isEmpty() {
		return emptyFile;
	}
	
	
	
	/**
	 * Returns whether this bundle contains any files.
	 * @return true if bundle contains files, false if not
	 */
	public boolean hasFiles() {
		if ( heapDumpFile != null || swmtFile != null )
			return true;
		
		return false;
	}
	
	
	
	/**
	 * Tests whether this bundle still exists in the drive.
	 * If bundle is empty or waiting, true is always returned. 
	 * 
	 * @return <code>true</code> if bundle exists, <code>false</code> if not.
	 */
	public boolean exists() {
		if (isEmpty())
			return true;
		
		try {
			File f = new File(bundleFolder);
			if (f.isDirectory() && f.exists())
				return true;
		} catch (Exception e) {
			return false;
		}
		
		return false;
	}
	
	
	
	/**
	 * Returns this bundle's folder
	 * @return bundle's folder or empty
	 */
	protected String getBundleFolder() {
		return bundleFolder;
	}
	
	
	/**
	 * Has this bundle a SWMT log file.
	 * @return <code>true</code> if this bundle has SWMT-log <code>false</code> otherwise.
	 */
	public boolean hasSWMTLogFile(){
		if( swmtFile != null ){
			return true;
		}
		return false;
	}
	
	/**
	 * Has this bundle a heap dump file
	 * @return <code>true</code> if this bundle has Heap Dump <code>false</code> otherwise.
	 */
	public boolean hasHeapDumpFile(){
		
		if( heapDumpFile != null ){
			return true;
			
		}
		return false;

	}
	
	/**
	 * Deletes this bundle. I.e deletes all files under this
	 * bundle's folder and finally deletes the bundle folder.
	 */
	public boolean delete() {
		if (!"".equals(bundleFolder)) {
			return !MemSpyFileOperations.deleteDir( new File( bundleFolder ) );
		}
		else{
			return false;
		}
		
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */	
	public String toString(){
		if(swmtFile != null){
			return swmtFile.getFilePath();
		}
		else{
			return null;
		}
	}
	
	/**
	 * Checks if bundles are equal. Two bundles are equal if
	 * their bundleFolder is the same.
	 */
	public boolean equals(Object other) {
		if (this == other)
			return true;
		
		if (!(other instanceof MemSpyFileBundle))
			return false;
		
		MemSpyFileBundle othr = (MemSpyFileBundle)other;
		if (bundleFolder.compareToIgnoreCase(othr.getBundleFolder()) == 0)
			return true;
		return false;
	}
	
	
	
}
