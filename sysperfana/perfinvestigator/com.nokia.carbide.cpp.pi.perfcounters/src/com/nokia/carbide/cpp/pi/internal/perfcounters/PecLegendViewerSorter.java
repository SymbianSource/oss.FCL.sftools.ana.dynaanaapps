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

import org.eclipse.jface.viewers.Viewer;

import com.nokia.carbide.cpp.pi.peccommon.PecCommonLegendViewerSorter;

/**
 * Sorter for the PEC legend
 */
public class PecLegendViewerSorter extends PecCommonLegendViewerSorter {
	
	/**
	 * Constructor
	 */
	public PecLegendViewerSorter() {
		super();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ViewerComparator#compare(org.eclipse.jface.viewers.Viewer, java.lang.Object, java.lang.Object)
	 */
	@Override
	public int compare(Viewer viewer, Object e1, Object e2) {
		PecLegendElement le1 = (PecLegendElement)e1;
		PecLegendElement le2 = (PecLegendElement)e2;
		int ret = 0;

		switch (columnIdx) {
		case PecLegend.COLUMN_PER_A:
			ret = Float.compare(le1.getxOverY(0), le2.getxOverY(0));
			break;
		case PecLegend.COLUMN_PER_B:
			ret = Float.compare(le1.getxOverY(1), le2.getxOverY(1));
			break;
		case PecLegend.COLUMN_PER_C:
			ret = Float.compare(le1.getxOverY(2), le2.getxOverY(2));
			break;
		default:
			return super.compare(viewer, e1, e2);
		}
		
		return sortUp? ret : (ret * -1);
		
	}

}
