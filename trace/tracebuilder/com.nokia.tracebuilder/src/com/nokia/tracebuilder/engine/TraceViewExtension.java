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
* Extensions to the trace view that can be stored into the model implement this interface
*
*/
package com.nokia.tracebuilder.engine;

import java.util.Iterator;

import com.nokia.tracebuilder.model.TraceModelUpdatableExtension;

/**
 * Extensions to the trace view that can be stored into the model implement this
 * interface. The view implementation needs to be able to show all the
 * extensions it finds from the model.
 * 
 */
public interface TraceViewExtension extends TraceModelUpdatableExtension {

	/**
	 * Gets the children of this extension
	 * 
	 * @return the children in array
	 */
	public Iterator<?> getChildren();

	/**
	 * Determines if this extension has children
	 * 
	 * @return true if this has children
	 */
	public boolean hasChildren();

	/**
	 * Flag that determines whether to show or hide the extension when it has no
	 * children
	 * 
	 * @return the flag
	 */
	public boolean hideWhenEmpty();

	/**
	 * View reference must be stored and returned via getViewReference
	 * 
	 * @param reference
	 *            the reference
	 */
	public void setViewReference(Object reference);

	/**
	 * Gets the view reference
	 * 
	 * @return the view reference
	 */
	public Object getViewReference();

}
