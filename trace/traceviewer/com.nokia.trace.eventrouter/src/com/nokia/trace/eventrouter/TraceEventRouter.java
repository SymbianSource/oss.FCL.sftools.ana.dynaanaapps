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
 * Event router main class
 *
 */
package com.nokia.trace.eventrouter;

import java.util.ArrayList;

/**
 * Event router main class
 * 
 */
public class TraceEventRouter {

	/**
	 * Event router instance
	 */
	private static TraceEventRouter instance;

	/**
	 * List of listeners
	 */
	private ArrayList<TraceEventListener> listeners = new ArrayList<TraceEventListener>();

	/**
	 * Can only be created via getInstance
	 */
	private TraceEventRouter() {
	}

	/**
	 * Adds an event listener
	 * 
	 * @param listener
	 *            the event listener
	 */
	public void addEventListener(TraceEventListener listener) {
		listeners.add(listener);
	}

	/**
	 * Removes an event listener
	 * 
	 * @param listener
	 *            the event listener
	 */
	public void removeEventListener(TraceEventListener listener) {
		listeners.remove(listener);
	}

	/**
	 * Posts an event to listeners
	 * 
	 * @param event
	 *            the event
	 */
	public void postEvent(TraceEvent event) {
		for (int i = 0; i < listeners.size(); i++) {
			listeners.get(i).processEvent(event);
		}
	}

	/**
	 * Gets the event router instance
	 * 
	 * @return the event router
	 */
	public static TraceEventRouter getInstance() {
		if (instance == null) {
			instance = new TraceEventRouter();
		}
		return instance;
	}

}
