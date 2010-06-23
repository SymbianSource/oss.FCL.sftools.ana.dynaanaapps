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
* Formatting rule, which delegates calls to plug-in API
*
*/
package com.nokia.tracebuilder.engine.rules.plugin;

import com.nokia.tracebuilder.engine.rules.TraceFormatRuleBase;
import com.nokia.tracebuilder.model.Trace;
import com.nokia.tracebuilder.plugin.TraceAPIFormatter;
import com.nokia.tracebuilder.plugin.TraceAPIFormatter.TraceFormatType;

/**
 * Formatting rule, which delegates calls to plug-in API
 * 
 */
public final class PluginTraceFormatRule extends TraceFormatRuleBase {

	/**
	 * The plug-in formatter
	 */
	private TraceAPIFormatter formatter;

	/**
	 * Creates a new formatter
	 * 
	 * @param formatter
	 *            the plug-in formatter
	 */
	public PluginTraceFormatRule(TraceAPIFormatter formatter) {
		this.formatter = formatter;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.project.TraceProjectAPI#getName()
	 */
	public String getName() {
		return formatter.getName();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.project.TraceProjectAPI#getTitle()
	 */
	public String getTitle() {
		return formatter.getTitle();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.engine.source.SourceFormatRule#
	 *      getFormat(com.nokia.tracebuilder.model.Trace, int)
	 */
	public String getFormat(Trace trace, TraceFormatType formatType) {
		return formatter.getTraceFormat(formatType);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.project.TraceProjectAPI#isVisibleInConfiguration()
	 */
	public boolean isVisibleInConfiguration() {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.project.TraceProjectAPI#
	 *      formatTraceForExport(com.nokia.tracebuilder.model.Trace,
	 *      com.nokia.tracebuilder.project.TraceProjectAPI.TraceFormatFlags)
	 */
	public String formatTraceForExport(Trace trace, TraceFormatFlags flags) {
		return trace.getTrace();
	}

}
