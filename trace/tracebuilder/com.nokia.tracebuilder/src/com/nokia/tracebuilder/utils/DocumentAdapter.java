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
* Adapter for document interface
*
*/
package com.nokia.tracebuilder.utils;

import com.nokia.tracebuilder.source.SourceDocumentInterface;
import com.nokia.tracebuilder.source.SourceLocationInterface;
import com.nokia.tracebuilder.source.SourceParserException;
import com.nokia.tracebuilder.source.SourcePropertyProvider;
import com.nokia.tracebuilder.source.SourceSelector;

/**
 * Adapter for document interface
 * 
 */
public class DocumentAdapter implements SourceDocumentInterface {

	/**
	 * Document owner
	 */
	private Object owner;

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.source.SourceDocumentInterface#
	 *      addLocation(com.nokia.tracebuilder.source.SourceLocationInterface)
	 */
	public void addLocation(SourceLocationInterface location) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.source.SourceDocumentInterface#get(int, int)
	 */
	public String get(int start, int length) throws SourceParserException {
		return ""; //$NON-NLS-1$
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.source.SourceDocumentInterface#getChar(int)
	 */
	public char getChar(int offset) throws SourceParserException {
		return '\0';
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.source.SourceDocumentInterface#getLength()
	 */
	public int getLength() {
		return 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.source.SourceDocumentInterface#
	 *      getLineOfOffset(int)
	 */
	public int getLineOfOffset(int offset) throws SourceParserException {
		return -1;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.source.SourceDocumentInterface#getOwner()
	 */
	public Object getOwner() {
		return owner;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.source.SourceDocumentInterface#
	 *      removeLocation(com.nokia.tracebuilder.source.SourceLocationInterface)
	 */
	public void removeLocation(SourceLocationInterface location) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.source.SourceDocumentInterface#replace(int,
	 *      int, java.lang.String)
	 */
	public void replace(int offset, int length, String newText)
			throws SourceParserException {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.source.SourceDocumentInterface#
	 *      setOwner(java.lang.Object)
	 */
	public void setOwner(Object owner) {
		this.owner = owner;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.source.SourceDocumentInterface#getPropertyProvider()
	 */
	public SourcePropertyProvider getPropertyProvider() {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.source.SourceDocumentInterface#getSourceSelector()
	 */
	public SourceSelector getSourceSelector() {
		return null;
	}

}
