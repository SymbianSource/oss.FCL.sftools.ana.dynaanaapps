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
 * Imports Filter configuration from XML file
 *
 */
package com.nokia.traceviewer.engine.preferences;

import java.io.File;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.nokia.traceviewer.dialog.BasePropertyDialog;
import com.nokia.traceviewer.dialog.treeitem.FilterTreeBaseItem;
import com.nokia.traceviewer.dialog.treeitem.FilterTreeComponentItem;
import com.nokia.traceviewer.dialog.treeitem.FilterTreeItem;
import com.nokia.traceviewer.dialog.treeitem.FilterTreeTextItem;
import com.nokia.traceviewer.dialog.treeitem.TreeItem;
import com.nokia.traceviewer.dialog.treeitem.TreeItemListener;
import com.nokia.traceviewer.engine.TraceViewerGlobals;
import com.nokia.traceviewer.engine.dataprocessor.FilterProcessor;
import com.nokia.traceviewer.engine.dataprocessor.FilterRuleSet.LogicalOperator;

/**
 * Imports Filter configuration from XML file
 */
public final class XMLFilterConfigurationImporter extends
		XMLBaseConfigurationImporter {

	/**
	 * Tells the logical operator in use
	 */
	private LogicalOperator operator = LogicalOperator.OR;

	/**
	 * Advanced filter string
	 */
	private String advancedFilterString = ""; //$NON-NLS-1$

	/**
	 * Constructor
	 * 
	 * @param root
	 *            root of the Filter elements
	 * @param configurationFileName
	 *            file name where to import data from
	 * @param pathRelative
	 *            tells is the path relative
	 */
	public XMLFilterConfigurationImporter(TreeItem root,
			String configurationFileName, boolean pathRelative) {
		super(root, configurationFileName, pathRelative);
	}

	/**
	 * Imports data
	 */
	public void importData() {
		File file = new File(filePath);
		if (file.exists()) {

			Document doc = getDocument();

			if (doc != null) {

				// Delete old filter rules
				Object[] oldFilters = root.getChildren();
				for (int i = 0; i < oldFilters.length; i++) {
					root.removeChild((FilterTreeItem) oldFilters[i]);
				}

				// Get filter node from the XML file
				NodeList list = doc.getElementsByTagName(FILTER_TAG);
				Node parent = list.item(0);

				if (parent != null) {
					// Process Filter settings
					NamedNodeMap parentAttributes = parent.getAttributes();
					processFilterSettings(parentAttributes);

					// Import filters
					NodeList filters = parent.getChildNodes();
					for (int i = 0; i < filters.getLength(); i++) {
						if (filters.item(i) instanceof Element) {
							processFilter(filters.item(i), root);
						}
					}

					// Enable possible advanced filter
					if (!advancedFilterString.equals("")) { //$NON-NLS-1$
						TraceViewerGlobals.getTraceViewer()
								.getDataProcessorAccess().getFilterProcessor()
								.enableAdvancedFilter(advancedFilterString);
					}
				}
			}
		}
	}

	/**
	 * Process filter settings
	 * 
	 * @param itemAttributes
	 *            attributes
	 */
	private void processFilterSettings(NamedNodeMap itemAttributes) {
		FilterProcessor proc = TraceViewerGlobals.getTraceViewer()
				.getDataProcessorAccess().getFilterProcessor();
		// Show / Hide action
		Attr action = ((Attr) itemAttributes.getNamedItem(ACTION_TAG));
		if (action != null) {
			proc
					.setShowTracesContainingRule(action.getValue().equals(
							SHOW_TAG));
		}

		// Type of the filters
		Attr type = ((Attr) itemAttributes.getNamedItem(TYPE_TAG));
		if (type != null) {
			boolean orInUse = type.getValue().equals(OR_TAG);
			if (!orInUse) {
				operator = LogicalOperator.AND;
			}
			proc.setLogicalOrInUse(orInUse);
		}

		// Advanced filter string
		Attr advancedFilter = ((Attr) itemAttributes
				.getNamedItem(ADVANCEDFILTER_TAG));

		// If exists, set it as member variable. Will be set to processor after
		// all filter rules are applied
		if (advancedFilter != null) {
			if (!advancedFilter.getValue().equals("")) { //$NON-NLS-1$
				advancedFilterString = advancedFilter.getValue();
			}
		}
	}

	/**
	 * Import this filter rule to root
	 * 
	 * @param node
	 *            Filter node to import
	 * @param root
	 *            Root node of filter dialog
	 */
	private void processFilter(Node node, TreeItem root) {
		String name = null;
		NamedNodeMap itemAttributes = node.getAttributes();

		TreeItem newItem = null;

		// Get tree item listener from the processor
		TreeItemListener listener = TraceViewerGlobals.getTraceViewer()
				.getDataProcessorAccess().getFilterProcessor()
				.getTreeItemListener();

		// Group item
		if (node.getNodeName().equals(GROUP_TAG)) {
			// Get name
			Attr nameAttr = (Attr) itemAttributes.getNamedItem(NAME_TAG);
			name = nameAttr.getValue();
			newItem = new FilterTreeBaseItem(listener, root, name,
					FilterTreeItem.Rule.GROUP);

			// This is for normal rules
		} else if (node.getNodeName().equals(CONFIGURATION_TAG)) {
			// Get name
			Attr nameAttr = (Attr) itemAttributes.getNamedItem(NAME_TAG);
			name = nameAttr.getValue();

			// Get rule
			Attr ruleAttr = (Attr) itemAttributes.getNamedItem(RULE_TAG);
			String rule = ruleAttr.getValue();

			if (rule.equals(TEXTRULE_TAG)) {
				newItem = processTextRule(node, root, name, listener);
			} else if (rule.equals(COMPONENTRULE_TAG)) {
				newItem = processComponentRule(node, root, name, listener);
			}

			// Check if the filter rules should be kept over boot
			Attr enabledAttr = (Attr) itemAttributes.getNamedItem(ENABLED_TAG);
			if (enabledAttr != null) {
				String enabled = enabledAttr.getValue();
				if (enabled.equals(YES_TAG)) {
					TraceViewerGlobals.getTraceViewer()
							.getDataProcessorAccess().getFilterProcessor()
							.enableRule((FilterTreeItem) newItem, operator);
				}
			}

		} else {
			// Unknown rule, shouldn't come here
		}

		// Add the child to the parent node
		if (newItem != null) {
			root.addChild(newItem);
		}

		// If node was group, process it's children
		if (node.getNodeName().equals(GROUP_TAG)) {
			// Get children
			NodeList children = node.getChildNodes();
			for (int i = 0; i < children.getLength(); i++) {
				if (children.item(i) instanceof Element) {
					processFilter(children.item(i), newItem);
				}
			}
		}

	}

	/**
	 * Process text filter rule
	 * 
	 * @param node
	 *            node to process
	 * @param root
	 *            root node
	 * @param name
	 *            name for the rule
	 * @param listener
	 *            TreeItem Listener
	 * @return the newly created text item
	 */
	private TreeItem processTextRule(Node node, TreeItem root, String name,
			TreeItemListener listener) {
		TreeItem newItem;
		String text = null;
		boolean matchCase = false;
		NodeList textProperties = node.getChildNodes();
		for (int i = 0; i < textProperties.getLength(); i++) {
			if (textProperties.item(i) instanceof Element) {
				if (textProperties.item(i).getNodeName().equals(TEXT_TAG)) {
					text = textProperties.item(i).getTextContent();
				} else if (textProperties.item(i).getNodeName().equals(
						MATCHCASE_TAG)) {
					if (textProperties.item(i).getTextContent().equals(NO_TAG)) {
						matchCase = false;
					} else {
						matchCase = true;
					}
				}
			}
		}

		// Create new item
		newItem = new FilterTreeTextItem(listener, root, name,
				FilterTreeItem.Rule.TEXT_RULE, text, matchCase);

		return newItem;
	}

	/**
	 * Process component filter rule
	 * 
	 * @param node
	 *            node to process
	 * @param root
	 *            root node
	 * @param name
	 *            name of the rule
	 * @param listener
	 *            TreeItem Listener
	 * @return the newly created component item
	 */
	private TreeItem processComponentRule(Node node, TreeItem root,
			String name, TreeItemListener listener) {
		TreeItem newItem;
		int componentId = BasePropertyDialog.WILDCARD_INTEGER;
		int groupId = BasePropertyDialog.WILDCARD_INTEGER;
		NodeList properties = node.getChildNodes();
		for (int i = 0; i < properties.getLength(); i++) {
			if (properties.item(i) instanceof Element) {
				String nodeName = properties.item(i).getNodeName();
				if (nodeName.equals(COMPONENTID_TAG)) {
					// Get component ID. If parse fails, it means that component
					// ID is a wildcard
					try {
						componentId = Integer.parseInt(properties.item(i)
								.getTextContent());
					} catch (NumberFormatException e) {
					}
				} else if (nodeName.equals(GROUPID_TAG)) {
					// Get group ID. If parse fails, it means that group
					// ID is a wildcard
					try {
						groupId = Integer.parseInt(properties.item(i)
								.getTextContent());
					} catch (NumberFormatException e) {
					}
				}
			}
		}

		// Create new component item
		newItem = new FilterTreeComponentItem(listener, root, name,
				FilterTreeItem.Rule.COMPONENT_RULE, componentId, groupId);

		return newItem;
	}
}
