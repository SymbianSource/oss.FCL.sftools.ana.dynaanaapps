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
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Vector;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IContributionManager;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.SubMenuManager;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Decorations;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.ide.IIDEActionConstants;

import com.nokia.carbide.cpp.internal.pi.address.GppModelAdapter;
import com.nokia.carbide.cpp.internal.pi.interfaces.ISaveSamples;
import com.nokia.carbide.cpp.internal.pi.interfaces.ISaveTable;
import com.nokia.carbide.cpp.internal.pi.model.ProfiledBinary;
import com.nokia.carbide.cpp.internal.pi.model.ProfiledFunction;
import com.nokia.carbide.cpp.internal.pi.model.ProfiledGeneric;
import com.nokia.carbide.cpp.internal.pi.model.ProfiledThread;
import com.nokia.carbide.cpp.internal.pi.model.ProfiledThreshold;
import com.nokia.carbide.cpp.internal.pi.save.SaveTableWizard;
import com.nokia.carbide.cpp.internal.pi.visual.Defines;
import com.nokia.carbide.cpp.internal.pi.visual.GenericTable;
import com.nokia.carbide.cpp.pi.editors.PIPageEditor;


public abstract class GenericAddrTable extends GenericTable
{
	protected GppTraceGraph myGraph;
	protected Composite     parent;
	protected GppModelAdapter adapter;

	// sorting column and order
	protected int     sortColumn = COLUMN_ID_SAMPLE_COUNT;
	protected boolean sortAscending;

	protected GppTableSorter sorter;
	protected boolean sorting = false;

	abstract protected Menu getTableMenu(Decorations parent, int graphIndex, int drawMode);
	
	protected boolean isDrilldown = false;
    
	// menu items
	protected Action selectAllAction;
	protected Action copyTableAction;
	protected Action copyAction;
	protected Action copyDrilldownAction;
	protected Action saveTableAction;
	protected Action saveDrilldownAction;
	
	protected static int SAMPLES_AT_ONE_TIME = 1000;
	
	public GenericAddrTable(GppTraceGraph myGraph, Composite parent, GppModelAdapter adapter)
	{
		this.myGraph = myGraph;
		this.parent  = parent;
		this.adapter = adapter;
	}

	// class to pass sample data to the save wizard
    public class SaveSampleString implements ISaveSamples {
     	int graphIndex;
    	int drawMode;
    	int startIndex = 0;
    	
    	public SaveSampleString(int graphIndex, int drawMode) {
    		this.graphIndex = graphIndex;
    		this.drawMode   = drawMode;
		}

    	public String getData() {
    		return getData(SAMPLES_AT_ONE_TIME);
		}

		public String getData(int size) {
			String returnString = getSampleString(graphIndex, drawMode, this.startIndex, this.startIndex + size);
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
	
	protected MenuItem getSaveSamplesItem(Menu menu, String sampleType, boolean enabled) {
	    MenuItem saveSamplesItem = new MenuItem(menu, SWT.PUSH);

		saveSamplesItem.setText(Messages.getString("GenericAddrTable.saveSamples1") + sampleType + Messages.getString("GenericAddrTable.saveSamples2"));   //$NON-NLS-1$ //$NON-NLS-2$
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

	public GppTraceGraph getGraph() {
		return myGraph;
	}

	protected class TableMouseListener implements MouseListener
	{
		public void mouseDoubleClick(org.eclipse.swt.events.MouseEvent e) {
			if (e.button == MouseEvent.BUTTON1)
			{
				TableItem[] selectedItems = table.getSelection();
				if (selectedItems.length == 0)
					return;

				if (selectedItems[0].getData() instanceof ProfiledGeneric)
				{
					ProfiledGeneric pg = (ProfiledGeneric)(selectedItems[0].getData());
				    if (pg.isEnabled(myGraph.getGraphIndex()))
				        action("remove"); //$NON-NLS-1$
				    else
				        action("add"); //$NON-NLS-1$
				    action("doubleClick");  //$NON-NLS-1$
				}
			}
		}

		public void mouseDown(org.eclipse.swt.events.MouseEvent e) {
		}

		public void mouseUp(org.eclipse.swt.events.MouseEvent e) {
			if (e.button == MouseEvent.BUTTON3) {
				Menu menu = getTableMenu(table.getShell(), myGraph.getGraphIndex(), myGraph.getDrawMode());
				menu.setLocation(parent.toDisplay(e.x + table.getLocation().x, e.y + table.getLocation().y));
				table.setMenu(menu);
			}
		}
	}

	public void setVisualisationColumnVisible(boolean flag) {}

	public void valueChanged(SelectionEvent e) {}

	protected Vector<ProfiledGeneric> setTableItemData(Enumeration<ProfiledGeneric> enu) {
	    Vector<ProfiledGeneric> tmpItems = new Vector<ProfiledGeneric>();

	    while (enu.hasMoreElements())
		{
		    tmpItems.add(enu.nextElement());
		}
		return tmpItems;
	}

	protected void addDrillDownItems(Menu tableMenu, int drawMode)
	{
		switch (drawMode)
		{
			case Defines.THREADS:
			{
				// thread drill down items
				threadItem        (tableMenu, true);
				threadBinaryItem  (tableMenu, false);
				threadFunctionItem(tableMenu, false);
				break;
			}
			case Defines.THREADS_BINARIES:
			{
				// thread drill down items
				threadItem              (tableMenu, false);
				threadBinaryItem        (tableMenu, true);
				threadBinaryFunctionItem(tableMenu, false);
				break;
			}
			case Defines.THREADS_BINARIES_FUNCTIONS:
			{
				// thread drill down items
				threadItem              (tableMenu, false);
				threadBinaryItem        (tableMenu, false);
				threadBinaryFunctionItem(tableMenu, true);
				break;
			}
			case Defines.THREADS_FUNCTIONS:
			{
				// thread drill down items
				threadItem             (tableMenu, false);
				threadFunctionItem     (tableMenu, true);
				newThreadFunctionBinary(tableMenu, false);
				break;
			}
			case Defines.THREADS_FUNCTIONS_BINARIES:
			{
				// thread drill down items
				threadItem             (tableMenu, false);
				threadFunctionItem     (tableMenu, false);
				newThreadFunctionBinary(tableMenu, true);
				break;
			}
			case Defines.BINARIES:
			{
				// binary drill down items
				binaryItem        (tableMenu, true);
				binaryThreadItem  (tableMenu, false);
				binaryFunctionItem(tableMenu, false);
				break;
			}
			case Defines.BINARIES_THREADS:
			{
				// binary drill down items
				binaryItem              (tableMenu, false);
				binaryThreadItem        (tableMenu, true);
				binaryThreadFunctionItem(tableMenu, false);
				break;
			}
			case Defines.BINARIES_THREADS_FUNCTIONS:
			{
				// binary drill down items
				binaryItem              (tableMenu, false);
				binaryThreadItem        (tableMenu, false);
				binaryThreadFunctionItem(tableMenu, true);
				break;
			}
			case Defines.BINARIES_FUNCTIONS:
			{
				// binary drill down items
				binaryItem              (tableMenu, false);
				binaryFunctionItem      (tableMenu, true);
				binaryFunctionThreadItem(tableMenu, false);
				break;
			}
			case Defines.BINARIES_FUNCTIONS_THREADS:
			{
				// binary drill down items
				binaryItem              (tableMenu, false);
				binaryFunctionItem      (tableMenu, false);
				binaryFunctionThreadItem(tableMenu, true);
				break;
			}
			case Defines.FUNCTIONS:
			{
				// function drill down items 
				functionItem      (tableMenu, true);
				functionThreadItem(tableMenu, false);
				functionBinaryItem(tableMenu, false);
				break;
			}
			case Defines.FUNCTIONS_THREADS:
			{
				// function drill down items 
				functionItem            (tableMenu, false);
				functionThreadItem      (tableMenu, true);
				functionThreadBinaryItem(tableMenu, false);
				break;
			}
			case Defines.FUNCTIONS_THREADS_BINARIES:
			{
				// function drill down items 
				functionItem            (tableMenu, false);
				functionThreadItem      (tableMenu, false);
				functionThreadBinaryItem(tableMenu, true);
				break;
			}
			case Defines.FUNCTIONS_BINARIES:
			{
				// function drill down items 
				functionItem            (tableMenu, false);
				functionBinaryItem      (tableMenu, true);
				functionBinaryThreadItem(tableMenu, false);
				break;
			}
			case Defines.FUNCTIONS_BINARIES_THREADS:
			{
				// function drill down items 
				functionItem            (tableMenu, false);
				functionBinaryItem      (tableMenu, false);
				functionBinaryThreadItem(tableMenu, true);
				break;
			}
			default:
				break;
		}
	}

	//unused?
//	/*
//	 * Find if any threads with checkboxes checked
//	 */
//	protected boolean haveCheckedThread(GppTrace trace, int graphIndex)
//	{
//		ProfiledThread pThread;
//		for (ProfiledGeneric pGeneric: trace.getIndexedThreads()) {
//			if (pGeneric instanceof ProfiledThread) {
//				pThread = (ProfiledThread) pGeneric;
//				if (pThread.isEnabled(graphIndex))
//					return true;
//			}
//		}
//		return false;
//	}
//	
//
//	/*
//	 * Find if any binaries with checkboxes checked
//	 */
//	protected boolean haveCheckedBinary(GppTrace trace, int graphIndex)
//	{
//		ProfiledBinary pBinary;
//		for (ProfiledGeneric pGeneric: trace.getIndexedBinaries()) {
//			if (pGeneric instanceof ProfiledBinary) {
//				pBinary = (ProfiledBinary) pGeneric;
//				if (pBinary.isEnabled(graphIndex))
//					return true;
//			}
//		}
//		return false;
//	}
//
//	/*
//	 * Find if any functions with checkboxes checked
//	 */
//	protected boolean haveCheckedFunction(GppTrace trace, int graphIndex)
//	{
//		ProfiledFunction pFunction;
//		for (ProfiledGeneric pGeneric: trace.getIndexedFunctions()) {
//			if (pGeneric instanceof ProfiledFunction) {
//				pFunction = (ProfiledFunction) pGeneric;
//				if (pFunction.isEnabled(graphIndex))
//					return true;
//			}
//		}
//		return false;
//	}
	
	/* 
	 * get list of matching samples based on draw mode 
	 */
	private ArrayList<GppSample> getMatchingSamples(int drawMode, GppTrace trace, int startIndex, int endIndex, int graphIndex)
	{
		ArrayList<GppSample> samplesArray = new ArrayList<GppSample>(endIndex - startIndex > 1000 ? 1000 : endIndex - startIndex);
		Vector<ProfiledThread> traceThreads   = trace.getIndexedThreads();
		Vector<ProfiledBinary> traceBinaries  = trace.getIndexedBinaries();
		Vector<ProfiledFunction> traceFunctions = trace.getIndexedFunctions();
		GppSample[] samples = trace.getSortedGppSamples();
		GppSample sample;

		switch (drawMode)
		{
		case Defines.THREADS:
			for (int i = startIndex; i < endIndex; i++) {
				sample = samples[i];
				ProfiledThread pThread = (ProfiledThread) traceThreads.elementAt(sample.threadIndex);
				if (pThread.isEnabled(graphIndex))
					samplesArray.add(sample);
			}
			break;
		case Defines.BINARIES:
			for (int i = startIndex; i < endIndex; i++) {
				sample = samples[i];
				ProfiledBinary pBinary = (ProfiledBinary) traceBinaries.elementAt(sample.binaryIndex);
				if (pBinary.isEnabled(graphIndex))
					samplesArray.add(sample);
			}
			break;
		case Defines.FUNCTIONS:
			for (int i = startIndex; i < endIndex; i++) {
				sample = samples[i];
				ProfiledFunction pFunction = (ProfiledFunction) traceFunctions.elementAt(sample.functionIndex);
				if (pFunction.isEnabled(graphIndex))
					samplesArray.add(sample);
			}
			break;
		case Defines.THREADS_BINARIES:
		case Defines.BINARIES_THREADS:
			for (int i = startIndex; i < endIndex; i++) {
				sample = samples[i];
				ProfiledThread pThread = (ProfiledThread) traceThreads.elementAt(sample.threadIndex);
				if (pThread.isEnabled(graphIndex)) {
					ProfiledBinary pBinary = (ProfiledBinary) traceBinaries.elementAt(sample.binaryIndex);
					if (pBinary.isEnabled(graphIndex)) {
						samplesArray.add(sample);
					}
				}
			}
			break;
		case Defines.THREADS_FUNCTIONS:
		case Defines.FUNCTIONS_THREADS:
			for (int i = startIndex; i < endIndex; i++) {
				sample = samples[i];
				ProfiledThread pThread = (ProfiledThread) traceThreads.elementAt(sample.threadIndex);
				if (pThread.isEnabled(graphIndex)) {
					ProfiledFunction pFunction = (ProfiledFunction) traceFunctions.elementAt(sample.functionIndex);
					if (pFunction.isEnabled(graphIndex)) {
						samplesArray.add(sample);
					}
				}
			}
			break;
		case Defines.BINARIES_FUNCTIONS:
		case Defines.FUNCTIONS_BINARIES:
			for (int i = startIndex; i < endIndex; i++) {
				sample = samples[i];
				ProfiledBinary pBinary = (ProfiledBinary) traceBinaries.elementAt(sample.binaryIndex);
				if (pBinary.isEnabled(graphIndex)) {
					ProfiledFunction pFunction = (ProfiledFunction) traceFunctions.elementAt(sample.functionIndex);
					if (pFunction.isEnabled(graphIndex)) {
						samplesArray.add(sample);
					}
				}
			}
			break;
		case Defines.THREADS_FUNCTIONS_BINARIES:
		case Defines.THREADS_BINARIES_FUNCTIONS:
			for (int i = startIndex; i < endIndex; i++) {
				sample = samples[i];
				ProfiledThread pThread = (ProfiledThread) traceThreads.elementAt(sample.threadIndex);
				if (pThread.isEnabled(graphIndex)) {
					ProfiledBinary pBinary = (ProfiledBinary) traceBinaries.elementAt(sample.binaryIndex);
					if (pBinary.isEnabled(graphIndex)) {
						ProfiledFunction pFunction = (ProfiledFunction) traceFunctions.elementAt(sample.functionIndex);
						if (pFunction.isEnabled(graphIndex)) {
							samplesArray.add(sample);
						}
					}
				}
			}
			break;
		}
		
		samplesArray.trimToSize();
		return samplesArray;
	}

	/*
	 * return the thread, binary, or function samples selected in the interval 
	 */
	protected String getSampleString(int graphIndex, int drawMode, int startIndex, int endIndex)
	{
		boolean threads   = false;
		boolean binaries  = false;
		boolean functions = false;
		GppTraceGraph graph = (GppTraceGraph)(this.myGraph);
		GppTrace trace = (GppTrace)(graph.getTrace());

		// The current graph shows either threads, binaries, or functions
		switch (drawMode)
		{
		case Defines.THREADS:
		case Defines.BINARIES_THREADS:
		case Defines.BINARIES_FUNCTIONS_THREADS:
		case Defines.FUNCTIONS_THREADS:
		case Defines.FUNCTIONS_BINARIES_THREADS:
			threads = true;
			break;
		case Defines.BINARIES:
		case Defines.THREADS_BINARIES:
		case Defines.THREADS_FUNCTIONS_BINARIES:
		case Defines.FUNCTIONS_BINARIES:
		case Defines.FUNCTIONS_THREADS_BINARIES:
			binaries = true;
			break;
		case Defines.FUNCTIONS:
		case Defines.THREADS_FUNCTIONS:
		case Defines.THREADS_BINARIES_FUNCTIONS:
		case Defines.BINARIES_FUNCTIONS:
		case Defines.BINARIES_THREADS_FUNCTIONS:
			functions = true;
			break;
		default:
			break;
		}

		int startTime = trace.getStartSampleIndex();
		int endTime   = trace.getEndSampleIndex();
		
		// check if we have returned everything
		if (startIndex > (endTime - startTime))
			return null;

		ArrayList<GppSample> matchingSamples = getMatchingSamples(drawMode, trace, startTime, endTime, graphIndex);

		String returnString = ""; //$NON-NLS-1$
		
		if (threads) {
			Vector<ProfiledThread> traceThreads   = trace.getIndexedThreads();
			if (startIndex == 0)
				returnString = Messages.getString("GenericAddrTable.threadSampleHeading");  //$NON-NLS-1$
			
			for (int i = startIndex; i < matchingSamples.size() && i < endIndex; i++) {
				GppSample sample = matchingSamples.get(i);
				returnString +=   sample.sampleSynchTime + ",0x" + Long.toHexString(sample.programCounter) + ","	  //$NON-NLS-1$ //$NON-NLS-2$; $NON-NLS-2$;
								+ traceThreads.get(sample.threadIndex).getNameString() + "\n";  //$NON-NLS-1$
			}
		} else if (binaries) {
			if (startIndex == 0)
				returnString = Messages.getString("GenericAddrTable.binarySampleHeading");  //$NON-NLS-1$
			
			for (int i = startIndex; i < matchingSamples.size() && i < endIndex; i++) {
				GppSample sample = matchingSamples.get(i);
				ProfiledBinary binary = (ProfiledBinary) trace.getIndexedBinaries().get(sample.binaryIndex);
				returnString +=   sample.sampleSynchTime + ",0x" + Long.toHexString(sample.programCounter) + ","	  //$NON-NLS-1$ //$NON-NLS-2$; $NON-NLS-2$;
								+ binary.getNameString() + "\n";  // $NON-NLS-1$; //$NON-NLS-1$
			}
		} else if (functions) {
			if (startIndex == 0)
				returnString = Messages.getString("GenericAddrTable.functionSampleHeading");  //$NON-NLS-1$
			
			for (int i = startIndex; i < matchingSamples.size() && i < endIndex; i++) {
				GppSample sample = matchingSamples.get(i);
				ProfiledFunction function = (ProfiledFunction) trace.getIndexedFunctions().get(sample.functionIndex);
				returnString +=   sample.sampleSynchTime + ",0x" + Long.toHexString(sample.programCounter) + ",\""	  //$NON-NLS-1$ //$NON-NLS-2$; $NON-NLS-2$;
								+ function.getNameString() + "\",0x" + Long.toHexString(function.getFunctionAddress()) + "\n";  //$NON-NLS-1$ //$NON-NLS-2$; $NON-NLS-2$;
			}
		}
		
		return returnString;
	}

	private void threadItem(Menu menu, boolean chosen) {
		MenuItem menuItem = new MenuItem(menu, SWT.RADIO);
		menuItem.setText(Messages.getString("GenericAddrTable.threadOnly"));  //$NON-NLS-1$
		menuItem.setSelection(chosen);
		menuItem.addSelectionListener(new SelectionAdapter() { 
			public void widgetSelected(SelectionEvent e) {
			    action("thread-only"); //$NON-NLS-1$
			}
		});
	}

	private void threadBinaryItem(Menu menu, boolean chosen) {
		MenuItem menuItem = new MenuItem(menu, SWT.RADIO);
		menuItem.setText(Messages.getString("GenericAddrTable.threadBinary"));  //$NON-NLS-1$
		menuItem.setSelection(chosen);
		menuItem.addSelectionListener(new SelectionAdapter() { 
			public void widgetSelected(SelectionEvent e) {
			    action("thread-binary"); //$NON-NLS-1$
			}
		});
	}

	private void threadBinaryFunctionItem(Menu menu, boolean chosen) {
		MenuItem menuItem = new MenuItem(menu, SWT.RADIO);
		menuItem.setText(Messages.getString("GenericAddrTable.threadBinaryFunction"));  //$NON-NLS-1$
		menuItem.setSelection(chosen);
		menuItem.addSelectionListener(new SelectionAdapter() { 
			public void widgetSelected(SelectionEvent e) {
			    action("thread-binary-function"); //$NON-NLS-1$
			}
		});
	}

	private void threadFunctionItem(Menu menu, boolean chosen) {
		MenuItem menuItem = new MenuItem(menu, SWT.RADIO);
		menuItem.setText(Messages.getString("GenericAddrTable.threadFunction"));  //$NON-NLS-1$
		menuItem.setSelection(chosen);
		menuItem.addSelectionListener(new SelectionAdapter() { 
			public void widgetSelected(SelectionEvent e) {
			    action("thread-function"); //$NON-NLS-1$
			}
		});
	}

	private void newThreadFunctionBinary(Menu menu, boolean chosen) {
		MenuItem menuItem = new MenuItem(menu, SWT.RADIO);
		menuItem.setText(Messages.getString("GenericAddrTable.threadFunctionBinary"));  //$NON-NLS-1$
		menuItem.setSelection(chosen);
		menuItem.addSelectionListener(new SelectionAdapter() { 
			public void widgetSelected(SelectionEvent e) {
			    action("thread-function-binary"); //$NON-NLS-1$
			}
		});
	}

	private void binaryItem(Menu menu, boolean chosen) {
		MenuItem menuItem = new MenuItem(menu, SWT.RADIO);
		menuItem.setText(Messages.getString("GenericAddrTable.binaryOnly"));  //$NON-NLS-1$
		menuItem.setSelection(chosen);
		menuItem.addSelectionListener(new SelectionAdapter() { 
			public void widgetSelected(SelectionEvent e) {
			    action("binary-only"); //$NON-NLS-1$
			}
		});
	}

	private void binaryThreadItem(Menu menu, boolean chosen) {
		MenuItem menuItem = new MenuItem(menu, SWT.RADIO);
		menuItem.setText(Messages.getString("GenericAddrTable.binaryThread"));  //$NON-NLS-1$
		menuItem.setSelection(chosen);
		menuItem.addSelectionListener(new SelectionAdapter() { 
			public void widgetSelected(SelectionEvent e) {
			    action("binary-thread"); //$NON-NLS-1$
			}
		});
	}

	private void binaryThreadFunctionItem(Menu menu, boolean chosen) {
		MenuItem menuItem = new MenuItem(menu, SWT.RADIO);
		menuItem.setText(Messages.getString("GenericAddrTable.binaryThreadFunction"));  //$NON-NLS-1$
		menuItem.setSelection(chosen);
		menuItem.addSelectionListener(new SelectionAdapter() { 
			public void widgetSelected(SelectionEvent e) {
			    action("binary-thread-function"); //$NON-NLS-1$
			}
		});
	}

	private void binaryFunctionItem(Menu menu, boolean chosen) {
		MenuItem menuItem = new MenuItem(menu, SWT.RADIO);
		menuItem.setText(Messages.getString("GenericAddrTable.binaryFunction"));  //$NON-NLS-1$
		menuItem.setSelection(chosen);
		menuItem.addSelectionListener(new SelectionAdapter() { 
			public void widgetSelected(SelectionEvent e) {
			    action("binary-function"); //$NON-NLS-1$
			}
		});
	}

	private void binaryFunctionThreadItem(Menu menu, boolean chosen) {
		MenuItem menuItem = new MenuItem(menu, SWT.RADIO);
		menuItem.setText(Messages.getString("GenericAddrTable.binaryFunctionThread"));  //$NON-NLS-1$
		menuItem.setSelection(chosen);
		menuItem.addSelectionListener(new SelectionAdapter() { 
			public void widgetSelected(SelectionEvent e) {
			    action("binary-function-thread"); //$NON-NLS-1$
			}
		});
	}

	private void functionItem(Menu menu, boolean chosen) {
		MenuItem menuItem = new MenuItem(menu, SWT.RADIO);
		menuItem.setText(Messages.getString("GenericAddrTable.functionOnly"));  //$NON-NLS-1$
		menuItem.setSelection(chosen);
		menuItem.addSelectionListener(new SelectionAdapter() { 
			public void widgetSelected(SelectionEvent e) {
			    action("function-only"); //$NON-NLS-1$
			}
		});
	}

	private void functionThreadItem(Menu menu, boolean chosen) {
		MenuItem menuItem = new MenuItem(menu, SWT.RADIO);
		menuItem.setText(Messages.getString("GenericAddrTable.functionThread"));  //$NON-NLS-1$
		menuItem.setSelection(chosen);
		menuItem.addSelectionListener(new SelectionAdapter() { 
			public void widgetSelected(SelectionEvent e) {
			    action("function-thread"); //$NON-NLS-1$
			}
		});
	}

	private void functionThreadBinaryItem(Menu menu, boolean chosen) {
		MenuItem menuItem = new MenuItem(menu, SWT.RADIO);
		menuItem.setText(Messages.getString("GenericAddrTable.functionThreadBinary"));  //$NON-NLS-1$
		menuItem.setSelection(chosen);
		menuItem.addSelectionListener(new SelectionAdapter() { 
			public void widgetSelected(SelectionEvent e) {
			    action("function-thread-binary"); //$NON-NLS-1$
			}
		});
	}

	private void functionBinaryItem(Menu menu, boolean chosen) {
		MenuItem menuItem = new MenuItem(menu, SWT.RADIO);
		menuItem.setText(Messages.getString("GenericAddrTable.functionBinary"));  //$NON-NLS-1$
		menuItem.setSelection(chosen);
		menuItem.addSelectionListener(new SelectionAdapter() { 
			public void widgetSelected(SelectionEvent e) {
			    action("function-binary"); //$NON-NLS-1$
			}
		});
	}

	private void functionBinaryThreadItem(Menu menu, boolean chosen) {
		MenuItem menuItem = new MenuItem(menu, SWT.RADIO);
		menuItem.setText(Messages.getString("GenericAddrTable.functionBinaryThread"));  //$NON-NLS-1$
		menuItem.setSelection(chosen);
		menuItem.addSelectionListener(new SelectionAdapter() { 
			public void widgetSelected(SelectionEvent e) {
			    action("function-binary-thread"); //$NON-NLS-1$
			}
		});
	}
	
	protected String copyDrilldown(int tableCount, Table[] tables, String separator)
	{	
		// make sure it's a valid drilldown
		if (   (tableCount < 2) || (tableCount > 3)
			|| (tables[0] == null) || (tables[1] == null)
			|| (tableCount == 2 && tables[2] != null)
			|| (tableCount == 3 && tables[2] == null))
			return ""; //$NON-NLS-1$
		
		String copyString = ""; //$NON-NLS-1$

		// create the multiple table heading line (e.g., "Threads     Binaries")
		// space them out based on how many columns are in their table
		for (int i = 0; i < tableCount; i++) {
			if (tables[i].getData() instanceof String) {
				copyString += (String) (tables[i].getData());
			}
			for (int j = 0; j < tables[i].getColumnCount(); j++) {
				copyString += separator;
			}
		}
		copyString += "\n"; //$NON-NLS-1$
		
		// create the multiple table column headings
		for (int i = 0; i < tableCount; i++) {
			if (i != tableCount - 1) {
				copyString += copyHeading(tables[i], CHECKBOX_NO_TEXT, separator, separator); //$NON-NLS-1$
			} else {
				copyString += copyHeading(tables[i], CHECKBOX_NO_TEXT, separator, "\n"); //$NON-NLS-1$
			}
		}
		
		// determine the row, column count, and column ordering in each table
		int rowCount0      = tables[0].getItemCount();
		int columnCount0   = tables[0].getColumnCount();
		int[] columnOrder0 = tables[0].getColumnOrder();
		boolean[] isHex0   = (boolean[]) tables[0].getData("isHex"); //$NON-NLS-1$
		String emptyRow0 = ""; //$NON-NLS-1$

		int rowCount1      = tables[1].getItemCount();
		int columnCount1   = tables[1].getColumnCount();
		int[] columnOrder1 = tables[1].getColumnOrder();
		boolean[] isHex1   = (boolean[]) tables[1].getData("isHex"); //$NON-NLS-1$
		String emptyRow1 = ""; //$NON-NLS-1$

		int rowCount2      = tableCount > 2 ? tables[2].getItemCount() : 0;
		int columnCount2   = tableCount > 2 ? tables[2].getColumnCount() : 0;
		int[] columnOrder2 = tableCount > 2 ? tables[2].getColumnOrder() : null;
		boolean[] isHex2   = tableCount > 2 ? ((boolean[]) tables[2].getData("isHex")) : null; //$NON-NLS-1$
		String emptyRow2 = ""; //$NON-NLS-1$

		// determine the number of multiple table rows (max of any table's rows) 
		int rowCount = rowCount0 >= rowCount1 ? rowCount0 : rowCount1;
		rowCount = rowCount > rowCount2 ? rowCount : rowCount2;
		
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

		if ((tableCount > 2) && (rowCount2 < rowCount)) {
			for (int j = 0; j < columnCount2; j++) {
				emptyRow2 += separator;
			}
		}

		// generate the rows
		for (int i = 0; i < rowCount; i++) {
			if (i < rowCount0) {
				copyString += copyRow(isHex0, tables[0].getItem(i), CHECKBOX_NO_TEXT, columnCount0, columnOrder0, separator);
			} else {
				copyString += emptyRow0;
			}
			
			if (i < rowCount1) {
				copyString += separator + copyRow(isHex1, tables[1].getItem(i), CHECKBOX_NO_TEXT, columnCount1, columnOrder1, separator);
			} else {
				// NOTE: if this is the last table, or the 3rd table has nothing but empty
				// rows left, we may not need to fill in these fields
				copyString += emptyRow1;
			}
			
			if (tableCount > 2) {
				if (i < rowCount2) {
					copyString += separator + copyRow(isHex2, tables[2].getItem(i), CHECKBOX_NO_TEXT, columnCount2, columnOrder2, separator);
				} else {
					// NOTE: we may not need to fill in the empty fields of the last table
					copyString += emptyRow2;
				}
			}
			
			copyString += "\n"; //$NON-NLS-1$
		}
		
		return copyString;
	}
	
	// class to pass drilldown tables to the save wizard
	class SaveDrillDownString implements ISaveTable {
		private int tableCount;
		private Table[] tables;
		private String separator;
		
		public SaveDrillDownString(int tableCount, Table[] tables, String separator) {
			this.tableCount = tableCount;
			this.tables     = tables;
			this.separator  = separator;
		}

		public String getData() {
			return copyDrilldown(tableCount, tables, separator);
		}
	}
	
	public Table[] getDrillDownTables()
	{
		// copy all tables in a drilldown to the clipboard or save to a file
		int drawMode = this.myGraph.getDrawMode();

		int tableCount = 0;
		Table[] tables = new Table[3];
		
		// determine which tables are in the drilldown
		switch (drawMode)
		{
		case Defines.THREADS_BINARIES:
			tables[tableCount++] = this.myGraph.getThreadTable().getTable();
			tables[tableCount++] = this.myGraph.getBinaryTable().getTable();
			break;
		case Defines.THREADS_BINARIES_FUNCTIONS:
			tables[tableCount++] = this.myGraph.getThreadTable().getTable();
			tables[tableCount++] = this.myGraph.getBinaryTable().getTable();
			tables[tableCount++] = this.myGraph.getFunctionTable().getTable();
			break;
		case Defines.THREADS_FUNCTIONS:
			tables[tableCount++] = this.myGraph.getThreadTable().getTable();
			tables[tableCount++] = this.myGraph.getFunctionTable().getTable();
			break;
		case Defines.THREADS_FUNCTIONS_BINARIES:
			tables[tableCount++] = this.myGraph.getThreadTable().getTable();
			tables[tableCount++] = this.myGraph.getFunctionTable().getTable();
			tables[tableCount++] = this.myGraph.getBinaryTable().getTable();
			break;
		case Defines.BINARIES_THREADS:
			tables[tableCount++] = this.myGraph.getBinaryTable().getTable();
			tables[tableCount++] = this.myGraph.getThreadTable().getTable();
			break;
		case Defines.BINARIES_THREADS_FUNCTIONS:
			tables[tableCount++] = this.myGraph.getBinaryTable().getTable();
			tables[tableCount++] = this.myGraph.getThreadTable().getTable();
			tables[tableCount++] = this.myGraph.getFunctionTable().getTable();
			break;
		case Defines.BINARIES_FUNCTIONS:
			tables[tableCount++] = this.myGraph.getBinaryTable().getTable();
			tables[tableCount++] = this.myGraph.getFunctionTable().getTable();
			break;
		case Defines.BINARIES_FUNCTIONS_THREADS:
			tables[tableCount++] = this.myGraph.getBinaryTable().getTable();
			tables[tableCount++] = this.myGraph.getFunctionTable().getTable();
			tables[tableCount++] = this.myGraph.getThreadTable().getTable();
			break;
		case Defines.FUNCTIONS_THREADS:
			tables[tableCount++] = this.myGraph.getFunctionTable().getTable();
			tables[tableCount++] = this.myGraph.getThreadTable().getTable();
			break;
		case Defines.FUNCTIONS_THREADS_BINARIES:
			tables[tableCount++] = this.myGraph.getFunctionTable().getTable();
			tables[tableCount++] = this.myGraph.getThreadTable().getTable();
			tables[tableCount++] = this.myGraph.getBinaryTable().getTable();
			break;
		case Defines.FUNCTIONS_BINARIES:
			tables[tableCount++] = this.myGraph.getFunctionTable().getTable();
			tables[tableCount++] = this.myGraph.getBinaryTable().getTable();
			break;
		case Defines.FUNCTIONS_BINARIES_THREADS:
			tables[tableCount++] = this.myGraph.getFunctionTable().getTable();
			tables[tableCount++] = this.myGraph.getBinaryTable().getTable();
			tables[tableCount++] = this.myGraph.getThreadTable().getTable();
			break;
		default:
		}
		
		return tables;
	}
    
    protected void actionCopyOrSaveDrilldown(boolean doCopy, String separator)
    {
		Table[] tables = getDrillDownTables();
		
		int tableCount = 0;
		while ((tableCount < tables.length) && (tables[tableCount] != null))
			tableCount++;
		
		
		if (doCopy) {
			// change the clipboard contents
	        Clipboard cb = Toolkit.getDefaultToolkit().getSystemClipboard();
			String copyString = copyDrilldown(tableCount, tables, separator);
			StringSelection contents = new StringSelection(copyString);
	        cb.setContents(contents, contents);
		} else {
			// save to a file
			SaveDrillDownString getString = new SaveDrillDownString(tableCount, tables, separator);
			WizardDialog dialog;
			Wizard w = new SaveTableWizard(getString);
			dialog = new WizardDialog(PlatformUI.getWorkbench().getDisplay().getActiveShell(), w);
	    	dialog.open();
		}
    }
	
	protected void createDefaultActions()
	{
		selectAllAction = new Action("SelectAll") { //$NON-NLS-1$
			public void run() {
				action("selectAll");  //$NON-NLS-1$
			}
		};
		selectAllAction.setEnabled(true);

		copyAction = new Action("Copy") { //$NON-NLS-1$
			public void run() {
				action("copy");  //$NON-NLS-1$
			}
		};
		copyAction.setEnabled(false);

		copyTableAction = new Action("CopyTable") { //$NON-NLS-1$
			public void run() {
				action("copyTable");  //$NON-NLS-1$
			}
		};
		copyTableAction.setEnabled(true);
		copyTableAction.setId("PICopyTable");  //$NON-NLS-1$
		copyTableAction.setText(Messages.getString("GenericAddrTable.copyTable"));  //$NON-NLS-1$

		copyDrilldownAction = new Action("CopyDrilldown") {  //$NON-NLS-1$
			public void run() {
				action("copyDrilldown");  //$NON-NLS-1$
			}
		};
		copyDrilldownAction.setEnabled(false);
		copyDrilldownAction.setId("PICopyDrilldown");  //$NON-NLS-1$
		copyDrilldownAction.setText(Messages.getString("GenericAddrTable.copyDrilldownTables"));  //$NON-NLS-1$

		saveTableAction = new Action("SaveTable") { //$NON-NLS-1$
			public void run() {
				action("saveTable");  //$NON-NLS-1$
			}
		};
		saveTableAction.setEnabled(true);
		saveTableAction.setId("PISaveTable");  //$NON-NLS-1$
		saveTableAction.setText(Messages.getString("GenericAddrTable.saveTable")); //$NON-NLS-1$

		saveDrilldownAction = new Action("SaveDrilldown") {  //$NON-NLS-1$
			public void run() {
				action("saveDrilldown");  //$NON-NLS-1$
			}
		};
		saveDrilldownAction.setEnabled(false);
		saveDrilldownAction.setId("PISaveDrilldown");  //$NON-NLS-1$
		saveDrilldownAction.setText(Messages.getString("GenericAddrTable.saveDrilldownTables"));  //$NON-NLS-1$

//		saveSamplesAction = new Action("SaveSamples") { //$NON-NLS-1$
//			public void run() {
//				action("saveSamples");  //$NON-NLS-1$
//			}
//		};
//		saveSamplesAction.setEnabled(true);
//		saveSamplesAction.setId("PISaveAddressSamples");  //$NON-NLS-1$
//		saveSamplesAction.setText(Messages.getString("GenericAddrTable.23") + sampleType + Messages.getString("GenericAddrTable.24")); //$NON-NLS-1$ //$NON-NLS-2$
	}
	
	public void setIsDrilldown(boolean isDrilldown)
	{
		this.isDrilldown = isDrilldown;
		copyDrilldownAction.setEnabled(isDrilldown);
		saveDrilldownAction.setEnabled(isDrilldown);

		// may need to clean up stale Edit & File menu entry for PICopyDrilldown and PISaveDrilldown 
		if (!isDrilldown) {
			IMenuManager editMenuManager = PIPageEditor.getActionBars().getMenuManager().findMenuUsingPath(IIDEActionConstants.M_EDIT);
			
	        if (editMenuManager instanceof SubMenuManager)
	        {
	        	IContributionManager editManager = ((SubMenuManager)editMenuManager).getParent();
	        	ActionContributionItem item;
	
				editMenuManager.remove("PICopyDrilldown");  //$NON-NLS-1$
	        	item = new ActionContributionItem(copyDrilldownAction);
	        	item.setVisible(true);
	        	editManager.prependToGroup(IIDEActionConstants.CUT_EXT, item);
	
				editMenuManager.remove("PICopyTable");  //$NON-NLS-1$
	        	copyTableAction.setEnabled(table.getItemCount() > 0);
	        	item = new ActionContributionItem(copyTableAction);
	        	item.setVisible(true);
	        	editManager.prependToGroup(IIDEActionConstants.CUT_EXT, item);
	        }

			IMenuManager fileMenuManager = PIPageEditor.getActionBars().getMenuManager().findMenuUsingPath(IIDEActionConstants.M_FILE);

	        if (fileMenuManager instanceof SubMenuManager)
	        {
	        	IContributionManager fileManager = ((SubMenuManager)fileMenuManager).getParent();
	        	ActionContributionItem item;
	
				fileMenuManager.remove("PISaveTable");  //$NON-NLS-1$
	        	saveTableAction.setEnabled(table.getItemCount() > 0);
	        	item = new ActionContributionItem(saveTableAction);
	        	item.setVisible(true);
	        	fileManager.insertAfter("saveAll", item); //$NON-NLS-1$
	        	
				fileMenuManager.remove("PISaveDrilldown");  //$NON-NLS-1$
	        	item = new ActionContributionItem(saveDrilldownAction);
	        	item.setVisible(true);
	        	fileManager.insertAfter("PISaveTable", item); //$NON-NLS-1$
	        	fileManager.update(true);
	        }
		}
	}

	protected MenuItem getCopyDrilldownItem(Menu menu, boolean enabled) {
	    MenuItem copyDrilldownItem = new MenuItem(menu, SWT.PUSH);

	    copyDrilldownItem.setText(Messages.getString("GenericAddrTable.copyDrilldownTables"));  //$NON-NLS-1$
		copyDrilldownItem.setEnabled(enabled);
		
		if (enabled) {
			copyDrilldownItem.addSelectionListener(new SelectionAdapter() { 
				public void widgetSelected(SelectionEvent e) {
					action("copyDrilldown"); //$NON-NLS-1$
				}
			});
		}

		return copyDrilldownItem;
	}

	protected MenuItem getSaveDrilldownItem(Menu menu, boolean enabled) {
	    MenuItem saveDrilldownItem = new MenuItem(menu, SWT.PUSH);

	    saveDrilldownItem.setText(Messages.getString("GenericAddrTable.saveDrilldownTables"));  //$NON-NLS-1$
		saveDrilldownItem.setEnabled(enabled);
		
		if (enabled) {
			saveDrilldownItem.addSelectionListener(new SelectionAdapter() { 
				public void widgetSelected(SelectionEvent e) {
					action("saveDrilldown"); //$NON-NLS-1$
				}
			});
		}

		return saveDrilldownItem;
	}

	protected class AddrTableFocusListener implements FocusListener
	{
		IAction oldSelectAllAction = null;
		IAction oldCopyAction = null;

		public void focusGained(org.eclipse.swt.events.FocusEvent arg0) {
			IActionBars bars = PIPageEditor.getActionBars();
			
			// modify what is executed when Select All and Copy are called from the Edit menu
			oldSelectAllAction = bars.getGlobalActionHandler(ActionFactory.SELECT_ALL.getId());
			oldCopyAction = bars.getGlobalActionHandler(ActionFactory.COPY.getId());

			bars.setGlobalActionHandler(ActionFactory.COPY.getId(), copyAction);
			bars.setGlobalActionHandler(ActionFactory.SELECT_ALL.getId(), selectAllAction);

			copyAction.setEnabled(table.getSelectionCount() > 0);
			selectAllAction.setEnabled(table.getItemCount() > 0);
			bars.updateActionBars();
			
			// add to the Edit menu
	        IMenuManager editMenuManager = bars.getMenuManager().findMenuUsingPath(IIDEActionConstants.M_EDIT);

	        if (editMenuManager instanceof SubMenuManager)
	        {
	        	IContributionManager editManager = ((SubMenuManager)editMenuManager).getParent();
	        	ActionContributionItem item;

				editMenuManager.remove("PICopyDrilldown");  //$NON-NLS-1$
	        	item = new ActionContributionItem(copyDrilldownAction);
	        	item.setVisible(true);
	        	editManager.prependToGroup(IIDEActionConstants.CUT_EXT, item);

				editMenuManager.remove("PICopyTable");  //$NON-NLS-1$
	        	copyTableAction.setEnabled(table.getItemCount() > 0);
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

				fileMenuManager.remove("PISaveTable");  //$NON-NLS-1$
	        	saveTableAction.setEnabled(table.getItemCount() > 0);
	        	item = new ActionContributionItem(saveTableAction);
	        	item.setVisible(true);
	        	fileManager.insertAfter("saveAll", item); //$NON-NLS-1$

				fileMenuManager.remove("PISaveDrilldown");  //$NON-NLS-1$
	        	item = new ActionContributionItem(saveDrilldownAction);
	        	item.setVisible(true);
	        	fileManager.insertAfter("PISaveTable", item); //$NON-NLS-1$
	        }
		}

		public void focusLost(org.eclipse.swt.events.FocusEvent arg0) {
			IActionBars bars = PIPageEditor.getActionBars();
			bars.setGlobalActionHandler(ActionFactory.COPY.getId(), oldCopyAction);
			bars.setGlobalActionHandler(ActionFactory.SELECT_ALL.getId(), oldSelectAllAction);
			bars.updateActionBars();

			SubMenuManager editMenuManager = (SubMenuManager) PIPageEditor.getMenuManager().find(IIDEActionConstants.M_EDIT);
			editMenuManager.remove("PICopyTable");  //$NON-NLS-1$
			editMenuManager.remove("PICopyDrilldown");  //$NON-NLS-1$

			SubMenuManager fileMenuManager = (SubMenuManager) PIPageEditor.getMenuManager().find(IIDEActionConstants.M_FILE);
			fileMenuManager.remove("PISaveTable");  //$NON-NLS-1$
			fileMenuManager.remove("PISaveDrilldown");  //$NON-NLS-1$
		}
	}

}
