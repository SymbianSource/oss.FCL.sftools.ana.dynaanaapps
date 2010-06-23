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
* Constant name modifier
*
*/
package com.nokia.tracebuilder.model;

/**
 * Constant name modifier
 * 
 */
final class DuplicateConstantNameModifier extends DuplicateValueModifier {

	/**
	 * The constant table
	 */
	private TraceConstantTable table;

	/**
	 * Constructor
	 * 
	 * @param table
	 *            the table owning the entry
	 * @param name
	 *            the constant name
	 */
	DuplicateConstantNameModifier(TraceConstantTable table, String name) {
		super(name);
		this.table = table;
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
		if (table != null) {
			retval = table.findEntryByName(name);
		} else {
			retval = null;
		}
		return retval;
	}
}