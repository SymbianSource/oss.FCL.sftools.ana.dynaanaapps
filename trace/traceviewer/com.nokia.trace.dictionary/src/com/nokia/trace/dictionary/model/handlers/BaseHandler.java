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
 * Base class for handlers
 *
 */
package com.nokia.trace.dictionary.model.handlers;

import org.xml.sax.Attributes;

import com.nokia.trace.dictionary.model.DecodeObject;
import com.nokia.trace.dictionary.model.DictionaryContentHandler;
import com.nokia.trace.dictionary.model.DictionaryDecodeModel;

/**
 * Base class for handlers
 * 
 */
abstract class BaseHandler implements DictionaryHandler,
		DictionaryFileConstants {

	/**
	 * Model
	 */
	protected DictionaryDecodeModel model;

	/**
	 * Name
	 */
	private String name;

	/**
	 * Constructor
	 * 
	 * @param model
	 *            decode model
	 * @param name
	 *            handler name
	 */
	protected BaseHandler(DictionaryDecodeModel model, String name) {
		this.model = model;
		this.name = name;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.nokia.trace.dictionary.model.handlers.DictionaryHandler#getName()
	 */
	public String getName() {
		return name;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.trace.dictionary.model.handlers.DictionaryHandler#
	 * processStartElement(org.xml.sax.Attributes,
	 * com.nokia.trace.dictionary.model.DictionaryContentHandler)
	 */
	public void processStartElement(Attributes atts,
			DictionaryContentHandler handler) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.nokia.trace.dictionary.model.handlers.DictionaryHandler#processEndElement
	 * (java.lang.StringBuffer, java.lang.Object,
	 * com.nokia.trace.dictionary.model.DictionaryContentHandler,
	 * com.nokia.trace.dictionary.model.DecodeObject)
	 */
	public void processEndElement(StringBuffer elementContent,
			Object unFinishedObject, DictionaryContentHandler handler,
			DecodeObject parentObject) {
	}

}
