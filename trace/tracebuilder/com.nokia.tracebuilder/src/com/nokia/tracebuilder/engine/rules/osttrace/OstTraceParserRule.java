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
 * OST trace parser rule
 *
 */
package com.nokia.tracebuilder.engine.rules.osttrace;

import java.util.ArrayList;
import java.util.List;

import com.nokia.tracebuilder.engine.TraceBuilderGlobals;
import com.nokia.tracebuilder.engine.TraceLocation;
import com.nokia.tracebuilder.engine.TraceBuilderErrorCodes.TraceBuilderErrorCode;
import com.nokia.tracebuilder.engine.rules.AutoAddFunctionParametersRule;
import com.nokia.tracebuilder.engine.rules.AutoAddReturnParameterRule;
import com.nokia.tracebuilder.engine.rules.AutoAddThisPtrRule;
import com.nokia.tracebuilder.engine.rules.AutomaticTraceTextRule;
import com.nokia.tracebuilder.engine.rules.ComplexHeaderRuleImpl;
import com.nokia.tracebuilder.engine.rules.EntryTraceRule;
import com.nokia.tracebuilder.engine.rules.ExitTraceRule;
import com.nokia.tracebuilder.engine.rules.PerformanceEventStartRule;
import com.nokia.tracebuilder.engine.rules.PerformanceEventStopRule;
import com.nokia.tracebuilder.engine.rules.ReadOnlyObjectRuleImpl;
import com.nokia.tracebuilder.engine.rules.StateTraceRule;
import com.nokia.tracebuilder.engine.rules.printf.PrintfTraceParserRule;
import com.nokia.tracebuilder.engine.source.SourceParserResult;
import com.nokia.tracebuilder.model.TraceBuilderException;
import com.nokia.tracebuilder.model.TraceModelExtension;
import com.nokia.tracebuilder.model.TraceParameter;
import com.nokia.tracebuilder.project.GroupNameHandlerBase;
import com.nokia.tracebuilder.source.FormatMapping;
import com.nokia.tracebuilder.source.SourceContext;
import com.nokia.tracebuilder.source.SourceParser;

/**
 * OST trace parser rule
 * 
 */
public final class OstTraceParserRule extends PrintfTraceParserRule {

	/**
	 * Data trace parameter count
	 */
	private static final int DATA_PARAMETER_COUNT = 2; // CodForChk_Dis_Magic

	/**
	 * Entry Ext trace parameter count
	 */
	private static final int ENTRY_EXT_PARAMETER_COUNT = 1; // CodForChk_Dis_Magic

	/**
	 * Exit Ext trace parameter count
	 */
	private static final int EXIT_EXT_PARAMETER_COUNT = 2; // CodForChk_Dis_Magic

	/**
	 * Trace parameter index list
	 * 
	 */
	class TraceParameterIndexList {

		/**
		 * Minimum number of parameters needed to decode traces
		 */
		int minParamCount;

		/**
		 * Preprocessor level index
		 */
		int levelIndex;

		/**
		 * Trace name index
		 */
		int nameIndex;

		/**
		 * Trace text index
		 */
		int textIndex;

		/**
		 * Trace group index in case group name is free-form
		 */
		int groupIndex;

		/**
		 * Trace group name in case group name is pre-determined by rules
		 */
		String groupName;
	}

	/**
	 * List of flags related to OST API macro
	 * 
	 */
	class TraceParameterFlagList {

		/**
		 * Data tag
		 */
		boolean hasDataTag;

		/**
		 * State tag
		 */
		boolean hasStateTag;

		/**
		 * Ext tag
		 */
		boolean hasExtTag;

		/**
		 * Event start tag
		 */
		boolean hasEventStartTag;

		/**
		 * Event stop
		 */
		boolean hasEventStopTag;

		/**
		 * Function entry
		 */
		boolean hasFunctionEntryTag;

		/**
		 * Function exit
		 */
		boolean hasFunctionExitTag;

		/**
		 * Constructor
		 * 
		 * @param tag
		 *            the trace tag
		 */
		TraceParameterFlagList(String tag) {
			hasDataTag = tag.indexOf(OstConstants.DATA_TRACE_TAG) > 0;
			hasStateTag = tag.indexOf(OstConstants.STATE_TRACE_TAG) > 0;
			hasExtTag = tag.indexOf(OstConstants.EXTENSION_TRACE_TAG) > 0;
			hasEventStartTag = tag
					.indexOf(OstConstants.PERFORMANCE_EVENT_START_TAG) > 0;
			hasEventStopTag = tag
					.indexOf(OstConstants.PERFORMANCE_EVENT_STOP_TAG) > 0;
			hasFunctionEntryTag = tag.indexOf(OstConstants.FUNCTION_ENTRY_TAG) > 0;
			hasFunctionExitTag = tag.indexOf(OstConstants.FUNCTION_EXIT_TAG) > 0;
		}

		/**
		 * Checks if any of the flags is set
		 * 
		 * @return true if flag is set
		 */
		boolean hasFlags() {
			return hasDataTag || hasStateTag || hasExtTag || hasEventStartTag
					|| hasEventStopTag || hasFunctionEntryTag
					|| hasFunctionExitTag;
		}

	}

	/**
	 * Offset to preprocessor level
	 */
	private static final int PREPROCESSOR_LEVEL_OFFSET = 0; // CodForChk_Dis_Magic

	/**
	 * Offset to group name if preprocessor level is not in use
	 */
	private static final int GROUP_NAME_OFFSET = 0; // CodForChk_Dis_Magic

	/**
	 * Offset to trace name if preprocessor level is not in use
	 */
	private static final int TRACE_NAME_OFFSET = 1; // CodForChk_Dis_Magic

	/**
	 * Offset to trace text if preprocessor level is not in use
	 */
	private static final int TRACE_TEXT_OFFSET = 2; // CodForChk_Dis_Magic

	/**
	 * Minimum number of parameters if preprocessor level is not in use
	 */
	private static final int MIN_PARAMETER_COUNT = 3; // CodForChk_Dis_Magic

	/**
	 * Parser tag
	 */
	private static final String OST_TRACE_PARSER_TAG = "OstTrace"; //$NON-NLS-1$

	/**
	 * OstTrace parser formats
	 */
	private final static String[] OST_TRACE_PARSER_FORMATS = { "0", //$NON-NLS-1$
			"1", //$NON-NLS-1$
			"Data", //$NON-NLS-1$
			"Ext?", //$NON-NLS-1$
			"FunctionEntry0", //$NON-NLS-1$
			"FunctionEntry1", //$NON-NLS-1$
			"FunctionEntryExt", //$NON-NLS-1$
			"FunctionExit0", //$NON-NLS-1$
			"FunctionExit1", //$NON-NLS-1$
			"FunctionExitExt", //$NON-NLS-1$
			"EventStart0", //$NON-NLS-1$
			"EventStart1", //$NON-NLS-1$
			"EventStop", //$NON-NLS-1$
			"Def0", //$NON-NLS-1$
			"Def1", //$NON-NLS-1$
			"DefData", //$NON-NLS-1$
			"DefExt?", //$NON-NLS-1$
			"State0", //$NON-NLS-1$
			"State1" //$NON-NLS-1$
	};

	/**
	 * Creates a new OstTrace parser rule
	 */
	public OstTraceParserRule() {
		super(OST_TRACE_PARSER_TAG, OST_TRACE_PARSER_FORMATS);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.engine.source.SourceParserRule#getName()
	 */
	@Override
	public String getName() {
		return OstTraceFormatRule.STORAGE_NAME;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.engine.rules.printf.PrintfTraceParserRule#parseParameters(java.util.List)
	 */
	@Override
	public SourceParserResult parseParameters(String tag, List<String> list)
			throws TraceBuilderException {
		SourceParserResult result = new SourceParserResult();
		TraceParameterIndexList indexList = getIndexList(tag);
		if (list.size() >= indexList.minParamCount) {
			// Name must exist
			result.originalName = list.get(indexList.nameIndex);
			result.convertedName = result.originalName;
			// Text is optional
			if (indexList.textIndex >= 0) {
				result.traceText = trimTraceText(list.get(indexList.textIndex));
			} else {
				result.traceText = ""; //$NON-NLS-1$
			}
			// Group ID and preprocessor level are stored into the
			// parser-specific data
			result.parserData = new ArrayList<String>();
			if (indexList.levelIndex >= 0) {
				result.parserData.add(list.get(indexList.levelIndex));
			}
			if (indexList.groupIndex >= 0) {
				result.parserData.add(list.get(indexList.groupIndex));
			} else if (indexList.groupName != null) {
				result.parserData.add(indexList.groupName);
			}

			// Extra parameters are converted to trace parameters
			result.parameters = new ArrayList<String>();
			for (int i = indexList.minParamCount; i < list.size(); i++) {
				result.parameters.add(list.get(i));
			}
		} else {
			throw new TraceBuilderException(
					TraceBuilderErrorCode.NOT_ENOUGH_PARAMETERS);
		}
		return result;
	}

	/**
	 * Gets the parameter index list based on trace tag
	 * 
	 * @param tag
	 *            the trace tag
	 * @return the index list
	 */
	private TraceParameterIndexList getIndexList(String tag) {
		TraceParameterIndexList indexes = new TraceParameterIndexList();
		indexes.levelIndex = -1;
		GroupNameHandlerBase groupNameHandler = TraceBuilderGlobals.getGroupNameHandler();
		String[] defaultGroups = groupNameHandler.getDefaultGroups();
		if (tag.indexOf(OstConstants.FUNCTION_ENTRY_TAG) > 0
				|| tag.indexOf(OstConstants.FUNCTION_EXIT_TAG) > 0) {
			indexes.minParamCount = 1; // Name is mandatory
			indexes.textIndex = -1; // No trace text
			indexes.nameIndex = 0; // Trace name at index 0
			indexes.groupIndex = -1; // Group is fixed to TRACE_FLOW
			indexes.groupName = defaultGroups[groupNameHandler.getFlowGroupIdIndex()];
		} else if (tag.indexOf(OstConstants.STATE_TRACE_TAG) > 0) {
			indexes.minParamCount = 1; // Name is mandatory
			indexes.textIndex = -1; // No trace text
			indexes.nameIndex = 0; // Trace name at index 0
			indexes.groupIndex = -1; // Group is fixed to TRACE_STATE
			indexes.groupName = defaultGroups[groupNameHandler.getStateGroupIdIndex()];
		} else if (tag.indexOf(OstConstants.PERFORMANCE_EVENT_START_TAG) > 0) {
			// Name and event name are mandatory
			indexes.minParamCount = 2; // CodForChk_Dis_Magic
			indexes.textIndex = 1; // Trace text at index 1
			indexes.nameIndex = 0; // Trace name at index 0
			indexes.groupIndex = -1; // Group is fixed to TRACE_PERFORMANCE
			indexes.groupName = defaultGroups[groupNameHandler.getPerformanceGroupIdIndex()];
		} else if (tag.indexOf(OstConstants.PERFORMANCE_EVENT_STOP_TAG) > 0) {
			//  Name and event name are mandatory
			indexes.minParamCount = 2; // CodForChk_Dis_Magic
			indexes.textIndex = 1; // Trace text at index 1
			indexes.nameIndex = 0; // Trace name at index 0
			indexes.groupIndex = -1; // Group is fixed to TRACE_PERFORMANCE
			indexes.groupName = defaultGroups[groupNameHandler.getPerformanceGroupIdIndex()];
		} else {
			indexes.minParamCount = MIN_PARAMETER_COUNT;
			indexes.textIndex = TRACE_TEXT_OFFSET;
			indexes.nameIndex = TRACE_NAME_OFFSET;
			indexes.groupIndex = GROUP_NAME_OFFSET;
		}
		// If the trace macro contains preprocessor level, the offsets are
		// incremented by one
		if (tag.indexOf(OstConstants.PREPROCESSOR_LEVEL_TAG) > 0) {
			indexes.minParamCount++;
			if (indexes.textIndex >= 0) {
				indexes.textIndex++;
			}
			if (indexes.nameIndex >= 0) {
				indexes.nameIndex++;
			}
			if (indexes.groupIndex >= 0) {
				indexes.groupIndex++;
			}
			indexes.levelIndex = PREPROCESSOR_LEVEL_OFFSET;
		}
		return indexes;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.engine.rules.printf.PrintfTraceParserRule#
	 *      convertLocation(com.nokia.tracebuilder.engine.TraceLocation)
	 */
	@Override
	public TraceConversionResult convertLocation(TraceLocation location)
			throws TraceBuilderException { // CodForChk_Dis_ComplexFunc
		TraceParameterFlagList flags = checkParameterCount(location);

		// Data tag does not have parameters
		boolean checkParameters = !flags.hasDataTag;

		List<FormatMapping> typeList;
		if (flags.hasExtTag
				&& (flags.hasFunctionEntryTag || flags.hasFunctionExitTag)) {
			// Parameters are generated by AutoAdd rules
			typeList = new ArrayList<FormatMapping>();
			checkParameters = false;
		} else if (!flags.hasFlags() || flags.hasDataTag || flags.hasExtTag) {
			// If the Ext, Data or EventStart tag is present, all formats
			// are supported. If no flags is set, only 32-bit formats are
			// supported.
			typeList = buildParameterTypeList(location.getTraceText(),
					!flags.hasDataTag && !flags.hasExtTag);
		} else if (flags.hasEventStartTag) {
			// In case of Start1 tag value parameter is supported
			typeList = new ArrayList<FormatMapping>();
			// Check that does optional value exist
			if (location.getParameterCount() == 1) {
				FormatMapping mapping = new FormatMapping(TraceParameter.SDEC32);
				mapping.isSimple = true;
				typeList.add(mapping);
			}
			checkParameters = false;
		} else if (flags.hasEventStopTag) {
			// If the Event stop tag is presented, start event trace
			// id parameter is supported
			typeList = new ArrayList<FormatMapping>();
			FormatMapping mapping = new FormatMapping(TraceParameter.UDEC32);
			mapping.isSimple = true;
			typeList.add(mapping);
			checkParameters = false;

		} else if (flags.hasStateTag) {
			// If the State tag is presented, two ascii parameters are supported
			// in case of State0 tag (parameter count = 2). In case of State1
			// tag (parameter count = 3) two ascii and one 32-bit hex parameters
			// are supported
			typeList = new ArrayList<FormatMapping>();
			FormatMapping mapping = new FormatMapping(TraceParameter.ASCII);
			mapping.isSimple = true;
			typeList.add(mapping);
			mapping = new FormatMapping(TraceParameter.ASCII);
			mapping.isSimple = true;
			typeList.add(mapping);

			// Check that does optional instance identifier exist
			if (location.getParameterCount() == 3) { // CodForChk_Dis_Magic
				mapping = new FormatMapping(TraceParameter.HEX32);
				mapping.isSimple = true;
				typeList.add(mapping);
			}
			checkParameters = false;
		} else {
			// If some other flag than Data, State, Ext or EventStart is set,
			// only one 32-bit hex parameter is supported
			typeList = new ArrayList<FormatMapping>();
			if (location.getParameterCount() == 1) {
				FormatMapping mapping = new FormatMapping(TraceParameter.HEX32);
				mapping.isSimple = true;
				typeList.add(mapping);
			}
		}
		// If no flags or Ext flag is present, the parameter count needs to be
		// verified
		TraceConversionResult result = super.convertLocation(location,
				checkParameters, typeList);
		// If the extension or state tag is present, zero parameters or a single
		// 32-bit parameter is not accepted because they do not need to generate
		// a function into the header
		if (((flags.hasExtTag && !flags.hasFunctionExitTag && !flags.hasFunctionEntryTag) || (flags.hasStateTag))
				&& (typeList.size() == 0 || (typeList.size() == 1 && typeList
						.get(0).isSimple))) {
			throw new TraceBuilderException(
					TraceBuilderErrorCode.PARAMETER_FORMAT_UNNECESSARY_EXT_MACRO);
		}
		// Ext-macros are tagged with the complex header rule, so the header
		// gets written when traces are exported. Data-macros are tagged with
		// read-only rule, so they are not updated via UI. Other special cases
		// are flagged with corresponding rule.
		// If trace text does not exist, it is created based on context
		AutomaticTraceTextRule rule = null;
		if (flags.hasDataTag) {
			addRule(result, new ReadOnlyObjectRuleImpl());
		} else if (flags.hasStateTag) {
			addRule(result, new StateTraceRule());
			addRule(result, new ComplexHeaderRuleImpl());
		} else if (flags.hasEventStartTag) {
			addRule(result, new PerformanceEventStartRule());
		} else if (flags.hasEventStopTag) {
			addRule(result, new PerformanceEventStopRule());
			addRule(result, new ComplexHeaderRuleImpl());
		} else if (flags.hasFunctionEntryTag) {
			if (flags.hasExtTag) {
				// Entry trace may contain Ext tag. In that case the trace
				// parameters are an instance variable and function parameters
				// parsed from source. It is also flagged as complex, so the
				// function gets generated to the trace header
				addRule(result, new ComplexHeaderRuleImpl());
				addRule(result, new AutoAddFunctionParametersRule());
				addRule(result, new AutoAddThisPtrRule());
			}
			rule = new EntryTraceRule();
			addRule(result, rule);
		} else if (flags.hasFunctionExitTag) {
			if (flags.hasExtTag) {
				// Exit trace may contain Ext tag. In that case the trace has
				// two parameters: instance variable and return statement
				// It is also flagged as complex, so the function gets generated
				// to the trace header
				addRule(result, new ComplexHeaderRuleImpl());
				addRule(result, new AutoAddThisPtrRule());
				addRule(result, new AutoAddReturnParameterRule());
			}
			rule = new ExitTraceRule();
			addRule(result, rule);
		} else if (flags.hasExtTag) {
			addRule(result, new ComplexHeaderRuleImpl());
		}
		if (rule != null) {
			setAutoTextToTrace(location, result, rule);
		}
		List<String> parserData = location.getParserData();
		result.group = parserData.get(parserData.size() - 1);
		// The convert flag is reset to prevent further conversions
		location.locationConverted();
		return result;
	}

	/**
	 * Uses the auto-text rule to create trace text
	 * 
	 * @param location
	 *            the location
	 * @param result
	 *            the conversion result
	 * @param rule
	 *            the auto-text rule
	 * @throws TraceBuilderException
	 *             if update fails
	 */
	private void setAutoTextToTrace(TraceLocation location,
			TraceConversionResult result, AutomaticTraceTextRule rule)
			throws TraceBuilderException {
		// The trace text comes from the auto-text rule
		SourceParser parser = location.getParser();
		SourceContext context = parser.getContext(location.getOffset());
		if (context != null) {
			result.text = rule.formatTrace(context);
		} else {
			throw new TraceBuilderException(
					TraceBuilderErrorCode.NO_CONTEXT_FOR_LOCATION);
		}
	}

	/**
	 * Checks parameter count
	 * 
	 * @param location
	 *            the location
	 * @return the location tag flags
	 * @throws TraceBuilderException
	 *             if parameter count is not valid
	 */
	private TraceParameterFlagList checkParameterCount(TraceLocation location)
			throws TraceBuilderException {
		TraceParameterFlagList flags = new TraceParameterFlagList(location
				.getTag());

		// If the trace has some tag, the parameter count is fixed
		// Data has 2 parameters
		// State has 2 or 3 parameters
		// Function entry-exit has 0 or 1 parameters
		// Event start has 0 or 1 parameters
		// Event stop has 1 parameters
		int parameterCount = location.getParameterCount();

		// Entry trace may have zero or one parameter
		// In case of Ext, it must have one parameter
		if (flags.hasFunctionEntryTag
				&& ((parameterCount > 1) || (flags.hasExtTag && (parameterCount != ENTRY_EXT_PARAMETER_COUNT)))) {
			throw new TraceBuilderException(
					TraceBuilderErrorCode.PARAMETER_COUNT_DOES_NOT_MATCH_API);
		}

		// Exit trace may have zero or one parameter
		// In case of Ext, it must have two parameters
		if (flags.hasFunctionExitTag
				&& ((!flags.hasExtTag && (parameterCount > 1)) || (flags.hasExtTag && parameterCount != EXIT_EXT_PARAMETER_COUNT))) { // CodForChk_Dis_LengthyLine
			throw new TraceBuilderException(
					TraceBuilderErrorCode.PARAMETER_COUNT_DOES_NOT_MATCH_API);
		}

		// Event start may have zero or one parameter
		if (flags.hasEventStartTag
				&& (parameterCount > 1)) {
			throw new TraceBuilderException(
					TraceBuilderErrorCode.PARAMETER_COUNT_DOES_NOT_MATCH_API);
		}

		// Event stop have one parameters
		if (flags.hasEventStopTag && (parameterCount != 1)) {
			throw new TraceBuilderException(
					TraceBuilderErrorCode.PARAMETER_COUNT_DOES_NOT_MATCH_API);
		}

		// Data trace has two parameters
		if ((flags.hasDataTag && (parameterCount != DATA_PARAMETER_COUNT))) {
			throw new TraceBuilderException(
					TraceBuilderErrorCode.PARAMETER_COUNT_DOES_NOT_MATCH_API);
		}

		// State trace may have two or three parameter
		if (flags.hasStateTag && (parameterCount < 2 || parameterCount > 3)) { // CodForChk_Dis_Magic
			throw new TraceBuilderException(
					TraceBuilderErrorCode.PARAMETER_COUNT_DOES_NOT_MATCH_API);
		}

		return flags;
	}

	/**
	 * Adds a rule to result
	 * 
	 * @param result
	 *            the result
	 * @param rule
	 *            the rule
	 */
	private void addRule(TraceConversionResult result, TraceModelExtension rule) {
		if (result.extensions == null) {
			result.extensions = new ArrayList<TraceModelExtension>();
		}
		result.extensions.add(rule);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.engine.rules.SourceParserRuleBase#
	 *      isLocationConverted(com.nokia.tracebuilder.engine.TraceLocation)
	 */
	@Override
	public boolean isLocationConverted(TraceLocation location) {
		boolean retval = location.hasChangedAfterConvert();
		if (!retval) {
			// Duplicate-location conversions need to be retried in case the
			// location is no longer a duplicate
			retval = (location.getValidityCode() == TraceBuilderErrorCode.TRACE_HAS_MULTIPLE_LOCATIONS);
		}
		return retval;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.engine.rules.printf.PrintfTraceParserRule#getLocationGroup()
	 */
	@Override
	public String getLocationGroup() {
		return null;
	}
}
