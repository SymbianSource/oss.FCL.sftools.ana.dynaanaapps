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
 * Imports Color configuration from XML file
 *
 */
package com.nokia.traceviewer.engine.preferences;

import java.io.File;

import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.graphics.Color;
import org.eclipse.ui.PlatformUI;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.nokia.traceviewer.dialog.BasePropertyDialog;
import com.nokia.traceviewer.dialog.treeitem.ColorTreeBaseItem;
import com.nokia.traceviewer.dialog.treeitem.ColorTreeComponentItem;
import com.nokia.traceviewer.dialog.treeitem.ColorTreeItem;
import com.nokia.traceviewer.dialog.treeitem.ColorTreeTextItem;
import com.nokia.traceviewer.dialog.treeitem.TreeItem;
import com.nokia.traceviewer.dialog.treeitem.TreeItemListener;
import com.nokia.traceviewer.engine.TraceViewerGlobals;
import com.nokia.traceviewer.engine.dataprocessor.ColorProcessor;

/**
 * Imports Color configuration from XML file
 * 
 */
public final class XMLColorConfigurationImporter extends
		XMLBaseConfigurationImporter {

	/**
	 * Constructor
	 * 
	 * @param root
	 *            root of the Color elements
	 * @param configurationFileName
	 *            file name where to import data from
	 * @param pathRelative
	 *            tells is the path relative
	 */
	public XMLColorConfigurationImporter(TreeItem root,
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

				// Delete old color rules
				Object[] oldColorRules = root.getChildren();
				for (int i = 0; i < oldColorRules.length; i++) {

					// Dispose colors
					if ((((ColorTreeItem) oldColorRules[i])
							.getForegroundColor() != null)
							&& ((ColorTreeItem) oldColorRules[i])
									.getBackgroundColor() != null) {
						((ColorTreeItem) oldColorRules[i]).getForegroundColor()
								.dispose();
						((ColorTreeItem) oldColorRules[i]).getBackgroundColor()
								.dispose();
					}
					root.removeChild((ColorTreeItem) oldColorRules[i]);
				}

				// Clear all old rules
				ColorProcessor proc = TraceViewerGlobals.getTraceViewer()
						.getDataProcessorAccess().getColorer();
				proc.getTextRules().clear();
				proc.getComponentRules().clear();
				proc.clearRanges();
				TraceViewerGlobals.getTraceViewer().getView().applyColorRules(
						proc.getRanges().toArray(new StyleRange[0]));

				// Get color node from the XML file
				NodeList list = doc.getElementsByTagName(COLOR_TAG);
				Node parent = list.item(0);

				// Import colors
				if (parent != null) {
					NodeList colors = parent.getChildNodes();
					for (int i = 0; i < colors.getLength(); i++) {
						if (colors.item(i) instanceof Element) {
							processColorRule(colors.item(i), root);
						}
					}
				}

				// Create new rules
				proc.createColorRules();
			}
		}
	}

	/**
	 * Import this color rule to root
	 * 
	 * @param node
	 *            Color node to import
	 * @param root
	 *            Root node of color dialog
	 */
	private void processColorRule(Node node, TreeItem root) {
		String name = null;
		NamedNodeMap itemAttributes = node.getAttributes();

		// Get name
		Attr nameAttr = (Attr) itemAttributes.getNamedItem(NAME_TAG);
		name = nameAttr.getValue();

		// Group item
		TreeItem newItem = null;
		if (node.getNodeName().equals(GROUP_TAG)) {
			newItem = new ColorTreeBaseItem(TraceViewerGlobals.getTraceViewer()
					.getDataProcessorAccess().getColorer()
					.getTreeItemListener(), root, name,
					ColorTreeItem.Rule.GROUP, null, null);

			// Process normal rules
		} else if (node.getNodeName().equals(CONFIGURATION_TAG)) {

			// Get tree item listener from the processor
			TreeItemListener listener = TraceViewerGlobals.getTraceViewer()
					.getDataProcessorAccess().getColorer()
					.getTreeItemListener();

			// Get rule name
			Attr ruleAttr = (Attr) itemAttributes.getNamedItem(RULE_TAG);
			String rule = ruleAttr.getValue();

			// Create Text Item
			if (rule.equals(TEXTRULE_TAG)) {
				newItem = processTextRule(node, root, name, listener);
				// Create Component Item
			} else if (rule.equals(COMPONENTRULE_TAG)) {
				newItem = processComponentRule(node, root, name, listener);
			} else {
				// Unknown rule, shouldn't come here
			}

			// Check if the color rules should be kept over boot
			Attr enabledAttr = (Attr) itemAttributes.getNamedItem(ENABLED_TAG);
			if (enabledAttr != null) {
				String enabled = enabledAttr.getValue();
				if (enabled.equals(YES_TAG)) {
					TraceViewerGlobals.getTraceViewer()
							.getDataProcessorAccess().getColorer().enableRule(
									(ColorTreeItem) newItem);
				}
			}

		} else {
			// Unknown node name, shouldn't come here
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
					processColorRule(children.item(i), newItem);
				}
			}
		}

	}

	/**
	 * Processes text rule
	 * 
	 * @param node
	 *            Node to process
	 * @param root
	 *            root node
	 * @param name
	 *            name of the rule
	 * @param listener
	 *            TreeItem listener
	 * @return the newly created text item
	 */
	private TreeItem processTextRule(Node node, TreeItem root, String name,
			TreeItemListener listener) {
		TreeItem newItem;
		String text = null;
		boolean matchCase = false;
		Color foregroundColor = null;
		Color backgroundColor = null;
		NodeList properties = node.getChildNodes();
		for (int i = 0; i < properties.getLength(); i++) {
			if (properties.item(i) instanceof Element) {
				String nodeName = properties.item(i).getNodeName();
				if (nodeName.equals(FORECOLOR_TAG)) {
					foregroundColor = createColorFromHexString(properties.item(
							i).getTextContent());
				} else if (nodeName.equals(BACKCOLOR_TAG)) {
					backgroundColor = createColorFromHexString(properties.item(
							i).getTextContent());
				} else if (nodeName.equals(TEXT_TAG)) {
					text = properties.item(i).getTextContent();
				} else if (nodeName.equals(MATCHCASE_TAG)) {
					if (properties.item(i).getTextContent().equals(NO_TAG)) {
						matchCase = false;
					} else {
						matchCase = true;
					}
				}
			}
		}

		// Create new text item
		newItem = new ColorTreeTextItem(listener, root, name,
				ColorTreeItem.Rule.TEXT_RULE, foregroundColor, backgroundColor,
				text, matchCase);
		return newItem;
	}

	/**
	 * Processes component rule
	 * 
	 * @param node
	 *            Node to process
	 * @param root
	 *            root node
	 * @param name
	 *            name of the rule
	 * @param listener
	 *            TreeItem listener
	 * @return the newly created component item
	 */
	private TreeItem processComponentRule(Node node, TreeItem root,
			String name, TreeItemListener listener) {
		TreeItem newItem;
		Color foregroundColor = null;
		Color backgroundColor = null;
		int componentId = BasePropertyDialog.WILDCARD_INTEGER;
		int groupId = BasePropertyDialog.WILDCARD_INTEGER;
		NodeList properties = node.getChildNodes();
		for (int i = 0; i < properties.getLength(); i++) {
			if (properties.item(i) instanceof Element) {
				String nodeName = properties.item(i).getNodeName();
				if (nodeName.equals(FORECOLOR_TAG)) {
					foregroundColor = createColorFromHexString(properties.item(
							i).getTextContent());
				} else if (nodeName.equals(BACKCOLOR_TAG)) {
					backgroundColor = createColorFromHexString(properties.item(
							i).getTextContent());
				} else if (nodeName.equals(COMPONENTID_TAG)) {
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
		newItem = new ColorTreeComponentItem(listener, root, name,
				ColorTreeItem.Rule.COMPONENT_RULE, foregroundColor,
				backgroundColor, componentId, groupId);
		return newItem;
	}

	/**
	 * Creates color from hex string
	 * 
	 * @param hexString
	 *            hex string
	 * @return color
	 */
	private Color createColorFromHexString(String hexString) {
		final int hex = 16;
		int red = Integer.parseInt(hexString.substring(0, 2), hex);
		int green = Integer.parseInt(hexString.substring(2, 4), hex);
		int blue = Integer.parseInt(hexString.substring(4, 6), hex);
		Color color = new Color(PlatformUI.getWorkbench().getDisplay(), red,
				green, blue);
		return color;
	}
}
