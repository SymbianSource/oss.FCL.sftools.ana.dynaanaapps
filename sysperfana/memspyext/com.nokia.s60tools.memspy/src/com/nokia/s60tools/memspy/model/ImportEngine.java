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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import com.nokia.s60tools.memspy.containers.SWMTLogInfo;
import com.nokia.s60tools.memspy.containers.ThreadInfo;
import com.nokia.s60tools.memspy.containers.SWMTLogInfo.SWMTLogType;
import com.nokia.s60tools.memspy.model.AnalyserXMLGenerator.XMLGeneratorAction;
import com.nokia.s60tools.memspy.ui.views.MemSpyMainView;

/**
 * Class that is used when importing MemSpy data.
 * ImportEngine takes care of copying files into predefined file structure that Main View is able to read.
 */
public class ImportEngine {

	/* MemSpy's Main View */
	private MemSpyMainView view;
	
	/**
	 * Import Engine constructor.
	 * @param view MemSpy's main view
	 */
	public ImportEngine( MemSpyMainView view ){
		this.view = view;
	}
	
	/**
	 * Imports Heap Dump file(s) and starts heap analyser if only one file was imported. Method also shows 
	 * error message if some of operations was not successful.
	 * @param importedHeaps list of imported files
	 * @param symbols symbol information that is used.
	 * @return true, if importing was successful.
	 */
	public boolean importAndAnalyseHeap( ArrayList<ThreadInfo> importedHeaps, AnalyserXMLGenerator xmlGenerator, boolean startHeapAnalyser ){
		return importAndAnalyseHeap( importedHeaps, xmlGenerator, startHeapAnalyser, true);
	}	
	
	/**
	 * Imports Heap Dump file(s) and starts heap analyser if only one file was imported. Method also shows 
	 * error message if some of operations was not successful.
	 * @param importedHeaps list of imported files
	 * @param symbols symbol information that is used.
	 * @param deleteTempFiles if temp files and folder should be deleted after import
	 * @return true, if importing was successful.
	 */
	public boolean importAndAnalyseHeap( ArrayList<ThreadInfo> importedHeaps, AnalyserXMLGenerator xmlGenerator, boolean startHeapAnalyser, boolean deleteTempFiles ){
	    
		boolean importFailed = false;
		
		// Set xmlGenerators action type correct.
		xmlGenerator.setXMLAction( XMLGeneratorAction.ANALYSE_HEAP );
		String fileName = "";
		for ( ThreadInfo threadInfo : importedHeaps ){
			fileName = this.moveHeapToImportedFolder( threadInfo );
	    	if( fileName != null ){
	    		// save new filepath into threadInfo-variable.
    			threadInfo.setThreadFilePath( fileName );
	    		
	    		if( !generateViewConfigurationFile( fileName, threadInfo.getThreadName(), xmlGenerator) ){
	    			importFailed = true;
	    		}
	    		
	    	}
	    	else{
	    		importFailed = true;
	    	}
	    }
		
		// if importing was failed, show error message.
		if( importFailed ){
			view.showErrorMessage( MemSpyMainView.ERROR_IMPORT_HEADER, MemSpyMainView.ERROR_HEAP_IMPORT_FAILED );
			return false;
		}
		
		// Start heap analyser if only one heap was imported.
		if( importedHeaps.size() == 1 && startHeapAnalyser  ){
			
			// get configuration files path
			String analyserFileOutput = importedHeaps.get(0).getThreadFilePath().substring( 0, fileName.lastIndexOf("\\") + 1);
			analyserFileOutput = analyserFileOutput + "configuration.xml";
			
			// launch Heap Analyser
			view.launchHeapAnalyser( analyserFileOutput, null, xmlGenerator.getXMLThreadName(), true );
			
		}
		
		
		// Refresh main view so that newly imported files are shown.
		view.refreshContentAndViewAsync();
		
		// delete temp folder
		if(deleteTempFiles){
			MemSpyFileOperations.deleteTempMemSpyFiles();
		}
		
		return true;
	}
	
	/**
	 * Move imported heap from temporary file into import directory
	 * @param threadInfo imported heap
	 * @return true if file operations were successful
	 */
	
	private String moveHeapToImportedFolder( ThreadInfo threadInfo ){

		// Get new file name for imported file.
	    String newFileName = MemSpyFileOperations.getFileNameForHeapDump( threadInfo.getThreadFilePath() );

	    if( newFileName == null ){
	    	return null;
	    }
	    
	    // Move heap dump from temp folder into Heap Dumps-folder
		if( MemSpyFileOperations.moveFile(new File(threadInfo.getThreadFilePath()), new File(newFileName)) ){
	    	return newFileName;
	    }	
		else{
			return null;
		}
	}
	
	/**
	 * Starts comparing two heap dump files.
	 * @param firstHeap first heap dump
	 * @param secondHeap second heap dump
	 */
	public void compareHeaps( ThreadInfo firstHeap, ThreadInfo secondHeap, AnalyserXMLGenerator xmlGenerator, String output ){
		
		boolean importFailed = false;

		// get file paths of heap dumps.
		String firstHeapFile = firstHeap.getThreadFilePath();
		String secondHeapFile = secondHeap.getThreadFilePath();
		
		if( firstHeapFile != null && secondHeapFile != null ){
		
			// generate compare heaps configuration file.
			xmlGenerator.setXMLAnalyseFileOutput( output );
			if( this.generateCompareConfigurationFile( new String[]{ firstHeapFile, secondHeapFile}, firstHeap.getThreadName(), xmlGenerator) ){
				
				// launch Heap Analyser
				view.launchHeapAnalyser( MemSpyFileOperations.getCompareConfigurationFilePath(), xmlGenerator.getXMLAnalyseFileOutput(), xmlGenerator.getXMLThreadName(), false );
			}
			else{
				importFailed = true;
			}
		}
		if( importFailed ){
			view.showErrorMessage( MemSpyMainView.ERROR_IMPORT_HEADER, MemSpyMainView.ERROR_HEAP_IMPORT_FAILED);
		}
		
	}

	
	/**
	 * Imports SystemWide Memory Tracking logs and starts swmt viewer.
	 * @param importedLogs imported swmt logs.
	 */
	public void importSWMTLogs( final ArrayList<SWMTLogInfo> importedLogs ){
		importSWMTLogs( importedLogs, true);
	}
	
	/**
	 * Imports SystemWide Memory Tracking logs and starts swmt viewer.
	 * @param importedLogs imported swmt logs.
	 * @param deleteTempFiles if temp files and folder should be deleted after import
	 */
	public void importSWMTLogs( final ArrayList<SWMTLogInfo> importedLogs, boolean deleteTempFiles){
	
		boolean isImportedFromDevice = false;
		boolean importFailed = false;
		
		// check if files were imported from device.
		if( importedLogs.size() > 0 ){
			if (importedLogs.get(0).getType() == SWMTLogType.DEVICE ){
				isImportedFromDevice = true;
			}
		}
		
		ArrayList<String> directories = new ArrayList<String>();
		
		// If files were imported from device, move those files into imported directory.
		if( isImportedFromDevice ){

			for( SWMTLogInfo swmtLogInfo : importedLogs){
				
				// Get next free directory
				String directory = MemSpyFileOperations.getNextFreeDirectory();
				directory = MemSpyFileOperations.addSlashToEnd( directory );
			
				// Add directory to arraylist
				directories.add( directory );
				
				if( !MemSpyFileOperations.copyFileToDirectory( swmtLogInfo.getPath(), directory ) ){
					
					// if operations fail show error message and remove all imported files.
					importFailed = true;
					MemSpyFileOperations.deleteDir( new File( directory ) ); 	
		        	break;
				}
			}
		}
		else{// if importing log from file system, copy files into imported directory
			
			for( SWMTLogInfo swmtLogInfo : importedLogs){

				// if file
				if( swmtLogInfo.getType() == SWMTLogType.FILE ){
				
					// Get next free directory
					String directory = MemSpyFileOperations.getNextFreeDirectory();
					directory = MemSpyFileOperations.addSlashToEnd( directory );
				
					// Add directory to arraylist
					directories.add( directory );
					
					if( !MemSpyFileOperations.copyFileToDirectory( swmtLogInfo.getPath(), directory ) ){
					
						// if operation fails show error message and remove all imported files.
						importFailed = true;
						MemSpyFileOperations.deleteDir( new File( directory ) ); 	
			        	break;
					}
				}
				
				
			}
			
		}
		// if error occurred show error message
		if( importFailed ){
			view.showErrorMessage( MemSpyMainView.ERROR_IMPORT_HEADER, MemSpyMainView.ERROR_SWMT_IMPORT_FAILED);
		}
		else{
			// Get ArrayList of imported files.
			ArrayList<String> files = new ArrayList<String>();
			try{
				for( String item : directories ){
					// Get filename from Log file from each directory that is saved into directories-ArrayList.
					File directory = new File(item);
					String fileName = directory.list()[0];
					fileName = item + fileName;
					files.add( fileName );
				}
				view.launchSWMTAnalyser( files );

			}
			catch( Exception e ){
				view.showErrorMessage( MemSpyMainView.ERROR_IMPORT_HEADER, MemSpyMainView.ERROR_LAUNCH_SWMT_ANALYSER );
			}
			
		}
		
		// Refresh main view so that newly imported files are shown.
		view.refreshContentAndViewAsync();
		
		// delete temp folder
		if(deleteTempFiles){
			MemSpyFileOperations.deleteTempMemSpyFiles();
		}
		
		
		

	}
	
	
	/**
	 * Uses AnalyserXMLGenerator to generate configuration file for viewing heap with Heap Analyser
	 * @param name of the source file
	 * @param threadName thread's name
	 * @param xmlGenerator XML generator that is used.
	 * @return true if file was generated successfully
	 */
	public boolean generateViewConfigurationFile( String fileName, String threadName, AnalyserXMLGenerator xmlGenerator ){
		
		xmlGenerator.setXMLAction(XMLGeneratorAction.ANALYSE_HEAP);
		xmlGenerator.setXMLSourceFile( new String[]{ fileName } );
		xmlGenerator.setXMLThreadName( threadName );
		
		// get filenames path
		String analyserFileOutput = fileName.substring( 0, fileName.lastIndexOf("\\") + 1);
		analyserFileOutput = analyserFileOutput + "configuration.xml";
		
		// generate xml-file

		File filename = new File( analyserFileOutput );			
		BufferedWriter writer = null;

		try {
			// Construct FileWriter and print xml into that stream
			writer = new BufferedWriter(new FileWriter(filename)); 
			xmlGenerator.GenerateXML(writer);
		} 
		catch (IOException e) {
			
			e.printStackTrace();
			return false;
		} 
		finally {
			try {
				if (writer != null) {
					writer.flush();
		            writer.close();
		            return true;
		        }
		    } 
			catch (IOException ex) {
				ex.printStackTrace();
				return false;
			}
			
		}
		return false;
	}

	/**
	 * Uses AnalyserXMLGenerator to generate configuration file for comparing two heap's with Heap Analyser
	 * @param fileName names of the source files.
	 * @param threadName thread's name
	 * @param xmlGenerator XML generator that is used.
	 * @return true if file was generated successfully
	 */
	public boolean generateCompareConfigurationFile( String[] fileName, String threadName, AnalyserXMLGenerator xmlGenerator ){
		xmlGenerator.setXMLAction(XMLGeneratorAction.COMPARE_HEAPS);
		xmlGenerator.setXMLSourceFile( fileName );
		xmlGenerator.setXMLThreadName( threadName );
		
		String analyserFileOutput = MemSpyFileOperations.getCompareConfigurationFilePath();
	
		// generate xml-file
		File filename = new File( analyserFileOutput );			

		
		BufferedWriter writer = null;

		try {
			// Construct FileWriter and print xml into that stream
			writer = new BufferedWriter(new FileWriter(filename)); 
			xmlGenerator.GenerateXML(writer);
		} 
		catch (IOException e) {
			
			e.printStackTrace();
			return false;
		} 
		finally {
			try {
				if (writer != null) {
					writer.flush();
		            writer.close();
		            return true;
		        }
		    } 
			catch (IOException ex) {
				ex.printStackTrace();
				return false;
			}
			
		}
		return false;

	}

	
}
