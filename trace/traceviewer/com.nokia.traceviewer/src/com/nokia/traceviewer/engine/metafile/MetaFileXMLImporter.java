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
 * Metadata file Importer class
 *
 */
package com.nokia.traceviewer.engine.metafile;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Metadata file Importer class
 */
public class MetaFileXMLImporter {

	/**
	 * File path where to import
	 */
	private final String filePath;

	/**
	 * Constructor
	 * 
	 * @param filePath
	 *            file name where to export data
	 */
	public MetaFileXMLImporter(String filePath) {
		this.filePath = filePath;
	}

	/**
	 * Imports trace comment data
	 * 
	 * @param traceComments
	 *            trace comments map
	 * @return true if everything went fine
	 */
	public boolean importData(Map<Integer, String> traceComments) {
		boolean success = true;
		File file = new File(filePath);
		if (file.exists()) {
			Document doc = getDocument();

			// Get root node of trace comment rules
			NodeList list = doc
					.getElementsByTagName(MetaFileXMLConstants.TRACECOMMENTS_TAG);
			Node parent = list.item(0);

			// Go trough the comments
			if (parent != null) {
				NodeList comments = parent.getChildNodes();
				if (comments != null) {

					for (int i = 0; i < comments.getLength(); i++) {
						Node node = comments.item(i);
						if (node instanceof Element) {

							// Get line number
							NamedNodeMap itemAttributes = node.getAttributes();
							Attr lineAttr = (Attr) itemAttributes
									.getNamedItem(MetaFileXMLConstants.LINE_TAG);
							String line = lineAttr.getValue();

							// Get comment
							String comment = node.getTextContent();

							// Insert to the comments map
							traceComments.put(Integer.valueOf(line), comment);
						}
					}
				}
			}
		}

		return success;
	}

	/**
	 * Gets DOM document for the file path
	 * 
	 * @return document
	 */
	protected Document getDocument() {
		DocumentBuilderFactory docFactory = DocumentBuilderFactory
				.newInstance();
		DocumentBuilder docBuilder = null;
		try {
			docBuilder = docFactory.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		}

		// Get the document
		Document doc = null;
		try {
			if (docBuilder != null) {
				File file = new File(filePath);
				if (file.exists()) {
					doc = docBuilder.parse(filePath);
				}
			}
		} catch (SAXException e1) {
			e1.printStackTrace();

		} catch (IOException e1) {
			e1.printStackTrace();
		}
		return doc;
	}
}
