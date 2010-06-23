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
* Wrapper for a single object in trace view
*
*/
package com.nokia.tracebuilder.view;

/**
 * Wrapper for a single object in trace view
 * 
 */
class ObjectWrapper extends WrapperBase {

	/**
	 * The object object
	 */
	private Object object;

	/**
	 * Creates a wrapper
	 * 
	 * @param object
	 *            the object
	 * @param parent
	 *            the parent wrapper
	 * @param updater
	 *            the update notifier
	 */
	public ObjectWrapper(Object object, WrapperBase parent,
			WrapperUpdater updater) {
		super(parent, updater);
		this.object = object;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.view.WrapperBase#getChildren()
	 */
	@Override
	public Object[] getChildren() {
		return new Object[0];
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.view.WrapperBase#hasChildren()
	 */
	@Override
	public boolean hasChildren() {
		return false;
	}

	/**
	 * Gets the wrapped object.
	 * 
	 * @return the object
	 */
	public Object getObject() {
		return object;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.view.WrapperBase#delete()
	 */
	@Override
	public void delete() {
		super.delete();
		object = null;
	}
}
