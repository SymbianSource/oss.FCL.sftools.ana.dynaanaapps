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
* Creation rule for this parameter
*
*/
package com.nokia.tracebuilder.engine.rules;

import com.nokia.tracebuilder.engine.TraceBuilderGlobals;
import com.nokia.tracebuilder.model.TraceObjectRule;
import com.nokia.tracebuilder.model.TraceParameter;
import com.nokia.tracebuilder.source.SourceContext;

/**
 * Creation rule for "this pointer" parameter
 * 
 */
public final class AutoAddThisPtrRule extends AutoAddParameterRuleBase {

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.engine.rules.AutoAddParameterRuleBase#createObject()
	 */
	@Override
	public void createObject() {
		SourceContext context = TraceBuilderGlobals.getSourceContextManager()
				.getContext();
		if (context != null && !RuleUtils.isStaticFunction(context)) {
			super.createObject();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.engine.rules.CopyExtensionRule#createCopy()
	 */
	public TraceObjectRule createCopy() {
		return new AutoAddThisPtrRule();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.engine.rules.AutoAddParameterRuleBase#getName()
	 */
	@Override
	protected String getName() {
		return ThisPointerParameterTemplate.PARAMETER_NAME;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.engine.rules.AutoAddParameterRuleBase#getRule()
	 */
	@Override
	protected TraceObjectRule getRule() {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.engine.rules.AutoAddParameterRuleBase#getType()
	 */
	@Override
	protected String getType() {
		return TraceParameter.HEX32;
	}

}
