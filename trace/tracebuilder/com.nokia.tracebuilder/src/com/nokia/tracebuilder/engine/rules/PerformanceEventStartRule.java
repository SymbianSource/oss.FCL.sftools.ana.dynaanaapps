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
* Rule for performance timer entry trace
*
*/
package com.nokia.tracebuilder.engine.rules;

import com.nokia.tracebuilder.model.Trace;
import com.nokia.tracebuilder.model.TraceObjectRule;

/**
 * Rule for performance timer entry trace
 * 
 */
public class PerformanceEventStartRule extends PerformanceEventRuleBase
		implements CopyExtensionRule {

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.rules.TraceParameterRestrictionRule#canAddParameters()
	 */
	@Override
	public boolean canAddParameters() {
		boolean retval;
		Trace trace = (Trace) getOwner();
		// Supports single parameter
		if (trace != null && trace.getParameterCount() == 0) {
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
	@Override
	public boolean canRemoveParameters() {
		boolean retval;
		Trace trace = (Trace) getOwner();
		// Single parameter can be removed
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
	 * @see com.nokia.tracebuilder.engine.rules.EntryTraceRule#createCopy()
	 */
	public TraceObjectRule createCopy() {
		return new PerformanceEventStopRule();
	}
}
