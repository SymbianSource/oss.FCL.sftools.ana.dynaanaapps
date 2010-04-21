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
 * Stores STAK data.
 *
 */
public class StackData {
	private String threadName;
	private String chunkName;
	private long size;
	private int status;
	
	
	public String getChunkName() {
		return chunkName;
	}
	public void setChunkName(String chunkName) {
		this.chunkName = chunkName;
	}
	public long getSize() {
		return size;
	}
	public void setSize(String size) {
		this.size = Long.parseLong(size);
	}
	public String getThreadName() {
		return threadName;
	}
	public void setThreadName(String threadName) {
		this.threadName = threadName;
	}
	public int getStatus() {
		return status;
	}
	
	/**
	 * @param status
	 * This method sets the status of this stack to New, Alive or Deleted
	 */
	public void setStatus(String status)
	{
		if(status.equals("[N]+[A]"))
			this.status = CycleData.New;
		else if (status.equals("[A]"))
			this.status = CycleData.Alive;
		else
			this.status = CycleData.Deleted;
	}
}
