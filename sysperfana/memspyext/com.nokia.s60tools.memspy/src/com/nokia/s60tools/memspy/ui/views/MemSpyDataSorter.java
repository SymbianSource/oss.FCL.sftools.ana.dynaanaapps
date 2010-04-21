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


package com.nokia.s60tools.memspy.ui.views;

import org.eclipse.jface.viewers.Viewer;

import com.nokia.s60tools.memspy.model.MemSpyFileBundle;
import com.nokia.s60tools.ui.S60ToolsViewerSorter;

/**
 * class MemSpyDataSorter
 * Data sorter for MemSpy's main view.
 */

public class MemSpyDataSorter extends S60ToolsViewerSorter {

	/**
	 * Import function data is sorted by file type
	 */
	public static final int FILE_TYPE = 1;
	/**
	 * Import function data is sorted by file name
	 */
	public static final int FILENAME = 2;
	
	/**
	 * Import function data is sorted by time
	 */
	public static final int TIME = 3;

	/**
	 * Constructor
	 */
	public MemSpyDataSorter() {
		super();		
		// By default set sorting to time
		setSortCriteria(TIME);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ViewerSorter#compare(org.eclipse.jface.viewers.Viewer, java.lang.Object, java.lang.Object)
	 */
	public int compare(Viewer viewer, Object e1, Object e2) {
		
		
		// By default comparison does not do any ordering
		int comparisonResult = 0;
		
		MemSpyFileBundle f1 = (MemSpyFileBundle) e1;
		MemSpyFileBundle f2 = (MemSpyFileBundle) e2;
		
		switch (sortCriteria) {
		
		case FILE_TYPE:
			comparisonResult = f1.getFileType().compareToIgnoreCase( f2.getFileType() );
			// when type is same, sort alphabetically
			if( comparisonResult == 0 ){
				comparisonResult = numericSort( f2.getTimeAsLong(), f1.getTimeAsLong() );
			}
			break;
		case FILENAME:
			comparisonResult = f1.getFileName().compareToIgnoreCase( f2.getFileName() );
			break;
		case TIME:
			comparisonResult = numericSort( f2.getTimeAsLong(), f1.getTimeAsLong() );
			if( comparisonResult == 0 ){
				comparisonResult = numericSort( f2.getCycleNumberFromFileName(), f1.getCycleNumberFromFileName() );
			}
			
			break;
		case CRITERIA_NO_SORT:
			comparisonResult = f2.getFileType().compareToIgnoreCase( f1.getFileType() );
			// when type is same, sort by file time
			if( comparisonResult == 0 ){
				comparisonResult = numericSort( f2.getTimeAsLong(), f1.getTimeAsLong() );
			}
			break;
		default:
			break;
		}
		
		return comparisonResult;
	}

}
