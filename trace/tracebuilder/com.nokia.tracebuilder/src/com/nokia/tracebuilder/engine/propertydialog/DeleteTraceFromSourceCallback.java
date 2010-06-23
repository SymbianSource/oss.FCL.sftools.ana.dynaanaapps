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
* Base class for deleter classes that remove traces from source file
*
*/
package com.nokia.tracebuilder.engine.propertydialog;

import com.nokia.tracebuilder.engine.LocationProperties;
import com.nokia.tracebuilder.engine.TraceLocation;
import com.nokia.tracebuilder.engine.TraceLocationList;
import com.nokia.tracebuilder.engine.source.SourceEngine;
import com.nokia.tracebuilder.model.Trace;
import com.nokia.tracebuilder.model.TraceObject;

/**
 * Base class for deleter classes that remove traces from source file
 * 
 */
abstract class DeleteTraceFromSourceCallback extends DeleteObjectCallback {

	/**
	 * The source engine for trace removal
	 */
	private SourceEngine sourceEngine;

	/**
	 * Constructor
	 * 
	 * @param object
	 *            the object to be removed
	 * @param sourceEngine
	 *            the source engine for trace removal
	 */
	DeleteTraceFromSourceCallback(TraceObject object, SourceEngine sourceEngine) {
		super(object);
		this.sourceEngine = sourceEngine;
	}

	/**
	 * Removes a trace from source files
	 * 
	 * @param trace
	 *            the trace to be removed
	 */
	protected void removeTraceFromSource(Trace trace) {
		TraceLocationList list = trace.getExtension(TraceLocationList.class);
		if (list != null) {
			for (LocationProperties loc : list) {
				sourceEngine.removeLocation((TraceLocation) loc);
			}
		}
	}

}
