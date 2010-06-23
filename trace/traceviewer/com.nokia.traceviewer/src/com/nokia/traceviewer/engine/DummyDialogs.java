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
 * Dummy Dialog class
 *
 */
package com.nokia.traceviewer.engine;

/**
 * Dummy Dialog class
 * 
 */
public final class DummyDialogs implements TraceViewerDialogInterface {

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.nokia.traceviewer.engine.TraceViewerDialogInterface#openPreferencePage
	 * (
	 * com.nokia.traceviewer.engine.TraceViewerDialogInterface.TVPreferencePage)
	 */
	public boolean openPreferencePage(TVPreferencePage page) {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seecom.nokia.traceviewer.engine.TraceViewerDialogInterface#
	 * showConfirmationDialog(java.lang.String)
	 */
	public boolean showConfirmationDialog(String message) {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.nokia.traceviewer.engine.TraceViewerDialogInterface#showErrorMessage
	 * (java.lang.String)
	 */
	public void showErrorMessage(String error) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seecom.nokia.traceviewer.engine.TraceViewerDialogInterface#
	 * showInformationMessage(java.lang.String)
	 */
	public void showInformationMessage(String message) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.traceviewer.engine.TraceViewerDialogInterface
	 * #createDialog
	 * (com.nokia.traceviewer.engine.TraceViewerDialogInterface.Dialog)
	 */
	public TraceViewerDialog createDialog(Dialog name) {
		// Return dummy TraceViewerDialog to prevent null pointer exception
		return new TraceViewerDialog() {

			public void openDialog() {
			}

		};
	}

}
