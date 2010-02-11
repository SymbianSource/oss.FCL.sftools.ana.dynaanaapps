/*
* Copyright (c) 2008-2009 Nokia Corporation and/or its subsidiary(-ies).
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
* Description:  Definitions for the class AnalyzeToolHelpContextIDs
*
*/

package com.nokia.s60tools.analyzetool;

/**
 * IDs for context sensitive help.
 * @author kihe
 */
public class AnalyzeToolHelpContextIDs {

   /**
     * The AnalyzeTool help plug-in ID.
     */
    private static final String ANALYZE_TOOL_HELP_PROJECT_PLUGIN_ID =
                                        "com.nokia.s60tools.analyzetool.help"; //$NON-NLS-1$

    /**
     * AnalyzeTool view memory leaks page ID
     */
    public static final String ANALYZE_TOOL_VIEW_MEM_LEAKS=
    	ANALYZE_TOOL_HELP_PROJECT_PLUGIN_ID +".ANALYZETOOL_VIEW_MEM_LEAKS";

    /**
     * AnalyzeTool main page ID
     */
    public static final String ANALYZE_MAIN=
    	ANALYZE_TOOL_HELP_PROJECT_PLUGIN_ID +".ANALYZETOOL_MAIN";
    /**
     * AnalyzeTool graph page ID
     */
    public static final String ANALYZE_GRAPH=
    	ANALYZE_TOOL_HELP_PROJECT_PLUGIN_ID +".ANALYZETOOL_GRAPH";

}
