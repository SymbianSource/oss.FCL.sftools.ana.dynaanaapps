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
 * Base class for event list entries
 *
 */
package com.nokia.trace.eventview;

import org.eclipse.jface.action.IMenuManager;

/**
 * Base class for event list entries
 * 
 */
public abstract class EventListEntry {

	/**
	 * Event type
	 */
	private int type;

	/**
	 * Event category
	 */
	private String category;

	/**
	 * Constructor
	 * 
	 * @param type
	 *            event type
	 */
	protected EventListEntry(int type) {
		this.type = type;
	}

	/**
	 * Gets the event type
	 * 
	 * @return event type
	 */
	public int getType() {
		return type;
	}

	/**
	 * Gets the event category
	 * 
	 * @return the category
	 */
	public String getCategory() {
		return category;
	}

	/**
	 * Sets the event category
	 * 
	 * @param category
	 *            the new category
	 */
	public void setCategory(String category) {
		this.category = category;
	}

	/**
	 * Resets this entry
	 */
	protected void reset() {
	}

	/**
	 * Gets the event description
	 * 
	 * @return event description
	 */
	public abstract String getDescription();

	/**
	 * Flags which determines if this entry has source
	 * 
	 * @return true if this entry has a source
	 */
	protected abstract boolean hasSource();

	/**
	 * Gets the name of source
	 * 
	 * @return the name of source
	 */
	protected abstract String getSourceName();

	/**
	 * Gets the source
	 * 
	 * @return the source
	 */
	protected abstract Object getSource();

	/**
	 * Flags which determines if this entry has source associated with actions
	 * 
	 * @return true if this entry has a source which is associated to actions
	 */
	protected abstract boolean hasSourceActions();

	/**
	 * Adds actions to given menu
	 * 
	 * @param manager
	 *            the menu manager
	 */
	protected abstract void addSourceActions(IMenuManager manager);

}