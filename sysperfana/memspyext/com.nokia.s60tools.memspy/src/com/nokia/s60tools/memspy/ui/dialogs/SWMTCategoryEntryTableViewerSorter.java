/*
* Copyright (c) 2006 Nokia Corporation and/or its subsidiary(-ies). 
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
 
package com.nokia.s60tools.memspy.ui.dialogs;

import org.eclipse.jface.viewers.Viewer;

import com.nokia.s60tools.memspy.util.MemSpyConsole;
import com.nokia.s60tools.ui.S60ToolsViewerSorter;

/**
 * Sorter implementation for SWMT category entry data.
 */
public class SWMTCategoryEntryTableViewerSorter extends S60ToolsViewerSorter {

	//
	// Sorting criteria constants
	//
	public static final int CRITERIA_NAME = 1;

	/**
	 * Constructor
	 */
	public SWMTCategoryEntryTableViewerSorter() {
		super();		
		// By default we are not sorting the information
		setSortCriteria(CRITERIA_NO_SORT);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ViewerSorter#compare(org.eclipse.jface.viewers.Viewer, java.lang.Object, java.lang.Object)
	 */
	public int compare(Viewer viewer, Object e1, Object e2) {
		
		// By default comparison does not do any ordering
		int compRes = 0;
		
		SWMTCategoryEntry entry1 = (SWMTCategoryEntry) e1;
		SWMTCategoryEntry entry2 = (SWMTCategoryEntry) e2;
		
		switch (sortCriteria) {

		case CRITERIA_NAME:
			compRes = alphabeticSort(entry1.getCategoryName(), entry2.getCategoryName());
			break;
			
		case CRITERIA_NO_SORT:
			// No sorting criteria defined.
			break;

		default:			
			MemSpyConsole.getInstance()
					.println(
							"Unexpected sort criteria for a catecory entriey encountered: " + sortCriteria, //$NON-NLS-1$
							MemSpyConsole.MSG_ERROR);
			break;
		}
				
		return compRes;
	}

}
