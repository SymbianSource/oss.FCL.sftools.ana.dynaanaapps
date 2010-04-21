/*
* Copyright (c) 2009 Nokia Corporation and/or its subsidiary(-ies). 
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
*/
package com.nokia.s60tools.memspy.export;

/**
 * Data processor interfacing for handling trace output data from device.
 */
public interface ITraceDataProcessor {
	
	/**
	 * Handles a single trace data line.
	 * @param traceDataLine trade data line
	 */
	public void processDataLine(String traceDataLine);
}
