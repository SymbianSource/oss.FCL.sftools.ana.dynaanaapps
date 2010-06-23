/*
* Copyright (c) 2008 Nokia Corporation and/or its subsidiary(-ies). 
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
* Event listener interface
*
*/
package com.nokia.tracebuilder.engine;

import com.nokia.tracebuilder.model.TraceBuilderException;

/**
 * Event listener interface
 * 
 */
public interface TraceBuilderEvents {

	/**
	 * Event category for verbose info events
	 */
	String VERBOSE_INFO = "Verbose"; //$NON-NLS-1$

	/**
	 * Posts an error event to TraceBuilder
	 * 
	 * @param exception
	 *            the error data
	 */
	public void postError(TraceBuilderException exception);

	/**
	 * Posts an error event to TraceBuilder
	 * 
	 * @param message
	 *            the error message
	 * @param source
	 *            the error source
	 * @param postEvent
	 *            defines is error event also posted to trace event view
	 */
	public void postErrorMessage(String message, Object source, boolean postEvent);

	/**
	 * Posts a warning event to TraceBuilder
	 * 
	 * @param message
	 *            the warning message
	 * @param source
	 *            the warning source
	 */
	public void postWarningMessage(String message, Object source);

	/**
	 * Posts an info event to TraceBuilder
	 * 
	 * @param message
	 *            the info message
	 * @param source
	 *            the info source
	 */
	public void postInfoMessage(String message, Object source);

	/**
	 * Posts a critical assertion failed event
	 * 
	 * @param message
	 *            the message
	 * @param source
	 *            the source of the assertion
	 */
	public void postCriticalAssertionFailed(String message, Object source);

	/**
	 * Posts an assertion failed event
	 * 
	 * @param message
	 *            the message
	 * @param source
	 *            the source of the assertion
	 */
	public void postAssertionFailed(String message, Object source);

	/**
	 * Sets the category for all events posted to event manager
	 * 
	 * @param category
	 *            the new category
	 * @return the old category
	 */
	public String setEventCategory(String category);

	/**
	 * Gets the current event category
	 * 
	 * @return the current category
	 */
	public String getEventCategory();

}
