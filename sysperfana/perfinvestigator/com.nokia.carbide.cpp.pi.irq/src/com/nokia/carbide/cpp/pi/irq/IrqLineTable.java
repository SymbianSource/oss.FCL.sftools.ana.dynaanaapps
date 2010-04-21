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
import org.eclipse.swt.widgets.ColorDialog;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import com.nokia.carbide.cpp.internal.pi.visual.GenericTable;

/**
 * Table for irq lines
 */
public class IrqLineTable extends GenericTable implements ICheckStateListener {

	/* sort direction */
	private boolean sortAscending = true;

	/* Graph */
	private IrqTraceGraph myGraph;

	/* Parent component where ui components are placed */
	private Composite parent;

	/* item data for table */
	protected Vector<IrqSampleTypeWrapper> tableItemData;

	/**
	 * Constructor
	 * 
	 * @param myGraph
	 *            graph
	 * @param parent
	 *            Parent component where ui components are placed
	 */
	public IrqLineTable(IrqTraceGraph myGraph, Composite parent) {
		this.myGraph = myGraph;
		this.parent = parent;

		this.tableViewer = CheckboxTableViewer.newCheckList(parent, SWT.BORDER
				| SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION);
		this.table = this.tableViewer.getTable();

		// add the check state handler, label provider and content provider
		this.tableViewer.addCheckStateListener(this);
		this.tableViewer.setLabelProvider(new IrqLineLabelProvider(this.table));
		this.tableViewer.setContentProvider(new TableContentProvider());
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
		column.setText(Messages.IrqLineTable_0);
		column.setWidth(COLUMN_WIDTH_CHECK_COLUMN);
		column.setData(COLOR_COLUMN_INDEX);
		column.setMoveable(true);
		column.setResizable(true);
		column.addSelectionListener(new ColumnSelectionHandler());

		// Thread line column
		column = new TableColumn(this.table, SWT.LEFT);
		column.setText(Messages.IrqLineTable_1);
		column.setWidth(COLUMN_WIDTH_THREAD_IRQ_LINE);
		column.setData(COLUMN_ID_IRQ_LINE);
		column.setMoveable(true);
		column.setResizable(true);
		column.addSelectionListener(new ColumnSelectionHandler());

		// Return address
		column = new TableColumn(tableViewer.getTable(), SWT.RIGHT);
		column.setText(Messages.IrqLineTable_2);
		column.setWidth(COLUMN_WIDTH_ADDRESS_COUNT);
		column.setData(COLUMN_ID_IRQ_COUNT);
		column.setMoveable(true);
		column.setResizable(true);
		column.addSelectionListener(new ColumnSelectionHandler());

		// add mouse listener and set other table settings
		this.table.addMouseListener(new TableMouseListener());
		this.table.setHeaderVisible(true);
		this.table.setLinesVisible(true);
		this.table.setRedraw(true);

		// format data into table
		this.updateItemData(true);
		((SharedSorter) tableViewer.getSorter()).doSort(COLUMN_ID_IRQ_COUNT);

		// Select initially no lines
		this.tableViewer.setAllChecked(false);

		// listen for key sequences such as Ctrl-A and Ctrl-C
		table.addKeyListener(new TableKeyListener());

		tableViewer.refresh();
		table.redraw();
		this.addColor();
	}

	/**
	 * content provider for table
	 */
	private static class TableContentProvider implements
			IStructuredContentProvider {

		/**
		 * Constructor
		 */
		public TableContentProvider() {
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
		} else if (actionString.equals(Messages.IrqLineTable_3)) {
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
	 * Checks or unchecks all selected irq lines from table
	 * 
	 * @param value
	 *            true if items are checked
	 */
	private void checkOrUncheckSelectedItems(boolean value) {
		// go thru selected items from thread table and notify graph that
		// threads are checked or unchecked
		TableItem[] selectedItems = this.table.getSelection();
		for (int i = 0; i < selectedItems.length; i++) {

			if (selectedItems[i].getData().getClass() == IrqSampleTypeWrapper.class) {
				selectedItems[i].setChecked(value);
				IrqSampleTypeWrapper wrapper = (IrqSampleTypeWrapper) selectedItems[i]
						.getData();
				if (value) {
					myGraph.irqLineChecked(wrapper);
				} else {
					myGraph.irqLineUnchecked(wrapper);

				}
			}

		}
		myGraph.updateIrqCountsInLegendsAsynch(IrqTraceGraph.TYPE_IRQ);
		myGraph.repaint();

	}

	/**
	 * Checks or unchecks all table items
	 * 
	 * @param value
	 *            true if all items are checked
	 */
	private void checkOrUncheckAllItems(boolean value) {
		// go thru all items from thread table and notify graph that threads are
		// checked or unchecked
		TableItem[] allItems = this.table.getItems();
		for (int i = 0; i < allItems.length; i++) {
			if (allItems[i].getData().getClass() == IrqSampleTypeWrapper.class) {
				allItems[i].setChecked(value);
				IrqSampleTypeWrapper wrapper = (IrqSampleTypeWrapper) allItems[i]
						.getData();
				if (value) {
					myGraph.irqLineChecked(wrapper);
				} else {
					myGraph.irqLineUnchecked(wrapper);

				}
			}

		}
		myGraph.updateIrqCountsInLegendsAsynch(IrqTraceGraph.TYPE_IRQ);
		myGraph.repaint();

	}

	/**
	 * Updates item data to table
	 * 
	 * @param setInput
	 *            true if table needs to be refreshed
	 */
	public void updateItemData(boolean setInput) {

		// get sample type wrappers from irq trace
		tableItemData = new Vector<IrqSampleTypeWrapper>();
		Hashtable<Long, IrqSampleTypeWrapper> irqLines = this.myGraph
				.getIrqTrace().getIrqTable();

		// create table item for each sample type wrapper
		Enumeration<Long> k = irqLines.keys();
		while (k.hasMoreElements()) {
			IrqSampleTypeWrapper threadValue = (IrqSampleTypeWrapper) irqLines
					.get(k.nextElement());
			tableItemData.add(threadValue);
		}

		// refresh the table, if needed
		if (setInput)
			refreshTableViewer();
	}

	/**
	 * Refreshes table viewer
	 */
	public void refreshTableViewer() {
		this.tableViewer.setInput(tableItemData);
		this.addColor();

	}

	/**
	 * @return table viewer
	 */
	public CheckboxTableViewer getTableViewer() {
		return this.tableViewer;
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
			IrqSampleTypeWrapper irqLine = (IrqSampleTypeWrapper) event
					.getElement();
			if (event.getChecked()) {
				myGraph.irqLineChecked(irqLine);
			} else {
				myGraph.irqLineUnchecked(irqLine);
			}
			myGraph.repaint();
			myGraph.updateIrqCountsInLegendsAsynch(IrqTraceGraph.TYPE_IRQ);

		}
	}

	/**
	 * Adds colors into irq line table
	 */
	public void addColor() {
		if (this.tableViewer == null)
			return;

		IrqSampleTypeWrapper pGeneric;
		TableItem[] items = this.table.getItems();
		for (int i = 0; i < items.length; i++) {
			if (items[i].getData().getClass() == IrqSampleTypeWrapper.class) {
				pGeneric = (IrqSampleTypeWrapper) items[i].getData();
				items[i].setBackground(COLOR_COLUMN_INDEX, new Color(parent
						.getDisplay(), pGeneric.rgb));
			}
		}

		table.redraw();
	}

	/**
	 * MouseListener for table
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
				getRecolorItem(contextMenu, Messages.IrqLineTable_4, table
						.getSelectionCount() > 0);

				contextMenu.setLocation(parent.toDisplay(e.x
						+ table.getLocation().x, e.y + table.getLocation().y));
				contextMenu.setVisible(true);

				table.setMenu(contextMenu);
			}
		}
	}

	/**
	 * ColumnselectionHandler for irq columns
	 */
	private class ColumnSelectionHandler extends SelectionAdapter {
		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.eclipse.swt.events.SelectionAdapter#widgetSelected(org.eclipse
		 * .swt.events.SelectionEvent)
		 */
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
	 * Sorter for irq table
	 */
	private class SharedSorter extends ViewerSorter {
		// last column sorted
		private int column = -1;

		/*
		 * decide on which column to sort by, and the sort ordering
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
				case COLUMN_ID_IRQ_LINE:
				case COLUMN_ID_IRQ_COUNT: {
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
		 * compare two items from a table column
		 */
		public int compare(Viewer viewer, Object e1, Object e2) {
			int returnCode = 0;

			IrqSampleTypeWrapper elem1 = (IrqSampleTypeWrapper) e1;
			IrqSampleTypeWrapper elem2 = (IrqSampleTypeWrapper) e2;

			// find the information for the two irq lines

			// compare based on the memory information
			switch (column) {
			case COLOR_COLUMN_INDEX:
				if (tableViewer.getChecked(e1) == true
						&& tableViewer.getChecked(e2) == false) {
					returnCode = -1;
				} else {
					returnCode = 1;
				}
				break;
			case COLUMN_ID_IRQ_LINE:
				returnCode = elem1.getPrototypeSample().getIrqL1Value() > elem2
						.getPrototypeSample().getIrqL1Value() ? 1 : -1;
				break;
			case COLUMN_ID_IRQ_COUNT:
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
	 * Action for opening color dialog for all selected irq lines.
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
						errorDialog.setText(Messages.IrqLineTable_5);
						errorDialog
								.setMessage(Messages.IrqLineTable_6);
						errorDialog.open();
					}
				}

			}

		}
		this.myGraph.repaint();
		myGraph.getIrqTrace().updateAllTableViewers();
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
