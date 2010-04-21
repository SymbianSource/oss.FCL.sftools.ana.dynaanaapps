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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class CycleData implements Comparable<CycleData>{

	private String fileName; 
	private int cycleNumber;
	private String time;
	private String romCheckSum;
	private String romVersion;
	private long changeCount;
	private long freeMemory = -1;
	private long totalMemory = -1;
	private long prevFreeMem;
	private long memspyRam;
	
	public static final int Deleted = 0;
	public static final int New = 1;
	public static final int Alive =2;
	
	private ArrayList<PSHandlesData> psHandlesList;
	private ArrayList<KernelHandles> kernelHandlesList;
	private ArrayList<DiskData> disksList;
	private ArrayList<FilesData> filesList;
	private ArrayList<GlobalDataChunks> globalDataChunksList;
	private ArrayList<StackData> stacksList;
	private ArrayList<ChunksData> nonRHeapList;
	private ArrayList<HeapData> heapsList;
	private ArrayList<ChunksData> chunksList;
	private ArrayList<WindowGroups> wndgList;
	
	private ArrayList<String> newlyCreatedDisks;
	private ArrayList<String> updatedDisks;
	private ArrayList<String> deletedDisks;
	
	private ArrayList<String> newlyCreatedThreads;
	private ArrayList<String> updatedThreads;
	private ArrayList<String> deletedThreads;
	
	private ArrayList<String> newlyCreatedHeaps;
	private ArrayList<String> updatedHeaps;
	private ArrayList<String> deletedHeaps;
	
	
	/**
	 * @param diskData will be added to the list of disks, maintained internally 
	 */
	public void addDiskData(DiskData diskData)
	{
		if(disksList == null)
			disksList = new ArrayList<DiskData>();
		
		disksList.add(diskData);
	}
	
	/**
	 * @param kernelData will be added to the list of kernelhandles, maintained internally 
	 */
	public void addKernelData(KernelHandles kernelData)
	{
		if(kernelHandlesList == null)
			kernelHandlesList = new ArrayList<KernelHandles>();
		
		kernelHandlesList.add(kernelData);
	}

	/**
	 * @param fileData will be added to the list of files, maintained internally 
	 */
	public void addFileData(FilesData fileData)
	{
		if(filesList == null)
			filesList = new ArrayList<FilesData>();
		
		filesList.add(fileData);
	}

	/**
	 * @param HPAS Handles Data will be added to the list of ps handles data, maintained internally 
	 */
	public void addHPASHandlesData(PSHandlesData psHandlesData)
	{
		if(psHandlesList == null)
			psHandlesList = new ArrayList<PSHandlesData>();
		
		psHandlesList.add(psHandlesData);
	}
	
	/**
	 * @param heapData will be added to the list of heaps, maintained internally 
	 */
	public void addHeapData(HeapData heapData)
	{
		if(heapsList == null)
			heapsList = new ArrayList<HeapData>();
		
		heapsList.add(heapData);
	}
	
	/**
	 * @param stackData will be added to the list of stacks, maintained internally 
	 */
	public void addStackData(StackData stackData)
	{
		if(stacksList == null)
			stacksList = new ArrayList<StackData>();
		
		stacksList.add(stackData);
	}
	
	/**
	 * @param Chunks Data will be added to the list of chunks data, maintained internally 
	 */
	public void addChunksData(ChunksData chunksData)
	{
		if( chunksList == null)
			chunksList = new ArrayList<ChunksData>();
		
		chunksList.add(chunksData);
	}
	
	/**
	 * @param Chunks Data will be added to the list of chunks data, maintained internally. 
	 */
	public void addGlobalChunksData(GlobalDataChunks globalChunksData)
	{
		if( globalDataChunksList == null)
			globalDataChunksList = new ArrayList<GlobalDataChunks>();
		
		globalDataChunksList.add(globalChunksData);
	}
	
	/**
	 * 
	 * @param Window Groups Data will be added to the list of window groups, maintained internally.
	 */
	public void addWindowGroupsData(WindowGroups wndgData)
	{
		if(wndgList == null)
			wndgList = new ArrayList<WindowGroups>();
		
		wndgList.add(wndgData);
	}
	
	/**
	 * @returns an arraylist of disknames present in this cycle.
	 */
	public ArrayList<String> getDiskNames()
	{
		ArrayList<String> diskNames = new ArrayList<String>();
		
		if(disksList != null)
			for(DiskData d:disksList)
				diskNames.add(d.getName());
		
		return diskNames;
	}

	/**
	 * This method parses the cycle data and stores the disk names to
	 * corresponding lists based on their status.
	 *
	 */
	public void parseDisksList()
	{
		newlyCreatedDisks = new ArrayList<String>();
		updatedDisks = new ArrayList<String>();
		deletedDisks = new ArrayList<String>();
		
		if(disksList != null){
			for(DiskData d:disksList){
				if(d.getStatus() == CycleData.New)
					newlyCreatedDisks.add(d.getName());
				else if(d.getStatus() == CycleData.Alive)
					updatedDisks.add(d.getName());
				else if(d.getStatus() == CycleData.Deleted)
					deletedDisks.add(d.getName());
			}
		}
		
	}
	
	/**
	 * This method parses the handles list of a cycle data and if the handle type
	 * if thread, it stores thread names of those handles to corresponding lists 
	 * based on their status
	 *
	 */
	public void parseAllThreads()
	{
		newlyCreatedThreads = new ArrayList<String>();
		updatedThreads = new ArrayList<String>();
		deletedThreads = new ArrayList<String>();
		
		if(kernelHandlesList != null){
			for(KernelHandles handle:kernelHandlesList){
				if(handle.getHandleType().equals("Thread"))
				{
					if(handle.getStatus() == CycleData.New)
						newlyCreatedThreads.add(handle.getHandleName());
					else if(handle.getStatus() == CycleData.Alive)
						updatedThreads.add(handle.getHandleName());
					else if(handle.getStatus() == CycleData.Deleted)
						deletedThreads.add(handle.getHandleName());
				}
			}
		}
		
	}
	
	/**
	 * 
	 * @param threadName name of the thread whose stack data is needed
	 * @return class which stores stack information
	 */
	public StackData getStackData(String threadName)
	{
		if(stacksList != null)
		{
			for(StackData st:stacksList)
			{
				if(st.getThreadName().equalsIgnoreCase(threadName))
					return st;
			}
		}
		
		return null;
	}
	
	public int getNewOpenFiles(String threadName)
	{
		int openFilesCnt = 0;
	
		if(filesList != null)
		{
			for(FilesData file:filesList)
			{
				if(file.getThreadName().equalsIgnoreCase(threadName)
						&& file.getStatus() == CycleData.New)
					openFilesCnt++;
			}
			
		}
		
		return openFilesCnt;
	}
	
	public int getDeletedFiles(String threadName)
	{
		int closedFilesCnt = 0;
		
		if(filesList != null)
		{
			for(FilesData file:filesList)
			{
				if(file.getThreadName().equalsIgnoreCase(threadName)
						&& file.getStatus() == CycleData.Deleted)
					closedFilesCnt++;
			}
		}
		
		return closedFilesCnt;
	}
	
	public int getUpdatedFiles(String threadName)
	{
		int openFilesCnt = 0;
		
		if(filesList != null)
		{					
			for(FilesData file:filesList)
			{
				if(file.getThreadName().equalsIgnoreCase(threadName)
						&& file.getStatus() == CycleData.Alive)
					openFilesCnt++;
			}
			//DbgUtility.println(DbgUtility.PRIORITY_OPERATION, "Openfiles for " + threadName + " are " + openFilesCnt);  
		}
		
		return openFilesCnt;
	}
	
	public int getNewPSHandles(String threadName)
	{
		int psHandlesCnt = 0;
		
		if(psHandlesList != null)
		{
			for(PSHandlesData psHandle:psHandlesList)
			{
				if(psHandle.getThreadName().equalsIgnoreCase(threadName)
						&& (psHandle.getStatus() == CycleData.New))
					psHandlesCnt++;
			}
		}
	
		return psHandlesCnt;
	}
		
	public int getDeletedPSHandles(String threadName)
	{
		int deletedHandlesCnt = 0;
		
		if(psHandlesList != null)
		{
			for(PSHandlesData psHandle:psHandlesList)
			{
				if(psHandle.getThreadName().equalsIgnoreCase(threadName)
						&& psHandle.getStatus() == CycleData.Deleted)
					deletedHandlesCnt++;
			}
		}
		
		return deletedHandlesCnt;
	}
	
	public int getUpdatedPSHandles(String threadName)
	{
		int alivePSHandles = 0;
		
		if(psHandlesList != null)
		{
			for(PSHandlesData psHandle:psHandlesList)
			{
				if(psHandle.getThreadName().equalsIgnoreCase(threadName)
						&& psHandle.getStatus() == CycleData.Alive)
					alivePSHandles++;
			}
			//DbgUtility.println(DbgUtility.PRIORITY_OPERATION, "Openfiles for " + threadName + " are " + openFilesCnt);  
	
		}
		
		return alivePSHandles;
	}
	
	public ArrayList<String> getNewlyCreatedDisks()
	{
		return newlyCreatedDisks;
	}
	
	public ArrayList<String> getUpdatedDisks()
	{
		return updatedDisks;
	}
	
	public ArrayList<String> getDeletedDisks()
	{
		return deletedDisks;
	}
	
	public ArrayList<String> getNewlyCreatedThreads()
	{
		return newlyCreatedThreads;
	}
	
	public ArrayList<String> getUpdatedThreads()
	{
		return updatedThreads;
	}
	
	public ArrayList<String> getDeletedThreads()
	{
		return deletedThreads;
	}
	
	/**
	 * Reads the values for given disk in this cycledata.
	 * @param diskName name of the disk whose info must be fetched.
	 * @return a structure which can store used memory and size values.
	 */
	public DiskOverview getDiskUsedAndFreeSize(String diskName)
	{
		if(disksList != null)
		{
			DiskOverview overview = new DiskOverview();
			
			int index = -1;
			for(DiskData disk:disksList)
			{
				index++;
				if(disk.getName().equalsIgnoreCase(diskName))
				{
					DiskData diskData = disksList.get(index);
					overview.setSize(diskData.getSize());
					overview.setFreeSize(diskData.getFreeSize());
					overview.setStatus(diskData.getStatus());
					
					return overview;
				}
			}
		}
		return null;
	}
	
	/**
	 * 
	 * @param threadName name of the thread whose heap data is needed.
	 * @return class which stores heap information of given thread in this cycle.
	 */
	public ThreadData getHeapData(String threadName)
	{
		
		if(heapsList != null)
		{
			ThreadData data = new ThreadData(threadName);
			for(HeapData heap:heapsList)
			{
				if(heap.getThreadName().equalsIgnoreCase(threadName))
				{
					data.setHeapChunkSize(heap.getSize());
					data.setMaxHeapSize(heap.getMaxSize());
					data.setAllocatedCells(heap.getAllocatedCells());
					data.setFreeCells(heap.getFreeCells());
					data.setHeapAllocatedSpace(heap.getAllocSpace());
					data.setHeapFreeSpace(heap.getFreeSpace());
					data.setLargestAllocCellSize(heap.getLargestAllocCell());
					data.setLargestFreeCellSize(heap.getLargestFreeCell());
					data.setFreeSlackSize(heap.getFreeSlack());
					data.setStatus(heap.getStatus());
				
					return data;
				}
			}
		}
		
		return null;
	}
	
	public int getKernelHandles()
	{
		if(kernelHandlesList != null)
			return kernelHandlesList.size();
		else
			return 0;
	}
	
	/**
	 * This method parses all heap structures for this cycle and 
	 * stores the thread names of those heaps to corresponding lists 
	 * based on their status
	 *
	 */
	public void parseAllHeaps()
	{
		newlyCreatedHeaps = new ArrayList<String>();
		updatedHeaps = new ArrayList<String>();
		deletedHeaps = new ArrayList<String>();
		
		if(heapsList != null){
			for(HeapData heap:heapsList){
				if(heap.getStatus() == CycleData.New)
					newlyCreatedHeaps.add(heap.getThreadName());
				else if(heap.getStatus() == CycleData.Alive)
					updatedHeaps.add(heap.getThreadName());
				else if(heap.getStatus() == CycleData.Deleted)
					deletedHeaps.add(heap.getThreadName());
			}
		}
		
	}
	
	/**
	 * @return list of thread names whose heap is created in this cycle.
	 */
	public ArrayList<String> getNewHeapThreads()
	{
		return newlyCreatedHeaps;
	}
	
	/**
	 * @return list of thread names whose heap information is 
	 * updated in this cycle.
	 */
	public ArrayList<String> getAliveHeapThreads()
	{
		return updatedHeaps;
	}
	
	/**
	 * @return list of thread names whose heap is deleted in this cycle.
	 * 
	 */
	public ArrayList<String> getDeletedHeapThreads()
	{
		return deletedHeaps;
	}
	
	/**
	 * This method parses all stack structures for this cycle and 
	 * returns the list of thread names for all newly created stacks. 
	 *
	 */
	public ArrayList<String> getNewStackThreads()
	{
		ArrayList<String> threadNames = new ArrayList<String>();
		
		if(stacksList != null)
			for(StackData stack:stacksList)
			{
				if(stack.getStatus() == CycleData.New)
					threadNames.add(stack.getThreadName());
			}
		return threadNames;
	}
	
	/**
	 * This method parses all stack structures for this cycle and 
	 * returns the list of thread names for updated stacks. 
	 *
	 */
	public ArrayList<String> getAliveStackThreads()
	{
		ArrayList<String> threadNames = new ArrayList<String>();
		
		if(stacksList != null)
			for(StackData stack:stacksList)
			{
				if(stack.getStatus() == CycleData.Alive)
					threadNames.add(stack.getThreadName());
			}
		return threadNames;
	}
	
	/**
	 * This method parses all stack structures for this cycle and 
	 * returns the list of thread names for all deleted stacks. 
	 *
	 */
	public ArrayList<String> getDeletedStackThreads()
	{
		ArrayList<String> threadNames = new ArrayList<String>();
		
		if(stacksList != null)
			for(StackData stack:stacksList)
			{
				if(stack.getStatus() == CycleData.Deleted){
					threadNames.add(stack.getThreadName());
				}
			}
		return threadNames;
	}
	
	/**
	 * 
	 * @return list of thread names which contain PSHandles
	 */
	public ArrayList<String> getHPASThreads()
	{
		ArrayList<String> threadNames = new ArrayList<String>();
		
		if(psHandlesList != null)
			for(PSHandlesData hpas:psHandlesList)
				threadNames.add(hpas.getThreadName());
		
		return threadNames;
	}
	
	/**
	 * 
	 * @return list of thread names which contain file handles.
	 */
	public ArrayList<String> getFileThreads()
	{
		ArrayList<String> threadNames = new ArrayList<String>();
		
		if(filesList != null)
			for(FilesData file:filesList)
				threadNames.add(file.getThreadName());
		
		return threadNames;
	}
	
	public ArrayList<String> getDeletedChunkNames ()
	{
		ArrayList<String> deletedChunks = new ArrayList<String>();
	
		if(kernelHandlesList != null){
			for(KernelHandles handle:kernelHandlesList){
		
				if(handle.getHandleType().equals("Chunk") &&
						(handle.getStatus() == CycleData.Deleted))
				{
					deletedChunks.add(handle.getHandleName());
				}
			}
		}
		
		return deletedChunks;
	}
	
	
	
	public long getChangeCount() {
		return changeCount;
	}
	public void setChangeCount(long changeCount) {
		this.changeCount = changeCount;
	}
	public int getCycleNumber() {
		return cycleNumber;
	}
	public void setCycleNumber(int cycleNumber) {
		this.cycleNumber = cycleNumber;
	}
	public String getFileName() {
		return fileName;
	}
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
	
	public long getMemspyRam() {
		return memspyRam;
	}
	public void setMemspyRam(long memspyRam) {
		this.memspyRam = memspyRam;
	}
	public long getPrevFreeMem() {
		return prevFreeMem;
	}
	public void setPrevFreeMem(long prevFreeMem) {
		this.prevFreeMem = prevFreeMem;
	}
	public String getTime() {
		return time;
	}
	public void setTime(String time) {
		this.time = time;
	}

	/**
	 * Clears all the previous data
	 *
	 */
	public void clear()
	{
		if(disksList != null)
			disksList.clear();
		if(heapsList != null)
			heapsList.clear();
		if(stacksList != null)
			stacksList.clear();
		if(globalDataChunksList != null)
			globalDataChunksList.clear();
		if(nonRHeapList != null)
			nonRHeapList.clear();
		if(chunksList != null)
			chunksList.clear();
		if(kernelHandlesList != null)
			kernelHandlesList.clear();
		if(filesList != null)
			filesList.clear();
		if(psHandlesList != null)
			psHandlesList.clear();
	}

	public ArrayList<GlobalDataChunks> getGlobalDataChunksList() {
		ArrayList<GlobalDataChunks> s = new ArrayList<GlobalDataChunks>();		
		if(globalDataChunksList!=null)
			for (GlobalDataChunks data: globalDataChunksList)
				s.add(data);
		return s;
	}
	
	public GlobalDataChunks getGlobalChunkDataFor(String chunkName)
	{
		if(globalDataChunksList != null)
		{
			for(GlobalDataChunks data : globalDataChunksList)
			{
				if(data.getChunkName().equalsIgnoreCase(chunkName))
				{
					return data;
				}
			}
		}
		return null;
	}

	public ChunksData getChunkDataFor(String chunkName)
	{
		if(chunksList != null)
		{
			for(ChunksData data : chunksList)
			{
				if(data.getChunkName().equalsIgnoreCase(chunkName))
				{
					return data;
				}
			}
		}
		return null;
	}

	public ArrayList<String> getGlobalChunkNames()
	{
		ArrayList<String> chunkNames = new ArrayList<String>();
		
		if(globalDataChunksList != null)
			for(GlobalDataChunks glod:globalDataChunksList)
				chunkNames.add(glod.getChunkName());
		
		return chunkNames;
	}
	
	

	public ArrayList<String> getNonHeapChunkNames()
	{
		ArrayList<String> chunkNames = new ArrayList<String>();
		
		if(chunksList != null)
			for(ChunksData chnk:chunksList)
				chunkNames.add(chnk.getChunkName());
		
		return chunkNames;
	}
	
	public ArrayList<PSHandlesData> getPsHandlesList() {
		ArrayList<PSHandlesData> s = new ArrayList<PSHandlesData>();		
		for (PSHandlesData data: psHandlesList)
			s.add(data);
		
		return s;
	}

	public ArrayList<KernelHandles> getGenHandlesList() {
		ArrayList<KernelHandles> s = new ArrayList<KernelHandles>();		
		for (KernelHandles data: kernelHandlesList)
			s.add(data);

		return s;

	}

	public ArrayList<DiskData> getDisksList() {
		ArrayList<DiskData> s = new ArrayList<DiskData>();		
		for (DiskData data: disksList)
			s.add(data);
		return s;
	}

	public ArrayList<HeapData> getHeapsList() {
		ArrayList<HeapData> s = new ArrayList<HeapData>();		
		for (HeapData data: heapsList)
			s.add(data);
		return s;
	}

	public ArrayList<ChunksData> getChunksList() {
		ArrayList<ChunksData> s = new ArrayList<ChunksData>();		
		if(chunksList!=null)
			for (ChunksData data: chunksList)
				s.add(data);
		return s;
	}
	
	/**
	 * 
	 * @returns list of window groups from this cycle.
	 */
	public ArrayList<WindowGroups> getWindowGroupsData()
	{
		ArrayList<WindowGroups> list = new ArrayList<WindowGroups>();
		
		if(wndgList != null)
		{
			for(WindowGroups wndg:wndgList)
			{
				list.add(wndg);
			}
		}
		
		return list;
	}
	public long getFreeMemory() {
		return freeMemory;
	}

	public void setFreeMemory(long freeMemory) {
		this.freeMemory = freeMemory;
	}

	public long getTotalMemory() {
		return totalMemory;
	}

	public void setTotalMemory(long totalMemory) {
		this.totalMemory = totalMemory;
	}

	public String getRomCheckSum() {
		return romCheckSum;
	}

	public void setRomCheckSum(String romCheckSum) {
		this.romCheckSum = romCheckSum;
	}

	public String getRomVersion() {
		return romVersion;
	}

	public void setRomVersion(String romVersion) {
		this.romVersion = romVersion;
	}
	
	/**
	 * 
	 * This method returns the data related to various kernel elements.
	 */
	public KernelElements getAllOpenKernelElements()
	{
		KernelElements elements = new KernelElements();
		
		if(kernelHandlesList != null){
			for(KernelHandles handle:kernelHandlesList)
			{
				if(handle.getHandleType().equals("Timer"))
				{
					int count = elements.getNumberOfTimers();
					
					if(handle.getStatus() == CycleData.New)
						count++;
					else if(handle.getStatus() == CycleData.Deleted)
						count--;
					
					elements.setNumberOfTimers(count);
				}
				else if(handle.getHandleType().equals("Semaphore"))
				{
					int count = elements.getNumberOfSemaphores();
					
					if(handle.getStatus() == CycleData.New)
						count++;
					else if(handle.getStatus() == CycleData.Deleted)
						count--;
					
					elements.setNumberOfSemaphores(count);
				}
				else if(handle.getHandleType().equals("Process"))
				{
					int count = elements.getNumberOfProcesses();
					
					if(handle.getStatus() == CycleData.New)
						count++;
					else if(handle.getStatus() == CycleData.Deleted)
						count--;
					
					elements.setNumberOfProcesses(count);
				}
				else if(handle.getHandleType().equals("Thread"))
				{
					int count = elements.getNumberOfThreads();
					
					if(handle.getStatus() == CycleData.New)
						count++;
					else if(handle.getStatus() == CycleData.Deleted)
						count--;
					
					elements.setNumberOfThreads(count);
				}
				else if(handle.getHandleType().equals("Chunk"))
				{
					int count = elements.getNumberOfChunks();
					
					if(handle.getStatus() == CycleData.New)
						count++;
					else if(handle.getStatus() == CycleData.Deleted)
						count--;
					
					elements.setNumberOfChunks(count);
				}
				else if(handle.getHandleType().equals("Session"))
				{
					int count = elements.getNumberOfSessions();
					
					if(handle.getStatus() == CycleData.New)
						count++;
					else if(handle.getStatus() == CycleData.Deleted)
						count--;
					
					elements.setNumberOfSessions(count);
				}
				else if(handle.getHandleType().equals("Server"))
				{
					int count = elements.getNumberOfServers();
				
					if(handle.getStatus() == CycleData.New)
						count++;
					else if(handle.getStatus() == CycleData.Deleted)
						count--;
					
					elements.setNumberOfServers(count);
				}
				else if(handle.getHandleType().equals("Msg. Queue"))
				{
					int count = elements.getNumberOfMsgQueues();
				
					if(handle.getStatus() == CycleData.New)
						count++;
					else if(handle.getStatus() == CycleData.Deleted)
						count--;
					
					elements.setNumberOfMsgQueues(count);
				}
			}
		}
		return elements;	
	}

	public int compareTo(CycleData tmp) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd HH:mm:ss");
		Date aDate=null;
		Date bDate=null;
		try {
			aDate = sdf.parse(this.getTime());
			bDate = sdf.parse(tmp.getTime());
		} catch (ParseException e) {
			e.printStackTrace();
		}
		
		if(aDate.before(bDate))
		{
			return -1;
		} 
		else if(aDate.after(bDate))
		{
			return 1;
		}
		return 0;
	}
}
