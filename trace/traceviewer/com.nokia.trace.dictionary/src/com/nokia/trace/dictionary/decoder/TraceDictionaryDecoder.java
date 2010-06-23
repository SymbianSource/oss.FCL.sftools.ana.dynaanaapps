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
 * Trace Dictionary Decoder
 *
 */
package com.nokia.trace.dictionary.decoder;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import com.nokia.trace.dictionary.model.DictionaryDecodeModel;
import com.nokia.trace.dictionary.model.Location;
import com.nokia.trace.dictionary.model.Trace;
import com.nokia.trace.dictionary.model.TraceComponent;
import com.nokia.trace.dictionary.model.TraceData;
import com.nokia.trace.dictionary.model.TraceGroup;
import com.nokia.trace.dictionary.model.decodeparameters.DecodeParameter;

/**
 * Trace Dictionary Decoder
 * 
 */
public final class TraceDictionaryDecoder {

	/**
	 * Estimated trace size
	 */
	private static final int ESTIMATED_TRACE_SIZE = 70;

	/**
	 * Separator between class and function
	 */
	private static final String CLASS_FUNCTION_SEPARATOR = "::"; //$NON-NLS-1$

	/**
	 * Separator after function name
	 */
	private static final String AFTER_FUNCTION_SEPARATOR = ": "; //$NON-NLS-1$

	/**
	 * Model to use for decoding
	 */
	private DictionaryDecodeModel model;

	/**
	 * Previous trace used when getting metadata so that trace is only need to
	 * be get once
	 */
	private Trace previousMetaTrace;

	/**
	 * Constructor
	 * 
	 * @param model
	 *            dictionary model
	 */
	public TraceDictionaryDecoder(DictionaryDecodeModel model) {
		this.model = model;
	}

	/**
	 * Decodes the contents of a trace
	 * 
	 * @param dataBuffer
	 *            buffer containing data
	 * @param dataStart
	 *            data start offset
	 * @param dataLength
	 *            data length
	 * @param componentId
	 *            component ID of the trace
	 * @param groupId
	 *            group ID of the trace
	 * @param traceId
	 *            trace ID
	 * @param showClassPrefixes
	 *            if true, add class and function name to the trace text if they
	 *            exist
	 * @param showComponentGroupPrefixes
	 *            if true, add component and group name to the trace text if
	 *            they exist
	 * @param parameterList
	 *            parameter list where to add each parameters separately when
	 *            decoding
	 * @return decoded trace string buffer
	 */
	public StringBuffer decode(ByteBuffer dataBuffer, int dataStart,
			int dataLength, int componentId, int groupId, int traceId,
			boolean showClassPrefixes, boolean showComponentGroupPrefixes,
			ArrayList<String> parameterList) {

		// Get trace
		Trace trace = getTrace(componentId, groupId, traceId);

		// Create variables
		StringBuffer buf = null;
		ArrayList<DecodeParameter> parameters = null;
		TraceData traceData = null;

		// Get trace data
		if (trace != null) {
			traceData = trace.getTraceData();

			// Get trace parameters
			if (traceData != null) {
				parameters = traceData.getDecodeParameters();

				// Check if the trace only contains one variable and tell it to
				// the decoders
				if (traceData.containsOnlyOneVariable()) {
					DecodeParameter.isOnlyVariableInTrace = true;
				} else {
					DecodeParameter.isOnlyVariableInTrace = false;
				}
			}
		}

		// Decode the trace to the StringBuffer
		if (parameters != null && trace != null) {
			buf = new StringBuffer(ESTIMATED_TRACE_SIZE);

			// Create parameter list if it doesn't exist so the decode
			// parameters doesn't have to make a null check
			if (parameterList == null) {
				parameterList = new ArrayList<String>();
			}

			// Insert prefixes
			TraceGroup group = trace.getGroup();
			insertPrefixes(buf, trace, group, showClassPrefixes,
					showComponentGroupPrefixes);

			// Decode all parameters
			Iterator<DecodeParameter> it = parameters.iterator();
			int offset = dataStart;

			while (it.hasNext()) {
				DecodeParameter par = it.next();
				offset = par.decode(dataBuffer, offset, buf, dataStart,
						dataLength, parameterList);
			}

			// Insert suffixes
			insertSuffixes(buf, group);
		}

		return buf;
	}

	/**
	 * Inserts prefixes
	 * 
	 * @param buf
	 *            trace buffer
	 * @param trace
	 *            trace
	 * @param group
	 *            trace group
	 * @param showClassPrefixes
	 *            if true, add class and function name to the trace text if they
	 *            exist
	 * @param showComponentGroupPrefixes
	 *            if true, add component and group name to the trace text if
	 *            they exist
	 */
	private void insertPrefixes(StringBuffer buf, Trace trace,
			TraceGroup group, boolean showClassPrefixes,
			boolean showComponentGroupPrefixes) {
		if (showComponentGroupPrefixes) {
			if (group.getComponent().getPrefix() != null) {
				buf.append(group.getComponent().getPrefix());
			}
			if (group.getPrefix() != null) {
				buf.append(group.getPrefix());
			}
		}
		if (showClassPrefixes && trace.getClassName() != null
				&& trace.getMethodName() != null) {
			buf.append(trace.getClassName());
			buf.append(CLASS_FUNCTION_SEPARATOR);
			buf.append(trace.getMethodName());
			buf.append(AFTER_FUNCTION_SEPARATOR);
		}
	}

	/**
	 * Insert suffixes
	 * 
	 * @param buf
	 *            trace buffer
	 * @param group
	 *            trace group
	 */
	private void insertSuffixes(StringBuffer buf, TraceGroup group) {
		if (group.getSuffix() != null) {
			buf.append(group.getSuffix());
		}
		if (group.getComponent().getSuffix() != null) {
			buf.append(group.getComponent().getSuffix());
		}
	}

	/**
	 * Gets trace with component, group and trace IDs
	 * 
	 * @param componentId
	 *            component Id
	 * @param groupId
	 *            group Id
	 * @param traceId
	 *            trace Id
	 * @return Trace
	 */
	private Trace getTrace(int componentId, int groupId, int traceId) {
		TraceGroup group = null;
		Trace trace = null;

		// Get component
		TraceComponent comp = model.getComponent(componentId);

		// Get group
		if (comp != null) {
			group = comp.getGroup(groupId);
			// Get trace
			if (group != null) {
				trace = group.getTrace(traceId);
			}
		}
		return trace;
	}

	/**
	 * Sets previous trace where to get data from. Must be set before calling
	 * decode or getMetaData functions
	 * 
	 * @param componentId
	 *            component Id
	 * @param groupId
	 *            group Id
	 * @param traceId
	 *            trace Id
	 */
	public void setTraceWhereToGetMetaData(int componentId, int groupId,
			int traceId) {
		previousMetaTrace = getTrace(componentId, groupId, traceId);
	}

	/**
	 * Gets location
	 * 
	 * @return location
	 */
	public Location getLocation() {
		Location location = null;
		if (previousMetaTrace != null) {
			location = previousMetaTrace.getLocation();
		}
		return location;
	}

	/**
	 * Gets line number
	 * 
	 * @return line number
	 */
	public int getLineNumber() {
		int lineNum = 0;
		if (previousMetaTrace != null) {
			lineNum = previousMetaTrace.getLineNumber();
		}
		return lineNum;
	}

	/**
	 * Gets class name
	 * 
	 * @return class name
	 */
	public String getClassName() {
		String className = null;
		if (previousMetaTrace != null) {
			className = previousMetaTrace.getClassName();
		}
		return className;
	}

	/**
	 * Gets method name
	 * 
	 * @return method name
	 */
	public String getMethodName() {
		String methodName = null;
		if (previousMetaTrace != null) {
			methodName = previousMetaTrace.getMethodName();
		}
		return methodName;
	}

	/**
	 * Gets metadata map
	 * 
	 * @return metadata map
	 */
	public HashMap<String, HashMap<String, Object>> getMetadata() {
		HashMap<String, HashMap<String, Object>> metadata = null;

		// Create a new metadata map if trace has some metadata
		if (previousMetaTrace != null
				&& (previousMetaTrace.getMetadata() != null
						|| previousMetaTrace.getGroup().getMetadata() != null || previousMetaTrace
						.getGroup().getComponent().getMetadata() != null)) {
			TraceGroup group = previousMetaTrace.getGroup();
			metadata = new HashMap<String, HashMap<String, Object>>();

			// Add trace metadata
			if (previousMetaTrace.getMetadata() != null) {
				addMissingMetadataToList(previousMetaTrace.getMetadata(),
						metadata);
			}

			// Add group metadata
			if (group.getMetadata() != null) {
				addMissingMetadataToList(group.getMetadata(), metadata);
			}

			// Add component metadata
			if (group.getComponent().getMetadata() != null) {
				addMissingMetadataToList(group.getComponent().getMetadata(),
						metadata);
			}
		}
		return metadata;
	}

	/**
	 * Adds missing metadata to list
	 * 
	 * @param oldList
	 *            old list to append from
	 * @param newList
	 *            new list to append to
	 */
	private void addMissingMetadataToList(
			HashMap<String, HashMap<String, Object>> oldList,
			HashMap<String, HashMap<String, Object>> newList) {
		Iterator<String> it = oldList.keySet().iterator();
		while (it.hasNext()) {
			String key = it.next();

			// Doesn't contain this key, add it
			if (!newList.containsKey(key)) {
				newList.put(key, oldList.get(key));

				// Contains the key, check values inside
			} else {
				Iterator<String> valueIt = oldList.get(key).keySet().iterator();
				while (valueIt.hasNext()) {
					String valueKey = valueIt.next();

					// Doesn't contain value key, add it
					if (!newList.get(key).containsKey(valueKey)) {
						newList.get(key).put(valueKey,
								newList.get(key).get(valueKey));
					}
				}
			}
		}
	}
}
