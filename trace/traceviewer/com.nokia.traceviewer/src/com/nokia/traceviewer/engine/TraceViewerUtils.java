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
 * TraceViewer Utils contains utilities that can be used anywhere
 *
 */
package com.nokia.traceviewer.engine;

import java.nio.ByteBuffer;
import java.util.Calendar;
import java.util.List;

/**
 * TraceViewer Utils contains utilities that can be used anywhere
 * 
 */
public class TraceViewerUtils {

	/**
	 * Time separator
	 */
	private static final String TIME_SEPARATOR = ":"; //$NON-NLS-1$

	/**
	 * Millisecond separator
	 */
	private static final String MILLISEC_SEPARATOR = "."; //$NON-NLS-1$

	/**
	 * Number ten
	 */
	private static final int TEN = 10;

	/**
	 * Number zero
	 */
	private static final String ZERO = "0"; //$NON-NLS-1$

	/**
	 * Characters for hex string
	 */
	private final static char hexChars[] = { '0', '1', '2', '3', '4', '5', '6',
			'7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };

	/**
	 * Gets trace as hex string
	 * 
	 * @param buf
	 *            ByteBuffer to the data
	 * @param startPos
	 *            starting position in the byte buffer where to start reading
	 * @param length
	 *            how many bytes to read
	 * @param spacesBetweenBytes
	 *            if true, add spaces between bytes
	 * @return the trace as hex string
	 */
	public static String getTraceAsHexString(ByteBuffer buf, int startPos,
			int length, boolean spacesBetweenBytes) {
		StringBuffer out;

		// Read the message to a byte array
		if (length >= 0) {
			byte[] byteArr = new byte[length];
			buf.position(startPos);
			buf.get(byteArr, 0, length);

			// Create new buffer with the size of messageLength multiplied with
			// 3 (2 chars for the hex and one for the separator)

			if (spacesBetweenBytes) {
				out = new StringBuffer(byteArr.length * 3);
			} else {
				out = new StringBuffer(byteArr.length * 2);
			}

			// Go through every byte
			for (byte b : byteArr) {
				int v = b & 0xFF;
				out.append(hexChars[v >>> 4]);
				out.append(hexChars[v & 0xF]);
				if (spacesBetweenBytes) {
					out.append(' ');
				}
			}
		} else {
			out = new StringBuffer(Messages
					.getString("TraceViewerUtils.DataCorruptMsg")); //$NON-NLS-1$
		}

		return out.toString();
	}

	/**
	 * Checks if we need to read data file from the beginning
	 * 
	 * @return true if we need to read data file from the beginning
	 */
	public static boolean isReadingFromStartNeeded() {
		boolean needed = false;

		DataProcessorAccess access = TraceViewerGlobals.getTraceViewer()
				.getDataProcessorAccess();

		// Filtering is one condition
		if (access.getFilterProcessor().isFiltering()) {
			needed = true;

			// Counting lines with text rules is one condition
		} else if (access.getLineCountProcessor().getTextRules().size() > 0) {
			needed = true;

			// Tracing variables with text rules is one condition
		} else if (access.getVariableTracingProcessor().getTextRules().size() > 0) {
			needed = true;
		}

		return needed;
	}

	/**
	 * Constructs time string from current time
	 * 
	 * @return current time as a String
	 */
	public static String constructTimeString() {
		Calendar cal = Calendar.getInstance();
		int hour = cal.get(Calendar.HOUR_OF_DAY);
		int minute = cal.get(Calendar.MINUTE);
		int seconds = cal.get(Calendar.SECOND);
		int milliSeconds = cal.get(Calendar.MILLISECOND);

		String time = ""; //$NON-NLS-1$

		// Add hours
		if (hour < TEN) {
			time += ZERO;
		}
		time += hour + TIME_SEPARATOR;

		// Add minutes
		if (minute < TEN) {
			time += ZERO;
		}
		time += minute + TIME_SEPARATOR;

		// Add seconds
		if (seconds < TEN) {
			time += ZERO;
		}
		time += seconds + MILLISEC_SEPARATOR;

		// Add milliseconds
		if (milliSeconds < TEN * TEN) {
			time += ZERO;
		}
		if (milliSeconds < TEN) {
			time += ZERO;
		}
		time += milliSeconds;

		return time;
	}

	/**
	 * Gets timestamp string for a trace
	 * 
	 * @param traceNumber
	 *            trace number
	 * @return timestamp string for a trace number or empty string if not found
	 */
	public static String getTimestampStringForTrace(int traceNumber) {
		String timestamp = ""; //$NON-NLS-1$

		// Get the trace
		List<TraceProperties> traceArr = TraceViewerGlobals.getTraceViewer()
				.getTraces(traceNumber, traceNumber);
		if (!traceArr.isEmpty()) {
			TraceProperties trace = traceArr.get(0);

			// Process timestamp
			TraceViewerGlobals.getTraceViewer().getDataProcessorAccess()
					.getTimestampParser().processData(trace);

			if (trace.timestampString != null) {
				timestamp = trace.timestampString;
			}
		}
		return timestamp;
	}
}
