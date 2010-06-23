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
 * Constructs TraceViewer dialogs
 *
 */
package com.nokia.traceviewer.view;

import org.eclipse.swt.widgets.Shell;

import com.nokia.traceviewer.dialog.ColorDialog;
import com.nokia.traceviewer.dialog.FilterAdvancedDialog;
import com.nokia.traceviewer.dialog.FilterDialog;
import com.nokia.traceviewer.dialog.LineCountDialog;
import com.nokia.traceviewer.dialog.ProgressBarDialog;
import com.nokia.traceviewer.dialog.SearchDialog;
import com.nokia.traceviewer.dialog.TraceActivationDialog;
import com.nokia.traceviewer.dialog.TriggerDialog;
import com.nokia.traceviewer.dialog.VariableTracingDialog;
import com.nokia.traceviewer.dialog.VariableTracingHistoryDialog;
import com.nokia.traceviewer.dialog.treeitem.TreeItem;
import com.nokia.traceviewer.dialog.treeitem.TreeItemContentProvider;
import com.nokia.traceviewer.engine.DataProcessorAccess;
import com.nokia.traceviewer.engine.TraceViewerDialog;
import com.nokia.traceviewer.engine.TraceViewerGlobals;
import com.nokia.traceviewer.engine.TraceViewerDialogInterface.Dialog;

/**
 * Constructs TraceViewer dialogs
 * 
 */
public final class DialogFactory {

	/**
	 * Constructs a new TraceViewer dialog
	 * 
	 * @param name
	 *            name of the dialog
	 * @param shell
	 *            shell of the view
	 * @return the newly created dialog
	 */
	public TraceViewerDialog construct(Dialog name, Shell shell) {
		TraceViewerDialog dialog = null;

		// First check that the main shell exists and isn't disposed
		if (shell != null && !shell.isDisposed()) {
			DataProcessorAccess access = TraceViewerGlobals.getTraceViewer()
					.getDataProcessorAccess();

			// Create a dialog according to the name
			switch (name) {

			// Filter dialog
			case FILTER:
				dialog = new FilterDialog(shell,
						(TreeItemContentProvider) access.getFilterProcessor()
								.getTreeItemListener(), (TreeItem) access
								.getFilterProcessor().getRoot().getParent());
				break;

			// Advanced filter dialog
			case ADVANCEDFILTER:
				dialog = new FilterAdvancedDialog(shell,
						(TreeItemContentProvider) access.getFilterProcessor()
								.getTreeItemListener(), (TreeItem) access
								.getFilterProcessor().getRoot().getParent());
				break;

			// Search dialog
			case SEARCH:
				dialog = new SearchDialog(shell, TraceViewerGlobals
						.getTraceViewer().getView());
				break;

			// Color dialog
			case COLOR:
				dialog = new ColorDialog(shell,
						(TreeItemContentProvider) access.getColorer()
								.getTreeItemListener(), (TreeItem) access
								.getColorer().getRoot().getParent());
				break;

			// Count lines dialog
			case COUNTLINES:
				dialog = new LineCountDialog(shell,
						(TreeItemContentProvider) access
								.getLineCountProcessor().getTreeItemListener(),
						(TreeItem) access.getLineCountProcessor().getRoot()
								.getParent());
				break;

			// Variable tracing dialog
			case VARIABLETRACING:
				dialog = new VariableTracingDialog(shell,
						(TreeItemContentProvider) access
								.getVariableTracingProcessor()
								.getTreeItemListener(), (TreeItem) access
								.getVariableTracingProcessor().getRoot()
								.getParent());
				break;

			// Variable tracing history dialog
			case VARIBLETRACINGHISTORY:
				dialog = new VariableTracingHistoryDialog(shell);
				break;

			// Trace activation dialog
			case TRACEACTIVATION:
				dialog = new TraceActivationDialog(shell);
				break;

			// Trigger dialog
			case TRIGGER:
				dialog = new TriggerDialog(shell,
						(TreeItemContentProvider) access.getTriggerProcessor()
								.getTreeItemListener(), (TreeItem) access
								.getTriggerProcessor().getRoot().getParent());
				break;

			// Progressbar dialog
			case PROGRESSBAR:
				dialog = new ProgressBarDialog(shell);
				break;

			// Default, do nothing
			default:
				break;
			}

		}
		return dialog;
	}
}
