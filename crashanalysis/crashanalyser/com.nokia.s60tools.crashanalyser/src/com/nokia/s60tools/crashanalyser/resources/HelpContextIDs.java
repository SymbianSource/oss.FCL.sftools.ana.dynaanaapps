/*
* Copyright (c) 2008 Nokia Corporation and/or its subsidiary(-ies). 
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

package com.nokia.s60tools.crashanalyser.resources;

/**
 * IDs for context sensitive help.
 * @see contexts.xml -file IDs links to <code> <context id="<ID>"> </code>
 */
public class HelpContextIDs {

	/**
	 * The plug-in ID. 
	 */	 
	private static final String CRASH_ANALYSER_HELP_PROJECT_PLUGIN_ID = "com.nokia.s60tools.crashanalyser.help"; //$NON-NLS-1$

	
	/**
	 * ID to CrashAnalyser Help TOC
	 */
    public static final String CRASH_ANALYSER_HELP = 
    	CRASH_ANALYSER_HELP_PROJECT_PLUGIN_ID +".CRASH_ANALYSER_HELP_TOC"; //$NON-NLS-1$

    public static final String CRASH_ANALYSER_HELP_PREFERENCES = 
    	CRASH_ANALYSER_HELP_PROJECT_PLUGIN_ID +".CRASH_ANALYSER_HELP_PREFERENCES"; //$NON-NLS-1$

    public static final String CRASH_ANALYSER_HELP_MAIN_VIEW = 
    	CRASH_ANALYSER_HELP_PROJECT_PLUGIN_ID +".CRASH_ANALYSER_HELP_MAIN_VIEW"; //$NON-NLS-1$

    public static final String CRASH_ANALYSER_HELP_IMPORT_CRASH_FILES = 
    	CRASH_ANALYSER_HELP_PROJECT_PLUGIN_ID +".CRASH_ANALYSER_HELP_IMPORT"; //$NON-NLS-1$

    public static final String CRASH_ANALYSER_HELP_DECODE_CRASH_FILES = 
    	CRASH_ANALYSER_HELP_PROJECT_PLUGIN_ID +".CRASH_ANALYSER_HELP_DECODE"; //$NON-NLS-1$

    public static final String CRASH_ANALYSER_HELP_ERROR_LIBRARY = 
    	CRASH_ANALYSER_HELP_PROJECT_PLUGIN_ID +".CRASH_ANALYSER_HELP_ERROR_LIBRARY"; //$NON-NLS-1$

    public static final String CRASH_ANALYSER_HELP_CRASH_VISUALISER = 
    	CRASH_ANALYSER_HELP_PROJECT_PLUGIN_ID +".CRASH_ANALYSER_HELP_ANALYSE_CRASH"; //$NON-NLS-1$
}
