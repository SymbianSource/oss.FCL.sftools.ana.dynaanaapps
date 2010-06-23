/*
 * Copyright (c) 2007-2010 Nokia Corporation and/or its subsidiary(-ies). 
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
 * Data Processor interface
 *
 */
package com.nokia.traceviewer.engine.dataprocessor;

import com.nokia.traceviewer.engine.TraceProperties;

/**
 * Data Processor interface
 */
public interface DataProcessor {

	/**
	 * Processes trace that comes through dataprocessor
	 * 
	 * @param properties
	 *            trace properties
	 */
	public void processData(TraceProperties properties);

}
