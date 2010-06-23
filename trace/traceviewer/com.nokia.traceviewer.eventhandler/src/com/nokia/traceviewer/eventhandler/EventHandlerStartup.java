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
 * Plugin startup class
 *
 */
package com.nokia.traceviewer.eventhandler;

import org.eclipse.ui.IStartup;

import com.nokia.trace.eventview.TraceEventView;

/**
 * Plugin startup class
 */
public final class EventHandlerStartup implements IStartup {

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IStartup#earlyStartup()
	 */
	public void earlyStartup() {

		// By calling getEventList, we get EventView to register to
		// EventRouter so the messages will go through to it.
		TraceEventView.getEventList();
	}
}
