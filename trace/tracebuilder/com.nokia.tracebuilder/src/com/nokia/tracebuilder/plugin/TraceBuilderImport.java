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
* Provides support to import a project file into TraceBuilder
*
*/
package com.nokia.tracebuilder.plugin;

import com.nokia.tracebuilder.model.TraceBuilderException;
import com.nokia.tracebuilder.model.TraceModel;

/**
 * Provides support to import a project file into TraceBuilder
 * 
 */
public interface TraceBuilderImport extends TraceBuilderPlugin {

	/**
	 * Gets the file extensions for this import plug-in. These are used as file
	 * filter in query dialog.
	 * 
	 * @return the file extensions
	 */
	public String[] getFileExtensions();

	/**
	 * Imports a trace project
	 * 
	 * @param model
	 *            the trace model to be updated
	 * @param file
	 *            the file to be opened
	 * @throws TraceBuilderException
	 *             if opening fails
	 */
	public void importTraceProject(TraceModel model, String file)
			throws TraceBuilderException;

}
