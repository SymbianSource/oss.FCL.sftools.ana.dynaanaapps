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
 * Trace Properties
 *
 */
package com.nokia.traceviewer.engine;

import java.nio.ByteBuffer;
import java.util.ArrayList;

/**
 * Trace Properties
 */
public class TraceProperties {

	/**
	 * Constructor
	 * 
	 * @param configuration
	 *            trace configuration
	 */
	public TraceProperties(TraceConfiguration configuration) {
		this.traceConfiguration = configuration;
		bTraceInformation = new BTraceInformation();
		information = new TraceInformation();
		parameters = new ArrayList<String>();
	}

	/**
	 * Tells that this trace is binary and should be decoded
	 */
	public boolean binaryTrace;

	/**
	 * Tells that this is the last trace of the file or the trace block
	 */
	public boolean lastTrace;

	/**
	 * Message start offset in the byte buffer
	 */
	public int messageStart;

	/**
	 * Message length
	 */
	public int messageLength;

	/**
	 * Data start offset in the byte buffer
	 */
	public int dataStart;

	/**
	 * Data length
	 */
	public int dataLength;

	/**
	 * Trace number in the file
	 */
	public int traceNumber;

	/**
	 * Pointer to bytebuffer where trace is
	 */
	public ByteBuffer byteBuffer;

	/**
	 * Timestamp of the trace in nanoseconds
	 */
	public long timestamp;

	/**
	 * Time from previous trace in milliseconds. 0 if not available.
	 */
	public long timeFromPreviousTrace;

	/**
	 * Decoded trace string
	 */
	public String traceString;

	/**
	 * Comment for the trace
	 */
	public String traceComment;

	/**
	 * Decoded timestamp string in format HH:mm:ss.SSS[SSS] depending if the
	 * chosen timestamp accuracy is milliseconds or microseconds
	 */
	public String timestampString;

	/**
	 * Trace configuration. Do not change the values if you don't know what you
	 * are doing!
	 */
	public TraceConfiguration traceConfiguration;

	/**
	 * Trace information used to get metadata when needed from model
	 */
	public TraceInformation information;

	/**
	 * BTrace information containing for example Thread ID
	 */
	public BTraceInformation bTraceInformation;

	/**
	 * Decoded trace parameters as String
	 */
	public ArrayList<String> parameters;
}