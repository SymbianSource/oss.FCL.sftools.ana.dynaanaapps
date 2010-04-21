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

import java.util.ArrayList;

import com.nokia.s60tools.swmtanalyser.data.CycleData;
import com.nokia.s60tools.swmtanalyser.data.ParsedData;
import com.nokia.s60tools.swmtanalyser.data.ThreadData;
import com.nokia.s60tools.swmtanalyser.data.ThreadSegments;
import com.nokia.s60tools.swmtanalyser.model.SWMTLogReaderUtils;

/**
 * 
 * Defines Enum constants for all Thread events to be analysed.
 */
enum THREAD_EVENTS {
	
	HEAP_SIZE("Heap size"), NO_OF_FILES("No of Files"), HEAP_ALLOC_SPACE("Heap allocated space"), HEAP_ALLOC_CELLS("Heap allocated cell count"), NO_OF_PSHANDLES("No of PS Handles");

	private String description;

	THREAD_EVENTS(String desc)
	{
		description = desc;
	}
	public String getDescription()
	{
		return description;
	}
}
/**
 * Analyser class implementation for Thread data
 */
public class ThreadDataAnalyser extends LinearAnalyser{
	
	private static final String THREADS_TITLE = "Threads";
	
	ArrayList<ResultsParentNodes> treeElements = new ArrayList<ResultsParentNodes>();
	
	
	/* (non-Javadoc)
	 * @see com.nokia.s60tools.swmtanalyser.analysers.LinearAnalyser#analyse(com.nokia.s60tools.swmtanalyser.data.ParsedData)
	 */
	public void analyse(ParsedData logData) {
						
		treeElements.clear();
		ResultsParentNodes thread_issues = new ResultsParentNodes(THREADS_TITLE);
		
		SWMTLogReaderUtils utils = new SWMTLogReaderUtils();
		
		ArrayList<String> heapThreads = utils.getAllThreadNames(logData);
		
		int in = 0;
		
		ArrayList<ResultElements> sub_issues = new ArrayList<ResultElements> ();
	
		for(String thName: heapThreads)
		{
			ArrayList<ThreadData> thData = utils.getHeapDataFromAllCycles(thName, logData);
			ThreadSegments [] segments = utils.getHeapSegments(thData);
			
			if(segments != null){
				
				THREAD_EVENTS [] events = THREAD_EVENTS.values();
				
				for(THREAD_EVENTS event:events)
					analyseThreadEvent(event, thName, thData, logData, segments, sub_issues);
				
			}
			
			in++;
		}

		thread_issues.setChildren(sub_issues);
		
		treeElements.add(thread_issues);
				
	}

	/* (non-Javadoc)
	 * @see com.nokia.s60tools.swmtanalyser.analysers.LinearAnalyser#getResults()
	 */
	public Object[] getResults() {
		
		return treeElements.toArray();
	}

	/* (non-Javadoc)
	 * @see com.nokia.s60tools.swmtanalyser.analysers.LinearAnalyser#getChildren(java.lang.Object)
	 */
	public Object[] getChildren(Object parent)
	{
		if(parent instanceof ResultsParentNodes && treeElements.contains(parent))
		{
			return ((ResultsParentNodes)(parent)).getChildren();
		}
		else
			return null;
	}
	
	/**
	 * This method analyses data of given thread event
	 * @param event thread event to be analysed
	 * @param thName name of the thread
	 * @param heapData represents data of given thread from all log files
	 * @param logData represents data of all log files
	 * @param thSegments array of segments in which the thread is alive
	 * @param results structure to which all thread issues will be added to.
	 */
	private void analyseThreadEvent(THREAD_EVENTS event, String thName, ArrayList<ThreadData> heapData, ParsedData logData, ThreadSegments [] thSegments, ArrayList<ResultElements> results)
	{
		SWMTLogReaderUtils utils = new SWMTLogReaderUtils();
		CycleData[] cyclesData = logData.getLogData();
		
		for(int i=0; i<thSegments.length; i++)
		{
			ThreadSegments segment = thSegments[i];
			long [] heap_size_values;
			int [] time_intervals;
			
			int startIndex = segment.getStartCycle() - 1;
			int endIndex = segment.getEndCycle() - 1;
			long delta;
			
			long [] event_values = new long[logData.getNumberOfCycles()];
			
			if(heapData.get(endIndex).getStatus() == CycleData.Deleted)
			{
				int tmp = 0;
				heap_size_values = new long[segment.getEndCycle() - segment.getStartCycle()];
				
				time_intervals = new int[heap_size_values.length];
								
				for(int j = startIndex; j < endIndex; j++, tmp++)
				{
					heap_size_values[tmp] = getEventValue(heapData.get(j), event);
					
					if(tmp == 0)
						time_intervals[tmp] = 0;
					else
						time_intervals[tmp] = (int)(time_intervals[tmp -1] + utils.getDurationInSeconds(cyclesData[j-1].getTime(), cyclesData[j].getTime()));
				}
				
			}
			else
			{
				int tmp = 0;
				heap_size_values = new long[segment.getEndCycle() - segment.getStartCycle() + 1];
				time_intervals = new int[heap_size_values.length];
				
				for(int j = startIndex; j <= endIndex; j++, tmp++)
				{
					heap_size_values[tmp] = getEventValue(heapData.get(j), event);
					
					if(tmp == 0)
						time_intervals[tmp] = 0;
					else
						time_intervals[tmp] = (int)(time_intervals[tmp -1] + utils.getDurationInSeconds(cyclesData[j-1].getTime(), cyclesData[j].getTime()));
				}
				
			}
			
			delta = utils.calculateDeltaForGivenSet(heap_size_values);
			
			String thread_instance = thName;
			
			if(thSegments.length > 1){
			    int id = i+1; 
				thread_instance += "(0" + id + ")";
				
				int index;
				
				for(index=0; index < startIndex; index++){
					event_values[index] = 0;
				}
				
				for(int tmp =0; tmp < heap_size_values.length; tmp++)
				{
					event_values[index] = heap_size_values[tmp];
					index++;
				}	
			
				for(;index < cyclesData.length ;index++)
					event_values[index] = 0;
			}
			else
			{
				for(int index=0; index < heapData.size(); index++)
					event_values[index] = getEventValue(heapData.get(index), event);
			}
			String formatted_delta = Long.toString(delta);
			AnalyserConstants.DeltaType delta_type = AnalyserConstants.DeltaType.COUNT;
			switch(event)
			{
				case HEAP_SIZE:
				case HEAP_ALLOC_SPACE:
					formatted_delta = getFormattedBytes(delta);
					delta_type = AnalyserConstants.DeltaType.SIZE;
			}
			
			ResultElements res_elem = new ResultElements(thread_instance, event.getDescription(), formatted_delta, delta, delta_type);
			res_elem.setEventValues(event_values);
						
			calculateGrowinessAndPriority(heap_size_values, time_intervals, res_elem);
			if(res_elem.getPriority() != AnalyserConstants.Priority.NEGLIGIBLE)
			{
				results.add(res_elem);
			}
		}
				
	}
		
	/**
	 * 
	 * @param thData structure represents entire data of a thread in one log(cycle).
	 * @param event a specific thread event
	 * @return value of the given event.
	 */
	private long getEventValue(ThreadData  thData, THREAD_EVENTS event)
	{
		long event_value = 0;
		
		if(thData.getStatus() == CycleData.Deleted)
			return 0;
		
		switch(event)
		{
			case HEAP_SIZE:
				event_value = thData.getHeapChunkSize();
				break;
			case NO_OF_FILES:
				event_value = thData.getOpenFiles();
				break;
			case HEAP_ALLOC_SPACE:
				event_value = thData.getHeapAllocatedSpace();
				break;
			case HEAP_ALLOC_CELLS:
				event_value = thData.getAllocatedCells();
				break;
			case NO_OF_PSHANDLES:
				event_value = thData.getPsHandles();
				break;
		}
		
		return event_value;
		
	}
	
}
