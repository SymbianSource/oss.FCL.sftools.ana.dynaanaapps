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
* Rule for instrumented traces
*
*/
package com.nokia.tracebuilder.engine.rules;

import com.nokia.tracebuilder.engine.propertydialog.InstrumentedTraceRule;
import com.nokia.tracebuilder.model.TraceModelPersistentExtension;

/**
 * Rule for instrumented traces. Enables automatic removal of instrumentation
 * 
 */
class InstrumentedTraceRuleImpl extends RuleBase implements
		TraceModelPersistentExtension, InstrumentedTraceRule {

	/**
	 * Storage name
	 */
	static final String STORAGE_NAME = "Instrumenter"; //$NON-NLS-1$

	/**
	 * ID of instrumenter used when the trace was added
	 */
	private String instrumenterID;

	/**
	 * Constructor for reflection
	 */
	InstrumentedTraceRuleImpl() {
	}

	/**
	 * Constructor
	 * 
	 * @param instrumenterID
	 *            the instrumenter ID
	 */
	InstrumentedTraceRuleImpl(String instrumenterID) {
		this.instrumenterID = instrumenterID;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.model.TraceModelPersistentExtension#getData()
	 */
	public String getData() {
		return String.valueOf(instrumenterID);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.model.TraceModelPersistentExtension#getStorageName()
	 */
	public String getStorageName() {
		return STORAGE_NAME;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.model.TraceModelPersistentExtension#setData(java.lang.String)
	 */
	public boolean setData(String data) {
		boolean retval = false;
		if (data != null && data.length() > 0) {
			instrumenterID = data;
			retval = true;
		}
		return retval;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.engine.InstrumentedTraceRule#getInstrumenterID()
	 */
	public String getInstrumenterID() {
		return instrumenterID;
	}

}
