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

public class DiskOverview { 

	private long freeSize;
	private long size;
	private int status;
	
	public DiskOverview(DiskOverview target)
	{
		this.freeSize = target.freeSize;
		this.size = target.size;
		this.status = target.status;
	}
	
	public DiskOverview()
	{
		this.freeSize = -1;
		this.size = -1;
	}
	
	public long getFreeSize()
	{
		return freeSize;
	}
	
	public long getSize()
	{
		return size;
	}
	
	public long getUsedSize()
	{
		if(freeSize == -1 || size == -1)
			return -1;
		else
			return size - freeSize;
	}
	
	public void setFreeSize(long freeSize)
	{
		this.freeSize = freeSize;
	}
	
	public void setSize(long size)
	{
		this.size = size;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}
	
}
