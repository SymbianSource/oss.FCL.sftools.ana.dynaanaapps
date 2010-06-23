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
 * Exports Color configuration to XML file
 *
 */
package com.nokia.traceviewer.engine.preferences;

import javax.xml.transform.stream.StreamResult;

import org.eclipse.swt.graphics.Color;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.nokia.traceviewer.dialog.treeitem.ColorTreeComponentItem;
import com.nokia.traceviewer.dialog.treeitem.ColorTreeItem;
import com.nokia.traceviewer.dialog.treeitem.ColorTreeTextItem;
import com.nokia.traceviewer.dialog.treeitem.TreeItem;
import com.nokia.traceviewer.engine.TraceViewerGlobals;

/**
 * Exports Color configuration to XML file
 */
public final class XMLColorConfigurationExporter extends
		XMLBaseConfigurationExporter {

	/**
	 * Constructor
	 * 
	 * @param root
	 *            root of the Color elements
	 * @param configurationFileName
	 *            file name where to export data
	 * @param pathRelative
	 *            tells is the path relative
	 */
	public XMLColorConfigurationExporter(TreeItem root,
			String configurationFileName, boolean pathRelative) {
		super(root, configurationFileName, pathRelative);
	}

	/**
	 * Exports data
	 */
	public void export() {
		Document doc = getDocument();
		Node parent;

		// Get root node of color rules
		NodeList list = doc.getElementsByTagName(COLOR_TAG);
		if (list.getLength() == 0) {
			parent = doc.createElement(COLOR_TAG);
			// Get the main configurations tag
			NodeList list2 = doc.getElementsByTagName(CONFIGURATIONS_TAG);
			Node mainRoot = list2.item(0);
			mainRoot.appendChild(parent);
		} else {
			parent = list.item(0);
		}

		// Delete old color information
		NodeList colorChilds = parent.getChildNodes();
		int len = colorChilds.getLength();
		for (int i = 0; i < len; i++) {
			parent.removeChild(colorChilds.item(0));
		}

		// Process through the tree
		for (int i = 0; i < root.getChildren().length; i++) {
			processChild((ColorTreeItem) root.getChildren()[i], doc, parent);
		}

		// Get result from document
		StreamResult result = getResultFromDocument(doc);

		// Print out
		String xmlString = result.getWriter().toString();
		writeFile(xmlString);
	}

	/**
	 * Process child
	 * 
	 * @param item
	 *            child to process
	 * @param doc
	 *            document
	 * @param root
	 *            node to insert this child to
	 */
	private void processChild(ColorTreeItem item, Document doc, Node root) {
		// Process through the tree
		Node newItem = null;
		// Group rule
		if (item.getRule() == ColorTreeItem.Rule.GROUP) {
			newItem = doc.createElement(GROUP_TAG);
			// Create a configuration
		} else {
			newItem = doc.createElement(CONFIGURATION_TAG);
		}

		// Get attributes
		NamedNodeMap itemAttributes = newItem.getAttributes();

		// Set name
		Attr name = doc.createAttribute(NAME_TAG);
		name.setValue(item.getName());
		itemAttributes.setNamedItem(name);

		// Check is the item group
		if (item.getRule() != ColorTreeItem.Rule.GROUP) {

			// Add rule type attribute
			Attr rule = doc.createAttribute(RULE_TAG);
			if (item.getRule() == ColorTreeItem.Rule.TEXT_RULE) {
				rule.setValue(TEXTRULE_TAG);
			} else if (item.getRule() == ColorTreeItem.Rule.COMPONENT_RULE) {
				rule.setValue(COMPONENTRULE_TAG);
			} else {
				String unknownText = Messages
						.getString("XMLColorConfigurationExporter.UnknownText"); //$NON-NLS-1$ 
				rule.setValue(unknownText);
			}
			itemAttributes.setNamedItem(rule);

			// Add enabled attribute
			Attr enabled = doc.createAttribute(ENABLED_TAG);
			boolean checked = isChecked(item);
			if (checked) {
				enabled.setValue(YES_TAG);
			} else {
				enabled.setValue(NO_TAG);
			}
			itemAttributes.setNamedItem(enabled);
		}

		// Set as child to parent
		root.appendChild(newItem);

		// Set common attributes for non-group items
		if (item.getRule() != ColorTreeItem.Rule.GROUP) {

			// ForeColor
			Node foreColor = doc.createElement(FORECOLOR_TAG);
			Color foregroundColor = item.getForegroundColor();
			String foregroundColorString = createHexStringFromColor(foregroundColor);
			foreColor.setTextContent(foregroundColorString);
			newItem.appendChild(foreColor);

			// BackColor
			Node backColor = doc.createElement(BACKCOLOR_TAG);
			Color backgroundColor = item.getBackgroundColor();
			String backgroundColorString = createHexStringFromColor(backgroundColor);
			backColor.setTextContent(backgroundColorString);
			newItem.appendChild(backColor);
		}

		// Set rest of the attributes from text rule
		if (item.getRule() == ColorTreeItem.Rule.TEXT_RULE) {
			ColorTreeTextItem textItem = (ColorTreeTextItem) item;

			// Text
			Node text = doc.createElement(TEXT_TAG);
			text.setTextContent(textItem.getText());
			newItem.appendChild(text);

			// Match case
			Node matchCase = doc.createElement(MATCHCASE_TAG);
			if (textItem.isMatchCase()) {
				matchCase.setTextContent(YES_TAG);
			} else {
				matchCase.setTextContent(NO_TAG);
			}
			newItem.appendChild(matchCase);

			// Component rule
		} else if (item.getRule() == ColorTreeItem.Rule.COMPONENT_RULE) {
			ColorTreeComponentItem componentItem = (ColorTreeComponentItem) item;

			// Component ID
			Node component = doc.createElement(COMPONENTID_TAG);
			component.setTextContent(String.valueOf(componentItem
					.getComponentId()));
			newItem.appendChild(component);

			// Group ID
			Node group = doc.createElement(GROUPID_TAG);
			group.setTextContent(String.valueOf(componentItem.getGroupId()));
			newItem.appendChild(group);
		}

		// Loop through own children
		for (int i = 0; i < item.getChildren().length; i++) {
			processChild((ColorTreeItem) item.getChildren()[i], doc, newItem);
		}
	}

	/**
	 * Checks it the given item is checked in the color dialog
	 * 
	 * @param item
	 *            item to be checked
	 * @return true if the given item is checked
	 */
	private boolean isChecked(ColorTreeItem item) {
		boolean checked = false;
		if (TraceViewerGlobals.getTraceViewer().getDataProcessorAccess()
				.getColorer().getTextRules().contains(item)
				|| TraceViewerGlobals.getTraceViewer().getDataProcessorAccess()
						.getColorer().getComponentRules().contains(item)) {
			checked = true;
		}
		return checked;
	}

	/**
	 * Creates hex string from color
	 * 
	 * @param color
	 *            color to create hex string from
	 * @return hex string
	 */
	private String createHexStringFromColor(Color color) {
		String colorString = ""; //$NON-NLS-1$
		if (color != null) {

			// Add red color as hex
			String colorPart = Integer.toHexString(color.getRed());
			if (colorPart.equals("0")) { //$NON-NLS-1$
				colorString += "0"; //$NON-NLS-1$
			}
			colorString += colorPart;

			// Add green color as hex
			colorPart = Integer.toHexString(color.getGreen());
			if (colorPart.equals("0")) { //$NON-NLS-1$
				colorString += "0"; //$NON-NLS-1$
			}
			colorString += colorPart;

			// Add blue color as hex
			colorPart = Integer.toHexString(color.getBlue());
			if (colorPart.equals("0")) { //$NON-NLS-1$
				colorString += "0"; //$NON-NLS-1$
			}
			colorString += colorPart;
		}
		return colorString;
	}
}
