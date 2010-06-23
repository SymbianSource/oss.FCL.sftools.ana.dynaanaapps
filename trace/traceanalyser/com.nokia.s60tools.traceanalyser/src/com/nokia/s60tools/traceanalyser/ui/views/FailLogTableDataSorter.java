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


package com.nokia.s60tools.traceanalyser.ui.views;
import org.eclipse.jface.viewers.Viewer;

import com.nokia.s60tools.traceanalyser.export.RuleEvent;
import com.nokia.s60tools.ui.S60ToolsViewerSorter;


/**
 * class FailLogTableDataSorter
 * Data sorter for Trace Analyser's fail log view.
 */

public class FailLogTableDataSorter extends S60ToolsViewerSorter {

	/**
	 * Numbers of columns that can be sorted.
	 */
	public static final int TIME = 1;
	public static final int RULE = 2;
	public static final int VIOLATION = 3;
	public static final int LIMIT = 4;
	
	
	public FailLogTableDataSorter() {
		super();		
		// By default set sort criteria to time
		setSortCriteria(TIME);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ViewerSorter#compare(org.eclipse.jface.viewers.Viewer, java.lang.Object, java.lang.Object)
	 */
	public int compare(Viewer viewer, Object e1, Object e2) {
		
		
		// By default comparison does not do any ordering
		int comparisonResult = 0;
		
		RuleEvent f1 = (RuleEvent) e1;
		RuleEvent f2 = (RuleEvent) e2;
		
		switch (sortCriteria) {
		

		case TIME:
		case -1:	
			if(f1.getTime().before(f2.getTime())){
				comparisonResult = 1;
			}
			else{
				comparisonResult = -1;
			}	
			break;
			
		case RULE:
			comparisonResult = f1.getRuleName().compareToIgnoreCase(f2.getRuleName());

			break;
			
		case VIOLATION:
			if(f1.getViolation() >  f2.getViolation()){
				comparisonResult = 1;
			}
			else{
				comparisonResult = -1;
			}
			
			break;
			
		case LIMIT:	
			if(f1.getLimit() >  f2.getLimit()){
				comparisonResult = 1;
			}
			else{
				comparisonResult = -1;
			}
			break;
			
		default:
			break;
		}
		
		return comparisonResult;
	
	}
	


}
