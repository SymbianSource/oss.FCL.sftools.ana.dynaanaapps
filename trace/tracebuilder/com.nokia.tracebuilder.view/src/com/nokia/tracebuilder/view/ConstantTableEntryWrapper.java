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

import com.nokia.tracebuilder.model.TraceConstantTableEntry;

/**
 * Wrapper for a constant table entry
 * 
 */
final class ConstantTableEntryWrapper extends TraceObjectWrapper {

	/**
	 * Default constructor
	 * 
	 * @param entry
	 *            the table entry
	 * @param parent
	 *            parent wrapper
	 * @param updater
	 *            the update notifier
	 */
	ConstantTableEntryWrapper(TraceConstantTableEntry entry,
			WrapperBase parent, WrapperUpdater updater) {
		super(entry, parent, updater);
	}

}
