/*
* Copyright (c) 2009 Nokia Corporation and/or its subsidiary(-ies).
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
package com.nokia.s60tools.swmtanalyser.editors;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.util.Map.Entry;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;

import com.nokia.s60tools.swmtanalyser.ui.actions.CopyToClipboardAction;
import com.nokia.s60tools.swmtanalyser.ui.actions.ISelectionProvider;
import com.nokia.s60tools.swmtanalyser.ui.graphs.IGraphTypeSelectionListener;
import com.nokia.s60tools.util.debug.DbgUtility;

/**
 * Customized TableViewer UI class for showing items in Graphs Tab.
 *
 */
public class FilterTextTable extends Composite implements ISelectionProvider{

	
	/**
	 * Copy a to clipboard action
	 */
	private IAction actionContextMenuCopyTo;
	
	//Check box Table Viewer
	private CheckboxTableViewer tableViewer;
	//Text box to filter items in table
	private Text filterText;
	//Drop down to select filter type -> 'Start with' or -> 'Contains'
	private Combo filterTypes;
	//Label for showing the numbr of checked elements
	private Label statusLabel;
	//Viewer filter for tableviewer
	private TableTextFilter textFilter;
	private IGraphTypeSelectionListener graphListener;
	//Checkbox state change listener
	private CheckBoxStateChangeListener listener;
	//Temporary elements
	private String tableName;
	/**
	 * Item names for creating {@link TableViewerInputObject}
	 */
	ArrayList<String> input = new ArrayList<String>();

	/**
	 * Save all checked elements in this
	 */
	Map<String, Color> checked = new HashMap<String, Color>();
	/**
	 * Threads
	 */
	static final String THREADS_TITLE = "Threads";
	/**
	 * Chunks
	 */
	static final String CHUNKS_TITLE = "Chunks";
	/**
	 * Disks
	 */
	static final String DISKS_TITLE = "Disks";
	/**
	 * System Data
	 */
	static final String SYSTEM_DATA_TITLE = "System Data";
	
	/**
	 * Construction
	 * @param graphListener
	 * @param composite
	 * @param columnName
	 */
	public FilterTextTable(IGraphTypeSelectionListener graphListener, Composite composite, String columnName) {

		super(composite, SWT.NONE);
		
		this.tableName = columnName;
		this.graphListener = graphListener;
		
		setLayout(new GridLayout(2, false));
		GridData g = new GridData(GridData.FILL_BOTH);
		setLayoutData(g);
		
		filterText = new Text(this, SWT.BORDER);
		filterText.setText("type filter text");
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.grabExcessHorizontalSpace = true;
		gd.grabExcessVerticalSpace = false;
		gd.verticalAlignment = SWT.TOP;
		filterText.setLayoutData(gd);
		filterText.selectAll();
		textFilter = new TableTextFilter(1);
		
		filterText.addModifyListener(new ModifyListener(){
			public void modifyText(ModifyEvent e) {
				String t = ((Text)e.widget).getText();
				textFilter.setFilterText(t);
				textFilter.setFilterTypeIndex(filterTypes.getSelectionIndex());
				refreshTableContents();
			}

		});
	
		filterTypes = new Combo(this, SWT.BORDER|SWT.READ_ONLY);
		filterTypes.setItems(new String[]{"Starts with", "Contains"});
		GridData data = new GridData();
		data.widthHint = 100;
		data.heightHint = 15;
		filterTypes.select(0);
		filterTypes.setLayoutData(data);
		filterTypes.addSelectionListener(new SelectionListener() {			
			/* (non-Javadoc)
			 * @see org.eclipse.swt.events.SelectionListener#widgetSelected(org.eclipse.swt.events.SelectionEvent)
			 */
			public void widgetSelected(SelectionEvent e) {
				textFilter.setFilterTypeIndex(filterTypes.getSelectionIndex());
				refreshTableContents();				
			}			
			/* (non-Javadoc)
			 * @see org.eclipse.swt.events.SelectionListener#widgetDefaultSelected(org.eclipse.swt.events.SelectionEvent)
			 */
			public void widgetDefaultSelected(SelectionEvent e) {
				// Not needed/used				
			}
		});
		
		tableViewer = CheckboxTableViewer.newCheckList(this, SWT.BORDER|SWT.FULL_SELECTION | SWT.MULTI);
		GridData tableData = new GridData(GridData.FILL_BOTH);
		tableData.horizontalSpan = 2;
		
		tableViewer.getTable().setLayoutData(tableData);
		
		TableColumn tc = new TableColumn(tableViewer.getTable(), SWT.CENTER);
		tc.setWidth(50);
		tc.setResizable(true);

		TableColumn tc1 = new TableColumn(tableViewer.getTable(), SWT.LEFT);
		tc1.setText(columnName);
		tc1.setWidth(400);
		tc1.setResizable(true);
		tc1.addListener(SWT.Selection, new Listener(){
			public void handleEvent(Event event) {
				TableColumn sortedColumn = tableViewer.getTable().getSortColumn();
				TableColumn currentSelected = (TableColumn)event.widget;
				
				int dir = tableViewer.getTable().getSortDirection();
				
				if(sortedColumn == currentSelected){
					dir = dir == SWT.UP ? SWT.DOWN : SWT.UP;
				}else
				{
					tableViewer.getTable().setSortColumn(currentSelected);
					dir = SWT.UP;
				}
				if(currentSelected == tableViewer.getTable().getColumn(1))
				{
					tableViewer.setSorter(new Sorter(dir));
				}
				tableViewer.getTable().setSortDirection(dir);
			}
		});
		
		tableViewer.getTable().setHeaderVisible(true);
		tableViewer.getTable().setLinesVisible(true);
		tableViewer.setContentProvider(new TableStructuredContentProvider(this));
		tableViewer.setLabelProvider(new TableLabelColorProvider());

		listener = new CheckBoxStateChangeListener();
		tableViewer.addCheckStateListener(listener);

		tableViewer.addFilter(textFilter);
		
		GridData lGd = new GridData(GridData.FILL_HORIZONTAL);
		lGd.verticalAlignment = GridData.END;
		statusLabel = new Label(this, SWT.NONE);
		statusLabel.setText("Selected : 0");
		statusLabel.setLayoutData(lGd);
		
		
		actionContextMenuCopyTo = new CopyToClipboardAction(this);		
		
		//
		// Context menu 
		//
		MenuManager menuMgr = new MenuManager("#ContextMenu1");
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager manager) {
				FilterTextTable.this.populateContextMenu(manager);
			}
		});		
		Menu menu = menuMgr.createContextMenu(tableViewer.getTable());
		tableViewer.getTable().setMenu(menu);				
	}

	/**
	 * Refreshes data table contents.
	 */
	private void refreshTableContents() {
		//Refresh in separate thread to avoid invalid thread access.
		Runnable runnable = new Runnable(){
			public void run() {
				tableViewer.refresh();
			}
		};
		Display.getDefault().syncExec(runnable);
		
		//After refresh, check the elements again if any selected before.
		Iterator<Entry<String, Color>> itr = checked.entrySet().iterator();
		while(itr.hasNext())
		{
			Entry<String, Color> entry = itr.next();
			for(int i=0; i < tableViewer.getTable().getItemCount(); i++)
			{
				TableViewerInputObject objNow = (TableViewerInputObject)tableViewer.getElementAt(i);
				//If name is same, then update the color and object
				if(entry.getKey().equals(objNow.getName()))
				{
					tableViewer.setChecked(objNow, true);
					tableViewer.getTable().getItem(i).setBackground(0, entry.getValue());
					objNow.setColor(entry.getValue());
					CheckStateChangedEvent ev = new CheckStateChangedEvent(tableViewer, objNow, true);
					listener.checkStateChanged(ev);
				}
			}
		}
		
		//If in the filtered list no selection is there, then clear the graph
		if(tableViewer.getCheckedElements().length==0)
		{
			CheckStateChangedEvent ev = new CheckStateChangedEvent(tableViewer, null, false);
			listener.checkStateChanged(ev);
		}
		//Update the status label
		statusLabel.setText("Selected : "+tableViewer.getCheckedElements().length);
	}
	
	/**
	 * Set the input and update label
	 * @param input
	 */
	public void setInput(ArrayList<String> input) {
		this.input = input;	
		statusLabel.setText("Selected : 0");
		tableViewer.setInput(this.input);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.swt.widgets.Control#setEnabled(boolean)
	 */
	public void setEnabled(boolean arg0) {
		filterText.setEnabled(arg0);
		filterTypes.setEnabled(arg0);
		tableViewer.getTable().setEnabled(arg0);
		//tableViewer.setAllGrayed(arg0);
		super.setEnabled(arg0);
	}
	
	/**
	 * Clears the selections in table. 
	 *
	 */
	public void cancelSelectionList(){
		statusLabel.setText("Selected : 0");
		tableViewer.refresh();
		checked.clear();
	}
	
	/**
	 * Set the text to filter and notify listeners
	 * @param text
	 */
	public void setFilterText(String text)
	{
		filterText.setText(text);
		filterText.notifyListeners(SWT.Modify, new Event());
	}
	
	/**
	 * 
	 * Customized CheckState Listener class.
	 *
	 */
	class CheckBoxStateChangeListener implements ICheckStateListener
	{
		/**
		 * When CheckBox selection is changed, the tool generates 
		 * random colors and assigns them to selected threads. 
		 * These colors are used to distinguish graphs related to different threads.
		 * Also, this method informs the listeners about selected threads/chunks/disks/system-elements.
		 */
		public void checkStateChanged(CheckStateChangedEvent e) {
			
			TableViewerInputObject obj =((TableViewerInputObject)e.getElement());

			if(obj!=null)
			{
				if(e.getChecked())
			    {
					DbgUtility.println(DbgUtility.PRIORITY_OPERATION, "::::::::"+obj);
					Random rand = new Random();
					int r = rand.nextInt(255);
					int g = rand.nextInt(255);
					int b = rand.nextInt(255);
					
					if(obj.getColor() == null)
						obj.setColor(new Color(Display.getDefault(), r, g, b));
					
					if(!checked.containsKey(obj.getName()))
						checked.put(obj.getName(), obj.getColor());
				}
				else
				{
					obj.setColor(null);
					checked.remove(obj.getName());
				}
				tableViewer.update(obj, null);
			}
			if(tableName.equals(THREADS_TITLE))
				graphListener.notifyThreadsSelection();
			else if(tableName.equals(CHUNKS_TITLE))
				graphListener.notifyChunksSelection();
			else if(tableName.equals(DISKS_TITLE))
				graphListener.notifyDisksSelection();
			else if(tableName.equals(SYSTEM_DATA_TITLE))
				graphListener.notifySysElementsSelection();
						
			tableViewer.getTable().deselectAll();
			statusLabel.setText("Selected : "+tableViewer.getCheckedElements().length);			
		}
			
	}
	
	/**
	 * Returns the table viewer associated with this.
	 * @return tableViewer
	 */
	public CheckboxTableViewer getTableViewer()
	{
		return this.tableViewer;
	}
	
	/**
	 * @return check state listener associated with this table.
	 */
	public CheckBoxStateChangeListener  getCheckStateListener()
	{
		return this.listener;
	}
	
	/**
	 * This sorter class is associated with element name
	 * 
	 */
	class Sorter extends ViewerSorter
	{
		int sortDirection;
		
		Sorter(int sortDirection)
		{
			this.sortDirection = sortDirection;
		}
		public int compare(Viewer viewer, Object e1, Object e2)
		{
			int returnValue = 0;
			
			TableViewerInputObject o1 = (TableViewerInputObject)e1;
			TableViewerInputObject o2 = (TableViewerInputObject)e2;
			
			returnValue = o1.getName().toLowerCase().compareTo(o2.getName().toLowerCase());
			
			if(sortDirection == SWT.UP)
				return returnValue;
			else
				return returnValue * -1;
		}
	}
	
	/**
	 * Populates context menu at run time.
	 * @param manager Menu manager instance.
	 */
	private void populateContextMenu(IMenuManager manager) {		
		manager.add(actionContextMenuCopyTo);
	}		

	/* (non-Javadoc)
	 * @see com.nokia.s60tools.swmtanalyser.ui.actions.ISelectionProvider#getSelection()
	 */
	public ISelection getUserSelection() {
		return tableViewer.getSelection();
	}	
}
