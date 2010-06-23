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



package com.nokia.s60tools.traceanalyser.resources;

/**
 * IDs for context sensitive help.
 * @see contexts.xml -file IDs links to <code> <context id="<ID>"> </code>
 */
public class HelpContextIDs {

	/**
	 * The plug-in ID. 
	 */	 
	private static final String TRACE_ANALYSER_PROJECT_PLUGIN_ID = "com.nokia.s60tools.traceanalyser.help";
	
	/**
	 * ID to Trace Analyser Help TOC
	 */

	
	public static final String TRACE_ANALYSER_MAIN_VIEW = 
    	TRACE_ANALYSER_PROJECT_PLUGIN_ID +".TRACE_ANALYSER_MAIN_VIEW";
	
	public static final String TRACE_ANALYSER_HISTORY_VIEW= 
    	TRACE_ANALYSER_PROJECT_PLUGIN_ID +".TRACE_ANALYSER_HISTORY_VIEW";

	public static final String TRACE_ANALYSER_FAIL_LOG =
		TRACE_ANALYSER_PROJECT_PLUGIN_ID +".TRACE_ANALYSER_FAIL_LOG";

	public static final String TRACE_ANALYSER_RULE_EDITOR =
		TRACE_ANALYSER_PROJECT_PLUGIN_ID +".TRACE_ANALYSER_RULE_EDITOR";

	public static final String TRACE_ANALYSER_TRACE_SELECTION_DIALOG =
		TRACE_ANALYSER_PROJECT_PLUGIN_ID +".TRACE_ANALYSER_TRACE_SELECTION_DIALOG";

}
