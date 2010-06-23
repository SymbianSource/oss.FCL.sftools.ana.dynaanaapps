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
* Monitor for source files
*
*/
package com.nokia.tracebuilder.source;

/**
 * Monitor for source files
 * 
 */
public interface SourceDocumentMonitor extends
		Iterable<SourceDocumentInterface> {

	/**
	 * Gets a document factory, which is shared between all documents created
	 * into this monitor
	 * 
	 * @return the factory
	 */
	public SourceDocumentFactory getFactory();

	/**
	 * Starts the monitor
	 * 
	 * @param processor
	 *            document processor callback
	 */
	public void startMonitor(SourceDocumentProcessor processor);

	/**
	 * Stops the monitor
	 */
	public void stopMonitor();

	/**
	 * Gets the currently active source
	 * 
	 * @return the source
	 */
	public SourceDocumentInterface getSelectedSource();

	/**
	 * Gets the current selection from given source
	 * 
	 * @param props
	 *            the source
	 * @return the selection
	 */
	public OffsetLength getSelection(SourceDocumentInterface props);

	/**
	 * Sets focus to currently selected source
	 */
	public void setFocus();

	/**
	 * Checks if source can be edited
	 * 
	 * @param selectedSource
	 *            the source
	 * @return true if can be edited, false if read-only
	 */
	public boolean isSourceEditable(SourceDocumentInterface selectedSource);

}
