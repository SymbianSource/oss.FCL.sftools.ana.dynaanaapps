/*
 * Copyright (c) 2009 Nokia Corporation and/or its subsidiary(-ies). 
 * All rights reserved.
 * This component and the accompanying materials are made available
 * under the terms of the License "Eclipse Public License v1.0"
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

/**
 * 
 */
package com.nokia.carbide.cpp.pi.call;

//import java.awt.Color;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.MouseEvent;
import java.text.DecimalFormat;
import java.util.Vector;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IContributionManager;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.SubMenuManager;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.ide.IIDEActionConstants;

import com.nokia.carbide.cpp.internal.pi.interfaces.ISaveSamples;
import com.nokia.carbide.cpp.internal.pi.interfaces.ISaveTable;
import com.nokia.carbide.cpp.internal.pi.save.SaveTableWizard;
import com.nokia.carbide.cpp.internal.pi.visual.GenericTable;
import com.nokia.carbide.cpp.pi.editors.PIPageEditor;
import com.nokia.carbide.cpp.pi.util.SourceLookup;


public class CallVisualiser extends GenericTable
{
	private static final long serialVersionUID = 1L;

	// table column IDs in hopes it's quicker than comparing characters
	protected static final int COLUMN_ID_IS_CALLED       = 100;
	protected static final int COLUMN_ID_IS_CALLER       = 101;
	protected static final int COLUMN_ID_RECURSIVE_CALL  = 102;
	protected static final int COLUMN_ID_CALLER_PERCENT  = 103;
	protected static final int COLUMN_ID_CALLEE_PERCENT  = 104;
	protected static final int COLUMN_ID_IS_CALLED_COUNT = 105;
	protected static final int COLUMN_ID_IS_CALLER_COUNT = 106;
	
	// table column headings
	protected static final String COLUMN_HEAD_IS_CALLED       = Messages.getString("CallVisualiser.isCalled"); //$NON-NLS-1$
	protected static final String COLUMN_HEAD_IS_CALLER       = Messages.getString("CallVisualiser.isCaller"); //$NON-NLS-1$
	protected static final String COLUMN_HEAD_RECURSIVE_CALL  = Messages.getString("CallVisualiser.recursiveCaller"); //$NON-NLS-1$
	protected static final String COLUMN_HEAD_CALLER_PERCENT  = Messages.getString("CallVisualiser.percentOfCalls"); //$NON-NLS-1$
	protected static final String COLUMN_HEAD_CALLEE_PERCENT  = Messages.getString("CallVisualiser.percentOfCalls"); //$NON-NLS-1$
	protected static final String COLUMN_HEAD_IS_CALLED_COUNT = Messages.getString("CallVisualiser.calledSamples"); //$NON-NLS-1$
	protected static final String COLUMN_HEAD_IS_CALLER_COUNT = Messages.getString("CallVisualiser.callerSamples"); //$NON-NLS-1$
	protected static final String COLUMN_HEAD_IS_CALLED_COUNT2 = Messages.getString("CallVisualiser.calls"); //$NON-NLS-1$
	protected static final String COLUMN_HEAD_IS_CALLER_COUNT2 = Messages.getString("CallVisualiser.calls"); //$NON-NLS-1$

	// table column widths
	protected static final int COLUMN_WIDTH_IS_CALLED       = 70;
	protected static final int COLUMN_WIDTH_IS_CALLER       = 70;
	protected static final int COLUMN_WIDTH_RECURSIVE_CALL  = 105;
	protected static final int COLUMN_WIDTH_CALLER_PERCENT  = 98;
	protected static final int COLUMN_WIDTH_CALLEE_PERCENT  = 98;
	protected static final int COLUMN_WIDTH_IS_CALLED_COUNT = 113;
	protected static final int COLUMN_WIDTH_IS_CALLER_COUNT = 85;
	protected static final int COLUMN_WIDTH_IS_CALLED_COUNT2 = 65;
	protected static final int COLUMN_WIDTH_IS_CALLER_COUNT2 = 65;
	
	// SashForm to hold the tables and their titles
	private SashForm sashForm;
	
	// Table viewers and their tables
	private TableViewer callerTableViewer;
	private Table callerTable;
	
	private TableViewer currentFunctionTableViewer;
	private Table       currentFunctionTable;
	private TableColumn  currentFunctionDefaultColumn;

	private TableViewer calleeTableViewer;
	private Table calleeTable;

	// context menus
	private Menu callerMenu;
	private Menu currentFunctionMenu;
	private Menu calleeMenu;
	
	private Table currentMenuTable;

	private SashForm parent;

	// this display's editor and page
	private PIPageEditor pageEditor;
	private int pageIndex;
	
	// trace associated with this display
	private GfcTrace myTrace;
	
	// lists of functions, function callers, and function callees
	private GfcFunctionItem[]  functionArray;
	private CallerCalleeItem[] callerList;
	private CallerCalleeItem[] calleeList;
	
	// menu items
	protected Action copyAction;
	protected Action copyTableAction;
	protected Action functionCopyFunctionAction;
	protected Action saveTableAction;
	protected Action functionSaveFunctionAction;
	
	protected static int SAMPLES_AT_ONE_TIME = 1000;
	
	// class to pass sample data to the save wizard
    public class SaveSampleString implements ISaveSamples {
    	int startIndex = 0;
    	
    	public SaveSampleString() {
		}

    	public String getData() {
    		return getData(SAMPLES_AT_ONE_TIME);
		}

		public String getData(int size) {
			String returnString = getSampleString(this.startIndex, this.startIndex + size);
    		if (returnString == null)
    			this.startIndex = 0;
    		else
    			this.startIndex += size;
			return returnString;
		}

		public int getIndex() {
			return this.startIndex;
		}

		public void clear() {
			this.startIndex = 0;
		}
    }

	/*
	 * return the call samples selected in the interval 
	 */
	protected String getSampleString(int startIndex, int endIndex)
	{
		int startTime = (int) (PIPageEditor.currentPageEditor().getStartTime() * 1000.0 + 0.0005);
		int endTime   = (int) (PIPageEditor.currentPageEditor().getEndTime()   * 1000.0 + 0.0005);
		
		// check if we have returned everything
		if (startIndex > (endTime - startTime))
			return null;
		
		GfcTrace trace = this.myTrace;
		Vector samples = trace.samples;
	
		String returnString = ""; //$NON-NLS-1$
		
		if (startIndex == 0)
			returnString = Messages.getString("CallVisualiser.callHeading"); //$NON-NLS-1$

		for (int i = startTime + startIndex; i < endTime && i < startTime + endIndex; i++) {
			GfcSample sample = (GfcSample) samples.get(i);

			returnString +=   sample.sampleSynchTime + ",0x" //$NON-NLS-1$
							+ Long.toHexString(sample.linkRegister)
							+ ",\"" //$NON-NLS-1$
							+ (sample.callerFunctionItt != null
								? sample.callerFunctionItt.functionName
								: sample.callerFunctionSym.functionName)
							+ "\"," //$NON-NLS-1$
							+ (sample.callerFunctionItt != null
								? sample.callerFunctionItt.functionBinary.binaryName
								: sample.callerFunctionSym.functionBinary.binaryName)
							+ ",0x" //$NON-NLS-1$
							+ Long.toHexString(sample.programCounter)
							+ ",\"" //$NON-NLS-1$
							+ (sample.currentFunctionItt != null
								? sample.currentFunctionItt.functionName
								: sample.currentFunctionSym.functionName)
							+ "\"," //$NON-NLS-1$
							+ (sample.currentFunctionItt != null
								? sample.currentFunctionItt.functionBinary.binaryName
								: sample.currentFunctionSym.functionBinary.binaryName)
							+ "\n"; //$NON-NLS-1$
		}

		return returnString;
	}

	protected MenuItem getSaveSamplesItem(Menu menu, boolean enabled) {
	    MenuItem saveSamplesItem = new MenuItem(menu, SWT.PUSH);

		saveSamplesItem.setText(Messages.getString("CallVisualiser.saveAllSamplesForInterval")); //$NON-NLS-1$
		saveSamplesItem.setEnabled(enabled);
		
		if (enabled) {
			saveSamplesItem.addSelectionListener(new SelectionAdapter() { 
				public void widgetSelected(SelectionEvent e) {
					action("saveSamples"); //$NON-NLS-1$
				}
			});
		}
	
		return saveSamplesItem;
	}

	public CallVisualiser(PIPageEditor pageEditor, int pageIndex, SashForm parent, GfcTrace trace)
	{
		this.pageEditor = pageEditor;
		this.pageIndex  = pageIndex;
		this.parent     = parent;
		this.myTrace    = trace;
		
		// let the trace know about the CallVisualiser so that unit tests can find it
		trace.setCallVisualiser(this);

		// create the 3 table viewers: caller functions, selected function, callee functions
		createTableViewers(parent);
	}

	public void createTableViewers(SashForm parent)
	{
		if (parent == null)
			return;

		/*
		 * Create a SashForm with three labeled tables:
		 * 
		 * 1. Functions called by
		 * 2. Functions (one checkbox selectable at a time)
		 * 3. Functions called
		 */

		// the 3 tables will be in a sashForm
		if (parent.getOrientation() != SWT.VERTICAL)
		{
			// put a SashForm in the SashForm
			this.sashForm = new SashForm(parent, SWT.VERTICAL);
			this.sashForm.SASH_WIDTH = 5; // 5 pixel wide sash
		} else
			this.sashForm = parent;
		
		createCallerTableViewer(sashForm);
		createCurrentFunctionTableViewer(sashForm);
		createCalleeTableViewer(sashForm);

		createDefaultActions(true);
	}
	
	private void createCurrentFunctionTableViewer(SashForm sashForm)
	{
		/*
		 * Functions (one checkbox selectable at a time)
		 *
		 * 		selected checkbox
		 *		percent load
		 *  	function name
		 *		function start address
		 *		binary containing function
		 *		sample count
		 */
		Label label;
		Composite holder;
		Table table;
		TableColumn column;
		
		// middle functionTable and title
		holder = new Composite(sashForm, SWT.NONE);
		GridLayout gridLayout = new GridLayout(1, true);
		gridLayout.marginWidth = 0;
		gridLayout.marginHeight = 0;
		holder.setLayout(gridLayout);

		label = new Label(holder, SWT.CENTER | SWT.BORDER);
		label.setBackground(parent.getDisplay().getSystemColor(SWT.COLOR_YELLOW));
		label.setFont(PIPageEditor.helvetica_10);
		label.setText(Messages.getString("CallVisualiser.selectFunction")); //$NON-NLS-1$
		label.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		this.currentFunctionTableViewer = new TableViewer(holder,
  				SWT.BORDER | SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION);
		this.currentFunctionTable = currentFunctionTableViewer.getTable();
		
		// add the label provider and content provider
		this.currentFunctionTableViewer.setLabelProvider(new SharedLabelProvider(this.currentFunctionTable));
		this.currentFunctionTableViewer.setContentProvider(new ArrayContentProvider());
		this.currentFunctionTableViewer.setSorter(new SharedSorter());
		
		table = this.currentFunctionTable;
		table.setRedraw(false);

		// give the table a heading for use in copying and exported
		table.setData(Messages.getString("CallVisualiser.selectedFunction")); //$NON-NLS-1$
		
		// create the other table entries when a row in this table is selected
		table.addSelectionListener(new SelectionListener() {

			public void widgetSelected(SelectionEvent event) {

				if (!(event.item instanceof TableItem))
					return;
				
	       		// set the other tables based on this selection
	       		if (!(event.item.getData() instanceof GfcFunctionItem))
	       			return;
	       		
	       		updateCallerCalleeTables((GfcFunctionItem)event.item.getData());
			}

			public void widgetDefaultSelected(SelectionEvent e) {
				widgetSelected(e);
			}});
		
		// data associated with the TableViewer will note which columns contain hex values
		// Keep this in the order in which columns have been created
		boolean[] isHex = {false, false, false, true, false, false, false};
		this.currentFunctionTable.setData("isHex", isHex); //$NON-NLS-1$

		// is called percent column
		column = new TableColumn(table, SWT.RIGHT);
		column.setText(COLUMN_HEAD_IS_CALLED);
		column.setWidth(COLUMN_WIDTH_IS_CALLED);
		column.setData(new Integer(COLUMN_ID_IS_CALLED));
		column.setMoveable(true);
		column.setResizable(true);
		column.addSelectionListener(new CheckboxColumnSelectionHandler());

		// is caller percent column
		column = new TableColumn(table, SWT.RIGHT);
		column.setText(COLUMN_HEAD_IS_CALLER);
		column.setWidth(COLUMN_WIDTH_IS_CALLER);
		column.setData(new Integer(COLUMN_ID_IS_CALLER));
		column.setMoveable(true);
		column.setResizable(true);
		column.addSelectionListener(new CheckboxColumnSelectionHandler());

		// function name column
		column = new TableColumn(table, SWT.LEFT);
		column.setText(COLUMN_HEAD_FUNCTION);
		column.setWidth(COLUMN_WIDTH_FUNCTION_NAME);
		column.setData(new Integer(COLUMN_ID_FUNCTION));
		column.setMoveable(true);
		column.setResizable(true);
		column.addSelectionListener(new CheckboxColumnSelectionHandler());

		// function start address column
		column = new TableColumn(table, SWT.CENTER);
		column.setText(COLUMN_HEAD_START_ADDR);
		column.setWidth(COLUMN_WIDTH_START_ADDRESS);
		column.setData(new Integer(COLUMN_ID_START_ADDR));
		column.setMoveable(true);
		column.setResizable(true);
		column.addSelectionListener(new CheckboxColumnSelectionHandler());

		// binary containing function column
		column = new TableColumn(table, SWT.LEFT);
		column.setText(COLUMN_HEAD_IN_BINARY);
		column.setWidth(COLUMN_WIDTH_IN_BINARY);
		column.setData(new Integer(COLUMN_ID_IN_BINARY));
		column.setMoveable(true);
		column.setResizable(true);
		column.addSelectionListener(new CheckboxColumnSelectionHandler());

		// path to binary containing function column
		column = new TableColumn(table, SWT.LEFT);
		column.setText(COLUMN_HEAD_IN_BINARY_PATH);
		column.setWidth(COLUMN_WIDTH_IN_BINARY_PATH);
		column.setData(new Integer(COLUMN_ID_IN_BINARY_PATH));
		column.setMoveable(true);
		column.setResizable(true);
		column.addSelectionListener(new CheckboxColumnSelectionHandler());

		// sample count column
		column = new TableColumn(table, SWT.CENTER);
		column.setText(COLUMN_HEAD_IS_CALLED_COUNT);
		column.setWidth(COLUMN_WIDTH_IS_CALLED_COUNT);
		column.setData(new Integer(COLUMN_ID_IS_CALLED_COUNT));
		column.setMoveable(true);
		column.setResizable(true);
		column.addSelectionListener(new CheckboxColumnSelectionHandler());

		// set the default sort column to COLUMN_ID_IS_CALLED_COUNT
		currentFunctionDefaultColumn = column;

		// sample count column
		column = new TableColumn(table, SWT.CENTER);
		column.setText(COLUMN_HEAD_IS_CALLER_COUNT);
		column.setWidth(COLUMN_WIDTH_IS_CALLER_COUNT);
		column.setData(new Integer(COLUMN_ID_IS_CALLER_COUNT));
		column.setMoveable(true);
		column.setResizable(true);
		column.addSelectionListener(new CheckboxColumnSelectionHandler());

		table.setLayoutData(new GridData(GridData.FILL_BOTH));
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		table.setRedraw(true);

		// listen for mouse clicks: to select a row, pop up a menu, etc.
		table.addMouseListener(new CurrentFunctionMouseListener());

		table.addFocusListener(new FocusListener() {
			IAction oldCopyAction = null;

			public void focusGained(org.eclipse.swt.events.FocusEvent arg0) {
				IActionBars bars = PIPageEditor.getActionBars();
				
				// modify what is executed when Copy is called from the Edit menu
				oldCopyAction = bars.getGlobalActionHandler(ActionFactory.COPY.getId());

				bars.setGlobalActionHandler(ActionFactory.COPY.getId(), copyAction);

				copyAction.setEnabled(currentFunctionTable.getSelectionCount() > 0);
				bars.updateActionBars();
				
				// add to the Edit menu
		        IMenuManager editMenuManager = bars.getMenuManager().findMenuUsingPath(IIDEActionConstants.M_EDIT);

		        if (editMenuManager instanceof SubMenuManager)
		        {
		        	IContributionManager editManager = ((SubMenuManager)editMenuManager).getParent();
		        	ActionContributionItem item;

					editMenuManager.remove("PICopyFunction"); //$NON-NLS-1$
					functionCopyFunctionAction.setEnabled(currentFunctionTable.getSelectionCount() > 0);
		        	item = new ActionContributionItem(functionCopyFunctionAction);
		        	item.setVisible(true);
		        	editManager.prependToGroup(IIDEActionConstants.CUT_EXT, item);

					editMenuManager.remove("PICopyTable"); //$NON-NLS-1$
					copyTableAction.setEnabled(currentFunctionTable.getItemCount() > 0);
		        	item = new ActionContributionItem(copyTableAction);
		        	item.setVisible(true);
		        	editManager.prependToGroup(IIDEActionConstants.CUT_EXT, item);
		        }
				
				// add to the File menu
		        IMenuManager fileMenuManager = bars.getMenuManager().findMenuUsingPath(IIDEActionConstants.M_FILE);

		        if (fileMenuManager instanceof SubMenuManager)
		        {
		        	IContributionManager fileManager = ((SubMenuManager)fileMenuManager).getParent();
		        	ActionContributionItem item;

					fileMenuManager.remove("PISaveTable"); //$NON-NLS-1$
					saveTableAction.setEnabled(currentFunctionTable.getItemCount() > 0);
		        	item = new ActionContributionItem(saveTableAction);
		        	item.setVisible(true);
		        	fileManager.insertAfter("saveAll", item); //$NON-NLS-1$

					fileMenuManager.remove("PISaveFunction"); //$NON-NLS-1$
					functionSaveFunctionAction.setEnabled(currentFunctionTable.getSelectionCount() > 0);
		        	item = new ActionContributionItem(functionSaveFunctionAction);
		        	item.setVisible(true);
		        	fileManager.insertAfter("PISaveTable", item); //$NON-NLS-1$
		        }
		}

			public void focusLost(org.eclipse.swt.events.FocusEvent arg0) {
				IActionBars bars = PIPageEditor.getActionBars();
				bars.setGlobalActionHandler(ActionFactory.COPY.getId(), oldCopyAction);
				bars.updateActionBars();

				SubMenuManager editMenuManager = (SubMenuManager) PIPageEditor.getMenuManager().find(IIDEActionConstants.M_EDIT);
				editMenuManager.remove("PICopyTable"); //$NON-NLS-1$
				editMenuManager.remove("PICopyFunction"); //$NON-NLS-1$

				SubMenuManager fileMenuManager = (SubMenuManager) PIPageEditor.getMenuManager().find(IIDEActionConstants.M_FILE);
				fileMenuManager.remove("PISaveTable"); //$NON-NLS-1$
				fileMenuManager.remove("PISaveFunction"); //$NON-NLS-1$
			}
		});
	}
	
	private void createCallerTableViewer(SashForm sashForm)
	{
		/*
		 * Functions called by
		 * 
		 *		percent of calls
		 *		total percent
		 *		caller percent
		 *  	function name
		 *		function start address
		 *		binary containing function
		 *		sample count
		 */
		Label label;
		Composite holder;
		Table table;
		TableColumn column;
		
		// top functionTable and title
		holder = new Composite(sashForm, SWT.NONE);
//		holder.setLayout(new FillLayout(SWT.VERTICAL));
		GridLayout gridLayout = new GridLayout(1, true);
		gridLayout.marginWidth = 0;
		gridLayout.marginHeight = 0;
		holder.setLayout(gridLayout);

		label = new Label(holder, SWT.CENTER | SWT.BORDER);
		label.setBackground(parent.getDisplay().getSystemColor(SWT.COLOR_CYAN));
		label.setFont(PIPageEditor.helvetica_10);
		label.setText(Messages.getString("CallVisualiser.callingSelectedFunction")); //$NON-NLS-1$
		label.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		this.callerTableViewer = new TableViewer(holder,
  				SWT.BORDER | SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION);
		this.callerTable = callerTableViewer.getTable();
		
		// add the label provider and content provider
		this.callerTableViewer.setLabelProvider(new SharedLabelProvider(this.callerTable));
		this.callerTableViewer.setContentProvider(new ArrayContentProvider());
		this.callerTableViewer.setSorter(new SharedSorter());

		table = this.callerTable;
		table.setRedraw(false);

		// give the table a heading for possible use in copying and exported
		table.setData(Messages.getString("CallVisualiser.callerFunctions")); //$NON-NLS-1$
		
		// data associated with the TableViewer will note which columns contain hex values
		// Keep this in the order in which columns have been created
		boolean[] isHex = {false, false, false, true, false, false, false};
		this.callerTable.setData("isHex", isHex); //$NON-NLS-1$

		// percent of calls column
		column = new TableColumn(table, SWT.RIGHT);
		column.setText(COLUMN_HEAD_CALLER_PERCENT);
		column.setWidth(COLUMN_WIDTH_CALLER_PERCENT);
		column.setData(new Integer(COLUMN_ID_CALLER_PERCENT));
		column.setMoveable(true);
		column.setResizable(true);
		column.addSelectionListener(new CalledByColumnSelectionHandler());

		// sample count column
		column = new TableColumn(table, SWT.CENTER);
		column.setText(COLUMN_HEAD_IS_CALLER_COUNT2);
		column.setWidth(COLUMN_WIDTH_IS_CALLER_COUNT2);
		column.setData(new Integer(COLUMN_ID_IS_CALLER_COUNT));
		column.setMoveable(true);
		column.setResizable(true);
		column.addSelectionListener(new CalledByColumnSelectionHandler());

		// function name column
		column = new TableColumn(table, SWT.LEFT);
		column.setText(COLUMN_HEAD_FUNCTION);
		column.setWidth(COLUMN_WIDTH_FUNCTION_NAME);
		column.setData(new Integer(COLUMN_ID_FUNCTION));
		column.setMoveable(true);
		column.setResizable(true);
		column.addSelectionListener(new CalledByColumnSelectionHandler());

		// function start address column
		column = new TableColumn(table, SWT.CENTER);
		column.setText(COLUMN_HEAD_START_ADDR);
		column.setWidth(COLUMN_WIDTH_START_ADDRESS);
		column.setData(new Integer(COLUMN_ID_START_ADDR));
		column.setMoveable(true);
		column.setResizable(true);
		column.addSelectionListener(new CalledByColumnSelectionHandler());

		// binary containing function column
		column = new TableColumn(table, SWT.LEFT);
		column.setText(COLUMN_HEAD_IN_BINARY);
		column.setWidth(COLUMN_WIDTH_IN_BINARY);
		column.setData(new Integer(COLUMN_ID_IN_BINARY));
		column.setMoveable(true);
		column.setResizable(true);
		column.addSelectionListener(new CalledByColumnSelectionHandler());

		// path to binary containing function column
		column = new TableColumn(table, SWT.LEFT);
		column.setText(COLUMN_HEAD_IN_BINARY_PATH);
		column.setWidth(COLUMN_WIDTH_IN_BINARY_PATH);
		column.setData(new Integer(COLUMN_ID_IN_BINARY_PATH));
		column.setMoveable(true);
		column.setResizable(true);
		column.addSelectionListener(new CalledByColumnSelectionHandler());

		// is caller percent column
		column = new TableColumn(table, SWT.RIGHT);
		column.setText(COLUMN_HEAD_IS_CALLER);
		column.setWidth(COLUMN_WIDTH_IS_CALLER);
		column.setData(new Integer(COLUMN_ID_IS_CALLER));
		column.setMoveable(true);
		column.setResizable(true);
		column.addSelectionListener(new CalledByColumnSelectionHandler());

		table.setLayoutData(new GridData(GridData.FILL_BOTH));
//		table.setLayout(new FillLayout());
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		table.setRedraw(true);
		
		// when a row is selected, allow the user to double-click or right click and make that function the selected function
		table.addSelectionListener(new SelectionAdapter() {

			public void widgetDefaultSelected(SelectionEvent se) {
				TableItem item = (TableItem)se.item;
				Table table = (Table)se.widget;
				CallerCalleeItem callerCalleeItem = (CallerCalleeItem)item.getData();
				
				// deselect this line
				table.deselectAll();
				
				// choose this row's function
				chooseNewFunction(callerCalleeItem);
			}
		});
		
		table.addMouseListener(new CallerMouseListener());
		
		table.addFocusListener(new FocusListener() {
			IAction oldCopyAction = null;

			public void focusGained(org.eclipse.swt.events.FocusEvent arg0) {
				IActionBars bars = PIPageEditor.getActionBars();
				
				// modify what is executed when Copy is called from the Edit menu
				oldCopyAction = bars.getGlobalActionHandler(ActionFactory.COPY.getId());

				bars.setGlobalActionHandler(ActionFactory.COPY.getId(), copyAction);

				copyAction.setEnabled(callerTable.getSelectionCount() > 0);
				bars.updateActionBars();
				
				// add to the Edit menu
		        IMenuManager editMenuManager = bars.getMenuManager().findMenuUsingPath(IIDEActionConstants.M_EDIT);

		        if (editMenuManager instanceof SubMenuManager)
		        {
		        	IContributionManager editManager = ((SubMenuManager)editMenuManager).getParent();
		        	ActionContributionItem item;

					editMenuManager.remove("PICopyTable"); //$NON-NLS-1$
					copyTableAction.setEnabled(callerTable.getItemCount() > 0);
		        	item = new ActionContributionItem(copyTableAction);
		        	item.setVisible(true);
		        	editManager.prependToGroup(IIDEActionConstants.CUT_EXT, item);
		        }
				
				// add to the File menu
		        IMenuManager fileMenuManager = bars.getMenuManager().findMenuUsingPath(IIDEActionConstants.M_FILE);

		        if (fileMenuManager instanceof SubMenuManager)
		        {
		        	IContributionManager fileManager = ((SubMenuManager)fileMenuManager).getParent();
		        	ActionContributionItem item;

					fileMenuManager.remove("PISaveTable"); //$NON-NLS-1$
					saveTableAction.setEnabled(callerTable.getItemCount() > 0);
		        	item = new ActionContributionItem(saveTableAction);
		        	item.setVisible(true);
		        	fileManager.insertAfter("saveAll", item); //$NON-NLS-1$
		        }
			}

			public void focusLost(org.eclipse.swt.events.FocusEvent arg0) {
				IActionBars bars = PIPageEditor.getActionBars();
				bars.setGlobalActionHandler(ActionFactory.COPY.getId(), oldCopyAction);
				bars.updateActionBars();

				SubMenuManager editMenuManager = (SubMenuManager) PIPageEditor.getMenuManager().find(IIDEActionConstants.M_EDIT);
				editMenuManager.remove("PICopyTable"); //$NON-NLS-1$

				SubMenuManager fileMenuManager = (SubMenuManager) PIPageEditor.getMenuManager().find(IIDEActionConstants.M_FILE);
				fileMenuManager.remove("PISaveTable"); //$NON-NLS-1$
			}
		});
	}
	
	private void createCalleeTableViewer(SashForm sashForm)
	{
		/*
		 * Functions called
		 * 
		 *		percent of calls
		 *		total percent
		 *		caller percent
		 *  	function name
		 *		function start address
		 *		binary containing function
		 *		sample count
		 */
		Label label;
		Composite holder;
		Table table;
		TableColumn column;
		
		// bottom functionTable and title
		holder = new Composite(sashForm, SWT.NONE);
//		holder.setLayout(new FillLayout(SWT.VERTICAL));
		GridLayout gridLayout = new GridLayout(1, true);
		gridLayout.marginWidth = 0;
		gridLayout.marginHeight = 0;
		holder.setLayout(gridLayout);

		label = new Label(holder, SWT.CENTER | SWT.BORDER);
		label.setBackground(parent.getDisplay().getSystemColor(SWT.COLOR_CYAN));
		label.setFont(PIPageEditor.helvetica_10);
		label.setText(Messages.getString("CallVisualiser.calledBySelectedFunction")); //$NON-NLS-1$
		label.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		this.calleeTableViewer = new TableViewer(holder,
  				SWT.BORDER | SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION);
		this.calleeTable = calleeTableViewer.getTable();
		
		// add the label provider and content provider
		this.calleeTableViewer.setLabelProvider(new SharedLabelProvider(this.calleeTable));
		this.calleeTableViewer.setContentProvider(new ArrayContentProvider());
		this.calleeTableViewer.setSorter(new SharedSorter());

		table = this.calleeTable;
		table.setRedraw(false);

		// give the table a heading for possible use in copying and exported
		table.setData(Messages.getString("CallVisualiser.calleeFunctions")); //$NON-NLS-1$
		
		// data associated with the TableViewer will note which columns contain hex values
		// Keep this in the order in which columns have been created
		boolean[] isHex = {false, false, false, true, false, false, false};
		this.calleeTable.setData("isHex", isHex); //$NON-NLS-1$
		
		// percent of calls column
		column = new TableColumn(table, SWT.RIGHT);
		column.setText(COLUMN_HEAD_CALLEE_PERCENT);
		column.setWidth(COLUMN_WIDTH_CALLEE_PERCENT);
		column.setData(new Integer(COLUMN_ID_CALLEE_PERCENT));
		column.setMoveable(true);
		column.setResizable(true);
		column.addSelectionListener(new CalledColumnSelectionHandler());

		// sample count column
		column = new TableColumn(table, SWT.CENTER);
		column.setText(COLUMN_HEAD_IS_CALLED_COUNT2);
		column.setWidth(COLUMN_WIDTH_IS_CALLED_COUNT2);
		column.setData(new Integer(COLUMN_ID_IS_CALLED_COUNT));
		column.setMoveable(true);
		column.setResizable(true);
		column.addSelectionListener(new CalledColumnSelectionHandler());

		// function name column
		column = new TableColumn(table, SWT.LEFT);
		column.setText(COLUMN_HEAD_FUNCTION);
		column.setWidth(COLUMN_WIDTH_FUNCTION_NAME);
		column.setData(new Integer(COLUMN_ID_FUNCTION));
		column.setMoveable(true);
		column.setResizable(true);
		column.addSelectionListener(new CalledColumnSelectionHandler());

		// function start address column
		column = new TableColumn(table, SWT.CENTER);
		column.setText(COLUMN_HEAD_START_ADDR);
		column.setWidth(COLUMN_WIDTH_START_ADDRESS);
//		column.setData(new Integer(COLUMN_ID_START_ADDR3));
		column.setData(new Integer(COLUMN_ID_START_ADDR));
		column.setMoveable(true);
		column.setResizable(true);
		column.addSelectionListener(new CalledColumnSelectionHandler());

		// binary containing function column
		column = new TableColumn(table, SWT.LEFT);
		column.setText(COLUMN_HEAD_IN_BINARY);
		column.setWidth(COLUMN_WIDTH_IN_BINARY);
//		column.setData(new Integer(COLUMN_ID_IN_BINARY3));
		column.setData(new Integer(COLUMN_ID_IN_BINARY));
		column.setMoveable(true);
		column.setResizable(true);
		column.addSelectionListener(new CalledColumnSelectionHandler());

		// path to binary containing function column
		column = new TableColumn(table, SWT.LEFT);
		column.setText(COLUMN_HEAD_IN_BINARY_PATH);
		column.setWidth(COLUMN_WIDTH_IN_BINARY_PATH);
//		column.setData(new Integer(COLUMN_ID_IN_BINARY_PATH3));
		column.setData(new Integer(COLUMN_ID_IN_BINARY_PATH));
		column.setMoveable(true);
		column.setResizable(true);
		column.addSelectionListener(new CalledColumnSelectionHandler());

		// percent of calls column
		column = new TableColumn(table, SWT.RIGHT);
		column.setText(COLUMN_HEAD_IS_CALLED);
		column.setWidth(COLUMN_WIDTH_IS_CALLED);
//		column.setData(new Integer(COLUMN_ID_IS_CALLED3));
		column.setData(new Integer(COLUMN_ID_IS_CALLED));
		column.setMoveable(true);
		column.setResizable(true);
		column.addSelectionListener(new CalledColumnSelectionHandler());

		table.setLayoutData(new GridData(GridData.FILL_BOTH));
//		table.setLayout(new FillLayout());
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		table.setRedraw(true);
		
		// when a row is selected, allow the user to double-click or right click and make that function the selected function
		table.addSelectionListener(new SelectionAdapter() {

			public void widgetDefaultSelected(SelectionEvent se) {
				TableItem item = (TableItem)se.item;
				Table table = (Table)se.widget;
				CallerCalleeItem callerCallee = (CallerCalleeItem)item.getData();
				
				// deselect this row
				table.deselectAll();
				
				// choose this row's function
				chooseNewFunction(callerCallee);
			}
		});
		
		table.addMouseListener(new CalleeMouseListener());
		
		table.addFocusListener(new FocusListener() {
			IAction oldCopyAction = null;

			public void focusGained(org.eclipse.swt.events.FocusEvent arg0) {
				IActionBars bars = PIPageEditor.getActionBars();
				
				// modify what is executed when Copy is called from the Edit menu
				oldCopyAction = bars.getGlobalActionHandler(ActionFactory.COPY.getId());

				bars.setGlobalActionHandler(ActionFactory.COPY.getId(), copyAction);

				copyAction.setEnabled(calleeTable.getSelectionCount() > 0);
				bars.updateActionBars();
				
				// add to the Edit menu
		        IMenuManager editMenuManager = bars.getMenuManager().findMenuUsingPath(IIDEActionConstants.M_EDIT);

		        if (editMenuManager instanceof SubMenuManager)
		        {
		        	IContributionManager editManager = ((SubMenuManager)editMenuManager).getParent();
		        	ActionContributionItem item;

					editMenuManager.remove("PICopyTable"); //$NON-NLS-1$
					copyTableAction.setEnabled(calleeTable.getItemCount() > 0);
		        	item = new ActionContributionItem(copyTableAction);
		        	item.setVisible(true);
		        	editManager.prependToGroup(IIDEActionConstants.CUT_EXT, item);
		        }
				
				// add to the File menu
		        IMenuManager fileMenuManager = bars.getMenuManager().findMenuUsingPath(IIDEActionConstants.M_FILE);

		        if (fileMenuManager instanceof SubMenuManager)
		        {
		        	IContributionManager fileManager = ((SubMenuManager)fileMenuManager).getParent();
		        	ActionContributionItem item;

					fileMenuManager.remove("PISaveTable"); //$NON-NLS-1$
					saveTableAction.setEnabled(calleeTable.getItemCount() > 0);
		        	item = new ActionContributionItem(saveTableAction);
		        	item.setVisible(true);
		        	fileManager.insertAfter("saveAll", item); //$NON-NLS-1$
		        }
			}

			public void focusLost(org.eclipse.swt.events.FocusEvent arg0) {
				IActionBars bars = PIPageEditor.getActionBars();
				bars.setGlobalActionHandler(ActionFactory.COPY.getId(), oldCopyAction);
				bars.updateActionBars();

				SubMenuManager editMenuManager = (SubMenuManager) PIPageEditor.getMenuManager().find(IIDEActionConstants.M_EDIT);
				editMenuManager.remove("PICopyTable"); //$NON-NLS-1$

				SubMenuManager fileMenuManager = (SubMenuManager) PIPageEditor.getMenuManager().find(IIDEActionConstants.M_FILE);
				fileMenuManager.remove("PISaveTable"); //$NON-NLS-1$
			}
		});
	}
	
	private void chooseNewFunction(CallerCalleeItem callerCalleeItem)
	{
		// find the correct line in the function table and select it
		int count = currentFunctionTable.getItemCount();

		for (int i = 0; i < count; i++) {
			GfcFunctionItem functionItem = (GfcFunctionItem)(currentFunctionTable.getItem(i).getData());
			if (   functionItem.name.equals(callerCalleeItem.item.name)
				&& functionItem.dllName.equals(callerCalleeItem.item.dllName)) {
				currentFunctionTable.select(i);
				currentFunctionTable.showSelection();
				updateCallerCalleeTables(functionItem);
				return;
			}
		}
	}

	public void updateCallerCalleeTables(GfcFunctionItem item)
	{
		Table table;
		TableColumn sortByColumn;

   		setCallerListToFunctionsThatCallThisFunction(item);
    	setCalleeListToFunctionsThisFunctionCalls(item);
    	
    	callerTableViewer.setInput(callerList);

    	if ((callerList != null) && (callerList.length > 0)) {
    		SharedSorter sorter = ((SharedSorter) callerTableViewer.getSorter());
    		int columnID = sorter.getColumnID();
    		
    		if (columnID == -1) {
    			columnID = COLUMN_ID_CALLER_PERCENT;
    		} else {
    			sorter.setSortAscending(!sorter.getSortAscending());
    		}
			sorter.doSort(columnID);
 	
	    	sortByColumn = null;
	    	table = callerTableViewer.getTable();
			for (int i = 0; i < table.getColumnCount(); i++) {
				if (table.getColumn(i).getData() instanceof Integer) {
					if (((Integer)table.getColumn(i).getData()) == columnID) {
						sortByColumn = table.getColumn(i);
						break;
					}
				}
			}
	
			if (sortByColumn != null) {
				table.setSortColumn(sortByColumn);
				table.setSortDirection(sorter.getSortAscending() ? SWT.UP : SWT.DOWN);
			}
	    	callerTableViewer.refresh();
    	} else {
    		callerTableViewer.getTable().setSortColumn(null);
    	}

    	calleeTableViewer.setInput(calleeList);

    	if ((calleeList != null) && (calleeList.length > 0)) {
    		SharedSorter sorter = ((SharedSorter) calleeTableViewer.getSorter());
    		int columnID = sorter.getColumnID();
    		
    		if (columnID == -1) {
    			columnID = COLUMN_ID_CALLEE_PERCENT;
    		} else {
    			sorter.setSortAscending(!sorter.getSortAscending());
    		}
			sorter.doSort(columnID);
	
	    	sortByColumn = null;
	    	table = calleeTableViewer.getTable();
			for (int i = 0; i < table.getColumnCount(); i++) {
				if (table.getColumn(i).getData() instanceof Integer) {
					if (((Integer)table.getColumn(i).getData()) == columnID) {
						sortByColumn = table.getColumn(i);
						break;
					}
				}
			}
	
			if (sortByColumn != null) {
				table.setSortColumn(sortByColumn);
				table.setSortDirection(sorter.getSortAscending() ? SWT.UP : SWT.DOWN);
			}
			calleeTableViewer.refresh();
    	} else {
    		calleeTableViewer.getTable().setSortColumn(null);
    	}
	}
	
	private class CallerMouseListener implements MouseListener
	{
		public void mouseDoubleClick(org.eclipse.swt.events.MouseEvent me) {}

		public void mouseDown(org.eclipse.swt.events.MouseEvent me) {}

		public void mouseUp(org.eclipse.swt.events.MouseEvent me) {
			copyAction.setEnabled(callerTable.getSelectionCount() > 0);
			copyTableAction.setEnabled(callerTable.getItemCount() > 0);

			// only look for button 3 (right click)
			if (me.button != MouseEvent.BUTTON3)
				return;

			// make the caller table the menu table
			currentMenuTable = callerTable;

			if (callerMenu != null) {
				callerMenu.dispose();
			}

			callerMenu = new Menu(callerTable.getShell(), SWT.POP_UP); 
			
			MenuItem showCallInfoItem = new MenuItem(callerMenu, SWT.PUSH);
			showCallInfoItem.setText(Messages.getString("CallVisualiser.showCallInfo")); //$NON-NLS-1$
			showCallInfoItem.addSelectionListener(new SelectionAdapter() { 
				public void widgetSelected(SelectionEvent se) {
				    // based on the table's selected item, update the tables
					TableItem[] selections = callerTable.getSelection();
					callerTable.deselectAll();
					
					if (selections.length != 0)
						chooseNewFunction((CallerCalleeItem)selections[0].getData());
				}
			});
			
			new MenuItem(callerMenu, SWT.SEPARATOR);
			
			MenuItem sourceLookupItem = new MenuItem(callerMenu, SWT.PUSH);
			sourceLookupItem.setText(Messages.getString("CallVisualiser.sourcelookup"));	//$NON-NLS-1$
			sourceLookupItem.addSelectionListener(new SelectionListener() {

				public void widgetDefaultSelected(SelectionEvent arg0) {
				}

				public void widgetSelected(SelectionEvent arg0) {
					doSourceLookup(callerTable);
				}
				
			});

			if (callerTable.getSelectionCount() == 0) {
				showCallInfoItem.setEnabled(false);
				sourceLookupItem.setEnabled(false);
			}

			// add copy, copy all, and save all
			new MenuItem(callerMenu, SWT.SEPARATOR);
			getCopyItem(callerMenu, callerTable.getSelectionCount() > 0);
			getCopyTableItem(callerMenu, callerTable.getItemCount() > 0);
			copyAction.setEnabled(callerTable.getSelectionCount() > 0);
			copyTableAction.setEnabled(callerTable.getItemCount() > 0);

			new MenuItem(callerMenu, SWT.SEPARATOR);
			getSaveTableItem(callerMenu, callerTable.getItemCount() > 0);
			saveTableAction.setEnabled(callerTable.getItemCount() > 0);
			
			// save samples
			int startTime = (int) (PIPageEditor.currentPageEditor().getStartTime() * 1000.0f);
			int endTime   = (int) (PIPageEditor.currentPageEditor().getEndTime()   * 1000.0f);

			if ((startTime == -1) || (endTime   == -1) || (startTime == endTime))
				getSaveSamplesItem(callerMenu, false); //$NON-NLS-1$
			else
				getSaveSamplesItem(callerMenu, true); //$NON-NLS-1$
			
			callerMenu.setLocation(callerTable.getParent().toDisplay(me.x, me.y));
			callerMenu.setVisible(true);
			callerTable.setMenu(callerMenu);
		}
	}
	
	private class CurrentFunctionMouseListener implements MouseListener
	{
		public void mouseDoubleClick(org.eclipse.swt.events.MouseEvent me) {}

		public void mouseDown(org.eclipse.swt.events.MouseEvent me) {}

		public void mouseUp(org.eclipse.swt.events.MouseEvent me) {
			copyAction.setEnabled(currentFunctionTable.getSelectionCount() > 0);
			copyTableAction.setEnabled(currentFunctionTable.getItemCount() > 0);
			functionCopyFunctionAction.setEnabled(currentFunctionTable.getSelectionCount() > 0);

			// only look for button 3 (right click)
			if (me.button != MouseEvent.BUTTON3)
				return;

			// make the current function table the menu table
			currentMenuTable = currentFunctionTable;

			if (currentFunctionMenu != null) {
				currentFunctionMenu.dispose();
			}

			currentFunctionMenu = new Menu(currentFunctionTable.getShell(), SWT.POP_UP); 
			
//			new MenuItem(callerMenu, SWT.SEPARATOR);
			
			MenuItem sourceLookupItem = new MenuItem(currentFunctionMenu, SWT.PUSH);
			sourceLookupItem.setText(Messages.getString("CallVisualiser.sourcelookup"));	//$NON-NLS-1$
			sourceLookupItem.addSelectionListener(new SelectionListener() {

				public void widgetDefaultSelected(SelectionEvent arg0) {
				}

				public void widgetSelected(SelectionEvent arg0) {
					doSourceLookup(currentFunctionTable);
				}
				
			});
			
			if (currentFunctionTable.getSelectionCount() == 0) {
				sourceLookupItem.setEnabled(false);
			}

			// add copy, copy all, copy caller/callee info, save all, save caller/callee info
			new MenuItem(currentFunctionMenu, SWT.SEPARATOR);
			getCopyItem(currentFunctionMenu, currentFunctionTable.getSelectionCount() > 0);
			getCopyTableItem(currentFunctionMenu, currentFunctionTable.getItemCount() > 0);
			getCopyFunctionItem(currentFunctionMenu, currentFunctionTable.getSelectionCount() > 0);
			copyAction.setEnabled(currentFunctionTable.getSelectionCount() > 0);
			copyTableAction.setEnabled(currentFunctionTable.getItemCount() > 0);
			functionCopyFunctionAction.setEnabled(currentFunctionTable.getSelectionCount() > 0);

			new MenuItem(currentFunctionMenu, SWT.SEPARATOR);
			getSaveTableItem(currentFunctionMenu, currentFunctionTable.getItemCount() > 0);
			getSaveFunctionItem(currentFunctionMenu, currentFunctionTable.getSelectionCount() > 0);
			saveTableAction.setEnabled(currentFunctionTable.getItemCount() > 0);
			functionSaveFunctionAction.setEnabled(currentFunctionTable.getSelectionCount() > 0);
			
			// save samples
			int startTime = (int) (PIPageEditor.currentPageEditor().getStartTime() * 1000.0f);
			int endTime   = (int) (PIPageEditor.currentPageEditor().getEndTime()   * 1000.0f);

			if ((startTime == -1) || (endTime   == -1) || (startTime == endTime))
				getSaveSamplesItem(currentFunctionMenu, false); //$NON-NLS-1$
			else
				getSaveSamplesItem(currentFunctionMenu, true); //$NON-NLS-1$

			currentFunctionMenu.setLocation(currentFunctionTable.getParent().toDisplay(me.x, me.y));
			currentFunctionMenu.setVisible(true);
			currentFunctionTable.setMenu(currentFunctionMenu);
		}
	}

	private class CalleeMouseListener implements MouseListener
	{
		public void mouseDoubleClick(org.eclipse.swt.events.MouseEvent me) {}

		public void mouseDown(org.eclipse.swt.events.MouseEvent me) {}

		public void mouseUp(org.eclipse.swt.events.MouseEvent me) {
			copyAction.setEnabled(calleeTable.getSelectionCount() > 0);
			copyTableAction.setEnabled(calleeTable.getItemCount() > 0);

			// only look for button 3 (right click)
			if (me.button != MouseEvent.BUTTON3)
				return;

			// make the callee table the menu table
			currentMenuTable = calleeTable;

			if (calleeMenu != null) {
				calleeMenu.dispose();
			}
			
			calleeMenu = new Menu(calleeTable.getShell(), SWT.POP_UP); 
			
			MenuItem showCallInfoItem = new MenuItem(calleeMenu, SWT.PUSH);
			showCallInfoItem.setText(Messages.getString("CallVisualiser.showCallInfo")); //$NON-NLS-1$
			showCallInfoItem.addSelectionListener(new SelectionAdapter() { 
				public void widgetSelected(SelectionEvent se) {
				    // based on the table's selected item, update the tables
					TableItem[] selections = calleeTable.getSelection();
					calleeTable.deselectAll();
					
					if (selections.length != 0)
						chooseNewFunction((CallerCalleeItem)selections[0].getData());
				}
			});
			
			new MenuItem(calleeMenu, SWT.SEPARATOR);
			
			MenuItem sourceLookupItem = new MenuItem(calleeMenu, SWT.PUSH);
			sourceLookupItem.setText(Messages.getString("CallVisualiser.sourcelookup"));	//$NON-NLS-1$
			sourceLookupItem.addSelectionListener(new SelectionListener() {

				public void widgetDefaultSelected(SelectionEvent arg0) {
				}

				public void widgetSelected(SelectionEvent arg0) {
					doSourceLookup(calleeTable);
				}
				
			});
			
			if (calleeTable.getSelectionCount() == 0) {
				showCallInfoItem.setEnabled(false);
				sourceLookupItem.setEnabled(false);
			}

			// add copy, copy all, and save all
			new MenuItem(calleeMenu, SWT.SEPARATOR);
			getCopyItem(calleeMenu, calleeTable.getSelectionCount() > 0);
			getCopyTableItem(calleeMenu, calleeTable.getItemCount() > 0);
			copyAction.setEnabled(calleeTable.getSelectionCount() > 0);
			copyTableAction.setEnabled(calleeTable.getItemCount() > 0);

			new MenuItem(calleeMenu, SWT.SEPARATOR);
			getSaveTableItem(calleeMenu, calleeTable.getItemCount() > 0);
			saveTableAction.setEnabled(calleeTable.getItemCount() > 0);
			
			// save samples
			int startTime = (int) (PIPageEditor.currentPageEditor().getStartTime() * 1000.0f);
			int endTime   = (int) (PIPageEditor.currentPageEditor().getEndTime()   * 1000.0f);

			if ((startTime == -1) || (endTime   == -1) || (startTime == endTime))
				getSaveSamplesItem(calleeMenu, false); //$NON-NLS-1$
			else
				getSaveSamplesItem(calleeMenu, true); //$NON-NLS-1$

			calleeMenu.setLocation(calleeTable.getParent().toDisplay(me.x, me.y));
			calleeMenu.setVisible(true);
			calleeTable.setMenu(calleeMenu);
		}
	}
	
	protected void createDefaultActions(boolean copyFunction)
	{
		copyAction = new Action(Messages.getString("CallVisualiser.0")) { //$NON-NLS-1$
			public void run() {
				action("copy"); //$NON-NLS-1$
			}
		};
		copyAction.setEnabled(false);

		copyTableAction = new Action(Messages.getString("CallVisualiser.1")) { //$NON-NLS-1$
			public void run() {
				action("copyTable"); //$NON-NLS-1$
			}
		};
		copyTableAction.setEnabled(true);
		copyTableAction.setId("PICopyTable"); //$NON-NLS-1$
		copyTableAction.setText(Messages.getString("CallVisualiser.CopyTable")); //$NON-NLS-1$

		saveTableAction = new Action(Messages.getString("CallVisualiser.2")) { //$NON-NLS-1$
			public void run() {
				action("saveTable"); //$NON-NLS-1$
			}
		};
		saveTableAction.setEnabled(true);
		saveTableAction.setId("PISaveTable"); //$NON-NLS-1$
		saveTableAction.setText(Messages.getString("CallVisualiser.SaveTable")); //$NON-NLS-1$

		functionCopyFunctionAction = new Action(Messages.getString("CallVisualiser.3")) { //$NON-NLS-1$
			public void run() {
				action("copyFunction"); //$NON-NLS-1$
			}
		};
		functionCopyFunctionAction.setEnabled(true);
		functionCopyFunctionAction.setId("PICopyFunction"); //$NON-NLS-1$
		functionCopyFunctionAction.setText(Messages.getString("CallVisualiser.CopyDataForFunction")); //$NON-NLS-1$

		functionSaveFunctionAction = new Action(Messages.getString("CallVisualiser.4")) { //$NON-NLS-1$
			public void run() {
				action("saveFunction"); //$NON-NLS-1$
			}
		};
		functionSaveFunctionAction.setEnabled(true);
		functionSaveFunctionAction.setId("PISaveFunction"); //$NON-NLS-1$
		functionSaveFunctionAction.setText(Messages.getString("CallVisualiser.SaveDataForFunction")); //$NON-NLS-1$
	}

	private class SharedLabelProvider extends LabelProvider implements ITableLabelProvider {
		
		Table table;

		public SharedLabelProvider(Table table) {
			super();
			this.table = table;
		}

		public String getColumnText(Object element, int columnIndex) {
			if (   !(element instanceof GfcFunctionItem)
				&& !(element instanceof CallerCalleeItem))
				return ""; //$NON-NLS-1$

			int columnId = ((Integer) this.table.getColumn(columnIndex).getData()).intValue();
			
			GfcFunctionItem item = null;
			
			if (element instanceof GfcFunctionItem)
				item = (GfcFunctionItem)element;
			else
				item = ((CallerCalleeItem)element).item;

			switch (columnId)
			{
				case COLUMN_ID_CALLEE_PERCENT:
				case COLUMN_ID_CALLER_PERCENT:
				{
					double percent;
					if (element instanceof CallerCalleeItem)
						percent = ((CallerCalleeItem)element).percent;
					else
						percent = 0.0;
						
					// Percent load string
					return (new DecimalFormat(Messages.getString("CallVisualiser.shortDecimalFormat"))).format(percent); //$NON-NLS-1$
				}
				case COLUMN_ID_FUNCTION:
				{
					// Function
					return item.name;
				}
				case COLUMN_ID_START_ADDR:
				{
					// Function start
					return Long.toHexString(item.address);
				}
				case COLUMN_ID_IN_BINARY:
				{
					// Binary
					String binary = item.dllName;
					int index = binary.lastIndexOf('\\');
					if (index == -1)
						return binary;
					else
						return binary.substring(index + 1);
				}
				case COLUMN_ID_IN_BINARY_PATH:
				{
					// Path
					String binary = item.dllName;
					int index = binary.lastIndexOf('\\');
					if (index == -1)
						return ""; //$NON-NLS-1$
					else
						return binary.substring(0, index);
				}
				case COLUMN_ID_IS_CALLED:
				{
					// Percent load string
					return (new DecimalFormat(Messages.getString("CallVisualiser.decimalFormat"))).format(myTrace.getAbsoluteTraditionalPercentageFor(item)); //$NON-NLS-1$
				}
				case COLUMN_ID_IS_CALLER:
				{
					// Percent load string
					return (new DecimalFormat(Messages.getString("CallVisualiser.decimalFormat"))).format(myTrace.getAbsoluteCallerPercentageFor(item)); //$NON-NLS-1$
				}
				case COLUMN_ID_RECURSIVE_CALL:
				{
					// Percent load string
					return (new DecimalFormat(Messages.getString("CallVisualiser.decimalFormat"))).format(myTrace.getRecursiveCallerPrecentageFor(item)); //$NON-NLS-1$
				}
				case COLUMN_ID_IS_CALLED_COUNT:
				{
					// Sample count
					int samples;
					if (element instanceof CallerCalleeItem)
						samples = ((CallerCalleeItem)element).samples;
					else
						samples = item.isCalledCount();
					return String.valueOf(samples);
				}
				case COLUMN_ID_IS_CALLER_COUNT:
				{
					// Sample count
					int samples;
					if (element instanceof CallerCalleeItem)
						samples = ((CallerCalleeItem)element).samples;
					else
						samples = item.isCallerCount();
					return String.valueOf(samples);
				}
				default:
				{
					break;
				}
			}
			// should never get here
			return ""; //$NON-NLS-1$
		}

		public Image getColumnImage(Object element, int columnIndex) {
			return null;
		}
	}

	public void selectFunction(String functionName)
	{
	}
  
	public void setStartAndEnd(int start, int end)
	{
		if (this.myTrace == null)
			return;
		
	    this.myTrace.parseEntries(start, end);
	    this.functionArray = myTrace.getEntriesSorted(GfcTrace.SORT_BY_TOTAL_LOAD);
	    this.currentFunctionTableViewer.setInput(this.functionArray);
		
	    updateCallerCalleeTables(null);
	    
	    Table table = this.currentFunctionTableViewer.getTable();
	    
	    if (table.getItemCount() == 0)
	    	return;
	    
	    if (table.getSortColumn() == null) {
	    	table.setSortColumn(currentFunctionDefaultColumn);
	    	table.setSortDirection(SWT.UP);
	    } else {
	    	// use the user's preferred sort column, if any
	    	boolean sortAscending = !((SharedSorter) currentFunctionTableViewer.getSorter()).getSortAscending();
			((SharedSorter) currentFunctionTableViewer.getSorter()).setSortAscending(sortAscending);
	    }
	    
	    sortAndRefresh(this.currentFunctionTableViewer, table.getSortColumn());
	}

	private static class CallerCalleeItem {
	    GfcFunctionItem item;
	    double percent;
	    int samples;
	}
	
	public void setCallerListToFunctionsThatCallThisFunction(GfcFunctionItem function)
	{
		if (function == null) {
			this.callerList = null;
			return;
		}

		GfcFunctionItem[] list = function.getCallerList();
	    Double[] perc = function.getCallerPercentages();
	
	    this.callerList = new CallerCalleeItem[list.length];
	    
	    for (int i = 0; i < list.length; i++)
	    {
	    	this.callerList[i] = new CallerCalleeItem();
	    	this.callerList[i].item    = list[i];
	    	this.callerList[i].percent = perc[i];
	    	this.callerList[i].samples = (int) (perc[i] * function.isCalledCount() + 0.5) / 100;
	    }
	}

	public void setCalleeListToFunctionsThisFunctionCalls(GfcFunctionItem function)
	{
		if (function == null) {
			this.calleeList = null;
			return;
		}

	    GfcFunctionItem[] list = function.getCalleeList();
	    Double[] perc = function.getCalleePercentages();
	
	    this.calleeList = new CallerCalleeItem[list.length];
	    
	    for (int i = 0; i < list.length; i++)
	    {
	    	this.calleeList[i] = new CallerCalleeItem();
	    	this.calleeList[i].item    = list[i];
	    	this.calleeList[i].percent = perc[i];
	    	this.calleeList[i].samples = (int) (perc[i] * function.isCallerCount() + 0.5) / 100;
	    }
	}

	public void action(String actionString) {
		if (actionString.equals("copy")) //$NON-NLS-1$
	    {
	    	actionCopyOrSave(true, currentMenuTable, CHECKBOX_NONE, false, "\t", "\n"); //$NON-NLS-1$ //$NON-NLS-2$
	        return;
	    }
	    else if (actionString.equals("copyTable")) //$NON-NLS-1$
	    {
	    	actionCopyOrSave(true, currentMenuTable, CHECKBOX_NONE, true, "\t", "\n"); //$NON-NLS-1$ //$NON-NLS-2$
	        return;
	    }
	    else if (actionString.equals("copyFunction")) //$NON-NLS-1$
	    {
	    	actionCopyOrSaveFunction(true, "\t"); //$NON-NLS-1$
	        return;
	    }
	    else if (actionString.equals("saveTable")) //$NON-NLS-1$
	    {
	    	actionCopyOrSave(false, currentMenuTable, CHECKBOX_NONE, true, ",", "\n"); //$NON-NLS-1$ //$NON-NLS-2$
	        return;
	    }
	    else if (actionString.equals("saveFunction")) //$NON-NLS-1$
	    {
	    	actionCopyOrSaveFunction(false, ","); //$NON-NLS-1$
	        return;
	    }
	    else if (actionString.equals("saveSamples")) //$NON-NLS-1$
	    {
	    	SaveSampleString saveSampleString = new SaveSampleString();
	    	actionSaveSamples(saveSampleString); //$NON-NLS-1$
	        return;
	    }
	    else if (actionString.equals("saveTableTest")) //$NON-NLS-1$
	    {
			// copy save file contents to the clipboard for easy viewing
	        Clipboard cb = Toolkit.getDefaultToolkit().getSystemClipboard();
			SaveTableString getString = new SaveTableString(currentMenuTable, CHECKBOX_NONE, ",", "\n"); //$NON-NLS-1$ //$NON-NLS-2$
	        String copyString = getString.getData();
			StringSelection contents = new StringSelection(copyString);
	        cb.setContents(contents, contents);
	        return;
	    }
	    else if (actionString.equals("saveFunctionTest")) //$NON-NLS-1$
	    {
			// copy save file contents to the clipboard for easy viewing
	        Clipboard cb = Toolkit.getDefaultToolkit().getSystemClipboard();
	        SaveFunctionString getString = new SaveFunctionString(","); //$NON-NLS-1$
	        String copyString = getString.getData();
			StringSelection contents = new StringSelection(copyString);
	        cb.setContents(contents, contents);
	        return;
	    }
	}
	
	/*
	 * TableViewer sorter for the called-by and called function tableviewers
	 */
	private class SharedSorter extends ViewerSorter {
		// sort direction
		private boolean sortAscending;
		
		// last column sorted
		private int column = -1;
		
		/* 
		 * decide on which column to sort by, and the sort ordering
		 */
		public void doSort(int column) {
			// ignore the column passed in and use the id set by the column selection handler
			if (column == this.column) {
				// sort in other order
				sortAscending = !sortAscending;
			} else {
				switch (column) {
					case COLUMN_ID_FUNCTION:
					case COLUMN_ID_START_ADDR:
					case COLUMN_ID_IN_BINARY:
					case COLUMN_ID_IN_BINARY_PATH:
					{
		            	// sort in ascending order
		            	sortAscending = true;
		                break;
					}
					case COLUMN_ID_IS_CALLED:
					case COLUMN_ID_IS_CALLER:
					case COLUMN_ID_RECURSIVE_CALL:
					case COLUMN_ID_CALLER_PERCENT:
					case COLUMN_ID_CALLEE_PERCENT:
					case COLUMN_ID_IS_CALLED_COUNT:
					case COLUMN_ID_IS_CALLER_COUNT:
					{
		            	// sort in descending order
		            	sortAscending = false;
		                break;
					}
					default:
					{
						// ignore the column
						return;
					}
				}
				this.column = column;
			}
		}
		
		/*
		 * compare two items from a table column
		 */
		public int compare(Viewer viewer, Object e1, Object e2) {
			int comparison = 0;
			
			GfcFunctionItem item1 = null;
			GfcFunctionItem item2 = null;
			CallerCalleeItem ccItem1 = null;
			CallerCalleeItem ccItem2 = null;
			
			if (e1 instanceof GfcFunctionItem) {
				item1 = (GfcFunctionItem) e1;
				item2 = (GfcFunctionItem) e2;
			} else {
				ccItem1 = (CallerCalleeItem)e1;
				ccItem2 = (CallerCalleeItem)e2;
				item1 =   ccItem1.item;
				item2 =   ccItem2.item;
			}

			switch (column) {
				case COLUMN_ID_CALLER_PERCENT:
				case COLUMN_ID_CALLEE_PERCENT:
				{
					double percent1;
					double percent2;
					if (e1 instanceof GfcFunctionItem) {
						percent1 = 0.0;
						percent2 = 0.0;
					} else {
						percent1 = ccItem1.percent;
						percent2 = ccItem2.percent;
					}
					comparison = percent1 > percent2 ? 1 : -1;
					break;
				}
				case COLUMN_ID_FUNCTION:
				{
					comparison = this.getComparator().compare(item1.name, item2.name);
					break;
				}
				case COLUMN_ID_START_ADDR:
				{
					comparison = (item1.address > item2.address) ? 1 : -1; 
					break;
				}
				case COLUMN_ID_IN_BINARY:
				{
					int index;
					String name1 = item1.dllName;
					index = name1.lastIndexOf('\\');
					if (index != -1)
						name1 = name1.substring(index);

					String name2 = item2.dllName;
					index = name2.lastIndexOf('\\');
					if (index != -1)
						name2 = name2.substring(index);

					comparison = this.getComparator().compare(name1, name2);
					break;
				}
				case COLUMN_ID_IN_BINARY_PATH:
				{
					int index;
					String name1 = item1.dllName;
					index = name1.lastIndexOf('\\');
					if (index == -1)
						name1 = ""; //$NON-NLS-1$
					else
						name1 = name1.substring(0, index);

					String name2 = item2.dllName;
					index = name2.lastIndexOf('\\');
					if (index == -1)
						name2 = ""; //$NON-NLS-1$
					else
						name2 = name2.substring(0, index);

					comparison = this.getComparator().compare(name1, name2);
					break;
				}
				case COLUMN_ID_IS_CALLED:
				{
					// actual sample count used as a proxy for the percentage
					comparison = item1.isCalledCount() - item2.isCalledCount();
					break;
				}
				case COLUMN_ID_IS_CALLER:
				{
					// actual sample count used as a proxy for the percentage
					comparison = item1.isCallerCount() - item2.isCallerCount();
					break;
				}
				case COLUMN_ID_RECURSIVE_CALL:
				{
					comparison = myTrace.getRecursiveCallerPrecentageFor(item1) >
								 myTrace.getRecursiveCallerPrecentageFor(item2) ? 1 : -1;
					break;
				}
				case COLUMN_ID_IS_CALLED_COUNT:
				{
					if (e1 instanceof GfcFunctionItem) {
						comparison = item1.isCalledCount() - item2.isCalledCount();
					} else {
						comparison = ccItem1.samples - ccItem2.samples;
					}
					break;
				}
				case COLUMN_ID_IS_CALLER_COUNT:
				{
					if (e1 instanceof GfcFunctionItem) {
						comparison = item1.isCallerCount() - item2.isCallerCount();
					} else {
						comparison = ccItem1.samples - ccItem2.samples;
					}
					break;
				}
				default:
				{
					break;
				}
			}

			// for descending order, reverse the sense of the compare
			if (!sortAscending)
				comparison = -comparison;
			return comparison;
		}
		
		public boolean getSortAscending() {
			return sortAscending;
		}
		
		public void setSortAscending(boolean sortAscending) {
			this.sortAscending = sortAscending;
		}
		
		public int getColumnID() {
			return column;
		}
	}
	
	public void sortAndRefresh(TableViewer tableViewer, TableColumn tableColumn)
	{
		Table table = tableViewer.getTable();
    	int columnID = ((Integer) tableColumn.getData()).intValue();
    	
    	if (table.getItemCount() == 0)
    		return;
		
		// sort by selected columnID
    	((SharedSorter) tableViewer.getSorter()).doSort(columnID);
		table.setSortColumn(tableColumn);
		table.setSortDirection(((SharedSorter) tableViewer.getSorter()).getSortAscending() ? SWT.UP : SWT.DOWN);
		tableViewer.refresh();
	}

	private class CalledByColumnSelectionHandler extends SelectionAdapter
	{
		public void widgetSelected(SelectionEvent e)
        {
        	if (!(e.widget instanceof TableColumn))
        		return;
        	
        	sortAndRefresh(callerTableViewer, (TableColumn) e.widget);
       }
	}

	private class CalledColumnSelectionHandler extends SelectionAdapter
	{
		public void widgetSelected(SelectionEvent e)
        {
        	if (!(e.widget instanceof TableColumn))
        		return;

        	sortAndRefresh(calleeTableViewer, (TableColumn) e.widget);
        }
	}

	private class CheckboxColumnSelectionHandler extends SelectionAdapter
	{
		public void widgetSelected(SelectionEvent e)
        {
        	if (!(e.widget instanceof TableColumn))
        		return;

        	sortAndRefresh(currentFunctionTableViewer, (TableColumn) e.widget);
        }
	}
	
	public PIPageEditor getPageEditor()
	{
		return this.pageEditor;
	}
	
	public int getPageIndex()
	{
		return this.pageIndex;
	}
	
	private MenuItem getCopyFunctionItem(Menu menu, boolean enabled) {
	    MenuItem copyFunctionItem = new MenuItem(menu, SWT.PUSH);
	    
		copyFunctionItem.setText(Messages.getString("CallVisualiser.CopyDataForFunction")); //$NON-NLS-1$
		copyFunctionItem.setEnabled(enabled);
		
		if (enabled) {
			copyFunctionItem.addSelectionListener(new SelectionAdapter() { 
				public void widgetSelected(SelectionEvent e) {
				    action("copyFunction"); //$NON-NLS-1$
				}
			});
		}
		
		return copyFunctionItem;
	}
	
	private MenuItem getSaveFunctionItem(Menu menu, boolean enabled) {
	    MenuItem saveFunctionItem = new MenuItem(menu, SWT.PUSH);
	    
		saveFunctionItem.setText(Messages.getString("CallVisualiser.SaveDataForFunction")); //$NON-NLS-1$
		saveFunctionItem.setEnabled(enabled);
		
		if (enabled) {
			saveFunctionItem.addSelectionListener(new SelectionAdapter() { 
				public void widgetSelected(SelectionEvent e) {
				    action("saveFunction"); //$NON-NLS-1$
				}
			});
		}
		
		return saveFunctionItem;
	}

	private void doSourceLookup(Table mytable) {
		// source look up for selected items
		if ((mytable.getItemCount() == 0) || (mytable.getSelectionCount() == 0))
			return;

		TableItem selectedItem = mytable.getSelection()[0];
		Object selectedData = selectedItem.getData();
		String functionName = null;
		String binaryName = null;
		if (selectedData instanceof GfcFunctionItem) {
			GfcFunctionItem functionItem = (GfcFunctionItem) selectedData;
			functionName = functionItem.name;
			binaryName = functionItem.dllName;
		} else if (selectedData instanceof CallerCalleeItem) {
			CallerCalleeItem callerCalleeItem = (CallerCalleeItem) selectedData;
			functionName = callerCalleeItem.item.name;
			binaryName = callerCalleeItem.item.dllName;
		}
		if (functionName != null && binaryName != null)
		{
			SourceLookup.getInstance().lookupAndopenEditorWithHighlight(functionName , binaryName);
		}

	}
	
	// class to pass the function caller/callee data to the save wizard
	private class SaveFunctionString implements ISaveTable {
		private String separator;
		
		public SaveFunctionString(String separator) {
			this.separator = separator;
		}

		public String getData() {
			return copyFunction(separator);
		}
	}
	
    private void actionCopyOrSaveFunction(boolean doCopy, String separator)
    {
    	// one function must be selected
    	if (currentFunctionTable.getSelectionCount() != 1)
    		return;
 
		// copy one function's caller and callee data to the clipboard or save to a file
		if (doCopy) {
			// change the clipboard contents
	        Clipboard cb = Toolkit.getDefaultToolkit().getSystemClipboard();
			String copyString = copyFunction(separator);
			StringSelection contents = new StringSelection(copyString);
	        cb.setContents(contents, contents);
	    } else {
			SaveFunctionString getString = new SaveFunctionString(separator);
			WizardDialog dialog;
			Wizard w = new SaveTableWizard(getString);
			dialog = new WizardDialog(PlatformUI.getWorkbench().getDisplay().getActiveShell(), w);
	    	dialog.open();
	    }
    }

    private String copyFunction(String separator)
	{	
    	// one function must be selected
    	if (currentFunctionTable.getSelectionCount() != 1)
			return ""; //$NON-NLS-1$
		
		String copyString = ""; //$NON-NLS-1$

		// create the multiple table heading line (e.g., "Selected Function     Callers")
		// space them out based on how many columns are in their table
		if (currentFunctionTable.getData() instanceof String) {
			copyString += (String) (currentFunctionTable.getData());
		}
		for (int j = 0; j < currentFunctionTable.getColumnCount(); j++) {
			copyString += separator;
		}

		if (callerTable.getData() instanceof String) {
			copyString += (String) (callerTable.getData());
		}
		for (int j = 0; j < callerTable.getColumnCount(); j++) {
			copyString += separator;
		}

		if (calleeTable.getData() instanceof String) {
			copyString += (String) (calleeTable.getData());
		}
		for (int j = 0; j < calleeTable.getColumnCount(); j++) {
			copyString += separator;
		}

		copyString += "\n"; //$NON-NLS-1$
		
		// create the multiple table column headings
		copyString += copyHeading(currentFunctionTable, CHECKBOX_NONE, separator, separator); //$NON-NLS-1$
		copyString += copyHeading(callerTable, CHECKBOX_NONE, separator, separator); //$NON-NLS-1$
		copyString += copyHeading(calleeTable, CHECKBOX_NONE, separator, "\n"); //$NON-NLS-1$
		
		// determine the row, column count, and column ordering in each table
		// NOTE: the first table in the copy will contain the function, and it only its
		// one selected line will be shown
		int rowCount0      = 1;
		int columnCount0   = currentFunctionTable.getColumnCount();
		int[] columnOrder0 = currentFunctionTable.getColumnOrder();
		boolean[] isHex0   = (boolean[]) currentFunctionTable.getData("isHex"); //$NON-NLS-1$
		String emptyRow0 = ""; //$NON-NLS-1$

		int rowCount1      = callerTable.getItemCount();
		int columnCount1   = callerTable.getColumnCount();
		int[] columnOrder1 = callerTable.getColumnOrder();
		boolean[] isHex1   = (boolean[]) callerTable.getData("isHex"); //$NON-NLS-1$
		String emptyRow1 = ""; //$NON-NLS-1$

		int rowCount2      = calleeTable.getItemCount();
		int columnCount2   = calleeTable.getColumnCount();
		int[] columnOrder2 = calleeTable.getColumnOrder();
		boolean[] isHex2   = (boolean[]) calleeTable.getData("isHex"); //$NON-NLS-1$
		String emptyRow2 = ""; //$NON-NLS-1$

		// determine the number of multiple table rows (max of any table's rows) 
		int rowCount = rowCount0 >= rowCount1 ? rowCount0 : rowCount1;
		rowCount = rowCount2 > rowCount ? rowCount2 : rowCount;
		
		// generate empty row strings, to speed things up
		if (rowCount0 < rowCount) {
			for (int j = 0; j < columnCount0 - 1; j++) {
				emptyRow0 += separator;
			}
		}

		if (rowCount1 < rowCount) {
			for (int j = 0; j < columnCount1; j++) {
				emptyRow1 += separator;
			}
		}

		if (rowCount2 < rowCount) {
			for (int j = 0; j < columnCount2; j++) {
				emptyRow2 += separator;
			}
		}

		// generate the rows
		for (int i = 0; i < rowCount; i++) {
			if (i < 1) {
				copyString += copyRow(isHex0, currentFunctionTable.getItem(currentFunctionTable.getSelectionIndex()),
										CHECKBOX_NONE, columnCount0, columnOrder0, separator);
			} else {
				copyString += emptyRow0;
			}
			
			if (i < rowCount1) {
				copyString += separator + copyRow(isHex1, callerTable.getItem(i), CHECKBOX_NONE, columnCount1, columnOrder1, separator);
			} else {
				// NOTE: if this is the last table, or the 3rd table has nothing but empty
				// rows left, we may not need to fill in these fields
				copyString += emptyRow1;
			}
			
			if (i < rowCount2) {
				copyString += separator + copyRow(isHex2, calleeTable.getItem(i), CHECKBOX_NONE, columnCount2, columnOrder2, separator);
			} else {
				// NOTE: we may not need to fill in the empty fields of the last table
				copyString += emptyRow2;
			}
			
			copyString += "\n"; //$NON-NLS-1$
		}
		
		return copyString;
	}
	
    // added to give JUnit tests access
	public TableViewer getCallerViewer() {
		return this.callerTableViewer;
	}
	
    // added to give JUnit tests access
	public TableViewer getCurrentFunctionViewer() {
		return this.currentFunctionTableViewer;
	}
	
    // added to give JUnit tests access
	public TableViewer getCalleeViewer() {
		return this.calleeTableViewer;
	}
	
    // added to give JUnit tests access
	public void setCurrentMenuTable(Table currentMenuTable) {
		this.currentMenuTable = currentMenuTable;
	}
}
