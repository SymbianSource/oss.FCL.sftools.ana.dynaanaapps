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
* Preprocessor definitions found from source
*
*/
package com.nokia.tracebuilder.source;

/**
 * Preprocessor definitions found from source
 * 
 */
public class SourcePreprocessorDefinition extends SourceLocationBase {

	/**
	 * Definition name
	 */
	private String name;

	/**
	 * Definition value
	 */
	private String value;

	/**
	 * Constructor
	 * 
	 * @param parser
	 *            the document parser
	 * @param offset
	 *            the offset to the location within source
	 */
	SourcePreprocessorDefinition(SourceParser parser, int offset) {
		super(parser, offset);
	}

	/**
	 * Sets the definition name
	 * 
	 * @param name
	 *            the name
	 */
	void setName(String name) {
		this.name = name;
	}

	/**
	 * Sets the definition value
	 * 
	 * @param value
	 *            the value
	 */
	void setValue(String value) {
		this.value = value;
	}

	/**
	 * Returns the name of the definition
	 * 
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Returns the value of the definition
	 * 
	 * @return the value
	 */
	public String getValue() {
		return value;
	}

}
