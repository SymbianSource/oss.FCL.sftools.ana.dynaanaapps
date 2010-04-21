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
import java.util.Random;

import org.eclipse.jface.action.Action;
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
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.TableColumn;

import com.nokia.s60tools.swmtanalyser.ui.actions.CopyToClipboardAction;
import com.nokia.s60tools.swmtanalyser.ui.actions.ISelectionProvider;
import com.nokia.s60tools.swmtanalyser.ui.graphs.GraphForAllEvents;

/**
 * Helper class for graphed items tab.
 *
 */
public class GraphedItemsHelper implements Listener, ISelectionProvider{
	
	/**
	 * Copy a to clipboard action
	 */
	private IAction actionContextMenuCopyTo;


	CheckboxTableViewer graphedItemsViewer = null;
	
	private GraphForAllEvents graphedItemsGraph;
	private GraphedItemsSelectionListener graphedItemChangedListener;
	
	/**
	 * Construts UI controls over the area of given TabItem.
	 * @param graphedItemsTab represents Graphed items Tab.
	 * @param allEventsGraph represents the graph which gets updated based upon
	 * the actions on UI controls of the Graphed items tab. 
	 */
	public CheckboxTableViewer constructGraphedItemsViewer(TabItem graphedItemsTab, GraphForAllEvents allEventsGraph)
	{
		this.graphedItemsGraph = allEventsGraph;
		
		Composite compAllItems = new Composite(graphedItemsTab.getParent(),  SWT.NONE); 
		compAllItems.setLayout(new GridLayout(1, true));

		
		graphedItemsViewer = CheckboxTableViewer.newCheckList(compAllItems, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI );  
		graphedItemsViewer.getTable().setLayoutData(new GridData(GridData.FILL_VERTICAL|GridData.FILL_HORIZONTAL));
		
		TableColumn tc = new TableColumn(graphedItemsViewer.getTable(), SWT.CENTER);
		tc.setWidth(50);
		tc.setResizable(true);

		TableColumn elems_col = new TableColumn(graphedItemsViewer.getTable(), SWT.LEFT);
		elems_col.setText(GraphedItemsInput.COL1_ELEMENTS);
		elems_col.setWidth(600);
		elems_col.setResizable(true);
		elems_col.addListener(SWT.Selection, this);
				
		TableColumn event_col = new TableColumn(graphedItemsViewer.getTable(), SWT.LEFT);
		event_col.setText(GraphedItemsInput.COL2_EVENT);
		event_col.setWidth(150);
		event_col.setResizable(true);
		event_col.addListener(SWT.Selection, this);
		
		TableColumn type_col = new TableColumn(graphedItemsViewer.getTable(), SWT.LEFT);
		type_col.setText(GraphedItemsInput.COL3_TYPE);
		type_col.setWidth(150);
		type_col.setResizable(true);
		type_col.addListener(SWT.Selection, this);
		
		graphedItemsViewer.getTable().setHeaderVisible(true);
		graphedItemsViewer.getTable().setLinesVisible(true);		
		
		graphedItemsViewer.setContentProvider(new GraphedItemsContentProvider());
		graphedItemsViewer.setLabelProvider(new TableLabelColorProvider());
		
		graphedItemChangedListener = new GraphedItemsSelectionListener(allEventsGraph);
		graphedItemsViewer.addCheckStateListener(graphedItemChangedListener);
		
		hookContextMenu();
		
		graphedItemsTab.setControl(compAllItems);
				
		return graphedItemsViewer;
	}
	
	/**
	 * This method creates the context menu on the Graphed Items Tab
	 */
	 
	private void hookContextMenu()
	{
		MenuManager menuMgr = new MenuManager("#PopupMenu"); //$NON-NLS-1$
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager manager) {
				fillContextMenu(manager);
			}
        });
	    Menu menu = menuMgr.createContextMenu(graphedItemsViewer.getTable());
		graphedItemsViewer.getTable().setMenu(menu);
	}
	
	private Color getRandomColor()
	{
		Random rand = new Random();
		int r = rand.nextInt(255);
		int g = rand.nextInt(255);
		int b = rand.nextInt(255);
		return new Color(Display.getCurrent(), r, g,b);
	}
	
	/**
	 * The method checks all items in the given viewer and assigns random colors for all checked items.
	 * @param tableViewer
	 */
	private void selectAllItems(CheckboxTableViewer tableViewer)
	{
		tableViewer.setAllChecked(true);
		
		for(Object obj:tableViewer.getCheckedElements())
		{
			if(obj instanceof GraphedItemsInput){
				GraphedItemsInput graphedItem = (GraphedItemsInput)obj;
				
				if(graphedItem.getColor() == null)
					graphedItem.setColor(getRandomColor());
				
				tableViewer.update(obj, null);
			}
		}
		
	}
	
	/**
	 * @param manager -- MenuManager on which actions will be created.
	 */
	private void fillContextMenu(IMenuManager manager) {
		//Popup Action to check all items in the graphed items viewer.
		Action checkAllItems = new Action()
		{
			{
				this.setText("Check all");
				this.setToolTipText("");
			}
			public void run()
			{
				selectAllItems(graphedItemsViewer);
				notifyGraphedItemsSelection(graphedItemsViewer, graphedItemsGraph);
			}
			
		};
		manager.add(checkAllItems);
		
		//Popup action to uncheck all items in the graphed items viewer
		Action unCheckAllItems = new Action()
		{
			{
				this.setText("Uncheck all");
			}
			public void run()
			{
				for(Object obj:graphedItemsViewer.getCheckedElements())
				{
					((GraphedItemsInput)obj).setColor(null);
					graphedItemsViewer.setChecked(obj, false);
					graphedItemsViewer.update(obj, null);
					CheckStateChangedEvent e = new CheckStateChangedEvent(graphedItemsViewer, obj, false);
					graphedItemChangedListener.checkStateChanged(e);
				}
				this.setToolTipText("");
			}
			
		};
		manager.add(unCheckAllItems);	

		//Popup action to remove all items in the graphed items viewer
		Action removeAllItems = new Action()
				{
					public void run()
					{
						graphedItemsViewer.setInput(null);
						graphedItemsViewer.refresh();
						CheckStateChangedEvent e = new CheckStateChangedEvent(graphedItemsViewer, null, false);
						graphedItemChangedListener.checkStateChanged(e);
						graphedItemsGraph.removeAllData();
					}
					{
						this.setText("Remove all");
						this.setToolTipText("");
					}
				};
		manager.add(removeAllItems);
		
		actionContextMenuCopyTo = new CopyToClipboardAction(this);				
		manager.add(actionContextMenuCopyTo);
	}
	
	/**
	 * This method updates the graph based on the selections made in the given graphed items viewer.
	 * @param tableViewer 
	 * @param graphToBeUpdated
	 */
	private void notifyGraphedItemsSelection(CheckboxTableViewer tableViewer, GraphForAllEvents graphToBeUpdated)
	{
		Object [] checkedElems = tableViewer.getCheckedElements();
		
		ArrayList<GraphedItemsInput> selectedItems = new ArrayList<GraphedItemsInput>();
		
		for(Object obj:checkedElems)
		{
			GraphedItemsInput graphInput = (GraphedItemsInput)obj;
			selectedItems.add(graphInput);
		}
		
		graphToBeUpdated.setGraphedItemsInput(selectedItems);		
		graphToBeUpdated.constructGraphArea(); 
	}

	/* (non-Javadoc)
	 * @see org.eclipse.swt.widgets.Listener#handleEvent(org.eclipse.swt.widgets.Event)
	 */
	public void handleEvent(Event event) {
		
		TableColumn sortedColumn = graphedItemsViewer.getTable().getSortColumn();
		TableColumn currentSelected = (TableColumn)event.widget;
		
		int dir = graphedItemsViewer.getTable().getSortDirection();
		
		if(sortedColumn == currentSelected){
			dir = dir == SWT.UP ? SWT.DOWN : SWT.UP;
		}else
		{
			graphedItemsViewer.getTable().setSortColumn(currentSelected);
			dir = SWT.UP;
		}
		graphedItemsViewer.setSorter(new Sorter(dir,currentSelected));
		graphedItemsViewer.getTable().setSortDirection(dir);
	}
	

	/**
	 * 
	 * Customized CheckStateListener for Graphed Items viewer.
	 * This listener is associated with the Graphed Items viewer.
	 * The 'checkStateChanged' method gets invoked, when items are
	 * checked/unchecked in Graphed Items viewer. 
	 *
	 */
	class GraphedItemsSelectionListener implements ICheckStateListener
	{
			private GraphForAllEvents graphToBeUpdated;
			
			public GraphedItemsSelectionListener(GraphForAllEvents graphToBeUpdated){
				this.graphToBeUpdated = graphToBeUpdated;
			}
			public void checkStateChanged(CheckStateChangedEvent e) {
				
				GraphedItemsInput obj =((GraphedItemsInput)e.getElement());
				if(obj!=null)
				{
					if(e.getChecked())
				    {
						if(obj.getColor() == null)
							obj.setColor(getRandomColor());
					}
					else
					{
						obj.setColor(null);
					}	
					graphedItemsViewer.update(obj, null);
				}
				
				notifyGraphedItemsSelection(graphedItemsViewer,graphToBeUpdated);
			}
			
			
	}
	
	/**
	 * This sorter class is associated with table in Graphed Items Tab
	 * It contains logic to sort the table based on various columns.
	 * 
	 */
	class Sorter extends ViewerSorter
	{
		int sortDirection;
		TableColumn column;
		Sorter(int sortDirection, TableColumn column)
		{
			this.sortDirection = sortDirection;
			this.column = column;
		}
		public int compare(Viewer viewer, Object e1, Object e2)
		{
			int returnValue = 0;
			
			GraphedItemsInput o1 = (GraphedItemsInput)e1;
			GraphedItemsInput o2 = (GraphedItemsInput)e2;
			
			if(column.getText().equalsIgnoreCase(GraphedItemsInput.COL1_ELEMENTS))
				returnValue = o1.getName().toLowerCase().compareTo(o2.getName().toLowerCase());
			else if(column.getText().equalsIgnoreCase(GraphedItemsInput.COL2_EVENT))
				returnValue = o1.getEvent().toLowerCase().compareTo(o2.getEvent().toLowerCase());
			else if(column.getText().equalsIgnoreCase(GraphedItemsInput.COL3_TYPE))
				returnValue = o1.getType().toLowerCase().compareTo(o2.getType().toLowerCase());
			
			if(sortDirection == SWT.UP)
				return returnValue;
			else
				return returnValue * -1;
		}
	}

	/* (non-Javadoc)
	 * @see com.nokia.s60tools.swmtanalyser.ui.actions.ISelectionProvider#getSelection()
	 */
	public ISelection getUserSelection() {
		return graphedItemsViewer.getSelection();
	}
}
