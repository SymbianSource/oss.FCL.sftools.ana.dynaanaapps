/*
 * Copyright (c) 2007-2010 Nokia Corporation and/or its subsidiary(-ies). 
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
 * Interface implemented by the view of Trace Viewer
 *
 */
package com.nokia.traceviewer.engine;

/**
 * Interface implemented by the views of TraceViewer. The view registers itself
 * to TraceViewer via TraceViewerInterface.setView call
 * 
 */
public interface TraceViewerViewInterface {

	/**
	 * ID of the propertyView
	 */
	public static final String PROPERTYVIEW_ID = "com.nokia.traceviewer.view.TracePropertyView"; //$NON-NLS-1$

	/**
	 * Tells if the view is disposed. Must be called from UI thread!
	 * 
	 * @return true if view is disposed
	 */
	public boolean isDisposed();

	/**
	 * Clears all data from the display
	 */
	public void clearAll();

	/**
	 * Tells is view has data that isn't shown yet
	 * 
	 * @return true if there is unshown data
	 */
	public boolean hasUnshownData();

	/**
	 * Updates the view
	 */
	public void update();
}
