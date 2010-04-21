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
package com.nokia.s60tools.swmtanalyser.editors;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.Random;
import java.util.Map.Entry;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageLoader;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Sash;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.events.ExpansionAdapter;
import org.eclipse.ui.forms.events.ExpansionEvent;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.forms.widgets.TableWrapData;
import org.eclipse.ui.forms.widgets.TableWrapLayout;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.part.MultiPageEditorPart;

import com.nokia.s60tools.swmtanalyser.SwmtAnalyserPlugin;
import com.nokia.s60tools.swmtanalyser.analysers.AnalyserConstants;
import com.nokia.s60tools.swmtanalyser.analysers.IAnalyser;
import com.nokia.s60tools.swmtanalyser.analysers.ResultElements;
import com.nokia.s60tools.swmtanalyser.analysers.ResultsParentNodes;
import com.nokia.s60tools.swmtanalyser.data.CycleData;
import com.nokia.s60tools.swmtanalyser.data.OverviewData;
import com.nokia.s60tools.swmtanalyser.data.ParsedData;
import com.nokia.s60tools.swmtanalyser.dialogs.AdvancedFilterDialog;
import com.nokia.s60tools.swmtanalyser.dialogs.AdvancedFilterDialog.FilterInput;
import com.nokia.s60tools.swmtanalyser.model.ExcelCreator;
import com.nokia.s60tools.swmtanalyser.model.SWMTLogReaderUtils;
import com.nokia.s60tools.swmtanalyser.ui.graphs.ChunksGraph;
import com.nokia.s60tools.swmtanalyser.ui.graphs.DisksGraph;
import com.nokia.s60tools.swmtanalyser.ui.graphs.GenericGraph;
import com.nokia.s60tools.swmtanalyser.ui.graphs.GraphForAllEvents;
import com.nokia.s60tools.swmtanalyser.ui.graphs.GraphsUtils;
import com.nokia.s60tools.swmtanalyser.ui.graphs.IGraphTypeSelectionListener;
import com.nokia.s60tools.swmtanalyser.ui.graphs.LinearIssuesGraph;
import com.nokia.s60tools.swmtanalyser.ui.graphs.SwmtGraph;
import com.nokia.s60tools.swmtanalyser.ui.graphs.SystemDataGraph;
import com.nokia.s60tools.swmtanalyser.ui.graphs.ThreadsGraph;
import com.nokia.s60tools.swmtanalyser.wizards.ReportGenerationWizard;

/**
 * SWMT Analyser editor view
 *
 */
public class SWMTEditor extends MultiPageEditorPart implements SelectionListener, IGraphTypeSelectionListener{  

	// Overview Page Controls
	private ScrolledForm form;
	private Combo toCombo;
	private Button allBtn;
	private Button notAllBtn;
	private Button export;

	//Trace Page Controls
	private TabItem eventsTab;
	private Combo eventsCombo;
	private List eventList;
	private TabFolder innerTabFolder;
	private TabItem threadTab;
	private TabItem chunksTab;
	private TabItem diskTab;
	private TabItem sysInfoTab;
	private FilterTextTable threadTble;
	private FilterTextTable chunkTble;
	private FilterTextTable diskTble;
	private FilterTextTable sysinfoTble;

	private static enum CATEGORIES { All_events, Chunks, Disk, Files, Heap, HPAS, RAM, System_info };
	private String[] CHUNKS_GRP = { "Global data size", "Non heap chunk size" };  
	private String[] DISK_GRP = { "Disk used", "Disk total" };  
	private String[] FILES_GRP = { "No of Files" }; 
	private String[] HEAP_GRP = { "Max size", "Heap size", "Heap allocated space", "Heap free space", "Heap allocated cell count", "Heap free cell count", "Free slack" };   //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$
	private String[] HPAS_GRP = { "No of PS Handles" }; 
	private String[] RAM_GRP = { "RAM used", "RAM total" };  
	private String[] SYSINFO_GRP = { "System Data" }; 
	private String[] ALL_GRP = { "RAM used", "RAM total", "Global data size", "Non heap chunk size", "Disk used", "Disk total", "No of Files", "Max size", "Heap size", "Heap allocated space", "Heap free space", "Heap allocated cell count", "Heap free cell count", "Free slack", "No of PS Handles", "System Data" };   //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$ //$NON-NLS-8$ //$NON-NLS-9$ //$NON-NLS-10$ //$NON-NLS-11$ //$NON-NLS-12$ //$NON-NLS-13$ //$NON-NLS-14$ //$NON-NLS-15$ //$NON-NLS-16$
	
	//Stores overiew information to be displayed in the first page
	private OverviewData ov;

	// All cycles data
	private ParsedData parsedData = new ParsedData();
	private ArrayList<String> threads = new ArrayList<String>();
	private ArrayList<String> chunks = new ArrayList<String>();
	private ArrayList<String> nonHeapChunks = new ArrayList<String>();
	private ArrayList<String> disks = new ArrayList<String>();
	
	//Four table viewer for 4 tabs in the Graphs tab
	private CheckboxTableViewer threadViewer;
	private CheckboxTableViewer chunksViewer; 
	private CheckboxTableViewer disksViewer; 
	private CheckboxTableViewer sysElemsViewer; 
	
	private String lastSelectedEvent = RAM_GRP[0];

	private SwmtGraph graph;
	private GraphForAllEvents allEventsGraph;
	
	private String selectedEvent;
	private CheckboxTableViewer graphedItemsViewer;
	private ArrayList<GraphedItemsInput> graphed = new ArrayList<GraphedItemsInput>();
	private TabFolder mainTabFolder;
	private Label title;

	private Table issues_table;
	private Button viewAll_btn;;

	private int ANALYSIS_PAGE = -1;
	private int OVERVIEW_PAGE = -1;
	private int GRAPHS_PAGE = -1;
	private Tree issues_tree;
	private MenuItem analyse_menuItem;
	private Combo severity_combo;
	private Label severity_label;
	private IssuesFilter filter;
	private IssuesViewer viewer;
	private ArrayList<Object> analysis_results_obj = new ArrayList<Object>();
	
	private static final String NO_ISSUES_MSG = "No Critical issues found."; 
	private static final String GRAPHED_ITEMS_LABEL = "Graphed items"; 
	private Button advanced_filter_button;
	private Button generate_report_btn;
	private SashForm graphSash;
	
	/**
	 * Creates a multi-page editor.
	 */
	public SWMTEditor() {
		super();
	}
	
	/**
	 * Creates overview page of the SWMT Editor.
	 */
	private void createOverviewPage() {
		Composite composite = new Composite(getContainer(), SWT.NONE);
		FillLayout layoutF = new FillLayout();
		composite.setLayout(layoutF);
		FormToolkit toolkit = new FormToolkit(composite.getDisplay());
		form = toolkit.createScrolledForm(composite);
		form.setText("Overview:"); 
	
		TableWrapLayout layout = new TableWrapLayout();
		layout.leftMargin = 10;
		layout.rightMargin = 10;
		layout.numColumns = 2;
		form.getBody().setLayout(layout);
		form.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		if(parsedData.getNumberOfCycles() == 1 && parsedData.getLogData()[0].getCycleNumber() != 1)
			form.setMessage("This is a delta log file. It may not contain complete information. \nTo get complete information, selected logs must be in consecutive order starting from cycle 1.", IMessageProvider.WARNING); 
		
		Section section = toolkit.createSection(form.getBody(), Section.DESCRIPTION|Section.TITLE_BAR|Section.TWISTIE|Section.EXPANDED);
		TableWrapData td = new TableWrapData(TableWrapData.FILL);
		td.align = TableWrapData.FILL;
		td.grabHorizontal = true;
		
		section.setLayoutData(td);
		section.addExpansionListener(new ExpansionAdapter() {
			public void expansionStateChanged(ExpansionEvent e) {
				form.reflow(true);
			}
		});
		section.setText("Properties"); 
		section.setDescription("This section describes general information about log files"); 
		Composite sectionClient = toolkit.createComposite(section);
		sectionClient.setLayout(new GridLayout());
		toolkit.createLabel(sectionClient, "Number of Cycles : "+ov.noOfcycles); 
		
		if(ov.noOfcycles > 1)
			toolkit.createLabel(sectionClient, "Time Period	: "+ov.fromTime + " to " + ov.toTime);  
		else if(ov.noOfcycles == 1){
			toolkit.createLabel(sectionClient, "Time Period	: "+ov.fromTime); 
		}
		if(ov.duration >= 60)
			toolkit.createLabel(sectionClient, "Time Duration	: "+ov.duration + " sec (" + ov.durationString +")");   //$NON-NLS-3$
		else
			toolkit.createLabel(sectionClient, "Time Duration	: "+ov.duration + " sec");  
		section.setClient(sectionClient);
		
		Section analysisSection = toolkit.createSection(form.getBody(), Section.TITLE_BAR|Section.TWISTIE|Section.EXPANDED);
		TableWrapData td3 = new TableWrapData(TableWrapData.FILL_GRAB);
		td3.rowspan = 3;
		td3.grabHorizontal = true;
		td3.grabVertical = true;
		analysisSection.setLayoutData(td3);
		analysisSection.setText("Analysis"); 
		Composite analysisComp = toolkit.createComposite(analysisSection);
		analysisComp.setLayout(new GridLayout(1, false));
		analysisComp.setLayoutData(new GridData(GridData.FILL));
		
		toolkit.createLabel(analysisComp, "Top 5 issues:"); 
		
		issues_table = toolkit.createTable(analysisComp, SWT.FULL_SELECTION|SWT.BORDER|SWT.SINGLE);
		GridData table_GD = new GridData(GridData.FILL_HORIZONTAL);
		//table_GD.heightHint = 200;
		issues_table.setLayoutData(table_GD);
		TableColumn col1 = new TableColumn(issues_table, SWT.NONE);
		col1.setWidth(250);
		col1.setText("Item"); 
		TableColumn col2 = new TableColumn(issues_table, SWT.NONE);
		col2.setWidth(200);
		col2.setText("Event"); 
		issues_table.pack();
		issues_table.setHeaderVisible(true);
		issues_table.setToolTipText("Double click to analyse..."); 
		
		issues_table.addSelectionListener(this);
		
		final Menu menuPopup = new Menu(issues_table);
		analyse_menuItem = new MenuItem(menuPopup, SWT.CASCADE);
		analyse_menuItem.setText("Analyse..."); 
		analyse_menuItem.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
								
				//Open Analysis tab
				setActivePage(ANALYSIS_PAGE);
				//If the issues viewer is in filtered state, then make it to show all the issues.
				filter.setFilterText(null);
				viewer.refresh();
				
				//Find the TreeItem which matches the selection.
				TreeItem child_toBeSelected = null;
				TreeItem parent_toBeExpanded = null;
				for(TreeItem parent:issues_tree.getItems())
				{
					parent.setExpanded(true);
					parent.notifyListeners(SWT.Expand, new Event());
					for(TreeItem child:parent.getItems())
					{
						if(child.getText(1).equals(issues_table.getSelection()[0].getText(0)) && child.getText(2).equals(issues_table.getSelection()[0].getText(1)))
						{
							parent_toBeExpanded = parent;
							child_toBeSelected = child;
							break;
						}
					}
				}

				//Select the matched Treeitem in viewer.
				if(child_toBeSelected!=null && parent_toBeExpanded !=null)
				{
					issues_tree.showItem(child_toBeSelected);
					issues_tree.select(child_toBeSelected);
					issues_tree.setFocus();
				}
			}
		});
		
		
		issues_table.setMenu(menuPopup);
		
		issues_table.addMouseListener(new MouseListener(){
			public void mouseDoubleClick(MouseEvent e) {
				if(analyse_menuItem.isEnabled())
					analyse_menuItem.notifyListeners(SWT.Selection, new Event());
			}
			public void mouseDown(MouseEvent e) {
			}
			public void mouseUp(MouseEvent e) {
			}
		});
		issues_table.pack();
		
		viewAll_btn = toolkit.createButton(analysisComp, "View all issues...", SWT.PUSH); 
		GridData viewAll_GD = new GridData(GridData.FILL);
		viewAll_GD.horizontalAlignment = GridData.END;
		viewAll_btn.setLayoutData(viewAll_GD);
		viewAll_btn.addSelectionListener(this);
		analysisSection.setClient(analysisComp);
		
		Section romDetails = toolkit.createSection(form.getBody(), Section.DESCRIPTION|Section.TITLE_BAR|Section.TWISTIE|Section.EXPANDED);
		TableWrapData tableData = new TableWrapData(TableWrapData.FILL);
		tableData.align = TableWrapData.FILL;
		tableData.grabHorizontal = true;
		
		romDetails.setLayoutData(tableData);
		romDetails.addExpansionListener(new ExpansionAdapter() {
			public void expansionStateChanged(ExpansionEvent e) {
				form.reflow(true);
			}
		});
		romDetails.setText("ROM Details"); 
		romDetails.setDescription("This section displays the ROM information from log files"); 
		Composite romSection = toolkit.createComposite(romDetails);
		romSection.setLayout(new GridLayout());
		
		CycleData firstCycle = parsedData.getLogData()[0];
		
		toolkit.createLabel(romSection, "ROM Checksum : " + firstCycle.getRomCheckSum()); 
		toolkit.createLabel(romSection, "ROM Version  : " + firstCycle.getRomVersion()); 
		romDetails.setClient(romSection);
		
		Section section2 = toolkit.createSection(form.getBody(), Section.DESCRIPTION|Section.TITLE_BAR|Section.TWISTIE|Section.EXPANDED);
		TableWrapData td2 = new TableWrapData(TableWrapData.FILL);
		section2.setLayoutData(td2);
		section2.addExpansionListener(new ExpansionAdapter() {
			public void expansionStateChanged(ExpansionEvent e) {
				form.reflow(true);
			}
		});
		section2.setText("Export Options"); 
		section2.setDescription("Specify the export options"); 
		Composite sectionClient2 = toolkit.createComposite(section2);
		sectionClient2.setLayout(new GridLayout(4, false));
		
		GridData gd1 = new GridData();
		gd1.horizontalSpan = 4;
		allBtn = toolkit.createButton(sectionClient2, "All",SWT.RADIO); 
		notAllBtn = toolkit.createButton(sectionClient2, "Selected log files",SWT.RADIO); 
		allBtn.setLayoutData(gd1);
		allBtn.setSelection(true);
		allBtn.addSelectionListener(this);
		
		notAllBtn.setLayoutData(gd1);
		notAllBtn.addSelectionListener(this);
		
		toolkit.createLabel(sectionClient2, "From"); 
		Label fromLabel = new Label(sectionClient2, SWT.NONE);
		
		toolkit.createLabel(sectionClient2, " To"); 
		toCombo = new Combo(sectionClient2, SWT.DROP_DOWN|SWT.READ_ONLY);
		toCombo.setEnabled(false);
		toCombo.addSelectionListener(this);
		
		Composite exportComp = new Composite(sectionClient2,SWT.NONE);
		exportComp.setLayout(new GridLayout());
		GridData exGD = new GridData();
		exGD.horizontalSpan = 4;
		exportComp.setLayoutData(exGD);
		export = toolkit.createButton(exportComp, "Export as XLS...", SWT.PUSH); 
		export.addSelectionListener(this);
		
		section2.setClient(sectionClient2);
	
		OVERVIEW_PAGE = addPage(composite);
		setPageText(OVERVIEW_PAGE, "Overview");	 
		
		
		CycleData [] parsed_cycles = parsedData.getLogData();
		
		if(parsedData.getNumberOfCycles() == 1)
		{
			int cycleNo = parsed_cycles[0].getCycleNumber();
			fromLabel.setText(Integer.toString(cycleNo));
			toCombo.add(Integer.toString(cycleNo));
		}
		else
		{
			fromLabel.setText("1"); 
			for (int i = 1 ; i <= parsed_cycles.length ; i++)
			{
				toCombo.add(Integer.toString(i));
			}
		}
		
		toCombo.select(parsed_cycles.length -1);

	}
	
	/**
	 * Creates Graphs page of the SWMT Editor.
	 *
	 */
	private void createGraphsPage()
	{
		Composite parent = new Composite(getContainer(), SWT.NONE);
		parent.setLayout(new FormLayout());
		
		Composite titleBar = new Composite(parent, SWT.NONE);
		Composite holder = new Composite(parent, SWT.NONE);
		
		title = new Label(titleBar, SWT.CENTER|SWT.BORDER);
		title.setBackground(parent.getDisplay().getSystemColor(SWT.COLOR_DARK_BLUE));
		title.setForeground(parent.getDisplay().getSystemColor(SWT.COLOR_WHITE));
		
		Font roman_8 = new Font(parent.getDisplay(), "Arial",  10, SWT.BOLD); 
		title.setFont(roman_8);
		title.setText(""); 
		
		FormData data = new FormData();
		data.left = new FormAttachment(0);
		data.right = new FormAttachment(100);
		title.setLayoutData(data);
		
		FormData formData = new FormData();
		formData.top    = new FormAttachment(0);
		formData.left   = new FormAttachment(0);
		formData.right  = new FormAttachment(100);
		titleBar.setLayoutData(formData);
		titleBar.setLayout(new FormLayout());
		
		// FormData for the overall holder composite
		formData = new FormData();
		formData.top    = new FormAttachment(titleBar);
		formData.bottom = new FormAttachment(100);
		formData.left   = new FormAttachment(0);
		formData.right  = new FormAttachment(100);
		holder.setLayoutData(formData);
		holder.setLayout(new FormLayout());
				
		SashForm graphSash = new SashForm(holder, SWT.VERTICAL|SWT.SMOOTH|SWT.BORDER);
		graphSash.SASH_WIDTH = 5;
		graphSash.setLayout(new FillLayout());
		
		Composite bottomComposite = new SashForm(holder, SWT.VERTICAL);
		final Sash acrossSash = new Sash(holder, SWT.HORIZONTAL);
		
		formData = new FormData();
		formData.top    = new FormAttachment(0);
		formData.bottom = new FormAttachment(acrossSash);
		formData.left   = new FormAttachment(0);
		formData.right  = new FormAttachment(100);
		graphSash.setLayoutData(formData);
		graphSash.setLayout(new FormLayout());
		
		// FormData for bottom composite
		formData = new FormData();
		formData.top    = new FormAttachment(acrossSash);
		formData.bottom = new FormAttachment(100);
		formData.left   = new FormAttachment(0);
		formData.right  = new FormAttachment(100);
		bottomComposite.setLayoutData(formData);
		bottomComposite.setLayout(new FormLayout());
		
		// FormData for acrossSash
		// Put it initially in the middle
		formData = new FormData();
		formData.top    = new FormAttachment(50);
		formData.left   = new FormAttachment(0);
		formData.right  = new FormAttachment(100);
		acrossSash.setLayoutData(formData);
				
		final FormData acrossSashData = formData;
		final Composite parentFinal = acrossSash.getParent();
		acrossSash.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				if (event.detail != SWT.DRAG) {
					acrossSashData.top = new FormAttachment(0, event.y);
					parentFinal.layout();
				}
			}
		});
		graph = new SwmtGraph(graphSash);
		allEventsGraph = new GraphForAllEvents(graphSash);
		
		graph.setInputCyclesData(parsedData);
		allEventsGraph.setInputCyclesData(parsedData);
		
		graph.constructGraphArea();
				
		mainTabFolder = new TabFolder(bottomComposite, SWT.NONE);
		mainTabFolder.setLayout(new FillLayout(2));
		GridData tabGD=new GridData(GridData.FILL_HORIZONTAL);
		mainTabFolder.setLayoutData(tabGD);
		mainTabFolder.addSelectionListener(this);
		
		//Construct Events Tab
		constructEventTabsArea(mainTabFolder);
		//Construct Graphed Items Tab
		TabItem graphedItemsTab = new TabItem(mainTabFolder,SWT.NONE);
		graphedItemsTab.setText(GRAPHED_ITEMS_LABEL);
		
		GraphedItemsHelper graphedTabHelper = new GraphedItemsHelper();
		graphedItemsViewer = graphedTabHelper.constructGraphedItemsViewer(graphedItemsTab, allEventsGraph);
				
		GRAPHS_PAGE = addPage(parent);
		setPageText(GRAPHS_PAGE, "  Graphs  ");	 
		
		if(threads != null)
			threadTble.setInput(threads);
		if(chunks != null)
			chunkTble.setInput(chunks);
		if(disks != null)
			diskTble.setInput(disks);
		
		sysinfoTble.setInput(GenericGraph.getGraphableKernels());
		

		
	}
	
	/**
	 * Creates Anlysis page in the SWMT Editor
	 *
	 */
	private void createAnalysisPage()
	{
		Composite parentComposite = new Composite(getContainer(), SWT.NONE);
		parentComposite.setLayout(new FormLayout());

		Composite titleBar = new Composite(parentComposite, SWT.NONE);
		Composite holder = new Composite(parentComposite, SWT.NONE);
		
		Label graph_title = new Label(titleBar, SWT.CENTER|SWT.BORDER);
		graph_title.setBackground(parentComposite.getDisplay().getSystemColor(SWT.COLOR_DARK_BLUE));
		graph_title.setForeground(parentComposite.getDisplay().getSystemColor(SWT.COLOR_WHITE));
		
		Font roman_8 = new Font(parentComposite.getDisplay(), "Arial",  10, SWT.BOLD);
		graph_title.setFont(roman_8);
		graph_title.setText("");
		
		FormData formData = new FormData();
		formData.top    = new FormAttachment(0);
		formData.left   = new FormAttachment(0);
		formData.right  = new FormAttachment(100);
		titleBar.setLayoutData(formData);
		titleBar.setLayout(new FormLayout());
		
		FormData data = new FormData();
		data.left = new FormAttachment(0);
		data.right = new FormAttachment(100);
		graph_title.setLayoutData(data);
		
		// FormData for the overall holder composite
		formData = new FormData();
		formData.top    = new FormAttachment(titleBar);
		formData.bottom = new FormAttachment(100);
		formData.left   = new FormAttachment(0);
		formData.right  = new FormAttachment(100);
		holder.setLayoutData(formData);
		holder.setLayout(new FormLayout());
				
		graphSash = new SashForm(holder, SWT.VERTICAL|SWT.SMOOTH|SWT.BORDER);
		graphSash.SASH_WIDTH = 5;
		graphSash.setLayout(new FillLayout());
		
		Composite bottomComposite = new SashForm(holder, SWT.VERTICAL);
		final Sash acrossSash = new Sash(holder, SWT.HORIZONTAL);
		
		formData = new FormData();
		formData.top    = new FormAttachment(0);
		formData.bottom = new FormAttachment(acrossSash);
		formData.left   = new FormAttachment(0);
		formData.right  = new FormAttachment(100);
		graphSash.setLayoutData(formData);
		graphSash.setLayout(new FormLayout());
				
		// FormData for bottom
		formData = new FormData();
		formData.top    = new FormAttachment(acrossSash);
		formData.bottom = new FormAttachment(100);
		formData.left   = new FormAttachment(0);
		formData.right  = new FormAttachment(100);
		bottomComposite.setLayoutData(formData);
		bottomComposite.setLayout(new FormLayout());
				
		// FormData for acrossSash
		// Put it initially in the middle
		formData = new FormData();
		formData.top    = new FormAttachment(50);
		formData.width = 20;
		formData.left   = new FormAttachment(0);
		formData.right  = new FormAttachment(100);
		acrossSash.setLayoutData(formData);
		
		final FormData acrossSashData = formData;
		final Composite parentFinal = acrossSash.getParent();
		acrossSash.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				if (event.detail != SWT.DRAG) {
					acrossSashData.top = new FormAttachment(0, event.y);
					parentFinal.layout();
				}
			}
		});
		
		Composite parent = new Composite(bottomComposite, SWT.NONE);
		parent.setLayout(new GridLayout(7, false));
		parent.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		Label title = new Label(parent, SWT.WRAP);
		title.setText("Linear Analysis Results"); 
		title.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		//Action button used for creation of report file.
		generate_report_btn = new Button(parent, SWT.PUSH);
		generate_report_btn.setText("Generate report...");
		generate_report_btn.setToolTipText("Click here to generate pdf report for the selected issues.");
		generate_report_btn.addSelectionListener(this);
		
		Label separator = new Label(parent, SWT.SEPARATOR);
		GridData sepGD = new GridData();
		sepGD.heightHint = 25;
		separator.setLayoutData(sepGD);
		
		Label severity = new Label(parent, SWT.NONE);
		severity.setText("Severity:"); 
		
		severity_label = new Label(parent, SWT.WRAP);
		severity_label.setText("     "); 
		GridData d = new GridData(GridData.FILL);
		d.heightHint = 20;
		severity_label.setLayoutData(d);
		
		severity_combo = new Combo(parent, SWT.BORDER|SWT.READ_ONLY);
		d = new GridData();
		d.widthHint = 100;
		severity_combo.setLayoutData(d);
		severity_combo.setItems(new String[]{"All", AnalyserConstants.Priority.CRITICAL.name(), AnalyserConstants.Priority.HIGH.name(),AnalyserConstants.Priority.NORMAL.name(), "Custom filter"}); 
		severity_combo.addSelectionListener(this);
		severity_combo.select(0);
		severity_combo.setToolTipText("Severity of an issue.");
		
		//Action button used to launch Custom filter dialog.
		advanced_filter_button = new Button(parent, SWT.PUSH);
		advanced_filter_button.setText("Set Custom filter..."); 
		advanced_filter_button.addSelectionListener(this);
		advanced_filter_button.setToolTipText("Advanced settings to filter issues below.");
		advanced_filter_button.setEnabled(false);
		
		issues_tree = new Tree(parent, SWT.FULL_SELECTION | SWT.MULTI | SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL | SWT.CHECK);
		GridData gd = new GridData(GridData.FILL_BOTH);
		gd.horizontalSpan = 7;
		gd.grabExcessVerticalSpace = true;
		issues_tree.addSelectionListener(this);
		
		LinearIssuesGraph issue_graph = new LinearIssuesGraph(graphSash);
		issue_graph.setLogData(parsedData);
		
		//Creates issues viewer.
		viewer = new IssuesViewer(issues_tree, issue_graph);
		viewer.createIssuesViewerAndGraph();
		viewer.setContentProvider(new IssuesTreeContentProvider());
		viewer.setLabelProvider(new IssuesTreeLabelProvider());
		filter = new IssuesFilter();
		viewer.addFilter(filter);
		
		ANALYSIS_PAGE = addPage(parentComposite);
		setPageText(ANALYSIS_PAGE, " Analysis "); 
		
		SWMTAnalysisRunnable start_analysis = new SWMTAnalysisRunnable();
		IWorkbench wb = PlatformUI.getWorkbench();
		IWorkbenchWindow win = wb.getActiveWorkbenchWindow();
		Shell shell = win != null ? win.getShell() : null;
		try {
			new ProgressMonitorDialog(shell).run(true, true, start_analysis);
		} catch (InvocationTargetException err) {
			err.printStackTrace();
		} catch (InterruptedException err) {
			err.printStackTrace();
		}
		viewer.setInput(analysis_results_obj);
		
		//Set top 5 issues in the overview tab
		setTopIssues(analysis_results_obj);
		
		issues_tree.setFocus();
	}

	/**
	 * Construts UI controls under Events Tab area.
	 * @param mainTabFolder represents parent folder
	 */
	private void constructEventTabsArea(TabFolder mainTabFolder)
	{
		eventsTab = new TabItem(mainTabFolder,SWT.NONE);
		eventsTab.setText("  Events  "); 
		
		//Events Tab => events list and 4 inner tabs
		SashForm form=new SashForm(mainTabFolder, SWT.BORDER|SWT.HORIZONTAL|SWT.SMOOTH);
		form.setLayout(new GridLayout());
		
		Composite eventC = new Composite(form, SWT.NONE);
		eventC.setLayout(new GridLayout(1,false));
		eventC.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		eventsCombo = new Combo(eventC, SWT.BORDER|SWT.V_SCROLL | SWT.READ_ONLY);
		
		GridData comboGD = new GridData(GridData.FILL_HORIZONTAL);
		eventsCombo.setLayoutData(comboGD);
		for(CATEGORIES value : CATEGORIES.values())
			eventsCombo.add(value.toString());
		eventsCombo.addSelectionListener(this);
		
				
		eventList = new List(eventC, SWT.BORDER|SWT.SINGLE|SWT.V_SCROLL|SWT.H_SCROLL);
		GridData listGD = new GridData(GridData.FILL_BOTH);
		listGD.grabExcessVerticalSpace = true;
		eventList.setLayoutData(listGD);
		eventList.addSelectionListener(this);
	
		innerTabFolder = new TabFolder(form, SWT.NONE);
		innerTabFolder.setLayout(new GridLayout());
		GridData innertabGD = new GridData(GridData.FILL_HORIZONTAL|GridData.FILL_VERTICAL);
		innertabGD.verticalSpan = 2;
		innerTabFolder.setLayoutData(innertabGD);
		innerTabFolder.addSelectionListener(this);
		
		threadTab = new TabItem(innerTabFolder,SWT.NONE);
		threadTab.setText("  Threads  "); 
		
		Composite compositeThrd = new Composite(innerTabFolder, SWT.NONE);
		compositeThrd.setLayout(new GridLayout(1, true));
		threadTble = new FilterTextTable(this, compositeThrd, FilterTextTable.THREADS_TITLE);
		this.threadViewer = threadTble.getTableViewer();
	
		GridData table_layout_data = new GridData(GridData.FILL_BOTH);
		table_layout_data.grabExcessVerticalSpace = true;
		
		compositeThrd.setLayoutData(table_layout_data);
		threadTab.setControl(compositeThrd);
		
		chunksTab = new TabItem(innerTabFolder,SWT.NONE);
		chunksTab.setText("  Chunks  "); 
		
		Composite compositeChnk = new Composite(innerTabFolder, SWT.NONE);
		compositeChnk.setLayout(new GridLayout(1, true));
		chunkTble = new FilterTextTable(this, compositeChnk, FilterTextTable.CHUNKS_TITLE);
		compositeChnk.setLayoutData(table_layout_data);
		chunksTab.setControl(compositeChnk);
		this.chunksViewer = chunkTble.getTableViewer(); 
	
		diskTab = new TabItem(innerTabFolder,SWT.NONE);
		diskTab.setText("  Disks  "); 
	
		Composite compositeDisk = new Composite(innerTabFolder, SWT.NONE);
		compositeDisk.setLayout(new GridLayout(1, false));
		compositeDisk.setLayoutData(table_layout_data);
		diskTble = new FilterTextTable(this, compositeDisk, FilterTextTable.DISKS_TITLE);
		diskTab.setControl(compositeDisk);		
		this.disksViewer = diskTble.getTableViewer();
		
		sysInfoTab = new TabItem(innerTabFolder,SWT.NONE);
		sysInfoTab.setText("  System Data  "); 
		
		Composite compositeSysinfo = new Composite(innerTabFolder, SWT.NONE);
		compositeSysinfo.setLayout(new GridLayout(1, false));
		compositeSysinfo.setLayoutData(table_layout_data);
		sysinfoTble = new FilterTextTable(this, compositeSysinfo, FilterTextTable.SYSTEM_DATA_TITLE);
		sysInfoTab.setControl(compositeSysinfo);
		this.sysElemsViewer = sysinfoTble.getTableViewer();
		
		eventsCombo.select(0);
		eventsCombo.notifyListeners(SWT.Selection, new Event());
		
		form.setWeights(new int[] {30,70});
		form.SASH_WIDTH = 1;
		eventsTab.setControl(form);
	}
	
	/**
	 * Creates the pages of the multi-page editor.
	 */
	protected void createPages() {

		if(parsedData == null || parsedData.getNumberOfCycles() == 0)
			return;
		
		createOverviewPage();
		
		if(parsedData.getNumberOfCycles() > 1)
		{
			createGraphsPage();
			createAnalysisPage();
		}
		else
		{
			TableItem no_issues_item = new TableItem(issues_table,SWT.NONE);
			no_issues_item.setText(NO_ISSUES_MSG); 
			viewAll_btn.setEnabled(false);
		}
	}
	
	/**
	 * The method picks up the top 5 critical issues from the given list of issues
	 * and displays them in the overview page.
	 * @param issues_results
	 */
	public void setTopIssues(ArrayList<Object> issues_results)
	{
		if(issues_results.size() == 0)
		{
			TableItem item = new TableItem(issues_table,SWT.NONE);
			item.setText(NO_ISSUES_MSG);
			return;
		}
	
		ArrayList<ResultElements> critical_issues = new ArrayList<ResultElements>();
		
		for(Object obj:issues_results)
		{
			if(obj instanceof ResultsParentNodes)
			{
				ResultsParentNodes parent = (ResultsParentNodes)obj;
				Object[] children = parent.getChildren();
				
				for(Object issue:children)
				{
					if(issue instanceof ResultElements)
					{
						ResultElements issue_elem = (ResultElements)(issue);
						if(issue_elem.getPriority() == AnalyserConstants.Priority.CRITICAL)
							critical_issues.add(issue_elem);
					}
				}
			}
		}
		
		if(critical_issues.size() == 0)
		{
			TableItem item = new TableItem(issues_table,SWT.NONE);
			item.setText(NO_ISSUES_MSG);
			return;
		}
		else
		{
			Collections.sort(critical_issues);
		
			for(int index = critical_issues.size()-1, i=1; i <=5;i++,index--)
			{
				if(index < 0)
					break;
				
				ResultElements temp = critical_issues.get(index);
				TableItem item = new TableItem(issues_table,SWT.NONE);
				item.setText(new String[]{temp.getItemName(),temp.getEvent()});
			}
		}
		
	}
	

	/* (non-Javadoc)
	 * @see org.eclipse.ui.part.MultiPageEditorPart#dispose()
	 */
	public void dispose() {
		super.dispose();
	}
	/* (non-Javadoc)
	 * @see org.eclipse.ui.part.EditorPart#doSave(org.eclipse.core.runtime.IProgressMonitor)
	 */
	public void doSave(IProgressMonitor monitor) {
		getEditor(0).doSave(monitor);
	}
	/**
	 * Saves the multi-page editor's document as another file.
	 * Also updates the text for page 0's tab, and updates this multi-page editor's input
	 * to correspond to the nested editor's.
	 * @see org.eclipse.ui.part.EditorPart#doSaveAs()
	 */
	public void doSaveAs() {
		IEditorPart editor = getEditor(0);
		editor.doSaveAs();
		setPageText(0, editor.getTitle());
		setInput(editor.getEditorInput());
	}
	
	/**
	 * Go to marker
	 * @see IDE#gotoMarker(IEditorPart, IMarker)
	 * @param marker
	 */
	public void gotoMarker(IMarker marker) {
		setActivePage(0);
		IDE.gotoMarker(getEditor(0), marker);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.part.MultiPageEditorPart#init(org.eclipse.ui.IEditorSite, org.eclipse.ui.IEditorInput)
	 */
	public void init(IEditorSite site, IEditorInput editorInput)
		throws PartInitException {
		if (!(editorInput instanceof SWMTEditorInput))
			throw new PartInitException("Invalid Input: Must be SWMTEditorInput"); 
		parsedData = ((SWMTEditorInput)editorInput).getParsedData();
		ov = ((SWMTEditorInput)editorInput).getOverview();
		
		SWMTLogReaderUtils util = new SWMTLogReaderUtils();
		
		threads = util.getAllThreadNames(parsedData);
		if(threads != null)
			Collections.sort(threads);
		
		chunks = util.getAllGlobalChunkNames(parsedData);
		if(chunks != null)
			Collections.sort(chunks);
		
		nonHeapChunks = util.getAllNonHeapChunkNames(parsedData);
		if(nonHeapChunks != null)
			Collections.sort(nonHeapChunks);
		
		disks = util.getAllDiskNames(parsedData);
		if(disks != null)
			Collections.sort(disks);
		
		super.init(site, editorInput);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.part.EditorPart#isSaveAsAllowed()
	 */
	public boolean isSaveAsAllowed() {
		return true;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.swt.events.SelectionListener#widgetDefaultSelected(org.eclipse.swt.events.SelectionEvent)
	 */
	public void widgetDefaultSelected(SelectionEvent e) {
	}
	/* (non-Javadoc)
	 * @see org.eclipse.swt.events.SelectionListener#widgetSelected(org.eclipse.swt.events.SelectionEvent)
	 */
	public void widgetSelected(SelectionEvent e) {
		
		//When Export button is selected, data from given log files 
		//will be exported to an excel file.
		if(e.widget == export)
		{
			FileDialog dlg = new FileDialog(Display.getCurrent().getActiveShell(), SWT.SAVE);
			dlg.setFilterExtensions(new String[]{"*.xls"}); 
			String fileName = dlg.open();
			if(fileName==null)
				return;

			SWMTExcelCreationRunnableWithProcess process = new SWMTExcelCreationRunnableWithProcess(fileName, parsedData, toCombo.getSelectionIndex()+1);
			IWorkbench wb = PlatformUI.getWorkbench();
			IWorkbenchWindow win = wb.getActiveWorkbenchWindow();
			Shell shell = win != null ? win.getShell() : null;
			try {
				new ProgressMonitorDialog(shell).run(true, true, process);
			} catch (InvocationTargetException err) {
				err.printStackTrace();
			} catch (InterruptedException err) {
				err.printStackTrace();
			}
		}
		else if(e.widget == allBtn)
		{
			toCombo.setEnabled(false);
		}
		else if(e.widget == notAllBtn)
		{
			toCombo.setEnabled(true);
		}
		else if(e.widget == viewAll_btn)
		{
			this.setActivePage(ANALYSIS_PAGE);
		}
		/*
		 * When Events list selection is changed, all previous selections should be cleared
		 * and they should be moved to Graphed items tab.
		 * Also, listeners of enabled tables should be notified.
		 */
		else if(e.widget == eventList)
		{
			selectedEvent = eventList.getSelection()[0];
			title.setText(selectedEvent);
			updateGraphedItemsList(lastSelectedEvent);
		
			if(!selectedEvent.equals(lastSelectedEvent))
			{
				chunkTble.cancelSelectionList();
				threadTble.cancelSelectionList();
				diskTble.cancelSelectionList();
				sysinfoTble.cancelSelectionList();
			}
			
			if(!selectedEvent.equals(lastSelectedEvent) && graph != null)
				graph.clearGraph();
			
			if(Arrays.asList(RAM_GRP).contains(selectedEvent))
			{
				threadTble.setEnabled(false);
				chunkTble.setEnabled(false);
				diskTble.setEnabled(false);
				sysinfoTble.setEnabled(false);
				
				DisksGraph diskGraph = new DisksGraph();
				GenericGraph.EventTypes eventType = GraphsUtils.getMappedEvent(selectedEvent);
				diskGraph.setEvent(eventType);
				diskGraph.setCyclesData(parsedData);
				
				if(graph != null)
					graph.redraw(diskGraph);
			}
			else
			{
				if(Arrays.asList(CHUNKS_GRP).contains(selectedEvent))
				{
					if(Arrays.asList(CHUNKS_GRP).indexOf(selectedEvent)==0 && !selectedEvent.equals(lastSelectedEvent))
						chunkTble.setInput(chunks);
					else if(Arrays.asList(CHUNKS_GRP).indexOf(selectedEvent)==1 && !selectedEvent.equals(lastSelectedEvent))
						chunkTble.setInput(nonHeapChunks);
					innerTabFolder.setSelection(chunksTab);
					
					CheckStateChangedEvent event = new CheckStateChangedEvent(chunksViewer, null, false);
					
					if(chunkTble.getCheckStateListener() != null)
						chunkTble.getCheckStateListener().checkStateChanged(event);
				}
				else if(Arrays.asList(HEAP_GRP).contains(selectedEvent) || Arrays.asList(FILES_GRP).contains(selectedEvent) || Arrays.asList(HPAS_GRP).contains(selectedEvent))
				{
					innerTabFolder.setSelection(threadTab);
					
					CheckStateChangedEvent event = new CheckStateChangedEvent(threadViewer, null, false);
					if(threadTble.getCheckStateListener() != null)
						threadTble.getCheckStateListener().checkStateChanged(event);
				}
				else if(Arrays.asList(DISK_GRP).contains(selectedEvent))
				{
					innerTabFolder.setSelection(diskTab);
					
					CheckStateChangedEvent event = new CheckStateChangedEvent(disksViewer, null, false);
					if(diskTble.getCheckStateListener() != null)
						diskTble.getCheckStateListener().checkStateChanged(event);
				}
				else if(Arrays.asList(SYSINFO_GRP).contains(selectedEvent))
				{
					innerTabFolder.setSelection(sysInfoTab);
					
					CheckStateChangedEvent event = new CheckStateChangedEvent(sysElemsViewer, null, false);
					
					if(sysinfoTble.getCheckStateListener() != null)
						sysinfoTble.getCheckStateListener().checkStateChanged(event);
				}
			
				threadTble.setEnabled(innerTabFolder.getItem(innerTabFolder.getSelectionIndex())==threadTab);
				chunkTble.setEnabled(innerTabFolder.getItem(innerTabFolder.getSelectionIndex())==chunksTab);
				diskTble.setEnabled(innerTabFolder.getItem(innerTabFolder.getSelectionIndex())==diskTab);
				sysinfoTble.setEnabled(innerTabFolder.getItem(innerTabFolder.getSelectionIndex())==sysInfoTab);
			
			}
			
			lastSelectedEvent = selectedEvent;
		}
		//Events list will be modified based on the selection of 
		//event category from the drop down box.
		else if(e.widget == eventsCombo)
		{
			eventList.removeAll();
			CATEGORIES index = CATEGORIES.valueOf(eventsCombo.getText());
			switch(index)
			{
			case All_events:
				eventList.setItems(ALL_GRP);
				break;
			case Chunks:
				eventList.setItems(CHUNKS_GRP);
				break;
			case Disk:
				eventList.setItems(DISK_GRP);
				break;
			case Files:
				eventList.setItems(FILES_GRP);
				break;
			case Heap:
				eventList.setItems(HEAP_GRP);
				break;
			case HPAS:
				eventList.setItems(HPAS_GRP);
				break;
			case RAM:
				eventList.setItems(RAM_GRP);
				break;
			case System_info:
				eventList.setItems(SYSINFO_GRP);
				break;			
			}
			eventList.select(0);
			
			if(eventList.getItemCount() > 0) 
			{
				eventList.notifyListeners(SWT.Selection, new Event());
			}
		}	
		else if(e.widget == mainTabFolder && mainTabFolder.getSelectionIndex() != -1 &&
				mainTabFolder.getSelection()[0].getText().trim().equalsIgnoreCase(GRAPHED_ITEMS_LABEL))
		{
			selectedEvent = eventList.getSelection()[0];
			updateGraphedItemsList(selectedEvent);
			
			title.setText("Graphed Items"); 
			
			Object [] checkedElems = graphedItemsViewer.getCheckedElements();
			ArrayList<GraphedItemsInput> selectedItems = new ArrayList<GraphedItemsInput>();
				
			for(Object obj:checkedElems)
			{
				GraphedItemsInput graphInput = (GraphedItemsInput)obj;
				selectedItems.add(graphInput);
			}
				
			allEventsGraph.setGraphedItemsInput(selectedItems);				
			allEventsGraph.constructGraphArea();
			
		}
		else if(e.widget == mainTabFolder && mainTabFolder.getSelectionIndex() != -1 && mainTabFolder.getSelection()[0] == eventsTab)
		{
			if(graphedItemsViewer.getInput() == null)
			{
				graphed.clear();
			}
			graph.constructGraphArea();
			
			if(eventList.getSelectionIndex() != -1){
				title.setText(eventList.getItem(eventList.getSelectionIndex()));
				eventList.notifyListeners(SWT.Selection, new Event());
			}
			else
				title.setText(""); 
			
		}
		
		//Issues list gets modified based on the selection of Severity 
		//from the drop down box.
		else if(e.widget == severity_combo)
		{
			advanced_filter_button.setEnabled(false);
			filter.setAdvancedSearchOptions(null);
			Image img = null;
			String icon_name = ""; 
			switch(severity_combo.getSelectionIndex())
			{
			case 1:
				icon_name = "\\red.png"; 
				filter.setFilterText(AnalyserConstants.Priority.CRITICAL.name());
				break;
			case 2:
				icon_name = "\\yellow.png"; 
				filter.setFilterText(AnalyserConstants.Priority.HIGH.name());
				break;
			case 3:
				icon_name = "\\green.png"; 
				filter.setFilterText(AnalyserConstants.Priority.NORMAL.name());
				break;
			case 4:
				advanced_filter_button.setEnabled(true);
			default:
				icon_name = null;
				filter.setFilterText(null);
				break;
			}
			if(icon_name!=null)
			{
				try {
					img = new Image( Display.getCurrent(), SwmtAnalyserPlugin.getPluginInstallPath() + "\\icons" + icon_name); 
				} catch (RuntimeException e1) {
					e1.printStackTrace();
				}
			}
			severity_label.setImage(img);
			issues_tree.setFocus();
			viewer.refresh();
		}
		else if(e.widget == issues_table)
		{
			analyse_menuItem.setEnabled(!issues_table.getSelection()[0].getText(0).startsWith(NO_ISSUES_MSG));
		}
		else if(e.widget == advanced_filter_button)
		{
			AdvancedFilterDialog dlg = new AdvancedFilterDialog(Display.getCurrent().getActiveShell());
			int status = dlg.open();
			if(status == Dialog.OK)
			{
				FilterInput input = dlg.getFilterOptions();
				filter.setAdvancedSearchOptions(input);
				issues_tree.setFocus();
				viewer.refresh();
			}			
		}
		else if(e.widget == generate_report_btn)
		{
			//Save graph in temporary location
			GC gc = new GC(graphSash);
			Image image = new Image(Display.getCurrent(), graphSash.getClientArea().width, graphSash.getClientArea().height);
			graphSash.setFocus();
			gc.copyArea(image, 0, 0);
			gc.dispose();
			ImageData data = image.getImageData();
			ImageLoader loader = new ImageLoader();
			loader.data = new ImageData[] {data};
			loader.save(SwmtAnalyserPlugin.getPluginInstallPath()+"\\swmt_graph.bmp", SWT.IMAGE_BMP);
			image.dispose();
			
			// Now open the wizard
			Runnable showWizardRunnable = new Runnable(){
				public void run(){
					WizardDialog wizDialog;
					ReportGenerationWizard wiz = new ReportGenerationWizard(ov, parsedData.getLogData()[0].getRomCheckSum(), parsedData.getLogData()[0].getRomVersion(), issues_tree);
					wizDialog = new WizardDialog(Display.getCurrent().getActiveShell(), wiz);
					wizDialog.create();		
					wizDialog.getShell().setSize(400, 500);
					wizDialog.addPageChangedListener(wiz);
					wizDialog.open();
				}
			};
			Display.getDefault().asyncExec(showWizardRunnable); 
		}
	}
	
	/**
	 * SWMT specific runnable process for creating excel.
	 *
	 */
	private class SWMTExcelCreationRunnableWithProcess implements IRunnableWithProgress
	{
		private String fileName;
		private CycleData [] data = new CycleData [0];
		private int exportFilesNo;
		private OverviewData ovData;
		public SWMTExcelCreationRunnableWithProcess(String fileName, ParsedData parsedData,int exportFiles) {
			this.data = parsedData.getLogData();
			this.fileName = fileName;
			this.exportFilesNo = exportFiles;
		}
		
		public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
			createExcel(monitor);
		}
		
		private void createExcel(IProgressMonitor monitor)
		{
			monitor.beginTask("Parsing log files...", 10); 
			ExcelCreator creator = new ExcelCreator(fileName);
			ovData = (new SWMTLogReaderUtils()).getOverviewInformationFromCyclesData(data, this.exportFilesNo);
			creator.setOverviewPageInput(ovData);
			
			ArrayList<CycleData> exportedCycles = new ArrayList<CycleData>();
			
			if(this.exportFilesNo >= 255)
			{
				int extras = this.exportFilesNo - 254;
				ArrayList<Integer> series = new ArrayList<Integer>(extras);
				
				if(extras ==1)
				{
					series.add(this.exportFilesNo/2);
					creator.setSkipFileConstant(series);
				}
				else
				{
					int temp = this.exportFilesNo;
					while(series.size()!=extras)
					{
						temp = temp/2;
						for(int i=2; i<this.exportFilesNo; i++)
						{
							if(i%temp == 0)
							{
								if(!series.contains(i))
									series.add(i);
								if(series.size() == extras)
									break;
							}
						}
					}						
					creator.setSkipFileConstant(series);
				}
			}
			
			for(int i=0; i<exportFilesNo; i++)
			{
				CycleData cycle = data[i];
				exportedCycles.add(cycle);
			}
			monitor.worked(3);
			
			ParsedData exportedData = new ParsedData();
			exportedData.setParsedData(exportedCycles);
			
			creator.setInputCyclesData(exportedData);

			if(creator.createExcel(monitor))
			{
				monitor.done();
				Runnable p = new Runnable(){

					public void run() {
							if(MessageDialog.openQuestion(Display.getCurrent().getActiveShell(),"Confirmation","Would you like to open the saved file?"))  
							{
								Program p=Program.findProgram(".xls"); 
								if(p!=null)
									p.execute(fileName);
							}	
						}		
						
				};
				Display.getDefault().asyncExec(p);
			}
			else
			{
				File file = new File(fileName);
				file.delete();
			}
		}
	}

	/**
	 * Runnable class to run analysis in new thread.
	 *
	 */
	public class SWMTAnalysisRunnable implements IRunnableWithProgress
	{
		
		public SWMTAnalysisRunnable() {
		}
		
		public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
			monitor.beginTask("Analysing data...", 10); 
			int worked = 0;
			for(IAnalyser analyser:SwmtAnalyserPlugin.getDefault().getRegisteredAnalysers())
			{
				analyser.analyse(parsedData);
				analysis_results_obj.addAll(Arrays.asList(analyser.getResults()));
				worked = worked + 10/(SwmtAnalyserPlugin.getDefault().getRegisteredAnalysers().length) ;
				monitor.worked(worked);
			}
			monitor.done();
		}
	}
	
	/* (non-Javadoc)
	 * @see com.nokia.s60tools.swmtanalyser.ui.graphs.IGraphTypeSelectionListener#notifyThreadsSelection()
	 */
	public void notifyThreadsSelection()
	{
		Object [] checkedElements = this.threadViewer.getCheckedElements();
		
		ArrayList<String> threadNames = new ArrayList<String>();
		ArrayList<Color> threadColors = new ArrayList<Color>();
		
		for(Object obj: checkedElements)
		{
			if(obj instanceof TableViewerInputObject)
			{
				TableViewerInputObject checkedItem = (TableViewerInputObject)obj;
				
				threadNames.add(checkedItem.getName());
				threadColors.add(checkedItem.getColor());
			}
		}
		
		//Graph class will be informed about the selected event and selected threads.
		ThreadsGraph thGraph = new ThreadsGraph();
		GenericGraph.EventTypes eventType = GraphsUtils.getMappedEvent(selectedEvent);
		thGraph.setEvent(eventType);
		thGraph.setUserSelectedItems(threadNames);
		thGraph.setCyclesData(parsedData);
		thGraph.setColors(threadColors);
		
		graph.redraw(thGraph);
	}
	
	/* (non-Javadoc)
	 * @see com.nokia.s60tools.swmtanalyser.ui.graphs.IGraphTypeSelectionListener#notifyChunksSelection()
	 */
	public void notifyChunksSelection()
	{
		Object [] checkedElements = this.chunksViewer.getCheckedElements();
		
		ArrayList<String> chunkNames = new ArrayList<String>();
		ArrayList<Color> chunkColors = new ArrayList<Color>();
		
		for(Object obj: checkedElements)
		{
			if(obj instanceof TableViewerInputObject)
			{
				TableViewerInputObject checkedItem = (TableViewerInputObject)obj;
				
				chunkNames.add(checkedItem.getName());
				chunkColors.add(checkedItem.getColor());
			}
		}
		
		//Graph class will be informed about the selected event and selected chunks.
		ChunksGraph chnkGraph = new ChunksGraph();
		GenericGraph.EventTypes eventType = GraphsUtils.getMappedEvent(selectedEvent);
		chnkGraph.setEvent(eventType);
		chnkGraph.setUserSelectedItems(chunkNames);
		chnkGraph.setCyclesData(parsedData);
		chnkGraph.setColors(chunkColors);
		
		graph.redraw(chnkGraph);
	}
	
	/* (non-Javadoc)
	 * @see com.nokia.s60tools.swmtanalyser.ui.graphs.IGraphTypeSelectionListener#notifyDisksSelection()
	 */
	public void notifyDisksSelection()
	{
		Object [] checkedElements = this.disksViewer.getCheckedElements();
		
		ArrayList<String> diskNames = new ArrayList<String>();
		ArrayList<Color> diskColors = new ArrayList<Color>();
		
		for(Object obj: checkedElements)
		{
			if(obj instanceof TableViewerInputObject)
			{
				TableViewerInputObject checkedItem = (TableViewerInputObject)obj;
				
				diskNames.add(checkedItem.getName());
				diskColors.add(checkedItem.getColor());
			}
		}
		//Graph class will be informed about the selected event and selected disks.
		DisksGraph diskGraph = new DisksGraph();
		GenericGraph.EventTypes eventType = GraphsUtils.getMappedEvent(selectedEvent);
		diskGraph.setEvent(eventType);
		diskGraph.setUserSelectedItems(diskNames);
		diskGraph.setCyclesData(parsedData);
		diskGraph.setColors(diskColors);
		
		this.setGraphTitle(selectedEvent);
		graph.redraw(diskGraph);
	}
	
	/* (non-Javadoc)
	 * @see com.nokia.s60tools.swmtanalyser.ui.graphs.IGraphTypeSelectionListener#notifySysElementsSelection()
	 */
	public void notifySysElementsSelection()
	{
		Object [] checkedElements = this.sysElemsViewer.getCheckedElements();
		
		ArrayList<String> sysElemNames = new ArrayList<String>();
		ArrayList<Color> sysElemColors = new ArrayList<Color>();
		
		for(Object obj: checkedElements)
		{
			if(obj instanceof TableViewerInputObject)
			{
				TableViewerInputObject checkedItem = (TableViewerInputObject)obj;
				
				sysElemNames.add(checkedItem.getName());
				sysElemColors.add(checkedItem.getColor());
			}
		}
		
		//Graph class will be informed about the selected event and selected system elements.
		SystemDataGraph sysDataGraph = new SystemDataGraph();
		GenericGraph.EventTypes eventType = GraphsUtils.getMappedEvent(selectedEvent);
		sysDataGraph.setEvent(eventType);
		sysDataGraph.setUserSelectedItems(sysElemNames);
		sysDataGraph.setCyclesData(parsedData);
		sysDataGraph.setColors(sysElemColors);
		
		graph.redraw(sysDataGraph);
	}

	/**
	 * Changes the graph title with the given name
	 * @param name graph name to be set
	 */
	private void setGraphTitle(String name)
	{
		if(this.title != null)
			title.setText(name);
	}
	
	/**
	 * WHen Graphed items tab is selected, the items selected in the events tab will be moved 
	 * to Graphed items tab.
	 * @param event
	 */
	public void updateGraphedItemsList(String event)
	{
		if(threadTble.getEnabled())
		{
			Iterator<Entry<String, Color>> itr = threadTble.checked.entrySet().iterator();
			while(itr.hasNext())
			{
				Entry<String, Color> entry = itr.next();
				GraphedItemsInput item = new GraphedItemsInput();
				graph.storeClearedEventValues(entry.getKey(), allEventsGraph);
				
				item.setName(entry.getKey());
				item.setEvent(event);
				item.setColor(entry.getValue());
				item.setType("Thread"); 
				if(!checkItemInTheList(item,graphed))
				graphed.add(item);
			}
		}
		else if(chunkTble.getEnabled())
		{
			Iterator<Entry<String, Color>> itr = chunkTble.checked.entrySet().iterator();
			while(itr.hasNext())
			{
				Entry<String, Color> entry = itr.next();
				GraphedItemsInput item = new GraphedItemsInput();
				graph.storeClearedEventValues(entry.getKey(), allEventsGraph);
				
				item.setName(entry.getKey());
				item.setColor(entry.getValue());
				item.setEvent(event);
				item.setType("Chunk"); 
				if(!checkItemInTheList(item,graphed))
				graphed.add(item);
			}
		}
		else if(diskTble.getEnabled())
		{
			Iterator<Entry<String, Color>> itr = diskTble.checked.entrySet().iterator();
			while(itr.hasNext())
			{
				Entry<String, Color> entry = itr.next();
				GraphedItemsInput item = new GraphedItemsInput();
				graph.storeClearedEventValues(entry.getKey(), allEventsGraph);
				
				item.setName(entry.getKey());
				item.setColor(entry.getValue());
				item.setEvent(event);
				item.setType("Disk"); 
				if(!checkItemInTheList(item,graphed))
				graphed.add(item);
			}
		}
		else if(sysinfoTble.getEnabled())
		{
			Iterator<Entry<String, Color>> itr = sysinfoTble.checked.entrySet().iterator();
			while(itr.hasNext())
			{
				Entry<String, Color> entry = itr.next();
				GraphedItemsInput item = new GraphedItemsInput();
				graph.storeClearedEventValues(entry.getKey(), allEventsGraph);
				
				item.setName(entry.getKey());
				item.setColor(entry.getValue());
				item.setEvent(event);
				item.setType("System Data"); 
				if(!checkItemInTheList(item,graphed))
				graphed.add(item);
			}
		}
		else if(Arrays.asList(RAM_GRP).contains(event))
		{
			GraphedItemsInput item = new GraphedItemsInput();
			graph.storeClearedEventValues(event, allEventsGraph);
			
			item.setName(event);
			item.setEvent(event);
			item.setType("Memory"); 
			if(!checkItemInTheList(item,graphed))
			graphed.add(item);
		}
		
		if(graphed.size()>0)			
		{
			graphedItemsViewer.setInput(graphed);
			graphedItemsViewer.setAllChecked(true);
			for(Object obj : graphedItemsViewer.getCheckedElements())
			{
				GraphedItemsInput item = (GraphedItemsInput)obj;
				if(item.getColor() == null)
					item.setColor(getRandomColor());
				graphedItemsViewer.update(item, null);
			}
		}
	}
	
	/**
	 * The given item from the given list is checked.
	 * @param item
	 * @param list
	 * @return
	 */
	public boolean checkItemInTheList(GraphedItemsInput item, ArrayList<GraphedItemsInput> list)
	{
		for(GraphedItemsInput obj: list)
		{
			if(item.getEvent().equals(obj.getEvent()) && item.getName().equals(obj.getName()) && item.getType().equals(obj.getType()))
				return true;
		}
		return false;
	}
		
	/**
	 * Generates random color.
	 * @return a random color
	 */
	public Color getRandomColor()
	{
		Random rand = new Random();
		int r = rand.nextInt(255);
		int g = rand.nextInt(255);
		int b = rand.nextInt(255);
		return new Color(Display.getCurrent(), r, g,b);
	}
	

			
}
