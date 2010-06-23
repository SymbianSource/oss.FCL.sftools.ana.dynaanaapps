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
 * DecodeHandler handles decoding
 *
 */
package com.nokia.traceviewer.dictionary;

import java.nio.ByteBuffer;
import java.util.HashMap;

import com.nokia.trace.dictionary.TraceDictionaryEngine;
import com.nokia.trace.dictionary.decoder.TraceDictionaryDecoder;
import com.nokia.trace.dictionary.model.Location;
import com.nokia.traceviewer.engine.TraceInformation;
import com.nokia.traceviewer.engine.TraceMetaData;
import com.nokia.traceviewer.engine.TraceProperties;

/**
 * DecodeHandler handles decoding
 */
public class DecodeHandler {

	/**
	 * Trace Dictionary Engine
	 */
	private final TraceDictionaryEngine dictionaryEngine;

	/**
	 * Constructor
	 * 
	 * @param dictionaryEngine
	 *            engine
	 */
	public DecodeHandler(TraceDictionaryEngine dictionaryEngine) {
		this.dictionaryEngine = dictionaryEngine;
	}

	/**
	 * Decode this trace
	 * 
	 * @param dataBuffer
	 *            dataBuffer where trace is
	 * @param properties
	 *            trace properties
	 * @param addClassMethodPrefix
	 *            if true, add class and function name to the trace text if they
	 *            exist
	 * @param addComponentGroupPrefix
	 *            if true, add component and group name to the trace text if
	 *            they exist
	 * @return traceProperties having decoded traceString
	 */
	public TraceProperties decode(ByteBuffer dataBuffer,
			TraceProperties properties, boolean addClassMethodPrefix,
			boolean addComponentGroupPrefix) {
		if (dictionaryEngine.getModel() != null
				&& dictionaryEngine.getModel().isValid()) {

			// Decode trace
			StringBuffer traceData = dictionaryEngine.getDecoder().decode(
					dataBuffer, properties.dataStart, properties.dataLength,
					properties.information.getComponentId(),
					properties.information.getGroupId(),
					properties.information.getTraceId(), addClassMethodPrefix,
					addComponentGroupPrefix, properties.parameters);

			if (traceData != null) {

				// Set traceString property from this stringBuffer
				properties.traceString = traceData.toString();
				properties.binaryTrace = false;
			} else {
				// If couldn't decode traceString, it's still a binary trace
				properties.binaryTrace = true;
			}
		}

		return properties;
	}

	/**
	 * Gets trace metadata
	 * 
	 * @param information
	 *            trace information
	 * @return trace metadata
	 */
	public TraceMetaData getTraceMetaData(TraceInformation information) {
		TraceMetaData metaData = new TraceMetaData();
		TraceDictionaryDecoder decoder = dictionaryEngine.getDecoder();

		// Set trace where to get MetaData from
		decoder.setTraceWhereToGetMetaData(information.getComponentId(),
				information.getGroupId(), information.getTraceId());

		// Get line number
		int lineNumber = decoder.getLineNumber();
		metaData.setLineNumber(lineNumber);

		// Get location
		Location location = decoder.getLocation();
		if (location != null && location.getPath() != null) {
			metaData.setPath(location.getPath().getFilePath()
					+ location.getFilename());
		}

		// Get classname
		String className = decoder.getClassName();
		if (className != null) {
			metaData.setClassName(className);
		}

		// Get function name
		String methodName = decoder.getMethodName();
		if (methodName != null) {
			metaData.setMethodName(methodName);
		}

		// Get metadata array
		HashMap<String, HashMap<String, Object>> map = decoder.getMetadata();
		if (map != null) {
			metaData.setMetaData(map);
		}

		return metaData;
	}
}
