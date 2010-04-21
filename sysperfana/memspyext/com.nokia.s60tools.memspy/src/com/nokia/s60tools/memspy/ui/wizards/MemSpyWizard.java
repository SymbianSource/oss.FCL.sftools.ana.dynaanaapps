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
import java.util.ArrayList;
import java.util.Arrays;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.ui.IWorkbench;

import com.nokia.s60tools.memspy.containers.SWMTLogInfo;
import com.nokia.s60tools.memspy.containers.ThreadInfo;
import com.nokia.s60tools.memspy.model.AnalyserXMLGenerator;
import com.nokia.s60tools.memspy.model.ImportEngine;
import com.nokia.s60tools.memspy.model.MemSpyFileOperations;
import com.nokia.s60tools.memspy.model.TraceCoreEngine;
import com.nokia.s60tools.memspy.model.UserEnteredData.ValueTypes;
import com.nokia.s60tools.memspy.resources.ImageKeys;
import com.nokia.s60tools.memspy.resources.ImageResourceManager;
import com.nokia.s60tools.memspy.ui.views.MemSpyMainView;
import com.nokia.s60tools.memspy.ui.wizards.DeviceOrFileSelectionPage.PageType;
import com.nokia.s60tools.memspy.ui.wizards.SelectActionPage.MemSpyAction;
import com.nokia.s60tools.ui.wizards.S60ToolsWizard;
import com.nokia.s60tools.util.debug.DbgUtility;

public class MemSpyWizard extends S60ToolsWizard {

	public enum MemSpyWizardType{ FULL, COMPARE, SYMBOLS };
	
	static private final ImageDescriptor bannerImgDescriptor = ImageResourceManager.getImageDescriptor(ImageKeys.IMG_WIZARD);

	
	SelectActionPage selectActionPage;
	DeviceOrFileSelectionPage importHeapPage;
	DeviceOrFileSelectionPage compareHeapsFirstPage;
	DeviceOrFileSelectionPage compareHeapsSecondPage;
	ParameterFilesPage parameterFilesPage;
	ExportFileNamePage exportFileNamePage;
	SWMTLogPage swmtLogPage;
	MemSpyMainView mainView;
	ImportEngine importEngine;
	MemSpyWizardDialog wizDialog;
	
	MemSpyWizardType wizardType; 
	TraceCoreEngine traceEngine;
	AnalyserXMLGenerator fillValues;
	
	private final static String IMPORTING_FILES = "Importing Files";
	
	public MemSpyWizard(){
		super(bannerImgDescriptor);
		DbgUtility.println(DbgUtility.PRIORITY_OPERATION, "MemSpyWizard constructor - start");

		this.mainView = MemSpyMainView.showAndReturnYourself();
		this.importEngine = mainView.getImportEngine();
		this.setNeedsProgressMonitor(true);
		this.traceEngine = new TraceCoreEngine();
		this.wizardType = MemSpyWizardType.FULL;
		this.fillValues = null;
		DbgUtility.println(DbgUtility.PRIORITY_OPERATION, "MemSpyWizard constructor - end");

	}
	
	public MemSpyWizard( MemSpyWizardType wizardType, AnalyserXMLGenerator fillValues ) {
		this();
		this.wizardType = wizardType;
		this.fillValues = fillValues;
		if( wizardType != MemSpyWizardType.SYMBOLS ){
			MemSpyMainView.showTraceViewer();	
		}
	}

	
	/**
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.s60tools.ui.wizards.S60ToolsWizard#addPages()
	 */
	public void addPages() {
		DbgUtility.println(DbgUtility.PRIORITY_OPERATION, "MemSpyWizard addpages - start");

		// if wizard's type is full, add all pages.
		if( wizardType == MemSpyWizardType.FULL ){
	
			selectActionPage = new SelectActionPage("Select Action");
			addPage(selectActionPage);
			
			importHeapPage = new DeviceOrFileSelectionPage( "Get Heap From", 
					"Import Heap Wizard, step 1", "Define source for Heap Dump files. Logs can be imported from already existing files or from device via TraceViewer.", ValueTypes.IMPORT_HEAP, 
					PageType.IMPORT_PAGE, traceEngine );
			addPage(importHeapPage);
	
			compareHeapsFirstPage = new DeviceOrFileSelectionPage("Select First Heap", 
					"Compare Two Heaps Wizard, step 1", "Define source for first compared Heap Dump. Heap Dumps can be imported from already existing files\nor from device via TraceViewer.", ValueTypes.COMPARE_HEAP_FIRST_HEAP, 
					PageType.COMPARE_FIRST_HEAP, traceEngine);
			addPage(compareHeapsFirstPage);
			
			compareHeapsSecondPage = new DeviceOrFileSelectionPage("Select Second Heap", 
					"Compare Two Heaps Wizard, step 2", "Define source for second compared Heap Dump. Compared heap dumps must be from same thread.", ValueTypes.COMPARE_HEAP_SECOND_HEAP, 
					PageType.COMPARE_SECOND_HEAP, traceEngine);
			addPage(compareHeapsSecondPage);
			
			exportFileNamePage = new ExportFileNamePage("Define export file name");
			addPage(exportFileNamePage);
			
			parameterFilesPage = new ParameterFilesPage(null, null, "Import Heap Wizard, step 2");
			addPage(parameterFilesPage);
			
			swmtLogPage = new SWMTLogPage("Get SWMT-logs", traceEngine);
			addPage(swmtLogPage);		
		}
		// if wizard type is compare add only compare output and symbol pages.
		else if( wizardType == MemSpyWizardType.COMPARE){
			
			compareHeapsFirstPage = new DeviceOrFileSelectionPage("Select First Heap", 
					"Compare Two Heaps Wizard, step 1", "Define source for first compared Heap Dump. Heap Dumps can be imported from already existing files\nor from device via TraceViewer.", ValueTypes.COMPARE_HEAP_FIRST_HEAP, 
					PageType.COMPARE_FIRST_HEAP, traceEngine);
			addPage(compareHeapsFirstPage);
			
			// prefill path of compared Heap Dump to wizard page.
			compareHeapsFirstPage.setHeapDumpFile( fillValues.getXMLSourceFile()[0] );
			
			compareHeapsSecondPage = new DeviceOrFileSelectionPage("Select Second Heap", 
					"Compare Two Heaps Wizard, step 2", "Define source for second compared Heap Dump. Compared heap dumps must be from same thread.", ValueTypes.COMPARE_HEAP_SECOND_HEAP, 
					PageType.COMPARE_SECOND_HEAP, traceEngine);
			addPage(compareHeapsSecondPage);
			
			// reset file combo box text from second page.
			compareHeapsSecondPage.resetFileCombo();
			
			exportFileNamePage = new ExportFileNamePage("Define export file name");
			addPage(exportFileNamePage);
			
			parameterFilesPage = new ParameterFilesPage(fillValues.getXMLDebugMetaDataFile(), null, "Import Heap Wizard, step 2");
			addPage(parameterFilesPage);
			
			

			
		}
		else if( wizardType == MemSpyWizardType.SYMBOLS ){
			parameterFilesPage = new ParameterFilesPage( fillValues.getXMLDebugMetaDataFile(), fillValues.getXMLDebugMetaDataDirectory(), "Edit Symbol Definitions" );
			addPage(parameterFilesPage);
		}
		DbgUtility.println(DbgUtility.PRIORITY_OPERATION, "MemSpyWizard addpages - end");

	}

	/**
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.wizard.Wizard#getNextPage(org.eclipse.jface.wizard.IWizardPage)
	 */
	public IWizardPage getNextPage(IWizardPage page) {

		
		if (page.equals( selectActionPage )) {
			
			SelectActionPage SelectWizardPage = (SelectActionPage) page;
			if ( SelectWizardPage.importHeapRadioButton.getSelection() ) {
				return importHeapPage;
			} 
			else if ( SelectWizardPage.compareTwoHeapsRadioButton.getSelection() ) {
				return compareHeapsFirstPage;
			}
			else if ( SelectWizardPage.swmtRadioButton.getSelection() ) {
				return swmtLogPage;
			}
		} else if ( page.equals(importHeapPage) ) {
			return parameterFilesPage;
		}

		else if ( page.equals(compareHeapsFirstPage) ) {
			return compareHeapsSecondPage;
		}
		else if ( page.equals(compareHeapsSecondPage ) ) {
			return exportFileNamePage;
		}
		else if ( page.equals( exportFileNamePage ) ){
			boolean setText = true;
 			File file = new File( exportFileNamePage.getOutputFileName());
			if( file.exists() ){
				// if file already exists confirm that user wants to overwrite it.
				MessageBox messageBox = new MessageBox(this.getShell(), SWT.ICON_QUESTION | SWT.YES | SWT.NO);
		        messageBox.setText( MemSpyMainView.WARNING_COMPARE );
		        messageBox.setMessage( MemSpyMainView.WARNING_FILE_EXISTS );
		        int buttonID = messageBox.open();
		        if (buttonID == SWT.NO) {
		        	setText = false;
		        }
			}
			if( setText == false ){
				return exportFileNamePage;
			}
			return parameterFilesPage;
		}


		return page;
	}

	/**
	 * (non-Javadoc)
	 * @see org.eclipse.jface.wizard.Wizard#canFinish()
	 */
	public boolean canFinish() {
		if (this.getContainer().getCurrentPage() == parameterFilesPage) {
			return parameterFilesPage.canFinish();
		} 
		else if (this.getContainer().getCurrentPage() == swmtLogPage ){
			return swmtLogPage.canFinish();
		}
		else {
			return false;
		}

	}
	

	

	/**
	 * (non-Javadoc)
	 * @see org.eclipse.jface.wizard.Wizard#performFinish()
	 */
	
	
	public boolean performFinish() {
		
		if( this.wizardType == MemSpyWizardType.FULL ){
			// save form values
			saveUserEnteredData();
			
			IRunnableWithProgress importFiles = null;
			
			// get selected action from first page.
			final MemSpyAction action = selectActionPage.getAction();
			
			importHeapPage.getTraceEngine().shutDownMemSpy();
			// Initialize XML generator
			AnalyserXMLGenerator generator = new AnalyserXMLGenerator();
			
	
			if( action != MemSpyAction.SWMT ){
				
				// read symbol definitions
				this.getSymbolInformation(generator);
				
				final AnalyserXMLGenerator finalGenerator = generator; 
				
				// if importing Heap Dump(s)
				if( selectActionPage.getAction() == MemSpyAction.IMPORT_HEAP ){
					final ArrayList<ThreadInfo> importedHeaps = importHeapPage.getImportedHeaps();
					importFiles = new IRunnableWithProgress() {
						public void run(IProgressMonitor monitor) {
							monitor.beginTask(IMPORTING_FILES, IProgressMonitor.UNKNOWN);
							
							// import heaps
							importEngine.importAndAnalyseHeap( importedHeaps, finalGenerator, true );
						}
					};
				}//if Comparing heaps
				else if( selectActionPage.getAction() == MemSpyAction.COMPARE_HEAPS ){
					
					final ArrayList<ThreadInfo> heaps = new ArrayList<ThreadInfo>();
					
					heaps.add( compareHeapsFirstPage.getRecentHeap() );
					heaps.add( compareHeapsSecondPage.getRecentHeap() );
					final String output = exportFileNamePage.getOutputFileName();
	
					importFiles = new IRunnableWithProgress() {
						public void run(IProgressMonitor monitor) {
							monitor.beginTask(IMPORTING_FILES, IProgressMonitor.UNKNOWN);
							// import heaps
							importEngine.importAndAnalyseHeap( heaps, finalGenerator, false );
							// compare heaps
							importEngine.compareHeaps( heaps.get(0), heaps.get(1), finalGenerator, output );
						}
					};
					
				}
			}
			else{ // if importing SWMT-logs
				final ArrayList<SWMTLogInfo> logList = swmtLogPage.getLogList();
				final AnalyserXMLGenerator finalGenerator = generator;
				importFiles = new IRunnableWithProgress() {
					public void run(IProgressMonitor monitor) {
						monitor.beginTask(IMPORTING_FILES, IProgressMonitor.UNKNOWN);

						// import heaps if SWMT logging was also dumping heap data for thread(s)
						final ArrayList<ThreadInfo> importedHeaps = traceEngine.getImportedSWMTHeaps();
						if(importedHeaps != null){
							//Don't delete temp folder and files yet, SWMT import will do that
							importEngine.importAndAnalyseHeap( importedHeaps, finalGenerator, false, false );
						}						
						
						// import swmt-logs and delete temp files & folder
						importEngine.importSWMTLogs( logList, true );						
						
					}
				};
			}
			try {
				getContainer().run(true, false, importFiles);
			} 
			catch (InvocationTargetException e1) {
				// do nothing
				e1.printStackTrace();
			} 
			catch (InterruptedException e1) {
				// do nothing
				e1.printStackTrace();
			}
			MemSpyMainView.showAndReturnYourself();
		}
		else if( this.wizardType == MemSpyWizardType.COMPARE ){
			
			IRunnableWithProgress importFiles = null;

			compareHeapsFirstPage.getTraceEngine().shutDownMemSpy();

			// Initialize XML generator
			AnalyserXMLGenerator generator = new AnalyserXMLGenerator();
			
			// read symbol definitions
			this.getSymbolInformation(generator);
			
			final AnalyserXMLGenerator finalGenerator = generator; 
			final ThreadInfo secondHeap = compareHeapsSecondPage.getRecentHeap();
			
			// create new ThreadInfo object and format combobox value from first compare page to it's 
			// threadFilePath variable.(We don't want to import that heap again as it has already 
			// been updated.) 
			ThreadInfo firstModifiedHeap = new ThreadInfo();
			firstModifiedHeap.setThreadFilePath( compareHeapsFirstPage.getSelectedFilePath() );
			firstModifiedHeap.setThreadName( secondHeap.getThreadName() );
			final ThreadInfo firstHeap = firstModifiedHeap;
			
			final String output = exportFileNamePage.getOutputFileName();

			importFiles = new IRunnableWithProgress() {
				public void run(IProgressMonitor monitor) {
					monitor.beginTask(IMPORTING_FILES, IProgressMonitor.UNKNOWN);
					ArrayList<ThreadInfo> heaps = new ArrayList<ThreadInfo>();
					heaps.add( secondHeap );
					importEngine.importAndAnalyseHeap(heaps, finalGenerator, false);
					importEngine.compareHeaps( firstHeap, secondHeap, finalGenerator, output );
				}
			};
				
			
			try {
				getContainer().run(true, false, importFiles);
			} 
			catch (InvocationTargetException e1) {
				// do nothing
				e1.printStackTrace();
			} 
			catch (InterruptedException e1) {
				// do nothing
				e1.printStackTrace();
			}
			MemSpyMainView.showAndReturnYourself();
			
		}
		else if(this.wizardType == MemSpyWizardType.SYMBOLS ){
			
			// read symbol definitions
			this.getSymbolInformation(fillValues);
			
			// send new symbols to Main View
			IRunnableWithProgress importFiles = new IRunnableWithProgress() {
				public void run(IProgressMonitor monitor) {
					monitor.beginTask("Saving new symbol definitions", IProgressMonitor.UNKNOWN);
					mainView.symbolsUpdated(fillValues);
					monitor.done();
				}
			};
				
			
			try {
				getContainer().run(true, false, importFiles);
			} 
			catch (InvocationTargetException e1) {
				// do nothing
				e1.printStackTrace();
			} 
			catch (InterruptedException e1) {
				// do nothing
				e1.printStackTrace();
			}
		}
		return true;
	}

	/**
	 * (non-Javadoc)
	 * @see org.eclipse.jface.wizard.Wizard#performCancel()
	 */
	public boolean performCancel() {		
		try {
			if( this.wizardType == MemSpyWizardType.FULL){
				this.saveUserEnteredData();
			}
			
			MemSpyMainView.showAndReturnYourself();
			
			// delete temp files
			MemSpyFileOperations.deleteTempMemSpyFiles();
			traceEngine.shutDownMemSpy();			
		} catch (Exception e) {
			// Some failure in above should not prevent user Canceling dialog
			e.printStackTrace();
			DbgUtility.println(DbgUtility.PRIORITY_OPERATION, "performCancel/exception: " + e);
		}
		
		return true;
	}

	/**
	 * (non-Javadoc)
	 * @see org.eclipse.jface.wizard.Wizard#getPreviousPage(org.eclipse.jface.wizard.IWizardPage)
	 */
	public IWizardPage getPreviousPage(IWizardPage page) {
		if (page.equals(importHeapPage)) {
			return selectActionPage;
		} else if (page.equals(compareHeapsFirstPage)) {
			return selectActionPage;
		} else if (page.equals(compareHeapsSecondPage)) {
			return compareHeapsFirstPage;
		}
		return page;
	}
	
	/**
	 * setComparedHeaps
	 * sets compared thread names into each compare page
	 */
	public void setComparedHeaps(){
		
		// get recent thread infos from compare heap pages
		ThreadInfo firstHeap = null;
		ThreadInfo secondHeap = null;
		
		firstHeap = compareHeapsFirstPage.getRecentHeap();
		secondHeap = compareHeapsSecondPage.getRecentHeap();
	
		// save thread infos into compare pages
		compareHeapsSecondPage.setComparedHeap(firstHeap, secondHeap);
		compareHeapsFirstPage.setComparedHeap(firstHeap, secondHeap);
	}
	
	/**
	 * updateThreadLists
	 * updates tread lists into each file or device selection page
	 * @param threadList
	 */	
	public void updateThreadLists(ArrayList<ThreadInfo> threadList){
		// Each page gets new thread list so that status of threads won't change when another page is modified.
		if(importHeapPage != null){
			this.importHeapPage.setDeviceThreadList( copyThreadList( threadList ) );
			this.importHeapPage.updateThreadList();
		}		
		if(compareHeapsFirstPage != null){
			this.compareHeapsFirstPage.setDeviceThreadList( copyThreadList( threadList ) );
			this.compareHeapsFirstPage.updateThreadList();
		}
		if(compareHeapsFirstPage != null){
			this.compareHeapsSecondPage.setDeviceThreadList( copyThreadList( threadList ) );
			this.compareHeapsSecondPage.updateThreadList();
		}
	}
	
	/**
	 * Copies threads to new list.
	 * @param threadList List to be copied.
	 * @return New list containing thread information in new list.
	 */
	private ArrayList<ThreadInfo> copyThreadList(ArrayList<ThreadInfo> threadList) {
		ArrayList<ThreadInfo> returnList = new ArrayList<ThreadInfo>();
		for(ThreadInfo thread : threadList) {
			returnList.add(thread.clone());
		}
		return returnList;
	}
	
	/**
	 * sets thread list selection and hides threadListTable from 2. comparePage
	 * @param thread Thread information.
	 */
	public void setThreadListSelectionToComparePages( ThreadInfo thread ){
		this.compareHeapsSecondPage.setThreadListSelection( thread );
	}
	
	/**
	 * shows or hides threadListTable in 2. comparePage
	 */
	public void showImportedHeapsInComparePage( boolean value ){
		this.compareHeapsSecondPage.setImportedThreadTableVisible( value );
	
	}
	

	
	/**
	 * Check if compared heaps are from same thread
	 * @return true if heaps are from same thread or other or both heaps are still undefined
	 */
	public boolean areComparedHeapsFromSameThread(){
		if( compareHeapsFirstPage.getRecentHeap() != null && 
			compareHeapsSecondPage.getRecentHeap() != null ){
			if( compareHeapsFirstPage.getRecentHeap().getThreadName().equals(compareHeapsSecondPage.getRecentHeap().getThreadName() ) ){
				return true;
			}
			else{
				return false;
			}
		}
		else{
			return true;
		}

	}
	
	
	/**
	 * saveUserEnteredData
	 * calls saveUserEnteredData method of all wizard pages.
	 */
	private void saveUserEnteredData(){
		
		// Save form values so that they can be restored later
		if( selectActionPage != null){
			selectActionPage.saveUserEnteredData();
		}
		if( importHeapPage != null){
			importHeapPage.saveUserEnteredData();
		}
		if( parameterFilesPage != null){
			parameterFilesPage.saveUserEnteredData();
		}
		if( compareHeapsFirstPage != null){
			compareHeapsFirstPage.saveUserEnteredData();
		}
		if( compareHeapsSecondPage != null){
			compareHeapsSecondPage.saveUserEnteredData();
		}
		if( exportFileNamePage != null){
			exportFileNamePage.saveUserEnteredData();
		}
		if( swmtLogPage != null){
			swmtLogPage.saveUserEnteredData();
		}
	}

	public void updateConnectionSettings(){
		if( importHeapPage != null ){
			this.importHeapPage.updateConnectionSettings();
		}
		if( compareHeapsFirstPage != null ){
			this.compareHeapsFirstPage.updateConnectionSettings();
		}
		if( compareHeapsSecondPage != null ){
			this.compareHeapsSecondPage.updateConnectionSettings();	
		}
		if( swmtLogPage != null ){
			this.swmtLogPage.updateConnectionSettings();
		}
		
		if(traceEngine != null){
			// Resetting progress status in case connection settings have been changed
			traceEngine.resetProgressStatus();
		}
		
	}

	public void init(IWorkbench arg0, IStructuredSelection arg1) {
		
	}
	
	public void setData(MemSpyWizardDialog wizDialog){
		this.wizDialog = wizDialog;
	}

	public void setCancelText( String newText ){
		wizDialog.setCancelText( newText );
	}
	
	private void getSymbolInformation( AnalyserXMLGenerator symbolInfo ){
	
		// combine map and symbol files into one String[] and set them into xmlGenerator
		ArrayList<String> debugMetaData;
		String[] symbolFiles = parameterFilesPage.getSymbolFiles();
		if( symbolFiles == null ){
			debugMetaData = new ArrayList<String>();
		}
		else{
			debugMetaData = new ArrayList<String>(Arrays.asList( symbolFiles ));
		}
		if( parameterFilesPage.getMapFilesZip().equals("") == false ){
			debugMetaData.add( parameterFilesPage.getMapFilesZip() );
		}
		
		// Set meta data folder				
		symbolInfo.setXMLDebugMetaDataFile(debugMetaData.toArray(new String [debugMetaData.size()]));
		if( parameterFilesPage.getMapFilesFolder().equals("") == false ){
			symbolInfo.setXMLDebugMetaDataDirectory( parameterFilesPage.getMapFilesFolder() );
		}
		else{
			symbolInfo.setXMLDebugMetaDataDirectory(null);
		}
		
	}
		
}
