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
 * This class is used to store RAM information of a cycle
 */
public class SystemData {
	//Free memory
	private long freeMemory;
	//Total memory
	private long totalMemory;
	
	public long getFreeMemory() {
		return freeMemory;
	}
	public void setFreeMemory(long freeMemory) {
		this.freeMemory = freeMemory;
	}
	public long getTotalMemory() {
		return totalMemory;
	}
	public void setTotalMemory(long usedMemory) {
		this.totalMemory = usedMemory;
	}
	
	public SystemData()
	{
		this.freeMemory = -1;
		this.totalMemory = -1;
	}
	
	public SystemData(SystemData target)
	{
		this.freeMemory = target.freeMemory;
		this.totalMemory = target.totalMemory;
	}
}
