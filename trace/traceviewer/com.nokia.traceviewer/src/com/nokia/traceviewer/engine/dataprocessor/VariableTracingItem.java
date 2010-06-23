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
 * Variable Tracing Item
 *
 */
package com.nokia.traceviewer.engine.dataprocessor;

import java.util.LinkedList;

/**
 * Varible tracing item showed in the variableTracingTable
 */
public class VariableTracingItem {

	/**
	 * Name of the item
	 */
	private final String name;

	/**
	 * Text to be compared
	 */
	private final String textToCompare;

	/**
	 * Indicates if item needs matching case
	 */
	private final boolean matchCase;

	/**
	 * Number of history items to save in this item
	 */
	private final int saveHistory;

	/**
	 * Linked list containing variable tracing events
	 */
	private final LinkedList<VariableTracingEvent> events;

	/**
	 * Indicates if item has changed since last update
	 */
	private boolean changed;

	/**
	 * Constructor
	 * 
	 * @param name
	 *            name of the item
	 * @param text
	 *            text of the item
	 * @param matchCase
	 *            matchcase indicator
	 * @param saveHistory
	 *            number of history steps to save
	 */
	public VariableTracingItem(String name, String text, boolean matchCase,
			int saveHistory) {
		// Create the linked list
		events = new LinkedList<VariableTracingEvent>();
		this.name = name;
		this.matchCase = matchCase;

		// Always save at least 1
		if (saveHistory < 1) {
			this.saveHistory = 1;
		} else {
			this.saveHistory = saveHistory;
		}

		// Save text to compare
		if (matchCase || text == null) {
			textToCompare = text;
		} else {
			textToCompare = text.toLowerCase();
		}
	}

	/**
	 * Tells the status of changed attribute
	 * 
	 * @return the changed
	 */
	public boolean isChanged() {
		return changed;
	}

	/**
	 * Sets changed attribute
	 * 
	 * @param changed
	 *            the changed to set
	 */
	public void setChanged(boolean changed) {
		this.changed = changed;
	}

	/**
	 * Tells the status of match case attribute
	 * 
	 * @return the matchCase
	 */
	public boolean isMatchCase() {
		return matchCase;
	}

	/**
	 * Gets the name
	 * 
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Gets text to compare
	 * 
	 * @return the text to compare
	 */
	public String getTextToCompare() {
		return textToCompare;
	}

	/**
	 * Adds new variable tracing event to the list
	 * 
	 * @param event
	 *            the event
	 */
	public void addEvent(VariableTracingEvent event) {
		if (events.size() >= saveHistory) {
			events.removeLast();
		}
		events.addFirst(event);
	}

	/**
	 * Gets list of events from this item
	 * 
	 * @return list of events
	 */
	public LinkedList<VariableTracingEvent> getEventList() {
		return events;
	}

	/**
	 * Adds new variable tracing event to the list
	 */
	public void clear() {
		events.clear();
	}

	/**
	 * Gets value of the latest item
	 * 
	 * @return value of the latest item
	 */
	public String getValue() {
		String value = ""; //$NON-NLS-1$
		if (!events.isEmpty()) {
			value = events.get(0).getValue();
		}
		return value;
	}
}
