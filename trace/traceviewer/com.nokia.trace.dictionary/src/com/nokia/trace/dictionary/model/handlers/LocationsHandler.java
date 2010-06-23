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
 * Location handler
 *
 */
package com.nokia.trace.dictionary.model.handlers;

import org.xml.sax.Attributes;

import com.nokia.trace.dictionary.model.DictionaryContentHandler;
import com.nokia.trace.dictionary.model.DictionaryDecodeModel;

/**
 * Location handler
 * 
 */
final class LocationsHandler extends BaseHandler {

	/**
	 * Tag name this handler handles
	 */
	private static final String LOCATIONS_TAG = "locations"; //$NON-NLS-1$

	/**
	 * Constructor
	 * 
	 * @param model
	 *            the model
	 */
	LocationsHandler(DictionaryDecodeModel model) {
		super(model, LOCATIONS_TAG);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.trace.dictionary.model.handlers.DictionaryHandler#
	 * processElement(org.xml.sax.Attributes)
	 */
	@Override
	public void processStartElement(Attributes atts,
			DictionaryContentHandler handler) {
		// Do nothing
	}

}
