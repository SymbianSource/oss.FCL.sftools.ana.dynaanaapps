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
 * Formatting rule for OST traces
 *
 */
package com.nokia.tracebuilder.engine.rules.osttrace;

import com.nokia.tracebuilder.engine.TraceBuilderGlobals;
import com.nokia.tracebuilder.engine.rules.AutomaticTraceTextRule;
import com.nokia.tracebuilder.engine.rules.ComplexHeaderRule;
import com.nokia.tracebuilder.engine.rules.EntryTraceRule;
import com.nokia.tracebuilder.engine.rules.ExitTraceRule;
import com.nokia.tracebuilder.engine.rules.PerformanceEventRuleBase;
import com.nokia.tracebuilder.engine.rules.PerformanceEventStartRule;
import com.nokia.tracebuilder.engine.rules.PerformanceEventStopRule;
import com.nokia.tracebuilder.engine.rules.StateTraceRule;
import com.nokia.tracebuilder.engine.rules.TraceFormatRuleBase;
import com.nokia.tracebuilder.model.Trace;
import com.nokia.tracebuilder.model.TraceModelListener;
import com.nokia.tracebuilder.model.TraceObject;
import com.nokia.tracebuilder.model.TraceParameter;
import com.nokia.tracebuilder.plugin.TraceFormatConstants;
import com.nokia.tracebuilder.plugin.TraceHeaderContribution;
import com.nokia.tracebuilder.plugin.TraceAPIFormatter.TraceFormatType;
import com.nokia.tracebuilder.project.GroupNameHandlerBase;
import com.nokia.tracebuilder.rules.HiddenTraceObjectRule;
import com.nokia.tracebuilder.source.SourceConstants;
import com.nokia.tracebuilder.source.SourceUtils;
import java.util.regex.Matcher;

/**
 * Formatting rule for OST traces.
 * 
 */
public final class OstTraceFormatRule extends TraceFormatRuleBase implements
		TraceHeaderContribution, TraceModelListener {

	/**
	 * Separator for parameters within trace text
	 */
	private static final String PARAMETER_VALUE_SEPARATOR = "="; //$NON-NLS-1$

	/**
	 * Separator for parameter name ane value
	 */
	private static final String PARAMETER_SEPARATOR = ";"; //$NON-NLS-1$

	/**
	 * String parameter tag
	 */
	private static final String STRING_PARAMETER_TAG = "%s"; //$NON-NLS-1$

	/**
	 * Hex parameter tag
	 */
	private static final String HEX_PARAMETER_TAG = "0x%x"; //$NON-NLS-1$

	/**
	 * SDEC parameter tag
	 */
	private static final String SDEC_PARAMETER_TAG = "%d"; //$NON-NLS-1$

	/**
	 * Category for traces
	 */
	private static final String TRACE_CATEGORY = "KBTraceCategoryOpenSystemTrace"; //$NON-NLS-1$

	/**
	 * Title shown in UI
	 */
	private static final String UI_TITLE = "Open System Trace"; //$NON-NLS-1$

	/**
	 * Name for storage
	 */
	public static final String STORAGE_NAME = "OstTraceFormat"; //$NON-NLS-1$

	/**
	 * OstTraceEventStart tag
	 */
	public static final String OST_TRACE_EVENT_START_TAG = "OstTraceEventStart"; //$NON-NLS-1$

	/**
	 * OstTraceEventStop tag
	 */
	public static final String OST_TRACE_EVENT_STOP_TAG = "OstTraceEventStop"; //$NON-NLS-1$

	/**
	 * Source formatting
	 */
	private static final String TRACE_FORMAT = "OstTrace" //$NON-NLS-1$
			+ TraceFormatConstants.PARAM_COUNT_FORMAT // Number of parameters
			+ "( " //$NON-NLS-1$
			+ TraceFormatConstants.GROUP_FORMAT // Group name
			+ ", " //$NON-NLS-1$
			+ TraceFormatConstants.NAME_FORMAT // Trace name
			+ ", " //$NON-NLS-1$
			+ TraceFormatConstants.TEXT_FORMAT // Trace text
			+ TraceFormatConstants.PARAMETERS_FORMAT // Trace parameters
			+ " );"; //$NON-NLS-1$

	/**
	 * Source formatting with complex traces
	 */
	private static final String COMPLEX_TRACE_FORMAT = "OstTraceExt" //$NON-NLS-1$
			+ TraceFormatConstants.PARAM_COUNT_FORMAT // Number of parameters
			+ "( " //$NON-NLS-1$
			+ TraceFormatConstants.GROUP_FORMAT // Group name
			+ ", " //$NON-NLS-1$
			+ TraceFormatConstants.NAME_FORMAT // Trace name
			+ ", " //$NON-NLS-1$
			+ TraceFormatConstants.TEXT_FORMAT // Trace text
			+ TraceFormatConstants.PARAMETERS_FORMAT // Trace parameters
			+ " );"; //$NON-NLS-1$

	/**
	 * Function entry formatting
	 */
	private static final String ENTRY_TRACE_FORMAT = "OstTraceFunctionEntry" //$NON-NLS-1$
			+ TraceFormatConstants.PARAM_COUNT_FORMAT + "( " //$NON-NLS-1$
			+ TraceFormatConstants.NAME_FORMAT // Trace name
			+ TraceFormatConstants.PARAMETERS_FORMAT // Trace parameters
			+ " );"; //$NON-NLS-1$

	/**
	 * Function entry Ext formatting
	 */
	private static final String ENTRY_TRACE_EXT_FORMAT_THIS = "OstTraceFunctionEntryExt( " //$NON-NLS-1$
			+ TraceFormatConstants.NAME_FORMAT // Trace name
			+ ", this );"; //$NON-NLS-1$

	/**
	 * Function exit formatting
	 */
	private static final String EXIT_TRACE_FORMAT = "OstTraceFunctionExit" //$NON-NLS-1$
			+ TraceFormatConstants.PARAM_COUNT_FORMAT + "( " //$NON-NLS-1$
			+ TraceFormatConstants.NAME_FORMAT // Trace name
			+ TraceFormatConstants.PARAMETERS_FORMAT // Trace parameters
			+ " );"; //$NON-NLS-1$

	/**
	 * Function exit Ext formatting
	 */
	private static final String EXIT_TRACE_EXT_FORMAT = "OstTraceFunctionExitExt( " //$NON-NLS-1$
			+ TraceFormatConstants.NAME_FORMAT // Trace name
			+ TraceFormatConstants.PARAMETERS_FORMAT // Trace parameters
			+ " );"; //$NON-NLS-1$

	/**
	 * State formatting
	 */
	private static final String STATE_TRACE_FORMAT = "OstTraceState" //$NON-NLS-1$
			+ TraceFormatConstants.PARAM_COUNT_FORMAT
			+ "( " //$NON-NLS-1$
			+ TraceFormatConstants.NAME_FORMAT // Trace name
			+ TraceFormatConstants.STATE_MACHINE_NAME_FORMAT
			+ TraceFormatConstants.STATE_MACHINE_STATE_FORMAT
			+ TraceFormatConstants.PARAMETERS_FORMAT // Trace parameters
			+ " );"; //$NON-NLS-1$

	/**
	 * Performance event start formatting
	 */
	private static final String EVENT_START_FORMAT = OST_TRACE_EVENT_START_TAG
			+ TraceFormatConstants.PARAM_COUNT_FORMAT + "( " //$NON-NLS-1$
			+ TraceFormatConstants.NAME_FORMAT // Trace name
			+ ", " //$NON-NLS-1$
			+ TraceFormatConstants.EVENT_NAME_FORMAT // Event name
			+ TraceFormatConstants.PARAMETERS_FORMAT // Trace parameters
			+ " );"; //$NON-NLS-1$

	/**
	 * Performance event stop formatting
	 */
	private static final String EVENT_STOP_FORMAT = OST_TRACE_EVENT_STOP_TAG
			+ "( " //$NON-NLS-1$
			+ TraceFormatConstants.NAME_FORMAT // Trace name
			+ ", " //$NON-NLS-1$
			+ TraceFormatConstants.EVENT_NAME_FORMAT // Event name
			+ ", " //$NON-NLS-1$
			// Start Event Trace name
			+ TraceFormatConstants.EVENT_START_TRACE_NAME_FORMAT 
			+ TraceFormatConstants.PARAMETERS_FORMAT // Trace parameters
			+ " );"; //$NON-NLS-1$

	/**
	 * Ext-function declaration format
	 */
	private static final String HEADER_FORMAT = "OstTraceGen" //$NON-NLS-1$
			+ TraceFormatConstants.PARAM_COUNT_FORMAT // Number of parameters
			+ "( TUint32 aTraceID" //$NON-NLS-1$
			+ TraceFormatConstants.PARAMETERS_FORMAT // Trace parameters
			+ " )"; //$NON-NLS-1$

	/**
	 * Activation query formatting
	 */
	private static final String ACTIVATION_FORMAT = "BTrace8( " //$NON-NLS-1$
			+ TRACE_CATEGORY + ", " //$NON-NLS-1$
			+ "EOstTraceActivationQuery, KOstTraceComponentID, aTraceID )"; //$NON-NLS-1$

	/**
	 * Buffered trace format
	 */
	private static final String TRACE_BUFFER_FORMAT = "OstSendNBytes( " //$NON-NLS-1$
			+ TRACE_CATEGORY + ", " //$NON-NLS-1$
			+ "EOstTrace, KOstTraceComponentID, aTraceID, " //$NON-NLS-1$
			+ TraceFormatConstants.DATA_BUFFER_FORMAT // Trace data
			+ ", " //$NON-NLS-1$
			+ TraceFormatConstants.DATA_LENGTH_FORMAT // Trace data length
			+ " );"; //$NON-NLS-1$

	/**
	 * Packed trace format
	 */
	private static final String TRACE_PACKED_FORMAT = "BTraceContext12( " //$NON-NLS-1$
			+ TRACE_CATEGORY + ", " //$NON-NLS-1$
			+ "EOstTrace, KOstTraceComponentID, aTraceID, " //$NON-NLS-1$
			+ TraceFormatConstants.DATA_BUFFER_FORMAT // Trace data
			+ " );"; //$NON-NLS-1$

	/**
	 * #include format
	 */
	private String INCLUDE_FORMAT = "#include \"OstTraceDefinitions.h\"\r\n" //$NON-NLS-1$
			+ "#ifdef OST_TRACE_COMPILER_IN_USE\r\n#include \"" //$NON-NLS-1$
			+ TraceFormatConstants.INCLUDE_FORMAT + "\"\r\n#endif"; //$NON-NLS-1$

	/**
	 * Template for the OstTraceDefinitions.h header file
	 */
	private static final String[] MAIN_HEADER_TEMPLATE = { "\r\n" //$NON-NLS-1$
			+ "// OST_TRACE_COMPILER_IN_USE flag has been added by Trace Compiler\r\n" //$NON-NLS-1$
			+ "// REMOVE BEFORE CHECK-IN TO VERSION CONTROL\r\n" //$NON-NLS-1$
			+ "#define OST_TRACE_COMPILER_IN_USE\r\n" //$NON-NLS-1$
			+ "#include <OpenSystemTrace.h>\r\n#endif\r\n" }; //$NON-NLS-1$

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.project.TraceProjectAPI#getName()
	 */
	public String getName() {
		return STORAGE_NAME;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.project.TraceProjectAPI#getTitle()
	 */
	public String getTitle() {
		return UI_TITLE;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.engine.source.TraceFormattingRule#
	 *      getFormat(com.nokia.tracebuilder.model.Trace,
	 *      com.nokia.tracebuilder.plugin.TraceAPIFormatter.TraceFormatType)
	 */
	public String getFormat(Trace trace, TraceFormatType formatType) { // CodForChk_Dis_ComplexFunc
		String retval;
		if (formatType == TraceFormatType.NORMAL_TRACE
				|| formatType == TraceFormatType.COMPLEX_TRACE) {
			if (trace.getExtension(PerformanceEventStartRule.class) != null) {
				retval = EVENT_START_FORMAT;
			} else if (trace.getExtension(PerformanceEventStopRule.class) != null) {
				retval = EVENT_STOP_FORMAT;
			} else if (trace.getExtension(EntryTraceRule.class) != null) {
				// If the trace is entry trace with function parameters, the
				// parameters are not inserted into source. Instead, only the
				// "this" pointer is added
				if (trace.getExtension(ComplexHeaderRule.class) != null) {
					retval = ENTRY_TRACE_EXT_FORMAT_THIS;
				} else {
					retval = ENTRY_TRACE_FORMAT;
				}
			} else if (trace.getExtension(ExitTraceRule.class) != null) {
				if (trace.getExtension(ComplexHeaderRule.class) != null) {
					retval = EXIT_TRACE_EXT_FORMAT;
				} else {
					retval = EXIT_TRACE_FORMAT;
				}
			} else if (trace.getExtension(StateTraceRule.class) != null) {
				retval = STATE_TRACE_FORMAT;
			} else {
				if (formatType == TraceFormatType.NORMAL_TRACE) {
					retval = TRACE_FORMAT;
				} else if (formatType == TraceFormatType.COMPLEX_TRACE) {
					retval = COMPLEX_TRACE_FORMAT;
				} else {
					retval = null;
				}
			}
		} else {
			if (formatType == TraceFormatType.HEADER) {
				retval = HEADER_FORMAT;
			} else if (formatType == TraceFormatType.TRACE_BUFFER) {
				retval = TRACE_BUFFER_FORMAT;
			} else if (formatType == TraceFormatType.TRACE_PACKED) {
				retval = TRACE_PACKED_FORMAT;
			} else if (formatType == TraceFormatType.TRACE_ACTIVATION) {
				retval = ACTIVATION_FORMAT;
			} else if (formatType == TraceFormatType.INCLUDE_FORMAT) {
				retval = INCLUDE_FORMAT;
			} else {
				retval = null;
			}
		}
		return retval;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.engine.source.SourceFormatRule#
	 *      mapParameterCountToSource(com.nokia.tracebuilder.model.Trace, int)
	 */
	@Override
	public String mapParameterCountToSource(Trace trace, int count) {
		String retval;
		ComplexHeaderRule rule = trace.getExtension(ComplexHeaderRule.class);
		if (rule != null && rule.getTraceIDDefineExtension() != null) {
			// Uses the extension tag with extension headers
			retval = OstConstants.EXTENSION_TRACE_TAG;
		} else {
			retval = String.valueOf(count);
		}
		return retval;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.project.TraceHeaderContribution#
	 *      getContribution(com.nokia.tracebuilder.project.TraceHeaderContribution.TraceHeaderContributionType)
	 */
	public String[] getContribution(TraceHeaderContributionType type) {
		String[] retval = null;
		if (type == TraceHeaderContributionType.GLOBAL_DEFINES) {
			retval = new String[] { "KOstTraceComponentID 0x" //$NON-NLS-1$
					+ Integer.toHexString(getOwner().getModel().getID()) };
		} else if (type == TraceHeaderContributionType.MAIN_HEADER_CONTENT) {
			retval = MAIN_HEADER_TEMPLATE;
		}
		return retval;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.project.TraceProjectAPI#isVisibleInConfiguration()
	 */
	public boolean isVisibleInConfiguration() {
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.engine.rules.ExtensionBase#setOwner(com.nokia.tracebuilder.model.TraceObject)
	 */
	@Override
	public void setOwner(TraceObject owner) {
		if (getOwner() != null) {
			getOwner().getModel().removeModelListener(this);
		}
		super.setOwner(owner);
		if (owner != null) {
			owner.getModel().addModelListener(this);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.model.TraceModelListener#
	 *      objectAdded(com.nokia.tracebuilder.model.TraceObject,
	 *      com.nokia.tracebuilder.model.TraceObject)
	 */
	public void objectAdded(TraceObject owner, TraceObject object) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.model.TraceModelListener#
	 *      objectCreationComplete(com.nokia.tracebuilder.model.TraceObject)
	 */
	public void objectCreationComplete(TraceObject object) {
		// Model must be valid and converter not running
		// -> Parameter was added via UI
		// Also, parameter must not be hidden, otherwise fillers would update
		// the trace
		if (object.getModel().isValid()
				&& object instanceof TraceParameter
				&& !TraceBuilderGlobals.getSourceContextManager()
						.isConverting()
				&& object.getExtension(HiddenTraceObjectRule.class) == null) {
			TraceParameter param = (TraceParameter) object;
			Trace parent = param.getTrace();

			// In case of performance event trace we do not add parameter to
			// trace text
			GroupNameHandlerBase groupNameHandler = TraceBuilderGlobals.getGroupNameHandler();
			if (!(parent.getGroup().getName()
					.equals(groupNameHandler.getDefaultGroups()[groupNameHandler.getPerformanceGroupIdIndex()]))) {
				addParameterToTraceText(param);
			}
		}
	}

	/**
	 * Adds parameter formatting to trace text
	 * 
	 * @param param
	 *            the parameter
	 */
	private void addParameterToTraceText(TraceParameter param) {
		// Adds the parameter format to the trace text
		// Auto-text traces are not updated
		String text;
		Trace parent = param.getTrace();

		if (parent.getExtension(AutomaticTraceTextRule.class) == null) {
			text = parent.getTrace() + PARAMETER_SEPARATOR + param.getName()
					+ PARAMETER_VALUE_SEPARATOR
					+ SourceUtils.mapParameterTypeToFormat(param);

			parent.setTrace(text);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.model.TraceModelListener#
	 *      objectRemoved(com.nokia.tracebuilder.model.TraceObject,
	 *      com.nokia.tracebuilder.model.TraceObject)
	 */
	public void objectRemoved(TraceObject owner, TraceObject object) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.engine.rules.TraceFormatRuleBase#
	 *      parameterAboutToBeRemoved(com.nokia.tracebuilder.model.TraceParameter,
	 *      int)
	 */
	@Override
	public void parameterAboutToBeRemoved(TraceParameter parameter, int index) {
		Trace trace = parameter.getTrace();
		String tracetext = trace.getTrace();

		if (tracetext != "") { //$NON-NLS-1$
			Matcher matcher = SourceUtils.traceTextPattern.matcher(tracetext);

			boolean found = true;
			int i = -1;
			int previousmatchend = 0;

			do {
				found = matcher.find();
				i++;
				// Store end location of the previous match
				if (i < index) {
					previousmatchend = matcher.start()
							+ matcher.group().length();
				}
			} while (i < index);

			if (found) {
				int matchstart = matcher.start();
				String group = matcher.group();

				if (index == 0) {
					// Fist parameter is going to be removed
					int firstpartend = tracetext.lastIndexOf(';', matchstart);
					if (firstpartend < 0) {
						// Trace text is not TB in format, so remove only
						// parameter tag.
						firstpartend = matchstart;

						// If trace group is TRACE_PERFORMANCE, trace could be
						// in LocigAnalyzer format. Remove also extra space
						// before parameter tag
						String traceGroupName = trace.getGroup().getName();
						GroupNameHandlerBase groupNameHandler = TraceBuilderGlobals.getGroupNameHandler();
						int performanceGroupIndex = groupNameHandler.getPerformanceGroupIdIndex();
						String performanceGroupName = groupNameHandler.getDefaultGroups()[performanceGroupIndex];
						if (traceGroupName.equals(performanceGroupName)) {
							if (tracetext.charAt(firstpartend - 1) == SourceConstants.SPACE_CHAR) {
								firstpartend--;
							}
						}
					}
					trace.setTrace(tracetext.substring(0, firstpartend)
							+ tracetext.substring(matchstart + group.length()));
				} else {
					// Some other than first parameter is going to be removed
					if (tracetext.charAt(previousmatchend) == ';') {
						// Trace text is TB in format, so remove also some text
						// before parameter tag.
						trace.setTrace(tracetext.substring(0, previousmatchend)
								+ tracetext.substring(matchstart
										+ group.length()));
					} else {
						// Trace text is not TB in format, so remove only
						// parameter tag.
						trace.setTrace(tracetext.substring(0, matchstart)
								+ tracetext.substring(matchstart
										+ group.length()));
					}
				}
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.model.TraceModelListener#
	 *      propertyUpdated(com.nokia.tracebuilder.model.TraceObject, int)
	 */
	public void propertyUpdated(TraceObject object, int property) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.project.TraceProjectAPI#
	 *      formatTraceForExport(com.nokia.tracebuilder.model.Trace,
	 *      com.nokia.tracebuilder.project.TraceProjectAPI.TraceFormatFlags)
	 */
	public String formatTraceForExport(Trace trace, TraceFormatFlags flags) {
		// TODO: This uses default formats
		// -> Should be configurable
		// > for entry traces
		// < for exit traces
		// Logic analyzer format for performance traces
		String retval = trace.getTrace();
		AutomaticTraceTextRule rule = trace
				.getExtension(AutomaticTraceTextRule.class);
		int parameterCount = trace.getParameterCount();
		if (rule != null) {
			if (rule instanceof EntryTraceRule) {
				retval = "> " //$NON-NLS-1$
						+ retval;
			} else if (rule instanceof ExitTraceRule) {
				retval = "< " //$NON-NLS-1$
						+ retval;
			} else if (rule instanceof StateTraceRule) {
				if (parameterCount == 2) { // CodForChk_Dis_Magic
					retval = retval + "Machine" + PARAMETER_VALUE_SEPARATOR //$NON-NLS-1$
							+ STRING_PARAMETER_TAG + PARAMETER_SEPARATOR
							+ "State" + PARAMETER_VALUE_SEPARATOR //$NON-NLS-1$
							+ STRING_PARAMETER_TAG;
				} else {
					retval = retval
							+ "Machine" + PARAMETER_VALUE_SEPARATOR //$NON-NLS-1$
							+ STRING_PARAMETER_TAG + PARAMETER_SEPARATOR
							+ "State" + PARAMETER_VALUE_SEPARATOR //$NON-NLS-1$
							+ STRING_PARAMETER_TAG + PARAMETER_SEPARATOR
							+ "Instance" + PARAMETER_VALUE_SEPARATOR //$NON-NLS-1$
							+ HEX_PARAMETER_TAG;
				}
			}

			if (parameterCount >= 1 && !(rule instanceof StateTraceRule)) {
				TraceParameter param;
				for (int i = 0; i < parameterCount; i++) {
					param = trace.getParameter(i);
					retval += PARAMETER_SEPARATOR + param.getName()
							+ PARAMETER_VALUE_SEPARATOR
							+ SourceUtils.mapParameterTypeToFormat(param);
				}
			}
		} else {
			PerformanceEventRuleBase perf = trace
					.getExtension(PerformanceEventRuleBase.class);
			if (perf != null) {
				if (perf instanceof PerformanceEventStartRule) {
					retval = retval + "> " + PARAMETER_SEPARATOR //$NON-NLS-1$
							+ "Value" + PARAMETER_VALUE_SEPARATOR //$NON-NLS-1$
							+ SDEC_PARAMETER_TAG;
				} else if (perf instanceof PerformanceEventStopRule) {
					retval = retval + "< " //$NON-NLS-1$
							+ PARAMETER_SEPARATOR
							+ "Value" + PARAMETER_VALUE_SEPARATOR //$NON-NLS-1$
							+ SDEC_PARAMETER_TAG
							+ PARAMETER_SEPARATOR
							+ "Start Event Trace ID" + PARAMETER_VALUE_SEPARATOR //$NON-NLS-1$
							+ SDEC_PARAMETER_TAG;
				}
			}
		}
		// If formatting is not supported the format characters and parameters
		// are removed.
		if (!flags.isFormattingSupported) {
			int index = retval.indexOf(PARAMETER_SEPARATOR);
			if (index > 0) {
				retval = retval.substring(0, index);
			}
			retval = SourceUtils.removePrintfFormatting(retval).trim();
		}
		return retval;
	}
}
