/*
 * Copyright (c) 2009 Nokia Corporation and/or its subsidiary(-ies). 
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

package com.nokia.carbide.cpp.internal.pi.plugin.model;

import java.util.ArrayList;
import java.util.Hashtable;

import org.eclipse.jface.action.MenuManager;

public interface IReportable extends ITrace
{
	/*
	 * Hashtable key should contain an Object (e.g. an Integer representing thread id) and value a Vector which contains Strings
	 */
	public static final Boolean SORT_BY_NAME   = Boolean.TRUE;
	public static final Boolean SORT_BY_NUMBER = Boolean.FALSE;
	
	public ArrayList<String>  getColumnNames(); // table column names
	public ArrayList<Boolean> getColumnSortTypes(); // This gives feedback to the system whether column data strings should be sorted by name or the first number they may contain. 
										// Boolean values SORT_BY_NAME and SORT_BY_NUMBER are meant to be used to indicate this information.
	public Hashtable<Integer,Object> getSummaryTable(double startTime, double endTime);
	public String getActiveInfo(Object key, double startTime, double endTime);
	public String getGeneralInfo(); // this is reserved for future implementation and has no functionality at the moment

	//	this menu allows plugin to control reporting within the report generator plugin's frame
	//	public JMenu getReportMenu(Component parentComponent);
	public MenuManager getReportGeneratorManager();
}
