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
package com.nokia.s60tools.swmtanalyser.model;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

import org.eclipse.core.runtime.IProgressMonitor;

import com.nokia.s60tools.swmtanalyser.data.ChunksData;
import com.nokia.s60tools.swmtanalyser.data.CycleData;
import com.nokia.s60tools.swmtanalyser.data.DiskOverview;
import com.nokia.s60tools.swmtanalyser.data.GlobalDataChunks;
import com.nokia.s60tools.swmtanalyser.data.KernelElements;
import com.nokia.s60tools.swmtanalyser.data.OverviewData;
import com.nokia.s60tools.swmtanalyser.data.ParsedData;
import com.nokia.s60tools.swmtanalyser.data.StackData;
import com.nokia.s60tools.swmtanalyser.data.SystemData;
import com.nokia.s60tools.swmtanalyser.data.ThreadData;
import com.nokia.s60tools.swmtanalyser.data.ThreadSegments;
import com.nokia.s60tools.swmtanalyser.data.WindowGroupEventData;
import com.nokia.s60tools.swmtanalyser.data.WindowGroups;
import com.nokia.s60tools.util.debug.DbgUtility;

/**
 * Helper class which has some util methods.
 *
 */
public class SWMTLogReaderUtils {

	private static final String CHECK_SUM = "Checksum";
	private static final String VERSION = "Version";

	/**
	 * Construction
	 */
	public SWMTLogReaderUtils() {
	}

	/**
	 * Parse all the given text log files and create store the basic data in CycleData object for each file.
	 * @param inputFiles
	 * @param cycles
	 * @param monitor
	 * @return <code>null</code> if everything was OK, error message if otherwise.
	 */
	public String getCycleDataArrayFromLogFiles(ArrayList<String> inputFiles, ArrayList<CycleData> cycles, IProgressMonitor monitor)
	{
		int i = 0;
		for(String path: inputFiles)
		{
			i++;
			if(monitor != null)
			{
				if(monitor.isCanceled())
					return null;
				if(i == inputFiles.size()/2)
					monitor.worked(5);
			}
			File aFile = new File(path); 
			ArrayList<String> lines = new ArrayList<String>();
			int lineNum;
			try {
				BufferedReader input =  new BufferedReader(new FileReader(aFile));
				try {
					String line = null;
					lineNum = 0;
					while (( line = input.readLine()) != null){

						//Increment line number 
						lineNum++;

						line = line.trim();
						//Ignoring newlines and lines which are having only spaces
						if(line.length() == 0)
							continue;

						//Line must start with test [MemSpy], if not it is invalid input.
						if(!line.startsWith("[MemSpy]"))
						{
							return "Error in the log ("+path+"): at line " + lineNum + "\nLine does not start with [MemSpy].";
						}

						if(line.contains("Type"))
							break;

						//Add valid line to array
						lines.add(line);	

					}
				}
				finally {
					input.close();
				}
			}
			catch (IOException ex){
				ex.printStackTrace();
				return ex.getMessage();
			}

			String cycleNumLine = null;
			String timeLine = null;

			String romCheckSum = null;
			String romVersion = null;

			for(String str:lines)
			{
				if(str.contains("Cycle number"))
					cycleNumLine = str;
				if(str.contains("Time"))
					timeLine = str;

				if(str.contains("ROM") && str.contains(CHECK_SUM))
					romCheckSum = str;
				else if(str.contains("ROM") && str.contains(VERSION))
					romVersion = str;
			}

			if(cycleNumLine == null || timeLine == null)
				return "Error in the Log " + path + "\n The log might not contain Cycle number or Time information. Please check";

			if(romCheckSum == null || romVersion == null)
				return "Error in the Log " + path + "\n The log might not contain ROM information. Please check";

			//Read cycle number
			String num = cycleNumLine.substring(cycleNumLine.indexOf("Cycle number") + 12).trim();
			int cycleNo = Integer.parseInt(num);

			//Read Time
			String time = timeLine.substring(timeLine.indexOf("Time") + 4).trim();

			String checkSum = romCheckSum.substring(romCheckSum.indexOf(CHECK_SUM) + (CHECK_SUM).length()).trim();
			String romVer = romVersion.substring(romVersion.indexOf(VERSION) + (VERSION).length()).trim();

			CycleData cycleData = new CycleData();
			cycleData.setTime(time);
			cycleData.setCycleNumber(cycleNo);
			cycleData.setFileName(path);
			cycleData.setRomCheckSum(checkSum);
			cycleData.setRomVersion(romVer);

			cycles.add(cycleData);	
		}

		return null;
	}

	/**
	 * Gets the overview information till the selected log file.
	 * @param allCyclesData CycleData list
	 * @param toCycle Cycle number till where you want to export
	 * @return OverviewData
	 */
	public OverviewData getOverviewInformationFromCyclesData(CycleData[] allCyclesData, int toCycle)
	{	
		OverviewData overview = new OverviewData();
		overview.noOfcycles = toCycle;

		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd HH:mm:ss");
		Date aDate=null;
		Date bDate=null;
		try {
			aDate = sdf.parse(allCyclesData[0].getTime());
			bDate = sdf.parse(allCyclesData[toCycle-1].getTime());
		} catch (ParseException e) {
			e.printStackTrace();
		}

		long t1= aDate.getTime();
		long t2= bDate.getTime();
		//time difference in seconds
		overview.duration = (t2-t1)/1000;
		//Get date and time in the format like : Jan 7, 2008 1:56:44 PM
		overview.fromTime = DateFormat.getDateTimeInstance().format(aDate);
		overview.toTime = DateFormat.getDateTimeInstance().format(bDate);
		overview.durationString = millisecondToDHMS(overview.duration*1000);

		DbgUtility.println(DbgUtility.PRIORITY_OPERATION, "Cycle Numbers	:" + overview.noOfcycles );
		DbgUtility.println(DbgUtility.PRIORITY_OPERATION, "Time Period	:" + overview.fromTime + " to " + overview.toTime );
		DbgUtility.println(DbgUtility.PRIORITY_OPERATION, "Time Duration	:" + overview.duration + " sec (" + overview.durationString + ")");

		return overview;
	}

	/**
	 * Returns the difference between two time strings
	 * @param startTime 
	 * @param endTime
	 * @return difference between startTime and endTime in seconds.
	 */
	public long getDurationInSeconds(String startTime, String endTime)
	{
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd HH:mm:ss");

		Date aDate=null;
		Date bDate=null;

		try {
			aDate = sdf.parse(startTime);
			bDate = sdf.parse(endTime);
		} catch (ParseException e) {
			return -1;
		}

		long t1= aDate.getTime();
		long t2= bDate.getTime();
		//time difference in seconds
		return (t2-t1)/1000;

	}
	/**
	 * converts time (in milliseconds) to
	 *  "d, h, min, sec"
	 *  @return time in readable format.
	 */
	public String millisecondToDHMS(long duration) {
		long ONE_SECOND = 1000;
		long ONE_MINUTE = ONE_SECOND * 60;
		long ONE_HOUR   = ONE_MINUTE * 60;
		long ONE_DAY    = ONE_HOUR * 24;

		String res = "";
		long temp = 0;
		if (duration >= ONE_SECOND) {
			temp = duration / ONE_DAY;
			if (temp > 0) {
				res = temp + " d";
				duration -= temp * ONE_DAY;

				if (duration >= ONE_MINUTE) {
					res += ", ";
				}
			}

			temp = duration / ONE_HOUR;
			if (temp > 0) {
				res += temp + " h";
				duration -= temp * ONE_HOUR;
				if (duration >= ONE_MINUTE) {
					res += ", ";
				}
			}

			temp = duration / ONE_MINUTE;
			if (temp > 0) {
				res += temp + " min";
				duration -= temp * ONE_MINUTE;

				if(duration >= ONE_SECOND) {
					res += ", ";
				}
			}

			temp = duration / ONE_SECOND;
			if (temp > 0) {
				res += temp + " sec";
			}
			return res;
		}
		else {
			return "0 sec";
		}
	}

	/**
	 * This will check whether the CycleData objects are in concecutive order and 
	 * if not, this will return the reordered list. 
	 * @param allCyclesData
	 * @return Reordered list, if any cycle data is missing null will be returned 
	 */
	public ArrayList<CycleData> checkCycleOrder(ArrayList<CycleData> allCyclesData)
	{
		ArrayList<CycleData> orderedCycles = new ArrayList<CycleData>();

		/*for(int i=0; i<allCyclesData.size(); i++) {
			for (CycleData cycledata : allCyclesData) {
				if(cycledata.getCycleNumber() == i+1)
				{
					orderedCycles.add(cycledata);
					break;
				}
			}
			if(orderedCycles.size() != i+1)
				return null;
		}*/
		CycleData[] before = allCyclesData.toArray(new CycleData[0]);
		Arrays.sort(before);
		if(before!=null && before.length>0 && before[0].getCycleNumber() == 1)
		{	
			int i= 1;
			for (CycleData data: before) {
				data.setCycleNumber(i);
				orderedCycles.add(data);
				i++;			
			}
			return orderedCycles;
		}
		else
			return null;
	}

	/**
	 * This method fetches list of all newly created disk names from all the 
	 * cycles. If only one cycle is selected it fetches list of new and updated disk names.
	 * @param data specifies list of CycleData to be parsed
	 * @return list of disknames from all the cycles
	 */
	public ArrayList<String> getAllDiskNames(ParsedData data)
	{
		if(data == null || data.getNumberOfCycles() ==0)
			return null;

		CycleData [] parsed_cycles = data.getLogData();

		CycleData firstCycle = parsed_cycles[0];
		firstCycle.parseDisksList();
		ArrayList<String> totalDisks = new ArrayList<String>(firstCycle.getNewlyCreatedDisks());

		if(parsed_cycles.length == 1)
		{
			ArrayList<String> updatedDisks = firstCycle.getUpdatedDisks();

			for(String disk:updatedDisks)
			{
				if(!totalDisks.contains(disk))
					totalDisks.add(disk);
			}

			return totalDisks;
		}
		for(int i=1; i<parsed_cycles.length;i++)
		{
			CycleData cycle = parsed_cycles[i];
			cycle.parseDisksList();
			ArrayList<String> newDisks = cycle.getNewlyCreatedDisks();

			for(String disk:newDisks)
			{
				if(!totalDisks.contains(disk))
					totalDisks.add(disk);
			}
		}

		return totalDisks;
	}	

	/**
	 * This method fetches Used Memory and Size data for a given disk from given list of cycledata.
	 * @param diskName name of the disk, whose used memory and size must be fetched.
	 * @param data list of cycledata which needs to be parsed.
	 * @return list of used memory and size values for all cycles.
	 */
	public ArrayList<DiskOverview> getUsedMemoryAndSizesForDisk(String diskName, ParsedData parsedData)
	{

		ArrayList<DiskOverview> diskTotalData = new ArrayList<DiskOverview>();
		DiskOverview prevData = new DiskOverview();

		CycleData [] cycles = parsedData.getLogData();

		for(CycleData cycle:cycles)
		{
			DiskOverview ov = null;

			if(!cycle.getDeletedDisks().contains(diskName)){
				DiskOverview tmp = cycle.getDiskUsedAndFreeSize(diskName);

				if(tmp != null)
					ov = new DiskOverview(tmp);
				else
				{
					ov = new DiskOverview(prevData);

					if(prevData.getStatus() == CycleData.New)
						ov.setStatus(CycleData.Alive);
				}

			}
			else
				ov = new DiskOverview();

			diskTotalData.add(ov);
			prevData = ov;
		}

		/*for(int i=0;i<diskTotalData.size();i++)
			{
				System.out.print("Variation for DISK " + diskName + " in Cycle " + i);
				DbgUtility.println(DbgUtility.PRIORITY_OPERATION, "USED: " + diskTotalData.get(i).getUsedSize() + " FREE: " + diskTotalData.get(i).getFreeSize());
			}*/

		return diskTotalData;
	}

	/**
	 * This method fetches System Used Memory and Size for each cycle from given list of cycledata.
	 * @param data list of cycledata which needs to be parsed.
	 * @return list of system used memory and size values for all cycles.
	 */
	public ArrayList<SystemData> getSystemDataFromAllCycles(ParsedData parsedData)
	{

		ArrayList<SystemData> systemData = new ArrayList<SystemData>();
		SystemData prevData = new SystemData();

		CycleData [] cycles = parsedData.getLogData();

		for(CycleData cycle:cycles)
		{
			SystemData ov = new SystemData(prevData);

			long freeMem = cycle.getFreeMemory();
			long totalMem = cycle.getTotalMemory();

			if(freeMem != -1)
			{
				ov.setFreeMemory(freeMem);
			}
			if(totalMem != -1)
				ov.setTotalMemory(totalMem);

			systemData.add(ov);
			prevData = ov;
		}

		/*for(int i=0;i<diskTotalData.size();i++)
			{
				System.out.print("Variation for DISK " + diskName + " in Cycle " + i);
				DbgUtility.println(DbgUtility.PRIORITY_OPERATION, "USED: " + diskTotalData.get(i).getUsedSize() + " FREE: " + diskTotalData.get(i).getFreeSize());
			}*/

		return systemData;
	}
	/**
	 * The method creates a union list of newly created threads in each cycle.
	 * @param data list of Cycle data structures to be parsed
	 * @return union list of thread names from all the cycles.
	 */
	public ArrayList<String> getAllThreadNames(ParsedData data)
	{
		if(data == null || data.getNumberOfCycles() == 0)
			return null;

		CycleData[] parsed_cycles = data.getLogData();
		CycleData firstCycle = parsed_cycles[0];
		firstCycle.parseAllThreads();
		ArrayList<String> totalThreads = new ArrayList<String>(firstCycle.getNewlyCreatedThreads());

		if(parsed_cycles.length == 1)
		{
			ArrayList<String> updatedThreads = firstCycle.getUpdatedThreads();

			for(String thread:updatedThreads)
			{
				if(!totalThreads.contains(thread))
					totalThreads.add(thread);
			}

			ArrayList<String> threadsFromAllViews = getThreadsFromAllViews(firstCycle);

			for(String thread:threadsFromAllViews)
			{
				if(!totalThreads.contains(thread))
					totalThreads.add(thread);
			}

			return totalThreads;
		}
		for(int i=1; i<parsed_cycles.length;i++)
		{
			CycleData cycle = parsed_cycles[i];
			cycle.parseAllThreads();
			ArrayList<String> newThreads = cycle.getNewlyCreatedThreads();

			for(String thread:newThreads)
			{
				if(!totalThreads.contains(thread))
					totalThreads.add(thread);
			}
		}

		return totalThreads;
	}	

	/**
	 * This method fetches list of all heap thread names from all the cycles. 
	 * If only one cycle is selected it fetches list of new and updated heap thread names.
	 * @param data specifies list of CycleData to be parsed
	 * @return list of those thread names which contain heap information, in one or many cycles.
	 */
	public ArrayList<String> getAllHeapThreads(ParsedData parsedData)
	{

		CycleData [] logData = parsedData.getLogData();
		CycleData firstCycle = logData[0];

		firstCycle.parseAllHeaps();
		ArrayList<String> totalHeaps = new ArrayList<String>(firstCycle.getNewHeapThreads());

		if(logData.length == 1)
		{
			ArrayList<String> updatedHeaps = firstCycle.getAliveHeapThreads();

			for(String thread:updatedHeaps)
			{
				if(!totalHeaps.contains(thread))
					totalHeaps.add(thread);
			}

			return totalHeaps;
		}
		for(int i=1; i<logData.length;i++)
		{
			CycleData cycle = logData[i];

			cycle.parseAllHeaps();
			ArrayList<String> newHeaps = cycle.getNewHeapThreads();

			for(String thread:newHeaps)
			{
				if(!totalHeaps.contains(thread))
					totalHeaps.add(thread);
			}
		}

		return totalHeaps;
	}

	/**
	 * This method fetches all the data for a given thread from given list of cycledata.
	 * @param threadName name of the thread whose data is needed.
	 * @param cycles list of cycledata structures to be parsed for the thread data.
	 * @return list of ThreadData structures. Each structure holds values of given thread fields in each cycle.
	 */
	public ArrayList<ThreadData> getHeapDataFromAllCycles(String threadName, ParsedData parsedData)
	{
		ArrayList<ThreadData> allData = new ArrayList<ThreadData>();

		ThreadData prevData = new ThreadData(threadName);
		CycleData[] cycles = parsedData.getLogData();

		for(CycleData cycle:cycles)
		{
			ThreadData currentData = null; 

			cycle.parseAllThreads();

			//Fetch the data for this thread in this cycle.
			//If no data in this cycle, copy the data from the previous cycle.	

			ThreadData tmp = cycle.getHeapData(threadName);

			if(tmp != null)
				currentData = tmp;
			else{
				currentData = new ThreadData(prevData);
				if(prevData.getStatus() == CycleData.New)
					currentData.setStatus(CycleData.Alive);
			}

			StackData stakData = cycle.getStackData(threadName);

			if(stakData != null)
			{
				currentData.setStackStatus(stakData.getStatus());
				currentData.setStackSize(stakData.getSize());
			}
			else
			{
				if(prevData.getStackStatus() == CycleData.New)
					currentData.setStackStatus(CycleData.Alive);
				else
					currentData.setStackStatus(prevData.getStackStatus());

				currentData.setStackSize(prevData.getStackSize());
			}

			int openFilesCnt = cycle.getNewOpenFiles(threadName);
			int closedFilesCnt = cycle.getDeletedFiles(threadName);

			int newPSHandlesCnt = cycle.getNewPSHandles(threadName);
			int deletedPSHandlesCnt = cycle.getDeletedPSHandles(threadName);

			if(cycles.length == 1)
			{
				int updatedFiles = cycle.getUpdatedFiles(threadName);
				int updatedPSHandles = cycle.getUpdatedPSHandles(threadName);

				currentData.setOpenFiles(openFilesCnt + updatedFiles);
				currentData.setPsHandles(newPSHandlesCnt + updatedPSHandles);
			}

			else
			{
				openFilesCnt += prevData.getOpenFiles();

				openFilesCnt -= closedFilesCnt;
				currentData.setOpenFiles(openFilesCnt);

				newPSHandlesCnt += prevData.getPsHandles();
				newPSHandlesCnt -= deletedPSHandlesCnt;

				currentData.setPsHandles(newPSHandlesCnt);
			}

			if(cycle.getDeletedThreads().contains(threadName))
				currentData.setKernelHandleDeleted(true);

			currentData.setCycleNumber(cycle.getCycleNumber());	
			allData.add(currentData);
			prevData = currentData;
		}

		return allData;
	}

	/**
	 * Returns the array of global data chunks list of given chunk 
	 * from all the log files data.
	 * @param chunkName
	 * @param cycles
	 * @return List of {@link GlobalDataChunks} with given chunk name
	 */
	public ArrayList<GlobalDataChunks> getGLOBDataFromAllCycles(String chunkName, ParsedData logData)
	{

		ArrayList<GlobalDataChunks> allData = new ArrayList<GlobalDataChunks>();

		GlobalDataChunks prevData = new GlobalDataChunks();

		CycleData [] logCycles = logData.getLogData();

		for(CycleData cycle:logCycles)
		{
			GlobalDataChunks currentData = null;
			GlobalDataChunks tmp = cycle.getGlobalChunkDataFor(chunkName);
			if(tmp != null)
				currentData = tmp;
			else{
				currentData = new GlobalDataChunks(prevData);

				if(prevData.getAttrib() == CycleData.New)
					currentData.setAttrib(CycleData.Alive);
			}

			if(cycle.getDeletedChunkNames().contains(chunkName))
				currentData.setKernelHandleDeleted(true);

			allData.add(currentData);
			prevData = currentData;
		}

		return allData;
	}

	/**
	 * Returns the array of chunks data list of given chunk 
	 * from all the log files data.
	 * @param chunkName
	 * @param cycles
	 * @return list of {@link ChunksData} with given name
	 */
	public ArrayList<ChunksData> getChunkDataFromAllCycles(String chunkName, ParsedData logData)
	{
		ArrayList<ChunksData> allData = new ArrayList<ChunksData>();

		ChunksData prevData = new ChunksData();

		CycleData[] cycles = logData.getLogData();
		for(CycleData cycle:cycles)
		{
			ChunksData currentData = null;
			ChunksData tmp = cycle.getChunkDataFor(chunkName);
			if(tmp != null)
				currentData = tmp;
			else{
				currentData = new ChunksData(prevData);
				if(prevData.getAttrib() == CycleData.New)
					currentData.setAttrib(CycleData.Alive);
			}

			if(cycle.getDeletedChunkNames().contains(chunkName))
				currentData.setKernelHandleDeleted(true);

			allData.add(currentData);
			prevData = currentData;
		}

		return allData;
	}

	/**
	 * This method returns the array of kernel elements data from all the given log files data.
	 * @param parsedData
	 * @return array of kernel elements
	 */
	public ArrayList<KernelElements> getKerenelElemsFromAllCycles(ParsedData parsedData)
	{
		ArrayList<KernelElements> allData = new ArrayList<KernelElements>();
		KernelElements prevData = new KernelElements();

		CycleData [] cycles = parsedData.getLogData();
		for(CycleData cycle:cycles)
		{
			KernelElements currentData = cycle.getAllOpenKernelElements();

			int timers = currentData.getNumberOfTimers();
			int semaphores = currentData.getNumberOfSemaphores();
			int servers = currentData.getNumberOfServers();
			int sessions = currentData.getNumberOfSessions();
			int processes = currentData.getNumberOfProcesses();
			int threads = currentData.getNumberOfThreads();
			int chunks = currentData.getNumberOfChunks();
			int msgQueues = currentData.getNumberOfMsgQueues();


			if(parsedData.getNumberOfCycles() != 1)
			{

				timers += prevData.getNumberOfTimers();
				semaphores += prevData.getNumberOfSemaphores();
				sessions += prevData.getNumberOfSessions();
				servers += prevData.getNumberOfServers();
				processes += prevData.getNumberOfProcesses();
				threads += prevData.getNumberOfThreads();
				chunks += prevData.getNumberOfChunks();
				msgQueues += prevData.getNumberOfMsgQueues();

				//newTimers -= closedTimers;
				currentData.setNumberOfTimers(timers);
				currentData.setNumberOfSemaphores(semaphores);
				currentData.setNumberOfProcesses(processes);
				currentData.setNumberOfSessions(sessions);
				currentData.setNumberOfServers(servers);
				currentData.setNumberOfThreads(threads);
				currentData.setNumberOfChunks(chunks);
				currentData.setNumberOfMsgQueues(msgQueues);

			}
			allData.add(currentData);
			prevData = currentData;

		}
		return allData;
	}
	/**
	 * 
	 * @param threadName name of thread whose status is needed.
	 * @param data list of cycledata to be parsed
	 * @return the recent status of the thread from given cycles. 
	 */
	public int getThreadStatusFromAllCycles(String threadName, ParsedData parsedData)
	{
		int status = -1;

		CycleData [] cycles = parsedData.getLogData();

		for(CycleData cycle:cycles)
		{
			cycle.parseAllThreads();
			if(cycle.getNewlyCreatedThreads().contains(threadName))
				status = CycleData.New;
			else if(cycle.getUpdatedThreads().contains(threadName))
				status = CycleData.Alive;
			else if(cycle.getDeletedThreads().contains(threadName))
				status = CycleData.Deleted;

		}
		return status;
	}

	/**
	 * 
	 * @param threadName name of thread whose heap status is needed.
	 * @param data list of cycledata to be parsed
	 * @return the recent status of the given thread's heap.
	 */
	public int getHeapStatusFromAllCycles(String threadName, ParsedData parsedData)
	{
		int status = 0;
		CycleData [] cycles = parsedData.getLogData();

		for(CycleData cycle:cycles)
		{
			cycle.parseAllHeaps();

			ArrayList<String> newHeapThreads = convertAllStringsToLowerCase(cycle.getNewHeapThreads());
			ArrayList<String> aliveHeaps = convertAllStringsToLowerCase(cycle.getAliveHeapThreads());
			ArrayList<String> deletedHeaps = convertAllStringsToLowerCase(cycle.getDeletedHeapThreads());

			if(newHeapThreads.contains(threadName.toLowerCase()) ||
					aliveHeaps.contains(threadName.toLowerCase()))
				status = 1;
			else if(deletedHeaps.contains(threadName.toLowerCase()))
				status = 0;
		}

		return status;
	}

	/**
	 * Get the stack status of a given thread name
	 * @param threadName name of thread whose heap status is needed.
	 * @param data list of cycledata to be parsed
	 * @return the recent status of the given thread's stack.
	 */
	public int getStackStatusFromAllCycles(String threadName, ParsedData parsedData)
	{
		int status = 0;
		CycleData[] cycles = parsedData.getLogData();

		for(CycleData cycle:cycles)
		{
			ArrayList<String> newStacks = convertAllStringsToLowerCase(cycle.getNewStackThreads());
			ArrayList<String> aliveStacks = convertAllStringsToLowerCase(cycle.getAliveStackThreads());
			ArrayList<String> deletedStacks = convertAllStringsToLowerCase(cycle.getDeletedStackThreads());

			if(newStacks.contains(threadName.toLowerCase()) ||
					aliveStacks.contains(threadName.toLowerCase()))
				status = 1;
			else if(deletedStacks.contains(threadName.toLowerCase()))
				status = 0;	
		}

		return status;
	}

	/**
	 * Get the delta data for the given threadname from all the cycles.
	 * @param threadName name of the thread
	 * @param logData list of cycledata to be parsed.
	 * @return the ThreadData structure which holds the differences for various
	 * fields in the lastCycle and the cycle in which the thread has actually started.  
	 */
	public ThreadData getChangeinHeapData(String threadName, ArrayList<ThreadData> allCyclesData, ParsedData logData)
	{
		ThreadData heapDataChange = new ThreadData(threadName);

		//If the heap for this thread is already deleted all the
		//heap related fields will be set to 0.
		if(getHeapStatusFromAllCycles(threadName, logData) == 0)
		{
			//Display zeros for all heap fields
			heapDataChange.setMaxHeapSize(0);
			heapDataChange.setHeapChunkSize(0);
			heapDataChange.setHeapAllocatedSpace(0);
			heapDataChange.setHeapFreeSpace(0);
			heapDataChange.setAllocatedCells(0);
			heapDataChange.setFreeCells(0);
			heapDataChange.setFreeSlackSize(0);
			heapDataChange.setLargestAllocCellSize(0);
			heapDataChange.setLargestFreeCellSize(0);

		}
		else{

			ThreadData lastHeapData = allCyclesData.get(logData.getNumberOfCycles()-1);

			//Get the heap values from the cycle in which the
			//thread is started.
			ThreadData firstHeapData = null;

			for(int i=allCyclesData.size()-2; i>=0; i--)
			{
				ThreadData data = allCyclesData.get(i);
				if(data.getStatus() != CycleData.Deleted){
					firstHeapData = data;
				}
				else
					break;
			}

			if(firstHeapData != null)
			{
				heapDataChange.setMaxHeapSize(firstHeapData.getMaxHeapSize());
				long changeInHeapSize = lastHeapData.getHeapChunkSize() - firstHeapData.getHeapChunkSize();
				long changeInAllocSpace = lastHeapData.getHeapAllocatedSpace() - firstHeapData.getHeapAllocatedSpace();
				long changeInFreeSpace = lastHeapData.getHeapFreeSpace() - firstHeapData.getHeapFreeSpace();
				long changeInAllocCells = lastHeapData.getAllocatedCells() - firstHeapData.getAllocatedCells();
				long changeInFreeCells = lastHeapData.getFreeCells() - firstHeapData.getFreeCells();
				long changeInSlackSize = lastHeapData.getFreeSlackSize() - firstHeapData.getFreeSlackSize();
				long changeInLargestAllocCell = lastHeapData.getLargestAllocCellSize() - firstHeapData.getLargestAllocCellSize();
				long changeInLargestFreeCell = lastHeapData.getLargestFreeCellSize() - firstHeapData.getLargestFreeCellSize();

				heapDataChange.setHeapChunkSize(changeInHeapSize);
				heapDataChange.setHeapAllocatedSpace(changeInAllocSpace);
				heapDataChange.setHeapFreeSpace(changeInFreeSpace);
				heapDataChange.setAllocatedCells(changeInAllocCells);
				heapDataChange.setFreeCells(changeInFreeCells);
				heapDataChange.setFreeSlackSize(changeInSlackSize);
				heapDataChange.setLargestAllocCellSize(changeInLargestAllocCell);
				heapDataChange.setLargestFreeCellSize(changeInLargestFreeCell);

			}
			else
			{
				heapDataChange.setMaxHeapSize(lastHeapData.getMaxHeapSize());
				heapDataChange.setHeapChunkSize(lastHeapData.getHeapChunkSize());
				heapDataChange.setHeapAllocatedSpace(lastHeapData.getHeapAllocatedSpace());
				heapDataChange.setHeapFreeSpace(lastHeapData.getHeapFreeSpace());
				heapDataChange.setAllocatedCells(lastHeapData.getAllocatedCells());
				heapDataChange.setFreeCells(lastHeapData.getFreeCells());
				heapDataChange.setFreeSlackSize(lastHeapData.getFreeSlackSize());
				heapDataChange.setLargestAllocCellSize(lastHeapData.getLargestAllocCellSize());
				heapDataChange.setLargestFreeCellSize(lastHeapData.getLargestFreeCellSize());
			}
		}

		ThreadData lastHeapData = allCyclesData.get(logData.getNumberOfCycles()-1);

		if(getStackStatusFromAllCycles(threadName, logData) == 0)
		{
			heapDataChange.setStackSize(0);
		}
		else
		{
			heapDataChange.setStackSize(lastHeapData.getStackSize());
		}

		heapDataChange.setOpenFiles(lastHeapData.getOpenFiles());

		if(lastHeapData.getPsHandles() == -1)
			heapDataChange.setPsHandles(0);
		else
			heapDataChange.setPsHandles(lastHeapData.getPsHandles());

		return heapDataChange;
	}

	/**
	 * Returns the start and end cycle numbers for the threads having multiple instances 
	 * @param threadData list
	 * @return start and end cycle numbers 
	 */
	public ThreadSegments [] getHeapSegments(ArrayList<ThreadData> threadData)
	{
		ArrayList<ThreadSegments> thSegments = new ArrayList<ThreadSegments>();
		boolean is_thread_started = false;
		ThreadSegments segment = null;

		for(ThreadData data: threadData)
		{
			if(data.getStatus() == CycleData.New && !is_thread_started)
			{
				segment = new ThreadSegments();
				is_thread_started = true;
				segment.setStartCycle(data.getCycleNumber());
			}
			if((data.getStatus() == CycleData.Deleted || data.getCycleNumber() == threadData.size()) && is_thread_started)
			{
				is_thread_started = false;
				if(segment != null){
					segment.setEndCycle(data.getCycleNumber());
					thSegments.add(segment);
				}
			}
		}
		return thSegments.toArray(new ThreadSegments[0]);
	}
	/**
	 * Returns the delta value for the given global chunk.
	 * @param chunkName
	 * @param logData
	 * @return delta value for the given global chunk
	 */
	public long getChangeinGlodChunksData(String chunkName, ParsedData logData)
	{

		ArrayList<GlobalDataChunks> heapData = getGLOBDataFromAllCycles(chunkName, logData);


		if(heapData.get(heapData.size()-1).getAttrib() == CycleData.Deleted)
			return 0;
		else
		{
			long lastValue = heapData.get(heapData.size()-1).getSize();
			long firstValue = 0;
			for(GlobalDataChunks data: heapData)
			{
				if(data.getSize() != -1)
				{
					firstValue = data.getSize();
					break;
				}
			}
			return lastValue - firstValue;
		}		
	}

	/**
	 * Returns the delta value for the given chunk.
	 * @param chunkName
	 * @param logData
	 * @return delta value for the given chunk
	 */
	public long getChangeinChunksData(String chunkName, ParsedData logData)
	{
		ArrayList<ChunksData> heapData = getChunkDataFromAllCycles(chunkName, logData);

		if(heapData.get(heapData.size()-1).getAttrib() == CycleData.Deleted)
			return 0;
		else
		{
			long lastValue = heapData.get(heapData.size()-1).getSize();
			long firstValue = 0;
			for(ChunksData data: heapData)
			{
				if(data.getSize() != -1)
				{
					firstValue = data.getSize();
					break;
				}
			}
			return lastValue - firstValue;
		}		
	}

	/**
	 * Return sum of the given delta values.
	 * @param deltaValues
	 * @return the sum of all given values
	 */
	public long calculateAndGetTotal(long[] deltaValues)
	{
		long total = 0;

		for(long value:deltaValues)
		{
			total = total + value;
		}		
		return total;
	}

	/**
	 * Returns time intervals for all the given cycles.
	 * @param parsedData
	 * @return intervals
	 */
	public int [] getTimeIntervalsFromLogData(ParsedData parsedData)
	{
		CycleData [] cyclesData = parsedData.getLogData();

		if(cyclesData == null)
			return null;

		int [] timeIntervals = new int[cyclesData.length];

		timeIntervals[0] = 0;

		for(int i=1; i<timeIntervals.length; i++)
			timeIntervals[i] = (int)getDurationInSeconds(cyclesData[i-1].getTime(), cyclesData[i].getTime()) + timeIntervals[i-1];

		return timeIntervals;
	}
	/**
	 * 
	 * @param valuesArr
	 * @return the difference between the last value and the first value, if the given set does not contain -1.
	 * If the set contains -1, the value after that would be treated as the first value.
	 */
	public long calculateDeltaForGivenSet(long [] valuesArr)
	{
		int length = valuesArr.length;

		long lastValue = valuesArr[length-1];
		long firstValue = -1;

		for(int i = length-2; i>=0;i--)
		{
			if(valuesArr[i] != -1)
				firstValue = valuesArr[i];
			else
				break;
		}

		if(lastValue != -1)
		{
			if(firstValue != -1)
				return lastValue - firstValue;
			else
				return lastValue;
		}
		else
			return 0;
	}

	/**
	 * Returns unique list of global chunk names from all the log files.
	 * @see GlobalDataChunks#getChunkName()
	 * @param data
	 * @return list about global chunk names
	 */
	public ArrayList<String> getAllGlobalChunkNames(ParsedData data)
	{
		if(data == null || data.getNumberOfCycles() == 0)
			return null;

		CycleData [] parsed_cycles = data.getLogData();
		ArrayList<String> globalChunkNames = new ArrayList<String>();

		for(CycleData cycle: parsed_cycles)
		{
			if(cycle.getGlobalDataChunksList()!=null)
				for(GlobalDataChunks chunkName:cycle.getGlobalDataChunksList())
				{
					if(!globalChunkNames.contains(chunkName.getChunkName()))
						globalChunkNames.add(chunkName.getChunkName());
				}
		}

		return globalChunkNames;
	}

	/**
	 * Returns unique list of non heap chunk names from all the log files.
	 * @see ChunksData#getChunkName()
	 * @param data
	 * @return List of non heap chunk names
	 */
	public ArrayList<String> getAllNonHeapChunkNames(ParsedData data)
	{
		if(data == null || data.getNumberOfCycles() == 0)
			return null;

		ArrayList<String> chunkNames = new ArrayList<String>();
		CycleData [] parsed_cycles = data.getLogData();

		for(CycleData cycle: parsed_cycles)
		{
			if(cycle.getChunksList()!=null)
				for(ChunksData chunkName:cycle.getChunksList())
				{
					if(!chunkNames.contains(chunkName.getChunkName()))
						chunkNames.add(chunkName.getChunkName());
				}
		}

		return chunkNames;
	}

	/**
	 * This method returns unique list of window group names from all the log files.
	 * @param data
	 * @return window group names
	 */
	public ArrayList<String> getWindowGroupNames(ParsedData data)
	{
		if(data == null || data.getNumberOfCycles() == 0)
			return null;
		
		ArrayList<String> wndgNames = new ArrayList<String>();
		CycleData [] parsed_cycles = data.getLogData();
		
		for(CycleData cycle: parsed_cycles)
		{
			for(WindowGroups name:cycle.getWindowGroupsData())
			{
				if(!wndgNames.contains(name.getName()))
						wndgNames.add(name.getName());
			}
		}
		
		return wndgNames;
	}
	
	/**
	 * Get {@link WindowGroupEventData} for given window group name
	 * @param windowGroupName
	 * @param logData
	 * @return list of window group data
	 */
	public ArrayList<WindowGroupEventData> getAllWindowGroupEvents(String windowGroupName, ParsedData logData)
	{
		if(logData == null || logData.getNumberOfCycles() == 0)
			return null;
		
		CycleData [] parsed_cycles = logData.getLogData();
		
		ArrayList<WindowGroupEventData> eventsData = new ArrayList<WindowGroupEventData>();
		
		WindowGroupEventData prevData = null;
		
		for(CycleData cycle:parsed_cycles)
		{
			WindowGroupEventData currentData;
			
			if(prevData == null)
				currentData = new WindowGroupEventData();
			else
				currentData = new WindowGroupEventData(prevData);
			
			for(WindowGroups grp: cycle.getWindowGroupsData())
			{
				if(grp.getName().equalsIgnoreCase(windowGroupName))
				{
					if(grp.getStatus() == CycleData.New || grp.getStatus() == CycleData.Alive)
						currentData.incrementEventCount(grp.getEvent());
					else if(grp.getStatus() == CycleData.Deleted)
						currentData.decrementEventCount(grp.getEvent());
				}
			}
			
			prevData = currentData;
			eventsData.add(currentData);
		}
		
		return eventsData;
	}
	/**
	 * Get all threads names from data
	 * @param data
	 * @return List of thread names.
	 */
	public ArrayList<String> getThreadsFromAllViews(CycleData data)
	{
		ArrayList<String> threads = new ArrayList<String>();

		data.parseAllHeaps();

		for(String threadName:data.getNewHeapThreads())
		{
			if(!threads.contains(threadName))
				threads.add(threadName);
		}

		for(String threadName:data.getAliveHeapThreads())
		{
			if(!threads.contains(threadName))
				threads.add(threadName);
		}

		for(String threadName:data.getNewStackThreads())
		{
			if(!threads.contains(threadName))
				threads.add(threadName);
		}

		for(String threadName:data.getAliveStackThreads())
		{
			if(!threads.contains(threadName))
				threads.add(threadName);
		}

		for(String threadName:data.getFileThreads())
		{
			if(!threads.contains(threadName))
				threads.add(threadName);
		}

		for(String threadName:data.getHPASThreads())
		{
			if(threadName.length() !=0 && !threads.contains(threadName))
				threads.add(threadName);
		}
		return threads;
	}

	/**
	 * Gets the total for given delta values.
	 * @param deltaValues
	 * @return total delta values counted together
	 */
	public long getTotal(ArrayList<String> deltaValues)
	{
		long total = 0;
		for(String value:deltaValues)
		{
			if(value != "N/A")
				total = total + Long.parseLong(value);
		}		
		return total;
	}

	/**
	 * Converts all given strings to lower case.
	 * @param inputList
	 * @return
	 */
	private ArrayList<String> convertAllStringsToLowerCase(ArrayList<String> inputList)
	{
		ArrayList<String> outputList = new ArrayList<String>();
		if(inputList != null)
		{
			for(int i=0;i<inputList.size();i++)
				outputList.add(inputList.get(i).toLowerCase());

		}
		return outputList;
	}

	/**
	 * Checks whether the ROM version and ROM Checksum of all log files are same or not.
	 * @param cycleData
	 * @return <code>true</code> if ROM Checksum is OK <code>false</code> otherwise.
	 */
	public boolean checkRomInfo(ArrayList<CycleData> cycleData)
	{
		boolean result = true;

		for(int i = 0; i<=cycleData.size()-2; i++)
		{
			String currentCheckSum = cycleData.get(i).getRomCheckSum();
			String nextCheckSum = cycleData.get(i+1).getRomCheckSum();

			String currentVersion = cycleData.get(i).getRomVersion();
			String nextVersion = cycleData.get(i+1).getRomVersion();

			if((!currentCheckSum.equals(nextCheckSum)) || (!currentVersion.equals(nextVersion)))
			{
				result = false;
				break;
			}
		}

		return result;
	}

	/**
	 * Check timestamps from consecutive cycles
	 * if log n+1 time stamp is lesser than log n then return log n+1 cycle number. else return 0
	 * @param allcycleData
	 * @return Returns cycle number or 0   
	 */
	public int checkTimeStamp(ArrayList<CycleData> allcycleData){

		for(int i=1;i<allcycleData.size();i++){
			String currentTime = allcycleData.get(i).getTime();
			String prevTime = allcycleData.get(i-1).getTime();
			long timeDiff = getDurationInSeconds(prevTime, currentTime);
			if(timeDiff < 0){
				// if time stamp of log n+1 is lesser than log n
				return allcycleData.get(i).getCycleNumber();  
			}

		}
		return 0;// if time stamp of log n+1 is greater than log n 
	}
}
