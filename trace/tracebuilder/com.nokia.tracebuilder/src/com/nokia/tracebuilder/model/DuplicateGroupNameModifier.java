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
* Group name modifier
*
*/
package com.nokia.tracebuilder.model;

/**
 * Group name modifier
 * 
 */
final class DuplicateGroupNameModifier extends DuplicateValueModifier {

	/**
	 * Trace model
	 */
	private TraceModel model;

	/**
	 * Constructor
	 * 
	 * @param model
	 *            the trace model
	 * @param name
	 *            the group name
	 */
	DuplicateGroupNameModifier(TraceModel model, String name) {
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
		return model.findGroupByName(name);
	}
}