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
* Parser for enum element
*
*/
package com.nokia.tracebuilder.engine.propertyfile;

import org.w3c.dom.Element;

import com.nokia.tracebuilder.engine.TraceBuilderGlobals;
import com.nokia.tracebuilder.model.TraceBuilderException;
import com.nokia.tracebuilder.model.TraceConstantTable;
import com.nokia.tracebuilder.model.TraceModel;
import com.nokia.tracebuilder.model.TraceModelExtension;

/**
 * Parser for enum element
 * 
 */
final class EnumElementParser implements PropertyFileElementParser {

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
	EnumElementParser(PropertyFileParser propertyFileParser) {
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
		TraceModel model = TraceBuilderGlobals.getTraceModel();
		String name = element
				.getAttribute(PropertyFileConstants.NAME_ATTRIBUTE);
		int id = model.getNextConstantTableID();
		model.getVerifier().checkConstantTableProperties(model, null, id, name);
		// Document element reference is stored to the model
		TraceModelExtension[] exts = new TraceModelExtension[] { new DocumentElementWrapper(
				element) };
		TraceConstantTable table = model.getFactory().createConstantTable(id,
				name, exts);
		propertyFileParser.parseChildren(table, element);
	}
}