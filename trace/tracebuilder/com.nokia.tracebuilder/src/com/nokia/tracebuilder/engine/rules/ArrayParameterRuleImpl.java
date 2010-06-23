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
* Rule which defines a parameter as array type
*
*/
package com.nokia.tracebuilder.engine.rules;

import com.nokia.tracebuilder.engine.TraceObjectPropertyDialogEnabler;
import com.nokia.tracebuilder.model.TraceModelPersistentExtension;
import com.nokia.tracebuilder.rules.ArrayParameterRule;

/**
 * Rule which defines a parameter as array type
 * 
 */
final class ArrayParameterRuleImpl extends RuleBase implements
		ArrayParameterRule, TraceModelPersistentExtension,
		TraceObjectPropertyDialogEnabler {

	/**
	 * Storage name for array parameter
	 */
	static final String STORAGE_NAME = "Array"; //$NON-NLS-1$

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.model.TraceModelPersistentExtension#getData()
	 */
	public String getData() {
		return ""; //$NON-NLS-1$
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
	 * @see com.nokia.tracebuilder.model.TraceModelPersistentExtension#
	 *      setData(java.lang.String)
	 */
	public boolean setData(String data) {
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.engine.TraceObjectPropertyDialogEnabler#isIdEnabled()
	 */
	public boolean isIdEnabled() {
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.engine.TraceObjectPropertyDialogEnabler#isNameEnabled()
	 */
	public boolean isNameEnabled() {
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.engine.TraceObjectPropertyDialogEnabler#isTypeEnabled()
	 */
	public boolean isTypeEnabled() {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.engine.TraceObjectPropertyDialogEnabler#isValueEnabled()
	 */
	public boolean isValueEnabled() {
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.engine.TraceObjectPropertyDialogEnabler#isTargetEnabled()
	 */
	public boolean isTargetEnabled() {
		return true;
	}	
}
