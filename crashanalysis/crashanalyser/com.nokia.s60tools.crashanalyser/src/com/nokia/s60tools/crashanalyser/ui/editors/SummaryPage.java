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
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.browser.*;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.ui.PlatformUI;

import com.nokia.s60tools.crashanalyser.files.*;
import com.nokia.s60tools.crashanalyser.model.HtmlFormatter;
import com.nokia.s60tools.crashanalyser.containers.Summary;
import com.nokia.s60tools.crashanalyser.containers.Process;
import com.nokia.s60tools.crashanalyser.containers.Thread;
import com.nokia.s60tools.crashanalyser.containers.Stack;
import com.nokia.s60tools.crashanalyser.containers.StackEntry;
import com.nokia.s60tools.crashanalyser.containers.RegisterSet;
import com.nokia.s60tools.crashanalyser.resources.HelpContextIDs;
import com.nokia.s60tools.crashanalyser.ui.viewers.*;
import com.nokia.s60tools.crashanalyser.ui.views.*;
import java.util.List;
import org.eclipse.jface.resource.FontRegistry;
import org.eclipse.swt.graphics.FontData;

/**
 * Crash Data page in Crash Visualiser editor. 
 *
 */
public class SummaryPage implements SelectionListener {
	
	// summary group UI items
	Table tableSummary;

	// exit info group UI items
	Label labelExitType;
	Label labelPanicSummary;
	Browser browserPanicDescription;
	
	// call stack group UI items
	Combo comboCallStack;
	Label labelStackWarning;
	Button buttonDecodeFile;
	Table tableCallStack;
	CallStackTableViewer tableViewerCallStack;
	Button buttonAllStackEntries;
	Button buttonSymbolStackEntries;
	
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
		AutoSizeCallStackTableCells();
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
			initialCallStackTableLoad();
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
		SashForm sashFormTop = new SashForm(sashFormMain, SWT.HORIZONTAL);
		createSummaryGroup(sashFormTop);
		createExitInfoGroup(sashFormTop);
		createCallStackGroup(sashFormMain);

		sashFormMain.setWeights(new int[] {1,2});
		
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
		groupSummary.setText("Summary");
		GridData groupGD = new GridData(GridData.FILL_HORIZONTAL);
		groupGD.heightHint = 200;
		groupSummary.setLayoutData(groupGD);
		
		tableSummary = new Table(groupSummary, SWT.BORDER);
		tableSummary.setHeaderVisible(true);
		tableSummary.setLinesVisible(true);
		TableColumn col1 = new TableColumn(tableSummary, SWT.LEFT);
		col1.setWidth(130);
		TableColumn col2 = new TableColumn(tableSummary, SWT.LEFT);
		col2.setWidth(300);
		tableSummary.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		tableSummary.setFont(fontRegistry.get("monospace"));

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
	 * Creates call stack group
	 * @param parent
	 */
	void createCallStackGroup(Composite parent) {
		Group groupCallStack = new Group(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.numColumns = 3;
		groupCallStack.setLayout(layout);
		GridData groupGD = new GridData(GridData.FILL_BOTH);
		groupGD.horizontalSpan = 3;
		groupCallStack.setText("Call Stack");
		groupCallStack.setLayoutData(groupGD);
		
		comboCallStack = new Combo(groupCallStack, SWT.BORDER | SWT.DROP_DOWN | SWT.READ_ONLY);
		comboCallStack.addSelectionListener(this);
		
		labelStackWarning = new Label(groupCallStack, SWT.NONE);
		labelStackWarning.setText("(selected stack is build with heuristic algorithm)");
		labelStackWarning.setVisible(false);
		
		buttonDecodeFile = new Button(groupCallStack, SWT.PUSH);
		buttonDecodeFile.setText("Decode");
		buttonDecodeFile.setVisible(false);
		buttonDecodeFile.addSelectionListener(this);

		tableCallStack = new Table(groupCallStack, SWT.MULTI | SWT.BORDER | SWT.FULL_SELECTION | 
													SWT.V_SCROLL | SWT.H_SCROLL);
		tableCallStack.setHeaderVisible(true);
		tableCallStack.setFont(fontRegistry.get("monospace"));
		
		GridData tableGD = new GridData(GridData.FILL_BOTH);
		tableGD.horizontalSpan = 3;
		tableCallStack.setLayoutData(tableGD);

		GridData buttonGD = new GridData(GridData.FILL_HORIZONTAL);
		buttonGD.horizontalSpan = 3;

		buttonAllStackEntries = new Button(groupCallStack, SWT.RADIO);
		buttonAllStackEntries.setText("Show all stack entries");
		buttonAllStackEntries.addSelectionListener(this);
		buttonAllStackEntries.setLayoutData(buttonGD);
		
		buttonSymbolStackEntries = new Button(groupCallStack, SWT.RADIO);
		buttonSymbolStackEntries.setText("Show stack entries which have associated symbols");
		buttonSymbolStackEntries.addSelectionListener(this);
		buttonSymbolStackEntries.setLayoutData(buttonGD);
		buttonSymbolStackEntries.setSelection(true);
		
		tableViewerCallStack = new CallStackTableViewer(tableCallStack);

		initialCallStackTableLoad();
		AutoSizeCallStackTableCells();
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
	 * Packs columns in call stack table nicely
	 */
	void AutoSizeCallStackTableCells() {
		int tableWidth = tableCallStack.getBounds().width;
		tableCallStack.getColumn(CallStackTableViewer.COLUMN_ADDRESS).setWidth(80);
		tableCallStack.getColumn(CallStackTableViewer.COLUMN_VALUE).setWidth(80);
		tableCallStack.getColumn(CallStackTableViewer.COLUMN_OFFSET).setWidth(65);
		tableCallStack.getColumn(CallStackTableViewer.COLUMN_TEXT).setWidth(60);
		int space = tableWidth - 80 - 80 - 65 - 60 - 25;
		int object = space / 3;
		if (object < 70)
			object = 70;
		tableCallStack.getColumn(CallStackTableViewer.COLUMN_OBJECT).setWidth(object);
		tableCallStack.getColumn(CallStackTableViewer.COLUMN_SYMBOL).setWidth(object*2);
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
			Process process = crashFile.getCrashedProcess();
			if (process != null) {
				newSummaryTableItem("PROCESS", process.getName(), true);
				
				// current UI support only one thread, so show the first thread of first process
				Thread thread = process.getFirstThread();
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
				newSummaryTableItem("CRASH TIME", crashSummary.getCrashTime(), true);
				newSummaryTableItem("CRASH DATE", crashSummary.getCrashDate(), true);
				newSummaryTableItem("UPTIME", crashSummary.getUpTime(), true);
				newSummaryTableItem("ROM ID", crashSummary.getRomId(), true);
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
				newSummaryTableItem("SERIAL NUMBER", crashSummary.getSerialNumber(), true);
				newSummaryTableItem("IMEI", crashSummary.getImei(), true);
				newSummaryTableItem("FREE RAM", crashSummary.getFreeRam(), true);
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
	
	/**
	 * Loads stacks to combo, selects the correct stack as
	 * default set and then loads stack's data to table.
	 */
	void initialCallStackTableLoad() {
		int selectedIndex = 0;
		if (selectedThread != null) {
			List<Stack> stacks = selectedThread.getStacks();
			if (stacks != null && !stacks.isEmpty()) {
				// add all stacks into combo
				for (int i = 0; i < stacks.size(); i++) {
					Stack stack = stacks.get(i);
					// show stack which contains CPSR register as default
					if (stack.stackRegisterContainsCpsr()) 
						selectedIndex  = i;
					comboCallStack.add(stack.getStackType());
					comboCallStack.setData(stack.getStackType(), stack);
				}
			}
		}
		
		List<Stack> standAloneStacks = crashFile.getStandAloneStacks();
		if (standAloneStacks != null && !standAloneStacks.isEmpty()) {
			for (int i = 0; i < standAloneStacks.size(); i++) {
				Stack stack = standAloneStacks.get(i);
				if (stack.stackRegisterContainsCpsr())
					selectedIndex = comboCallStack.getItemCount() + i;
				comboCallStack.add(stack.getStackType());
				comboCallStack.setData(stack.getStackType(), stack);
			}
		}

		if (comboCallStack.getItemCount() > 0) {
			comboCallStack.select(selectedIndex);
			loadCallStackTable(true);
		}
	}
	
	/**
	 * Loads call stack table according to which stack is
	 * selected in combo. 
	 */
	void loadCallStackTable(boolean autoSizeCells) {
		tableCallStack.removeAll();
		labelStackWarning.setVisible(false);
		
		if (comboCallStack.getItemCount() < 1)
			return;
		
		try {
			Stack stack = (Stack)comboCallStack.getData(comboCallStack.getText());
			boolean containsAccurate = stack.containsAccurateStackEntries();
			labelStackWarning.setVisible(!containsAccurate);
			List<StackEntry> stackEntries = stack.getStackEntries();
			if (stackEntries != null && !stackEntries.isEmpty()) {
				for (int i = 0; i < stackEntries.size(); i++) {
					StackEntry stackEntry = stackEntries.get(i);
					newStackTableItem(stackEntry, containsAccurate);
				}
				if (autoSizeCells)
					AutoSizeCallStackTableCells();
			}
			
			// nothing in stack
			if (stackEntries == null || stackEntries.isEmpty()) {
				disableStack();
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Disables stack UI elements
	 */
	void disableStack() {
		labelStackWarning.setVisible(true);
		labelStackWarning.setText("Symbols were not available while creating stack.");
		buttonDecodeFile.setVisible(true);
		comboCallStack.setEnabled(false);
		buttonAllStackEntries.setEnabled(false);
		buttonSymbolStackEntries.setEnabled(false);
		tableCallStack.setEnabled(false);
	}
	
	public void widgetDefaultSelected(SelectionEvent arg0) {
		// no implementation required
	}

	public void widgetSelected(SelectionEvent event) {
		// stack was changed in combo
		if (event.widget == comboCallStack) {
			loadCallStackTable(true);
		// stack radio button changes
		} else if (event.widget == buttonAllStackEntries || 
					event.widget == buttonSymbolStackEntries) {
			loadCallStackTable(false);
		// decode button was pressed
		} else if (event.widget == buttonDecodeFile) {
			MainView mv = MainView.showAndReturnYourself(true);
			mv.decodeFile(crashFile);
		}
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
	 * Adds new stack table row.
	 * @param stackEntry row data
	 * @param stackContainsAccurateEntries affects row colorings
	 */
	void newStackTableItem(StackEntry stackEntry, boolean stackContainsAccurateEntries) {
		// if 'Show stack entries which have associated symbols' radio button is selected and
		// if stack contains accurate stack entries, and this stack entry is not accurate
		// don't add it to table (unless this stack entry is current stack pointer or register based)
		if (buttonSymbolStackEntries.getSelection() &&
			stackContainsAccurateEntries && 
			!stackEntry.accurate() && 
			!stackEntry.currentStackPointer() &&
			!stackEntry.registerBased()) {
			return;
		// if 'Show stack entries which have associated symbols' radio button is selected
		// if stack doesn't contain accurate stack entries, don't show current stack entry
		// if it doesn't have associated symbol (unless this stack entry is current stack pointer or register based)
		} else if (buttonSymbolStackEntries.getSelection() &&
					!stackContainsAccurateEntries &&
					"".equals(stackEntry.getSymbol()) &&
					!stackEntry.currentStackPointer() &&
					!stackEntry.registerBased())
			return;

		TableItem item = new TableItem(tableCallStack, SWT.NONE);
		item.setText(new String[] {stackEntry.getAddress(),
									stackEntry.getSymbol(),
									stackEntry.getValue(),
									stackEntry.getOffset(),
									stackEntry.getObject(),
									stackEntry.getText()});
		
		item.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));
		item.setForeground(Display.getCurrent().getSystemColor(SWT.COLOR_BLACK));
		
		// item is current stack pointer FG black, BG light pink
		if (stackEntry.currentStackPointer()) {
			item.setBackground(new Color(Display.getCurrent(), 255, 182, 193)); // light pink
		// PC or LR
		} else if (stackEntry.registerBased()) {
			item.setBackground(new Color(Display.getCurrent(), 173, 216, 230)); // light blue
		// item is out of stack bound FG gray, BG white
		} else if (stackEntry.outsideStackBounds()) {
			item.setForeground(Display.getCurrent().getSystemColor(SWT.COLOR_GRAY));
		// item has no symbols, FG black, BG white
		} else if ("".equals(stackEntry.getSymbol())) {
		} else
			// if stack contains accurate entries, show "ghost" entries as gray
			if (stackContainsAccurateEntries) {
				// color accurate stack entries black or blue
				if (stackEntry.accurate()) {
					// ram loaded code (instead of execute-in-place code) FG blue
					if (!stackEntry.xip()) {
						item.setForeground(Display.getCurrent().getSystemColor(SWT.COLOR_DARK_BLUE));
					}
				// not accurate stack entry FG gray
				} else {
					item.setForeground(Display.getCurrent().getSystemColor(SWT.COLOR_GRAY));
				}
			// stack isn't accurate,
			} else {
				// ram loaded code (instead of execute-in-place code)
				if (!stackEntry.xip()) {
					item.setForeground(Display.getCurrent().getSystemColor(SWT.COLOR_DARK_BLUE));
			}
		}
		
		item.setData(stackEntry);
	}
	
	/**
	 * Sets context sensitive help ids to UI elements
	 */
	void setHelps() {
		PlatformUI.getWorkbench().getHelpSystem().setHelp(tableCallStack,
				HelpContextIDs.CRASH_ANALYSER_HELP_CRASH_VISUALISER);	
		PlatformUI.getWorkbench().getHelpSystem().setHelp(tableSummary,
				HelpContextIDs.CRASH_ANALYSER_HELP_CRASH_VISUALISER);	
		PlatformUI.getWorkbench().getHelpSystem().setHelp(comboCallStack,
				HelpContextIDs.CRASH_ANALYSER_HELP_CRASH_VISUALISER);	
		PlatformUI.getWorkbench().getHelpSystem().setHelp(buttonAllStackEntries,
				HelpContextIDs.CRASH_ANALYSER_HELP_CRASH_VISUALISER);	
		PlatformUI.getWorkbench().getHelpSystem().setHelp(buttonDecodeFile,
				HelpContextIDs.CRASH_ANALYSER_HELP_CRASH_VISUALISER);	
		PlatformUI.getWorkbench().getHelpSystem().setHelp(buttonSymbolStackEntries,
				HelpContextIDs.CRASH_ANALYSER_HELP_CRASH_VISUALISER);	
		PlatformUI.getWorkbench().getHelpSystem().setHelp(browserPanicDescription,
				HelpContextIDs.CRASH_ANALYSER_HELP_CRASH_VISUALISER);	
	}
}
