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
* Help context builder for view
*
*/
package com.nokia.tracebuilder.view;

import com.nokia.tracebuilder.engine.TraceBuilderHelp;
import com.nokia.tracebuilder.engine.TraceObjectPropertyDialog;
import com.nokia.tracebuilder.engine.TraceBuilderDialogs.CheckListDialogType;

/**
 * Help context builder for view
 * 
 */
public final class TraceViewHelp {

	/**
	 * Prevents construction
	 */
	private TraceViewHelp() {
	}

	/**
	 * Gets a context name based on property dialog type
	 * 
	 * @param dialogType
	 *            the dialog type
	 * @return the context name
	 */
	static String getPropertyDialogContext(int dialogType) {
		return TraceBuilderHelp.HELP_CONTEXT_BASE
				+ getPropertyDialogTypeContext(dialogType);
	}

	/**
	 * Gets the context name based on check list dialog type
	 * 
	 * @param dialogType
	 *            the dialog type
	 * @return the context name
	 */
	static String getCheckListDialogContext(CheckListDialogType dialogType) {
		return TraceBuilderHelp.HELP_CONTEXT_BASE
				+ getCheckListDialogTypeContext(dialogType);
	}

	/**
	 * Gets context name based on property dialog type
	 * 
	 * @param dialogType
	 *            the type of the property dialog
	 * @return the context identifier
	 */
	private static String getPropertyDialogTypeContext(int dialogType) {
		String retval;
		switch (dialogType) {
		case TraceObjectPropertyDialog.ADD_TRACE:
			retval = TraceBuilderHelp.ADD_TRACE_HELP_CONTEXT;
			break;
		case TraceObjectPropertyDialog.ADD_PARAMETER:
			retval = TraceBuilderHelp.ADD_PARAMETER_HELP_CONTEXT;
			break;
		case TraceObjectPropertyDialog.SELECT_COMPONENT:
			retval = TraceBuilderHelp.SELECT_COMPONENT_HELP_CONTEXT;
			break;
		case TraceObjectPropertyDialog.EDIT_GROUP:
			retval = TraceBuilderHelp.EDIT_PROPERTIES_HELP_CONTEXT;
			break;
		case TraceObjectPropertyDialog.EDIT_TRACE:
			retval = TraceBuilderHelp.EDIT_PROPERTIES_HELP_CONTEXT;
			break;
		case TraceObjectPropertyDialog.ADD_CONSTANT:
			retval = TraceBuilderHelp.ADD_ENUM_HELP_CONTEXT;
			break;
		case TraceObjectPropertyDialog.EDIT_CONSTANT:
			retval = TraceBuilderHelp.EDIT_PROPERTIES_HELP_CONTEXT;
			break;
		case TraceObjectPropertyDialog.EDIT_CONSTANT_TABLE:
			retval = TraceBuilderHelp.EDIT_PROPERTIES_HELP_CONTEXT;
			break;
		case TraceObjectPropertyDialog.INSTRUMENTER:
			retval = TraceBuilderHelp.INSTRUMENTER_HELP_CONTEXT;
			break;
		default:
			retval = null;
			break;
		}
		return retval;
	}

	/**
	 * Gets context name based on check list dialog type
	 * 
	 * @param dialogType
	 *            the type of the check list dialog
	 * @return the context identifier
	 */
	private static String getCheckListDialogTypeContext(
			CheckListDialogType dialogType) {
		String retval;
		switch (dialogType) {
		case DELETE_TRACES:
			retval = TraceBuilderHelp.DELETE_TRACES_HELP_CONTEXT;
			break;
		case INSTRUMENT_FILES:
			retval = TraceBuilderHelp.INSTRUMENTER_HELP_CONTEXT;
			break;
		default:
			retval = null;
			break;
		}
		return retval;
	}
}
