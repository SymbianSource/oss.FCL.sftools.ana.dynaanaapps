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
 * Event handler
 *
 */
package com.nokia.trace.eventview;

import com.nokia.trace.eventrouter.TraceEvent;

/**
 * Event handler
 * 
 */
public interface TraceEventHandler {

	/**
	 * Handles an event. If this returns true, the event is not delegated to
	 * other handlers. If all registered handlers return false,
	 * {@link EventListEntryString} is added to the view.
	 * 
	 * @param event
	 *            the event to be handled
	 * @return true if event was handled, false if not
	 */
	public boolean handleEvent(TraceEvent event);

}
