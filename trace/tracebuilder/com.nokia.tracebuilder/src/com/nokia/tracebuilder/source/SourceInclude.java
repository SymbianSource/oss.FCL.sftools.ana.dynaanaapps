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
* #include found from source
*
*/
package com.nokia.tracebuilder.source;

/**
 * #include found from source
 * 
 */
public class SourceInclude extends SourceLocationBase {

	/**
	 * Header name
	 */
	private String name;

	/**
	 * Constructor
	 * 
	 * @param parser
	 *            the source parser
	 * @param offset
	 *            the offset to the location within source
	 */
	SourceInclude(SourceParser parser, int offset) {
		super(parser, offset);
	}

	/**
	 * Sets the header name
	 * 
	 * @param name
	 *            the name
	 */
	void setHeaderName(String name) {
		this.name = name;
	}

	/**
	 * Returns the name of the header
	 * 
	 * @return the name
	 */
	public String getHeaderName() {
		return name;
	}

}
