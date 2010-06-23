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
 * Interface implemented by the some view of Trace Viewer to show up and create dialogs
 *
 */
package com.nokia.traceviewer.engine;

/**
 * Interface implemented by the some view of Trace Viewer to show up and create
 * dialogs
 * 
 */
public interface TraceViewerDialogInterface {

	/**
	 * Enum containing the names for TraceViewer dialogs
	 */
	public enum Dialog {

		/**
		 * Filter dialog
		 */
		FILTER,

		/**
		 * Advanced filter dialog
		 */
		ADVANCEDFILTER,

		/**
		 * Search dialog
		 */
		SEARCH,

		/**
		 * Color dialog
		 */
		COLOR,

		/**
		 * Count lines dialog
		 */
		COUNTLINES,

		/**
		 * Variable tracing dialog
		 */
		VARIABLETRACING,

		/**
		 * Variable Tracing history dialog
		 */
		VARIBLETRACINGHISTORY,

		/**
		 * Trace activation dialog
		 */
		TRACEACTIVATION,

		/**
		 * Log dialog
		 */
		LOG,

		/**
		 * Trigger dialog
		 */
		TRIGGER,

		/**
		 * ProgressBar dialog
		 */
		PROGRESSBAR;
	}

	/**
	 * Enum containing the preference pages for TraceViewer
	 */
	public enum TVPreferencePage {

		/**
		 * General preference pages
		 */
		GENERAL,

		/**
		 * Advanced preference page
		 */
		ADVANCED,

		/**
		 * Configurations preference page
		 */
		CONFIGURATIONS,

		/**
		 * Connection preference page
		 */
		CONNECTION;
	}

	/**
	 * Shows an error message
	 * 
	 * @param error
	 *            error string
	 */

	public void showErrorMessage(String error);

	/**
	 * Shows an information message to user
	 * 
	 * @param message
	 *            the message to show
	 */
	public void showInformationMessage(String message);

	/**
	 * Shows confirmation dialog with ok and cancel choices
	 * 
	 * @param message
	 *            message to show
	 * @return true if user clicked ok, false if cancel
	 */
	public boolean showConfirmationDialog(String message);

	/**
	 * Opens project preference page
	 * 
	 * @param page
	 *            the page name
	 * @return true if user pressed OK from the dialog, false otherwise
	 */
	public boolean openPreferencePage(TVPreferencePage page);

	/**
	 * Creates a TraceViewer dialog
	 * 
	 * @param name
	 *            name for the dialog
	 * @return the newly created dialog
	 */
	public TraceViewerDialog createDialog(Dialog name);
}
