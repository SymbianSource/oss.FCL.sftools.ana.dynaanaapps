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
 * Parser for printf traces
 *
 */
package com.nokia.tracebuilder.engine.rules.printf;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.nokia.tracebuilder.engine.TraceLocation;
import com.nokia.tracebuilder.engine.TraceBuilderErrorCodes.StringErrorParameters;
import com.nokia.tracebuilder.engine.TraceBuilderErrorCodes.TraceBuilderErrorCode;
import com.nokia.tracebuilder.engine.rules.SourceParserRuleBase;
import com.nokia.tracebuilder.engine.source.SourceParserResult;
import com.nokia.tracebuilder.engine.utils.TraceUtils;
import com.nokia.tracebuilder.model.TraceBuilderException;
import com.nokia.tracebuilder.source.FormatMapping;
import com.nokia.tracebuilder.source.SourceConstants;
import com.nokia.tracebuilder.source.SourceUtils;

/**
 * Parser for printf traces
 * 
 */
public class PrintfTraceParserRule extends SourceParserRuleBase {

	/**
	 * Default name for a parameter
	 */
	public static final String DEFAULT_PARAMETER_NAME = "arg"; //$NON-NLS-1$

	/**
	 * Name of this parser
	 */
	private static final String PARSER_NAME = "PrintfParser"; //$NON-NLS-1$

	/**
	 * Pattern for trimming c++ cast operator away from trace parameters
	 */
	private Pattern cppCastTrimPattern = Pattern.compile("\\s*(.+<.+>)\\s*"); //$NON-NLS-1$

	/**
	 * Pattern for trimming c cast operator away from trace parameters
	 */
	private Pattern cCastTrimPattern = Pattern.compile("(\\([^(]+?\\))"); //$NON-NLS-1$

	/**
	 * Pattern for getting a valid token from parameter data
	 */
	private Pattern parameterNamePattern = Pattern
			.compile("[a-zA-Z][a-zA-Z\\d]*"); //$NON-NLS-1$

	/**
	 * Symbian literal tags
	 */
	private String[] TRACE_TEXT_TAGS = { "_L8", //$NON-NLS-1$
			"_T8", //$NON-NLS-1$
			"_L", //$NON-NLS-1$
			"_T" //$NON-NLS-1$
	};

	/**
	 * Constructor
	 * 
	 * @param tag
	 *            the tag to be found from source
	 */
	public PrintfTraceParserRule(String tag) {
		super(tag, null);
	}

	/**
	 * Constructor
	 * 
	 * @param tag
	 *            the tag to be searched from source
	 * @param tagSuffixes
	 *            the list of allowed suffixes to the tag
	 */
	public PrintfTraceParserRule(String tag, String[] tagSuffixes) {
		super(tag, tagSuffixes);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.engine.source.SourceParserRule#getName()
	 */
	public String getName() {
		return PARSER_NAME;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.engine.source.SourceParserRule#
	 *      parseParameters(java.util.List)
	 */
	public SourceParserResult parseParameters(String tag, List<String> list)
			throws TraceBuilderException {
		SourceParserResult result = new SourceParserResult();
		result.traceText = trimTraceText(list.get(0));
		result.originalName = result.traceText;
		result.convertedName = TraceUtils.convertName(
				SourceUtils.removePrintfFormatting(result.traceText))
				.toUpperCase();
		result.parameters = list.subList(1, list.size());
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.engine.rules.SourceParserRuleBase#getLocationGroup()
	 */
	@Override
	public String getLocationGroup() {
		return Messages.getString("PrintfTraceParserRule.Title"); //$NON-NLS-1$
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.engine.rules.SourceParserRuleBase#
	 *      convertLocation(com.nokia.tracebuilder.engine.TraceLocation)
	 */
	@Override
	public TraceConversionResult convertLocation(TraceLocation location)
			throws TraceBuilderException {
		// All parameter types are supported by default -> false is passed to
		// buildParameterTypeList.
		List<FormatMapping> types = buildParameterTypeList(location
				.getTraceText(), false);
		// Parameter count must match the number of format elements in trace
		// text -> true is passed to convertLocation by default
		return convertLocation(location, true, types);
	}

	/**
	 * Converts a location to trace
	 * 
	 * @param location
	 *            the location to be converted
	 * @param useParametersFromLocation
	 *            true if location parameters need to be checked
	 * @param types
	 *            the parameter types list
	 * @return the conversion result
	 * @throws TraceBuilderException
	 *             if conversion fails
	 */
	protected TraceConversionResult convertLocation(TraceLocation location,
			boolean useParametersFromLocation, List<FormatMapping> types)
			throws TraceBuilderException {
		TraceConversionResult result = new TraceConversionResult();
		result.text = location.getTraceText();

		// If trace text is in multiple lines, remove extra characters
		int removableAreaBeging = -1;
		for (int i = 0; i < result.text.length(); i++) {
			char c = result.text.charAt(i);
			if (c == SourceConstants.QUOTE_CHAR
					&& (i == 0 || result.text.charAt(i - 1) != SourceConstants.BACKSLASH_CHAR)) {
				if (removableAreaBeging != -1) {
					String startString = result.text.substring(0,
							removableAreaBeging);
					String endString = result.text.substring(i + 1, result.text
							.length());
					result.text = startString + endString;
					i = removableAreaBeging;
					removableAreaBeging = -1;
				} else {
					removableAreaBeging = i;
				}
			}
		}

		result.name = location.getConvertedName();

		// If location parameters are used, the count must match the number of
		// formats parsed from the trace string.
		if (!useParametersFromLocation
				|| (types.size() == location.getParameterCount() && numberOfParametersInTagMatchSize(
						location.getTag(), types.size()))) {
			result.parameters = new ArrayList<ParameterConversionResult>(types
					.size());
			Iterator<String> itr = location.getParameters();
			// The FormatMapping objects are converted to
			// ParameterConversionResult objects:
			// - Name is associated to the parameter
			// - Extensions are created
			for (int i = 0; i < types.size(); i++) {
				FormatMapping mapping = types.get(i);
				String name;
				if (useParametersFromLocation) {
					name = TraceUtils.convertName(trimParameter(itr.next(), i));
				} else {
					name = DEFAULT_PARAMETER_NAME + (i + 1);
				}
				ParameterConversionResult param = mapFormatToConversionResult(mapping);
				param.name = modifyDuplicateName(result.parameters, name);
				result.parameters.add(param);
			}
		} else {
			throw new TraceBuilderException(
					TraceBuilderErrorCode.PARAMETER_FORMAT_MISMATCH, null,
					location);
		}
		return result;
	}

	/**
	 * Checks if the number of the parameters in the trace tag matches the size
	 * of parameter list
	 * 
	 * @param tag
	 *            the trace tag
	 * @param size
	 *            size of the parameter list
	 * @return true is parameter count matches, false otherwise
	 */
	private boolean numberOfParametersInTagMatchSize(String tag, int size) {
		boolean matches = true;
		// Parse the last character of the tag to a integer
		if (tag != null) {
			try {

				// Check the tag parameter count and compare it to the size of
				// the parameter list
				String tagParamStr = tag.substring(tag.length() - 1);
				int tagParamCount = Integer.parseInt(tagParamStr);

				if (tagParamCount != size) {
					matches = false;
				}
			} catch (NumberFormatException e) {
			}
		}
		return matches;
	}

	/**
	 * Builds the parameter type array which is passed to convertLocation
	 * 
	 * @param text
	 *            the trace text
	 * @param simpleParameters
	 *            true if only simple types are supported
	 * @return the list of types
	 * @throws TraceBuilderException
	 *             if parser fails
	 */
	protected List<FormatMapping> buildParameterTypeList(String text,
			boolean simpleParameters) throws TraceBuilderException {
		Matcher matcher = SourceUtils.traceTextPattern.matcher(text);
		ArrayList<FormatMapping> types = new ArrayList<FormatMapping>();
		boolean found = true;
		do {
			found = matcher.find();
			if (found) {
				String tag = matcher.group();
				FormatMapping mapping = SourceUtils
						.mapFormatToParameterType(tag);
				if (simpleParameters && !mapping.isSimple) {
					StringErrorParameters param = new StringErrorParameters();
					param.string = tag;
					throw new TraceBuilderException(
							TraceBuilderErrorCode.PARAMETER_FORMAT_NEEDS_EXT_MACRO,
							param, null);
				}
				types.add(mapping);
			}
		} while (found);
		return types;
	}

	/**
	 * Changes a duplicate parameter name to unique
	 * 
	 * @param parameters
	 *            the list of existing parameters
	 * @param name
	 *            the name
	 * @return the modified name
	 */
	private String modifyDuplicateName(
			List<ParameterConversionResult> parameters, String name) {
		String retval = name;
		for (ParameterConversionResult result : parameters) {
			if (result.name.equals(name)) {
				retval = name + (parameters.size() + 1);
				break;
			}
		}
		return retval;
	}

	/**
	 * Trims extra stuff away from trace text
	 * 
	 * @param data
	 *            the data to be trimmed
	 * @return trimmed text
	 */
	protected String trimTraceText(String data) {
		// Removes literal macros
		for (String element : TRACE_TEXT_TAGS) {
			if (data.startsWith(element)) {
				data = data.substring(element.length());
			}
		}
		data = data.trim();
		// Removes the opening bracket and quotes
		data = removeBrackets(data);
		if (data.startsWith("\"") //$NON-NLS-1$
				&& data.length() >= 2) { // CodForChk_Dis_Magic
			data = data.substring(1, data.length() - 1);
		}
		return data;
	}

	/**
	 * Removes the brackets around the given data
	 * 
	 * @param data
	 *            the data
	 * @return the modified data
	 */
	protected String removeBrackets(String data) {
		boolean canRemove = true;
		while (data.startsWith("(") //$NON-NLS-1$
				&& data.endsWith(")") //$NON-NLS-1$
				&& canRemove) {
			// If closing bracket is found first, the first bracket cannot be
			// removed.
			// TODO: Does not work with for example ((Cast)abc).Func()
			// -> (Cast)abc).Func(
			for (int i = 1; i < data.length() - 1 && canRemove; i++) {
				if (data.charAt(i) == '(') {
					i = data.length();
				} else if (data.charAt(i) == ')') {
					canRemove = false;
				}
			}
			if (canRemove) {
				data = data.substring(1, data.length() - 1).trim();
			}
		}
		return data;
	}

	/**
	 * Trims extra stuff away from a parameter to create a parameter label
	 * 
	 * @param data
	 *            the parameter data
	 * @param index
	 *            the parameter index. The index will be used as label if the
	 *            label cannot be parsed
	 * @return the parameter label
	 */
	protected String trimParameter(String data, int index) {
		// Removes possible literal macros
		data = trimTraceText(data);
		// Removes casting operations.
		Matcher matcher = cppCastTrimPattern.matcher(data);
		data = removeBrackets(matcher.replaceFirst("")); //$NON-NLS-1$
		matcher = cCastTrimPattern.matcher(data);
		data = matcher.replaceFirst(""); //$NON-NLS-1$
		// Finds the next valid token from the data
		matcher = parameterNamePattern.matcher(data);
		if (matcher.find()) {
			data = matcher.group();
		} else {
			data = DEFAULT_PARAMETER_NAME + (index + 1);
		}
		return data;
	}

}
