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
* Help context constants
*
*/
package com.nokia.tracebuilder.engine;

/**
 * Help context constants
 * 
 */
public interface TraceBuilderHelp {

	/**
	 * Help context ID base
	 */
	public String HELP_CONTEXT_BASE = "com.nokia.tracebuilder.help."; //$NON-NLS-1$

	/**
	 * Help context for tree viewer
	 */
	public String TREE_VIEWER_HELP_CONTEXT = "TREE_VIEWER"; //$NON-NLS-1$

	/**
	 * Help context for add trace action
	 */
	public String ADD_TRACE_HELP_CONTEXT = "ADD_TRACE"; //$NON-NLS-1$

	/**
	 * Help context for add parameter action
	 */
	public String ADD_PARAMETER_HELP_CONTEXT = "ADD_PARAMETER"; //$NON-NLS-1$

	/**
	 * Help context for add enum action
	 */
	public String ADD_ENUM_HELP_CONTEXT = "ADD_ENUM"; //$NON-NLS-1$

	/**
	 * Help context for edit properties action. Note that there are more
	 * specific contexts for edit group, edit trace etc.
	 */
	public String EDIT_PROPERTIES_HELP_CONTEXT = "EDIT_PROPERTIES"; //$NON-NLS-1$

	/**
	 * Help context for delete action
	 */
	public String DELETE_OBJECT_HELP_CONTEXT = "DELETE_OBJECT"; //$NON-NLS-1$

	/**
	 * Help context for delete traces action
	 */
	public String DELETE_TRACES_HELP_CONTEXT = "DELETE_TRACES"; //$NON-NLS-1$

	/**
	 * Help context for insert trace action
	 */
	public String INSERT_TRACE_HELP_CONTEXT = "INSERT_TRACE"; //$NON-NLS-1$

	/**
	 * Help context for remove trace from source action
	 */
	public String REMOVE_TRACE_HELP_CONTEXT = "REMOVE_TRACE"; //$NON-NLS-1$

	/**
	 * Help context for remove unrelated traces action
	 */
	public String REMOVE_UNRELATED_HELP_CONTEXT = "REMOVE_UNRELATED"; //$NON-NLS-1$

	/**
	 * Help context for convert trace action
	 */
	public String CONVERT_TRACE_HELP_CONTEXT = "CONVERT_TRACE"; //$NON-NLS-1$

	/**
	 * Help context for instrumenter
	 */
	public String INSTRUMENTER_HELP_CONTEXT = "INSTRUMENTER"; //$NON-NLS-1$

	/**
	 * Help context for parse enum action
	 */
	public String PARSE_ENUM_HELP_CONTEXT = "PARSE_ENUM"; //$NON-NLS-1$

	/**
	 * Help context for general preferences
	 */
	public String GENERAL_PREFERENCES_CONTEXT = "GENERAL_PREFERENCES"; //$NON-NLS-1$

	/**
	 * Help context for select component dialog
	 */
	public String SELECT_COMPONENT_HELP_CONTEXT = "SELECT_COMPONENT"; //$NON-NLS-1$

}
