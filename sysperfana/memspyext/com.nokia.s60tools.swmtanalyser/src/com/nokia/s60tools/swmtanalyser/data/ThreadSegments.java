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
package com.nokia.s60tools.swmtanalyser.data;

/**
 * Stores start and end cycle numbers of an instance if the thread has multiple instances.
 *
 */
public class ThreadSegments {

	private int startCycle = -1;
	private int endCycle = -1;
	
	public int getStartCycle() {
		return startCycle;
	}
	public void setStartCycle(int startCycle) {
		this.startCycle = startCycle;
	}
	public int getEndCycle() {
		return endCycle;
	}
	public void setEndCycle(int endCycle) {
		this.endCycle = endCycle;
	}
}
