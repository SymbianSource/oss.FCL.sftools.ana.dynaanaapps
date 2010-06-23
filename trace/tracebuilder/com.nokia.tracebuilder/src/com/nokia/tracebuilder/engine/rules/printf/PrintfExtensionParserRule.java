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
* Parser for printf extension macros
*
*/
package com.nokia.tracebuilder.engine.rules.printf;

/**
 * Parser for printf extension macros
 * 
 */
public final class PrintfExtensionParserRule extends PrintfTraceParserRule {

	/**
	 * Constructor
	 * 
	 * @param tag
	 *            the extension tag
	 */
	public PrintfExtensionParserRule(String tag) {
		super(tag);
	}

}
