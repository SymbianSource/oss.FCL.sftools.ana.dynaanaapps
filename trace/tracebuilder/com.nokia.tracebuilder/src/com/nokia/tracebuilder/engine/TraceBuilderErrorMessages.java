/*
* Copyright (c) 2008-2010 Nokia Corporation and/or its subsidiary(-ies). 
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
* Error code to error message mapper
*
*/
package com.nokia.tracebuilder.engine;

import java.io.File;

import com.nokia.tracebuilder.engine.TraceBuilderErrorCodes.FileErrorParameters;
import com.nokia.tracebuilder.engine.TraceBuilderErrorCodes.RangeErrorParameters;
import com.nokia.tracebuilder.engine.TraceBuilderErrorCodes.StringErrorParameters;
import com.nokia.tracebuilder.engine.TraceBuilderErrorCodes.TraceBuilderErrorCode;
import com.nokia.tracebuilder.model.TraceBuilderErrorParameters;
import com.nokia.tracebuilder.model.TraceBuilderException;
import com.nokia.tracebuilder.project.TraceProjectAPI;
import com.nokia.tracebuilder.source.SourceConstants;
import com.nokia.tracebuilder.source.SourceUtils;

/**
 * Error code to error message mapper
 * 
 */
public final class TraceBuilderErrorMessages {

	/**
	 * Maps an exception to error message
	 * 
	 * @param e
	 *            the exception
	 * @return the message
	 */
	public static String getErrorMessage(TraceBuilderException e) {
		return TraceBuilderErrorMessages.getErrorMessage(
				(TraceBuilderErrorCode) e.getErrorCode(), e
						.getErrorParameters());
	}

	/**
	 * Maps error code to error message
	 * 
	 * @param error
	 *            the error code
	 * @param parameters
	 *            the parameters of the error
	 * @return the message
	 */
	public static String getErrorMessage(TraceBuilderErrorCode error,
			TraceBuilderErrorParameters parameters) {
		// CodForChk_Dis_LengthyFunc
		// CodForChk_Dis_ComplexFunc
		String s;
		StringBuffer sb = new StringBuffer();
		switch (error) {
		case DUPLICATE_GROUP_ID:
			sb.append(Messages
					.getString("TraceBuilderErrorMessages.DuplicateGroupID")); //$NON-NLS-1$
			break;
		case DUPLICATE_GROUP_NAME:
			sb.append(Messages
					.getString("TraceBuilderErrorMessages.DuplicateGroupName")); //$NON-NLS-1$
			break;
		case DUPLICATE_TRACE_ID:
			sb.append(Messages
					.getString("TraceBuilderErrorMessages.DuplicateTraceID")); //$NON-NLS-1$
			break;
		case DUPLICATE_TRACE_NAME:
			sb.append(Messages
					.getString("TraceBuilderErrorMessages.DuplicateTraceName")); //$NON-NLS-1$
			break;
		case DUPLICATE_PARAMETER_ID:
			s = Messages
					.getString("TraceBuilderErrorMessages.DuplicateParameterID"); //$NON-NLS-1$
			sb.append(s);
			break;
		case DUPLICATE_CONSTANT_VALUE:
			s = Messages
					.getString("TraceBuilderErrorMessages.DuplicateConstantValue"); //$NON-NLS-1$
			sb.append(s);
			break;
		case DUPLICATE_CONSTANT_ID:
			s = Messages
					.getString("TraceBuilderErrorMessages.DuplicateConstantID"); //$NON-NLS-1$
			sb.append(s);
			break;
		case DUPLICATE_CONSTANT_TABLE_NAME:
			s = Messages
					.getString("TraceBuilderErrorMessages.DuplicateConstantTableName"); //$NON-NLS-1$
			sb.append(s);
			break;
		case DUPLICATE_CONSTANT_TABLE_ID:
			s = Messages
					.getString("TraceBuilderErrorMessages.DuplicateConstantTableID"); //$NON-NLS-1$
			sb.append(s);
			break;
		case DUPLICATE_PARAMETER_NAME:
			s = Messages
					.getString("TraceBuilderErrorMessages.DuplicateParameterName"); //$NON-NLS-1$
			sb.append(s);
			break;
		case INVALID_GROUP_ID:
			createInvalidGroupIDMessage(parameters, sb);
			break;
		case INVALID_TRACE_ID:
			createInvalidTraceIDMessage(parameters, sb);
			break;
		case INVALID_MODEL_PROPERTIES_FOR_EXPORT:
			s = Messages
					.getString("TraceBuilderErrorMessages.InvalidModelPropertiesForExport"); //$NON-NLS-1$
			sb.append(s);
			break;
		case INVALID_MODEL_NAME:
			sb.append(Messages
					.getString("TraceBuilderErrorMessages.InvalidProjectName")); //$NON-NLS-1$
			break;
		case INVALID_GROUP_NAME:
			sb.append(Messages
					.getString("TraceBuilderErrorMessages.InvalidGroupName")); //$NON-NLS-1$
			break;
		case INVALID_TRACE_NAME:
			sb.append(Messages
					.getString("TraceBuilderErrorMessages.InvalidTraceName")); //$NON-NLS-1$
			break;
		case INVALID_PARAMETER_NAME:
			s = Messages
					.getString("TraceBuilderErrorMessages.InvalidParameterName"); //$NON-NLS-1$
			sb.append(s);
			break;
		case INVALID_CONSTANT_TABLE_NAME:
			s = Messages
					.getString("TraceBuilderErrorMessages.InvalidConstantTableName"); //$NON-NLS-1$
			sb.append(s);
			break;
		case CONSTANT_TABLE_NOT_PART_OF_PROJECT:
			s = Messages
					.getString("TraceBuilderErrorMessages.ConstantTableNotPartOfProject"); //$NON-NLS-1$
			sb.append(s);
			break;			
		case INVALID_TRACE_DATA:
			sb.append(Messages
					.getString("TraceBuilderErrorMessages.InvalidTraceData")); //$NON-NLS-1$
			break;
		case INVALID_PARAMETER_TYPE:
			createInvalidParameterTypeMessage(parameters, sb);
			break;
		case INVALID_CONSTANT_VALUE:
			s = Messages
					.getString("TraceBuilderErrorMessages.InvalidConstantValue"); //$NON-NLS-1$
			sb.append(s);
			break;
		case SOURCE_NOT_EDITABLE:
			s = Messages
					.getString("TraceBuilderErrorMessages.SourceNotEditable"); //$NON-NLS-1$
			sb.append(s);
			break;
		case INVALID_SOURCE_LOCATION:
			s = Messages
					.getString("TraceBuilderErrorMessages.InvalidSourceLocation"); //$NON-NLS-1$
			sb.append(s);
			break;
		case UNREACHABLE_TRACE_LOCATION:
			s = Messages
					.getString("TraceBuilderErrorMessages.UnreachableTraceLocation"); //$NON-NLS-1$
			sb.append(s);
			break;
		case INVALID_PROJECT_FILE:
			sb.append(Messages
					.getString("TraceBuilderErrorMessages.InvalidTraceFile")); //$NON-NLS-1$
			break;
		case FILE_NOT_FOUND:
			createFileNotFoundMessage((FileErrorParameters) parameters, sb);
			break;
		case INVALID_PATH:
			createInvalidPathMessage((FileErrorParameters) parameters, sb);
			break;
		case SOURCE_NOT_OPEN:
			sb.append(Messages
					.getString("TraceBuilderErrorMessages.SourceNotOpen")); //$NON-NLS-1$
			break;
		case CANNOT_OPEN_PROJECT_FILE:
			s = Messages
					.getString("TraceBuilderErrorMessages.CannotOpenProjectFile"); //$NON-NLS-1$
			sb.append(s);
			break;
		case CANNOT_WRITE_PROJECT_FILE:
			s = Messages
					.getString("TraceBuilderErrorMessages.CannotWriteProjectFile"); //$NON-NLS-1$
			sb.append(s);
			break;
		case PARAMETER_FORMAT_MISMATCH:
			s = Messages
					.getString("TraceBuilderErrorMessages.ParameterFormatMismatch"); //$NON-NLS-1$
			sb.append(s);
			break;
		case GROUP_NOT_SELECTED:
			sb.append(Messages
					.getString("TraceBuilderErrorMessages.GroupNotSelected")); //$NON-NLS-1$
			break;
		case TRACE_NOT_SELECTED:
			sb.append(Messages
					.getString("TraceBuilderErrorMessages.TraceNotSelected")); //$NON-NLS-1$
			break;
		case CONSTANT_TABLE_NOT_SELECTED:
			s = Messages
					.getString("TraceBuilderErrorMessages.ConstantTableNotSelected"); //$NON-NLS-1$
			sb.append(s);
			break;
		case LOCATION_NOT_SELECTED:
			s = Messages
					.getString("TraceBuilderErrorMessages.TraceLocationNotSelected"); //$NON-NLS-1$
			sb.append(s);
			break;
		case CANNOT_DELETE_SELECTED_OBJECT:
			s = Messages
					.getString("TraceBuilderErrorMessages.CannotDeleteSelectedObject"); //$NON-NLS-1$
			sb.append(s);
			break;
		case MODEL_NOT_READY:
			s = Messages
					.getString("TraceBuilderErrorMessages.TraceProjectNotOpen"); //$NON-NLS-1$
			sb.append(s);
			break;
		case NO_TRACE_GROUPS:
			sb.append(Messages
					.getString("TraceBuilderErrorMessages.NoTraceGroups")); //$NON-NLS-1$
			break;
		case NOT_ENOUGH_PARAMETERS:
			s = Messages
					.getString("TraceBuilderErrorMessages.NotEnoughParameters"); //$NON-NLS-1$
			sb.append(s);
			break;
		case PARAMETER_ADD_NOT_ALLOWED:
			s = Messages
					.getString("TraceBuilderErrorMessages.ParameterAddNotAllowed"); //$NON-NLS-1$
			sb.append(s);
			break;
		case PARAMETER_REMOVE_NOT_ALLOWED:
			s = Messages
					.getString("TraceBuilderErrorMessages.ParameterRemoveNotAllowed"); //$NON-NLS-1$
			sb.append(s);
			break;
		case PARAMETER_TEMPLATE_ALREADY_IN_USE:
			s = Messages
					.getString("TraceBuilderErrorMessages.ParameterTemplateInUse"); //$NON-NLS-1$
			sb.append(s);
			break;
		case CONSTANT_TABLE_PARSE_FAILED:
			s = Messages
					.getString("TraceBuilderErrorMessages.ConstantTableParseFailed"); //$NON-NLS-1$
			sb.append(s);
			break;
		case UNEXPECTED_EXCEPTION:
			s = Messages
					.getString("TraceBuilderErrorMessages.UnexpectedException"); //$NON-NLS-1$
			sb.append(s);
			break;
		case TRACE_NAME_FORMAT_MISSING_FUNCTION:
			s = Messages
					.getString("TraceBuilderErrorMessages.NameFormatMissingFunction"); //$NON-NLS-1$
			sb.append(s);
			break;
		case INVALID_TRACE_TEXT_FORMAT:
			s = Messages
					.getString("TraceBuilderErrorMessages.InvalidTraceTextFormat"); //$NON-NLS-1$
			sb.append(s);
			break;
		case INVALID_TRACE_NAME_FORMAT:
			s = Messages
					.getString("TraceBuilderErrorMessages.InvalidTraceNameFormat"); //$NON-NLS-1$
			sb.append(s);
			break;
		case NO_FUNCTIONS_TO_INSTRUMENT_WITH_TEMPLATE:
			s = Messages
					.getString("TraceBuilderErrorMessages.NoFunctionsToInstrumentPrefix"); //$NON-NLS-1$
			sb.append(s);
			sb.append(((StringErrorParameters) parameters).string);
			s = Messages
					.getString("TraceBuilderErrorMessages.NoFunctionsToInstrumentPostfix"); //$NON-NLS-1$
			sb.append(s);
			break;
		case NO_FUNCTIONS_TO_INSTRUMENT:
			s = Messages
					.getString("TraceBuilderErrorMessages.NoFunctionsToInstrument"); //$NON-NLS-1$
			sb.append(s);
			break;
		case MULTIPLE_ERRORS_IN_OPERATION:
			s = Messages
					.getString("TraceBuilderErrorMessages.MultipleErrorsInOperation"); //$NON-NLS-1$
			sb.append(s);
			break;
		case NO_TRACES_TO_DELETE:
			sb.append(Messages
					.getString("TraceBuilderErrorMessages.NoTracesToDelete")); //$NON-NLS-1$
			break;
		case TRACE_DOES_NOT_EXIST:
			s = Messages
					.getString("TraceBuilderErrorMessages.TraceDoesNotExist"); //$NON-NLS-1$
			sb.append(s);
			break;
		case TRACE_NEEDS_CONVERSION:
			s = Messages
					.getString("TraceBuilderErrorMessages.TraceNeedsConversionPrefix"); //$NON-NLS-1$
			sb.append(s);
			sb.append(TraceBuilderGlobals.getTraceModel().getExtension(
					TraceProjectAPI.class).getTitle());
			break;
		case PARAMETER_COUNT_MISMATCH:
			s = Messages
					.getString("TraceBuilderErrorMessages.ParameterCountMismatch"); //$NON-NLS-1$
			sb.append(s);
			break;
		case PARAMETER_COUNT_DOES_NOT_MATCH_API:
			s = Messages
					.getString("TraceBuilderErrorMessages.ParameterCountDoesNotMatchApi"); //$NON-NLS-1$
			sb.append(s);
			break;
		case TRACE_HAS_NO_LOCATIONS:
			s = Messages
					.getString("TraceBuilderErrorMessages.TraceHasNoLocations"); //$NON-NLS-1$
			sb.append(s);
			break;
		case TRACE_HAS_MULTIPLE_LOCATIONS:
			s = Messages
					.getString("TraceBuilderErrorMessages.TraceHasMultipleLocations"); //$NON-NLS-1$
			sb.append(s);
			break;
		case LOCATION_PARSER_FAILED:
			s = Messages
					.getString("TraceBuilderErrorMessages.LocationCouldNotBeParsed"); //$NON-NLS-1$
			sb.append(s);
			break;
		case NO_TRACES_TO_EXPORT:
			s = Messages
					.getString("TraceBuilderErrorMessages.NoTracesToExport"); //$NON-NLS-1$
			sb.append(s);
			break;
		case CANNOT_OPEN_SOURCE_FILE:
			s = Messages
					.getString("TraceBuilderErrorMessages.CannotOpenSourceFile"); //$NON-NLS-1$
			sb.append(s);
			break;
		case CANNOT_UPDATE_TRACE_INTO_SOURCE:
			s = Messages
					.getString("TraceBuilderErrorMessages.CannotUpdateTraceIntoSource"); //$NON-NLS-1$
			sb.append(s);
			break;
		case PARAMETER_FORMAT_NOT_SUPPORTED:
			createParameterFormatNotSupportedMessage(parameters, sb);
			break;
		case PARAMETER_FORMAT_NEEDS_EXT_MACRO:
			createParameterFormatNotSupportedInMacroMessage(parameters, sb);
			break;
		case PARAMETER_FORMAT_NOT_SUPPORTED_IN_ARRAY:
			createParameterFormatNotSupportedInArrayMessage(parameters, sb);
			break;
		case PARAMETER_FORMAT_UNNECESSARY_EXT_MACRO:
			s = Messages
					.getString("TraceBuilderErrorMessages.ParameterFormatUnnecessaryExtMacro"); //$NON-NLS-1$
			sb.append(s);
			break;
		case PROPERTY_FILE_ELEMENT_NOT_SUPPORTED:
			s = Messages
					.getString("TraceBuilderErrorMessages.PropertyFileElementNotSupportedPrefix"); //$NON-NLS-1$ CodForChk_Dis_LengthyLine
			sb.append(s);
			sb.append(((StringErrorParameters) parameters).string);
			s = Messages
					.getString("TraceBuilderErrorMessages.PropertyFileElementNotSupportedPostfix"); //$NON-NLS-1$ CodForChk_Dis_LengthyLine
			sb.append(s);
			break;
		case PROPERTY_FILE_ELEMENT_MISPLACED:
			s = Messages
					.getString("TraceBuilderErrorMessages.PropertyFileElementMisplacedPrefix"); //$NON-NLS-1$
			sb.append(s);
			sb.append(((StringErrorParameters) parameters).string);
			s = Messages
					.getString("TraceBuilderErrorMessages.PropertyFileElementMisplacedPostfix"); //$NON-NLS-1$
			sb.append(s);
			break;
		case PROPERTY_FILE_ATTRIBUTE_INVALID:
			s = Messages
					.getString("TraceBuilderErrorMessages.PropertyFileAttributeInvalidPrefix"); //$NON-NLS-1$
			sb.append(s);
			sb.append(((StringErrorParameters) parameters).string);
			s = Messages
					.getString("TraceBuilderErrorMessages.PropertyFileAttributeInvalidPostfix"); //$NON-NLS-1$
			sb.append(s);
			break;
		case INSERT_TRACE_DOES_NOT_WORK:
			s = Messages
					.getString("TraceBuilderErrorMessages.InsertTraceDoesNotWork"); //$NON-NLS-1$
			sb.append(s);
			break;
		case NO_CONTEXT_FOR_LOCATION:
			s = Messages
					.getString("TraceBuilderErrorMessages.NoContextForLocation"); //$NON-NLS-1$
			sb.append(s);
			break;
		case CANNOT_PARSE_FUNCTION_PARAMETERS:
			s = Messages
					.getString("TraceBuilderErrorMessages.CannotParseFunctionParameters"); //$NON-NLS-1$
			sb.append(s);
			break;
		case INVALID_PARAMETER_NAME_IN_RETURN_VALUE:
			s = Messages
					.getString("TraceBuilderErrorMessages.InvalidParameterNameInReturnValue"); //$NON-NLS-1$
			sb.append(s);
			break;
		case RUN_OUT_OF_USER_DEFINED_GROUP_IDS:
			sb.append(Messages
					.getString("TraceBuilderErrorMessages.RunOutOfGroupIDs")); //$NON-NLS-1$
			break;
		case VAR_ARG_LIST_PARAMETER_FOUND:
			sb.append(Messages
					.getString("TraceBuilderErrorMessages.VarArgListParameterFound")); //$NON-NLS-1$
			break;			
		case UNKNOWN_OST_VERSION:
			sb.append(Messages
					.getString("TraceBuilderErrorMessages.UnkownOstVersion")); //$NON-NLS-1$
			break;				
		default:
			break;
		}
		return sb.toString();
	}

	/**
	 * Creates invalid parameter type message
	 * 
	 * @param parameters
	 *            the parameters
	 * @param sb
	 *            the message buffer
	 */
	private static void createInvalidParameterTypeMessage(
			TraceBuilderErrorParameters parameters, StringBuffer sb) {
		String s;
		if (parameters instanceof StringErrorParameters) {
			s = Messages
					.getString("TraceBuilderErrorMessages.InvalidParameterTypePrefix"); //$NON-NLS-1$
			sb.append(s);
			sb.append(((StringErrorParameters) parameters).string);
			String format = SourceUtils
					.mapNormalTypeToFormat(((StringErrorParameters) parameters).string);
			if (format != null) {
				sb.append(" "); //$NON-NLS-1$
				sb.append(format);
			}
			s = Messages
					.getString("TraceBuilderErrorMessages.InvalidParameterTypePostfix"); //$NON-NLS-1$
			sb.append(s);
		} else {
			s = Messages
					.getString("TraceBuilderErrorMessages.InvalidParameterType"); //$NON-NLS-1$
			sb.append(s);
		}
	}

	/**
	 * Creates parameter format not supported message
	 * 
	 * @param parameters
	 *            the parameters
	 * @param sb
	 *            the message buffer
	 */
	private static void createParameterFormatNotSupportedMessage(
			TraceBuilderErrorParameters parameters, StringBuffer sb) {
		String s;
		if (parameters instanceof StringErrorParameters) {
			s = Messages
					.getString("TraceBuilderErrorMessages.ParameterFormatNotSupportedPrefix"); //$NON-NLS-1$
			sb.append(s);
			sb.append(((StringErrorParameters) parameters).string);
			s = Messages
					.getString("TraceBuilderErrorMessages.ParameterFormatNotSupportedPostfix"); //$NON-NLS-1$
			sb.append(s);
		} else {
			s = Messages
					.getString("TraceBuilderErrorMessages.ParameterFormatNotSupported"); //$NON-NLS-1$
			sb.append(s);
		}
	}

	/**
	 * Creates parameter not supported in macro message
	 * 
	 * @param parameters
	 *            the parameters
	 * @param sb
	 *            the message buffer
	 */
	private static void createParameterFormatNotSupportedInMacroMessage(
			TraceBuilderErrorParameters parameters, StringBuffer sb) {
		String s;
		if (parameters instanceof StringErrorParameters) {
			s = Messages
					.getString("TraceBuilderErrorMessages.ParameterFormatNotSupportedInMacroPrefix"); //$NON-NLS-1$ CodForChk_Dis_LengthyLine
			sb.append(s);
			sb.append(((StringErrorParameters) parameters).string);
			s = Messages
					.getString("TraceBuilderErrorMessages.ParameterFormatNotSupportedInMacroPostfix"); //$NON-NLS-1$ CodForChk_Dis_LengthyLine
			sb.append(s);
		} else {
			s = Messages
					.getString("TraceBuilderErrorMessages.ParameterFormatNotSupportedInMacro"); //$NON-NLS-1$
			sb.append(s);
		}
	}

	/**
	 * Creates parameter not supported in array message
	 * 
	 * @param parameters
	 *            the parameters
	 * @param sb
	 *            the message buffer
	 */
	private static void createParameterFormatNotSupportedInArrayMessage(
			TraceBuilderErrorParameters parameters, StringBuffer sb) {
		String s;
		if (parameters instanceof StringErrorParameters) {
			s = Messages
					.getString("TraceBuilderErrorMessages.ParameterFormatNotSupportedInArrayPrefix"); //$NON-NLS-1$ CodForChk_Dis_LengthyLine
			sb.append(s);
			sb.append(((StringErrorParameters) parameters).string);
			s = Messages
					.getString("TraceBuilderErrorMessages.ParameterFormatNotSupportedInArrayPostfix"); //$NON-NLS-1$ CodForChk_Dis_LengthyLine
			sb.append(s);
		} else {
			s = Messages
					.getString("TraceBuilderErrorMessages.ParameterFormatNotSupportedInArray"); //$NON-NLS-1$
			sb.append(s);
		}
	}

	/**
	 * Creates invalid trace ID message
	 * 
	 * @param parameters
	 *            the parameters
	 * @param sb
	 *            the message buffer
	 */
	private static void createInvalidTraceIDMessage(
			TraceBuilderErrorParameters parameters, StringBuffer sb) {
		String s;
		if (parameters instanceof RangeErrorParameters) {
			s = Messages
					.getString("TraceBuilderErrorMessages.InvalidTraceIDStart"); //$NON-NLS-1$
			sb.append(s);
			s = Messages
					.getString("TraceBuilderErrorMessages.InvalidTraceIDMiddle"); //$NON-NLS-1$
			addRangeParameter((RangeErrorParameters) parameters, sb, s);
			sb.append(Messages
					.getString("TraceBuilderErrorMessages.InvalidTraceIDEnd")); //$NON-NLS-1$
		} else {
			sb.append(Messages
					.getString("TraceBuilderErrorMessages.InvalidTraceID")); //$NON-NLS-1$
		}
	}

	/**
	 * Creates invalid group ID message
	 * 
	 * @param parameters
	 *            the parameters
	 * @param sb
	 *            the message buffer
	 */
	private static void createInvalidGroupIDMessage(
			TraceBuilderErrorParameters parameters, StringBuffer sb) {
		String s;
		if (parameters instanceof RangeErrorParameters) {
			s = Messages
					.getString("TraceBuilderErrorMessages.InvalidGroupIDStart"); //$NON-NLS-1$
			sb.append(s);
			s = Messages
					.getString("TraceBuilderErrorMessages.InvalidGroupIDMiddle"); //$NON-NLS-1$
			addRangeParameter((RangeErrorParameters) parameters, sb, s);
			sb.append(Messages
					.getString("TraceBuilderErrorMessages.InvalidGroupIDEnd")); //$NON-NLS-1$
		} else {
			sb.append(Messages
					.getString("TraceBuilderErrorMessages.InvalidGroupID")); //$NON-NLS-1$
		}
	}

	/**
	 * Adds a range parameter to error buffer
	 * 
	 * @param parameters
	 *            the range
	 * @param sb
	 *            the buffer
	 * @param middleText
	 *            the text between the range
	 */
	private static void addRangeParameter(RangeErrorParameters parameters,
			StringBuffer sb, String middleText) {
		if (parameters.isHex) {
			sb.append(SourceConstants.HEX_PREFIX);
			sb.append(Integer.toHexString(parameters.start));
		} else {
			sb.append(parameters.start);
		}
		sb.append(middleText);
		if (parameters.isHex) {
			sb.append(SourceConstants.HEX_PREFIX);
			sb.append(Integer.toHexString(parameters.end));
		} else {
			sb.append(parameters.end);
		}
	}

	/**
	 * Creates "Invalid directory" message
	 * 
	 * @param parameters
	 *            the message parameters
	 * @param sb
	 *            the string buffer where the message is stored
	 */
	private static void createInvalidPathMessage(
			FileErrorParameters parameters, StringBuffer sb) {
		String s;
		if (parameters != null) {
			s = Messages
					.getString("TraceBuilderErrorMessages.InvalidDirectoryPrefix"); //$NON-NLS-1$
			sb.append(s);
			sb.append(convertPath(parameters.file));
			s = Messages
					.getString("TraceBuilderErrorMessages.InvalidDirectoryPostfix"); //$NON-NLS-1$
			sb.append(s);
		} else {
			sb.append(Messages
					.getString("TraceBuilderErrorMessages.InvalidDirectory")); //$NON-NLS-1$
		}
	}

	/**
	 * Creates "File not found" message
	 * 
	 * @param parameters
	 *            the message parameters
	 * @param sb
	 *            the string buffer where the message is stored
	 */
	private static void createFileNotFoundMessage(
			FileErrorParameters parameters, StringBuffer sb) {
		String s;
		if (parameters != null) {
			s = Messages
					.getString("TraceBuilderErrorMessages.FileDoesNotExistPrefix"); //$NON-NLS-1$
			sb.append(s);
			sb.append(convertPath(parameters.file));
			s = Messages
					.getString("TraceBuilderErrorMessages.FileDoesNotExistPostfix"); //$NON-NLS-1$
			sb.append(s);
		} else {
			sb.append(Messages
					.getString("TraceBuilderErrorMessages.FileDoesNotExist")); //$NON-NLS-1$
		}
	}

	/**
	 * Adds some spaces to path string to allow folding
	 * 
	 * @param path
	 *            the path
	 * @return the converted path
	 */
	public static String convertPath(String path) {
		StringBuffer sb = new StringBuffer();
		int strIndex = -1;
		do {
			strIndex++;
			int lastIndex = strIndex;
			strIndex = path.indexOf(File.separatorChar, strIndex);
			if (strIndex != -1) {
				String sub = path.substring(lastIndex, strIndex);
				if (sub.length() > 0) {
					sb.append(sub);
					sb.append(' ');
					sb.append(File.separatorChar);
					sb.append(' ');
				}
			} else {
				// If the data ends with file separator, lastIndex points to
				// end-of-data. If not, the rest of the data is appended without
				// further white spaces
				if (lastIndex < path.length()) {
					String sub = path.substring(lastIndex);
					sb.append(sub);
				}
			}
		} while (strIndex != -1);
		return sb.toString();
	}

}
