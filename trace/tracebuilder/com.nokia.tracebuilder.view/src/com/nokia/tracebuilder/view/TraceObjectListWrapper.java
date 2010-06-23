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
* Wrapper for list of TraceObject instances
*
*/
package com.nokia.tracebuilder.view;

/**
 * Wrapper for list of TraceObject instances
 * 
 */
abstract class TraceObjectListWrapper extends ListWrapper {

	/**
	 * Constructor
	 * 
	 * @param parent
	 *            parent wrapper
	 * @param updater
	 *            the update notifier
	 */
	TraceObjectListWrapper(WrapperBase parent, WrapperUpdater updater) {
		super(parent, updater);
	}

	/**
	 * Hides this object from view
	 * 
	 * @return the wrapper that needs to be refreshed
	 */
	WrapperBase hideFromView() {
		WrapperBase parent = getParent();
		if (parent instanceof TraceObjectWrapper) {
			((TraceObjectWrapper) parent).hide(this);
		}
		return parent;
	}

}
