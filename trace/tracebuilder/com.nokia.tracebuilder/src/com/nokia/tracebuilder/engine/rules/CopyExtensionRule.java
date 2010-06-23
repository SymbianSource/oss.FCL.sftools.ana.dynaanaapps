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
* Template for function exit trace
*
*/
package com.nokia.tracebuilder.engine.rules;

import com.nokia.tracebuilder.model.TraceObjectRule;

/**
 * Used be trace multipliers to copy extensions into new traces
 * 
 */
interface CopyExtensionRule extends TraceObjectRule {

	/**
	 * Creates a copy of this rule
	 * 
	 * @return the new object
	 */
	public TraceObjectRule createCopy();

}
