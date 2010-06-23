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
 * External Filter Data Writer
 *
 */
package com.nokia.traceviewer.engine.dataprocessor;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

import com.nokia.traceviewer.engine.TraceProperties;
import com.nokia.traceviewer.engine.TraceViewerGlobals;

/**
 * External Filter Data Writer sends data to Processes output stream
 */
public class ExternalFilterDataWriter {

	/**
	 * Output stream writer
	 */
	private final BufferedWriter writer;

	/**
	 * Constructor
	 * 
	 * @param os
	 *            output stream
	 */
	public ExternalFilterDataWriter(OutputStream os) {
		OutputStreamWriter osw = new OutputStreamWriter(os);
		writer = new BufferedWriter(osw);
	}

	/**
	 * Writes trace to Process input stream
	 * 
	 * @param properties
	 *            trace properties
	 */
	public void writeTrace(TraceProperties properties) {
		try {
			StringBuffer buf = new StringBuffer();
			if (properties.timestampString != null) {
				buf.append(properties.timestampString);
				buf.append(TraceViewerGlobals.getTraceViewer()
						.getDataProcessorAccess().getTimestampParser()
						.getTimeFromPreviousString(
								properties.timeFromPreviousTrace));
				buf.append('\t');
			}
			if (properties.traceString != null) {
				buf.append(properties.traceString);
			}
			buf.append('\n');
			writer.write(buf.toString());

			// Flush when last trace is written
			if (properties.lastTrace) {
				writer.flush();
			}
		} catch (IOException e) {
			// If writing goes wrong, shut down possible progressbar
			e.printStackTrace();
			closeProgressBar();
		}
	}

	/**
	 * Closes progressbar
	 */
	private void closeProgressBar() {
		if (TraceViewerGlobals.getTraceViewer().getDataProcessorAccess()
				.getFilterProcessor().isProcessingFilter()) {

			// Close progressbar
			TraceViewerGlobals.getTraceViewer().getView().closeProgressBar(
					TraceViewerGlobals.getTraceViewer()
							.getDataProcessorAccess().getFilterProcessor()
							.getFilterDialog().getProgressBar());
		}
	}

	/**
	 * Shuts down the writer
	 */
	public void shutDown() {
		try {
			writer.close();
		} catch (IOException e) {
			// If writing goes wrong, shut down possible progressbar
			// e.printStackTrace();
			closeProgressBar();
		}
	}
}
