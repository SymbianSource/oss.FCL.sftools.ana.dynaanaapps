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
 * Object handler
 *
 */
package com.nokia.trace.dictionary.model.handlers;

import org.xml.sax.Attributes;

import com.nokia.trace.dictionary.TraceDictionaryEngine;
import com.nokia.trace.dictionary.model.DictionaryContentHandler;
import com.nokia.trace.dictionary.model.DictionaryDecodeModel;
import com.nokia.trace.dictionary.model.decodeparameters.BinaryParameter;
import com.nokia.trace.dictionary.model.decodeparameters.CompoundParameter;
import com.nokia.trace.dictionary.model.decodeparameters.DecodeParameter;
import com.nokia.trace.dictionary.model.decodeparameters.EnumParameter;
import com.nokia.trace.dictionary.model.decodeparameters.FloatParameter;
import com.nokia.trace.dictionary.model.decodeparameters.HexParameter;
import com.nokia.trace.dictionary.model.decodeparameters.IntegerParameter;
import com.nokia.trace.dictionary.model.decodeparameters.OctalParameter;
import com.nokia.trace.dictionary.model.decodeparameters.RawParameter;
import com.nokia.trace.dictionary.model.decodeparameters.StringParameter;
import com.nokia.trace.eventrouter.TraceEvent;

/**
 * Object handler
 */
final class ObjectHandler extends BaseHandler {

	/**
	 * Tag name this handler handles
	 */
	private static final String OBJECT_TAG = "object"; //$NON-NLS-1$

	/**
	 * String type
	 */
	private static final String STRING = "string"; //$NON-NLS-1$

	/**
	 * Integer type
	 */
	private static final String INTEGER = "integer"; //$NON-NLS-1$

	/**
	 * Float type
	 */
	private static final String FLOAT = "float"; //$NON-NLS-1$

	/**
	 * Hex type
	 */
	private static final String HEX = "hex"; //$NON-NLS-1$

	/**
	 * Binary type
	 */
	private static final String BINARY = "binary"; //$NON-NLS-1$

	/**
	 * Octal type
	 */
	private static final String OCTAL = "octal"; //$NON-NLS-1$

	/**
	 * Enum type
	 */
	private static final String ENUM = "enum"; //$NON-NLS-1$

	/**
	 * Raw type
	 */
	private static final String RAW = "raw"; //$NON-NLS-1$

	/**
	 * Compound type
	 */
	private static final String COMPOUND = "compound"; //$NON-NLS-1$

	/**
	 * Constructor
	 * 
	 * @param model
	 *            the model
	 */
	ObjectHandler(DictionaryDecodeModel model) {
		super(model, OBJECT_TAG);
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
		StringBuffer typeString = new StringBuffer();
		String type = atts.getValue(TYPE);
		typeString.append(type);

		// Add these brackets around the type
		typeString.insert(0, START_BRACKET);
		typeString.append(END_BRACKET);

		DecodeParameter newParameter = null;
		DecodeParameter newFormatCharParameter = null;

		String classification = atts.getValue(CLASSIFICATION);
		String formatChar = atts.getValue(FORMATCHAR);
		int size = Integer.parseInt(atts.getValue(SIZE));

		// String
		if (classification.equals(STRING)) {
			newParameter = new StringParameter(typeString.toString(), false,
					size);
			if (formatChar != null) {
				newFormatCharParameter = new StringParameter(formatChar, false,
						size);
			}
			// Integer
		} else if (classification.equals(INTEGER)) {
			newParameter = new IntegerParameter(typeString.toString(), false,
					size, true); // Always signed
			if (formatChar != null) {
				newFormatCharParameter = new IntegerParameter(formatChar,
						false, size, true);
			}
			// Float
		} else if (classification.equals(FLOAT)) {
			newParameter = new FloatParameter(typeString.toString(), false,
					size, true); // Always signed
			if (formatChar != null) {
				newFormatCharParameter = new FloatParameter(formatChar, false,
						size, true);
			}
			// Hex
		} else if (classification.equals(HEX)) {
			newParameter = new HexParameter(typeString.toString(), false, size);
			if (formatChar != null) {
				newFormatCharParameter = new HexParameter(formatChar, false,
						size);

				// Set to be printed in upper case letters
				if (formatChar.indexOf('X') != -1) {
					((HexParameter) newFormatCharParameter)
							.setPrintInUpperCase(true);
				}
			}
			// Binary
		} else if (classification.equals(BINARY)) {
			newParameter = new BinaryParameter(typeString.toString(), false,
					size);
			if (formatChar != null) {
				newFormatCharParameter = new BinaryParameter(formatChar, false,
						size);
			}
			// Octal
		} else if (classification.equals(OCTAL)) {
			newParameter = new OctalParameter(typeString.toString(), false,
					size);
			if (formatChar != null) {
				newFormatCharParameter = new OctalParameter(formatChar, false,
						size);
			}
			// Enum
		} else if (classification.equals(ENUM)) {
			newParameter = new EnumParameter(typeString.toString(), false, size);
			handler.getVariables().setPreviousEnumParameter(
					(EnumParameter) newParameter);
			// Raw
		} else if (classification.equals(RAW)) {
			newParameter = new RawParameter(typeString.toString(), false);
			if (formatChar != null) {
				newFormatCharParameter = new RawParameter(formatChar, false);
			}
			// Compound
		} else if (classification.equals(COMPOUND)) {
			newParameter = new CompoundParameter(typeString.toString(), false);
			handler.getVariables().setPreviousCompoundParameter(
					(CompoundParameter) newParameter);
		}

		// Add to the model
		DecodeParameter oldParameter = model.addDecodeParameter(newParameter);
		checkCollision(oldParameter, newParameter, handler);
		if (newFormatCharParameter != null) {
			DecodeParameter oldParameter2 = model
					.addDecodeParameter(newFormatCharParameter);
			checkCollision(oldParameter2, newFormatCharParameter, handler);
		}
	}

	/**
	 * Checks that new decode parameter didn't collide with old one
	 * 
	 * @param oldParameter
	 *            old parameter
	 * @param newParameter
	 *            new parameter
	 * @param handler
	 *            dictionary content handler
	 */
	private void checkCollision(DecodeParameter oldParameter,
			DecodeParameter newParameter, DictionaryContentHandler handler) {
		boolean collision = true;
		// If old parameter is null, everyrything is ok. If not, check collision
		if (oldParameter != null) {
			// Classes must be same
			if (oldParameter.getClass() == newParameter.getClass()) {
				// Types must be same
				if (oldParameter.getType().equals(newParameter.getType())) {
					// Sizes must be same
					if (oldParameter.getSize() == newParameter.getSize()) {
						collision = false;
					}
				}
			}
			if (collision) {
				TraceEvent event = new TraceEvent(TraceEvent.WARNING, Messages
						.getString("ObjectHandler.CollisionParameter") //$NON-NLS-1$
						+ oldParameter.getType());
				event.setCategory(EVENT_CATEGORY);
				event.setSource(Integer.valueOf(handler.getLocator()
						.getLineNumber()));
				TraceDictionaryEngine.postEvent(event);
			}
		}
	}
}
