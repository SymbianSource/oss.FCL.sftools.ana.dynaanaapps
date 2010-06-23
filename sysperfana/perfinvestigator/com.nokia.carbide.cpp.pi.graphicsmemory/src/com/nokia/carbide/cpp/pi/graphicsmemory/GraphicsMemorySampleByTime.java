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

import java.util.ArrayList;

public class GraphicsMemorySampleByTime {
	private long time;
	private ArrayList<GraphicsMemorySample> samples;
	private long totalMemory;
	private long usedMemory;

	public GraphicsMemorySampleByTime(long time, long totalMemory,
			long usedMemory) {
		this.time = time;
		this.totalMemory = totalMemory;
		this.usedMemory = usedMemory;
		this.samples = new ArrayList<GraphicsMemorySample>();
	}

	public ArrayList<GraphicsMemorySample> getSamples() {
		return this.samples;
	}

	public long getTime() {
		return this.time;
	}

	public long getTotalMemory() {
		return this.totalMemory;
	}

	public long getUsedMemory() {
		return this.usedMemory;
	}
}
