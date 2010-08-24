/*
 * Copyright (c) 2008-2010 Nokia Corporation and/or its subsidiary(-ies).
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
 * Description:  Definitions for the class BaseInfo
 *
 */

package com.nokia.s60tools.analyzetool.engine.statistic;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;

/**
 * Base class for {@link AllocInfo} and {@link FreeInfo}
 *
 * @author kihe
 *
 */
public class BaseInfo {
	
	private List<AllocCallstack> callstacks = null;
	
	/**
	 * Cache for callstack items. Used when allocation fragment is parsed wrong
	 * order.
	 */
	private Hashtable<Integer, AbstractList<AllocCallstack>> callstackCache = null;

	/** Memory address of this memory allocation. */
	private long memoryAddress;

	/** Memory allocation process id. */
	private int processID;

	/** Allocation time */
	private long time;
	
	/** Total memory size at this time */
	private int totalMemory;
	private int size;
	
	/** Thread Id. **/
	private int threadId;
	
	/** file position to defer callstack reading */
	private long filePos;

	/**
	 * Constructor
	 * 
	 * @param memoryAddress The address for this memory operation
	 */
	public BaseInfo(String memoryAddress) {
		this.memoryAddress = Long.parseLong(memoryAddress, 16);
	}

	/**
	 * Sets memory allocation callstack. Use this method
	 * only for the first set of callstacks. 
	 *
	 * @param callstack
	 *            Memory allocation callstack
	 */
	public void addCallstack(AbstractList<AllocCallstack> callstack) {
		if (callstack.size() > 0){
			callstacks = new ArrayList<AllocCallstack>(callstack);						
		}
	}

	/**
	 * Returns memory allocation callstack. This method should only be called
	 * after all data has finished loading.
	 * 
	 * @return Callstack of memory allocation
	 */
	public List<AllocCallstack> getCallstack() {
		
		//we assume all data has been loaded at this point
		finaliseCallstack();
		
		return callstacks == null ? Collections.<AllocCallstack>emptyList() : Collections.unmodifiableList(callstacks);
	}

	/**
	 * Returns memory address
	 *
	 * @return Memory address
	 */
	public long getMemoryAddress() {
		return memoryAddress;
	}

	/**
	 * Returns process ID.
	 *
	 * @return Process ID.
	 */
	public int getProcessID() {
		return processID;
	}

	/**
	 * Returns time for the allocation
	 *
	 * @return Allocation time
	 */
	public long getTime() {
		return time;
	}

	/**
	 * Sets process ID.
	 *
	 * @param newProcessID
	 *            Process ID
	 */
	public void setProcessID(String newProcessID) {
		int iValue = Integer.parseInt(newProcessID, 16);
		this.processID = iValue;
	}

//	/**
//	 * Sets time for the allocation
//	 *
//	 * @param newTime
//	 *            Allocation time
//	 */
//	public void setTime(String newTime) {
//		long lValue = Long.parseLong(newTime, 16);
//		this.time = lValue;
//	}
	
	/**
	 * Sets the time stamp of event occurrence
	 * 
	 * @param newTime
	 */
	public void setTime(long newTime)
	{
		this.time = newTime;
	}

	/**
	 * Updates allocation fragment. Means that given callstack is addition to
	 * previous added alloc
	 *
	 * @param callstack
	 *            Addition callstack items
	 * @param packetNumber
	 *            AllocF id value
	 */
	public void updateFragment(AbstractList<AllocCallstack> callstack,
			String packetNumber) {
		int pck = Integer.parseInt(packetNumber, 16);
		if (pck == 1){
			//special case; this can be added to the end of the list straight away
			callstacks.addAll(callstack);
		} else {
			//packages may come out of order; this is managed in the callstackCache
			if (callstackCache == null){
				callstackCache = new Hashtable<Integer, AbstractList<AllocCallstack>>();			
			}
			callstackCache.put(pck, callstack);			
		}
	}
	
	/**
	 * Optimises internal callstack data structures.
	 * Should only be called after all data for this memory operation
	 * has been loaded (i.e. all fragments)
	 */
	public void finaliseCallstack(){
		if (callstacks == null && callstackCache != null){
			throw new IllegalStateException(); //first set of callstacks should always be in callstacks
		}
		
		if (callstackCache != null){
			int size = callstackCache.size();
			int i = 2;
			while(size != 0){
				AbstractList<AllocCallstack> nextCallStacks = callstackCache.get(i);
				if (nextCallStacks != null){
					size --;
					callstacks.addAll(nextCallStacks);
				} //TODO else: missing callstack: shall we report it or log it?
				i++;
			}
			callstackCache = null;
		}
	}
	
	/**
	 * set total memory used at this time.
	 * @param newSize 
	 */
	public void setTotalMem(int newSize) {
		totalMemory = newSize;
	}
	
	/**
	 * get total memory used at this time
	 * @return size
	 */
	public int getTotalMem() {
		return totalMemory;
	}
	
	/**
	 * Sets the number of bytes allocated or freed in this memory operation
	 * @param aSize size in bytes
	 */
	public void setSizeInt(int aSize) {
		size = aSize;
	}
	
	/**
	 * Gets the number of bytes allocated or freed in this memory operation
	 * @return size in bytes
	 */
	public int getSizeInt() {
		return size;
	}
	
	/**
	 * Sets thread id.
	 * 
	 * @param threadId thread id
	 */
	public void setThreadId(String threadId){
		this.threadId = Integer.parseInt(threadId, 16);
	}
	
	/**
	 * Gets thread id.
	 * 
	 * @return thread id
	 */
	public int getThreadId(){
		return threadId;
	}

	/**
	 * Getter for file position pointing to first record in 
	 * .dat file where callstack for this BaseInfo starts.
	 * @return the file position for the start of callstack information,
	 * or -1 if no callstack available for this BaseInfo
	 */
	public long getFilePos() {
		return filePos;
	}

	/**
	 * Setter for file position pointing to callstack information
	 * @param filePos the file position pointing to start of record with callstack information;
	 * or -1 if no callstack available for this BaseInfo
	 */
	public void setFilePos(long filePos) {
		this.filePos = filePos;
	}

	@Override
	public String toString() {
		if (filePos > -1 && (callstacks == null || callstacks.size() == 0)){
			return String.format(
					"BaseInfo [memoryAddress=0x%08X, processID=%s, time=%s, size=%s, totalMemory=%s, callstacks on demand]",
					memoryAddress, processID, time, size, totalMemory);  
							
		} else {
			return String.format(
					"BaseInfo [memoryAddress=0x%08X, processID=%s, time=%s, size=%s, totalMemory=%s, callstacks=%s]",
					memoryAddress, processID, time, size, totalMemory,  
					callstacksToString());
			
		}
	}

	private String callstacksToString() {
		if (callstacks == null){
			return "null";
		}
		
		StringBuilder sb = new StringBuilder();
		boolean addComma = false;
		for (AllocCallstack callstack : callstacks) {
			if (addComma){
				sb.append(", ");
			}
			addComma = true;
			sb.append(String.format("addr=0x%08X dll=%s", callstack.getMemoryAddress(), callstack.getDllLoad() == null ? "null" : callstack.getDllLoad().getName()));
		}
		return sb.toString();
	}
}
