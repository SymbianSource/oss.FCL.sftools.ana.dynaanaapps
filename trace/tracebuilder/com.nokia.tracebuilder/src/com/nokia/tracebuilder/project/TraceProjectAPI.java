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
* Interface to the properties of the trace API used by the currently open trace project
*
*/
package com.nokia.tracebuilder.project;

import com.nokia.tracebuilder.model.Trace;
import com.nokia.tracebuilder.model.TraceModelExtension;

/**
 * Interface to the properties of the trace API used by the currently open trace
 * project
 * 
 */
public interface TraceProjectAPI extends TraceModelExtension {

	/**
	 * Formatting flags for formatTraceForExport
	 * 
	 */
	public class TraceFormatFlags {

		/**
		 * Formatting characters supported flag
		 */
		public boolean isFormattingSupported;
	}

	/**
	 * Gets the name of this API
	 * 
	 * @return the name
	 */
	public String getName();

	/**
	 * Gets the title that is shown in the API selection preferences dialog
	 * 
	 * @return the title
	 */
	public String getTitle();

	/**
	 * Flag which determines if this API is shown in the TraceBuilder
	 * configuration
	 * 
	 * @return the flag
	 */
	public boolean isVisibleInConfiguration();

	/**
	 * Formats a trace for into a string suitable for export
	 * 
	 * @param trace
	 *            the trace to be formatted
	 * @param flags
	 *            the formatting flags
	 * @return the formatted text
	 */
	public String formatTraceForExport(Trace trace, TraceFormatFlags flags);

}
