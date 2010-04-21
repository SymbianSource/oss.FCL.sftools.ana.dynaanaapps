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

import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;

/**
 * Filter class for the all tables 
 *
 */
public class TableTextFilter extends ViewerFilter {

	private String filterText = "";
	private int columnIndex = 0;
	private int filterTypeIndex = 0;
	
	/**
	 * Constructor
	 * @param columnIndex
	 */
	public TableTextFilter(int columnIndex) {
		super();
		this.columnIndex = columnIndex;
	}

	/**
	 * Get filter text
	 * @return filter text
	 */
	public String getFilterText() {
		return filterText;
	}

	/**
	 * Get column index
	 * @return column index
	 */
	public int getColumnIndex() {
		return columnIndex;
	}

	/**
	 * Set column index
	 * @param columnIndex
	 */
	public void setColumnIndex(int columnIndex) {
		this.columnIndex = columnIndex;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ViewerFilter#select(org.eclipse.jface.viewers.Viewer, java.lang.Object, java.lang.Object)
	 */
	@Override
	public boolean select(Viewer viewer, Object parentElement, Object element) {
		if (filterText.length() > 0) {
			String name = ((ITableLabelProvider)((TableViewer) viewer).getLabelProvider()).getColumnText(element, columnIndex);
			if(filterTypeIndex == 0){
				return (name.toLowerCase().startsWith(this.filterText.toLowerCase()));				
			}
			else{
				return (name.toLowerCase().indexOf(this.filterText.toLowerCase()) > -1);				
			}
		} else
			return true;
	}

	/**
	 * Set filter text
	 * @param filterText
	 */
	public void setFilterText(String filterText) {
		this.filterText = filterText;
	}
	
	/**
	 * Set filter type index
	 * @param index
	 */
	public void setFilterTypeIndex(int index) {
		this.filterTypeIndex = index;
	}

}
