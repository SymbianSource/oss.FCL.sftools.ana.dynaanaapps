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
* Handler for global focus command
*
*/
package com.nokia.tracebuilder.action;

import com.nokia.tracebuilder.engine.TraceBuilderGlobals;
import com.nokia.tracebuilder.model.TraceBuilderException;

/**
 * Handler for global delete command
 * 
 */
public class DeleteGlobalAction extends TraceBuilderGlobalAction {

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.action.TraceBuilderGlobalAction#doRun()
	 */
	@Override
	protected void doRun() throws TraceBuilderException {
		TraceBuilderGlobals.getTraceBuilder().delete();
	}

}
