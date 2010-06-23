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
* Interface provided to other components via TraceBuilderGlobals
*
*/
package com.nokia.tracebuilder.engine;

import java.util.List;

/**
 * Interface provided to other components via {@link TraceBuilderGlobals}. This
 * can be used to show various query and error dialogs to the user
 * 
 */
public interface TraceBuilderDialogs {

	/**
	 * Parameters for a check list dialog
	 * 
	 */
	public class CheckListDialogParameters {

		/**
		 * The dialog type
		 */
		public CheckListDialogType dialogType;

		/**
		 * Root items for the dialog
		 */
		public List<CheckListDialogEntry> rootItems;

		/**
		 * Initial expand level for dialog tree
		 */
		public int expandLevel;

		/**
		 * Flag to show / hide the root element
		 */
		public boolean showRoot;

	}

	/**
	 * Parameters for a query dialog
	 * 
	 */
	public class QueryDialogParameters {

		/**
		 * The query dialog type
		 */
		public QueryDialogType dialogType;

	}

	/**
	 * Query, which shows a file path but does not allow the user to change it
	 * 
	 */
	public class FileQueryParameters extends QueryDialogParameters {

		/**
		 * File path
		 */
		public String path;

	}

	/**
	 * Query, which allows user to select a file via file dialog
	 * 
	 */
	public class FileDialogQueryParameters extends FileQueryParameters {

		/**
		 * File filter titles
		 */
		public String[] filterTitles;

		/**
		 * File filters
		 */
		public String[] filters;

	}

	/**
	 * Query, which allows user to select a directory via directory dialog
	 * 
	 */
	public class DirectoryDialogQueryParameters extends FileQueryParameters {
	}

	/**
	 * Parameters for delete object query
	 * 
	 */
	public class DeleteObjectQueryParameters extends QueryDialogParameters {

		/**
		 * Name of the object to be deleted
		 */
		public String objectName;

		/**
		 * Name of the owner of the object to be deleted
		 */
		public String ownerName;

	}

	/**
	 * Query with user-defined message and buttons
	 * 
	 */
	public class ExtendedQueryParameters extends QueryDialogParameters {

		/**
		 * Dialog message
		 */
		public String message;

		/**
		 * Titles for the buttons
		 */
		public String[] buttonTitles;

	}

	/**
	 * Query dialog types
	 * 
	 */
	enum QueryDialogType {

		/**
		 * Delete group confirmation query type
		 */
		DELETE_GROUP,

		/**
		 * Delete trace confirmation query type
		 */
		DELETE_TRACE,

		/**
		 * Delete parameter confirmation query type
		 */
		DELETE_PARAMETER,

		/**
		 * Delete constant table query type
		 */
		DELETE_CONSTANT_TABLE,

		/**
		 * Delete constant query type
		 */
		DELETE_CONSTANT,

		/**
		 * Import project query
		 */
		IMPORT_TRACE_PROJECT,

		/**
		 * Select environment query
		 */
		SELECT_TARGET_ENVIRONMENT,

		/**
		 * Model update when source is not open
		 */
		UPDATE_WHEN_SOURCE_NOT_OPEN,

		/**
		 * Replace existing file query type. This does not use the normal Yes /
		 * No dialog
		 */
		REPLACE_FILE
	}

	/**
	 * Checklist dialog type
	 * 
	 */
	enum CheckListDialogType {

		/**
		 * Delete multiple traces check list type
		 */
		DELETE_TRACES,

		/**
		 * Instrument files check list type
		 */
		INSTRUMENT_FILES

	}

	/**
	 * OK result
	 */
	int OK = 0; // CodForChk_Dis_Magic

	/**
	 * Cancel result
	 */
	int CANCEL = 1; // CodForChk_Dis_Magic

	/**
	 * Shows an error dialog to user. This must use display.asyncExec instead of
	 * showing the dialog directly
	 * 
	 * @param message
	 *            the message
	 */
	public void showErrorMessage(String message);

	/**
	 * Shows a confirmation (Yes / No) dialog to the user. This uses one of the
	 * pre-defined confirmation query types.
	 * 
	 * @param parameters
	 *            the parameters for the query
	 * @return YES / NO
	 */
	public int showConfirmationQuery(QueryDialogParameters parameters);

	/**
	 * Shows a dialog with list of selectable items and OK / Cancel buttons.
	 * This uses one of the pre-defined check list dialog types.
	 * 
	 * @param parameters
	 *            the parameters for the dialog
	 * @return OK / CANCEL
	 */
	public int showCheckList(CheckListDialogParameters parameters);

}
