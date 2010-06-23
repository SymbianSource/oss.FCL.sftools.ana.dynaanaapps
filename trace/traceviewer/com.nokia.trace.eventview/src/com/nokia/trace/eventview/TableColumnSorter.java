/*
 * Copyright (c) 2007-2010 Nokia Corporation and/or its subsidiary(-ies). 
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
 * Table column sorter
 *
 */
package com.nokia.trace.eventview;

import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.TableColumn;

/**
 * Table column sorter class
 * 
 */
public abstract class TableColumnSorter extends ViewerComparator {

	/**
	 * Ascending sorting
	 */
	public static final int ASC = 1;

	/**
	 * No sorting
	 */
	public static final int NONE = 0;

	/**
	 * Descending sorting
	 */
	public static final int DESC = -1;

	/**
	 * Current direction
	 */
	private int direction = 0;

	/**
	 * Column for this sorter
	 */
	private TableColumn column;

	/**
	 * Table viewer for this column
	 */
	private TableViewer viewer;

	/**
	 * Constructor
	 * 
	 * @param viewer
	 *            table viewer
	 * @param column
	 *            column
	 */
	public TableColumnSorter(final TableViewer viewer, TableColumn column) {
		this.column = column;
		this.viewer = viewer;
		this.column.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				if (viewer.getComparator() != null) {
					if (viewer.getComparator() == TableColumnSorter.this) {
						int tdirection = TableColumnSorter.this.direction;

						if (tdirection == ASC) {
							setSorter(TableColumnSorter.this, DESC);
						} else if (tdirection == DESC) {
							setSorter(TableColumnSorter.this, NONE);
						}
					} else {
						setSorter(TableColumnSorter.this, ASC);
					}
				} else {
					setSorter(TableColumnSorter.this, ASC);
				}
			}
		});
	}

	/**
	 * Sets sorter
	 * 
	 * @param sorter
	 *            the sorter
	 * @param direction
	 *            direction
	 */
	public void setSorter(TableColumnSorter sorter, int direction) {
		if (direction == NONE) {
			column.getParent().setSortColumn(null);
			column.getParent().setSortDirection(SWT.NONE);
			viewer.setComparator(null);
		} else {
			column.getParent().setSortColumn(column);
			sorter.direction = direction;

			if (direction == ASC) {
				column.getParent().setSortDirection(SWT.DOWN);
			} else {
				column.getParent().setSortDirection(SWT.UP);
			}

			if (viewer.getComparator() == sorter) {
				viewer.refresh();
			} else {
				viewer.setComparator(sorter);
			}

		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jface.viewers.ViewerComparator#compare(org.eclipse.jface.
	 * viewers.Viewer, java.lang.Object, java.lang.Object)
	 */
	@Override
	public int compare(Viewer viewer, Object o1, Object o2) {
		return direction * doCompare(viewer, o1, o2);
	}

	/**
	 * Compare method
	 * 
	 * @param TableViewer
	 *            table viewer
	 * @param o1
	 *            object 1
	 * @param o2
	 *            object 2
	 * @return less than 0 if o1 is smaller than o2, 0 if equal, more than 0 if
	 *         o1 is bigger than o2
	 */
	protected abstract int doCompare(Viewer TableViewer, Object o1, Object o2);
}
