/*
 * Copyright (c) 2007-2010 Nokia Corporation and/or its subsidiary(-ies). 
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
 * Implementation of ComplexHeaderRule
 *
 */
package com.nokia.tracebuilder.engine.rules;

import com.nokia.tracebuilder.model.Trace;
import com.nokia.tracebuilder.rules.TraceParameterRestrictionRule;

/**
 * Implementation of ComplexHeaderRule
 * 
 */
public final class ComplexHeaderRuleImpl extends RuleBase implements
		ComplexHeaderRule, TraceParameterRestrictionRule {

	/**
	 * Trace ID define extension
	 */
	private String traceIDDefineExtension;

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.nokia.tracebuilder.engine.header.ComplexHeaderRule#needsFunction()
	 */
	public boolean needsFunction() {
		return true;
	}

	/**
	 * Sets the data to be added to the trace ID define statement after the ID
	 * 
	 * @param traceIDDefineExtension
	 *            the extension
	 */
	void setTraceIDDefineExtension(String traceIDDefineExtension) {
		this.traceIDDefineExtension = traceIDDefineExtension;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seecom.nokia.tracebuilder.engine.header.ComplexHeaderRule#
	 * getTraceIDDefineExtension()
	 */
	public String getTraceIDDefineExtension() {
		return traceIDDefineExtension;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.nokia.tracebuilder.rules.TraceParameterRestrictionRule#canAddParameters
	 * ()
	 */
	public boolean canAddParameters() {
		boolean retval;
		Trace trace = (Trace) getOwner();

		// Max parameter count int OstTraceExt traces is 5. Parameters can not
		// be added to OstTraceFunctionEntryExt and OstTraceFunctionExitExt
		// traces.
		if (trace != null && trace.getParameterCount() < 5
				&& trace.getExtension(EntryTraceRule.class) == null
				&& trace.getExtension(ExitTraceRule.class) == null) {
			retval = true;
		} else {
			retval = false;
		}
		return retval;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seecom.nokia.tracebuilder.rules.TraceParameterRestrictionRule#
	 * canRemoveParameters()
	 */
	public boolean canRemoveParameters() {
		boolean retval;
		Trace trace = (Trace) getOwner();

		// Parameters can not be removed from OstTraceFunctionEntryExt and
		// OstTraceFunctionExitExt traces.
		if (trace.getExtension(EntryTraceRule.class) == null
				&& trace.getExtension(ExitTraceRule.class) == null) {
			retval = true;
		} else {
			retval = false;
		}
		return retval;
	}
}