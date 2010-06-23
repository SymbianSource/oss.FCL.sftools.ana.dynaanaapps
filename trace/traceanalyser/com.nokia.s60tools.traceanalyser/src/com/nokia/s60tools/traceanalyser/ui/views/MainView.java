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



package com.nokia.s60tools.traceanalyser.ui.views;


import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Timer;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.part.*;
import org.eclipse.jface.viewers.*;
import org.eclipse.jface.action.*;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.*;
import org.eclipse.swt.widgets.Menu;

import com.nokia.s60tools.traceanalyser.containers.RuleInformation;
import com.nokia.s60tools.traceanalyser.export.ITraceAnalyserRuleType;
import com.nokia.s60tools.traceanalyser.export.RuleEvent;
import com.nokia.s60tools.traceanalyser.export.TraceAnalyserRule;
import com.nokia.s60tools.traceanalyser.interfaces.ITraceAnalyserFileObserver;
import com.nokia.s60tools.traceanalyser.model.Engine;
import com.nokia.s60tools.traceanalyser.plugin.TraceAnalyserPlugin;
import com.nokia.s60tools.traceanalyser.resources.ImageKeys;
import com.nokia.s60tools.traceanalyser.resources.ImageResourceManager;
import com.nokia.s60tools.traceanalyser.ui.dialogs.EditRuleDialog;
import com.nokia.s60tools.traceanalyser.ui.editors.HistoryEditor;
import com.nokia.s60tools.traceanalyser.ui.editors.HistoryEditorInput;
import com.nokia.s60tools.ui.S60ToolsTable;
import com.nokia.s60tools.ui.S60ToolsTableColumnData;
import com.nokia.s60tools.ui.S60ToolsTableFactory;
import com.nokia.traceviewer.api.TraceViewerAPI;
import com.nokia.traceviewer.api.TraceViewerAPI.TVAPIError;


/**
 * <p>
 * MainView.
 * The main view of Trace Analyser Carbide extension. 
 * <p>
 */

public class MainView extends ViewPart implements ITraceAnalyserFileObserver, SelectionListener, ActionListener, KeyListener {

	
	/* TraceViewers ID */
	public static final String TRACE_VIEWER_VIEW_ID = "com.nokia.traceviewer.view.TraceViewerView";
	
	/* Main view's ID */
	public static final String ID = "com.nokia.s60tools.traceanalyser.ui.views.MainView";
	private static final int TAB_INDEX_RULE_LIST = 0;
	private static final int TAB_INDEX_FAIL_LOG =1;
	
	/* Table Viewers for both main view's tables. */
	private CheckboxTableViewer viewerRuleTable;
	private TableViewer viewerFailLogTable;

	/* content providers for both main view's tables */
	private RuleTableContentProvider contentProviderRuleTable;
	private FailLogTableContentProvider contentProviderFailLogTable;
	
	/* actions */
	
	private Action doubleClickAction;
	private Action actionCopyRule;
	
	/* rule view actions */
	private Action actionDeleteRule;
	private Action actionClearOneRuleCounters;
	private Action actionClearAllCounters;
	private Action actionCreateNewRule;
	private Action actionEditRule;
	private Action actionShowHistory;

	/* fail log actions */
	private Action actionClearFailLog;
	private Action actionShowInTraceViewer;
	
	/* list of rule types */
	ArrayList<ITraceAnalyserRuleType> ruleTypes;

	/* composites for both tabs */
	private Composite compositeRules;
	private Composite compositeFailLog;

	
	/* Tab Folder for main view */
	TabFolder tabFolder;
	
	/* Trace Analyser engine */
	Engine engine;
	
	/* Timer that is used when blinking icon */
	Timer timer;
	
	/* count of how many times icon is blinked */
	int blinkCount;
	
	/* Arraylist containing all open editor views */
	ArrayList<HistoryEditor> openEditors;
	
	
	/**
	 * The constructor.
	 */
	public MainView() {
		ruleTypes = new ArrayList<ITraceAnalyserRuleType>();
		engine = TraceAnalyserPlugin.getEngine();
		TraceAnalyserPlugin.setMainView(this);
		ruleTypes = engine.getRuleTypes();
		blinkCount = 0;
		openEditors = new ArrayList<HistoryEditor>();
	}

	/**
	 * showAndReturnYourself.
     * Makes main view visible and returns an instance of itself. Can be called from
     * an UI thread only.
     * @return instance of main view
     */
    public static MainView showAndReturnYourself() {
    	return showAndReturnYourself(false);
    }
	/**
     * showAndReturnYourself.
     * Makes main view visible and returns an instance of itself. Can be called from
     * an UI thread only.
     * @return instance of main view
     */
	public static MainView showAndReturnYourself(boolean openOnly) {
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
				if(id.equalsIgnoreCase(MainView.ID)){
					// Found, restoring the view
					IViewPart viewPart = reference.getView(true);
					if (!openOnly)
						page.activate(viewPart);
					return (MainView)viewPart;
				}
			}
    		
    		// View was not found, opening it up as a new view.
    		MainView mView = (MainView)page.showView(MainView.ID);
    		if (openOnly)
    			page.bringToTop(currentPart);
    		return mView;
    		
    	} catch (Exception e) {
			e.printStackTrace();
			
			return null;
		}
	}
	
	
	/**
	 * This is a callback that will allow us
	 * to create the viewer and initialize it.
	 */
	public void createPartControl(Composite parent) {
		
		createTabControls(parent);
		createFailLogTable();
		createRuleTable();


		// Create the help context id for the viewer's control
		PlatformUI.getWorkbench().getHelpSystem().setHelp(viewerRuleTable.getControl(), "com.nokia.s60tools.traceanalyser.viewer");
		makeActions();
		hookContextMenu();
		hookDoubleClickAction();
		contributeToActionBars();
		
		refreshRuleContentAndViewAsynch();
		refreshFailLogContentAndViewAsynch();
		readActivationStatus();
		setHelps();
	}

	/**
	 * Creates Tab controls
	 * @param parent composite where tab controls are placed.
	 */
	private void createTabControls(Composite parent){
		// create tab folder.
		tabFolder = new TabFolder (parent, SWT.DOWN);
		tabFolder.addSelectionListener(this);

		// create rule composite
		compositeRules = new Composite(tabFolder, SWT.NONE);
 		GridLayout layoutCompositeRules = new GridLayout();
 		layoutCompositeRules.numColumns = 1;
 		compositeRules.setLayout(layoutCompositeRules);

		// create fail log composite
		compositeFailLog = new Composite(tabFolder, SWT.NONE);
 		GridLayout layoutCompositeFailLog = new GridLayout();
 		layoutCompositeFailLog.numColumns = 1;
 		compositeFailLog.setLayout(layoutCompositeFailLog);
		
 		// create tab items
 		TabItem item = new TabItem (tabFolder, SWT.NONE);
		item.setText ("Trace Analyser rules");
		item.setControl(compositeRules);
		tabFolder.pack();
 		
		TabItem item2 = new TabItem (tabFolder, SWT.NONE);
		item2.setText ("Fail Log");
		item2.setControl(compositeFailLog);
		tabFolder.pack();
	}
	
	/**
	 * Creates fail log table
	 */
	private void createFailLogTable(){
		// Create fail log table
		ArrayList<S60ToolsTableColumnData> failLogColumnArrayList = new ArrayList<S60ToolsTableColumnData>();
		
		failLogColumnArrayList.add(new S60ToolsTableColumnData("Time", 150, 0, FailLogTableDataSorter.TIME)); 
		failLogColumnArrayList.add(new S60ToolsTableColumnData("Rule", 175, 1, FailLogTableDataSorter.RULE));
		failLogColumnArrayList.add(new S60ToolsTableColumnData("Violation", 75, 2, FailLogTableDataSorter.VIOLATION));
		failLogColumnArrayList.add(new S60ToolsTableColumnData("Limit", 75, 3, FailLogTableDataSorter.LIMIT));
		 
		S60ToolsTableColumnData[] columnDataTableFailLog = failLogColumnArrayList.toArray(new S60ToolsTableColumnData[0]);
		S60ToolsTable tableFailLog = S60ToolsTableFactory.create(compositeFailLog, columnDataTableFailLog);
		
		viewerFailLogTable = new TableViewer(tableFailLog.getTableInstance());
		contentProviderFailLogTable = new FailLogTableContentProvider(engine);
		viewerFailLogTable.setContentProvider(contentProviderFailLogTable);
		viewerFailLogTable.setLabelProvider(new FailLogTableLabelProvider());
		viewerFailLogTable.setComparator(new FailLogTableDataSorter());
		viewerFailLogTable.setSorter(new FailLogTableDataSorter());
		viewerFailLogTable.setInput(getViewSite());
		tableFailLog.setHostingViewer(viewerFailLogTable);
	}
	

	/**
	 * Creates rule table
	 */
	private void createRuleTable(){
		Table tableRules =  new Table(compositeRules, SWT.CHECK | SWT.MULTI | SWT.FULL_SELECTION | SWT.BORDER);
		
		tableRules.setLayoutData(new GridData(GridData.FILL_BOTH));
		tableRules.setLinesVisible(true);
		tableRules.setHeaderVisible(true);

		// Add selection listeners for each column after creating it
		tableRules.addSelectionListener(this);
		
		int parameter_column_length = 60;
		
		TableColumn tableColumn = new TableColumn(tableRules, SWT.NONE);
		tableColumn.setWidth(20);
		tableColumn.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				checkAllPressed();
			}
	    });
		
		TableColumn tableColumn2 = new TableColumn(tableRules, SWT.NONE);
		tableColumn2.setWidth(150);
		tableColumn2.setText("Rule Name");

		tableColumn2.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				viewerRuleTable.setSorter(new RuleTableDataSorter(
						RuleTableDataSorter.RULE_NAME));
			}
		});
		
		TableColumn tableColumn3 = new TableColumn(tableRules, SWT.NONE);
		tableColumn3.setWidth(parameter_column_length);
		tableColumn3.setText("Pass");

		tableColumn3.addSelectionListener(new SelectionAdapter() {
	          public void widgetSelected(SelectionEvent e) {
	              	viewerRuleTable.setSorter(
	                   new RuleTableDataSorter(RuleTableDataSorter.PASS));
	               }
	          });

		
		TableColumn tableColumn4 = new TableColumn(tableRules, SWT.NONE);
		tableColumn4.setWidth(parameter_column_length);
		tableColumn4.setText("Fail");
		tableColumn4.addSelectionListener(new SelectionAdapter() {
	          public void widgetSelected(SelectionEvent e) {
	              	viewerRuleTable.setSorter(
	                   new RuleTableDataSorter(RuleTableDataSorter.FAIL));
	               }
	          });
		
		TableColumn tableColumn5 = new TableColumn(tableRules, SWT.NONE);
		tableColumn5.setWidth(parameter_column_length);
		tableColumn5.setText("Pass %");
		tableColumn5.addSelectionListener(new SelectionAdapter() {
	          public void widgetSelected(SelectionEvent e) {
	              	viewerRuleTable.setSorter(
	                   new RuleTableDataSorter(RuleTableDataSorter.PASSPERCENT));
	               }
	          });
		
		TableColumn tableColumn6 = new TableColumn(tableRules, SWT.NONE);
		tableColumn6.setWidth(parameter_column_length);
		tableColumn6.setText("Min");
		tableColumn6.addSelectionListener(new SelectionAdapter() {
	          public void widgetSelected(SelectionEvent e) {
	              	viewerRuleTable.setSorter(
	                   new RuleTableDataSorter(RuleTableDataSorter.MIN));
	               }
	          });
		
		TableColumn tableColumn7 = new TableColumn(tableRules, SWT.NONE);
		tableColumn7.setWidth(parameter_column_length);
		tableColumn7.setText("Max");
		tableColumn7.addSelectionListener(new SelectionAdapter() {
	          public void widgetSelected(SelectionEvent e) {
	              	viewerRuleTable.setSorter(
	                   new RuleTableDataSorter(RuleTableDataSorter.MAX));
	               }
	          });
		
		TableColumn tableColumn8 = new TableColumn(tableRules, SWT.NONE);
		tableColumn8.setWidth(parameter_column_length);
		tableColumn8.setText("Avg");
		tableColumn8.addSelectionListener(new SelectionAdapter() {
	          public void widgetSelected(SelectionEvent e) {
	              	viewerRuleTable.setSorter(
	                   new RuleTableDataSorter(RuleTableDataSorter.AVG));
	               }
	          });
		
		TableColumn tableColumn9 = new TableColumn(tableRules, SWT.NONE);
		tableColumn9.setWidth(parameter_column_length);
		tableColumn9.setText("Med");
		tableColumn9.addSelectionListener(new SelectionAdapter() {
	          public void widgetSelected(SelectionEvent e) {
	              	viewerRuleTable.setSorter(
	                   new RuleTableDataSorter(RuleTableDataSorter.MED));
	               }
	          });
		
		viewerRuleTable = new CheckboxTableViewer(tableRules);
		contentProviderRuleTable = new RuleTableContentProvider(engine);
		viewerRuleTable.setContentProvider(contentProviderRuleTable);
		viewerRuleTable.setLabelProvider(new RuleTableLabelProvider());
		/*viewerRuleTable.setComparator(new RuleTableDataSorter());
		viewerRuleTable.setSorter(new RuleTableDataSorter());*/
		viewerRuleTable.setInput(getViewSite());
		tableRules.addKeyListener(this);

	}
	
	
	/**
	 * hookContextMenu.
	 * Creates context menu for main view.
	 */
	private void hookContextMenu() {
		MenuManager menuMgrRuleTable = new MenuManager("#PopupMenu");
		menuMgrRuleTable.setRemoveAllWhenShown(true);
		menuMgrRuleTable.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager manager) {
				MainView.this.fillContextMenuRuleTable(manager);
			}
		});
		Menu menuRuleTable = menuMgrRuleTable.createContextMenu(viewerRuleTable.getControl());
		viewerRuleTable.getControl().setMenu(menuRuleTable);
		getSite().registerContextMenu(menuMgrRuleTable, viewerRuleTable);
	
	
		MenuManager menuMgrFailLog = new MenuManager("#PopupMenu");
		menuMgrFailLog.setRemoveAllWhenShown(true);
		menuMgrFailLog.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager manager) {
				MainView.this.fillContextMenuFailLog(manager);
			}
		});
		Menu menuFailLog = menuMgrFailLog.createContextMenu(viewerFailLogTable.getControl());
		viewerFailLogTable.getControl().setMenu(menuFailLog);
		getSite().registerContextMenu(menuMgrFailLog, viewerFailLogTable);
	}
	
	

	/**
	 * contributeToActionBars
	 */
	private void contributeToActionBars() {
		IActionBars bars = getViewSite().getActionBars();
		fillLocalPullDown(bars.getMenuManager());
		fillLocalToolBar(bars.getToolBarManager());
	}
	
	/**
	 * fillContextMenuRuleTable
	 * fills context menu for rule table.
	 * @param manager menumanager
	 */

	@SuppressWarnings("unchecked")
	private void fillContextMenuRuleTable(IMenuManager manager) {
		ISelection selection = viewerRuleTable.getSelection();
		Object obj = ((IStructuredSelection)selection).toList();
		List<RuleInformation> selectionList = (List<RuleInformation>)obj;
		if(selectionList.size() == 1){		
			manager.add(actionEditRule);
			manager.add(actionClearOneRuleCounters);
			manager.add(actionShowHistory);
			manager.add(actionCopyRule);
			manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
			manager.add(actionDeleteRule);
		}
		else if(selectionList.size() > 1){		
			manager.add(actionClearOneRuleCounters);
			manager.add(actionShowHistory);
			manager.add(actionDeleteRule);
			manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
		}
	}
	
	
	/**
	 * fillContextMenuFailLog.
	 * fills context menu for fail log view.
	 * @param manager menumanager
	 */

	private void fillContextMenuFailLog(IMenuManager manager) {
			manager.add(actionShowInTraceViewer);
	}
	
	/**
	 * fillLocalToolBar.
	 * fills local toolbar.
	 * @param manager menumanager
	 */
	private void fillLocalToolBar(IToolBarManager manager) {
		manager.removeAll();
		if(tabFolder.getSelectionIndex() == TAB_INDEX_RULE_LIST && actionCopyRule != null){
			manager.add(actionCreateNewRule);
			manager.add(actionClearAllCounters);
			manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
			manager.add(actionDeleteRule);
			manager.update(true);
		}
		else if (tabFolder.getSelectionIndex() == TAB_INDEX_FAIL_LOG && actionCopyRule != null){
			manager.add(actionClearFailLog);
			manager.update(true);
		}
	}
	
	/**
	 * fills local menu
	 */
	private void fillLocalPullDown(IMenuManager manager) {
		manager.removeAll();
		if(tabFolder.getSelectionIndex() == TAB_INDEX_RULE_LIST && actionCopyRule != null){
			manager.add(actionCreateNewRule);
			manager.add(actionClearAllCounters);
			manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
			manager.add(actionDeleteRule);
			manager.update(true);
		}
		else if (tabFolder.getSelectionIndex() == TAB_INDEX_FAIL_LOG && actionCopyRule != null){
			manager.add(actionClearFailLog);
			manager.update(true);
		}
	}

	/**
	 * makeActions.
	 * Creates actions for main view.
	 */
	@SuppressWarnings("unchecked")
	private void makeActions() {
		
		actionDeleteRule = new Action() {
			public void run() {
				if(tabFolder.getSelectionIndex() == TAB_INDEX_RULE_LIST){
					ISelection selection = viewerRuleTable.getSelection();
					Object obj = ((IStructuredSelection)selection).toList();
					List<RuleInformation> selectionList = (List<RuleInformation>)obj;
					
					// Confirm deletion
					if(showConfirmation("Trace Analyser - Delete Rule", "Are you sure you want to delete selected files?")){
						boolean error = false;
						
						for(RuleInformation item : selectionList){
							if(!engine.removeRule(item.getRule().getName())){
								error = true;
							}
						}
						if(error){
							showErrorMessage("Trace Analyser - Error", "An error occured when deleting rule(s).");	
						}
						refreshRuleContentAndView();
					}
				}
						
			}
		};
		actionDeleteRule.setText("Delete Selected Rule");
		actionDeleteRule.setToolTipText("Delete Selected Rule");
		actionDeleteRule.setImageDescriptor( PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(ISharedImages.IMG_TOOL_DELETE));	


		
		actionClearOneRuleCounters = new Action() {
			public void run() {
				ISelection selection = viewerRuleTable.getSelection();
				Object obj = ((IStructuredSelection)selection).toList();
				List<RuleInformation> selectionList = (List<RuleInformation>)obj;
				
				if(showConfirmation("Trace Analyser - Clear History", "Are you sure you want to clear selected rule's history information?")){
					for(RuleInformation item : selectionList){
						engine.resetOneRulesHistory(item.getRule().getName());
					}
				}

			}
		};
		actionClearOneRuleCounters.setText("Clear Selected Rule's Counters");
		actionClearOneRuleCounters.setToolTipText("Clear Selected Rule's Counters");


		
		actionClearAllCounters = new Action() {
			public void run() {
				if(showConfirmation("Trace Analyser - Clear History", "Are you sure you want to clear all rule's history information?")){
					engine.resetHistory();
				}
			}
		};
		actionClearAllCounters.setText("Clear All Rule's counters");
		actionClearAllCounters.setToolTipText("Clear All Rule's counters");
		actionClearAllCounters.setImageDescriptor(ImageResourceManager.getImageDescriptor(ImageKeys.IMG_CLEAR_ALL));

		actionCreateNewRule = new Action() {
			public void run() {		
				// create dialog
				EditRuleDialog dialog = new EditRuleDialog(getSite().getShell(), ruleTypes, null, engine, true);

				// open query dialog
				int retVal = dialog.open();

				// if ok pressed
				if( retVal == 0 ){
					refreshRuleContentAndViewAsynch();
				}
		
			}
		};
		actionCreateNewRule.setText("Create New Rule");
		actionCreateNewRule.setToolTipText("Create New Rule");
		actionCreateNewRule.setImageDescriptor(ImageResourceManager.getImageDescriptor(ImageKeys.IMG_CREATE_NEW));

		
		doubleClickAction = new Action() {
			public void run() {
				if(tabFolder.getSelectionIndex() == TAB_INDEX_RULE_LIST){
					actionEditRule.run();
				}
				else if(tabFolder.getSelectionIndex() == TAB_INDEX_FAIL_LOG){
					actionShowInTraceViewer.run();
					
				}
			}
		};
		
		actionEditRule = new Action() {
			public void run() {		
				ISelection selection = viewerRuleTable.getSelection();
				Object obj = ((IStructuredSelection)selection).toList();
				List<RuleInformation> selectionList = (List<RuleInformation>)obj;
				//verify that only one file is selected.
				if( selectionList.size() == 1 ){
					
					TraceAnalyserRule rule = selectionList.get(0).getRule();
					String ruleName = rule.getName();
					EditRuleDialog dialog = new EditRuleDialog(getSite().getShell(), ruleTypes, rule, engine, false);
					int retVal = dialog.open();
					// open query dialog
					if( retVal == 0 ){
						for(HistoryEditor item : openEditors){
							if(item.getRuleName().equals(ruleName)){
								IWorkbenchPage activePage = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
								activePage.closeAllEditors(false);

							}
						}
						refreshRuleContentAndViewAsynch();
					}
					// Cancel pressed
					else if( retVal == 1 ){
						
					}			
				}
						
			}
		};
		actionEditRule.setText("Edit Selected Rule");
		actionEditRule.setToolTipText("Edit Selected Rule");
		
		actionClearFailLog = new Action() {
			public void run() {	
				if(showConfirmation("Trace Analyser - Clear Fail Log", "Are your sure you want to clear Fail Log")){
					engine.clearFailLog();
				}
			}
		};
		actionClearFailLog.setText("Clear Fail Log");
		actionClearFailLog.setToolTipText("Clear Fail Log");
		actionClearFailLog.setImageDescriptor(ImageResourceManager.getImageDescriptor(ImageKeys.IMG_CLEAR_FAIL_LOG));


		
		actionShowInTraceViewer = new Action() {
			public void run() {	
				ISelection selection = viewerFailLogTable.getSelection();
				Object obj = ((IStructuredSelection)selection).toList();
				List<RuleEvent> selectionList = (List<RuleEvent>)obj;
				//verify that only one file is selected.
				if( selectionList.size() == 1 ){
					//sync to trace
					
					TVAPIError error;
					if(selectionList.get(0).getTraceNumbers().length == 1){
						error = TraceViewerAPI.syncToTrace(selectionList.get(0).getTraceNumbers()[0], 0);

					}
					else{
						error = TraceViewerAPI.syncToTrace(selectionList.get(0).getTraceNumbers()[0], selectionList.get(0).getTraceNumbers()[1]);
					}
					
					
					if(error == TVAPIError.NONE){
						showTraceViewer();
					}
					else{
						showErrorMessage("Trace Analyser - Error", 
						"Unable to locate events from TraceViewer's log. Perhaps the log has been cleared.");						
					}

				}
			}
		};
		actionShowInTraceViewer.setText("Show in TraceViewer");
		actionShowInTraceViewer.setToolTipText("Show in TraceViewer");

		actionShowHistory = new Action() {
			public void run() {	
				ISelection selection = viewerRuleTable.getSelection();
				Object obj = ((IStructuredSelection)selection).toList();
				List<RuleInformation> selectionList = (List<RuleInformation>)obj;
				//verify that only one file is selected.
				if( selectionList.size() == 1 ){
					HistoryEditorInput input = new HistoryEditorInput(selectionList.get(0));
					IWorkbenchPage activePage = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
					try {
						openEditors.add((HistoryEditor)activePage.openEditor(input,"com.nokia.s60tools.traceanalyser.ui.editors.HistoryEditor"));
					} catch (PartInitException e) {
						e.printStackTrace();
					}
				}
			}
		};
		actionShowHistory.setText("Show History");
		actionShowHistory.setToolTipText("Show History");
		
		
		actionCopyRule = new Action() {
			public void run() {	
				ISelection selection = viewerRuleTable.getSelection();
				Object obj = ((IStructuredSelection)selection).toList();
				List<RuleInformation> selectionList = (List<RuleInformation>)obj;
				//verify that only one file is selected.
				if( selectionList.size() == 1 ){

					TraceAnalyserRule rule = selectionList.get(0).getRule();
					
					EditRuleDialog dialog = new EditRuleDialog(getSite().getShell(), ruleTypes, rule, engine, true);
					int retVal = dialog.open();
					// open query dialog
					if( retVal == 0 ){
						
						refreshRuleContentAndViewAsynch();
					}
					// Cancel pressed
					else if( retVal == 1 ){
						
					}			

					
					
				}
			}
		};
		actionCopyRule.setText("Copy Selected Rule");
		actionCopyRule.setToolTipText("Copy Selected Rule");
	}

	/**
	 * hookDoubleClickAction.
	 */
	private void hookDoubleClickAction() {
		IDoubleClickListener listener = new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent event) {
				doubleClickAction.run();
			}
		};
		
		viewerRuleTable.addDoubleClickListener(listener);
		viewerFailLogTable.addDoubleClickListener(listener);
	}
	


	/**
	 * Passing the focus request to the viewer's control.
	 */
	public void setFocus() {
		viewerRuleTable.getControl().setFocus();
	}
	


	
	/*
	 * (non-Javadoc)
	 * @see com.nokia.s60tools.traceanalyser.interfaces.ITraceAnalyserFileObserver#rulesUpdated()
	 */
	public void rulesUpdated() {
		Runnable refreshRunnable = new Runnable(){
			public void run(){

				viewerRuleTable.refresh();
				readActivationStatus();
				for(HistoryEditor item : openEditors){
					item.historyUpdated();
					
				}
			
			}
		};
		
		Display.getDefault().asyncExec(refreshRunnable);       
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.nokia.s60tools.traceanalyser.interfaces.ITraceAnalyserFileObserver#ruleUpdated(java.lang.String)
	 */
	public void ruleUpdated(final String ruleName){
		
		Runnable refreshRunnable = new Runnable(){
			public void run(){

				viewerRuleTable.refresh();
				readActivationStatus();
				
				for(HistoryEditor item : openEditors){
					item.historyUpdated();

				}
			
			}
		};
		
		Display.getDefault().asyncExec(refreshRunnable);    
		
	}
	
	/**
	 * refreshRuleContentAndViewAsynch.
	 * Refreshes content and view asynchronously.
	 */
	private void refreshRuleContentAndViewAsynch(){
		Runnable refreshRunnable = new Runnable(){
			public void run(){
				refreshRuleContentAndView();
			}
		};
		
		Display.getDefault().asyncExec(refreshRunnable);       
		
	}
	/**
	 * refreshRuleContentAndView.
	 * Refreshes content and view asynchronously.
	 */
	private void refreshRuleContentAndView(){
		contentProviderRuleTable.refresh();
		viewerRuleTable.refresh();
		readActivationStatus();
		
		
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.swt.events.SelectionListener#widgetDefaultSelected(org.eclipse.swt.events.SelectionEvent)
	 */
	public void widgetDefaultSelected(SelectionEvent arg0) {
		// nothing to be done.
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.swt.events.SelectionListener#widgetSelected(org.eclipse.swt.events.SelectionEvent)
	 */
	public void widgetSelected(SelectionEvent event) {
		if(event.widget == tabFolder){
			contributeToActionBars();
		}
		if(event.detail == SWT.CHECK){
			RuleInformation info = (RuleInformation)event.item.getData();
			boolean value = viewerRuleTable.getChecked(event.item.getData());
			engine.changeRuleaActivation(info.getRule().getName(), value);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see com.nokia.s60tools.traceanalyser.interfaces.ITraceAnalyserFileObserver#failLogUpdated()
	 */
	public void failLogUpdated() {
		Runnable refreshRunnable = new Runnable(){
			public void run(){

			viewerFailLogTable.refresh();
			}
		};
		
		Display.getDefault().asyncExec(refreshRunnable);  
		
	}
	
	/**
	 * refreshFailLogContentAndViewAsynch.
	 * Refreshes content and view asynchronously.
	 */
	private void refreshFailLogContentAndViewAsynch(){
		Runnable refreshRunnable = new Runnable(){
			public void run(){
				refreshFailLogContentAndView();
			}
		};
		
		Display.getDefault().asyncExec(refreshRunnable);       
		
	}
	/**
	 * refreshFailLogContentAndView.
	 * Refreshes content and view asynchronously.
	 */
	private void refreshFailLogContentAndView(){
		contentProviderFailLogTable.refresh();
		viewerFailLogTable.refresh();
	}

	public void dispose(){
		TraceAnalyserPlugin.setMainView(null);
	}

	
	/**
	 * showTraceViewer
	 * shows trace viewer plugin
	 */
	public static void showTraceViewer() {
    	try {

    		IWorkbenchWindow ww = TraceAnalyserPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow();
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
	 * blinks icon when fail received.
	 */
	public void blinkIcon(){
		Runnable refreshRunnable = new Runnable(){
			public void run(){
				setTitleImage(ImageResourceManager.getImage(ImageKeys.IMG_FAIL_RECEIVED));
			}
		};
		
		
		if(timer == null || !timer.isRunning()){
			timer = new Timer(500, this);
			timer.start();
			Display.getDefault().asyncExec(refreshRunnable);

		}
		else{
			
		}
	
	}

	/*
	 * (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent arg0) {
		Runnable refreshRunnable = new Runnable(){
			public void run(){

				if(blinkCount % 2 == 1){
					setTitleImage(ImageResourceManager.getImage(ImageKeys.IMG_FAIL_RECEIVED));
				}
				else{
					setTitleImage(ImageResourceManager.getImage(ImageKeys.IMG_APP_ICON));
				}
				blinkCount++;
				
				if( blinkCount == 3){
					blinkCount = 0;
					timer.stop();
				}
			}
		};
		Display.getDefault().asyncExec(refreshRunnable);
		
	}

	/**
	 * Reads activation status and sets table's check/uncheck status correct
	 */
	private void readActivationStatus(){
		int i = 0;
		while(true){
			Object information = viewerRuleTable.getElementAt(i);
			if(information != null){
				if( ((RuleInformation)information).getRule().isActivated()){
					viewerRuleTable.setChecked(information, true);
				}
			}
			else{
				break;
			}
			i++;
			
		}
	}
	
	
	
	/**
	 * showErrorMessage.
	 * shows error message asynchronously
	 * @param header header of message
	 * @param text error text
	 */
	public void showErrorMessage(String header, String text){
		MessageDialog.openError( getSite().getShell(), header, text);
	}
	
	
	
	/**
	 * showConfirmation.
	 * Displays a Confirmation dialog
	 * @param header text for window header
	 * @param text text for window text
	 * @return true, if yes was pressed.
	 */
	public boolean showConfirmation(String header, String text){
		MessageBox messageBox = new MessageBox(this.getSite().getShell(), SWT.ICON_QUESTION | SWT.YES | SWT.NO);
	    messageBox.setText(header);
	    messageBox.setMessage(text);
	    int retval = messageBox.open();
	    if(retval == SWT.YES){
	    	return true;
	    }
	    else{
	    	return false;
	    }

	}
	
	
	/**
	 * editorClosed
	 * removes editor pointer from openEditors-array
	 * @param ruleName name of the rule.
	 */
	public void editorClosed(String ruleName){
		HistoryEditor closeEditor = null;
		for(HistoryEditor item : openEditors){
			if(item.getRuleName().equals(ruleName)){
				closeEditor = item;
			}
		}
		openEditors.remove(closeEditor);
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.swt.events.KeyListener#keyPressed(org.eclipse.swt.events.KeyEvent)
	 */
	public void keyPressed(KeyEvent keyEvent) {
		
		// if delete key is pressed run delete -action.
		if( keyEvent.keyCode == java.awt.event.KeyEvent.VK_DELETE){
			actionDeleteRule.run();
		}
	}
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.swt.events.KeyListener#keyReleased(org.eclipse.swt.events.KeyEvent)
	 */
	public void keyReleased(KeyEvent arg0) {
		// can be left empty.
	}
	
	/**
	 * Activate/deactivate all rules in main view
	 */
	private void checkAllPressed(){
		boolean activationStatus = false;
		
		// compare amount of checked and unchecked rules 
		if( viewerRuleTable.getCheckedElements().length <= (viewerRuleTable.getTable().getItemCount() / 2)){
			activationStatus = true;
		}
		int i = 0;
		while(true){
			RuleInformation information = (RuleInformation)viewerRuleTable.getElementAt(i);
			if(information != null){
				viewerRuleTable.setChecked(information, activationStatus);
				engine.changeRuleaActivation(information.getRule().getName(), activationStatus);
			}
			else{
				break;
			}
			i++;
			
		}
	}
	
	/**
	 * Sets this page's context sensitive helps
	 */
	protected void setHelps() {
		PlatformUI.getWorkbench().getHelpSystem().setHelp( viewerFailLogTable.getControl(), com.nokia.s60tools.traceanalyser.resources.HelpContextIDs.TRACE_ANALYSER_FAIL_LOG);
		PlatformUI.getWorkbench().getHelpSystem().setHelp( tabFolder, com.nokia.s60tools.traceanalyser.resources.HelpContextIDs.TRACE_ANALYSER_MAIN_VIEW);
		PlatformUI.getWorkbench().getHelpSystem().setHelp( viewerRuleTable.getControl(), com.nokia.s60tools.traceanalyser.resources.HelpContextIDs.TRACE_ANALYSER_MAIN_VIEW);
		PlatformUI.getWorkbench().getHelpSystem().setHelp( getSite().getShell(), com.nokia.s60tools.traceanalyser.resources.HelpContextIDs.TRACE_ANALYSER_MAIN_VIEW);
		
	}
	

}