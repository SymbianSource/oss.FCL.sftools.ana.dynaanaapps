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
 * Viewer Selection Changed Listener listens changes in viewers selection
 *
 */
package com.nokia.traceviewer.view.listener;

import org.eclipse.jface.text.TextViewer;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.widgets.Slider;

import com.nokia.traceviewer.engine.StateHolder;
import com.nokia.traceviewer.engine.TraceViewerGlobals;
import com.nokia.traceviewer.engine.TraceViewerUtils;

/**
 * Viewer Selection Changed Listener listens changes in viewers selection
 * 
 */
public class ViewerSelectionChangedListener implements
		ISelectionChangedListener {

	/**
	 * Seconds in day
	 */
	private static final int SECONDS_IN_DAY = 86400;

	/**
	 * Timestamp length
	 */
	private static final int TIMESTAMP_LENGTH = 12;

	/**
	 * The slider in the view
	 */
	private final Slider slider;

	/**
	 * Constructor
	 * 
	 * @param slider
	 *            the slider
	 */
	public ViewerSelectionChangedListener(Slider slider) {
		this.slider = slider;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.ISelectionChangedListener
	 * #selectionChanged(org.eclipse.jface.viewers.SelectionChangedEvent)
	 */
	public void selectionChanged(SelectionChangedEvent event) {
		handleViewUpdate(event);
		handleTrimInformationUpdate();
	}

	/**
	 * Handles view update
	 * 
	 * @param event
	 *            selection changed event
	 */
	private void handleViewUpdate(SelectionChangedEvent event) {
		TextViewer viewer = (TextViewer) event.getSource();
		StyledText widget = viewer.getTextWidget();
		int showingTracesFrom = TraceViewerGlobals.getTraceViewer().getView()
				.getShowingTracesFrom();

		slider.setSelection(showingTracesFrom + viewer.getTopIndex());

		int caretOffset = widget.getCaretOffset();
		int line = widget.getLineAtOffset(caretOffset);

		if (line <= 0) {
			// First line
			TraceViewerGlobals.getTraceViewer().getStateHolder().setState(
					StateHolder.State.SCROLLING_WITH_ARROWS);
			slider.setSelection(showingTracesFrom - 1);
		} else if (line >= widget.getLineCount() - 1
				&& line < TraceViewerGlobals.getTraceViewer()
						.getDataReaderAccess().getCurrentDataReader()
						.getTraceCount()
						- showingTracesFrom) {
			TraceViewerGlobals.getTraceViewer().getStateHolder().setState(
					StateHolder.State.SCROLLING_WITH_ARROWS);
			slider.setSelection(showingTracesFrom + widget.getLineCount() + 1);
		}

		updateSelectionProperties(widget);

		// Scroll view to correct line
		TraceViewerGlobals.getTraceViewer().getView().scrollViewToLine(
				slider.getSelection());
	}

	/**
	 * Updates selection properties
	 * 
	 * @param widget
	 *            styledtext widget
	 */
	private void updateSelectionProperties(StyledText widget) {
		int showingTracesFrom = TraceViewerGlobals.getTraceViewer().getView()
				.getShowingTracesFrom();

		// Get selection start
		int selectionStart = widget.getSelectionRange().x;

		int upperSelectionLine = widget.getLineAtOffset(widget
				.getSelectionRange().x);
		int upperSelectionLineOffset = widget
				.getOffsetAtLine(upperSelectionLine);

		int belowSelectionLine = widget.getLineAtOffset(widget
				.getSelectionRange().x
				+ widget.getSelectionRange().y);
		int belowSelectionLineOffset = widget
				.getOffsetAtLine(belowSelectionLine);

		// If last line selected, take it out
		if (belowSelectionLineOffset + TIMESTAMP_LENGTH >= widget
				.getCharCount()
				&& belowSelectionLine > 0) {
			belowSelectionLine--;
			belowSelectionLineOffset = widget
					.getOffsetAtLine(belowSelectionLine);
		}

		int firstSelLine = upperSelectionLine;
		int firstSelLineOffset = upperSelectionLineOffset;
		int firstSelCaretOffset = widget.getSelectionRange().x
				- upperSelectionLineOffset;
		int lastSelLine = belowSelectionLine;
		int lastSelLineOffset = belowSelectionLineOffset;
		int lastSelCaretOffset = widget.getSelectionRange().x
				+ widget.getSelectionRange().y - belowSelectionLineOffset;

		// If caret is not at start, we are selection downwards
		if (widget.getCaretOffset() == selectionStart) {
			firstSelLine = belowSelectionLine;
			firstSelLineOffset = belowSelectionLineOffset;

			int tmp = firstSelCaretOffset;
			firstSelCaretOffset = lastSelCaretOffset;
			lastSelCaretOffset = tmp;

			lastSelLine = upperSelectionLine;
			lastSelLineOffset = upperSelectionLineOffset;
		}

		// Only save new start offset if current offset is in same block,
		// at initial state -1 or start at position 0
		boolean offsetInSameBlock = SelectionProperties.firstClickedLine > showingTracesFrom
				&& SelectionProperties.firstClickedLine < showingTracesFrom
						+ widget.getLineCount();

		if (offsetInSameBlock
				|| SelectionProperties.firstClickedLine == -1
				|| (showingTracesFrom == 0 && SelectionProperties.firstClickedLine == 0)) {

			SelectionProperties.firstClickedLine = firstSelLine
					+ showingTracesFrom;
			SelectionProperties.firstClickedLineCaretOffset = firstSelCaretOffset;
			if (firstSelLineOffset + TIMESTAMP_LENGTH < widget.getCharCount()) {
				SelectionProperties.firstClickedTimestamp = TraceViewerUtils
						.getTimestampStringForTrace(SelectionProperties.firstClickedLine);
			}
		}

		SelectionProperties.lastClickedLine = lastSelLine + showingTracesFrom;
		SelectionProperties.lastClickedLineCaretOffset = lastSelCaretOffset;
		if (lastSelLineOffset + TIMESTAMP_LENGTH < widget.getCharCount()) {
			SelectionProperties.lastClickedTimestamp = TraceViewerUtils
					.getTimestampStringForTrace(SelectionProperties.lastClickedLine);
		}
	}

	/**
	 * Handles trim information update
	 */
	public static void handleTrimInformationUpdate() {
		String timeDiff = ""; //$NON-NLS-1$

		// Check that first and last timestamps exist
		if (SelectionProperties.firstClickedTimestamp.length() > 0
				&& SelectionProperties.lastClickedTimestamp.length() > 0) {
			int firstTimeSeconds = getTimeInSeconds(SelectionProperties.firstClickedTimestamp);
			int firstTimeMilliSeconds = getTimeInMilliSeconds(SelectionProperties.firstClickedTimestamp);
			int lastTimeSeconds = getTimeInSeconds(SelectionProperties.lastClickedTimestamp);
			int lastTimeMilliSeconds = getTimeInMilliSeconds(SelectionProperties.lastClickedTimestamp);

			// Go here if first clicked line is smaller than last clicked line
			// -> selecting forward
			if (SelectionProperties.firstClickedLine < SelectionProperties.lastClickedLine) {
				timeDiff = generateLabelString(
						SelectionProperties.firstClickedLine, firstTimeSeconds,
						firstTimeMilliSeconds,
						SelectionProperties.lastClickedLine, lastTimeSeconds,
						lastTimeMilliSeconds);

				// Go here if first clicked line is bigger than last clicked
				// line -> selecting backward
			} else {
				timeDiff = generateLabelString(
						SelectionProperties.lastClickedLine, lastTimeSeconds,
						lastTimeMilliSeconds,
						SelectionProperties.firstClickedLine, firstTimeSeconds,
						firstTimeMilliSeconds);
			}
		}
		TraceViewerGlobals.getTrimProvider().updateText(timeDiff);
	}

	/**
	 * Generates label string
	 * 
	 * @param firstLineNumber
	 *            first line number
	 * @param firstTimeSeconds
	 *            first time seconds
	 * @param firstTimeMilliSeconds
	 *            first time milliseconds
	 * @param lastLineNumber
	 *            last line number
	 * @param lastTimeSeconds
	 *            last time seconds
	 * @param lastTimeMilliSeconds
	 *            last time milliseconds
	 * @return label string
	 */
	private static String generateLabelString(int firstLineNumber,
			int firstTimeSeconds, int firstTimeMilliSeconds,
			int lastLineNumber, int lastTimeSeconds, int lastTimeMilliSeconds) {

		// There cannot be too many traces selected. This makes Select all to
		// show correct amount of traces while still actually selecting also the
		// last trace
		if (lastLineNumber >= TraceViewerGlobals.getTraceViewer()
				.getDataReaderAccess().getCurrentDataReader().getTraceCount()) {
			lastLineNumber = TraceViewerGlobals.getTraceViewer()
					.getDataReaderAccess().getCurrentDataReader()
					.getTraceCount() - 1;
		}

		boolean moreMillisInFirst = false;

		if (firstTimeSeconds > lastTimeSeconds) {
			firstTimeSeconds -= SECONDS_IN_DAY;
		}

		// Take one second off if more milliseconds in the first
		// time
		if (firstTimeMilliSeconds > lastTimeMilliSeconds) {
			moreMillisInFirst = true;
			lastTimeSeconds--;
		}

		int diffSeconds = lastTimeSeconds - firstTimeSeconds;
		int diffMillis;
		if (moreMillisInFirst) {
			diffMillis = 1000 - Math.abs(lastTimeMilliSeconds
					- firstTimeMilliSeconds);
		} else {
			diffMillis = Math.abs(lastTimeMilliSeconds - firstTimeMilliSeconds);
		}

		// String buffer where to construct the text
		StringBuffer labelText = new StringBuffer();
		labelText
				.append(Messages
						.getString("ViewerSelectionChangedListener.TracesSelectedText")); //$NON-NLS-1$
		labelText.append((lastLineNumber - firstLineNumber + 1));
		labelText
				.append(Messages
						.getString("ViewerSelectionChangedListener.TimeDifferenceText")); //$NON-NLS-1$

		// Hours
		int hours = diffSeconds / 3600;
		if (hours < 10) {
			labelText.append('0');
		}
		labelText.append(hours);
		labelText.append(':');

		int minutes = (diffSeconds % 3600) / 60;
		if (minutes < 10) {
			labelText.append('0');
		}
		labelText.append(minutes);
		labelText.append(':');

		int seconds = diffSeconds % 60;
		if (seconds < 10) {
			labelText.append('0');
		}
		labelText.append(seconds);
		labelText.append('.');

		// Milliseconds
		if (diffMillis < 100) {
			labelText.append('0');
		}
		if (diffMillis < 10) {
			labelText.append('0');
		}
		labelText.append(diffMillis);

		return labelText.toString();
	}

	/**
	 * Gets time in seconds from timestamp string
	 * 
	 * @param timestamp
	 *            timestamp string
	 * @return time in seconds
	 */
	private static int getTimeInSeconds(String timestamp) {
		int seconds = 0;
		try {
			seconds = (Integer.parseInt(timestamp.substring(0, 2)) * 3600);
			seconds += (Integer.parseInt(timestamp.substring(3, 5)) * 60);
			seconds += Integer.parseInt(timestamp.substring(6, 8));
		} catch (Exception e) {
		}
		return seconds;
	}

	/**
	 * Gets time in milliseconds from timestamp string
	 * 
	 * @param timestamp
	 *            timestamp string
	 * @return time in milliseconds
	 */
	private static int getTimeInMilliSeconds(String timestamp) {
		int milliseconds = 0;
		try {
			milliseconds += (Float.parseFloat(timestamp.substring(9, 12)));
		} catch (Exception e) {
		}
		return milliseconds;
	}
}
