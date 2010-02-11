/*
 * Copyright (c) 2008-2009 Nokia Corporation and/or its subsidiary(-ies).
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
 * Description:  Definitions for the class StatisticView
 *
 */

package com.nokia.s60tools.analyzetool.ui.statistic;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Iterator;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

import java.util.Comparator;
import java.util.Collections;



/**
 * Sorts statistic view contents
 * @author kihe
 *
 */
public class ATComparator implements Listener, Comparator<String[]> {

	/** Used table*/
	private final Table memoryTable;

	/** Column index to sort */
	private final int columnIndex;

	/** Sort only integer values */
	private final boolean intComparator;

	/** Sort order */
	boolean ascending;

	/** Parent class reference */
	StatisticView parent;

	/**
	 * Constructor
	 * @param view Parent view reference
	 * @param table Used table
	 * @param clnIndex Used column index
	 * @param compInteger Compare only for integer values
	 */
	public ATComparator(StatisticView view, Table table, int clnIndex, boolean compInteger) {
		parent = view;
		memoryTable = table;
		columnIndex = clnIndex;
		intComparator = compInteger;
		ascending = true;
	}


	/*
	 * (non-Javadoc)
	 * @see org.eclipse.swt.widgets.Listener#handleEvent(org.eclipse.swt.widgets.Event)
	 */

	public void handleEvent(Event e) {

		//get existing items
        TableItem[] items = memoryTable.getItems();


        //add existing items to list => this helps item sorting
        AbstractList<String[]> list = new ArrayList<String[]>();
        for(int i=0; i<items.length; i++)
        {
        	TableItem oneItem = memoryTable.getItem(i);

			String[] values = { oneItem.getText(0),oneItem.getText(1),
					oneItem.getText(2),oneItem.getText(3),
					oneItem.getText(4),oneItem.getText(5),
					oneItem.getText(6) };
			list.add(values);
        }

        //clear existing items
        memoryTable.clearAll();

        //sort items, use compare method provide by this class
        Collections.sort(list, this);

        //thru sorted items
        Iterator<String[]> iterList = list.iterator();
        memoryTable.setItemCount(list.size());
        int index = 0;
        while(iterList.hasNext())
        {
        	String[] value = iterList.next();
        	TableItem item = memoryTable.getItem(index);
        	item.setText(value);
        	index++;
        }

        TableColumn currColumn = (TableColumn) e.widget;
        memoryTable.setSortColumn(currColumn);

        //update sort order
        if(ascending) {
        	ascending = false;
        	memoryTable.setSortDirection(SWT.DOWN);
        }
        else {
           	ascending = true;
           	memoryTable.setSortDirection(SWT.UP);
        }
        doPackColumns();
	}

	/**
	 * Fit columns size to match column text size.
	 */
	public void doPackColumns()
	{
		TableColumn[] columns = memoryTable.getColumns();
		for (int i = 0; i < columns.length; i++) {
			columns[i].pack();
		}
	}
	/*
	 * (non-Javadoc)
	 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
	 */
	public int compare(String[] o1, String[] o2) {
		int result = 0;

		//if compare only integer values
		if( intComparator )
		{
			//get values
			int value1 = Integer.parseInt(o1[columnIndex]);
			int value2 = Integer.parseInt(o2[columnIndex]);
			result = (value1 < value2) ? -1 : 1;
		}
		else {
			//get values
			Long time1 = parent.getTimeFromCache(o1[columnIndex]);
			Long time2 = parent.getTimeFromCache(o2[columnIndex]);

			if( time1 == null || time2 == null ) {
				result = -1;
			}else {
				result = (time1 < time2) ? -1 : 1;
			}

		}
		//if sort order is set ascending change result to negative
		if( ascending ){
			result = -result;
		}
		return result;
	}
}
