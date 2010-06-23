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
* Location rule for a function return trace
*
*/
package com.nokia.tracebuilder.engine.rules;

import com.nokia.tracebuilder.engine.source.SourceLocationRule;
import com.nokia.tracebuilder.source.SourceReturn;

/**
 * Location rule for a function return trace
 * 
 */
public final class FunctionReturnLocationRule extends RuleBase implements
		SourceLocationRule {

	/**
	 * Properties of the return statement to be traced
	 */
	private SourceReturn returnProperties;

	/**
	 * Constructor
	 * 
	 * @param returnProperties
	 *            the properties of the return statement
	 */
	public FunctionReturnLocationRule(SourceReturn returnProperties) {
		this.returnProperties = returnProperties;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.engine.source.SourceLocationRule#getLocationOffset()
	 */
	public int getLocationOffset() {
		return returnProperties.getOffset();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.engine.source.SourceLocationRule#getLocationType()
	 */
	public int getLocationType() {
		// This inserts into absolute location
		return ABSOLUTE;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.engine.source.SourceLocationRule#isRemovedAfterInsert()
	 */
	public boolean isRemovedAfterInsert() {
		return true;
	}

}
