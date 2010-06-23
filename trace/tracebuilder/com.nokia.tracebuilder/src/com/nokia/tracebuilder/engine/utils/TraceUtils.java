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
* Utility functions related to traces
*
*/
package com.nokia.tracebuilder.engine.utils;

import java.util.Iterator;

import com.nokia.tracebuilder.engine.TraceBuilderConfiguration;
import com.nokia.tracebuilder.engine.TraceBuilderGlobals;
import com.nokia.tracebuilder.engine.source.SourceEngine;
import com.nokia.tracebuilder.model.Trace;
import com.nokia.tracebuilder.model.TraceBuilderException;
import com.nokia.tracebuilder.plugin.TraceFormatConstants;
import com.nokia.tracebuilder.source.SourceContext;

/**
 * Utility functions related to traces
 * 
 */
public final class TraceUtils {

	/**
	 * Default name format
	 */
	private static final String DEFAULT_NAME_FORMAT = "{$CN}_{$FN}"; //$NON-NLS-1$

	/**
	 * Default trace format
	 */
	private static final String DEFAULT_TRACE_FORMAT = "{$cn}::{$fn}"; //$NON-NLS-1$

	/**
	 * Underscore character
	 */
	private static final String UNDERSCORE = "_"; //$NON-NLS-1$

	/**
	 * Underscore character
	 */
	private static final char UNDERSCORE_CHAR = '_';

	/**
	 * Cannot be constructed
	 */
	private TraceUtils() {
	}

	/**
	 * Gets the default trace name format
	 * 
	 * @return the default trace name format
	 */
	public static String getDefaultNameFormat() {
		return DEFAULT_NAME_FORMAT;
	}

	/**
	 * Gets the default trace text format
	 * 
	 * @return the default trace text format
	 */
	public static String getDefaultTraceFormat() {
		return DEFAULT_TRACE_FORMAT;
	}

	/**
	 * Formats a trace
	 * 
	 * @param format
	 *            the format specification
	 * @param cname
	 *            the class name
	 * @param fname
	 *            the function name
	 * @return the formatted trace
	 */
	public static String formatTrace(String format, String cname, String fname) {
		StringBuffer sb = new StringBuffer(format);
		int cnindex = sb
				.indexOf(TraceFormatConstants.FORMAT_CLASS_NAME_NORMAL_CASE);
		if (cnindex >= 0) {
			if (cname != null) {
				sb.replace(cnindex, cnindex
						+ TraceFormatConstants.FORMAT_CLASS_NAME_NORMAL_CASE
								.length(), cname);
			} else {
				sb.replace(cnindex, cnindex
						+ TraceFormatConstants.FORMAT_CLASS_NAME_NORMAL_CASE
								.length(), ""); //$NON-NLS-1$
			}
		}
		int cnup = sb
				.indexOf(TraceFormatConstants.FORMAT_CLASS_NAME_UPPER_CASE);
		if (cnup >= 0) {
			if (cname != null) {
				sb.replace(cnup, cnup
						+ TraceFormatConstants.FORMAT_CLASS_NAME_UPPER_CASE
								.length(), cname.toUpperCase());
			} else {
				sb.replace(cnup, cnup
						+ TraceFormatConstants.FORMAT_CLASS_NAME_UPPER_CASE
								.length(), ""); //$NON-NLS-1$
			}
		}
		int fnindex = sb
				.indexOf(TraceFormatConstants.FORMAT_FUNCTION_NAME_NORMAL_CASE);
		if (fnindex >= 0) {
			sb.replace(fnindex, fnindex
					+ TraceFormatConstants.FORMAT_FUNCTION_NAME_NORMAL_CASE
							.length(), fname);
		}
		int fnup = sb
				.indexOf(TraceFormatConstants.FORMAT_FUNCTION_NAME_UPPER_CASE);
		if (fnup >= 0) {
			sb.replace(fnup, fnup
					+ TraceFormatConstants.FORMAT_FUNCTION_NAME_UPPER_CASE
							.length(), fname.toUpperCase());
		}
		return sb.toString();
	}

	/**
	 * Replaces invalid characters with '_'
	 * 
	 * @param name
	 *            name to be converted
	 * @return the converted name
	 */
	public static String convertName(String name) {
		StringBuffer sb;
		if (name.length() > 0) {
			boolean underscore = false;
			sb = new StringBuffer(name);
			if (Character.isDigit(name.charAt(0))) {
				sb.insert(0, UNDERSCORE_CHAR);
			}
			for (int i = 0; i < sb.length(); i++) {
				char c = sb.charAt(i);
				if (!Character.isLetterOrDigit(c)) {
					if (!underscore) {
						sb.setCharAt(i, UNDERSCORE_CHAR);
						underscore = true;
					} else {
						sb.deleteCharAt(i);
						i--;
					}
				} else {
					underscore = false;
				}
			}
			if (sb.length() > 0) {
				if (sb.charAt(sb.length() - 1) == UNDERSCORE_CHAR) {
					sb.deleteCharAt(sb.length() - 1);
				}
			} else {
				sb.append(UNDERSCORE);
			}
		} else {
			sb = new StringBuffer();
		}
		// If parameter value is NULL, it would be used as name
		String s = sb.toString();
		if (s.equals("NULL")) { //$NON-NLS-1$
			s = "_NULL"; //$NON-NLS-1$
		}
		return s;
	}

	/**
	 * Creates copies of the trace if it has TraceMultiplierRule extensions
	 * 
	 * @param trace
	 *            the trace to be multiplied
	 * @param insertLocation
	 *            the location where the original trace was inserted
	 * @param sourceEngine
	 *            the source engine
	 */
	public static void multiplyTrace(Trace trace, int insertLocation,
			SourceEngine sourceEngine) {
		// If multiplier rules have been added to the trace, they are
		// used to create copies of the trace
		TraceMultiplierRule multiplier;
		trace.getModel().startProcessing();
		try {
			// Trace may contain multiple multipliers. This loop gets one,
			// removes it and stops when there are no more multipliers
			do {
				multiplier = trace.getExtension(TraceMultiplierRule.class);
				if (multiplier != null) {
					processMultiplier(trace, multiplier, insertLocation,
							sourceEngine);
					// Multiplier extension is removed after use
					trace.removeExtension(multiplier);
				}
			} while (multiplier != null);
		} finally {
			trace.getModel().processingComplete();
		}
	}

	/**
	 * Processes a trace multiplier
	 * 
	 * @param trace
	 *            the trace
	 * @param multiplier
	 *            the multiplier
	 * @param insertLocation
	 *            the location where the original trace was inserted
	 * @param sourceEngine
	 *            the source engine
	 */
	private static void processMultiplier(Trace trace,
			TraceMultiplierRule multiplier, int insertLocation,
			SourceEngine sourceEngine) {
		Iterator<Trace> itr = multiplier.createCopies(trace);
		while (itr.hasNext()) {
			Trace copy = itr.next();
			addMultipliedTrace(copy, insertLocation, sourceEngine);
		}
	}

	/**
	 * Adds a trace that has been created by a multiplier rule. The trace is
	 * inserted to the same location as the original trace.
	 * 
	 * @param trace
	 *            the trace to be added
	 * @param insertLocation
	 *            the location where the original trace was inserted
	 * @param sourceEngine
	 *            the source engine
	 */
	private static void addMultipliedTrace(Trace trace, int insertLocation,
			SourceEngine sourceEngine) {
		try {
			// If location is specified, the trace is inserted into it
			if (insertLocation != -1) {
				SourceContext context = TraceBuilderGlobals
						.getSourceContextManager().getContext();
				if (insertLocation >= context.getOffset()
						&& insertLocation < context.getOffset()
								+ context.getLength()) {
					sourceEngine.insertTrace(trace, sourceEngine
							.getSourceOfContext(context), insertLocation);
				} else {
					if (TraceBuilderConfiguration.ASSERTIONS_ENABLED) {
						TraceBuilderGlobals.getEvents().postAssertionFailed(
								"Out of context insert", trace); //$NON-NLS-1$
					}
				}
			}
		} catch (TraceBuilderException e) {
			TraceBuilderGlobals.getEvents().postError(e);
		}
	}

}
