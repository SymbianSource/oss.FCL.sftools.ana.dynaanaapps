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



package com.nokia.s60tools.memspy.ui.views;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.part.ViewPart;

import com.nokia.s60tools.heapanalyser.interfaces.HeapAnalyserLauncher;
import com.nokia.s60tools.memspy.interfaces.IAnalyseHeapJobListener;
import com.nokia.s60tools.memspy.interfaces.INewMemSpyFilesObserver;
import com.nokia.s60tools.memspy.model.AnalyseHeapJob;
import com.nokia.s60tools.memspy.model.AnalyserXMLGenerator;
import com.nokia.s60tools.memspy.model.AnalyserXMLParser;
import com.nokia.s60tools.memspy.model.ImportEngine;
import com.nokia.s60tools.memspy.model.MemSpyFileBundle;
import com.nokia.s60tools.memspy.model.MemSpyFileOperations;
import com.nokia.s60tools.memspy.model.UserEnteredData;
import com.nokia.s60tools.memspy.model.UserEnteredData.ValueTypes;
import com.nokia.s60tools.memspy.plugin.MemSpyPlugin;
import com.nokia.s60tools.memspy.resources.HelpContextIDs;
import com.nokia.s60tools.memspy.resources.ImageKeys;
import com.nokia.s60tools.memspy.resources.ImageResourceManager;
import com.nokia.s60tools.memspy.ui.wizards.MemSpyWizard;
import com.nokia.s60tools.memspy.ui.wizards.MemSpyWizardDialog;
import com.nokia.s60tools.memspy.ui.wizards.MemSpyWizard.MemSpyWizardType;
import com.nokia.s60tools.memspy.util.MemSpyConsole;
import com.nokia.s60tools.swmtanalyser.ui.actions.SwmtAnalyser;
import com.nokia.s60tools.ui.S60ToolsTable;
import com.nokia.s60tools.ui.S60ToolsTableColumnData;
import com.nokia.s60tools.ui.S60ToolsTableFactory;
import com.nokia.s60tools.ui.S60ToolsViewerSorter;
import com.nokia.s60tools.util.debug.DbgUtility;



/**
 * <p>
 * MemSpyMainView.
 * The main view of MemSpy Carbide extension. Shows imported MemSpy data files in a table and allows user to:
 *  - Launch Heap Analyser to analyse one Heap Dump file.
 *  - Launch Heap Analyser to compare two Heap Dump files.
 *  - Launch SWMT-analyser and analyse one System Wide Memory Tracking log.
 * <p>
 */

public class MemSpyMainView extends ViewPart implements INewMemSpyFilesObserver, SelectionListener, IAnalyseHeapJobListener, KeyListener {
	

	/**
	 * Enumeration for actions for a MemSpy file
	 */
	private enum FileActionType{NONE, ANALYSE_SWMT_ONE, ANALYSE_SWMT_MULTIPLE, ANALYSE_HEAP, COMPARE_HEAPS, DELETE};
	
	/**
	 *  TraceViewers ID com.nokia.traceviewer.view.TraceViewerView
	 */
	private static final String TRACE_VIEWER_VIEW_ID = "com.nokia.traceviewer.view.TraceViewerView";
	
	/**
	 *  Main view's ID com.nokia.s60tools.memspy.ui.views.MemSpyMainView
	 */
	private static final String ID = "com.nokia.s60tools.memspy.ui.views.MemSpyMainView";

	/* viewer for table */
	private TableViewer viewer;
	
	/* contentProvider for table */
	private MemSpyMainViewContentProvider contentProvider;
	
	/* Launch Heap Analyser - action */
	private Action launchHeapAnalyserAction;
	
	/* Launch Wizard - action */
	private Action launchWizardAction;
	
	/* compare two heaps - action */
	private Action compareTwoHeapsAction;
	
	/* import and compare two heaps - action */
	private Action importAndCompareHeapsAction;

	/* import and compare two heaps - toolbar action
	   that changes based on selection*/
	private Action compareHeapsToolbarAction;
	
	/* Launch SWMT-analyser - action */
	private Action launchSWMTAction;
	
	/* delete selected files - action */
	private Action deleteAction;

	/* import engine */
	private ImportEngine importEngine;
	
	/* doubleClick - action */
	private Action doubleClickAction;

	/* open file in text editor */
	private Action openFile;
	
	/* edit symbol information */
	private Action editSymbols;
	

	//
	//Strings for error and info messages
	//
	
	/**
	 * Error message for analysis startup
	 */
	public final static String ERROR_LAUNCH_SWMT_ANALYSER				= "MemSpy was unable to start analysing imported files.";
	/**
	 * Error message for Heap Dump import
	 */
	public final static String ERROR_HEAP_IMPORT_FAILED 				= "Some of the Heap Dumps were not imported successfully.( file operations were not successfull )";
	/**
	 * Error message for SWMT import
	 */
	public final static String ERROR_SWMT_IMPORT_FAILED 				= "SWMT-files were not imported successfully. ( file operations were not successfull )";
	/**
	 * Error message for import
	 */
	public final static String ERROR_IMPORT_HEADER 						= "Import error";
	/**
	 * Warning message for compare
	 */
	public final static String WARNING_COMPARE							= "Compare Warning";
	/**
	 * Warning message for filea allready exist
	 */
	public final static String WARNING_FILE_EXISTS						= "The file already exists. Do you want to replace the existing file?";

	private final static String ERROR_COMPARE							= "Compare error";
	private final static String ERROR_HEAP_THREADS_NOT_SAME				= "Selected Heaps cannot be compared. Only two Heap Dumps that are from same thread can be compared.";
	private final static String ERROR_TOPIC								= "MemSpy Error";
	private final static String ERROR_CONFIGURATION_FILES_NOT_FOUND 	= "Configuration files for selected heaps cannot be found.";
	private final static String ERROR_UNABLE_TO_GENERATE_CONFIGURATIONS = "Error was encountered when generating files that are needed when running Heap Analyser.";
	private final static String ERROR_HEAP_ANALYSER 					= "Heap Analyser error.";
	private final static String WARNING_SYMBOLS_NOT_SAME				= "Selected files symbol definitions differ, do you wish to compare heaps anyway?";
	private final static String FILE_DIALOG_DEFINE_OUTPUT				= "Define output file name";
	private final static String CONFIRMATION							= "Confirmation";
	private final static String CONFIRMATION_OPEN_FILE 					= "Open generated xls-file with application that is assosiated for that file type?";	
	
	private final static String TOOLTIP_COMPARE_2_IMPORTED_FILE			= "Compare selected heaps with Heap Analyser";
	private final static String TOOLTIP_TEXT_IMPORT_AND_COMPARE			= "Import heap dump and compare it to selected heap";

	
	/**
	 * The constructor.
	 */
	public MemSpyMainView() {
		importEngine = new ImportEngine( this );
	}
	/* (non-Javadoc)
	 * @see org.eclipse.ui.part.WorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
	 */
	public void createPartControl(Composite parent) {
		
		
		ArrayList<S60ToolsTableColumnData> columnDataArr = new ArrayList<S60ToolsTableColumnData>();
		
		columnDataArr.add(new S60ToolsTableColumnData("File Type", 150, 0, MemSpyDataSorter.FILE_TYPE)); 
		columnDataArr.add(new S60ToolsTableColumnData("File Name", 300, 1, MemSpyDataSorter.FILENAME));
		columnDataArr.add(new S60ToolsTableColumnData("Time", 150, 2, MemSpyDataSorter.TIME));
		 
		 
		S60ToolsTableColumnData[] arr = columnDataArr.toArray(new S60ToolsTableColumnData[0]);
		
		S60ToolsTable tbl = S60ToolsTableFactory.create(parent, arr);
		
		viewer = new TableViewer( tbl.getTableInstance() );
		contentProvider = new MemSpyMainViewContentProvider( this );
		viewer.setContentProvider( contentProvider );
		viewer.setLabelProvider(new MemSpyMainViewLabelProvider());
		viewer.setComparator(new MemSpyDataSorter());
		viewer.setSorter(new MemSpyDataSorter());
		viewer.setInput(getViewSite());
			
		tbl.addSelectionListener(this);
		tbl.addKeyListener(this);
		tbl.setHostingViewer(viewer);

		try {
			PlatformUI.getWorkbench().getHelpSystem().setHelp(viewer.getControl(),HelpContextIDs.MEMSPY_MAIN_VIEW);
		} catch (Exception e) {
			e.printStackTrace();
		}
		// Make actions
		makeActions();
		
		// hook context menus
		hookContextMenu();
		
		// hook double click action
		hookDoubleClickAction();
		
		// contribute to action bars
		contributeToActionBars();
		
		// refresh view
		this.refreshView();
		
		// update toolbar items state
		this.enableAndDisableToolbarItems();

		
		
	}

	/**
	 * hookContextMenu.
	 * Creates context menu.
	 */
	private void hookContextMenu() {
		MenuManager menuMgr = new MenuManager("#PopupMenu");
		
		// Adding all actions to menu.
		menuMgr.add(launchHeapAnalyserAction);
		menuMgr.add(compareTwoHeapsAction);
		menuMgr.add(importAndCompareHeapsAction);
		menuMgr.add(launchSWMTAction);
		menuMgr.add(editSymbols);
		menuMgr.add(openFile);
		menuMgr.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
		menuMgr.add(deleteAction);
		
		menuMgr.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager manager) {
				MemSpyMainView.this.fillContextMenu(manager);
			}
		});
		Menu menu = menuMgr.createContextMenu(viewer.getControl());
		viewer.getControl().setMenu(menu);
		getSite().registerContextMenu(menuMgr, viewer);
	}

	/**
	 * contributeToActionBars.
	 * fills pull down menu and local toolbar
	 */
	private void contributeToActionBars() {
		IActionBars bars = getViewSite().getActionBars();
		fillLocalToolBar(bars.getToolBarManager());
	}

	
	/**
	 * fillContextMenu.
	 * fills context menu depending what type of file or files are selected.
	 * @param manager menuManager
	 */
	private void fillContextMenu(IMenuManager manager) {
		
		FileActionType availableAction = this.getAvailableAction();
		
		if( availableAction == FileActionType.ANALYSE_HEAP ){
			launchHeapAnalyserAction.setEnabled(true);
			compareTwoHeapsAction.setEnabled(false);
			importAndCompareHeapsAction.setEnabled(true);
			launchSWMTAction.setEnabled(false);
			editSymbols.setEnabled(true);
			openFile.setEnabled(true);
			deleteAction.setEnabled(true);
		}
		else if( availableAction == FileActionType.ANALYSE_SWMT_ONE ){
			launchHeapAnalyserAction.setEnabled(false);
			compareTwoHeapsAction.setEnabled(false);
			importAndCompareHeapsAction.setEnabled(false);
			launchSWMTAction.setEnabled(true);
			editSymbols.setEnabled(false);
			openFile.setEnabled(true);
			deleteAction.setEnabled(true);
		}
		else if( availableAction == FileActionType.ANALYSE_SWMT_MULTIPLE ){
			launchHeapAnalyserAction.setEnabled(false);
			compareTwoHeapsAction.setEnabled(false);
			importAndCompareHeapsAction.setEnabled(false);
			launchSWMTAction.setEnabled(true);
			editSymbols.setEnabled(false);
			openFile.setEnabled(false);
			deleteAction.setEnabled(true);
		}
		else if( availableAction == FileActionType.COMPARE_HEAPS ){
			launchHeapAnalyserAction.setEnabled(false);
			compareTwoHeapsAction.setEnabled(true);
			importAndCompareHeapsAction.setEnabled(false);
			launchSWMTAction.setEnabled(false);
			editSymbols.setEnabled(false);
			openFile.setEnabled(false);
			deleteAction.setEnabled(true);
		}
		else if( availableAction == FileActionType.DELETE ){
			launchHeapAnalyserAction.setEnabled(false);
			compareTwoHeapsAction.setEnabled(false);
			importAndCompareHeapsAction.setEnabled(false);
			launchSWMTAction.setEnabled(false);
			editSymbols.setEnabled(false);
			openFile.setEnabled(false);
			deleteAction.setEnabled(true);
		}
		else if( availableAction == FileActionType.NONE ){
			launchHeapAnalyserAction.setEnabled(false);
			compareTwoHeapsAction.setEnabled(false);
			importAndCompareHeapsAction.setEnabled(false);
			launchSWMTAction.setEnabled(false);
			editSymbols.setEnabled(false);
			openFile.setEnabled(false);
			deleteAction.setEnabled(false);
		}
	}
	
	
	/**
	 * getAvailableAction.
	 * @return action type that is available for item or items that are currently selected.
	 */
	private FileActionType getAvailableAction(){
		
		// get selected items
		IStructuredSelection selection = (IStructuredSelection) viewer.getSelection();
		
		@SuppressWarnings("unchecked")
		List selectionList = selection.toList();
		
		boolean swmtFileFound = false;
		boolean heapFileFound = false;
		
		// go thru selected files-list
		for(Object obj : selectionList){
			MemSpyFileBundle bundle = (MemSpyFileBundle) obj;
			if( bundle.getFileType().equals("SWMT-Log") ){
				swmtFileFound = true;
			}
			else{
				heapFileFound = true;
			}
		}
		
		// if two files and no swmt files are found, compare heaps - action is available
		if( selectionList.size() == 2 && heapFileFound && !swmtFileFound ){
			return FileActionType.COMPARE_HEAPS;
		}
		
		// if swmt-files are found and no heap dumps are found, analyse swmt-action is available 
		else if( swmtFileFound && selectionList.size() == 1 ){
			return FileActionType.ANALYSE_SWMT_ONE;
		}
		else if( swmtFileFound && !heapFileFound){
			return FileActionType.ANALYSE_SWMT_MULTIPLE;
		}
		
		// if heap one heap file is found, analyse heap - action is available
		else if( heapFileFound && selectionList.size() == 1 ){
			return FileActionType.ANALYSE_HEAP;
		}
		
		// There are files selected, but only available action is delete.
		else if( selectionList.size() > 0 ){
			return FileActionType.DELETE;
		}
		
		// No files selected - no actions are available
		return FileActionType.NONE;
	}
	
	
	/**
	 * enableAndDisableToolbarItems.
	 * Enables and disable toolbar items according to type of the selected file.
	 */
	private void enableAndDisableToolbarItems(){
		FileActionType availableAction = this.getAvailableAction();
		
		if( availableAction == FileActionType.ANALYSE_HEAP ){
			launchSWMTAction.setEnabled(false);
			launchHeapAnalyserAction.setEnabled(true);
			compareHeapsToolbarAction.setEnabled(true);
			compareHeapsToolbarAction.setToolTipText(TOOLTIP_TEXT_IMPORT_AND_COMPARE);
			deleteAction.setEnabled(true);
			
			
		}
		else if( availableAction == FileActionType.ANALYSE_SWMT_MULTIPLE || availableAction == FileActionType.ANALYSE_SWMT_ONE ){
			launchSWMTAction.setEnabled(true);
			launchHeapAnalyserAction.setEnabled(false);
			compareHeapsToolbarAction.setEnabled(false);
			deleteAction.setEnabled(true);
		}
		else if( availableAction == FileActionType.COMPARE_HEAPS ){
			launchSWMTAction.setEnabled(false);
			launchHeapAnalyserAction.setEnabled(false);
			compareHeapsToolbarAction.setEnabled(true);
			compareHeapsToolbarAction.setToolTipText(TOOLTIP_COMPARE_2_IMPORTED_FILE);
			deleteAction.setEnabled(true);

		}
		else if( availableAction == FileActionType.DELETE ){
			launchSWMTAction.setEnabled(false);
			launchHeapAnalyserAction.setEnabled(false);
			compareHeapsToolbarAction.setEnabled(false);
			deleteAction.setEnabled(true);
		}	
		else if( availableAction == FileActionType.NONE ){
			launchSWMTAction.setEnabled(false);
			launchHeapAnalyserAction.setEnabled(false);
			compareHeapsToolbarAction.setEnabled(false);
			deleteAction.setEnabled(false);
		}	
	}
	
	
	/**
	 * fillLocalToolBar.
	 * fills toolbar
	 * @param manager Toolbar manager
	 */
	private void fillLocalToolBar(IToolBarManager manager) {
		manager.add(launchSWMTAction);
		manager.add(compareHeapsToolbarAction);
		manager.add(launchHeapAnalyserAction);
		manager.add(launchWizardAction);
		manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
		manager.add(deleteAction);

	}

	
	/**
	 * makeActions.
	 * Creates actions
	 */
	private void makeActions() {

		launchSWMTAction = new Action() {
			@SuppressWarnings("unchecked")
			public void run() {
				
				// get selection list.
				IStructuredSelection selection = (IStructuredSelection) viewer.getSelection();
				
				// ensure that selected items are swmt-logs 
				if( !selection.isEmpty() ){
					
					List<MemSpyFileBundle> list = selection.toList();
					ArrayList<String> swmtLogs = new ArrayList<String>();

					boolean allFilesAreSwmtLogs = true;
					
					// Go thru bundle list and add all file paths into arraylist
					for( MemSpyFileBundle item : list ){
						swmtLogs.add( item.getFilePath() );
						if( item.getFileType().equals("SWMT-log") ){
							allFilesAreSwmtLogs = false;
						}
					}
					
					// ensure that all selected files were swmt logs
					if( allFilesAreSwmtLogs ){
						// launch swmt analyser for selected logs.
						SwmtAnalyser analyser = new SwmtAnalyser(MemSpyConsole.getInstance());
						analyser.analyse( swmtLogs );
					}
				}
			}
		};
		launchSWMTAction.setText("Launch SWMT-Analyser");
		launchSWMTAction.setToolTipText("Launch SWMT-Analyser");
		launchSWMTAction.setImageDescriptor(ImageResourceManager.getImageDescriptor(ImageKeys.IMG_LAUNCH_SWMT));
	
		
		compareTwoHeapsAction = new Action() {
			@SuppressWarnings("unchecked")
			public void run() {
				
				// get selected items:
				ISelection selection = viewer.getSelection();
				Object obj = ((IStructuredSelection)selection).toList();
				
				List<MemSpyFileBundle> selectionList = (List<MemSpyFileBundle>)obj;
								
				//verify that to heap files are selected.
				if( getAvailableAction() == FileActionType.COMPARE_HEAPS ){
					compareHeaps(selectionList);
				}
			}
		};

		compareTwoHeapsAction.setText(TOOLTIP_COMPARE_2_IMPORTED_FILE);
		compareTwoHeapsAction.setToolTipText(TOOLTIP_COMPARE_2_IMPORTED_FILE);
		compareTwoHeapsAction.setImageDescriptor(ImageResourceManager.getImageDescriptor(ImageKeys.IMG_COMPARE_2_HEAP));
	
		importAndCompareHeapsAction = new Action() {
			@SuppressWarnings("unchecked")
			public void run() {
				
				// get selected items:
				ISelection selection = viewer.getSelection();
				Object obj = ((IStructuredSelection)selection).toList();
				
				List<MemSpyFileBundle> selectionList = (List<MemSpyFileBundle>)obj;
								
				//verify that only one file is selected.
				if( getAvailableAction() == FileActionType.ANALYSE_HEAP ){
					importAndCompareHeaps(selectionList);
				}
			}
		};
		
		importAndCompareHeapsAction.setText(TOOLTIP_TEXT_IMPORT_AND_COMPARE);
		importAndCompareHeapsAction.setToolTipText(TOOLTIP_TEXT_IMPORT_AND_COMPARE);
		importAndCompareHeapsAction.setImageDescriptor(ImageResourceManager.getImageDescriptor(ImageKeys.IMG_COMPARE_2_HEAP));
		
		compareHeapsToolbarAction = new Action() {
			@SuppressWarnings("unchecked")
			public void run() {
				
				// get selected items:
				ISelection selection = viewer.getSelection();
				Object obj = ((IStructuredSelection)selection).toList();
				
				List<MemSpyFileBundle> selectionList = (List<MemSpyFileBundle>)obj;
								
				// check if only one file is selected.
				if( getAvailableAction() == FileActionType.ANALYSE_HEAP ){
					importAndCompareHeaps(selectionList);
				}
				// or if two heaps are selected, then compare them.
				else if( getAvailableAction() == FileActionType.COMPARE_HEAPS ){
					compareHeaps(selectionList);
				}
			}
		};

		compareHeapsToolbarAction.setText(TOOLTIP_COMPARE_2_IMPORTED_FILE);
		compareHeapsToolbarAction.setToolTipText(TOOLTIP_COMPARE_2_IMPORTED_FILE);
		compareHeapsToolbarAction.setImageDescriptor(ImageResourceManager.getImageDescriptor(ImageKeys.IMG_COMPARE_2_HEAP));
		
		launchHeapAnalyserAction = new Action() {
			@SuppressWarnings("unchecked")
			public void run() {
			
				ISelection selection = viewer.getSelection();
				Object obj = ((IStructuredSelection)selection).toList();
				
				List<MemSpyFileBundle> selectionList = (List<MemSpyFileBundle>)obj;
				
				// Get thread name from xml-file
				String xmlFile = selectionList.get(0).getXMLFilePath();
				AnalyserXMLGenerator xml = readXMLFile(xmlFile);
				
				//If Symbol files has not been set to data, before analyze, user have to set a Symbol file
				if(!xml.hasXMLDebugMetaDataFile()){
					//verify that only one file is selected.
					if( selectionList.size() == 1 ){
						//Make user to edit symbol files, and after that, continue to launch
						int retCode = runWizard( MemSpyWizardType.SYMBOLS, xml );
						//Don't giving an error message if user presses Cancel, instead just return
						if(retCode == Window.CANCEL){
							return;
						}
					}						
				}
				
				
				String threadName = xml.getXMLThreadName();
				
				//verify that only one file is selected.
				if( selectionList.size() == 1 ){
					
					//verify that heap dump file is selected.
					if( selectionList.get(0).hasHeapDumpFile() ){
						
						// get xml files path
						String xmlPath = xmlFile;
						
						// launch heap analyser 
						launchHeapAnalyser( xmlPath, null, threadName, true );

					}
					
					
				}
			}
		};
		launchHeapAnalyserAction.setText("Analyse selected heap with Heap Analyser");
		launchHeapAnalyserAction.setToolTipText("Analyse selected heap with Heap Analyser");
		launchHeapAnalyserAction.setImageDescriptor(ImageResourceManager.getImageDescriptor(ImageKeys.IMG_ANALYZE_HEAP));
		
		
		launchWizardAction = new Action() {
			public void run() {
				runWizard( MemSpyWizardType.FULL, null );
			}
		};
		
		launchWizardAction.setText("Launch MemSpy's import Wizard");
		launchWizardAction.setToolTipText("Launch MemSpy's import Wizard");
		launchWizardAction.setImageDescriptor(ImageResourceManager.getImageDescriptor(ImageKeys.IMG_APP_ICON));
		
		
		doubleClickAction = new Action() {
			public void run() {
				FileActionType action = getAvailableAction();
				switch (action){
					case ANALYSE_HEAP:{
						launchHeapAnalyserAction.run();
						break;
					}
					case ANALYSE_SWMT_ONE:
					case ANALYSE_SWMT_MULTIPLE:{
						launchSWMTAction.run();
						break;
					}
					case COMPARE_HEAPS:{
						compareTwoHeapsAction.run();
						break;
					}
					default:{
						// No need to do anything with other action.
						break;
					}
				
				}
			}
		};
		
		
		
		deleteAction = new Action() {
			@SuppressWarnings("unchecked")
			public void run() {
				// get selected items:
				ISelection selection = viewer.getSelection();
				Object obj = ((IStructuredSelection)selection).toList();
				
				
				List<MemSpyFileBundle> selectionList = (List<MemSpyFileBundle>)obj;
				
				// ensure that some item(s) is selected.
				if( selectionList.size() >= 1 ){
					
					// Display a Confirmation dialog
					MessageBox messageBox = new MessageBox(viewer.getControl().getShell(), SWT.ICON_QUESTION | SWT.YES | SWT.NO);
			        messageBox.setText("MemSpy - Delete Files");
			        messageBox.setMessage("Are you sure you want to delete selected files?");
			        int buttonID = messageBox.open();
			        if (buttonID == SWT.YES) {
			        	for( MemSpyFileBundle item : selectionList ){
							
							// delete it
							item.delete();
		
						}
						refreshContentAndView();
			        }
				
				}
				
			}
		};
		
		deleteAction.setText("Delete");
		deleteAction.setToolTipText("Delete selected item");
		deleteAction.setImageDescriptor( PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(ISharedImages.IMG_TOOL_DELETE));	

	
		openFile = new Action() {
			@SuppressWarnings("unchecked")
			public void run() {
				ISelection selection = viewer.getSelection();
				Object obj = ((IStructuredSelection)selection).toList();
				
				List<MemSpyFileBundle> selectionList = (List<MemSpyFileBundle>)obj;
				
				//verify that only one file is selected.
				if( selectionList.size() == 1 ){

					// open selected log file in editor.
					File fileToOpen = new File(selectionList.get(0).getFilePath());
					if (fileToOpen.exists() && fileToOpen.isFile()) {
					    IFileStore fileStore = EFS.getLocalFileSystem().getStore(fileToOpen.toURI());
					    IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
					 
					    try {
					        IDE.openEditorOnFileStore( page, fileStore );
					    } catch ( PartInitException e ) {
					    	showErrorMessage(ERROR_TOPIC, "MemSpy was unable to open file.");
					    }
					} else {
				    	showErrorMessage(ERROR_TOPIC, "File does not exists.");

					}
					
				}
			}
		};
		openFile.setText("Open selected file in text editor");
		openFile.setToolTipText("Open selected file in text editor");

		editSymbols = new Action() {
			@SuppressWarnings("unchecked")
			public void run() {
				ISelection selection = viewer.getSelection();
				Object obj = ((IStructuredSelection)selection).toList();
				
				List<MemSpyFileBundle> selectionList = (List<MemSpyFileBundle>)obj;
				
				//verify that only one file is selected.
				if( selectionList.size() == 1 ){
					AnalyserXMLGenerator info = readXMLFile(selectionList.get(0).getXMLFilePath()); 
					runWizard( MemSpyWizardType.SYMBOLS, info );
				}
			}
		};
		editSymbols.setText("Edit symbol definitions of selected Heap Dump");
		editSymbols.setToolTipText("Edit symbol definitions of selected Heap Dump");
	}

	
	/**
	 * Starts wizard to compare currently selected heap file.
	 * @param selectionList List from which selected file is found.
	 */
	private void importAndCompareHeaps(List<MemSpyFileBundle> selectionList) {
		// get info from xml-file
		AnalyserXMLGenerator info = readXMLFile(selectionList.get(0).getXMLFilePath());
		
		// set source files path into info
		info.setXMLSourceFile(new String[]{ selectionList.get(0).getFilePath() });
		
		// start wizard
		runWizard( MemSpyWizardType.COMPARE, info);
	}
	
	/**
	 * Starts Heap analyzer to compare two currently selected heaps.
	 * @param selectionList List from which selected heap files are found.
	 */
	private void compareHeaps(List<MemSpyFileBundle> selectionList) {
		// get selected file bundles.
		MemSpyFileBundle bundle1 = selectionList.get(0);
		MemSpyFileBundle bundle2 = selectionList.get(1);
		
		// read both Heap Dumps configuration files
		AnalyserXMLGenerator generator1 = readXMLFile(bundle1.getXMLFilePath());
		AnalyserXMLGenerator generator2 = readXMLFile(bundle2.getXMLFilePath());
		
		// ensure that both generators were created successfully.
		if( generator1 == null || generator2 == null ){
			MessageDialog.openError( getSite().getShell(), ERROR_COMPARE, ERROR_CONFIGURATION_FILES_NOT_FOUND );
			return;
		}
		
		// if Heaps are not from same thread show error message
		if( !generator1.getXMLThreadName().equals( generator2.getXMLThreadName() ) ){
			MessageDialog.openError( getSite().getShell(), ERROR_COMPARE, ERROR_HEAP_THREADS_NOT_SAME );
			return;
		}
		
		boolean compare = true;
		
		// if symbol definitions are not same, confirm that user really wants continue comparing 
		if( !Arrays.equals(generator1.getXMLDebugMetaDataFile(), generator2.getXMLDebugMetaDataFile() ) ){
			// Display a Confirmation dialog
			MessageBox messageBox = new MessageBox(viewer.getControl().getShell(), SWT.ICON_QUESTION | SWT.YES | SWT.NO);
	        messageBox.setText( WARNING_COMPARE );
	        messageBox.setMessage( WARNING_SYMBOLS_NOT_SAME );
	        int buttonID = messageBox.open();
	        if (buttonID == SWT.NO) {
	        	compare = false;
	        }
		}
		if( compare ){
			
			UserEnteredData data = new UserEnteredData();
								
			// open file dialog for selecting a MemSpy file
			FileDialog dialog = new FileDialog( getSite().getShell(), SWT.SAVE );
			dialog.setText(FILE_DIALOG_DEFINE_OUTPUT);
			String[] filterExt = { "*.xls" };
			dialog.setFilterExtensions(filterExt);
			
	 		// Restore previous output filename
			String[] lastUsedFiles = data.getPreviousValues(ValueTypes.OUTPUT_FILE);
			if( lastUsedFiles != null && lastUsedFiles.length > 0 ){
				dialog.setFileName(lastUsedFiles[0]);
			}
			
			String result = dialog.open();
			
			
			if( result != null ){
				
				File file = new File( result );
				// If file already exists, confirm that user wants to rewrite it. 
				if(	file.exists() ){
			
					// Display a Confirmation dialog
					MessageBox messageBox = new MessageBox(viewer.getControl().getShell(), SWT.ICON_QUESTION | SWT.YES | SWT.NO);
			        messageBox.setText( WARNING_COMPARE );
			        messageBox.setMessage( WARNING_FILE_EXISTS );
			        int buttonID = messageBox.open();
			        if (buttonID == SWT.NO) {
			        	compare = false;
			        }
				}
				
				if( compare ){
				
					// Save output filename so that it can be restored later
					data.saveValue(ValueTypes.OUTPUT_FILE, result);
					
					generator1.setXMLAnalyseFileOutput(result);
					String[] sources = new String[]{ bundle1.getFilePath(), bundle2.getFilePath() };

					// generate configuration file for comparison
					if( importEngine.generateCompareConfigurationFile(sources, generator1.getXMLThreadName(), generator1)){		
						launchHeapAnalyser( MemSpyFileOperations.getCompareConfigurationFilePath(), result, generator1.getXMLThreadName(), false );
					}
					else{
						MessageDialog.openError( getSite().getShell(), ERROR_COMPARE, ERROR_UNABLE_TO_GENERATE_CONFIGURATIONS );
						return;
					}
				}
			}
		}
	}
	
	/**
	 * hookDoubleClickAction
	 * adds double click listener
	 */
	private void hookDoubleClickAction() {
		viewer.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent event) {
				doubleClickAction.run();
			}
		});
	}

	/**
	 * setFocus.
	 * Passing the focus request to the viewer's control.
	 */
	public void setFocus() {
		viewer.getControl().setFocus();
	}
	
	
	/**
     * Makes main view visible and returns an instance of itself. Can be called from
     * an UI thread only.
     * @return instance of main view
     */
    public static MemSpyMainView showAndReturnYourself() {
    	return showAndReturnYourself(false);
    }
    
    /**
     * Makes main view visible and returns an instance of itself. Can be called from
     * an UI thread only.
     * @return instance of main view
     */
	public static MemSpyMainView showAndReturnYourself(boolean openOnly) {
    	try {
    		
    		IWorkbenchWindow ww = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
    		if (ww == null)
    			return null;
    		
    		IWorkbenchPage page = ww.getActivePage();
    		IWorkbenchPart currentPart = page.getActivePart();
    		
    		// Checking if view is already open
    		IViewReference[] viewRefs = page.getViewReferences();
    		for (int i = 0; i < viewRefs.length; i++) {
				IViewReference reference = viewRefs[i];
				String id = reference.getId();
				if(id.equalsIgnoreCase(MemSpyMainView.ID)){
					// Found, restoring the view
					IViewPart viewPart = reference.getView(true);
					if (!openOnly)
						page.activate(viewPart);
					return (MemSpyMainView)viewPart;
				}
			}
    		
    		// View was not found, opening it up as a new view.
    		MemSpyMainView mView = (MemSpyMainView)page.showView(MemSpyMainView.ID);
    		if (openOnly)
    			page.bringToTop(currentPart);
    		return mView;
    		
    	} catch (Exception e) {
			e.printStackTrace();
			
			return null;
		}
	}
	
	/**
	 * runWizard().
	 * Starts MemSpy import wizard.
	 * @return {@link Window#OK} if Finish pressed, {@link Window#CANCEL} if Cancel was pressed.
	 */
	public int runWizard( MemSpyWizardType wizardType, AnalyserXMLGenerator info ){

		
		MemSpyWizard wizard = null;
		if( wizardType == MemSpyWizardType.FULL ){
			wizard = new MemSpyWizard( wizardType, null );

		}
		else if( wizardType == MemSpyWizardType.COMPARE || wizardType == MemSpyWizardType.SYMBOLS ){
			wizard = new MemSpyWizard( wizardType, info );
		}
		
		DbgUtility.println(DbgUtility.PRIORITY_OPERATION, "Open wizard dialog");
		// Open wizard dialog
		DbgUtility.println(DbgUtility.PRIORITY_OPERATION, "Create Dialog");
		MemSpyWizardDialog wizDialog = new MemSpyWizardDialog(getViewSite().getShell(), wizard);
		DbgUtility.println(DbgUtility.PRIORITY_OPERATION, "Set dialog data");
		wizard.setData(wizDialog);
		DbgUtility.println(DbgUtility.PRIORITY_OPERATION, "Set dialog size");
		wizDialog.setPageSize(600, 600);
		DbgUtility.println(DbgUtility.PRIORITY_OPERATION, "Add pagecnhangelistener");
		wizDialog.addPageChangedListener(wizard);
	    wizard.setWindowTitle("MemSpy Import Wizard");
		DbgUtility.println(DbgUtility.PRIORITY_OPERATION, "Creating wizaDialog");
	    wizDialog.create();
		DbgUtility.println(DbgUtility.PRIORITY_OPERATION, "open wizaDialog");

	    return wizDialog.open();
	}
	
	/**
	 * readXMLFile.
	 * Reads xml file from given path.
	 * @param XMLFilePath path of xml-file
	 * @return analyserXMLFile with read values.
	 */
	private AnalyserXMLGenerator readXMLFile(String XMLFilePath){
	
		FileReader reader = null;
		try {
			File file = new File( XMLFilePath );
			reader = new FileReader(file );
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return null;
		}
		catch (Exception e) {
			e.printStackTrace();
			return null;
		}		
		BufferedReader bReader = new BufferedReader(reader);
		AnalyserXMLGenerator xml = AnalyserXMLParser.parseXML( bReader );
		return xml; 
		
	}	
	
	/*
	 * (non-Javadoc)
	 * @see com.nokia.s60tools.memspy.interfaces.INewMemSpyFilesObserver#memSpyFilesUpdated()
	 */
	public void memSpyFilesUpdated() {
		Runnable refreshRunnable = new Runnable(){
			public void run(){
				refreshView();
				// Sort files so that newly imported files are in top
				((S60ToolsViewerSorter) viewer.getSorter()).setSortCriteria(MemSpyDataSorter.TIME);
			}
		};
		Display.getDefault().asyncExec(refreshRunnable);    


	}

	/**
	 * refreshView.
	 * Refreshes view asynchronously.
	 */
	private void refreshView() {
		Runnable refreshRunnable = new Runnable(){
			public void run(){
				viewer.refresh();
				enableAndDisableToolbarItems();

			}
		};
		
		// Has to be done in its own thread
		// in order not to cause invalid thread access
		Display.getDefault().asyncExec(refreshRunnable);        		
	}
	
	/**
	 * refreshContentAndView.
	 * Refreshes content and view synchronously.
	 */
	private void refreshContentAndView(){
		contentProvider.refresh();
		viewer.refresh();
		this.enableAndDisableToolbarItems();
	}
	
	/**
	 * Refreshes content and view asynchronously.
	 */
	public void refreshContentAndViewAsync() {
		Runnable refreshRunnable = new Runnable(){
			public void run(){
				refreshContentAndView();
			}
		};
		
		Display.getDefault().asyncExec(refreshRunnable);        		
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.swt.events.SelectionListener#widgetDefaultSelected(org.eclipse.swt.events.SelectionEvent)
	 */
	public void widgetDefaultSelected(SelectionEvent arg0) {
		// can be left empty.
		
	}
	public void widgetSelected(SelectionEvent arg0) {
		this.enableAndDisableToolbarItems();
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.nokia.s60tools.memspy.interfaces.IAnalyseHeapJobListener#heapAnalyserReturnValue(int)
	 */
	public void heapAnalyserFinished(final int returnValue) {
		
		if( returnValue != 0 ){
			String errorMessage = HeapAnalyserLauncher.getErrorMessage(returnValue);
			// Reporting error to console
			String consoleErrMsg = "Heap Analyser launch failure with process exit value=" + returnValue + "."
								    + " " + errorMessage;
			reportConsoleMessage(consoleErrMsg, MemSpyConsole.MSG_ERROR);
			// Reporting error to user
			showErrorMessage(ERROR_HEAP_ANALYSER, errorMessage	);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see com.nokia.s60tools.memspy.interfaces.IAnalyseHeapJobListener#heapAnalyserReturnValue(int, java.lang.String)
	 */
	public void heapAnalyserFinished(int returnValue, final String outputFilePath) {
		
		// if error occured
		if( returnValue != 0 ){
			this.heapAnalyserFinished(returnValue);
		}
		
		else{
			Runnable refreshRunnable = new Runnable(){
				public void run(){
					/*// Ask user if generated file should be opened on excel
					boolean compare = MessageDialog.openConfirm(getSite().getShell(), CONFIRMATION, CONFIRMATION_OPEN_FILE );
					*/
					boolean compare = true;
					// Display a Confirmation dialog
					MessageBox messageBox = new MessageBox(viewer.getControl().getShell(), SWT.ICON_QUESTION | SWT.YES | SWT.NO);
			        messageBox.setText( CONFIRMATION );
			        messageBox.setMessage( CONFIRMATION_OPEN_FILE );
			        int buttonID = messageBox.open();
			        if (buttonID == SWT.NO) {
			        	compare = false;
			        }
					if( compare ){
						
						// find program for xls-filetype
						Program p=Program.findProgram(".xls");
						
						// if found, launch it.
						if(p!=null){
							p.execute(outputFilePath);
						}
					}

				}
			};
			
			// Has to be done in its own thread
			// in order not to cause invalid thread access
			Display.getDefault().asyncExec(refreshRunnable);     
			
			
		}
	}	
	
	
	/**
	 * Get import engine.
	 * @return importEngine
	 */
	public ImportEngine getImportEngine() {
		return importEngine;
	}
	
	/**
	 * Shows error message asynchronously.
	 * @param header header of message
	 * @param text error text
	 */
	public void showErrorMessage(final String header, final String text){
		Runnable showErrorMessageRunnable = new Runnable(){
			public void run(){
				MessageDialog.openError( getSite().getShell(), header, text);
			}
		};
		
		// Has to be done in its own thread
		// in order not to cause invalid thread access
		Display.getDefault().asyncExec(showErrorMessageRunnable);    
	}

	/**
	 * Reports message to console asynchronously.
	 * @param consoleMsg console message.
	 * @param messageType console message type
	 */
	public void reportConsoleMessage(final String consoleMsg, final int messageType){
		Runnable reportConsoleMessageRunnable = new Runnable(){
			public void run(){
				MemSpyConsole.getInstance().println(consoleMsg, messageType);
			}
		};
		
		// Has to be done in its own thread
		// in order not to cause invalid thread access
		Display.getDefault().asyncExec(reportConsoleMessageRunnable);    
	}
	
	/**
	 * Shows trace viewer plugin
	 */
	public static void showTraceViewer() {
    	try {

    		IWorkbenchWindow ww = MemSpyPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow();
    		if (ww == null)
    			return;
    		IWorkbenchPage page = ww.getActivePage();
    		
    		// Checking if view is already open
    		IViewReference[] viewRefs = page.getViewReferences();
    		for (int i = 0; i < viewRefs.length; i++) {
				IViewReference reference = viewRefs[i];
				String id = reference.getId();
				if(id.equalsIgnoreCase(TRACE_VIEWER_VIEW_ID)){
					// Found, restoring the view
					IViewPart viewPart = reference.getView(true);
					page.activate(viewPart);
					return;
				}
			}
    		
    		// View was not found, opening it up as a new view.
    		page.showView(TRACE_VIEWER_VIEW_ID);
    		
    	} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * launchHeapAnalyser.
	 * Launches Heap Analyser.
	 * @param configurationFilePath path of the configuration file
	 * @param analyseOutputFilePath path of the output file
	 * @param threadName thread's name
	 * @param viewingHeap true, if action is view heap
	 */
	public void launchHeapAnalyser( String configurationFilePath, String analyseOutputFilePath, String threadName, boolean viewingHeap ){
		
		AnalyseHeapJob analyseHeapJob;

		// set heap job's name correct
		if( viewingHeap ){
			analyseHeapJob = new AnalyseHeapJob( "Heap Analyser Running, Viewing heap from thread: " + threadName, this );	
		}
		else{
			analyseHeapJob = new AnalyseHeapJob( "Heap Analyser Running, Comparing Heaps from thread: " + threadName , this );
		}
		
		// set output file path
		analyseHeapJob.setAnalyseOutputFile( analyseOutputFilePath );
		
		// launch Heap Analyser
		analyseHeapJob.setConfigurationFile( configurationFilePath );
		analyseHeapJob.refresh();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.swt.events.KeyListener#keyPressed(org.eclipse.swt.events.KeyEvent)
	 */
	public void keyPressed(KeyEvent keyEvent) {
		
		// if delete key is pressed run delete -action.
		if( keyEvent.keyCode == java.awt.event.KeyEvent.VK_DELETE){
			deleteAction.run();
		}
	}
	/* (non-Javadoc)
	 * @see org.eclipse.swt.events.KeyListener#keyReleased(org.eclipse.swt.events.KeyEvent)
	 */
	public void keyReleased(KeyEvent arg0) {
		// can be left empty.
	}
	/* (non-Javadoc)
	 * @see com.nokia.s60tools.memspy.interfaces.INewMemSpyFilesObserver#setOutputFilePath(java.lang.String)
	 */
	public void setOutputFilePath(String outputFilePath) {
		// can be left empty.		
	}

	/**
	 * Launch the SWMT analyser with given files
	 * @param files
	 */
	public void launchSWMTAnalyser( final ArrayList<String> files ){
		Runnable showErrorMessageRunnable = new Runnable(){
			public void run(){
				// launch swmt analyser for selected logs.
				SwmtAnalyser analyser = new SwmtAnalyser(MemSpyConsole.getInstance());
				analyser.analyse( files );
			}
		};
		
		// Has to be done in its own thread
		// in order not to cause invalid thread access
		Display.getDefault().asyncExec(showErrorMessageRunnable);  
		
	}
	
	/**
	 * Callback called when symbol filea are updated by user
	 * @param symbols
	 */
	public void symbolsUpdated( final AnalyserXMLGenerator symbols ){
		Runnable editSymbolDefinitions = new Runnable(){
			@SuppressWarnings("unchecked")
			public void run(){
				// get selected file bundle
				ISelection selection = viewer.getSelection();
				Object obj = ((IStructuredSelection)selection).toList();
				List<MemSpyFileBundle> selectionList = (List<MemSpyFileBundle>)obj;
				//verify that only one file is selected.
				if( selectionList.size() == 1 ){
					if( !importEngine.generateViewConfigurationFile(selectionList.get(0).getFilePath(), symbols.getXMLThreadName(), symbols) ){
						showErrorMessage(ERROR_TOPIC, "File operations were failed when trying to save new symbol definitions. Definitions may not be saved.");
					}
				}
			}
		};
		Display.getDefault().asyncExec(editSymbolDefinitions);  

	}
	
}