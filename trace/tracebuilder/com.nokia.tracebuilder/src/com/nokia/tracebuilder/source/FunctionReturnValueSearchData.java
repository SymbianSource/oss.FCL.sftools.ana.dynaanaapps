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
* Parser for function return values
*
*/
package com.nokia.tracebuilder.source;

/**
 * Parse data for function return value parser
 * 
 */
final class FunctionReturnValueSearchData {

	/**
	 * Data index
	 */
	int index;

	/**
	 * Iterator
	 */
	SourceIterator itr;

	/**
	 * Return statement start offset
	 */
	int startOffset;

	/**
	 * Return statement end offset
	 */
	int endOffset;

}