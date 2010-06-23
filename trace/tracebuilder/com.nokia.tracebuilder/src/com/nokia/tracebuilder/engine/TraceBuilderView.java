/*
* Copyright (c) 2008 Nokia Corporation and/or its subsidiary(-ies). 
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
* Interface implemented by the view of Trace Builder
*
*/
package com.nokia.tracebuilder.engine;

import com.nokia.tracebuilder.model.TraceObject;

/**
 * Interface implemented by the view of Trace Builder. The view is implemented
 * as a separate plug-in, which is registered via
 * {@link TraceBuilderGlobals#setView(TraceBuilderView)}
 * 
 * @see com.nokia.tracebuilder.engine.TraceBuilderGlobals#setView(TraceBuilderView)
 */
public interface TraceBuilderView {

	/**
	 * Selects a trace object from the view
	 * 
	 * @param object
	 *            the object to be selected
	 */
	public void selectObject(TraceObject object);

	/**
	 * Selects a location from the view.
	 * 
	 * @param location
	 *            the location to be selected
	 */
	public void selectLocation(TraceLocation location);

	/**
	 * Sets the focus to the view
	 */
	public void setFocus();

	/**
	 * Checks if the view has the focus
	 * 
	 * @return true if view has focus
	 */
	public boolean hasFocus();

	/**
	 * Gets a dialog for editing trace object properties
	 * 
	 * @return the dialog
	 */
	public TraceObjectPropertyDialog getPropertyDialog();

	/**
	 * Gets the dialogs interface
	 * 
	 * @return the dialogs
	 */
	public TraceBuilderDialogs getDialogs();

	/**
	 * Gets the actions interface
	 * 
	 * @return the actions interface
	 */
	public TraceBuilderActions getActions();

	/**
	 * Gets the configuration interface
	 * 
	 * @return the configuration interface
	 */
	public TraceBuilderConfiguration getConfiguration();

	/**
	 * Runs an asynchronous operation
	 * 
	 * @param runnable
	 *            the operation to be run
	 */
	public void runAsyncOperation(Runnable runnable);

	/**
	 * Refresh the view
	 */
	public void refresh();
	
	/**
	 * Expand Trace Groups branch
	 */
	public void expandTraceGroupsBranch();
}
