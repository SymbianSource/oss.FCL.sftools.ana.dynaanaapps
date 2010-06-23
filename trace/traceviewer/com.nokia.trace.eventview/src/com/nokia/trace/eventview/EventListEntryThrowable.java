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
 * Event list entry, which contains a Throwable
 *
 */
package com.nokia.trace.eventview;

import org.eclipse.jface.action.IMenuManager;

/**
 * Event list entry, which contains a Throwable
 * 
 */
final class EventListEntryThrowable extends EventListEntryString {

	/**
	 * Action to show the exception to user
	 */
	private ShowExceptionAction action;

	/**
	 * Source
	 */
	private Throwable throwable;

	/**
	 * Constructor
	 * 
	 * @param type
	 *            event type
	 * @param description
	 *            event description
	 * @param source
	 *            event source
	 */
	EventListEntryThrowable(int type, String description, Throwable source) {
		super(type, description, null);
		this.throwable = source;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.nokia.trace.eventview.EventListEntryString#addSourceActions(org.eclipse
	 * .jface.action.IMenuManager)
	 */
	@Override
	protected void addSourceActions(IMenuManager manager) {
		super.addSourceActions(manager);
		if (action == null) {
			action = new ShowExceptionAction((Throwable) getSource());
		}
		manager.add(action);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.trace.eventview.EventListEntryString#getSource()
	 */
	@Override
	protected Object getSource() {
		return throwable;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.trace.eventview.EventListEntryString#getSourceName()
	 */
	@Override
	protected String getSourceName() {
		return throwable.getMessage();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.trace.eventview.EventListEntryString#hasSource()
	 */
	@Override
	protected boolean hasSource() {
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.trace.eventview.EventListEntryString#hasSourceActions()
	 */
	@Override
	protected boolean hasSourceActions() {
		return true;
	}

}
