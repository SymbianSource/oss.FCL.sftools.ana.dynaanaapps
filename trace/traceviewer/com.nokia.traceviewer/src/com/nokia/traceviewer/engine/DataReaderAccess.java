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
 * Data Reader Access class
 *
 */
package com.nokia.traceviewer.engine;

import java.util.ArrayList;
import java.util.List;

import com.nokia.traceviewer.engine.dataprocessor.FilterProcessor;

/**
 * DataReader Access class
 */
public class DataReaderAccess {

	/**
	 * Main file path
	 */
	private final String filePath;

	/**
	 * Callback where dataReaders return data
	 */
	private final MediaCallback mediaCallback;

	/**
	 * The Main dataReader which is always reading the binary file
	 */
	private DataReader mainDataReader;

	/**
	 * Reference to the current dataReader reading data to view
	 */
	private DataReader currentDataReader;

	/**
	 * DataReader which handles scrolling
	 */
	private DataScrollReader scrollReader;

	/**
	 * Datareader which reads only the filter file
	 */
	private DataReader filterDataReader;

	/**
	 * List of media callbacks which should be informed when file handle is
	 * changed
	 */
	private List<MediaCallback> ownMediaCallbacks;

	/**
	 * File start offset. Is non-zero when triggering was on.
	 */
	private long fileStartOffset;

	/**
	 * Constructor
	 * 
	 * @param mediaCallback
	 *            media callback
	 * @param fileName
	 *            file name
	 */
	public DataReaderAccess(MediaCallback mediaCallback, String fileName) {
		this.mediaCallback = mediaCallback;
		this.filePath = fileName;
	}

	/**
	 * Creates the main DataReader
	 */
	public void createMainDataReader() {
		// Create Main reader
		TraceConfiguration conf = new TraceConfiguration();
		conf.setScrolledTrace(false);
		conf.setFilteredOut(false);
		conf.setReadFromFilterFile(false);

		if (mainDataReader != null) {
			mainDataReader.shutdown();
		}
		mainDataReader = TraceViewerGlobals.getTraceProvider()
				.createDataReader(mediaCallback, conf);

		mainDataReader.setFilePath(filePath);
		mainDataReader.setFileStartOffset(fileStartOffset);
		mainDataReader.start();

		// Not filtering
		if (!TraceViewerGlobals.getTraceViewer().getDataProcessorAccess()
				.getFilterProcessor().isFiltering()) {
			setCurrentDataReader(mainDataReader);
			// Filtering, create filter data reader
		} else {
			TraceViewerGlobals.getTraceViewer().getDataProcessorAccess()
					.getFilterProcessor().buildFilterFileWriter();
		}

		if (scrollReader != null) {
			scrollReader.shutdown();
			scrollReader = null;
		}
	}

	/**
	 * Gets current data reader
	 * 
	 * @return the current data reader
	 */
	public DataReader getCurrentDataReader() {
		return currentDataReader;
	}

	/**
	 * Sets current data reader
	 * 
	 * @param reader
	 *            new current data reader
	 */
	public void setCurrentDataReader(DataReader reader) {
		currentDataReader = reader;
		notifyOwnCallbacks();
	}

	/**
	 * Gets the main data reader
	 * 
	 * @return main data reader
	 */
	public DataReader getMainDataReader() {
		return mainDataReader;
	}

	/**
	 * Sets the main data reader
	 * 
	 * @param reader
	 *            new main data reader
	 */
	public void setMainDataReader(DataReader reader) {
		if (mainDataReader != null) {
			mainDataReader.shutdown();
		}
		mainDataReader = reader;
	}

	/**
	 * Gets the scroll reader
	 * 
	 * @return scroll reader
	 */
	public DataScrollReader getScrollReader() {
		return scrollReader;
	}

	/**
	 * Sets the scroll reader
	 * 
	 * @param scrollReader
	 *            new scroll reader
	 */
	public void setScrollReader(DataScrollReader scrollReader) {
		this.scrollReader = scrollReader;
	}

	/**
	 * Deletes current scroll reader
	 */
	public void deleteScrollReader() {
		if (scrollReader != null) {
			scrollReader.shutdown();
			scrollReader = null;
		}
	}

	/**
	 * Deletes current filter reader
	 */
	public void deleteFilterReader() {
		if (filterDataReader != null) {
			filterDataReader.shutdown();
			filterDataReader = null;
		}
	}

	/**
	 * Sets the file start offset
	 * 
	 * @param offset
	 *            file start offset
	 */
	public void setFileStartOffset(long offset) {
		this.fileStartOffset = offset;
	}

	/**
	 * Start scroll reader
	 * 
	 * @param offset
	 *            start offset
	 * @param numberOfBlocks
	 *            number of blocks
	 */
	public void startScrollReader(int offset, int numberOfBlocks) {
		// Get the file position having given offset
		int index = currentDataReader.getFileMap().getIndexFromOffset(offset);
		long pos = currentDataReader.getFileMap().getItem(index).longValue();
		int startTrace = index * TraceViewerGlobals.blockSize;
		boolean scrollReadedExisted = true;

		if (scrollReader == null) {
			scrollReadedExisted = false;
			// Sets this reader to read only blocks
			TraceConfiguration conf = new TraceConfiguration();
			conf.setScrolledTrace(true);
			// Check if filtering
			if (TraceViewerGlobals.getTraceViewer().getDataProcessorAccess()
					.getFilterProcessor().isFiltering()) {
				conf.setReadFromFilterFile(true);
			}

			// Create scroll reader the same type as current data reader is
			scrollReader = currentDataReader.createScrollReader(mediaCallback,
					conf);
		}

		scrollReader.setBlockReader(numberOfBlocks, startTrace, true);
		scrollReader.setFilePosition(pos);

		// Synchronize the threads
		synchronized (scrollReader) {
			if (!scrollReadedExisted) {
				// Start the new thread
				scrollReader.start();
			} else {
				// Wake the thread if it exists
				scrollReader.notifyAll();
			}
			// Put caller thread waiting. Must be careful here because the
			// caller thread is usually the UI thread. If syncExec call happens
			// during the fetching of the data, UI will be blocked forever!
			try {
				scrollReader.wait();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Creates and starts own data reader. When reader is no longer needed, call
	 * shutdown method and set the reader to null.
	 * 
	 * @param reader
	 *            own data reader to start. Should be null when calling this
	 *            method for the first time. Then the return value of this
	 *            method should be inserted to the reader object in the calling
	 *            class
	 * @param callback
	 *            callback class where to return traces read
	 * @param numberOfBlocks
	 *            number of blocks to read
	 * @param pos
	 *            file position to set
	 * @param startTrace
	 *            trace number where to start reading
	 * @param blocking
	 *            if true, blocks the calling thread until all the traces are
	 *            read. Use with caution. If the amount of traces is big,
	 *            program may hang or the stack could get full. Only use
	 *            blocking when reading couple of blocks!
	 * @return reference to the reader
	 */
	public DataScrollReader startOwnDataReader(DataScrollReader reader,
			MediaCallback callback, int numberOfBlocks, long pos,
			int startTrace, boolean blocking) {
		boolean dataReadedExisted = true;

		// Create new general data reader if it doesn't exist or the callback is
		// different
		if (reader == null) {
			dataReadedExisted = false;
			// Sets this reader to read only blocks
			TraceConfiguration conf = new TraceConfiguration();
			conf.setScrolledTrace(true);
			// Check if filtering
			if (TraceViewerGlobals.getTraceViewer().getDataProcessorAccess()
					.getFilterProcessor().isFiltering()) {
				conf.setReadFromFilterFile(true);
			}

			// Create own data reader the same type as current data reader is
			reader = currentDataReader.createScrollReader(callback, conf);
			addMediaCallbackToList(callback);
		}

		reader.setBlockReader(numberOfBlocks, startTrace, blocking);
		reader.setFilePosition(pos);

		// Synchronize the threads
		synchronized (reader) {
			if (!dataReadedExisted) {
				// Start the new thread
				reader.start();
			} else {
				// Wake the thread if it exists
				reader.notifyAll();
			}
			if (blocking) {
				// Put caller thread waiting
				try {
					reader.wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
		return reader;
	}

	/**
	 * Creates filter data reader
	 */
	public void createFilterDataReader() {
		// Configuration for filter file reader
		TraceConfiguration conf = new TraceConfiguration();
		conf.setScrolledTrace(false);
		conf.setReadFromFilterFile(true);

		// Shut down possible old one
		deleteFilterReader();

		// Create binary filter file data reader
		if (!TraceViewerGlobals.getTraceViewer().getDataProcessorAccess()
				.getFilterProcessor().isUsingExternalFilter()
				&& !(mainDataReader instanceof PlainTextReader)) {
			filterDataReader = TraceViewerGlobals.getTraceProvider()
					.createDataReader(mediaCallback, conf);

			// Create plain text filter file data reader
		} else {
			filterDataReader = new PlainTextReader(mediaCallback, conf);
		}

		filterDataReader.setFilePath(FilterProcessor.DEFAULT_FILTER_FILE_PATH);

		// Set as current datareader. Will also notify own callbacks.
		setCurrentDataReader(filterDataReader);

		// Delete old scrollReader
		deleteScrollReader();

		// Start the reader
		filterDataReader.start();
	}

	/**
	 * Creates log file data reader
	 * 
	 * @param file
	 *            file name
	 * @param binary
	 *            is the log file binary
	 */
	public void createLogFileReader(String file, boolean binary) {
		// Configuration for log file reader
		TraceConfiguration conf = new TraceConfiguration();
		conf.setScrolledTrace(false);
		conf.setReadFromFilterFile(false);
		conf.setFilteredOut(false);

		// New main reader
		DataReader newReader = null;

		// Binary file
		if (binary) {
			// Create new DataReader
			newReader = TraceViewerGlobals.getTraceProvider().createDataReader(
					mediaCallback, conf);

			// Plain text file
		} else {
			newReader = new PlainTextReader(mediaCallback, conf);
		}
		newReader.setFilePath(file);

		// Set the new reader to main reader
		setMainDataReader(newReader);

		// If not filtering, set as currentDataReader
		if (!TraceViewerGlobals.getTraceViewer().getDataProcessorAccess()
				.getFilterProcessor().isFiltering()) {
			setCurrentDataReader(newReader);

			// If filtering, empty old filter file and create new filter data
			// reader
		} else {
			TraceViewerGlobals.getTraceViewer().getDataProcessorAccess()
					.getFilterProcessor().buildFilterFileWriter();
			createFilterDataReader();
		}

		TraceViewerGlobals.getTraceViewer().getDataProcessorAccess()
				.getLogger().setLogFileOpened(true);

		// Import possible trace comments first
		TraceViewerGlobals.getTraceViewer().getDataProcessorAccess()
				.getTraceCommentHandler().importTraceComments(file);

		// Start the reader
		newReader.start();
	}

	/**
	 * Opens log file
	 * 
	 * @param file
	 *            file to be opened
	 * @param binary
	 *            is this binary file
	 */
	public void openLogFile(String file, boolean binary) {
		// Delete old scrollReader and generalReader
		deleteScrollReader();
		notifyOwnCallbacks();

		// Stop reader if it exists
		if (getMainDataReader() != null) {

			// If paused, unpause
			boolean paused = getMainDataReader().isPaused();
			getMainDataReader().shutdown();

			if (paused) {
				TraceViewerGlobals.getTraceViewer().getView()
						.getActionFactory().getPauseAction().run();
			}
		}

		// Clear everything
		TraceViewerGlobals.getTraceViewer().getDataProcessorAccess()
				.getLogger().setLogFileOpened(false);
		TraceViewerGlobals.getTraceViewer().clearAllData();

		// Disconnect
		if (TraceViewerGlobals.getTraceViewer().getConnection() != null
				&& TraceViewerGlobals.getTraceViewer().getConnection()
						.isConnected()) {
			TraceViewerGlobals.getTraceViewer().getView().getActionFactory()
					.getConnectAction().run();
		}

		// Remove possible triggers
		TraceViewerGlobals.getTraceViewer().getDataProcessorAccess()
				.getTriggerProcessor().removeTriggers();

		// Create and start log file reader
		createLogFileReader(file, binary);
	}

	/**
	 * Notifies own callbacks that file handle has changed. They should shutdown
	 * the reader and set it null.
	 */
	private void notifyOwnCallbacks() {
		if (ownMediaCallbacks != null) {
			for (int i = 0; i < ownMediaCallbacks.size(); i++) {
				MediaCallback callback = ownMediaCallbacks.get(i);
				if (callback != null) {
					callback.dataHandleChanged();
				}
			}
			ownMediaCallbacks.clear();
		}
	}

	/**
	 * Adds own callback to the list
	 * 
	 * @param callback
	 *            own callback to be added
	 */
	private void addMediaCallbackToList(MediaCallback callback) {

		// Create the array
		if (ownMediaCallbacks == null) {
			ownMediaCallbacks = new ArrayList<MediaCallback>();
		}

		// Add to the list
		if (!ownMediaCallbacks.contains(callback)) {
			ownMediaCallbacks.add(callback);
		}
	}
}
