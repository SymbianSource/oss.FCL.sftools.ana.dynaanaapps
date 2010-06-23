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
* Model reset listener
*
*/
package com.nokia.tracebuilder.engine.rules;

import com.nokia.tracebuilder.model.Trace;
import com.nokia.tracebuilder.model.TraceGroup;
import com.nokia.tracebuilder.model.TraceModel;
import com.nokia.tracebuilder.model.TraceModelResetListener;
import com.nokia.tracebuilder.project.TraceProjectAPI;

/**
 * Model reset listener
 * 
 */
final class RulesEngineResetListener implements TraceModelResetListener {

	/**
	 * Rule engine
	 */
	private final RulesEngine engine;

	/**
	 * Trace model
	 */
	private final TraceModel model;

	/**
	 * Constructor
	 * 
	 * @param engine
	 *            rule engine
	 * @param model
	 *            the trace model
	 */
	RulesEngineResetListener(RulesEngine engine, TraceModel model) {
		this.engine = engine;
		this.model = model;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.model.TraceModelResetListener#modelResetting()
	 */
	public void modelResetting() {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.model.TraceModelResetListener#modelReset()
	 */
	public void modelReset() {
		model.removeExtensions(TraceProjectAPI.class);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.model.TraceModelResetListener#modelValid(boolean)
	 */
	public void modelValid(boolean valid) {
		if (valid) {
			model.startProcessing();
			try {
				// Creates the trace API if it does not exist yet
				engine.setDefaultTraceAPI();
				// Adds filler parameters to correct places
				for (TraceGroup group : model) {
					for (Trace trace : group) {
						engine.checkFillerParameters(trace);
					}
				}
			} finally {
				model.processingComplete();
			}
		}
	}
}