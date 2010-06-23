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
* Trace model listener
*
*/
package com.nokia.tracebuilder.engine.rules;

import com.nokia.tracebuilder.model.Trace;
import com.nokia.tracebuilder.model.TraceModelListener;
import com.nokia.tracebuilder.model.TraceObject;
import com.nokia.tracebuilder.model.TraceParameter;
import com.nokia.tracebuilder.rules.FillerParameterRule;

/**
 * Trace model listener
 * 
 */
final class RulesEngineModelListener implements TraceModelListener {

	/**
	 * Rules engine
	 */
	private final RulesEngine engine;

	/**
	 * Constructor
	 * 
	 * @param engine
	 *            rules engine
	 */
	RulesEngineModelListener(RulesEngine engine) {
		this.engine = engine;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.model.TraceModelListener#
	 *      objectAdded(com.nokia.tracebuilder.model.TraceObject,
	 *      com.nokia.tracebuilder.model.TraceObject)
	 */
	public void objectAdded(TraceObject owner, TraceObject object) {
		// Updated in objectCreationComplete
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.model.TraceModelListener#
	 *      objectRemoved(com.nokia.tracebuilder.model.TraceObject,
	 *      com.nokia.tracebuilder.model.TraceObject)
	 */
	public void objectRemoved(TraceObject owner, TraceObject object) {
		if (owner instanceof Trace && object instanceof TraceParameter) {
			engine.checkParameterTypes((Trace) owner);
			if (object.getExtension(FillerParameterRule.class) == null) {
				engine.checkFillerParameters((Trace) owner);
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
		if (object.getModel().isValid() && object.isComplete()
				&& object instanceof TraceParameter
				&& property == TraceModelListener.TYPE) {
			Trace owner = ((TraceParameter) object).getTrace();
			if (owner.isComplete()) {
				engine.checkParameterTypes(owner);
				engine.checkFillerParameters(owner);
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
		if (object.getModel().isValid()) {
			if (object instanceof TraceParameter) {
				Trace owner = ((TraceParameter) object).getTrace();
				if (owner.isComplete()) {
					engine.checkParameterTypes(owner);
					// When adding fillers, the check is not done
					if (object.getExtension(FillerParameterRule.class) == null) {
						engine.checkFillerParameters(owner);
					}
				}
			} else if (object instanceof Trace) {
				// When a trace with multiple parameters is created, the
				// above branch does not get entered since the trace is not
				// yet complete. Check is made after trace is complete
				if (((Trace) object).getParameterCount() > 0) {
					engine.checkParameterTypes((Trace) object);
					engine.checkFillerParameters((Trace) object);
				}
			}
		}
	}
}