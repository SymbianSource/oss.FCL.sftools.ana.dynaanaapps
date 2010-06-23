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
 * Select all action
 *
 */
package com.nokia.traceviewer.action;

import java.util.List;

import org.eclipse.swt.custom.StyledText;

import com.nokia.traceviewer.engine.TraceProperties;
import com.nokia.traceviewer.engine.TraceViewerGlobals;
import com.nokia.traceviewer.view.listener.SelectionProperties;
import com.nokia.traceviewer.view.listener.ViewerSelectionChangedListener;

/**
 * Select all action
 */
final class SelectAllAction extends TraceViewerAction {

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.traceviewer.action.TraceViewerAction#doRun()
	 */
	@Override
	protected void doRun() {
		StyledText widget = TraceViewerGlobals.getTraceViewer().getView()
				.getViewer().getTextWidget();

		int totalTraceCount = TraceViewerGlobals.getTraceViewer()
				.getDataReaderAccess().getCurrentDataReader().getTraceCount();

		// Set selection properties
		SelectionProperties.firstClickedLine = 0;
		SelectionProperties.firstClickedLineCaretOffset = 0;
		SelectionProperties.lastClickedLine = totalTraceCount;

		// Save current top index
		int topIndex = widget.getTopIndex();

		// Select everything from the current showing text widget
		int endOffset = widget.getOffsetAtLine(widget.getLineCount() - 1);
		SelectionProperties.lastClickedLineCaretOffset = 0;
		widget.setSelection(0, endOffset);

		// Return old top index
		widget.setTopIndex(topIndex);

		// Get first trace
		List<TraceProperties> firstTraceArr = TraceViewerGlobals
				.getTraceViewer().getTraces(0, 0);
		if (!firstTraceArr.isEmpty()) {
			TraceProperties firstTrace = firstTraceArr.get(0);

			// Process timestamp
			TraceViewerGlobals.getTraceViewer().getDataProcessorAccess()
					.getTimestampParser().processData(firstTrace);

			if (firstTrace.timestampString != null) {
				SelectionProperties.firstClickedTimestamp = firstTrace.timestampString;
			}

		}

		// Get last trace
		List<TraceProperties> lastTraceArr = TraceViewerGlobals
				.getTraceViewer().getTraces(totalTraceCount - 1,
						totalTraceCount - 1);
		if (!lastTraceArr.isEmpty()) {
			TraceProperties lastTrace = lastTraceArr.get(0);

			// Process timestamp
			TraceViewerGlobals.getTraceViewer().getDataProcessorAccess()
					.getTimestampParser().processData(lastTrace);

			if (lastTrace.timestampString != null) {
				SelectionProperties.lastClickedTimestamp = lastTrace.timestampString;
			}

		}
		ViewerSelectionChangedListener.handleTrimInformationUpdate();
	}
}
