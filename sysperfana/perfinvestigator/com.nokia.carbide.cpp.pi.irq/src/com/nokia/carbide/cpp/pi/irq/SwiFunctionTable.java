/*
 * Copyright (c) 2010 Nokia Corporation and/or its subsidiary(-ies). 
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

package com.nokia.carbide.cpp.pi.irq;

import java.awt.event.MouseEvent;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.ColorDialog;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

import com.nokia.carbide.cpp.internal.pi.visual.GenericTable;

/**
 * Software function interrupt table.
 * 
 */
public class SwiFunctionTable extends GenericTable implements
		ICheckStateListener {

	// sort direction
	private boolean sortAscending = true;

	/* irq trace graph */
	private IrqTraceGraph myGraph;

	/* parent component where all ui components are placed */
	private Composite parent;

	/* item data for table data */
	protected Vector<IrqSampleTypeWrapper> tableItemData;

	/* function color */
	private Hashtable<String, IrqSampleTypeWrapper> functionColors;

	final boolean checkAllWhenOpened = true;

	/**
	 * Constructor
	 * 
	 * @param myGraph
	 *            irq trace graph
	 * @param parent
	 *            parent component where all ui components are placed
	 */
	public SwiFunctionTable(IrqTraceGraph myGraph, Composite parent) {
		this.myGraph = myGraph;
		this.parent = parent;

		this.tableViewer = CheckboxTableViewer.newCheckList(parent, SWT.BORDER
				| SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION);
		this.table = this.tableViewer.getTable();
		this.table.setLayoutData(new GridData(GridData.FILL_BOTH));

		// add the check state handler, label provider and content provider
		this.tableViewer.addCheckStateListener(this);
		this.tableViewer.setLabelProvider(new SwiFunctionLabelProvider(
				this.table));
		this.tableViewer.setContentProvider(new FunctionTableContentProvider());
		this.tableViewer.setSorter(new SharedSorter());

		// create the columns
		TableColumn column;

		// data associated with the TableViewer will note which columns contain
		// hex values
		// Keep this in the order in which columns have been created
		boolean[] isHex = { false, false, false, false };
		this.table.setData("isHex", isHex); //$NON-NLS-1$

		// Check and color column
		column = new TableColumn(this.table, SWT.CENTER);
		column.setText(Messages.SwiFunctionTable_0);
		column.setWidth(30 + 15);
		column.setData(COLOR_COLUMN_INDEX);
		column.setMoveable(true);
		column.setResizable(true);
		column.addSelectionListener(new ColumnSelectionHandler());

		// Function name column
		column = new TableColumn(this.table, SWT.LEFT);
		column.setText(Messages.SwiFunctionTable_1);
		column.setWidth(COLUMN_WIDTH_SWI_FUNCTION + 15);
		column.setData(COLUMN_ID_SWI_FUNCTION);
		column.setMoveable(true);
		column.setResizable(true);
		column.addSelectionListener(new ColumnSelectionHandler());

		// return address column
		column = new TableColumn(tableViewer.getTable(), SWT.RIGHT);
		column.setText(Messages.SwiFunctionTable_2);
		column.setWidth(COLUMN_WIDTH_RETURN_ADDRESS);//  
		column.setData(COLUMN_ID_RETURN_ADDRESS);
		column.setMoveable(true);
		column.setResizable(true);
		column.addSelectionListener(new ColumnSelectionHandler());

		// count column
		column = new TableColumn(tableViewer.getTable(), SWT.RIGHT);
		column.setText(Messages.SwiFunctionTable_3);
		column.setWidth(COLUMN_WIDTH_COUNT);
		column.setData(COLUMN_ID_SWI_COUNT);
		column.setMoveable(true);
		column.setResizable(true);
		column.addSelectionListener(new ColumnSelectionHandler());

		// add mouse listener and set other table settings
		this.table.addMouseListener(new TableMouseListener());
		this.table.setHeaderVisible(true);
		this.table.setLinesVisible(true);
		this.table.setRedraw(true);

		// format data into table and sort table
		this.updateItemData(true);
		((SharedSorter) tableViewer.getSorter()).doSort(COLUMN_ID_SWI_COUNT);

		// initially, all rows are checked
		this.tableViewer.setAllChecked(checkAllWhenOpened);
		this.addColor();

		// listen for key sequences such as Ctrl-A and Ctrl-C
		table.addKeyListener(new TableKeyListener());

		tableViewer.refresh();
		table.redraw();
		this.addColor();

	}

	/**
	 * Function table content provider
	 */
	private static class FunctionTableContentProvider implements
			IStructuredContentProvider {
		/**
		 * Constructor
		 */
		public FunctionTableContentProvider() {
			super();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.eclipse.jface.viewers.IStructuredContentProvider#getElements(
		 * java.lang.Object)
		 */
		@SuppressWarnings("unchecked")
		public Object[] getElements(Object inputElement) {
			return ((Vector<IrqSampleTypeWrapper>) inputElement).toArray();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.jface.viewers.IContentProvider#dispose()
		 */
		public void dispose() {
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.eclipse.jface.viewers.IContentProvider#inputChanged(org.eclipse
		 * .jface.viewers.Viewer, java.lang.Object, java.lang.Object)
		 */
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}
	}

	/**
	 * updates item data into function table
	 * 
	 * @param setInput
	 *            true if table needs to be refreshed
	 */
	public void updateItemData(boolean setInput) {

		// Reset table data
		tableItemData = new Vector<IrqSampleTypeWrapper>();

		functionColors = new Hashtable<String, IrqSampleTypeWrapper>();

		// Get sample type wrappers from trace
		Hashtable<Long, IrqSampleTypeWrapper> sampleTypeWrappers = ((IrqTrace) this.myGraph
				.getTrace()).getSwiTable();
		if (sampleTypeWrappers == null) {
			return;
		}

		// Add sample type wrappers to table data
		Enumeration<Long> k = sampleTypeWrappers.keys();
		while (k.hasMoreElements()) {
			Long key = (Long) k.nextElement();
			if (sampleTypeWrappers.get(key).getClass() == IrqSampleTypeWrapper.class) {
				IrqSampleTypeWrapper wrapper = (IrqSampleTypeWrapper) sampleTypeWrappers
						.get(key);
				if (wrapper.getPrototypeSample().getFunction() != null) {
					wrapper.setSelected(checkAllWhenOpened);
					tableItemData.add(wrapper);
					functionColors.put(wrapper.getPrototypeSample()
							.getFunction().getFunctionName(), wrapper);
				}

			}
		}

		// refresh the table, if needed
		if (setInput)
			refreshTableViewer();

	}

	/**
	 * refreshes table viewer and adds colors into check box column
	 */
	public void refreshTableViewer() {
		this.tableViewer.setInput(tableItemData);
		this.addColor();

	}

	/**
	 * @return hashtable containing all function colors
	 */
	public Hashtable<String, IrqSampleTypeWrapper> getFunctionColors() {
		return functionColors;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jface.viewers.ICheckStateListener#checkStateChanged(org.eclipse
	 * .jface.viewers.CheckStateChangedEvent)
	 */
	public void checkStateChanged(CheckStateChangedEvent event) {
		if (event.getElement().getClass() == IrqSampleTypeWrapper.class) {
			IrqSampleTypeWrapper wrapper = (IrqSampleTypeWrapper) event
					.getElement();
			wrapper.setSelected(event.getChecked());
			myGraph.recalculateWholeGraph();
			myGraph.repaint();
			// myGraph.updateIrqCountsInLegends(IrqTraceGraph.TYPE_SWI);

		}
	}

	/**
	 * adds function colors into check box column
	 */
	public void addColor() {
		if (this.tableViewer == null)
			return;

		IrqSampleTypeWrapper wrapper;
		TableItem[] items = this.table.getItems();

		for (int i = 0; i < items.length; i++) {
			if (items[i].getData().getClass() == IrqSampleTypeWrapper.class) {
				wrapper = (IrqSampleTypeWrapper) items[i].getData();
				items[i].setBackground(COLOR_COLUMN_INDEX, new Color(parent
						.getDisplay(), wrapper.rgb));
			}
		}

		table.redraw();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.nokia.carbide.cpp.internal.pi.visual.GenericTable#action(java.lang
	 * .String)
	 */
	public void action(String actionString) {
		if (actionString.equals("add")) { //$NON-NLS-1$
			checkOrUncheckSelectedItems(true);
		} else if (actionString.equals("remove")) { //$NON-NLS-1$
			checkOrUncheckSelectedItems(false);
		} else if (actionString.equals("addall")) { //$NON-NLS-1$
			checkOrUncheckAllItems(true);
		} else if (actionString.equals("removeall")) { //$NON-NLS-1$
			checkOrUncheckAllItems(false);
		} else if (actionString.equals(Messages.SwiFunctionTable_4)) {
			actionRecolor();
		} else if (actionString.equals("copy")) //$NON-NLS-1$
		{
			actionCopyOrSave(true, this.table, CHECKBOX_TEXT, false, "\t", "\n"); //$NON-NLS-1$ //$NON-NLS-2$
			return; // no redraw needed
		} else if (actionString.equals("copyTable")) //$NON-NLS-1$
		{
			actionCopyOrSave(true, this.table, CHECKBOX_TEXT, true, "\t", "\n"); //$NON-NLS-1$ //$NON-NLS-2$
			return; // no redraw needed
		} else if (actionString.equals("selectAll")) //$NON-NLS-1$
		{
			actionSelectAll();
			return;
		}

		else if (actionString.equals("saveTable")) //$NON-NLS-1$
		{
			actionCopyOrSave(false, this.table, CHECKBOX_TEXT, true, ",", "\n"); //$NON-NLS-1$ //$NON-NLS-2$
			return; // no redraw needed
		}
	}

	/**
	 * Checks or unchecks all selected threads from table
	 * 
	 * @param value
	 *            true if items are checked
	 */
	private void checkOrUncheckSelectedItems(boolean value) {

		TableItem[] selectedItems = this.table.getSelection();
		for (int i = 0; i < selectedItems.length; i++) {
			if (selectedItems[i].getData().getClass() == IrqSampleTypeWrapper.class) {
				selectedItems[i].setChecked(value);
				IrqSampleTypeWrapper wrapper = (IrqSampleTypeWrapper) selectedItems[i]
						.getData();
				wrapper.setSelected(value);
			}
		}
		myGraph.recalculateWholeGraph();
		myGraph.repaint();

	}

	/**
	 * Checks or unchecks all table items
	 * 
	 * @param value
	 *            true if all items are checked
	 */
	private void checkOrUncheckAllItems(boolean value) {

		TableItem[] allItems = this.table.getItems();
		for (int i = 0; i < allItems.length; i++) {
			if (allItems[i].getData().getClass() == IrqSampleTypeWrapper.class) {
				allItems[i].setChecked(value);
				IrqSampleTypeWrapper wrapper = (IrqSampleTypeWrapper) allItems[i]
						.getData();
				if (value) {
					wrapper.setSelected(value);
				} else {
					wrapper.setSelected(value);

				}
			}

		}
		myGraph.recalculateWholeGraph();
		myGraph.repaint();

	}

	/**
	 * Mouselistener for function table
	 */
	private class TableMouseListener implements MouseListener {
		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.eclipse.swt.events.MouseListener#mouseDoubleClick(org.eclipse
		 * .swt.events.MouseEvent)
		 */
		public void mouseDoubleClick(org.eclipse.swt.events.MouseEvent e) {
			if (e.button == MouseEvent.BUTTON1) {
				/*
				 * TableItem[] selectedItems = table.getSelection(); if
				 * (selectedItems.length == 0) return;
				 * 
				 * if (selectedItems[0].getData() instanceof MemThread) {
				 * MemThread pMemThread =
				 * (MemThread)(selectedItems[0].getData()); if
				 * (pMemThread.isEnabled(myGraph.getGraphIndex()))
				 * action("remove"); //$NON-NLS-1$ else action("add");
				 * //$NON-NLS-1$ }
				 */
			}
			/*
			 * selectAllAction.setEnabled(table.getItemCount() > 0);
			 * copyAction.setEnabled(table.getSelectionCount() > 0);
			 * copyTableAction.setEnabled(table.getItemCount() > 0);
			 */
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.eclipse.swt.events.MouseListener#mouseDown(org.eclipse.swt.events
		 * .MouseEvent)
		 */
		public void mouseDown(org.eclipse.swt.events.MouseEvent e) {
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.eclipse.swt.events.MouseListener#mouseUp(org.eclipse.swt.events
		 * .MouseEvent)
		 */
		public void mouseUp(org.eclipse.swt.events.MouseEvent e) {

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

				// save all
				new MenuItem(contextMenu, SWT.SEPARATOR);
				getSaveTableItem(contextMenu, table.getItemCount() > 0);

				// Recolor highlighted items
				new MenuItem(contextMenu, SWT.SEPARATOR);
				getRecolorItem(contextMenu, Messages.SwiFunctionTable_5, table
						.getSelectionCount() > 0);

				contextMenu.setLocation(parent.toDisplay(e.x
						+ table.getLocation().x, e.y + table.getLocation().y));
				contextMenu.setVisible(true);

				table.setMenu(contextMenu);
			}
		}
	}

	/**
	 * columnselectionhandler for function table columns
	 */
	private class ColumnSelectionHandler extends SelectionAdapter {
		public void widgetSelected(SelectionEvent e) {
			if (!(e.widget instanceof TableColumn))
				return;

			sortOnColumnSelection((TableColumn) e.widget);
		}
	}

	/**
	 * @param tableColumn
	 *            which column is sorted
	 */
	public void sortOnColumnSelection(TableColumn tableColumn) {
		int columnID = ((Integer) tableColumn.getData()).intValue();
		((SharedSorter) tableViewer.getSorter()).doSort(columnID);

		this.refreshTableViewer();
		this.table.redraw();
	}

	/**
	 * Sorter for function column
	 */
	private class SharedSorter extends ViewerSorter {
		// last column sorted
		private int column = -1;

		/**
		 * decide on which column to sort by, and the sort ordering
		 * 
		 * @param column
		 *            which column to sort
		 */
		public void doSort(int column) {
			// ignore the column passed in and use the id set by the column
			// selection handler
			if (column == this.column) {
				// sort in other order
				sortAscending = !sortAscending;
			} else {
				// changed columns, so sort in the default order
				switch (column) {
				case COLOR_COLUMN_INDEX: {
					// sort in ascending order
					sortAscending = true;
					break;
				}
				case COLUMN_ID_SWI_FUNCTION:
				case COLUMN_ID_RETURN_ADDRESS:
				case COLUMN_ID_SWI_COUNT: {
					// sort in descending order
					sortAscending = false;
					break;
				}
				default: {
					// ignore the column
					return;
				}
				}
				this.column = column;
			}

			// find the TableColumn corresponding to column, and give it a
			// column direction
			TableColumn sortByColumn = null;
			for (int i = 0; i < table.getColumnCount(); i++) {
				if (table.getColumn(i).getData() instanceof Integer) {
					if (((Integer) table.getColumn(i).getData()) == column) {
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
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.eclipse.jface.viewers.ViewerComparator#compare(org.eclipse.jface
		 * .viewers.Viewer, java.lang.Object, java.lang.Object)
		 */
		public int compare(Viewer viewer, Object e1, Object e2) {

			// compare two items from a table column

			int returnCode = 0;

			IrqSampleTypeWrapper elem1 = (IrqSampleTypeWrapper) e1;
			IrqSampleTypeWrapper elem2 = (IrqSampleTypeWrapper) e2;

			// find the information for the two functions

			// compare based on the function information
			switch (column) {

			// color column
			case COLOR_COLUMN_INDEX:
				if (tableViewer.getChecked(e1) == true
						&& tableViewer.getChecked(e2) == false) {
					returnCode = -1;
				} else {
					returnCode = 1;
				}
				break;
			// function name column
			case COLUMN_ID_SWI_FUNCTION:
				if (elem1.getPrototypeSample().getFunction() != null
						&& elem2.getPrototypeSample().getFunction() != null) {
					returnCode = elem1.getPrototypeSample().getFunction().getFunctionName()
							.compareToIgnoreCase(elem2.getPrototypeSample()
									.getFunction().getFunctionName());
				}
				break;

			// return address column
			case COLUMN_ID_RETURN_ADDRESS:
				if (elem1.getPrototypeSample().getFunction() != null) {
					returnCode = elem1.getPrototypeSample().getFunction().getStartAddress() > elem2
							.getPrototypeSample().getFunction().getStartAddress() ? 1
							: -1;
				}
				break;

			// interrupt count column
			case COLUMN_ID_SWI_COUNT:
				returnCode = elem1.count > elem2.count ? 1 : -1;
				break;
			default:
				break;
			}

			// for descending order, reverse the sense of the compare
			if (!sortAscending)
				returnCode = -returnCode;

			return returnCode;
		}
	}

	/**
	 * Action for opening color dialog for all selected functions.
	 */
	private void actionRecolor() {

		// go thru selected items and open color dialog for each.
		TableItem[] selection = table.getSelection();
		for (TableItem item : selection) {

			if (item.getData().getClass() == IrqSampleTypeWrapper.class) {

				IrqSampleTypeWrapper wrapper = (IrqSampleTypeWrapper) item
						.getData();
				ColorDialog colorDialog = new ColorDialog(
						this.table.getShell(), SWT.PRIMARY_MODAL);

				// set name of the irq line as topic of the dialog
				colorDialog.setText(IrqSampleTypeWrapper.getLineText(wrapper
						.getPrototypeSample().getIrqL1Value()));
				RGB color = colorDialog.open();

				// if OK pressed, save new color into trace and set editor as
				// dirty
				if (color != null && color != wrapper.rgb) {

					// ensure that color is not yet assigned for another
					// thread/irq line
					if (!myGraph.getIrqTrace().getColorSet().contains(color)) {
						myGraph.getIrqTrace().changeColorOfThreadOrIRQLine(
								wrapper, color);

					} else {
						// if same color was alreadyassigned for another
						// thread/irq line, show error message
						MessageBox errorDialog = new MessageBox(this.table
								.getShell(), SWT.ICON_ERROR | SWT.OK);
						errorDialog.setText(Messages.SwiFunctionTable_6);
						errorDialog
								.setMessage(Messages.SwiFunctionTable_7);
						errorDialog.open();
					}
				}
			}

		}
		this.myGraph.repaint();
		this.refreshTableViewer();
	}

	/**
	 * sets sort direction
	 * 
	 * @param sortAscending
	 *            new sort direction
	 */
	public void setSortAscending(boolean sortAscending) {
		this.sortAscending = sortAscending;
	}
}
