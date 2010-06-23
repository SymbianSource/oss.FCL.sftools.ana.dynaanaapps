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
* Base class for performance event rules
*
*/
package com.nokia.tracebuilder.engine.rules;

import com.nokia.tracebuilder.rules.TraceParameterRestrictionRule;

/**
 * Base class for performance event rules
 * 
 */
public class PerformanceEventRuleBase extends RuleBase implements
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
		return false;
	}

}
