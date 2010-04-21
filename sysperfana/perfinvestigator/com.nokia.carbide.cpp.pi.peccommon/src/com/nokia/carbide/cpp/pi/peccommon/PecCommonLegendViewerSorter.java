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

package com.nokia.carbide.cpp.pi.peccommon;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;

/**
 * Sorter for the IPC legend
 */
public class PecCommonLegendViewerSorter extends ViewerComparator {
	protected int columnIdx;
	protected boolean sortUp;
	
	/**
	 * Constructor
	 */
	public PecCommonLegendViewerSorter() {
		super();
		sortUp = false;
		columnIdx = -1;
	}
	
	/**
	 * Sets the column to be sorted for subsequent sorts. This will also 
	 * reverse sort order. 
	 * @param columnIdx the column to sort on next
	 */
	public void setSortColumn(int columnIdx){
		if (this.columnIdx == columnIdx){
			sortUp = !sortUp;//change sort order	
		} else {
			this.columnIdx = columnIdx;
			sortUp = true; //default sort order		
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ViewerComparator#compare(org.eclipse.jface.viewers.Viewer, java.lang.Object, java.lang.Object)
	 */
	@Override
	public int compare(Viewer viewer, Object e1, Object e2) {
		PecCommonLegendElement le1 = (PecCommonLegendElement)e1;
		PecCommonLegendElement le2 = (PecCommonLegendElement)e2;
		int ret = 0;

		switch (columnIdx) {
		case PecCommonLegend.COLUMN_SHORT_TITLE:
			ret = le1.getCharacter() - le2.getCharacter();
			break;
		case PecCommonLegend.COLUMN_NAME:
			ret = le1.getName().compareTo(le2.getName()); 
			break;
		case PecCommonLegend.COLUMN_AVERAGE:
			float a1 = le1.getSum() / (le1.getCnt() > 0 ? le1.getCnt() : 1); 
			float a2 = le2.getSum() / (le2.getCnt() > 0 ? le2.getCnt() : 1); 
			ret = Float.compare(a1, a2);
			break;
		case PecCommonLegend.COLUMN_SUM:
			ret = (int)(le1.getSum() - le2.getSum());
			break;
		case PecCommonLegend.COLUMN_MIN:
			ret = le1.getMin() - le2.getMin(); 
			break;
		case PecCommonLegend.COLUMN_MAX:
			ret = le1.getMax() - le2.getMax(); 
			break;
		default:
			throw new IllegalStateException("Unknown column index in legend"); //$NON-NLS-1$
		}
		
		return sortUp? ret : (ret * -1);
		
	}

}
