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
* Modifier for duplicate constant table names
*
*/
package com.nokia.tracebuilder.model;

/**
 * Modifier for duplicate constant table names
 * 
 */
final class DuplicateConstantTableNameModifier extends DuplicateValueModifier {

	/**
	 * Trace model
	 */
	private TraceModel model;

	/**
	 * Constructor
	 * 
	 * @param model
	 *            the trace model
	 * @param value
	 *            the value to be modified
	 */
	DuplicateConstantTableNameModifier(TraceModel model, String value) {
		super(value);
		this.model = model;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.model.DuplicateValueModifier#findObject(java.lang.String)
	 */
	@Override
	TraceObject findObject(String value) {
		return model.findConstantTableByName(value);
	}

}
