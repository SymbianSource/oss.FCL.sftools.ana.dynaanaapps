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
 * Media Callback interface
 *
 */
package com.nokia.traceviewer.engine;

/**
 * Media Callback interface
 * 
 */
public interface MediaCallback {

	/**
	 * Processes data
	 * 
	 * @param properties
	 *            trace properties
	 * 
	 */
	public void processTrace(TraceProperties properties);

	/**
	 * Informs that Reader has reached EOF
	 * 
	 * @param reader
	 *            reader hit the EOF
	 */
	public void endOfFile(DataReader reader);

	/**
	 * Notifies that data handle has changed. MediaCallback should shutdown the
	 * reader and set it to null so that it will be created again when started
	 * next time.
	 */
	public void dataHandleChanged();

}
