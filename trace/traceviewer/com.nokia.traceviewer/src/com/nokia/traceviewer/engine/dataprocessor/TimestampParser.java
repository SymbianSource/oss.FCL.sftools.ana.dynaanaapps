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
 * Timestamp Parser DataProcessor
 *
 */
package com.nokia.traceviewer.engine.dataprocessor;

import org.eclipse.jface.preference.IPreferenceStore;

import com.nokia.traceviewer.TraceViewerPlugin;
import com.nokia.traceviewer.engine.PlainTextReader;
import com.nokia.traceviewer.engine.TraceProperties;
import com.nokia.traceviewer.engine.TraceViewerGlobals;
import com.nokia.traceviewer.engine.preferences.PreferenceConstants;

/**
 * Timestamp Parser DataProcessor
 */
public class TimestampParser implements DataProcessor {

	/**
	 * Ten
	 */
	private static final int TEN = 10;

	/**
	 * Thousand
	 */
	private static final int THOUSAND = 1000;

	/**
	 * Hours in a day
	 */
	private static final int HOURS_IN_DAY = 24;

	/**
	 * Minutes in a hour
	 */
	private static final int MINUTES_IN_HOUR = 60;

	/**
	 * Seconds in a minute
	 */
	private static final int SECONDS_IN_MINUTE = 60;

	/**
	 * Milliseconds in second
	 */
	private static final int MILLISECS_IN_SECOND = THOUSAND;

	/**
	 * Milliseconds in minute
	 */
	private static final long MILLISECS_IN_MINUTE = MILLISECS_IN_SECOND
			* SECONDS_IN_MINUTE;

	/**
	 * Milliseconds in hour
	 */
	private static final long MILLISECS_IN_HOUR = MILLISECS_IN_MINUTE
			* MINUTES_IN_HOUR;

	/**
	 * Milliseconds in day
	 */
	private static final long MILLISECS_IN_DAY = MILLISECS_IN_HOUR
			* HOURS_IN_DAY;

	/**
	 * Hour start index
	 */
	private static final int HOUR_START_INDEX = 0;

	/**
	 * Hour stop index
	 */
	private static final int HOUR_STOP_INDEX = 2;

	/**
	 * Minute start index
	 */
	private static final int MINUTE_START_INDEX = 3;

	/**
	 * Minute stop index
	 */
	private static final int MINUTE_STOP_INDEX = 5;

	/**
	 * Second start index
	 */
	private static final int SECOND_START_INDEX = 6;

	/**
	 * Second stop index
	 */
	private static final int SECOND_STOP_INDEX = 8;

	/**
	 * Millisecond start index
	 */
	private static final int MILLISECOND_START_INDEX = 9;

	/**
	 * Millisecond stop index
	 */
	private static final int MILLISECOND_STOP_INDEX = 12;

	/**
	 * Bracket start index when accuracy is milliseconds
	 */
	private static final int BRACKET_START_INDEX_ACCURACY_MILLISECS = 13;

	/**
	 * Bracket start index when accuracy is microseconds
	 */
	private static final int BRACKET_START_INDEX_ACCURACY_MICROSECS = 16;

	/**
	 * Length of timestamp string from format HH:mm:ss.SSSSSS
	 */
	private static final int LENGTH_OF_TIMESTAMP_STRING = 15;

	/**
	 * Empty stringbuffer
	 */
	private final StringBuffer emptyBuffer = new StringBuffer(0);

	/**
	 * Bracket start offset
	 */
	private int bracketStartIndex;

	/**
	 * Previous traces timestamp
	 */
	private long timeOfPreviousNormalTrace;

	/**
	 * Previous scrolled traces timestamp
	 */
	private long timeOfPreviousScrolledTrace;

	/**
	 * Previous traces timestamp when filtering
	 */
	private long timeOfPreviousFilterTrace;

	/**
	 * Previous plain text filter trace timestamp
	 */
	private long timeOfPreviousPlainTextFilterTrace;

	/**
	 * Previous timestamp for own calculations
	 */
	private long timeOfPreviousOwnEvent;

	/**
	 * If true, timestamp accuracy is millisecs, otherwise it's microsecs
	 */
	private boolean timestampAccuracyMilliSecs;

	/**
	 * If true, show time from previous trace as milliseconds
	 */
	private boolean showTimeFromPreviousTrace;

	/**
	 * Constructor
	 */
	public TimestampParser() {
		// Get variables from preference store
		IPreferenceStore store = TraceViewerPlugin.getDefault()
				.getPreferenceStore();
		boolean millisec = false;
		if (store.getString(PreferenceConstants.TIMESTAMP_ACCURACY).equals(
				PreferenceConstants.MILLISECOND_ACCURACY)) {
			millisec = true;
		}

		timestampAccuracyMilliSecs = millisec;
		showTimeFromPreviousTrace = store
				.getBoolean(PreferenceConstants.TIME_FROM_PREVIOUS_TRACE_CHECKBOX);

		// Start index of "time from previous trace" changes according to the
		// timestamp accuracy
		if (timestampAccuracyMilliSecs) {
			bracketStartIndex = BRACKET_START_INDEX_ACCURACY_MILLISECS;
		} else {
			bracketStartIndex = BRACKET_START_INDEX_ACCURACY_MICROSECS;
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.nokia.traceviewer.engine.DataProcessor#processData(com.nokia.traceviewer
	 * .engine.TraceProperties)
	 */
	public void processData(TraceProperties properties) {

		if (properties.timestamp != 0) {
			properties.timestampString = processTimestamp(properties);
		} else if (properties.traceConfiguration.isReadFromFilterFile()
				&& TraceViewerGlobals.getTraceViewer().getDataReaderAccess()
						.getCurrentDataReader() instanceof PlainTextReader) {
			properties.traceString = processTimestampFromPlainText(
					properties.traceString, true);
		} else {
			properties.timestampString = null;
		}
	}

	/**
	 * Nulls previous timestamps
	 */
	public void nullPreviousTimestamp() {
		timeOfPreviousNormalTrace = 0;
		timeOfPreviousScrolledTrace = 0;
		timeOfPreviousFilterTrace = 0;
		timeOfPreviousPlainTextFilterTrace = 0;
	}

	/**
	 * Nulls previous own timestamp
	 */
	public void nullPreviousOwnTimestamp() {
		timeOfPreviousOwnEvent = 0;
	}

	/**
	 * Processes nanosecond timestamp to human readable form
	 * 
	 * @param properties
	 *            trace properties
	 * @return human readable timestamp
	 */
	private String processTimestamp(TraceProperties properties) {
		long previousTimestamp;

		// Get the previous timestamp
		if (!properties.traceConfiguration.isReadFromFilterFile()) {
			if (properties.traceConfiguration.isScrolledTrace()) {
				previousTimestamp = timeOfPreviousScrolledTrace;
			} else {
				previousTimestamp = timeOfPreviousNormalTrace;
			}
		} else {
			previousTimestamp = timeOfPreviousFilterTrace;
		}

		long timestamp = properties.timestamp;
		StringBuffer newData = new StringBuffer(LENGTH_OF_TIMESTAMP_STRING);

		long microseconds = timestamp / THOUSAND;
		long milliseconds = microseconds / THOUSAND;

		if (previousTimestamp == 0
				|| TraceViewerGlobals.getTraceViewer().getDataReaderAccess()
						.getCurrentDataReader().getTraceCount() == 1) {
			previousTimestamp = milliseconds;
		}

		// Insert milliseconds from previous trace to trace properties
		properties.timeFromPreviousTrace = milliseconds - previousTimestamp;
		previousTimestamp = milliseconds;

		long seconds = milliseconds / THOUSAND;
		milliseconds = milliseconds % THOUSAND;

		int minutes = (int) (seconds / SECONDS_IN_MINUTE);
		seconds = seconds % SECONDS_IN_MINUTE;

		int hours = (minutes / MINUTES_IN_HOUR);
		minutes = minutes % MINUTES_IN_HOUR;
		hours = hours % HOURS_IN_DAY;

		// Format the String, add hours
		if (hours < TEN) {
			newData.append('0');
		}
		newData.append(hours);

		// Minutes
		newData.append(':');
		if (minutes < TEN) {
			newData.append('0');
		}
		newData.append(minutes);

		// Seconds
		newData.append(':');
		if (seconds < TEN) {
			newData.append('0');
		}
		newData.append(seconds);

		// Milliseconds
		newData.append('.');
		if (milliseconds < (TEN * TEN)) {
			newData.append('0');
		}
		if (milliseconds < TEN) {
			newData.append('0');
		}
		newData.append(milliseconds);

		// Microseconds
		if (!timestampAccuracyMilliSecs) {
			microseconds = microseconds % THOUSAND;

			if (microseconds < (TEN * TEN)) {
				newData.append('0');
			}
			if (microseconds < TEN) {
				newData.append('0');
			}
			newData.append(microseconds);
		}

		// Set the previous timestamp
		if (!properties.traceConfiguration.isReadFromFilterFile()) {
			if (properties.traceConfiguration.isScrolledTrace()) {
				timeOfPreviousScrolledTrace = previousTimestamp;
			} else {
				timeOfPreviousNormalTrace = previousTimestamp;
			}
		} else {
			timeOfPreviousFilterTrace = previousTimestamp;
		}

		return newData.toString();
	}

	/**
	 * Processes timestamp from plain text trace
	 * 
	 * @param traceLine
	 *            traceline where we need to change the time
	 * @param filterTrace
	 *            indicates that this is a filter trace
	 * @return traceline with a correct timestamp string
	 */
	public String processTimestampFromPlainText(String traceLine,
			boolean filterTrace) {
		long previousTime;

		if (filterTrace) {
			previousTime = timeOfPreviousPlainTextFilterTrace;
		} else {
			previousTime = timeOfPreviousOwnEvent;
		}

		StringBuffer newTrace = new StringBuffer();

		try {

			int bracketStart = traceLine.indexOf('[');
			int bracketEnd = traceLine.indexOf(']');

			// If start bracket not in place or end bracket not found, skip the
			// whole process
			if (bracketStart == bracketStartIndex && bracketEnd != -1) {

				// Get the current time in milliseconds
				long currentTime = Integer.parseInt(traceLine.substring(
						HOUR_START_INDEX, HOUR_STOP_INDEX))
						* MILLISECS_IN_HOUR;
				currentTime += Integer.parseInt(traceLine.substring(
						MINUTE_START_INDEX, MINUTE_STOP_INDEX))
						* MILLISECS_IN_MINUTE;
				currentTime += Integer.parseInt(traceLine.substring(
						SECOND_START_INDEX, SECOND_STOP_INDEX))
						* MILLISECS_IN_SECOND;
				currentTime += Integer.parseInt(traceLine.substring(
						MILLISECOND_START_INDEX, MILLISECOND_STOP_INDEX));

				long timeFromLastTrace;

				if (previousTime == 0
						|| TraceViewerGlobals.getTraceViewer()
								.getDataReaderAccess().getCurrentDataReader()
								.getTraceCount() == 1) {
					timeFromLastTrace = 0;
				} else {
					if (currentTime < previousTime) {
						currentTime += MILLISECS_IN_DAY;
					}
					timeFromLastTrace = currentTime - previousTime;
				}

				previousTime = currentTime;

				newTrace.append(traceLine.substring(0, bracketStartIndex + 1));
				newTrace.append(timeFromLastTrace);
				newTrace.append(traceLine.substring(bracketEnd));
			} else {
				newTrace.append(traceLine);
			}

			// If something goes wrong, don't put timestamp in to the trace
		} catch (Exception e) {
			// e.printStackTrace();
			newTrace.append(traceLine);
		}

		// Set back the previous time to correct variable
		if (filterTrace) {
			timeOfPreviousPlainTextFilterTrace = previousTime;
		} else {
			timeOfPreviousOwnEvent = previousTime;
		}

		return newTrace.toString();

	}

	/**
	 * Gets timestamp accuracy
	 * 
	 * @return true if timestamp accuracy is milliseconds. False if
	 *         microseconds.
	 */
	public boolean isTimestampAccuracyMilliSecs() {
		return timestampAccuracyMilliSecs;
	}

	/**
	 * Sets timestamp accuracy
	 * 
	 * @param timestampAccuracyMilliSecs
	 *            if true, set timestamp accuracy to milliseconds. If false, set
	 *            it to microseconds.
	 */
	public void setTimestampAccuracyMilliSecs(boolean timestampAccuracyMilliSecs) {
		this.timestampAccuracyMilliSecs = timestampAccuracyMilliSecs;

		if (timestampAccuracyMilliSecs) {
			bracketStartIndex = BRACKET_START_INDEX_ACCURACY_MILLISECS;
		} else {
			bracketStartIndex = BRACKET_START_INDEX_ACCURACY_MICROSECS;
		}
	}

	/**
	 * Sets showing time from previous trace on / off
	 * 
	 * @param showTimeFromPreviousTrace
	 *            if true, show time from previous trace
	 */
	public void setShowTimeFromPrevious(boolean showTimeFromPreviousTrace) {
		this.showTimeFromPreviousTrace = showTimeFromPreviousTrace;
	}

	/**
	 * Gets timestamp strings length
	 * 
	 * @return length of the timestamp string
	 */
	public int getTimestampStringLength() {

		// Length is bracket start offset + tabulator
		int minLength = bracketStartIndex + 1;

		// If showing time from previous trace, there is at least three more
		// characters added
		if (showTimeFromPreviousTrace) {
			minLength += 3;
		}

		return minLength;
	}

	/**
	 * Gets time from previous trace String
	 * 
	 * @param timeFromPrevious
	 *            time from previous as long
	 * 
	 * @return stringBuffer containing string where time from previous trace is
	 *         in milliseconds
	 */
	public StringBuffer getTimeFromPreviousString(long timeFromPrevious) {
		StringBuffer buf;

		// Insert to stringbuffer
		if (showTimeFromPreviousTrace) {
			buf = new StringBuffer();
			buf.append(' ');
			buf.append('[');
			buf.append(timeFromPrevious);
			buf.append(']');

			// Else return empty buffer
		} else {
			buf = emptyBuffer;
		}
		return buf;
	}
}