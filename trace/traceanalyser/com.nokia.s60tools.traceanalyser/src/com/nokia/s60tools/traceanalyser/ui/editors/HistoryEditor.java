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



package com.nokia.s60tools.traceanalyser.ui.editors;

import java.util.ArrayList;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.MultiPageEditorPart;

import com.nokia.s60tools.traceanalyser.export.RuleEvent;
import com.nokia.s60tools.traceanalyser.plugin.TraceAnalyserPlugin;
import com.nokia.s60tools.traceanalyser.ui.views.MainView;
import com.nokia.s60tools.ui.S60ToolsTable;
import com.nokia.s60tools.ui.S60ToolsTableColumnData;
import com.nokia.s60tools.ui.S60ToolsTableFactory;


/**
 * HistoryEditor.
 * History view of Trace Analyser
 */
public class HistoryEditor extends MultiPageEditorPart{

	/* Tableviewer for history table */
	TableViewer viewer;
	
	/* contentprovider for history table */
	HistoryTableContentProvider contentProvider;
	
	/* Name of the rule */
	String ruleName;
	
	/* History Graph object */
	HistoryGraph graph;
	
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.part.MultiPageEditorPart#createPages()
	 */
	@Override
	protected void createPages() {
		int index = 0;
		createGraphPage(index);
		index++;
		createTablePage(index);
		
		ruleName = ((HistoryEditorInput)this.getEditorInput()).getEvents().getRule().getName();
		setPartName(ruleName);
		setHelps();
	}

	/**
	 * createTablePage.
	 * Method that creates table page of view
	 * @param index tab index of this page
	 */
	private void createTablePage(int index){
		// create composite where all components are placed
		Composite composite = new Composite(getContainer(), SWT.NONE);
 		GridLayout contentsLayout = new GridLayout();
 		contentsLayout.numColumns = 1;
 		composite.setLayout(contentsLayout);
 		composite.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		// Create column name array
		ArrayList<S60ToolsTableColumnData> columnArrayList = new ArrayList<S60ToolsTableColumnData>();
		
		// add column names
		columnArrayList.add(new S60ToolsTableColumnData("Status", 60, 0, HistoryTableDataSorter.STATUS)); 
		columnArrayList.add(new S60ToolsTableColumnData("Time", 150, 1, HistoryTableDataSorter.TIME)); 
		columnArrayList.add(new S60ToolsTableColumnData("Value", 60, 2, HistoryTableDataSorter.VALUE));
		columnArrayList.add(new S60ToolsTableColumnData("Violation", 60, 3, HistoryTableDataSorter.VIOLATION));
		
		
		S60ToolsTableColumnData[] columnDataTableHistory = columnArrayList.toArray(new S60ToolsTableColumnData[0]);
		
		// Create table for history events
		S60ToolsTable tableHistory = S60ToolsTableFactory.create(composite, columnDataTableHistory);
		ArrayList<RuleEvent> events = ((HistoryEditorInput)this.getEditorInput()).getEvents().getEvents();
		
		viewer = new TableViewer(tableHistory.getTableInstance());
		contentProvider = new HistoryTableContentProvider(events);
		viewer.setContentProvider(contentProvider);
		viewer.setLabelProvider(new HistoryTableLabelProvider());
		viewer.setComparator(new HistoryTableDataSorter());
		viewer.setSorter(new HistoryTableDataSorter());
		//viewer.setInput(getViewSite());
		viewer.setInput(this.getSite());
		tableHistory.setHostingViewer(viewer);

		// add this page to composite
		addPage(composite);
		
		// set page name
		setPageText(index,"Table" );

	}
	
	/**
	 * createGraphPage.
	 * Method that creates graph page of view
	 * @param index tab index of this page
	 */	
	private void createGraphPage(int index){
		
		// create composite where all components are placed
		Composite composite = new Composite(getContainer(), SWT.NONE);
 		GridLayout contentsLayout = new GridLayout();
 		contentsLayout.numColumns = 1;
 		composite.setLayout(contentsLayout);
 		composite.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		graph = new HistoryGraph(composite, 
				((HistoryEditorInput)this.getEditorInput()).getEvents());
		graph.drawGraph();
		
		addPage(composite);
		setPageText(index,"Graph" );
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.part.EditorPart#doSave(org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	public void doSave(IProgressMonitor arg0) {
		// Nothing to be done		
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.part.EditorPart#doSaveAs()
	 */
	@Override
	public void doSaveAs() {
		// Nothing to be done		
		
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.part.EditorPart#isSaveAsAllowed()
	 */
	@Override
	public boolean isSaveAsAllowed() {
		// Nothing to be done		
		return false;
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.part.MultiPageEditorPart#dispose()
	 */
	public void dispose(){
		MainView view = TraceAnalyserPlugin.getMainView();
		graph.dispose();
		if(view != null){
			view.editorClosed(ruleName);
		}
	}
	
	/**
	 * getRuleName.
	 * @return rule name
	 */
	public String getRuleName(){
		return ruleName;
	}
	
	/**
	 * historyUpdated.
	 * Method that is called when rule's history is updated 
	 * so that graph and table needs to be refreshed. 
	 */
	public void historyUpdated(){
		graph.redraw();
		viewer.refresh();
	}
	
	/**
	 * Set this page's context sensitive helps
	 */
	protected void setHelps() {
		// Set help
		PlatformUI.getWorkbench().getHelpSystem().setHelp( viewer.getControl(), com.nokia.s60tools.traceanalyser.resources.HelpContextIDs.TRACE_ANALYSER_HISTORY_VIEW);
		graph.setHelps();
	}
	
}