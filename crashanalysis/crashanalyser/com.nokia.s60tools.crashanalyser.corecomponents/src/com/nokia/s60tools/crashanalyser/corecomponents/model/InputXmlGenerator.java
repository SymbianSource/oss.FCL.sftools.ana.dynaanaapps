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

package com.nokia.s60tools.crashanalyser.corecomponents.model;

import java.io.BufferedWriter;
import java.io.IOException;

/**
 * InputXmlGenerator.
 * XML file generator that writes configuration XML-files for Heap Analyser and 
 * Crash Analyser based on parameters it has received.
 * 
 * InputXmlGenerator can be used for example like this:
 * <PRE>
 * InputXmlGenerator generator = new InputXmlGenerator(XMLGeneratorAction.ANALYSE_HEAP, "c:\\temp\\source.txt", "c:\\directory\\", null, null, "thread", "c:\\temp\\output.xls", null, null);
 * File filename = new File("c:\\temp\\testxml.xml");
 * BufferedWriter writer = null;
 * 	try {
 * 		writer = new BufferedWriter(new FileWriter(filename)); 
 * 		generator.GenerateXML(writer);
 * 	} catch (IOException e) {
 * 		e.printStackTrace();
 * 	} 
 * 	finally {
 *  	try {
 *   		if (writer != null) {
 *   			writer.flush();
 *   			writer.close();
 *   		}
 *  	} 
 *  	catch (IOException ex) {
 *  		ex.printStackTrace();
 *  		return false;
 *  	}
 *  }
 *  </PRE>
 */

public class InputXmlGenerator {
	public enum XMLGeneratorAction {
		ANALYSE_HEAP, COMPARE_HEAPS, ANALYSE_CRASH_SUMMARY, ANALYSE_CRASH_FULL 
	}
	
	private XMLGeneratorAction 	XMLAction;
	private String[] 			XMLSourceFile;
	private String 				XMLSourceDirectory;
	private String[] 			XMLDebugMetaDataFile;
	private String 				XMLDebugMetaDataDirectory;
	private String 				XMLThreadName;
	private String 				XMLExtension;
	private String				XMLFailedExtension;
	private String 				XMLAnalyseFileOutput;
	private String 				XMLAnalyseDirectoryOutput;	
	private String				XMLSelgeEventIniFile;

	//Commands for XML
	
	private final static String COMMAND_VIEWER 			= "Viewer";
	private final static String COMMAND_COMPARE_HEAPS 	= "CompareTwoHeaps";
	private final static String COMMAND_SUMMARY 		= "Summary";
	private final static String COMMAND_FULL_ANALYSIS	= "Full";
	
	// XML-tags

	private final static String CRASH_ANALYSER_START 	= "<crash_analysis>";
	private final static String CRASH_ANALYSER_END 		= "</crash_analysis>";

	private final static String HEAP_ANALYSER_START 	= "<heap_analysis>";
	private final static String HEAP_ANALYSER_END 		= "</heap_analysis>";

	private final static String SOURCE_START 			= "  <category name=\"source\">";
	private final static String SOURCE_END 				= "  </category>";

	private final static String DEBUG_META_DATA_START 	= "  <category name=\"debug_meta_data\">";
	private final static String DEBUG_META_DATA_END 	= "  </category>";

	private final static String FILE_START 				= "    <file name=\"";
	private final static String FILE_END 				= "\"/>";

	private final static String DIRECTORY_START 		= "    <directory name=\"";
	private final static String DIRECTORY_END 			= "\"/>";
	
	private final static String PARAMETERS_START 		= "  <category name=\"parameters\">";
	private final static String PARAMETERS_END 			= "  </category>";

	private final static String THREAD_START 			= "<thread>";
	private final static String THREAD_END 				= "</thread>";

	private final static String COMMAND_START 			= "    <command name=\"analysis_type\">";
	private final static String COMMAND_END 			= "</command>";

	private final static String OUTPUT_START 			= "  <category name=\"output\">";
	private final static String OUTPUT_END 				= "  </category>";
	
	private final static String EXTENSION_START 		= "    <extension name=\"";
	private final static String EXTENSION_FAILED		= "\" type=\"failed";
	private final static String EXTENSION_END 			= "\"/>";
	
	/**
	 * XMLGenerator()
	 * Constructor
	 */
	public InputXmlGenerator() {
		
		XMLAction					= null;
		XMLSourceFile 				= null;
		XMLSourceDirectory			= null;
		XMLDebugMetaDataFile 		= null;
		XMLDebugMetaDataDirectory 	= null;
		XMLThreadName 				= null;
		XMLAnalyseFileOutput 		= null;
		XMLExtension				= null;
		XMLFailedExtension			= null;
	
	}

	/**
	 * AnalyserXMLGenerator()
	 * Constructor for XMLGenerator
	 *
	 * @param action 
	 * @param sourceFile file that is added into <source> -section under <file> -tag
	 * @param sourceDirectory directory that is added into <source> -section under <Directory> -tag
	 * @param debugMetaDataFile file that is added into <debug_meta_data> -section under <file> -tag
	 * @param debugMetaDataDirectory directory that is added into <debug_meta_data> -section under <Directory> -tag
	 * @param threadName thread name that is added into <parameters> -section under <thread> -tag
	 * @param analyseFileOutput file name that is added into <output> -section under <file> -tag
 	 * @param analyseDirectoryOutput directory name that is added into <output> -section under <directory> -tag
	 * @param extension extension text that is is added into <parameters> -section under <extension> -tag
	 * @param failedExtension extension text that is is added into <parameters> -section under <extension> -tag
	 */
	public InputXmlGenerator( XMLGeneratorAction action,
								 String[] sourceFile, 
								 String sourceDirectory,
								 String[] debugMetaDataFile, 
								 String debugMetaDataDirectory, 
								 String threadName, 
								 String analyseFileOutput,
								 String analyseDirectoryOutput,
								 String extension,
								 String failedExtension,
								 String selgeEventIniFile) { 
		
		XMLAction					= action;
		XMLSourceFile 				= sourceFile;
		XMLSourceDirectory			= sourceDirectory;
		XMLDebugMetaDataFile 		= debugMetaDataFile;
		XMLDebugMetaDataDirectory	= debugMetaDataDirectory;
		XMLThreadName 				= threadName;
		XMLAnalyseFileOutput 		= analyseFileOutput;
		XMLAnalyseDirectoryOutput	= analyseDirectoryOutput;
		XMLExtension				= extension;
		XMLFailedExtension			= failedExtension;
		XMLSelgeEventIniFile		= selgeEventIniFile;
	}
	
	/**
	 * GenerateXML()
	 * Writes XML file.
	 * @param writer BufferedWriter stream where XML is written. 
	 * @throws IOException if something goes when writing to stream 
	 */
	public void GenerateXML( BufferedWriter writer ) throws IOException{
		if( XMLAction != null )	{
			// Begin XML
			switch( XMLAction ){
				case ANALYSE_CRASH_FULL:
				case ANALYSE_CRASH_SUMMARY:
					writer.write(CRASH_ANALYSER_START);
					break;
				case ANALYSE_HEAP:
				case COMPARE_HEAPS:
					writer.write(HEAP_ANALYSER_START);
					break;
				default:
					break;
			}
					
			writer.newLine();
			writer.newLine();
		}	
		// Write Sources
		this.writeSource(writer);
		
		// Write debug meta data
		this.writeDebugMetaData(writer);
		
		// Write parameters
		this.writeParameters(writer);
		
		// Write output location
		this.writeOutput(writer);
		
		// End XML
		if( XMLAction != null )	{
			switch(XMLAction){
				case ANALYSE_CRASH_SUMMARY:
				case ANALYSE_CRASH_FULL:
					writer.write(CRASH_ANALYSER_END);
					break;
				case ANALYSE_HEAP:
				case COMPARE_HEAPS:
					writer.write(HEAP_ANALYSER_END);
					break;
				default:
					break;
			}
			writer.newLine();
		}
	}

	/**
	 * writeSource()
	 * Writes <source> - parameters
	 * @param writer BufferedWriter stream where XML is written. 
	 */
	private void writeSource( BufferedWriter writer ) throws IOException{
		
		
		// Start Source definition
		writer.write(SOURCE_START);
		writer.newLine();
	
		if( XMLSourceFile != null ){
			// File Definition
			for(int i = 0; i < XMLSourceFile.length; i++ ){
				writer.write( FILE_START );
				writer.write( XMLSourceFile[i] );
				writer.write( FILE_END );
				writer.newLine();
			}
		}
		if( XMLSourceDirectory != null ) {
			writer.write( DIRECTORY_START );
			writer.write( XMLSourceDirectory );
			writer.write( DIRECTORY_END );
			writer.newLine();
		}
		
		// End Source definition
		writer.write(SOURCE_END);
		writer.newLine();
		writer.newLine();
	}
	
	/**
	 * writeDebugMetaData()
	 * Writes <debug_meta_data> - parameters
	 * @param writer BufferedWriter stream where XML is written. 
	 * @throws IOException
	 */
	private void writeDebugMetaData( BufferedWriter writer ) throws IOException{
		// Start Debug meta data definition
	
		if( XMLDebugMetaDataFile != null || 
			XMLDebugMetaDataDirectory != null ){
		
			writer.write(DEBUG_META_DATA_START);
			writer.newLine();
			
			// File Definition
			if( XMLDebugMetaDataFile != null ){
				for(int i = 0; i < XMLDebugMetaDataFile.length; i++ ){
					writer.write( FILE_START );
					writer.write( XMLDebugMetaDataFile[i] );
					writer.write( FILE_END );
					writer.newLine();
				}
			}			
			if( XMLDebugMetaDataDirectory != null ) {
				writer.write( DIRECTORY_START );
				writer.write( XMLDebugMetaDataDirectory );
				writer.write( DIRECTORY_END );
				writer.newLine();
			}
			if (!"".equals(XMLSelgeEventIniFile)) {
				writer.write( FILE_START );
				writer.write( XMLSelgeEventIniFile );
				writer.write( FILE_END );
				writer.newLine();
			}
			
			// End Debug meta data definition
			writer.write(DEBUG_META_DATA_END);
			writer.newLine();
			writer.newLine();
		}
	}
	
	/**
	 * writeParameters(
	 * Writes <parameters> - parameters
	 * @param writer BufferedWriter stream where XML is written. 
	 * @throws IOException
	 */
	private void writeParameters( BufferedWriter writer ) throws IOException {
	
		if( XMLThreadName != null || XMLAction != null || XMLExtension != null){
		
			// Start parameter definition
			writer.write(PARAMETERS_START);
			writer.newLine();
			
			if( XMLThreadName != null ){
				// Add thread info 
				writer.write( THREAD_START );
				writer.write( XMLThreadName );
				writer.write( THREAD_END );
				writer.newLine();				
				
			}	
		
			if( XMLAction != null ){
				// Command
				
				writer.write(COMMAND_START);
		
				// Begin XML
				switch(XMLAction){
					case ANALYSE_CRASH_SUMMARY:
						writer.write(COMMAND_SUMMARY);
						break;
					case ANALYSE_CRASH_FULL:
						writer.write(COMMAND_FULL_ANALYSIS);
						break;
					case ANALYSE_HEAP:
						writer.write(COMMAND_VIEWER);
						break;
					case COMPARE_HEAPS:
						writer.write(COMMAND_COMPARE_HEAPS);
						break;
					default:
						break;
				}		
				writer.write(COMMAND_END);
				writer.newLine();
			}			
			
			if( XMLExtension != null ){
				// extension
				writer.write( EXTENSION_START );
				writer.write( XMLExtension );
				writer.write( EXTENSION_END );
				writer.newLine();
			}
			
			if (XMLFailedExtension != null) {
				// failed extension
				writer.write( EXTENSION_START );
				writer.write( XMLFailedExtension );
				writer.write( EXTENSION_FAILED );
				writer.write( EXTENSION_END );
				writer.newLine();
			}
			
			// End parameter definition
			writer.write(PARAMETERS_END);
			writer.newLine();
			writer.newLine();
		}		
	}
	
	/**
	 * Writes <output> - parameters
	 * @param writer BufferedWriter stream where XML is written. 
	 * @throws IOException
	 */
	private void writeOutput( BufferedWriter writer ) throws IOException{
	
		if( XMLAnalyseFileOutput != null || XMLAnalyseDirectoryOutput != null ){
			writer.write(OUTPUT_START);
			writer.newLine();
			
			// Output file
			if( XMLAnalyseFileOutput != null ){
				writer.write(FILE_START);
				writer.write(XMLAnalyseFileOutput);
				writer.write(FILE_END);
				writer.newLine();
			}
			// Output directory
			if( XMLAnalyseDirectoryOutput != null ) {
				writer.write(DIRECTORY_START);
				writer.write(XMLAnalyseDirectoryOutput);
				writer.write(DIRECTORY_END);
				writer.newLine();
			}
		
			writer.write(OUTPUT_END);
			writer.newLine();
			writer.newLine();
		
			
		}
	}
	
	/* Getters and setters for private variables */
	
	/**
	 * @return XMLSourceFile
	 */
	public String[] getXMLSourceFile() {
		return XMLSourceFile;
	}

	/**
	 * @param sourceFile the XMLSourceFile to set
	 */
	public void setXMLSourceFile(String[] sourceFile) {
		XMLSourceFile = sourceFile;
	}

	/**
	 * @return XMLDebugMetaDataFile
	 */
	public String[] getXMLDebugMetaDataFile() {
		return XMLDebugMetaDataFile;
	}

	/**
	 * @param debugMetaDataFile the XMLDebugMetaDataFile to set
	 */
	public void setXMLDebugMetaDataFile(String[] debugMetaDataFile) {
		XMLDebugMetaDataFile = debugMetaDataFile;
	}

	/**
	 * @return XMLThreadName
	 */
	public String getXMLThreadName() {
		return XMLThreadName;
	}

	/**
	 * @param threadName the XMLThreadName to set
	 */
	public void setXMLThreadName(String threadName) {
		XMLThreadName = threadName;
	}

	/**
	 * @return XMLAnalyseFileOutput
	 */
	public String getXMLAnalyseFileOutput() {
		return XMLAnalyseFileOutput;
	}

	/**
	 * @param analyseFileOutpu the XMLAnalyseFileOutput to set
	 */
	public void setXMLAnalyseFileOutput(String analysefileOutput) {
		XMLAnalyseFileOutput = analysefileOutput;
	}

	/**
	 * @return XMLAction
	 */
	public XMLGeneratorAction getXMLAction() {
		return XMLAction;
	}
	
	/**
	 * @param action the XMLGeneratorAction to set
	 */
	public void setXMLAction(XMLGeneratorAction action) {
		XMLAction = action;
	}

	/**
	 * @return XMLSourceDirectory
	 */
	public String getXMLSourceDirectory() {
		return XMLSourceDirectory;
	}

	/**
	 * @param sourceDirectory the XMLSourceDirectory to set
	 */
	public void setXMLSourceDirectory(String sourceDirectory) {
		XMLSourceDirectory = sourceDirectory;
	}

	/**
	 * @return XMLDebugMetaDataDirectory
	 */
	public String getXMLDebugMetaDataDirectory() {
		return XMLDebugMetaDataDirectory;
	}

	/**
	 * @param debugMetaDataDirectory the XMLDebugMetaDataDirectory to set
	 */
	public void setXMLDebugMetaDataDirectory(String debugMetaDataDirectory) {
		XMLDebugMetaDataDirectory = debugMetaDataDirectory;
	}

	/**
	 * @return XMLExtension
	 */
	public String getXMLExtension() {
		return XMLExtension;
	}

	/**
	 * @param extension the XMLExtension to set
	 */
	public void setXMLExtension(String extension) {
		XMLExtension = extension;
	}

	/**
	 * @return XMLAnalyseDirectoryOutput
	 */
	public String getXMLAnalyseDirectoryOutput() {
		return XMLAnalyseDirectoryOutput;
	}

	/**
	 * @param analyseDirectoryOutput the XMLAnalyseDirectoryOutput to set
	 */
	public void setXMLAnalyseDirectoryOutput(String analyseDirectoryOutput) {
		XMLAnalyseDirectoryOutput = analyseDirectoryOutput;
	}
}
