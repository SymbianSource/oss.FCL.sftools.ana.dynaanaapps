/*
* Copyright (c) 2008 Nokia Corporation and/or its subsidiary(-ies). 
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
* Rule for exit traces
*
*/
package com.nokia.tracebuilder.engine.rules;

import com.nokia.tracebuilder.engine.utils.TraceUtils;
import com.nokia.tracebuilder.model.Trace;
import com.nokia.tracebuilder.rules.TraceParameterRestrictionRule;
import com.nokia.tracebuilder.source.SourceContext;

/**
 * Rule for exit traces
 * 
 */
public class ExitTraceRule extends AutomaticTraceTextRule implements
		TraceParameterRestrictionRule {

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.rules.TraceParameterRestrictionRule#canAddParameters()
	 */
	public boolean canAddParameters() {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.rules.TraceParameterRestrictionRule#canRemoveParameters()
	 */
	public boolean canRemoveParameters() {
		boolean retval;
		Trace trace = (Trace) getOwner();
		// Parameters cannot be removed from Ext trace
		if (trace != null && trace.getParameterCount() == 1) {
			retval = true;
		} else {
			retval = false;
		}
		return retval;
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
