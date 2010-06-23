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
 * String event is not associated to anything
 *
 */
package com.nokia.trace.eventview;

import org.eclipse.jface.action.IMenuManager;

/**
 * String event is created when none of the registered event handlers process an
 * event. If the event source is instance of String, it is shown in the UI. If
 * not, the source is left null. This entry does not provide any actions to the
 * list
 * 
 */
public class EventListEntryString extends EventListEntry {

	/**
	 * Event description
	 */
	private String description;

	/**
	 * Event source
	 */
	private String source;

	/**
	 * Creates a new string entry
	 * 
	 * @param type
	 *            the event type
	 * @param description
	 *            the event description
	 * @param source
	 *            the event source
	 */
	public EventListEntryString(int type, String description, String source) {
		super(type);
		this.description = description;
		this.source = source;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.eventview.EventListEntry#getDescription()
	 */
	@Override
	public String getDescription() {
		return description;
	}

	/**
	 * Changes the description of this entry
	 * 
	 * @param description
	 *            the new description
	 */
	protected void setDescription(String description) {
		this.description = description;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.eventview.EventListEntry#hasSource()
	 */
	@Override
	protected boolean hasSource() {
		return source != null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.eventview.EventListEntry#getSourceName()
	 */
	@Override
	protected String getSourceName() {
		return source;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.trace.eventview.EventListEntry#getSource()
	 */
	@Override
	protected Object getSource() {
		return source;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.eventview.EventListEntry#hasSourceActions()
	 */
	@Override
	protected boolean hasSourceActions() {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.trace.eventview.EventListEntry#
	 * addSourceActions(org.eclipse.jface.action.IMenuManager)
	 */
	@Override
	protected void addSourceActions(IMenuManager manager) {
	}

}
