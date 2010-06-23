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
* Creates the action objects for Trace Builder view
*
*/
package com.nokia.tracebuilder.action;

import java.util.Iterator;

import org.eclipse.jface.action.IAction;

import com.nokia.tracebuilder.engine.TraceViewExtension;

/**
 * Interface that can be implemented by trace view extensions to provide actions
 * to the view
 * 
 */
public interface TraceViewActionProvider extends TraceViewExtension {

	/**
	 * Gets a iterator of action objects
	 * 
	 * @return the action list iterator
	 */
	public Iterator<IAction> getActions();

}
