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
 * Def handler
 *
 */
package com.nokia.trace.dictionary.model.handlers;

import org.xml.sax.Attributes;

import com.nokia.trace.dictionary.TraceDictionaryEngine;
import com.nokia.trace.dictionary.model.DecodeObject;
import com.nokia.trace.dictionary.model.DictionaryContentHandler;
import com.nokia.trace.dictionary.model.DictionaryDecodeModel;
import com.nokia.trace.dictionary.model.TraceData;
import com.nokia.trace.dictionary.model.decodeparameters.ArrayParameter;
import com.nokia.trace.dictionary.model.decodeparameters.ConstantParameter;
import com.nokia.trace.dictionary.model.decodeparameters.DecodeParameter;
import com.nokia.trace.dictionary.model.decodeparameters.HexParameter;
import com.nokia.trace.dictionary.model.decodeparameters.IntegerParameter;
import com.nokia.trace.eventrouter.TraceEvent;

/**
 * Def handler
 */
final class DefHandler extends BaseHandler {

	/**
	 * Tag name this handler handles
	 */
	private static final String DEF_TAG = "def"; //$NON-NLS-1$

	/**
	 * Bytes in block
	 */
	private int bytesInBlock;

	/**
	 * Constructor
	 * 
	 * @param model
	 *            the model
	 */
	DefHandler(DictionaryDecodeModel model) {
		super(model, DEF_TAG);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.trace.dictionary.model.handlers.BaseHandler#
	 * processStartElement(org.xml.sax.Attributes,
	 * com.nokia.trace.dictionary.model.DictionaryContentHandler)
	 */
	@Override
	public void processStartElement(Attributes atts,
			DictionaryContentHandler handler) {
		TraceData traceData = new TraceData(
				Integer.parseInt(atts.getValue(ID)), atts.getValue(TYPE));
		handler.catchElementContents(traceData);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.nokia.trace.dictionary.model.handlers.BaseHandler#processEndElement
	 * (java.lang.StringBuffer, java.lang.Object,
	 * com.nokia.trace.dictionary.model.DictionaryContentHandler,
	 * com.nokia.trace.dictionary.model.DecodeObject)
	 */
	@Override
	public void processEndElement(StringBuffer elementContent,
			Object unFinishedObject, DictionaryContentHandler handler,
			DecodeObject parentObject) {

		TraceData traceData = (TraceData) unFinishedObject;

		// Check if there is only one variable in the trace. If
		// split has two parts, there is only one variable indicator
		if (elementContent.toString().split(VARIABLE_INDICATOR).length <= 2) {
			traceData.setContainsOnlyOneVariable(true);
		}

		int startOffset = 0;
		int foundOffset = 0;
		int endOffset = 0;

		// Search all variables from the string
		while ((foundOffset = elementContent.indexOf(VARIABLE_INDICATOR,
				startOffset)) != -1) {
			createConstantParameter(elementContent, traceData, startOffset,
					foundOffset);

			String parameterType;
			StringBuilder digitsSB = new StringBuilder();
			boolean isArray = false;

			int nextCharOffset = foundOffset + 1;

			// First gather all possible digits to variable. e.g. from %02x
			// gather 02
			char c = elementContent.charAt(nextCharOffset);
			while (Character.isDigit(c)) {
				digitsSB.append(c);
				nextCharOffset++;
				c = elementContent.charAt(nextCharOffset);
			}

			// Check if next char is {
			if (c == START_BRACKET) {
				// Find offset of char }
				endOffset = elementContent.indexOf(Character
						.toString(END_BRACKET), foundOffset) + 1;

				// End offset was not found,let's change it to end of whole
				// string to get some reasonable error message
				if (endOffset == 0) {
					endOffset = elementContent.length();
				}

				// Try to find array indicator characters
				int indexOfBrackets = elementContent.indexOf(ARRAY_INDICATOR,
						nextCharOffset);
				// Indicator is found and is inside this parameter -> array
				if (indexOfBrackets != -1
						&& indexOfBrackets + ARRAY_INDICATOR.length() < endOffset) {
					isArray = true;
				}

				// Parse from between
				parameterType = elementContent.substring(nextCharOffset,
						endOffset);

				// Next char is not {, so it's a format char
			} else {

				// If the first char is L, l, or h, format char has 2 or 3 chars
				if (c == FORMATCHAR_INDICATOR_BIG_L
						|| c == FORMATCHAR_INDICATOR_SMALL_L
						|| c == FORMATCHAR_INDICATOR_H) {
					c = elementContent.charAt(nextCharOffset + 1);

					// Next char is also h or l, format char is 3 chars
					if (c == FORMATCHAR_INDICATOR_H
							|| c == FORMATCHAR_INDICATOR_SMALL_L) {
						parameterType = elementContent.substring(
								nextCharOffset, nextCharOffset
										+ LONG_FORMATCHAR_LENGTH + 1);
						endOffset = nextCharOffset + LONG_FORMATCHAR_LENGTH + 1;

						// Format char is 2 chars
					} else {
						parameterType = elementContent.substring(
								nextCharOffset, nextCharOffset
										+ LONG_FORMATCHAR_LENGTH);
						endOffset = nextCharOffset + LONG_FORMATCHAR_LENGTH;
					}

					// Only 1 char
				} else {
					parameterType = elementContent.substring(nextCharOffset,
							nextCharOffset + SHORT_FORMATCHAR_LENGTH);
					endOffset = nextCharOffset + SHORT_FORMATCHAR_LENGTH;
				}
			}

			DecodeParameter parameter;

			// Array parameter
			if (isArray) {
				parameter = handleArray(traceData, parameterType, digitsSB
						.toString());

				// Normal parameter
			} else {
				parameter = getDecodeParameter(parameterType, digitsSB
						.toString());

				if (parameter != null) {
					// Add possible fillers before this new parameter
					addFillers(traceData, parameter.getSize());

					// Add to the parameter list
					traceData.addDecodeParameter(parameter);
				}
			}

			// Parameter couldn't be found or created, inform user
			if (parameter == null) {
				postParameterNotFoundMsg(handler, parameterType);
			}

			// Set start offset to previous end offset
			startOffset = endOffset;
		} // while ends

		// Create constant from the remaining
		createConstantParameter(elementContent, traceData, startOffset,
				elementContent.length());

		// Add trace data to model
		model.addTraceData(traceData);

		// Null bytes in block value afterwards
		bytesInBlock = 0;
	}

	/**
	 * Handles array
	 * 
	 * @param traceData
	 *            trace data
	 * @param parameterType
	 *            parameter type
	 * @param digits
	 * @return created parameter or null if cannot be created
	 */
	private DecodeParameter handleArray(TraceData traceData,
			String parameterType, String digits) {
		DecodeParameter parameter = null;

		// Remove array indicator and try decode parameter again
		String arrayContainsStr = parameterType.replace(ARRAY_INDICATOR, ""); //$NON-NLS-1$
		DecodeParameter arrayContains = getDecodeParameter(arrayContainsStr,
				digits);

		// Basic type is found, create array parameter and add it
		// also to the model and to the trace parameter list
		if (arrayContains != null) {
			parameter = new ArrayParameter(parameterType, false, arrayContains);

			model.addDecodeParameter(parameter);
			traceData.addDecodeParameter(parameter);
		}

		return parameter;
	}

	/**
	 * Gets (either from model or creates new) decode parameter
	 * 
	 * @param parameterType
	 *            parameter type to be get
	 * @param digits
	 *            possible format digits
	 * @return found or new decode parameter or null if not found
	 */
	private DecodeParameter getDecodeParameter(String parameterType,
			String digits) {
		String parameterWithFormat = digits + parameterType;

		// Find from parameter list using the possible formatting (e.g. %02)
		DecodeParameter parameter = model
				.getDecodeParameter(parameterWithFormat);

		// Not found, try to find without the formatting
		if (parameter == null) {
			parameter = model.getDecodeParameter(parameterType);

			// If found, we need to create new decode parameter with the
			// formatting included
			if (parameter != null) {
				parameter = createNewFormatDecodeParameter(parameter,
						parameterWithFormat, digits);
			}
		}

		return parameter;
	}

	/**
	 * Create new decode parameter with formatting
	 * 
	 * @param oldParameter
	 *            old parameter
	 * @param parameterWithFormat
	 *            new parameter name
	 * @param digits
	 *            how should the parameter be formatted
	 * @return new decode parameter
	 */
	private DecodeParameter createNewFormatDecodeParameter(
			DecodeParameter oldParameter, String parameterWithFormat,
			String digits) {
		DecodeParameter newParameter = null;

		// This is already checked with "isDigit()" so doesn't need try catch
		int digit = Integer.parseInt(digits);

		// Integer parameter
		if (oldParameter instanceof IntegerParameter) {
			newParameter = new IntegerParameter(parameterWithFormat, false,
					oldParameter.getSize(), true);
			((IntegerParameter) newParameter).setFormatToChars(digit);
		}

		// Hex parameter
		else if (oldParameter instanceof HexParameter) {
			newParameter = new HexParameter(parameterWithFormat, false,
					oldParameter.getSize());
			((HexParameter) newParameter)
					.setPrintInUpperCase(((HexParameter) oldParameter)
							.getPrintInUpperCase());
			((HexParameter) newParameter).setFormatToChars(digit);
		}

		// Add to the parameter list
		if (newParameter != null) {
			model.addDecodeParameter(newParameter);
		}

		return newParameter;
	}

	/**
	 * Posts parameter not found message to user
	 * 
	 * @param handler
	 *            Dictionary handler
	 * @param parameterType
	 *            parameter type that is not found
	 */
	private void postParameterNotFoundMsg(DictionaryContentHandler handler,
			String parameterType) {
		String parameterNotFound = Messages
				.getString("DefHandler.ParameterNotFoundMsg"); //$NON-NLS-1$
		TraceEvent event = new TraceEvent(TraceEvent.ERROR, parameterNotFound
				+ parameterType);
		event.setCategory(EVENT_CATEGORY);
		event.setSource(Integer.valueOf(handler.getLocator().getLineNumber()));
		TraceDictionaryEngine.postEvent(event);
	}

	/**
	 * Adds possible filler parameters to traceData
	 * 
	 * @param traceData
	 *            traceData
	 * @param paramSize
	 *            size of the new item
	 */
	private void addFillers(TraceData traceData, int paramSize) {
		// Parameters are aligned to 32 bits. Parameter after
		// end-of-string is aligned dynamically and thus no filler is
		// created for it
		if (paramSize == 0 || paramSize == 4 || paramSize == 8) {
			if (bytesInBlock > 0) {
				int fillerCount = 4 - bytesInBlock;
				for (int i = 0; i < fillerCount; i++) {
					traceData.addDecodeParameter(model.getFillerParameter());
				}
				bytesInBlock = 0;
			}
		} else if (paramSize == 2) {
			if (bytesInBlock == 1 || bytesInBlock == 3) {
				traceData.addDecodeParameter(model.getFillerParameter());
				// If there was 1 existing byte and filler was added,
				// the number of bytes in the block is now 4 including
				// the 2-byte parameter. If there was 3 bytes, the
				// filler brings it to 4 and the 16-bit parameter
				// changes it to 2
				bytesInBlock += 3;
			} else {
				bytesInBlock += 2;
			}
			if (bytesInBlock >= 4) {
				bytesInBlock -= 4;
			}
		} else {
			bytesInBlock++;
			if (bytesInBlock == 4) {
				bytesInBlock = 0;
			}
		}
	}

	/**
	 * Create constant parameter
	 * 
	 * @param elementContent
	 *            buffer where data is
	 * @param traceData
	 *            TraceData where to insert this parameter
	 * @param startOffset
	 *            start offset from buffer
	 * @param endOffset
	 *            end offset from buffer
	 */
	private void createConstantParameter(StringBuffer elementContent,
			TraceData traceData, int startOffset, int endOffset) {

		// Remove line breaks from the buffer
		int len = elementContent.length();
		for (int i = 0; i < len; i++) {
			char c = elementContent.charAt(i);
			if (c == '\n' || c == '\r') {
				elementContent.setCharAt(i, ' ');
			}
		}

		// First create constant parameter
		String constantPar = elementContent.substring(startOffset, endOffset);
		if (constantPar.length() > 0) {
			// Try to find from the list
			ConstantParameter parameter = model
					.getConstantParameter(constantPar);

			// Couldn't be found
			if (parameter == null) {
				parameter = new ConstantParameter(constantPar, false);
			}

			// Add to lists
			traceData.addDecodeParameter(parameter);
			model.addConstantParameter(parameter);
		}
	}

}
