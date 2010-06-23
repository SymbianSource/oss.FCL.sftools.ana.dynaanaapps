/*
 * Copyright (c) 2009-2010 Nokia Corporation and/or its subsidiary(-ies). 
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
 * Formatting rules are provided by Trace objects
 *
 */
package com.nokia.tracebuilder.engine.source;

import java.util.Iterator;

import com.nokia.tracebuilder.engine.TraceBuilderGlobals;
import com.nokia.tracebuilder.engine.rules.RuleUtils;
import com.nokia.tracebuilder.engine.rules.StateTraceRule;
import com.nokia.tracebuilder.engine.rules.osttrace.OstTraceFormatRule;
import com.nokia.tracebuilder.engine.rules.printf.PrintfTraceParserRule;
import com.nokia.tracebuilder.model.Trace;
import com.nokia.tracebuilder.model.TraceParameter;
import com.nokia.tracebuilder.plugin.TraceFormatConstants;
import com.nokia.tracebuilder.plugin.TraceAPIFormatter.TraceFormatType;
import com.nokia.tracebuilder.project.GroupNameHandlerBase;
import com.nokia.tracebuilder.source.SourceConstants;
import com.nokia.tracebuilder.source.SourceUtils;
import com.nokia.tracebuilder.source.SymbianConstants;

/**
 * Static functions for source formatting
 * 
 */
public class SourceFormatter {

	/**
	 * Value parameter name
	 */
	public static final String VALUE_PARAMETER_NAME = "value"; //$NON-NLS-1$	

	/**
	 * Event start trace id parameter name
	 */
	public static final String EVENT_START_TRACE_ID_PARAMETER_NAME = "linkToStart"; //$NON-NLS-1$		

	/**
	 * Numder of first parameter
	 */
	private static final int FIRST_PARAMETER = 1;

	/**
	 * Numder of second parameter
	 */
	private static final int SECOND_PARAMETER = 2;

	/**
	 * Constructor is hidden
	 */
	private SourceFormatter() {
	}

	/**
	 * Formats a trace to string format using the formatting rule from the trace
	 * 
	 * @param trace
	 *            the trace
	 * @param formatType
	 *            the type of format
	 * @return the trace string
	 */
	public static String formatTrace(Trace trace, TraceFormatType formatType) {
		return formatTrace(trace, null, formatType, null, false);
	}

	/**
	 * Formats a trace to string format
	 * 
	 * @param trace
	 *            the trace
	 * @param traceRule
	 *            the formatting rule to be used
	 * @param formatType
	 *            the type of format
	 * @param tags
	 *            the tags for parameters or null if parameter names are used
	 * @param fixedTags
	 *            true if the <i>tags</i> iterator is fixed, false if the
	 *            contents of <i>tags</i> should go through
	 *            <code>SourceRule.mapNameToSource</code>
	 * @return the trace string
	 */
	static String formatTrace(Trace trace, TraceFormattingRule traceRule,
			TraceFormatType formatType, Iterator<String> tags, boolean fixedTags) {
		StringBuffer data = new StringBuffer();
		String format = null;
		if (traceRule == null) {

			// If rule is not explicitly provided, it is fetched from the trace
			traceRule = trace.getExtension(TraceFormattingRule.class);
			if (traceRule == null) {

				// If trace does not have a formatting rule, the project API's
				// should implement default rule
				traceRule = trace.getModel().getExtension(
						TraceFormattingRule.class);
			}
		}
		if (traceRule != null) {
			format = traceRule.getFormat(trace, formatType);
		}
		if (format != null && traceRule != null) {
			data.append(format);
			data.append(SourceConstants.LINE_FEED);
			buildParameterList(trace, traceRule, data, formatType, tags,
					fixedTags);

			String traceName = traceRule.mapNameToSource(trace);
			String traceGroupName = trace.getGroup().getName();

			// %NAME% is replaced with rule-mapped trace name
			replaceData(data, traceName, TraceFormatConstants.NAME_FORMAT);
			GroupNameHandlerBase groupNameHandler = TraceBuilderGlobals.getGroupNameHandler();
			String[] defaultGroups = groupNameHandler.getDefaultGroups();
			if ((formatType == TraceFormatType.NORMAL_TRACE
					&& traceGroupName
							.equals(defaultGroups[groupNameHandler.getStateGroupIdIndex()]) && trace
					.getParameterCount() <= 1)) {

				// In case of State Trace set default values to State Machine
				// Name and State Machine State if needed

				// %STATE_MACHINE_NAME% is replaced with default machine name
				String defaultMachineNameText = Messages
						.getString("SourceFormatter.defaultStateMachineNameText"); //$NON-NLS-1$
				replaceData(data, defaultMachineNameText,
						TraceFormatConstants.STATE_MACHINE_NAME_FORMAT);

				TraceParameter machineParameter = new TraceParameter(trace);
				machineParameter
						.setName(PrintfTraceParserRule.DEFAULT_PARAMETER_NAME
								+ FIRST_PARAMETER);
				machineParameter.setType(TraceParameter.ASCII);

				// %STATE_MACHINE_STATE% is replaced with default state
				String defaultStateText = Messages
						.getString("SourceFormatter.defaultStateMachineStateText"); //$NON-NLS-1$
				replaceData(data, defaultStateText,
						TraceFormatConstants.STATE_MACHINE_STATE_FORMAT);

				TraceParameter stateParameter = new TraceParameter(trace);
				stateParameter
						.setName(PrintfTraceParserRule.DEFAULT_PARAMETER_NAME
								+ SECOND_PARAMETER);
				stateParameter.setType(TraceParameter.ASCII);
			} else if ((formatType == TraceFormatType.COMPLEX_TRACE
					&& traceGroupName
							.equals(defaultGroups[groupNameHandler.getStateGroupIdIndex()]) && trace
					.getParameterCount() > 1)) {

				// %STATE_MACHINE_NAME% is replaced with empty string
				replaceData(data, "", //$NON-NLS-1$
						TraceFormatConstants.STATE_MACHINE_NAME_FORMAT);

				// %STATE_MACHINE_STATE% is replaced with empty string
				replaceData(data, "", //$NON-NLS-1$
						TraceFormatConstants.STATE_MACHINE_STATE_FORMAT);
			} else if (formatType == TraceFormatType.NORMAL_TRACE
					&& traceGroupName
							.equals(defaultGroups[groupNameHandler.getPerformanceGroupIdIndex()])
					&& (data.toString()
							.startsWith(OstTraceFormatRule.OST_TRACE_EVENT_START_TAG))) {

				// %EVENT_NAME% is replaced with event name
				replaceData(data, "\"" + trace.getTrace() //$NON-NLS-1$
						+ "\"", TraceFormatConstants.EVENT_NAME_FORMAT); //$NON-NLS-1$

				// In case of OstTraceEventStart1 add value parameter if it is
				// not yet added
				if (data.toString().startsWith(
						OstTraceFormatRule.OST_TRACE_EVENT_START_TAG + "1") //$NON-NLS-1$
						&& trace.getParameterCount() < 1) {
					TraceParameter valueParameter = new TraceParameter(trace);
					valueParameter.setName(VALUE_PARAMETER_NAME);
					valueParameter.setType(TraceParameter.SDEC32);
				}
			} else if ((formatType == TraceFormatType.NORMAL_TRACE
					&& traceGroupName
							.equals(defaultGroups[groupNameHandler.getPerformanceGroupIdIndex()]) 
							&& trace.getParameterCount() < 1)
					&& data.toString().startsWith(
							OstTraceFormatRule.OST_TRACE_EVENT_STOP_TAG)) {

				// %EVENT_NAME% is replaced with event name
				replaceData(data, "\"" + trace.getTrace() //$NON-NLS-1$
						+ "\"", TraceFormatConstants.EVENT_NAME_FORMAT); //$NON-NLS-1$

				// In case of Performance Event Stop Trace set value to Event
				// Start Trace ID
				int exitNameSuffixLength = RuleUtils.EXIT_NAME_SUFFIXES[RuleUtils.TYPE_PERF_EVENT]
						.length();
				String startTraceName = traceName.substring(0, traceName
						.length()
						- exitNameSuffixLength)
						+ RuleUtils.ENTRY_NAME_SUFFIXES[RuleUtils.TYPE_PERF_EVENT];
				replaceData(data, startTraceName,
						TraceFormatConstants.EVENT_START_TRACE_NAME_FORMAT);

				// Create event start trace id parameter
				TraceParameter eventStartTraceIDParameter = new TraceParameter(
						trace);
				eventStartTraceIDParameter
						.setName(EVENT_START_TRACE_ID_PARAMETER_NAME);
				eventStartTraceIDParameter.setType(TraceParameter.UDEC32);
			} else if ((formatType == TraceFormatType.NORMAL_TRACE
					&& traceGroupName
							.equals(defaultGroups[groupNameHandler.getPerformanceGroupIdIndex()]) 
							&& trace.getParameterCount() < 1)
					&& data.toString().startsWith(
							OstTraceFormatRule.OST_TRACE_EVENT_STOP_TAG)) {

				// "%EVENT_NAME%, " is replaced with empty string
				String stringToBeReplaced = TraceFormatConstants.EVENT_NAME_FORMAT
						+ ", "; //$NON-NLS-1$
				replaceData(data, "", stringToBeReplaced); //$NON-NLS-1$

				// %EVENT_START_TRACE_NAME% is replaced with empty string
				replaceData(data, "", //$NON-NLS-1$
						TraceFormatConstants.EVENT_START_TRACE_NAME_FORMAT);
			}

			// %GROUP% is replaced with group name
			replaceData(data, traceGroupName, TraceFormatConstants.GROUP_FORMAT);

			// %TEXT% is replaced with trace text
			replaceData(data, "\"" + trace.getTrace() //$NON-NLS-1$
					+ "\"", TraceFormatConstants.TEXT_FORMAT); //$NON-NLS-1$

			// %FORMATTED_TRACE% is replaced with trace data
			replaceData(data, trace.getTrace(),
					TraceFormatConstants.FORMATTED_TRACE);

			// Comment is inserted before the trace
			int index = data.indexOf(TraceFormatConstants.COMMENT_FORMAT);
			if (index >= 0) {
				String comment = data.substring(index + 1);
				data.delete(index, data.length());
				data.insert(0, comment);
				data.append(SourceConstants.LINE_FEED);
			}
		}

		// If trace does not have formatting, it is not shown in source
		return data.toString();
	}

	/**
	 * Adds the parameters to the data buffer
	 * 
	 * @param trace
	 *            the trace
	 * @param format
	 *            the formatter from trace
	 * @param data
	 *            the data buffer where the formatted data is stored
	 * @param formatType
	 *            the format type to be applied
	 * @param tags
	 *            the tags for parameters or null if parameter names are used
	 * @param fixedTags
	 *            true if the <i>tags</i> iterator is fixed, false if the
	 *            contents of <i>tags</i> should go through
	 *            <code>SourceRule.mapNameToSource</code>
	 */
	private static void buildParameterList(Trace trace,
			TraceFormattingRule format, StringBuffer data,
			TraceFormatType formatType, Iterator<String> tags, boolean fixedTags) {

		// Auto add value parameter in case of Performance Event Stop header
		// trace
		GroupNameHandlerBase groupNameHandler = TraceBuilderGlobals.getGroupNameHandler();
		if (trace.getGroup().getName().endsWith(
				groupNameHandler.getDefaultGroups()[groupNameHandler.getPerformanceGroupIdIndex()])
				&& formatType == TraceFormatType.HEADER) {
			// Add value parameter before Event Start Trace ID parameter
			TraceParameter eventStartTraceIDParameter = trace.getParameter(0);
			trace.removeParameterAt(0);
			TraceParameter valueParameter = new TraceParameter(trace);
			valueParameter.setName(VALUE_PARAMETER_NAME);
			valueParameter.setType(TraceParameter.SDEC32);
			trace.addParameter(eventStartTraceIDParameter);
		}

		int count = trace.getParameterCount();

		Iterator<TraceParameter> itr = trace.getParameters();
		StringBuffer paramList = new StringBuffer();
		// Index is incremented by one for each parameter that has been added to
		// source
		int parameterIndex = 0;
		while (itr.hasNext()) {
			TraceParameter param = itr.next();
			TraceParameterFormattingRule rule = param
					.getExtension(TraceParameterFormattingRule.class);
			String name;
			// Count and name may be adjusted by rules provided by parameters
			if (rule != null) {
				boolean isInSource = rule.isShownInSource();
				// If the parameter iterator is explicitly provided, the
				// parameter name is fetched from it. If the parameter list does
				// not have enough tags (for example when a new parameter is
				// added to trace) the name of the parameter is used. The source
				// rule is used to map the parameter name to correct format
				if (isInSource) {
					if (formatType == TraceFormatType.NORMAL_TRACE
							|| formatType == TraceFormatType.COMPLEX_TRACE) {
						name = getMappedTag(tags, fixedTags, param, rule);
					} else {
						name = getTagWithoutMapping(tags, param);
					}
					addParameter(paramList, param, name, ++parameterIndex,
							formatType);
				} else {
					// If the parameter is not shown in source, it is skipped
					count--;
				}
			} else {
				// If the parameter is not associated with a source rule, it is
				// added without mapping
				name = getTagWithoutMapping(tags, param);
				addParameter(paramList, param, name, ++parameterIndex,
						formatType);
			}
		}
		// %PC% is replaced with adjusted parameter count
		// In case of packed trace, the header engine does the count mapping
		if (formatType != TraceFormatType.TRACE_PACKED) {
			String val = format.mapParameterCountToSource(trace, count);

			if (trace.getExtension(StateTraceRule.class) != null
					&& data.toString().startsWith("OstTraceState")) { //$NON-NLS-1$

				// In case of State Trace macro value in trace macro is
				// parameter count - 2
				if (count > 1) {
					val = String.valueOf(count - 2); // CodForChk_Dis_Magic
				} else {
					val = String.valueOf(count);
				}
			}
			replaceData(data, val, TraceFormatConstants.PARAM_COUNT_FORMAT);
		}
		// %PARAMETERS% is replaced with parameter names
		replaceData(data, paramList.toString(),
				TraceFormatConstants.PARAMETERS_FORMAT);
	}

	/**
	 * Gets the name for a parameter without source rule mapping. If the tags
	 * iterator contains a valid entry, the name is fetched from it. If not, the
	 * parameter name is used instead.
	 * 
	 * @param tags
	 *            the list of tags
	 * @param param
	 *            the parameter
	 * @return the parameter name
	 */
	private static String getTagWithoutMapping(Iterator<String> tags,
			TraceParameter param) {
		String name;
		// If the parameter iterator is explicitly provided, the
		// parameter name is fetched from it
		if (tags != null && tags.hasNext()) {
			name = tags.next();
			// The list may contain 0-length items to represent
			// that that parameter name should be used instead
			if (name == null || name.length() == 0) {
				name = param.getName();
			}
		} else {
			name = param.getName();
		}
		return name;
	}

	/**
	 * Gets the name for a parameter using source rule mapping. If the tags
	 * iterator contains a valid entry, the name is fetched from it and if the
	 * fixedTags flag is false, the name is mapped using the source rule. If the
	 * iterator does not contain a valid entry, the parameter name is used and
	 * the mapping is done regardless of the fixedTag value.
	 * 
	 * @param tags
	 *            the tags iterator
	 * @param fixedTags
	 *            false if the tag from iterator should be passed through the
	 *            source rule
	 * @param param
	 *            the parameter
	 * @param rule
	 *            the formatting rule for the parameter
	 * @return the parameter name
	 */
	private static String getMappedTag(Iterator<String> tags,
			boolean fixedTags, TraceParameter param,
			TraceParameterFormattingRule rule) {
		String name;
		boolean nameFromTag;
		// If the parameter iterator is explicitely provided,
		// the parameter name is fetched from it
		if (tags != null && tags.hasNext()) {
			name = tags.next();
			nameFromTag = true;
			// The list may contain 0-length items to represent
			// that that parameter name should be used instead
			if (name == null || name.length() == 0) {
				name = param.getName();
				nameFromTag = false;
			}
		} else {
			name = param.getName();
			nameFromTag = false;
		}
		// If tags are fixed and the name was from the tags
		// list, the source mapping is not done
		if (!fixedTags || !nameFromTag) {
			name = rule.mapNameToSource(name);
		}
		return name;
	}

	/**
	 * Adds a parameter to the parameter list
	 * 
	 * @param paramList
	 *            the parameter list
	 * @param param
	 *            the parameter to be added
	 * @param name
	 *            a name replacement for the parameter
	 * @param parameterIndex
	 *            the index of the parameter
	 * @param formatType
	 *            the type of the format
	 */
	private static void addParameter(StringBuffer paramList,
			TraceParameter param, String name, int parameterIndex,
			TraceFormatType formatType) {
		paramList.append(SourceConstants.PARAMETER_SEPARATOR);
		if (formatType == TraceFormatType.HEADER) {
			paramList.append(SourceUtils.mapParameterTypeToSymbianType(param));
			paramList.append(SourceConstants.SPACE_CHAR);
			paramList.append(SymbianConstants.PARAMETER_DECLARATION_PREFIX);
			paramList.append(parameterIndex);
		} else if (formatType == TraceFormatType.NORMAL_TRACE
				|| formatType == TraceFormatType.COMPLEX_TRACE) {
			paramList.append(name);
		} else if (formatType == TraceFormatType.EMPTY_MACRO) {
			paramList.append(SymbianConstants.PARAMETER_DECLARATION_PREFIX);
			paramList.append(parameterIndex);
		}
	}

	/**
	 * Replaces data from the stringbuffer
	 * 
	 * @param data
	 *            the data
	 * @param replaceData
	 *            the data to be used
	 * @param replaceFormat
	 *            the format to be replaced
	 */
	private static void replaceData(StringBuffer data, String replaceData,
			String replaceFormat) {
		int replaceOffset = 0;
		do {
			replaceOffset = data.indexOf(replaceFormat, replaceOffset);
			if (replaceOffset >= 0) {
				data.replace(replaceOffset, replaceOffset
						+ replaceFormat.length(), replaceData);
				replaceOffset += replaceData.length();
			}
		} while (replaceOffset != -1);
	}

}
