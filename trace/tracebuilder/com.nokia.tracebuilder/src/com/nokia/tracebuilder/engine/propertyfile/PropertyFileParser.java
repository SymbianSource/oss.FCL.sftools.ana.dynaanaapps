/*
* Copyright (c) 2008 Nokia Corporation and/or its subsidiary(-ies). 
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
* Parser for trace property files
*
*/
package com.nokia.tracebuilder.engine.propertyfile;

import java.util.HashMap;

import javax.xml.parsers.DocumentBuilder;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.nokia.tracebuilder.engine.TraceBuilderErrorMessages;
import com.nokia.tracebuilder.engine.TraceBuilderGlobals;
import com.nokia.tracebuilder.engine.TraceBuilderErrorCodes.StringErrorParameters;
import com.nokia.tracebuilder.engine.TraceBuilderErrorCodes.TraceBuilderErrorCode;
import com.nokia.tracebuilder.model.TraceBuilderException;
import com.nokia.tracebuilder.model.TraceModel;
import com.nokia.tracebuilder.project.ProjectFileParser;

/**
 * Parser for trace property files
 * 
 */
final class PropertyFileParser extends ProjectFileParser {

	/**
	 * DOM document representing the property file
	 */
	private Document document;

	/**
	 * Parsers for document elements
	 */
	private HashMap<String, PropertyFileElementParser> elementParsers = new HashMap<String, PropertyFileElementParser>();

	/**
	 * Document builder
	 */
	private DocumentBuilder builder;

	/**
	 * Group properties
	 */
	private HashMap<String, TraceObjectPropertyListImpl> groupProperties;

	/**
	 * Trace properties
	 */
	private HashMap<String, TraceObjectPropertyListImpl> traceProperties;

	/**
	 * Constructor
	 * 
	 * @param model
	 *            the trace model
	 * @param fileName
	 *            the property file name
	 * @param builder
	 *            document builder
	 * @throws TraceBuilderException
	 *             if parser cannot be created
	 */
	protected PropertyFileParser(TraceModel model, String fileName,
			DocumentBuilder builder) throws TraceBuilderException {
		super(model, fileName);
		this.builder = builder;
		elementParsers.put(PropertyFileConstants.ENUM_ELEMENT,
				new EnumElementParser(this));
		elementParsers.put(PropertyFileConstants.VALUE_ELEMENT,
				new ValueElementParser());
		elementParsers.put(PropertyFileConstants.PROPERTY_ELEMENT,
				new PropertyElementParser());
		elementParsers.put(PropertyFileConstants.COMPONENT_ELEMENT,
				new ComponentElementParser(this));
		elementParsers.put(PropertyFileConstants.GROUP_ELEMENT,
				new GroupElementParser(this));
		elementParsers.put(PropertyFileConstants.TRACE_ELEMENT,
				new TraceElementParser(this));
		elementParsers.put(PropertyFileConstants.FILE_ELEMENT,
				new FileElementParser());

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.project.ProjectFileParser#createParser()
	 */
	@Override
	protected void createParser() throws TraceBuilderException {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.project.ProjectFileParser#parse()
	 */
	@Override
	public void parse() throws TraceBuilderException {
		try {
			document = builder.parse(projectFile);
			Element rootElement = PropertyFileUtils.findRoot(document);
			if (rootElement != null) {
				parseChildren(model, rootElement);
			} else {
				throw new TraceBuilderException(
						TraceBuilderErrorCode.INVALID_PROJECT_FILE);
			}
		} catch (TraceBuilderException e) {
			throw e;
		} catch (Exception e) {
			throw new TraceBuilderException(
					TraceBuilderErrorCode.INVALID_PROJECT_FILE, e);
		}
	}

	/**
	 * Parses child elements of given element
	 * 
	 * @param owner
	 *            the owning object
	 * @param element
	 *            the element
	 */
	void parseChildren(Object owner, Element element) {
		NodeList list;
		list = element.getChildNodes();
		for (int i = 0; i < list.getLength(); i++) {
			Node node = list.item(i);
			if (node.getNodeType() == Node.ELEMENT_NODE) {
				parseElement(owner, (Element) node);
			}
		}
	}

	/**
	 * Parses an element
	 * 
	 * @param owner
	 *            the owning trace object
	 * @param element
	 *            the element to be parsed
	 */
	private void parseElement(Object owner, Element element) {
		String name = element.getNodeName();
		PropertyFileElementParser parser = elementParsers.get(name);
		if (parser != null) {
			try {
				parser.parse(owner, element);
			} catch (TraceBuilderException e) {
				String msg = TraceBuilderErrorMessages.getErrorMessage(e);
				TraceBuilderGlobals.getEvents().postWarningMessage(msg,
						e.getErrorSource());
			}
		} else {
			postElementNotSupportedWarning(name);
		}
	}

	/**
	 * Posts element not supported warning
	 * 
	 * @param name
	 *            the element name
	 */
	private void postElementNotSupportedWarning(String name) {
		StringErrorParameters parameter = new StringErrorParameters();
		parameter.string = name;
		String msg = TraceBuilderErrorMessages.getErrorMessage(
				TraceBuilderErrorCode.PROPERTY_FILE_ELEMENT_NOT_SUPPORTED,
				parameter);
		TraceBuilderGlobals.getEvents().postWarningMessage(msg, null);
	}

	/**
	 * Gets the document representing the property file
	 * 
	 * @return the document
	 */
	Document getDocument() {
		return document;
	}

	/**
	 * Gets the property list for given group
	 * 
	 * @param name
	 *            the group name
	 * @return the property list
	 */
	TraceObjectPropertyListImpl getGroupPropertyList(String name) {
		if (groupProperties == null) {
			groupProperties = new HashMap<String, TraceObjectPropertyListImpl>();
		}
		TraceObjectPropertyListImpl propertyList = groupProperties.get(name);
		if (propertyList == null) {
			propertyList = new TraceObjectPropertyListImpl();
			groupProperties.put(name, propertyList);
		}
		return propertyList;
	}

	/**
	 * Gets the property list for given trace
	 * 
	 * @param name
	 *            the trace name
	 * @return the property list
	 */
	TraceObjectPropertyListImpl getTracePropertyList(String name) {
		if (traceProperties == null) {
			traceProperties = new HashMap<String, TraceObjectPropertyListImpl>();
		}
		TraceObjectPropertyListImpl propertyList = traceProperties.get(name);
		if (propertyList == null) {
			propertyList = new TraceObjectPropertyListImpl();
			traceProperties.put(name, propertyList);
		}
		return propertyList;
	}

	/**
	 * Gets the group properties
	 * 
	 * @return the group properties
	 */
	HashMap<String, TraceObjectPropertyListImpl> getGroupProperties() {
		return groupProperties;
	}

	/**
	 * Gets the trace properties
	 * 
	 * @return the trace properties
	 */
	public HashMap<String, TraceObjectPropertyListImpl> getTraceProperties() {
		return traceProperties;
	}

}
