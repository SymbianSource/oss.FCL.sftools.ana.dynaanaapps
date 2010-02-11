/*
 * Copyright (c) 2009 Nokia Corporation and/or its subsidiary(-ies). 
 * All rights reserved.
 * This component and the accompanying materials are made available
 * under the terms of the License "Eclipse Public License v1.0"
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

package com.nokia.carbide.cpp.internal.pi.plugin.model;

import java.io.File;

import com.nokia.carbide.cpp.internal.pi.model.GenericTrace;
import com.nokia.carbide.cpp.internal.pi.model.ParsedTraceData;


public interface ITrace 
{
	public Class getTraceClass();
	public void initialiseTrace(GenericTrace trace);
	public String getTraceName();
	public int getTraceId();
	public ParsedTraceData parseTraceFile(File file /*, ProgressBar progressBar*/) throws Exception;
}
