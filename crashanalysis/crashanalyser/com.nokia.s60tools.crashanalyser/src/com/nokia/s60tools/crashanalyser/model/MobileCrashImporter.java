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

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import com.nokia.s60tools.crashanalyser.corecomponents.interfaces.*;
import com.nokia.s60tools.crashanalyser.files.*;
import com.nokia.s60tools.crashanalyser.data.*;
import com.nokia.s60tools.crashanalyser.ui.views.*;
import java.io.*;

/**
 * This class imports a mobilecrash file received via TraceViewer.
 *
 */
public class MobileCrashImporter extends Job {

	String traceDumpFolder = "";
	String traceDumpFileName = "";
	ErrorLibrary errorLibrary = null;
	boolean decodeFile = false;
	
	/**
	 * Constructor
	 */
	public MobileCrashImporter() {
		super("Importing MobileCrash File via Trace Data");
	}
	
	/**
	 * Starts import process
	 * @param dumpFolder folder where file received via Trace is
	 * @param dumpFileName name of the trace file
	 * @param library error library
	 * @param decode if true, crash file is also decoded, if false, crash file is only imported as undecoded state
	 */
	public void importFrom(String dumpFolder, String dumpFileName, ErrorLibrary library, boolean decode) {
		traceDumpFolder = FileOperations.addSlashToEnd(dumpFolder);
		traceDumpFileName = dumpFileName;
		decodeFile = decode;
		errorLibrary = library;
		setPriority(Job.LONG);
		setUser(false);
		schedule();		
	}
	
	/**
	 * Executes CrashAnalyser.exe with correct parameters
	 */
	void executeCrashAnalyserExe(IProgressMonitor monitor) {
		CommandLineManager.executeSummary(traceDumpFolder, 
											traceDumpFolder + traceDumpFileName, 
											CrashAnalyserFile.SUMMARY_FILE_EXTENSION,
											monitor);
	}
	
	@Override
	protected IStatus run(IProgressMonitor monitor) {
		try {
			// if we need to decoded the received crash file
			if (decodeFile) {
				executeCrashAnalyserExe(monitor);
				File summaryFile = new File(traceDumpFolder + 
											traceDumpFileName + 
											"." + 
											CrashAnalyserFile.SUMMARY_FILE_EXTENSION);
				SummaryFile sf = SummaryFile.read(summaryFile, errorLibrary);
				if (sf == null) {
					MainView.showOrRefresh();
				} else {
					MainView.showOrRefreshAndOpenFile(sf);
				}
			// we did not need to decode the file, just refresh main view and the undecoded file is shown
			} else {
				MainView.showOrRefresh();
			}
			
		} catch (Exception e){
			e.printStackTrace();
		}
		
		return Status.OK_STATUS;
	}
}
