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


package com.nokia.s60tools.memspy.ui;

import java.io.File;
import java.io.IOException;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.Display;

import com.nokia.s60tools.memspy.interfaces.IMemSpyTraceListener;
import com.nokia.s60tools.memspy.interfaces.IMemSpyTraceListener.LauncherErrorType;
import com.nokia.s60tools.memspy.model.TraceCoreEngine.ProgressStatus;
import com.nokia.s60tools.memspy.plugin.MemSpyPlugin;
import com.nokia.s60tools.memspy.util.MemSpyConsole;
import com.nokia.s60tools.util.debug.DbgUtility;

/**
 * Static UI utility methods that are needed more than one place
 * are located here in order to prevent code duplication.
 */
public class UiUtils {

	
	/**
	 * Launcher SIS file name
	 */
	private static final String MEM_SPY_LAUNCHER_S60_50_RN_D_SIGNED_SIS_FILE_NAME = "MemSpyLauncher_S60-50_RnD-signed.sis";
	
	/**
	 * Shows error dialog that gives error context related information and guidance.
	 * E.g. Advises user to install launcher component to the device if it is needed.
	 * @param error Launcher error encountered
	 * @param errorMessage Error message to be shown to user
	 * @param progressStatus 
	 */
	public static void showErrorDialogToUser(final LauncherErrorType error, final String errorMessage, final ProgressStatus progressStatus) {
		
		Runnable updateUiRunnable = new Runnable(){
			public void run(){
				
		        // If no answer from device received, display dialog that allows user to install MemSpy Launcher.
		 	    if( error == LauncherErrorType.NO_ANSWER_FROM_DEVICE || error == LauncherErrorType.TOO_OLD_MEMSPY_LAUNCHER_DATAVERSION ){
		 	    	
					switch (progressStatus) {		
						// Flow through on purpose 1
						case EPROGRESS_MEMSPY_LAUNCHED: // MemSpy is launched but not yet actual task
						case EPROGRESS_FIRST_TASK_LAUNCHED: // MemSpy is launched and also 1st real task is done
						case EPROGRESS_FIRST_TASK_DONE: // Trace data has been received successfully at least once
				 	    	showStandardErrorMessageDialog(errorMessage);
							break;							
						// Flow through on purpose 2
						case EPROGRESS_INITIAL: // MemSpy has not been launched successfully, so no progress at all					
						default:
				 	    	adviceUserToInstallLauncherComponent(errorMessage);
							break;
					}
		 	    }
		 	    else{
		 	    	showStandardErrorMessageDialog(errorMessage);
		 	    }
		 	    
			}

		};
		
		Display.getDefault().asyncExec(updateUiRunnable);
	}

	/**
	 * Shows standard eclipse error dialog with given error message
	 * @param errorMessage error message
	 */
	private static void showStandardErrorMessageDialog(String errorMessage) {
		Status status = new Status(IStatus.ERROR, MemSpyPlugin.PLUGIN_ID, 0, errorMessage, null);
        // Display the dialog
 	    ErrorDialog.openError(Display.getCurrent().getActiveShell(),IMemSpyTraceListener.ERROR_MEMSPY, null, status);
	}

	/**
	 * Advises used to install MemSpy launcher component and provides necessary action alternatives.
	 * @param errorMessage error message
	 */
	private static void adviceUserToInstallLauncherComponent(final String errorMessage) {
	    	
	    	MessageDialog dialog = new MessageDialog( Display.getCurrent().getActiveShell(), 
	    			IMemSpyTraceListener.ERROR_MEMSPY, null, errorMessage, MessageDialog.ERROR, 
	    			new String[]{ "Install RnD-signed MemSpy Launcher", "Open sis-file's directory in Explorer", "Don't install" }, 1);
	    	dialog.open();		 	    	
	    	
		String launcherFolder = MemSpyPlugin.getDefault().getMemspyLauncherBinDir();
	    	String launcherLocation = launcherFolder + File.separatorChar + MEM_SPY_LAUNCHER_S60_50_RN_D_SIGNED_SIS_FILE_NAME;					
	    	
	    	// if user wants to install launcher:
	    	if( dialog.getReturnCode() == 0 ){
				// find program for xls-filetype
				Program p=Program.findProgram(".sis");
				// if found, launch it.
				if(p!=null){
					// Check that found program was Nokia PC Suite.
					p.execute( launcherLocation );
				}
				else{
					Status status = new Status(IStatus.ERROR, MemSpyPlugin.PLUGIN_ID, 0, 
							"Unable to locate PC suite or other suitable software for installing .sis -file from computer. You can try installing MemSpy launcher manually from:\n"
							+ launcherLocation, null);
		 	    	ErrorDialog.openError(Display.getCurrent().getActiveShell(),"MemSpy Error", null, status);	
				}
				
	    	}
	    	
	    	// Open directory in explorer
	    	else if( dialog.getReturnCode() == 1 ){
	    		try {
					String directory = Platform.getConfigurationLocation().getURL().getFile();
					directory = directory.substring(1);
					Runtime.getRuntime().exec("explorer " + launcherFolder);
				} catch (IOException e) {
					Status status = new Status(IStatus.ERROR, MemSpyPlugin.PLUGIN_ID, 0, "Unable to open Explorer", null);
	 	    	ErrorDialog.openError(Display.getCurrent().getActiveShell(),IMemSpyTraceListener.ERROR_MEMSPY, null, status);	
					e.printStackTrace();
				}
	    	}
	    	
	}
	
	/**
	 * Maps given launcher error to corresponding error message. 
	 * @param error error enumerator
	 * @param clientContextString client context string for giving usage context info. Used only for possible console logging.
	 * @param progressStatus current progress status for giving extra information on the possible error condition.
	 * @return error message string
	 */
	public static String getErrorMessageForLauncherError(LauncherErrorType error, String clientContextString, ProgressStatus progressStatus) {
		
		String errorMessage;
		
		DbgUtility.println(DbgUtility.PRIORITY_OPERATION, "getErrorMessageForLauncherError/progressStatus: " + progressStatus); //$NON-NLS-1$
		
 		switch (error){
	 		case NO_ANSWER_FROM_DEVICE:{
	 			errorMessage = getErrorDescriptionForNoAnswerFromDeviceError(progressStatus, clientContextString);
	 			break;
	 		}
	 		case MEMSPY_NOT_RUNNING:{
	 			errorMessage = IMemSpyTraceListener.ERROR_MEMSPY_NOT_RUNNING;
	 			break;
	 		}
	 		case MEMSPY_NOT_FOUND:{
	 			errorMessage = IMemSpyTraceListener.ERROR_MEMSPY_NOT_FOUND;
	 			break;
	 		}
	 		case ACTIVATION:{
	 			errorMessage = IMemSpyTraceListener.ERROR_ACTIVATION_NOT_SUCCESFUL;
	 			break;
	 		}
	 		case FILE:{
	 			errorMessage = IMemSpyTraceListener.ERROR_FILE_OPERATIONS_NOT_SUCCESSFUL;
	 			break;
	 		}
	 		case TOO_OLD_MEMSPY_LAUNCHER_DATAVERSION:{
	 			errorMessage = IMemSpyTraceListener.ERROR_TOO_OLD_MEMSPY_LAUNCHER_VERSION;
	 			break;
	 		}
	 		case DUMPED_TRACES:{
	 			errorMessage = IMemSpyTraceListener.ERROR_DUMPED_TRACES;
				break;
	 		}
	 		case CATEGORIES_NOT_SUPPORTED:{
	 			errorMessage = IMemSpyTraceListener.ERROR_CATEGORIES_NOT_SUPPORTED;	 
	 			break;
	 		}
	 		case GENERAL_LAUNCHER_ERROR:{
	 			errorMessage = IMemSpyTraceListener.ERROR_GENERAL_LAUNCHER_ERROR;	 
	 			break;
	 		}	 		
	 		// default handling in case new launcher error has been added but not handled appropriately  
	 		default:{
	 			MemSpyConsole.getInstance().println(clientContextString + " error: '" //$NON-NLS-1$ 
							+ error.name() + "' occurrence."		  //$NON-NLS-1$
							, MemSpyConsole.MSG_ERROR);		  //$NON-NLS-1$
	 			errorMessage = IMemSpyTraceListener.ERROR_ACTIVATION_NOT_SUCCESFUL;
	 			break;	 			
	 		} 		
		}

 		return errorMessage;
	}

	/**
	 * Forms error description in no answer from device error situation.
	 * @param progressStatus progress status at the moment when error occurred.
	 * @param clientContextString Possibly more context-specific information
	 * @return 
	 */
	private static String getErrorDescriptionForNoAnswerFromDeviceError(ProgressStatus progressStatus, String clientContextString) {

			// Default message start portion
			String errorMessage = IMemSpyTraceListener.ERROR_NO_RESPONSE + " " + IMemSpyTraceListener.ERROR_POSSIBLE_REASONS;
			
			switch (progressStatus) {
			
				// Flow through on purpose 1
				case EPROGRESS_MEMSPY_LAUNCHED: // MemSpy is launched but not yet actual task
				case EPROGRESS_FIRST_TASK_DONE: // Trace data has been received successfully at least once
				case EPROGRESS_FIRST_TASK_LAUNCHED: // MemSpy is launched and also 1st real task is done
		 			// add USB error note.
		 			errorMessage = errorMessage + IMemSpyTraceListener.ERROR_USB_TRACE_ENABLED;		 							
		 			// add check for connection information message
		 			errorMessage = errorMessage + IMemSpyTraceListener.ERROR_CONNECTION_BROKEN;
					break;
					
				// Flow through on purpose 2					
				case EPROGRESS_INITIAL: // MemSpy has not been launched successfully, so no progress at all					
				default:
					errorMessage = IMemSpyTraceListener.ERROR_NO_RESPONSE + " " + IMemSpyTraceListener.ERROR_POSSIBLE_REASONS;	 			
		 			// add USB error note.
		 			errorMessage = errorMessage + IMemSpyTraceListener.ERROR_USB_TRACE_ENABLED;		 		
		 			// add install note for MemSpy Launcher
		 			errorMessage = errorMessage + IMemSpyTraceListener.ERROR_INSTALL_MEMSPY_LAUNCHER;
					break;
			}
			
			if(clientContextString.length() > 0){
	 			MemSpyConsole.getInstance().println(IMemSpyTraceListener.ERROR_NO_RESPONSE
							+ IMemSpyTraceListener.ERROR_LAUNCHER_ERROR_DETAILS + clientContextString		  //$NON-NLS-1$
							, MemSpyConsole.MSG_ERROR);		  				
	 			errorMessage = errorMessage + IMemSpyTraceListener.ERROR_SEE_CONSOLE_LOG;
			}
			
		return errorMessage;
	}

}
