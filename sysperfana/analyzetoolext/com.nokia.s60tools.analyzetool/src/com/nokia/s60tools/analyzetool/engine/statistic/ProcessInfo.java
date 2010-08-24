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
 * Description:  Definitions for the class ProcessInfo
 *
 */

package com.nokia.s60tools.analyzetool.engine.statistic;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IStatus;

import com.nokia.s60tools.analyzetool.Activator;

/**
 * Contains information of process
 * 
 * @author kihe
 * 
 */
public class ProcessInfo {

	/** List of allocations and frees sorted as they arrive in time */
	private AbstractList<BaseInfo> allocsFrees;

	/** List of DLL loads */
	private List<DllLoad> dllLoads;

	/** Process id */
	private int processID;

	/** Process Name */
	private String processName;

	/** Log time */
	private long logTime;

	/** Process start time */
	private Long startTime;

	/** Process end time */
	private Long endTime;

	/** Trace data format version number */
	private int traceDataVersion = 0;

	/** Active allocations account */
	private int allocCount = 0;

	/** Total memory consumed by this process at each event */
	private int totalMemory = 0;

	/** Highest memory consumed by this process */
	private int highestMemory = 0;

	/** Map of potential leaks; to track allocations not yet freed */
	private Map<Long, List<AllocInfo>> potentialLeaksMap;

	/**
	 * Constructor
	 */
	public ProcessInfo() {
		dllLoads = new ArrayList<DllLoad>();
		allocsFrees = new ArrayList<BaseInfo>();
		allocCount = 0;
		potentialLeaksMap = new HashMap<Long, List<AllocInfo>>();
	}

	/**
	 * Adds one memory allocation to the list.
	 * 
	 * @param oneInfo
	 *            One memory allocation
	 */
	public void addOneAlloc(AllocInfo oneInfo) {
		allocCount++;
		totalMemory = totalMemory + oneInfo.getSizeInt();
		oneInfo.setTotalMem(totalMemory);
		if (totalMemory > highestMemory) {
			highestMemory = totalMemory;
		}
		allocsFrees.add(oneInfo);
		// add this alloc to the potential leaks map
		Long addr = oneInfo.getMemoryAddress();
		List<AllocInfo> allocsSameAddr = potentialLeaksMap.get(addr);
		if (allocsSameAddr == null) {
			allocsSameAddr = new ArrayList<AllocInfo>();
			potentialLeaksMap.put(addr, allocsSameAddr);
		}
		allocsSameAddr.add(oneInfo);
	}

	/**
	 * Adds one DLL load to the list.
	 * 
	 * @param dllLoad
	 *            One DLL load
	 */
	public void addOneDllLoad(DllLoad dllLoad) {
		// make sure there is no DLL with the same details already loaded
		for (DllLoad dll : dllLoads) {
			if (dll.getName().equalsIgnoreCase(dllLoad.getName())
					&& dll.getProcessID() == dllLoad.getProcessID()
					&& dll.getUnloadTime() > dllLoad.getLoadTime()) {
				return;
			}
		}

		dllLoads.add(dllLoad);
	}

	/**
	 * Updates memory leaks list by given free information
	 * 
	 * @param info
	 *            Which memory allocation to be deallocated.
	 */
	public void free(FreeInfo info) {

		int freeSize = 0;

		// remove allocs with the same address from the potential leaks map
		Long freeAddr = info.getMemoryAddress();
		List<AllocInfo> allocsSameAddr = potentialLeaksMap.remove(freeAddr);
		if (allocsSameAddr != null && allocsSameAddr.size() > 0) {
			for (AllocInfo allocInfo : allocsSameAddr) {
				allocInfo.setFreedBy(info);
				allocCount--;
				int thisFreedSize = allocInfo.getSizeInt();
				freeSize = freeSize + thisFreedSize;
				totalMemory = totalMemory - thisFreedSize;
			}
			info.setFreedAllocs(new HashSet<AllocInfo>(allocsSameAddr));
		}

		info.setSizeInt(freeSize);
		info.setTotalMem(totalMemory);

		if (info.getTime() == 0) {
			// support old format
			// set time as last operation time or start time.
			Long time = getPreviousTime();
			if (time == null || time == -1L) {
				Activator
						.getDefault()
						.log(
								IStatus.WARNING,
								String
										.format(
												"AnalyzeTool encountered a process = %s, which starts with FREE.",
												processID), null);
				time = startTime == null ? 0 : startTime;
			}
			info.setTime(time);
		}
		allocsFrees.add(info);
	}

	/**
	 * Returns the time stamp of the last memory operation
	 * 
	 * @return the time stamp of the last memory operation, or -1 if there are
	 *         no memory operations
	 */
	private Long getPreviousTime() {
		Long time = -1L;
		if (!allocsFrees.isEmpty()) {
			time = (allocsFrees.get(allocsFrees.size() - 1)).getTime();
		}
		return time;
	}

	/**
	 * Returns list of allocations
	 * 
	 * @return List of allocations
	 */
	public AbstractList<AllocInfo> getAllocs() {
		AbstractList<AllocInfo> allocs = new ArrayList<AllocInfo>();
		for (BaseInfo alloc : allocsFrees) {
			if (alloc instanceof AllocInfo) {
				allocs.add((AllocInfo) alloc);
			}
		}
		return allocs;
	}

	/**
	 * Returns list of DLL loads
	 * 
	 * @return List of DLL loads
	 */
	public List<DllLoad> getDllLoads() {
		return dllLoads;
	}

	/**
	 * Returns list of memory leak from this process.
	 * 
	 * @return List of memory leaks.
	 */
	public AbstractList<AllocInfo> getMemLeaks() {
		AbstractList<AllocInfo> leaks = new ArrayList<AllocInfo>();
		for (List<AllocInfo> potLeaks : potentialLeaksMap.values()) {
			leaks.addAll(potLeaks);
		}
		return leaks;
	}

	/**
	 * Returns the number of memory leaks of this process.
	 * 
	 * @return number of memory leaks.
	 */
	public int getMemLeaksNumber() {
		int count = 0;
		for (List<AllocInfo> potLeaks : potentialLeaksMap.values()) {
			count += potLeaks.size();
		}
		return count;
	}

	/**
	 * get list of frees
	 * 
	 * @return List of FreeInfo
	 */
	public AbstractList<FreeInfo> getFrees() {
		AbstractList<FreeInfo> frees = new ArrayList<FreeInfo>();
		for (BaseInfo alloc : allocsFrees) {
			if (alloc instanceof FreeInfo) {
				frees.add((FreeInfo) alloc);
			}
		}
		return frees;
	}

	/**
	 * Returns process id
	 * 
	 * @return Process id
	 */
	public int getProcessID() {
		return processID;
	}

	/**
	 * Returns process start time
	 * 
	 * @return Process start time
	 */
	public Long getStartTime() {
		return startTime;
	}

	/**
	 * Returns trace data version number
	 * 
	 * @return Trace data version number
	 */
	public int getTraceDataVersion() {
		return traceDataVersion;
	}

	/**
	 * Sets process id
	 * 
	 * @param newProcessID
	 *            Process id
	 */
	public void setProcessID(int newProcessID) {
		this.processID = newProcessID;
	}

	/**
	 * Sets process start time
	 * 
	 * @param newTime
	 *            process start time
	 */
	public void setStartTime(String newTime) {
		long lValue = Long.parseLong(newTime, 16);
		this.startTime = lValue;
	}

	/**
	 * Sets log time
	 * 
	 * @param logTime
	 *            log time of one trace message in microseconds
	 */
	public void setLogTime(long logTime) {
		this.logTime = logTime;
	}

	/**
	 * Gets log time
	 * 
	 * @return log time of one trace message in microseconds
	 */
	public long getLogTime() {
		return logTime;
	}

	/**
	 * Updates trace version number
	 * 
	 * @param newTraceDataVersion
	 *            New trace data version number
	 */
	public void setTraceDataVersion(String newTraceDataVersion) {
		try {
			traceDataVersion = Integer.parseInt(newTraceDataVersion, 16);
		} catch (NumberFormatException nfe) {
			traceDataVersion = 0;
		}
	}

	/**
	 * Marks given dll as unloaded
	 * 
	 * @param dllName
	 *            Dll name
	 * @param startAddr
	 *            memory start address for DLL
	 * @param endAddr
	 *            memory end address for DLL
	 * @param dllUnloadTime
	 *            time when DLL is unloaded
	 * @return the dll marked as unloaded, or null if no match found
	 */
	public DllLoad unloadOneDll(String dllName, long startAddr, long endAddr,
			long dllUnloadTime) {
		DllLoad ret = null;
		for (DllLoad dll : dllLoads) {
			if (dll.getName().equals(dllName)
					&& dll.getStartAddress().longValue() == startAddr
					&& dll.getEndAddress().longValue() == endAddr) {
				dll.setUnloadTime(dllUnloadTime);
				ret = dll;
			}
		}
		return ret;
	}

	/**
	 * set end time of the process
	 * 
	 * @param endTime
	 */
	public void setEndTime(long endTime) {
		this.endTime = endTime;
	}

	// /**
	// * set end time of the process
	// *
	// * @param aTime
	// */
	// public void setEndTime(String aTime) {
	// Long lValue = Long.parseLong(aTime, 16);
	// endTime = lValue;
	// }

	/**
	 * set process name
	 * 
	 * @param aProcessName
	 */
	public void setProcessName(String aProcessName) {
		processName = aProcessName;
	}

	/**
	 * @return the process name
	 */
	public String getProcessName() {
		return processName;
	}

	/**
	 * get all events
	 * 
	 * @return list of BaseInfo
	 */
	public AbstractList<BaseInfo> getAllocsFrees() {
		return allocsFrees;
	}

	/**
	 * Get end time of the process
	 * 
	 * @return End time
	 */
	public Long getEndTime() {
		return endTime;
	}

	/**
	 * get list of allocations freed by a FreeInfo
	 * 
	 * @param free
	 * @return AbstractList<AllocInfo> list of allocations freed by the provided
	 *         FreeInfo
	 */
	public AbstractList<AllocInfo> getAllocsFreedBy(FreeInfo free) {
		AbstractList<AllocInfo> allocs = new ArrayList<AllocInfo>();
		for (BaseInfo alloc : allocsFrees) {
			if (alloc instanceof AllocInfo
					&& ((AllocInfo) alloc).getFreedBy() == free) {
				allocs.add((AllocInfo) alloc);
			}
		}
		return allocs;
	}

	/**
	 * Getter for highest memory allocation for the current process
	 * 
	 * @return int containing highest memory usage
	 */
	public int getHighestCumulatedMemoryAlloc() {
		return highestMemory;
	}
}
