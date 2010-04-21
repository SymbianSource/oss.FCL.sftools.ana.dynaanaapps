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

package com.nokia.carbide.cpp.pi.internal.perfcounters;

import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

import com.nokia.carbide.cpp.pi.peccommon.PecCommonLegend;
import com.nokia.carbide.cpp.pi.peccommon.PecCommonLegendLabelProvider;
import com.nokia.carbide.cpp.pi.peccommon.PecCommonLegendViewerSorter;

/**
 * Manages the legend for Performance Counter traces. 
 */
public class PecLegend extends PecCommonLegend{
	
	//the first few columns are in PecCommonLegend
	
	/** column containing average value divided by column A average value */
	public static final int COLUMN_PER_A = 6;
	/** column containing average value divided by column B average value */
	public static final int COLUMN_PER_B = 7;
	/** column containing average value divided by column C average value */
	public static final int COLUMN_PER_C = 8;

	
	/**
	 * Constructor
	 * @param graph the graph that this legend belongs to 
	 * @param parent the parent composite
	 * @param title Title for the legend
	 * @param trace The model containing the samples
	 */
	public PecLegend(final PecTraceGraph graph, Composite parent, String title, PecTrace trace){
		super(graph,  parent, title, trace);
	}
	

	/**
	 * Creates the LabelProvider for the legend table
	 * @return
	 */
	@Override
	protected PecCommonLegendLabelProvider createLabelProvider() {
		return new PecLegendLabelProvider();
	}

	/**
	 * Creates the sorter used for the legend view. Sub classes may override this
	 * @return PecCommonLegendViewerSorter
	 */
	@Override
	protected PecCommonLegendViewerSorter createLegendSorter(){
		return new PecLegendViewerSorter();
	}
	
	@Override
	protected void createColumns(final TableViewer aViewer, final PecCommonLegendViewerSorter columnSorter)
 {
		super.createColumns(aViewer, columnSorter);
		
		final Table table = aViewer.getTable();

		TableColumn column = new TableColumn(table, SWT.RIGHT);
		column.setText(Messages.PecLegend_6);
		column.setWidth(100);
		column.setMoveable(true);
		column.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				columnSorter.setSortColumn(COLUMN_PER_A);
				aViewer.refresh();
			}
		});

		column = new TableColumn(table, SWT.RIGHT);
		column.setText(Messages.PecLegend_7);
		column.setWidth(100);
		column.setMoveable(true);
		column.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				columnSorter.setSortColumn(COLUMN_PER_B);
				aViewer.refresh();
			}
		});

		column = new TableColumn(table, SWT.RIGHT);
		column.setText(Messages.PecLegend_8);
		column.setWidth(100);
		column.setMoveable(true);
		column.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				columnSorter.setSortColumn(COLUMN_PER_C);
				aViewer.refresh();
			}
		});
	}

}
