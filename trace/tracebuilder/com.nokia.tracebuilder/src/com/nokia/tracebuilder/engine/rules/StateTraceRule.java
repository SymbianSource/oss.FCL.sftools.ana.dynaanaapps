/*
* Copyright (c) 2009 Nokia Corporation and/or its subsidiary(-ies). 
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
* Rule for state traces
*
*/
package com.nokia.tracebuilder.engine.rules;

import com.nokia.tracebuilder.engine.utils.TraceUtils;
import com.nokia.tracebuilder.model.Trace;
import com.nokia.tracebuilder.model.TraceObjectRule;
import com.nokia.tracebuilder.rules.TraceParameterRestrictionRule;
import com.nokia.tracebuilder.source.SourceContext;

/**
 * Rule for state traces
 * 
 */
public class StateTraceRule extends AutomaticTraceTextRule implements
		CopyExtensionRule, TraceParameterRestrictionRule {

	/**
	 * Mandatory parameter count
	 */
	private static final int MANDATORY_PARAMETER_COUNT = 2; // CodForChk_Dis_Magic
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.rules.TraceParameterRestrictionRule#canAddParameters()
	 */
	public boolean canAddParameters() {
		boolean retval;
		Trace trace = (Trace) getOwner();
		// Supports max three parameters
		if (trace != null && trace.getParameterCount() <= MANDATORY_PARAMETER_COUNT) { 
			retval = true;
		} else {
			retval = false;
		}
		return retval;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.rules.TraceParameterRestrictionRule#canRemoveParameters()
	 */
	public boolean canRemoveParameters() {
		boolean retval;
		Trace trace = (Trace) getOwner();
		// There must be at least two parameters in State trace
		if (trace != null && trace.getParameterCount() > MANDATORY_PARAMETER_COUNT) { 
			retval = true;
		} else {
			retval = false;
		}
		return retval;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.engine.rules.CopyExtensionRule#createCopy()
	 */
	public TraceObjectRule createCopy() {
		return new StateTraceRule();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.engine.rules.AutomaticTraceTextRule#
	 *      formatTrace(com.nokia.tracebuilder.source.SourceContext)
	 */
	@Override
	public String formatTrace(SourceContext context) {
		return TraceUtils.formatTrace(RuleUtils.TEXT_FORMAT_BASE, context
				.getClassName(), context.getFunctionName());
	}
}
