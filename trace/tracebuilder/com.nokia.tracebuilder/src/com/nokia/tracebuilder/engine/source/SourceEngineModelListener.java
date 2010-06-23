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
* Trace model listener implementation for SourceEngine
*
*/
package com.nokia.tracebuilder.engine.source;

import com.nokia.tracebuilder.model.Trace;
import com.nokia.tracebuilder.model.TraceGroup;
import com.nokia.tracebuilder.model.TraceModelListener;
import com.nokia.tracebuilder.model.TraceObject;
import com.nokia.tracebuilder.model.TraceParameter;

/**
 * Trace model listener implementation for SourceEngine
 * 
 */
final class SourceEngineModelListener implements TraceModelListener {

	/**
	 * Source engine
	 */
	private final SourceEngine sourceEngine;

	/**
	 * Constructor
	 * 
	 * @param engine
	 *            the source engine
	 */
	SourceEngineModelListener(SourceEngine engine) {
		sourceEngine = engine;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.model.TraceModelListener#
	 *      objectAdded(com.nokia.tracebuilder.model.TraceObject,
	 *      com.nokia.tracebuilder.model.TraceObject)
	 */
	public void objectAdded(TraceObject owner, TraceObject object) {
		// Update is called from TraceBuilder engine
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.model.TraceModelListener#
	 *      objectRemoved(com.nokia.tracebuilder.model.TraceObject,
	 *      com.nokia.tracebuilder.model.TraceObject)
	 */
	public void objectRemoved(TraceObject owner, TraceObject object) {
		if (object instanceof TraceParameter) {
			// If there is a rule that prevents the parameter to be shown in
			// source, there is no need to update
			TraceParameterFormattingRule rule = object
					.getExtension(TraceParameterFormattingRule.class);
			if (rule == null || rule.isShownInSource()) {
				sourceEngine.updateTrace((Trace) owner);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.model.TraceModelListener#
	 *      objectCreationComplete(com.nokia.tracebuilder.model.TraceObject)
	 */
	public void objectCreationComplete(TraceObject object) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.model.TraceModelListener#
	 *      propertyUpdated(com.nokia.tracebuilder.model.TraceObject, int)
	 */
	public void propertyUpdated(TraceObject object, int property) {
		// Does not update until complete
		if (object.isComplete()) {
			if (object instanceof Trace) {
				if (property == TraceModelListener.NAME
						|| property == TraceModelListener.TRACE) {
					sourceEngine.updateTrace((Trace) object);
				}
			} else if (object instanceof TraceGroup) {
				// Updates the group name
				if (property == TraceModelListener.NAME) {
					TraceGroup group = (TraceGroup) object;
					for (Trace trace : group) {
						sourceEngine.updateTrace(trace);
					}
				}
			}
		}
	}
}