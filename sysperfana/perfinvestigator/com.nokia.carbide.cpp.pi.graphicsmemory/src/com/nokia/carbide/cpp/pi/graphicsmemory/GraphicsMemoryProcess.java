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

import com.nokia.carbide.cpp.internal.pi.model.GenericThread;

public class GraphicsMemoryProcess extends GenericThread {
	private static final long serialVersionUID = -3170239590768930245L;

	// name to be shown for the process
	transient public String fullName;

	// maximum memory usage of process within the current time interval
	transient public MaxGraphicsMemoryItem maxMemoryItem;

	// remember whether the process is enabled for the given graph
	transient public boolean[] enabled;

	public GraphicsMemoryProcess(Integer processId, String processName) {
		this.processId = processId;
		this.processName = processName;
	}

	// get whether the process is enabled for the given graph
	public boolean isEnabled(int graphIndex) {
		return this.enabled[graphIndex];
	}

	// set whether the process is enabled for the given graph
	public void setEnabled(int graphIndex, boolean enabled) {
		this.enabled[graphIndex] = enabled;
	}
}
