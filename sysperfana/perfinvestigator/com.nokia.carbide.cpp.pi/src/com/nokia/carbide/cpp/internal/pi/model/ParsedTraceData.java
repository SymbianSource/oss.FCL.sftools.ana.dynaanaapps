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

package com.nokia.carbide.cpp.internal.pi.model;



public class ParsedTraceData 
{
	public GenericTrace traceData;
	public TraceDataContainer staticData; // TODO this was added when implementing IRQ
	public FunctionResolver[] functionResolvers;
	
	public ParsedTraceData(){
		
	}
}
