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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.resource.FontRegistry;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;

import com.nokia.s60tools.crashanalyser.containers.CodeSegment;
import com.nokia.s60tools.crashanalyser.containers.EventLog;
import com.nokia.s60tools.crashanalyser.containers.Process;
import com.nokia.s60tools.crashanalyser.containers.Register;
import com.nokia.s60tools.crashanalyser.containers.RegisterBit;
import com.nokia.s60tools.crashanalyser.containers.RegisterDetails;
import com.nokia.s60tools.crashanalyser.containers.RegisterSet;
import com.nokia.s60tools.crashanalyser.containers.Thread;
import com.nokia.s60tools.crashanalyser.files.CrashFile;
import com.nokia.s60tools.crashanalyser.files.SummaryFile;
import com.nokia.s60tools.crashanalyser.resources.HelpContextIDs;
import com.nokia.s60tools.crashanalyser.ui.viewers.CodeSegmentsTableViewer;

/**
 * Advanced page in Crash Visualiser editor. 
 *
 */
public class AdvancedPage implements SelectionListener{

	// registers group UI items
	Combo comboRegisters;
	Table tableRegisters;

	// code segments group UI items
	Table tableCodeSegments;
	CodeSegmentsTableViewer tableViewerCodeSegments;
	
	// event log group UI items
	Table tableEventLog;
	
	// CPSR details group UI items
	Group groupCpsrEmpty;
	Combo comboCpsrDetails;
	Table tableCpsrDetails;
	
	FontRegistry fontRegistry;
	SummaryFile crashFile;
	Process selectedProcess = null;
	Thread selectedThread = null;
		
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
			initialUserRegistersTableLoad();
			loadCodeSegmentsTable();
			loadEventLogTable();
			initialCpsrDetailsTableLoad();
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
		SashForm sashFormTop = new SashForm(sashFormMain, SWT.HORIZONTAL);
		createRegistersGroup(sashFormTop);
		createCodeSegmentsGroup(sashFormTop);
		SashForm sashFormBottom = new SashForm(sashFormMain, SWT.HORIZONTAL);
		createEventLogGroup(sashFormBottom);
		createCpsrDetailsGroup(sashFormBottom);
		
		sashFormTop.setWeights(new int[]{3,2});
		sashFormBottom.setWeights(new int[]{3,7});
			
		setHelps();
		
		return parent;
	}
	
	/**
	 * Creates registers group
	 * @param parent
	 */
	void createRegistersGroup(Composite parent) {
		Group groupRegisters = new Group(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		groupRegisters.setLayout(layout);
		groupRegisters.setText("Registers");
		GridData groupGD = new GridData(GridData.FILL_HORIZONTAL);
		groupRegisters.setLayoutData(groupGD);
		
		comboRegisters = new Combo(groupRegisters, SWT.BORDER | SWT.DROP_DOWN | SWT.READ_ONLY);
		GridData comboGD = new GridData();
		comboGD.horizontalSpan = 2;
		comboRegisters.setLayoutData(comboGD);
		comboRegisters.addSelectionListener(this);
		
		tableRegisters = new Table(groupRegisters, SWT.BORDER);
		tableRegisters.setHeaderVisible(true);
		tableRegisters.setLinesVisible(true);
		TableColumn col = new TableColumn(tableRegisters, SWT.LEFT);
		col.setWidth(100);
		col.setText("Register");
		col = new TableColumn(tableRegisters, SWT.LEFT);
		col.setWidth(100);
		col.setText("Value");
		col = new TableColumn(tableRegisters, SWT.LEFT);
		col.setWidth(100);
		col.setText("Symbol");
		col = new TableColumn(tableRegisters, SWT.LEFT);
		col.setWidth(100);
		col.setText("Comment");
		tableRegisters.setFont(fontRegistry.get("monospace"));

		GridData tableGd = new GridData(GridData.FILL_BOTH);
		tableGd.horizontalSpan = 2;
		tableRegisters.setLayoutData(tableGd);

		initialUserRegistersTableLoad();
		
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
	 * Creates event log group
	 * @param parent
	 */
	void createEventLogGroup(Composite parent) {
		Group groupEventLog = new Group(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.numColumns = 1;
		groupEventLog.setLayout(layout);
		groupEventLog.setText("Event Log");
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
	 * Creates cpsr details group
	 * @param parent
	 */
	void createCpsrDetailsGroup(Composite parent) {
		Group groupCpsrDetails = new Group(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.numColumns = 1;
		groupCpsrDetails.setLayout(layout);
		groupCpsrDetails.setText("CPSR Details");
		GridData groupGD = new GridData(GridData.FILL_VERTICAL);
		groupCpsrDetails.setLayoutData(groupGD);
		
		comboCpsrDetails = new Combo(groupCpsrDetails, SWT.BORDER | SWT.DROP_DOWN | SWT.READ_ONLY);
		comboCpsrDetails.addSelectionListener(this);

		// Empty group
		groupCpsrEmpty = new Group(groupCpsrDetails, SWT.NONE);
		FormLayout fl = new FormLayout();
		fl.marginRight = 5;
		fl.marginLeft = 5;
		fl.marginBottom = 5;
		groupCpsrEmpty.setLayout(fl);
		GridData groupCpsrEmptyGd = new GridData(GridData.FILL_HORIZONTAL);
		groupCpsrEmptyGd.horizontalAlignment = SWT.FILL;
		groupCpsrEmptyGd.grabExcessHorizontalSpace = true;
		groupCpsrEmpty.setLayoutData(groupCpsrEmptyGd);

		// create 32 empty text boxes
		for (int i = 0; i < 32; i++) {
			Text text = new Text(groupCpsrEmpty, SWT.READ_ONLY | SWT.BORDER | SWT.NO_FOCUS | SWT.CENTER);
			text.setFont(fontRegistry.get("monospace"));
			FormData textFd = new FormData();
			textFd.left = new FormAttachment(0,i*20);
			textFd.top = new FormAttachment(8, 15);
			textFd.width = 8;
			text.setLayoutData(textFd); 			
		}

		tableCpsrDetails = new Table(groupCpsrDetails, SWT.BORDER);
		tableCpsrDetails.setLinesVisible(true);
		tableCpsrDetails.setHeaderVisible(true);
		TableColumn col = new TableColumn(tableCpsrDetails, SWT.LEFT);
		col.setWidth(380);
		col.setText("Name");
		col = new TableColumn(tableCpsrDetails, SWT.LEFT);
		col.setWidth(220);
		col.setText("Value");
		GridData tableGd = new GridData(GridData.FILL_VERTICAL | GridData.FILL_HORIZONTAL);
		tableGd.horizontalAlignment = SWT.FILL;
		tableGd.grabExcessHorizontalSpace = true;
		tableCpsrDetails.setLayoutData(tableGd);
		tableCpsrDetails.setFont(fontRegistry.get("monospace"));
		initialCpsrDetailsTableLoad();
	}
	
	
	/**
	 * Loads register sets to combo, selects the correct set as
	 * default set and then loads set's registers to table.
	 */
	void initialUserRegistersTableLoad() {
		if (crashFile == null)
			return;

		int defaultSelectionIndex = 0;

		// show the data of crashed process
		selectedProcess = crashFile.getCrashedProcess();
		if (selectedProcess != null) {
			// current UI support only one thread, so show the first thread of first process
			selectedThread = selectedProcess.getFirstThread();
			if (selectedThread != null) {
				List<RegisterSet> registerSets = selectedThread.getRegisters();
				if (registerSets != null && !registerSets.isEmpty()) {
					// load all register sets to combo
					for (int i = 0; i < registerSets.size(); i++) {
						RegisterSet registerSet = registerSets.get(i);
						// default register to show is the one that contains CPSR register
						if (registerSet.containsCPSR()) 
							defaultSelectionIndex = i;
						comboRegisters.add(registerSet.getName());
						comboRegisters.setData(registerSet.getName(), registerSet);
					}
				}
			}
		}
		
		List<RegisterSet> standAloneRegisters = crashFile.getStandAloneRegisterSets();
		if (standAloneRegisters != null && !standAloneRegisters.isEmpty()) {
			for (int i = 0; i < standAloneRegisters.size(); i++) {
				RegisterSet regSet = standAloneRegisters.get(i);
				if (regSet.containsCPSR())
					defaultSelectionIndex = comboRegisters.getItemCount() + i;
				comboRegisters.add(regSet.getName());
				comboRegisters.setData(regSet.getName(), regSet);
			}
		}
		
		if (comboRegisters.getItemCount() > 0) {
			comboRegisters.select(defaultSelectionIndex);
			loadRegistersTable();
		}
		
	}
	
	/**
	 * Loads user registers table according to which registers set is
	 * selected in combo. 
	 */
	void loadRegistersTable() {
		tableRegisters.removeAll();
		
		if (comboRegisters.getItemCount() < 1)
			return;
		
		try {
			RegisterSet registerSet = (RegisterSet)comboRegisters.getData(comboRegisters.getText());
			List<Register> registers = registerSet.getRegisters();
			if (registers != null && !registers.isEmpty()) {
				// show all register values in selected register set
				for (int i = 0; i < registers.size(); i++) {
					Register register = registers.get(i);
					newRegistersTableItem(register);
				}
				AutoSizeTableCells(tableRegisters);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Loads code segments to table
	 */
	void loadCodeSegmentsTable() {
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
	 * Loads cprs types to combo, selects first type and then loads it's
	 * data to table.
	 */
	void initialCpsrDetailsTableLoad() {
		if (crashFile == null)
			return;

		List<RegisterDetails> registerDetails = crashFile.getRegisterDetails();
		if (registerDetails != null && !registerDetails.isEmpty()) {
			for (int i = 0; i < registerDetails.size(); i++) {
				RegisterDetails regDetails = registerDetails.get(i);
				if (regDetails.getDescription().contains("CPSR")) {
					comboCpsrDetails.add(regDetails.getDescription());
					comboCpsrDetails.setData(regDetails.getDescription(), regDetails);
				}
			}
			comboCpsrDetails.select(0);
			loadCpsrDetailsTable();
		}		
	}
	
	/**
	 * Loads currently selected cprs's details into table.
	 */
	void loadCpsrDetailsTable() {
		try {
			tableCpsrDetails.removeAll();

			RegisterDetails registerDetails = null;
			if (comboRegisters.getSelectionIndex() >= 0)
				registerDetails = (RegisterDetails)comboCpsrDetails.getData(comboCpsrDetails.getText());

			boolean bitStartFromRight = registerDetails.bitsStartFromRight();
			Map<String, String> categories = new HashMap<String, String>();
			// go through all 32 text boxes, and see if there is a bit for each box
			for (int i = 0, j = 31; i < 32; i++, j--) {
				Text box = (Text)groupCpsrEmpty.getChildren().clone()[j];
				// bits can start from the right or left (endianess)
				if (!bitStartFromRight)
					box = (Text)groupCpsrEmpty.getChildren().clone()[i];
				RegisterBit bit = registerDetails.getBit(i);
				// there's no bit for this box
				if (bit == null) {
					box.setText("");
					box.setToolTipText("");
				// there is a bit for this box
				} else {
					String regChar = bit.getRegisterChar();
					String category = bit.getCategory();
					String interpretation = bit.getInterpretation();
					// if this bit contains a longer description, it should be shown in the table also
					if (!"".equals(interpretation) && !categories.containsKey(category)) {
						categories.put(category, interpretation);
					}
					box.setText(regChar);
					box.setToolTipText(category);
				}
			}
			
			// load table
			for (int i = 0; i < categories.size(); i++) {
				newCpsrDetailsTableItem(categories.keySet().toArray()[i].toString(),
										categories.values().toArray()[i].toString());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Adds a new table row for registers table
	 * @param register row data
	 */
	void newRegistersTableItem(Register register) {
		TableItem item = new TableItem(tableRegisters, SWT.NONE);
		item.setText(new String[] {register.getName(),
									register.getValue(),
									register.getSymbol(),
									register.getComments()});
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
	 * Adds a new table row for event log table
	 * @param header header text
	 * @param value value text
	 */
	void newEventTableItem(String header, String value) {
		TableItem item = new TableItem(tableEventLog, SWT.NONE);
		item.setText(new String[] {header, value});
	}

	/**
	 * Adds a new table row for cpsr details table
	 * @param header header text
	 * @param value value text
	 */
	void newCpsrDetailsTableItem(String header, String value) {
		TableItem item = new TableItem(tableCpsrDetails, SWT.NONE);
		item.setText(new String[] {header, value});
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
	
	public void widgetDefaultSelected(SelectionEvent arg0) {
		// No implementation needed
	}

	public void widgetSelected(SelectionEvent event) {
		// cpsr was changed in combo
		if (event.widget == comboCpsrDetails) {
			loadCpsrDetailsTable();
		// register set was changed in combo
		} else if (event.widget == comboRegisters) {
			loadRegistersTable();
		}
	}
	
	/**
	 * Sets context sensitive help ids to UI elements
	 */
	void setHelps() {
		PlatformUI.getWorkbench().getHelpSystem().setHelp(tableCodeSegments,
				HelpContextIDs.CRASH_ANALYSER_HELP_CRASH_VISUALISER);	
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
	}
}
