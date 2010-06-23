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
 * Trace Viewer interface
 *
 */
package com.nokia.traceviewer.engine;

import java.util.List;

/**
 * Trace Viewer interface
 */
public interface TraceViewerInterface {

	/**
	 * Registers the trace view to Trace Viewer
	 * 
	 * @param view
	 *            the view
	 */
	public void setTraceView(TraceViewerTraceViewInterface view);

	/**
	 * Registers the property view to Trace Viewer
	 * 
	 * @param view
	 *            the view
	 */
	public void setPropertyView(TraceViewerPropertyViewInterface view);

	/**
	 * Registers dialog implementer
	 * 
	 * @param dialogs
	 *            dialog implementer
	 */
	public void setDialogs(TraceViewerDialogInterface dialogs);

	/**
	 * Gets the dataprocessor access
	 * 
	 * @return dataprocessor access
	 */
	public DataProcessorAccess getDataProcessorAccess();

	/**
	 * Gets the datareader access
	 * 
	 * @return datareader access
	 */
	public DataReaderAccess getDataReaderAccess();

	/**
	 * Gets the connection
	 * 
	 * @return the connection
	 */
	public Connection getConnection();

	/**
	 * Sets the file handler
	 * 
	 * @param fileHandler
	 *            the new file handler
	 */
	public void setFileHandler(TraceFileHandler fileHandler);

	/**
	 * Gets the file handler
	 * 
	 * @return the connection
	 */
	public TraceFileHandler getFileHandler();

	/**
	 * Gets the trace view
	 * 
	 * @return the view
	 */
	public TraceViewerTraceViewInterface getView();

	/**
	 * Gets the property view
	 * 
	 * @return the property view
	 */
	public TraceViewerPropertyViewInterface getPropertyView();

	/**
	 * Gets the dialogs
	 * 
	 * @return the dialogs
	 */
	public TraceViewerDialogInterface getDialogs();

	/**
	 * Gets StateHolder
	 * 
	 * @return the state holder
	 */
	public StateHolder getStateHolder();

	/**
	 * Connects to trace source using connection properties
	 * 
	 * @return TVAPI error code
	 */
	public boolean connect();

	/**
	 * Disconnects from trace source
	 * 
	 * @return true if disconnection succeeded, false otherwise
	 */
	public boolean disconnect();

	/**
	 * Controlled shutdown
	 */
	public void shutdown();

	/**
	 * Clears all data
	 */
	public void clearAllData();

	/**
	 * Reads the trace data file from the beginning. First clears the views and
	 * then sets the data file position to the beginning of the file.
	 */
	public void readDataFileFromBeginning();

	/**
	 * Empties both views
	 */
	public void emptyViews();

	/**
	 * Get traces from the file between start and end indices. Start and end
	 * traces are also included. Maximum number of traces to get with this
	 * method is 5 times blocksize.
	 * 
	 * @param start
	 *            the first trace to get
	 * @param end
	 *            the last trace to get
	 * @return list of traces
	 */
	public List<TraceProperties> getTraces(int start, int end);
}
