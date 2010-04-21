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



package com.nokia.s60tools.memspy.resources;

/**
 * IDs for context sensitive help.
 * @see contexts.xml -file IDs links to <code> <context id="<ID>"> </code>
 */
public class HelpContextIDs {

	/**
	 * The plug-in ID. 
	 */	 
	private static final String MEMSPY_HELP_PROJECT_PLUGIN_ID = "com.nokia.s60tools.memspy.help";
	
	//
	// ID's to MemSpy Help TOC
	//

	/**
	 * Id to Importing and analyzing heap dump files
	 */
	public static final String MEMSPY_IMPORT_HEAP = 
    	MEMSPY_HELP_PROJECT_PLUGIN_ID +".MEMSPY_HELP_IMPORT_HEAP"; 
	
	/**
	 * Id to MemSpy Main View
	 */
	public static final String MEMSPY_MAIN_VIEW = 
    	MEMSPY_HELP_PROJECT_PLUGIN_ID +".MEMSPY_HELP_MAIN_VIEW";
	
	/**
	 * Id to MemSpy Tasks
	 */
	public static final String MEMSPY_SELECT_ACTION = 
    	MEMSPY_HELP_PROJECT_PLUGIN_ID +".MEMSPY_HELP_IMPORT_SELECT_ACTION";

	/**
	 * Id to Symbol and Map files
	 */
	public static final String MEMSPY_IMPORT_SYMBOLS =
		MEMSPY_HELP_PROJECT_PLUGIN_ID +".MEMSPY_HELP_IMPORT_SYMBOLS";

	/**
	 * Id to Importing and comparing two heap dump files
	 */
	public static final String MEMSPY_IMPORT_COMPARE =
		MEMSPY_HELP_PROJECT_PLUGIN_ID +".MEMSPY_HELP_IMPORT_COMPARE";

	/**
	 * Id to Importing and analyzing System Wide Memory Tracking log files
	 */
	public static final String MEMSPY_IMPORT_SWMT =
		MEMSPY_HELP_PROJECT_PLUGIN_ID +".MEMSPY_HELP_IMPORT_SWMT";
	
	/**
	 * Id to Setting System Wide Memory Tracking tracked categories
	 */
	public static final String MEMSPY_IMPORT_SWMT_CATEGORIES_DIALOG =
		MEMSPY_HELP_PROJECT_PLUGIN_ID +".MEMSPY_HELP_IMPORT_SWMT_CATEGORIES_DIALOG";

	/**
	 * Id to Configuring connection settings
	 */
	public static final String MEMSPY_IMPORT_CONNECTION_SETTINGS =
		MEMSPY_HELP_PROJECT_PLUGIN_ID +".MEMSPY_HELP_IMPORT_CONNECTION_SETTINGS";

}
