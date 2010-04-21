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



import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

import com.nokia.s60tools.heapanalyser.interfaces.HeapAnalyserLauncher;
import com.nokia.s60tools.memspy.interfaces.IAnalyseHeapJobListener;


/**
 * This class is responsible for launching Heap Analyser and waiting for it to stop.
 * If Heap Analyser returns with and error code, error code is passed to job listener
 */
public class AnalyseHeapJob extends Job {

	
	/* path of the configuration file */
	private String configurationFile;
	
	/* path of output file */
	private String analyseOutputFile;
	
	/* Job listener(MemSpy main view */
	private IAnalyseHeapJobListener listener;
	 
	/**
	 * Constructor
	 * @param name Name of the process
	 * @param listener listener that is notified if errors occur.
	 */
	public AnalyseHeapJob(String name, IAnalyseHeapJobListener listener ) {
		super(name);
		this.listener = listener;
		analyseOutputFile = null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.jobs.Job#run(org.eclipse.core.runtime.IProgressMonitor)
	 */
	protected IStatus run(IProgressMonitor arg0) {

		// launch heap analyser and wait for it to stop
		int retVal = HeapAnalyserLauncher.launchHeapAnalyser( configurationFile );
		
		// if output file was generated return it as attchment to main view.
		if( analyseOutputFile != null ){
			listener.heapAnalyserFinished( retVal, analyseOutputFile );
		}
		else{
			listener.heapAnalyserFinished( retVal );
		}
		
		return Status.OK_STATUS;
	}
	
	/**
	 * Launches Heap Analyser.
	 */
	public void refresh() {
		try {
			setPriority(Job.LONG);
			setUser(false);
			schedule(100);
		
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
		}
	}

	
	/**
	 * setter for configurationFiles path
	 * @param configurationFile
	 */
	public void setConfigurationFile(String configurationFile) {
		this.configurationFile = configurationFile;
	}

	/**
	 * setAnalyseOutputFile.
	 * @param analyseOutputFile file path of analyse file output.
	 */
	public void setAnalyseOutputFile(String analyseOutputFile) {
		this.analyseOutputFile = analyseOutputFile;
	}

	
}
