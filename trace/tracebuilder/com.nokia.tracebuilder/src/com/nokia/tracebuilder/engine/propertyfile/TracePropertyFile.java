/*
* Copyright (c) 2007 Nokia Corporation and/or its subsidiary(-ies). 
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
* Trace property file properties
*
*/
package com.nokia.tracebuilder.engine.propertyfile;

import java.util.HashMap;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.nokia.tracebuilder.engine.TraceBuilderConfiguration;
import com.nokia.tracebuilder.engine.TraceBuilderGlobals;
import com.nokia.tracebuilder.model.Trace;
import com.nokia.tracebuilder.model.TraceConstantTable;
import com.nokia.tracebuilder.model.TraceConstantTableEntry;
import com.nokia.tracebuilder.model.TraceGroup;
import com.nokia.tracebuilder.model.TraceModelListener;
import com.nokia.tracebuilder.model.TraceObject;
import com.nokia.tracebuilder.project.TraceProjectFile;

/**
 * Trace property file properties
 * 
 */
final class TracePropertyFile extends TraceProjectFile {

	/**
	 * Title shown in UI
	 */
	private static final String PROPERTY_FILE = Messages
			.getString("TracePropertyFile.Title"); //$NON-NLS-1$

	/**
	 * Property file document
	 */
	private Document document;

	/**
	 * Group properties
	 */
	private HashMap<String, TraceObjectPropertyListImpl> groupProperties;

	/**
	 * Trace properties
	 */
	private HashMap<String, TraceObjectPropertyListImpl> traceProperties;

	/**
	 * Creates a new property file
	 * 
	 * @param filePath
	 *            path to the file
	 * @param document
	 *            the document representing the property file
	 */
	TracePropertyFile(String filePath, Document document) {
		super(filePath, ""); //$NON-NLS-1$
		this.document = document;
	}

	/**
	 * Stores the group property cache. The properties are associated to groups
	 * when they are created.
	 * 
	 * @param groupProperties
	 *            properties for trace groups
	 * @param traceProperties
	 *            properties for traces
	 */
	void setProperties(
			HashMap<String, TraceObjectPropertyListImpl> groupProperties,
			HashMap<String, TraceObjectPropertyListImpl> traceProperties) {
		this.groupProperties = groupProperties;
		this.traceProperties = traceProperties;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.project.TraceProjectFile#getFileExtension()
	 */
	@Override
	protected String getFileExtension() {
		return PropertyFileConstants.PROPERTY_FILE_NAME;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.project.TraceProjectFile#getTitle()
	 */
	@Override
	public String getTitle() {
		return PROPERTY_FILE;
	}

	/**
	 * Gets the document
	 * 
	 * @return the document
	 */
	Document getDocument() {
		return document;
	}

	/**
	 * Adds an element to the property file document
	 * 
	 * @param object
	 *            the object to be added
	 */
	void addElement(TraceObject object) {
		if (object instanceof TraceConstantTableEntry) {
			addConstantTableEntry((TraceConstantTableEntry) object);
		} else if (object instanceof TraceConstantTable) {
			addConstantTable((TraceConstantTable) object);
		}
	}

	/**
	 * Removes an element from the property file document
	 * 
	 * @param object
	 *            the object to be added
	 */
	void removeElement(TraceObject object) {
		DocumentElementWrapper wrapper = object
				.getExtension(DocumentElementWrapper.class);
		if (wrapper != null) {
			Element e = wrapper.getElement();
			e.getParentNode().removeChild(e);
		} else {
			if (TraceBuilderConfiguration.ASSERTIONS_ENABLED) {
				TraceBuilderGlobals.getEvents().postAssertionFailed(
						"Element wrapper was missing", //$NON-NLS-1$
						null);
			}
		}
	}

	/**
	 * Updates an element from the property file document
	 * 
	 * @param object
	 *            the object to be updated
	 */
	void updateElement(TraceObject object) {
		DocumentElementWrapper wrapper = object
				.getExtension(DocumentElementWrapper.class);
		if (wrapper != null) {
			Element e = wrapper.getElement();
			if (object instanceof TraceConstantTableEntry) {
				updateConstantTableEntry(e, (TraceConstantTableEntry) object);
			} else if (object instanceof TraceConstantTable) {
				updateConstantTable(e, (TraceConstantTable) object);
			}
		} else {
			if (TraceBuilderConfiguration.ASSERTIONS_ENABLED) {
				TraceBuilderGlobals.getEvents().postAssertionFailed(
						"Element wrapper was missing", //$NON-NLS-1$
						null);
			}
		}
	}

	/**
	 * Adds a constant table to the document
	 * 
	 * @param table
	 *            the table
	 */
	void addConstantTable(TraceConstantTable table) {
		Element rootElement = getRoot();
		Element tableElement = document
				.createElement(PropertyFileConstants.ENUM_ELEMENT);
		updateConstantTable(tableElement, table);
		rootElement.appendChild(tableElement);
		// The element reference is stored into the model
		DocumentElementWrapper wrapper = new DocumentElementWrapper(
				tableElement);
		table.addExtension(wrapper);
		for (TraceConstantTableEntry entry : table) {
			addConstantTableEntry(tableElement, entry);
		}
	}

	/**
	 * Adds a constant table entry to the document
	 * 
	 * @param object
	 *            the table entry
	 */
	void addConstantTableEntry(TraceConstantTableEntry object) {
		Element tableElement = findNamedElement(object.getTable().getName(),
				PropertyFileConstants.ENUM_ELEMENT);
		if (tableElement != null) {
			addConstantTableEntry(tableElement, object);
		} else {
			TraceBuilderGlobals.getEvents().postAssertionFailed(
					"Enum element was missing from property file", //$NON-NLS-1$
					null);
		}
	}

	/**
	 * Adds a constant table entry to given element
	 * 
	 * @param tableElement
	 *            the constant table element
	 * @param entry
	 *            the constant table entry
	 */
	private void addConstantTableEntry(Element tableElement,
			TraceConstantTableEntry entry) {
		Element entryElement = document
				.createElement(PropertyFileConstants.VALUE_ELEMENT);
		updateConstantTableEntry(entryElement, entry);
		tableElement.appendChild(entryElement);
		// The element reference is stored into the model
		DocumentElementWrapper wrapper = new DocumentElementWrapper(
				entryElement);
		entry.addExtension(wrapper);
	}

	/**
	 * Updates a constant table
	 * 
	 * @param element
	 *            the element to be updated
	 * @param table
	 *            the constant table
	 */
	private void updateConstantTable(Element element, TraceConstantTable table) {
		// TODO: Table type and size are not supported
		element.setAttribute(PropertyFileConstants.NAME_ATTRIBUTE, table
				.getName());
	}

	/**
	 * Updates a constant table entry
	 * 
	 * @param element
	 *            the element to be updated
	 * @param entry
	 *            the table entry
	 */
	private void updateConstantTableEntry(Element element,
			TraceConstantTableEntry entry) {
		element.setAttribute(PropertyFileConstants.ID_ATTRIBUTE, String
				.valueOf(entry.getID()));
		element.setTextContent(entry.getName());
	}

	/**
	 * Finds a named element
	 * 
	 * @param name
	 *            the element name
	 * @param type
	 *            the element type
	 * @return the element
	 */
	private Element findNamedElement(String name, String type) {
		Element element = null;
		Element rootElement = getRoot();
		NodeList list = rootElement.getChildNodes();
		for (int i = 0; element == null && i < list.getLength(); i++) {
			Node node = list.item(i);
			if (node.getNodeType() == Node.ELEMENT_NODE) {
				if (node.getNodeName().equals(type)) {
					String nameAttr = ((Element) node)
							.getAttribute(PropertyFileConstants.NAME_ATTRIBUTE);
					if (nameAttr != null && nameAttr.equals(name)) {
						element = (Element) node;
					}
				}
			}
		}
		return element;
	}

	/**
	 * Gets the root element and creates if not found
	 * 
	 * @return the root
	 */
	private Element getRoot() {
		Element rootElement = PropertyFileUtils.findRoot(document);
		if (rootElement == null) {
			rootElement = document
					.createElement(PropertyFileConstants.ROOT_ELEMENT);
			document.appendChild(rootElement);
		}
		return rootElement;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.project.TraceProjectFile#
	 *      objectCreationComplete(com.nokia.tracebuilder.model.TraceObject)
	 */
	@Override
	public void objectCreationComplete(TraceObject object) {
		super.objectCreationComplete(object);
		linkToProperty(object);
	}

	/**
	 * Links an object to properties
	 * 
	 * @param object
	 *            the object
	 */
	private void linkToProperty(TraceObject object) {
		// Links the cached property lists to objects
		if (object instanceof TraceGroup && groupProperties != null) {
			TraceObjectPropertyListImpl propertyList = groupProperties
					.get(object.getName());
			if (propertyList != null) {
				object.addExtension(propertyList);
			}
		} else if (object instanceof Trace && traceProperties != null) {
			TraceObjectPropertyListImpl propertyList = traceProperties
					.get(object.getName());
			if (propertyList != null) {
				object.addExtension(propertyList);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.project.TraceProjectFile#
	 *      objectRemoved(com.nokia.tracebuilder.model.TraceObject,
	 *      com.nokia.tracebuilder.model.TraceObject)
	 */
	@Override
	public void objectRemoved(TraceObject owner, TraceObject object) {
		super.objectRemoved(owner, object);
		// Removes the properties from the object
		object.removeExtensions(TraceObjectPropertyListImpl.class);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.project.TraceProjectFile#
	 *      propertyUpdated(com.nokia.tracebuilder.model.TraceObject, int)
	 */
	@Override
	public void propertyUpdated(TraceObject object, int property) {
		super.propertyUpdated(object, property);
		if (property == TraceModelListener.NAME) {
			// Removes the old properties from the object and links to new ones
			object.removeExtensions(TraceObjectPropertyListImpl.class);
			linkToProperty(object);
		}
	}

}
