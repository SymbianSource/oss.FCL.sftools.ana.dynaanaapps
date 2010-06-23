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
 * LineCountProcessor DataProcessor
 *
 */
package com.nokia.traceviewer.engine.dataprocessor;

import java.util.ArrayList;
import java.util.List;

import com.nokia.traceviewer.TraceViewerPlugin;
import com.nokia.traceviewer.action.TraceViewerActionUtils;
import com.nokia.traceviewer.dialog.BasePropertyDialog;
import com.nokia.traceviewer.dialog.LineCountDialog;
import com.nokia.traceviewer.dialog.treeitem.LineCountTreeBaseItem;
import com.nokia.traceviewer.dialog.treeitem.LineCountTreeComponentItem;
import com.nokia.traceviewer.dialog.treeitem.LineCountTreeItem;
import com.nokia.traceviewer.dialog.treeitem.LineCountTreeTextItem;
import com.nokia.traceviewer.dialog.treeitem.TreeItem;
import com.nokia.traceviewer.dialog.treeitem.TreeItemContentProvider;
import com.nokia.traceviewer.dialog.treeitem.TreeItemListener;
import com.nokia.traceviewer.engine.TraceInformation;
import com.nokia.traceviewer.engine.TraceProperties;
import com.nokia.traceviewer.engine.TraceViewerGlobals;
import com.nokia.traceviewer.engine.TraceViewerPropertyViewInterface;
import com.nokia.traceviewer.engine.TraceViewerDialogInterface.Dialog;
import com.nokia.traceviewer.engine.preferences.PreferenceConstants;
import com.nokia.traceviewer.engine.preferences.XMLLineCountConfigurationImporter;

/**
 * LineCount DataProcessor
 * 
 */
public final class LineCountProcessor implements DataProcessor {

	/**
	 * Interval how often to update progressbar
	 */
	private static final int PROGRESSBAR_UPDATE_INTERVAL = 100;

	/**
	 * Line count dialog used in setting rules
	 */
	private LineCountDialog lineCountDialog;

	/**
	 * Content provider for the dialog
	 */
	private TreeItemContentProvider contentProvider;

	/**
	 * First visible object in the dialog tree
	 */
	private TreeItem root;

	/**
	 * Line count items array
	 */
	private final List<LineCountItem> lineCountItems;

	/**
	 * Text rules that are applied
	 */
	private final List<LineCountTreeTextItem> textRules;

	/**
	 * Component rules that are applied
	 */
	private final List<LineCountTreeComponentItem> componentRules;

	/**
	 * Constructor
	 */
	public LineCountProcessor() {
		// Create initial tree
		createInitialTree();

		// Create rule arrays
		lineCountItems = new ArrayList<LineCountItem>();
		textRules = new ArrayList<LineCountTreeTextItem>();
		componentRules = new ArrayList<LineCountTreeComponentItem>();

	}

	/**
	 * Creates initial tree
	 */
	public void createInitialTree() {
		contentProvider = new TreeItemContentProvider();
		// Create root node
		TreeItem treeRoot = new LineCountTreeBaseItem(contentProvider, null,
				"root", //$NON-NLS-1$
				LineCountTreeItem.Rule.GROUP);
		root = new LineCountTreeBaseItem(contentProvider, treeRoot,
				TraceViewerPlugin.getDefault().getPreferenceStore().getString(
						PreferenceConstants.CONFIGURATION_FILE),
				LineCountTreeItem.Rule.GROUP);
		treeRoot.addChild(root);
	}

	/**
	 * Imports line count rules from configuration file
	 */
	public void importLineCountRules() {
		// Import Line Count rules
		XMLLineCountConfigurationImporter importer = new XMLLineCountConfigurationImporter(
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
			if (!lineCountItems.isEmpty()) {
				boolean ruleHit = false;

				// Process component rules
				ruleHit = processComponentRules(properties);

				// Process text rules
				if (!ruleHit) {
					processTextRules(properties);
				}

				// Update progressBar if needed
				updateProgressBar();
			}
		}
	}

	/**
	 * Process component rules
	 * 
	 * @param properties
	 *            trace properties
	 * @return true if rule hits
	 */
	private boolean processComponentRules(TraceProperties properties) {
		boolean ruleHit = false;
		TraceInformation information = properties.information;

		// Information must be defined
		if (information != null && information.isDefined()) {

			// Loop through component rules
			int len = componentRules.size();
			LineCountTreeComponentItem rule;
			for (int i = 0; i < len; i++) {
				rule = componentRules.get(i);
				// Get component ID
				int compId = rule.getComponentId();

				// Component ID matches
				if (compId == BasePropertyDialog.WILDCARD_INTEGER
						|| compId == information.getComponentId()) {

					// Get group ID
					int groupId = rule.getGroupId();

					// Group ID matches
					if (groupId == BasePropertyDialog.WILDCARD_INTEGER
							|| groupId == information.getGroupId()) {

						updateLineCountItem(i);
						ruleHit = true;
						break;
					}
				}
			}
		}
		return ruleHit;
	}

	/**
	 * Process text rules
	 * 
	 * @param properties
	 *            trace properties
	 * @return true if rule hits
	 */
	private boolean processTextRules(TraceProperties properties) {
		boolean ruleHit = false;

		// Loop through text rules
		int len = textRules.size();
		LineCountTreeTextItem rule;
		for (int i = 0; i < len; i++) {
			rule = textRules.get(i);
			String traceLine = ""; //$NON-NLS-1$

			// Traces missing
			if (properties.bTraceInformation.isTraceMissing()) {
				traceLine = TraceViewerActionUtils.TRACES_DROPPED_MSG;
			}
			if (properties.traceString != null) {
				traceLine += properties.traceString;
			}

			String ruleStr = rule.getTextToCompare();
			if (ruleStr == null) {
				break;
			}
			if (!rule.isMatchCase()) {
				traceLine = traceLine.toLowerCase();
			}

			// Line hits
			if (traceLine.contains(ruleStr)) {
				// Get offset of this text rule in lineCountItems list by adding
				// i to the number of component rules existing
				updateLineCountItem(componentRules.size() + i);
				ruleHit = true;
			}
		}
		return ruleHit;
	}

	/**
	 * Updates Line Count Item
	 * 
	 * @param offset
	 *            offset of item to update
	 */
	private void updateLineCountItem(int offset) {
		LineCountItem item = lineCountItems.get(offset);
		int count = item.getCount();
		item.setCount(count + 1);
		item.setChanged(true);

		TraceViewerPropertyViewInterface view = TraceViewerGlobals
				.getTraceViewer().getPropertyView();
		if (view != null) {
			view.setLineCountTableChanged();
		}
	}

	/**
	 * Update progressBar if needed
	 */
	private void updateProgressBar() {
		// Update possible progressBar
		if (isProcessingCounting()
				&& TraceViewerGlobals.getTraceViewer().getDataReaderAccess()
						.getCurrentDataReader().getTraceCount()
						% PROGRESSBAR_UPDATE_INTERVAL == 0) {
			lineCountDialog.getProgressBar().updateProgressBar(
					TraceViewerGlobals.getTraceViewer().getDataReaderAccess()
							.getCurrentDataReader().getTraceCount());

		}
	}

	/**
	 * Tells if initial counting is in process
	 * 
	 * @return true if initial counting is in progress
	 */
	public boolean isProcessingCounting() {
		return (lineCountDialog != null
				&& lineCountDialog.getProgressBar() != null
				&& lineCountDialog.getProgressBar().getShell() != null && !lineCountDialog
				.getProgressBar().getShell().isDisposed());
	}

	/**
	 * Gets lineCount dialog
	 * 
	 * @return filter dialog
	 */
	public LineCountDialog getLineCountDialog() {
		if (lineCountDialog == null) {
			lineCountDialog = (LineCountDialog) TraceViewerGlobals
					.getTraceViewer().getDialogs().createDialog(
							Dialog.COUNTLINES);
		}
		return lineCountDialog;
	}

	/**
	 * Gets root of the filter tree
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
	 * Gets lineCountItems
	 * 
	 * @return the lineCountItems
	 */
	public List<LineCountItem> getLineCountItems() {
		return lineCountItems;
	}

	/**
	 * Gets text rules
	 * 
	 * @return text rules
	 */
	public List<LineCountTreeTextItem> getTextRules() {
		return textRules;
	}

	/**
	 * Gets component rules
	 * 
	 * @return component rules
	 */
	public List<LineCountTreeComponentItem> getComponentRules() {
		return componentRules;
	}

	/**
	 * Empty lineCountItems
	 */
	public void emptyLineCountItems() {
		for (int i = 0; i < lineCountItems.size(); i++) {
			lineCountItems.get(i).setCount(0);
			lineCountItems.get(i).setChanged(true);
		}
	}

	/**
	 * Enable linecount rule
	 * 
	 * @param item
	 *            the rule item
	 */
	public void enableRule(LineCountTreeItem item) {
		LineCountItem newItem = new LineCountItem(item.getName(), 0);
		if (item instanceof LineCountTreeTextItem) {
			textRules.add((LineCountTreeTextItem) item);
			lineCountItems.add(newItem);

			// Add component item to the beginning of the list
		} else if (item instanceof LineCountTreeComponentItem) {
			componentRules.add((LineCountTreeComponentItem) item);
			lineCountItems.add(0, newItem);
		}
		if (TraceViewerGlobals.getTraceViewer().getPropertyView() != null) {
			TraceViewerGlobals.getTraceViewer().getPropertyView()
					.createNewPropertyTableItems();
		}
	}
}