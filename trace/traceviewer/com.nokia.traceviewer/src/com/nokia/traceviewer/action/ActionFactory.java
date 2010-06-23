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
 * ActionFactory class
 *
 */
package com.nokia.traceviewer.action;

import java.net.URL;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IWorkbenchActionConstants;

import com.nokia.traceviewer.TraceViewerPlugin;
import com.nokia.traceviewer.engine.TraceViewerGlobals;
import com.nokia.traceviewer.engine.dataprocessor.Logger;

/**
 * ActionFactory
 * 
 */
public final class ActionFactory {

	/**
	 * Clear view action
	 */
	private Action clearViewAction;

	/**
	 * Color action
	 */
	private Action colorAction;

	/**
	 * Connect action
	 */
	private Action connectAction;

	/**
	 * Count lines action
	 */
	private Action countLinesAction;

	/**
	 * Filter action
	 */
	private Action filterAction;

	/**
	 * Close ASCII log action
	 */
	private Action logCloseAsciiAction;

	/**
	 * Close Binary log action
	 */
	private Action logCloseBinaryAction;

	/**
	 * New ASCII log action
	 */
	private Action logNewAsciiAction;

	/**
	 * New Binary log action
	 */
	private Action logNewBinaryAction;

	/**
	 * Open log action
	 */
	private Action logOpenLogAction;

	/**
	 * Save Binary log action
	 */
	private Action logSaveBinaryAction;

	/**
	 * Save Ascii log action
	 */
	private Action logSaveAsciiAction;

	/**
	 * Open Decode file action
	 */
	private Action openDecodeFileAction;

	/**
	 * Append decode file action
	 */
	private Action appendDecodeFileAction;

	/**
	 * Reload decode files actions
	 */
	private Action reloadDecodeFilesAction;

	/**
	 * Pause action
	 */
	private Action pauseAction;

	/**
	 * Search action
	 */
	private Action searchAction;

	/**
	 * Trace Activation action
	 */
	private Action traceActivationAction;

	/**
	 * Trace Variables action
	 */
	private Action traceVariablesAction;

	/**
	 * Trigger action
	 */
	private Action triggerAction;

	/**
	 * Show trace info action
	 */
	private Action showTraceInfoAction;

	/**
	 * Open trace location action
	 */
	private Action openTraceLocationAction;

	/**
	 * Copy selection action
	 */
	private Action copySelectionAction;

	/**
	 * Open variable tracing history dialog
	 */
	private Action openVariableTracingHistoryAction;

	/**
	 * Start external filter program action
	 */
	private Action startExternalFilterAction;

	/**
	 * Select all action
	 */
	private Action selectAllAction;

	/**
	 * Open Connection Settings action
	 */
	private Action openConnectionSettingsAction;

	/**
	 * Close and restart logging action
	 */
	private Action closeAndRestartLoggingAction;

	/**
	 * Toolbar manager
	 */
	private IToolBarManager manager;

	/**
	 * List of normal items in toolbar
	 */
	private IContributionItem[] normalToolbarItems;

	/**
	 * Image for the log submenu
	 */
	private static ImageDescriptor logSubMenuImage;

	static {
		URL url = TraceViewerPlugin.getDefault().getBundle().getEntry(
				"/icons/log.gif"); //$NON-NLS-1$
		logSubMenuImage = ImageDescriptor.createFromURL(url);
	}

	/**
	 * Constructor
	 * 
	 */
	public ActionFactory() {
		createActions();
	}

	/**
	 * Creates all the action items
	 */
	private void createActions() {

		// Toolbar actions
		reloadDecodeFilesAction = new ReloadDecodeFilesAction();
		connectAction = new ConnectAction();
		pauseAction = new PauseAction();
		filterAction = new FilterAction();
		startExternalFilterAction = new StartExternalFilterAction();
		searchAction = new SearchAction();
		colorAction = new ColorAction();
		countLinesAction = new CountLinesAction();
		traceVariablesAction = new TraceVariablesAction();
		clearViewAction = new ClearViewAction();

		// Menubar actions
		traceActivationAction = new TraceActivationAction();
		triggerAction = new TriggerAction();
		openConnectionSettingsAction = new OpenConnectionSettingsAction();

		// Log actions are in own submenu
		logCloseAsciiAction = new LogCloseAsciiAction();
		logCloseBinaryAction = new LogCloseBinaryAction();
		logNewAsciiAction = new LogNewAsciiAction();
		logNewBinaryAction = new LogNewBinaryAction();
		logOpenLogAction = new LogOpenLogAction();
		logSaveBinaryAction = new LogSaveBinaryAction();
		logSaveAsciiAction = new LogSaveAsciiAction();

		// Other, non-visible, actions
		openDecodeFileAction = new OpenDecodeFileAction();
		showTraceInfoAction = new ShowTraceInfoAction();
		openTraceLocationAction = new OpenTraceLocationAction();
		appendDecodeFileAction = new AppendDecodeFileAction();
		openVariableTracingHistoryAction = new OpenVariableTracingHistoryAction();
		closeAndRestartLoggingAction = new CloseAndRestartLoggingAction();

		// System action overrides
		copySelectionAction = new CopySelectionAction();
		selectAllAction = new SelectAllAction();
	}

	/**
	 * Fills the view menu with the actions
	 * 
	 * @param manager
	 *            the menu manager
	 */
	public void fillMenu(IMenuManager manager) {
		manager.add(traceActivationAction);
		manager.add(new Separator());
		IMenuManager subMgr = new MenuManager(
				Messages.getString("ActionFactory.LogSubMenuTitle"), logSubMenuImage, null); //$NON-NLS-1$
		subMgr.setRemoveAllWhenShown(true);
		subMgr.setVisible(true);
		subMgr.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager mgr) {
				mgr.add(logOpenLogAction);
				mgr.add(new Separator());
				mgr.add(logNewBinaryAction);
				mgr.add(logSaveBinaryAction);
				mgr.add(logCloseBinaryAction);

				mgr.add(new Separator());
				mgr.add(logNewAsciiAction);
				mgr.add(logSaveAsciiAction);
				mgr.add(logCloseAsciiAction);

				// Get the logger DataProcessor
				Logger logger = TraceViewerGlobals.getTraceViewer()
						.getDataProcessorAccess().getLogger();

				boolean asciiLogging = logger.isPlainLogging();
				boolean binaryLogging = logger.isBinLogging();
				boolean logFileOpened = logger.isLogFileOpened();

				// Check if Binary logging is on
				logNewBinaryAction.setEnabled(!binaryLogging);
				logCloseBinaryAction.setEnabled(binaryLogging);

				// Check if ASCII logging is on
				logNewAsciiAction.setEnabled(!asciiLogging);
				logCloseAsciiAction.setEnabled(asciiLogging);

				logOpenLogAction.setEnabled(!asciiLogging && !binaryLogging);

				// Check if log file is opened
				if (logFileOpened) {
					logNewBinaryAction.setEnabled(false);
					logCloseBinaryAction.setEnabled(false);
					logNewAsciiAction.setEnabled(false);
					logCloseAsciiAction.setEnabled(false);
				}

			}
		});

		manager.add(subMgr);
		manager.add(new Separator());
		manager.add(triggerAction);
		manager.add(new Separator());
		manager.add(openConnectionSettingsAction);
		manager.add(new Separator());
		manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
	}

	/**
	 * Fills the toolbar
	 * 
	 * @param manager
	 *            the toolbar manager
	 */
	public void fillToolBar(IToolBarManager manager) {
		this.manager = manager;
		manager.add(reloadDecodeFilesAction);
		manager.add(connectAction);
		manager.add(new Separator());
		manager.add(pauseAction);
		manager.add(filterAction);
		manager.add(startExternalFilterAction);
		manager.add(searchAction);
		manager.add(colorAction);
		manager.add(new Separator());
		manager.add(countLinesAction);
		manager.add(traceVariablesAction);
		manager.add(new Separator());
		manager.add(clearViewAction);
		manager.add(new Separator());
		manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
	}

	/**
	 * Adds close and restart logging button
	 * 
	 * @param filePath
	 *            file path of the log file
	 */
	public void addCloseAndRestartLoggingButton(String filePath) {
		((CloseAndRestartLoggingAction) closeAndRestartLoggingAction)
				.changeToStopLoggingAction();

		if (normalToolbarItems == null) {
			normalToolbarItems = manager.getItems();
			manager.removeAll();

			// First add reload Dictionary files action
			manager.add(reloadDecodeFilesAction);

			((CloseAndRestartLoggingAction) closeAndRestartLoggingAction)
					.setFilePath(filePath);
			manager.add(closeAndRestartLoggingAction);

			// Add old items back. Start from index 1 because in index 0 there
			// is reload Dictionary file action which is already added to the
			// manager
			for (int i = 1; i < normalToolbarItems.length; i++) {
				manager.add(normalToolbarItems[i]);
			}

			manager.update(true);
		}
	}

	/**
	 * Removes close and restart logging button
	 */
	public void removeCloseAndRestartLoggingButton() {
		if (normalToolbarItems != null) {
			manager.removeAll();

			// Add old items back
			for (int i = 0; i < normalToolbarItems.length; i++) {
				manager.add(normalToolbarItems[i]);
			}
			normalToolbarItems = null;

			manager.update(true);
		}
	}

	/**
	 * Gets connect action
	 * 
	 * @return connect action
	 */
	public Action getConnectAction() {
		return connectAction;
	}

	/**
	 * Gets pause action
	 * 
	 * @return pause action
	 */
	public Action getPauseAction() {
		return pauseAction;
	}

	/**
	 * Gets search action
	 * 
	 * @return search action
	 */
	public Action getSearchAction() {
		return searchAction;
	}

	/**
	 * Gets show trace info action
	 * 
	 * @return show trace info action
	 */
	public Action getShowTraceInfoAction() {
		return showTraceInfoAction;
	}

	/**
	 * Gets open trace location action
	 * 
	 * @return open trace location action
	 */
	public Action getOpenTraceLocationAction() {
		return openTraceLocationAction;
	}

	/**
	 * Gets trace activation action
	 * 
	 * @return trace activation action
	 */
	public Action getTraceActivationAction() {
		return traceActivationAction;
	}

	/**
	 * Gets coloring action
	 * 
	 * @return coloring action
	 */
	public Action getColorAction() {
		return colorAction;
	}

	/**
	 * Gets open decode file action
	 * 
	 * @return open decode file action
	 */
	public Action getOpenDecodeFileAction() {
		return openDecodeFileAction;
	}

	/**
	 * Gets append decode file action
	 * 
	 * @return append decode file action
	 */
	public Action getAppendDecodeFileAction() {
		return appendDecodeFileAction;
	}

	/**
	 * Gets copy selection action
	 * 
	 * @return copy selection action
	 */
	public Action getCopySelectionAction() {
		return copySelectionAction;
	}

	/**
	 * Gets select all action
	 * 
	 * @return select all action
	 */
	public Action getSelectAllAction() {
		return selectAllAction;
	}

	/**
	 * Gets open variableTracing details action
	 * 
	 * @return the open variable tracing details action
	 */
	public Action getOpenVariableTracingHistoryAction() {
		return openVariableTracingHistoryAction;
	}

	/**
	 * Gets reload decode files action
	 * 
	 * @return the reload decode files action
	 */
	public Action getReloadDecodeFilesAction() {
		return reloadDecodeFilesAction;
	}

	/**
	 * Gets open log action
	 * 
	 * @return open log action
	 */
	public Action getLogOpenLogAction() {
		return logOpenLogAction;
	}

	/**
	 * Gets close ascii log action
	 * 
	 * @return close ascii log action
	 */
	public Action getLogCloseAsciiAction() {
		return logCloseAsciiAction;
	}

	/**
	 * Gets close binary log action
	 * 
	 * @return close binary log action
	 */
	public Action getLogCloseBinaryAction() {
		return logCloseBinaryAction;
	}

	/**
	 * Gets new ascii log action
	 * 
	 * @return new ascii log action
	 */
	public Action getLogNewAsciiAction() {
		return logNewAsciiAction;
	}

	/**
	 * Gets new binary log action
	 * 
	 * @return new binary log action
	 */
	public Action getLogNewBinaryAction() {
		return logNewBinaryAction;
	}

	/**
	 * Gets save binary log action
	 * 
	 * @return save binary log action
	 */
	public Action getLogSaveBinaryAction() {
		return logSaveBinaryAction;
	}

	/**
	 * Gets save ascii log action
	 * 
	 * @return save ascii log action
	 */
	public Action getLogSaveAsciiAction() {
		return logSaveAsciiAction;
	}

	/**
	 * Gets trigger action
	 * 
	 * @return trigger action
	 */
	public Action getTriggerAction() {
		return triggerAction;
	}
}
