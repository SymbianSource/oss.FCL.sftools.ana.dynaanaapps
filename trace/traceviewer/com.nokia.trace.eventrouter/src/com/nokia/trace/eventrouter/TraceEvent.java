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
 * Event
 *
 */
package com.nokia.trace.eventrouter;

/**
 * Event
 * 
 */
public class TraceEvent {

	/**
	 * Info event type
	 */
	public final static int INFO = 1;

	/**
	 * Warning event type
	 */
	public final static int WARNING = 2;

	/**
	 * Error event type
	 */
	public final static int ERROR = 3;

	/**
	 * Critical assertion failure event type
	 */
	public final static int ASSERT_CRITICAL = 4;

	/**
	 * Non-critical assertion failure event type
	 */
	public final static int ASSERT_NORMAL = 5;

	/**
	 * Event type
	 */
	private int type;

	/**
	 * Event description
	 */
	private String description;

	/**
	 * Event category
	 */
	private String category;

	/**
	 * Event source object
	 */
	private Object source;

	/**
	 * Creates a new event
	 * 
	 * @param type
	 *            the event type
	 * @param description
	 *            the event description
	 */
	public TraceEvent(int type, String description) {
		this.type = type;
		this.description = description;
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
	 * Gets the event description
	 * 
	 * @return the event description
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * Sets the source object
	 * 
	 * @param source
	 *            the source object
	 */
	public void setSource(Object source) {
		this.source = source;
	}

	/**
	 * Gets the event source
	 * 
	 * @return the event source
	 */
	public Object getSource() {
		return source;
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
	 *            the category to set
	 */
	public void setCategory(String category) {
		this.category = category;
	}

}
