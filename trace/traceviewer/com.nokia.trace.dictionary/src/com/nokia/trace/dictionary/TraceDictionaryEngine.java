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
 * Trace Dictionary Engine
 *
 */
package com.nokia.trace.dictionary;

import com.nokia.trace.dictionary.decoder.TraceDictionaryDecoder;
import com.nokia.trace.dictionary.model.DictionaryDecodeModel;
import com.nokia.trace.dictionary.model.DictionaryModelBuilder;
import com.nokia.trace.eventrouter.TraceEvent;
import com.nokia.trace.eventrouter.TraceEventRouter;

/**
 * Trace Dictionary Engine
 */
public class TraceDictionaryEngine {

	/**
	 * Decoder
	 */
	private TraceDictionaryDecoder decoder;

	/**
	 * Model Builder
	 */
	private DictionaryModelBuilder modelBuilder;

	/**
	 * Decode Model
	 */
	private DictionaryDecodeModel decodeModel;

	/**
	 * Constructor
	 */
	public TraceDictionaryEngine() {
		decodeModel = new DictionaryDecodeModel();
	}

	/**
	 * Gets decoder
	 * 
	 * @return the decoder
	 */
	public TraceDictionaryDecoder getDecoder() {
		if (decoder == null) {
			decoder = new TraceDictionaryDecoder(decodeModel);
		}
		return decoder;
	}

	/**
	 * Gets model builder
	 * 
	 * @return the model builder
	 */
	public DictionaryModelBuilder getModelBuilder() {
		if (modelBuilder == null) {
			modelBuilder = new DictionaryModelBuilder(decodeModel);
		}
		return modelBuilder;
	}

	/**
	 * Gets model
	 * 
	 * @return the decode model
	 */
	public DictionaryDecodeModel getModel() {
		return decodeModel;
	}

	/**
	 * Posts event to event view
	 * 
	 * @param event
	 *            event
	 */
	public static void postEvent(final TraceEvent event) {
		// If source has line number, add also file name
		if (event.getSource() != null && event.getSource() instanceof Integer) {
			event.setSource(DictionaryModelBuilder.getCurrentFile()
					+ " line " + event.getSource()); //$NON-NLS-1$
		}
		TraceEventRouter.getInstance().postEvent(event);
	}
}
