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
 * Imports Variable Tracing configuration from XML file
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

import com.nokia.traceviewer.dialog.treeitem.TreeItem;
import com.nokia.traceviewer.dialog.treeitem.TreeItemListener;
import com.nokia.traceviewer.dialog.treeitem.VariableTracingTreeBaseItem;
import com.nokia.traceviewer.dialog.treeitem.VariableTracingTreeItem;
import com.nokia.traceviewer.dialog.treeitem.VariableTracingTreeTextItem;
import com.nokia.traceviewer.engine.TraceViewerGlobals;

/**
 * Imports Variable Tracing configuration from XML file
 */
public final class XMLVariableTracingConfigurationImporter extends
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
	public XMLVariableTracingConfigurationImporter(TreeItem root,
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

				// Delete and clear old rules
				Object[] oldRules = root.getChildren();
				for (int i = 0; i < oldRules.length; i++) {
					root.removeChild((VariableTracingTreeItem) oldRules[i]);
				}
				TraceViewerGlobals.getTraceViewer().getDataProcessorAccess()
						.getVariableTracingProcessor()
						.getVariableTracingItems().clear();
				TraceViewerGlobals.getTraceViewer().getDataProcessorAccess()
						.getVariableTracingProcessor().getTextRules().clear();

				// Get variabletracing node from the XML file
				NodeList list = doc.getElementsByTagName(VARIABLETRACING_TAG);
				Node parent = list.item(0);

				// Import children
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
	 * Import this variableTracing rule to root
	 * 
	 * @param node
	 *            VariableTracing node to import
	 * @param root
	 *            Root node of variableTracing dialog
	 */
	private void processChild(Node node, TreeItem root) {
		String name = null;
		String text = null;
		boolean matchCase = false;
		int saveHistory = 0;

		NamedNodeMap itemAttributes = node.getAttributes();

		// Get name
		Attr nameAttr = (Attr) itemAttributes.getNamedItem(NAME_TAG);
		name = nameAttr.getValue();

		// Group item
		TreeItem newItem = null;
		if (node.getNodeName().equals(GROUP_TAG)) {
			newItem = new VariableTracingTreeBaseItem(TraceViewerGlobals
					.getTraceViewer().getDataProcessorAccess()
					.getVariableTracingProcessor().getTreeItemListener(), root,
					name, VariableTracingTreeItem.Rule.GROUP);

			// Configurations tag
		} else if (node.getNodeName().equals(CONFIGURATION_TAG)) {

			// Get rule
			Attr ruleAttr = (Attr) itemAttributes.getNamedItem(RULE_TAG);
			String rule = ruleAttr.getValue();

			// Process text rule
			if (rule.equals(TEXTRULE_TAG)) {
				newItem = processTextRule(node, root, name, text, matchCase,
						saveHistory);
			}

			// Check if the rules should be kept over boot
			Attr enabledAttr = (Attr) itemAttributes.getNamedItem(ENABLED_TAG);
			if (enabledAttr != null && newItem != null) {
				String enabled = enabledAttr.getValue();
				if (enabled.equals(YES_TAG)) {
					TraceViewerGlobals.getTraceViewer()
							.getDataProcessorAccess()
							.getVariableTracingProcessor().enableRule(
									(VariableTracingTreeItem) newItem);
				}
			}
		} else {
			// Shouldn't come here
		}

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
	 * Processes text rule
	 * 
	 * @param node
	 *            xml node
	 * @param root
	 *            parent node
	 * @param name
	 *            name of the rule
	 * @param text
	 *            text of the rule
	 * @param matchCase
	 *            match case value
	 * @param saveHistory
	 *            amount of history items to save
	 * @return newly created text item
	 */
	private TreeItem processTextRule(Node node, TreeItem root, String name,
			String text, boolean matchCase, int saveHistory) {
		TreeItem newItem;
		// Get the text properties
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
				} else if (textProperties.item(i).getNodeName().equals(
						SAVEHISTORY_TAG)) {
					try {
						saveHistory = Integer.parseInt(textProperties.item(i)
								.getTextContent());
					} catch (Exception e) {
						saveHistory = 0;
					}
				}
			}
		}

		// Create new item
		TreeItemListener listener = TraceViewerGlobals.getTraceViewer()
				.getDataProcessorAccess().getVariableTracingProcessor()
				.getTreeItemListener();
		newItem = new VariableTracingTreeTextItem(listener, root, name,
				VariableTracingTreeItem.Rule.TEXT_RULE, text, matchCase,
				saveHistory);
		return newItem;
	}
}
