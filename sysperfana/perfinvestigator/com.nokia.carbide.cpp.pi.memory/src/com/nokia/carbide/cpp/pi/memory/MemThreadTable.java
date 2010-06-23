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

package com.nokia.carbide.cpp.pi.memory;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.FocusEvent;
import java.awt.event.MouseEvent;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.Vector;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IContributionManager;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.SubMenuManager;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.ide.IIDEActionConstants;

import com.nokia.carbide.cpp.internal.pi.interfaces.ISaveSamples;
import com.nokia.carbide.cpp.internal.pi.memory.actions.MemoryStatisticsDialog;
import com.nokia.carbide.cpp.internal.pi.visual.GenericTable;
import com.nokia.carbide.cpp.internal.pi.visual.PIEvent;
import com.nokia.carbide.cpp.pi.editors.PIPageEditor;


public class MemThreadTable extends GenericTable
{
	private static final int COLUMN_ID_MEMORY_CHUNKS  = 14;
	private static final int COLUMN_ID_MEMORY_STACK   = 15;
	private static final int COLUMN_ID_MEMORY_TOTAL   = 16;
	private static final int COLUMN_ID_MEMORY_NAME    = 17;

	// sort direction
	private boolean sortAscending = true;
	
	// trace associated with this display
//	private MemTrace myTrace;
	private MemTraceGraph myGraph;
	private Composite parent;

	// override
    protected Vector<MemThread> tableItemData;

    // menu items
	private Action selectAllAction;
	private Action copyTableAction;
	private Action copyAction;
	private Action saveTableAction;
	
	// class to pass sample data to the save wizard
    public class SaveSampleString implements ISaveSamples {
    	boolean done = false;
    	
    	public SaveSampleString() {
		}

    	public String getData() {
    		if (done)
    			return null;
    		
			String returnString = getSampleString();
			done = true;
			return returnString;
		}

		public String getData(int size) {
			return getData();
		}

		public int getIndex() {
			return done ? 1 : 0;
		}

		public void clear() {
			done = false;
		}
    }

	/*
	 * return the memory samples selected in the interval 
	 */
	protected String getSampleString()
	{
		int startTime = (int) (PIPageEditor.currentPageEditor().getStartTime() * 1000.0 + 0.0005);
		int endTime   = (int) (PIPageEditor.currentPageEditor().getEndTime()   * 1000.0 + 0.0005);
		
		MemTrace trace = this.myGraph.getMemTrace();
		if(trace.getVersion() >= 202){
			String returnString = Messages.getString("MemThreadTable.saveSamplesHeading"); //$NON-NLS-1$
	
			TreeMap<Long, ArrayList<MemSample>> sorted = new TreeMap<Long, ArrayList<MemSample>>();		
			Enumeration<TreeMap<Long, MemSample>> enume = trace.getDrawDataByMemThread().elements();
			while(enume.hasMoreElements()){
				TreeMap<Long, MemSample> map = enume.nextElement();	
				Iterator<MemSample> iterator = map.values().iterator();
				while(iterator.hasNext()) {
					MemSample memSample = iterator.next();
					ArrayList<MemSample> memList = sorted.get(memSample.sampleSynchTime);
					if(memList == null){
						memList = new ArrayList<MemSample>();
					}
					memList.add(memSample);		
					sorted.put(memSample.sampleSynchTime, memList);
				}			
			}
			
			SortedMap<Long, ArrayList<MemSample>> selectionAreaMap = sorted.subMap((long)startTime, (long)endTime);
			Iterator<ArrayList<MemSample>> iterator = selectionAreaMap.values().iterator();
						
			while (iterator.hasNext()) {
				ArrayList<MemSample> memSamples = iterator.next();
				for(MemSample memSample : memSamples){
					if (memSample.thread.isEnabled(myGraph.getGraphIndex()) && !memSample.thread.fullName.equals("TOTAL_MEMORY::TOTAL_MEMORY_-1162089814")) {
						
						returnString += memSample.sampleSynchTime
								+ Messages.getString("MemThreadTable.comma") //$NON-NLS-1$
								+ memSample.thread.fullName							
								+ Messages.getString("MemThreadTable.comma") //$NON-NLS-1$
								+ ((memSample.heapSize + 512)/1024)
								+ Messages.getString("MemThreadTable.comma") //$NON-NLS-1$
								+ ((memSample.stackSize + 512)/1024) + "\n"; //$NON-NLS-1$
					}
				}			
			}
			
			return returnString;
		}else{
			ArrayList<MemSampleByTime> drawDataByTime = trace.getDrawDataByTime();
			
			if (drawDataByTime == null || drawDataByTime.size() == 0)
				return "";	// should have been checked so this never happens //$NON-NLS-1$
			
			String returnString = Messages.getString("MemThreadTable.saveSamplesHeading"); //$NON-NLS-1$

			if (drawDataByTime.get(0).getTime() > startTime) {
				int graphIndex = this.myGraph.getGraphIndex();
				TableItem[] items = this.table.getItems();
				
				for (int i = 0; i < items.length; i++) {
					Object data = items[i].getData();
					
					if (!(data instanceof MemThread))
						continue;
					
					MemThread pmt = (MemThread) data;
					
					if (!pmt.enabled[graphIndex])
						continue;
					
					returnString +=   Messages.getString("MemThreadTable.notRecorded1") + items[i].getText() + Messages.getString("MemThreadTable.notRecorded2"); //$NON-NLS-1$ //$NON-NLS-2$
				}
			}
			
			// find least sampling time greater than or equal to the start time
			int i = 0;
			long sampleTime = 0;
			for ( ; i < drawDataByTime.size(); i++)	{
				sampleTime = drawDataByTime.get(i).getTime(); 
				if (sampleTime > startTime)
					break;
			}
			
			if (i != 0)
				i--;

			int graphIndex = myGraph.getGraphIndex();
			for ( ; i < drawDataByTime.size() && drawDataByTime.get(i).getTime() <= endTime; i++) {
				ArrayList<MemSample> samples = drawDataByTime.get(i).getSamples();
				for (MemSample sample : samples) {
					if (!sample.thread.enabled[graphIndex])
						continue;
						
					returnString +=   sample.sampleSynchTime + Messages.getString("MemThreadTable.comma") + sample.thread.fullName //$NON-NLS-1$
									+ Messages.getString("MemThreadTable.comma") + (sample.heapSize / 1024) + Messages.getString("MemThreadTable.comma") + (sample.stackSize / 1024) + "\n"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				}
			}

			return returnString;
		}
	
	}

	protected MenuItem getSaveSamplesItem(Menu menu, boolean enabled) {
	    MenuItem saveSamplesItem = new MenuItem(menu, SWT.PUSH);

		saveSamplesItem.setText(Messages.getString("MemThreadTable.saveCheckedSamples")); //$NON-NLS-1$
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

	public MemThreadTable(MemTraceGraph myGraph, Composite parent)
	{
		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout gl = new GridLayout();
		gl.marginHeight = 0;
		gl.marginWidth = 0;
		gl.marginLeft = 0;
		gl.marginRight = 0;	
		composite.setLayout(gl);
		this.myGraph = myGraph;
		this.parent  = composite;

		Label label = new Label(composite, SWT.CENTER);
		label
				.setBackground(composite.getDisplay().getSystemColor(
						SWT.COLOR_WHITE));
		label.setFont(PIPageEditor.helvetica_8);
		label.setText(Messages.getString("MemThreadTable.title")); //$NON-NLS-1$
		
		label.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		

		this.tableViewer = CheckboxTableViewer.newCheckList(composite,
  				SWT.BORDER | SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION);
		this.table = this.tableViewer.getTable();
		this.table.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		// add the check state handler, label provider and content provider
		this.tableViewer.addCheckStateListener(new CheckHandler());
		this.tableViewer.setLabelProvider(new SharedLabelProvider(this.table));
		this.tableViewer.setContentProvider(new MemoryTableContentProvider());
		this.tableViewer.setSorter(new SharedSorter());

		// give the table a heading for possible use in copying and exported
		this.table.setData(Messages.getString("MemThreadTable.memory")); //$NON-NLS-1$
		
		// create the columns
		TableColumn column;

		// data associated with the TableViewer will note which columns contain hex values
		// Keep this in the order in which columns have been created
		boolean[] isHex = {false, false, false, false};
		this.table.setData("isHex", isHex); //$NON-NLS-1$

		// select/deselect column
		column = new TableColumn(this.table, SWT.CENTER);
		column.setText(COLUMN_HEAD_MEMORY_NAME);
		column.setWidth(COLUMN_WIDTH_MEMORY_NAME + 15); // extra space for the checkbox
		column.setData(Integer.valueOf(COLUMN_ID_MEMORY_NAME));
		column.setMoveable(true);
		column.setResizable(true);
		column.addSelectionListener(new ColumnSelectionHandler());

		column = new TableColumn(tableViewer.getTable(), SWT.RIGHT);
		column.setText(COLUMN_HEAD_MEMORY_CHUNKS);
		column.setWidth(COLUMN_WIDTH_MEMORY_CHUNKS);
		column.setData(Integer.valueOf(COLUMN_ID_MEMORY_CHUNKS));
		column.setMoveable(true);
		column.setResizable(true);
		column.addSelectionListener(new ColumnSelectionHandler());

		column = new TableColumn(tableViewer.getTable(), SWT.RIGHT);
		column.setText(COLUMN_HEAD_MEMORY_STACK);
		column.setWidth(COLUMN_WIDTH_MEMORY_STACK);
		column.setData(Integer.valueOf(COLUMN_ID_MEMORY_STACK));
		column.setMoveable(true);
		column.setResizable(true);
		column.addSelectionListener(new ColumnSelectionHandler());

		column = new TableColumn(tableViewer.getTable(), SWT.RIGHT);
		column.setText(COLUMN_HEAD_MEMORY_TOTAL);
		column.setWidth(COLUMN_WIDTH_MEMORY_TOTAL);
		column.setData(Integer.valueOf(COLUMN_ID_MEMORY_TOTAL));
		column.setMoveable(true);
		column.setResizable(true);
		column.addSelectionListener(new ColumnSelectionHandler());
		
		// initially, all rows are checked
		this.tableViewer.setAllChecked(true);

		this.table.addMouseListener(new TableMouseListener());
		this.table.setHeaderVisible(true);
		this.table.setLinesVisible(true);
		this.table.setRedraw(true);
		
		updateItemData(true);
		((SharedSorter) tableViewer.getSorter()).doSort(COLUMN_ID_MEMORY_NAME);

		// initially, all rows are checked
		this.tableViewer.setAllChecked(true);
		
		createDefaultActions();

		// listen for key sequences such as Ctrl-A and Ctrl-C
		table.addKeyListener(new TableKeyListener());
		
		table.addFocusListener(new FocusListener() {
			IAction oldSelectAllAction = null;
			IAction oldCopyAction = null;

			public void focusGained(org.eclipse.swt.events.FocusEvent arg0) {
				IActionBars bars = PIPageEditor.getActionBars();
				
				oldSelectAllAction = PIPageEditor.getActionBars().getGlobalActionHandler(ActionFactory.SELECT_ALL.getId());
				oldCopyAction = PIPageEditor.getActionBars().getGlobalActionHandler(ActionFactory.COPY.getId());

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

					editMenuManager.remove("PICopyTable"); //$NON-NLS-1$
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

		        	fileMenuManager.remove("PISaveTable"); //$NON-NLS-1$
		        	saveTableAction.setEnabled(table.getItemCount() > 0);
		        	item = new ActionContributionItem(saveTableAction);
		        	item.setVisible(true);
		        	fileManager.insertAfter("saveAll", item); //$NON-NLS-1$
		        }
			}

			public void focusLost(org.eclipse.swt.events.FocusEvent arg0) {
				IActionBars bars = PIPageEditor.getActionBars();
				bars.setGlobalActionHandler(ActionFactory.COPY.getId(), oldCopyAction);
				bars.setGlobalActionHandler(ActionFactory.SELECT_ALL.getId(), oldSelectAllAction);
				bars.updateActionBars();

				SubMenuManager editMenuManager = (SubMenuManager) PIPageEditor.getMenuManager().find(IIDEActionConstants.M_EDIT);
				editMenuManager.remove("PICopyTable"); //$NON-NLS-1$
				editMenuManager.update();

				SubMenuManager fileMenuManager = (SubMenuManager) PIPageEditor.getMenuManager().find(IIDEActionConstants.M_FILE);
				fileMenuManager.remove("PISaveTable"); //$NON-NLS-1$
				fileMenuManager.update();
			}
		});

		tableViewer.refresh();
		table.redraw();
	}

	private class TableMouseListener implements MouseListener
	{
		public void mouseDoubleClick(org.eclipse.swt.events.MouseEvent e) {
			if (e.button == MouseEvent.BUTTON1)
			{
				TableItem[] selectedItems = table.getSelection();
				if (selectedItems.length == 0)
					return;

				if (selectedItems[0].getData() instanceof MemThread)
				{
					MemThread pMemThread = (MemThread)(selectedItems[0].getData());
				    if (pMemThread.isEnabled(myGraph.getGraphIndex()))
				        action("remove"); //$NON-NLS-1$
				    else
				        action("add"); //$NON-NLS-1$
				}
			}
			selectAllAction.setEnabled(table.getItemCount() > 0);
			copyAction.setEnabled(table.getSelectionCount() > 0);
			copyTableAction.setEnabled(table.getItemCount() > 0);
		}

		public void mouseDown(org.eclipse.swt.events.MouseEvent e) {
		}

		public void mouseUp(org.eclipse.swt.events.MouseEvent e) {

			selectAllAction.setEnabled(table.getItemCount() > 0);
			copyAction.setEnabled(table.getSelectionCount() > 0);
			copyTableAction.setEnabled(table.getItemCount() > 0);

			if (e.button == MouseEvent.BUTTON3) {
				// get rid of last Menu created so we don't have double menu
				// on click
				if (contextMenu != null) {
					contextMenu.dispose();
				}

				contextMenu = new Menu(table.getShell(), SWT.POP_UP);
				getCheckRows(contextMenu, table.getSelectionCount() > 0);

				// select all, copy, and copy all
				new MenuItem(contextMenu, SWT.SEPARATOR);
				getSelectAllItem(contextMenu, table.getItemCount() > 0);
				getCopyItem(contextMenu, table.getSelectionCount() > 0);
				getCopyTableItem(contextMenu, table.getItemCount() > 0);
				selectAllAction.setEnabled(table.getItemCount() > 0);
				copyAction.setEnabled(table.getSelectionCount() > 0);
				copyTableAction.setEnabled(table.getItemCount() > 0);
				
				// save all
				new MenuItem(contextMenu, SWT.SEPARATOR);
				getSaveTableItem(contextMenu, table.getItemCount() > 0);
				saveTableAction.setEnabled(table.getItemCount() > 0);

				// save samples
				int startTime = (int) (PIPageEditor.currentPageEditor().getStartTime() * 1000.0f);
				int endTime   = (int) (PIPageEditor.currentPageEditor().getEndTime()   * 1000.0f);

				getSaveSamplesItem(contextMenu,
					myGraph.haveEnabled() && (startTime != -1) && (endTime != -1) && (startTime != endTime));

				contextMenu.setLocation(parent.toDisplay(e.x + table.getLocation().x, e.y + table.getLocation().y));
			    contextMenu.setVisible(true);

				new MenuItem(contextMenu, SWT.SEPARATOR);

				MenuItem memoryStatsItem = new MenuItem(contextMenu, SWT.PUSH);
				memoryStatsItem.setText(Messages.getString("MemoryPlugin.memoryStats")); //$NON-NLS-1$
				memoryStatsItem.addSelectionListener(new SelectionAdapter() {
					public void widgetSelected(SelectionEvent e) {
						new MemoryStatisticsDialog(Display.getCurrent());
					}
				});

				table.setMenu(contextMenu);
			}
		}
	}

	private static class MemoryTableContentProvider implements IStructuredContentProvider {

		public MemoryTableContentProvider() {
			super();
		}

		public Object[] getElements(Object inputElement) {
			return ((Vector<MemThread>) inputElement).toArray();
		}

		public void dispose() {
		}

		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}
	}

	private class CheckHandler implements ICheckStateListener
	{
		public void checkStateChanged(CheckStateChangedEvent event) {

       		if (!(event.getElement() instanceof MemThread))
       			return;
       		
       		// set the stored value to the checkbox value
       		MemThread pMemThread = (MemThread)event.getElement();
       		pMemThread.setEnabled(myGraph.getGraphIndex(), event.getChecked());

       		selectionChangeNotify();

       		table.deselectAll();
		}
	}

	void selectionChangeNotify() {
		this.tableViewer.refresh();
		this.table.redraw();
		PIEvent be = new PIEvent(null, PIEvent.CHANGED_MEMORY_TABLE);
		myGraph.piEventReceived(be);
    }

	private class SharedLabelProvider extends LabelProvider implements ITableLabelProvider {
		
        private DecimalFormat decimalFormat = new DecimalFormat(Messages.getString("MemThreadTable.kbFormat")); //$NON-NLS-1$

        private Table table;

		public SharedLabelProvider(Table table) {
			super();
			this.table = table;
		}

		public String getColumnText(Object element, int columnIndex) {
	        int columnId = ((Integer) table.getColumn(columnIndex).getData()).intValue();

			if (!(element instanceof MemThread))
				return ""; //$NON-NLS-1$

			MemThread profiledItem = (MemThread) element;
			
			switch (columnId)
			{
				case COLUMN_ID_MEMORY_NAME:
				{
					return profiledItem.fullName;
				}
				case COLUMN_ID_MEMORY_CHUNKS:
			    {
					double startTime = PIPageEditor.currentPageEditor().getStartTime();
					double endTime   = PIPageEditor.currentPageEditor().getEndTime();

					if ((startTime == -1) || (endTime   == -1) || (startTime == endTime))
						return ""; //$NON-NLS-1$

					return decimalFormat.format((profiledItem.maxMemoryItem.maxChunks + 512)/1024);
			    }
				case COLUMN_ID_MEMORY_STACK:
			    {
					double startTime = PIPageEditor.currentPageEditor().getStartTime();
					double endTime   = PIPageEditor.currentPageEditor().getEndTime();

					if ((startTime == -1) || (endTime   == -1) || (startTime == endTime))
						return ""; //$NON-NLS-1$

					return decimalFormat.format((profiledItem.maxMemoryItem.maxStackHeap + 512)/1024);
			    }
				case COLUMN_ID_MEMORY_TOTAL:
			    {
					double startTime = PIPageEditor.currentPageEditor().getStartTime();
					double endTime   = PIPageEditor.currentPageEditor().getEndTime();

					if ((startTime == -1) || (endTime   == -1) || (startTime == endTime))
						return ""; //$NON-NLS-1$
					
					return decimalFormat.format((profiledItem.maxMemoryItem.maxTotal + 512)/1024);
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

	
	/*
	 * TableViewer sorter for the called-by and called function tableviewers
	 */
	private class SharedSorter extends ViewerSorter {
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
				// changed columns, so sort in the default order
				switch (column) {
					case COLUMN_ID_MEMORY_NAME:
					{
		            	// sort in ascending order
		            	sortAscending = true;
		                break;
					}
					case COLUMN_ID_MEMORY_CHUNKS:
					case COLUMN_ID_MEMORY_STACK:
					case COLUMN_ID_MEMORY_TOTAL:
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

			// find the TableColumn corresponding to column, and give it a column direction
			TableColumn sortByColumn = null;
			for (int i = 0; i < table.getColumnCount(); i++) {
				if (table.getColumn(i).getData() instanceof Integer) {
					if (((Integer)table.getColumn(i).getData()) == column) {
						sortByColumn = table.getColumn(i);
						break;
					}
				}
			}

			if (sortByColumn != null) {
				table.setSortColumn(sortByColumn);
				table.setSortDirection(sortAscending ? SWT.UP : SWT.DOWN);
			}
		}
		
		/*
		 * compare two items from a table column
		 */
		public int compare(Viewer viewer, Object e1, Object e2) {
			int returnCode = 0;
			
			MemThread elem1 = (MemThread)e1;
			MemThread elem2 = (MemThread)e2;

			// find the memory information for the two threads
			
			// compare based on the memory information
			switch (column) {
			case COLUMN_ID_MEMORY_CHUNKS:
				
				returnCode = elem1.maxMemoryItem.maxChunks >
									elem2.maxMemoryItem.maxChunks ? 1 : -1;
				break;
			case COLUMN_ID_MEMORY_STACK:
				returnCode = elem1.maxMemoryItem.maxStackHeap >
									elem2.maxMemoryItem.maxStackHeap ? 1 : -1;
				break;
			case COLUMN_ID_MEMORY_TOTAL:
				returnCode = elem1.maxMemoryItem.maxTotal >
									elem2.maxMemoryItem.maxTotal ? 1 : -1;
				break;
			case COLUMN_ID_MEMORY_NAME:
			{
				returnCode = this.getComparator().compare(elem1.fullName, elem2.fullName);
				break;
			}
			default:
				break;
			}

			// for descending order, reverse the sense of the compare
			if (!sortAscending)
				returnCode = -returnCode;

			return returnCode;
		}
	}

	public void actionPerformed(ActionEvent e) {}
	
	public void action(String actionString)
	{
		int graphIndex = this.myGraph.getGraphIndex();

		if (   actionString.equals("add") //$NON-NLS-1$
			|| actionString.equals("remove")) //$NON-NLS-1$
	    {
			actionAddRemove(actionString, graphIndex);
	    }
		else if (   actionString.equals("addall") //$NON-NLS-1$
				 || actionString.equals("removeall")) //$NON-NLS-1$
	    {
			actionAddRemoveAll(actionString, graphIndex);
	    }
	    else if (actionString.equals("copy")) //$NON-NLS-1$
	    {
	    	actionCopyOrSave(true, this.table, CHECKBOX_TEXT, false, "\t", "\n"); //$NON-NLS-1$ //$NON-NLS-2$
	        return; // no redraw needed
	    }
	    else if (actionString.equals("copyTable")) //$NON-NLS-1$
	    {
	    	actionCopyOrSave(true, this.table, CHECKBOX_TEXT, true, "\t", "\n"); //$NON-NLS-1$ //$NON-NLS-2$
	        return; // no redraw needed
	    }
		else if (actionString.equals("selectAll")) //$NON-NLS-1$
	    {
	    	actionSelectAll();
	        return;
	    }
	    else if (actionString.equals("saveTable")) //$NON-NLS-1$
	    {
	    	actionCopyOrSave(false, this.table, CHECKBOX_TEXT, true, ",", "\n"); //$NON-NLS-1$ //$NON-NLS-2$
	        return; // no redraw needed
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
			SaveTableString getString = new SaveTableString(this.table, CHECKBOX_TEXT, ",", "\n"); //$NON-NLS-1$ //$NON-NLS-2$
	        String copyString = getString.getData();
			StringSelection contents = new StringSelection(copyString);
	        cb.setContents(contents, contents);
	        return;
	    }

		tableViewer.refresh();
	    table.redraw();
	    this.myGraph.repaint();
	}

	private void actionAddRemove(String actionString, int graphIndex)
	{
		MemThread memThread;
		
		// true for "add", false for "remove"
		boolean addIt = actionString.equals("add"); //$NON-NLS-1$

		TableItem[] selectedItems = this.table.getSelection();
		for (int i = 0; i < selectedItems.length; i++)
		{
			selectedItems[i].setChecked(addIt);
			memThread = (MemThread)((TableItem)selectedItems[i]).getData();
			memThread.setEnabled(graphIndex, addIt);
		}

        // this table's set of checkbox-selected rows has changed,
		// so propagate that information
  		if (selectedItems.length != 0)
   			selectionChangeNotify();

  		this.table.deselectAll();
	}
	
	private void actionAddRemoveAll(String actionString, int graphIndex)
	{
		MemThread memThread;

		// true for "add", false for "remove"
		boolean addIt = actionString.equals("addall"); //$NON-NLS-1$

		TableItem[] selectedItems = this.table.getItems();
		for (int i = 0; i < selectedItems.length; i++)
		{
			selectedItems[i].setChecked(addIt);
			memThread = (MemThread)((TableItem)selectedItems[i]).getData();
			memThread.setEnabled(graphIndex, addIt);
		}

        // this table's set of checkbox-selected rows has changed,
		// so propagate that information
		selectionChangeNotify();

		this.table.deselectAll();
	}

	public void focusGained(FocusEvent e) {}

	public void focusLost(FocusEvent e)	{}

	public void piEventReceived(PIEvent be) 
	{
	    if (be.getType() == PIEvent.SELECTION_AREA_CHANGED2)
		{
			this.tableViewer.refresh();
			this.table.redraw();
		}
	    else if (be.getType() == PIEvent.CHANGED_MEMORY_TABLE)
		{
//			this.tableViewer.refresh();
//			this.table.redraw();
		}
	}

	public void updateItemData(boolean setInput)
	{
		HashSet<MemThread> noDuplicateMemThreads = myGraph.getMemTrace().getNoDuplicateMemThreads();
		if (tableItemData == null)
			tableItemData = new Vector<MemThread>(noDuplicateMemThreads.size());
		else
			tableItemData.clear();
		
		// tableItemData contains one entry per table row
		// the first profiled thread is the system-wide total memory information
		for (Iterator<MemThread> iter = noDuplicateMemThreads.iterator(); iter.hasNext(); )
		{
			MemThread memThread = iter.next();
			if (   (memThread.threadId != 0xffffffffbabbeaaaL)
				&& (memThread.threadId != 0xffffffffbabbea20L))
			{
				tableItemData.add(memThread);
			}
		}

		// refresh the table, if needed
		if (setInput)
			refreshTableViewer();
	}

	public void refreshTableViewer()
	{
		this.tableViewer.setInput(tableItemData);
	}
	
	public void sortOnColumnSelection(TableColumn tableColumn) {
    	int columnID = ((Integer) tableColumn.getData()).intValue();
    	((SharedSorter) tableViewer.getSorter()).doSort(columnID);

		tableViewer.refresh();
    	table.redraw();
	}
	
	private class ColumnSelectionHandler extends SelectionAdapter
	{
		public void widgetSelected(SelectionEvent e)
        {
        	if (!(e.widget instanceof TableColumn))
        		return;
        	
        	sortOnColumnSelection((TableColumn) e.widget);
        }
	}
	
	public CheckboxTableViewer getTableViewer()
	{
		return this.tableViewer;
	}

	protected void createDefaultActions()
	{
		selectAllAction = new Action("SelectAll") { //$NON-NLS-1$
			public void run() {
				action("selectAll"); //$NON-NLS-1$
			}
		};
		selectAllAction.setEnabled(true);

		copyAction = new Action("Copy") { //$NON-NLS-1$
			public void run() {
				action("copy"); //$NON-NLS-1$
			}
		};
		copyAction.setEnabled(false);

		copyTableAction = new Action("CopyTable") { //$NON-NLS-1$
			public void run() {
				action("copyTable"); //$NON-NLS-1$
			}
		};
		copyTableAction.setEnabled(true);
		copyTableAction.setId("PICopyTable"); //$NON-NLS-1$
		copyTableAction.setText(Messages.getString("MemThreadTable.copyTable")); //$NON-NLS-1$

		saveTableAction = new Action("SaveTable") { //$NON-NLS-1$
			public void run() {
				action("saveTable"); //$NON-NLS-1$
			}
		};
		saveTableAction.setEnabled(true);
		saveTableAction.setId("PISaveTable"); //$NON-NLS-1$
		saveTableAction.setText(Messages.getString("MemThreadTable.SaveTable")); //$NON-NLS-1$
	}
}
