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
 * Viewer Mouse Click Listener class
 *
 */
package com.nokia.traceviewer.view.listener;

import java.util.List;

import org.eclipse.jface.text.TextViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.graphics.Point;

import com.nokia.traceviewer.action.OpenTraceLocationAction;
import com.nokia.traceviewer.engine.TraceMetaData;
import com.nokia.traceviewer.engine.TraceProperties;
import com.nokia.traceviewer.engine.TraceViewerGlobals;
import com.nokia.traceviewer.engine.TraceViewerUtils;

/**
 * Viewer Mouse Click listener
 * 
 */
public class ViewerClickListener implements MouseListener {

	/**
	 * Left mouse button
	 */
	private static final int BUTTON_1 = 1;

	/**
	 * Right mouse button
	 */
	private static final int BUTTON_3 = 3;

	/**
	 * Double click count
	 */
	private static final int DOUBLE_CLICK_COUNT = 2;

	/**
	 * Triple click count
	 */
	private static final int TRIPLE_CLICK_COUNT = 3;

	/**
	 * Text viewer
	 */
	private final TextViewer viewer;

	/**
	 * Constructor
	 * 
	 * @param viewer
	 */
	public ViewerClickListener(TextViewer viewer) {
		this.viewer = viewer;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.swt.events.MouseListener#mouseDoubleClick(org.eclipse.swt
	 * .events.MouseEvent)
	 */
	public void mouseDoubleClick(MouseEvent event) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.swt.events.MouseListener#mouseDown(org.eclipse.swt.events
	 * .MouseEvent)
	 */
	public void mouseDown(MouseEvent event) {
		if (event.button == BUTTON_1) {
			// If shift was not pressed, save the line where click happened
			if ((event.stateMask & SWT.SHIFT) == 0) {

				// Single click
				if (event.count == 1) {
					saveClickedLineProperties(event, true);

					// Double click, jump to source code
				} else if (event.count == DOUBLE_CLICK_COUNT) {
					jumpToSource(event);

					// Triple click, remove one character from the selection
				} else if (event.count == TRIPLE_CLICK_COUNT) {
					Point point = viewer.getSelectedRange();
					viewer.setSelectedRange(point.x, point.y - 1);
				}

			} else {
				// Shift was pressed, select lines from viewer if necessary
				saveClickedLineProperties(event, false);
				selectLinesFromViewer(event);
			}

			// Select the line with right mouse button
		} else if (event.button == BUTTON_3) {
			String text = viewer.getTextWidget().getText();
			if (text.length() > 0) {
				int startOffset = viewer.getTextWidget().getSelectionRange().x - 1;
				int endOffset = startOffset
						+ viewer.getTextWidget().getSelectionRange().y + 1;
				if (startOffset < 0) {
					startOffset = 0;
				}
				if (endOffset >= text.length()) {
					endOffset = text.length() - 1;
				}
				char c1 = text.charAt(startOffset);
				char c2 = text.charAt(endOffset);
				if (!viewer.getTextWidget().getSelectionText().contains("\n") //$NON-NLS-1$
						&& (viewer.getTextWidget().getSelectionCount() == 0
								|| (c1 == '\n' && c2 == '\n') || startOffset == 0)) {

					// Save the line number
					int clickedLine = getClickedLine(event)
							+ TraceViewerGlobals.getTraceViewer().getView()
									.getShowingTracesFrom();

					SelectionProperties.firstClickedLine = clickedLine;
					SelectionProperties.lastClickedLine = clickedLine;

					selectClickedLine(event);
				}
			}
		}
	}

	/**
	 * Saves clicked lines properties
	 * 
	 * @param event
	 *            mouse event
	 * @param firstClick
	 *            if true, save line properties to first click, otherwise to
	 *            last click
	 */
	private void saveClickedLineProperties(MouseEvent event, boolean firstClick) {
		// Get timestamp length
		int timestampLength = TraceViewerGlobals.getTraceViewer()
				.getDataProcessorAccess().getTimestampParser()
				.getTimestampStringLength();

		// Save the line number
		int clickedLine = getClickedLine(event)
				+ TraceViewerGlobals.getTraceViewer().getView()
						.getShowingTracesFrom();

		int offsetAtLine = viewer.getTextWidget().getOffsetAtLine(
				getClickedLine(event));

		if (offsetAtLine + timestampLength > viewer.getTextWidget()
				.getCharCount()
				&& getClickedLine(event) > 0) {
			int line = getClickedLine(event) - 1;
			if (line > 0 && line < viewer.getTextWidget().getLineCount()) {
				offsetAtLine = viewer.getTextWidget().getOffsetAtLine(line);
			} else {
				// Offset is wrong, make sure we won't save it. Minus some value
				// so the value doesn't go around when something is added to it
				offsetAtLine = Integer.MAX_VALUE - timestampLength - 1;
			}
		}

		int clickedLineCaretOffset = 0;
		String clickedTimestamp = ""; //$NON-NLS-1$
		if (offsetAtLine + timestampLength < viewer.getTextWidget()
				.getCharCount()) {

			// Save the caret offset
			clickedLineCaretOffset = viewer.getTextWidget().getCaretOffset()
					- offsetAtLine;

			// Save the timestamp String
			clickedTimestamp = TraceViewerUtils
					.getTimestampStringForTrace(clickedLine);
		}
		// Save variables to the first click
		if (firstClick) {
			SelectionProperties.firstClickedLine = clickedLine;
			SelectionProperties.firstClickedLineCaretOffset = clickedLineCaretOffset;
			SelectionProperties.firstClickedTimestamp = clickedTimestamp;

			// Null last click values
			SelectionProperties.lastClickedLine = -1;
			SelectionProperties.lastClickedLineCaretOffset = 0;
			SelectionProperties.lastClickedTimestamp = ""; //$NON-NLS-1$
			ViewerSelectionChangedListener.handleTrimInformationUpdate();
		} else {
			// Save variables to the last click
			SelectionProperties.lastClickedLine = clickedLine;
			SelectionProperties.lastClickedLineCaretOffset = clickedLineCaretOffset;
			SelectionProperties.lastClickedTimestamp = clickedTimestamp;
		}
	}

	/**
	 * Selects lines from viewer when clicked with shift down
	 * 
	 * @param event
	 *            mouse click event
	 */
	private void selectLinesFromViewer(MouseEvent event) {
		int showingFrom = TraceViewerGlobals.getTraceViewer().getView()
				.getShowingTracesFrom();
		int currentLine = getClickedLine(event) + showingFrom;
		int caretOffset = viewer.getTextWidget().getCaretOffset();

		// Current line is smaller than the one clicked first
		// Select everything from current line to the first clicked
		// or to the end of the block
		if (currentLine < SelectionProperties.firstClickedLine) {

			// Selection goes out of the block
			if (SelectionProperties.firstClickedLine > showingFrom
					+ viewer.getTextWidget().getLineCount()) {
				int topIndex = viewer.getTopIndex();
				viewer.getTextWidget().setRedraw(false);
				viewer.getTextWidget().setSelection(
						viewer.getTextWidget().getCharCount() - 1, caretOffset);
				viewer.setTopIndex(topIndex);
				viewer.getTextWidget().setRedraw(true);
			}
		} else {
			// Current line is bigger than the one clicked first
			// Select everything upwards from the current line to the first
			// clicked or to the top of the block

			// Selection start goes up from this block
			if (SelectionProperties.firstClickedLine < showingFrom) {
				// Select from start of this block to current pos
				viewer.setSelectedRange(0, caretOffset);
			}
		}

		// Update Trim widget
		ViewerSelectionChangedListener.handleTrimInformationUpdate();
	}

	/**
	 * Selects clicked line
	 * 
	 * @param event
	 *            clicking event
	 */
	private void selectClickedLine(MouseEvent event) {
		int clickedInLine = getClickedLine(event);
		if (clickedInLine < viewer.getTextWidget().getLineCount() - 1) {
			int beginIdx = viewer.getTextWidget()
					.getOffsetAtLine(clickedInLine);
			int endIdx = viewer.getTextWidget().getOffsetAtLine(
					clickedInLine + 1) - 1;

			// Select backwards to keep Viewer in the beginning of the trace
			viewer.getTextWidget().setSelection(endIdx, beginIdx);

			String clickedTimestamp = TraceViewerUtils
					.getTimestampStringForTrace(clickedInLine);

			// Set properties
			SelectionProperties.firstClickedLineCaretOffset = 0;
			SelectionProperties.firstClickedTimestamp = clickedTimestamp;

			SelectionProperties.lastClickedLineCaretOffset = endIdx - beginIdx;
			SelectionProperties.lastClickedTimestamp = clickedTimestamp;

			// Update Trim widget
			ViewerSelectionChangedListener.handleTrimInformationUpdate();
		}
	}

	/**
	 * Gets linenumber where clicking happened
	 * 
	 * @param event
	 *            clicking event
	 * @return line number where clicking happened
	 */
	private int getClickedLine(MouseEvent event) {
		int clickedInLine = viewer.getTextWidget().getLineIndex(event.y);
		return clickedInLine;
	}

	/**
	 * Jumps to source code
	 * 
	 * @param event
	 *            click event
	 */
	private void jumpToSource(MouseEvent event) {
		// Get the line number
		int clickedLine = getClickedLine(event)
				+ TraceViewerGlobals.getTraceViewer().getView()
						.getShowingTracesFrom();

		// Get the trace from the file
		List<TraceProperties> traceList = TraceViewerGlobals.getTraceViewer()
				.getTraces(clickedLine, clickedLine);

		TraceProperties trace = null;
		if (traceList != null && !traceList.isEmpty()) {
			trace = traceList.get(0);
		}

		// If traceInformation is found, jump to source code line
		if (trace != null && trace.information != null) {
			TraceMetaData metaData = TraceViewerGlobals.getDecodeProvider()
					.getTraceMetaData(trace.information);
			if (metaData != null && metaData.getPath() != null) {
				OpenTraceLocationAction openLocationAction = (OpenTraceLocationAction) TraceViewerGlobals
						.getTraceViewer().getView().getActionFactory()
						.getOpenTraceLocationAction();
				openLocationAction.setMetaData(metaData, false);
				openLocationAction.run();
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.eclipse.swt.events.MouseListener#mouseUp(org.eclipse.swt.events.
	 * MouseEvent)
	 */
	public void mouseUp(MouseEvent event) {
	}

}
