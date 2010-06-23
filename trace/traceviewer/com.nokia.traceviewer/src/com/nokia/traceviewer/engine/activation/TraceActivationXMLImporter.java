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
 * Trace activation information Importer class
 *
 */
package com.nokia.traceviewer.engine.activation;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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

import com.nokia.traceviewer.engine.TraceViewerGlobals;

/**
 * Trace activation information Importer class
 * 
 */
public class TraceActivationXMLImporter {

	/**
	 * Category for trigger events
	 */
	private static final String TRIGGER_CATEGORY = Messages
			.getString("TraceActivationXMLImporter.TriggerCategory"); //$NON-NLS-1$

	/**
	 * File path where to import
	 */
	private final String filePath;

	/**
	 * Constructor
	 * 
	 * @param configurationFileName
	 *            file name where to export data
	 */
	public TraceActivationXMLImporter(String configurationFileName) {
		this.filePath = configurationFileName;
	}

	/**
	 * Imports configuration names found from this XML file
	 * 
	 * @return the configuration names
	 */
	public String[] importConfigurationNames() {
		ArrayList<String> confNameList = new ArrayList<String>();
		String[] confNameArr = null;
		File file = new File(filePath);
		if (file.exists() && file.length() > 0) {
			Document doc = getDocument();

			// Get activations node from the XML file
			NodeList list = doc
					.getElementsByTagName(TraceActivationXMLConstants.ACTIVATIONS_TAG);
			Node parent = list.item(0);

			// Go through activations
			if (parent != null) {
				NodeList activations = parent.getChildNodes();
				if (activations != null) {
					for (int i = 0; i < activations.getLength(); i++) {
						if (activations.item(i) instanceof Element) {
							NamedNodeMap itemAttributes = activations.item(i)
									.getAttributes();

							// Get name
							Attr nameAttr = (Attr) itemAttributes
									.getNamedItem(TraceActivationXMLConstants.NAME_TAG);
							String name = nameAttr.getValue();
							confNameList.add(name);
						}
					}
				} else {
					// If comes here, activation information was not found
				}
			} else {
				// If comes here, this XML file isn't the correct format
				String xmlCorrupted = Messages
						.getString("TraceActivationXMLImporter.XMLCorrupted"); //$NON-NLS-1$
				TraceViewerGlobals.getTraceViewer().getDialogs()
						.showErrorMessage(xmlCorrupted);
			}
		}

		// Get items from list to array
		confNameArr = new String[confNameList.size()];
		for (int j = 0; j < confNameList.size(); j++) {
			confNameArr[j] = confNameList.get(j);
		}

		return confNameArr;
	}

	/**
	 * Inserts components from configuration to the given array
	 * 
	 * @param components
	 *            components
	 * @param configurationName
	 *            configuration name
	 */
	public void createComponentListFromConfigurationName(
			List<TraceActivationComponentItem> components,
			String configurationName) {
		File file = new File(filePath);
		if (file.exists()) {
			Document doc = getDocument();

			// Get activations node from the XML file
			NodeList list = doc
					.getElementsByTagName(TraceActivationXMLConstants.ACTIVATIONS_TAG);
			Node parent = list.item(0);

			// Go trough activations
			if (parent != null) {
				NodeList activations = parent.getChildNodes();
				if (activations != null) {
					boolean found = false;
					for (int i = 0; i < activations.getLength(); i++) {
						if (activations.item(i) instanceof Element) {
							NamedNodeMap itemAttributes = activations.item(i)
									.getAttributes();

							// Get name
							Attr nameAttr = (Attr) itemAttributes
									.getNamedItem(TraceActivationXMLConstants.NAME_TAG);
							String name = nameAttr.getValue();
							if (name.equals(configurationName)) {
								NodeList componentList = activations.item(i)
										.getChildNodes();
								for (int j = 0; j < componentList.getLength(); j++) {
									if (componentList.item(j) instanceof Element) {
										processXMLTriggerRule(componentList
												.item(j), components);
									}
								}
								found = true;
								break;
							}
						}
					}
					if (!found) {
						// Specific activation information was not found
						String confReference = Messages
								.getString("TraceActivationXMLImporter.ConfNotFound"); //$NON-NLS-1$
						TraceViewerGlobals.postErrorEvent(confReference,
								TRIGGER_CATEGORY, configurationName);
					}
				} else {
					// Activation informations were not found
					String confNotFound = Messages
							.getString("TraceActivationXMLImporter.ConfNotFound"); //$NON-NLS-1$
					TraceViewerGlobals.postErrorEvent(confNotFound,
							TRIGGER_CATEGORY, configurationName);
				}
			} else {
				// XML file is corrupted
				String xmlCorrupted = Messages
						.getString("TraceActivationXMLImporter.XMLCorrupted"); //$NON-NLS-1$
				TraceViewerGlobals.postErrorEvent(xmlCorrupted,
						TRIGGER_CATEGORY, filePath);
			}
		} else {
			// File was not found
			String fileNotFound = Messages
					.getString("TraceActivationXMLImporter.FileNotFound"); //$NON-NLS-1$
			TraceViewerGlobals.postErrorEvent(fileNotFound, TRIGGER_CATEGORY,
					filePath);
		}
	}

	private void processXMLTriggerRule(Node item,
			List<TraceActivationComponentItem> components) {
		NamedNodeMap itemAttributes = item.getAttributes();

		// Get id
		Attr idAttr = (Attr) itemAttributes
				.getNamedItem(TraceActivationXMLConstants.ID_TAG);
		int id = Integer.parseInt(idAttr.getValue());

		// Create new Activation item
		TraceActivationComponentItem newActivationItem = new TraceActivationComponentItem(
				id, ""); //$NON-NLS-1$
		components.add(newActivationItem);

		// Get groups from Activation item
		List<TraceActivationGroupItem> groups = newActivationItem.getGroups();

		// Get groups from XML item
		NodeList XMLgroups = item.getChildNodes();

		// Process groups from the Node item
		for (int i = 0; i < XMLgroups.getLength(); i++) {
			if (XMLgroups.item(i) instanceof Element) {
				processGroups(XMLgroups.item(i), groups, newActivationItem);
			}
		}
	}

	private void processGroups(Node groupItem,
			List<TraceActivationGroupItem> groups,
			TraceActivationComponentItem parent) {
		NamedNodeMap itemAttributes = groupItem.getAttributes();

		// Get id
		Attr idAttr = (Attr) itemAttributes
				.getNamedItem(TraceActivationXMLConstants.ID_TAG);
		int id = Integer.parseInt(idAttr.getValue());

		// Create new group
		TraceActivationGroupItem newGroup = new TraceActivationGroupItem(
				parent, id, ""); //$NON-NLS-1$
		groups.add(newGroup);

		// Check activated status
		String activated = groupItem.getTextContent();
		if (activated.equals(TraceActivationXMLConstants.YES_TAG)) {
			newGroup.setActivated(true);
		}
	}

	/**
	 * Imports activation data
	 * 
	 * @param components
	 *            components
	 * @param configurationName
	 *            name of the configuration
	 * @param changedComponents
	 *            list of changed components to use when activating
	 * @return true if everything went fine
	 */
	public boolean importData(List<TraceActivationComponentItem> components,
			String configurationName,
			List<TraceActivationComponentItem> changedComponents) {
		boolean success = true;
		File file = new File(filePath);
		if (file.exists()) {
			Document doc = getDocument();

			// Get activations node from the XML file
			NodeList list = doc
					.getElementsByTagName(TraceActivationXMLConstants.ACTIVATIONS_TAG);
			Node parent = list.item(0);

			// Go trough activations
			if (parent != null) {
				NodeList activations = parent.getChildNodes();
				if (activations != null) {
					boolean found = false;
					for (int i = 0; i < activations.getLength(); i++) {
						if (activations.item(i) instanceof Element) {
							NamedNodeMap itemAttributes = activations.item(i)
									.getAttributes();

							// Get name
							Attr nameAttr = (Attr) itemAttributes
									.getNamedItem(TraceActivationXMLConstants.NAME_TAG);
							String name = nameAttr.getValue();
							if (name.equals(configurationName)) {
								NodeList componentList = activations.item(i)
										.getChildNodes();
								for (int j = 0; j < componentList.getLength(); j++) {
									if (componentList.item(j) instanceof Element) {
										processXMLComponent(componentList
												.item(j), components,
												changedComponents);
									}
								}
								found = true;
								break;
							}
						}
					}
					if (!found) {
						// Specific activation information was not found
						String confReference = Messages
								.getString("TraceActivationXMLImporter.ConfNotFound"); //$NON-NLS-1$
						TraceViewerGlobals.getTraceViewer().getDialogs()
								.showErrorMessage(confReference);
						success = false;
					}
				} else {
					// Activation information was not found
					String confNotFound = Messages
							.getString("TraceActivationXMLImporter.ConfNotFound"); //$NON-NLS-1$
					TraceViewerGlobals.getTraceViewer().getDialogs()
							.showErrorMessage(confNotFound);
					success = false;
				}
			} else {
				// XML file is corrupted
				String xmlCorrupted = Messages
						.getString("TraceActivationXMLImporter.XMLCorrupted"); //$NON-NLS-1$
				TraceViewerGlobals.getTraceViewer().getDialogs()
						.showErrorMessage(xmlCorrupted);
				success = false;
			}
		} else {
			// File was not found
			String fileNotFound = Messages
					.getString("TraceActivationXMLImporter.FileNotFound"); //$NON-NLS-1$
			TraceViewerGlobals.getTraceViewer().getDialogs().showErrorMessage(
					fileNotFound);
			success = false;
		}
		return success;
	}

	/**
	 * Process XML component
	 * 
	 * @param component
	 *            XML component to process
	 * @param components
	 *            all components loaded to model
	 * @param changedComponents
	 *            changed components list
	 */
	private void processXMLComponent(Node component,
			List<TraceActivationComponentItem> components,
			List<TraceActivationComponentItem> changedComponents) {
		NamedNodeMap itemAttributes = component.getAttributes();

		// Get id
		Attr idAttr = (Attr) itemAttributes
				.getNamedItem(TraceActivationXMLConstants.ID_TAG);
		int id = Integer.parseInt(idAttr.getValue());

		// Find component with correct id
		for (int i = 0; i < components.size(); i++) {
			if (components.get(i).getId() == id) {
				processGroups(components.get(i), component, changedComponents);
				break;
			}
		}
	}

	/**
	 * Process groups from this component
	 * 
	 * @param item
	 *            real component item
	 * @param component
	 *            XML component node
	 * @param changedComponents
	 *            changed components list
	 */
	private void processGroups(TraceActivationComponentItem item,
			Node component, List<TraceActivationComponentItem> changedComponents) {
		// Get groups from real component
		List<TraceActivationGroupItem> groups = item.getGroups();

		// Get groups from xml component
		NodeList XMLgroups = component.getChildNodes();

		// Go trough all groups from xml component
		for (int i = 0; i < XMLgroups.getLength(); i++) {
			if (XMLgroups.item(i) instanceof Element) {
				NamedNodeMap itemAttributes = XMLgroups.item(i).getAttributes();

				// Get id
				Attr idAttr = (Attr) itemAttributes
						.getNamedItem(TraceActivationXMLConstants.ID_TAG);
				int id = Integer.parseInt(idAttr.getValue());

				// Find this id from real groups
				for (int j = 0; j < groups.size(); j++) {
					TraceActivationGroupItem group = groups.get(j);
					if (group.getId() == id) {
						// Read the activation status
						String activated = XMLgroups.item(i).getTextContent();

						// Activate a group if it wasn't already activated
						if (activated
								.equals(TraceActivationXMLConstants.YES_TAG)) {
							if (!group.isActivated()) {
								group.setActivated(true);

								// Add this components to the changed components
								// list
								if (!changedComponents.contains(group
										.getParent())) {
									changedComponents.add(group.getParent());
								}
							}
						} else {

							// Deactivate a group if it was activated
							if (group.isActivated()) {
								group.setActivated(false);

								// Add this to the changed components
								// list
								if (!changedComponents.contains(group
										.getParent())) {
									changedComponents.add(group.getParent());
								}
							}
						}
					}
				}
			}
		}
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

			// Show file incorrect message
			String fileIncorrect = Messages
					.getString("TraceActivationXMLImporter.FileIncorrect"); //$NON-NLS-1$
			TraceViewerGlobals.getTraceViewer().getDialogs().showErrorMessage(
					fileIncorrect);

		} catch (IOException e1) {
			e1.printStackTrace();
		}
		return doc;
	}
}
