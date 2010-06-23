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
* Trace name modifier
*
*/
package com.nokia.tracebuilder.model;

/**
 * Trace name modifier
 * 
 */
final class DuplicateTraceNameModifier extends DuplicateValueModifier {

	/**
	 * The trace group
	 */
	private TraceGroup group;

	/**
	 * The trace group
	 */
	private TraceModel model;

	/**
	 * Constructor
	 * 
	 * @param group
	 *            the trace group owning the trace
	 * @param name
	 *            the trace name
	 */
	DuplicateTraceNameModifier(TraceGroup group, String name) {
		super(name);
		this.group = group;
	}

	/**
	 * Constructor
	 * 
	 * @param model
	 *            the trace model
	 * @param name
	 *            the trace name
	 */
	DuplicateTraceNameModifier(TraceModel model, String name) {
		super(name);
		this.model = model;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.model.TraceObjectUtils.DuplicateValueModifier#
	 *      findObject(java.lang.String)
	 */
	@Override
	TraceObject findObject(String name) {
		TraceObject retval;
		if (group != null) {
			retval = group.findTraceByName(name);
		} else {
			retval = model.findTraceByName(name);
		}
		return retval;
	}
}