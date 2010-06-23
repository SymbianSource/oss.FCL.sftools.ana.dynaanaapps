/*
 * Copyright (c) 2010 Nokia Corporation and/or its subsidiary(-ies). 
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

package com.nokia.carbide.cpp.pi.graphicsmemory;

import com.nokia.carbide.cpp.internal.pi.model.GenericSample;

public class GraphicsMemorySample extends GenericSample {
	private static final long serialVersionUID = -854731258436977706L;
	public GraphicsMemoryProcess process;
	public int privateSize;
	public int sharedSize;
	public int sampleNum; // not used
	public int type; // not used

	/**
	 * Process with time stamp
	 * 
	 * @param process
	 * @param sampleSynchTime
	 */
	public GraphicsMemorySample(GraphicsMemoryProcess process,
			int sampleSynchTime) {
		this.process = process;
		this.sampleSynchTime = sampleSynchTime;
	}

	/**
	 * System memory usage with time stamp
	 * 
	 * @param usedMemory
	 * @param totalMemory
	 * @param sampleNum
	 */
	public GraphicsMemorySample(int usedMemory, int totalMemory,
			int sampleSynchTime) {
		this.privateSize = usedMemory;
		this.sharedSize = totalMemory;
		this.sampleSynchTime = sampleSynchTime;
		this.process = new GraphicsMemoryProcess(
				GraphicsMemoryTraceParser.SAMPLE_TOTAL_MEMORY_PROCESS_ID,
				GraphicsMemoryTraceParser.SAMPLE_TOTAL_MEMORY_PROCESS_NAME);
	}
}
