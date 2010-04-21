/*
 * Copyright (c) 2010 Nokia Corporation and/or its subsidiary(-ies). 
 * All rights reserved.
 * This component and the accompanying materials are made available
 * under the terms of the License "Eclipse Public License v1.0"
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
 */

package com.nokia.carbide.cpp.internal.pi.plugin.model;

import org.eclipse.jface.action.Action;

/**
 * Interface that is used when Performance Investigator's graph plug-in uses
 * drop-down menu in the title bar of its graph view.
 */

public interface ITitleBarMenu extends IMenu {

	/**
	 * Method that is called when graph is initialized.
	 * @return Array of actions that are placed into drop-down menu. Each
	 *         action's run()-method is called when the action is selected from
	 *         drop-down menu. If some of the actions need to be preselected
	 *         when view is opened its isChecked method should return true.
	 */
	public Action[] addTitleBarMenuItems();
	
	/**
	 * Returns the main context help id for this view which is displayed when the
	 * user presses the question mark button on the toolbar
	 * @return main context help id for analysis view
	 */
	public String getContextHelpId();
}
