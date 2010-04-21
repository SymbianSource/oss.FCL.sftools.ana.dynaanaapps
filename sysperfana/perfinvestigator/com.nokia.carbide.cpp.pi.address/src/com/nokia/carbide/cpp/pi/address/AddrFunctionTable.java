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

package com.nokia.carbide.cpp.pi.address;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.FocusEvent;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.Vector;

import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Decorations;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Sash;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

import com.nokia.carbide.cpp.internal.pi.address.GppModelAdapter;
import com.nokia.carbide.cpp.internal.pi.analyser.NpiInstanceRepository;
import com.nokia.carbide.cpp.internal.pi.analyser.ProfileVisualiser;
import com.nokia.carbide.cpp.internal.pi.model.ProfiledFunction;
import com.nokia.carbide.cpp.internal.pi.model.ProfiledGeneric;
import com.nokia.carbide.cpp.internal.pi.model.ProfiledThreshold;
import com.nokia.carbide.cpp.internal.pi.visual.Defines;
import com.nokia.carbide.cpp.internal.pi.visual.PIEvent;
import com.nokia.carbide.cpp.internal.pi.visual.PIVisualSharedData;
import com.nokia.carbide.cpp.pi.editors.PIPageEditor;
import com.nokia.carbide.cpp.pi.util.SourceLookup;
import com.nokia.carbide.cpp.pi.util.TableColorPalette;


public class AddrFunctionTable extends GenericAddrTable
{
	// local copy of profiled binaries, which we can sort
	// without affecting the original
	Vector<ProfiledGeneric> profiledFunctions = new Vector<ProfiledGeneric>();

	public AddrFunctionTable(GppTraceGraph myGraph, Composite parent, GppModelAdapter adapter)
	{
		super(myGraph, parent, adapter);
	}

	public void createTableViewer(int drawMode)
	{
		if (this.parent == null)
			return;

		// Function table:
		//		checkbox + colored or white background
		//  	percent load
		//  	function name
		//		function start address
		//		binary containing function
		//		path of binary containing function
		//		sample count

		// Check the drawMode, and use it to decide whether or not to show a color column
		// or the number of samples
		switch (drawMode)
		{
			case Defines.FUNCTIONS:
			case Defines.THREADS_BINARIES_FUNCTIONS:
			case Defines.BINARIES_FUNCTIONS:
			case Defines.THREADS_FUNCTIONS:
			case Defines.BINARIES_THREADS_FUNCTIONS:
			case Defines.THREADS_FUNCTIONS_BINARIES:
			case Defines.BINARIES_FUNCTIONS_THREADS:
			case Defines.FUNCTIONS_THREADS:
			case Defines.FUNCTIONS_THREADS_BINARIES:
			case Defines.FUNCTIONS_BINARIES:
			case Defines.FUNCTIONS_BINARIES_THREADS:
			{
				break;
			}
			default:
				// no function table in this draw mode
				return;
		}

		// create the table viewer
		this.tableViewer = CheckboxTableViewer.newCheckList(this.parent,
  				SWT.BORDER | SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION);

		// add the check state handler, label provider and content provider
		tableViewer.addCheckStateListener(new SharedCheckHandler());
		tableViewer.setLabelProvider(new shownFunctionsLabelProvider());
		tableViewer.setContentProvider(new shownFunctionsContentProvider());
		tableViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent arg0) {
				if (copyAction == null)
					return;

				// when selection changes, the ability to copy may change
				copyAction.setEnabled(table.getSelectionCount() > 0);
				PIPageEditor.getActionBars().updateActionBars();
			}
		});

		createDefaultActions();

		// make sure the table viewer has a sorter
		if (this.sorter == null)
			this.sorter = new GppTableSorter();

		this.table = tableViewer.getTable();
		this.table.setRedraw(false);

		// give the table a heading for use in copying and exported
		this.table.setData(Messages.getString("AddrFunctionTable.functions")); //$NON-NLS-1$
		
		// create the columns
		TableColumn column;

		// data associated with the TableViewer will note which columns contain hex values
		// Keep this in the order in which columns have been created
		boolean[] isHex = {false, false, false, true, false, false, false};
		this.table.setData("isHex", isHex); //$NON-NLS-1$

		// select/deselect column
		column = new TableColumn(table, SWT.CENTER);
		column.setText(COLUMN_HEAD_SHOW);
		column.setWidth(COLUMN_WIDTH_SHOW);
		column.setData(Integer.valueOf(COLUMN_ID_SHOW));
		column.setMoveable(true);
		column.setResizable(true);
		column.addSelectionListener(new ColumnSelectionHandler());

		// percent load column
		column = new TableColumn(table, SWT.RIGHT);
		column.setText(COLUMN_HEAD_PERCENT_LOAD);
		column.setWidth(COLUMN_WIDTH_PERCENT_LOAD);
		column.setData(Integer.valueOf(COLUMN_ID_PERCENT_LOAD));
		column.setMoveable(true);
		column.setResizable(true);
		column.addSelectionListener(new ColumnSelectionHandler());

		// function name column
		column = new TableColumn(table, SWT.LEFT);
		column.setText(COLUMN_HEAD_FUNCTION);
		column.setWidth(COLUMN_WIDTH_FUNCTION_NAME);
		column.setData(Integer.valueOf(COLUMN_ID_FUNCTION));
		column.setMoveable(true);
		column.setResizable(true);
		column.addSelectionListener(new ColumnSelectionHandler());

		// function start address column
		column = new TableColumn(table, SWT.CENTER);
		column.setText(COLUMN_HEAD_START_ADDR);
		column.setWidth(COLUMN_WIDTH_START_ADDRESS);
		column.setData(Integer.valueOf(COLUMN_ID_START_ADDR));
		column.setMoveable(true);
		column.setResizable(true);
		column.addSelectionListener(new ColumnSelectionHandler());

		// binary containing function column
		column = new TableColumn(table, SWT.LEFT);
		column.setText(COLUMN_HEAD_IN_BINARY);
		column.setWidth(COLUMN_WIDTH_IN_BINARY);
		column.setData(Integer.valueOf(COLUMN_ID_IN_BINARY));
		column.setMoveable(true);
		column.setResizable(true);
		column.addSelectionListener(new ColumnSelectionHandler());

		// path to binary containing function column
		column = new TableColumn(table, SWT.LEFT);
		column.setText(COLUMN_HEAD_IN_BINARY_PATH);
		column.setWidth(COLUMN_WIDTH_IN_BINARY_PATH);
		column.setData(Integer.valueOf(COLUMN_ID_IN_BINARY_PATH));
		column.setMoveable(true);
		column.setResizable(true);
		column.addSelectionListener(new ColumnSelectionHandler());

		// sample count column
		column = new TableColumn(table, SWT.CENTER);
		column.setText(COLUMN_HEAD_SAMPLE_COUNT);
		column.setWidth(COLUMN_WIDTH_SAMPLE_COUNT);
		column.setData(Integer.valueOf(COLUMN_ID_SAMPLE_COUNT));
		column.setMoveable(true);
		column.setResizable(true);
		column.addSelectionListener(new ColumnSelectionHandler());

		// listen for mouse clicks: to select a row, pop up a menu, etc.
		table.addMouseListener(new TableMouseListener());

		// listen for key sequences such as Ctrl-A and Ctrl-C
		table.addKeyListener(new TableKeyListener());

		table.addFocusListener(new AddrTableFocusListener());

		// add form data in case later we add a sash to the right
		FormData viewerData = new FormData();
		viewerData.top    = new FormAttachment(0);
		viewerData.bottom = new FormAttachment(100);
		viewerData.left   = new FormAttachment(0);
		viewerData.right  = new FormAttachment(100);
		table.setLayoutData(viewerData);
		table.setLayout(new FormLayout());

		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		table.setRedraw(true);
	}

	public void setTableViewer(CheckboxTableViewer tableViewer)
	{
		this.tableViewer = tableViewer;

		if (tableViewer == null)
			this.table = null;
	}

	public void setTableViewer(int drawMode)
	{
		if (this.parent == null)
			return;

		createTableViewer(drawMode);

		// sort by sample count
		this.sortColumn = COLUMN_ID_SAMPLE_COUNT;
		this.sortAscending = false;

		// profiledFunctions and tableItemData contain one entry per table row
		updateProfiledAndItemData(false);
		quickSort(sortColumn, profiledFunctions);

		// initially, all rows are selected
		this.tableViewer.setAllChecked(true);
	}

	public void refreshTableViewer()
	{
		if (this.tableViewer == null)
			return;

		this.tableViewer.setInput(tableItemData);
		
		addColor(this.myGraph.getDrawMode());
	}
	
	public void addColor(int drawMode)
	{
		if (this.tableViewer == null)
			return;

		// make sure that this table's colors are being shown
		if (   (drawMode != Defines.FUNCTIONS)
			&& (drawMode != Defines.THREADS_FUNCTIONS)
			&& (drawMode != Defines.THREADS_BINARIES_FUNCTIONS)
			&& (drawMode != Defines.BINARIES_FUNCTIONS)
			&& (drawMode != Defines.BINARIES_THREADS_FUNCTIONS))
			return;

		ProfiledGeneric pGeneric;

		TableItem[] items = this.table.getItems();

		for (int i = 0; i < items.length; i++) {
			pGeneric = (ProfiledGeneric) items[i].getData();
//			Color color = ((GppTrace)this.myGraph.getTrace()).getFunctionColorPalette().getColor(pGeneric.getNameString());
			items[i].setBackground(COLOR_COLUMN_INDEX, pGeneric.getColor());
		}

		table.redraw();
	}
	
	public void removeColor(int drawMode)
	{
		if (this.tableViewer == null)
			return;

		// make sure that this table's colors should not be shown
		if (   (drawMode == Defines.FUNCTIONS)
			|| (drawMode == Defines.THREADS_FUNCTIONS)
			|| (drawMode == Defines.THREADS_BINARIES_FUNCTIONS)
			|| (drawMode == Defines.BINARIES_FUNCTIONS)
			|| (drawMode == Defines.BINARIES_THREADS_FUNCTIONS))
			return;

		TableItem[] items = this.table.getItems();

		for (int i = 0; i < items.length; i++) {
			items[i].setBackground(COLOR_COLUMN_INDEX, this.parent.getDisplay().getSystemColor(SWT.COLOR_WHITE));
		}

		table.redraw();
	}

	private static class shownFunctionsContentProvider implements IStructuredContentProvider {

		public shownFunctionsContentProvider() {
			super();
		}

		public Object[] getElements(Object inputElement) {
			return ((Vector<?>) inputElement).toArray();
		}

		public void dispose() {
		}

		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}
	}

	private class shownFunctionsLabelProvider extends LabelProvider implements ITableLabelProvider {

		public shownFunctionsLabelProvider() {
			super();
		}

		/* (non-Javadoc)
		 * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnText(java.lang.Object, int)
		 */
		public String getColumnText(Object element, int columnIndex) {
			int columnId = ((Integer) table.getColumn(columnIndex).getData()).intValue();

			if (element instanceof ProfiledThreshold) {
				ProfiledThreshold pThreshold = (ProfiledThreshold) element;
				switch (columnId)
				{
					case COLUMN_ID_SHOW:
					{
						return SHOW_ITEM_VALUE;
					}
					case COLUMN_ID_PERCENT_LOAD:
					{
						// Percent load string
						double startTime = PIPageEditor.currentPageEditor().getStartTime();
						double endTime   = PIPageEditor.currentPageEditor().getEndTime();
						if (   (startTime == -1)
							|| (endTime   == -1)
							|| (startTime == endTime)) {
							pThreshold.setAverageLoadValueString(myGraph.getGraphIndex(), ""); //$NON-NLS-1$
						} else {
							float load = (float) (pThreshold.getSampleCount(myGraph.getGraphIndex())/(endTime - startTime)/10.0);
							
							if (load < 0.005)
								pThreshold.setAverageLoadValueString(myGraph.getGraphIndex(), Messages.getString("AddrFunctionTable.zeroFormat")); //$NON-NLS-1$
							else
								pThreshold.setAverageLoadValueString(myGraph.getGraphIndex(), load);
						}
						return pThreshold.getAverageLoadValueString(myGraph.getGraphIndex());
					}
					case COLUMN_ID_FUNCTION:
					{
						DecimalFormat timeFormat = new DecimalFormat(Messages.getString("AddrFunctionTable.decimalFormat")); //$NON-NLS-1$
						int count = pThreshold.getItemCount();

						return count + (count > 1 ? Messages.getString("AddrFunctionTable.threshold1") : Messages.getString("AddrFunctionTable.threshold2"))   //$NON-NLS-1$ //$NON-NLS-2$
								+ timeFormat.format((Double)NpiInstanceRepository.getInstance().activeUidGetPersistState("com.nokia.carbide.cpp.pi.address.thresholdLoadThread") * 100.0) + Messages.getString("AddrFunctionTable.threshold3")  //$NON-NLS-1$ //$NON-NLS-2$
								+ (Integer)NpiInstanceRepository.getInstance().activeUidGetPersistState("com.nokia.carbide.cpp.pi.address.thresholdCountFunction") + Messages.getString("AddrFunctionTable.threshold4"); //$NON-NLS-1$ //$NON-NLS-2$	
					}
					case COLUMN_ID_SAMPLE_COUNT:
					{
						// Sample count
						return String.valueOf(pThreshold.getSampleCount(myGraph.getGraphIndex()));
					}
					default:
					{
						return ""; //$NON-NLS-1$
					}
				}
			}

			if (!(element instanceof ProfiledFunction))
				return ""; //$NON-NLS-1$

			ProfiledFunction profiledItem = (ProfiledFunction) element;

			switch (columnId)
			{
				case COLUMN_ID_SHOW:
				{
					return SHOW_ITEM_VALUE;
				}
				case COLUMN_ID_PERCENT_LOAD:
				{
					// Percent load string
					return profiledItem.getAverageLoadValueString(myGraph.getGraphIndex());
				}
				case COLUMN_ID_FUNCTION:
				{
					// Function
					return (profiledItem.getNameString());
				}
				case COLUMN_ID_START_ADDR:
				{
					// Function start
					return (Long.toHexString(profiledItem.getFunctionAddress()));
				}
				case COLUMN_ID_IN_BINARY:
				{
					// Binary
					String binary = profiledItem.getFunctionBinaryName();
					int index = binary.lastIndexOf('\\');
					if (index == -1)
						return binary;
					else
						return binary.substring(index + 1);
				}
				case COLUMN_ID_IN_BINARY_PATH:
				{
					// Path
					String binary = profiledItem.getFunctionBinaryName();
					int index = binary.lastIndexOf('\\');
					if (index == -1)
						return ""; //$NON-NLS-1$
					else
						return binary.substring(0, index);
				}
				case COLUMN_ID_SAMPLE_COUNT:
				{
					// Sample count
					return String.valueOf(profiledItem.getSampleCount(myGraph.getGraphIndex()));
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

    @Override
	public void action(String actionString)
	{
		int graphIndex = this.myGraph.getGraphIndex();

		if (   actionString.equals("add") //$NON-NLS-1$
			|| actionString.equals("remove")) //$NON-NLS-1$
	    {
			actionAddRemove(actionString, graphIndex);
			return;
	    }
		else if (   actionString.equals("addall") //$NON-NLS-1$
				 || actionString.equals("removeall")) //$NON-NLS-1$
	    {
			actionAddRemoveAll(actionString, graphIndex);
			return;
	    }
		else if (actionString.equals("recolor")) //$NON-NLS-1$
		{
			actionRecolor();
			return;
		}
	    else if (actionString.equals("copy")) //$NON-NLS-1$
	    {
	    	actionCopyOrSave(true, this.table, CHECKBOX_NO_TEXT, false, "\t", "\n"); //$NON-NLS-1$ //$NON-NLS-2$
	        return;
	    }
	    else if (actionString.equals("copyTable")) //$NON-NLS-1$
	    {
	    	actionCopyOrSave(true, this.table, CHECKBOX_NO_TEXT, true, "\t", "\n"); //$NON-NLS-1$ //$NON-NLS-2$
	        return;
	    }
	    else if (actionString.equals("copyDrilldown")) //$NON-NLS-1$
	    {
	    	actionCopyOrSaveDrilldown(true, "\t"); //$NON-NLS-1$
	        return;
	    }
	    else if (actionString.equals("saveTable")) //$NON-NLS-1$
	    {
	    	actionCopyOrSave(false, this.table, CHECKBOX_NO_TEXT, true, ",", "\n"); //$NON-NLS-1$ //$NON-NLS-2$
	        return;
	    }
	    else if (actionString.equals("saveDrilldown")) //$NON-NLS-1$
	    {
	    	actionCopyOrSaveDrilldown(false, ","); //$NON-NLS-1$
	        return;
	    }
	    else if (actionString.equals("saveSamples")) //$NON-NLS-1$
	    {
	    	SaveSampleString saveSampleString = new SaveSampleString(graphIndex, myGraph.getDrawMode());
	    	actionSaveSamples(saveSampleString); //$NON-NLS-1$
	        return;
	    }
		else if (actionString.equals("selectAll")) //$NON-NLS-1$
	    {
	    	actionSelectAll();
	        return;
	    }
		else if (actionString.equals("doubleClick")) //$NON-NLS-1$
	    {
	    	copyAction.setEnabled(false);
			PIPageEditor.getActionBars().updateActionBars();
	        return;
	    }
		else if (actionString.equals("sortinfullpath")) //$NON-NLS-1$
		{
            if (this.sortColumn == COLUMN_ID_FULL_IN_PATH)
            	// sort in other order
            	sortAscending = !sortAscending;
            else
            	// sort in ascending order
            	sortAscending = true;
            sortColumn = COLUMN_ID_FULL_IN_PATH;
            quickSort(sortColumn, profiledFunctions);
		}
		else if (actionString.equals("function-only")) //$NON-NLS-1$
		{
			actionFunction();
			return;
		}
		else if (actionString.equals("function-thread")) //$NON-NLS-1$
		{
			actionFunctionThread();
			return;
		}
		else if (actionString.equals("function-thread-binary")) //$NON-NLS-1$
		{
			actionFunctionThreadBinary();
			return;
		}
		else if (actionString.equals("function-binary")) //$NON-NLS-1$
		{
			actionFunctionBinary();
			return;
		}
		else if (actionString.equals("function-binary-thread")) //$NON-NLS-1$
		{
			actionFunctionBinaryThread();
			return;
		}
		else if (   (actionString.equals("thread-only")) //$NON-NLS-1$
				 || (actionString.equals("thread-binary")) //$NON-NLS-1$
				 || (actionString.equals("thread-binary-function")) //$NON-NLS-1$
				 || (actionString.equals("thread-function")) //$NON-NLS-1$
				 || (actionString.equals("thread-function-binary"))) //$NON-NLS-1$
		{
			// let the thread page action handler handle it
			this.myGraph.getThreadTable().action(actionString);
			return;
		}
		else if (   (actionString.equals("binary-only")) //$NON-NLS-1$
				 || (actionString.equals("binary-thread")) //$NON-NLS-1$
				 || (actionString.equals("binary-thread-function")) //$NON-NLS-1$
				 || (actionString.equals("binary-function")) //$NON-NLS-1$
				 || (actionString.equals("binary-function-thread"))) //$NON-NLS-1$
		{
			// let the binary page action handler handle it
			this.myGraph.getBinaryTable().action(actionString);
			return;
		}
		else if (actionString.equals("changeThresholdFunction")) //$NON-NLS-1$
		{
			ProfiledThreshold threshold = this.myGraph.getThresholdFunction();
			boolean enabled = threshold.isEnabled(graphIndex);

			this.tableItemData.clear();
			this.profiledFunctions.clear();
			this.myGraph.getSortedFunctions().clear();
			if (threshold.getItems() != null)
				threshold.getItems().clear();
			adapter.init(threshold, graphIndex);

			// if this appears, it needs to be the first item, so that it is drawn at the bottom
			myGraph.getSortedFunctions().add(threshold);

			int functionThreshold = (Integer)NpiInstanceRepository.getInstance().activeUidGetPersistState("com.nokia.carbide.cpp.pi.address.thresholdCountFunction"); //$NON-NLS-1$
			for (int i = 0; i < this.myGraph.getGppTrace().getSortedFunctions().size(); i++) {
				ProfiledGeneric nextElement = (ProfiledGeneric)this.myGraph.getGppTrace().getSortedFunctions().get(i);
				if (adapter.getTotalSampleCount(nextElement) < functionThreshold) {
					nextElement.setEnabled(graphIndex, enabled);
					adapter.addItem(threshold, graphIndex, nextElement, 0);
				} else {
					tableItemData.add(nextElement);
					profiledFunctions.add(nextElement);
					myGraph.getSortedFunctions().add(nextElement);
				}
			}

			if (threshold.getItemCount() != 0) {
				tableItemData.add(threshold);
				profiledFunctions.add(threshold);
			} else {
				// remove the threshold item
				myGraph.getSortedFunctions().remove(0);
			}
			
			refreshTableViewer();
			threshold.setEnabled(graphIndex, enabled);

			// make sure that checkboxes shown reflect the actual enabled values
			TableItem[] tableItems = this.table.getItems();
			for (int i = 0; i < tableItems.length; i++) {
				if (tableItems[i].getData() instanceof ProfiledGeneric) {
					ProfiledGeneric pGeneric = (ProfiledGeneric) tableItems[i].getData();
					if (tableItems[i].getChecked() != pGeneric.isEnabled(graphIndex))
						tableItems[i].setChecked(pGeneric.isEnabled(graphIndex));
				}
			}

			this.myGraph.genericRefreshCumulativeThreadTable();
		}
		else if (actionString.equals("sourceLookup")) //$NON-NLS-1$
		{
			actionSourceLookup();
			return;
		}
	    else if (actionString.equals("saveTableTest")) //$NON-NLS-1$
	    {
			// copy save file contents to the clipboard for easy viewing
	        Clipboard cb = Toolkit.getDefaultToolkit().getSystemClipboard();
			SaveTableString getString = new SaveTableString(this.table, CHECKBOX_NO_TEXT, Messages.getString("AddrFunctionTable.comma"), "\n"); //$NON-NLS-1$ //$NON-NLS-2$
	        String copyString = getString.getData();
			StringSelection contents = new StringSelection(copyString);
	        cb.setContents(contents, contents);
	        return;
	    }
	    else if (actionString.equals("saveDrilldownTest")) //$NON-NLS-1$
	    {
			// copy save file contents to the clipboard for easy viewing
	        Clipboard cb = Toolkit.getDefaultToolkit().getSystemClipboard();
	        Table[] tables = getDrillDownTables();
	        
	        int tableCount = 0;
	        while (tableCount < tables.length && tables[tableCount] != null)
	        	tableCount++;

	        SaveDrillDownString getString = new SaveDrillDownString(tableCount, tables, Messages.getString("AddrFunctionTable.comma")); //$NON-NLS-1$
	        String copyString = getString.getData();
			StringSelection contents = new StringSelection(copyString);
	        cb.setContents(contents, contents);
	        return;
	    }
	}

    private void actionAddRemove(String actionString, int graphIndex)
	{
		ProfiledGeneric pGeneric;
		int totalSamples = 0;
		
		// true for "add", false for "remove"
		boolean addIt = actionString.equals("add"); //$NON-NLS-1$

		TableItem[] selectedItems = this.table.getSelection();
		for (int i = 0; i < selectedItems.length; i++)
		{
			selectedItems[i].setChecked(addIt);
			Object item = ((TableItem)selectedItems[i]).getData();
			if (item instanceof ProfiledGeneric)
			{
				pGeneric = (ProfiledGeneric)item;
				pGeneric.setEnabled(graphIndex, addIt);
				totalSamples += pGeneric.getSampleCount(graphIndex);
   				
   				if (item instanceof ProfiledThreshold)
   				{
   					ProfiledThreshold pThreshold = (ProfiledThreshold)item;
   					for (ProfiledGeneric p : pThreshold.getItems())
   						p.setEnabled(graphIndex, addIt);
   				}
			}
		}

        // this table's set of checkbox-selected rows has changed,
		// so propagate that information
		Object[] selectedValues = this.tableViewer.getCheckedElements();
		String[] nameList = new String[selectedValues.length];

		for (int i = 0; i < selectedValues.length; i++)
		{
   			if (selectedValues[i] instanceof ProfiledGeneric)
   			{
   				pGeneric = (ProfiledGeneric)selectedValues[i];
   				nameList[i] = pGeneric.getNameString();
   			}
		}

		PIVisualSharedData shared = myGraph.getSharedDataInstance();
		shared.GPP_SelectedFunctionNames = nameList;

  		if (   (totalSamples != 0)
      		|| (myGraph.getDrawMode() == Defines.FUNCTIONS))
      			selectionChangeNotify();

  		this.table.deselectAll();
	}
	
	private void actionAddRemoveAll(String actionString, int graphIndex)
	{
		ProfiledGeneric pGeneric;

		// true for "add", false for "remove"
		boolean addIt = actionString.equals("addall"); //$NON-NLS-1$

		TableItem[] selectedItems = this.table.getItems();
		String[] nameList = new String[selectedItems.length];
		for (int i = 0; i < selectedItems.length; i++)
		{
			selectedItems[i].setChecked(addIt);
			Object item = ((TableItem)selectedItems[i]).getData();
			if (item instanceof ProfiledGeneric)
			{
				pGeneric = (ProfiledGeneric)item;
				pGeneric.setEnabled(graphIndex, addIt);
   				nameList[i] = pGeneric.getNameString();
   				
   				if (item instanceof ProfiledThreshold)
   				{
   					ProfiledThreshold pThreshold = (ProfiledThreshold)item;
   					for (ProfiledGeneric p : pThreshold.getItems())
   						p.setEnabled(graphIndex, addIt);
   				}
			}
		}

        // this table's set of checkbox-selected rows has changed,
		// so propagate that information
		PIVisualSharedData shared = myGraph.getSharedDataInstance();
		shared.GPP_SelectedFunctionNames = nameList;

			selectionChangeNotify();
  		this.table.deselectAll();
	}
	
	private void actionRecolor()
	{
		int uid    			= this.myGraph.getUid();
		GppTrace gppTrace = this.myGraph.getGppTrace();

		// recolor selected items
		boolean didRecolor = false;

		TableItem[] selectedItems = table.getSelection();
		TableColorPalette palette = ((GppTrace)this.myGraph.getTrace()).getFunctionColorPalette();
		for (int i = 0; i < selectedItems.length;i++)
		{
			if (selectedItems[i].getData() instanceof ProfiledGeneric)
			{
				ProfiledGeneric pGeneric = (ProfiledGeneric)selectedItems[i].getData();
				String nameKey = pGeneric.getNameString();
				if (palette.recolorEntryDialog(table.getShell(), nameKey))
				{
					Color color = palette.getColor(nameKey);
					Color oldColor = pGeneric.getColor();
					
					if (color.equals(oldColor))
						continue;
					
					didRecolor = true;
					
					if (!(pGeneric instanceof ProfiledThreshold)){
						PIPageEditor.currentPageEditor().setDirty();
						pGeneric.setColor(color);
					}
					else {
						// for the threshold item, we must change every graph's thread threshold item
						// CH: refactor! This could be done via an observer pattern. This class should not have knowledge of all other graphs  
						gppTrace.getGppGraph(PIPageEditor.THREADS_PAGE,   uid).getThresholdThread().setColor(color);
						gppTrace.getGppGraph(PIPageEditor.BINARIES_PAGE,  uid).getThresholdThread().setColor(color);
						gppTrace.getGppGraph(PIPageEditor.FUNCTIONS_PAGE, uid).getThresholdThread().setColor(color);
//						if (gppTrace.getCPUCount() > 1){ //SMP CH: comment this out once we have SMP graphs on the functions page
//							for (int cpu = 0; cpu < gppTrace.getCPUCount(); cpu++) {
//								gppTrace.getGppGraph(11 + cpu, uid).getThresholdThread().setColor(color);															
//							}
//						}
					}
				}

				// recoloring should only be done in a draw mode that displays this table's colors
				selectedItems[i].setBackground(COLOR_COLUMN_INDEX, palette.getColor(nameKey));
			}
		}
		
		if (!didRecolor)
			return;

		table.redraw();
		this.myGraph.repaint();
		this.myGraph.setGraphImageChanged(true);	// any selection change to drill down will change graph
		
		// if any other tabs are displaying this type of graph, they need to be scheduled for redrawing
		for (int i = 0; i < 3; i++) {
			IGppTraceGraph graph = gppTrace.getGppGraph(i, uid);

			if (graph == this.myGraph)
				continue;

			int drawMode = graph.getDrawMode();
			
			if (   (drawMode == Defines.FUNCTIONS)
				|| (drawMode == Defines.THREADS_FUNCTIONS)
				|| (drawMode == Defines.THREADS_BINARIES_FUNCTIONS)
				|| (drawMode == Defines.BINARIES_FUNCTIONS)
				|| (drawMode == Defines.BINARIES_THREADS_FUNCTIONS)) {
				graph.getFunctionTable().addColor(drawMode);
				graph.setGraphImageChanged(true);	// any selection change to drill down will change graph
				graph.repaint();
			}
		}
	}
	
	private void actionFunction()
	{
		// current drawMode should be FUNCTIONS, FUNCTIONS_THREADS, FUNCTIONS_THREADS_BINARIES,
		// FUNCTIONS_BINARIES, or FUNCTIONS_BINARIES_THREADS
		int drawMode = this.myGraph.getDrawMode();

		if (   (drawMode != Defines.FUNCTIONS_THREADS)
			&& (drawMode != Defines.FUNCTIONS_THREADS_BINARIES)
			&& (drawMode != Defines.FUNCTIONS_BINARIES)
			&& (drawMode != Defines.FUNCTIONS_BINARIES_THREADS))
		{
			// this case should be drawMode == Defines.FUNCTIONS
			return;
		}

		setIsDrilldown(false);

		// get rid of any existing tables and sashes
		if (this.myGraph.getLeftSash() != null) {
			this.myGraph.getLeftSash().dispose();
			this.myGraph.setLeftSash(null);

			// detach the table from the sash
			try {
				FormData formData = (FormData) this.table.getLayoutData();
				formData.right = new FormAttachment(100);
			} catch (ClassCastException e1) {
			}
		}
		if (this.myGraph.getRightSash() != null) {
			this.myGraph.getRightSash().dispose();
			this.myGraph.setRightSash(null);
		}
		if (   (this.myGraph.getThreadTable() != null)
			&& (this.myGraph.getThreadTable().getTable() != null)) {
			this.myGraph.getThreadTable().getTableViewer().getTable().dispose();
			this.myGraph.getThreadTable().setTableViewer(null);
		}
		if (   (this.myGraph.getBinaryTable() != null)
			&& (this.myGraph.getBinaryTable().getTable() != null)) {
			this.myGraph.getBinaryTable().getTableViewer().getTable().dispose();
			this.myGraph.getBinaryTable().setTableViewer(null);
		}

		// set the draw mode
		this.myGraph.setDrawMode(Defines.FUNCTIONS);

		// add colors to the rightmost table
		addColor(Defines.FUNCTIONS);
		
		this.parent.layout();

 		this.myGraph.repaint();
	}

	private void actionFunctionBinary()
	{
		// current drawMode should be FUNCTIONS, FUNCTIONS_BINARIES, or FUNCTIONS_BINARIES_THREADS
		int drawMode = this.myGraph.getDrawMode();
		int graphIndex = this.myGraph.getGraphIndex();
		
		if (   drawMode != Defines.FUNCTIONS
			&& drawMode != Defines.FUNCTIONS_BINARIES_THREADS) {
			return;
		}

		setIsDrilldown(true);

		if (drawMode == Defines.FUNCTIONS) {

			// set the draw mode
			this.myGraph.setDrawMode(Defines.FUNCTIONS_BINARIES);

			// create the binary graph table viewer
			AddrBinaryTable binaryTable = this.myGraph.getBinaryTable();
			binaryTable.createTableViewer(Defines.FUNCTIONS_BINARIES);
			binaryTable.setIsDrilldown(true);

			// create a reduced set of binary entries based on enabled function entries
			GppTrace gppTrace = (GppTrace) this.myGraph.getTrace();
			gppTrace.setFunctionBinary(graphIndex, adapter, this.myGraph.getProfiledBinaries());

			// put check marks on all rows, and sort by sample count
			binaryTable.quickSort(COLUMN_ID_SAMPLE_COUNT, this.myGraph.getProfiledBinaries());
			binaryTable.updateProfiledAndItemData(true);
			binaryTable.getTableViewer().setAllChecked(true);
			binaryTable.getTableViewer().refresh();

			// put check marks on all enabled binary rows
			for (int i = 0; i < this.table.getItemCount(); i++) {
				ProfiledGeneric pFunction = (ProfiledGeneric) this.table.getItem(i).getData();
				if (pFunction.isEnabled(graphIndex))
					this.table.getItem(i).setChecked(true);
			}

			// remove colors where appropriate
			removeColor(Defines.FUNCTIONS_BINARIES);

			// connect the tables with a sash
			Sash leftSash   = new Sash(this.parent, SWT.VERTICAL);

			final FormData leftSashData = new FormData();
			leftSashData.top    = new FormAttachment(0);
			leftSashData.bottom = new FormAttachment(100);
			leftSashData.left   = new FormAttachment(50); // middle
			leftSash.setLayoutData(leftSashData);

			final Composite sashParent = this.parent;
			leftSash.addListener(SWT.Selection, new Listener() {
				public void handleEvent(Event event) {
					if (event.detail != SWT.DRAG) {
						leftSashData.left = new FormAttachment(0, event.x);
						sashParent.layout();
					}
				}
			});

			myGraph.setLeftSash(leftSash);

			// attach the binary table to the sash
			final FormData viewerData = new FormData();
			viewerData.top    = new FormAttachment(0);
			viewerData.bottom = new FormAttachment(100);
			viewerData.left   = new FormAttachment(leftSash);
			viewerData.right  = new FormAttachment(100);
			binaryTable.getTable().setLayoutData(viewerData);

			// attach the function table to the sash
			try {
				FormData formData = (FormData) this.table.getLayoutData();
				formData.right = new FormAttachment(leftSash);
			} catch (ClassCastException e1) {
			}

			this.parent.layout();

			this.myGraph.repaint();

		} else if (drawMode == Defines.FUNCTIONS_BINARIES_THREADS) {

			// get rid of the thread table and its sash
			if (this.myGraph.getRightSash() != null) {
				this.myGraph.getRightSash().dispose();
				this.myGraph.setRightSash(null);
			}
			if (   (this.myGraph.getThreadTable() != null)
				&& (this.myGraph.getThreadTable().getTable() != null)) {
				this.myGraph.getThreadTable().getTableViewer().getTable().dispose();
				this.myGraph.getThreadTable().setTableViewer(null);
			}

			// get rid of the middle table's connection to the sash
			try {
				FormData formData = (FormData) this.myGraph.getBinaryTable().getTable().getLayoutData();
				formData.right = new FormAttachment(100);
			} catch (ClassCastException e1) {
			}

			// move the left sash to the middle
			try {
				FormData formData = (FormData) this.myGraph.getLeftSash().getLayoutData();
				formData.left = new FormAttachment(50); // middle
			} catch (ClassCastException e1) {
			}

			// set the draw mode
			this.myGraph.setDrawMode(Defines.FUNCTIONS_BINARIES);

			// show colors in the rightmost table
			this.myGraph.getBinaryTable().addColor(Defines.FUNCTIONS_BINARIES);

			this.parent.layout();

			this.myGraph.repaint();
		}

		// this case should be drawMode == Defines.FUNCTIONS_BINARIES
		return;
	}

	private void actionFunctionBinaryThread()
	{
		// current drawMode should be FUNCTIONS_BINARIES or FUNCTIONS_BINARIES_THREADS
		int drawMode = this.myGraph.getDrawMode();
		int graphIndex = this.myGraph.getGraphIndex();

		if (drawMode != Defines.FUNCTIONS_BINARIES) {
			// this case should be drawMode == Defines.FUNCTIONS_BINARIES_THREADS
			return;
		}

		setIsDrilldown(true);

		// set the draw mode
		this.myGraph.setDrawMode(Defines.FUNCTIONS_BINARIES_THREADS);

		// create the thread graph table viewer
		AddrThreadTable threadTable = this.myGraph.getThreadTable();
		threadTable.createTableViewer(Defines.FUNCTIONS_BINARIES_THREADS);
		threadTable.setIsDrilldown(true);

		// create a reduced set of thread entries based on enabled binary and function entries
		GppTrace gppTrace = (GppTrace) this.myGraph.getTrace();
		gppTrace.setFunctionBinaryThread(graphIndex, adapter, this.myGraph.getProfiledThreads());

		// put check marks on all rows, and sort by sample count
		threadTable.quickSort(COLUMN_ID_SAMPLE_COUNT, this.myGraph.getProfiledThreads());
		threadTable.updateProfiledAndItemData(true);
		threadTable.getTableViewer().setAllChecked(true);
		threadTable.getTableViewer().refresh();

		// remove colors where appropriate
		this.myGraph.getBinaryTable().removeColor(Defines.FUNCTIONS_BINARIES_THREADS);

		// connect the 2nd and 3rd tables with a sash
		Sash rightSash   = new Sash(this.parent, SWT.VERTICAL);

		final FormData rightSashData = new FormData();
		rightSashData.top    = new FormAttachment(0);
		rightSashData.bottom = new FormAttachment(100);
		rightSashData.left   = new FormAttachment(67); // two thirds
		rightSash.setLayoutData(rightSashData);

		final Composite sashParent = this.parent;
		rightSash.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				if (event.detail != SWT.DRAG) {
					rightSashData.left = new FormAttachment(0, event.x);
					sashParent.layout();
				}
			}
		});

		myGraph.setRightSash(rightSash);

		// attach the thread table to the sash
		final FormData viewerData = new FormData();
		viewerData.top    = new FormAttachment(0);
		viewerData.bottom = new FormAttachment(100);
		viewerData.left   = new FormAttachment(rightSash);
		viewerData.right  = new FormAttachment(100);
		threadTable.getTable().setLayoutData(viewerData);

		// attach the binary table to the sash
		try {
			FormData formData = (FormData) this.myGraph.getBinaryTable().getTable().getLayoutData();
			formData.right = new FormAttachment(rightSash);
		} catch (ClassCastException e1) {
		}

		// move the left sash to 1/3 from the left
		try {
			FormData formData = (FormData) this.myGraph.getLeftSash().getLayoutData();
			formData.left = new FormAttachment(33); // one third
		} catch (ClassCastException e1) {
		}

		this.parent.layout();

		this.myGraph.repaint();
	}

	private void actionFunctionThread()
	{
		// current drawMode should be FUNCTIONS, FUNCTIONS_THREADS, or FUNCTIONS_THREADS_BINARIES
		int drawMode = this.myGraph.getDrawMode();
		int graphIndex = this.myGraph.getGraphIndex();

		if (   drawMode != Defines.FUNCTIONS
			&& drawMode != Defines.FUNCTIONS_THREADS_BINARIES) {
			return;
		}

		setIsDrilldown(true);

		if (drawMode == Defines.FUNCTIONS) {

			// set the draw mode
			this.myGraph.setDrawMode(Defines.FUNCTIONS_THREADS);

			// create the thread graph table viewer
			AddrThreadTable threadTable = this.myGraph.getThreadTable();
			threadTable.createTableViewer(Defines.FUNCTIONS_THREADS);
			threadTable.setIsDrilldown(true);

			// create a reduced set of thread entries based on enabled function entries
			GppTrace gppTrace = (GppTrace) this.myGraph.getTrace();
			gppTrace.setFunctionThread(graphIndex, adapter, this.myGraph.getProfiledThreads());

			// put check marks on all rows, and sort by sample count
			threadTable.quickSort(COLUMN_ID_SAMPLE_COUNT, this.myGraph.getProfiledThreads());
			threadTable.updateProfiledAndItemData(true);
			threadTable.getTableViewer().setAllChecked(true);
			threadTable.getTableViewer().refresh();

			// remove colors where appropriate
			removeColor(Defines.FUNCTIONS_THREADS);

			// connect the tables with a sash
			Sash leftSash   = new Sash(this.parent, SWT.VERTICAL);

			final FormData leftSashData = new FormData();
			leftSashData.top    = new FormAttachment(0);
			leftSashData.bottom = new FormAttachment(100);
			leftSashData.left   = new FormAttachment(50); // middle
			leftSash.setLayoutData(leftSashData);

			final Composite sashParent = this.parent;
			leftSash.addListener(SWT.Selection, new Listener() {
				public void handleEvent(Event event) {
					if (event.detail != SWT.DRAG) {
						leftSashData.left = new FormAttachment(0, event.x);
						sashParent.layout();
					}
				}
			});

			myGraph.setLeftSash(leftSash);

			// attach the thread table to the sash
			final FormData viewerData = new FormData();
			viewerData.top    = new FormAttachment(0);
			viewerData.bottom = new FormAttachment(100);
			viewerData.left   = new FormAttachment(leftSash);
			viewerData.right  = new FormAttachment(100);
			threadTable.getTable().setLayoutData(viewerData);

			// attach the function table to the sash
			try {
				FormData formData = (FormData) this.table.getLayoutData();
				formData.right = new FormAttachment(leftSash);
			} catch (ClassCastException e1) {
			}

			this.parent.layout();

			this.myGraph.repaint();

		} else if (drawMode == Defines.FUNCTIONS_THREADS_BINARIES) {

			// get rid of the binary table and its sash
			if (this.myGraph.getRightSash() != null) {
				this.myGraph.getRightSash().dispose();
				this.myGraph.setRightSash(null);
			}
			if (   (this.myGraph.getBinaryTable() != null)
				&& (this.myGraph.getBinaryTable().getTable() != null)) {
				this.myGraph.getBinaryTable().getTableViewer().getTable().dispose();
				this.myGraph.getBinaryTable().setTableViewer(null);
			}

			// get rid of the middle table's connection to the sash
			try {
				FormData formData = (FormData) this.myGraph.getThreadTable().getTable().getLayoutData();
				formData.right = new FormAttachment(100);
			} catch (ClassCastException e1) {
			}

			// move the left sash to the middle
			try {
				FormData formData = (FormData) this.myGraph.getLeftSash().getLayoutData();
				formData.left = new FormAttachment(50); // middle
			} catch (ClassCastException e1) {
			}

			// set the draw mode
			this.myGraph.setDrawMode(Defines.FUNCTIONS_THREADS);

			// show colors in the rightmost table
			this.myGraph.getThreadTable().addColor(Defines.FUNCTIONS_THREADS);

			this.parent.layout();

			this.myGraph.repaint();
		}

		// this case should be drawMode == Defines.FUNCTIONS_THREADS
		return;
	}

	private void actionFunctionThreadBinary()
	{
		// current drawMode is FUNCTIONS_THREADS, or FUNCTIONS_THREADS_BINARIES
		int drawMode = this.myGraph.getDrawMode();
		int graphIndex = this.myGraph.getGraphIndex();

		if (drawMode != Defines.FUNCTIONS_THREADS) {
			// this case should be drawMode == Defines.FUNCTIONS_THREADS_BINARIES
			return;
		}

		setIsDrilldown(true);

		// set the draw mode
		this.myGraph.setDrawMode(Defines.FUNCTIONS_THREADS_BINARIES);

		// create the binary graph table viewer
		AddrBinaryTable binaryTable = this.myGraph.getBinaryTable();
		binaryTable.createTableViewer(Defines.FUNCTIONS_THREADS_BINARIES);
		binaryTable.setIsDrilldown(true);

		// create a reduced set of binary entries based on enabled thread entries
		GppTrace gppTrace = (GppTrace) this.myGraph.getTrace();
		gppTrace.setFunctionThreadBinary(graphIndex, adapter, this.myGraph.getProfiledBinaries());

		// put check marks on all rows, and sort by sample count
		binaryTable.quickSort(COLUMN_ID_SAMPLE_COUNT, this.myGraph.getProfiledBinaries());
		binaryTable.updateProfiledAndItemData(true);
		binaryTable.getTableViewer().setAllChecked(true);
		binaryTable.getTableViewer().refresh();

		// remove colors where appropriate
		this.myGraph.getThreadTable().removeColor(Defines.FUNCTIONS_THREADS_BINARIES);

		// connect the 2nd and 3rd tables with a sash
		Sash rightSash   = new Sash(this.parent, SWT.VERTICAL);

		final FormData rightSashData = new FormData();
		rightSashData.top    = new FormAttachment(0);
		rightSashData.bottom = new FormAttachment(100);
		rightSashData.left   = new FormAttachment(67); // two thirds
		rightSash.setLayoutData(rightSashData);

		final Composite sashParent = this.parent;
		rightSash.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				if (event.detail != SWT.DRAG) {
					rightSashData.left = new FormAttachment(0, event.x);
					sashParent.layout();
				}
			}
		});

		myGraph.setRightSash(rightSash);

		// attach the binary table to the sash
		final FormData viewerData = new FormData();
		viewerData.top    = new FormAttachment(0);
		viewerData.bottom = new FormAttachment(100);
		viewerData.left   = new FormAttachment(rightSash);
		viewerData.right  = new FormAttachment(100);
		binaryTable.getTable().setLayoutData(viewerData);

		// attach the thread table to the sash
		try {
			FormData formData = (FormData) this.myGraph.getThreadTable().getTable().getLayoutData();
			formData.right = new FormAttachment(rightSash);
		} catch (ClassCastException e1) {
		}

		// move the left sash to 1/3 from the left
		try {
			FormData formData = (FormData) this.myGraph.getLeftSash().getLayoutData();
			formData.left = new FormAttachment(33); // one third
		} catch (ClassCastException e1) {
		}

		this.parent.layout();

		this.myGraph.repaint();
	}
	
	private void actionSourceLookup() {
		// source look up for selected items
		TableItem selectedItem = table.getSelection()[0];
		if (selectedItem.getData() instanceof ProfiledGeneric)
		{
			ProfiledGeneric pGeneric = (ProfiledGeneric)selectedItem.getData();
			
			if (pGeneric instanceof ProfiledFunction) {
				ProfiledFunction pFunc = (ProfiledFunction) pGeneric;
				SourceLookup.getInstance().lookupAndopenEditorWithHighlight (pFunc.getNameString(), pFunc.getFunctionBinaryName());
			}
		}
	}

	public void focusGained(FocusEvent e) {}

	public void focusLost(FocusEvent e) {}

	public void piEventReceived(PIEvent be)
	{
		if (be.getType() == PIEvent.SELECTION_AREA_CHANGED3)
		{
			// % loads, % load strings, and/or sample counts have been updated
			// due to a change in the graph area selected, and all table items
			// need to be checked
			updateProfiledAndItemData(true);

			Display.getDefault().syncExec(new Runnable() {
				public void run() {
					quickSort(sortColumn, profiledFunctions);

					// initially, all rows are selected
					tableViewer.setAllChecked(true);

					table.redraw();
				}
			});
		}
		else if (   (be.getType() == PIEvent.SELECTION_AREA_CHANGED2)
				 || (be.getType() == PIEvent.CHANGED_FUNCTION_TABLE))
		{
			int graphIndex = this.myGraph.getGraphIndex();

			// This routine does not change which functions are enabled, but it enables
			// all entries in the 2nd and 3rd tables.
			// It assumes that GppTrace.setSelectedArea(), action("add") or action("remove") has set
			// the % load and sample counts for functions, except for the threshold list.
			if (be.getType() == PIEvent.SELECTION_AREA_CHANGED2) {
				int thresholdCount = (Integer)NpiInstanceRepository.getInstance().activeUidGetPersistState("com.nokia.carbide.cpp.pi.address.thresholdCountFunction"); //$NON-NLS-1$
				if (thresholdCount > 0) {
					Vector<ProfiledGeneric> pGeneric = this.myGraph.getProfiledFunctions();
					int sampleCount = 0;
					for (int i = 0; i < pGeneric.size(); i++)
						if (adapter.getTotalSampleCount(pGeneric.elementAt(i)) < thresholdCount)
							sampleCount += pGeneric.elementAt(i).getSampleCount(graphIndex);
					this.myGraph.getThresholdFunction().setSampleCount(graphIndex, sampleCount);
				}
			}

			// redraw this table
			Display.getDefault().syncExec(new Runnable() {
				public void run() {
					if (   (sortColumn == COLUMN_ID_PERCENT_LOAD)
						|| (sortColumn == COLUMN_ID_SAMPLE_COUNT))
					{
						quickSort(sortColumn, profiledFunctions);
					}
					else
						refreshTableViewer();

					table.redraw();
				}
			});

			int drawMode = this.myGraph.getDrawMode();
			if (drawMode == Defines.FUNCTIONS)
				return;

			GppTrace trace = (GppTrace)(this.myGraph.getTrace());
			PIEvent be3 = new PIEvent(be.getValueObject(), PIEvent.SELECTION_AREA_CHANGED3);

			switch (drawMode) {
				case Defines.FUNCTIONS_THREADS:
				{
					trace.setFunctionThread(graphIndex, adapter, this.myGraph.getProfiledThreads());

					// update the table items and redraw the table
					this.myGraph.getThreadTable().piEventReceived(be3);
					break;
				}
				case Defines.FUNCTIONS_THREADS_BINARIES:
				{
					// previous threads are not necessarily graphed this time - reset
					trace.setFunctionThread(graphIndex, adapter, this.myGraph.getProfiledThreads());
					// previous binaries are not necessarily graphed this time - reset
					trace.setFunctionThreadBinary(graphIndex, adapter, this.myGraph.getProfiledBinaries());

					// update the table items and redraw the table
					this.myGraph.getThreadTable().piEventReceived(be3);
					this.myGraph.getBinaryTable().piEventReceived(be3);
					break;
				}
				case Defines.FUNCTIONS_BINARIES:
				{
					trace.setFunctionBinary(graphIndex, adapter, this.myGraph.getProfiledBinaries());

					// update the table items and redraw the table
					this.myGraph.getBinaryTable().piEventReceived(be3);
					break;
				}
				case Defines.FUNCTIONS_BINARIES_THREADS:
				{
					// previous binaries are not necessarily graphed this time - reset
					trace.setFunctionBinary(graphIndex, adapter, this.myGraph.getProfiledBinaries());
					// previous threads are not necessarily graphed this time - reset
					trace.setFunctionBinaryThread(graphIndex, adapter, this.myGraph.getProfiledThreads());

					// update the table items and redraw the table
					this.myGraph.getThreadTable().piEventReceived(be3);
					this.myGraph.getBinaryTable().piEventReceived(be3);
					break;
				}
				default:
				{
					break;
				}
			}
		}
		else if (be.getType() == PIEvent.CHANGED_THREAD_TABLE)
		{
			// This routine enables all entries in the next table

			int drawMode = this.myGraph.getDrawMode();
			if (drawMode != Defines.FUNCTIONS_THREADS_BINARIES)
				return;

			// we don't need to redraw the function table, since it has not changed

			PIEvent be3 = new PIEvent(be.getValueObject(), PIEvent.SELECTION_AREA_CHANGED3);
			GppTrace trace = (GppTrace)(this.myGraph.getTrace());
			int graphIndex = this.myGraph.getGraphIndex();
			trace.setFunctionThreadBinary(graphIndex, adapter, this.myGraph.getProfiledBinaries());

			// update the table items and redraw the table
			this.myGraph.getBinaryTable().piEventReceived(be3);
		}
		else if (be.getType() == PIEvent.CHANGED_BINARY_TABLE)
		{
			// This routine enables all entries in the next table

			int drawMode = this.myGraph.getDrawMode();
			if (drawMode != Defines.FUNCTIONS_BINARIES_THREADS)
				return;

			// we don't need to redraw the function table, since it has not changed

			PIEvent be3 = new PIEvent(be.getValueObject(), PIEvent.SELECTION_AREA_CHANGED3);
			GppTrace trace = (GppTrace)(this.myGraph.getTrace());
			int graphIndex = this.myGraph.getGraphIndex();
			trace.setFunctionBinaryThread(graphIndex, adapter, this.myGraph.getProfiledThreads());

			// update the table items and redraw the table
			this.myGraph.getThreadTable().piEventReceived(be3);
		}
	}

	public void setSelectedNames()
	{
		Object[] selectedValues = this.tableViewer.getCheckedElements();
        String[] functionNames = new String[selectedValues.length];

        for (int i = 0; i < selectedValues.length; i++)
		{
			if (selectedValues[i] instanceof ProfiledFunction)
			{
				ProfiledFunction pf = (ProfiledFunction)selectedValues[i];
				functionNames[i] = pf.getNameString();
			}
		}

        PIVisualSharedData shared = myGraph.getSharedDataInstance();
		shared.GPP_SelectedFunctionNames = functionNames;
	}

	private class SharedCheckHandler implements ICheckStateListener
	{
		public void checkStateChanged(CheckStateChangedEvent event) {

       		if (!(event.getElement() instanceof ProfiledGeneric))
       			return;
       		
       		// set the stored value to the checkbox value
       		ProfiledGeneric pg = (ProfiledGeneric)event.getElement();
       		pg.setEnabled(myGraph.getGraphIndex(), event.getChecked());
 
	        // this table's set of checkbox-selected rows has changed, so propagate that information
       		setSelectedNames();

       		if (   (pg.getSampleCount(myGraph.getGraphIndex()) != 0)
       			|| (myGraph.getDrawMode() == Defines.FUNCTIONS))
       			selectionChangeNotify();

       		table.deselectAll();
		}
	}

	protected void selectionChangeNotify() {
		PIEvent be = new PIEvent(null, PIEvent.CHANGED_FUNCTION_TABLE);
		myGraph.piEventReceived(be);
    }

	public void sortOnColumnSelection(TableColumn tableColumn) {
		int columnId = ((Integer) tableColumn.getData()).intValue();
        if (sortColumn == columnId) {
        	// sort in other order
        	sortAscending = !sortAscending;
        } else {
        	// sort in the default order
			switch (columnId)
			{
				case COLUMN_ID_SHOW:
				case COLUMN_ID_PERCENT_LOAD:
				case COLUMN_ID_SAMPLE_COUNT:
				{
	            	// sort in descending order (for checked boxes, this means selected boxes first)
	            	sortAscending = false;
	                break;
				}
				case COLUMN_ID_FUNCTION:
				case COLUMN_ID_START_ADDR:
				case COLUMN_ID_IN_BINARY:
				case COLUMN_ID_IN_BINARY_PATH:
				{
	            	// sort in ascending order
	            	sortAscending = true;
	                break;
				}
				default:
				{
					return;
				}
            }
        }
		
		sortColumn = columnId;
		quickSort(sortColumn, profiledFunctions);
	}

	private class ColumnSelectionHandler extends SelectionAdapter
	{
		@Override
		public void widgetSelected(SelectionEvent e)
        {
        	// wait for the previous sort to finish
        	if (sorting || !(e.widget instanceof TableColumn))
        		return;

        	sortOnColumnSelection((TableColumn) e.widget);
        }
	}

	/**
	 * Adds ProfiledGenerics and threshold items to the table data
	 * (this.tableItemData and this.profiledThreads). Sorts the ProfiledGenerics
	 * according to load; and sets the graph's sorted list accordingly. Refreshes
	 * the TableViewer if parameter setInput is true.
	 * 
	 * @param setInput
	 *            if true, reset the TableViewer
	 */
	public void updateProfiledAndItemData(boolean setInput)
	{
		tableItemData.clear();
		profiledFunctions.clear();

		// profiledFunctions and tableItemData contain one entry per table row
		Enumeration<ProfiledGeneric> enu = myGraph.getProfiledFunctions().elements();
		while (enu.hasMoreElements())
		{
			ProfiledFunction nextElement = (ProfiledFunction)enu.nextElement();
			tableItemData.add(nextElement);
			profiledFunctions.add(nextElement);
		}

		if (myGraph.getThresholdFunction().getItemCount() != 0) {
			tableItemData.add(myGraph.getThresholdFunction());
			profiledFunctions.add(myGraph.getThresholdFunction());
		}

		// now sort the items in increasing total sample count, so that they graph correctly
		Object[] sorted = myGraph.getProfiledFunctions().toArray();
		Arrays.sort(sorted, new Comparator<Object>() {
			
			public int compare(Object arg0, Object arg1)
			{
				if (arg0 instanceof ProfiledGeneric && arg1 instanceof ProfiledGeneric)
					return (adapter.getTotalSampleCount((ProfiledGeneric)arg0)) -
					adapter.getTotalSampleCount(((ProfiledGeneric)arg1));
				return 0;
			}
		});

		// now create the sorted list used to draw the graph
		myGraph.getSortedFunctions().clear();

		if (myGraph.getThresholdFunction().getItemCount() != 0)
			myGraph.getSortedFunctions().add(myGraph.getThresholdFunction());

		for (int i = 0; i < sorted.length; i++)
			myGraph.getSortedFunctions().add((ProfiledGeneric) sorted[i]);

		// refresh the table, if needed
		if (setInput)
			refreshTableViewer();
	}
	
	public void quickSort(int sortBy, Vector<ProfiledGeneric> profiled)
	{
		if (profiled.size() == 0)
			return;

		this.sorting = true;

		ProfiledGeneric pGeneric = profiled.elementAt(profiled.size() - 1);

		if (pGeneric instanceof ProfiledThreshold) {
			profiled.removeElementAt(profiled.size() - 1);
		}

		this.sorter.setupSort(sortBy, this.myGraph.getGraphIndex(), sortAscending);

		switch (sortBy) {
			case COLUMN_ID_SHOW:
			{
			    this.sorter.quickSortByShow(profiled);
		        break;
			}
			case COLUMN_ID_PERCENT_LOAD:
			{
			    this.sorter.quickSortByAverageLoad(profiled);
		        break;
			}
			case COLUMN_ID_THREAD:
			{
			    this.sorter.quickSortByThread(profiled);
	            break;
			}
			case COLUMN_ID_BINARY:
			{
			    this.sorter.quickSortByBinary(profiled);
	            break;
			}
			case COLUMN_ID_FUNCTION:
			{
			    this.sorter.quickSortByFunction(profiled);
	            break;
			}
			case COLUMN_ID_SAMPLE_COUNT:
			{
			    this.sorter.quickSortBySampleCount(profiled);
	            break;
			}
			case COLUMN_ID_START_ADDR:
			{
			    this.sorter.quickSortByStartAddress(profiled);
	            break;
			}
			case COLUMN_ID_IN_BINARY:
			{
			    this.sorter.quickSortByAssocBinary(profiled);
	            break;
			}
			case COLUMN_ID_IN_BINARY_PATH:
			{
			    this.sorter.quickSortByAssocBinaryPath(profiled);
	            break;
			}
			case COLUMN_ID_FULL_IN_PATH:
			{
			    this.sorter.quickSortByFullAssocBinaryPath(profiled);
	            break;
			}
			default:
			{
				break;
			}
		}
 		
		Enumeration<ProfiledGeneric> e = this.sorter.getSortedList().elements();
		tableItemData = setTableItemData(e);
		if (pGeneric instanceof ProfiledThreshold) {
			tableItemData.add(pGeneric);
			profiled.add(pGeneric);
		}
		
		// find the column corresponding to sortBy, and give it a column direction
		// NOTE: treat sort by binary path then by binary name as sort by path
		if (sortBy == COLUMN_ID_FULL_PATH)
			sortBy = COLUMN_ID_PATH;
		else if (sortBy == COLUMN_ID_FULL_IN_PATH)
			sortBy = COLUMN_ID_IN_BINARY_PATH;

		TableColumn sortByColumn = null;
		for (int i = 0; i < this.table.getColumnCount(); i++) {
			if (this.table.getColumn(i).getData() instanceof Integer) {
				if (((Integer)this.table.getColumn(i).getData()) == sortBy) {
					sortByColumn = this.table.getColumn(i);
					break;
				}
			}
		}

		if (sortByColumn != null) {
			this.table.setSortColumn(sortByColumn);
			this.table.setSortDirection(sortAscending ? SWT.UP : SWT.DOWN);
		}

		refreshTableViewer();
		this.sorting = false;
	}

	@Override
	protected Menu getTableMenu(Decorations aParent, int graphIndex, int drawMode) {
		// get rid of last Menu created so we don't have double menu
		// in on click
		if (contextMenu != null) {
			contextMenu.dispose();
		}
		
		contextMenu = new Menu(aParent, SWT.POP_UP);

		// Use drawMode to determine the drill down items and
		// whether to show a color column
		addDrillDownItems(contextMenu, drawMode);
		
		// sort by path, then binary
		new MenuItem(contextMenu, SWT.SEPARATOR);
		getSortInFullPathItem(contextMenu, this.table.getItemCount() > 0);

		// check and uncheck boxes
		new MenuItem(contextMenu, SWT.SEPARATOR);
		getCheckRows(contextMenu, this.table.getSelectionCount() > 0);
		
		// select all, copy, and copy all
		new MenuItem(contextMenu, SWT.SEPARATOR);
		getSelectAllItem(contextMenu, this.table.getItemCount() > 0);
		getCopyItem(contextMenu, this.table.getSelectionCount() > 0);
		getCopyTableItem(contextMenu, this.table.getItemCount() > 0);
		
		// copy drilldown tables, if in drilldown mode
		switch (drawMode)
		{
			case Defines.THREADS:
			case Defines.BINARIES:
			case Defines.FUNCTIONS:
				getCopyDrilldownItem(contextMenu, false);
				break;
			default:
				getCopyDrilldownItem(contextMenu, true);
				break;
		}
		
		// save all and save drilldown tables, if in drilldown mode 
		new MenuItem(contextMenu, SWT.SEPARATOR);
		getSaveTableItem(contextMenu, this.table.getItemCount() > 0);
		
		switch (drawMode)
		{
			case Defines.THREADS:
			case Defines.BINARIES:
			case Defines.FUNCTIONS:
				getSaveDrilldownItem(contextMenu, false);
				break;
			default:
				getSaveDrilldownItem(contextMenu, true);
				break;
		}
		
		// save raw samples
		boolean haveSamples = false;
		for (int i = 0; !haveSamples && i < profiledFunctions.size(); i++) {
			ProfiledGeneric pg = profiledFunctions.get(i);
			haveSamples = pg.isEnabled(graphIndex) && pg.getSampleCount(graphIndex) > 0;
		}

		double startTime = PIPageEditor.currentPageEditor().getStartTime();
		double endTime   = PIPageEditor.currentPageEditor().getEndTime();
		if (!haveSamples || (startTime == -1) || (endTime   == -1) || (startTime == endTime))
			getSaveSamplesItem(contextMenu, Messages.getString("AddrFunctionTable.functions"), false); //$NON-NLS-1$
		else
			getSaveSamplesItem(contextMenu, Messages.getString("AddrFunctionTable.functions"), true); //$NON-NLS-1$

		// recolor selected threads
		switch (drawMode)
		{
			case Defines.FUNCTIONS:
			case Defines.THREADS_BINARIES_FUNCTIONS:
			case Defines.BINARIES_FUNCTIONS:
			case Defines.THREADS_FUNCTIONS:
			case Defines.BINARIES_THREADS_FUNCTIONS:
			{
				// recolor selected items
				new MenuItem(contextMenu, SWT.SEPARATOR);
				getRecolorItem(contextMenu, Messages.getString("AddressPlugin.functions"),   //$NON-NLS-1$
								this.table.getSelectionCount() > 0);
				break;
			}
			case Defines.THREADS_FUNCTIONS_BINARIES:
			case Defines.BINARIES_FUNCTIONS_THREADS:
			case Defines.FUNCTIONS_THREADS:
			case Defines.FUNCTIONS_THREADS_BINARIES:
			case Defines.FUNCTIONS_BINARIES:
			case Defines.FUNCTIONS_BINARIES_THREADS:
			{
				break;
			}
			default:
				break;
		}

		new MenuItem(contextMenu, SWT.SEPARATOR);
		getChangeThresholds(contextMenu);

		switch (drawMode) {
			case Defines.THREADS_FUNCTIONS:
			case Defines.THREADS_BINARIES_FUNCTIONS:
			case Defines.BINARIES_FUNCTIONS:
			case Defines.BINARIES_THREADS_FUNCTIONS:	
			case Defines.FUNCTIONS:
				new MenuItem(contextMenu, SWT.SEPARATOR);
				// source look up only when one item is selected
				if (table.getSelection().length == 1) {
					getSourceLookUpItem(contextMenu, Messages.getString("AddrFunctionTable.items"));  //$NON-NLS-1$
				} else {
					getDisabledSourceLookUpItem(contextMenu, Messages.getString("AddrFunctionTable.items"));  //$NON-NLS-1$
				}
			default:
				break;
		}

		contextMenu.setVisible(true);

		return contextMenu;
	}

	public Vector<ProfiledGeneric> getTableItemData() {
		return this.tableItemData;
	}

}
