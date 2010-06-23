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
 * External Filter Data Reader
 *
 */
package com.nokia.traceviewer.engine.dataprocessor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import com.nokia.traceviewer.engine.TraceConfiguration;
import com.nokia.traceviewer.engine.TraceProperties;
import com.nokia.traceviewer.engine.TraceViewerGlobals;

/**
 * External Filter Data Reader gets data from Processes input stream
 * 
 */
public class ExternalFilterDataReader extends Thread {

	/**
	 * Sleeping time
	 */
	private static final int SLEEPING_TIME = 1000;

	/**
	 * Input stream from the Process
	 */
	private final InputStream is;

	/**
	 * Buffered stream reader
	 */
	private final BufferedReader reader;

	/**
	 * Running boolean
	 */
	private boolean running;

	/**
	 * Trace properties used when inserting a string from external process to
	 * text rule filtering
	 */
	private final TraceProperties properties;

	/**
	 * Constructor
	 * 
	 * @param is
	 *            input stream
	 */
	public ExternalFilterDataReader(InputStream is) {
		this.is = is;
		InputStreamReader isr = new InputStreamReader(is);
		reader = new BufferedReader(isr);
		properties = new TraceProperties(new TraceConfiguration());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Thread#run()
	 */
	@Override
	public void run() {
		running = true;
		try {
			while (running) {
				while ((properties.traceString = reader.readLine()) != null) {
					TraceViewerGlobals.getTraceViewer()
							.getDataProcessorAccess().getFilterProcessor()
							.filterStringFromExternalProcess(properties);

					// If no data available in the stream, flush after every
					// trace
					if (is.available() == 0) {
						TraceViewerGlobals.getTraceViewer()
								.getDataProcessorAccess().getFilterProcessor()
								.flush();
					}
				}
				// No data, sleep for a while before trying again
				Thread.sleep(SLEEPING_TIME);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Shuts down the reader
	 */
	public void shutDown() {
		running = false;
		try {
			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
