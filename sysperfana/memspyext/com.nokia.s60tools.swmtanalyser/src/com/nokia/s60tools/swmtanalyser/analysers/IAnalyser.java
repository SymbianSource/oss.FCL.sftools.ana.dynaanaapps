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
package com.nokia.s60tools.swmtanalyser.analysers;

import com.nokia.s60tools.swmtanalyser.data.ParsedData;

/**
 * 
 * This Interface must be implemented by all swmt analysers
 */
public interface IAnalyser {


	/**
	 * Analysis is started from this method. And all analysis results 
	 * are added to a list.
	 * 
	 * @param logData data objects of log file
	 */
	public void analyse(ParsedData logData);
	 
	
	/** 
	 * The method returns the array of issues.
	 * @return results
	 */	 
	public Object [] getResults();
	
	/** 
	 * The method returns the array of children for a given issue.
	 * @param parent must be {@link ResultsParentNodes}
	 * @return childrens
	 */ 	
	public Object [] getChildren (Object parent);
}
