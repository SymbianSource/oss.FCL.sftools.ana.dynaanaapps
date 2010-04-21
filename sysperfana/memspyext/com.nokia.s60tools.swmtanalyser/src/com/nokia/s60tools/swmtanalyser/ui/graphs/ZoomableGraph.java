/*
* Copyright (c) 2009 Nokia Corporation and/or its subsidiary(-ies). 
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
*/


package com.nokia.s60tools.swmtanalyser.ui.graphs;

import org.eclipse.jface.action.IAction;

/**
 * Common abstract base class for graphs that support zoom operations.
 */
public abstract class ZoomableGraph {
	
	
	/**
	 * Context menu title for action: Zoom In
	 */
	protected static final String ZOOM_IN_CONTEXT_MENU_TITLE = "Zoom In";

	/**
	 * Context menu title for action: Zoom Out
	 */
	protected static final String ZOOM_OUT_CONTEXT_MENU_TITLE = "Zoom Out";

	/**
	 * Copy -action
	 */
	protected IAction copy;
	
	
	/**
	 * Zoom In -action
	 */
	protected IAction zoomIn;

	/**
	 * Zoom Out -action
	 */
	protected IAction zoomOut;
	
	/**
	 * This method zooms in the graph area.
	 */
	protected abstract void zoomIn();
	
	/**
	 * This method zooms out the graph area.
	 */
	protected abstract void zoomOut();

	/**
	 * Sets given enable state for an action if it is non <code>null</code>.
	 * @param action Action to set enable status for.
	 * @param enableStatus <code>true</code> if enabled, otherwise <code>false</code>.
	 */
	protected void setEnableState(IAction action, boolean enableStatus) {
		if(action != null){
			action.setEnabled(enableStatus);			
		}
	}
}
