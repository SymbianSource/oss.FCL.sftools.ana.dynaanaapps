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
* View adapter
*
*/
package com.nokia.tracebuilder.engine;

import com.nokia.tracebuilder.model.TraceObject;

/**
 * View adapter implements the view interface.
 * 
 */
public class ViewAdapter implements TraceBuilderView {

	/**
	 * Actions interface
	 */
	private TraceBuilderActions actions;

	/**
	 * Dialogs interface
	 */
	private TraceBuilderDialogs dialogs;

	/**
	 * Property dialog interface
	 */
	private TraceObjectPropertyDialog propertyDialog;

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.engine.TraceBuilderView#
	 *      createPropertyDialog()
	 */
	public TraceObjectPropertyDialog getPropertyDialog() {
		if (propertyDialog == null) {
			propertyDialog = new PropertyDialogAdapter();
		}
		return propertyDialog;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.engine.TraceBuilderView#getActions()
	 */
	public TraceBuilderActions getActions() {
		if (actions == null) {
			actions = new ActionsAdapter();
		}
		return actions;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.engine.TraceBuilderView#getConfiguration()
	 */
	public TraceBuilderConfiguration getConfiguration() {
		// This can be null, the configuration delegate checks it
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.engine.TraceBuilderView#getDialogs()
	 */
	public TraceBuilderDialogs getDialogs() {
		if (dialogs == null) {
			dialogs = new DialogsAdapter();
		}
		return dialogs;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.engine.TraceBuilderView#hasFocus()
	 */
	public boolean hasFocus() {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.engine.TraceBuilderView#
	 *      runAsyncOperation(java.lang.Runnable)
	 */
	public void runAsyncOperation(Runnable runnable) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.engine.TraceBuilderView#
	 *      selectLocation(com.nokia.tracebuilder.engine.TraceLocation)
	 */
	public void selectLocation(TraceLocation location) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.engine.TraceBuilderView#
	 *      selectObject(com.nokia.tracebuilder.model.TraceObject)
	 */
	public void selectObject(TraceObject object) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.engine.TraceBuilderView#setFocus()
	 */
	public void setFocus() {
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.engine.TraceBuilderView#
	 *      refresh()
	 */
	public void refresh() {
	}
	
	/* (non-Javadoc)
	 * @see com.nokia.tracebuilder.engine.TraceBuilderView#expandTraceGroupsBranch()
	 */
	public void expandTraceGroupsBranch() {
	}

}
