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
 * Stores the HEAP data from the log files.
 *
 */
public class HeapData {
	private String threadName;
	private String processName;
	private String baseAddr;
	private long size;
	private long maxSize;
	private long allocatedCells;
	private long freeCells;
	private long allocSpace;
	private long freeSpace;
	private long freeSlack;
	private long largestFreeCell;
	private long largestAllocCell;
	private int attrib;

	public void setThreadAndProcessName(String threadName)
	{
		int index = threadName.indexOf("::");
		this.processName = threadName.substring(0, index-1);
		this.threadName = threadName;
	}

	public long getAllocatedCells() {
		return allocatedCells;
	}

	public void setAllocatedCells(String allocatedCells) {
		this.allocatedCells = Long.parseLong(allocatedCells);
	}

	public long getAllocSpace() {
		return allocSpace;
	}

	public void setAllocSpace(String allocSpace) {
		this.allocSpace = Long.parseLong(allocSpace);
	}

	public String getBaseAddr() {
		return baseAddr;
	}

	public void setBaseAddr(String baseAddr) {
		this.baseAddr = baseAddr;
	}

	public long getFreeCells() {
		return freeCells;
	}

	public void setFreeCells(String freeCells) {
		this.freeCells = Long.parseLong(freeCells);
	}

	public long getFreeSlack() {
		return freeSlack;
	}

	public void setFreeSlack(String freeSlack) {
		this.freeSlack = Long.parseLong(freeSlack);
	}

	public long getFreeSpace() {
		return freeSpace;
	}

	public void setFreeSpace(String freeSpace) {
		this.freeSpace = Long.parseLong(freeSpace);
	}

	public long getLargestAllocCell() {
		return largestAllocCell;
	}

	public void setLargestAllocCell(String largestAllocCell) {
		this.largestAllocCell = Long.parseLong(largestAllocCell);
	}

	public long getLargestFreeCell() {
		return largestFreeCell;
	}

	public void setLargestFreeCell(String largestFreeCell) {
		this.largestFreeCell = Long.parseLong(largestFreeCell);
	}

	public String getProcessName() {
		return processName;
	}

	/*public void setProcessName(String processName) {
		this.processName = processName;
	}*/

	public long getSize() {
		return size;
	}

	public void setSize(String size) {
		this.size = Long.parseLong(size);
	}

	public long getMaxSize() {
		return maxSize;
	}

	public void setMaxSize(String maxSize) {
		this.maxSize = Long.parseLong(maxSize);
	}
	public String getThreadName() {
		return threadName;
	}
	/**
	 * @param status
	 * This method sets the status of this heap to New, Alive or Deleted
	 */
	public void setStatus(String status)
	{
		if(status.equals("[N]+[A]"))
			attrib = CycleData.New;
		else if (status.equals("[A]"))
				attrib = CycleData.Alive;
		else
			attrib = CycleData.Deleted;
	}
	public int getStatus()
	{
		return attrib;
	}
	
	public void setAllocatedCells(long allocatedCells) {
		this.allocatedCells = allocatedCells;
	}
	
	public void setAllocSpace(long allocSpace) {
		this.allocSpace = allocSpace;
	}
	public int getAttrib() {
		return attrib;
	}
	public void setAttrib(int attrib) {
		this.attrib = attrib;
	}

	public void setFreeCells(long freeCells) {
		this.freeCells = freeCells;
	}
	
	public void setFreeSlack(long freeSlack) {
		this.freeSlack = freeSlack;
	}
	public void setFreeSpace(long freeSpace) {
		this.freeSpace = freeSpace;
	}
	public void setLargestAllocCell(long largestAllocCell) {
		this.largestAllocCell = largestAllocCell;
	}
	public void setLargestFreeCell(long largestFreeCell) {
		this.largestFreeCell = largestFreeCell;
	}
		
	public void setSize(long size) {
		this.size = size;
	}
		
}
