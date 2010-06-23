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
* Parser for type definitions
*
*/
package com.nokia.tracebuilder.source;

import java.util.List;

/**
 * Parser for type definitions
 * 
 */
final class TypedefParser {

	/**
	 * Source parser
	 */
	private SourceParser parser;

	/**
	 * Constructor
	 * 
	 * @param parser
	 *            the source parser
	 */
	TypedefParser(SourceParser parser) {
		this.parser = parser;
	}

	/**
	 * Parses an enum statement from the source
	 * 
	 * @param offset
	 *            the offset to the start of the enum type
	 * @param list
	 *            the list for the name-value pairs are parsed from source
	 * @throws SourceParserException
	 *             if processing fails
	 */
	void parseEnum(int offset, List<StringValuePair> list)
			throws SourceParserException {
		EnumSearchData data = new EnumSearchData();
		data.itr = parser.createIterator(offset, SourceParser.SKIP_ALL);
		data.list = list;
		while (data.itr.hasNext() && !data.finished) {
			data.value = data.itr.next();
			if (data.value == '{') {
				processOpeningBracket(data);
			} else if (data.value == '}') {
				processClosingBracket(data);
			} else if (data.value == ';') {
				processEndOfEnum(data);
			} else if (data.openBrackets > 0) {
				processEnumChar(data);
			}
		}
	}

	/**
	 * Processes the opening bracket of the enum
	 * 
	 * @param data
	 *            the search data
	 */
	private void processOpeningBracket(EnumSearchData data) {
		data.openBrackets++;
	}

	/**
	 * Processes the closing bracket of the enum
	 * 
	 * @param data
	 *            the search data
	 * @throws SourceParserException
	 *             if processing fails
	 */
	private void processClosingBracket(EnumSearchData data)
			throws SourceParserException {
		data.openBrackets--;
		if (data.valueStartIndex != -1) {
			endValue(data);
		} else if (data.nameStartIndex != -1) {
			endName(data);
		}
	}

	/**
	 * Processes the ending ';' of the enum
	 * 
	 * @param data
	 *            the search data
	 */
	private void processEndOfEnum(EnumSearchData data) {
		if (data.openBrackets == 0) {
			data.isValid = true;
		}
		data.finished = true;
	}

	/**
	 * Processes an character within the enum
	 * 
	 * @param data
	 *            the search data
	 * @throws SourceParserException
	 *             if processing fails
	 */
	private void processEnumChar(EnumSearchData data)
			throws SourceParserException {
		if (data.nameStartIndex == -1) {
			data.nameStartIndex = data.itr.currentIndex();
		} else if (data.separatorIndex == -1) {
			if (data.itr.hasSkipped() || data.value == ',' || data.value == '=') {
				endName(data);
			}
		} else if (data.valueStartIndex == -1) {
			data.valueStartIndex = data.itr.currentIndex();
		} else {
			if (data.value == ',') {
				endValue(data);
			}
		}
	}

	/**
	 * Ends the name of a enum entry
	 * 
	 * @param data
	 *            the search data
	 * @throws SourceParserException
	 *             if processing fails
	 */
	private void endName(EnumSearchData data) throws SourceParserException {
		data.currentEntry = new StringValuePair();
		data.currentEntry.string = parser.getSource().get(data.nameStartIndex,
				data.itr.previousIndex() - data.nameStartIndex + 1);
		if (data.value == ',' || data.value == '}') {
			data.nameStartIndex = -1;
			data.currentEntry.value = ++data.previousValue;
			data.list.add(data.currentEntry);
			data.currentEntry = null;
		} else if (data.value == '=') {
			data.separatorIndex = data.itr.currentIndex();
		} else {
			// Invalid entry
			data.finished = true;
		}
	}

	/**
	 * Ends the value of a enum entry
	 * 
	 * @param data
	 *            the search data
	 * @throws SourceParserException
	 *             if processing fails
	 */
	private void endValue(EnumSearchData data) throws SourceParserException {
		String val = parser.getSource().get(data.valueStartIndex,
				data.itr.previousIndex() - data.valueStartIndex + 1);
		data.currentEntry.value = SourceUtils.parseNumberFromSource(val);
		data.nameStartIndex = -1;
		data.valueStartIndex = -1;
		data.separatorIndex = -1;
		data.previousValue = data.currentEntry.value;
		data.list.add(data.currentEntry);
		data.currentEntry = null;
	}

}
