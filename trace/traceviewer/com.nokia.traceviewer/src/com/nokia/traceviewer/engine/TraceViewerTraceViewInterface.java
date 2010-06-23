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
 * Interface implemented by the trace view of Trace Viewer
 *
 */
package com.nokia.traceviewer.engine;

import org.eclipse.jface.text.TextViewer;
import org.eclipse.swt.custom.StyleRange;

import com.nokia.traceviewer.action.ActionFactory;
import com.nokia.traceviewer.dialog.ProgressBarDialog;

/**
 * Interface implemented by the trace view of TraceViewer
 */
public interface TraceViewerTraceViewInterface extends TraceViewerViewInterface {

	/**
	 * View font
	 */
	String FONT = "Courier New"; //$NON-NLS-1$

	/**
	 * View font size
	 */
	public final int FONT_SIZE = 8;

	/**
	 * Number of lines to leave before (above) the found line
	 */
	static final int LINES_TO_LEAVE_BEFORE_FOUND_LINE = 6;

	/**
	 * Scrolls view to given line. If line is not visible, more traces are read
	 * from the trace file and then scrolling is done. If lineNumber is smaller
	 * than 0, first blocks of traces are shown. If lineNumber is bigger than
	 * trace count, last blocks of traces are shown. ONLY call from UI thread!
	 * 
	 * @param lineNumber
	 *            line number where to scroll
	 */
	public void scrollViewToLine(int lineNumber);

	/**
	 * Gets viewer from the view
	 * 
	 * @return the viewer
	 */
	public TextViewer getViewer();

	/**
	 * Gets showingTracesFrom variable
	 * 
	 * @return the showingTracesFrom variable
	 */
	public int getShowingTracesFrom();

	/**
	 * Highlights lines from start to end. If end is 0, only start line is
	 * highlighted. Syncs to UI thread using "syncExec" so the update happens
	 * immediately.
	 * 
	 * @param startLine
	 *            start line number to highlight
	 * @param endLine
	 *            end line number to highlight
	 * @param syncToSource
	 *            if true, sync to source code in case of OST trace
	 */

	public void highlightLines(int startLine, int endLine, boolean syncToSource);

	/**
	 * Closes progressbar while progressing something (filtering etc)
	 * 
	 * @param dialog
	 *            progressbar dialog
	 */
	public void closeProgressBar(ProgressBarDialog dialog);

	/**
	 * Colors current view with new color ranges
	 * 
	 * @param ranges
	 *            the ranges to use
	 */
	public void applyColorRules(StyleRange[] ranges);

	/**
	 * Sets the name of the view based on current situation
	 */
	public void updateViewName();

	/**
	 * Gets action factory
	 * 
	 * @return action factory
	 */
	public ActionFactory getActionFactory();

	/**
	 * Stops or restarts the TraceViewer view update
	 * 
	 * @param stop
	 *            if true, stops the view update. If false, restarts the update.
	 */
	public void stopViewUpdate(boolean stop);

	/**
	 * Refreshes current view in TraceViewer view. Is used if the way to show
	 * data (for example timestamp accuracy) in the view is changed.
	 */
	public void refreshCurrentView();

	/**
	 * Sets font size
	 * 
	 * @param size
	 *            the new size
	 */
	public void setFontSize(int size);
}
