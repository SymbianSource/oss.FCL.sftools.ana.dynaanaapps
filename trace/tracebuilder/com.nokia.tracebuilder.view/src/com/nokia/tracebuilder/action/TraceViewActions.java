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
* Allows TraceBuilder actions to be associated to different menus
*
*/
package com.nokia.tracebuilder.action;

import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;

import com.nokia.tracebuilder.engine.TraceBuilderActions;

/**
 * Allows TraceBuilder actions to be associated to different menus
 * 
 */
public interface TraceViewActions extends TraceBuilderActions {

	/**
	 * Fills the given context menu with TraceBuilder actions. Actions are based
	 * on last call to enableActions
	 * 
	 * @param manager
	 *            the menu manager
	 */
	public void fillContextMenu(IMenuManager manager);

	/**
	 * Fills the given menu with TraceBuilder actions. Actions are based on last
	 * call to enableActions
	 * 
	 * @param manager
	 *            the menu manager
	 */
	public void fillMenu(IMenuManager manager);

	/**
	 * Fills the given toolbar with TraceBuilder actions
	 * 
	 * @param manager
	 *            the toolbar manager
	 */
	public void fillToolBar(IToolBarManager manager);

}
