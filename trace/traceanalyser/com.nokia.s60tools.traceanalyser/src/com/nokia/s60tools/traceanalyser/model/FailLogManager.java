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


package com.nokia.s60tools.traceanalyser.model;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.*;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.ILock;


import com.nokia.s60tools.traceanalyser.interfaces.ITraceAnalyserFileObserver;
import com.nokia.s60tools.traceanalyser.plugin.TraceAnalyserPlugin;
import com.nokia.s60tools.traceanalyser.export.RuleEvent;


/**
 * This class is responsible for providing Trace Analyser Fail Log items to MainView's content provider.
 */
public class FailLogManager extends Job {
	
	/* file observer */
	ITraceAnalyserFileObserver filesObserver = null;
	
	/* accesslock */
	ILock accessLock = null;
	
	/* boolean value that is true when job is on-going */
	boolean jobRunning = false;
	
	/* list of fails */
	//ArrayList<FailLogItem> failLog;
	ArrayList<RuleEvent> failLog;
	
	
	/**
	 * TraceAnalyserFileManager.
	 * Constructor.
	 * @param observer observer, which is notified when reading is finished.
	 */
	public FailLogManager( ITraceAnalyserFileObserver observer ) {
		super("Trace Analyser - Reading Fail Log");
		filesObserver = observer;
		accessLock = Job.getJobManager().newLock();
		failLog = null;
	}


	/*
	 * (non-Javadoc)
	 * @see org.eclipse.core.runtime.jobs.Job#run(org.eclipse.core.runtime.IProgressMonitor)
	 */
	protected IStatus run(IProgressMonitor monitor) {

		accessLock.acquire();
		jobRunning = true;
		
		// if log has not yet been read, read it now.
		if(failLog == null){
			loadLogFromFile();
		}
		// notify main view that reading finished.
		if(filesObserver != null){
			filesObserver.failLogUpdated();
		}
		jobRunning = false;
		accessLock.release();
		
		return Status.OK_STATUS;
	}

	
	/**
	 * getTraceAnalyserFailLog.
	 * Method that returns read violations from fail log. 
	 * If fails are not read method starts reading them.
	 * @return array containing all fail events.
	 */
	public RuleEvent[] getTraceAnalyserFailLog() {
		// files have not yet been read, start reading process
		if (failLog == null ) {
			if(!jobRunning){
				jobRunning = true;
				setPriority(Job.LONG);
				setUser(false);
				schedule(100);
			}
			RuleEvent[] cFiles = new RuleEvent[0];
			
			
			return cFiles;
		}
		else{
			return failLog.toArray(new RuleEvent[failLog.size()]);

		}
		
	}
	
	/**
	 * addItem.
	 * adds one item to fail log.
	 * @param newItem
	 */
	public void addItem(RuleEvent newItem){
		if(failLog != null){
			failLog.add(0,newItem);
		}
		if(filesObserver != null){
			filesObserver.failLogUpdated();
		}
	}
	
	public void clearLog(){
		failLog.clear();
		if(filesObserver != null){
			filesObserver.failLogUpdated();
		}
	}
	
	/**
	 * refresh.
	 * Refresh rule list.
	 */
	public void refresh() {
		
		accessLock.acquire();
		try {
		if (!jobRunning){
			jobRunning = true;
			setPriority(Job.LONG);
			setUser(false);
			schedule(100);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			accessLock.release();
		}
	}

	/**
	 * getPluginWorkingLocation.
	 * Returns a path where Rule plug-in can do various tasks (located under workspace).
	 */	
	private String getFailLogFileName() {
		IPath location = Platform.getStateLocation( TraceAnalyserPlugin.getDefault().getBundle());
		return location.toOSString() + "//FailLog.log";		
	}
	
	/**
	 * saveLogToFile.
	 * Saves fail log into file system.
	 */
	public void saveLogToFile(){
		 try {

			OutputStream file = new FileOutputStream(getFailLogFileName());
			OutputStream buffer = new BufferedOutputStream(file);
			ObjectOutput output = new ObjectOutputStream(buffer);
			try {
				// serialize fail list.
				output.writeObject(failLog);
			} finally {
				output.close();
			}
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}
	
	@SuppressWarnings("unchecked")
	/**
	 * loadLogFromFile.
	 * Loads fail list from file system.
	 */
	private void loadLogFromFile() {
		try {
			// use buffering
			InputStream file = new FileInputStream(getFailLogFileName());
			InputStream buffer = new BufferedInputStream(file);
			ObjectInput input = new ObjectInputStream(buffer);
			try {
				// deserialize the List
				failLog = (ArrayList<RuleEvent>) input.readObject();
				
			} finally {
				input.close();
				if(failLog == null){
					failLog = new ArrayList<RuleEvent>();
				}
			}
		} catch (ClassNotFoundException ex) {
			ex.printStackTrace();
			failLog = new ArrayList<RuleEvent>();

		} catch (IOException ex) {
			failLog = new ArrayList<RuleEvent>();

		}
	}
	
	/**
	 * setObserver.
	 * Sets observer.
	 * @param filesObserver, new observer.
	 */
	public void setObserver(ITraceAnalyserFileObserver filesObserver){
		this.filesObserver = filesObserver;
	}

}
