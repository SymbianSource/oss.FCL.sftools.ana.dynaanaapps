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
 * Colorer DataProcessor
 *
 */
package com.nokia.traceviewer.engine.dataprocessor;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.text.IDocument;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Color;

import com.nokia.traceviewer.TraceViewerPlugin;
import com.nokia.traceviewer.dialog.BasePropertyDialog;
import com.nokia.traceviewer.dialog.ColorDialog;
import com.nokia.traceviewer.dialog.treeitem.ColorTreeBaseItem;
import com.nokia.traceviewer.dialog.treeitem.ColorTreeComponentItem;
import com.nokia.traceviewer.dialog.treeitem.ColorTreeItem;
import com.nokia.traceviewer.dialog.treeitem.ColorTreeTextItem;
import com.nokia.traceviewer.dialog.treeitem.TreeItemContentProvider;
import com.nokia.traceviewer.dialog.treeitem.TreeItemListener;
import com.nokia.traceviewer.engine.TraceInformation;
import com.nokia.traceviewer.engine.TraceProperties;
import com.nokia.traceviewer.engine.TraceViewerGlobals;
import com.nokia.traceviewer.engine.TraceViewerDialogInterface.Dialog;
import com.nokia.traceviewer.engine.preferences.PreferenceConstants;
import com.nokia.traceviewer.engine.preferences.XMLColorConfigurationImporter;

/**
 * Colorer DataProcessor
 * 
 */
public final class ColorProcessor {

	/**
	 * Color dialog used in setting color rules
	 */
	private ColorDialog colorDialog;

	/**
	 * Content provider for the filter dialog
	 */
	private TreeItemContentProvider contentProvider;

	/**
	 * First visible object in the filter dialog tree
	 */
	private ColorTreeItem root;

	/**
	 * Text rules that are applied
	 */
	private final List<ColorTreeTextItem> textRules;

	/**
	 * Component rules that are applied
	 */
	private final List<ColorTreeComponentItem> componentRules;

	/**
	 * Style ranges
	 */
	private final List<StyleRange> ranges;

	/**
	 * Constructor
	 */
	public ColorProcessor() {
		// Create initial tree
		createInitialColorTree();

		// Create rule arrays
		textRules = new ArrayList<ColorTreeTextItem>();
		componentRules = new ArrayList<ColorTreeComponentItem>();
		ranges = new ArrayList<StyleRange>();
	}

	/**
	 * Creates initial color tree
	 */
	public void createInitialColorTree() {
		contentProvider = new TreeItemContentProvider();

		// Create root node
		ColorTreeItem treeRoot = new ColorTreeBaseItem(contentProvider, null,
				"root", //$NON-NLS-1$
				ColorTreeItem.Rule.GROUP, null, null);
		root = new ColorTreeBaseItem(contentProvider, treeRoot,
				TraceViewerPlugin.getDefault().getPreferenceStore().getString(
						PreferenceConstants.CONFIGURATION_FILE),
				ColorTreeItem.Rule.GROUP, null, null);
		treeRoot.addChild(root);
	}

	/**
	 * Imports color rules from configuration file
	 */
	public void importColorRules() {
		XMLColorConfigurationImporter importer = new XMLColorConfigurationImporter(
				root, TraceViewerPlugin.getDefault().getPreferenceStore()
						.getString(PreferenceConstants.CONFIGURATION_FILE),
				true);
		importer.importData();
	}

	/**
	 * Create color rules
	 */
	public void createColorRules() {
		if (isColoring()) {

			// Get trace information ready if there for possible component rules
			ArrayList<TraceInformation> information = null;
			if (!componentRules.isEmpty()) {

				// Get properties from the traces in the view.
				int showingTracesFrom = TraceViewerGlobals.getTraceViewer()
						.getView().getShowingTracesFrom();
				int widgetLineCount = TraceViewerGlobals.getTraceViewer()
						.getView().getViewer().getTextWidget().getLineCount();
				int start = showingTracesFrom;

				// -2 comes from empty line in the end of widget and because
				// traces start from offset 0, not 1
				int end = showingTracesFrom + widgetLineCount - 2;
				List<TraceProperties> traces = TraceViewerGlobals
						.getTraceViewer().getTraces(start, end);

				// Get the informations array
				if (traces != null) {
					information = new ArrayList<TraceInformation>(traces.size());
					for (int i = 0; i < traces.size(); i++) {
						information.add(traces.get(i).information);
					}
				}
			}

			IDocument document = TraceViewerGlobals.getTraceViewer().getView()
					.getViewer().getDocument();
			StyledText widget = TraceViewerGlobals.getTraceViewer().getView()
					.getViewer().getTextWidget();
			int lineCount = widget.getLineCount() - 1;

			// Loop through all lines
			for (int i = 0; i < lineCount; i++) {
				try {
					int lineStart = document.getLineOffset(i);
					int lineLength = document.getLineLength(i);
					String line = document.get(lineStart, lineLength);

					boolean ruleAdded = false;

					// Loop through all component / group rules if there are any
					if (information != null) {
						ruleAdded = processComponentRules(lineStart,
								lineLength, information.get(i));
					}

					// Loop through all text rules
					if (!ruleAdded) {
						ruleAdded = processTextRules(lineStart, lineLength,
								line);
					}

				} catch (Throwable t) {
				}
			}
		} else {
			clearRanges();
		}

		// Apply made rules
		TraceViewerGlobals.getTraceViewer().getView().applyColorRules(
				ranges.toArray(new StyleRange[ranges.size()]));

		// Clear all rules
		clearRanges();
	}

	/**
	 * Processes component rules
	 * 
	 * @param lineStart
	 *            line start offset
	 * @param lineLength
	 *            line length
	 * @param information
	 *            trace information
	 * @return true if rule was added to this line
	 */
	private boolean processComponentRules(int lineStart, int lineLength,
			TraceInformation information) {
		boolean ruleAdded = false;

		// Information must be defined
		if (information != null && information.isDefined()) {

			// Loop through the component rules
			int len = componentRules.size();
			ColorTreeComponentItem rule;
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

						addRange(lineStart, lineLength, rule
								.getForegroundColor(), rule
								.getBackgroundColor());
						ruleAdded = true;
						break;
					}
				}
			}
		}

		return ruleAdded;
	}

	/**
	 * Processes text rules
	 * 
	 * @param lineStart
	 *            line start offset
	 * @param lineLength
	 *            line length
	 * @param line
	 *            line string
	 * @return true if rule was added to this line
	 */
	private boolean processTextRules(int lineStart, int lineLength, String line) {
		boolean ruleAdded = false;

		// Get the rules and loop through them
		int len = textRules.size();
		ColorTreeTextItem rule;
		for (int i = 0; i < len; i++) {
			rule = textRules.get(i);
			String colorRule = rule.getTextToCompare();

			// Case insensitive
			if (!rule.isMatchCase()) {
				line = line.toLowerCase();
			}

			if (line.contains(colorRule)) {
				addRange(lineStart, lineLength, rule.getForegroundColor(), rule
						.getBackgroundColor());
				ruleAdded = true;
				break;
			}
		}
		return ruleAdded;
	}

	/**
	 * Gets color dialog
	 * 
	 * @return color dialog
	 */
	public ColorDialog getColorDialog() {
		if (colorDialog == null) {
			colorDialog = (ColorDialog) TraceViewerGlobals.getTraceViewer()
					.getDialogs().createDialog(Dialog.COLOR);
		}
		return colorDialog;
	}

	/**
	 * Gets style ranges
	 * 
	 * @return style ranges
	 */
	public List<StyleRange> getRanges() {
		return ranges;
	}

	/**
	 * Clears style ranges
	 */
	public void clearRanges() {
		ranges.clear();
	}

	/**
	 * Add new style range
	 * 
	 * @param lineStart
	 *            offset of line start
	 * @param lineLength
	 *            line length
	 * @param foregroundColor
	 *            foreground color
	 * @param backgroundColor
	 *            background color
	 */
	public void addRange(int lineStart, int lineLength, Color foregroundColor,
			Color backgroundColor) {
		ranges.add(new StyleRange(lineStart, lineLength, foregroundColor,
				backgroundColor));
	}

	/**
	 * Get visible root of the color tree
	 * 
	 * @return the root
	 */
	public ColorTreeItem getRoot() {
		return root;
	}

	/**
	 * Returns tree item listener
	 * 
	 * @return the contentProvider
	 */
	public TreeItemListener getTreeItemListener() {
		return contentProvider;
	}

	/**
	 * Gets plain text color rules
	 * 
	 * @return plain text color rules
	 */
	public List<ColorTreeTextItem> getTextRules() {
		return textRules;
	}

	/**
	 * Gets component color rules
	 * 
	 * @return component color rules
	 */
	public List<ColorTreeComponentItem> getComponentRules() {
		return componentRules;
	}

	/**
	 * Indicates that some coloring rules are applied
	 * 
	 * @return true if some coloring rules are applied
	 */
	public boolean isColoring() {
		boolean hasRules = false;
		if (!textRules.isEmpty() || !componentRules.isEmpty()) {
			hasRules = true;
		}
		return hasRules;
	}

	/**
	 * Enable color rule
	 * 
	 * @param item
	 *            the rule item
	 */
	public void enableRule(ColorTreeItem item) {
		if (item instanceof ColorTreeTextItem) {
			textRules.add((ColorTreeTextItem) item);
		} else if (item instanceof ColorTreeComponentItem) {
			componentRules.add((ColorTreeComponentItem) item);
		}
	}

}