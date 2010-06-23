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
 * Interface DictionaryHandler
 *
 */
package com.nokia.trace.dictionary.model.handlers;

import org.xml.sax.Attributes;

import com.nokia.trace.dictionary.model.DecodeObject;
import com.nokia.trace.dictionary.model.DictionaryContentHandler;

/**
 * Interface DictionaryHandler
 * 
 */
public interface DictionaryHandler {

	/**
	 * Gets the name of the handler, same as tag in decode file
	 * 
	 * @return the handler name
	 */
	public String getName();

	/**
	 * Processes start element
	 * 
	 * @param atts
	 *            attributes
	 * @param handler
	 *            reference to content handler
	 */
	public void processStartElement(Attributes atts,
			DictionaryContentHandler handler);

	/**
	 * Processes end element
	 * 
	 * @param elementContent
	 *            elements contents
	 * @param unFinishedObject
	 *            unfinished object
	 * @param handler
	 *            dictionary content handler
	 * @param parentObject
	 *            parent decode object
	 */
	public void processEndElement(StringBuffer elementContent,
			Object unFinishedObject, DictionaryContentHandler handler,
			DecodeObject parentObject);
}
