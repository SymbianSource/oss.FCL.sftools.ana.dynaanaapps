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
* Contains search variables during SourceParser.findExcludedAreas call
*
*/
package com.nokia.tracebuilder.source;

/**
 * Contains search variables during SourceParser.findExcludedAreas call
 * 
 */
final class ExcludedAreaSearchData {

	/**
	 * Data index
	 */
	int index;

	/**
	 * Current character
	 */
	char value;

	/**
	 * Within line comment flag
	 */
	boolean inLineComment;

	/**
	 * Within comment flag
	 */
	boolean inComment;

	/**
	 * Within string flag
	 */
	boolean inString;

	/**
	 * Within character flag
	 */
	boolean inChar;

	/**
	 * Within preprocessor definition flag
	 */
	boolean inPreprocessor;
}