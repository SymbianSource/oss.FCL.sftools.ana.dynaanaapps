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
 * Imports Trigger configuration from XML file
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
import com.nokia.traceviewer.dialog.treeitem.TriggerTreeBaseItem;
import com.nokia.traceviewer.dialog.treeitem.TriggerTreeItem;
import com.nokia.traceviewer.dialog.treeitem.TriggerTreeTextItem;
import com.nokia.traceviewer.dialog.treeitem.TriggerTreeItem.Type;
import com.nokia.traceviewer.engine.TraceViewerGlobals;

/**
 * Imports Trigger configuration from XML file
 * 
 */
public class XMLTriggerConfigurationImporter extends
		XMLBaseConfigurationImporter {

	/**
	 * Constructor
	 * 
	 * @param root
	 *            root of the Trigger elements
	 * @param configurationFileName
	 *            file name where to export data
	 * @param pathRelative
	 *            tells is the path relative
	 */
	public XMLTriggerConfigurationImporter(TreeItem root,
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

				// Delete old rules
				Object[] oldRules = root.getChildren();
				for (int i = 0; i < oldRules.length; i++) {
					root.removeChild((TriggerTreeItem) oldRules[i]);
				}

				// Get trigger node from the XML file
				NodeList list = doc.getElementsByTagName(TRIGGER_TAG);
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
	 * Import this trigger rule to root
	 * 
	 * @param node
	 *            Trigger node to import
	 * @param root
	 *            Root node of trigger dialog
	 */
	private void processChild(Node node, TreeItem root) {
		String name = null;
		String text = null;
		boolean matchCase = false;
		Type triggerType = null;

		NamedNodeMap itemAttributes = node.getAttributes();

		// Get name
		Attr nameAttr = (Attr) itemAttributes.getNamedItem(NAME_TAG);
		name = nameAttr.getValue();

		// Group item
		TreeItem newItem = null;
		if (node.getNodeName().equals(GROUP_TAG)) {
			newItem = new TriggerTreeBaseItem(TraceViewerGlobals
					.getTraceViewer().getDataProcessorAccess()
					.getTriggerProcessor().getTreeItemListener(), root, name,
					TriggerTreeItem.Rule.GROUP,
					TriggerTreeItem.Type.STARTTRIGGER);

			// This is for Text rules
		} else if (node.getNodeName().equals(CONFIGURATION_TAG)) {

			// Get rule
			Attr ruleAttr = (Attr) itemAttributes.getNamedItem(RULE_TAG);
			String rule = ruleAttr.getValue();

			if (rule.equals(TEXTRULE_TAG)) {
				newItem = processTextRule(node, root, name, text, matchCase,
						triggerType);

			} else {
				// Unknown rule, shouldn't come here
			}

			// Check if the rules should be kept over boot
			Attr enabledAttr = (Attr) itemAttributes.getNamedItem(ENABLED_TAG);
			if (enabledAttr != null && newItem != null) {
				String enabled = enabledAttr.getValue();
				if (enabled.equals(YES_TAG)) {
					TraceViewerGlobals.getTraceViewer()
							.getDataProcessorAccess().getTriggerProcessor()
							.enableRule((TriggerTreeItem) newItem);
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
	 * Processes text rule
	 * 
	 * @param node
	 *            xml node
	 * @param root
	 *            root of trigger rules
	 * @param name
	 *            name of the rule
	 * @param text
	 *            text of the rule
	 * @param matchCase
	 *            match case value
	 * @param triggerType
	 *            trigger type
	 * @return newly created trigger item
	 */
	private TreeItem processTextRule(Node node, TreeItem root, String name,
			String text, boolean matchCase, Type triggerType) {
		String configurationFilePath = null;
		String configurationName = null;

		TreeItem newItem;
		// Get the text properties
		NodeList textProperties = node.getChildNodes();
		for (int i = 0; i < textProperties.getLength(); i++) {
			if (textProperties.item(i) instanceof Element) {
				// Text
				if (textProperties.item(i).getNodeName().equals(TEXT_TAG)) {
					text = textProperties.item(i).getTextContent();
					// Match case
				} else if (textProperties.item(i).getNodeName().equals(
						MATCHCASE_TAG)) {
					if (textProperties.item(i).getTextContent().equals(NO_TAG)) {
						matchCase = false;
					} else {
						matchCase = true;
					}
					// Trigger type
				} else if (textProperties.item(i).getNodeName().equals(
						TRIGGERTYPE_TAG)) {
					if (textProperties.item(i).getTextContent().equals(
							STARTTRIGGER_TAG)) {
						triggerType = TriggerTreeItem.Type.STARTTRIGGER;
					} else if (textProperties.item(i).getTextContent().equals(
							STOPTRIGGER_TAG)) {
						triggerType = TriggerTreeItem.Type.STOPTRIGGER;
					} else if (textProperties.item(i).getTextContent().equals(
							ACTIVATIONTRIGGER_TAG)) {
						triggerType = TriggerTreeItem.Type.ACTIVATIONTRIGGER;
					} else {
						// Unknown type, shouldn't come here
					}
					// Configuration file
				} else if (textProperties.item(i).getNodeName().equals(
						CONFIGURATION_FILE_TAG)) {
					configurationFilePath = textProperties.item(i)
							.getTextContent();

					// Configuration name
				} else if (textProperties.item(i).getNodeName().equals(
						CONFIGURATION_NAME_TAG)) {
					configurationName = textProperties.item(i).getTextContent();
				}
			}
		}

		// Create new item
		TreeItemListener listener = TraceViewerGlobals.getTraceViewer()
				.getDataProcessorAccess().getTriggerProcessor()
				.getTreeItemListener();
		newItem = new TriggerTreeTextItem(listener, root, name,
				TriggerTreeItem.Rule.TEXT_RULE, text, matchCase, triggerType,
				configurationFilePath, configurationName);
		return newItem;
	}
}
