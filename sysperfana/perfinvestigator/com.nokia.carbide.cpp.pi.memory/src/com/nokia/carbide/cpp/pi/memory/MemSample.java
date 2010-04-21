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

package com.nokia.carbide.cpp.pi.memory;

import com.nokia.carbide.cpp.internal.pi.model.GenericSample;


public class MemSample extends GenericSample
{
	private static final long serialVersionUID = 5829017896307372969L;
	
	public MemThread thread;
	public int heapSize;
	public int stackSize;
	public int sampleNum;
	public int type;
	
	
	public MemSample(MemThread thread, int heapSize, int stackSize, int sampleTime)
	{
		this.thread = thread;
		this.heapSize = heapSize;
		this.stackSize = stackSize;
		this.sampleNum = sampleTime;
		this.sampleSynchTime = sampleTime;
	}
	
	
	public MemSample(MemThread thread, int heapSize, int stackSize, int sampleNum, int type)
	{
		this.thread = thread;
		this.heapSize = heapSize;
		this.stackSize = stackSize;
		this.sampleNum = sampleNum;
		this.type = type;
		this.sampleSynchTime = sampleNum;
	}
	
	public MemSample(MemThread thread, int libSize, int libCount, int libThread, int sampleNum, int type)
	{
		this.thread = thread;
		this.heapSize = libSize;
		this.stackSize = libThread;
		this.sampleNum = libCount;
		this.type = type;
		this.sampleSynchTime = sampleNum;
	}
}
