/*
 * Copyright (c) 2007-2010 Nokia Corporation and/or its subsidiary(-ies). 
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
 * TypeMember handler
 *
 */
package com.nokia.trace.dictionary.model.handlers;

import org.xml.sax.Attributes;

import com.nokia.trace.dictionary.TraceDictionaryEngine;
import com.nokia.trace.dictionary.model.DictionaryContentHandler;
import com.nokia.trace.dictionary.model.DictionaryDecodeModel;
import com.nokia.trace.dictionary.model.decodeparameters.CompoundParameter;
import com.nokia.trace.dictionary.model.decodeparameters.DecodeParameter;
import com.nokia.trace.dictionary.model.decodeparameters.EnumMember;
import com.nokia.trace.dictionary.model.decodeparameters.EnumParameter;
import com.nokia.trace.eventrouter.TraceEvent;

/**
 * TypeMember handler
 */
final class TypeMemberHandler extends BaseHandler {

	/**
	 * Tag name this handler handles
	 */
	private static final String TYPEMEMBER_TAG = "typemember"; //$NON-NLS-1$

	/**
	 * Constructor
	 * 
	 * @param model
	 *            the model
	 */
	TypeMemberHandler(DictionaryDecodeModel model) {
		super(model, TYPEMEMBER_TAG);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.nokia.trace.dictionary.model.handlers.BaseHandler#processStartElement
	 * (org.xml.sax.Attributes,
	 * com.nokia.trace.dictionary.model.DictionaryContentHandler)
	 */
	@Override
	public void processStartElement(Attributes atts,
			DictionaryContentHandler handler) {
		// Check are we doing enum or compound
		if (atts.getValue(VALUE) != null) {
			// Get the previous enum parameter
			EnumParameter prevEnum = handler.getVariables()
					.getPreviousEnumParameter();

			// Create new enum member
			EnumMember enumMember = new EnumMember(atts.getValue(NAME), Integer
					.parseInt(atts.getValue(VALUE)));

			// Add to enum parameter
			prevEnum.addMember(enumMember);

		} else if (atts.getValue(TYPE) != null) {
			// Get the parameter from model
			DecodeParameter parameter = model.getDecodeParameter(atts
					.getValue(TYPE));

			// Get the previous compoundParameter
			CompoundParameter prevCompound = handler.getVariables()
					.getPreviousCompoundParameter();

			// Add parameter and it's name to compoundParameter
			prevCompound.addParameter(parameter, atts.getValue(NAME));

		} else {
			TraceEvent event = new TraceEvent(TraceEvent.ERROR, Messages
					.getString("TypeMemberHandler.WrongTypeMember")); //$NON-NLS-1$
			event.setCategory(EVENT_CATEGORY);
			event.setSource(Integer.valueOf(handler.getLocator()
					.getLineNumber()));
			TraceDictionaryEngine.postEvent(event);
		}
	}
}
