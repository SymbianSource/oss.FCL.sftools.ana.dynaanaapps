/*
* Copyright (c) 2007 Nokia Corporation and/or its subsidiary(-ies). 
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
* Interface that is notified about changes in source documents
*
*/
package com.nokia.tracebuilder.engine.source;

import com.nokia.tracebuilder.engine.TraceLocation;

/**
 * Interface that is notified about changes in source documents managed by
 * {@link SourceEngine}
 * 
 */
public interface SourceListener {

	/**
	 * Called when caret position in source is moved on top of a trace in source
	 * file
	 * 
	 * @param location
	 *            the trace location
	 */
	public void selectionChanged(TraceLocation location);

	/**
	 * Event that is fired when a source is opened
	 * 
	 * @param properties
	 *            the source properties
	 */
	public void sourceOpened(SourceProperties properties);

	/**
	 * Event that is fired when a source is changed
	 * 
	 * @param properties
	 *            the new properties of the source
	 */
	public void sourceChanged(SourceProperties properties);

	/**
	 * Event that is fired when a source is closed
	 * 
	 * @param source
	 *            the source that was closed
	 */
	public void sourceClosed(SourceProperties source);

	/**
	 * Operation which affects multiple source locations was started
	 * 
	 * @param source
	 *            the source
	 */
	public void sourceProcessingStarted(SourceProperties source);

	/**
	 * Operation which affects multiple source locations was completed
	 * 
	 * @param source
	 *            the source
	 */
	public void sourceProcessingComplete(SourceProperties source);

	/**
	 * Notification that source has been saved
	 * 
	 * @param properties
	 *            the source
	 */
	public void sourceSaved(SourceProperties properties);
}
