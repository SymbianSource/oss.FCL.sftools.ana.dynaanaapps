/*
* Copyright (c) 2008 Nokia Corporation and/or its subsidiary(-ies). 
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
* Properties of a source document opened to Eclipse editor
*
*/
package com.nokia.tracebuilder.engine.source;

import java.util.ArrayList;
import java.util.Collections;
import java.util.ConcurrentModificationException;
import java.util.Iterator;

import com.nokia.tracebuilder.engine.TraceBuilderConfiguration;
import com.nokia.tracebuilder.engine.TraceBuilderGlobals;
import com.nokia.tracebuilder.engine.TraceLocation;
import com.nokia.tracebuilder.engine.TraceLocationProperties;
import com.nokia.tracebuilder.engine.rules.ComplexHeaderRule;
import com.nokia.tracebuilder.model.Trace;
import com.nokia.tracebuilder.model.TraceModel;
import com.nokia.tracebuilder.plugin.TraceAPIFormatter.TraceFormatType;
import com.nokia.tracebuilder.source.PositionArrayComparator;
import com.nokia.tracebuilder.source.SourceConstants;
import com.nokia.tracebuilder.source.SourceDocumentFactory;
import com.nokia.tracebuilder.source.SourceDocumentInterface;
import com.nokia.tracebuilder.source.SourceEditor;
import com.nokia.tracebuilder.source.SourceIterator;
import com.nokia.tracebuilder.source.SourceLocationBase;
import com.nokia.tracebuilder.source.SourceParser;
import com.nokia.tracebuilder.source.SourceParserException;
import com.nokia.tracebuilder.source.SourcePropertyProvider;
import com.nokia.tracebuilder.source.SourceSelector;
import com.nokia.tracebuilder.source.SourceStringSearch;

/**
 * Properties of a source document which contains trace locations
 * 
 */
public class SourceProperties implements Iterable<TraceLocation> {

	/**
	 * Trace locations within the source
	 */
	private ArrayList<TraceLocation> locations = new ArrayList<TraceLocation>();

	/**
	 * Source editor
	 */
	private SourceEditor sourceEditor;

	/**
	 * Comparator for binary searches
	 */
	private PositionArrayComparator comparator = new PositionArrayComparator();

	/**
	 * Offset is stored in preProcess and reset in postProcess.
	 */
	private int firstChangedLocation = -1;

	/**
	 * Offset is stored in preProcess and reset in postProcess.
	 */
	private int firstUnchangedLocation = -1;

	/**
	 * The searchers for trace identifiers
	 */
	private ArrayList<SourceStringSearch> searchers = new ArrayList<SourceStringSearch>();

	/**
	 * Start index for calls to parseTrace
	 */
	private int searchStartIndex;

	/**
	 * Source update operation queue
	 */
	private SourceEditorUpdateQueue updateQueue = new SourceEditorUpdateQueue(
			this);

	/**
	 * Active source flag
	 */
	private boolean isActive;

	/**
	 * Read-only flag
	 */
	private boolean readOnly;

	/**
	 * Creates source properties for given source document
	 * 
	 * @param model
	 *            the trace model
	 * @param framework
	 *            the document framework
	 * @param document
	 *            the document
	 */
	public SourceProperties(TraceModel model, SourceDocumentFactory framework,
			SourceDocumentInterface document) {
		sourceEditor = new SourceEditor(framework, document);
		Iterator<SourceParserRule> parsers = model
				.getExtensions(SourceParserRule.class);
		while (parsers.hasNext()) {
			// The rule defines what to search and how to interpret the
			// parameters. It is stored into the searcher as search data
			addParserRule(parsers.next());
		}
	}

	/**
	 * Gets the source editor
	 * 
	 * @return the editor
	 */
	public SourceEditor getSourceEditor() {
		return sourceEditor;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Iterable#iterator()
	 */
	public Iterator<TraceLocation> iterator() {
		return locations.iterator();
	}

	/**
	 * Gets the file name of this source
	 * 
	 * @return the name
	 */
	public String getFileName() {
		String retval = null;
		if (sourceEditor != null) {
			SourceDocumentInterface source = sourceEditor.getSource();
			if (source != null) {
				SourcePropertyProvider provider = source.getPropertyProvider();
				if (provider != null) {
					retval = provider.getFileName();
				}
			}
		}
		return retval;
	}

	/**
	 * Gets the file path of this source
	 * 
	 * @return the name
	 */
	public String getFilePath() {
		String retval = null;
		if (sourceEditor != null) {
			SourceDocumentInterface source = sourceEditor.getSource();
			if (source != null) {
				SourcePropertyProvider provider = source.getPropertyProvider();
				if (provider != null) {
					retval = provider.getFilePath();
				}
			}
		}
		return retval;
	}

	/**
	 * Finds a location based on document offset
	 * 
	 * @param offset
	 *            the offset to the location
	 * @return the location of null if the offset does not point to a location
	 */
	public TraceLocation getLocation(int offset) {
		TraceLocation location = null;
		// If firstChangedLocation is set, this source is being processed
		// and binarySearch will not work. In that case, this function
		// must return null
		if (firstChangedLocation < 0) {
			SourceLocationBase base = sourceEditor.createHiddenLocation(offset,
					0);
			int index = Collections.binarySearch(locations, base, comparator);
			sourceEditor.removeHiddenLocation(base);
			if (index >= 0 && index < locations.size()) {
				location = locations.get(index);
			}
		}
		return location;
	}

	/**
	 * Returns the location count of this source
	 * 
	 * @return the count
	 */
	public int getLocationCount() {
		return locations.size();
	}

	/**
	 * Gets the offset to the first changed location. Only valid during
	 * sourceUpdated callback
	 * 
	 * @return the offset
	 */
	public int getFirstChangedLocationOffset() {
		return firstChangedLocation;
	}

	/**
	 * Sets the read-only flag for this source. Traces cannot be added to
	 * read-only sources, but they can be parsed for data
	 * 
	 * @param readOnly
	 *            the read-only flag
	 */
	void setReadOnly(boolean readOnly) {
		this.readOnly = readOnly;
	}

	/**
	 * Gets the read-only flag
	 * 
	 * @return read-only flag
	 */
	public boolean isReadOnly() {
		return readOnly;
	}

	/**
	 * Source opened notification
	 */
	void sourceOpened() {
		updateTraces(0, sourceEditor.getDataLength());
		isActive = true;
	}

	/**
	 * Source closed notification
	 */
	void sourceClosed() {
		if (sourceEditor != null) {
			sourceEditor.reset();
			sourceEditor = null;
		}
		updateQueue.resetUpdateQueue();
	}

	/**
	 * Called when the source document is about to change.
	 * 
	 * @param offset
	 *            the offset of the change
	 * @param length
	 *            the number of bytes that were removed
	 * @param newText
	 *            text that is going to be inserted into the document
	 * @throws SourceParserException
	 *             if offset / length are not valid
	 */
	void prepareChange(int offset, int length, String newText)
			throws SourceParserException {
		sourceEditor.prepareUpdateSource(offset, length, newText);
	}

	/**
	 * Called before SourceListeners when source changes. Updates locations that
	 * have changed content and locates possible new traces from the inserted
	 * text.
	 * 
	 * @param offset
	 *            the offset of the change
	 * @param length
	 *            the number of bytes that were removed
	 * @param newText
	 *            text that was added to the document
	 * @throws SourceParserException
	 *             if offset / length are not valid
	 */
	void preProcessChange(int offset, int length, String newText)
			throws SourceParserException {
		preProcessInit();
		// Refreshes the source editor. The editor returns the last offset that
		// could possibly be affected by the change
		int areaOfEffectEnd = sourceEditor.sourceUpdated(offset, length,
				newText);

		findFirstChangedLocation(offset);
		processChangedLocations(offset);
		int start = findTraceUpdateStartIndex(offset);
		int end = findTraceUpdateEndIndex(areaOfEffectEnd);
		if (end > start) {
			updateTraces(start, end);
		}
	}

	/**
	 * Initializes the variables for preProcess
	 * 
	 * @throws ConcurrentModificationException
	 *             if change is not allowed
	 */
	private void preProcessInit() throws ConcurrentModificationException {
		// Concurrent modifications during preProcessChange / postProcessChange
		// cycle are not allowed
		if (firstChangedLocation >= 0) {
			throw new ConcurrentModificationException();
		}
		firstChangedLocation = 0;
	}

	/**
	 * Finds the first location that has changed
	 * 
	 * @param offset
	 *            the start offset
	 */
	private void findFirstChangedLocation(int offset) {
		// BinarySearch cannot be used here, since at this point the array
		// contains the deleted items, which have invalid offsets
		Iterator<TraceLocation> itr = locations.iterator();
		boolean firstFound = false;
		while (itr.hasNext() && !firstFound) {
			TraceLocation loc = itr.next();
			// When a change occurs, the location can be:
			// - Deleted by complete removal
			// - Changed by modifying the contents of the location
			// - Changed by modifying the character before the location
			// - Changed by starting a comment which encloses the location
			if (loc.isDeleted() || loc.isContentChanged()
					|| offset == loc.getOffset() - 1
					|| sourceEditor.isInExcludedArea(loc.getOffset())) {
				firstFound = true;
			} else {
				firstChangedLocation++;
			}
		}
	}

	/**
	 * Processes the changed locations
	 * 
	 * @param offset
	 *            the start offset
	 */
	private void processChangedLocations(int offset) {
		// After the first changed entry has been found, the locations after
		// that are processed. If a location has been moved to excluded source
		// code areas, it is marked for deletion. If the content of a location
		// has been changed, the new contents are verified and if they are not
		// valid, the location is marked for deletion. When the first unchanged
		// trace is found, all changes have been processed and this loop is
		// terminated.
		firstUnchangedLocation = locations.size();
		int index = firstChangedLocation;
		while (index < locations.size()) {
			TraceLocation location = locations.get(index);
			if (!location.isDeleted()) {
				if (sourceEditor.isInExcludedArea(location.getOffset())) {
					location.delete();
				} else if (location.isContentChanged()
						|| offset == location.getOffset() - 1) {
					processChangedLocation(location);
				} else {
					firstUnchangedLocation = index;
					index = locations.size();
				}
			}
			index++;
		}
	}

	/**
	 * Finds the offset where trace updates should start
	 * 
	 * @param offset
	 *            the change offset
	 * @return the trace update offset
	 */
	private int findTraceUpdateStartIndex(int offset) {
		// After the existing traces have been processed and marked for removal,
		// the first ';' character preceeding the changed area is located from
		// the source. The source file is reparsed starting from that point up
		// to the next unchanged trace location. All new traces found from that
		// area are added to the trace location list.
		int start = findSemicolonOrBracket(offset, SourceParser.BACKWARD_SEARCH);
		if (start == -1) {
			start = 0;
		}
		return start;
	}

	/**
	 * Finds the offset where trace updates should end
	 * 
	 * @param endOffset
	 *            the end of change offset
	 * @return the trace update end offset
	 */
	private int findTraceUpdateEndIndex(int endOffset) {
		// The offset to the first unchanged location is fetched from
		// the array. The contents of the source file between the change
		// offset and the offset of the first unchanged trace is
		// processed. If none of the traces changed, the area of effect
		// determined by the source parser is used
		int end;
		if (firstUnchangedLocation < locations.size()) {
			TraceLocation loc = locations.get(firstUnchangedLocation);
			end = loc.getOffset();
		} else {
			end = findSemicolonOrBracket(endOffset, 0);
			if (end == -1) {
				end = sourceEditor.getDataLength();
			}
		}
		return end;
	}

	/**
	 * Called after listeners when source changes. Removes obsolete locations
	 * and resets change flags
	 */
	void postProcessChange() {
		int index = firstChangedLocation;
		// The location list might have been reset while processing if there was
		// an critical assertion
		if (locations != null && locations.size() >= firstUnchangedLocation) {
			// This loops through all changed locations. Deleted locations are
			// removed and the flags are reset
			while (index < firstUnchangedLocation) {
				TraceLocation location = locations.get(index);
				if (location.isDeleted()) {
					// Removes from parser and document
					location.dereference();
					locations.remove(index);
					index--;
					firstUnchangedLocation--;
				} else {
					location.setContentChanged(false);
				}
				index++;
			}
			firstChangedLocation = -1;
			firstUnchangedLocation = -1;
			// When all processing has been done, the location updates of
			// SourceLocation objects from the editor are sent to editor
			// listeners
			sourceEditor.notifyLocationUpdates();
		} else {
			firstChangedLocation = -1;
			firstUnchangedLocation = -1;
		}
	}

	/**
	 * Pre-processes a full source sync
	 * 
	 * @param offset
	 *            the offset of removed data
	 * @param length
	 *            the length of removed data
	 * @param newText
	 *            the new text
	 * @throws SourceParserException
	 *             if location is not valid
	 */
	void preProcessFullSync(int offset, int length, String newText)
			throws SourceParserException {
		preProcessInit();
		firstUnchangedLocation = locations.size();
		sourceEditor.prepareFullSync();
		sourceEditor.sourceUpdated(offset, length, newText);
		int index = 0;
		// All locations except deleted ones are processed as changed when
		// doing a full sync
		while (index < locations.size()) {
			TraceLocation location = locations.get(index);
			if (!location.isDeleted()) {
				if (sourceEditor.isInExcludedArea(location.getOffset())) {
					location.delete();
				} else {
					processChangedLocation(location);
				}
			}
			index++;
		}
		updateTraces(0, sourceEditor.getDataLength());
	}

	/**
	 * Called after SourceListener.sourceProcessingComplete. Removes obsolete
	 * locations and resets change flags
	 */
	void postProcessFullSync() {
		postProcessChange();
	}

	/**
	 * Searches the next semicolon or bracket from given index
	 * 
	 * @param offset
	 *            the offset where to start
	 * @param searchFlags
	 *            search flags in addition to SKIP_ALL
	 * @return the offset to semicolon or -1 if not found
	 */
	private int findSemicolonOrBracket(int offset, int searchFlags) {
		SourceIterator srcitr = sourceEditor.createIterator(
				offset > 0 ? offset - 1 : offset, searchFlags
						| SourceParser.SKIP_ALL);
		char c;
		boolean found = false;
		try {
			while (!found && srcitr.hasNext()) {
				c = srcitr.next();
				if (c == ';' || c == '}' || c == '{') {
					found = true;
				}
			}
		} catch (Exception e) {
		}
		int retval = -1;
		if (found) {
			retval = srcitr.currentIndex();
		}
		return retval;
	}

	/**
	 * Parses the document starting from given offset and locates the trace
	 * entries from it. The first unchanged trace entry stops the search
	 * 
	 * @param startOffset
	 *            the offset where to start the search
	 * @param endOffset
	 *            the offset where to end the search
	 */
	public void updateTraces(int startOffset, int endOffset) {
		Iterator<SourceStringSearch> itr = searchers.iterator();
		while (itr.hasNext()) {
			SourceStringSearch searcher = itr.next();
			searcher.resetSearch(startOffset, endOffset);
			updateTraces(endOffset, searcher);
		}
	}

	/**
	 * Uses the given SourceSearch to parse traces
	 * 
	 * @param end
	 *            the offset where parser should stop
	 * @param searcher
	 *            the searcher
	 */
	private void updateTraces(int end, SourceStringSearch searcher) {
		int offset;
		searchStartIndex = 0;
		// If not updating, the entries contents are processed
		do {
			offset = searcher.findNext();
			try {
				if (offset != -1 && offset < end) {
					String tag = isValidTrace(offset, searcher
							.getSearchString().length(), searcher, false);
					if (tag != null) {
						parseTrace(offset, (SourceParserRule) searcher
								.getSearchData(), tag);
					}
				}
			} catch (Exception e) {
				// If the parameters cannot be parsed, the trace is
				// not added to the array
			}
		} while (offset != -1 && offset < end);
	}

	/**
	 * Parses a trace found from the document and adds it to the document's list
	 * of positions. The position updater keeps the trace location up-to-date.
	 * 
	 * @param offset
	 *            the offset to the trace
	 * @param parserRule
	 *            the parser to be attached to the location
	 * @param locationTag
	 *            the tag of the location
	 * @throws SourceParserException
	 *             if trace cannot be parsed
	 */
	private void parseTrace(int offset, SourceParserRule parserRule,
			String locationTag) throws SourceParserException {
		int arrayIndex = -1;
		// Checks the changed locations. If a matching offset if found, the
		// location is an existing one. In that case the location is not
		// added to the array. If an offset larger than the new offset is
		// found from the array, the location is inserted into that slot. If
		// all locations within the array are smaller than the new offset,
		// the new location is inserted before the first unchanged location.
		// Since the locations in the array are ordered, the checking can
		// always start from the latest location that has been found from
		// the array. The caller of this function must set
		// parseTraceStartIndex to 0 before starting a loop where this
		// function is called. If firstUnchangedLocation is -1, this is the
		// first time the file is being parsed and thus all locations are
		// checked
		boolean found = false;
		int searchEndIndex;
		int newSearchStartIndex = -1;
		if (firstUnchangedLocation >= 0) {
			searchEndIndex = firstUnchangedLocation;
		} else {
			searchEndIndex = locations.size();
		}
		for (int i = searchStartIndex; i < searchEndIndex && !found; i++) {
			TraceLocation location = locations.get(i);
			// Deleted locations are ignored. If a trace was replaced, the
			// new offset will match the offset of the deleted one.
			if (!location.isDeleted()) {
				// If the offset of the trace matches an existing offset,
				// the trace is old one. If the offset within the array is
				// larger than the source offset, the trace found from
				// source is new.
				if (location.getOffset() == offset) {
					found = true;
					// Starts the next search from the value following the
					// trace that was found
					searchStartIndex = i + 1;
					arrayIndex = -1;
				} else if (location.getOffset() > offset) {
					found = true;
					// A new trace will be added into the current index, so
					// the next search will start from the same location as
					// was checked now. The index is updated after the trace has
					// succesfully been created
					newSearchStartIndex = i + 1;
					arrayIndex = i;
				}
			}
		}
		// If trace was not found from the list, the trace is new and all
		// traces following it are also new. The start index is set to point
		// past the first unchanged location and thus the next search will
		// ignore the above loop.
		if (!found) {
			arrayIndex = searchEndIndex;
			searchStartIndex = firstUnchangedLocation + 1;
		}
		if (arrayIndex >= 0) {
			// Creates a new location if it was not found
			ArrayList<String> list = new ArrayList<String>();
			int endOfTrace = sourceEditor
					.tokenizeParameters(offset, list, true);
			TraceLocation location = new TraceLocation(this, offset, endOfTrace
					- offset);
			// The parser rules have been associated with the searchers. The
			// parser rule that found the location is associated with the
			// location and used to process its parameters
			location.setTag(locationTag);
			location.setParserRule(parserRule);
			location.setData(list);
			locations.add(arrayIndex, location);
			// The changed flag is set to newly added traces. If a location
			// is added prior to the first changed location, the index of first
			// changed location needs to be adjusted so that the flag gets
			// cleared in postprocessing. Also the index of first unchanged
			// location needs to be updated to reflect the changed array
			if (firstUnchangedLocation >= 0) {
				location.setContentChanged(true);
				if (arrayIndex < firstChangedLocation) {
					firstChangedLocation = arrayIndex;
				}
				firstUnchangedLocation++;
			}
			// Updates the search start index if trace creation was succesful
			if (newSearchStartIndex >= 0) {
				searchStartIndex = newSearchStartIndex;
			}
		}
	}

	/**
	 * Processes the contents of a changed location
	 * 
	 * @param location
	 *            the location that was changed
	 */
	private void processChangedLocation(TraceLocation location) {
		// If the trace still contains the trace identifier, the parameters are
		// parsed. If not, the trace is deleted
		Iterator<SourceStringSearch> itr = searchers.iterator();
		String traceID = null;
		while (itr.hasNext() && traceID == null) {
			SourceStringSearch searcher = itr.next();
			traceID = isValidTrace(location.getOffset(), location.getLength(),
					searcher, true);
		}
		if (traceID != null) {
			ArrayList<String> list = new ArrayList<String>();
			try {
				int offset = location.getOffset() + traceID.length();
				int endOfTrace = sourceEditor.tokenizeParameters(offset, list,
						true);
				location.setTag(traceID);
				location.setLength(endOfTrace - location.getOffset());
				location.setData(list);
			} catch (SourceParserException e) {
				location.delete();
			}
		} else {
			location.delete();
		}
	}

	/**
	 * Checks that a trace is valid
	 * 
	 * @param offset
	 *            offset to trace identifier
	 * @param length
	 *            length of trace
	 * @param searcher
	 *            the source searcher
	 * @param checkMainTag
	 *            true if the main search tag needs to be checked, false if only
	 *            the tag suffix is checked
	 * @return the trace tag or null if trace is not valid
	 */
	private String isValidTrace(int offset, int length,
			SourceStringSearch searcher, boolean checkMainTag) {
		String retval = null;
		try {
			int idlen = searcher.getSearchString().length();
			int idend = offset + idlen;
			if (checkMainTag) {
				if (length >= idlen
						&& searcher.isSearchStringMatch(sourceEditor.getData(
								offset, idlen))) {
					// The previous character must be a separator or white space
					if (offset == 0
							|| !Character.isJavaIdentifierPart(sourceEditor
									.getData(offset - 1))) {
						retval = getSearchTag(offset, idend);
					}
				}
			} else {
				// If main tag is not checked
				retval = getSearchTag(offset, idend);
			}
			retval = verifyTag(searcher, retval, idlen);
		} catch (Exception e) {
			if (TraceBuilderConfiguration.ASSERTIONS_ENABLED) {
				TraceBuilderGlobals.getEvents().postAssertionFailed(
						"Trace validity check failed", e); //$NON-NLS-1$
			}
		}
		return retval;
	}

	/**
	 * Verifies the tag against tag suffixes from parser
	 * 
	 * @param searcher
	 *            the searcher
	 * @param tag
	 *            the tag include main tag and suffix
	 * @param idlen
	 *            the length of the main tag
	 * @return the tag if it is valid, null if not
	 */
	private String verifyTag(SourceStringSearch searcher, String tag, int idlen) {
		if (tag != null) {
			// The trace suffix is verified by the parser. For example, if
			// search data is "SymbianTrace" and the tag found from source
			// is "SymbianTraceData1", the parser checks if "Data1" is a
			// valid trace tag suffix.
			if (!((SourceParserRule) searcher.getSearchData())
					.isAllowedTagSuffix(tag.substring(idlen))) {
				tag = null;
			}
		}
		return tag;
	}

	/**
	 * Gets the search tag between offset and next '(' character
	 * 
	 * @param offset
	 *            the start of tag
	 * @param idend
	 *            the end of tag
	 * @return the tag
	 * @throws SourceParserException
	 *             if parser fails
	 */
	private String getSearchTag(int offset, int idend)
			throws SourceParserException {
		// Locates the parameters starting from trace identifier
		String retval = null;
		SourceIterator srcitr = sourceEditor.createIterator(idend - 1,
				SourceParser.SKIP_ALL);
		boolean found = false;
		while (srcitr.hasNext() && !found) {
			char c = srcitr.next();
			if (c == ';') {
				// Trace must have parameters
				found = true;
			} else if (c == '(') {
				found = true;
				// Stores the tag into location
				retval = sourceEditor.getData(offset, srcitr.previousIndex()
						- offset + 1);
			} else if (srcitr.hasSkipped()) {
				// White spaces are not allowed within trace tag
				found = true;
			}
		}
		return retval;
	}

	/**
	 * Checks if a trace can be inserted into given location
	 * 
	 * @param offset
	 *            the offset to the location
	 * @return true if location is valid
	 */
	boolean checkInsertLocation(int offset) {
		boolean retval = true;
		try {
			offset = sourceEditor.findStartOfLine(offset, false, true);
			if (sourceEditor.isInExcludedArea(offset)) {
				retval = false;
			}
		} catch (SourceParserException e) {
			retval = false;
		}
		return retval;
	}

	/**
	 * Creates a trace string
	 * 
	 * @param trace
	 *            the trace
	 * @param offset
	 *            the offset where to insert the trace
	 * @param replacedLocation
	 *            the existing location to be replaced
	 * @param locationProperties
	 *            properties to be used
	 * @throws SourceParserException
	 *             if source parser fails
	 */
	void internalInsertTrace(Trace trace, int offset,
			TraceLocation replacedLocation,
			TraceLocationProperties locationProperties)
			throws SourceParserException {
		String indent = sourceEditor.getIndent(offset);
		TraceFormattingRule formatRule = null;
		if (locationProperties != null) {
			formatRule = locationProperties.getFormatRule();
		}
		Iterator<String> tags;
		boolean fixedTags;
		if (replacedLocation != null) {
			// If replacing a location, the tags should not change
			// -> FixedTags is set to true
			tags = replacedLocation.getParameters();
			fixedTags = true;
		} else if (locationProperties != null) {
			// If new location with explicit tags, the tags are processed
			// -> FixedTags is set to false
			tags = locationProperties.getParameterTags();
			fixedTags = false;
		} else {
			// If no explicit tags, the fixedTags flag has no effect
			tags = null;
			fixedTags = false;
		}
		// Formatter creates the correct trace entry
		StringBuffer sb = new StringBuffer();
		sb.append(indent);
		sb.append(SourceFormatter.formatTrace(trace, formatRule,
				getFormatType(trace), tags, fixedTags));
		// Adds indent to line feeds. Last line feed is ignored
		int index = sb.length() - SourceConstants.LINE_FEED.length() - 1;
		do {
			index = sb.lastIndexOf(SourceConstants.LINE_FEED, index);
			if (index >= 0) {
				sb.insert(index + SourceConstants.LINE_FEED.length(), indent);
				index--;
			}
		} while (index >= 0);
		sourceEditor.updateSource(offset, 0, sb.toString());
		if (isActive) {
			SourceDocumentInterface owner = sourceEditor.getSource();
			if (owner != null) {
				SourceSelector selector = owner.getSourceSelector();
				if (selector != null) {
					selector.setSelection(offset + sb.length()
							+ indent.length(), 0);
				}
			}
		}
	}

	/**
	 * Gets the format type to be used for the trace
	 * 
	 * @param trace
	 *            the trace
	 * @return the format type
	 */
	private TraceFormatType getFormatType(Trace trace) {
		ComplexHeaderRule rule = trace.getExtension(ComplexHeaderRule.class);
		TraceFormatType type;
		if (rule != null) {
			type = TraceFormatType.COMPLEX_TRACE;
		} else {
			type = TraceFormatType.NORMAL_TRACE;
		}
		return type;
	}

	/**
	 * Adds a new parser
	 * 
	 * @param rule
	 *            the new parser rule
	 */
	void addParserRule(SourceParserRule rule) {
		SourceStringSearch searcher = sourceEditor.startStringSearch(rule
				.getSearchTag(), 0, -1, SourceParser.MATCH_WORD_BEGINNING
				| SourceParser.SKIP_ALL);
		searcher.setSearchData(rule);
		searchers.add(searcher);
	}

	/**
	 * Removes a parser
	 * 
	 * @param rule
	 *            the parser to be removed
	 */
	void removeParserRule(SourceParserRule rule) {
		Iterator<SourceStringSearch> itr = searchers.iterator();
		boolean found = false;
		while (itr.hasNext() && !found) {
			SourceStringSearch search = itr.next();
			if (search.getSearchString().equals(rule.getSearchTag())) {
				itr.remove();
				found = true;
			}
		}
	}

	/**
	 * Flag set by SourceEngine, which controls the parsing of this source
	 * 
	 * @return the active flag
	 */
	boolean isActive() {
		return isActive;
	}

	/**
	 * Sets the flag which controls source parsing
	 * 
	 * @param active
	 *            the active flag
	 */
	void setActive(boolean active) {
		isActive = active;
	}

	/**
	 * Gets the update queue
	 * 
	 * @return the update queue
	 */
	SourceEditorUpdateQueue getUpdateQueue() {
		return updateQueue;
	}

}
