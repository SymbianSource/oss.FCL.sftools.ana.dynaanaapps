/*
 * Copyright (c) 2007 Nokia Corporation and/or its subsidiary(-ies). 
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
 * Action ID definitions
 *
 */
package com.nokia.tracebuilder.action;

/**
 * Action ID definitions
 * 
 */
interface ActionIDs {

	/**
	 * Base for action IDs
	 */
	String ACTION_ID_BASE = "com.nokia.tracebuilder.command."; //$NON-NLS-1$

	/**
	 * Action icons directory
	 */
	String ICONS_DIRECTORY = "icons/actions/"; //$NON-NLS-1$

	/**
	 * GIF file
	 */
	String GIF = ".gif"; //$NON-NLS-1$

	/**
	 * Action ID for Add Trace
	 */
	String ADD_TRACE_ACTION_ID = "add_trace"; //$NON-NLS-1$

	/**
	 * Action ID for Add Parameter
	 */
	String ADD_PARAMETER_ACTION_ID = "add_parameter"; //$NON-NLS-1$

	/**
	 * Action ID for Add Constant
	 */
	String ADD_CONSTANT_ACTION_ID = "add_constant"; //$NON-NLS-1$

	/**
	 * Action ID for Delete
	 */
	String DELETE_ACTION_ID = "delete"; //$NON-NLS-1$

	/**
	 * Action ID for Delete Traces
	 */
	String DELETE_TRACES_ACTION_ID = "delete_traces"; //$NON-NLS-1$

	/**
	 * Action ID for Switch Focus
	 */
	String FOCUS_ACTION_ID = "focus"; //$NON-NLS-1$

	/**
	 * Action ID for Edit Properties
	 */
	String EDIT_PROPERTIES_ACTION_ID = "edit_properties"; //$NON-NLS-1$

	/**
	 * Action ID for Convert to Trace
	 */
	String CONVERT_TRACE_ACTION_ID = "convert_trace"; //$NON-NLS-1$

	/**
	 * Action ID for Instrumenter
	 */
	String INSTRUMENTER_ACTION_ID = "instrumenter"; //$NON-NLS-1$

	/**
	 * Action ID for parse enum
	 */
	String PARSE_ENUM_ACTION_ID = "parse_enum"; //$NON-NLS-1$
}
