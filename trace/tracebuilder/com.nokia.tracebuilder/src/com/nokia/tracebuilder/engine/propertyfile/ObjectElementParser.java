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
* Parser for component, group and trace
*
*/
package com.nokia.tracebuilder.engine.propertyfile;

import org.w3c.dom.Element;

import com.nokia.tracebuilder.engine.TraceBuilderErrorCodes.StringErrorParameters;
import com.nokia.tracebuilder.engine.TraceBuilderErrorCodes.TraceBuilderErrorCode;
import com.nokia.tracebuilder.model.TraceBuilderException;

/**
 * Parser for component, group and trace
 * 
 */
abstract class ObjectElementParser implements PropertyFileElementParser {

	/**
	 * Property file parser
	 */
	protected final PropertyFileParser propertyFileParser;

	/**
	 * Constructor
	 * 
	 * @param propertyFileParser
	 *            the parser
	 */
	ObjectElementParser(PropertyFileParser propertyFileParser) {
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
		String name = element
				.getAttribute(PropertyFileConstants.NAME_ATTRIBUTE);
		if (name != null && name.length() > 0) {
			TraceObjectPropertyListImpl propertyList = findPropertyList(name);
			// Adds the properties to the object
			propertyFileParser.parseChildren(propertyList, element);
		} else {
			StringErrorParameters parameter = new StringErrorParameters();
			parameter.string = PropertyFileConstants.NAME_ATTRIBUTE;
			throw new TraceBuilderException(
					TraceBuilderErrorCode.PROPERTY_FILE_ATTRIBUTE_INVALID,
					parameter, null);
		}
	}

	/**
	 * Finds a property list by name
	 * 
	 * @param name
	 *            the name
	 * @return the property list
	 */
	abstract TraceObjectPropertyListImpl findPropertyList(String name);

}
