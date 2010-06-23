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
* Wrapper for list of constant tables
*
*/
package com.nokia.tracebuilder.view;

import java.util.Iterator;

import com.nokia.tracebuilder.model.TraceConstantTable;
import com.nokia.tracebuilder.model.TraceModel;

/**
 * Wrapper for list of constant tables
 * 
 */
final class ConstantTableListWrapper extends TraceObjectListWrapper {

	/**
	 * Creates a new wrapper
	 * 
	 * @param model
	 *            trace model
	 * @param parent
	 *            parent wrapper
	 * @param updater
	 *            the update notifier
	 */
	ConstantTableListWrapper(TraceModel model, WrapperBase parent,
			WrapperUpdater updater) {
		super(parent, updater);
		Iterator<TraceConstantTable> itr = model.getConstantTables();
		while (itr.hasNext()) {
			addConstantTable(itr.next());
		}
	}

	/**
	 * Adds a new constant table
	 * 
	 * @param table
	 *            the table to be added
	 * @return the wrapper which needs to be refreshed
	 */
	WrapperBase addConstantTable(TraceConstantTable table) {
		add(new ConstantTableWrapper(table, this, getUpdater()));
		return this;
	}

	/**
	 * Removes a constant table
	 * 
	 * @param table
	 *            the constant table to be removed
	 * @return the wrapper which needs to be refreshed
	 */
	WrapperBase removeConstantTable(TraceConstantTable table) {
		remove(table.getExtension(ConstantTableWrapper.class));
		return this;
	}
}
