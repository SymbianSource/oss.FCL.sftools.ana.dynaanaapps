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
package com.nokia.s60tools.swmtanalyser.analysers;

import java.text.DecimalFormat;
import java.util.ArrayList;

import com.nokia.s60tools.swmtanalyser.data.DiskOverview;
import com.nokia.s60tools.swmtanalyser.data.KernelElements;
import com.nokia.s60tools.swmtanalyser.data.ParsedData;
import com.nokia.s60tools.swmtanalyser.data.SystemData;
import com.nokia.s60tools.swmtanalyser.model.SWMTLogReaderUtils;

/**
 * 
 * Defines Enum constants for all Kernel events to be analysed.
 */
enum KERNEL_EVENTS{
	NO_OF_THREADS("Number of Threads"), NO_OF_PROCESSES("Number of Processes"),
	NO_OF_TIMERS("Number of Timers"), NO_OF_SEMAPHORES("Number of Semaphores"),
	NO_OF_SERVERS("Number of Servers"), NO_OF_MSGQUEUES("Number of Msg. Queues"),
	NO_OF_SESSIONS("Number of Sessions"), NO_OF_CHUNKS("Number of Chunks");
	
	private String event_name;
	private String event_cateogory = "System Data";
	
	private KERNEL_EVENTS(String eventName) {
		this.event_name = eventName;
	}
	
	/**
	 * Returns event string
	 * @return name of the event
	 */
	public String getEventName()
	{
		return event_name;
	}
	
	/**
	 * Returns event category
	 * @return category of the event
	 */
	public String getEventCategory()
	{
		return event_cateogory;
	}
}

/**
 * Analyses RAM and DISK events and Kernal events. It implements interface IAnalyser.
 * 
 */
public class LinearAnalyser implements IAnalyser {

	/**
	 * {@link #RAM_AND_DISK_TITLE} and {@link #KERNEL_ELEMS_TITLE} issues
	 */
	ArrayList<ResultsParentNodes> allIssues = new ArrayList<ResultsParentNodes>();
	
	protected static DecimalFormat Bytes_Format = new DecimalFormat("#####.##");
	protected int [] time_intervals = null;
	
	private static final String RAM_AND_DISK_TITLE = "RAM and Disk Memory";
	private static final String KERNEL_ELEMS_TITLE = "Kernel Elements";
	
	private ParsedData logData = null;
	

	/* (non-Javadoc)
	 * @see com.nokia.s60tools.swmtanalyser.analysers.IAnalyser#analyse(com.nokia.s60tools.swmtanalyser.data.ParsedData)
	 */
	public void analyse(ParsedData logData) {
		
		this.logData = logData;
		allIssues.clear();
		
		//List to store RAM DISK issues
		ArrayList<ResultElements> ram_disk_issues = new ArrayList<ResultElements>();

		//Analyse RAM events using the logData and store the results in given list
		analyseUsedRam(logData, ram_disk_issues);
		//Analyse DISK events using the logData and store the results in given list
		analyseDiskSizes(logData, ram_disk_issues);
		
		//Create parent for all RAM and Disks issues.
		ResultsParentNodes ram_and_disk_node = new ResultsParentNodes(RAM_AND_DISK_TITLE);
		ram_and_disk_node.setChildren(ram_disk_issues);
		allIssues.add(ram_and_disk_node);
		
		//List to store Kernel issues
		ArrayList<ResultElements> kernel_issues = new ArrayList<ResultElements>();
		//Analyse Kernel events using the logData and store the results in given list		
		analyseKernelHandles(logData, kernel_issues);
	
		//Create parent for all kernel issues found
		ResultsParentNodes kernel_analysis_node = new ResultsParentNodes(KERNEL_ELEMS_TITLE);
		kernel_analysis_node.setChildren(kernel_issues);
		allIssues.add(kernel_analysis_node);
	}


	/* (non-Javadoc)
	 * @see com.nokia.s60tools.swmtanalyser.analysers.IAnalyser#getResults()
	 */
	public Object[] getResults() {
		return allIssues.toArray();
	}
	

	/* (non-Javadoc)
	 * @see com.nokia.s60tools.swmtanalyser.analysers.IAnalyser#getChildren(java.lang.Object)
	 */
	public Object[] getChildren(Object parent)
	{
		if(parent instanceof ResultsParentNodes && allIssues.contains(parent))
		{
			return ((ResultsParentNodes)(parent)).getChildren();
		}
		else
			return null;
	}
	
	/**
	 * This method linearly analyses the variation of used Ram size.
	 * @param logData signifies data of all seleced cycles.
	 * @param results signifies the list to which all issues will be added to.
	 */
	private void analyseUsedRam(ParsedData logData, ArrayList<ResultElements> results)
	{
		SWMTLogReaderUtils utils = new SWMTLogReaderUtils();
		
		ArrayList<SystemData> systemData = utils.getSystemDataFromAllCycles(logData);
		
		//Store used memory values
		long [] usedRamValues = new long[logData.getNumberOfCycles()];
		
		//Calculate used memory using the total and free memroy
		for(int i=0; i<logData.getNumberOfCycles(); i++)
		{
			long totalMem = systemData.get(i).getTotalMemory();
			long freeMem = systemData.get(i).getFreeMemory();
			
			if(totalMem == -1 || freeMem == -1){
				usedRamValues[i] = 0;
			}
			else{
				long usedMemory = totalMem - freeMem; 
				usedRamValues[i] = usedMemory;
			}
		}
		
		//Get the delta value.
		long usedMemChange = utils.calculateDeltaForGivenSet(usedRamValues);
			
		ResultElements res_elem = new ResultElements("RAM", "RAM used", getFormattedBytes(usedMemChange), usedMemChange, AnalyserConstants.DeltaType.SIZE);
		res_elem.setEventValues(usedRamValues);
		
		calculateGrowinessAndPriority(usedRamValues, res_elem);
			
		//Add if the element priority is CRITICAL or HIGH or NORMAL
		if(res_elem.getPriority() != AnalyserConstants.Priority.NEGLIGIBLE)
			results.add(res_elem);
			
	}
	
	/**
	 * This method linearly analyses the variation of used size for all disks.
	 * @param logData signifies data of all seleced cycles.
	 * @param results signifies the list to which all issues will be added to.
	 */
	private void analyseDiskSizes(ParsedData logData, ArrayList<ResultElements> results)
	{
		SWMTLogReaderUtils utils = new SWMTLogReaderUtils();
		ArrayList<String> diskNames = utils.getAllDiskNames(logData);
		for(String disk:diskNames)	{
			ArrayList<DiskOverview> diskData = utils.getUsedMemoryAndSizesForDisk(disk, logData);

			//Store used memory values
			long [] diskUsedValues = new long[logData.getNumberOfCycles()];
			for(int i=0; i<logData.getNumberOfCycles(); i++){
				diskUsedValues[i] = diskData.get(i).getUsedSize();
			}
			long delta = utils.calculateDeltaForGivenSet(diskUsedValues);
					
			ResultElements res_elem = new ResultElements(disk, "Disk used", getFormattedBytes(delta), delta, AnalyserConstants.DeltaType.SIZE);
			res_elem.setEventValues(diskUsedValues);
			
			calculateGrowinessAndPriority(diskUsedValues, res_elem);
			//Add if the element priority is CRITICAL or HIGH or NORMAL			
			if(res_elem.getPriority() != AnalyserConstants.Priority.NEGLIGIBLE)
				results.add(res_elem);			
		}	
	}
	
	/**
	 * This method analyses variation of various kernel handles.
	 * @param logData signifies data of all seleced cycles.
	 * @param results signifies the list to which all issues will be added to.
	 */
	private void analyseKernelHandles(ParsedData logData, ArrayList<ResultElements> results)
	{
		SWMTLogReaderUtils utils = new SWMTLogReaderUtils();
		ArrayList<KernelElements> kernelElems = utils.getKerenelElemsFromAllCycles(logData);
		
		for(KERNEL_EVENTS event: KERNEL_EVENTS.values()){
			long [] event_values = new long[kernelElems.size()];
			for(int i=0; i<kernelElems.size(); i++)
				event_values[i] = getKernelEventValue(kernelElems.get(i),event);
			long delta = utils.calculateDeltaForGivenSet(event_values);
			
			ResultElements result_elem = new ResultElements(event.getEventName(), event.getEventCategory(), Long.toString(delta), delta, AnalyserConstants.DeltaType.COUNT);
			result_elem.setEventValues(event_values);
			
			calculateGrowinessAndPriority(event_values,result_elem);
			//Add if the element priority is CRITICAL or HIGH or NORMAL
			if(result_elem.getPriority() != AnalyserConstants.Priority.NEGLIGIBLE)
				results.add(result_elem);
		}		
	}
	
	/** 
	 * 
	 * @param kernelData -- structure which holds info about all kernel handles in one cycle.
	 * @param event -- name of the kernel event whose value must be read from the structure.
	 * @return value of the event.
	 */
	private long getKernelEventValue(KernelElements kernelData, KERNEL_EVENTS event)
	{
		long event_value = 0;
		
		switch(event)
		{
			case NO_OF_PROCESSES:
				event_value = kernelData.getNumberOfProcesses();
				break;
			case NO_OF_THREADS:
				event_value = kernelData.getNumberOfThreads();
				break;
			case NO_OF_CHUNKS:
				event_value = kernelData.getNumberOfChunks();
				break;
			case NO_OF_SEMAPHORES:
				event_value = kernelData.getNumberOfSemaphores();
				break;
			case NO_OF_SERVERS:
				event_value = kernelData.getNumberOfServers();
				break;
			case NO_OF_SESSIONS:
				event_value = kernelData.getNumberOfSessions();
				break;
			case NO_OF_TIMERS:
				event_value = kernelData.getNumberOfTimers();
				break;
			case NO_OF_MSGQUEUES:
				event_value = kernelData.getNumberOfMsgQueues();
				break;
		}
		
		return event_value;
	}
	
	/**
	 * The method calculates growiness factor based on given event 
	 * values and given time intervals.
	 * @param values values of the event to be analysed
	 * @param time time intervals 
	 * @return growiness factor.
	 */
	private double calculateGrowiness(long [] values, int [] time)
	{
		if(values.length != time.length)
			return 0;
		
		long [] time_differences = new long[time.length];
		
		for(int i=0; i<time.length; i++)
		{
			if(i == 0)
				time_differences[i] = 0;
			else
				time_differences[i] = time[i] - time[i-1];
		}
		long [] normalizedRoCs = calculateNormalizedRoC(values);
		
		double [] normalizedRocWithTime = new double [normalizedRoCs.length];
		
		for(int i=0; i<normalizedRocWithTime.length; i++)
		{
			if(i == 0)
				normalizedRocWithTime[i] = 0;
			else
			{
				//Assuming time difference is greater than zero.
									
				normalizedRocWithTime[i] = (double)(normalizedRoCs[i])/Math.log(time_differences[i]);
			}
		}
		
		double [] overallNormalizedRoc = new double [normalizedRocWithTime.length];
		
		overallNormalizedRoc[0] = 0;
		double totalNormalizedRoc = 0.0;
		
		for(int i=1; i < normalizedRocWithTime.length; i++)
		{
			if(normalizedRocWithTime[i] > 0)
				overallNormalizedRoc[i] =  overallNormalizedRoc[i-1] + normalizedRocWithTime[i];
			else if(normalizedRocWithTime[i] < 0)
				overallNormalizedRoc[i] =  -Math.log(-normalizedRocWithTime[i])+ overallNormalizedRoc[i-1];
			else
				overallNormalizedRoc[i] = overallNormalizedRoc[i-1];
		}
		
		totalNormalizedRoc = overallNormalizedRoc[overallNormalizedRoc.length - 1];
		int growth_index = calculateStableGrowth(values);
		double growiness_factor = Math.exp(growth_index);
		
		double log_of_first = 0;
		double log_of_last = 0;
		
		if(values[values.length -1] != 0)
			log_of_last = Math.log(values[values.length -1]);
		
		if(values[0] != 0)
			log_of_first = Math.log(values[0]);
		
		double log_diff = log_of_last - log_of_first;
		
		double tmp = Math.round(Math.exp(totalNormalizedRoc));
		double final_growth_factor = tmp * log_diff + growiness_factor;
		
		return final_growth_factor;
	}
	
	protected long [] calculateNormalizedRoC(long [] values)
	{
		long [] deltas = new long[values.length];
		long [] valueVsBaseline = new long[values.length];
		
		deltas[0] = 0;
		valueVsBaseline[0] = 0;
		
		for(int i=1; i<values.length; i++)
		{
			deltas[i] = values[i] - values[i-1];
			valueVsBaseline[i] = values[i] - values[0];
		}
		
		long [] normalizedRoC = new long[values.length];
		normalizedRoC[0] = 0;
		
		for(int i=1; i < values.length; i++)
		{
			if(deltas[i] > 0)
			{
				if(valueVsBaseline[i] > 0)
					normalizedRoC[i] = (long)Math.log(deltas[i]);
					
				else if(valueVsBaseline[i] < 0)
					normalizedRoC[i] = (long)(Math.log(deltas[i] + valueVsBaseline[i]));
				else
					normalizedRoC[i] = 0;
			}
			else if(deltas[i] < 0)
				normalizedRoC[i] = -(long)Math.log(-deltas[i]);
			else
				normalizedRoC[i] = 0;
		}
		
		return normalizedRoC;
	}
	
	private int calculateStableGrowth(long [] values)
	{
		int [] stableGrowiness = new int[values.length];
		
		for(int i=0; i<stableGrowiness.length; i++)
		{
			if(i >0)
			{
				long diff = values[i] - values[i-1];
				
				if(diff > 0)
				{
					stableGrowiness[i] = stableGrowiness[i-1] + 1;
				}
				else if(diff == 0)
						stableGrowiness[i] = stableGrowiness[i-1];
				else
				{
					if(values[i] > values[0])
						stableGrowiness[i] = stableGrowiness[i-1] - 1;
					else
						stableGrowiness[i] = 0;
				}
			}
		}
		
		return stableGrowiness[stableGrowiness.length-1];
	}
	
	/**
	 * The method calculates priority of an issue, based on the values of
	 * growiness factor, delta factor and number of Cycles.
	 * @param growing_factor
	 * @param delta_factor
	 * @param noOfCycles
	 * @return priority of the issue.
	 */
	protected AnalyserConstants.Priority calculatePriorityFactor(double growing_factor, double delta_factor, int noOfCycles)
	{
		double high_threshold = Math.exp(noOfCycles - 1);
		int temp = noOfCycles/2;
		double normal_threshold = Math.exp(temp);
						
		int temp1 = noOfCycles/4;
		double low_threshold = Math.exp(temp1);
		
		if(growing_factor > high_threshold)
		{
			if(delta_factor > 0)
				return AnalyserConstants.Priority.CRITICAL;
		}
		if(growing_factor > normal_threshold)
		{
			if(delta_factor > 0.5)
				return AnalyserConstants.Priority.CRITICAL;
			else if(delta_factor > 0)
				return AnalyserConstants.Priority.HIGH;
		}
		if(growing_factor > low_threshold)
		{
			if(delta_factor > 3)
				return AnalyserConstants.Priority.CRITICAL;
			else if(delta_factor > 0.5)
				return AnalyserConstants.Priority.HIGH;
		}
		if(growing_factor > Math.exp(1))
		{
			if(delta_factor > 5)
				return AnalyserConstants.Priority.CRITICAL;
			else if(delta_factor > 3)
				return AnalyserConstants.Priority.HIGH;
		}
		if(growing_factor >0 && delta_factor > 0)
			return AnalyserConstants.Priority.NORMAL;
	
		return AnalyserConstants.Priority.NEGLIGIBLE;
	}
	
	/**
	 * The method formats the given value to Bytes, Kilo Bytes and Mega Bytes.
	 * @param bytes -- value to be formatted.
	 * @return formatted string in KB or MB
	 */
	protected String getFormattedBytes(long bytes)
	{
		String formatted_value = "";
		
		if (bytes < 1024)
		{
			formatted_value += Bytes_Format.format(bytes) + " B"; //$NON-NLS-1$
		}
		else if (bytes <= 500 * 1024)
		{
			formatted_value += Bytes_Format.format(bytes / 1024) + " KB"; //$NON-NLS-1$
		}
		else
		{
			formatted_value +=  Bytes_Format.format(((float) bytes / (1024 * 1024)))  + " MB"; //$NON-NLS-1$
		}
		
		return formatted_value;
	}
	
	/**
	 * The method calculates Growiness and priority based on given set of values.
	 * As no time intervals are provided, the method treats that the event is alive in all cycles (from first to last).
	 * @param event_values -- values of the event being analysed.
	 * @param result_elem -- structure to which calculated growiness and priority would be set to.
	 */
	protected void calculateGrowinessAndPriority(long [] event_values, ResultElements result_elem)
	{
		SWMTLogReaderUtils utils  = new SWMTLogReaderUtils();
		
		if(time_intervals == null)
			time_intervals = utils.getTimeIntervalsFromLogData(logData);
		
		calculateGrowinessAndPriority(event_values, time_intervals, result_elem);
		
	}
	
	/**
	 * The method calculates Growiness and priority based on given set of values and given time intervals
	 * @param event_values
	 * @param log_intervals
	 * @param result_elem
	 */
	protected void calculateGrowinessAndPriority(long [] event_values, int [] log_intervals, ResultElements result_elem)
	{
		double growiness = calculateGrowiness(event_values, log_intervals);
		
		long firstValue = event_values[0];
		long lastValue = event_values[event_values.length -1];
		
		double log_of_first = 0;
		double log_of_last = 0;
		
		if(firstValue != 0)
			log_of_first = Math.log(firstValue);
		if(lastValue != 0)
			log_of_last = Math.log(lastValue);
		
		double prioritization_factor = log_of_last - log_of_first;
		
		AnalyserConstants.Priority priority = calculatePriorityFactor(growiness, prioritization_factor, log_intervals.length);
	
		result_elem.setGrowingFactor(growiness);
		result_elem.setPriority(priority);
	
	}
}
