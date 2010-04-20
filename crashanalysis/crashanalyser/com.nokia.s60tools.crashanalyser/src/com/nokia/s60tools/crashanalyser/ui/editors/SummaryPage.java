/*
* Copyright (c) 2008 Nokia Corporation and/or its subsidiary(-ies). 
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

import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.browser.*;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.TableCursor;
import org.eclipse.ui.PlatformUI;

import com.nokia.s60tools.crashanalyser.files.*;
import com.nokia.s60tools.crashanalyser.files.SummaryFile.ContentType;
import com.nokia.s60tools.crashanalyser.model.HtmlFormatter;
import com.nokia.s60tools.crashanalyser.containers.Summary;
import com.nokia.s60tools.crashanalyser.containers.Process;
import com.nokia.s60tools.crashanalyser.containers.Thread;
import com.nokia.s60tools.crashanalyser.containers.Stack;
import com.nokia.s60tools.crashanalyser.containers.RegisterSet;
import com.nokia.s60tools.crashanalyser.resources.HelpContextIDs;

import java.util.List;
import org.eclipse.jface.resource.FontRegistry;
import org.eclipse.swt.graphics.FontData;

/**
 * Crash Data page in Crash Visualiser editor. 
 *
 */
public class SummaryPage {
	
	// summary group UI items
	Table tableSummary;

	// exit info group UI items
	Label labelExitType;
	Label labelPanicSummary;
	Browser browserPanicDescription;
		
	SummaryFile crashFile = null;
	FontRegistry fontRegistry;
	Thread selectedThread = null;
	
	/**
	 * Creates the page
	 * @param parent composite
	 * @param file summary file
	 * @return composite
	 */
	public Composite createPage(Composite parent, SummaryFile file) {
		crashFile = file;
		return doCreate(parent);
	}
	
	/**
	 * Creates the page
	 * @param parent composite
	 * @return composite
	 */
	public Composite createPage(Composite parent) {
		return doCreate(parent);
	}
	
	public void update() {
	//	AutoSizeCallStackTableCells();
	}
	
	/**
	 * Loads data from given file into UI elements.
	 * @param file crash file
	 */
	public void setFile(CrashFile file) {
		if (file != null) {
			crashFile = file;
			loadSummaryTable();
			loadExitInfo();
		}
	}
	
	/**
	 * Creates all UI elements to the page
	 * @param parent
	 * @return composite
	 */
	Composite doCreate(Composite parent) {
		GridLayout layout = new GridLayout();
		layout.numColumns = 1;
		parent.setLayout(layout);
		parent.setLayoutData(new GridData(GridData.FILL_BOTH));
		fontRegistry = new FontRegistry(Display.getCurrent());
		fontRegistry.put("monospace", new FontData[]{new FontData("Courier", 8, SWT.NORMAL)});
		SashForm sashFormMain = new SashForm(parent, SWT.VERTICAL);
		sashFormMain.setLayoutData(new GridData(GridData.FILL_BOTH));
		createSummaryGroup(sashFormMain);
		createExitInfoGroup(sashFormMain);
		
		setHelps();
		
		return parent;
	}
	
	/**
	 * Creates summary group
	 * @param parent
	 */
	void createSummaryGroup(Composite parent) {
		Group groupSummary = new Group(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.numColumns = 1;
		groupSummary.setLayout(layout);
		groupSummary.setText("General");
		GridData groupGD = new GridData(GridData.FILL_HORIZONTAL);
		groupGD.heightHint = 200;
		groupSummary.setLayoutData(groupGD);
		
		tableSummary = new Table(groupSummary, SWT.BORDER  | SWT.FULL_SELECTION |  
				SWT.V_SCROLL | SWT.H_SCROLL | SWT.READ_ONLY );
		tableSummary.setHeaderVisible(true);
		tableSummary.setLinesVisible(true);
		TableColumn col1 = new TableColumn(tableSummary, SWT.LEFT);
		col1.setWidth(130);
		TableColumn col2 = new TableColumn(tableSummary, SWT.LEFT);
		col2.setWidth(300);
		tableSummary.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		tableSummary.setFont(fontRegistry.get("monospace"));
		
		
		final TableCursor cursor = new TableCursor(tableSummary, SWT.NONE);
		TableKeyListener keyListener = new TableKeyListener(tableSummary, cursor);
		tableSummary.addKeyListener(keyListener);
		cursor.addKeyListener(keyListener);
                        
		loadSummaryTable();		
	}
	
	/**
	 * Creates exit info group
	 * @param parent
	 */
	void createExitInfoGroup(Composite parent) {
		Group groupExitInfo = new Group(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.numColumns = 1;
		groupExitInfo.setLayout(layout);
		groupExitInfo.setText("Exit Info");
		GridData groupGD = new GridData(GridData.FILL_HORIZONTAL);
		groupGD.heightHint = 200;
		groupExitInfo.setLayoutData(groupGD);
		
		labelExitType = new Label(groupExitInfo, SWT.NONE);
		labelExitType.setText("Exit Type:");
		labelExitType.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		labelPanicSummary = new Label(groupExitInfo, SWT.BORDER);
		labelPanicSummary.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		browserPanicDescription = new Browser(groupExitInfo, SWT.BORDER);
		browserPanicDescription.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		loadExitInfo();
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
	 * Loads data into summary table
	 */
	void loadSummaryTable() {
		// if there is no crashFile given, we are waiting for data to be loaded.
		// Show 'Loading file' text in summary table until we get data.
		if (crashFile == null) {
			TableItem item = new TableItem(tableSummary, SWT.NONE);
			item.setText(new String[] {"Loading file. Please wait.", ""});
		// we have crash data, load summary table
		} else {
			tableSummary.removeAll();
		
			// show the data of crashed process
			Process process = null;
			if (crashFile.getThread() != null) {
				process = crashFile.getProcessByThread(crashFile.getThread().getId());
			} else {
				process = crashFile.getCrashedProcess();
			}
			
			if (process != null) {
				newSummaryTableItem("PROCESS", process.getName(), true);
				
				// current UI support only one thread, so show the first thread of first process
				Thread thread = null;
				if (crashFile.getThread() != null) {
					// Show only thread information (no crash info)
					thread = crashFile.getThread();
				} else {
					thread = crashFile.getCrashedThread();
				}
				
				if (thread != null) {
					selectedThread = thread;
					newSummaryTableItem("THREAD", thread.getFullName(), true);
					newSummaryTableItem("STACK POINTER", thread.getStackPointer(), true);
					newSummaryTableItem("LINK REGISTER", thread.getLinkRegister(), true);
					newSummaryTableItem("PROGRAM COUNTER", thread.getProgramCounter(), true);
				}
			}
			
			Summary crashSummary = crashFile.getSummary();
			if (crashSummary != null) {
				// there can be several software version informations, show them all
				String[] versions = crashSummary.getSwVersion();
				if (versions != null && versions.length > 0) {
					for (int i = 0; i < versions.length; i++) {
						newSummaryTableItem("SW VERSION", versions[i], false);
					}
				}
				// there can be several hardware version informations, show them all
				versions = crashSummary.getHwVersion();
				if (versions != null && versions.length > 0) {
					for (int i = 0; i < versions.length; i++) {
						newSummaryTableItem("HW VERSION", versions[i], false);
					}
				}
				newSummaryTableItem("PRODUCT TYPE", crashSummary.getProductType(), true);
				newSummaryTableItem("PRODUCT CODE", crashSummary.getProductCode(), true);
				newSummaryTableItem("LANGUAGE", crashSummary.getLanguage(), true);
				newSummaryTableItem("IMEI", crashSummary.getImei(), true);
				newSummaryTableItem("CRASH TIME", crashSummary.getCrashTime(), true);
				newSummaryTableItem("CRASH DATE", crashSummary.getCrashDate(), true);
				newSummaryTableItem("ROM ID", crashSummary.getRomId(), true);
				newSummaryTableItem("AVAILABLE MEMORY", crashSummary.getFreeRam(), true);
				newSummaryTableItem("PSN", crashSummary.getSerialNumber(), true);
				newSummaryTableItem("UPTIME", crashSummary.getUpTime(), true);
				newSummaryTableItem("MOBILECRASH CONFIGURATION", crashSummary.getProductionMode(), true);
				newSummaryTableItem("CRASH SOURCE", crashSummary.getCrashSource(), true);
				newSummaryTableItem("FREE DISK SPACE", crashSummary.getFreeDisk(), true);
				
				// Print defect hash if exist
				List<Stack> stacks = null;
				if (selectedThread != null)
					stacks = selectedThread.getStacks();
				
				if (stacks != null && !stacks.isEmpty()) 
				{
					String defectHash = "";
					for(Stack stack: stacks) {
						defectHash = stack.getHash();
						if(! "".equals(defectHash)) {
							newSummaryTableItem("DEFECT HASH", defectHash, true);
						}
					}
				}
			}
			
			// stand alone stacks don't belong to a thread, check if there are any 
			// of these, and print SP, LR and PC if needed
			List<Stack> standAloneStacks = crashFile.getStandAloneStacks();
			if (standAloneStacks != null && !standAloneStacks.isEmpty()) {
				for (int i = 0; i < standAloneStacks.size(); i++) {
					Stack stack = standAloneStacks.get(i);
					if (stack.stackRegisterContainsCpsr()) {
						newSummaryTableItem("STACK POINTER", stack.getStackPointer(), true);
						newSummaryTableItem("LINK REGISTER", stack.getLinkRegister(), true);
						newSummaryTableItem("PROGRAM COUNTER", stack.getProgramCounter(), true);
					}
				}
			}
			
			// stand alone registers don't belong to a thread, check if there are any 
			// of these, and print SP, LR and PC if needed
			List<RegisterSet> standAloneRegisterSets = crashFile.getStandAloneRegisterSets();
			if (standAloneRegisterSets != null && !standAloneRegisterSets.isEmpty()) {
				for (int i = 0; i < standAloneRegisterSets.size(); i++) {
					RegisterSet regSet = standAloneRegisterSets.get(i);
					if (regSet.containsCPSR()) {
						newSummaryTableItem("STACK POINTER", regSet.getStackPointer(), true);
						newSummaryTableItem("LINK REGISTER", regSet.getLinkRegister(), true);
						newSummaryTableItem("PROGRAM COUNTER", regSet.getProgramCounter(), true);
					}
				}
			}
			
			AutoSizeTableCells(tableSummary);
		}
	}
	
	/**
	 * Load exit information
	 */
	void loadExitInfo() {
		if (crashFile == null)
			return;
					
		String panicSummary = "";
		String panicDescription = "";

		if (crashFile.getContentType() == ContentType.REGMSG) {
			labelPanicSummary.setText("Registration message");
			panicDescription = HtmlFormatter.formatRegistrationMessage();
			browserPanicDescription.setText(HtmlFormatter.formatHtmlStyle(labelPanicSummary.getFont(), 
					panicDescription));
			return;
		} else if (crashFile.getContentType() == ContentType.REPORT) {
			labelPanicSummary.setText("Report");
			panicDescription = HtmlFormatter.formatReport();
			browserPanicDescription.setText(HtmlFormatter.formatHtmlStyle(labelPanicSummary.getFont(), 
					panicDescription));
			return;
		}
		
		if (selectedThread != null && !"".equals(selectedThread.getExitType())) {
			panicDescription = selectedThread.getPanicDescription();
			panicSummary = selectedThread.getExitType();
			if (!"".equals(selectedThread.getExitCategory()) && !"".equals(selectedThread.getExitReason())) {
				// if crash was an exception
				if (selectedThread.getExitType().equals("Exception")) {
					panicSummary += ": " + selectedThread.getExitReason();
				// crash was a panic
				} else {
					panicSummary += ": " + selectedThread.getExitCategory() + " - " + selectedThread.getExitReason();
				}
			}
		}
		
		// we could not find panic data
		if ("".equals(panicSummary.trim())) {
			panicSummary = "Unknown";
		}
		labelPanicSummary.setText(panicSummary);
		if ("".equals(panicDescription.trim()))
			panicDescription = HtmlFormatter.formatUnknownPanicMessage(panicSummary); 

		browserPanicDescription.setText(HtmlFormatter.formatHtmlStyle(labelPanicSummary.getFont(), 
																		panicDescription));
	}
	
	public void widgetDefaultSelected(SelectionEvent arg0) {
		// no implementation required
	}

	
	/**
	 * Adds new summary table row
	 * @param header header text
	 * @param value value text
	 */
	boolean newSummaryTableItem(String header, String value, boolean doNotDuplicate) {
		if (!"".equals(value)) {
			if (doNotDuplicate) {
				for (int i = 0; i < tableSummary.getItemCount(); i++) {
					TableItem it = tableSummary.getItem(i);
					if (it.getText(0).equalsIgnoreCase(header))
						return false;
				}
			}
			TableItem item = new TableItem(tableSummary, SWT.NONE);
			item.setText(new String[] {header, value});
			return true;
		}
		
		return false;
	}
	
	/**
	 * Sets context sensitive help ids to UI elements
	 */
	void setHelps() {
		PlatformUI.getWorkbench().getHelpSystem().setHelp(tableSummary,
				HelpContextIDs.CRASH_ANALYSER_HELP_CRASH_VISUALISER);	
		PlatformUI.getWorkbench().getHelpSystem().setHelp(browserPanicDescription,
				HelpContextIDs.CRASH_ANALYSER_HELP_CRASH_VISUALISER);	
	}
}
