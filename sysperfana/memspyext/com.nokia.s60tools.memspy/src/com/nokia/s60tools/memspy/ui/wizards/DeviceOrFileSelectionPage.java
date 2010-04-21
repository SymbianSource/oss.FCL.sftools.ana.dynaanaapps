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
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;

import com.nokia.s60tools.memspy.containers.SWMTLogInfo;
import com.nokia.s60tools.memspy.containers.ThreadInfo;
import com.nokia.s60tools.memspy.containers.ThreadInfo.HeapDumpType;
import com.nokia.s60tools.memspy.interfaces.IMemSpyTraceListener;
import com.nokia.s60tools.memspy.model.MemSpyFileOperations;
import com.nokia.s60tools.memspy.model.MemSpyLogParserEngine;
import com.nokia.s60tools.memspy.model.TraceCoreEngine;
import com.nokia.s60tools.memspy.model.UserEnteredData;
import com.nokia.s60tools.memspy.model.UserEnteredData.ValueTypes;
import com.nokia.s60tools.memspy.plugin.MemSpyPlugin;
import com.nokia.s60tools.memspy.resources.HelpContextIDs;
import com.nokia.s60tools.memspy.ui.UiUtils;
import com.nokia.s60tools.ui.preferences.PreferenceUtils;
import com.nokia.s60tools.ui.wizards.S60ToolsWizardPage;
import com.nokia.s60tools.util.debug.DbgUtility;


public class DeviceOrFileSelectionPage extends S60ToolsWizardPage implements
		SelectionListener, ModifyListener, IMemSpyTraceListener {
	
	/**
	 * Selection index for import from device via TraceViewer radio button
	 */
	private static final int DEVICE_RADIO_BUTTON_SELECTION_INDEX = 2;
	
	public enum PageType{ IMPORT_PAGE, COMPARE_FIRST_HEAP, COMPARE_SECOND_HEAP };
	
	// UI elements
	private Combo fileCombo;
	private Button buttonBrowseFile;

	private Button fileRadioButton;
	private Button deviceRadioButton;
	private Button loadThreadListButton;
	private Button connectionSettingsButton;
	private Button getHeapButton;
	
	private Label heapsReceivedLabel;
	private Label connectionTextLabel;
	private Label connectionNameInUseLabel;
	
	private GridData threadSelectionGridData;
	private GridData fileSelectionGridData;
	private GridData deviceThreadTableGridData;
	private GridData importedHeapTableGridData;
	private GridData loadThreadListGridData;
	private GridData connectionGridData;
	private GridData connectionButtonGridData;
	private GridData connectionNameInUseGridData;
	private GridData threadFilterTypesGridData;
	private GridData threadFilterGridData;

	private Group compareGroup;

	
	private Group fileSelectionGroup;
	private Composite fileLocationGroup;
	private Composite fileThreadListGroup;
	
	private Group radioButtonGroup;
	private Group threadSelectionGroup;
	private Composite threadListGroup;
	private Composite threadButtonGroup;

	/**
	 * Table that is shown when getting heap From File System.
	 */
	private Table fileThreadTable;
	/**
	 * Table which contents can be loaded/refreshed from the device.
	 */
	private Table deviceThreadTable;
	/**
	 * Table that contains two heaps that are to be compared when Import and compare is selected.
	 */
	private Table comparedHeapsTable;
	/**
	 * Table in second page, where only imported heap can be selected.
	 */
	private Table importedHeapTable;

	/**
	 * Text field for setting filter text.
	 */
	private String filterText;
	/**
	 * Filter text.
	 */
	private Text threadFilterText;
	/**
	 * Combo for filtering types.
	 */
	private Combo threadFilterTypesCombo;
	
	// Thread list from one file
	private ArrayList<ThreadInfo> fileThreadList;
	
	// selected thread from thread list
	private ThreadInfo comparedThread;
	
	// Decoder Engine
	private MemSpyLogParserEngine engine;
	
	// Thread list received from usb/musti
	ArrayList<ThreadInfo> deviceThreadList;
	
	// TraceCore engine
	TraceCoreEngine traceEngine;
	
	// List of received heaps:
	ArrayList<ThreadInfo> receivedHeaps;
	
	// Heap that is currently received
	ThreadInfo receivedHeap;
	
	// this pages UserEnterData section
	ValueTypes section;
	
	// viewed error message
	String errorMessage;
	
	// Type of this page
	PageType pageType;
	
	// boolean variable, which is set to true if some MemSpy operation is running.
	boolean memSpyOperationRunning;
	
	// MemSpy operation processes:
	// receive thread list
	IRunnableWithProgress receiveThreadListProcess;
	// receive heap
	IRunnableWithProgress receiveHeapProcess;
	
	
	//Strings
	
	private final static String  FILE_SELECTION_DIALOG_TEXT 		= "Select Heap Dump File";
	private final static String  GET_HEAP_RADIO_BUTTON 				= "Get Heap:";
	private final static String  GET_HEAP_FROM_FILE_RADIO_BUTTON 	= "From File System";
	private final static String  GET_HEAP_FROM_DEVICE_RADIO_BUTTON 	= "From Device via TraceViewer";
	private final static String  HEAP_DUMP_LOCATION_TEXT 			= "Define Location of Heap Dump File:";
	private final static String  BROWSE_TEXT 						= "Browse...";
	private final static String  HEAP_CONTAINS_TEXT_IMPORT 			= "Selected file contains heaps from following threads.";
	private final static String  HEAP_CONTAINS_TEXT_COMPARE			= "Selected file contains heaps from following threads, select thread which heap needs to be compared.";
	private final static String  THREAD_NAME_TEXT 					= "Thread Name";
	private final static String  SELECT_THREAD_TEXT 				= GET_HEAP_FROM_DEVICE_RADIO_BUTTON;
	private final static String  SELECT_FILE_TEXT 					= GET_HEAP_FROM_FILE_RADIO_BUTTON;
	private final static String  LOAD_REFRESH_THREAD_LIST_BUTTON 	= "Load/Refresh Thread List";
	private final static String  CONNECTION_SETTINGS_BUTTON 		= "Connection Settings...";
	private final static String  CURRENTLY_USING_TEXT 				= "Currently using:";
	private final static String  GET_HEAP_NOW_BUTTON 				= "Get Selected Heap Now";
	private final static String  HEAP_DUMP_RECEIVED					= "Heap Dump Received";
	private final static String  RECEIVING_THREAD_LIST  			= "Receiving Thread List: ";
	private final static String  RECEIVING_HEAP_DUMP  				= "Receiving Heap Dump: ";
	private final static String  HEAP_DUMPS_RECEIVED				= " Heap Dumps Received";
	private final static String  COMPARE_HEAPS						= "Compared heaps";

	
	// Error/warning messages
	private final static String  ERROR_INVALID_FILE 				= "Invalid heap dump file. The file doesn't contain any threads with binary data information.";
	private final static String  ERROR_FILE_NOT_FOUND  				= "File not found";
	private final static String  SELECT_COMPARED_THREAD				= "Select thread which heap needs to be compared";
	private final static String  WARNING_HEAPS_NOT_FROM_SAME_THREAD = "Compared heaps are not from same thread";
	
	private final static String  ERROR_DRM_THREAD					= "Heaps from threads which name contains word \"drm\" cannot be received due to security policies.";
	private final static String  ERROR_DBGTRS_THREAD				= "Heaps from threads which name contains word \"dbgtrcserver::!DbgTrcServer\" cannot be received due to Trace restrictions.";

	
	/**
	 * DeviceOrFileSelectionPage(String pageName)
	 * Constructor
	 * @param pagename name of this wizard page
	 */
	protected DeviceOrFileSelectionPage( String pageName, String title, String description, ValueTypes section, 
										 PageType pageType, TraceCoreEngine traceEngine ) {
		super(pageName);
		setTitle( title );
		setDescription( description );
		this.engine = new MemSpyLogParserEngine();
		this.fileThreadList = new ArrayList<ThreadInfo>();
		this.deviceThreadList = new ArrayList<ThreadInfo>();
		this.traceEngine = traceEngine;
		this.receivedHeaps = new ArrayList<ThreadInfo>();
		this.receivedHeap = new ThreadInfo();
		this.section = section; 
		this.errorMessage = "";
		this.pageType = pageType;
		this.comparedThread = null;
		memSpyOperationRunning = false;
		this.createMemSpyProcesses();
		this.filterText = "";
	}	

	/**
	 * (non-Javadoc)
	 * @see com.nokia.s60tools.ui.wizards.S60ToolsWizardPage#setInitialFocus()
	 */

	public void setInitialFocus() {
		fileCombo.setFocus();
	}

	/**
	 * (non-Javadoc)
	 * @see org.eclipse.swt.events.SelectionListener#widgetDefaultSelected(org.eclipse.swt.events.SelectionEvent)
	 */
	public void widgetDefaultSelected(SelectionEvent e) {
		this.getHeapButtonPressed();
	}

	/**
	 * (non-Javadoc)
	 * @see org.eclipse.swt.events.SelectionListener#widgetSelected(org.eclipse.swt.events.SelectionEvent)
	 */
	public void widgetSelected(SelectionEvent e) {
		if (e.widget == buttonBrowseFile) {
			// open file dialog for selecting a Heap Dump file
			FileDialog dialog = new FileDialog(this.getShell());
			dialog.setText(FILE_SELECTION_DIALOG_TEXT);
			String[] filterExt = { "*.txt", "*.log", "*.*" };
			dialog.setFilterExtensions(filterExt);
			dialog.setFilterPath(fileCombo .getText());
			String result = dialog.open();
			if (result != null ){
				fileCombo.setText(result);
			}
			
		}

		// Update controls after radio buttons state changes to false
		else if ( ( e.widget == deviceRadioButton && deviceRadioButton.getSelection() == false ) ||
			 ( e.widget == fileRadioButton && fileRadioButton.getSelection() == false ) ){
			this.hideAndRevealItems();
			getWizard().getContainer().updateButtons();
			
			MemSpyWizard wizard = (MemSpyWizard) getWizard();
			if( this.pageType != PageType.IMPORT_PAGE ){
				wizard.setComparedHeaps();
			}
			
		}		
	
		// Open connection settings.
		else if ( e.widget == connectionSettingsButton ){
			// Open connection Trace viewers connection settings.
			Shell shell = MemSpyPlugin.getCurrentlyActiveWbWindowShell();
			PreferenceUtils.openPreferencePage(MemSpyPlugin.getTraceProvider().getTraceSourcePreferencePageId(), shell);
			
			// Disconnect trace source so that new settings are used when sending next request
			MemSpyPlugin.getTraceProvider().disconnectTraceSource();
			
			MemSpyWizard wizard = (MemSpyWizard) this.getWizard();
			// update new settings to each wizard page.
			wizard.updateConnectionSettings();
		}
		
		// Load thread list from device
		else if ( e.widget == loadThreadListButton ){
			this.loadThreadListButtonPressed();
		}
		else if ( e.widget == getHeapButton ){
			this.getHeapButtonPressed();
		}
		else if( e.widget == deviceThreadTable ){
			this.enableAndDisableGetHeapButton();
		}
		else if( e.widget == importedHeapTable ){
			this.enableAndDisableGetHeapButton();
		}
		else if( e.widget == fileThreadTable ){
			
			// get selection index from thread table
			int selectionIndex = fileThreadTable.getSelectionIndex();
			
			// set selected heap as compared heap
			comparedThread = fileThreadList.get( selectionIndex );

			// get wizard and update compared thread names
			MemSpyWizard wizard = (MemSpyWizard)this.getWizard();
			wizard.setComparedHeaps();
			
			wizard.showImportedHeapsInComparePage( false );

			// update buttons
			getWizard().getContainer().updateButtons();
			
			// Set selection back to table after updatebuttons has cleared it.
			fileThreadTable.setSelection( selectionIndex );
			
		}
		else if( e.widget == threadFilterTypesCombo) {
			updateThreadList();
		}


	}

	/**
	 * (non-Javadoc)
	 * @see org.eclipse.swt.events.ModifyListener#modifyText(org.eclipse.swt.events.ModifyEvent)
	 */
	public void modifyText(ModifyEvent event) {
		if (event.widget.equals(fileCombo)) {
			try {
				getWizard().getContainer().updateButtons();
			} catch (Exception e) {
			}
		}
		else if (event.widget.equals(threadFilterText)) {
			filterText = threadFilterText.getText();
			updateThreadList();
		}	
	}

	/**
	 * (non-Javadoc)
	 * @see com.nokia.s60tools.ui.wizards.S60ToolsWizardPage#recalculateButtonStates()
	 */
	public void recalculateButtonStates() {

	}


	/**
	 * hideAndRevealItems()
	 * Hides and reveals controls according to radiobutton states.
	 */
	private void hideAndRevealItems() {
		
		// get radiobutton selection
		boolean fileSelected = true;
		if( deviceRadioButton.getSelection() == false ){
			fileSelected = false;
		}

		// exlude/include needed controls
		fileSelectionGridData.exclude = fileSelected;
		threadSelectionGridData.exclude = !fileSelected;

		if( fileSelected ){
			// Since excluded groups size and location are not updated, do it now
			threadSelectionGroup.setSize( fileSelectionGroup.getSize() );
			threadSelectionGroup.setLocation(fileSelectionGroup.getLocation());
		
		}
		else{
			// Since excluded groups size and location are not updated, do it now
			fileSelectionGroup.setSize( threadSelectionGroup.getSize() );
			fileSelectionGroup.setLocation(threadSelectionGroup.getLocation());
	
		}
		// Hide/show needed controls
		fileSelectionGroup.setVisible(!fileSelected);
		threadSelectionGroup.setVisible(fileSelected);
		

		
	}

	
	/**
	 * canFinish()
	 * Returns false, because symbol files must be defined always.
	 */
	public boolean canFinish()
	{
		return false;
	}
	

	
	public boolean refreshFileThreadList(){

		File file = new File(fileCombo.getText());

		// If file exists
		if (file.isFile() && file.exists()) {
			
			// Empty thread list table
			fileThreadTable.removeAll();
			
			fileThreadList.clear();

			// Check that file is Heap Dump file and display thread names in table.
			if ( engine.isFileHeapDumpFile(file, fileThreadList) ) {
				this.setErrorMessage(null);
				
				
				for( int i = 0; i < fileThreadList.size(); i++ ){
					TableItem newItem = new TableItem(fileThreadTable, SWT.NONE,i);
					newItem.setText(fileThreadList.get(i).getThreadName());	
				}
				if( fileThreadList.size() == 1 ){
					comparedThread = fileThreadList.get(0);
					((MemSpyWizard)this.getWizard()).setComparedHeaps();
				}

				// If comparing heaps and no heap is selected as compared heap
				if( comparedThread == null && this.pageType != PageType.IMPORT_PAGE){
					this.setErrorMessage(SELECT_COMPARED_THREAD);
					return false;
				}
				else{
					// if importing heaps, set first heap as compared heap.
					if( this.pageType == PageType.IMPORT_PAGE ){
						comparedThread = fileThreadList.get(0);
					}
					return true;
				}
			} 
			else {
				this.setErrorMessage(ERROR_INVALID_FILE);
				return false;
			}
		} 
		else {
			this.setErrorMessage(ERROR_FILE_NOT_FOUND );
			return false;
		}
		
	}
	
	/**
	 * (non-Javadoc)
	 * @see org.eclipse.jface.wizard.WizardPage#canFlipToNextPage()
	 */
	
 	public boolean canFlipToNextPage() {
 		
 		if( fileRadioButton.getSelection() ){
			try {
				if( !this.refreshFileThreadList() ){
					return false;
				}
			}
			catch (Exception e) {
				e.printStackTrace();
				return false;
			}
			
 		}
 		else{ // deviceRadioButton.getSelection()
 			if( receivedHeaps.size() > 0 ){
 				this.setErrorMessage(null);
 			}
 			else{
 				this.setErrorMessage(null);
 				return false;
 			}
 		}
 		
 		if( this.pageType == PageType.COMPARE_FIRST_HEAP || this.pageType == PageType.COMPARE_SECOND_HEAP ){
 			MemSpyWizard wizard = (MemSpyWizard)this.getWizard();
 	 		if( !wizard.areComparedHeapsFromSameThread() ){
 	 			this.setMessage(WARNING_HEAPS_NOT_FROM_SAME_THREAD, ERROR);
 	 			if( this.pageType == PageType.COMPARE_SECOND_HEAP ){
 	 				return false;
 	 			}
 	 		}
 	 		else{
 	 			this.setMessage(null);
 	 		}
 		}
		return true;
 		
 		
 		
 		
	}
 	
 
 	/**
 	 * loadUserEnteredData
 	 * Loads previously used values and selections into UI components
 	 */
 	
 	private void loadUserEnteredData(){

 		// restore previous state of radio buttons.
 		// if last value is not found, set selection file.
 		
 		UserEnteredData data = new UserEnteredData();
		int lastUsedSource = data.getPreviousRadioButtonSelection(section);
		
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
		String[] lastUsedFiles = data.getPreviousValues( section );
		if ( lastUsedFiles != null ) {
			fileCombo.setItems( lastUsedFiles );
			fileCombo.select(0);
		}		
		
 	}
 	
 	/**
 	 * saveUserEnteredData.
 	 * Saves user entered values and selections.
 	 */
 	public void saveUserEnteredData(){
		UserEnteredData data = new UserEnteredData();
			
		// Save Action radio-buttons state
		if( deviceRadioButton.getSelection() ){
			data.saveRadioButtonSelection(section, 2);
		}
		else {
			data.saveRadioButtonSelection(section, 1);
		}
		
		// Save file combo box
		String item = fileCombo.getText();
		data.saveValue(section, item);
		
		
		
 	}
 	
 	/**
 	 * enableAndDisableGetHeapButton.
 	 * Enables/Disables "Get Heap Now" -button regarding wizard's state
 	 */
 	private void enableAndDisableGetHeapButton(){
 		final PageType type = this.pageType;
		Runnable updateUiRunnable = new Runnable(){
			public void run(){
				if( type == PageType.COMPARE_SECOND_HEAP && !importedHeapTableGridData.exclude ) {
					getHeapButton.setEnabled( true );
				}
				else if( deviceThreadTable.getSelectionCount() == 1 ){
					getHeapButton.setEnabled( true );
				}
				else{
					getHeapButton.setEnabled( false );
				}
			}
		};
		Display.getDefault().asyncExec(updateUiRunnable);   
		
 	} 	
 	
 	/*
 	 * (non-Javadoc)
 	 * @see com.nokia.s60tools.memspy.ui.wizards.MemSpyTraceListener#operationFinished(com.nokia.s60tools.memspy.ui.wizards.MemSpyTraceListener.LauncherAction)
 	 */
 	public void operationFinished( LauncherAction action ){
 		
 		
 		this.memSpyOperationRunning = false;
 		
 		if( action == LauncherAction.GET_HEAP_DUMP ){
 			this.updateReceivedHeap();
 		}
 		else if( action == LauncherAction.GET_THREAD_LIST ){

 			// command wizard to update all thread tables from all wizard pages
 			((MemSpyWizard)this.getWizard()).updateThreadLists( this.deviceThreadList );
 		}


 	}
 	
 	/**
 	 * updateReceivedHeap.
 	 * Does all actions that are done when Heap Dump file has been received.
 	 */
 	private void updateReceivedHeap(){
 		receivedHeaps.add( receivedHeap );
 		
 		
 		if( this.pageType == PageType.COMPARE_FIRST_HEAP || this.pageType == PageType.COMPARE_SECOND_HEAP ){
 			comparedThread = receivedHeap;
 		}
			
 		final MemSpyWizard wizard = (MemSpyWizard)this.getWizard();

		Runnable updateUiRunnable = new Runnable(){
			public void run(){
	 			
				// if this is import page, update UI's receivedHeaps-text
				if( pageType == PageType.IMPORT_PAGE ){
					heapsReceivedLabel.setText( Integer.toString( receivedHeaps.size() ) + HEAP_DUMPS_RECEIVED );
					threadButtonGroup.layout();
				}
				
	 			// If this is compare heaps page, command wizard to update all compare-tables.
				else{
					wizard.setComparedHeaps();
				}
				
				// hide thread list selection in second compare page.
				if( pageType == PageType.COMPARE_FIRST_HEAP ){
					wizard.showImportedHeapsInComparePage( true );
				}
				
				// Getting selected item from currently active table
		 		TableItem item;
		 		if( pageType == PageType.COMPARE_SECOND_HEAP && importedHeapTable.isVisible() ) {
		 			item = importedHeapTable.getItem( importedHeapTable.getSelectionIndex() );
		 		} else {
		 			item = deviceThreadTable.getItem( deviceThreadTable.getSelectionIndex() );
		 		}
		 		ThreadInfo thread = (ThreadInfo)item.getData();
		 		
		 		// Imported threads needs to be updated from first compare page.
		 		if( pageType == PageType.COMPARE_FIRST_HEAP ) {
					// sets thread list selection in 2. comparePage
					wizard.setThreadListSelectionToComparePages( thread );
		 		}
		 		
		 		// get date formatter
				SimpleDateFormat formatter = new SimpleDateFormat ( MemSpyFileOperations.DATEFORMAT );
		 		String date = formatter.format( receivedHeap.getDate() ); 
	 			item.setText( 1, date );
	 			// Date is saved to thread information so that it will be available if table is sorted.
	 			thread.setStatus(date);
			}

		};
		
		// needs to be called from UI thread.
		Display.getDefault().asyncExec(updateUiRunnable);
 	}
 	
 	/**
	 * Updates list and uses current filtering.
	 */
 	public void updateThreadList(){
 		Runnable updateUiRunnable = new Runnable(){
			public void run(){		
				String filter = filterText;
				deviceThreadTable.removeAll();
				for( ThreadInfo thread : deviceThreadList ) {
					if(threadFilterTypesCombo.getSelectionIndex() == 0) {
						// Starts with filter.
						if(thread.getThreadName().toLowerCase().startsWith(filter.toLowerCase())) {
							TableItem newItem = new TableItem(deviceThreadTable, SWT.NONE);
							newItem.setText( new String[]{ thread.getThreadName(), thread.getStatus() } );
							newItem.setData(thread);
						}
					} else {
						// Contains filter.
						if(thread.getThreadName().toLowerCase().contains(filter.toLowerCase())) {
							TableItem newItem = new TableItem(deviceThreadTable, SWT.NONE);
							newItem.setText( new String[]{ thread.getThreadName(), thread.getStatus() } );
							newItem.setData(thread);
						}
					}
				}
				
				// update wizard buttons
				getWizard().getContainer().updateButtons();
				// Update get heap button.
				enableAndDisableGetHeapButton();
			}
		};
		Display.getDefault().asyncExec(updateUiRunnable);   	

 	}
 	
 	/**
 	 * updateConnectionText
 	 * updates connection text to match used settings
 	 */

 	public void updateConnectionText(){
		// Updating connection name.
 		String displayName = MemSpyPlugin.getTraceProvider().getDisplayNameForCurrentConnection();
 		connectionNameInUseLabel.setText(displayName);
		// Update layout.		
		threadSelectionGroup.layout();
		threadButtonGroup.layout(); 			
 	}

	/*
	 * (non-Javadoc)
	 * @see com.nokia.s60tools.memspy.ui.wizards.MemSpyTraceListener#deviceError(com.nokia.s60tools.memspy.ui.wizards.MemSpyTraceListener.LauncherErrorType)
	 */
 	public void deviceError( final LauncherErrorType error ){
 		
 		Date date = new Date (System.currentTimeMillis());
 		DbgUtility.println(DbgUtility.PRIORITY_OPERATION, "DeviceOrFileSelectionPage.deviceError: '" + error.name() +"' time:'" +date.toString() + "'."); //$NON-NLS-1$ $NON-NLS-2$ $NON-NLS-3$
 		memSpyOperationRunning = false;
 		
 		// Getting user visible error message
 		errorMessage = UiUtils.getErrorMessageForLauncherError(error, traceEngine.getAdditionalErrorInformation(), traceEngine.getProgressStatus()); //$NON-NLS-1$

 		// Handling HeapDump query-specific logic related to the error  		
 		switch (error){
	 		case HEAP_NOT_FOUND:{
	 			errorMessage = ERROR_HEAP_NOT_FOUND;
	 			this.receivedHeap = null;
	 			break;
	 		}
 		}
 		
 		// Advising user to install launcher component to the device
 		UiUtils.showErrorDialogToUser(error, errorMessage, traceEngine.getProgressStatus());
 		
 	} 	 	

 	/**
 	 * Gets trace engine object instance.
 	 * @return used TraceEngine-object
 	 */
	public TraceCoreEngine getTraceEngine() {
		return traceEngine;
	}
	

	/**
	 * Sets thread info into comparedHeaps-table.
	 * @param firstHeap first compared heap
	 * @param secondHeap second compared heap
	 */
	public void setComparedHeap( final ThreadInfo firstHeap, final ThreadInfo secondHeap ){		
 		Runnable updateUiRunnable = new Runnable(){
			public void run(){
				comparedHeapsTable.removeAll();
				TableItem firstItem = new TableItem(comparedHeapsTable, SWT.NONE, 0 );
				TableItem secondItem = new TableItem(comparedHeapsTable, SWT.NONE, 1 );

		 		// get date formatter
				SimpleDateFormat formatter = new SimpleDateFormat ( MemSpyFileOperations.DATEFORMAT );
	 			if( firstHeap != null){
	 				firstItem.setText( new String[]{ firstHeap.getThreadName(), formatter.format( firstHeap.getDate() ) } );
	 			}
	 			if( secondHeap != null ){
		 			secondItem.setText( new String[]{ secondHeap.getThreadName(), formatter.format( secondHeap.getDate() ) } );
	 			}

			}

		};
		Display.getDefault().asyncExec(updateUiRunnable);   
	}

	/**
	 * setThreadListSelection.
	 * Sets thread list selection into given index and updates thread list selection into importedHeapTable if needed
	 * @param thread Thread information.
	 */
	public void setThreadListSelection( final ThreadInfo thread ){
		Runnable updateUiRunnable = new Runnable(){
			public void run(){
				if( pageType == PageType.COMPARE_SECOND_HEAP ){
					ThreadInfo importedThread = thread.clone();
					
					importedHeapTable.removeAll();
					// Get selected item from deviceThread
					TableItem importedItem = new TableItem( importedHeapTable, SWT.NONE, 0 );
					
					// copy selected item data into importedThread
					importedItem.setText(new String[]{ importedThread.getThreadName(), importedThread.getStatus() });
					importedItem.setData(importedThread);
					
					// select thread in importedHeapTable
					importedHeapTable.setSelection( 0 );
					
					enableAndDisableGetHeapButton();
				}
			}
	
		};
		Display.getDefault().asyncExec(updateUiRunnable);   
	}

	/**
	 * setDeviceThreadList.
	 * @param deviceThreadList new deviceThreadList
	 */
	public void setDeviceThreadList(ArrayList<ThreadInfo> deviceThreadList) {
		this.deviceThreadList = deviceThreadList;
	}
		
	/**
	 * getRecentHeap
	 * @return last received Heap's thread info.
	 */
	public ThreadInfo getRecentHeap(){
		if( this.deviceRadioButton.getSelection() ){
			if( receivedHeaps.size() > 0 ){
				ThreadInfo returnValue = receivedHeaps.get( receivedHeaps.size()-1 );
				return returnValue;
			}
			else{
				return null;
			}
		}
		else{
			if(comparedThread != null ){
				return comparedThread;
			}
			else{
				return null;
			}
		}
	}

	/**
	 * getImportedHeaps.
	 * @return imported heaps
	 */
	public ArrayList<ThreadInfo> getImportedHeaps() {
		if( this.deviceRadioButton.getSelection() ){
			return receivedHeaps;
		}
		else{
			return fileThreadList;
		}
	}

	/**
	 * updateConnectionSettings.
	 * Updates new connection settings to UI
	 */
	public void updateConnectionSettings() {
		this.updateConnectionText();
	}

	/**
	 * setImportedThreadTableVisible.
	 * Shows/Hides importedHeapTable
	 * @param value true if importedHeapsTable should be shown.
	 */
	public void setImportedThreadTableVisible( boolean value ){
		if( this.pageType == PageType.COMPARE_SECOND_HEAP ){
			
			deviceThreadTableGridData.exclude = value;
			deviceThreadTable.setVisible( !value );
			
			importedHeapTableGridData.exclude = !value;
			importedHeapTable.setVisible( value );
			
			loadThreadListGridData.exclude = value;
			loadThreadListButton.setVisible( !value );
			
			connectionButtonGridData.exclude = value;
			connectionSettingsButton.setVisible( !value );
			
			connectionGridData.exclude = value;
			connectionTextLabel.setVisible( !value );
			
			connectionNameInUseGridData.exclude = value;
			connectionNameInUseLabel.setVisible( !value );
			
			threadFilterGridData.exclude = value;
			threadFilterText.setVisible( !value );

			threadFilterTypesGridData.exclude = value;
			threadFilterTypesCombo.setVisible( !value );
			
			if( value == true ){
				
				threadButtonGroup.setLayoutData( new GridData( GridData.FILL_BOTH));
			}
			else{
				threadButtonGroup.setLayoutData( new GridData( GridData.VERTICAL_ALIGN_CENTER));

			}
			threadButtonGroup.layout();
			
			
		}

		threadSelectionGroup.layout();
		threadListGroup.layout();
		
		enableAndDisableGetHeapButton();
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
	 */
	public void createControl(Composite parent) {
	
		Composite composite = new Composite(parent, SWT.NULL);
	
		// create the desired layout for this wizard page
		GridLayout gl = new GridLayout();
		gl.numColumns = 1;
		composite.setLayout(gl);
		
		if( this.pageType == PageType.COMPARE_FIRST_HEAP || this.pageType == PageType.COMPARE_SECOND_HEAP ){
			GridLayout compareGroupGridLayout = new GridLayout();
			compareGroupGridLayout.numColumns = 2;
			compareGroup = new Group(composite, SWT.NONE);		
			compareGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			compareGroup.setText(COMPARE_HEAPS);
			compareGroup.setLayout(compareGroupGridLayout);
			
			
			// Table that contains all thread names and id's
			comparedHeapsTable = new Table(compareGroup, SWT.BORDER);
			TableColumn heapNameColumn = new TableColumn(comparedHeapsTable, SWT.LEFT);
			heapNameColumn.setText(THREAD_NAME_TEXT);
			heapNameColumn.setWidth(200);
			TableColumn receivedColumn = new TableColumn(comparedHeapsTable, SWT.LEFT);
			receivedColumn.setText(HEAP_DUMP_RECEIVED);
			receivedColumn.setWidth(150);
			comparedHeapsTable.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			comparedHeapsTable.setHeaderVisible(true);
			
		
			TableItem newItem = new TableItem(comparedHeapsTable, SWT.NONE,0);
			newItem.setText( new String[]{ "", "" } );
			TableItem newItem1 = new TableItem(comparedHeapsTable, SWT.NONE,1);
			newItem1.setText( new String[]{ "", "" } );
	
	
		}
		
	
		// Radio button group
		GridLayout radioButtonGroupGridLayout = new GridLayout();
		radioButtonGroup = new Group(composite, SWT.NONE);
		radioButtonGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		radioButtonGroup.setText(GET_HEAP_RADIO_BUTTON);
		radioButtonGroup.setLayout(radioButtonGroupGridLayout);
		GridData radioButtonGridData = new GridData(GridData.FILL_HORIZONTAL);
		radioButtonGridData.horizontalSpan = 2;
	
		// Heap Dump File radio button
		fileRadioButton = new Button(radioButtonGroup, SWT.RADIO);
		fileRadioButton.setText(GET_HEAP_FROM_FILE_RADIO_BUTTON);
		fileRadioButton.setLayoutData(radioButtonGridData);
		fileRadioButton.addSelectionListener(this);
		fileRadioButton.setSelection(true);
	
		// From Device via TraceViewer radio button
		deviceRadioButton = new Button(radioButtonGroup, SWT.RADIO);
		deviceRadioButton.setText(GET_HEAP_FROM_DEVICE_RADIO_BUTTON);
		deviceRadioButton.setLayoutData(radioButtonGridData);
		deviceRadioButton.addSelectionListener(this);
		deviceRadioButton.setSelection(false);
		
		// In case trace plugin is not available, disabling import from device selection
		if(!MemSpyPlugin.isTraceProviderAvailable()){
			deviceRadioButton.setEnabled(false);
		}
		
		// File selection group
	
		fileSelectionGroup = new Group(composite, SWT.NONE);
		GridLayout fileSelectionGridLayout = new GridLayout();
		fileSelectionGridLayout.numColumns = 1;
	
		fileSelectionGridData = new GridData(GridData.FILL_BOTH);
		fileSelectionGroup.setLayoutData(fileSelectionGridData);
		fileSelectionGroup.setLayout(fileSelectionGridLayout);
		fileSelectionGroup.setText(SELECT_FILE_TEXT);
		
		// File location composite
		fileLocationGroup = new Composite(fileSelectionGroup, SWT.NONE);
		GridLayout fileLocationGridLayout = new GridLayout();
		fileLocationGridLayout.numColumns = 2;
		GridData fileLocationGridData = new GridData(GridData.FILL_HORIZONTAL);
		fileLocationGroup.setLayoutData(fileLocationGridData);
		fileLocationGroup.setLayout(fileLocationGridLayout);
	
		// Define location label
		Label defineFileLocation = new Label(fileLocationGroup, SWT.NONE);
		defineFileLocation.setText( HEAP_DUMP_LOCATION_TEXT );
		GridData defineFileLocationGridData = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
		defineFileLocationGridData.horizontalSpan = 2;
		defineFileLocation.setLayoutData(defineFileLocationGridData);
	
		// File location combo
		fileCombo = new Combo(fileLocationGroup, SWT.BORDER);
		GridData fileDataGrid = new GridData(GridData.FILL_HORIZONTAL);
		
		fileCombo.setLayoutData(fileDataGrid);
		fileCombo.addModifyListener(this);
	
		// Browse-button
		buttonBrowseFile = new Button(fileLocationGroup, SWT.PUSH);
		buttonBrowseFile.setText(BROWSE_TEXT);
		buttonBrowseFile.addSelectionListener(this);
		
		// File location group
		fileThreadListGroup = new Composite(fileSelectionGroup, SWT.NONE);
		GridLayout fileThreadListGridLayout = new GridLayout();
		fileThreadListGridLayout.numColumns = 1;
	
		GridData fileThreadListGridData = new GridData(GridData.FILL_BOTH);
		fileThreadListGroup.setLayoutData(fileThreadListGridData);
		fileThreadListGroup.setLayout(fileThreadListGridLayout);
	
	
		// Define location label
		Label threadsFromFileLabel = new Label(fileThreadListGroup, SWT.NONE);
		if( this.pageType == PageType.IMPORT_PAGE ){
			threadsFromFileLabel.setText( HEAP_CONTAINS_TEXT_IMPORT );
		}
		else{
			threadsFromFileLabel.setText( HEAP_CONTAINS_TEXT_COMPARE );
		}
		threadsFromFileLabel.setLayoutData( new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING) );
		
		// Threads from file table
		fileThreadTable = new Table(fileThreadListGroup, SWT.BORDER);
	    TableColumn nameColumn = new TableColumn(fileThreadTable, SWT.LEFT);
	    nameColumn.setText(THREAD_NAME_TEXT);
	    nameColumn.setWidth(250);
	    fileThreadTable.setLayoutData(new GridData(GridData.FILL_BOTH));
	    fileThreadTable.setHeaderVisible(true);	    
		fileThreadTable.setItemCount(5);
		
		if( this.pageType == PageType.COMPARE_FIRST_HEAP || this.pageType == PageType.COMPARE_SECOND_HEAP ){
			fileThreadTable.addSelectionListener(this);
		}
	
	
		// Thread Selection group
	
		threadSelectionGroup = new Group(composite, SWT.NONE);
		GridLayout threadSelectionGridLayout = new GridLayout();
		threadSelectionGridLayout.numColumns = 2;
		
				
		threadSelectionGridData = new GridData(GridData.FILL_BOTH);
		threadSelectionGroup.setLayoutData(threadSelectionGridData);
		threadSelectionGroup.setLayout(threadSelectionGridLayout);
		threadSelectionGroup.setText(SELECT_THREAD_TEXT);
	
		// Thread list Group
		threadListGroup = new Composite(threadSelectionGroup, SWT.NONE);
		GridLayout threadListLayout = new GridLayout();
		threadListLayout.numColumns = 2;
		threadListGroup.setLayoutData(new GridData(GridData.FILL_BOTH));
		threadListGroup.setLayout(threadListLayout);
		
		// Filter text field.
		threadFilterText = new Text(threadListGroup, SWT.BORDER);
		threadFilterText.setText("type filter text");
		threadFilterGridData = new GridData(GridData.FILL_HORIZONTAL);
		threadFilterGridData.grabExcessHorizontalSpace = true;
		threadFilterGridData.grabExcessVerticalSpace = false;
		threadFilterGridData.verticalAlignment = SWT.TOP;
		threadFilterText.setLayoutData(threadFilterGridData);
		
		threadFilterText.addModifyListener(this);
		
		// Filter selection combo.
		threadFilterTypesCombo = new Combo(threadListGroup, SWT.BORDER|SWT.READ_ONLY);
		threadFilterTypesCombo.setItems(new String[]{"Starts with", "Contains"});
		threadFilterTypesGridData = new GridData();
		threadFilterTypesGridData.widthHint = 100;
		threadFilterTypesCombo.select(0);
		threadFilterTypesCombo.setLayoutData(threadFilterTypesGridData);

		threadFilterTypesCombo.addSelectionListener(this);
		
		// Table that contains all thread names and id's
		deviceThreadTable = new Table(threadListGroup, SWT.BORDER);

		TableColumn deviceThreadColumn = new TableColumn(deviceThreadTable, SWT.LEFT);
		deviceThreadColumn.setText(THREAD_NAME_TEXT);
		deviceThreadColumn.setWidth(200);
		TableColumn receivedColumn = new TableColumn(deviceThreadTable, SWT.LEFT);
		receivedColumn.setText(HEAP_DUMP_RECEIVED);
		receivedColumn.setWidth(150);
		deviceThreadTableGridData = new GridData(GridData.FILL_BOTH);
		deviceThreadTableGridData.horizontalSpan = 2;
	    deviceThreadTable.setLayoutData( deviceThreadTableGridData );
	    deviceThreadTable.setHeaderVisible(true);	    
		deviceThreadTable.addSelectionListener( this );
		
		if( this.pageType == PageType.COMPARE_SECOND_HEAP ){
			// Table where first imported heaps name is presented
			importedHeapTable = new Table( threadListGroup, SWT.BORDER );
			TableColumn importedThreadNameColumn = new TableColumn( importedHeapTable, SWT.LEFT );
			importedThreadNameColumn.setText( THREAD_NAME_TEXT );
			importedThreadNameColumn.setWidth( 200 );
			TableColumn importedWhenColumn = new TableColumn( importedHeapTable, SWT.LEFT );
			importedWhenColumn.setText( HEAP_DUMP_RECEIVED );
			importedWhenColumn.setWidth( 150 );
			//importedHeapTableGridData = new GridData(GridData.GRAB_VERTICAL);
			importedHeapTableGridData = new GridData(GridData.FILL_HORIZONTAL);
			importedHeapTable.setLayoutData( importedHeapTableGridData );
			importedHeapTable.setHeaderVisible( true );	    
			importedHeapTable.addSelectionListener( this );
		}
		
		// Thread button group
		threadButtonGroup = new Composite(threadSelectionGroup, SWT.NONE);
		GridLayout threadButtonLayout = new GridLayout();
		threadButtonLayout.numColumns = 1;
		
		GridData threadButtonGridData = new GridData(GridData.HORIZONTAL_ALIGN_CENTER);
		threadButtonGroup.setLayoutData(threadButtonGridData);
		threadButtonGroup.setLayout(threadButtonLayout);
			
		loadThreadListButton = new Button(threadButtonGroup, SWT.PUSH);
		loadThreadListGridData = new GridData(GridData.FILL_HORIZONTAL);
		loadThreadListButton.setLayoutData( loadThreadListGridData );
		loadThreadListButton.setText(LOAD_REFRESH_THREAD_LIST_BUTTON);
		loadThreadListButton.addSelectionListener( this );
	
		// Connection settings button
		
		connectionSettingsButton = new Button(threadButtonGroup, SWT.PUSH);
		connectionButtonGridData = new GridData(GridData.FILL_HORIZONTAL);
		connectionSettingsButton.setLayoutData( connectionButtonGridData );
		connectionSettingsButton.setText(CONNECTION_SETTINGS_BUTTON);
		connectionSettingsButton.addSelectionListener(this);
	
		// Connection settings labels
		connectionTextLabel = new Label(threadButtonGroup, SWT.LEFT);
		connectionTextLabel.setText(CURRENTLY_USING_TEXT);
		connectionGridData = new GridData(GridData.HORIZONTAL_ALIGN_CENTER);
		connectionTextLabel.setLayoutData( connectionGridData );
		
		connectionNameInUseLabel = new Label(threadButtonGroup, SWT.LEFT);
		connectionNameInUseGridData = new GridData(GridData.HORIZONTAL_ALIGN_CENTER);
		connectionNameInUseLabel.setLayoutData( connectionNameInUseGridData );
		
		// Get Heap button
		
		getHeapButton = new Button(threadButtonGroup, SWT.PUSH);
		getHeapButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		getHeapButton.setText(GET_HEAP_NOW_BUTTON);
		getHeapButton.addSelectionListener( this );
		this.enableAndDisableGetHeapButton();
		
		if( this.pageType == PageType.IMPORT_PAGE ){
			heapsReceivedLabel = new Label(threadButtonGroup, SWT.LEFT);
			heapsReceivedLabel.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_CENTER));
			heapsReceivedLabel.setText( "0" + HEAP_DUMPS_RECEIVED );
		}		
		// load saved user entered data.
		this.loadUserEnteredData();
		
		// update buttons and texts on screen.
		this.updateConnectionText();
		this.hideAndRevealItems();
		this.setImportedThreadTableVisible( false );

		setHelps();
		setInitialFocus();
		setControl(composite);
		}

	
	/*
	 * (non-Javadoc)
	 * @see com.nokia.s60tools.memspy.ui.wizards.MemSpyTraceListener#operationFinished(com.nokia.s60tools.memspy.ui.wizards.MemSpyTraceListener.LauncherAction, com.nokia.s60tools.memspy.containers.SWMTLogInfo)
	 */
	public void operationFinished(LauncherAction action, SWMTLogInfo swmtLogInfo, boolean timerRunning) {
		// Can be left empty.
		
	}

	/*
	 * (non-Javadoc)
	 * @see com.nokia.s60tools.memspy.ui.wizards.MemSpyTraceListener#startedReceivingSWMTLog()
	 */
	public void startedReceivingSWMTLog() {
		// Can be left empty.
		
	}
	
	private void loadThreadListButtonPressed(){
		deviceThreadList.clear();
		if( traceEngine.requestThreadList( deviceThreadList, (DeviceOrFileSelectionPage)getWizard().getContainer().getCurrentPage() ) ){			
			try {
				getContainer().run(true, false, receiveThreadListProcess);
			} catch (InvocationTargetException e1) {
				e1.printStackTrace();
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}
		}
		else{
			MessageDialog.openError( this.getShell(), ERROR_MEMSPY, ERROR_CONNECTION);
		}
	}
	
	/**
	 * getHeapButtonPressed.
	 * Method that is called when "Get Heap Now"-button is pressed.
	 */
	private void getHeapButtonPressed(){
		// Imported thread is loaded, if it is importedHeapTable is visible.
		ThreadInfo thread;
		if(this.pageType == PageType.COMPARE_SECOND_HEAP && importedHeapTable.isVisible()) {
			if(importedHeapTable.getSelectionIndex() == -1) {
				// No selection, nothing to do.
				return;
			}
			thread = (ThreadInfo)importedHeapTable.getSelection()[0].getData();
		} else {
			if(deviceThreadTable.getSelectionIndex() == -1) {
				// No selection, nothing to do.
				return;
			}
			thread = (ThreadInfo)deviceThreadTable.getSelection()[0].getData();
		}
		
		// if thread name contains text "drm" it cannot be requested because of security reasons
		if( thread.getThreadName().toLowerCase().contains("drm") ){
			MessageDialog.openError( this.getShell(), ERROR_MEMSPY, ERROR_DRM_THREAD);
			return;
		}
		// if thread name contains text "dbgtrcserver::!DbgTrcServer" it cannot be requested because of TraceCore
		else if(thread.getThreadName().toLowerCase().contains("dbgtrcserver::!dbgtrcserver") ){
			MessageDialog.openError( this.getShell(), ERROR_MEMSPY, ERROR_DBGTRS_THREAD);
			return;
		}
		
		String threadID = thread.getThreadID();
		Date date = new Date();
		String threadName = thread.getThreadName();
		
		// Get file name for heap dump from engine
		String filePath = MemSpyFileOperations.getFileNameForTempHeapDump( threadName, date);
		
		// Add new file into receivedHeaps-ArrayList
		receivedHeap = new ThreadInfo();
		receivedHeap.setThreadFilePath(filePath);
		receivedHeap.setThreadID(threadID);
		receivedHeap.setThreadName(threadName);
		receivedHeap.setDate(date);
		receivedHeap.setType(HeapDumpType.DEVICE);

		
		if( traceEngine.requestHeapDump( threadID, this, filePath ) ){
 		
			try {
				getContainer().run(true, false, receiveHeapProcess);
			} catch (InvocationTargetException e1) {
				// do nothing
				e1.printStackTrace();
			} catch (InterruptedException e1) {
				// do nothing
				e1.printStackTrace();
			}
		}
		else{
			MessageDialog.openError( this.getShell(), ERROR_MEMSPY, ERROR_CONNECTION);
			receivedHeap = null;
		}
		
		getWizard().getContainer().updateButtons();
		
	}
	
	/**
	 * createMemSpyProcesses.
	 * Creates MemSpy operation processes.
	 */
	private void createMemSpyProcesses(){
	
		// receive thread list process
		receiveThreadListProcess = new IRunnableWithProgress() {
			public void run(IProgressMonitor monitor) {
				memSpyOperationRunning = true; 
				monitor.beginTask(RECEIVING_THREAD_LIST, IProgressMonitor.UNKNOWN);
				while(true){
					// some delay, so that launcher has time to set it's state correct.
					try {
						Thread.sleep(500);
					} catch (InterruptedException e) {
						// do nothing
					}
					if( !memSpyOperationRunning ){
						break;
					}
				}
				monitor.done();
		    }
		};
		
		// receive heap process.
		receiveHeapProcess = new IRunnableWithProgress() {
			public void run(IProgressMonitor monitor) {
				memSpyOperationRunning = true; 
				monitor.beginTask(RECEIVING_HEAP_DUMP, IProgressMonitor.UNKNOWN);
				while(true){
					// some delay
					try {
						Thread.sleep(500);
					} catch (InterruptedException e) {
						// do nothing
					}
					if( !memSpyOperationRunning ){
						break;
					}
				
				}
				monitor.done();
				
		    }
		};
	}
	
	/**
	 * setHeapDumpFile.
	 * Sets parameter test to file combo box, selects file radio button and disables UI-components
	 * @param newFilePath new file path.
	 */
	public void setHeapDumpFile( final String newFilePath ){
		Runnable updateUiRunnable = new Runnable(){
			public void run(){
				
				if( deviceRadioButton.getSelection() ){
					// select file system-radio button
					fileRadioButton.setSelection(true);
					deviceRadioButton.setSelection(false);
					hideAndRevealItems();
				}
				// set text to file location combo box
				fileCombo.setText( newFilePath );
				
				// set selection buttons disabled.
				fileCombo.setEnabled( false );
				buttonBrowseFile.setEnabled( false );
				deviceRadioButton.setEnabled(false);
				

			}
		};
		Display.getDefault().asyncExec(updateUiRunnable);   
		
	}
	
	/**
	 * getSelectedFilePath.
	 * @return text value from file combo box.
	 */
	public String getSelectedFilePath(){
		return fileCombo.getText();
	}
	
	/**
	 * resetFileCombo.
	 * sets text field in file combo box empty.
	 */
	public void resetFileCombo(){
		Runnable updateUiRunnable = new Runnable(){
			public void run(){
				fileCombo.setText( "" );
			}
		};
		Display.getDefault().asyncExec(updateUiRunnable);   
	}
	

	/**
	 * Sets this page's context sensitive helps
	 *
	 */
	protected void setHelps() {
	
		String helpContextId = null;
		if( this.pageType == PageType.IMPORT_PAGE ){
			helpContextId = HelpContextIDs.MEMSPY_IMPORT_HEAP;
		}
		else{
			helpContextId = HelpContextIDs.MEMSPY_IMPORT_COMPARE;
		}
		PlatformUI.getWorkbench().getHelpSystem().setHelp( deviceRadioButton, helpContextId );
		PlatformUI.getWorkbench().getHelpSystem().setHelp( fileRadioButton, helpContextId);
		PlatformUI.getWorkbench().getHelpSystem().setHelp( fileCombo, helpContextId);
		PlatformUI.getWorkbench().getHelpSystem().setHelp( fileThreadTable, helpContextId);
		
		PlatformUI.getWorkbench().getHelpSystem().setHelp( loadThreadListButton, helpContextId );
		PlatformUI.getWorkbench().getHelpSystem().setHelp( connectionSettingsButton, helpContextId);
		PlatformUI.getWorkbench().getHelpSystem().setHelp( getHeapButton, helpContextId);
		PlatformUI.getWorkbench().getHelpSystem().setHelp( deviceThreadTable, helpContextId);
		
	}

	
	
}
