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
 * Metadata file Exporter class
 *
 */
package com.nokia.traceviewer.engine.metafile;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.nokia.traceviewer.engine.TraceViewerGlobals;

/**
 * Metadata file Exporter class
 * 
 */
public class MetaFileXMLExporter {

	/**
	 * File path to use when exporting
	 */
	private final String filePath;

	/**
	 * Output stream writer
	 */
	private OutputStreamWriter out;

	/**
	 * Constructor
	 * 
	 * @param filePath
	 *            file path where to export data
	 */
	public MetaFileXMLExporter(String filePath) {
		this.filePath = filePath;

		createFile();
	}

	/**
	 * Exports data
	 * 
	 * @return true if exporting succeeds
	 */
	public boolean export() {
		boolean succeeded = false;
		Document doc = getDocument();
		Node parent;

		if (doc != null) {

			// Get root node of trace comment rules
			NodeList list = doc
					.getElementsByTagName(MetaFileXMLConstants.TRACECOMMENTS_TAG);

			// Doesn't exist, create
			if (list.getLength() == 0) {
				parent = doc
						.createElement(MetaFileXMLConstants.TRACECOMMENTS_TAG);
				try {
					doc.appendChild(parent);
				} catch (Exception e) {
					parent = null;
				}
			} else {
				parent = list.item(0);
			}

			// Delete old trace comments
			if (parent != null) {
				NodeList filterChilds = parent.getChildNodes();
				int len = filterChilds.getLength();
				for (int i = 0; i < len; i++) {
					parent.removeChild(filterChilds.item(0));
				}

				// Get trace comments
				Map<Integer, String> traceComments = TraceViewerGlobals
						.getTraceViewer().getDataProcessorAccess()
						.getTraceCommentHandler().getComments();

				// Loop through the trace comments
				Iterator<Entry<Integer, String>> it = traceComments.entrySet()
						.iterator();
				while (it.hasNext()) {
					Entry<Integer, String> entry = it.next();

					Node newItem = null;
					newItem = doc
							.createElement(MetaFileXMLConstants.COMMENT_TAG);
					NamedNodeMap itemAttributes = newItem.getAttributes();
					Attr line = doc
							.createAttribute(MetaFileXMLConstants.LINE_TAG);
					line.setValue(String.valueOf(entry.getKey()));
					itemAttributes.setNamedItem(line);
					newItem.setTextContent(entry.getValue());
					parent.appendChild(newItem);
				}

				// Get result from document
				StreamResult result = getResultFromDocument(doc);

				// Print out
				String xmlString = result.getWriter().toString();
				writeFile(xmlString);
				succeeded = true;
			}
		}

		return succeeded;
	}

	/**
	 * Creates the file
	 */
	public void createFile() {
		File file = new File(filePath);

		// File doesn't exist, create it
		if (!file.exists() || file.length() == 0) {
			createFileSkeleton();
		} else {
			// File exists
		}
	}

	/**
	 * Creates XML file skeleton
	 */
	private void createFileSkeleton() {
		try {
			// Open an Output Stream Writer to set encoding
			OutputStream fout = new FileOutputStream(filePath);
			OutputStream bout = new BufferedOutputStream(fout);
			out = new OutputStreamWriter(bout, "UTF-8"); //$NON-NLS-1$

			out.write(MetaFileXMLConstants.XML_HEADER);
			out.write(MetaFileXMLConstants.FILE_START);
			out.write(MetaFileXMLConstants.TRACECOMMENTS_START);
			out.write(MetaFileXMLConstants.TRACECOMMENTS_END);
			out.write(MetaFileXMLConstants.FILE_END);
			out.flush();
			out.close();
		} catch (IOException e) {
		}

	}

	/**
	 * Writes ready XML string to file
	 * 
	 * @param xmlString
	 */
	protected void writeFile(String xmlString) {

		try {
			// Open an Output Stream Writer to set encoding
			OutputStream fout = new FileOutputStream(filePath);
			OutputStream bout = new BufferedOutputStream(fout);
			out = new OutputStreamWriter(bout, "UTF-8"); //$NON-NLS-1$

		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
			out.write(xmlString);
			out.flush();
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Gets result from document
	 * 
	 * @param doc
	 *            document
	 * @return result
	 */
	protected StreamResult getResultFromDocument(Document doc) {
		TransformerFactory tf = TransformerFactory.newInstance();
		int indentSize = 2;
		tf.setAttribute("indent-number", Integer.valueOf(indentSize)); //$NON-NLS-1$
		Transformer transformer = null;
		try {
			transformer = tf.newTransformer();
		} catch (TransformerConfigurationException e1) {
			e1.printStackTrace();
		}
		if (transformer != null) {
			transformer.setOutputProperty(OutputKeys.INDENT, "yes"); //$NON-NLS-1$
		}

		// initialize StreamResult with File object to save to file
		StreamResult result = new StreamResult(new StringWriter());
		DOMSource source = new DOMSource(doc);
		try {
			if (transformer != null) {
				transformer.transform(source, result);
			}
		} catch (TransformerException e) {
			e.printStackTrace();
		}
		return result;
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
