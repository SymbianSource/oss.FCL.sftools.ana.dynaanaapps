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
* Wrapper for parameter list
*
*/
package com.nokia.tracebuilder.view;

import com.nokia.tracebuilder.model.Trace;
import com.nokia.tracebuilder.model.TraceParameter;

/**
 * Wrapper for parameter list
 * 
 */
final class TraceParameterListWrapper extends TraceObjectListWrapper {

	/**
	 * Constructor adds the parameters of the trace object to the list
	 * 
	 * @param owner
	 *            the trace
	 * @param parent
	 *            parent wrapper
	 * @param updater
	 *            the update notifier
	 */
	TraceParameterListWrapper(Trace owner, WrapperBase parent,
			WrapperUpdater updater) {
		super(parent, updater);
		for (TraceParameter param : owner) {
			addParameter(param);
		}
	}

	/**
	 * Adds a new parameter to the list
	 * 
	 * @param parameter
	 *            the parameter
	 * @return the wrapper to be refreshed
	 */
	WrapperBase addParameter(TraceParameter parameter) {
		add(new TraceParameterWrapper(parameter, this, getUpdater()));
		return this;
	}

	/**
	 * Removes a parameter from this list
	 * 
	 * @param parameter
	 *            the parameter to be removed
	 * @return the wrapper to be refreshed
	 */
	WrapperBase removeParameter(TraceParameter parameter) {
		remove(parameter.getExtension(TraceParameterWrapper.class));
		return this;
	}
}