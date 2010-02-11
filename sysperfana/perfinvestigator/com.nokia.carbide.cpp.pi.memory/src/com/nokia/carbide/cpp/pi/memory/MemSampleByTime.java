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

import java.util.ArrayList;


public class MemSampleByTime {
	private long time;
	private ArrayList<MemSample> samples;
	private long totalMemory;
	private long usedMemory;
	
	public MemSampleByTime(long time, long totalMemory, long usedMemory)
	{
		this.time = time;
		this.totalMemory = totalMemory;
		this.usedMemory  = usedMemory;
		this.samples = new ArrayList<MemSample>();
	}

	public ArrayList<MemSample> getSamples()
	{
		return this.samples;
	}

	public long getTime()
	{
		return this.time;
	}

	public long getTotalMemory()
	{
		return this.totalMemory;
	}

	public long getUsedMemory()
	{
		return this.usedMemory;
	}
}
