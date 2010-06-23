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
* Filler parameter rule implementation
*
*/
package com.nokia.tracebuilder.engine.rules;

import com.nokia.tracebuilder.rules.FillerParameterRule;

/**
 * Filler parameter rule implementation. This returns false from isShownInSource
 * 
 */
final class FillerParameterRuleImpl extends TraceParameterFormattingRuleBase
		implements FillerParameterRule {

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.engine.source.TraceParameterFormattingRule#isShownInSource()
	 */
	@Override
	public boolean isShownInSource() {
		return false;
	}
}
