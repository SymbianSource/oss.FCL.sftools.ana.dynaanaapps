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
 * Filter Tree Text Item class
 *
 */

package com.nokia.traceviewer.dialog.treeitem;

import com.nokia.traceviewer.action.TraceViewerActionUtils;
import com.nokia.traceviewer.engine.TraceProperties;

/**
 * Filter Tree Text Item class
 * 
 */
public class FilterTreeTextItem extends FilterTreeBaseItem {

	/**
	 * Text of the item
	 */
	private final String text;

	/**
	 * Text to be compared when filtering
	 */
	private final String textToCompare;

	/**
	 * Indicates match case
	 */
	private final boolean matchCase;

	/**
	 * Constructor
	 * 
	 * @param listener
	 *            Treeitem listener
	 * @param parent
	 *            parent object
	 * @param name
	 *            name of the item
	 * @param rule
	 *            rule of the item
	 * @param text
	 *            text of the item
	 * @param matchCase
	 *            match case of the item
	 */
	public FilterTreeTextItem(TreeItemListener listener, Object parent,
			String name, Rule rule, String text, boolean matchCase) {
		super(listener, parent, name, rule);
		this.text = text;
		this.matchCase = matchCase;

		// Save text to compare
		if (matchCase || text == null) {
			textToCompare = text;
		} else {
			textToCompare = text.toLowerCase();
		}
	}

	/**
	 * Gets match case status
	 * 
	 * @return the matchCase
	 */
	public boolean isMatchCase() {
		return matchCase;
	}

	/**
	 * Gets text
	 * 
	 * @return the text
	 */
	public String getText() {
		return text;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.nokia.traceviewer.dialog.treeitem.FilterTreeBaseItem#processRule(
	 * com.nokia.traceviewer.engine.TraceProperties)
	 */
	@Override
	public boolean processRule(TraceProperties properties) {
		boolean filterHit = false;

		// Filter hits
		String traceLine = ""; //$NON-NLS-1$

		// Traces missing
		if (properties.bTraceInformation.isTraceMissing()) {
			traceLine = TraceViewerActionUtils.TRACES_DROPPED_MSG;
		}
		if (properties.traceString != null) {
			traceLine += properties.traceString;
		}

		// Check that filter is not null
		if (textToCompare != null) {
			if (!isMatchCase()) {
				traceLine = traceLine.toLowerCase();
			}
			if (traceLine.contains(textToCompare)) {
				filterHit = true;
			}
		}

		// If logical NOT, change the result to opposite
		if (isLogicalNotRule()) {
			filterHit = !filterHit;
		}

		return filterHit;
	}

}
