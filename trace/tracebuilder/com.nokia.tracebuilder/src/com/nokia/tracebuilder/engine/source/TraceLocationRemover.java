/*
* Copyright (c) 2007 Nokia Corporation and/or its subsidiary(-ies). 
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
* Removes a location from source
*
*/
package com.nokia.tracebuilder.engine.source;

import com.nokia.tracebuilder.engine.TraceLocation;
import com.nokia.tracebuilder.source.SourceConstants;
import com.nokia.tracebuilder.source.SourceEditor;
import com.nokia.tracebuilder.source.SourceExcludedArea;
import com.nokia.tracebuilder.source.SourceLocationBase;
import com.nokia.tracebuilder.source.SourceParserException;

/**
 * Removes a location from source
 * 
 */
class TraceLocationRemover extends SourceEditorUpdater {

	/**
	 * The trace location to be removed
	 */
	TraceLocation location;

	/**
	 * Creates a new location remover for given location
	 * 
	 * @param location
	 *            the location to be written to source
	 */
	TraceLocationRemover(TraceLocation location) {
		super(location.getSource());
		this.location = location;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.engine.source.SourceEditorUpdater#runUpdate()
	 */
	@Override
	protected boolean runUpdate() throws SourceParserException {
		// If the location was already deleted by some other operation while
		// waiting for this asynchronous operation to run, there is no need to
		// remove it
		boolean updated;
		if (!location.isDeleted()) {
			removeLocation(location);
			updated = true;
		} else {
			updated = false;
		}
		return updated;
	}

	/**
	 * Removes a location
	 * 
	 * @param location
	 *            the location to be removed
	 * @return the offset where the removed location started
	 * @throws SourceParserException
	 *             if remove fails
	 */
	static int removeLocation(TraceLocation location)
			throws SourceParserException {

		SourceEditor sourceEditor = location.getSource().getSourceEditor();
		int start;
		int end;

		// Old Symbian traces could include comment associated with the location
		// If it exists, it is also removed
		String listTitle = location.getLocationList().getListTitle();
		if (listTitle != null && listTitle.equals("SymbianTrace")) { //$NON-NLS-1$
			SourceExcludedArea area = TraceLocationWriter
					.findLocationComment(location);
			if (area != null) {
				if (area.getOffset() < location.getOffset()
						&& area.getType() == SourceExcludedArea.LINE_COMMENT) {
					start = area.getOffset();
					end = location.getOffset() + location.getLength();
				} else if (area.getOffset() > location.getOffset()
						&& area.getType() == SourceExcludedArea.LINE_COMMENT) {
					start = location.getOffset();
					end = area.getOffset() + area.getLength();
				} else {
					start = location.getOffset();
					end = start + location.getLength();
				}
			} else {
				start = location.getOffset();
				end = start + location.getLength();
			}
		} else {
			start = location.getOffset();
			end = start + location.getLength();
		}

		// If there is a line feed after the trace, the line feed is
		// also removed
		int lflen = SourceConstants.LINE_FEED.length();
		if (end <= (sourceEditor.getDataLength() - lflen)) {
			String data = sourceEditor.getData(end, lflen);
			if (data.equals(SourceConstants.LINE_FEED)) {
				end += lflen;
			}
		}
		start = sourceEditor.findStartOfLine(start, true, true);
		int len = end - start;
		sourceEditor.updateSource(start, len, ""); //$NON-NLS-1$
		return start;
	}

	/**
	 * Finds the start offset of given location
	 * 
	 * @param location
	 *            the location to be checked
	 * @return the offset where the location starts
	 * @throws SourceParserException
	 *             if operation fails
	 */
	static int findStartOffset(TraceLocation location)
			throws SourceParserException {
		int start;
		SourceEditor sourceEditor = location.getSource().getSourceEditor();

		// Old Symbian traces could include comment associated with the location
		// If it exists before location, it is also included
		String listTitle = location.getLocationList().getListTitle();
		if (listTitle != null && listTitle.equals("SymbianTrace")) { //$NON-NLS-1$
			SourceExcludedArea area = TraceLocationWriter
					.findLocationComment(location);

			if (area != null) {
				if (area.getOffset() < location.getOffset()
						&& area.getType() == SourceExcludedArea.LINE_COMMENT) {
					start = area.getOffset();
				} else {
					start = location.getOffset();
				}
			} else {
				start = location.getOffset();
			}
		} else {
			start = location.getOffset();
		}

		start = sourceEditor.findStartOfLine(start, true, true);
		return start;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.engine.source.SourceEditorUpdater#getPosition()
	 */
	@Override
	protected SourceLocationBase getPosition() {
		return location;
	}

}
