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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Date;

import javax.swing.Timer;

import com.nokia.s60tools.memspy.common.ProductInfoRegistry;
import com.nokia.s60tools.memspy.containers.SWMTLogInfo;
import com.nokia.s60tools.memspy.containers.ThreadInfo;
import com.nokia.s60tools.memspy.containers.SWMTLogInfo.SWMTLogType;
import com.nokia.s60tools.memspy.export.ITraceClientNotificationsIf;
import com.nokia.s60tools.memspy.interfaces.IMemSpyTraceListener;
import com.nokia.s60tools.memspy.interfaces.IMemSpyTraceListener.LauncherAction;
import com.nokia.s60tools.memspy.interfaces.IMemSpyTraceListener.LauncherErrorType;
import com.nokia.s60tools.memspy.plugin.MemSpyPlugin;
import com.nokia.s60tools.memspy.preferences.MemSpyPreferences;
import com.nokia.s60tools.memspy.ui.wizards.DeviceOrFileSelectionPage;
import com.nokia.s60tools.memspy.util.MemSpyConsole;
import com.nokia.s60tools.util.console.IConsolePrintUtility;
import com.nokia.s60tools.util.debug.DbgUtility;


/**
 * <code>TraceCoreEngine</code> class offers public services for starting operations for 
 * getting heap dump and SWMT log data. It creates and manages list of sub tasks 
 * are requested sequentially until main operation chain is over (either successfully 
 * or with error).
 * 
 *  This class is tightly coupled with <code>TraceCoreDataHandler</code> class
 *  that parses trace data and passes flow control back to <code>TraceCoreEngine</code>
 *  between individual sub tasks.  
 *  
 *  @see com.nokia.s60tools.memspy.model.TraceCoreDataHandler.java
 */
public class TraceCoreEngine implements ActionListener, ITraceClientNotificationsIf {

	/**
	 * Enumerator for upper level operation progress status. Ordinal order of the operations should not be changed.
	 * Once operation status is advanced to <code>EProgressMemSpyOperationDone</code>
	 * the progress status is not initialized until wizard re-start, connection setting are changed, or an error occurs. 
	 * 
	 * The enumerator is used in order being able to give user best possible guidance on time-out error when we know
	 * the context in which the error has occurred. 
	 */
	public enum ProgressStatus{
		EPROGRESS_INITIAL, 				// Initial status with no progress so far during current session
		EPROGRESS_MEMSPY_LAUNCHED,		// MemSpy has been launched successfully
		EPROGRESS_FIRST_TASK_LAUNCHED,	// First actual task for MemSpy is triggered (i.e. non-MemSpy launch task)
		EPROGRESS_FIRST_TASK_DONE		// First actual task for MemSpy completed successfully (i.e. non-MemSpy launch task)
	}

	// GroupIDs that are used
	public final static String MEMSPY_LAUNCH 				= "10";
	public final static String MEMSPY_THREAD_LIST 			= "11";
	public final static String MEMSPY_THREAD_INFO 			= "12";
	public final static String MEMSPY_GET_HEAP_DUMP			= "13";
	public final static String MEMSPY_SWMT_UPDATE			= "14";		
	public final static String MEMSPY_SWMT_RESET			= "16";
	public final static String MEMSPY_STOP					= "17";
	public final static String MEMSPY_SET_CATEGORIES_LOW	= "19";
	public final static String MEMSPY_SET_CATEGORIES_HIGH	= "20";	
	public final static String MEMSPY_SET_SWMT_HEAP_DUMP	= "21";
	public final static String MEMSPY_SET_HEAP_NAME_FILTER  = "SUBSCRIBE_COMM_EVENT_SET_HEAP_NAME_FILTER"; 
	
	// Time that device waits for line.
	private final int SECOND = 1000;
	private final int DEFAULT_WAIT_TIME =  20 * SECOND; // seconds
	private final int MAX_WAIT_TIME =  60 * SECOND; // seconds
	private int currentWaitTime;
	
	/**
	 * Indicate that MemSpy Launcher data version is not received.
	 */
	private static final int MEMSPY_LAUNCHER_VERSION_NOT_DEFINED = -1;	
	
	/* timer that is used in error correction */
	private Timer errorTimer;

	/* boolean value that is true when MemSpy is running */
	private boolean MemSpyRunning;
	
	/* Cycle number of swmt logs that is received next time */
	private int cycleNumber;
	
	/* interval between swmt logs */
	private int interval;
	
	/* timer that is used when receiving SWMT-logs via timer */
	private Timer intervalTimer;
	
	/* info of swmt-log that is currently received */
	private SWMTLogInfo swmtLogInfo;
		
	// Trace data handler
	private TraceCoreDataHandler handler;
	
	// Id of threads that is currently received
	int threadID;
	
	// Task list
	private ArrayList<String> taskList;
	
	// WizardPage, that is notified when operations are done. 
	private IMemSpyTraceListener wizardPage;
	
	// Name of the file where heap dumps and SWMT-logs are written.
	private String currentFile;

	/**
	 * If S60 MemSpy is to be closed between cycles
	 */
	private boolean resetBetweenCycles = false;

	/**
	 * Stores MemSpy launcher communication version.
	 */
	private int receivedMemSpyLauncherDataVersion = MEMSPY_LAUNCHER_VERSION_NOT_DEFINED;

	/**
	 * Upper level operation progress status. Ordinal order of the operations should not be changed.
	 * Once operation status is advanced to <code>EProgressMemSpyOperationDone</code>
	 * the progress status is not initialized until wizard re-start, connection setting are changed, or an error occurs. 
	 */
	private ProgressStatus progressStatus = ProgressStatus.EPROGRESS_INITIAL;

	/**
	 * Provides possibly additional error information about the occurred error.
	 */
	private String additionalErrorInformation;
	
	/**
	 * Constructor.
	 */
	public TraceCoreEngine(){
		DbgUtility.println(DbgUtility.PRIORITY_OPERATION, "TraceCoreEngine() Construct START"); //$NON-NLS-1$
		this.MemSpyRunning = false;
		this.handler = new TraceCoreDataHandler(this);
		this.taskList = new ArrayList<String>();
		this.errorTimer = null;
		this.cycleNumber = 1;
		this.interval = 0;
		this.swmtLogInfo = null;
		this.currentWaitTime = DEFAULT_WAIT_TIME;
		DbgUtility.println(DbgUtility.PRIORITY_OPERATION, "TraceCoreEngine() Construct END"); //$NON-NLS-1$
	}

	/**
	 * Request thread list from MemSpyLauncher.
	 * @param threadArray array where thread names and id's are written
	 * @param wizard page which is notified when request is finished
	 * @return true, if connection to TraceCore established successfully, else false
	 */
	
	public boolean requestThreadList( ArrayList<ThreadInfo> threadArray, DeviceOrFileSelectionPage wizard ){
		this.wizardPage 		= wizard;
		
		// Set handler values correct
		handler.setLastWasName( false );
		handler.setThreadArray( threadArray );
	
		// if connection established successfully, return true 
		if( this.connect() ){
			
			// Set handler values correct
			handler.setLastWasName( false );
			handler.setThreadArray( threadArray );
			
			if( !this.MemSpyRunning ){
				taskList.add( MEMSPY_LAUNCH );
			}
			taskList.add( MEMSPY_THREAD_LIST );
			return this.runNextTask();
		}
		else{
			return false;
		}
		
		
	}
	
	/**
	 * Request Heap Dump from device and write it into text file
	 * @param threadID ID of thread which is requested from device
 	 * @param wizardPage page which is notified when request is finished
	 * @param currentFile path of the file where Heap Dump is written
	 * @return true, if connection to TraceCore established successfully, else false 
	 */
	public boolean requestHeapDump( int threadID, IMemSpyTraceListener wizardPage, String currentFile ){

		this.currentFile 		= currentFile;
		this.wizardPage 		= wizardPage;
		this.threadID 			= threadID;
		
		DbgUtility.println(DbgUtility.PRIORITY_OPERATION, "TraceCoreEngine.requestHeapDump/threadID=" + threadID); //$NON-NLS-1$					

		if( this.connect() ){
			
			// Reset heap type value
			handler.setHeapTypeCorrect( false );
			
			// Send id to trace,
			if( !this.sendIntegerDataToLauncher(threadID) ){
				launcherError(LauncherErrorType.ACTIVATION);
				return false;
			}
			else{
				DbgUtility.println(DbgUtility.PRIORITY_OPERATION, "TraceCoreEngine.requestHeapDump/activateId => OK"); //$NON-NLS-1$					
			}
			
			if( !this.MemSpyRunning ){
				taskList.add( MEMSPY_LAUNCH );
			}
			
			// Add tasks into task list
			taskList.add( MEMSPY_THREAD_INFO );
			taskList.add( MEMSPY_GET_HEAP_DUMP );
			
			// start running tasks
			return this.runNextTask();
		}
		else{
			return false;
		}
	}
	
	/**
	 * Request Heap Dump from device and write it into text file
	 * @param threadID ID of thread which is requested from device
	 * @param wizardPage page which is notified when request is finished
	 * @param currentFile path of the file where Heap Dump is written
	 * @return true, if connection to TraceCore established successfully, else false 
	 */
	public boolean requestHeapDump( String threadID, IMemSpyTraceListener wizardPage, String currentFile ){
	
		return requestHeapDump( Integer.parseInt(threadID), wizardPage, currentFile );

	}
		
	/**
	 * Request SWMT-log from device and write it into file
	 * @param wizardPage page which is notified when request is finished
	 * @param currentFile path of the file where Heap Dump is written
	 * @param resetCycles should cycles be reseted
	 * @return true, if connection to TraceCore established successfully, else false 
	 */
	public boolean requestSWMTLog( IMemSpyTraceListener wizardPage, String currentFile, boolean resetCycles){
		this.currentFile 		= currentFile;
		this.wizardPage 		= wizardPage;
		
		DbgUtility.println(DbgUtility.PRIORITY_OPERATION, "TraceCoreEngine.requestSWMTLog(resetCycles=" + resetCycles + ")/currentFile: " + currentFile); //$NON-NLS-1$ //$NON-NLS-2$
		
		// if connection established successfully, return true 
		if( this.connect() ){
		
			// stop and start MemSpy so that logging is reseted.
			if( resetCycles == true ){
				taskList.add( MEMSPY_STOP );
				taskList.add( MEMSPY_LAUNCH );
			}
			
			// If MemSpy is not running launch it
			if( !this.MemSpyRunning ){
				taskList.add( MEMSPY_LAUNCH );
			}

			//Adding category settings requests, if the feature is supported
			if(MemSpyPlugin.getDefault().isSWMTCategorySettingFeatureEnabled()){
				taskList.add( MEMSPY_SET_CATEGORIES_LOW ); //LOW bytes has to be written always before high bytes
				taskList.add( MEMSPY_SET_CATEGORIES_HIGH ); //HIGH bytes has to be written always after low bytes				
			}
			
			
			// Set the name filter for User Heap SWMT category
			if(MemSpyPreferences.isSWMTHeapDumpSelected() && !MemSpyPreferences.isProfileTrackedCategoriesSelected()) {
				taskList.add( MEMSPY_SET_SWMT_HEAP_DUMP );
				taskList.add( MEMSPY_SET_HEAP_NAME_FILTER );
			}
			
			taskList.add( MEMSPY_SWMT_UPDATE );

			// start running tasks
			return this.runNextTask();
		}
		else{
			return false;
		}
	}
	
	/**
	 * Starts timer based SWMT logging.
	 * @param wizardPage page which is notified when request is finished
	 * @param currentFile path of the file where Heap Dump is written
	 * @param resetInStart should cycles be reseted
	 * @param resetBetweenCycles if MemSpy S60 application is to be reseted between every cycle
	 * @param interval poll interval
	 * @return true, if connection to TraceCore established successfully, else false 
	 */
	public boolean startSWMTTimer( IMemSpyTraceListener wizardPage, int cycleNumber, 
								   boolean resetInStart, boolean resetBetweenCycles,  int interval ){
		this.wizardPage 		= wizardPage;
		this.resetBetweenCycles = resetBetweenCycles;
	
		DbgUtility.println(DbgUtility.PRIORITY_OPERATION, "TraceCoreEngine.startSWMTTimer"); //$NON-NLS-1$
		
		// if connection established successfully, return true 
		if( this.connect() ){
			this.cycleNumber 		= cycleNumber - 1;
			this.interval			= interval;
			
			
			if( resetInStart == true ){
				taskList.add( MEMSPY_STOP );
				taskList.add( MEMSPY_LAUNCH );

			}
			
			this.runNextTimedTask();
			
			return true;
		}
		else{
			return false;
		}
		
		
	}
	
	
	/**
	 * Stops SWMT timer 
	 * @return true if timer was stopped immediately, false if MemSpy operation was on-going and timer is stopped after after operations are done.
	 */
	public boolean stopSWMTTimer(){
		
		DbgUtility.println(DbgUtility.PRIORITY_OPERATION, "TraceCoreEngine.stopSWMTTimer"); //$NON-NLS-1$
				
		// other variables to zero.
		this.interval = 0;
		this.cycleNumber = 1;

		// if timer is, running stop it
		if( intervalTimer != null && intervalTimer.isRunning() ){
			intervalTimer.stop();
			this.swmtLogInfo = null;
			return true;
		}
		else{
			return false;
		}
	}
	
	/**
	 * addNextTimedTask.
	 * Adds next timed tasks into taskList if needed
	 */
	private void runNextTimedTask(){
		
		cycleNumber++;
		
		// get new SWMT-object and set filename correct
		swmtLogInfo = this.getNewSWMTInfo();
		this.currentFile = swmtLogInfo.getPath();
		
		// If MemSpy is not running launch it
		if( !this.MemSpyRunning ){
			taskList.add( MEMSPY_LAUNCH );
		}
		//If memSpy is running and we want to reset it between cycles
		else if(this.MemSpyRunning && resetBetweenCycles == true ){
			taskList.add( MEMSPY_STOP );
			taskList.add( MEMSPY_LAUNCH );

		}
				
		//Adding category settings requests, if the feature is supported
		if(MemSpyPlugin.getDefault().isSWMTCategorySettingFeatureEnabled()){
			taskList.add( MEMSPY_SET_CATEGORIES_LOW ); //LOW bytes has to be written always before high bytes
			taskList.add( MEMSPY_SET_CATEGORIES_HIGH ); //HIGH bytes has to be written always after low bytes			
		}

		// Set the name filter for User Heap SWMT category
		if(MemSpyPreferences.isSWMTHeapDumpSelected() && !MemSpyPreferences.isProfileTrackedCategoriesSelected()) {
			taskList.add( MEMSPY_SET_SWMT_HEAP_DUMP );			
			taskList.add( MEMSPY_SET_HEAP_NAME_FILTER );
		}

		
		// Requesting SWMT update
		taskList.add( MEMSPY_SWMT_UPDATE );
		
		// start runnings tasks
		this.runNextTask();
	}
	
	/**
	 * Function that handles calls when MemSpys operation is finished successfully
	 * This method is called from DataHandler every time tag <MEMSPY_LAUNCHER_READY>-tag received.
	 */
	public void memSpyReady(){

		
		DbgUtility.println(DbgUtility.PRIORITY_OPERATION, "TraceCoreEngine.memSpyReady"); //$NON-NLS-1$
		
		//Check Launcher data version
		checkMemSpyLauncherDataVersion();
		//MemSpy launcher data version is received always before <MEMSPY_LAUNCHER_READY> -tag is received
		//When version number is checked, returning -1 to received value
		receivedMemSpyLauncherDataVersion = MEMSPY_LAUNCHER_VERSION_NOT_DEFINED;
		
		// Stop logging trace data
		handler.stopLogging();
		// Stop listening trace data
		MemSpyPlugin.getTraceProvider().stopListenTraceData();
		// Stop timer
		errorTimer.stop();
		
		// Checking an updating progress status based on the completed task type
		checkTaskForCurrentProgressStatus(this.taskList.get(0));
		
		if( this.taskList.get(0) == MEMSPY_LAUNCH ){			
			//Setting timer value to MAX here when known that MemSpy Launcher in S60 target is OK 
			//(last successfully run command is MemSpy launch).
			this.currentWaitTime = MAX_WAIT_TIME;

			// MemSpy started successfully, update status
			this.MemSpyRunning = true;
			
			DbgUtility.println(DbgUtility.PRIORITY_OPERATION, "TraceCoreEngine.memSpyReady/MEMSPY_LAUNCH/MemSpyRunning=true"); //$NON-NLS-1$
			if( this.taskList.size() >= 2 && this.taskList.get(1) == MEMSPY_THREAD_INFO ){
				DbgUtility.println(DbgUtility.PRIORITY_OPERATION, "TraceCoreEngine.memSpyReady/MEMSPY_LAUNCH/activateId( threadID: "+ threadID); //$NON-NLS-1$
				if( !this.sendIntegerDataToLauncher(threadID) ){
					launcherError(LauncherErrorType.ACTIVATION);
				}
				else{
					DbgUtility.println(DbgUtility.PRIORITY_OPERATION, "T raceCoreEngine.memSpyReady/MEMSPY_LAUNCH/activateId => OK"); //$NON-NLS-1$					
				}
			}
		}
		else if( this.taskList.get(0) == MEMSPY_THREAD_LIST ){
			DbgUtility.println(DbgUtility.PRIORITY_OPERATION, "TraceCoreEngine.memSpyReady/MEMSPY_THREAD_LIST"); //$NON-NLS-1$
			this.wizardPage.operationFinished( LauncherAction.GET_THREAD_LIST );
		}
		else if( this.taskList.get(0) == MEMSPY_THREAD_INFO ){
			// Heap info received
			DbgUtility.println(DbgUtility.PRIORITY_OPERATION, "TraceCoreEngine.memSpyReady/MEMSPY_THREAD_INFO"); //$NON-NLS-1$
			
			// If heap type is not correct, reset tasklist and return error
			if( !handler.isHeapTypeCorrect() ){
				this.taskList.clear();
				this.wizardPage.deviceError( LauncherErrorType.HEAP_TYPE_WRONG );
			}
			
			// ignore dumped traces-messages.
			if( handler.isDumpedTraces() ){
				handler.setDumpedTraces( false );
			}			
		}
		else if( this.taskList.get(0) == MEMSPY_GET_HEAP_DUMP ){
			// Heap Dump received
			DbgUtility.println(DbgUtility.PRIORITY_OPERATION, "TraceCoreEngine.memSpyReady/MEMSPY_GET_HEAP_DUMP"); //$NON-NLS-1$

			if( handler.isDumpedTraces() == false ){
				// Tell wizard that request is finished
				this.wizardPage.operationFinished( LauncherAction.GET_HEAP_DUMP );
			}
			else{
				handler.setDumpedTraces( false );
				this.launcherError(LauncherErrorType.HEAP_NOT_FOUND);
			}
		}		
		else if( this.taskList.get(0) == MEMSPY_SWMT_UPDATE || this.taskList.get(0) == MEMSPY_SWMT_RESET ){
			// if SWMT log received
			DbgUtility.println(DbgUtility.PRIORITY_OPERATION, "TraceCoreEngine.memSpyReady/MEMSPY_SWMT_UPDATE || MEMSPY_SWMT_RESET"); //$NON-NLS-1$
			
			if( handler.isDumpedTraces() == false ){
				// Tell wizard that request is finished
				
				// if timed swmt-logging is on request notification after interval
				if( swmtLogInfo != null ){
					
					boolean timerRunning = false;
					
					// if interval is more that zero, start counter and set timerRunning variable correct.
					if( interval > 0 ){
						intervalTimer = new Timer( interval * SECOND, this );
						intervalTimer.start();
						timerRunning = true;
					}
					
					// tell wizard that one log file has been received.
					this.wizardPage.operationFinished( LauncherAction.TIMED_SWMT_UPDATE, swmtLogInfo, timerRunning);
					swmtLogInfo = null;

				}
				else{
					this.wizardPage.operationFinished( LauncherAction.SWMT_UPDATE );
				}
			}
			else{
				//Reset SWMT timer values.
				this.stopSWMTTimer();
				
				handler.setDumpedTraces( false );
				this.launcherError(LauncherErrorType.HEAP_NOT_FOUND);
			}
		}						
		else if( this.taskList.get(0) == MEMSPY_STOP ){
			MemSpyRunning = false;
			DbgUtility.println(DbgUtility.PRIORITY_OPERATION, "TraceCoreEngine.memSpyReady/MEMSPY_STOP"); //$NON-NLS-1$
		}

		// Remove first task from list.
		if(taskList.size() > 0){
			this.taskList.remove(0);
			// Updating progress status to next task launched status 
			// (only updated in case this is progress to previous situation).
			setProgressStatus(ProgressStatus.EPROGRESS_FIRST_TASK_LAUNCHED);			
		}
		// run next task
		this.runNextTask();
	
			
	}

	/**
	 * Updates progress status based on given task id. 
	 * Used setter method takes care that update is done only 
	 * if there has been progress compared to previous situation.
	 * @param taskId task event for the task that was just completed
	 */
	private void checkTaskForCurrentProgressStatus(String taskId) {
		if( taskId == MEMSPY_LAUNCH ){			
			setProgressStatus(ProgressStatus.EPROGRESS_MEMSPY_LAUNCHED);
		}
		else{
			// In case of other than launch task first task has been executed properly
			setProgressStatus(ProgressStatus.EPROGRESS_FIRST_TASK_DONE);			
		}		
	}

	/**
	 * Handles calls when launcher prints error message into trace.
	 * @param error error code
	 * @param clientContextErrorString provides optionally additional information about the error occurred
	 */
	public void launcherError( LauncherErrorType error, String clientContextErrorString ){
		
		DbgUtility.println(DbgUtility.PRIORITY_OPERATION, "TraceCoreEngine.launcherError/error: " + error); //$NON-NLS-1$

		additionalErrorInformation = clientContextErrorString;;
		
		handler.stopLogging();
		// Stop listening trace data
		MemSpyPlugin.getTraceProvider().stopListenTraceData();

		// Stop timer
		if( errorTimer != null ){
			errorTimer.stop();
			//Setting timer value to default when error occurred 
			this.currentWaitTime = DEFAULT_WAIT_TIME;
		}

		// if wizard has been shut down, don't send error message.
		if( taskList.size() == 1 && taskList.get(0) == MEMSPY_STOP ){
			return;
		}
		this.taskList.clear();
		// Stop logging trace data
		
		//
		//When there are special handling about error, founding error codes and then call the wizard.
		//But in default case, we just pass the error code to wizard to show the error.
		//
		
		// MemSpy not running.
		if( error == LauncherErrorType.MEMSPY_NOT_RUNNING ){
			wizardPage.deviceError( LauncherErrorType.MEMSPY_NOT_RUNNING );
			this.MemSpyRunning = false;
		}
		else if( error == LauncherErrorType.NO_ANSWER_FROM_DEVICE ){ 
			// No answer from device can happen because
			if( handler.isDumpedTraces() ){
				// Input data is corrupted and traces are dumped...
				handler.setDumpedTraces( false );
				wizardPage.deviceError( LauncherErrorType.DUMPED_TRACES );
			}
			else{
				//..or connection is broken and we really has'nt got any response from device
				wizardPage.deviceError( LauncherErrorType.NO_ANSWER_FROM_DEVICE );
			}
		}		
		else {
			wizardPage.deviceError( error);
		}		

	}
	
	
	/**
	 * Function that is called when response from launcher is not received within reasonable time.
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent e) {
	
		DbgUtility.println(DbgUtility.PRIORITY_OPERATION, "TraceCoreEngine.actionPerformed/e.getSource(): " + e.getSource().toString()); //$NON-NLS-1$
		
		if( e.getSource() == errorTimer ){
			// This function is called if MemSpy operation does not respond in time stored in currentWaitTime member variable.
			// I.e. no answer has been received from the device in expected maximum time.
			this.launcherError(LauncherErrorType.NO_ANSWER_FROM_DEVICE);
		}
		else if( e.getSource() == intervalTimer ){			
			this.runNextTimedTask();
			// Notify wizard that SWMT receiving is started
			wizardPage.startedReceivingSWMTLog();
			intervalTimer.stop();
		}		

	}
	
	
	/**
	 * Shuts down MemSpy application. If some MemSpy operation is on-going schedule shutdown after that.
	 */
	public void shutDownMemSpy(){
	
		DbgUtility.println(DbgUtility.PRIORITY_OPERATION, "TraceCoreEngine.shutDownMemSpy"); //$NON-NLS-1$
		
		// if MemSpy is running, send stop request
		if( this.MemSpyRunning ) {
			this.taskList.add( MEMSPY_STOP );
		}
		
		// If timer is not running( MemSpy is not currently operating ), run next task
		if( errorTimer != null && !errorTimer.isRunning() ){
			this.runNextTask();
		}
		disconnectTrace();
	}
	
	/**
	 * Check if MemSpy is running
	 * @return <code>true</code> if MemSpy is Running <code>false</code> otherwise.
	 */
	public boolean isMemSpyRunning() {
		return MemSpyRunning;
	}
		
	/**
	 * Establishes connection between plugin and device.
	 * @return true, is connection established successfully
	 */
	private boolean connect(){	
		DbgUtility.println(DbgUtility.PRIORITY_OPERATION, "TraceCoreEngine.connect"); //$NON-NLS-1$
		return MemSpyPlugin.getTraceProvider().connectTraceSource(this);	
	}
	
	/**
	 * Disconnects connection between plugin and device if connection was started
	 * for this MemSpy run. Leaving connection up, if TraceViewer was already connected.
	 */
	public void disconnectTrace() {		
		DbgUtility.println(DbgUtility.PRIORITY_OPERATION, "TraceCoreEngine.disconnectTrace"); //$NON-NLS-1$
		MemSpyPlugin.getTraceProvider().disconnectTraceSource();	
	}
	
	/**
	 * Sends current usage context-specific integer data to launcher.
	 * Integer data can contain values that can be expressed with 10 bytes
	 * i.e. only 10 lower bytes are taken into account when setting data.
	 * @param integerData integer data to be sent
	 * @return <code>false</code> if failed to send integer data, otherwise <code>true</code>
	 */
	private boolean sendIntegerDataToLauncher(int integerData)
	{
		DbgUtility.println(DbgUtility.PRIORITY_OPERATION, "sendIntegerDataToLauncher: id=" + integerData); //$NON-NLS-1$
		return MemSpyPlugin.getTraceProvider().sendIntData(integerData);
	}

	/**
	 * Sends current usage context-specific string message to launcher.
	 * @param stringData string data to send
	 * @param writesFile set to <code>true</code> if set trace handler needs to write some data into file
	 * @return <code>true</code> on success, otherwise <code>false</code>
	 */
	private boolean sendStringDataToLauncher(String stringData, boolean writesFile){
		DbgUtility.println(DbgUtility.PRIORITY_OPERATION, "TraceCoreEngine.sendMessage/message=" + stringData); //$NON-NLS-1$ 
		
		if(! addDataProcessorAndSetupLogging(writesFile)){
			return false;
		}
		
		if(! MemSpyPlugin.getTraceProvider().sendStringData(stringData)){
			return false;
		}
		
		startErrorTimer();
		return true;
	}
	
	/**
	 * activateTrace
	 * Sends activation/deactivation message via TraceCore
	 * @param group GroupID
	 * @param writesFile true, if set trace handler needs to write some data into file.
	 * return false if trace activation was not successful.
	 */
	private boolean activateTrace( String group, boolean writesFile ){
			
		DbgUtility.println(DbgUtility.PRIORITY_OPERATION, "TraceCoreEngine.activateTrace/group=" + group + ", writesFile=" + writesFile); //$NON-NLS-1$ //$NON-NLS-2$
		
		if(! addDataProcessorAndSetupLogging(writesFile)){
			return false;
		}
		
		if(! MemSpyPlugin.getTraceProvider().activateTrace(group)){
			return false;
		}

		startErrorTimer();			
		return true;
		
	}

	/**
	 * Starts error time after request.
	 */
	private void startErrorTimer() {
		// Start Timer
		errorTimer = new Timer( currentWaitTime, this );
		errorTimer.start();
	}

	/**
	 * Adds dataprocessor and sets-up logging.
	 * @param writesFile true, if set trace handler needs to write some data into file.
	 * @return <code>true</code> in case of success, and <code>false</code> in case of some failure.
	 */
	private boolean addDataProcessorAndSetupLogging(boolean writesFile) {
				
		//Add DataProcessor to TraceViewer
		if(! MemSpyPlugin.getTraceProvider().startListenTraceData(handler)){
			return false;
		}
		
		// Start logging
		if( !handler.startLogging(currentFile, writesFile ) ){
			return false;
		}
		
		return true;
	}
	
	/**
	 * runNextTask
	 * Gets next task from list and sends it to TraceCore.
	 * @return false if operation fails.
	 */
	private boolean runNextTask(){
		
		try {
			if( taskList.size() > 0 ){
				String nextTask = taskList.get(0);
				DbgUtility.println(DbgUtility.PRIORITY_OPERATION, "TraceCoreEngine.runNextTask/taskList.get(0)=" + nextTask); //$NON-NLS-1$
								
				// Confirming that all necessary preparations for running the next task has been done.
				prepareRunNextTask();
				
				// Set writeFile value as false when task needs to write some data into file
				if( nextTask == MEMSPY_GET_HEAP_DUMP || nextTask == MEMSPY_SWMT_UPDATE || nextTask == MEMSPY_SWMT_RESET ){
					if( !activateTrace( nextTask, true ) ){
						this.launcherError(LauncherErrorType.ACTIVATION);
						return false;
					}
				}
				else if(nextTask == MEMSPY_SET_HEAP_NAME_FILTER){
					// This task used subscribe communication, instead of group IDs
					// Send SWMT heap filter before activating command group
					DbgUtility.println(DbgUtility.PRIORITY_OPERATION, "TraceCoreEngine.memSpyReady/MEMSPY_SET_HEAP_NAME_FILTER/sendMessage"); //$NON-NLS-1$
					if( !this.sendStringDataToLauncher(getHeapNameFilterForSWMT(), false)){
						launcherError(LauncherErrorType.ACTIVATION);
					}
					else{
						DbgUtility.println(DbgUtility.PRIORITY_OPERATION, "TraceCoreEngine.memSpyReady/MEMSPY_SET_HEAP_NAME_FILTER/sendMessage => OK"); //$NON-NLS-1$					
					}
				}
				else{
					if( !activateTrace( nextTask, false) ){
						this.launcherError(LauncherErrorType.ACTIVATION);
						return false;
					};

				}
			}
			else{
				DbgUtility.println(DbgUtility.PRIORITY_LOOP, "TraceCoreEngine.runNextTask/empty taskList"); //$NON-NLS-1$								
				//Setting timer value to default when there are no tasks to run
				this.currentWaitTime = DEFAULT_WAIT_TIME;
			}
			return true;
			
		} catch (Exception e) {
			String errMsg = "Unexpected exception in encountered in TraceCoreEngine.runNextTask: " + e;
			MemSpyConsole.getInstance().println(errMsg , IConsolePrintUtility.MSG_ERROR);
			return false;
		}
				
	}
	
	
	/**
	 * Confirms that all necessary preparations for running the next task has been done.
	 */
	private void prepareRunNextTask() {		
		if(this.taskList.get(0) == MEMSPY_SET_CATEGORIES_LOW ){
			// Setting SWMT low bits data before activating command group
			DbgUtility.println(DbgUtility.PRIORITY_OPERATION, "TraceCoreEngine.prepareRunNextTask/activateId( getCategoriesForSWMTLowBits(): "+ getCategoriesForSWMTLowBits()); //$NON-NLS-1$
			if( !this.sendIntegerDataToLauncher( getCategoriesForSWMTLowBits()) ){
				launcherError(LauncherErrorType.ACTIVATION );
			}
			else{
				DbgUtility.println(DbgUtility.PRIORITY_OPERATION, "TraceCoreEngine.prepareRunNextTask/MEMSPY_SET_CATEGORIES_HIGH/activateId => OK"); //$NON-NLS-1$					
			}
		}
		else if( this.taskList.get(0) == MEMSPY_SET_CATEGORIES_HIGH ){
			// Setting SWMT high bits data before activating command group
			DbgUtility.println(DbgUtility.PRIORITY_OPERATION, "TraceCoreEngine.prepareRunNextTask/MEMSPY_SET_CATEGORIES_HIGH/activateId( getCategoriesForSWMTHighBits(): "+ getCategoriesForSWMTHighBits()); //$NON-NLS-1$
			if( !this.sendIntegerDataToLauncher( getCategoriesForSWMTHighBits()) ){
				launcherError(LauncherErrorType.ACTIVATION );
			}
			else{
				DbgUtility.println(DbgUtility.PRIORITY_OPERATION, "TraceCoreEngine.prepareRunNextTask/MEMSPY_SET_CATEGORIES_HIGH/activateId => OK"); //$NON-NLS-1$					
			}
		}
	}

	/**
	 * getNewSWMTInfo.
	 * Creates a new SWMTLogInfo object and sets correct filename and time into it.
	 * @return SWMTLogInfo-object
	 */
	private SWMTLogInfo getNewSWMTInfo(){
		
		DbgUtility.println(DbgUtility.PRIORITY_OPERATION, "TraceCoreEngine.getNewSWMTInfo"); //$NON-NLS-1$
		
		// Create new SWMTLogInfo item.
		SWMTLogInfo newItem = new SWMTLogInfo();
		
		// set date correct
		Date date = new Date();		
		newItem.setDate(date);

	
		// set filename correct
		newItem.setPath( MemSpyFileOperations.getTempFileNameForSWMTLog(cycleNumber, date) );
		
		// set type
		newItem.setType( SWMTLogType.DEVICE );
		
		return newItem;
	}
	
	/**
	 * Get first task.
	 * @return first task from taskList.
	 */
	public String getFirstTask(){
		if( taskList.size() > 0 ){
			return taskList.get(0);
		}
		else{
			return null;
		}
	}	

	/**
	 * Gets lower 10 bits for SWMT categories that user wants to include into SWMT log.
	 * @return  lower 10 bits for SWMT categories that user wants to include into SWMT log.
	 */
	public int getCategoriesForSWMTLowBits() {
		int lowBits = getCategoriesForSWMTWithKernelHandles() & 0x3ff; // ANDs away all the other that lower 10 bits
		DbgUtility.println(DbgUtility.PRIORITY_OPERATION, "getCategoriesForSWMTLowBits(): " + String.format("0x%x", lowBits)); //$NON-NLS-1$
		return lowBits; 
	}

	/**
	 * Gets higher bits for SWMT categories that user wants to include into SWMT log.
	 * @return  higher bits for SWMT categories that user wants to include into SWMT log.
	 */
	public int getCategoriesForSWMTHighBits() {
		int highBits = getCategoriesForSWMTWithKernelHandles()>>10;
		DbgUtility.println(DbgUtility.PRIORITY_OPERATION, "getCategoriesForSWMTHighBits(): " + String.format("0x%x", highBits)); //$NON-NLS-1$
		return highBits;
	}

	/**
	 * Gets SWMT categories that user wants to include into SWMT log.
	 * Includes always {@link SWMTCategoryConstants#CATEGORY_KERNELHANDLES} with it because SWMT Analyser
	 * needs it to be functional
	 * @return  SWMT categories that user wants to include into SWMT log.
	 */
	private int getCategoriesForSWMTWithKernelHandles() {
		// CATEGORY_KERNELHANDLES is always included into fetched categories because data is required by SWMT analyser plug-in
		//If SWMT Analyzer is modified so that Kernel Handles is not always needed then SWMTCategoryConstants.CATEGORY_KERNELHANDLES can be removed.
		return getCategoriesForSWMT() | SWMTCategoryConstants.CATEGORY_KERNELHANDLES;
	}	
	

	/**
	 * Gets SWMT categories that user wants to include into SWMT log.
	 * @return  SWMT categories that user wants to include into SWMT log.
	 */
	public int getCategoriesForSWMT() {
		int sessionSpecificSWMTCategorySetting = MemSpyPreferences.getSWMTCategorySetting();
		DbgUtility.println(DbgUtility.PRIORITY_OPERATION, "getCategoriesForSWMT(): " + String.format("0x%x", sessionSpecificSWMTCategorySetting)); //$NON-NLS-1$
		SWMTCategoryConstants.debugPrintSWMTCategorySetting(sessionSpecificSWMTCategorySetting);
		return sessionSpecificSWMTCategorySetting;
	}	
	
	/**
	 * Sets SWMT categories that user wants to include into SWMT log.
	 * @param categoriesForSWMT  SWMT categories that user wants to include into SWMT log.
	 * @param isProfileSettings <code>true</code> if these settings are profile settings
	 * <code>false</code> if these are custom settings
	 */
	public void setCategoriesForSWMT(int categoriesForSWMT, boolean isProfileSettings) {
		MemSpyPreferences.setSWMTCategorySetting(categoriesForSWMT, isProfileSettings);
	}
	
	/**
	 * Sets if User has select a Profile or not
	 * @param isProfileCategoriesSelected 
	 */
	public void setProfileTrackedCategoriesSelected(boolean isProfileCategoriesSelected) {
		MemSpyPreferences.setProfileTrackedCategoriesSelected(isProfileCategoriesSelected);
	}	
	
	/**
	 * Gets if User has select a Profile or not
	 * @return <code>true</code> if one of the profiles has been selected
	 */
	public boolean isProfileTrackedCategoriesSelected() {
		return MemSpyPreferences.isProfileTrackedCategoriesSelected();
	}		

	/**
	 * Gets SWMT HeapNameFilter to filter User Heaps in SWMT log.
	 * @return SWMT HeapNameFilter that user wants to include into SWMT log.
	 */
	public String getHeapNameFilterForSWMT() {
		String filter = MemSpyPreferences.getSWMTHeapNameFilter();
		
		DbgUtility.println(DbgUtility.PRIORITY_OPERATION, "getHeapNameFilterForSWMT(): " + filter); //$NON-NLS-1$		
		return filter;
	}	
	
	/**
	 * Sets SWMT HeapNameFilter to filter User Heaps in SWMT log.
	 * @param HeapNameFilter SWMT HeapNameFilter that user wants to include into SWMT log.
	 */
	public void setHeapNameFilterForSWMT(String heapNameFilterForSWMT) {
		MemSpyPreferences.setSWMTHeapNameFilter(heapNameFilterForSWMT);
	}
	
	/**
	 * Restarts error timer
	 */
	public void restartErrorTimer() {
		if( errorTimer != null && errorTimer.isRunning() ){
			errorTimer.restart();
		}
	}

	/**
	 * Notify about MemSpy Launcher S60 application version
	 * @param version in format "x", e.g "1".
	 */
	public void setMemSpyLauncherVersion(String version) {
		String msg = "MemSpy Launcher data version: '" +version +"' detected in S60 target.";
		DbgUtility.println(DbgUtility.PRIORITY_LOOP, msg);
		MemSpyConsole.getInstance().println(msg);
		
		receivedMemSpyLauncherDataVersion = Integer.parseInt(version);
	}

	/**
	 * Check if received MemSpy Launcher data version is at least required version
	 */
	private void checkMemSpyLauncherDataVersion() {
		int requiredVersion = ProductInfoRegistry.getRequiredMemSpyLauncherDataVersion();
		DbgUtility.println(DbgUtility.PRIORITY_LOOP, "Required MemSpy Launcher data version: " +requiredVersion);
		if(requiredVersion > receivedMemSpyLauncherDataVersion){		
			this.launcherError(LauncherErrorType.TOO_OLD_MEMSPY_LAUNCHER_DATAVERSION);
		}
	}

	/**
	 * Get Heap Dump files that has been imported during SWMT logging.
	 * @return list about imported Heap Dumps.
	 */
	public ArrayList<ThreadInfo> getImportedSWMTHeaps() { 
		return handler.getImportedSWMTHeaps();
	}

	/**
	 * Gets progress status for current wizard session with current connection settings.
	 * @return Progress status for current wizard session with current connection settings.
	 */
	public ProgressStatus getProgressStatus() {
		return progressStatus;
	}

	/**
	 * Sets progress status if there has been further progress after recent progress status update.
	 * @param progressStatus the progressStatus to set
	 */
	private void setProgressStatus(ProgressStatus progressStatus) {
		if(progressStatus.ordinal() > this.progressStatus.ordinal()){
			this.progressStatus = progressStatus;			
			DbgUtility.println(DbgUtility.PRIORITY_OPERATION, "setProgressStatus: " + this.progressStatus.name()); //$NON-NLS-1$
		}
	}

	/**
	 * Resets progress status back into initial value.
	 */
	public void resetProgressStatus() {
		this.progressStatus = ProgressStatus.EPROGRESS_INITIAL;
	}

	/**
	 * Delegates launcher error info further without any additional information 
	 * @param generalLauncherError launcher error occurred
	 */
	public void launcherError(LauncherErrorType launcherError) {
		launcherError(launcherError, "");
	}

	/**
	 * Gets possible additional information related to occurred error.
	 * @return string containing additional information related to occurred error
	 */
	public String getAdditionalErrorInformation() {
		return additionalErrorInformation;
	}

	/* (non-Javadoc)
	 * @see com.nokia.s60tools.memspy.export.ITraceClientNotificationsIf#notifyError(java.lang.String)
	 */
	public void notifyError(String message) {
		// Currently only showing trace errors on console
		MemSpyConsole.getInstance().println(message, MemSpyConsole.MSG_ERROR);		
	}

	/* (non-Javadoc)
	 * @see com.nokia.s60tools.memspy.export.ITraceClientNotificationsIf#notifyInformation(java.lang.String)
	 */
	public void notifyInformation(String message) {
		// Currently only showing trace informative messages on console
		MemSpyConsole.getInstance().println(message, MemSpyConsole.MSG_NORMAL);		
	}

	/* (non-Javadoc)
	 * @see com.nokia.s60tools.memspy.export.ITraceClientNotificationsIf#notifyWarning(java.lang.String)
	 */
	public void notifyWarning(String message) {
		// Currently only showing trace warnings on console
		MemSpyConsole.getInstance().println(message, MemSpyConsole.MSG_WARNING);		
	}

}
