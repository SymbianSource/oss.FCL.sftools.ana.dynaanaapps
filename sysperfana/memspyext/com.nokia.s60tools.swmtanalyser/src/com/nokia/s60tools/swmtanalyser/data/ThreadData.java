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
 * This class is used to store thread information at one cycle
 */
public class ThreadData {
	private String name;						//name of the thread
	private int cycleNo;						//number of the swmt cycle
	private long heapChunkSize = -1;			//size of the heap chunk
	private long maxHeapSize = -1;				//maximum size of the heap
	private long heapAllocatedSpace = -1;		//allocated heap space	
	private long heapFreeSpace = -1;			//free heap space
	private long freeSlackSize = -1;			//free slack space
	private long allocatedCells = -1;			//number of allocated cells in the heap
	private long freeCells = -1;				//number of free cells in the heap.
	private long largestAllocCellSize = -1;		//size of the largest allocated cell
	private long largestFreeCellSize = -1;		//size of the largest free cell
	private long openFiles = 0;				//number of open files	
	private long stackSize = -1;				//size of stack
	private long psHandles = 0;				//number of p&s handles
	private boolean isKernelHandleDeleted;
	private int heapStatus;
	private int stackStatus;

	/**
	 * Default constructor
	 * @param name name of the thread.
	 */
	public ThreadData(String name){
		this.name = name;
	}
	
	/**
	 * Copy constructor
	 * @param target thread data to copy initial values from
	 */
	public ThreadData(ThreadData target)
	{
		this.name = target.name;
		this.heapChunkSize = target.heapChunkSize;
		this.maxHeapSize = target.maxHeapSize;
		this.heapAllocatedSpace = target.heapAllocatedSpace;
		this.heapFreeSpace = target.heapFreeSpace;
		this.freeSlackSize = target.freeSlackSize;
		this.allocatedCells = target.allocatedCells;
		this.freeCells = target.freeCells;
		this.largestAllocCellSize = target.largestAllocCellSize;
		this.largestFreeCellSize = target.largestFreeCellSize;
		this.openFiles = target.openFiles;
		this.stackSize = target.stackSize;
		this.psHandles = target.psHandles;
		this.stackStatus = target.stackStatus;
		this.heapStatus = target.heapStatus;
	}
	
	public long getAllocatedCells() {
		return allocatedCells;
	}
	public void setAllocatedCells(long allocatedCells) {
		this.allocatedCells = allocatedCells;
	}
	public long getFreeCells() {
		return freeCells;
	}
	public void setFreeCells(long freeCells) {
		this.freeCells = freeCells;
	}
	public long getFreeSlackSize() {
		return freeSlackSize;
	}
	public void setFreeSlackSize(long freeSlackSize) {
		this.freeSlackSize = freeSlackSize;
	}
	public long getHeapAllocatedSpace() {
		return heapAllocatedSpace;
	}
	public void setHeapAllocatedSpace(long heapAllocatedSpace) {
		this.heapAllocatedSpace = heapAllocatedSpace;
	}
	public long getHeapChunkSize() {
		return heapChunkSize;
	}
	public void setHeapChunkSize(long heapChunkSize) {
		this.heapChunkSize = heapChunkSize;
	}
	public long getHeapFreeSpace() {
		return heapFreeSpace;
	}
	public void setHeapFreeSpace(long heapFreeSpace) {
		this.heapFreeSpace = heapFreeSpace;
	}
	public long getMaxHeapSize() {
		return maxHeapSize;
	}
	public void setMaxHeapSize(long maxHeapSize) {
		this.maxHeapSize = maxHeapSize;
	}
	public String getName() {
		return name;
	}
	
	public long getOpenFiles() {
		return openFiles;
	}
	public void setOpenFiles(long openFiles) {
		this.openFiles = openFiles;
	}
	
	public long getPsHandles() {
		return psHandles;
	}
	public void setPsHandles(long psHandles) {
		this.psHandles = psHandles;
	}
	public long getStackSize() {
		return stackSize;
	}
	public void setStackSize(long stackSize) {
		this.stackSize = stackSize;
	}
	public long getLargestAllocCellSize() {
		return largestAllocCellSize;
	}
	public void setLargestAllocCellSize(long largestAllocCellSize) {
		this.largestAllocCellSize = largestAllocCellSize;
	}
	public long getLargestFreeCellSize() {
		return largestFreeCellSize;
	}
	public void setLargestFreeCellSize(long largestFreeCellSize) {
		this.largestFreeCellSize = largestFreeCellSize;
	}
	public int getStatus()
	{
		return heapStatus;
	}
	public void setStatus(int status)
	{
		this.heapStatus = status;
	}
	public int getStackStatus() {
		return stackStatus;
	}
	public void setStackStatus(int stackStatus) {
		this.stackStatus = stackStatus;
	}

	public int getCycleNumber() {
		return cycleNo;
	}

	public void setCycleNumber(int cycleNo) {
		this.cycleNo = cycleNo;
	}

	public boolean isKernelHandleDeleted() {
		return isKernelHandleDeleted;
	}

	public void setKernelHandleDeleted(boolean isKernelHandleDeleted) {
		this.isKernelHandleDeleted = isKernelHandleDeleted;
	}
	
}



