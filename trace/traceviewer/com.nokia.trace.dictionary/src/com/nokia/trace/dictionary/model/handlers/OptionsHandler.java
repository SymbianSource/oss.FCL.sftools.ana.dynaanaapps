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
 * Options handler
 *
 */
package com.nokia.trace.dictionary.model.handlers;

import org.xml.sax.Attributes;

import com.nokia.trace.dictionary.model.DecodeObject;
import com.nokia.trace.dictionary.model.DictionaryContentHandler;
import com.nokia.trace.dictionary.model.DictionaryDecodeModel;

/**
 * Options handler
 * 
 */
final class OptionsHandler extends BaseHandler {

	/**
	 * Tag name this handler handles
	 */
	private static final String OPTIONS_TAG = "options"; //$NON-NLS-1$

	/**
	 * Constructor
	 * 
	 * @param model
	 *            the model
	 */
	OptionsHandler(DictionaryDecodeModel model) {
		super(model, OPTIONS_TAG);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.nokia.trace.dictionary.model.handlers.BaseHandler#processStartElement
	 * (org.xml.sax.Attributes,
	 * com.nokia.trace.dictionary.model.DictionaryContentHandler)
	 */
	@Override
	public void processStartElement(Attributes atts,
			DictionaryContentHandler handler) {
		handler.getVariables().setInsideMetadataBlock(true);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.nokia.trace.dictionary.model.handlers.BaseHandler#processEndElement
	 * (java.lang.StringBuffer, java.lang.Object,
	 * com.nokia.trace.dictionary.model.DictionaryContentHandler,
	 * com.nokia.trace.dictionary.model.DecodeObject)
	 */
	@Override
	public void processEndElement(StringBuffer elementContent,
			Object unFinishedObject, DictionaryContentHandler handler,
			DecodeObject parentObject) {
		handler.getVariables().setInsideMetadataBlock(false);
	}

}
