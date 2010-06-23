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
 * Dictionary Content Handler
 *
 */
package com.nokia.trace.dictionary.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

import com.nokia.trace.dictionary.model.DictionaryContentVariables.ParentDecodeObject;
import com.nokia.trace.dictionary.model.handlers.DictionaryHandler;
import com.nokia.trace.dictionary.model.handlers.DictionaryHandlerCreator;

/**
 * Dictionary Content Handler
 * 
 */
public class DictionaryContentHandler implements ContentHandler {

	/**
	 * Tag for metadata
	 */
	private static final String METADATA_TAG = "options"; //$NON-NLS-1$

	/**
	 * Decode Model
	 */
	private DictionaryDecodeModel model;

	/**
	 * Handlers for start elements in dictionary file
	 */
	private ArrayList<DictionaryHandler> startHandlers;

	/**
	 * Handlers for end elements in dictionary file
	 */
	private ArrayList<DictionaryHandler> endHandlers;

	/**
	 * Metadata handlers
	 */
	private ArrayList<DictionaryHandler> metadataHandlers;

	/**
	 * Are we catching characters
	 */
	private boolean catchCharacters;

	/**
	 * Buffer where to store characters
	 */
	private StringBuffer elementContent;

	/**
	 * Content variables used to get all needed information when creating model
	 */
	private DictionaryContentVariables variables;

	/**
	 * Unfinished object which will be finished in endElement
	 */
	private Object unFinishedObject;

	/**
	 * Locator object
	 */
	private Locator locator;

	/**
	 * Handler creator
	 */
	private DictionaryHandlerCreator handlerCreator;

	/**
	 * Constructor
	 * 
	 * @param model
	 *            model
	 * @param handlers
	 *            list of metadata handlers
	 */
	public DictionaryContentHandler(DictionaryDecodeModel model,
			ArrayList<DictionaryHandler> handlers) {
		handlerCreator = new DictionaryHandlerCreator(model);
		this.model = model;
		this.metadataHandlers = handlers;
		variables = new DictionaryContentVariables();
		elementContent = new StringBuffer();
		startHandlers = handlerCreator.createStartHandlers();
		endHandlers = handlerCreator.createEndHandlers();

		// Sort metadata handlers
		handlerCreator.sortHandlers(metadataHandlers);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.xml.sax.ContentHandler#characters(char[], int, int)
	 */
	public void characters(char[] ch, int start, int length)
			throws SAXException {
		if (catchCharacters) {
			elementContent.append(new String(ch, start, length));
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.xml.sax.ContentHandler#endDocument()
	 */
	public void endDocument() throws SAXException {
		model.clearAfterModelIsReady();
		model.setValid(true);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.xml.sax.ContentHandler#endElement(java.lang.String,
	 * java.lang.String, java.lang.String)
	 */
	public void endElement(String uri, String localName, String qName)
			throws SAXException {

		// If processing metadata, use metadata handlers
		ArrayList<DictionaryHandler> handlers;
		if (variables.isInsideMetadataBlock() && !qName.equals(METADATA_TAG)) {
			handlers = metadataHandlers;
		} else {
			handlers = endHandlers;
		}

		int pos = Collections.binarySearch(handlers, qName,
				new Comparator<Object>() {

					public int compare(Object o1, Object o2) {
						int val = ((DictionaryHandler) o1).getName().compareTo(
								(String) o2);
						return val > 0 ? 1 : val < 0 ? -1 : 0;
					}
				});

		// Handler found, insert the right parent element as a parameter
		if (pos >= 0) {
			DecodeObject parentObj = null;
			if (variables.getParentDecodeObject() == ParentDecodeObject.COMPONENT) {
				parentObj = variables.getPreviousComponent();
			} else if (variables.getParentDecodeObject() == ParentDecodeObject.GROUP) {
				parentObj = variables.getPreviousGroup();
			} else if (variables.getParentDecodeObject() == ParentDecodeObject.TRACE) {
				parentObj = variables.getTraceInstanceList();
			} else if (variables.getParentDecodeObject() == ParentDecodeObject.TRACEINSTANCE) {
				parentObj = variables.getPreviousTrace();
			}
			handlers.get(pos).processEndElement(elementContent,
					unFinishedObject, this, parentObj);
		}

		// Don't check any more end elements automatically and don't catch
		// characters
		catchCharacters = false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.xml.sax.ContentHandler#endPrefixMapping(java.lang.String)
	 */
	public void endPrefixMapping(String prefix) throws SAXException {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.xml.sax.ContentHandler#ignorableWhitespace(char[], int, int)
	 */
	public void ignorableWhitespace(char[] ch, int start, int length)
			throws SAXException {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.xml.sax.ContentHandler#processingInstruction(java.lang.String,
	 * java.lang.String)
	 */
	public void processingInstruction(String target, String data)
			throws SAXException {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.xml.sax.ContentHandler#setDocumentLocator(org.xml.sax.Locator)
	 */
	public void setDocumentLocator(Locator locator) {
		this.locator = locator;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.xml.sax.ContentHandler#skippedEntity(java.lang.String)
	 */
	public void skippedEntity(String name) throws SAXException {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.xml.sax.ContentHandler#startDocument()
	 */
	public void startDocument() throws SAXException {
		model.setValid(false);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.xml.sax.ContentHandler#startElement(java.lang.String,
	 * java.lang.String, java.lang.String, org.xml.sax.Attributes)
	 */
	public void startElement(String uri, String localName, String qName,
			Attributes atts) throws SAXException {

		// If processing metadata, use metadata handlers
		ArrayList<DictionaryHandler> handlers;
		if (variables.isInsideMetadataBlock()) {
			handlers = metadataHandlers;
		} else {
			handlers = startHandlers;
		}

		int pos = Collections.binarySearch(handlers, qName,
				new Comparator<Object>() {

					public int compare(Object o1, Object o2) {
						int val = ((DictionaryHandler) o1).getName().compareTo(
								(String) o2);
						return val > 0 ? 1 : val < 0 ? -1 : 0;
					}
				});
		if (pos >= 0) {
			handlers.get(pos).processStartElement(atts, this);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.xml.sax.ContentHandler#startPrefixMapping(java.lang.String,
	 * java.lang.String)
	 */
	public void startPrefixMapping(String prefix, String uri)
			throws SAXException {
	}

	/**
	 * Tells this handler to catch element contents
	 * 
	 * @param object
	 *            unfinished object
	 */
	public void catchElementContents(Object object) {
		unFinishedObject = object;
		catchCharacters = true;
		elementContent.setLength(0);
	}

	/**
	 * Gets variables
	 * 
	 * @return the variables
	 */
	public DictionaryContentVariables getVariables() {
		return variables;
	}

	/**
	 * Gets locator
	 * 
	 * @return locator
	 */
	public Locator getLocator() {
		return locator;
	}
}
