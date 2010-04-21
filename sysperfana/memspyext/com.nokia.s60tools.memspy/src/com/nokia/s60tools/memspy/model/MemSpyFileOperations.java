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
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Platform;

import com.nokia.s60tools.memspy.plugin.MemSpyPlugin;

/**
 * Utilities for handling MemSpy specific file operation
 */
public class MemSpyFileOperations {

	private static final String TEMP = "\\Temp";

	/**
	 * Cycle
	 */
	public static final String CYCLE = "Cycle";

	/**
	 * Date format "dd.MM.yyyy HH:mm:ss"
	 */
	public final static String  DATEFORMAT					= "dd.MM.yyyy HH:mm:ss";
	
	private final static String  SWMT_LOG_TEXT				= "MemSpy SWMT-log";
	private final static String  CONFIGURATION_DIRECTORY_NAME 	= "ConfigurationFiles";
	private final static String  IMPORTED_DIRECTORY_NAME 	= "ImportedData";


	/**
	 * Generates file name for new temporary heap dump file based on threads name and current time
	 * @param threadName Name of the thread
	 * @param date date when log has been imported
	 * @return new file name
	 */
	public static String getFileNameForTempHeapDump( String threadName, Date date){
		
		
		// Format the current time.
		SimpleDateFormat formatter = new SimpleDateFormat ( DATEFORMAT );
		String dateString = formatter.format(date);
		String threadFileName = threadName + " " + dateString + ".txt" ;
		
		// replace :'s with -'s as :'s cannot be on file names.
		threadFileName = threadFileName.replace(':', '-');
		
		//String folder = Platform.getConfigurationLocation().getURL().getFile() + "memspy\\Temp";
		String folder = MemSpyFileOperations.getPluginWorkingLocation() + TEMP;
		
		// create directory if needed
		File file1 = new File(folder);
		if( !file1.exists() ){
			if( !file1.mkdirs() ){
				return null;
			}
		}

		// combine directory and filename
		String filePath = folder + "\\"+ threadFileName;
		return filePath;
	}
	

	
	
	/**
	 * Copies file into another location
	 * @param inputFile input file where file is currently found
	 * @param outputFile output file where file is copied
	 * @return true if file operations were successful
	 */
	public static boolean copyFile( File inputFile, File outputFile ){
		FileReader in;
		try {
			
			// create file reader and writer
			in = new FileReader(inputFile);
			FileWriter out = new FileWriter(outputFile);
		    int c;

		    // Read line from input file and write it into output file.
		    while ((c = in.read()) != -1){
		    	out.write(c);
		    }
		    
		    // close files
		    in.close();
		    out.close();
		} 
		catch (FileNotFoundException e) {
			return false;
		} 
		catch (IOException e) {
			return false;
		}
		return true;

	}
	
	
	/**
	 * Moves file into another location
	 * @param inputFile input file where file is currently found
	 * @param outputFile output file where file is moved
	 * @return <code>true</code> if file operations were successful <code>false</code> otherwise.
	 * @see File#renameTo(File)
	 */
	public static boolean moveFile(File inputFile, File outputFile ){
		
		 // Move file into new directory
	    return inputFile.renameTo(outputFile);
	  }
	
	
	/**
	 * Generates file name for new heap dump file based on threads name and current time
	 * @param currentFileName current temporary file name
	 * @return new file name
	 */
	public static String getFileNameForHeapDump(String currentFileName){

		// Get Filename
		String threadFileName = currentFileName.substring( currentFileName.lastIndexOf("\\") +1 );
		String folder = MemSpyFileOperations.getNextFreeDirectory();
		if( folder == null){
			return null;
		}
		
		folder = addSlashToEnd( folder );
		folder = folder + threadFileName;
		
		return folder;
	}
	
	/**
	 * Returns a path where Crash Analyser plugin can do various tasks (located under workspace).
	 * @return E.g. C:\My_Workspace\.metadata\.plugins\[plugin name]\
	 */	
	public static String getPluginWorkingLocation() {
		IPath location = Platform.getStateLocation( MemSpyPlugin.getDefault().getBundle());
		return location.toOSString();		
	}
	
	/**
	 * Deletes directory and everything inside.
	 * @param dir directory name
	 * @return true if file operations were successful
	 */
    public static boolean deleteDir(File dir) {
        boolean isAllDeleted =  deleteFiles(dir);
        // after everything inside directory is deleted, remove directory itself.
        return dir.delete() && isAllDeleted;
    }




	/**
	 * Delete files from dir, but leave the dir as it is.
	 * @param dir
	 * @return <code>true</code> if all files and subdirectories was deleted successfully, <code>false</code> otherwise.
	 */
	public static boolean deleteFiles(File dir) {
		boolean isAllDeleted = true;
		if ( dir.isDirectory() ) {
            
        	// get list of everything inside file.
        	String[] children = dir.list();
        	
        	// go thru file list and call this function recursively.
            for ( int i=0; i < children.length; i++) {
            	boolean success = deleteDir(new File(dir, children[i]));
                if (!success) {
                	isAllDeleted = false;
                }
            }
        }
		return isAllDeleted;
	}

    /**
     * Returns all list of all log files(*.txt and *.log) from directory
     * @param directory where from list is received
     * @return files found in directory
     */
    public static File[] getFilesFromDirectory(File directory){
    	FilenameFilter filter = new MemSpyFileNameFilter();
    	return directory.listFiles( filter );
    }
    
    /**
     * Get temporary SWMT file name with path
     * @param cycleNumber cycle number or received heap
     * @param date current date
     * @return temp file name, null if operation was failed.
     */
	public static String getTempFileNameForSWMTLog( int cycleNumber, Date date ) {
		
		
		// Format the current time.
		SimpleDateFormat formatter = new SimpleDateFormat ( DATEFORMAT );
		String dateString = formatter.format(date);
		
		String fileName = SWMT_LOG_TEXT + " " + dateString + " " + CYCLE + " " + Integer.toString(cycleNumber) + ".txt";

		// replace :'s with -'s as :'s cannot be on file names.
		fileName = fileName.replace(':', '-');
		
		String folder = MemSpyFileOperations.getPluginWorkingLocation() + TEMP;
		
		// create directory if needed
		File file1 = new File(folder);
		if( !file1.exists() ){
			if( !file1.mkdirs() ){
				return null;
			}
		}
		String filePath = file1.toString() + "\\"+ fileName;
		return filePath;
	}
	
	/**
	 * Deletes MemSpy's temp directory and everything inside it
	 * @return <code>true</code> if operation was successful <code>false</code> otherwise.
	 */
	public static boolean deleteTempMemSpyFiles(){
		File folder = new File( MemSpyFileOperations.getPluginWorkingLocation() + TEMP );
		return deleteDir(folder);
		 
	}

	
	/**
	 * Creates next free directory for MemSpy data files
	 * @return next free directory where files can be copied.
	 */
	public static String getNextFreeDirectory(){
	
		// Get fileList from importedHeaps-directory
		String directory = MemSpyFileOperations.getImportedDirectory();
		File file = new File( directory );	
		String[] fileList = file.list();
		directory = MemSpyFileOperations.addSlashToEnd( directory );
		
		
		int i = 0;
		
		File newFile = null; 
		
		
		// if ImportedHeaps-directory is found
		if( fileList != null ){
			// Go thru directory in a loop and find search for first free integer value for directory name.
			while( i <= fileList.length ){
				
				newFile = new File( directory + Integer.toString(i) );
				if ( !newFile.isDirectory() )
				{
					break;
				}
				i++;
			}
		}
		else{
			newFile = new File( directory + "0" );
		}

		// if directories are created successfully, return path, if not return null
		if( newFile.mkdirs() ){
			String newFileString = MemSpyFileOperations.addSlashToEnd( newFile.toString() );
			return newFileString;
		}
		else{
			return null;
		}
		
	}
	
	/**
	 * If the last character of the given path is not backslash, it is added
	 * and path with backslash is returned.
	 * @param path Path to which backslash is added
	 * @return Path which last character is backslash
	 */
	public static String addSlashToEnd(String path) {
		if (path.endsWith(File.separator)){
			return path;
		}
		else{
			return path + File.separator;
		}
	}
	
	/**
	 * Get imported files directory
	 * @return path of the imported-directory
	 */
	public static String getImportedDirectory(){
		// get imported-directory
		String directory = MemSpyFileOperations.getPluginWorkingLocation();
		directory = MemSpyFileOperations.addSlashToEnd( directory );
		return directory + IMPORTED_DIRECTORY_NAME;
	}
	
	/**
	 * Get configuration file path
	 * @return path of the configuration file, if file does not exist yet, method creates it.
	 */
	public static String getCompareConfigurationFilePath(){
		// get imported-directory
		String directory = MemSpyFileOperations.getPluginWorkingLocation();
		directory = MemSpyFileOperations.addSlashToEnd( directory );
		directory = directory + CONFIGURATION_DIRECTORY_NAME;
	
		File file = new File(directory);
		if( !file.exists() ){
			if( !file.mkdirs() ){
				return null;
			}
		}
		directory = MemSpyFileOperations.addSlashToEnd(directory);
		directory = directory + "input.xml";
		return directory;
		
	}
	
	/**
	 * Copies file to another directory with same filename
	 * @param filePath file that is copied
	 * @param directory output directory
	 * @return <code>true</code> if operation was successful <code>false</code> otherwise.
	 */
	public static boolean copyFileToDirectory( String filePath, String directory){
	
		String fileName = filePath.substring( filePath.lastIndexOf("\\") +1 );
		String directoryTo = directory + fileName;
		
		return copyFile( new File( filePath ), new File( directoryTo ) );
	
	}
	
	
	
}
