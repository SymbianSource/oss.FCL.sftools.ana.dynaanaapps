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
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.Hashtable;
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
import com.nokia.carbide.cpp.internal.pi.interfaces.ISaveSamples;
import com.nokia.carbide.cpp.internal.pi.model.ICPUScale;
import com.nokia.carbide.cpp.internal.pi.model.ProfiledGeneric;
import com.nokia.carbide.cpp.internal.pi.model.ProfiledThread;
import com.nokia.carbide.cpp.internal.pi.model.ProfiledThreshold;
import com.nokia.carbide.cpp.internal.pi.visual.Defines;
import com.nokia.carbide.cpp.internal.pi.visual.PIEvent;
import com.nokia.carbide.cpp.internal.pi.visual.PIVisualSharedData;
import com.nokia.carbide.cpp.pi.editors.PIPageEditor;
import com.nokia.carbide.cpp.pi.util.TableColorPalette;


public class AddrThreadTable extends GenericAddrTable
{
	// local copy of profiled threads, which we can sort
	// without affecting the original
	Vector<ProfiledGeneric> profiledThreads = new Vector<ProfiledGeneric>();

	// changes made to support priority plugin
	private boolean priorityAdded = false;
	private Hashtable<Integer,String> priorityTable;
	private Hashtable<Integer,Integer> priorityValues;

	public AddrThreadTable(GppTraceGraph myGraph, Composite parent, GppModelAdapter adapter)
	{
		super(myGraph, parent, adapter);
	}

	public void createTableViewer(int drawMode)
	{
		if (this.parent == null)
			return;

		// Thread table:
		//		checkbox + colored or white background
		//  	percent load
		//  	thread name
		//		sample count
		//		priority (optional)

		// Check the drawMode, and use it to decide whether or not to show a color column
		// or the number of samples
		switch (drawMode)
		{
			case Defines.THREADS:
			case Defines.BINARIES_THREADS:
			case Defines.BINARIES_FUNCTIONS_THREADS:
			case Defines.FUNCTIONS_THREADS:
			case Defines.FUNCTIONS_BINARIES_THREADS:
			case Defines.THREADS_FUNCTIONS:
			case Defines.THREADS_FUNCTIONS_BINARIES:
			case Defines.THREADS_BINARIES:
			case Defines.THREADS_BINARIES_FUNCTIONS:
			case Defines.BINARIES_THREADS_FUNCTIONS:
			case Defines.FUNCTIONS_THREADS_BINARIES:
			{
				break;
			}
			default:
				// no thread table in this draw mode
				return;
		}

		// create the table viewer
		this.tableViewer = CheckboxTableViewer.newCheckList(this.parent,
  				SWT.BORDER | SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION);

		// add the check state handler, label provider and content provider
		tableViewer.addCheckStateListener(new SharedCheckHandler());
		tableViewer.setLabelProvider(new ShownThreadsLabelProvider());
		tableViewer.setContentProvider(new ShownThreadsContentProvider());
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
		this.table.setData(Messages.getString("AddrThreadTable.threads")); //$NON-NLS-1$
		
		// create the columns
		TableColumn column;

		// data associated with the TableViewer will note which columns contain hex values
		// Keep this in the order in which columns have been created
		// Includes the priority column
		boolean[] isHex = {false, false, false, false, false};
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

		// thread name column
		column = new TableColumn(table, SWT.LEFT);
		column.setText(COLUMN_HEAD_THREAD);
		column.setWidth(COLUMN_WIDTH_THREAD_NAME);
		column.setData(Integer.valueOf(COLUMN_ID_THREAD));
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

		// priority column
		if (priorityAdded)
			this.addPriorityColumn();

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

		// sort by percent load
		this.sortColumn = COLUMN_ID_SAMPLE_COUNT;
		this.sortAscending = false;

		// profiledThreads and tableItemData contain one entry per table row
		updateProfiledAndItemData(false);
		quickSort(sortColumn, profiledThreads);

		// initially, all rows are checked
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
		if (   (drawMode != Defines.THREADS)
			&& (drawMode != Defines.BINARIES_THREADS)
			&& (drawMode != Defines.BINARIES_FUNCTIONS_THREADS)
			&& (drawMode != Defines.FUNCTIONS_THREADS)
			&& (drawMode != Defines.FUNCTIONS_BINARIES_THREADS))
			return;

		ProfiledGeneric pGeneric;

		TableItem[] items = this.table.getItems();

		for (int i = 0; i < items.length; i++) {
			pGeneric = (ProfiledGeneric) items[i].getData();
//			Color color = ((GppTrace)this.myGraph.getTrace()).getThreadColorPalette().getColor(pGeneric.getNameString());
			items[i].setBackground(COLOR_COLUMN_INDEX, pGeneric.getColor());
		}

		table.redraw();
	}
	
	public void removeColor(int drawMode)
	{
		if (this.tableViewer == null)
			return;

		// make sure that this table's colors should not be shown
		if (   (drawMode == Defines.THREADS)
			|| (drawMode == Defines.BINARIES_THREADS)
			|| (drawMode == Defines.BINARIES_FUNCTIONS_THREADS)
			|| (drawMode == Defines.FUNCTIONS_THREADS)
			|| (drawMode == Defines.FUNCTIONS_BINARIES_THREADS))
			return;

		TableItem[] items = this.table.getItems();

		for (int i = 0; i < items.length; i++) {
			items[i].setBackground(COLOR_COLUMN_INDEX, this.parent.getDisplay().getSystemColor(SWT.COLOR_WHITE));
		}

		table.redraw();
	}

	private static class ShownThreadsContentProvider implements IStructuredContentProvider {

		public ShownThreadsContentProvider() {
			super();
		}

		public Object[] getElements(Object inputElement) {
			return ((Vector) inputElement).toArray();
		}

		public void dispose() {
		}

		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}
	}

	private class ShownThreadsLabelProvider extends LabelProvider implements ITableLabelProvider {

        public ShownThreadsLabelProvider() {
			super();
		}

		/* (non-Javadoc)
		 * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnText(java.lang.Object, int)
		 */
		public String getColumnText(Object element, int columnIndex) {

			int graphIndex = myGraph.getGraphIndex();
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
							pThreshold.setAverageLoadValueString(graphIndex, ""); //$NON-NLS-1$
						} else {
							float load = (float) (pThreshold.getSampleCount(graphIndex)/(endTime - startTime)/10.0);
							
							if (load < 0.005)
								pThreshold.setAverageLoadValueString(graphIndex, Messages.getString("AddrThreadTable.zeroFormat")); //$NON-NLS-1$
							else
								pThreshold.setAverageLoadValueString(graphIndex, load);
						}
						return pThreshold.getAverageLoadValueString(graphIndex);
					}
					case COLUMN_ID_THREAD:
					{
						DecimalFormat timeFormat = new DecimalFormat(Messages.getString("AddrThreadTable.decimalFormat")); //$NON-NLS-1$

						int count = pThreshold.getItemCount();

						return count + (count > 1 ? Messages.getString("AddrThreadTable.thresholdMsg1") : Messages.getString("AddrThreadTable.thresholdMsg2"))   //$NON-NLS-1$ //$NON-NLS-2$
								+ timeFormat.format((Double)NpiInstanceRepository.getInstance().activeUidGetPersistState("com.nokia.carbide.cpp.pi.address.thresholdLoadThread") * 100.0) + Messages.getString("AddrThreadTable.thresholdMsg3") //$NON-NLS-1$ //$NON-NLS-2$ 
								+ (Integer)NpiInstanceRepository.getInstance().activeUidGetPersistState("com.nokia.carbide.cpp.pi.address.thresholdCountThread") + Messages.getString("AddrThreadTable.thresholdMsg4"); //$NON-NLS-1$ //$NON-NLS-2$ 
					}
					case COLUMN_ID_SAMPLE_COUNT:
					{
						// Sample count
						return String.valueOf(pThreshold.getSampleCount(graphIndex));
					}
					default:
					{
						return ""; //$NON-NLS-1$
					}
				}
			}

			if (!(element instanceof ProfiledThread))
				return ""; //$NON-NLS-1$

			ProfiledThread profiledItem = (ProfiledThread) element;

			switch (columnId)
			{
				case COLUMN_ID_SHOW:
				{
					return SHOW_ITEM_VALUE;
				}
				case COLUMN_ID_PERCENT_LOAD:
				{
					// Percent load string
					Object object = profiledItem.getAdapter(ICPUScale.class);
					if(object != null && profiledItem.isScaledCpu()){
						String avgLoad = profiledItem.getAverageLoadValueString(graphIndex);
						if (avgLoad != null && avgLoad.length() > 0) {
							if(avgLoad.indexOf(',') != -1){ //$NON-NLS-1$
								avgLoad = avgLoad.replace(',', '.');
							}
							float value = Float.valueOf(avgLoad);
							if (value > 0.0) {
								ICPUScale cpuScale = (ICPUScale) object;
								value = value / 100;
								int startTime = (int) (PIPageEditor
										.currentPageEditor().getStartTime() * 1000);
								int endTime = (int) (PIPageEditor
										.currentPageEditor().getEndTime() * 1000);
								value = value
										* cpuScale.calculateScale(startTime,
												endTime) * 100;
								DecimalFormat decimalFormat = new DecimalFormat(
										"#0.00"); //$NON-NLS-1$
								return decimalFormat.format(value);
							}
						}									
					}
					return profiledItem.getAverageLoadValueString(graphIndex);
				}
				case COLUMN_ID_THREAD:
				{
					// Thread
					if (profiledItem.getNameString().startsWith("*Native*")) //$NON-NLS-1$
						return Messages.getString("AddrThreadTable.NOSthreads"); //$NON-NLS-1$
					else
						return profiledItem.getNameString();
				}
				case COLUMN_ID_SAMPLE_COUNT:
				{
					// Sample count
					return String.valueOf(profiledItem.getSampleCount(graphIndex));
				}
				case COLUMN_ID_PRIORITY:
			    {
					if (priorityAdded) {
				        String priority = priorityTable.get(Integer.valueOf(profiledItem.getThreadId()));
				        if (priority == null)
				        	return Messages.getString("AddrThreadTable.unknownPriority"); //$NON-NLS-1$
				        else
				        	return priority;
					}
					return ""; //$NON-NLS-1$
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
	    else if (actionString.equals("savePrioritySamples")) //$NON-NLS-1$
	    {
	    	SavePrioritySampleString saveSampleString = new SavePrioritySampleString(graphIndex, myGraph.getDrawMode());
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
		else if (actionString.equals("thread-only")) //$NON-NLS-1$
		{
			actionThread();
			return;
		}
		else if (actionString.equals("thread-binary")) //$NON-NLS-1$
		{
			actionThreadBinary();
			return;
		}
		else if (actionString.equals("thread-binary-function")) //$NON-NLS-1$
		{
			actionThreadBinaryFunction();
			return;
		}
		else if (actionString.equals("thread-function")) //$NON-NLS-1$
		{
			actionThreadFunction();
			return;
		}
		else if (actionString.equals("thread-function-binary")) //$NON-NLS-1$
		{
			actionThreadFunctionBinary();
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
		else if (   (actionString.equals("function-only")) //$NON-NLS-1$
				 || (actionString.equals("function-thread")) //$NON-NLS-1$
				 || (actionString.equals("function-thread-binary")) //$NON-NLS-1$
				 || (actionString.equals("function-binary")) //$NON-NLS-1$
				 || (actionString.equals("function-binary-thread"))) //$NON-NLS-1$
		{
			// let the function page action handler handle it
			this.myGraph.getFunctionTable().action(actionString);
			return;
		}
		else if (actionString.equals(IGppTraceGraph.ACTION_CHANGE_THRESHOLD_THREAD))
		{
			ProfiledThreshold threshold = this.myGraph.getThresholdThread();
			boolean enabled = threshold.isEnabled(graphIndex);

			this.tableItemData.clear();
			this.profiledThreads.clear();
			this.myGraph.getSortedThreads().clear();
			if (threshold.getItems() != null)
				threshold.getItems().clear();
			adapter.init(threshold, graphIndex);

			// if this appears, it needs to be the first item, so that it is drawn at the bottom
			myGraph.getSortedThreads().add(threshold);

			int threadThreshold = (Integer) NpiInstanceRepository.getInstance().activeUidGetPersistState("com.nokia.carbide.cpp.pi.address.thresholdCountThread"); //$NON-NLS-1$
			for (int i = 0; i < this.myGraph.getGppTrace().getSortedThreads().size(); i++) {
				ProfiledGeneric nextElement = (ProfiledGeneric)this.myGraph.getGppTrace().getSortedThreads().get(i);
				if (adapter.getTotalSampleCount(nextElement) < threadThreshold) {
					nextElement.setEnabled(graphIndex, enabled);
					adapter.addItem(threshold, graphIndex, nextElement, 0);
				} else {
					tableItemData.add(nextElement);
					profiledThreads.add(nextElement);
					myGraph.getSortedThreads().add(nextElement);
				}
			}

			if (threshold.getItemCount() != 0) {
				tableItemData.add(threshold);
				profiledThreads.add(threshold);
			} else {
				// remove the threshold item
				myGraph.getSortedThreads().remove(0);
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
	    else if (actionString.equals("saveTableTest")) //$NON-NLS-1$
	    {
			// copy save file contents to the clipboard for easy viewing
	        Clipboard cb = Toolkit.getDefaultToolkit().getSystemClipboard();
			SaveTableString getString = new SaveTableString(this.table, CHECKBOX_NO_TEXT, ",", "\n"); //$NON-NLS-1$ //$NON-NLS-2$
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

	        SaveDrillDownString getString = new SaveDrillDownString(tableCount, tables, ","); //$NON-NLS-1$
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
		shared.gppSelectedThreadNames = nameList;

  		if (   (totalSamples != 0)
      		|| (myGraph.getDrawMode() == Defines.THREADS))
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
		shared.gppSelectedThreadNames = nameList;

			selectionChangeNotify();
  		this.table.deselectAll();
	}
	
	private void actionRecolor()
	{
		int uid 			= this.myGraph.getUid();
		GppTrace gppTrace = this.myGraph.getGppTrace();

		// recolor selected items
		boolean didRecolor = false;

		TableItem[] selectedItems = table.getSelection();
		for (int i = 0; i < selectedItems.length;i++)
		{
			if (selectedItems[i].getData() instanceof ProfiledGeneric)
			{
				ProfiledGeneric pGeneric = (ProfiledGeneric)selectedItems[i].getData();
				TableColorPalette palette = ((GppTrace)this.myGraph.getTrace()).getThreadColorPalette();
				String nameKey = pGeneric.getNameString();
				if (palette.recolorEntryDialog(table.getShell(), nameKey))
				{
					Color color = palette.getColor(nameKey);
					Color oldColor = pGeneric.getColor();
					
					if (color.equals(oldColor))
						continue;
					
					didRecolor = true;
					
					if (!(pGeneric instanceof ProfiledThreshold)) {
						PIPageEditor.currentPageEditor().setDirty();
						pGeneric.setColor(color);
					} else {
						// for the threshold item, we must change every graph's thread threshold item
						// CH: refactor! This could be done via an observer pattern. This class should not have knowledge of all other graphs  
						gppTrace.getGppGraph(PIPageEditor.THREADS_PAGE,   uid).getThresholdThread().setColor(color);
						gppTrace.getGppGraph(PIPageEditor.BINARIES_PAGE,  uid).getThresholdThread().setColor(color);
						gppTrace.getGppGraph(PIPageEditor.FUNCTIONS_PAGE, uid).getThresholdThread().setColor(color);
						if (gppTrace.getCPUCount() > 1){ //SMP
							for (int cpu = 0; cpu < gppTrace.getCPUCount(); cpu++) {
								gppTrace.getGppGraph(3 + cpu, uid).getThresholdThread().setColor(color);															
							}
						}
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
			
			if (   (drawMode == Defines.THREADS)
				|| (drawMode == Defines.BINARIES_THREADS)
				|| (drawMode == Defines.BINARIES_FUNCTIONS_THREADS)
				|| (drawMode == Defines.FUNCTIONS_THREADS)
				|| (drawMode == Defines.FUNCTIONS_BINARIES_THREADS)) {
				graph.getThreadTable().addColor(drawMode);
				graph.setGraphImageChanged(true);	// any selection change to drill down will change graph
				graph.repaint();
			}
		}
	}

	private void actionThread()
	{
		// current drawMode should be THREADS, THREADS_BINARIES, THREADS_BINARIES_FUNCTIONS,
		// THREADS_FUNCTIONS, or THREADS_FUNCTIONS_BINARIES
		int drawMode = this.myGraph.getDrawMode();

		if (   (drawMode != Defines.THREADS_BINARIES)
			&& (drawMode != Defines.THREADS_BINARIES_FUNCTIONS)
			&& (drawMode != Defines.THREADS_FUNCTIONS)
			&& (drawMode != Defines.THREADS_FUNCTIONS_BINARIES))
		{
			// this case should be drawMode == Defines.THREADS
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
		if (   (this.myGraph.getBinaryTable() != null)
			&& (this.myGraph.getBinaryTable().getTable() != null)) {
			this.myGraph.getBinaryTable().getTableViewer().getTable().dispose();
			this.myGraph.getBinaryTable().setTableViewer(null);
		}
		if (   (this.myGraph.getFunctionTable() != null)
			&& (this.myGraph.getFunctionTable().getTable() != null)) {
			this.myGraph.getFunctionTable().getTableViewer().getTable().dispose();
			this.myGraph.getFunctionTable().setTableViewer(null);
		}

		// set the draw mode
		this.myGraph.setDrawMode(Defines.THREADS);

		// add colors to the rightmost table
		addColor(Defines.THREADS);

		this.parent.layout();

		this.myGraph.repaint();
	}

	private void actionThreadBinary()
	{
		// current drawMode should be THREADS, THREADS_BINARIES, or THREADS_BINARIES_FUNCTIONS
		int drawMode = this.myGraph.getDrawMode();
		int graphIndex = this.myGraph.getGraphIndex();

		if (   drawMode != Defines.THREADS
			&& drawMode != Defines.THREADS_BINARIES_FUNCTIONS) {
			return;
		}
		
		setIsDrilldown(true);

		if (drawMode == Defines.THREADS) {

			// set the draw mode before populating table viewers, since the
			// color column depends on the draw mode
			this.myGraph.setDrawMode(Defines.THREADS_BINARIES);

			// create the binary graph table viewer
			AddrBinaryTable binaryTable = this.myGraph.getBinaryTable();
			binaryTable.createTableViewer(Defines.THREADS_BINARIES);
			binaryTable.setIsDrilldown(true);

			// create a reduced set of binary entries based on enabled thread entries
			GppTrace gppTrace = (GppTrace) this.myGraph.getTrace();
			gppTrace.setThreadBinary(graphIndex, adapter, this.myGraph.getProfiledBinaries());

			// put check marks on all rows, and sort by sample count
			binaryTable.quickSort(COLUMN_ID_SAMPLE_COUNT, this.myGraph.getProfiledBinaries());
			binaryTable.updateProfiledAndItemData(true);
			binaryTable.getTableViewer().setAllChecked(true);
			binaryTable.getTableViewer().refresh();

			// put check marks on all enabled thread rows
			for (int i = 0; i < this.table.getItemCount(); i++) {
				ProfiledGeneric pThread = (ProfiledGeneric) this.table.getItem(i).getData();
				if (pThread.isEnabled(graphIndex))
					this.table.getItem(i).setChecked(true);
			}
			
			// remove colors where appropriate
			removeColor(Defines.THREADS_BINARIES);

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

			// attach the thread table to the sash
			try {
				FormData formData = (FormData) this.table.getLayoutData();
				formData.right = new FormAttachment(leftSash);
			} catch (ClassCastException e1) {
			}

			this.parent.layout();

			this.myGraph.repaint();

		} else if (drawMode == Defines.THREADS_BINARIES_FUNCTIONS) {

			// get rid of the function table and its sash
			if (this.myGraph.getRightSash() != null) {
				this.myGraph.getRightSash().dispose();
				this.myGraph.setRightSash(null);
			}
			if (   (this.myGraph.getFunctionTable() != null)
				&& (this.myGraph.getFunctionTable().getTable() != null)) {
				this.myGraph.getFunctionTable().getTableViewer().getTable().dispose();
				this.myGraph.getFunctionTable().setTableViewer(null);
			}

			// get rid of the middle table's connection to the sash
			try {
				FormData formData = (FormData) this.myGraph.getBinaryTable().getTable().getLayoutData();
				formData.right = new FormAttachment(100);
			} catch (ClassCastException e1) { //CH: refactor!  Runtime exceptions should not be caught!
			}

			// move the left sash to the middle
			try {
				FormData formData = (FormData) this.myGraph.getLeftSash().getLayoutData();
				formData.left = new FormAttachment(50); // middle
			} catch (ClassCastException e1) { //CH: refactor!  Runtime exceptions should not be caught!
			}

			// set the draw mode
			this.myGraph.setDrawMode(Defines.THREADS_BINARIES);

			// show colors in the rightmost table
			this.myGraph.getBinaryTable().addColor(Defines.THREADS_BINARIES);

			this.parent.layout();

			this.myGraph.repaint();
		}

		// this case should be drawMode == Defines.THREADS_BINARIES
		return;
	}

	private void actionThreadBinaryFunction()
	{
		// current drawMode should be THREADS_BINARIES or THREADS_BINARIES_FUNCTIONS
		int drawMode = this.myGraph.getDrawMode();
		int graphIndex = this.myGraph.getGraphIndex();

		if (drawMode != Defines.THREADS_BINARIES) {
			// this case should be drawMode == Defines.THREADS_BINARIES_FUNCTIONS
			return;
		}

		setIsDrilldown(true);

		// set the draw mode before populating table viewers, since the
		// color column depends on the draw mode
		this.myGraph.setDrawMode(Defines.THREADS_BINARIES_FUNCTIONS);

		// create the function graph table viewer
		AddrFunctionTable functionTable = this.myGraph.getFunctionTable();
		functionTable.createTableViewer(Defines.THREADS_BINARIES_FUNCTIONS);
		functionTable.setIsDrilldown(true);

		// create a reduced set of function entries based on enabled thread and binary entries
		GppTrace gppTrace = (GppTrace) this.myGraph.getTrace();
		gppTrace.setThreadBinaryFunction(graphIndex, adapter, this.myGraph.getProfiledFunctions());

		// put check marks on all rows, and sort by sample count
		functionTable.quickSort(COLUMN_ID_SAMPLE_COUNT, this.myGraph.getProfiledFunctions());
		functionTable.updateProfiledAndItemData(true);
		functionTable.getTableViewer().setAllChecked(true);
		functionTable.getTableViewer().refresh();

		// remove colors where appropriate
		this.myGraph.getBinaryTable().removeColor(Defines.THREADS_BINARIES_FUNCTIONS);

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

		// attach the function table to the sash
		final FormData viewerData = new FormData();
		viewerData.top    = new FormAttachment(0);
		viewerData.bottom = new FormAttachment(100);
		viewerData.left   = new FormAttachment(rightSash);
		viewerData.right  = new FormAttachment(100);
		functionTable.getTable().setLayoutData(viewerData);

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

	private void actionThreadFunction()
	{
		// current drawMode should be THREADS, THREADS_FUNCTIONS, or THREADS_FUNCTIONS_BINARIES
		int drawMode = this.myGraph.getDrawMode();
		int graphIndex = this.myGraph.getGraphIndex();

		if (   drawMode != Defines.THREADS
			&& drawMode != Defines.THREADS_FUNCTIONS_BINARIES) {
			return;
		}

		setIsDrilldown(true);

		if (drawMode == Defines.THREADS) {

			// set the draw mode
			this.myGraph.setDrawMode(Defines.THREADS_FUNCTIONS);

			// create the function graph table viewer
			AddrFunctionTable functionTable = this.myGraph.getFunctionTable();
			functionTable.createTableViewer(Defines.THREADS_FUNCTIONS);
			functionTable.setIsDrilldown(true);

			// create a reduced set of function entries based on enabled thread and binary entries
			GppTrace gppTrace = (GppTrace) this.myGraph.getTrace();
			gppTrace.setThreadFunction(graphIndex, adapter, this.myGraph.getProfiledFunctions());

			// put check marks on all rows, and sort by sample count
			functionTable.quickSort(COLUMN_ID_SAMPLE_COUNT, this.myGraph.getProfiledFunctions());
			functionTable.updateProfiledAndItemData(true);
			functionTable.getTableViewer().setAllChecked(true);
			functionTable.getTableViewer().refresh();

			// remove colors where appropriate
			removeColor(Defines.THREADS_FUNCTIONS);

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

			// attach the function table to the sash
			final FormData viewerData = new FormData();
			viewerData.top    = new FormAttachment(0);
			viewerData.bottom = new FormAttachment(100);
			viewerData.left   = new FormAttachment(leftSash);
			viewerData.right  = new FormAttachment(100);
			functionTable.getTable().setLayoutData(viewerData);

			// attach the thread table to the sash
			try {
				FormData formData = (FormData) this.table.getLayoutData();
				formData.right = new FormAttachment(leftSash);
			} catch (ClassCastException e1) {
			}

			this.parent.layout();

			this.myGraph.repaint();

		} else if (drawMode == Defines.THREADS_FUNCTIONS_BINARIES) {
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
				FormData formData = (FormData) this.myGraph.getFunctionTable().getTable().getLayoutData();
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
			this.myGraph.setDrawMode(Defines.THREADS_FUNCTIONS);

			// show colors in the rightmost table
			this.myGraph.getFunctionTable().addColor(Defines.THREADS_FUNCTIONS);

			this.parent.layout();

			this.myGraph.repaint();
		}

		// this case should be drawMode == Defines.THREADS_BINARIES
		return;
	}

	private void actionThreadFunctionBinary()
	{
		// current drawMode is THREADS_FUNCTIONS, or THREADS_FUNCTIONS_BINARIES
		int drawMode   = this.myGraph.getDrawMode();
		int graphIndex = this.myGraph.getGraphIndex();

		if (drawMode != Defines.THREADS_FUNCTIONS) {
			// this case should be drawMode == Defines.THREADS_FUNCTIONS_BINARIES
			return;
		}

		setIsDrilldown(true);

		// set the draw mode
		this.myGraph.setDrawMode(Defines.THREADS_FUNCTIONS_BINARIES);

		// create the binary graph table viewer
		AddrBinaryTable binaryTable = this.myGraph.getBinaryTable();
		binaryTable.createTableViewer(Defines.THREADS_FUNCTIONS_BINARIES);
		binaryTable.setIsDrilldown(true);

		// create a reduced set of binary entries based on enabled thread and function entries
		GppTrace gppTrace = (GppTrace) this.myGraph.getTrace();
		gppTrace.setThreadFunctionBinary(graphIndex, adapter, this.myGraph.getProfiledBinaries());

		// put check marks on all rows, and sort by sample count
		binaryTable.quickSort(COLUMN_ID_SAMPLE_COUNT, this.myGraph.getProfiledBinaries());
		binaryTable.updateProfiledAndItemData(true);
		binaryTable.getTableViewer().setAllChecked(true);
		binaryTable.getTableViewer().refresh();

		// remove colors where appropriate
		this.myGraph.getFunctionTable().removeColor(Defines.THREADS_FUNCTIONS_BINARIES);

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

		// attach the function table to the sash
		try {
			FormData formData = (FormData) this.myGraph.getFunctionTable().getTable().getLayoutData();
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
					quickSort(sortColumn, profiledThreads);

					// initially, all rows are selected
					tableViewer.setAllChecked(true);

					table.redraw();
				}
			});
		}
		else if (   (be.getType() == PIEvent.SELECTION_AREA_CHANGED2)
				 || (be.getType() == PIEvent.CHANGED_THREAD_TABLE))
		{
			int graphIndex = this.myGraph.getGraphIndex();

			// This routine does not change which threads are enabled, but it enables
			// all entries in the 2nd and 3rd tables.
			// It assumes that GppTrace.setSelectedArea(), action("add") or action("remove") has set
			// the % load and sample counts for threads, except for the threshold list.
			if (be.getType() == PIEvent.SELECTION_AREA_CHANGED2) {
				int thresholdCount = (Integer)NpiInstanceRepository.getInstance().activeUidGetPersistState("com.nokia.carbide.cpp.pi.address.thresholdCountThread"); //$NON-NLS-1$
				if (thresholdCount > 0) {
					Vector<ProfiledGeneric> pGeneric = this.myGraph.getProfiledThreads();
					int sampleCount = 0;
					for (int i = 0; i < pGeneric.size(); i++)
						if (adapter.getTotalSampleCount(pGeneric.elementAt(i)) < thresholdCount)
							sampleCount += pGeneric.elementAt(i).getSampleCount(graphIndex);
					this.myGraph.getThresholdThread().setSampleCount(graphIndex, sampleCount);
				}
			}

			// redraw this table
			Display.getDefault().syncExec(new Runnable() {
				public void run() {
					if (   (sortColumn == COLUMN_ID_PERCENT_LOAD)
						|| (sortColumn == COLUMN_ID_SAMPLE_COUNT))
					{
						quickSort(sortColumn, profiledThreads);
					}
					else
						refreshTableViewer();

					table.redraw();
				}
			});

			int drawMode = this.myGraph.getDrawMode();
			if (drawMode == Defines.THREADS)
				return;

			GppTrace trace = (GppTrace)(this.myGraph.getTrace());
			PIEvent be3 = new PIEvent(be.getValueObject(), PIEvent.SELECTION_AREA_CHANGED3);

			switch (drawMode) {
				case Defines.THREADS_BINARIES:
				{
					trace.setThreadBinary(graphIndex, adapter, this.myGraph.getProfiledBinaries());

					// update the table items and redraw the table
					this.myGraph.getBinaryTable().piEventReceived(be3);
					break;
				}
				case Defines.THREADS_BINARIES_FUNCTIONS:
				{
					// previous binaries are not necessarily graphed this time - reset
					trace.setThreadBinary(graphIndex, adapter, this.myGraph.getProfiledBinaries());
					// previous functions are not necessarily graphed this time - reset
					trace.setThreadBinaryFunction(graphIndex, adapter, this.myGraph.getProfiledFunctions());

					// update the table items and redraw the table
					this.myGraph.getBinaryTable().piEventReceived(be3);
					this.myGraph.getFunctionTable().piEventReceived(be3);
					break;
				}
				case Defines.THREADS_FUNCTIONS:
				{
					// previous functions are not necessarily graphed this time
					trace.setThreadFunction(graphIndex, adapter, this.myGraph.getProfiledFunctions());

					// update the table items and redraw the table
					this.myGraph.getFunctionTable().piEventReceived(be3);
					break;
				}
				case Defines.THREADS_FUNCTIONS_BINARIES:
				{
					// previous functions and binaries are not necessarily graphed this time
					trace.setThreadFunction(graphIndex, adapter, this.myGraph.getProfiledFunctions());
					trace.setThreadFunctionBinary(graphIndex, adapter, this.myGraph.getProfiledBinaries());

					// update the table items and redraw the table
					this.myGraph.getBinaryTable().piEventReceived(be3);
					this.myGraph.getFunctionTable().piEventReceived(be3);
					break;
				}
				default:
				{
					break;
				}
			}
		}
		else if (be.getType() == PIEvent.CHANGED_BINARY_TABLE)
		{
			// This routine enables all entries in the next table

			int drawMode = this.myGraph.getDrawMode();
			if (drawMode != Defines.THREADS_BINARIES_FUNCTIONS)
				return;
			
			// we don't need to redraw the thread table, since it has not changed
			PIEvent be3 = new PIEvent(be.getValueObject(), PIEvent.SELECTION_AREA_CHANGED3);
			GppTrace trace = (GppTrace)(this.myGraph.getTrace());
			int graphIndex = this.myGraph.getGraphIndex();
			trace.setThreadBinaryFunction(graphIndex, adapter, this.myGraph.getProfiledFunctions());

			// update the table items and redraw the table
			this.myGraph.getFunctionTable().piEventReceived(be3);
		}
		else if (be.getType() == PIEvent.CHANGED_FUNCTION_TABLE)
		{
			// This routine enables all entries in the next table

			int drawMode = this.myGraph.getDrawMode();
			if (drawMode != Defines.THREADS_FUNCTIONS_BINARIES)
				return;

			// we don't need to redraw the thread table, since it has not changed
			PIEvent be3 = new PIEvent(be.getValueObject(), PIEvent.SELECTION_AREA_CHANGED3);
			GppTrace trace = (GppTrace)(this.myGraph.getTrace());
			int graphIndex = this.myGraph.getGraphIndex();
			trace.setThreadFunctionBinary(graphIndex, adapter, this.myGraph.getProfiledBinaries());

			// update the table items and redraw the table
			this.myGraph.getBinaryTable().piEventReceived(be3);
		}
	}

	/**
	 * this table's set of checkbox-selected rows has changed, so propagate that information
	 */
	private void setSelectedNames()
	{
		Object[] selectedValues = this.tableViewer.getCheckedElements();
        String[] threadNames = new String[selectedValues.length];

        for (int i = 0; i < selectedValues.length; i++)
		{
			if (selectedValues[i] instanceof ProfiledThread)
			{
				ProfiledThread pt = (ProfiledThread)selectedValues[i];
				threadNames[i] = pt.getNameString();
			}
		}

        PIVisualSharedData shared = myGraph.getSharedDataInstance();
		shared.gppSelectedThreadNames = threadNames;
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
       			|| (myGraph.getDrawMode() == Defines.THREADS))
       			selectionChangeNotify();

       		table.deselectAll();
		}
	}

	protected void selectionChangeNotify() {
		PIEvent be = new PIEvent(null, PIEvent.CHANGED_THREAD_TABLE);
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
					case COLUMN_ID_PRIORITY:
					{
		            	// sort in descending order (for checked boxes column, this means selected boxes first)
		            	sortAscending = false;
		                break;
					}
					case COLUMN_ID_THREAD:
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
			quickSort(sortColumn, profiledThreads);
	}

	private class ColumnSelectionHandler extends SelectionAdapter
	{
		/* (non-Javadoc)
		 * @see org.eclipse.swt.events.SelectionAdapter#widgetSelected(org.eclipse.swt.events.SelectionEvent)
		 */
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
		profiledThreads.clear();

		// profiledThreads and tableItemData contain one entry per table row
		Enumeration<ProfiledGeneric> enu = myGraph.getProfiledThreads().elements();
		while (enu.hasMoreElements())
		{
			ProfiledThread nextElement = (ProfiledThread)enu.nextElement();
			tableItemData.add(nextElement);
			profiledThreads.add(nextElement);
		}

		if (myGraph.getThresholdThread().getItemCount() != 0) {
			tableItemData.add(myGraph.getThresholdThread());
			profiledThreads.add(myGraph.getThresholdThread());
		}
		
		// now sort the items in increasing total sample count, so that they graph correctly
		Object[] sorted = myGraph.getProfiledThreads().toArray();
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
		myGraph.getSortedThreads().clear();

		if (myGraph.getThresholdThread().getItemCount() != 0)
			myGraph.getSortedThreads().add(myGraph.getThresholdThread());

		for (int i = 0; i < sorted.length; i++)
			myGraph.getSortedThreads().add((ProfiledGeneric) sorted[i]);

		// refresh the table, if needed
		if (setInput)
			refreshTableViewer();
	}

	public void quickSort(int sortBy, Vector<ProfiledGeneric> profiled)
	{
		if (profiled.size() == 0)
			return;

		this.sorting = true;

		//if the last element is a threshold element - remove it
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
			case COLUMN_ID_PRIORITY:
			{
			    this.sorter.quickSortByPriority(profiled, priorityValues);
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

	protected MenuItem getSavePrioritySamplesItem(Menu menu, boolean enabled) {
	    MenuItem saveSamplesItem = new MenuItem(menu, SWT.PUSH);

		saveSamplesItem.setText(Messages.getString("AddrThreadTable.savePrioritySamples")); //$NON-NLS-1$
		saveSamplesItem.setEnabled(enabled);
		
		if (enabled) {
			saveSamplesItem.addSelectionListener(new SelectionAdapter() { 
				@Override
				public void widgetSelected(SelectionEvent e) {
					action("savePrioritySamples"); //$NON-NLS-1$
				}
			});
		}

		return saveSamplesItem;
	}

	@Override
	protected Menu getTableMenu(Decorations parent, int graphIndex, int drawMode) {

		// get rid of last Menu created so we don't have double menu
		// in on click
		if (contextMenu != null) {
			contextMenu.dispose();
		}
		
		// recreate each time, in case the drawMode has changed since creation
		contextMenu = new Menu(parent, SWT.POP_UP);

		// Use drawMode to determine the drill down items and
		// whether to show a color column
		addDrillDownItems(contextMenu, drawMode);
		
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
		
		double startTime = PIPageEditor.currentPageEditor().getStartTime();
		double endTime   = PIPageEditor.currentPageEditor().getEndTime();

		// save raw samples
		boolean haveSamples = false;
		for (int i = 0; !haveSamples && i < profiledThreads.size(); i++) {
			ProfiledGeneric pg = profiledThreads.get(i);
			haveSamples = pg.isEnabled(graphIndex) && pg.getSampleCount(graphIndex) > 0;
		}
		
		if (!haveSamples || (startTime == -1) || (endTime   == -1) || (startTime == endTime))
			getSaveSamplesItem(contextMenu, Messages.getString("AddrThreadTable.threads"), false); //$NON-NLS-1$
		else
			getSaveSamplesItem(contextMenu, Messages.getString("AddrThreadTable.threads"), true); //$NON-NLS-1$
		
		// save priority samples
		if (priorityAdded) {
			if (!haveSamples || (startTime == -1) || (endTime   == -1) || (startTime == endTime))
				getSavePrioritySamplesItem(contextMenu, false);
			else
				getSavePrioritySamplesItem(contextMenu, true);
		}

		// recolor selected threads
		switch (drawMode)
		{
			case Defines.THREADS:
			case Defines.BINARIES_THREADS:
			case Defines.BINARIES_FUNCTIONS_THREADS:
			case Defines.FUNCTIONS_THREADS:
			case Defines.FUNCTIONS_BINARIES_THREADS:
			{
				// recolor selected items
				new MenuItem(contextMenu, SWT.SEPARATOR);
				getRecolorItem(contextMenu, Messages.getString("AddressPlugin.threads"),  //$NON-NLS-1$
								this.table.getSelectionCount() > 0);
				break;
			}
			case Defines.THREADS_FUNCTIONS:
			case Defines.THREADS_FUNCTIONS_BINARIES:
			case Defines.THREADS_BINARIES:
			case Defines.THREADS_BINARIES_FUNCTIONS:
			case Defines.BINARIES_THREADS_FUNCTIONS:
			case Defines.FUNCTIONS_THREADS_BINARIES:
			{
				break;
			}
			default:
				break;
		}

		new MenuItem(contextMenu, SWT.SEPARATOR);
		getChangeThresholds(contextMenu);

		contextMenu.setVisible(true);
		
		return contextMenu;
	}

	protected void addPriorityColumn(Hashtable<Integer,String> priorities)
	{
		if (priorityAdded)
			return;
		
		// store the priority information
		this.priorityTable = priorities;
	    priorityValues = new Hashtable<Integer,Integer>();
		for (Enumeration<Integer> e = priorityTable.keys(); e.hasMoreElements();)
		{
			Integer key = e.nextElement();
			priorityValues.put(key, parsePriorityValue(this.priorityTable.get(key)));
		}

		// if the tableViewer already exists, add a column
		if (   (tableViewer != null)
			&& (tableViewer.getTable() != null))
	    {
			Display.getDefault().syncExec( new Runnable() {
				public void run() {
					addPriorityColumn();
				}
			});	
	    }

		priorityAdded = true;
	}
	
	private void addPriorityColumn()
	{
		TableColumn column;
		column = new TableColumn(tableViewer.getTable(), SWT.LEFT);
		column.setText(COLUMN_HEAD_PRIORITY);
		column.setWidth(COLUMN_WIDTH_PRIORITY);
		column.setData(Integer.valueOf(COLUMN_ID_PRIORITY));
		column.setMoveable(true);
		column.setResizable(true);
		column.addSelectionListener(new ColumnSelectionHandler());
	}

	private Integer parsePriorityValue(String priorityString)
	{
		if (priorityString == null || priorityString.equals(Messages.getString("AddrThreadTable.unsolvedPriority"))) //$NON-NLS-1$
			return Integer.MIN_VALUE;
		else
		{
			int endIndex = priorityString.indexOf(Messages.getString("AddrThreadTable.priorityStringEnding")); //$NON-NLS-1$
			int beginIndex = priorityString.indexOf(Messages.getString("AddrThreadTable.priorityStringStart")); //$NON-NLS-1$
			String value = priorityString.substring(beginIndex+2, endIndex);
			return Integer.parseInt(value);
		}
	}

    /** class to pass sample data to the save wizard */
    public class SavePrioritySampleString implements ISaveSamples {
    	int graphIndex;
    	int drawMode;
    	boolean done = false;
    	
    	/**
    	 * Constructor
    	 * @param graphIndex
    	 * @param drawMode
    	 */
    	public SavePrioritySampleString(int graphIndex, int drawMode) {
    		this.graphIndex = graphIndex;
    		this.drawMode   = drawMode;
		}

    	/* (non-Javadoc)
    	 * @see com.nokia.carbide.cpp.internal.pi.interfaces.ISaveSamples#getData()
    	 */
    	public String getData() {
    		if (done)
    			return null;
    		
			String returnString = getPrioritySampleString(graphIndex, drawMode);
			done = true;
			return returnString;
		}

		public String getData(int size) {
 			return getData();
		}

		/* (non-Javadoc)
		 * @see com.nokia.carbide.cpp.internal.pi.interfaces.ISaveSamples#getIndex()
		 */
		public int getIndex() {
			return done ? 1 : 0;
		}

		/* (non-Javadoc)
		 * @see com.nokia.carbide.cpp.internal.pi.interfaces.ISaveSamples#clear()
		 */
		public void clear() {
			done = false;
		}
    }
    
    protected class PrioritySample {
    	int time;
    	ProfiledThread pt;
    	String priorityString;
    	
    	PrioritySample(int time, ProfiledThread pt, String priorityString) {
    		this.time = time;
    		this.pt   = pt;
    		this.priorityString = priorityString;
    	}
    }

    protected void createPrioritySample(ArrayList<PrioritySample> prioritySamples, int graphIndex, ProfiledThread pt, int startTime, int endTime) {
    	PrioritySample localSample = null;
    	boolean addedLocalSample = false;
    	String priority = priorityTable.get(Integer.valueOf(pt.getThreadId()));
		
		if (priority == null) {
			prioritySamples.add(new PrioritySample(0, pt, Messages.getString("AddrThreadTable.unknownPriority"))); //$NON-NLS-1$
			return;
		}
		
		localSample = new PrioritySample(0, pt, Messages.getString("AddrThreadTable.unrecordedPriority")); //$NON-NLS-1$
		addedLocalSample = false;
		
		while (priority.indexOf('(') != -1) {
			while (priority.charAt(0) == ' ')		// remove leading spaces
				priority = priority.substring(1);
			
			int nextMatch = priority.indexOf('(');
			String priorityStr = priority.substring(0, nextMatch);	// priority is before the open paren
			priority = priority.substring(nextMatch + 1);			// consume the paren, too 
			
			nextMatch = priority.indexOf(')');
			String timeStr = priority.substring(0, nextMatch - 1);
			
			double d = Double.parseDouble(timeStr) * 1000.0; 
			int time = (int) d;
			
			if (time > endTime) {
				// we're past the end
				if (!addedLocalSample) {
					// previous priority was before the start time, but include it anyway
					prioritySamples.add(localSample);
					return;
				}
			} else if (time < startTime) {
				// we're before the start
				// only keep the priority closest to the start time
				localSample = new PrioritySample(time, pt, priorityStr);
			} else {
				// we're in the interval, so add the previous sample, if needed
				if (!addedLocalSample && time != startTime)
					prioritySamples.add(localSample);

				localSample = new PrioritySample(time, pt, priorityStr);
				prioritySamples.add(localSample);
				addedLocalSample = true;
			}
			priority = priority.substring(nextMatch + 1);
		}
		
		if (localSample != null && !addedLocalSample)
			prioritySamples.add(localSample);
    }
    
    protected class PrioritySampleCompare implements Comparator<PrioritySample> {
    	public int compare(PrioritySample ps1, PrioritySample ps2) {
    		try {
    			if (ps1.time == ps2.time)
    				return ps1.pt.getNameString().compareToIgnoreCase(ps2.pt.getNameString());
    			else
    				return ps1.time - ps2.time;
    		} catch (Exception e) {
    			return 0;
    		}
    	}
    }
    
	protected String getPrioritySampleString(int graphIndex, int drawMode)
	{
		GppTraceGraph graph = (GppTraceGraph)(this.myGraph);
		GppTrace trace = (GppTrace)(graph.getTrace());
		
		int startIndex = trace.getStartSampleIndex();
		int endIndex   = trace.getEndSampleIndex();

		ArrayList<PrioritySample> prioritySamples = new ArrayList<PrioritySample>();
		ProfiledThread pt;
		
		for (int i = 0; i < profiledThreads.size(); i++) {
			if (!profiledThreads.get(i).isEnabled(graphIndex) || profiledThreads.get(i).getSampleCount(graphIndex) <= 0)
				continue;

			if (profiledThreads.get(i) instanceof ProfiledThread) {
				pt = (ProfiledThread) profiledThreads.get(i);

				createPrioritySample(prioritySamples, graphIndex, pt, startIndex, endIndex); 
			} else if (profiledThreads.get(i) instanceof ProfiledThreshold) {
				ProfiledThreshold pth = (ProfiledThreshold) profiledThreads.get(i);
				for (int j = 0; j < pth.getItems().size(); j++) {
					pt = (ProfiledThread) pth.getItems().get(j);
					
					if (pt.isEnabled(graphIndex) && pt.getSampleCount(graphIndex) > 0)
						createPrioritySample(prioritySamples, graphIndex, pt, startIndex, endIndex); 
				}
			}
		}
		
		Collections.sort(prioritySamples, new PrioritySampleCompare());
		
		String returnString = "Time (ms),Thread,Priority\n";  //$NON-NLS-1$
			
			for (PrioritySample sample : prioritySamples) {
				returnString +=   sample.time + "," + sample.pt.getNameString() + "," //$NON-NLS-1$ //$NON-NLS-2$
								+ sample.priorityString + "\n";  //$NON-NLS-1$ //$NON-NLS-2$
			}
		
		return returnString;
	}
	
}
