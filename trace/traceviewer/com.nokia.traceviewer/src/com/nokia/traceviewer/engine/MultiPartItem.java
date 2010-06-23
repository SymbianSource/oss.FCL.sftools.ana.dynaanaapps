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
 * Base class for Isi Readers
 *
 */
package com.nokia.traceviewer.engine;

import java.util.ArrayList;
import java.util.List;

/**
 * Multipart item
 */
public class MultiPartItem {

	/**
	 * Tells if this multipart trace is completed
	 */
	private boolean isCompleted;

	/**
	 * Total message length after assembling the parts
	 */
	private int totalMessageLength;

	/**
	 * Trace header size list containing header sizes
	 */
	private final List<Integer> tracePartHeaderSizeList = new ArrayList<Integer>();

	/**
	 * Trace part list containing whole messages as byte arrays
	 */
	private final List<byte[]> tracePartList = new ArrayList<byte[]>();

	/**
	 * Sets this trace to be completed
	 * 
	 * @param completed
	 */
	public void setCompleted(boolean completed) {
		this.isCompleted = completed;
	}

	/**
	 * Tells if the trace is completed
	 * 
	 * @return true if completed
	 */
	public boolean isCompleted() {
		return isCompleted;
	}

	/**
	 * Gets trace parts
	 * 
	 * @return trace parts list
	 */
	public List<byte[]> getTraceParts() {
		return tracePartList;
	}

	/**
	 * Gets trace part header sizes list
	 * 
	 * @return trace part header sizes list
	 */
	public List<Integer> getTracePartHeaderSizes() {
		return tracePartHeaderSizeList;
	}

	/**
	 * Gets total message length
	 * 
	 * @return total message length
	 */
	public int getTotalMessageLength() {
		return totalMessageLength;
	}

	/**
	 * Adds trace part
	 * 
	 * @param trace
	 *            trace part
	 * @param lastPart
	 *            if true, is the last part of the trace
	 */
	public void addPart(TraceProperties trace, boolean lastPart) {
		byte[] byteArr = new byte[trace.messageLength];

		// First copy the header
		int headerSize = trace.dataStart - trace.messageStart;
		trace.byteBuffer.position(trace.messageStart);
		trace.byteBuffer.get(byteArr, 0, trace.messageLength);

		// Insert header length and byte array to the map
		tracePartHeaderSizeList.add(Integer.valueOf(headerSize));
		tracePartList.add(byteArr);

		// Add total message length
		if (lastPart) {
			totalMessageLength += trace.messageLength;
		} else {
			totalMessageLength += trace.dataLength;
		}
	}
}
