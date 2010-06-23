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
* Parser for component element
*
*/
package com.nokia.tracebuilder.engine.propertyfile;

import org.w3c.dom.Element;

import com.nokia.tracebuilder.engine.TraceBuilderGlobals;
import com.nokia.tracebuilder.model.TraceBuilderException;
import com.nokia.tracebuilder.model.TraceModel;

/**
 * Parser for component element
 * 
 */
final class ComponentElementParser implements PropertyFileElementParser {

	/**
	 * Property file parser
	 */
	private final PropertyFileParser propertyFileParser;

	/**
	 * Constructor
	 * 
	 * @param propertyFileParser
	 *            the parser
	 */
	ComponentElementParser(PropertyFileParser propertyFileParser) {
		this.propertyFileParser = propertyFileParser;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.engine.propertyfile.PropertyFileElementParser#
	 *      parse(java.lang.Object, org.w3c.dom.Element)
	 */
	public void parse(Object owner, Element element)
			throws TraceBuilderException {
		// Adds the properties directly to the model
		TraceModel model = TraceBuilderGlobals.getTraceModel();
		TraceObjectPropertyListImpl propertyList = model
				.getExtension(TraceObjectPropertyListImpl.class);
		if (propertyList == null) {
			propertyList = new TraceObjectPropertyListImpl();
			model.addExtension(propertyList);
		}
		propertyFileParser.parseChildren(propertyList, element);
	}

}
