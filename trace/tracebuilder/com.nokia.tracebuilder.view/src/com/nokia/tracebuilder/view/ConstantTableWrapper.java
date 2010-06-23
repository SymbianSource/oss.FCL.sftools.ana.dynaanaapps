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
* Wrapper for a constant table
*
*/
package com.nokia.tracebuilder.view;

import java.util.Iterator;

import com.nokia.tracebuilder.model.TraceConstantTable;
import com.nokia.tracebuilder.model.TraceConstantTableEntry;

/**
 * Wrapper for a constant table
 * 
 */
final class ConstantTableWrapper extends TraceObjectWrapper {

	/**
	 * Wrapper constructor
	 * 
	 * @param table
	 *            constant table
	 * @param parent
	 *            parent wrapper
	 * @param updater
	 *            the update notifier
	 */
	ConstantTableWrapper(TraceConstantTable table, WrapperBase parent,
			WrapperUpdater updater) {
		super(table, parent, updater);
		Iterator<TraceConstantTableEntry> itr = table.getEntries();
		while (itr.hasNext()) {
			add(new ConstantTableEntryWrapper(itr.next(), this, updater));
		}
	}

	/**
	 * Adds a entry to this table
	 * 
	 * @param entry
	 *            the entry to be added
	 * @return the wrapper which needs to be refreshed
	 */
	WrapperBase addConstantTableEntry(TraceConstantTableEntry entry) {
		add(new ConstantTableEntryWrapper(entry, this, getUpdater()));
		return this;
	}

	/**
	 * Removes a table entry
	 * 
	 * @param entry
	 *            the entry to be removed
	 * @return the wrapper which needs to be refreshed
	 */
	WrapperBase removeConstantTableEntry(TraceConstantTableEntry entry) {
		remove(entry.getExtension(ConstantTableEntryWrapper.class));
		return this;
	}
}
