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
 * Handles surviving from manual closing of progressbar
 *
 */
package com.nokia.traceviewer.dialog;

import org.eclipse.swt.widgets.Shell;

import com.nokia.traceviewer.action.LogSaveAsciiAction;
import com.nokia.traceviewer.action.LogSaveBinaryAction;
import com.nokia.traceviewer.action.PauseAction;
import com.nokia.traceviewer.engine.TraceViewerGlobals;
import com.nokia.traceviewer.engine.dataprocessor.FilterProcessor;

/**
 * Handles surviving from manual closing of progressbar
 * 
 */
public final class ProgressBarCloseHandler {

	/**
	 * Waiting time for dataprocessors to stop
	 */
	private static final int WAITING_TIME = 1500;

	/**
	 * Progressbar closed, handle it
	 * 
	 * @param maxLines
	 *            max lines in progressbar
	 * @return true if progressbar should be closed, false otherwise
	 */
	public boolean progressBarClosed(int maxLines) {
		boolean closeDialog = true;

		// Closing from Filtering
		if (TraceViewerGlobals.getTraceViewer().getDataProcessorAccess()
				.getFilterProcessor().isProcessingFilter()) {
			closeDialog = closeFiltering(closeDialog, maxLines);

			// Closing from Counting or Variable Tracing
		} else if (TraceViewerGlobals.getTraceViewer().getDataProcessorAccess()
				.getLineCountProcessor().isProcessingCounting()
				|| TraceViewerGlobals.getTraceViewer().getDataProcessorAccess()
						.getVariableTracingProcessor().isProcessingTracing()) {
			closeDialog = closeCountingOrVariableTracing(closeDialog);

			// Closing when saving current traces to a log file
		} else if (((LogSaveBinaryAction) TraceViewerGlobals.getTraceViewer()
				.getView().getActionFactory().getLogSaveBinaryAction())
				.isSavingFile()) {
			closeDialog = closeSavingLog();

		} else if (((LogSaveAsciiAction) TraceViewerGlobals.getTraceViewer()
				.getView().getActionFactory().getLogSaveAsciiAction())
				.isSavingFile()) {
			closeDialog = closeSavingLog();
		}
		return closeDialog;
	}

	/**
	 * Closes filtering
	 * 
	 * @param closeDialog
	 *            boolean indicating are we closing the progressbar
	 * @param maxLines
	 *            maximum lines in progressbar
	 * @return true if we are closing the progressbar
	 */
	private boolean closeFiltering(boolean closeDialog, int maxLines) {
		FilterProcessor proc = TraceViewerGlobals.getTraceViewer()
				.getDataProcessorAccess().getFilterProcessor();
		if (proc.hasRules() || proc.isUsingExternalFilter()) {

			// Get close confirmation from the user
			String closeConfirmation = Messages
					.getString("ProgressBarCloseHandler.CloseFilterConfirmation"); //$NON-NLS-1$
			boolean close = TraceViewerGlobals.getTraceViewer().getDialogs()
					.showConfirmationDialog(closeConfirmation);

			Shell progressShell = TraceViewerGlobals.getTraceViewer()
					.getDataProcessorAccess().getFilterProcessor()
					.getFilterDialog().getProgressBar().getShell();

			// Remove filtering if clicked OK and progressBar shell is still
			// open
			if (close && progressShell != null && !progressShell.isDisposed()) {
				TraceViewerGlobals.getTraceViewer().getDataReaderAccess()
						.getMainDataReader().pause(true);
				TraceViewerGlobals.getTraceViewer().getDataReaderAccess()
						.getCurrentDataReader().shutdown();
				// Clear filters
				proc.getFilterRules().getFilterRules().clear();

				// Wait so dataProcessors have time to stop
				try {
					Thread.sleep(WAITING_TIME);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}

				// Shut down possible external data processor
				if (proc.isUsingExternalFilter()) {
					proc.getExternalFilterProcessor().stopExternalApplication();
					// Remove normal filters
				} else {
					proc.getFilterDialog().removeFilters(maxLines);
				}

				PauseAction action = (PauseAction) TraceViewerGlobals
						.getTraceViewer().getView().getActionFactory()
						.getPauseAction();
				action.setPaused(false);
			} else {
				closeDialog = false;
			}
		}

		return closeDialog;
	}

	/**
	 * Closes line counting or variable tracing
	 * 
	 * @param closeDialog
	 *            boolean indicating are we closing the progressbar
	 * @return true if we are closing the progressbar
	 */
	private boolean closeCountingOrVariableTracing(boolean closeDialog) {
		return closeDialog;
	}

	/**
	 * Closes when saving log
	 * 
	 * @return true if we are closing the progressbar
	 */
	private boolean closeSavingLog() {

		// Get close confirmation from the user
		String closeConfirmation = Messages
				.getString("ProgressBarCloseHandler.CloseSavingConfirmation"); //$NON-NLS-1$
		boolean close = TraceViewerGlobals.getTraceViewer().getDialogs()
				.showConfirmationDialog(closeConfirmation);

		return close;
	}
}
