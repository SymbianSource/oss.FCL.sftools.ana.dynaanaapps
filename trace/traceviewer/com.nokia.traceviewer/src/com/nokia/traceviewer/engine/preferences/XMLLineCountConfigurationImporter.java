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
 * Imports LineCountRule configuration from XML file
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
import com.nokia.traceviewer.dialog.treeitem.LineCountTreeBaseItem;
import com.nokia.traceviewer.dialog.treeitem.LineCountTreeComponentItem;
import com.nokia.traceviewer.dialog.treeitem.LineCountTreeItem;
import com.nokia.traceviewer.dialog.treeitem.LineCountTreeTextItem;
import com.nokia.traceviewer.dialog.treeitem.TreeItem;
import com.nokia.traceviewer.dialog.treeitem.TreeItemListener;
import com.nokia.traceviewer.engine.TraceViewerGlobals;

/**
 * Imports LineCountRule configuration from XML file
 */
public final class XMLLineCountConfigurationImporter extends
		XMLBaseConfigurationImporter {

	/**
	 * Constructor
	 * 
	 * @param root
	 *            root of the LineCount elements
	 * @param configurationFileName
	 *            file name where to export data
	 * @param pathRelative
	 *            tells is the path relative
	 */
	public XMLLineCountConfigurationImporter(TreeItem root,
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

				// Remove all children from the tree item
				Object[] oldRules = root.getChildren();
				for (int i = 0; i < oldRules.length; i++) {
					root.removeChild((LineCountTreeItem) oldRules[i]);
				}

				// Delete old rules from the processor
				TraceViewerGlobals.getTraceViewer().getDataProcessorAccess()
						.getLineCountProcessor().getLineCountItems().clear();
				TraceViewerGlobals.getTraceViewer().getDataProcessorAccess()
						.getLineCountProcessor().getTextRules().clear();
				TraceViewerGlobals.getTraceViewer().getDataProcessorAccess()
						.getLineCountProcessor().getComponentRules().clear();

				// Get lineCount node from the XML file
				NodeList list = doc.getElementsByTagName(LINECOUNT_TAG);
				Node parent = list.item(0);

				// Import rules
				if (parent != null) {
					NodeList children = parent.getChildNodes();
					for (int i = 0; i < children.getLength(); i++) {
						if (children.item(i) instanceof Element) {
							processChild(children.item(i), root);
						}
					}
				}
			}
		}
	}

	/**
	 * Import this Line Count rule to root
	 * 
	 * @param node
	 *            Line Count node to import
	 * @param root
	 *            Root node of lineCount dialog
	 */
	private void processChild(Node node, TreeItem root) {
		String name = null;
		NamedNodeMap itemAttributes = node.getAttributes();

		// Get name
		Attr nameAttr = (Attr) itemAttributes.getNamedItem(NAME_TAG);
		name = nameAttr.getValue();

		// Group item
		TreeItem newItem = null;
		if (node.getNodeName().equals(GROUP_TAG)) {
			newItem = new LineCountTreeBaseItem(TraceViewerGlobals
					.getTraceViewer().getDataProcessorAccess()
					.getLineCountProcessor().getTreeItemListener(), root, name,
					LineCountTreeItem.Rule.GROUP);

			// This is for normal rules
		} else if (node.getNodeName().equals(CONFIGURATION_TAG)) {

			// Get tree item listener from the processor
			TreeItemListener listener = TraceViewerGlobals.getTraceViewer()
					.getDataProcessorAccess().getLineCountProcessor()
					.getTreeItemListener();

			// Get rule
			Attr ruleAttr = (Attr) itemAttributes.getNamedItem(RULE_TAG);
			String rule = ruleAttr.getValue();

			if (rule.equals(TEXTRULE_TAG)) {
				newItem = processTextRule(node, root, name, listener);
			} else if (rule.equals(COMPONENTRULE_TAG)) {
				newItem = processComponentRule(node, root, name, listener);
			}

			// Check if the rules should be kept over boot
			Attr enabledAttr = (Attr) itemAttributes.getNamedItem(ENABLED_TAG);
			if (enabledAttr != null) {
				String enabled = enabledAttr.getValue();
				if (enabled.equals(YES_TAG)) {
					TraceViewerGlobals.getTraceViewer()
							.getDataProcessorAccess().getLineCountProcessor()
							.enableRule((LineCountTreeItem) newItem);
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
					processChild(children.item(i), newItem);
				}
			}
		}

	}

	/**
	 * Process text LineCount rule
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
		newItem = new LineCountTreeTextItem(listener, root, name,
				LineCountTreeItem.Rule.TEXT_RULE, text, matchCase);
		return newItem;
	}

	/**
	 * Process component LineCount rule
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
		newItem = new LineCountTreeComponentItem(listener, root, name,
				LineCountTreeItem.Rule.COMPONENT_RULE, componentId, groupId);
		return newItem;
	}
}
