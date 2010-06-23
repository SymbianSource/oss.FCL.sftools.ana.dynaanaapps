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
 * Base class for all configuration exporters
 *
 */
package com.nokia.traceviewer.engine.preferences;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringWriter;

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

import org.eclipse.core.runtime.IPath;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import com.nokia.traceviewer.TraceViewerPlugin;
import com.nokia.traceviewer.dialog.treeitem.TreeItem;

/**
 * Base class for all configuration exporters
 * 
 */
public abstract class XMLBaseConfigurationExporter implements
		XMLConfigurationFileConstants {

	/**
	 * Root Treeitem
	 */
	protected TreeItem root;

	/**
	 * File path
	 */
	private String filePath;

	/**
	 * Output writer
	 */
	private OutputStreamWriter out;

	/**
	 * Constructor
	 * 
	 * @param root
	 *            root of the elements
	 * @param configurationFileName
	 *            file name where to export data
	 * @param pathRelative
	 *            tells is the path relative
	 */
	public XMLBaseConfigurationExporter(TreeItem root,
			String configurationFileName, boolean pathRelative) {
		this.root = root;

		// If path is relative, get plugins metadata folder
		if (pathRelative) {
			IPath path = TraceViewerPlugin.getDefault().getStateLocation()
					.append(configurationFileName);
			filePath = path.toOSString();
		} else {
			filePath = configurationFileName;
		}
		createFile();
	}

	/**
	 * Creates the file
	 */
	public void createFile() {
		File file = new File(filePath);

		// File doesn't exist, create it
		if (!file.exists()) {
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
			// Open an Output Stream Writer and set encoding
			OutputStream fout = new FileOutputStream(filePath);
			OutputStream bout = new BufferedOutputStream(fout);
			out = new OutputStreamWriter(bout, "UTF-8"); //$NON-NLS-1$

			// Write dummy Dictionary XML file
			out.write(XML_HEADER);
			out.write(FILE_START);
			out.write(MAINCONFIGURATIONS_START);
			out.write(GENERAL_CONFIGURATION_START);
			out.write(GENERAL_CONFIGURATION_END);
			out.write(COLOR_CONFIGURATION_START);
			out.write(COLOR_CONFIGURATION_END);
			out.write(FILTER_CONFIGURATION_START);
			out.write(FILTER_CONFIGURATION_END);
			out.write(LINECOUNT_CONFIGURATION_START);
			out.write(LINECOUNT_CONFIGURATION_END);
			out.write(VARIABLETRACING_CONFIGURATION_START);
			out.write(VARIABLETRACING_CONFIGURATION_END);
			out.write(TRIGGER_CONFIGURATION_START);
			out.write(TRIGGER_CONFIGURATION_END);
			out.write(MAINCONFIGURATIONS_END);
			out.write(FILE_END);

			// Flush and close the stream
			out.flush();
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
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
				doc = docBuilder.parse(file);
			}
		} catch (SAXException e1) {
			e1.printStackTrace();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		return doc;
	}

}
