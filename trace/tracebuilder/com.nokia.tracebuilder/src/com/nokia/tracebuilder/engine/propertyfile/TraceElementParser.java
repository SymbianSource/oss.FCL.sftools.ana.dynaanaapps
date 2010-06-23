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
* Parser for trace element
*
*/
package com.nokia.tracebuilder.engine.propertyfile;

/**
 * Parser for trace element
 * 
 */
final class TraceElementParser extends ObjectElementParser {

	/**
	 * Constructor
	 * 
	 * @param propertyFileParser
	 *            the parser
	 */
	TraceElementParser(PropertyFileParser propertyFileParser) {
		super(propertyFileParser);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.engine.propertyfile.ObjectElementParser#findPropertyList(java.lang.String)
	 */
	@Override
	TraceObjectPropertyListImpl findPropertyList(String name) {
		return propertyFileParser.getTracePropertyList(name);
	}

}
