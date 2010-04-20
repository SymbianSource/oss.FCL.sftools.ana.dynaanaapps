/*
* Copyright (c) 2010 Nokia Corporation and/or its subsidiary(-ies). 
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

package com.nokia.s60tools.crashanalyser.ui.editors;

import java.util.List;

import org.eclipse.jface.resource.FontRegistry;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.PlatformUI;

import com.nokia.s60tools.crashanalyser.containers.EventLog;
import com.nokia.s60tools.crashanalyser.containers.OstTrace;
import com.nokia.s60tools.crashanalyser.containers.OstTraceLine;
import com.nokia.s60tools.crashanalyser.files.CrashFile;
import com.nokia.s60tools.crashanalyser.files.SummaryFile;
import com.nokia.s60tools.crashanalyser.resources.HelpContextIDs;

public class TracesPage {

	// event log group UI items
	Table tableEventLog;

	// OST traces group UI items
	Table tableOstTraces;
	
	FontRegistry fontRegistry;
	SummaryFile crashFile;
		
	/**
	 * Creates the page
	 * @param parent composite
	 * @param file summary file
	 * @return composite
	 */
	public Composite createPage(Composite parent, SummaryFile file) {
		crashFile = file;
		return doCreatePage(parent);
	}
	
	/**
	 * Creates the page
	 * @param parent composite
	 * @return composite
	 */
	public Composite createPage(Composite parent) {
		return doCreatePage(parent);
	}
	
	/**
	 * Loads data from given file into UI elements.
	 * @param file crash file
	 */
	public void setFile(CrashFile file) {
		if (file != null) {
			crashFile = file;
			loadEventLogTable();
		}
	}
	
	/**
	 * Creates all UI elements to the page
	 * @param parent
	 * @return composite
	 */
	Composite doCreatePage(Composite parent) {
		GridLayout layout = new GridLayout();
		layout.numColumns = 1;
		parent.setLayout(layout);
		parent.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		fontRegistry = new FontRegistry(Display.getCurrent());
		fontRegistry.put("monospace", new FontData[]{new FontData("Courier", 8, SWT.NORMAL)});

		SashForm sashFormMain = new SashForm(parent, SWT.VERTICAL);
		sashFormMain.setLayoutData(new GridData(GridData.FILL_BOTH));
		createOstTracesGroup(sashFormMain);
		createEventLogGroup(sashFormMain);
			
		setHelps();
		
		return parent;
	}

	/**
	 * Creates event log group
	 * @param parent
	 */
	void createEventLogGroup(Composite parent) {
		Group groupEventLog = new Group(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.numColumns = 1;
		groupEventLog.setLayout(layout);
		groupEventLog.setText("Event Log (latest events on top)");
		GridData groupGD = new GridData(GridData.FILL_BOTH);
		groupEventLog.setLayoutData(groupGD);

		tableEventLog = new Table(groupEventLog, SWT.BORDER);
		tableEventLog.setHeaderVisible(true);
		TableColumn col = new TableColumn(tableEventLog, SWT.LEFT);
		col.setWidth(200);
		col.setText("Type");
		col = new TableColumn(tableEventLog, SWT.LEFT);
		col.setWidth(200);
		col.setText("Value");
		tableEventLog.setLayoutData(new GridData(GridData.FILL_BOTH));
		tableEventLog.setFont(fontRegistry.get("monospace"));
		
		loadEventLogTable();
	}

	/**
	 * Creates OST traces group
	 * @param parent
	 */
	void createOstTracesGroup(Composite parent) {
		Group groupOstTraces = new Group(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.numColumns = 1;
		groupOstTraces.setLayout(layout);
		groupOstTraces.setText("OST Traces");
		GridData groupGD = new GridData(GridData.FILL_BOTH);
		groupOstTraces.setLayoutData(groupGD);

		tableOstTraces = new Table(groupOstTraces, SWT.BORDER);
		tableOstTraces.setHeaderVisible(true);
		TableColumn col = new TableColumn(tableOstTraces, SWT.LEFT);
		col.setWidth(90);
		col.setText("Timestamp");
		col = new TableColumn(tableOstTraces, SWT.LEFT);
		col.setWidth(600);
		col.setText("Text");
		col = new TableColumn(tableOstTraces, SWT.LEFT);
		col.setWidth(200);
		col.setText("File");
		col = new TableColumn(tableOstTraces, SWT.LEFT);
		col.setWidth(50);
		col.setText("Line");
		col = new TableColumn(tableOstTraces, SWT.LEFT);
		col.setWidth(100);
		col.setText("Type");
		col = new TableColumn(tableOstTraces, SWT.LEFT);
		col.setWidth(50);
		col.setText("Context ID");
		col = new TableColumn(tableOstTraces, SWT.LEFT);
		col.setWidth(100);
		col.setText("Prefix");
		col = new TableColumn(tableOstTraces, SWT.LEFT);
		col.setWidth(50);
		col.setText("Component");
		col = new TableColumn(tableOstTraces, SWT.LEFT);
		col.setWidth(50);
		col.setText("Group");
		col = new TableColumn(tableOstTraces, SWT.LEFT);
		col.setWidth(50);
		col.setText("ID");
		
		tableOstTraces.setLayoutData(new GridData(GridData.FILL_BOTH));
		tableOstTraces.setFont(fontRegistry.get("monospace"));
		
		loadOstTracesTable();
	}
	
	/**
	 * Loads all event log events to table
	 */
	void loadEventLogTable() {
		if (crashFile == null)
			return;
		
		EventLog eventLog = crashFile.getEventLog();
		if (eventLog != null) {
			List<String[]> events = eventLog.getLogEvents();
			if (events != null && !events.isEmpty()) {
				for (int i = 0; i < events.size(); i++) {
					String[] event = events.get(i);
					newEventTableItem(event[0], event[1]);
				}
				AutoSizeTableCells(tableEventLog);
			}
		}
	}

	/**
	 * Loads all OST traces to table
	 */
	void loadOstTracesTable() {
		if (crashFile == null)
			return;
	
		OstTrace ostTrace = crashFile.getOstTrace();
		
		if(ostTrace == null || ostTrace.getTraces() == null || ostTrace.getTraces().size() == 0) {
			newOstTableItem("", "No traces available. Set output traces to Mobile Crash in TraceSwitch tool to enable traces.", "", "", "", "", "", "", "", "");
		}
		
		if (ostTrace != null) {
			for(OstTraceLine traceLine : ostTrace.getTraces()) {
					newOstTableItem(traceLine.getTimestamp(), traceLine.getTraceText(),
							traceLine.getFile(), traceLine.getLineNumber(), traceLine.getType(),
							traceLine.getContextId(), traceLine.getPrefix(),
							traceLine.getComponent(), traceLine.getGroup(), traceLine.getId());
				}
				AutoSizeTableCells(tableOstTraces);
			}
		}
		
	

	/**
	 * Adds a new table row for event log table
	 * @param header header text
	 * @param value value text
	 */
	void newEventTableItem(String header, String value) {
		TableItem item = new TableItem(tableEventLog, SWT.NONE);
		item.setText(new String[] {header, value});
	}

	/**
	 * Adds a new table row for event log table
	 * @param header header text
	 * @param value value text
	 */
	void newOstTableItem(String timestamp, String text, String file, String line, String type, String contextId, String prefix, String component, String group, String id) {
		TableItem item = new TableItem(tableOstTraces, SWT.NONE);
		item.setText(new String[] {timestamp, text, file, line, type, contextId, prefix, component, group, id});
	}

	/**
	 * Packs all columns for given table
	 * @param table table which columns are to be packed
	 */
	void AutoSizeTableCells(Table table) {
		for (int i = 0; i < table.getColumnCount(); i++) {
			table.getColumn(i).pack();
		}
	}

	/**
	 * Sets context sensitive help ids to UI elements
	 */
	void setHelps() {
		PlatformUI.getWorkbench().getHelpSystem().setHelp(tableEventLog,
				HelpContextIDs.CRASH_ANALYSER_HELP_CRASH_VISUALISER);	
	}

}
