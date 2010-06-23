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
* Factory implementation for JFace
*
*/
package com.nokia.tracebuilder.eclipse;

import org.eclipse.jface.text.Document;

import com.nokia.tracebuilder.source.SourceDocumentInterface;
import com.nokia.tracebuilder.source.SourceLocationBase;
import com.nokia.tracebuilder.source.SourceLocationInterface;
import com.nokia.tracebuilder.utils.DocumentFactoryBase;

/**
 * Factory implementation for JFace
 * 
 */
public final class JFaceDocumentFactory extends DocumentFactoryBase {

	/**
	 * Constructor for reflection
	 */
	JFaceDocumentFactory() {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.utils.DocumentFactoryAdapter#
	 *      createDocument(java.lang.String)
	 */
	@Override
	public SourceDocumentInterface createDocument(String sourceData) {
		// When created via factory, the document is not associated with
		// document monitor
		return new JFaceDocumentWrapper(new Document(sourceData));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.utils.DocumentFactoryAdapter#
	 *      createLocation(com.nokia.tracebuilder.source.SourceLocationBase,
	 *      int, int)
	 */
	@Override
	public SourceLocationInterface createLocation(SourceLocationBase base,
			int offset, int length) {
		return new JFaceLocationWrapper(base, offset, length);
	}

}
