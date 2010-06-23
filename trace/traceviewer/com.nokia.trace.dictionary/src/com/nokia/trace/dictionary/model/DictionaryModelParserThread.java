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
 * Thread parsing one dictionary file
 *
 */
package com.nokia.trace.dictionary.model;

import java.io.InputStream;
import java.util.ArrayList;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import com.nokia.trace.dictionary.TraceDictionaryEngine;
import com.nokia.trace.dictionary.model.handlers.DictionaryHandler;
import com.nokia.trace.eventrouter.TraceEvent;

/**
 * Thread parsing one dictionary file
 * 
 */
public class DictionaryModelParserThread extends Thread {

	/**
	 * Category for events
	 */
	private final static String EVENT_CATEGORY = "Dictionary Model Builder"; //$NON-NLS-1$

	/**
	 * XML Reader
	 */
	private XMLReader reader;

	/**
	 * Decode model
	 */
	private DictionaryDecodeModel model;

	/**
	 * InputSource to read the dictionary file from
	 */
	private InputSource inputSource;

	/**
	 * Dictionary file name
	 */
	private String fileName;

	/**
	 * List of metadata handlers
	 */
	private ArrayList<DictionaryHandler> metadataHandlers;

	/**
	 * Constructor
	 * 
	 * @param inputSource
	 *            the input source
	 */
	DictionaryModelParserThread(InputStream inputStream, String fileName,
			DictionaryDecodeModel model,
			ArrayList<DictionaryHandler> metadataHandlers) {
		this.fileName = fileName;
		this.model = model;
		this.metadataHandlers = metadataHandlers;

		// Create input source
		createInputSource(inputStream);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Thread#run()
	 */
	@Override
	public void run() {
		if (inputSource != null) {
			try {

				// Parse
				reader.parse(inputSource);

			} catch (Exception e) {

				// Set error to eventview
				postErrorMessage(e.toString());

				// There might be something wrong with the decode file, set
				// model to valid to close progress bar
				model.setValid(true);
			}
		}
	}

	/**
	 * Creates input source
	 * 
	 * @param fileStream
	 *            file stream
	 */
	private void createInputSource(InputStream fileStream) {
		try {
			SAXParserFactory factory = SAXParserFactory.newInstance();
			factory.setValidating(false);

			SAXParser sp = factory.newSAXParser();
			reader = sp.getXMLReader();
			reader.setContentHandler(new DictionaryContentHandler(model,
					metadataHandlers));
			inputSource = new InputSource(fileStream);

		} catch (ParserConfigurationException e) {
			postErrorMessage(e.toString());
		} catch (SAXException e) {
			postErrorMessage(e.toString());
		}
	}

	/**
	 * Post error message
	 * 
	 * @param string
	 *            error message
	 */
	private void postErrorMessage(String string) {
		TraceEvent event = new TraceEvent(TraceEvent.ERROR, string);
		event.setCategory(EVENT_CATEGORY);
		event.setSource(fileName);
		TraceDictionaryEngine.postEvent(event);
	}

}
