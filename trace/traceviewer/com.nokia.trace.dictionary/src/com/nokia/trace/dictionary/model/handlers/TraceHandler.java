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
 * Trace handler
 *
 */
package com.nokia.trace.dictionary.model.handlers;

import org.xml.sax.Attributes;

import com.nokia.trace.dictionary.model.DecodeObject;
import com.nokia.trace.dictionary.model.DictionaryContentHandler;
import com.nokia.trace.dictionary.model.DictionaryContentVariables;
import com.nokia.trace.dictionary.model.DictionaryDecodeModel;
import com.nokia.trace.dictionary.model.TraceInstanceList;
import com.nokia.trace.dictionary.model.DictionaryContentVariables.ParentDecodeObject;

/**
 * Trace handler
 * 
 */
final class TraceHandler extends BaseHandler {

	/**
	 * Tag name this handler handles
	 */
	private static final String TRACE_TAG = "trace"; //$NON-NLS-1$

	/**
	 * Constructor
	 * 
	 * @param model
	 *            the model
	 */
	TraceHandler(DictionaryDecodeModel model) {
		super(model, TRACE_TAG);
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

		DictionaryContentVariables variables = handler.getVariables();

		// Save the data-ref value to variables
		variables.setPreviousTraceDataRef(Integer.parseInt(atts
				.getValue(DATAREF)));
		variables.setTraceInstanceList(new TraceInstanceList(0, "allthesame")); //$NON-NLS-1$
		variables.setParentDecodeObject(ParentDecodeObject.TRACE);

		// Save trace name to variables
		variables.setPreviousTraceName(atts.getValue(NAME));
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
		DictionaryContentVariables variables = handler.getVariables();

		// Set metadata to traces and remove the list afterwards
		variables.getTraceInstanceList().setMetadataToTraces();
		variables.setTraceInstanceList(null);
		variables.setParentDecodeObject(ParentDecodeObject.GROUP);
	}
}
