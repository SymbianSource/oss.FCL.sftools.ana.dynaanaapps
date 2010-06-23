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
* Document processor interface
*
*/
package com.nokia.tracebuilder.source;

/**
 * Document processor interface
 * 
 */
public interface SourceDocumentProcessor {

	/**
	 * Notification about source opened
	 * 
	 * @param document
	 *            the document
	 */
	public void sourceOpened(SourceDocumentInterface document);

	/**
	 * Notification before source change
	 * 
	 * @param source
	 *            the source
	 * @param offset
	 *            removed text offset
	 * @param length
	 *            removed text length
	 * @param newText
	 *            new text
	 */
	public void sourceAboutToBeChanged(SourceDocumentInterface source,
			int offset, int length, String newText);

	/**
	 * Notification about source change
	 * 
	 * @param document
	 *            the document
	 * @param offset
	 *            removed text offset
	 * @param length
	 *            removed text length
	 * @param newText
	 *            new text
	 */
	public void sourceChanged(SourceDocumentInterface document, int offset,
			int length, String newText);

	/**
	 * Notification about source closed
	 * 
	 * @param document
	 *            the document
	 */
	public void sourceClosed(SourceDocumentInterface document);

	/**
	 * Notification about change in selection
	 * 
	 * @param source
	 *            the source
	 * @param offset
	 *            the offset of selection
	 * @param length
	 *            the length of selection
	 */
	public void selectionChanged(SourceDocumentInterface source, int offset,
			int length);

	/**
	 * Source save notification
	 * 
	 * @param source
	 *            the source
	 */
	public void sourceSaved(SourceDocumentInterface source);

}
