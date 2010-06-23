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
 * Trace activation information Exporter class
 *
 */
package com.nokia.traceviewer.engine.activation;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.util.List;

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
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.nokia.traceviewer.engine.TraceViewerGlobals;

/**
 * Trace activation information Exporter class
 * 
 */
public class TraceActivationXMLExporter {

	/**
	 * Components that are exported
	 */
	private final List<TraceActivationComponentItem> components;

	/**
	 * File path to use when exporting
	 */
	private final String filePath;

	/**
	 * Configuration name to export
	 */
	private final String configurationName;

	/**
	 * Output stream writer
	 */
	private OutputStreamWriter out;

	/**
	 * Constructor
	 * 
	 * @param components
	 *            components
	 * @param configurationFileName
	 *            file name where to export data
	 * @param configurationName
	 *            name of the new configuration
	 */
	public TraceActivationXMLExporter(
			List<TraceActivationComponentItem> components,
			String configurationFileName, String configurationName) {
		this.components = components;
		this.configurationName = configurationName;
		this.filePath = configurationFileName;

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

			// Get root node of activation rules
			NodeList list = doc
					.getElementsByTagName(TraceActivationXMLConstants.ACTIVATIONS_TAG);

			// Doesn't exist, create
			if (list.getLength() == 0) {
				parent = doc
						.createElement(TraceActivationXMLConstants.ACTIVATIONS_TAG);
				try {
					doc.appendChild(parent);
				} catch (Exception e) {
					parent = null;
				}
			} else {
				parent = list.item(0);
			}

			// Search if given activation name already exists and delete it
			if (parent != null) {
				NodeList activations = parent.getChildNodes();
				for (int i = 0; i < activations.getLength(); i++) {
					if (activations.item(i) instanceof Element) {
						NamedNodeMap itemAttributes = activations.item(i)
								.getAttributes();

						// Get name
						Attr nameAttr = (Attr) itemAttributes
								.getNamedItem(TraceActivationXMLConstants.NAME_TAG);
						String name = nameAttr.getValue();
						if (name.equals(configurationName)) {
							parent.removeChild(activations.item(i));
							break;
						}
					}
				}

				// Create new activation and process components to it
				Node newActivation = doc
						.createElement(TraceActivationXMLConstants.ACTIVATION_TAG);
				parent.appendChild(newActivation);

				// Set name
				NamedNodeMap itemAttributes = newActivation.getAttributes();
				Attr name = doc
						.createAttribute(TraceActivationXMLConstants.NAME_TAG);
				name.setValue(configurationName);
				itemAttributes.setNamedItem(name);
				for (int j = 0; j < components.size(); j++) {
					processComponent(components.get(j), doc, newActivation);
				}

				// Get result from document
				StreamResult result = getResultFromDocument(doc);

				// Print out
				String xmlString = result.getWriter().toString();
				writeFile(xmlString);
				succeeded = true;
			} else {
				// If comes here, root node "activation" was not found and
				// couldn't be created
				String cannotSave = Messages
						.getString("TraceActivationXMLExporter.CannotSaveToTheFile"); //$NON-NLS-1$
				TraceViewerGlobals.getTraceViewer().getDialogs()
						.showErrorMessage(cannotSave);
			}
		} else {
			// If comes here, file was not found
			String fileNotFound = Messages
					.getString("TraceActivationXMLExporter.CannotSaveToTheFile"); //$NON-NLS-1$
			TraceViewerGlobals.getTraceViewer().getDialogs().showErrorMessage(
					fileNotFound);
		}

		return succeeded;
	}

	/**
	 * Removes given configuration
	 */
	public void remove() {
		Document doc = getDocument();
		Node parent;

		if (doc != null) {

			// Get root node of activation rules
			NodeList list = doc
					.getElementsByTagName(TraceActivationXMLConstants.ACTIVATIONS_TAG);
			parent = list.item(0);

			// Search if given activation name already exists and delete it
			if (parent != null) {
				NodeList activations = parent.getChildNodes();
				for (int i = 0; i < activations.getLength(); i++) {
					if (activations.item(i) instanceof Element) {
						NamedNodeMap itemAttributes = activations.item(i)
								.getAttributes();

						// Get name
						Attr nameAttr = (Attr) itemAttributes
								.getNamedItem(TraceActivationXMLConstants.NAME_TAG);
						String name = nameAttr.getValue();
						if (name.equals(configurationName)) {
							parent.removeChild(activations.item(i));
							break;
						}
					}
				}
			}

			// Get result from document
			StreamResult result = getResultFromDocument(doc);

			// Print out
			String xmlString = result.getWriter().toString();
			writeFile(xmlString);
		}
	}

	/**
	 * Process component
	 * 
	 * @param item
	 *            component to process
	 * @param doc
	 *            document
	 * @param root
	 *            node to insert this child to
	 */
	private void processComponent(TraceActivationComponentItem item,
			Document doc, Node root) {
		// Process through the component
		Node newItem = null;
		newItem = doc.createElement(TraceActivationXMLConstants.COMPONENT_TAG);

		// Get attributes
		NamedNodeMap itemAttributes = newItem.getAttributes();

		// Set ID
		Attr id = doc.createAttribute(TraceActivationXMLConstants.ID_TAG);
		id.setValue(String.valueOf(item.getId()));
		itemAttributes.setNamedItem(id);

		// Set as child to parent
		root.appendChild(newItem);

		// Loop through groups in this component
		List<TraceActivationGroupItem> groups = item.getGroups();
		for (int i = 0; i < groups.size(); i++) {

			// Create new group node
			Node newGroup = doc
					.createElement(TraceActivationXMLConstants.GROUP_TAG);

			// Get attributes
			NamedNodeMap attributes = newGroup.getAttributes();

			// Set ID
			Attr groupId = doc
					.createAttribute(TraceActivationXMLConstants.ID_TAG);
			groupId.setValue(String.valueOf(groups.get(i).getId()));
			attributes.setNamedItem(groupId);

			// Set activation value
			if (groups.get(i).isActivated()) {
				newGroup.setTextContent(TraceActivationXMLConstants.YES_TAG);
			} else {
				newGroup.setTextContent(TraceActivationXMLConstants.NO_TAG);
			}

			// Set as child to parent
			newItem.appendChild(newGroup);
		}
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

			out.write(TraceActivationXMLConstants.XML_HEADER);
			out.write(TraceActivationXMLConstants.FILE_START);
			out.write(TraceActivationXMLConstants.ACTIVATIONS_START);
			out.write(TraceActivationXMLConstants.ACTIVATIONS_END);
			out.write(TraceActivationXMLConstants.FILE_END);
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
		tf.setAttribute("indent-number", Integer.valueOf((indentSize))); //$NON-NLS-1$
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
