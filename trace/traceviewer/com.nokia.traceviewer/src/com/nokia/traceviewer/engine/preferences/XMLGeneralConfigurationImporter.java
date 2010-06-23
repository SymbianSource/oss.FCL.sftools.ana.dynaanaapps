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
 * Imports general configuration from XML file
 *
 */
package com.nokia.traceviewer.engine.preferences;

import java.io.File;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.nokia.traceviewer.action.OpenDecodeFileAction;
import com.nokia.traceviewer.engine.TraceViewerGlobals;

/**
 * Imports general configuration from XML file
 */
public class XMLGeneralConfigurationImporter extends
		XMLBaseConfigurationImporter {

	/**
	 * Constructor
	 * 
	 * @param configurationFileName
	 *            file name where to import data from
	 * @param pathRelative
	 *            tells is the path relative
	 */
	public XMLGeneralConfigurationImporter(String configurationFileName,
			boolean pathRelative) {
		super(null, configurationFileName, pathRelative);
	}

	/**
	 * Imports data
	 */
	public void importData() {
		File file = new File(filePath);
		if (file.exists()) {
			Document doc = getDocument();

			if (doc != null) {

				// Get lineCount node from the XML file
				NodeList list = doc.getElementsByTagName(GENERAL_TAG);
				Node parent = list.item(0);

				// Import configurations
				if (parent != null) {
					NodeList children = parent.getChildNodes();
					for (int i = 0; i < children.getLength(); i++) {
						if (children.item(i) instanceof Element) {
							processItems(children.item(i));
						}
					}
				}
			}
		}
	}

	/**
	 * Import configurations
	 * 
	 * @param node
	 *            Configuration node
	 */
	private void processItems(Node node) {
		// Decode file
		if (node.getNodeName().equals(DECODEFILE_TAG)) {
			processDecodeFile(node);
		}
	}

	/**
	 * Processes decode file node
	 * 
	 * @param node
	 *            node to be processed
	 */
	private void processDecodeFile(Node node) {
		String path = node.getTextContent();
		if (path != null && !path.equals("")) { //$NON-NLS-1$
			OpenDecodeFileAction action = (OpenDecodeFileAction) TraceViewerGlobals
					.getTraceViewer().getView().getActionFactory()
					.getOpenDecodeFileAction();
			action.addOpenInStartupDictionary(path);
		}
	}
}
