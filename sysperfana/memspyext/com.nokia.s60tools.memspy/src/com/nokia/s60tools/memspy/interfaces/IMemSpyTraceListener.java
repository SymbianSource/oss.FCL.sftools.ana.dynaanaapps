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



package com.nokia.s60tools.memspy.interfaces;

import com.nokia.s60tools.memspy.containers.SWMTLogInfo;


/**
 * interface MemSpyTraceListener
 * MemSpy trace listener interface that all MemSpy Trace listeners implement
 *
 */

public interface IMemSpyTraceListener {
	
	/**
	 * MemSpy Launchers actions.
	 */
	public enum LauncherAction{ 
		START_MEMSPY_THREAD_LIST, 
		GET_THREAD_LIST, 
		GET_HEAP_DUMP, 
		SWMT_UPDATE,
		TIMED_SWMT_UPDATE
		};	
		
	/**
	 * Launch error types.
	 */
	public enum LauncherErrorType{
		
		// The order of 4 first launcher error enumerators should not be changed because the error codes 
		// sent from launcher side must match enumerator ordinals.
		
		GENERAL_LAUNCHER_ERROR, 		// This is general launcher error when there is no error code, only error description.
		MEMSPY_NOT_RUNNING, 			// error code/ordinal=1, MemSpy is not running on device as should have been at the state of execution
		MEMSPY_NOT_FOUND,  				// error code/ordinal=2, MemSpywas not installed on device
		HEAP_NOT_FOUND,  				// error code/ordinal=3, Requested heap was not found from device
		
		// Ordinals of other errors are not used as error codes at launcher side,  
		// and therefore they can be freely re-organized if needed.	
		
		HEAP_TYPE_WRONG, 						//  				
		NO_ANSWER_FROM_DEVICE, 					//   				 				 
		DUMPED_TRACES, 							//  
		ACTIVATION, 							// Activation of MemSpy functionality via MemSpyLauncher has failed
		FILE,  									// 
		CATEGORIES_NOT_SUPPORTED, 				// MemSpy installed on the device does not support setting of custom categories
		TOO_OLD_MEMSPY_LAUNCHER_DATAVERSION 	// MemSpyLauncher installed supports only older data version 
		};
		   
	public final static String  ERROR_HEAP_TYPE 					= "Selected heap's heap type is wrong(needs to be Symbian OS RHeap).";
	public final static String  ERROR_NO_RESPONSE 					= "MemSpy extension was unable to receive response from MemSpy S60-application.";
	public final static String  ERROR_POSSIBLE_REASONS 				= "Following reason(s) might be causing this problem: ";
	public final static String  ERROR_MEMSPY						= "MemSpy Error";
	public final static String  ERROR_USB_TRACE_ENABLED 			= "\n- Trace data sending is not activated from your TraceSwitch application.";
	public final static String  ERROR_INSTALL_MEMSPY_LAUNCHER		= "\n- MemSpy Launcher application is not installed in your device, or the installed launcher version is not up-to-date.\n\n(Installing MemSpy Launcher requires that Nokia PC suite is found from this computer and device is connected with usb-cable.) ";
	public final static String  ERROR_CONNECTION_BROKEN 			= "\n- Network connection between workstation and Musti box does not work properly.";
	public final static String  ERROR_MEMSPY_NOT_RUNNING 			= "MemSpy is not running on device. MemSpy will be restarted when you perform next operation. ";
	public final static String  ERROR_MEMSPY_NOT_FOUND 				= "MemSpy s60-application was not found from your device.";
	public final static String  ERROR_HEAP_NOT_FOUND 				= "Requested heap not found from device.( Perhaps thread has been terminated after loading of thread list,  try reloading thread list. )";
	public final static String  ERROR_DUMPED_TRACES 				= "Received Heap Dump/SWMT-log is corrupted. See help for more information.";
	public final static String  ERROR_SWMT_NEEDS_RESET				= "\n\nSince last imported log was missed, logs cannot be imported anymore before removing all logs.";
	public final static String  ERROR_ACTIVATION_NOT_SUCCESFUL		= "Unable to send activation messages to TraceCore. Check connection settings.";
	public final static String  ERROR_FILE_OPERATIONS_NOT_SUCCESSFUL= "MemSpy was unable to write data to hard drive. Please make sure that MemSpy has sufficient user rights to Carbide's workspace.";
	public final static String  ERROR_CONNECTION					= "Cannot connect to trace. Check connection settings.";
	public final static String  ERROR_CATEGORIES_NOT_SUPPORTED	    = "SWMT category setting feature is not supported by the MemSpy installed on the device and will be disabled for the rest of the session. Re-run the operation without category support.";
	public final static String  ERROR_TOO_OLD_MEMSPY_LAUNCHER_VERSION = "Installed launcher version is not up-to-date.\n\n(Installing MemSpy Launcher requires that Nokia PC suite is found from this computer and device is connected with usb-cable.";
	public static final String  ERROR_GENERAL_LAUNCHER_ERROR = "An unexpected error occurred in launcher component. See console log for detailed error information.";
	public final static String  ERROR_SEE_CONSOLE_LOG				= "\n\nSee console log for additional error information.";
	public final static String  ERROR_LAUNCHER_ERROR_DETAILS		= "S60 MemSpy Launcher error details: ";
	
	/**
	 * deviceError.
	 * method that is called whenever errors occur.
	 * @param error error code
	 */
	public void deviceError( LauncherErrorType error );
	
	/**
	 * operationFinished.
	 * @param action which action was finished
	 */
	public void operationFinished( LauncherAction action );
	
	/**
	 * operationFinished.
	 * @param action which action was finished
	 * @param swmtLog that was received
	 */
	public void operationFinished( LauncherAction action, SWMTLogInfo swmtLogInfo, boolean timerRunning );
	
	/**
	 * startedReceivingSWMTLog.
	 * notification that is sent to interface when swmtReceiving is started.
	 */
	public void startedReceivingSWMTLog();
}
