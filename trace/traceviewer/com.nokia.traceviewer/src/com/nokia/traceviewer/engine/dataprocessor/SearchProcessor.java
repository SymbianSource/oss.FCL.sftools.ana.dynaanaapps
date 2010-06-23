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
 * SearchProcessor DataProcessor
 *
 */
package com.nokia.traceviewer.engine.dataprocessor;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.FindReplaceDocumentAdapter;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.ui.PlatformUI;

import com.nokia.traceviewer.action.TraceViewerActionUtils;
import com.nokia.traceviewer.dialog.SearchDialog;
import com.nokia.traceviewer.engine.DataProcessorAccess;
import com.nokia.traceviewer.engine.DataReader;
import com.nokia.traceviewer.engine.DataScrollReader;
import com.nokia.traceviewer.engine.MediaCallback;
import com.nokia.traceviewer.engine.StateHolder;
import com.nokia.traceviewer.engine.TraceInformation;
import com.nokia.traceviewer.engine.TraceProperties;
import com.nokia.traceviewer.engine.TraceViewerGlobals;
import com.nokia.traceviewer.engine.TraceViewerDialogInterface.Dialog;

/**
 * SearchProcessor DataProcessor
 */
public final class SearchProcessor implements DataProcessor, MediaCallback {

	/**
	 * Enum indicating the error codes of TraceViewer API operations
	 */
	public enum TimestampSearchPhase {

		/**
		 * Timestamp search is not ongoing
		 */
		NONE,

		/**
		 * Finding end timestamp
		 */
		FINDING_END,

		/**
		 * End found
		 */
		END_FOUND;

	}

	/**
	 * Search dialog used in searching
	 */
	private SearchDialog searchDialog;

	/**
	 * Temporary document used in searching
	 */
	private final IDocument document;

	/**
	 * Buffer to hold data before inserting all to document
	 */
	private final StringBuffer data;

	/**
	 * Finder adapter
	 */
	private final FindReplaceDocumentAdapter finder;

	/**
	 * Search properties
	 */
	private SearchProperties searchProperties;

	/**
	 * Timestamp range end trace number
	 */
	private int timestampEndTrace;

	/**
	 * Tells if we have hit EOF already
	 */
	private boolean hitEOFAlready;

	/**
	 * Tells that we should stop searching
	 */
	private boolean stopSearching;

	/**
	 * Tells that search is ongoing
	 */
	private boolean searchOngoing;

	/**
	 * Array containing trace informations (cid, gid and tid)
	 */
	private final List<TraceInformation> traceInformations;

	/**
	 * Array containing timestamp strings
	 */
	private final List<String> timestampStrings;

	/**
	 * Search data reader
	 */
	private DataScrollReader searchDataReader;

	/**
	 * Timestamp search phase
	 */
	private TimestampSearchPhase timestampSearchPhase = TimestampSearchPhase.NONE;

	/**
	 * Constructor
	 */
	public SearchProcessor() {
		// Create needed elements and arrays
		data = new StringBuffer();
		document = new Document();
		finder = new FindReplaceDocumentAdapter(document);
		searchProperties = new SearchProperties();
		traceInformations = new ArrayList<TraceInformation>();
		timestampStrings = new ArrayList<String>();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.nokia.traceviewer.engine.DataProcessor#processData(com.nokia.traceviewer
	 * .engine.TraceProperties)
	 */
	public void processData(TraceProperties properties) {
		DataProcessorAccess dpa = TraceViewerGlobals.getTraceViewer()
				.getDataProcessorAccess();

		// If binary trace, decode it
		if (properties.binaryTrace) {
			dpa.getDecoder().processData(properties);
		}

		// Parse timestamp
		dpa.getTimestampParser().processData(properties);

		// Traces missing
		if (properties.bTraceInformation.isTraceMissing()) {
			data.append(TraceViewerActionUtils.TRACES_DROPPED_MSG);
		}

		// Timestamp string
		if (properties.timestampString != null) {
			data.append(properties.timestampString);
			data.append(dpa.getTimestampParser().getTimeFromPreviousString(
					properties.timeFromPreviousTrace));
			data.append('\t');

			// Add to the timestampstrings array
			timestampStrings.add(properties.timestampString);

		}

		// Trace string
		data.append(properties.traceString);

		// Trace comment
		if (properties.traceComment != null) {
			data.append(TraceViewerActionUtils.COMMENT_PREFIX);
			data.append(properties.traceComment);
		}
		data.append('\n');

		traceInformations.add(properties.information);

		// Last trace, start search
		if (properties.lastTrace) {
			document.set(data.toString());
			data.setLength(0);

			// Data is received, start search
			startSearch();
			traceInformations.clear();
			timestampStrings.clear();
		}
	}

	/**
	 * Starts search
	 */
	private void startSearch() {
		if (!stopSearching) {
			try {
				int tracesTillNow = searchProperties
						.getCurrentSearchStartLine()
						+ TraceViewerGlobals.blockSize * 2;

				// Get the search start offset
				int searchStartOffset = getSearchStartOffset();

				// Find the trace and get the offset of it
				int offset = findTrace(searchStartOffset);

				// Update progressBar
				searchDialog.updateSearchProgressBar(searchProperties
						.getCurrentSearchStartLine() + 1);

				if (searchProperties.isSearchingForward()) {
					processSearchingForward(tracesTillNow, offset);
				} else {
					processSearchingBackward(offset);
				}

			} catch (BadLocationException e) {
				e.printStackTrace();
				searchOngoing = false;
			}
		} else {
			TraceViewerGlobals.getTraceViewer().getStateHolder().setState(
					StateHolder.State.NORMAL);
			stopSearching = false;
			searchOngoing = false;
		}

	}

	/**
	 * Gets the correct search start offset
	 * 
	 * @return search start offset
	 */
	private int getSearchStartOffset() {
		int searchStartOffset;

		// Set proper offset to start searching from
		if (searchProperties.isSearchingForward()) {
			searchStartOffset = 0;

			// Searching backward
		} else {
			searchStartOffset = document.getLength() - 1;
		}

		return searchStartOffset;
	}

	/**
	 * Finds the trace using search properties
	 * 
	 * @param searchStartOffset
	 *            start offset where to start the search
	 * @return offset of the trace found or -1 if not found
	 * @throws BadLocationException
	 */
	private int findTrace(int searchStartOffset) throws BadLocationException {
		int offset = SearchUtils.NOT_FOUND;

		// Search using component, group and trace ID's
		if (SearchUtils.containsIdQuery(searchProperties.getSearchString())) {
			offset = findTraceIDSearch(searchStartOffset);

			// Timestamp search
		} else if (SearchUtils.containsTimestampQuery(searchProperties
				.getSearchString())) {
			offset = findTraceTimestampSearch(searchStartOffset);

			// Create a normal text search
		} else {
			timestampSearchPhase = TimestampSearchPhase.NONE;
			IRegion region = finder.find(searchStartOffset, searchProperties
					.getSearchString(), searchProperties.isSearchingForward(),
					searchProperties.isCaseSensitive(), searchProperties
							.isWholeWord(), searchProperties.isRegExp());

			if (region != null) {
				offset = region.getOffset();
			}
		}

		return offset;
	}

	/**
	 * Finds the trace using ID search
	 * 
	 * @param searchStartOffset
	 *            search start offset
	 * @return found offset
	 * @throws BadLocationException
	 */
	private int findTraceIDSearch(int searchStartOffset)
			throws BadLocationException {
		int offset;
		timestampSearchPhase = TimestampSearchPhase.NONE;

		// Parse ID's from the search string
		TraceInformation inf = SearchUtils.parseIDsFromString(searchProperties
				.getSearchString());
		int startTrace = document.getLineOfOffset(searchStartOffset);

		// Get the trace number that matches the ID's
		int foundTrace = SearchUtils.findTraceOffsetFromInformations(
				startTrace, inf, traceInformations, searchProperties
						.isSearchingForward());

		if (foundTrace == SearchUtils.NOT_FOUND) {
			offset = SearchUtils.NOT_FOUND;
		} else {
			offset = document.getLineOffset(foundTrace);
		}
		return offset;
	}

	/**
	 * Finds the trace using timestamp search
	 * 
	 * @param searchStartOffset
	 *            search start offset
	 * @return found offset
	 * @throws BadLocationException
	 */
	private int findTraceTimestampSearch(int searchStartOffset)
			throws BadLocationException {
		int offset;
		boolean getFirstTimestamp = true;
		boolean timestampSearchOnGoing = false;
		if (timestampSearchPhase == TimestampSearchPhase.NONE
				|| timestampSearchPhase == TimestampSearchPhase.FINDING_END) {
			timestampSearchOnGoing = true;
		}
		if (SearchUtils.containsTimestampRangeQuery(searchProperties
				.getSearchString())
				&& timestampSearchOnGoing) {
			getFirstTimestamp = false;
			timestampSearchPhase = TimestampSearchPhase.FINDING_END;
		}

		// Parse timestamp from the search string
		String timestamp = SearchUtils.parseTimestampFromString(
				searchProperties.getSearchString(), getFirstTimestamp);

		int startTrace = document.getLineOfOffset(searchStartOffset);

		// Get the trace which matches the timestamp
		int foundTrace = SearchUtils.findTraceOffsetFromTimestamps(startTrace,
				timestamp, timestampStrings, searchProperties
						.isSearchingForward());

		if (foundTrace == SearchUtils.NOT_FOUND) {
			offset = SearchUtils.NOT_FOUND;
		} else {
			offset = document.getLineOffset(foundTrace);
		}
		return offset;
	}

	/**
	 * Processes situation when searching forward
	 * 
	 * @param tracesTillNow
	 *            Tells how many traces is read from file
	 * @param offset
	 *            offset to found item
	 * @throws BadLocationException
	 */
	private void processSearchingForward(int tracesTillNow, int offset)
			throws BadLocationException {

		// String found
		if (offset != SearchUtils.NOT_FOUND) {
			processStringFound(offset);

			// Full round processed
		} else if (tracesTillNow > searchProperties
				.getOriginalSearchStartLine()
				&& hitEOFAlready) {
			fullRoundProcessed();
			// We didn't find anything from these 2 blocks, go to next one
		} else {
			// Not End Of File
			if (tracesTillNow < TraceViewerGlobals.getTraceViewer()
					.getDataReaderAccess().getCurrentDataReader()
					.getTraceCount()) {
				searchProperties.setCurrentSearchStartLine(tracesTillNow + 1);
				processSearch(searchProperties, true);
				// End Of File
			} else {
				hitEOFAlready = true;
				searchDialog.updateSearchProgressBar(TraceViewerGlobals
						.getTraceViewer().getDataReaderAccess()
						.getCurrentDataReader().getTraceCount());
				searchDialog.searchHitEOF();
				searchOngoing = false;
			}
		}
	}

	/**
	 * Full round processed
	 */
	private void fullRoundProcessed() {
		hitEOFAlready = false;
		searchDialog.updateSearchProgressBar(searchProperties
				.getOriginalSearchStartLine());
		searchDialog.searchHitFullRound();
		searchOngoing = false;
	}

	/**
	 * Processes situation where result was found
	 * 
	 * @param offset
	 *            offset of the result
	 * @throws BadLocationException
	 */
	private void processStringFound(int offset) throws BadLocationException {
		hitEOFAlready = false;
		int line = document.getLineOfOffset(offset);

		int foundTraceLine = searchProperties.getCurrentSearchStartLine()
				+ line;

		// Update progressBar
		searchDialog.updateSearchProgressBar(foundTraceLine);

		// Set the search line
		if (timestampSearchPhase == TimestampSearchPhase.NONE) {
			searchOngoing = false;
			TraceViewerGlobals.getTraceViewer().getView().highlightLines(
					foundTraceLine, 0, false);

			// Timestamp range search
		} else {
			if (timestampSearchPhase == TimestampSearchPhase.FINDING_END) {
				timestampSearchPhase = TimestampSearchPhase.END_FOUND;
				timestampEndTrace = foundTraceLine;

				// Start new search
				searchProperties.setCurrentSearchStartLine(foundTraceLine);
				processSearch(searchProperties, true);

				// End and start both found
			} else if (timestampSearchPhase == TimestampSearchPhase.END_FOUND) {
				searchOngoing = false;
				TraceViewerGlobals.getTraceViewer().getView().highlightLines(
						foundTraceLine, timestampEndTrace, true);
				timestampSearchPhase = TimestampSearchPhase.NONE;
				timestampEndTrace = 0;
			} else {
				searchOngoing = false;
			}
		}

	}

	/**
	 * Processes situation when searching backwards
	 * 
	 * @param offset
	 *            offset to found item
	 * @throws BadLocationException
	 */
	private void processSearchingBackward(int offset)
			throws BadLocationException {

		// String found
		if (offset != SearchUtils.NOT_FOUND) {
			processStringFound(offset);
			// Full round processed
		} else if (searchProperties.getCurrentSearchStartLine() <= searchProperties
				.getOriginalSearchStartLine()
				&& hitEOFAlready) {
			fullRoundProcessed();
		} else {
			// Not End Of File
			if (searchProperties.getCurrentSearchStartLine() > 0) {
				searchProperties.setCurrentSearchStartLine(searchProperties
						.getCurrentSearchStartLine()
						- (TraceViewerGlobals.blockSize / 2));
				processSearch(searchProperties, true);
				// End Of File
			} else {
				hitEOFAlready = true;
				searchDialog.updateSearchProgressBar(0);

				// Timestamp search
				if (timestampSearchPhase == TimestampSearchPhase.END_FOUND
						|| timestampSearchPhase == TimestampSearchPhase.FINDING_END) {
					if (timestampEndTrace == 0) {
						timestampEndTrace--;
					}
					TraceViewerGlobals.getTraceViewer().getView()
							.highlightLines(0, timestampEndTrace + 1, false);
					timestampSearchPhase = TimestampSearchPhase.NONE;
					timestampEndTrace = 0;

					// Normal search
				} else {
					searchDialog.searchHitEOF();
				}
				searchOngoing = false;
			}
		}
	}

	/**
	 * Sets hitEOFAlready variable
	 * 
	 * @param hitEOF
	 *            boolean to set varible to
	 */
	public void setHitEOFAlready(boolean hitEOF) {
		this.hitEOFAlready = hitEOF;
	}

	/**
	 * Gets search dialog
	 * 
	 * @return search dialog
	 */
	public SearchDialog getSearchDialog() {
		if (searchDialog == null) {
			searchDialog = (SearchDialog) TraceViewerGlobals.getTraceViewer()
					.getDialogs().createDialog(Dialog.SEARCH);
		}
		return searchDialog;
	}

	/**
	 * Sets stop boolean as true or false. If true, searching will stop when
	 * next block has been fetched from the file
	 * 
	 * @param stop
	 *            indicates if the search should be stopped
	 */
	public void stopSearch(boolean stop) {
		stopSearching = stop;
	}

	/**
	 * Process search with given searchProperties
	 * 
	 * @param searchProperties
	 *            searchproperties
	 * @param internal
	 *            if true, the search request is coming from TraceViewer
	 *            internally
	 */
	public void processSearch(SearchProperties searchProperties,
			boolean internal) {

		// If search is already ongoing, stop it
		if (!internal && searchOngoing) {
			stopSearch(true);
			while (searchOngoing) {
				try {
					Thread.sleep(50);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
		searchOngoing = true;

		int traceCount = TraceViewerGlobals.getTraceViewer()
				.getDataReaderAccess().getCurrentDataReader().getTraceCount();

		int numberOfBlocks = 2;
		int traceToStartReading = searchProperties.getCurrentSearchStartLine();

		// Searching forward
		if (searchProperties.isSearchingForward()) {

			// Check now many blocks are needed
			if (traceToStartReading + TraceViewerGlobals.blockSize > traceCount) {
				numberOfBlocks = 1;
			}

			// Searching backward
		} else {
			// Check now many blocks are needed
			if (traceToStartReading < TraceViewerGlobals.blockSize) {
				numberOfBlocks = 1;
			} else {
				// If searching backwards, read traces from one block before
				traceToStartReading = traceToStartReading
						- TraceViewerGlobals.blockSize;
			}
		}

		// Get the file position having given offset
		int index = TraceViewerGlobals.getTraceViewer().getDataReaderAccess()
				.getCurrentDataReader().getFileMap().getIndexFromOffset(
						traceToStartReading);

		this.searchProperties = searchProperties;

		long pos = TraceViewerGlobals.getTraceViewer().getDataReaderAccess()
				.getCurrentDataReader().getFileMap().getItem(index).longValue();
		int startTrace = index * TraceViewerGlobals.blockSize;

		this.searchProperties.setCurrentSearchStartLine(startTrace);

		// Create searchDataReader if it doesn't exist and start it
		searchDataReader = TraceViewerGlobals.getTraceViewer()
				.getDataReaderAccess().startOwnDataReader(searchDataReader,
						this, numberOfBlocks, pos, startTrace, false);
	}

	/**
	 * Performs timestamp range search. Must be called from UI thread!
	 * 
	 * @param findStr
	 *            find str
	 */
	public void doTimestampRangeSearch(String findStr) {
		int traceCount = TraceViewerGlobals.getTraceViewer()
				.getDataReaderAccess().getCurrentDataReader().getTraceCount();
		searchProperties.setCurrentSearchStartLine(traceCount);
		searchProperties.setOriginalSearchStartLine(traceCount);
		searchProperties.setSearchString(findStr);
		searchProperties.setSearchingForward(false);
		getSearchDialog().setSearchText(findStr);
		processSearch(searchProperties, false);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.nokia.traceviewer.engine.MediaCallback#endOfFile(com.nokia.traceviewer
	 * .engine.DataReader)
	 */
	public void endOfFile(DataReader reader) {
		// Do nothing
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.traceviewer.engine.MediaCallback#dataHandleChanged()
	 */
	public void dataHandleChanged() {
		shutdownSearchReader();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.nokia.traceviewer.engine.MediaCallback#processTrace(com.nokia.traceviewer
	 * .engine.TraceProperties)
	 */
	public void processTrace(TraceProperties properties) {
		processData(properties);
	}

	/**
	 * Disposes search dialog. Also kill the search data reader.
	 * 
	 * @return true if search dialog was closed. False if it wasn't open.
	 */
	public boolean disposeSearchDialog() {
		boolean closed = false;
		if (searchDialog != null) {
			closed = searchDialog.forceClose();
			searchDialog = null;
			shutdownSearchReader();
		}
		return closed;
	}

	/**
	 * Shuts down search data reader
	 */
	public void shutdownSearchReader() {
		if (searchDataReader != null) {
			searchDataReader.shutdown();
			searchDataReader = null;
		}
	}

	/**
	 * Searches the next trace with ID. Must be called from UI thread!
	 * 
	 * @param cid
	 *            Component ID
	 * @param gid
	 *            Group ID
	 * @param tid
	 *            Trace ID
	 */
	public void searchTraceWithID(int cid, int gid, int tid) {

		// Create search string
		final String findStr = SearchUtils.createStringFromIDs(cid, gid, tid);
		searchTraceWithString(findStr);
	}

	/**
	 * Searches trace with timestamp. Must be called from UI thread and
	 * TraceViewer view must exists!
	 * 
	 * @param startTimestamp
	 *            start timestamp string
	 * @param endTimestamp
	 *            end timestamp string
	 */
	public void searchTraceWithTimestamp(final String startTimestamp,
			final String endTimestamp) {

		// Create search string
		final String findStr = SearchUtils.createStringFromTimestamp(
				startTimestamp, endTimestamp);
		searchTraceWithString(findStr);
	}

	/**
	 * Searches the next trace with text. Must be called from UI thread!
	 * 
	 * @param text
	 *            text to search for
	 */
	public void searchTraceWithString(final String text) {
		final SearchDialog dialog = getSearchDialog();
		if (!dialog.isOpen()) {
			dialog.create();
		}

		// Create a thread that will start the search because this thread will
		// stop to message event loop when opening the search dialog
		Thread a = new Thread() {

			/*
			 * (non-Javadoc)
			 * 
			 * @see java.lang.Thread#run()
			 */
			@Override
			public void run() {
				// Stop previous search
				if (searchOngoing) {
					stopSearch(true);
				}

				// Must be synced with the UI thread to be able to update the
				// search dialog
				PlatformUI.getWorkbench().getDisplay().asyncExec(
						new Runnable() {

							/*
							 * (non-Javadoc)
							 * 
							 * @see java.lang.Runnable#run()
							 */
							public void run() {
								dialog.startSearch(text);
							}
						});

			}
		};
		a.start();

		if (!dialog.isVisible()) {
			dialog.openDialog();
		}
	}
}