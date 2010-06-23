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
 * Base class for all action objects of Trace Viewer view
 *
 */
package com.nokia.traceviewer.action;

import org.eclipse.jface.action.Action;

import com.nokia.traceviewer.engine.TraceViewerGlobals;

/**
 * Base class for all action objects of Trace Viewer view
 * 
 */
abstract class TraceViewerAction extends Action {

	/**
	 * Constructor
	 */
	protected TraceViewerAction() {
	}

	/**
	 * Constructor with action type
	 * 
	 * @param type
	 *            the action type
	 */
	protected TraceViewerAction(int type) {
		super("", type); //$NON-NLS-1$
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.action.IAction#run()
	 */
	@Override
	public void run() {
		if (TraceViewerGlobals.getTraceProvider() != null) {
			doRun();
		} else {
			String providerMissingMsg = Messages
					.getString("TraceViewerAction.ProviderMissingError"); //$NON-NLS-1$

			// Show message that trace provider is missing
			TraceViewerGlobals.getTraceViewer().getDialogs().showErrorMessage(
					providerMissingMsg);
		}
	}

	/**
	 * Runs this action
	 */
	protected abstract void doRun();
}
