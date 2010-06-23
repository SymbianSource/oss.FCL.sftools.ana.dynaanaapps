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
import org.eclipse.jface.viewers.ViewerSorter;

import com.nokia.s60tools.traceanalyser.containers.RuleInformation;


/**
 * class RuleTableDataSorter
 * Data sorter for Trace Analyser's rule view.
 */

public class RuleTableDataSorter extends ViewerSorter {

	/**
	 * Import function data is sorted by file name
	 */
	public static final int RULE_NAME = 2;
	/**
	 * 
	 */
	public static final int PASS = 3;
	public static final int FAIL = 4;
	public static final int PASSPERCENT = 5;
	public static final int MIN = 6;
	public static final int MAX = 7;
	public static final int AVG = 8;
	public static final int MED = 9;

	private int sortCriteria;
	
	public RuleTableDataSorter(int sortCriteria) {
		super();		
		// By default set sort criterie
		this.sortCriteria = sortCriteria;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ViewerSorter#compare(org.eclipse.jface.viewers.Viewer, java.lang.Object, java.lang.Object)
	 */
	public int compare(Viewer viewer, Object e1, Object e2) {
		
		
		// By default comparison does not do any ordering
		int comparisonResult = 0;
		
		RuleInformation f1 = (RuleInformation) e1;
		RuleInformation f2 = (RuleInformation) e2;
		
		switch (sortCriteria) {
		
		case RULE_NAME:
			comparisonResult = f1.getRule().getName().compareToIgnoreCase(f2.getRule().getName());
			break;
		case PASS:
			if(f1.getPass() < f2.getPass()){
				comparisonResult = 1;
			}
			else{
				comparisonResult = -1;

			}
			break;
		case FAIL:
			if(f1.getFail() < f2.getFail()){
				comparisonResult = 1;
			}
			else{
				comparisonResult = -1;

			}
			break;

		case PASSPERCENT:
			if(f1.getPassPercent() < f2.getPassPercent()){
				comparisonResult = 1;
			}
			else{
				comparisonResult = -1;
 			}
			break;

		case MIN:
			if(f1.getMin() < f2.getMin()){
				comparisonResult = 1;
			}
			else{
				comparisonResult = -1;

			}
			break;

		case MAX:
			if(f1.getMax() < f2.getMax()){
				comparisonResult = 1;
			}
			else{
				comparisonResult = -1;

			}
			break;

		case MED:
			if(f1.getMed() < f2.getMed()){
				comparisonResult = 1;
			}
			else{
				comparisonResult = -1;

			}
			break;
		case AVG:
			if(f1.getAvg() < f2.getAvg()){
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
