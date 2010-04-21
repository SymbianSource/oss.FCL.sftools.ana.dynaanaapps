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

package com.nokia.carbide.cpp.pi.priority;

import java.util.Hashtable;

import com.nokia.carbide.cpp.internal.pi.model.GenericSampledTrace;
import com.nokia.carbide.cpp.internal.pi.model.GenericThread;
import com.nokia.carbide.cpp.internal.pi.model.TraceWithThreads;
import com.nokia.carbide.cpp.pi.visual.IGenericTraceGraph;


public class PriTrace extends GenericSampledTrace implements TraceWithThreads
{
	private static final long serialVersionUID = 7929002387070527775L;
	
	private Hashtable firstSynchTimes;
	private Hashtable lastSynchTimes;
	private int samplingTime;
	private PriThread[] threads;
	
	public PriTrace()
	{
		firstSynchTimes = new Hashtable();
		lastSynchTimes = new Hashtable();
	}
	
	public void addSample(PriSample sample)
	{
		this.samples.add(sample);
	}
	
	public PriSample getPriSample(int number)
	{
		return (PriSample)this.samples.elementAt(number);
	}
	  
	public IGenericTraceGraph getTraceGraph()
	{
	  	return null;
	}
	  
	public GenericThread[] getThreads()
	{
	  	return threads;
	}
	
	public void setThreads(PriThread[] threads)
	{
		this.threads = threads;
	}
	  
//	public long getLastSynchTimeForThread(GenericThread t)
//	{
//	  	return ((Integer) lastSynchTimes.get(t.threadId)).intValue();
//	}
//	  
//	public long getFirstSynchTimeForThread(GenericThread t)
//	{
//		return ((Integer) firstSynchTimes.get(t.threadId)).intValue();
//	}
	
	public void addFirstSynchTime(Integer id, Integer firstSynchTime)
	{
		firstSynchTimes.put(id, firstSynchTime);
	}
	
	public void addLastSynchTime(Integer id, Integer lastSynchTime)
	{
		lastSynchTimes.put(id, lastSynchTime);
	}
	
	public void setSamplingTime(int time)
	{
		this.samplingTime = time;
	}
	
	public int getSamplingTime()
	{
		return samplingTime;
	}
}
