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
 * Exports general configuration to XML file
 *
 */
package com.nokia.traceviewer.engine.preferences;

import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.nokia.traceviewer.action.OpenDecodeFileAction;
import com.nokia.traceviewer.engine.TraceViewerGlobals;
import com.nokia.traceviewer.engine.activation.TraceActivationComponentItem;

/**
 * Exports general configuration to XML file
 * 
 */
public class XMLGeneralConfigurationExporter extends
		XMLBaseConfigurationExporter {

	/**
	 * Constructor
	 * 
	 * @param configurationFileName
	 *            file name where to export data
	 * @param pathRelative
	 *            tells is the path relative
	 */
	public XMLGeneralConfigurationExporter(String configurationFileName,
			boolean pathRelative) {
		super(null, configurationFileName, pathRelative);
	}

	/**
	 * Exports data
	 */
	public void export() {
		Document doc = getDocument();
		Node parent;

		// Get root node of general configs
		NodeList list = doc.getElementsByTagName(GENERAL_TAG);

		// Tag not found
		if (list.getLength() == 0) {
			parent = doc.createElement(GENERAL_TAG);
			// Get the main configurations tag
			NodeList list2 = doc.getElementsByTagName(CONFIGURATIONS_TAG);
			Node mainRoot = list2.item(0);
			mainRoot.appendChild(parent);
		} else {
			parent = list.item(0);
		}

		// Delete old rule information
		NodeList children = parent.getChildNodes();
		int len = children.getLength();
		for (int i = 0; i < len; i++) {
			parent.removeChild(children.item(0));
		}

		// Process settings
		processSettings(doc, parent);

		// Get result from document
		StreamResult result = getResultFromDocument(doc);

		// Print out
		String xmlString = result.getWriter().toString();
		writeFile(xmlString);
	}

	/**
	 * Process settings
	 * 
	 * @param doc
	 *            document
	 * @param root
	 *            node to insert this child to
	 */
	private void processSettings(Document doc, Node root) {
		// Decode files
		processDecodeFiles(doc, root);
	}

	/**
	 * Processes decode files
	 * 
	 * @param doc
	 *            document
	 * @param root
	 *            node to insert this child to
	 */
	private void processDecodeFiles(Document doc, Node root) {
		// Get decode files from the decode model
		if (TraceViewerGlobals.getDecodeProvider() != null) {
			List<TraceActivationComponentItem> components = TraceViewerGlobals
					.getDecodeProvider().getActivationInformation(false);

			// Loop through components
			for (int i = 0; i < components.size(); i++) {

				// Get previously opened decode file
				String decodeFile = components.get(i).getFilePath();

				// Check that file exists
				File file = new File(decodeFile);
				if (file.exists()) {

					Node decodeFileNode = doc.createElement(DECODEFILE_TAG);

					// Set as contents of the node
					decodeFileNode.setTextContent(decodeFile);

					// Set as child to parent
					root.appendChild(decodeFileNode);
				}
			}

			// Check if there are ZIP files
			List<String> zipFiles = ((OpenDecodeFileAction) TraceViewerGlobals
					.getTraceViewer().getView().getActionFactory()
					.getOpenDecodeFileAction()).getStartupFiles();

			for (int i = 0; i < zipFiles.size(); i++) {
				String decodeFile = zipFiles.get(i);

				// Check that file exists
				File file = new File(decodeFile);
				if (file.exists()) {

					boolean addZipFile = false;

					// Check that at least one of the components can be found
					// from the ZIP file
					try {
						ZipFile zipFile = new ZipFile(file);
						Enumeration<? extends ZipEntry> entries = zipFile
								.entries();

						// Go through ZIP file entries
						while (entries.hasMoreElements()) {
							String name = entries.nextElement().getName();

							// Go through components
							for (int j = 0; j < components.size(); j++) {
								TraceActivationComponentItem component = components
										.get(j);

								// Component file path matches the ZIP file
								// entry
								if (component.getFilePath().equals(name)) {
									addZipFile = true;
									break;
								}
							}
						}
					} catch (IOException e) {
					}

					// Only add ZIP file if at least one component was found
					// from it
					if (addZipFile) {
						Node decodeFileNode = doc.createElement(DECODEFILE_TAG);

						// Set as contents of the node
						decodeFileNode.setTextContent(decodeFile);

						// Set as child to parent
						root.appendChild(decodeFileNode);
					}
				}
			}
		}
	}
}
