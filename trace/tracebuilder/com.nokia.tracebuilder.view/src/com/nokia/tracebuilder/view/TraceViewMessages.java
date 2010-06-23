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
* UI message builder
*
*/
package com.nokia.tracebuilder.view;

import com.nokia.tracebuilder.engine.TraceBuilderErrorMessages;
import com.nokia.tracebuilder.engine.TraceObjectPropertyDialog;
import com.nokia.tracebuilder.engine.TraceBuilderDialogs.CheckListDialogType;
import com.nokia.tracebuilder.engine.TraceBuilderDialogs.DeleteObjectQueryParameters;
import com.nokia.tracebuilder.engine.TraceBuilderDialogs.FileQueryParameters;
import com.nokia.tracebuilder.engine.TraceBuilderDialogs.QueryDialogParameters;
import com.nokia.tracebuilder.model.TraceParameter;

/**
 * UI message builder
 * 
 */
class TraceViewMessages {

	/**
	 * Maximum length for a name in property dialog UI
	 */
	private static final int MAX_NAME_LENGTH = 36; // CodForChk_Dis_Magic

	/**
	 * Mappings from parameter types to labels
	 */
	static final String[][] PARAMETER_LABEL_MAP = {
			// Order of two first parameter types is important, so do not change
			// order of those, because it affect to implementation of
			// PropertyDialogUIType class.
			{ TraceParameter.SDEC32,
					Messages.getString("TraceViewMessages.Signed32") }, //$NON-NLS-1$
			{ TraceParameter.UDEC32,
					Messages.getString("TraceViewMessages.Unsigned32") }, //$NON-NLS-1$
			{ TraceParameter.OCT32,
					Messages.getString("TraceViewMessages.Octal32") }, //$NON-NLS-1$	
			{ TraceParameter.HEX32,
					Messages.getString("TraceViewMessages.Hex32") }, //$NON-NLS-1$
			{ TraceParameter.HEX16,
					Messages.getString("TraceViewMessages.Hex16") }, //$NON-NLS-1$
			{ TraceParameter.SDEC16,
					Messages.getString("TraceViewMessages.Signed16") }, //$NON-NLS-1$
			{ TraceParameter.UDEC16,
					Messages.getString("TraceViewMessages.Unsigned16") }, //$NON-NLS-1$
			{ TraceParameter.OCT16,
					Messages.getString("TraceViewMessages.Octal16") }, //$NON-NLS-1$					
			{ TraceParameter.HEX8, Messages.getString("TraceViewMessages.Hex8") }, //$NON-NLS-1$
			{ TraceParameter.SDEC8,
					Messages.getString("TraceViewMessages.Signed8") }, //$NON-NLS-1$
			{ TraceParameter.UDEC8,
					Messages.getString("TraceViewMessages.Unsigned8") },//$NON-NLS-1$
			{ TraceParameter.OCT8,
					Messages.getString("TraceViewMessages.Octal8") },//$NON-NLS-1$					
			{ TraceParameter.HEX64,
					Messages.getString("TraceViewMessages.Hex64") }, //$NON-NLS-1$
			{ TraceParameter.SDEC64,
					Messages.getString("TraceViewMessages.Signed64") }, //$NON-NLS-1$
			{ TraceParameter.UDEC64,
					Messages.getString("TraceViewMessages.Unsigned64") }, //$NON-NLS-1$
			{ TraceParameter.OCT64,
					Messages.getString("TraceViewMessages.Octal64") }, //$NON-NLS-1$
			{ TraceParameter.FLOAT_FIX,
					Messages.getString("TraceViewMessages.FloatFix") }, //$NON-NLS-1$
			{ TraceParameter.FLOAT_EXP,
					Messages.getString("TraceViewMessages.FloatExp") }, //$NON-NLS-1$
			{ TraceParameter.FLOAT_OPT,
					Messages.getString("TraceViewMessages.FloatOpt") }, //$NON-NLS-1$
			{ TraceParameter.ASCII,
					Messages.getString("TraceViewMessages.Ascii") }, //$NON-NLS-1$
			{ TraceParameter.UNICODE,
					Messages.getString("TraceViewMessages.Unicode") }, //$NON-NLS-1$
			{ TraceParameter.POINTER,
					Messages.getString("TraceViewMessages.Pointer") }, //$NON-NLS-1$					
	};

	/**
	 * Not constructed
	 */
	private TraceViewMessages() {
	}

	/**
	 * Gets confirmation query text
	 * 
	 * @param parameters
	 *            parameters for the query
	 * @return the query text
	 */
	static String getConfirmationQueryText(QueryDialogParameters parameters) {
		StringBuffer sb = new StringBuffer();
		String s;
		switch (parameters.dialogType) {
		case DELETE_GROUP:
			sb.append(Messages
					.getString("TraceViewMessages.DeleteTraceGroupPrefix")); //$NON-NLS-1$
			sb.append(((DeleteObjectQueryParameters) parameters).objectName);
			sb.append(Messages
					.getString("TraceViewMessages.DeleteTraceGroupPostfix")); //$NON-NLS-1$
			break;
		case DELETE_TRACE:
			s = Messages.getString("TraceViewMessages.DeleteTracePrefix"); //$NON-NLS-1$
			sb.append(s);
			sb.append(((DeleteObjectQueryParameters) parameters).objectName);
			s = Messages.getString("TraceViewMessages.DeleteTraceMiddle"); //$NON-NLS-1$
			sb.append(s);
			sb.append(((DeleteObjectQueryParameters) parameters).ownerName);
			sb.append(Messages
					.getString("TraceViewMessages.DeleteTracePostfix")); //$NON-NLS-1$
			break;
		case DELETE_PARAMETER:
			sb.append(Messages
					.getString("TraceViewMessages.DeleteParameterPrefix")); //$NON-NLS-1$
			sb.append(((DeleteObjectQueryParameters) parameters).objectName);
			sb.append(Messages
					.getString("TraceViewMessages.DeleteParameterMiddle")); //$NON-NLS-1$
			sb.append(((DeleteObjectQueryParameters) parameters).ownerName);
			sb.append(Messages
					.getString("TraceViewMessages.DeleteParameterPostfix")); //$NON-NLS-1$
			break;
		case DELETE_CONSTANT_TABLE:
			sb.append(Messages
					.getString("TraceViewMessages.DeleteConstTablePrefix")); //$NON-NLS-1$
			sb.append(((DeleteObjectQueryParameters) parameters).objectName);
			sb.append(Messages
					.getString("TraceViewMessages.DeleteConstTablePostfix")); //$NON-NLS-1$
			break;
		case DELETE_CONSTANT:
			sb.append(Messages
					.getString("TraceViewMessages.DeleteConstantPrefix")); //$NON-NLS-1$
			sb.append(((DeleteObjectQueryParameters) parameters).objectName);
			sb.append(Messages
					.getString("TraceViewMessages.DeleteConstantMiddle")); //$NON-NLS-1$
			sb.append(((DeleteObjectQueryParameters) parameters).ownerName);
			sb.append(Messages
					.getString("TraceViewMessages.DeleteConstantPostfix")); //$NON-NLS-1$
			break;
		case UPDATE_WHEN_SOURCE_NOT_OPEN:
			sb.append(Messages
					.getString("TraceViewMessages.UpdateWhenSourceNotOpen")); //$NON-NLS-1$
			break;
		case REPLACE_FILE:
			s = Messages.getString("TraceViewMessages.ReplaceFilePrefix"); //$NON-NLS-1$
			sb.append(s);
			sb.append(TraceBuilderErrorMessages
					.convertPath(((FileQueryParameters) parameters).path));
			sb.append(Messages
					.getString("TraceViewMessages.ReplaceFilePostfix")); //$NON-NLS-1$
			break;
		case IMPORT_TRACE_PROJECT:
			s = Messages.getString("TraceViewMessages.ImportTraceProjectText"); //$NON-NLS-1$
			sb.append(s);
			break;
		case SELECT_TARGET_ENVIRONMENT:
			s = Messages
					.getString("TraceViewMessages.SelectEnvironmentDialogTitle"); //$NON-NLS-1$
			sb.append(s);
			break;
		default:
			break;
		}
		return sb.toString();
	}

	/**
	 * Gets the title for the checklist
	 * 
	 * @param type
	 *            the check list type
	 * @return the message
	 */
	static String getCheckListTitle(CheckListDialogType type) {
		String retval;
		switch (type) {
		case DELETE_TRACES:
			retval = Messages.getString("TraceViewMessages.DeleteTracesTitle"); //$NON-NLS-1$
			break;
		case INSTRUMENT_FILES:
			retval = Messages
					.getString("TraceViewMessages.InstrumentFilesTitle"); //$NON-NLS-1$
			break;
		default:
			retval = ""; //$NON-NLS-1$
			break;
		}
		return retval;
	}

	/**
	 * Gets the message for the checklist
	 * 
	 * @param type
	 *            the check list type
	 * @return the message
	 */
	static String getCheckListText(CheckListDialogType type) {
		String retval;
		switch (type) {
		case DELETE_TRACES:
			retval = Messages.getString("TraceViewMessages.DeleteTracesHint"); //$NON-NLS-1$
			break;
		case INSTRUMENT_FILES:
			retval = Messages
					.getString("TraceViewMessages.InstrumentFilesHint"); //$NON-NLS-1$
			break;
		default:
			retval = ""; //$NON-NLS-1$
			break;
		}
		return retval;
	}

	/**
	 * Maps parameter type to string
	 * 
	 * @param parameter
	 *            the parameter
	 * @return the type as string
	 */
	static String parameterTypeToString(TraceParameter parameter) {
		String ret = null;
		String type = parameter.getType();
		if (type != null) {
			for (int i = 0; i < PARAMETER_LABEL_MAP.length; i++) {
				if (parameter.getType().equals(PARAMETER_LABEL_MAP[i][0])) {
					ret = PARAMETER_LABEL_MAP[i][1];
				}
			}
			if (ret == null) {
				ret = parameter.getType();
			}
		} else {
			ret = Messages.getString("TraceViewMessages.UnknownType"); //$NON-NLS-1$
		}
		return ret;
	}

	/**
	 * Gets the property dialog title based on dialog type
	 * 
	 * @param dialogType
	 *            the dialog type
	 * @return the dialog title
	 */
	static String getPropertyDialogTitle(int dialogType) {
		return getPropertyDialogCaption(dialogType);
	}

	/**
	 * Gets the dialog caption based on dialog type
	 * 
	 * @param dialogType
	 *            the dialog type
	 * @return the caption
	 */
	static String getPropertyDialogCaption(int dialogType) {
		String caption;
		switch (dialogType) {
		case TraceObjectPropertyDialog.EDIT_GROUP:
			caption = Messages.getString("PropertyDialog.EditGroupCaption"); //$NON-NLS-1$
			break;
		case TraceObjectPropertyDialog.ADD_TRACE:
			caption = Messages.getString("PropertyDialog.AddTraceCaption"); //$NON-NLS-1$
			break;
		case TraceObjectPropertyDialog.EDIT_TRACE:
			caption = Messages.getString("PropertyDialog.EditTraceCaption"); //$NON-NLS-1$
			break;
		case TraceObjectPropertyDialog.ADD_PARAMETER:
			caption = Messages.getString("PropertyDialog.AddParameterCaption"); //$NON-NLS-1$
			break;
		case TraceObjectPropertyDialog.SELECT_COMPONENT:
			caption = Messages
					.getString("TraceViewMessages.SelectComponentCaption"); //$NON-NLS-1$
			break;
		case TraceObjectPropertyDialog.ADD_CONSTANT:
			caption = Messages.getString("PropertyDialog.AddConstantCaption"); //$NON-NLS-1$
			break;
		case TraceObjectPropertyDialog.EDIT_CONSTANT:
			caption = Messages.getString("PropertyDialog.EditConstantCaption"); //$NON-NLS-1$
			break;
		case TraceObjectPropertyDialog.EDIT_CONSTANT_TABLE:
			caption = Messages
					.getString("PropertyDialog.EditConstantTableCaption"); //$NON-NLS-1$
			break;
		case TraceObjectPropertyDialog.INSTRUMENTER:
			caption = Messages.getString("TraceViewMessages.InstrumentCaption"); //$NON-NLS-1$
			break;
		default:
			caption = ""; //$NON-NLS-1$
			break;
		}
		return caption;
	}

	/**
	 * Gets the property dialog message
	 * 
	 * @param dialogType
	 *            the type of the property dialog
	 * @param target
	 *            the target object of the property dialog
	 * @return the dialog message
	 */
	static String getPropertyDialogMessage(int dialogType, String target) {
		String hint;
		StringBuffer sb;
		switch (dialogType) {
		case TraceObjectPropertyDialog.EDIT_GROUP:
			sb = new StringBuffer();
			sb.append(Messages.getString("PropertyDialog.EditGroupHintPrefix")); //$NON-NLS-1$
			hint = Messages.getString("PropertyDialog.EditGroupHintPostfix"); //$NON-NLS-1$
			sb.append(getName(target, hint));
			sb.append(hint);
			hint = sb.toString();
			break;
		case TraceObjectPropertyDialog.ADD_TRACE:
			hint = Messages.getString("PropertyDialog.AddTraceHint"); //$NON-NLS-1$
			break;
		case TraceObjectPropertyDialog.EDIT_TRACE:
			sb = new StringBuffer();
			sb.append(Messages.getString("PropertyDialog.EditTraceHintPrefix")); //$NON-NLS-1$
			hint = Messages.getString("PropertyDialog.EditTraceHintPostfix"); //$NON-NLS-1$
			sb.append(getName(target, hint));
			sb.append(hint);
			hint = sb.toString();
			break;
		case TraceObjectPropertyDialog.ADD_PARAMETER:
			sb = new StringBuffer();
			sb.append(Messages
					.getString("PropertyDialog.AddTraceParameterHintPrefix")); //$NON-NLS-1$
			hint = Messages
					.getString("PropertyDialog.AddTraceParameterHintPostfix"); //$NON-NLS-1$
			sb.append(getName(target, hint));
			sb.append(hint);
			hint = sb.toString();
			break;
		case TraceObjectPropertyDialog.SELECT_COMPONENT:
			sb = new StringBuffer();
			sb.append(Messages
					.getString("PropertyDialog.SelectComponentHintPrefix")); //$NON-NLS-1$
			sb.append(Messages
					.getString("PropertyDialog.SelectComponentHintPostfix")); //$NON-NLS-1$
			hint = sb.toString();
			break;
		case TraceObjectPropertyDialog.ADD_CONSTANT:
			hint = Messages.getString("PropertyDialog.AddConstantHint"); //$NON-NLS-1$
			break;
		case TraceObjectPropertyDialog.EDIT_CONSTANT:
			sb = new StringBuffer();
			sb.append(Messages
					.getString("PropertyDialog.EditConstantHintPrefix")); //$NON-NLS-1$
			hint = Messages.getString("PropertyDialog.EditConstantHintPostfix"); //$NON-NLS-1$
			sb.append(getName(target, hint));
			sb.append(hint);
			hint = sb.toString();
			break;

		case TraceObjectPropertyDialog.EDIT_CONSTANT_TABLE:
			sb = new StringBuffer();
			sb.append(Messages
					.getString("PropertyDialog.EditConstantTableHintPrefix")); //$NON-NLS-1$
			hint = Messages
					.getString("PropertyDialog.EditConstantTableHintPostfix"); //$NON-NLS-1$
			sb.append(getName(target, hint));
			sb.append(hint);
			hint = sb.toString();
			break;
		case TraceObjectPropertyDialog.INSTRUMENTER:
			hint = Messages.getString("PropertyDialog.InstrumenterHint"); //$NON-NLS-1$
			break;
		default:
			hint = ""; //$NON-NLS-1$
			break;
		}
		return hint;
	}

	/**
	 * Gets the name of the object and truncates if too long
	 * 
	 * @param target
	 *            the target object
	 * @param postfix
	 *            the postfix
	 * @return the name
	 */
	private static String getName(String target, String postfix) {
		String retval = target;
		if (retval.length() > MAX_NAME_LENGTH) {
			String str;
			if (postfix.startsWith(".")) { //$NON-NLS-1$
				str = ".."; //$NON-NLS-1$
			} else {
				str = "..."; //$NON-NLS-1$
			}
			retval = retval.substring(0, MAX_NAME_LENGTH - str.length()) + str;
		}
		return retval;
	}
}
