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
* Formatting rules are provided by Trace objects
*
*/
package com.nokia.tracebuilder.engine.source;

import com.nokia.tracebuilder.model.Trace;
import com.nokia.tracebuilder.model.TraceObjectRule;
import com.nokia.tracebuilder.model.TraceParameter;
import com.nokia.tracebuilder.plugin.TraceAPIFormatter.TraceFormatType;

/**
 * Formatting rules are stored into traces as extension. When inserting a trace
 * to source the formatting rules are used to convert the trace into source file
 * representation.
 * 
 */
public interface TraceFormattingRule extends TraceObjectRule {

	/**
	 * Gets the format for given trace
	 * 
	 * @param trace
	 *            the trace
	 * @param formatType
	 *            the type of the format requested
	 * @return the format
	 */
	public String getFormat(Trace trace, TraceFormatType formatType);

	/**
	 * Maps the trace name to name shown in source
	 * 
	 * @param trace
	 *            the trace to be mapped
	 * @return the name shown in source
	 */
	public String mapNameToSource(Trace trace);

	/**
	 * Maps the parameter count to source.
	 * 
	 * @param trace
	 *            the trace
	 * @param count
	 *            the parameter count
	 * @return mapped parameter count
	 */
	public String mapParameterCountToSource(Trace trace, int count);

	/**
	 * Removes parameter text from trace text
	 * 
	 * @param parameter
	 *            the parameter 
	 * @param index
	 *            index of the parameter 
	 */
	public void parameterAboutToBeRemoved(TraceParameter parameter, int index);

}
