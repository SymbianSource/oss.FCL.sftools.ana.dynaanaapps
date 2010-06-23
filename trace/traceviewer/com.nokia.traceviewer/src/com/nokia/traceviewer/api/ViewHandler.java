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
 * View handler handles operations that require TraceViewer view 
 *
 */
package com.nokia.traceviewer.api;

import org.eclipse.ui.PlatformUI;

import com.nokia.traceviewer.api.TraceViewerAPI.TVAPIError;
import com.nokia.traceviewer.engine.TraceViewerGlobals;

/**
 * View handler handles operations that require TraceViewer view
 */
final class ViewHandler {

	/**
	 * Private variable to be able to change it in inner class
	 */
	private TVAPIError syncToTimestampRet;

	/**
	 * Stops or restarts the TraceViewer view update
	 * 
	 * @param stop
	 *            if true, stops the view update. If false, restarts the update.
	 */
	public void stopViewUpdate(boolean stop) {
		if (TraceViewerGlobals.getTraceViewer().getView() != null) {
			TraceViewerGlobals.getTraceViewer().getView().stopViewUpdate(stop);
		}
	}

	/**
	 * Syncs to timestamp in the TraceViewer view. If both start and end
	 * timestamps are given, the range is selected. This function assumes that
	 * the traces in the TraceViewer view are in chronological order. Also,
	 * endTimestamp must always be "bigger" than startTimestamp
	 * 
	 * @param startTimestamp
	 *            start timestamp in the format of hh:mm:ss.SSS
	 * @param endTimestamp
	 *            end timestamp in the format of hh:mm:ss.SSS or null if only
	 *            start timestamp is searched for
	 * @return error code from TraceViewerAPI
	 */
	public TVAPIError syncToTimestamp(final String startTimestamp,
			final String endTimestamp) {
		syncToTimestampRet = TVAPIError.NONE;

		if (TraceViewerGlobals.getTraceViewer().getView() != null) {

			PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {

				/*
				 * (non-Javadoc)
				 * 
				 * @see java.lang.Runnable#run()
				 */
				public void run() {

					// Check that the view is not disposed
					if (!TraceViewerGlobals.getTraceViewer().getView()
							.isDisposed()) {

						// Start searching for the timestamp(s)
						TraceViewerGlobals.getTraceViewer()
								.getDataProcessorAccess().getSearchProcessor()
								.searchTraceWithTimestamp(startTimestamp,
										endTimestamp);
					} else {
						syncToTimestampRet = TVAPIError.TRACE_VIEW_NOT_OPEN;
					}

				}
			});

		} else {
			syncToTimestampRet = TVAPIError.TRACE_VIEW_NOT_OPEN;
		}

		return syncToTimestampRet;
	}

	/**
	 * Syncs to trace in the TraceViewer view
	 * 
	 * @param startTrace
	 *            start trace number
	 * @param endTrace
	 *            end trace number of 0 if only start trace is searched for
	 * @return error code from TraceViewerAPI
	 */
	public TVAPIError syncToTrace(int startTrace, int endTrace) {
		TVAPIError ret = TVAPIError.NONE;

		if (TraceViewerGlobals.getTraceViewer().getView() != null) {
			int start = startTrace;
			int end = endTrace;

			// Decrease lines by one because actually traces start from index 0
			if (start > 0) {
				start--;
			}
			if (end > 0) {
				end--;
			}

			// Highlight lines
			TraceViewerGlobals.getTraceViewer().getView().highlightLines(start,
					end, true);

		} else {
			ret = TVAPIError.TRACE_VIEW_NOT_OPEN;
		}

		return ret;
	}

}
