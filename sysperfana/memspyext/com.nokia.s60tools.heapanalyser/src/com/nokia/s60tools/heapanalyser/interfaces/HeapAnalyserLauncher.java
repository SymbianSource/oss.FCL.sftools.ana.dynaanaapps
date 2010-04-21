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



package com.nokia.s60tools.heapanalyser.interfaces;

import java.io.File;

import com.nokia.s60tools.heapanalyser.HeapAnalyserPlugin;
import com.nokia.s60tools.util.debug.DbgUtility;

/**
 * Class with static variables and helper method to launch Heap Analyser.
 */
public class HeapAnalyserLauncher {

    /// Indicates an unspecified or general error
    public static final int KErrCommandLineGeneral = -1;

    /// Indicates that one or more mandatory command line arguments was omitted
    public static final int KErrCommandLineArgumentsMissing = -2;

    /// Indicates that an input command was not supported or valid
    public static final int KErrCommandLineInvalidCommand = -3;

    /// Indicates that the specified input file was not found
    public static final int KErrCommandLineArgumentsFileNotFound = -4;

    /// Indicates a fatal problem when attempting to read the input file
    public static final int KErrCommandLineArgumentsFileInvalid = -5;

    /// Indicates source file(s) were not found or specified
    public static final int KErrCommandLineSourceFileNotFound = -6;

    /// Indicates debug meta data was omitted from the inputs
    public static final int KErrCommandLineDebugMetaDataMissing = -7;

    /// Occurs if Heap Analyser cannot find a handler for the specified command
    /// line arguments.
    public static final int KErrCommandLineUINotAvailable = -8;

    /// The requested analysis type is not supported
    public static final int KErrCommandLineAnalysisTypeNotSupported = -9;

    /// The specified analysis thread name is invalid
    public static final int KErrCommandLineAnalysisThreadNameInvalid = -10;

    /// The specified output data is invalid
    public static final int KErrCommandLineAnalysisOutputInvalid = -11;
	
    /// The specified output data is invalid
    public static final int KErrCommandLineLaunchingFailed = -12;
	
	
	private static final String EXE_FOLDER = "Binaries";
	private static final String COMMAND_LINE_COMMAND = " -input "; 

	/**
	 * Launch Heap Analyser application.
	 * @param configurationFilePath
	 * @return {@link Process#exitValue()} or {@link HeapAnalyserLauncher#KErrCommandLineLaunchingFailed} 
	 * if an exception occurs.
	 */
	public static int launchHeapAnalyser( String configurationFilePath ){
		String workingDirectory = getHeapAnalyserPath();
		String heapAnalyserCommand = workingDirectory + "HeapAnalyser.exe";
		
		// surround file paths with quotation marks so that spaces in file names wont cause failure.
		heapAnalyserCommand = surroundWithQuotation( heapAnalyserCommand ); 
		configurationFilePath = surroundWithQuotation( configurationFilePath );
		
		String commandLineCommand = heapAnalyserCommand + COMMAND_LINE_COMMAND + configurationFilePath;
	
		int returnValue = 0;
		
		File file = new File(workingDirectory);
		
		// execute HeapAnalyser.exe
		try {
			DbgUtility.println(DbgUtility.PRIORITY_OPERATION, "Starting Heap Analyser: " + commandLineCommand); //$NON-NLS-N$
			Process p = Runtime.getRuntime().exec(commandLineCommand, null, file);
			p.waitFor();
			returnValue = p.exitValue();
		} 
		catch (Exception e) {
			return KErrCommandLineLaunchingFailed;
		} 
		
		return returnValue;
		
		
	}
	
	/**
	 * Gets the path where HeapAnalyserConsole.exe is located
	 * @return the path where HeapAnalyserConsole.exe is located
	 */
	private static String getHeapAnalyserPath() {
		String heapAnalyserExePath = HeapAnalyserPlugin.getPluginInstallPath();
		if (!heapAnalyserExePath.endsWith(File.separator)){
			heapAnalyserExePath += File.separator;
		}
		heapAnalyserExePath += EXE_FOLDER + File.separator;
		return heapAnalyserExePath;
	}
	
	private static String surroundWithQuotation( String text ){
		String retVal = "\"";
		retVal = retVal + text + retVal;
		return retVal;
	}
	
	/**
	 * Gets HeapAnalyser launcher error message.
	 */
	public static String getErrorMessage(int returnValue ){
		switch(returnValue){
			case HeapAnalyserLauncher.KErrCommandLineLaunchingFailed:{
				return "MemSpy was unable to start analysing imported files.";
			}
			case HeapAnalyserLauncher.KErrCommandLineGeneral:{
				return "Unspecified or general error";
			}
			case HeapAnalyserLauncher.KErrCommandLineArgumentsMissing:{
				return "An error occurred when starting Heap Analyser.(Command line argument missing)";
			}
			case HeapAnalyserLauncher.KErrCommandLineInvalidCommand:{
				return "An error occurred when starting Heap Analyser.(Input command not specified)";
			}
			case HeapAnalyserLauncher.KErrCommandLineArgumentsFileNotFound:{
				return "An error occurred when starting Heap Analyser.(Input.xml file not found)";
			}
			case HeapAnalyserLauncher.KErrCommandLineArgumentsFileInvalid:{
				return "An error occurred when starting Heap Analyser.(Unable to read input file. Perhaps your symbol or map files are moved/renamed. )";
			}
			case HeapAnalyserLauncher.KErrCommandLineSourceFileNotFound:{
				return "An error occurred when starting Heap Analyser.(Unable to read source file)";
			}
			case HeapAnalyserLauncher.KErrCommandLineDebugMetaDataMissing:{
				return "An error occurred when starting Heap Analyser.(Symbol files were not found)";
			}
			case HeapAnalyserLauncher.KErrCommandLineUINotAvailable:{
				return "An error occurred when starting Heap Analyser.(Command line UI not available)";
			}
			case HeapAnalyserLauncher.KErrCommandLineAnalysisTypeNotSupported:{
				return "An error occurred when starting Heap Analyser.(Analysis type not supported)";
			}
			case HeapAnalyserLauncher.KErrCommandLineAnalysisThreadNameInvalid:{
				return "An error occurred when starting Heap Analyser.(Thread name invalid)";
			}
			case HeapAnalyserLauncher.KErrCommandLineAnalysisOutputInvalid:{
				return "An error occurred when starting Heap Analyser.(Output file invalid)";
			}
			default:
				return "An unknown error occurred when starting Heap Analyser. "
				       + "Check software prerequisites from release notes "
				       + "(e.g. check correct version of .NET framework is installed).";
		}
	}
}
