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
* Parser for property element
*
*/
package com.nokia.tracebuilder.engine.propertyfile;

import org.w3c.dom.Element;

import com.nokia.tracebuilder.engine.TraceBuilderErrorCodes.StringErrorParameters;
import com.nokia.tracebuilder.engine.TraceBuilderErrorCodes.TraceBuilderErrorCode;
import com.nokia.tracebuilder.model.TraceBuilderException;

/**
 * Parser for property element
 * 
 */
final class PropertyElementParser implements PropertyFileElementParser {

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.engine.propertyfile.PropertyFileElementParser#
	 *      parse(java.lang.Object, org.w3c.dom.Element)
	 */
	public void parse(Object owner, Element element)
			throws TraceBuilderException {
		// Property must be parsed within context of property list
		if (owner instanceof TraceObjectPropertyListImpl) {
			String name = element
					.getAttribute(PropertyFileConstants.NAME_ATTRIBUTE);
			if (name != null && name.length() > 0) {
				String value = element.getTextContent();
				TraceObjectPropertyImpl property = new TraceObjectPropertyImpl(
						name, value);
				((TraceObjectPropertyListImpl) owner).addProperty(property);
			} else {
				StringErrorParameters parameter = new StringErrorParameters();
				parameter.string = PropertyFileConstants.NAME_ATTRIBUTE;
				throw new TraceBuilderException(
						TraceBuilderErrorCode.PROPERTY_FILE_ATTRIBUTE_INVALID,
						parameter, null);
			}
		} else {
			StringErrorParameters parameter = new StringErrorParameters();
			parameter.string = PropertyFileConstants.PROPERTY_ELEMENT;
			throw new TraceBuilderException(
					TraceBuilderErrorCode.PROPERTY_FILE_ELEMENT_MISPLACED,
					parameter, null);
		}
	}

}
