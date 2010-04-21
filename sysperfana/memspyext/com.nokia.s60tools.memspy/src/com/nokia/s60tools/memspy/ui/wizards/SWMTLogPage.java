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



package com.nokia.s60tools.memspy.ui.wizards;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.PlatformUI;

import com.nokia.s60tools.memspy.containers.SWMTLogInfo;
import com.nokia.s60tools.memspy.containers.SWMTLogInfo.SWMTLogType;
import com.nokia.s60tools.memspy.interfaces.IMemSpyTraceListener;
import com.nokia.s60tools.memspy.model.MemSpyFileOperations;
import com.nokia.s60tools.memspy.model.MemSpyLogParserEngine;
import com.nokia.s60tools.memspy.model.TraceCoreEngine;
import com.nokia.s60tools.memspy.model.UserEnteredData;
import com.nokia.s60tools.memspy.model.UserEnteredData.ValueTypes;
import com.nokia.s60tools.memspy.plugin.MemSpyPlugin;
import com.nokia.s60tools.memspy.preferences.MemSpyPreferences;
import com.nokia.s60tools.memspy.resources.HelpContextIDs;
import com.nokia.s60tools.memspy.ui.UiUtils;
import com.nokia.s60tools.ui.preferences.PreferenceUtils;
import com.nokia.s60tools.ui.wizards.S60ToolsWizardPage;
import com.nokia.s60tools.util.debug.DbgUtility;

/**
 * SWMT-log import page is used for importing SWMT-logs from file system or from device.
 */

public class SWMTLogPage extends S60ToolsWizardPage implements SelectionListener, IMemSpyTraceListener, SWMTCategorySelectionMediator{	
	
	/**
	 * Selection index for import from device via TraceViewer radio button
	 */
	private static final int DEVICE_RADIO_BUTTON_SELECTION_INDEX = 2;
	
	// Array list of logs that are imported
	// from files:
	private ArrayList<SWMTLogInfo> fileLogList;
	// from device:
	private ArrayList<SWMTLogInfo> deviceLogList;
	
	// SWMT-log that is currently received
	SWMTLogInfo receivedLog;
	
	// TraceCore engine
	private TraceCoreEngine traceEngine;
	
	// Cycle Number for SMWT-log file name
	private int cycleNumber;
	
	// Error message
	String errorMessage;
	
	// boolean value stating that error has been occurred and logs cannot be requested before removing 
	// all items and restarting from cycle 1.
	boolean missedLogs;
	
	//UI-components:
	private Group radioButtonGroup;
	private Button fileRadioButton;
	private Button deviceRadioButton;

	// Components for importing file from device 
	private Table fileLogsTable;
	private Composite fileSelectionButtonComposite;
	private Button addFileButton;
	private Button addDirectoryButton;
	private Button removeOneButton;
	private Button removeAllButton;
	
	private Group fileSelectionGroup;
	private Group deviceGroup;
	
	private GridData fileSelectionGridData;
	private GridData deviceGridData;

	
	// components for importing log from device	
	private Table deviceLogsTable;
	private Composite loggingComposite;
	private Button connectionSettingsButton;
	private Button getLogNowButton;
	private Button removeReceivedLogsButton;
	private Label connectionNameInUseLabel;

	// Timer related components
	private Label intervalLabel;
	private Label secondLabel;
	private Button startTimerButton;
	private Combo timerCombo;

	/**
	 * UI composite for SWMT Category group
	 */
	private SWMTCategoryGroupComposite categoryGroupComposite;	
	
	// boolean variable, which is set to true if some MemSpy operation is running.
	boolean memSpyOperationRunning;
	boolean memSpyStopping;
	boolean memSpyTimerRunning;
	
	// MemSpy operation processes:
	// receive SWMT-log manually
	IRunnableWithProgress receiveSWMTLogProcess;
	
	private final static String  GET_LOG_FROM_RADIO_BUTTON 			= "Get SWMT Log";
	private final static String  GET_FROM_FROM_FILE_RADIO_BUTTON 	= "From File System";
	private final static String  GET_LOG_FROM_DEVICE_RADIO_BUTTON 	= "From Device via TraceViewer";
	private final static String  LOG_FILES							= "Log files:";
	private final static String  LOG_TYPE							= "Type";
	private final static String  ADD_FILE							= "Add File";
	private final static String  ADD_DIRECTORY						= "Add Directory";
	private final static String  REMOVE_ONE							= "Remove";
	private final static String  REMOVE_ALL							= "Remove All";
	private final static String  ADD_FILE_TEXT			 			= "Define Location of SWMT Log file:";
	private final static String  ADD_DIRECTORY_TEXT		 			= "Define Location of directory that contains SWMT Logs:";
	private final static String  LOG_TYPE_FILE						= "File";
	private final static String  LOG_TYPE_DIRECTORY					= "Directory";	
	
	private final static String  CONNECTION_SETTINGS_BUTTON 		= "Connection Settings...";
	private final static String  CURRENTLY_USING_TEXT 				= "Currently using:";
	private final static String  GET_LOG_NOW_BUTTON 				= "Get SWMT Log Now";
	private final static String  LOGS 								= "Logs";
	private final static String  RECEIVED							= "Received";
	
	private final static String  RECEIVING_SWMT_LOG 				= "Receiving SWMT Log: ";
	private final static String  WAITING_FOR_TIMER					= "Waiting for timer to expire: ";
	private final static String  STOPPING_TIMER						= "Stopping timer: ";
	
	private final static String  TEXT_GET_LOG_WITH_TIMER			= "Get Log with Timer";
	private final static String  TEXT_INTERVAL						= "Interval:";
	private final static String  START_TIMER						= "Start Timer";
	private final static String  TEXT_SECOND						= "Seconds";
	
	private final static String  ERROR_INTERVAL						= "Time interval needs to be integer and more than zero.";
	
    /**
     * Constructor.
     * @param pageName name of the page
     * @param traceEngine TraceCore engine that is used when requesting data from TC
     */
    protected SWMTLogPage(String pageName, TraceCoreEngine traceEngine) {
		super(pageName);
		setTitle("System Wide Memory Tracking Wizard");
		setDescription("Define Source For SWMT logs that are imported. Logs can be imported from already existing files or from device via TraceViewer.");
		this.fileLogList = new ArrayList<SWMTLogInfo>();
		this.deviceLogList = new ArrayList<SWMTLogInfo>();
		this.traceEngine = traceEngine;
		this.createMemSpyProcesses();
		
		this.memSpyTimerRunning = false;
		this.memSpyOperationRunning = false;
		this.memSpyStopping = false;
		this.cycleNumber = 0;
		
		this.missedLogs = false;
    }
    
	@Override
	public void recalculateButtonStates() {
		// no implementation needed
	}

	@Override
	public void setInitialFocus() {
		timerCombo.setFocus();
	}

    /*
     * (non-Javadoc)
     * @see org.eclipse.swt.events.SelectionListener#widgetDefaultSelected(org.eclipse.swt.events.SelectionEvent)
     */
	public void widgetDefaultSelected(SelectionEvent e) {
		// Not needed in this case
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.swt.events.SelectionListener#widgetSelected(org.eclipse.swt.events.SelectionEvent)
	 */
	public void widgetSelected(SelectionEvent e) {
		// Update controls after radio buttons state changes to false
		if ( ( e.widget == deviceRadioButton && deviceRadioButton.getSelection() == false ) ||
			 ( e.widget == fileRadioButton && fileRadioButton.getSelection() == false ) ){
			//When selection is changed, we clear the previous results.
			removeReceivedLogs();
			this.hideAndRevealItems();
		}	

		// open file dialog
		else if (e.widget == addFileButton) {
			// open file dialog for selecting a crash file
			FileDialog dialog = new FileDialog(this.getShell(), SWT.MULTI);
			dialog.setText(ADD_FILE_TEXT);
			String[] filterExt = { "*.txt", "*.log", "*.*" };
			dialog.setFilterExtensions(filterExt);
			
			if (dialog.open() != null ){
		        String[] files = dialog.getFileNames();
		        String directory = dialog.getFilterPath();
		        directory = MemSpyFileOperations.addSlashToEnd( directory );
		        
		        boolean noLogFound = false;
		        
		        for( String item : files ){
		        	
		        	// confirm that file is smwt log
		        	if( MemSpyLogParserEngine.isFileSWMTLog( new File(directory + item)) ){
		        		this.addFileLog( directory + item );	
		        	}
		        	
		        	else{
		 				noLogFound = true;
		        	}
		        }
		        
		        // if some of the selected files is not swmt-file show error message.
		        if( noLogFound ){
	        		Status status = new Status(IStatus.ERROR, MemSpyPlugin.PLUGIN_ID, 0, "Some of the selected files is not SWMT-log file.", null);
			        // Display the dialog
			 	    ErrorDialog.openError(Display.getCurrent().getActiveShell(),ERROR_MEMSPY, null, status);
		        }
			}
			
			
		}
		
		// open directory dialog  
		else if (e.widget == addDirectoryButton) {
			// open file dialog for selecting a crash file
			DirectoryDialog dialog = new DirectoryDialog(this.getShell());
			dialog.setText(ADD_DIRECTORY_TEXT);
			String result = dialog.open();
			if (result != null ){
				
				File[] allFiles = MemSpyFileOperations.getFilesFromDirectory( new File( result ) );

				int swmtFileCount = 0;
				
				for ( File item : allFiles ){
					if( MemSpyLogParserEngine.isFileSWMTLog( item ) ){
						swmtFileCount++;
						this.addFileLog( item.toString() );
					}
				}
				// if no SWMT-log files were found from selected directory, show error message.
				if(swmtFileCount == 0){
	 				Status status = new Status(IStatus.ERROR, MemSpyPlugin.PLUGIN_ID, 0, "No SWMT-logs were found from selected directory.", null);
	 				// Display the dialog
			 	    ErrorDialog.openError(Display.getCurrent().getActiveShell(),ERROR_MEMSPY, null, status);
	        	
					
				}
			}
		}
		
		// remove selected log file 
		else if (e.widget == removeOneButton ){
			if( fileLogsTable.getSelectionCount() == 1 ){
				fileLogList.remove( fileLogsTable.getSelectionIndex() );
				this.refreshFileLogTable();
			}
		}
		
		// remove all log files
		else if (e.widget == removeAllButton ){
			fileLogList.clear();
			this.refreshFileLogTable();
		}
		
		// open connection settings
		else if ( e.widget == connectionSettingsButton ){
			// Open connection Trace viewers connection settings.
			Shell shell = MemSpyPlugin.getCurrentlyActiveWbWindowShell();
			PreferenceUtils.openPreferencePage(MemSpyPlugin.getTraceProvider().getTraceSourcePreferencePageId(), shell);
			
			// Disconnect trace source so that new settings are used when sending next request
			MemSpyPlugin.getTraceProvider().disconnectTraceSource();
			
			// get wizard pointer and cast it to MemSpyWizard
			MemSpyWizard wizard = (MemSpyWizard) this.getWizard();
			// update new settings to each wizard page.
			wizard.updateConnectionSettings();
		}
		
		// get log now from TC
		else if (e.widget == getLogNowButton ){
			this.getLogNowPressed();
		}
		
		// Remove all log received from device-button
		else if( e.widget == removeReceivedLogsButton ){
			this.removeReceivedLogs();
		}
		
		// SWMT timer started
		else if( e.widget == startTimerButton ){
			this.getLogWithTimerPressed();
		}	
				
	
		// if file logs table selection changes or button is pressed, enable and disable buttons
		if( e.widget == this.fileLogsTable || 
			e.widget == this.removeAllButton || 
			e.widget == this.removeOneButton || 
			e.widget == this.addFileButton || 
			e.widget == this.addDirectoryButton ){
			this.enableAndDisableFileButtons();
		}
		getWizard().getContainer().updateButtons();

	}
	
	/**
	 * Checks if wizard can be finished or not.
	 * @return boolean value if wizard can finish
	 */
	public boolean canFinish(){
		
		// if traceEngines taskList is not empty, return false
		if( traceEngine.getFirstTask() != null ){
			return false;
		}
		
		// if file or device log lists are not empty(depending on radiobutton selection) return true
		if( fileRadioButton.getSelection() ){
			if( fileLogList.size() > 0 ){
				return true;
			}
		}
		else{
			if( deviceLogList.size() > 0 ){
				return true;
			}
		}
		return false;
	}
	
	
	/**
	 * Hides and reveals controls according to radio button states.
	 */
	private void hideAndRevealItems() {
		
		boolean fileSelected = true;
		
		if( fileRadioButton.getSelection() == false ){
			fileSelected = false;
		}

		// exclude/include needed controls
		fileSelectionGridData.exclude = !fileSelected;
		deviceGridData.exclude = fileSelected;

		if( !fileSelected ){
			// Since excluded groups size and location are not updated, do it now
			deviceGroup.setSize( fileSelectionGroup.getSize() );
			deviceGroup.setLocation(fileSelectionGroup.getLocation());		
		}
		else{
			// Since excluded groups size and location are not updated, do it now
			fileSelectionGroup.setSize( deviceGroup.getSize() );
			fileSelectionGroup.setLocation(deviceGroup.getLocation());
	
		}
		// Hide/show needed controls
		fileSelectionGroup.setVisible(fileSelected);
		deviceGroup.setVisible(!fileSelected);
		
	}
	
	/**
	 * Adds a log file.
	 * @param location of log file
	 */
	private void addFileLog( String location ){
		
		// create SWMTLogInfo object for log file.
		SWMTLogInfo info = new SWMTLogInfo();
		info.setType( SWMTLogType.FILE );
		info.setPath( location );
		fileLogList.add(info);
		
		// get files name
		String fileName = location.substring( location.lastIndexOf("\\") + 1 );
		
		// add name into table
		TableItem item = new TableItem( fileLogsTable, SWT.NONE, fileLogsTable.getItemCount() );
		item.setText( new String[]{ fileName, LOG_TYPE_FILE });
		
	}
	
	/**
	 * Updates file log table so that it contains same data that file log list.
	 */
	private void refreshFileLogTable(){
		//Remove all items
		fileLogsTable.removeAll();
		
		for( int i = 0; i < fileLogList.size(); i++ ){
			TableItem newItem = new TableItem(fileLogsTable, SWT.NONE,i);

			// get item
			SWMTLogInfo log = fileLogList.get(i);
			String fileName = log.getPath();
			String fileType = LOG_TYPE_DIRECTORY;

			// Remove path if type is file
			if( log.getType() == SWMTLogType.FILE ){
				fileName = fileName.substring( fileName.lastIndexOf("\\") + 1 );
				fileType = LOG_TYPE_FILE;
			}
			
			newItem.setText( new String[]{ fileName, fileType } );
		}
		 
	}
		
	/**
	 * Removed received logs.
	 */
	private void removeReceivedLogs(){
		// Delete all items in deviceLogList and deviceLogsTable
		deviceLogList.clear();
		deviceLogsTable.removeAll();

		// Delete all temp files
		MemSpyFileOperations.deleteTempMemSpyFiles();
		
		// set cycle number to zero
		cycleNumber = 0;

		// reset missed logs value
		missedLogs = false;
		
		// enable and disable buttons
		this.enableAndDisableDeviceButtonsAndSWMTCategoryGroup();
	}
	
	/**
	 * Gets list of log files that are in currently selected table.
	 * @return list of log files that are in currently selected table
	 */
	public ArrayList<SWMTLogInfo> getLogList(){
		if( fileRadioButton.getSelection() ){
			return fileLogList;
		}
		else{
			return deviceLogList;
		}
	}
	
 	/**
 	 * Updates connection text to match used settings.
 	 */
 	public void updateConnectionText(){
		// Updating connection name.
 		String displayName = MemSpyPlugin.getTraceProvider().getDisplayNameForCurrentConnection();
 		connectionNameInUseLabel.setText(displayName);
 		loggingComposite.layout(); 			
 	}
 	
	/**
	 * Loads previous values into UI components.
	 */	
	private void loadUserEnteredData(){

		// if last value is not found, set selection file.		
		UserEnteredData data = new UserEnteredData();
		int lastUsedSource = data.getPreviousRadioButtonSelection( ValueTypes.SWMT );
		
		// Restoring previous state of radio buttons only if device import is also possible and was previously selected
		if(MemSpyPlugin.isTraceProviderAvailable() && lastUsedSource == DEVICE_RADIO_BUTTON_SELECTION_INDEX){
			deviceRadioButton.setSelection( true );
			fileRadioButton.setSelection( false );
		}
		else{
			deviceRadioButton.setSelection( false );
			fileRadioButton.setSelection( true );			
		}
		
		// Restore previous values to file combobox
		String[] lastUsedFiles = data.getPreviousValues( ValueTypes.SWMT );
		if (lastUsedFiles != null) {
			timerCombo.setItems(lastUsedFiles);
			timerCombo.select(0);
		}

	}	
	
	/**
	 * Saves user entered values and selections from UI components so that they can be restored later if needed.
	 */
	public void saveUserEnteredData(){
		UserEnteredData data = new UserEnteredData();
			
		// Save Action radio-buttons state
		if( deviceRadioButton.getSelection() ){
			data.saveRadioButtonSelection(ValueTypes.SWMT, 2);
		}
		else {
			data.saveRadioButtonSelection(ValueTypes.SWMT, 1);
		}
		data.saveValue(ValueTypes.SWMT, timerCombo.getText());
	}

	/*
	 * (non-Javadoc)
	 * @see com.nokia.s60tools.memspy.ui.wizards.MemSpyTraceListener#deviceError(com.nokia.s60tools.memspy.ui.wizards.MemSpyTraceListener.LauncherErrorType)
	 */
 	public void deviceError( final LauncherErrorType error ){
 		
 		Date date = new Date (System.currentTimeMillis());
 		DbgUtility.println(DbgUtility.PRIORITY_OPERATION, "SWMTLogPage.deviceError: '" + error.name() +"' time:'" +date.toString() + "'."); //$NON-NLS-1$ $NON-NLS-2$ $NON-NLS-3$
 		
 		// Set MemSpy's state correct
 		memSpyOperationRunning = false;
 		memSpyTimerRunning = false;
 		
		// change Cancel text from wizard back to "Cancel"
 		this.setCancelText( "Cancel" );
 		
 		// Getting user visible error message
 		errorMessage = UiUtils.getErrorMessageForLauncherError(error, traceEngine.getAdditionalErrorInformation(), traceEngine.getProgressStatus()); //$NON-NLS-1$

 		// Handling SWMT-specific logic related to the error  		
 		switch (error){
	 		case DUMPED_TRACES:{
				this.receivedLog = null;
				break;
	 		}
	 		case CATEGORIES_NOT_SUPPORTED:{
	 			disableSWMTCategoryFeature();
	 			break;
	 		}
		}
 		
 		// Add reset info into error message.
 		if(error != LauncherErrorType.CATEGORIES_NOT_SUPPORTED){
 	 		errorMessage = errorMessage + ERROR_SWMT_NEEDS_RESET; 			
 		}
 	
 		// Set missedLogs value to true so that logs cannot be requested anymore because of missed logs.
 		missedLogs = true;
 		
 		// Advising user to install launcher component to the device
 		UiUtils.showErrorDialogToUser(error, errorMessage, traceEngine.getProgressStatus());   
 	}

	/**
	 * Disables SWMT category feature for the rest of the session in case is was not supported by the device.
	 */
	private void disableSWMTCategoryFeature() {
		Runnable disableRunnable = new Runnable(){

			public void run() {
				try {
					MemSpyPlugin.getDefault().setSWMTCategorySettingFeatureEnabled(false);
					MemSpyPreferences.setProfileTrackedCategoriesSelected(true);
					categoryGroupComposite.disableCustomCategorySelection();
				} catch (Exception e) {
					e.printStackTrace();					
				}
			}
			
		};
		
		Display.getDefault().asyncExec(disableRunnable);
	}

	/*
	 * (non-Javadoc)
	 * @see com.nokia.s60tools.memspy.ui.wizards.MemSpyTraceListener#operationFinished(com.nokia.s60tools.memspy.ui.wizards.MemSpyTraceListener.LauncherAction)
	 */
	public void operationFinished(LauncherAction action) {
	
		// reset missed logs.
		missedLogs = false;
		
		if( action == LauncherAction.SWMT_UPDATE ){
			cycleNumber++;
			this.updateReceivedSWMTLog( false );
 		}		
				
	}
 	
	/**
	 * Does actions that are made when SWMT log is received.
	 */
	private void updateReceivedSWMTLog( final boolean timerRunning ){
		
		DbgUtility.println(DbgUtility.PRIORITY_OPERATION, "SWMTLogPage.updateReceivedSWMTLog/timerRunning=" + timerRunning ); //$NON-NLS-1$
		
		Runnable updateUiRunnable = new Runnable(){
			public void run(){
 			
		 		// get date formatter
				SimpleDateFormat formatter = new SimpleDateFormat ( MemSpyFileOperations.DATEFORMAT );
				String date = formatter.format(receivedLog.getDate() );
	 			// get files name
	 			String path	= receivedLog.getPath().substring( receivedLog.getPath().lastIndexOf("\\") + 1 );
				
	 			// add log into table
	 			TableItem item = new TableItem( deviceLogsTable, SWT.NONE, deviceLogsTable.getItemCount() );
	 			item.setText( new String[]{ path, date });
	 				 			
	 			// add receivedlog-object into deviceLogsList-Arraylist
	 			deviceLogList.add( receivedLog );

	 			if( timerRunning ){ 
	 				// show progress bar
	 				memSpyTimerRunning = true;
	 				memSpyOperationRunning = false;
	
	 			}
	 			else{

	 				// change Cancel text from wizard back to "Cancel"
	 				setCancelText("Cancel");

	 				// Set state correct
	 				memSpyOperationRunning = false;

	 			}
			}

		};
		Display.getDefault().asyncExec(updateUiRunnable);
	}
 	
 	/**
 	 * Enables and disables device buttons and SWMT category group UI according to current situation
 	 */
 	private void enableAndDisableDeviceButtonsAndSWMTCategoryGroup(){

		// Refreshes enable/disable status for SWMT category group UI
		categoryGroupComposite.refresh();

		// if log table is not empty, enable remove all button.
		if( this.deviceLogsTable.getItemCount() > 0 ){
			this.connectionSettingsButton.setEnabled(false);
			this.removeReceivedLogsButton.setEnabled(true);
			this.categoryGroupComposite.setButtonsEnabled(false);
			this.categoryGroupComposite.setEnabled(false);
			if( this.missedLogs ){
	 			this.getLogNowButton.setEnabled(false);
	 			this.startTimerButton.setEnabled(false);
	 		}
		}
		else{
			this.connectionSettingsButton.setEnabled(true);
			this.removeReceivedLogsButton.setEnabled(false);
			this.categoryGroupComposite.setButtonsEnabled(true);
			this.categoryGroupComposite.setEnabled(true);
	 		if( !this.missedLogs ){
	 			this.getLogNowButton.setEnabled(true);
	 			this.startTimerButton.setEnabled(true);
	 		}
		}
 	} 	
 	
 	/**
 	 * Enables and disables file buttons according to current situation.
 	 */
 	private void enableAndDisableFileButtons(){
 		
 		// If one log is selected, enable Remove button, otherwise disable it
 		if( this.fileLogsTable.getSelectionCount() == 1 ){
 			this.removeOneButton.setEnabled(true);
 		}
 		else{
 			this.removeOneButton.setEnabled(false);	
 		}
 		
		// if log table is not empty, enable remove all button.
 		if( this.fileLogsTable.getItemCount() > 0 ){
 			this.removeAllButton.setEnabled(true);
 		}
 		else{
 			this.removeAllButton.setEnabled(false);
 		}
 		 	
 	}
	
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.wizard.WizardPage#canFlipToNextPage()
	 */ 	
 	public boolean canFlipToNextPage() {
 		return false;
 	}
 	
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
	 */
	public void createControl(Composite parent) {
		
		DbgUtility.println(DbgUtility.PRIORITY_OPERATION, this.getClass().getSimpleName() + ": createControl()"); //$NON-NLS-1$
		
		// Radio button group
		Composite composite = new Composite(parent, SWT.NULL);
	
		// create the desired layout for this wizard page
		GridLayout gl = new GridLayout();
		gl.numColumns = 1;
		composite.setLayout(gl);
		
		//Create group where to select if import from File System / From Device 
		createRadioButtonGroup(composite);
	
		//Create group where import logs from File
		createImportFromFileGroup(composite);
		
		//Create group where import logs from Device
		createImportFromDeviceGroup(composite);


		// load saved user entered data.
		this.loadUserEnteredData();
				
		// update buttons and texts on screen.
		this.updateConnectionText();
		
		// Enable/disable buttons
		this.enableAndDisableDeviceButtonsAndSWMTCategoryGroup(); //will call also #enableOrDisableHeapFilterText();
		this.enableAndDisableFileButtons();
		
		this.hideAndRevealItems();
	
		// Setting context-sensitive help ID		
		PlatformUI.getWorkbench().getHelpSystem().setHelp( composite, HelpContextIDs.MEMSPY_IMPORT_SWMT);

		setInitialFocus();
		setControl(composite);
	 }

	private void createImportFromDeviceGroup(Composite parent) {
		// Device group
	
		deviceGroup = new Group(parent, SWT.NONE);
		GridLayout deviceGridLayout = new GridLayout();
		deviceGridLayout.numColumns = 2;
	
		deviceGridData = new GridData(GridData.FILL_BOTH);
		deviceGroup.setLayoutData(deviceGridData);
		deviceGroup.setLayout(deviceGridLayout);
		deviceGroup.setText(GET_LOG_FROM_DEVICE_RADIO_BUTTON);
		
		
		
		// Logs from device table
		
		deviceLogsTable = new Table(deviceGroup, SWT.BORDER);
		GridData tableGridData = new GridData(GridData.FILL_BOTH);
		deviceLogsTable.setLayoutData(tableGridData);		
		
	    TableColumn logsColumn = new TableColumn(deviceLogsTable, SWT.LEFT);
	    logsColumn.setText(LOGS);
	    logsColumn.setWidth(300);
	    
	    TableColumn receivedColumn = new TableColumn(deviceLogsTable, SWT.LEFT);
	    receivedColumn.setText(RECEIVED);
	    receivedColumn.setWidth(110);
	    
	    GridData deviceLogsTableGridData = new GridData(GridData.FILL_BOTH);
	    deviceLogsTable.setLayoutData(deviceLogsTableGridData);
	    deviceLogsTable.setHeaderVisible(true);	    
	    deviceLogsTable.addSelectionListener(this);
		
				
	    //
	    //Create logging composite, contains Get log now, Get log with timer and remove logs functions
	    //
		createLoggingComposite(deviceGroup);
		
		//
		//Bottom composite where settings and categories are located
		//
		Composite bottomComposite = new Composite(deviceGroup,SWT.NONE);
		GridLayout pgl = new GridLayout(2, false);
		pgl.marginHeight = 0;
		pgl.marginWidth = 0;
		GridData pgd = new GridData(GridData.VERTICAL_ALIGN_END | GridData.FILL_HORIZONTAL);
		bottomComposite.setLayout(pgl);
		bottomComposite.setLayoutData(pgd);
		
		//
		//Create Connection settings group
		//
		createConnectionSettingsGroup(bottomComposite);
		
		//
		// Create and add SWMT category setting group
		//
		categoryGroupComposite = new SWMTCategoryGroupComposite(bottomComposite, false, this, MemSpyPlugin.getDefault().isSWMTCategorySettingFeatureEnabled());		

	}

	private void createLoggingComposite(Composite parent) {
		
		loggingComposite = new Composite(parent, SWT.NONE);
		GridLayout deviceButtonLayout = new GridLayout();
		GridData deviceButtonGridData = new GridData(SWT.FILL, SWT.FILL, true, true);//GridData.FILL_BOTH | GridData.VERTICAL_ALIGN_CENTER
		deviceButtonLayout.numColumns = 1;
		loggingComposite.setLayoutData(deviceButtonGridData);
		loggingComposite.setLayout(deviceButtonLayout);
			
		//
		// Get Log now button
		//
		Composite getLogComposite = new Composite(loggingComposite, SWT.NONE);
		GridLayout getLogLayout = new GridLayout();
		GridData getLogGD = new GridData(SWT.FILL, SWT.BEGINNING, true, false);
		getLogComposite.setLayoutData(getLogGD);
		getLogComposite.setLayout(getLogLayout);
		
		//Null label is added with no text to align get log now
		@SuppressWarnings("unused")
		Label nullLabel = new Label(getLogComposite,SWT.NULL);
		
		getLogNowButton = new Button(getLogComposite, SWT.PUSH);
		getLogNowButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL ));//| GridData.VERTICAL_ALIGN_BEGINNING
		getLogNowButton.setText(GET_LOG_NOW_BUTTON);
		getLogNowButton.addSelectionListener(this);

		//
		// Timer composite
		//
		Composite timerComposite = new Composite(loggingComposite, SWT.NONE);
		GridLayout timerLayout = new GridLayout();
		timerLayout.marginWidth = 0;
		GridData timerGD = new GridData(SWT.FILL, SWT.CENTER, true, true);
		timerComposite.setLayoutData(timerGD);
		timerComposite.setLayout(timerLayout);
		
		createTimerComposite(timerComposite);
		
		//
		// Remove all files button
		//
		Composite removeComposite = new Composite(loggingComposite, SWT.NONE);
		GridLayout removeLayout = new GridLayout();
		GridData removeGD = new GridData(SWT.FILL, SWT.BOTTOM, true, false);
		removeComposite.setLayoutData(removeGD);
		removeComposite.setLayout(removeLayout);
		
		removeReceivedLogsButton = new Button(removeComposite, SWT.PUSH);
		removeReceivedLogsButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));//| GridData.VERTICAL_ALIGN_END
		removeReceivedLogsButton.setText(REMOVE_ALL);
		removeReceivedLogsButton.addSelectionListener(this);
	}

	private void createConnectionSettingsGroup(Composite parent) {
		Group connectionSettingsGroup = new Group(parent, SWT.NONE);
		connectionSettingsGroup.setText("Connection");
		GridLayout connectionGroupLayout = new GridLayout();
		connectionGroupLayout.numColumns = 1;
		connectionSettingsGroup.setLayout(connectionGroupLayout);
		GridData connectionGridData = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		connectionSettingsGroup.setLayoutData(connectionGridData);
		
		
		connectionSettingsButton = new Button(connectionSettingsGroup, SWT.PUSH);
		connectionSettingsButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		connectionSettingsButton.setText(CONNECTION_SETTINGS_BUTTON);
		connectionSettingsButton.addSelectionListener(this);
				
		// Connection settings labels
		Label connectionTextLabel = new Label(connectionSettingsGroup, SWT.LEFT);
		connectionTextLabel.setText(CURRENTLY_USING_TEXT);
		connectionTextLabel.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_CENTER));
		
		connectionNameInUseLabel = new Label(connectionSettingsGroup, SWT.LEFT);
		connectionNameInUseLabel.setLayoutData( new GridData(GridData.HORIZONTAL_ALIGN_CENTER) );
	}

	private void createImportFromFileGroup(Composite parent) {
		// File selection group
	
		fileSelectionGroup = new Group(parent, SWT.NONE);
		GridLayout fileSelectionGridLayout = new GridLayout();
		fileSelectionGridLayout.numColumns = 2;
	
		fileSelectionGridData = new GridData(GridData.FILL_BOTH);
		fileSelectionGroup.setLayoutData(fileSelectionGridData);
		fileSelectionGroup.setLayout(fileSelectionGridLayout);
		fileSelectionGroup.setText(GET_FROM_FROM_FILE_RADIO_BUTTON);
	
		// Logs from file table
		
		fileLogsTable = new Table(fileSelectionGroup, SWT.BORDER);
	    TableColumn fileColumn = new TableColumn(fileLogsTable, SWT.LEFT);
	    fileColumn.setText(LOG_FILES);
	    fileColumn.setWidth(300);
	    
	    TableColumn nameColumn = new TableColumn(fileLogsTable, SWT.LEFT);
	    nameColumn.setText(LOG_TYPE);
	    nameColumn.setWidth(100);
	    
	    GridData fileLogsTableGridData = new GridData(GridData.FILL_BOTH);
	    fileLogsTable.setLayoutData(fileLogsTableGridData);
	    fileLogsTable.setHeaderVisible(true);	    
	    fileLogsTable.addSelectionListener(this);
	    
	    // File Selection button composite. Contains file operation buttons
	
		fileSelectionButtonComposite = new Composite(fileSelectionGroup, SWT.NONE);
		GridLayout threadButtonLayout = new GridLayout();
		GridData fileSelectionButtonGridData = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		fileSelectionButtonComposite.setLayoutData(fileSelectionButtonGridData);
		fileSelectionButtonComposite.setLayout(threadButtonLayout);
		threadButtonLayout.numColumns = 1;
		
		// Add file button
		addFileButton = new Button(fileSelectionButtonComposite, SWT.PUSH);
		addFileButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		addFileButton.setText(ADD_FILE);
		addFileButton.addSelectionListener(this);
		
		// Add folder button
		addDirectoryButton = new Button(fileSelectionButtonComposite, SWT.PUSH);
		addDirectoryButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		addDirectoryButton.setText(ADD_DIRECTORY);
		addDirectoryButton.addSelectionListener(this);
	
		// Remove one file button
		removeOneButton = new Button(fileSelectionButtonComposite, SWT.PUSH);
		removeOneButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		removeOneButton.setText(REMOVE_ONE);
		removeOneButton.addSelectionListener(this);
	
		// Remove all files and folders button
		removeAllButton = new Button(fileSelectionButtonComposite, SWT.PUSH);
		removeAllButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		removeAllButton.setText(REMOVE_ALL);
		removeAllButton.addSelectionListener(this);
	}

	private void createRadioButtonGroup(Composite parent) {
		// Radio button group
		GridLayout radioButtonGroupGridLayout = new GridLayout();
		radioButtonGroup = new Group(parent, SWT.NONE);
		radioButtonGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		radioButtonGroup.setText(GET_LOG_FROM_RADIO_BUTTON);
		radioButtonGroup.setLayout(radioButtonGroupGridLayout);
		GridData radioButtonGridData = new GridData(GridData.FILL_HORIZONTAL);
		radioButtonGridData.horizontalSpan = 2;
	
		// File radio button
		fileRadioButton = new Button(radioButtonGroup, SWT.RADIO);
		fileRadioButton.setText(GET_FROM_FROM_FILE_RADIO_BUTTON);
		fileRadioButton.setLayoutData(radioButtonGridData);
		fileRadioButton.addSelectionListener(this);
		fileRadioButton.setSelection(true);
	
		// From Device via TraceViewer radio button
		deviceRadioButton = new Button(radioButtonGroup, SWT.RADIO);
		deviceRadioButton.setText(GET_LOG_FROM_DEVICE_RADIO_BUTTON);
		deviceRadioButton.setLayoutData(radioButtonGridData);
		deviceRadioButton.addSelectionListener(this);
		deviceRadioButton.setSelection(false);
		
		// In case trace plugin is not available, disabling import from device selection
		if(!MemSpyPlugin.isTraceProviderAvailable()){
			deviceRadioButton.setEnabled(false);
		}
	}

	private void createTimerComposite(Composite parent) {
		// Timer UI-components
		
		Group timerComposite = new Group(parent, SWT.NONE);
		timerComposite.setText(TEXT_GET_LOG_WITH_TIMER);
		
		GridLayout timerLayout = new GridLayout();
		timerLayout.numColumns = 2;
		GridData timerGd = new GridData(SWT.FILL, SWT.CENTER, true, true);;
		timerComposite.setLayoutData( timerGd );
		timerComposite.setLayout( timerLayout );
		
						
		// Interval- label
		intervalLabel = new Label( timerComposite, SWT.LEFT );
		intervalLabel.setText( TEXT_INTERVAL );
		GridData intervalGd = new GridData();
		intervalGd.horizontalSpan = 2;
		intervalLabel.setLayoutData( intervalGd );
		
		// Timer combo box
		timerCombo = new Combo( timerComposite, SWT.BORDER);
		GridData timerComboGridData = new GridData(GridData.HORIZONTAL_ALIGN_CENTER);
		timerComboGridData.widthHint = 25;
		timerCombo.setLayoutData( timerComboGridData );
		timerCombo.setTextLimit(3);		
		
		// Interval- label
		secondLabel = new Label( timerComposite, SWT.LEFT );
		secondLabel.setText( TEXT_SECOND );
		GridData secondLabelLayoutData = new GridData(GridData.HORIZONTAL_ALIGN_CENTER);
		secondLabel.setLayoutData( secondLabelLayoutData );
		
		// Start timer - button
		startTimerButton = new Button(timerComposite, SWT.PUSH);
		GridData startTimerGridData = new GridData( GridData.FILL_HORIZONTAL );
		startTimerGridData.horizontalSpan = 2;
		startTimerButton.setLayoutData( startTimerGridData );
		startTimerButton.setText( START_TIMER );
		startTimerButton.addSelectionListener(this);
			
		
	}

	
	/**
	 * Checks if device radio buttons is selected.
	 * @return <code>true</code> if device radio button is selected, otherwise <code>false</code>.
	 */
	public boolean isDeviceRadioButtonSelected(){
		return deviceRadioButton.getSelection();
	}

	/**
	 * Updates connection settings.
	 */
	public void updateConnectionSettings() {
		this.updateConnectionText();
	}


	/**
	 * Gets timer interval from interval UI-component. 
	 * If interval set to text box is not valid, method prints error message and returns value 0.
	 * @return current timer interval
	 */
	private int getTimerInteval() {
		boolean showError = false;
		 
		int integer = 0;
		try{
			integer = Integer.parseInt( timerCombo.getText() );
		}
		catch( NumberFormatException e ){
			// show error message of interval is not integer.
			showError = true;
		}
		
	 	if( integer <= 0 && showError == false ){
			// show error message if integer is negative or zero.
	 		showError = true;
	 	} 
	 	
		if( showError ){
			// open error dialog and print error message
	 	    Status status = new Status(IStatus.ERROR, MemSpyPlugin.PLUGIN_ID, 0, ERROR_INTERVAL, null);
	 	    ErrorDialog.openError(Display.getCurrent().getActiveShell(),"MemSpy Error", null, status);
	 	    return 0;
		}

		return integer;
		
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.nokia.s60tools.memspy.ui.wizards.MemSpyTraceListener#operationFinished(com.nokia.s60tools.memspy.ui.wizards.MemSpyTraceListener.LauncherAction, com.nokia.s60tools.memspy.containers.SWMTLogInfo)
	 */
	public void operationFinished(LauncherAction action, SWMTLogInfo swmtLogInfo, boolean timerRunning) {
		if( action == LauncherAction.TIMED_SWMT_UPDATE ){
			
			// update received log to table
			this.receivedLog = swmtLogInfo;
			this.updateReceivedSWMTLog( timerRunning );
			
			// increase cycle number
			cycleNumber++;
		}
	}

	/*
	 * (non-Javadoc)
	 * @see com.nokia.s60tools.memspy.ui.wizards.MemSpyTraceListener#startedReceivingSWMTLog()
	 */
	public void startedReceivingSWMTLog() {
		
		DbgUtility.println(DbgUtility.PRIORITY_OPERATION, "SWMTLogPage.startedReceivingSWMTLog"); //$NON-NLS-1$
		
 		// Set MemSpy's state correct
		this.memSpyOperationRunning = true;
		this.memSpyTimerRunning = false;
	}
	
	/**
	 * Method that is called when "Get Log Now" - button is pressed.
	 */
	private void getLogNowPressed(){

		DbgUtility.println(DbgUtility.PRIORITY_OPERATION, "SWMTLogPage.getLogNowPressed"); //$NON-NLS-1$
		
		// if cycle number is zero(this is first log file in list), MemSpy's SWMT-logging needs to be reseted.
		boolean reset = false;
		if( deviceLogsTable.getItemCount() == 0 ){
			reset = true;
			cycleNumber = 1;
		}
		
		//Check if S60 application is ment to reset between cycles
		boolean isToBeClosedBetweenCycles = MemSpyPreferences.isCloseSymbianAgentBetweenCyclesSelected() && !MemSpyPreferences.isProfileTrackedCategoriesSelected();		
		if(isToBeClosedBetweenCycles){
			reset = true;
		}
		
		// Create new SWMTLogInfo object
		receivedLog = new SWMTLogInfo();
	
		// get temp file name for swmt-log
		Date date = new Date();
		receivedLog.setPath( MemSpyFileOperations.getTempFileNameForSWMTLog( cycleNumber, date ) );
		receivedLog.setType( SWMTLogType.FILE );
		receivedLog.setDate(date);
		receivedLog.setType(SWMTLogType.DEVICE);
		
		if( traceEngine.requestSWMTLog( this, receivedLog.getPath(), reset) ){
			
			try {
				DbgUtility.println(DbgUtility.PRIORITY_OPERATION, "SWMTLogPage.getLogNowPressed/getContainer().run/receiveSWMTLogProcess"); //$NON-NLS-1$
				getContainer().run(true, false, receiveSWMTLogProcess);
			} catch (InvocationTargetException e1) {
				// do nothing
				e1.printStackTrace();
			} catch (InterruptedException e1) {
				// do nothing
				e1.printStackTrace();
			}
			enableAndDisableDeviceButtonsAndSWMTCategoryGroup();
		}
		else{
			DbgUtility.println(DbgUtility.PRIORITY_OPERATION, "SWMTLogPage.getLogNowPressed/openError"); //$NON-NLS-1$
			MessageDialog.openError( this.getShell(), ERROR_MEMSPY, ERROR_CONNECTION);
			receivedLog = null;
		}
	}

	
	/**
	 * Method that is called when "Start Timer" - button is pressed.
	 */
	private void getLogWithTimerPressed(){

		int interval = this.getTimerInteval();
		
		// if interval is not zero continue
		if( interval != 0 ){
		
			// if cycle number is zero(this is first log file in list), MemSpy's SWMT-logging needs to be reseted.
			boolean reset = false;
			if( deviceLogsTable.getItemCount() == 0 ){
				reset = true;
				cycleNumber = 1;
			}
			
			boolean isToBeClosedBetweenCycles = MemSpyPreferences.isCloseSymbianAgentBetweenCyclesSelected()  && !MemSpyPreferences.isProfileTrackedCategoriesSelected();
			
			// if connect to trace source was established
			if( traceEngine.startSWMTTimer(this, cycleNumber, reset, isToBeClosedBetweenCycles, interval) ){
				
				// change Cancel text from wizard to "Stop Timer"
				setCancelText("Stop Timer");
				
				// launch receiveSWMTLog process.
				try {
					DbgUtility.println(DbgUtility.PRIORITY_OPERATION, "SWMTLogPage.getLogWithTimerPressed/getContainer().run/receiveSWMTLogProcess"); //$NON-NLS-1$
					getContainer().run(true, true, receiveSWMTLogProcess);
				} catch (InvocationTargetException e1) {
					// do nothing
					e1.printStackTrace();
				} catch (InterruptedException e1) {
					// do nothing
					e1.printStackTrace();
				}
				
				// Enable/Disable device buttons.
				enableAndDisableDeviceButtonsAndSWMTCategoryGroup();

			}
			else{
				// increase cycle number
				cycleNumber++;
				
				// show error message
				MessageDialog.openError( this.getShell(), ERROR_MEMSPY, ERROR_CONNECTION);

			}
		}
	}	
	
	/**
	 * Method that is called when stop timer-button is pressed.
	 */
	private void stopTimerPressed(){
		
		this.memSpyTimerRunning = false;
		if( traceEngine.stopSWMTTimer() ){
			// if timer is stopped immediately, hide progress bar
			memSpyStopping = false;
			
			this.setCancelText("Cancel");
		}
	}
	
	/**
	 * Created needed MemSpy processes.
	 */	
	private void createMemSpyProcesses(){
	
		DbgUtility.println(DbgUtility.PRIORITY_OPERATION, "SWMTLogPage.createMemSpyProcesses/receiveSWMTLogProcess"); //$NON-NLS-1$
		
		// receive SWMT-Log process
		receiveSWMTLogProcess = new IRunnableWithProgress() {
			/**
			 * In receive SWMT-Log process process views progress bar and checks every 0,5 seconds 
			 * if current MemSpy process has been finished. After MemSpy process has finished. Progress
			 * bar is hidden.
			 */
			public void run(IProgressMonitor monitor) {
				memSpyOperationRunning = true;
				
				monitor.beginTask(RECEIVING_SWMT_LOG, IProgressMonitor.UNKNOWN);
				while(true){
					// some delay, so that launcher has time to set it's state correct.
					try {
						Thread.sleep(500);
					} catch (InterruptedException e) {
						// do nothing
					}
					
					// If no operations are on-going, break
					if( !memSpyOperationRunning && !memSpyTimerRunning && !memSpyStopping ){
						break;
					}
					
					// If Cancel-message received, send stop request.
					if( monitor.isCanceled() ){
						monitor.beginTask(STOPPING_TIMER, IProgressMonitor.UNKNOWN);
						stopTimerPressed();
					}
					
					// If timer and operation are running, show "Waiting for timer to expire"-text
					else if ( memSpyTimerRunning ){
						monitor.beginTask(WAITING_FOR_TIMER, IProgressMonitor.UNKNOWN);
					}
					
					// If MemSpy operation is running, show "Requesting Heap Dump" -text
					else if( memSpyOperationRunning ){
						monitor.beginTask(RECEIVING_SWMT_LOG, IProgressMonitor.UNKNOWN);
					}
				}
				monitor.done();
		    }
		};
	}	
	
	/**
	 * Changes wizard's cancel button's text. 
	 * @param newText new text for button
	 */
	private void setCancelText( final String newText ){
		Runnable updateUiRunnable = new Runnable(){
				public void run(){
					// change Cancel text from wizard back to "Cancel"
					( (MemSpyWizard)getWizard() ).setCancelText( newText );
			}
		};
		// needs to be done on UI thread.
		Display.getDefault().asyncExec(updateUiRunnable);  
	}
	
	/* (non-Javadoc)
	 * @see com.nokia.s60tools.memspy.ui.wizards.SWMTCategoryGroupComposite.SWMTCategorySelectionChangeListener#categorySelectionChanged(int)
	 */
	public void setCategorySelection(int newCategorySelection, boolean isProfileSettings) {
		traceEngine.setCategoriesForSWMT(newCategorySelection, isProfileSettings);
	}

	/* (non-Javadoc)
	 * @see com.nokia.s60tools.memspy.ui.wizards.SWMTCategorySelectionMediator#getCategorySelection()
	 */
	public int getCategorySelection() {
		return traceEngine.getCategoriesForSWMT();
	}
	
	/* (non-Javadoc)
	 * @see com.nokia.s60tools.memspy.ui.wizards.SWMTCategorySelectionMediator#setAllTrackedCategoriesSelected(boolean)
	 */
	public void setProfileTrackedCategoriesSelected(
			boolean isAllCategoriesSelected) {
		traceEngine.setProfileTrackedCategoriesSelected(isAllCategoriesSelected);
	}	

	/* (non-Javadoc)
	 * @see com.nokia.s60tools.memspy.ui.wizards.SWMTCategorySelectionMediator#getAllTrackedCategoriesSelected()
	 */
	public boolean isProfileTrackedCategoriesSelected() {
		return traceEngine.isProfileTrackedCategoriesSelected();		
	}	
	
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.DialogPage#dispose()
	 */
	public void dispose(){
		DbgUtility.println(DbgUtility.PRIORITY_OPERATION, this.getClass().getSimpleName() + ": dispose()"); //$NON-NLS-1$
		categoryGroupComposite.dispose();
		super.dispose();
	}


}

