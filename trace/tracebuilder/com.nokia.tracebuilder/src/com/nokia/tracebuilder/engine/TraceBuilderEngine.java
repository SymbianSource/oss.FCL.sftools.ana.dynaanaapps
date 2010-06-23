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
* Base class for all engine classes
*
*/
package com.nokia.tracebuilder.engine;

import com.nokia.tracebuilder.model.TraceBuilderException;

/**
 * Base class for all engine classes
 * 
 */
public abstract class TraceBuilderEngine {

	/**
	 * Called after trace project has been opened
	 */
	public abstract void projectOpened();

	/**
	 * Called when trace project is closed
	 */
	public abstract void projectClosed();

	/**
	 * Called when exporting the project
	 * 
	 * @throws TraceBuilderException
	 *             if export fails
	 */
	public abstract void exportProject() throws TraceBuilderException;

}
