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

import com.nokia.carbide.cpp.internal.pi.model.GenericThread;

public class MemThread extends GenericThread
{
	private static final long serialVersionUID = 2633643437981913631L;
	
	// name to be shown for the thread
	transient public String fullName;
	
	// maximum memory usage of thread/process within the current time interval
	transient public MaxMemoryItem maxMemoryItem;
	
	// remember whether the thread/process is enabled for the given graph
	transient public boolean[] enabled;

	public MemThread(Integer threadId, String threadName, String processName)
	{
		this.threadId = threadId;
		this.threadName = threadName;
		this.processName = processName;
	}
	
	// get whether the thread/process is enabled for the given graph
    public boolean isEnabled(int graphIndex)
    {
    	return this.enabled[graphIndex];
    }
	
    // set whether the thread/process is enabled for the given graph
    public void setEnabled(int graphIndex, boolean enabled)
    {
    	this.enabled[graphIndex] = enabled;
    }
}
