/*
* Copyright (c) 2006 Nokia Corporation and/or its subsidiary(-ies). 
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
* Search data for enum parser
*
*/
package com.nokia.tracebuilder.source;

import java.util.List;

/**
 * Search data for enum parser
 * 
 */
final class EnumSearchData {

	/**
	 * Data iterator
	 */
	SourceIterator itr;

	/**
	 * Number of opening brackets
	 */
	int openBrackets;

	/**
	 * Validity flag
	 */
	boolean isValid;

	/**
	 * Current enum value
	 */
	StringValuePair currentEntry;

	/**
	 * Index to start of enum value name
	 */
	int nameStartIndex = -1;

	/**
	 * Index to the '=' separator
	 */
	int separatorIndex = -1;

	/**
	 * Index to start of enum value
	 */
	int valueStartIndex = -1;

	/**
	 * Previous enum value
	 */
	int previousValue = -1;

	/**
	 * Parser finished flag
	 */
	boolean finished;

	/**
	 * The list where enum values are stored
	 */
	List<StringValuePair> list;

	/**
	 * Latest character returned by the iterator
	 */
	char value;

}
