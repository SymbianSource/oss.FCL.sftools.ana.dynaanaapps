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
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.PlatformUI;

import com.nokia.s60tools.crashanalyser.containers.CodeSegment;
import com.nokia.s60tools.crashanalyser.containers.Process;
//import com.nokia.s60tools.crashanalyser.containers.Thread;
import com.nokia.s60tools.crashanalyser.files.CrashFile;
import com.nokia.s60tools.crashanalyser.files.SummaryFile;
import com.nokia.s60tools.crashanalyser.resources.HelpContextIDs;
import com.nokia.s60tools.crashanalyser.ui.viewers.CodeSegmentsTableViewer;

public class CodesegmentsPage {

	// code segments group UI items
	Table tableCodeSegments;
	CodeSegmentsTableViewer tableViewerCodeSegments;

	FontRegistry fontRegistry;
	SummaryFile crashFile;
	Process selectedProcess = null;
//	Thread selectedThread = null;

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
//			initialUserRegistersTableLoad();
			loadCodeSegmentsTable();
//			loadEventLogTable();
//			initialCpsrDetailsTableLoad();
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
		createCodeSegmentsGroup(sashFormMain);
		
		/*
		SashForm sashFormTop = new SashForm(sashFormMain, SWT.HORIZONTAL);
		createRegistersGroup(sashFormTop);
		createCodeSegmentsGroup(sashFormTop);
		SashForm sashFormBottom = new SashForm(sashFormMain, SWT.HORIZONTAL);
		createEventLogGroup(sashFormBottom);
		createCpsrDetailsGroup(sashFormBottom);
		
		sashFormTop.setWeights(new int[]{3,2});
		sashFormBottom.setWeights(new int[]{3,7});
			*/
		setHelps();
		
		return parent;
	}
	
	
	/**
	 * Creates code segments group
	 * @param parent
	 */
	void createCodeSegmentsGroup(Composite parent) {
		Group groupCodeSegments = new Group(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.numColumns = 1;
		groupCodeSegments.setLayout(layout);
		groupCodeSegments.setText("Code Segments");
		GridData groupGD = new GridData();
		groupCodeSegments.setLayoutData(groupGD);
		
		tableCodeSegments = new Table(groupCodeSegments, SWT.BORDER);
		tableCodeSegments.setHeaderVisible(true);
		GridData tableGd = new GridData(GridData.FILL_BOTH);
		tableCodeSegments.setLayoutData(tableGd);
		
		tableViewerCodeSegments = new CodeSegmentsTableViewer(tableCodeSegments);
		tableCodeSegments.setFont(fontRegistry.get("monospace"));
		
		loadCodeSegmentsTable();
		AutoSizeTableCells(tableCodeSegments);
		
	}

	/**
	 * Loads code segments to table
	 */
	void loadCodeSegmentsTable() {
		if (crashFile == null) {
			return;
		}
		
		if (crashFile.getThread() != null) {
			selectedProcess = crashFile.getProcessByThread(crashFile.getThread().getId());
		} else {
			selectedProcess = crashFile.getCrashedProcess();
		}
		
		if (selectedProcess == null)
			return;
		
		List<CodeSegment> codeSegments = selectedProcess.getCodeSegments();
		if (codeSegments != null && !codeSegments.isEmpty()) {
			for (int i = 0; i < codeSegments.size(); i++) {
				CodeSegment codeSegment = codeSegments.get(i);
				newCodeSegmentsTableItem(codeSegment);
			}
		}
		
		AutoSizeTableCells(tableCodeSegments);
	}

	/**
	 * Adds a new table row for code segments table
	 * @param codeSegment
	 */
	void newCodeSegmentsTableItem(CodeSegment codeSegment) {
		TableItem item = new TableItem(tableCodeSegments, SWT.NONE);
		String segmentRange = codeSegment.getSegmentRange();
		String base = "";
		String top = "";
		try {
			// segment range is of format: XXXXX-YYYYY, parse this to XXXXX and YYYYY
			if (segmentRange.contains("-")) {
				top = segmentRange.substring(segmentRange.indexOf('-')+1);
				base = segmentRange.substring(0, segmentRange.indexOf('-'));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		item.setText(new String[] {base, top, codeSegment.getSegmentName()});
		
		// code segment should be highlighted if there was some problem with the code section
		// while creating data
		if (codeSegment.shouldBeHighlighted()) {
			item.setForeground(Display.getCurrent().getSystemColor(SWT.COLOR_MAGENTA));
		}
		item.setData(codeSegment);
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
		PlatformUI.getWorkbench().getHelpSystem().setHelp(tableCodeSegments,
				HelpContextIDs.CRASH_ANALYSER_HELP_CRASH_VISUALISER);	
/*
		PlatformUI.getWorkbench().getHelpSystem().setHelp(tableCpsrDetails,
				HelpContextIDs.CRASH_ANALYSER_HELP_CRASH_VISUALISER);	
		PlatformUI.getWorkbench().getHelpSystem().setHelp(tableEventLog,
				HelpContextIDs.CRASH_ANALYSER_HELP_CRASH_VISUALISER);	
		PlatformUI.getWorkbench().getHelpSystem().setHelp(tableRegisters,
				HelpContextIDs.CRASH_ANALYSER_HELP_CRASH_VISUALISER);	
		PlatformUI.getWorkbench().getHelpSystem().setHelp(tableEventLog,
				HelpContextIDs.CRASH_ANALYSER_HELP_CRASH_VISUALISER);	
		PlatformUI.getWorkbench().getHelpSystem().setHelp(tableRegisters,
				HelpContextIDs.CRASH_ANALYSER_HELP_CRASH_VISUALISER);	
		PlatformUI.getWorkbench().getHelpSystem().setHelp(comboCpsrDetails,
				HelpContextIDs.CRASH_ANALYSER_HELP_CRASH_VISUALISER);	
		PlatformUI.getWorkbench().getHelpSystem().setHelp(comboRegisters,
				HelpContextIDs.CRASH_ANALYSER_HELP_CRASH_VISUALISER);	
	*/
	}

}
