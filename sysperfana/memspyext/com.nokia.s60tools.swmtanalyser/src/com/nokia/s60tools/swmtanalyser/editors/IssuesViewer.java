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

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.swt.widgets.TreeItem;

import com.nokia.s60tools.swmtanalyser.analysers.ResultElements;
import com.nokia.s60tools.swmtanalyser.analysers.ResultsParentNodes;
import com.nokia.s60tools.swmtanalyser.resources.Tooltips;
import com.nokia.s60tools.swmtanalyser.ui.actions.CopyToClipboardAction;
import com.nokia.s60tools.swmtanalyser.ui.actions.ISelectionProvider;
import com.nokia.s60tools.swmtanalyser.ui.graphs.GraphsUtils;
import com.nokia.s60tools.swmtanalyser.ui.graphs.LinearIssuesGraph;

/**
 * Customized tree viewer in the Analysis tab.
 *
 */
public class IssuesViewer implements Listener, ICheckStateListener, ISelectionProvider {

	/**
	 * Copy a to clipboard action
	 */
	private IAction actionContextMenuCopyTo;
	
	private Tree issues_tree;
	private CheckboxTreeViewer issues_viewer;
	private LinearIssuesGraph issue_graph;
	

	/**
	 * Construction
	 * @param tree
	 * @param graphToBeUpdated
	 */
	public IssuesViewer(Tree tree, LinearIssuesGraph graphToBeUpdated)
	{
		this.issues_tree = tree;
		issues_viewer = new CheckboxTreeViewer(tree);
		
		GridData tableData = new GridData(GridData.FILL_BOTH);
		tableData.horizontalSpan = 7;
		tableData.grabExcessVerticalSpace = true;
		
		issues_viewer.getTree().setLayoutData(tableData);
		this.issue_graph = graphToBeUpdated;
		
		issues_viewer.addCheckStateListener(this);
	
	}
	/**
	 * Create linear analysis results tree and graph
	 */
	public void createIssuesViewerAndGraph()
	{
		TreeColumn tc = new TreeColumn(issues_viewer.getTree(), SWT.LEFT);
		tc.setWidth(60);
		tc.setResizable(true);

		TreeColumn itemName = new TreeColumn(issues_viewer.getTree(), SWT.NONE);
		itemName.setText(ResultElements.ITEM_NAME_COLUMN);
		itemName.setWidth(300);
		itemName.setToolTipText("Name of the thread/chunk/disk etc.");

		TreeColumn event = new TreeColumn(issues_viewer.getTree(), SWT.NONE);
		event.setText(ResultElements.EVENT_COLUMN);
		event.setWidth(200);
		event.setToolTipText("Name of the event.");

		TreeColumn delta = new TreeColumn(issues_viewer.getTree(), SWT.NONE);
		delta.setText(ResultElements.DELTA_COLUMN);
		delta.setWidth(150);
		delta.setToolTipText("Difference between the last and first cycle values.");

		TreeColumn severity = new TreeColumn(issues_viewer.getTree(), SWT.CENTER);
		severity.setText(ResultElements.SEVERITY_COLUMN);
		severity.setWidth(100);
		severity.setResizable(false);
		severity.setToolTipText("Severity of the issue.");

		itemName.addListener(SWT.Selection, this);		
		event.addListener(SWT.Selection, this);		
		delta.addListener(SWT.Selection,this);		
		severity.addListener(SWT.Selection, this);		

		issues_viewer.getTree().setHeaderVisible(true);
		issues_viewer.getTree().setLinesVisible(true);

		issues_tree.setSortColumn(severity);
		issues_tree.setSortDirection(SWT.DOWN);
		issues_viewer.setSorter(new IssuesSorter(SWT.DOWN, severity));

		//Showing tooltip for the each issue in the tree view
		Listener treeListener = new Listener () {
			Shell tip = null;
			Label label = null;
			public void handleEvent (Event event) {
				switch (event.type) {
				case SWT.Dispose:
				case SWT.KeyDown:
				case SWT.MouseExit:	
				case SWT.MouseMove: {
					if (tip == null) break;
					tip.dispose ();
					tip = null;
					label = null;
					break;
				}
				case SWT.MouseHover: {
					TreeItem item = issues_tree.getItem (new Point (event.x, event.y));
					if (item != null && getTooltipForItem(item)!="") {
						if (tip != null  && !tip.isDisposed ()) tip.dispose ();
						tip = new Shell (Display.getCurrent().getActiveShell(), SWT.ON_TOP | SWT.NO_FOCUS | SWT.TOOL);
						tip.setBackground (Display.getCurrent().getSystemColor (SWT.COLOR_INFO_BACKGROUND));
						FillLayout layout = new FillLayout ();
						layout.marginWidth = 2;
						tip.setLayout (layout);
						label = new Label (tip, SWT.NONE);
						label.setForeground (Display.getCurrent().getSystemColor (SWT.COLOR_INFO_FOREGROUND));
						label.setBackground (Display.getCurrent().getSystemColor (SWT.COLOR_INFO_BACKGROUND));
						label.setData ("TreeItem", item);
						label.setText (getTooltipForItem(item));
						Point size = tip.computeSize (SWT.DEFAULT, SWT.DEFAULT);
						Point p = new Point(event.x, event.y);
						Point pp = Display.getCurrent().map(issues_tree, null, p);
						tip.setBounds (pp.x, pp.y+20, size.x, size.y);
						tip.setVisible (true);
					}
				}
				}
			}
		};
		issues_tree.addListener (SWT.Dispose, treeListener); //When widget disposed
		issues_tree.addListener (SWT.KeyDown, treeListener); //When mouse key pressed
		issues_tree.addListener (SWT.MouseMove, treeListener); //When mouse moved
		issues_tree.addListener (SWT.MouseHover, treeListener); //When mouse hovers over the control
		issues_tree.addListener (SWT.MouseExit, treeListener);
		
		issue_graph.constructGraphArea();
		
		hookContextMenu();
	}

	/**
	 * Sets the content provider for viewer 
	 * @see {@link CheckboxTreeViewer#setContentProvider(org.eclipse.jface.viewers.IContentProvider)}
	 * @param contentProvider
	 */
	public void setContentProvider(ITreeContentProvider contentProvider)
	{
		issues_viewer.setContentProvider(contentProvider);	
	}

	/**
	 * Sets the label provider for viewer 
	 * @see {@link CheckboxTreeViewer#setLabelProvider(org.eclipse.jface.viewers.IBaseLabelProvider)}
	 * @param labelProvider
	 */
	public void setLabelProvider(IssuesTreeLabelProvider labelProvider)
	{
		issues_viewer.setLabelProvider(labelProvider);
	}

	/**
	 * Adds a filter to  viewer 
	 * @see {@link CheckboxTreeViewer#addFilter(ViewerFilter)}
	 * @param filter
	 */
	public void addFilter(ViewerFilter filter)
	{
		issues_viewer.addFilter(filter);
	}

	/**
	 * Refresh the viewer @see 
	 * {@link CheckboxTreeViewer#refresh()}
	 */
	public void refresh()
	{
		issues_viewer.refresh();
	}

	/**
	 * Set the input to viewer and expand 
	 * @see {@link CheckboxTreeViewer#setInput(Object)}
	 * @see {@link CheckboxTreeViewer#expandAll()} 
	 * @param input
	 */
	public void setInput(Object input)
	{
		issues_viewer.setInput(input);
		issues_viewer.expandAll();
	}

	/**
	 * Get the input from viewer 
	 * @see {@link CheckboxTreeViewer#getInput()}
	 * @param input
	 */
	public Object getInput()
	{
		return issues_viewer.getInput();
	}
	
	/**
	 * Returns the description for the given event.
	 * @param item TreeItem of event
	 * @return tooltip description
	 */
	public String getTooltipForItem(TreeItem item)
	{
		String tooltip = item.getText(2);
		if(tooltip == "Heap size")
		{
			tooltip = Tooltips.getTooltip("Tooltips.HeapSize")
				+"\n"+Tooltips.getTooltip("Tooltips.Delta")
				+"\n"+Tooltips.getTooltip("Tooltips.Severity");
		}
		else if(tooltip == "No of Files")
		{
			tooltip = Tooltips.getTooltip("Tooltips.NoOfFiles1")+item.getText(1)+Tooltips.getTooltip("Tooltips.NoOfFiles2")
			+"\n"+Tooltips.getTooltip("Tooltips.Delta")
			+"\n"+Tooltips.getTooltip("Tooltips.Severity");
		}
		else if(tooltip == "Heap allocated space")
		{
			tooltip = Tooltips.getTooltip("Tooltips.HeapAllocatedSpace")
			+"\n"+Tooltips.getTooltip("Tooltips.Delta")
			+"\n"+Tooltips.getTooltip("Tooltips.Severity");
		}
		else if(tooltip == "Heap allocated cell count")
		{
			tooltip = Tooltips.getTooltip("Tooltips.HeapAllocatedCellCount")
			+"\n"+Tooltips.getTooltip("Tooltips.Delta")
			+"\n"+Tooltips.getTooltip("Tooltips.Severity");
		}
		else if(tooltip == "No of PS Handles")
		{
			tooltip = Tooltips.getTooltip("Tooltips.NoOfPSHandles")+item.getText(1)
			+"\n"+Tooltips.getTooltip("Tooltips.Delta")
			+"\n"+Tooltips.getTooltip("Tooltips.Severity");
		}
		else if(tooltip == "System Data")
		{
			tooltip = Tooltips.getTooltip("Tooltips.SystemData")
			+"\n"+Tooltips.getTooltip("Tooltips.Delta")
			+"\n"+Tooltips.getTooltip("Tooltips.Severity");
		}
		else if(tooltip == "RAM used")
		{
			tooltip = Tooltips.getTooltip("Tooltips.RAMUsed")
			+"\n"+Tooltips.getTooltip("Tooltips.Delta")
			+"\n"+Tooltips.getTooltip("Tooltips.Severity");
		}
		else if(tooltip == "Disk used")
		{
			tooltip = Tooltips.getTooltip("Tooltips.DiskUsed")+item.getText(1)
			+"\n"+Tooltips.getTooltip("Tooltips.Delta")
			+"\n"+Tooltips.getTooltip("Tooltips.Severity");
		}

		return tooltip;	
	}

	private class IssuesSorter extends ViewerSorter
	{

		private int direction;
		private TreeColumn givenColumn;

		public IssuesSorter(int direction, TreeColumn sortColumn)
		{
			this.direction = direction;
			this.givenColumn = sortColumn;
		}
		public int compare(Viewer viewer, Object obj1, Object obj2)
		{
			int result = 0;

			if(obj1 instanceof ResultElements && obj2 instanceof ResultElements)
			{
				ResultElements elem1 = (ResultElements)obj1;
				ResultElements elem2 = (ResultElements)obj2;

				if(givenColumn.getText().equalsIgnoreCase(ResultElements.DELTA_COLUMN))
				{
					result = elem1.compareByDelta(elem2);
				}
				else if(givenColumn.getText().equalsIgnoreCase(ResultElements.SEVERITY_COLUMN))
				{
					result = elem1.compareTo(elem2);
				}
				else if(givenColumn.getText().equalsIgnoreCase(ResultElements.ITEM_NAME_COLUMN))
				{
					result = elem1.getItemName().compareTo(elem2.getItemName());
				}
				else if(givenColumn.getText().equalsIgnoreCase(ResultElements.EVENT_COLUMN))
				{
					result = elem1.getEvent().compareTo(elem2.getEvent());
				}
				if(direction == SWT.UP)
					return result * 1;
				else
					return result * -1;
			}
			return result;
		}

	}

	/* (non-Javadoc)
	 * @see org.eclipse.swt.widgets.Listener#handleEvent(org.eclipse.swt.widgets.Event)
	 */
	public void handleEvent(Event event)
	{
		TreeColumn sortedColumn = issues_tree.getSortColumn();
		TreeColumn currentSelected = (TreeColumn)event.widget;

		int dir = issues_tree.getSortDirection();

		if(sortedColumn == currentSelected){
			dir = dir == SWT.UP ? SWT.DOWN : SWT.UP;
		}else
		{
			issues_tree.setSortColumn(currentSelected);
			dir = SWT.UP;
		}

		issues_viewer.setSorter(new IssuesSorter(dir, currentSelected));
		issues_viewer.getTree().setSortDirection(dir);

	}
	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ICheckStateListener#checkStateChanged(org.eclipse.jface.viewers.CheckStateChangedEvent)
	 */
	public void checkStateChanged(CheckStateChangedEvent event) {

		Object selectedElement = event.getElement();

		if(selectedElement == null)
			return;
		
		if(selectedElement instanceof ResultsParentNodes)
		{
			((CheckboxTreeViewer)(issues_viewer)).setSubtreeChecked(selectedElement, event.getChecked());
			Object [] children = ((ResultsParentNodes)(selectedElement)).getChildren();
			
			for(Object obj:children)
			{
				if(obj instanceof ResultElements)
				{
					ResultElements res = (ResultElements)obj;
					CheckStateChangedEvent stateChangeEvent = new CheckStateChangedEvent(((CheckboxTreeViewer)(issues_viewer)), res, event.getChecked());
					checkStateChanged(stateChangeEvent);
				}
			}
		}
		else if(selectedElement instanceof ResultElements) {
		{
			ResultElements elem = ((ResultElements)selectedElement);

			if(event.getChecked())
			{
				if(elem.getColor() == null)
					elem.setColor(GraphsUtils.getRandomColor());
			}
			else
			{
				elem.setColor(null);
			}	
			issues_viewer.update(elem, null);
		}
		notifyIssuesSelection();
	  }	
	
	}
	
	private void notifyIssuesSelection()
	{
		Object [] checkedElems = ((CheckboxTreeViewer)(issues_viewer)).getCheckedElements();
		
		ArrayList<ResultElements> selectedItems = new ArrayList<ResultElements>();
		
		for(Object obj:checkedElems)
		{
			if(obj instanceof ResultElements)
			{
				ResultElements elem = (ResultElements)obj;
				selectedItems.add(elem);
			}
		}
		
		issue_graph.setSelectedIssues(selectedItems);
		issue_graph.constructGraphArea(); 
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
	    Menu menu = menuMgr.createContextMenu(issues_viewer.getControl());
	    issues_viewer.getControl().setMenu(menu);
	}	
	
	/**
	 * @param manager -- MenuManager on which actions will be created.
	 */
	private void fillContextMenu(IMenuManager manager) {
		actionContextMenuCopyTo = new CopyToClipboardAction(this);				
		manager.add(actionContextMenuCopyTo);
	}
	
	/* (non-Javadoc)
	 * @see com.nokia.s60tools.swmtanalyser.ui.actions.ISelectionProvider#getSelection()
	 */
	public ISelection getUserSelection() {
		return issues_viewer.getSelection();
	}		
}