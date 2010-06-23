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
* Extends the SourceParser with support for data changes
*
*/
package com.nokia.tracebuilder.source;

import java.util.Iterator;

/**
 * Extends the SourceParser with support for data changes
 * 
 */
public class SourceEditor extends SourceParser {

	/**
	 * Full sync needed flag
	 */
	private boolean fullSync;

	/**
	 * End of the area affected by the change
	 */
	private int affectedAreaEnd;

	/**
	 * Constructor
	 * 
	 * @param framework
	 *            the document framework
	 * @param sourceData
	 *            the source document data
	 */
	public SourceEditor(SourceDocumentFactory framework, String sourceData) {
		super(framework, sourceData);
	}

	/**
	 * Constructor
	 * 
	 * @param documentFramework
	 *            the document framework
	 * @param source
	 *            the source
	 */
	public SourceEditor(SourceDocumentFactory documentFramework,
			SourceDocumentInterface source) {
		super(documentFramework, source);
	}

	/**
	 * Updates the source file.
	 * 
	 * @param offset
	 *            the offset to removed data
	 * @param length
	 *            the length of removed data
	 * @param newText
	 *            the data inserted at offset
	 * @throws SourceParserException
	 *             if update fails
	 */
	public void updateSource(int offset, int length, String newText)
			throws SourceParserException {
		getSource().replace(offset, length, newText);
	}

	/**
	 * Prepares document update
	 * 
	 * @param offset
	 *            the offset to removed data
	 * @param length
	 *            the length of removed data
	 * @param newText
	 *            the data inserted at offset
	 * @throws SourceParserException
	 *             if update fails
	 */
	public void prepareUpdateSource(int offset, int length, String newText)
			throws SourceParserException {
		// If the modified data contains characters used in building the
		// excluded areas, the results might propagate to
		// other parts of the source code. For example, removing the '*'
		// from end-of-comment causes the comment to continue past the
		// modified area to the next */. In that case a full re-sync needs
		// to be done. One extra character is examined from both sides of the
		// removed area. For example, introducing ' ' between '/' and '*' would
		// remove the comment and affect rest of the code. Also if
		// the new text introduces characters used in excluded areas, a full
		// sync is done. For example, if '*' is inserted into a code, it might
		// terminate an existing comment and thus affect the rest of the source.
		// \n also needs to be processed, since it affects the line comments
		// -> That should be the first one to be optimized away
		fullSync = false;
		SourceDocumentInterface source = getSource();
		int start = offset == 0 ? offset : offset - 1;
		int end = (offset + length) >= source.getLength() - 1 ? (offset + length)
				: offset + length + 1;
		for (int i = start; i < end && !fullSync; i++) {
			char c = source.getChar(i);
			if (c == '/' || c == '*' || c == '"' || c == '\n' || c == '\'') {
				fullSync = true;
			}
		}
		if (!fullSync && newText != null) {
			int len = newText.length();
			for (int i = 0; i < len && !fullSync; i++) {
				char c = newText.charAt(i);
				if (c == '/' || c == '*' || c == '"' || c == '\n' || c == '\'') {
					fullSync = true;
				}
			}
		}
	}

	/**
	 * Sets the full sync flag. This needs to be called if there have been
	 * multiple changes without prepareUpdateSource / sourceUpdated calls
	 */
	public void prepareFullSync() {
		fullSync = true;
	}

	/**
	 * Updates all excluded areas that follow the change offset
	 * 
	 * @param offset
	 *            the offset to removed data
	 * @param length
	 *            the length of removed data
	 * @param newText
	 *            the data inserted at offset
	 * @return end of the area where the change might affect
	 * @throws SourceParserException
	 *             if offset is not valid
	 */
	public int sourceUpdated(int offset, int length, String newText)
			throws SourceParserException {
		if (newText == null) {
			newText = ""; //$NON-NLS-1$
		}
		// Full sync flag was calculated in prepareUpdateSource
		if (fullSync) {
			findExcludedAreas();
			affectedAreaEnd = getSource().getLength();
		} else {
			// If the change was within an excluded area, the length of the
			// area is updated. The offset of the areas following the change
			// are updated.
			int diff = newText.length() - length;
			if (diff != 0) {
				int index = findExcludedAreaIndex(offset);
				if (index >= 0) {
					SourceLocationBase p = getExcludedAreas().get(index);
					p.setLength(p.getLength() + diff);
					index++;
				} else {
					index = -1 - index;
				}
				for (; index < getExcludedAreas().size(); index++) {
					SourceLocationBase p = getExcludedAreas().get(index);
					p.setOffset(p.getOffset() + diff);
				}
			}
			affectedAreaEnd = getSource().getLength();
		}
		// Contexts and preprocessor definitions are updated when needed for
		// the next time
		resetContexts();
		resetPreprocessor();
		return affectedAreaEnd;
	}

	/**
	 * Sends update notifications to location listeners of modified and deleted
	 * locations
	 */
	public void notifyLocationUpdates() {
		// Posts delete notifications to location listeners
		Iterator<SourceLocation> itr = getLocations();
		while (itr.hasNext()) {
			SourceLocation location = itr.next();
			if (location.isDeleted()) {
				location.notifyLocationDeleted();
				itr.remove();
			} else {
				location.notifyLocationChanged();
			}
		}
	}

}
