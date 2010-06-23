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
* Wrapper for a trace parameter
*
*/
package com.nokia.tracebuilder.view;

import com.nokia.tracebuilder.model.TraceModelListener;
import com.nokia.tracebuilder.model.TraceParameter;
import com.nokia.tracebuilder.rules.ArrayParameterRule;

/**
 * Wrapper for a trace parameter
 * 
 */
final class TraceParameterWrapper extends TraceObjectWrapper {

	/**
	 * Array type flag
	 */
	private boolean isArray;

	/**
	 * Constructor
	 * 
	 * @param parameter
	 *            the parameter
	 * @param parent
	 *            the parent wrapper
	 * @param updater
	 *            the update notifier
	 */
	TraceParameterWrapper(TraceParameter parameter, WrapperBase parent,
			WrapperUpdater updater) {
		super(parameter, parent, updater);
		isArray = parameter.getExtension(ArrayParameterRule.class) != null;
	}

	/**
	 * Gets the array type flag
	 * 
	 * @return the array flag
	 */
	boolean isArrayType() {
		return isArray;
	}

	/**
	 * Sets the array type flag
	 * 
	 * @param array
	 *            the array flag
	 * @return the wrapper that needs to be refreshed
	 */
	WrapperBase setArrayType(boolean array) {
		isArray = array;
		return refreshProperty(TraceModelListener.TYPE);
	}

}
