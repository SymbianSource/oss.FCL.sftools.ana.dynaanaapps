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
* Interface that is added to traces created by the instrumentation tool
*
*/
package com.nokia.tracebuilder.engine.propertydialog;

import com.nokia.tracebuilder.model.TraceObjectRule;

/**
 * Interface that is added to traces created by the instrumentation tool
 * 
 */
public interface InstrumentedTraceRule extends TraceObjectRule {

	/**
	 * Gets the ID of the instrumenter that created this trace
	 * 
	 * @return the instrumenter ID
	 */
	public String getInstrumenterID();

}
