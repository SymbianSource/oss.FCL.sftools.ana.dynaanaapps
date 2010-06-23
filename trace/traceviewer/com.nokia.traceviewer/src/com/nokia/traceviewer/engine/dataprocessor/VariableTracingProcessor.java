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
 * VariableTracingProcessor DataProcessor
 *
 */
package com.nokia.traceviewer.engine.dataprocessor;

import java.util.ArrayList;
import java.util.List;

import com.nokia.traceviewer.TraceViewerPlugin;
import com.nokia.traceviewer.action.TraceViewerActionUtils;
import com.nokia.traceviewer.dialog.VariableTracingDialog;
import com.nokia.traceviewer.dialog.treeitem.TreeItem;
import com.nokia.traceviewer.dialog.treeitem.TreeItemContentProvider;
import com.nokia.traceviewer.dialog.treeitem.TreeItemListener;
import com.nokia.traceviewer.dialog.treeitem.VariableTracingTreeBaseItem;
import com.nokia.traceviewer.dialog.treeitem.VariableTracingTreeItem;
import com.nokia.traceviewer.dialog.treeitem.VariableTracingTreeTextItem;
import com.nokia.traceviewer.engine.TraceProperties;
import com.nokia.traceviewer.engine.TraceViewerGlobals;
import com.nokia.traceviewer.engine.TraceViewerPropertyViewInterface;
import com.nokia.traceviewer.engine.TraceViewerDialogInterface.Dialog;
import com.nokia.traceviewer.engine.preferences.PreferenceConstants;
import com.nokia.traceviewer.engine.preferences.XMLVariableTracingConfigurationImporter;

/**
 * VariableTracing DataProcessor
 * 
 */
public final class VariableTracingProcessor implements DataProcessor {

	/**
	 * Interval how often to update progressbar
	 */
	private static final int PROGRESSBAR_UPDATE_INTERVAL = 100;

	/**
	 * Variable Tracing dialog used in setting rules
	 */
	private VariableTracingDialog variableTracingDialog;

	/**
	 * Content provider for the dialog
	 */
	private TreeItemContentProvider contentProvider;

	/**
	 * First visible object in the dialog tree
	 */
	private TreeItem root;

	/**
	 * List of variable tracing items
	 */
	private final List<VariableTracingItem> variableTracingItems;

	/**
	 * Text rules that are applied
	 */
	private final List<VariableTracingTreeTextItem> textRules;

	/**
	 * Constructor
	 */
	public VariableTracingProcessor() {
		createInitialTree();
		variableTracingItems = new ArrayList<VariableTracingItem>();
		textRules = new ArrayList<VariableTracingTreeTextItem>();

	}

	/**
	 * Creates initial tree
	 */
	public void createInitialTree() {
		contentProvider = new TreeItemContentProvider();
		// Create root node
		TreeItem treeRoot = new VariableTracingTreeBaseItem(contentProvider,
				null, "root", VariableTracingTreeItem.Rule.GROUP); //$NON-NLS-1$
		root = new VariableTracingTreeBaseItem(contentProvider, treeRoot,
				TraceViewerPlugin.getDefault().getPreferenceStore().getString(
						PreferenceConstants.CONFIGURATION_FILE),
				VariableTracingTreeItem.Rule.GROUP);
		treeRoot.addChild(root);
	}

	/**
	 * Imports line count rules from configuration file
	 */
	public void importVariableTracingRules() {
		// Import rules
		XMLVariableTracingConfigurationImporter importer = new XMLVariableTracingConfigurationImporter(
				root, TraceViewerPlugin.getDefault().getPreferenceStore()
						.getString(PreferenceConstants.CONFIGURATION_FILE),
				true);
		importer.importData();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.nokia.traceviewer.engine.DataProcessor#processData(com.nokia.traceviewer
	 * .engine.TraceProperties)
	 */
	public void processData(TraceProperties properties) {
		if (!properties.traceConfiguration.isScrolledTrace()
				&& !properties.traceConfiguration.isFilteredOut()
				&& !properties.traceConfiguration.isTriggeredOut()) {
			int len = variableTracingItems.size();
			if (len > 0) {

				// Loop through variable tracing items
				VariableTracingItem item;
				for (int i = 0; i < len; i++) {
					item = variableTracingItems.get(i);

					String traceLine = ""; //$NON-NLS-1$

					// Traces missing
					if (properties.bTraceInformation.isTraceMissing()) {
						traceLine = TraceViewerActionUtils.TRACES_DROPPED_MSG;
					}
					if (properties.traceString != null) {
						traceLine += properties.traceString;
					}

					String rule = item.getTextToCompare();
					if (rule == null) {
						break;
					}

					String checkString = traceLine;
					if (!item.isMatchCase()) {
						checkString = checkString.toLowerCase();
					}

					// Line hits
					if (checkString.contains(rule)) {
						VariableTracingEvent event = new VariableTracingEvent(
								item,
								properties.timestampString,
								traceLine.substring(checkString.indexOf(rule)
										+ rule.length()),
								TraceViewerGlobals.getTraceViewer()
										.getDataReaderAccess()
										.getCurrentDataReader().getTraceCount(),
								properties.information);
						item.addEvent(event);
						item.setChanged(true);
						TraceViewerPropertyViewInterface view = TraceViewerGlobals
								.getTraceViewer().getPropertyView();
						if (view != null) {
							view.setVariableTracingTableChanged();
						}
					}
				}

				// Update progressbar if needed
				updateProgressBar();
			}
		}
	}

	/**
	 * Update progressbar if needed
	 */
	private void updateProgressBar() {
		// Update possible progressBar
		if (isProcessingTracing()
				&& TraceViewerGlobals.getTraceViewer().getDataReaderAccess()
						.getCurrentDataReader().getTraceCount()
						% PROGRESSBAR_UPDATE_INTERVAL == 0) {

			variableTracingDialog.getProgressBar().updateProgressBar(
					TraceViewerGlobals.getTraceViewer().getDataReaderAccess()
							.getCurrentDataReader().getTraceCount());
		}
	}

	/**
	 * Tells if initial tracing is in process
	 * 
	 * @return true if initial tracing is in progress
	 */
	public boolean isProcessingTracing() {
		return (variableTracingDialog != null
				&& variableTracingDialog.getProgressBar() != null
				&& variableTracingDialog.getProgressBar().getShell() != null && !variableTracingDialog
				.getProgressBar().getShell().isDisposed());
	}

	/**
	 * Gets variable tracing dialog
	 * 
	 * @return variable tracing dialog
	 */
	public VariableTracingDialog getVariableTracingDialog() {
		if (variableTracingDialog == null) {
			variableTracingDialog = (VariableTracingDialog) TraceViewerGlobals
					.getTraceViewer().getDialogs().createDialog(
							Dialog.VARIABLETRACING);
		}
		return variableTracingDialog;
	}

	/**
	 * Gets root of the tree
	 * 
	 * @return root
	 */
	public TreeItem getRoot() {
		return root;
	}

	/**
	 * Gets item listener
	 * 
	 * @return the contentProvider
	 */
	public TreeItemListener getTreeItemListener() {
		return contentProvider;
	}

	/**
	 * Gets VariableTracingItems
	 * 
	 * @return the VariableTracingItems
	 */
	public List<VariableTracingItem> getVariableTracingItems() {
		return variableTracingItems;
	}

	/**
	 * Empty variablesTracingItems
	 */
	public void emptyVariableTracingItems() {
		for (int i = 0; i < variableTracingItems.size(); i++) {
			variableTracingItems.get(i).clear();
			variableTracingItems.get(i).setChanged(true);
		}
	}

	/**
	 * Gets text rules
	 * 
	 * @return text rules
	 */
	public List<VariableTracingTreeTextItem> getTextRules() {
		return textRules;
	}

	/**
	 * Enable variabletracing rule
	 * 
	 * @param item
	 *            the rule item
	 */
	public void enableRule(VariableTracingTreeItem item) {
		if (item instanceof VariableTracingTreeTextItem) {
			VariableTracingTreeTextItem item2 = (VariableTracingTreeTextItem) item;
			VariableTracingItem newItem = new VariableTracingItem(item2
					.getName(), item2.getText(), item2.isMatchCase(), item2
					.getHistoryCount());
			textRules.add((VariableTracingTreeTextItem) item);
			variableTracingItems.add(newItem);
		}

		if (TraceViewerGlobals.getTraceViewer().getPropertyView() != null) {
			TraceViewerGlobals.getTraceViewer().getPropertyView()
					.createNewPropertyTableItems();
		}
	}
}