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
 * TraceViewer view key listener
 *
 */
package com.nokia.traceviewer.view.listener;

import org.eclipse.jface.text.TextViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.widgets.Slider;

import com.nokia.traceviewer.engine.StateHolder;
import com.nokia.traceviewer.engine.TraceViewerGlobals;
import com.nokia.traceviewer.engine.TraceViewerTraceViewInterface;
import com.nokia.traceviewer.engine.TraceViewerUtils;
import com.nokia.traceviewer.view.TraceViewerView;

/**
 * TraceViewer view key listener
 * 
 */
public class ViewerKeyListener implements KeyListener {

	/**
	 * The slider in the view
	 */
	private final Slider slider;

	/**
	 * The TextViewer in the view
	 */
	private final TextViewer viewer;

	/**
	 * TraceViewer view
	 */
	private final TraceViewerView view;

	/**
	 * Boolean determining if view should be updated after arrow up/down
	 */
	private boolean shouldBeUpdated;

	/**
	 * Font size
	 */
	private int fontSize = TraceViewerTraceViewInterface.FONT_SIZE;

	/**
	 * Max font size
	 */
	private static final int MAX_FONT_SIZE = 100;

	/**
	 * Constructor
	 * 
	 * @param slider
	 *            reference to the slider
	 * @param viewer
	 *            reference to the viewer
	 * @param view
	 *            reference to the view
	 */
	public ViewerKeyListener(Slider slider, TextViewer viewer,
			TraceViewerView view) {
		this.slider = slider;
		this.viewer = viewer;
		this.view = view;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.swt.events.KeyListener#keyPressed(org.eclipse.swt.events.
	 * KeyEvent)
	 */
	public void keyPressed(KeyEvent e) {
		int keyCode = e.keyCode;
		int showingTracesFrom = TraceViewerGlobals.getTraceViewer().getView()
				.getShowingTracesFrom();

		switch (keyCode) {

		// Page up pressed
		case SWT.PAGE_UP:
			// If there is at least one full page left in up of the cursor, we
			// just move the slider to where top index is
			if (viewer.getTopIndex() > (viewer.getBottomIndex() - viewer
					.getTopIndex())) {
				slider.setSelection(showingTracesFrom + viewer.getTopIndex());

				// If there is not at least one full page left, we move the
				// slider with one thumb
			} else {
				slider.setSelection(slider.getSelection() - slider.getThumb());
				TraceViewerGlobals.getTraceViewer().getStateHolder().setState(
						StateHolder.State.SCROLLING_WITH_PAGEUPDOWN);
			}

			break;

		// Page down pressed
		case SWT.PAGE_DOWN:
			// If there is at least full page left down from the cursor, we
			// just move the slider to where top index is
			if (viewer.getTopIndex() + viewer.getBottomIndex()
					- viewer.getTopIndex() + 2 < viewer.getTextWidget()
					.getLineCount()) {

				slider.setSelection(showingTracesFrom + viewer.getTopIndex());

				// If there is not full page left, we move the
				// slider with one thumb
			} else {
				slider.setSelection(slider.getSelection() + slider.getThumb());
				TraceViewerGlobals.getTraceViewer().getStateHolder().setState(
						StateHolder.State.SCROLLING_WITH_PAGEUPDOWN);
			}

			break;

		case SWT.ARROW_UP:
			// There is at least 1 row in top of the view, just move slider to
			// where top index is
			if (viewer.getTopIndex() > 0) {
				slider.setSelection(showingTracesFrom + viewer.getTopIndex());
			} else {
				// When we are in the first row, the second time is when we want
				// to update
				if (shouldBeUpdated) {
					TraceViewerGlobals.getTraceViewer().getStateHolder()
							.setState(StateHolder.State.SCROLLING_WITH_ARROWS);
					slider.setSelection(showingTracesFrom - 1);
					shouldBeUpdated = false;
				} else {
					shouldBeUpdated = true;
				}

			}
			removePossibleSelection(e.stateMask);
			break;
		case SWT.ARROW_DOWN:
			// There is at least 1 row in bottom of the view, just move slider
			// to where top index is
			if (viewer.getBottomIndex() < viewer.getTextWidget().getLineCount() - 1) {
				slider.setSelection(showingTracesFrom + viewer.getTopIndex());
			} else {
				TraceViewerGlobals.getTraceViewer().getStateHolder().setState(
						StateHolder.State.SCROLLING_WITH_ARROWS);
				slider.setSelection(slider.getSelection() + 1);
			}
			removePossibleSelection(e.stateMask);
			break;
		case SWT.ARROW_LEFT:
			// Fall through
		case SWT.ARROW_RIGHT:
			removePossibleSelection(e.stateMask);
			break;
		case SWT.HOME:
			// Ctrl was used
			if ((e.stateMask & SWT.CTRL) == SWT.CTRL) {

				// Shift was used
				if ((e.stateMask & SWT.SHIFT) == SWT.SHIFT) {
					SelectionProperties.lastClickedLine = 0;
					SelectionProperties.lastClickedLineCaretOffset = 0;
					SelectionProperties.lastClickedTimestamp = TraceViewerUtils
							.getTimestampStringForTrace(0);

				} else {
					SelectionProperties.firstClickedLine = 0;
					SelectionProperties.firstClickedLineCaretOffset = 0;
					SelectionProperties.firstClickedTimestamp = TraceViewerUtils
							.getTimestampStringForTrace(0);
					SelectionProperties.lastClickedLine = -1;
					SelectionProperties.lastClickedTimestamp = ""; //$NON-NLS-1$
				}

				// If we are in the first block
				if (isInFirstBlock()) {
					// Update trim and set selection
					ViewerSelectionChangedListener
							.handleTrimInformationUpdate();
					view.setSelection();
				}

				// Jump to first line
				viewer.getTextWidget().setCaretOffset(0);
				slider.setSelection(0);

				// Ctrl was not used
			} else {
				removePossibleSelection(e.stateMask);
			}

			break;
		case SWT.END:
			// Ctrl was used
			if ((e.stateMask & SWT.CTRL) == SWT.CTRL) {
				int traceCount = TraceViewerGlobals.getTraceViewer()
						.getDataReaderAccess().getCurrentDataReader()
						.getTraceCount();

				// Shift was used
				if ((e.stateMask & SWT.SHIFT) == SWT.SHIFT) {
					SelectionProperties.lastClickedLine = traceCount;
					SelectionProperties.lastClickedLineCaretOffset = 0;
					SelectionProperties.lastClickedTimestamp = TraceViewerUtils
							.getTimestampStringForTrace(traceCount - 1);
				} else {
					SelectionProperties.firstClickedLine = traceCount;
					SelectionProperties.firstClickedLineCaretOffset = 0;
					SelectionProperties.firstClickedTimestamp = TraceViewerUtils
							.getTimestampStringForTrace(traceCount - 1);
					SelectionProperties.lastClickedLine = -1;
					SelectionProperties.lastClickedTimestamp = ""; //$NON-NLS-1$
				}

				// If we are in the last block
				if (isInLastBlock()) {

					// Set the caret to the end
					viewer.getTextWidget().setCaretOffset(
							viewer.getTextWidget().getCharCount());

					viewer.setTopIndex(traceCount);

					// Update trim and set selection
					ViewerSelectionChangedListener
							.handleTrimInformationUpdate();
					view.setSelection();

					// Not in the last block
				} else {
					SelectionProperties.putCaretToTheEnd = true;
				}

				// Change the slider position
				slider.setSelection(traceCount);

				// Ctrl was not used
			} else {
				removePossibleSelection(e.stateMask);
			}

			break;

		// Search again
		case SWT.F3:
			String previousSearch = TraceViewerGlobals.getTraceViewer()
					.getDataProcessorAccess().getSearchProcessor()
					.getSearchDialog().getPreviousSearchString();

			if (!previousSearch.equals("")) { //$NON-NLS-1$
				TraceViewerGlobals.getTraceViewer().getDataProcessorAccess()
						.getSearchProcessor().searchTraceWithString(
								previousSearch);
			}
			break;
		default:
			break;
		}

		// Font size can be changed with CTRL + "+" or CTRL + "-" chars
		if (e.stateMask == SWT.CTRL) {
			switch (e.character) {
			case '+':
				if (fontSize++ <= MAX_FONT_SIZE) {
					TraceViewerGlobals.getTraceViewer().getView().setFontSize(
							fontSize);
				} else {
					fontSize = MAX_FONT_SIZE;
				}

				break;
			case '-':
				if (fontSize-- > 0) {
					TraceViewerGlobals.getTraceViewer().getView().setFontSize(
							fontSize);
				} else {
					fontSize = 1;
				}
				break;
			default:
				break;
			}

		}

		// Scroll to new line or get more data
		TraceViewerGlobals.getTraceViewer().getView().scrollViewToLine(
				slider.getSelection());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.swt.events.KeyListener#keyReleased(org.eclipse.swt.events
	 * .KeyEvent)
	 */
	public void keyReleased(KeyEvent e) {

	}

	/**
	 * Checks if we are in the last block of traces
	 * 
	 * @return true if we are in the last block of traces
	 */
	private boolean isInLastBlock() {
		boolean lastBlock = false;
		if (TraceViewerGlobals.getTraceViewer().getView()
				.getShowingTracesFrom()
				+ (2 * TraceViewerGlobals.blockSize) > TraceViewerGlobals
				.getTraceViewer().getDataReaderAccess().getCurrentDataReader()
				.getTraceCount()) {
			lastBlock = true;
		}
		return lastBlock;

	}

	/**
	 * Checks if we are in the first block of traces
	 * 
	 * @return true if we are in the first block of traces
	 */
	private boolean isInFirstBlock() {
		boolean firstBlock = false;
		if (TraceViewerGlobals.getTraceViewer().getView()
				.getShowingTracesFrom() == 0) {
			firstBlock = true;
		}
		return firstBlock;

	}

	/**
	 * Removes possible selection
	 * 
	 * @param stateMask
	 *            state mask for the key
	 */
	private void removePossibleSelection(int stateMask) {
		if (stateMask != SWT.SHIFT) {
			SelectionProperties.clear();
		}
	}
}
