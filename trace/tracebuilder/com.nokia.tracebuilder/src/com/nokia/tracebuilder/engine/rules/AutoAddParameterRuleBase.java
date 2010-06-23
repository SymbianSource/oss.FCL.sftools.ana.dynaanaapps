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
* Base class for the auto-add parameter rules
*
*/
package com.nokia.tracebuilder.engine.rules;

import com.nokia.tracebuilder.engine.TraceBuilderGlobals;
import com.nokia.tracebuilder.engine.utils.TraceMultiplierRule;
import com.nokia.tracebuilder.model.Trace;
import com.nokia.tracebuilder.model.TraceBuilderException;
import com.nokia.tracebuilder.model.TraceModelExtension;
import com.nokia.tracebuilder.model.TraceObjectRule;
import com.nokia.tracebuilder.model.TraceObjectRuleCreateObject;
import com.nokia.tracebuilder.model.TraceObjectRuleRemoveOnCreate;
import com.nokia.tracebuilder.model.TraceObjectUtils;

/**
 * Base class for the auto-add parameter rules
 * 
 */
abstract class AutoAddParameterRuleBase extends RuleBase implements
		TraceObjectRuleCreateObject, TraceObjectRuleRemoveOnCreate,
		CopyAndRemoveExtensionRule {

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.model.TraceObjectRuleCreateObject#createObject()
	 */
	public void createObject() {
		TraceObjectRule rule = getRule();
		TraceModelExtension[] extensions = null;
		if (rule != null) {
			extensions = new TraceModelExtension[] { rule };
		}
		Trace owner = (Trace) getOwner();
		int id = owner.getNextParameterID();
		String name = TraceObjectUtils.modifyDuplicateParameterName(owner,
				getName()).getData();
		String type = getType();
		try {
			owner.getModel().getVerifier().checkTraceParameterProperties(owner,
					null, id, name, type);
			owner.getModel().getFactory().createTraceParameter(owner, id, name,
					type, extensions);
		} catch (TraceBuilderException e) {
			TraceBuilderGlobals.getEvents().postError(e);
		}
	}

	/**
	 * Gets the name for the new parameter
	 * 
	 * @return the parameter name
	 */
	protected abstract String getName();

	/**
	 * Gets the new parameter type
	 * 
	 * @return the type
	 */
	protected abstract String getType();

	/**
	 * Gets the rule for the parameter
	 * 
	 * @return the rule
	 */
	protected abstract TraceObjectRule getRule();

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.model.TraceObjectRuleRemoveOnCreate#canBeRemoved()
	 */
	public boolean canBeRemoved() {
		// If the owner has a multiplier, this needs to be moved to it using the
		// CopyAndRemoveExtensionRule
		return getOwner().getExtension(TraceMultiplierRule.class) == null;
	}

}
