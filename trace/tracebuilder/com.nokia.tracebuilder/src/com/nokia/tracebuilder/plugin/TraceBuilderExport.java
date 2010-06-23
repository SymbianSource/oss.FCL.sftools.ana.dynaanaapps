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
* Export plug-in interface for trace projects
*
*/
package com.nokia.tracebuilder.plugin;

import com.nokia.tracebuilder.model.TraceBuilderException;

/**
 * Export plug-in interface for trace projects.
 * 
 */
public interface TraceBuilderExport extends TraceBuilderPlugin {

	/**
	 * Exports the trace project. This is called when the user selects the
	 * export action from the menu
	 * 
	 * @throws TraceBuilderException
	 *             if export fails
	 */
	public void exportTraceProject() throws TraceBuilderException;
}
