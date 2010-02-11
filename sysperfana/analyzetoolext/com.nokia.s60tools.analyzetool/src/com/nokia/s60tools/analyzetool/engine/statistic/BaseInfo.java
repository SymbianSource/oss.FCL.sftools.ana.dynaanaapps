/*
 * Copyright (c) 2008-2009 Nokia Corporation and/or its subsidiary(-ies).
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
import java.util.Hashtable;
import java.util.Iterator;

/**
 * Base class for {@link AllocInfo} and {@link FreeInfo}
 *
 * @author kihe
 *
 */
public class BaseInfo {

	/**
	 * Cache for callstack items. Used when allocation fragment is parsed wrong
	 * order.
	 */
	private final Hashtable<Integer, AbstractList<AllocCallstack>> callstackCache;

	/** Memory address of this memory allocation. */
	private Long memoryAddress;

	/** Memory allocation process id. */
	private int processID;

	/** Allocation time */
	private Long time;
	
	/** Total memory size at this time */
	private int totalMemory = 0;
	private int size = 0;

	/**
	 * Constructor
	 */
	public BaseInfo() {
		callstackCache = new Hashtable<Integer, AbstractList<AllocCallstack>>();
	}

	/**
	 * Sets memory allocation callstack
	 *
	 * @param callstack
	 *            Memory allocation callstack
	 */
	public void addCallstack(AbstractList<AllocCallstack> callstack) {
		callstackCache.put(0, callstack);
	}

	/**
	 * Returns memory allocation callstack.
	 *
	 * @return Callstack of memory allocation
	 */
	public AbstractList<AllocCallstack> getCallstack() {
		AbstractList<AllocCallstack> wholeCallstack = new ArrayList<AllocCallstack>();
		int callstacksize = callstackCache.size();
		for (int i = 0; i < callstacksize; i++) {
			AbstractList<AllocCallstack> tempCallstack = callstackCache
					.get(i);
			if (tempCallstack == null || tempCallstack.isEmpty())
				continue;
			Iterator<AllocCallstack> iterCall = tempCallstack.iterator();
			while (iterCall.hasNext()) {
				wholeCallstack.add(iterCall.next());
			}
		}
		return wholeCallstack;
	}

	/**
	 * Returns memory address
	 *
	 * @return Memory address
	 */
	public Long getMemoryAddress() {
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
	public Long getTime() {
		return time == null ? 0 : time;
	}


	/**
	 * Sets memory address
	 *
	 * @param newMemoryAddress
	 *            Memory address
	 */
	public void setMemoryAddress(String newMemoryAddress) {
		Long lValue = Long.parseLong(newMemoryAddress, 16);
		this.memoryAddress = lValue;
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

	/**
	 * Sets time for the allocation
	 *
	 * @param newTime
	 *            Allocation time
	 */
	public void setTime(String newTime) {
		Long lValue = Long.parseLong(newTime, 16);
		this.time = lValue;
	}
	
	public void setTime(Long newTime)
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
		callstackCache.put(Integer.parseInt(packetNumber, 16), callstack);
	}
	
	/**
	 * set total memory used at this time.
	 * @param size 
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
	
	public void setSizeInt(int aSize) {
		size = aSize;
	}
	
	public int getSizeInt() {
		return size;
	}
}
