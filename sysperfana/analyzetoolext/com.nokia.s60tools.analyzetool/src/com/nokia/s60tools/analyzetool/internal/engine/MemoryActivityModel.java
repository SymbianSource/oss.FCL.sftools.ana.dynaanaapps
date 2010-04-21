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
 * Description:  Definitions for the class MemoryActivityModel
 *
 */
package com.nokia.s60tools.analyzetool.internal.engine;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Collection;

import com.nokia.s60tools.analyzetool.engine.ICallstackManager;
import com.nokia.s60tools.analyzetool.engine.IMemoryActivityModel;
import com.nokia.s60tools.analyzetool.engine.IMemoryActivityModelChangeListener;
import com.nokia.s60tools.analyzetool.engine.statistic.BaseInfo;
import com.nokia.s60tools.analyzetool.engine.statistic.ProcessInfo;

/**
 * AnalyseGraphModel
 * this class fills a model suitable for AnalyseTool graph
 * the model is initialised from the constructor.  
 *
 */
public class MemoryActivityModel implements IMemoryActivityModel {
	private final Collection<IMemoryActivityModelChangeListener> listeners; // listeners for changes to the model

	/** list of processes */
	private AbstractList<ProcessInfo> processList;
	
	/** time of the process that started first */
	private Long firstProcessTime = 0L;
	
	/** time of the process that ended last */
	private Long lastProcessTime = 0L;
	
	/** first memory operation in the model */
	private BaseInfo firstMemOp = null;
	
	/** time of the first memory operation in the model */
	private Long firstMemOpTime = 0L;
	
	/** last memory opeartion in the  model */
	private BaseInfo lastMemOp =  null;
	
	/** time of the last memory operation in the model */
	private Long lastMemOpTime = 0L;
	
	/** highest cummulative memory allocation in all processes */
	private int highestCumMemSize = 0;

	/** the currently selected process*/
	private ProcessInfo selectedProcess;
	
	/** computed values for currently selected process*/
	private ProcessInfoComputedValues selectedProcessComputedValues;

	private boolean deferredCallstackReading;
	private ICallstackManager callstackManager;
	

	/**
	 * Constructor. 
	 * 
	 */
	public MemoryActivityModel() {
		this.listeners= new ArrayList<IMemoryActivityModelChangeListener>();
	}
	
	/* (non-Javadoc)
	 * @see com.nokia.s60tools.analyzetool.engine.IMemoryActivityModel#addProcesses(java.util.AbstractList)
	 */
	public void addProcesses(AbstractList<ProcessInfo> processes){
		if (processes == null) {
			this.processList = new ArrayList<ProcessInfo>();
		} else {
			this.processList = new ArrayList<ProcessInfo>(processes);
		}
		selectedProcess = null;
		selectedProcessComputedValues = null;
		firstProcessTime = 0L;
		lastProcessTime = 0L;
		firstMemOp = null;
		firstMemOpTime = 0L;
		lastMemOp =  null;
		lastMemOpTime = 0L;
		highestCumMemSize = 0;
		// compute all needed values.
		if (!processList.isEmpty()) {
			computeValues();
		}	
		fireDataChanged();
		
	}
	
	/**
	 * Notifies listeners that data has changed
	 */
	private void fireDataChanged() {
		for (IMemoryActivityModelChangeListener listener : listeners) {
			listener.onProcessesAdded();
		}
		
	}
	/* (non-Javadoc)
	 * @see com.nokia.s60tools.analyzetool.engine.IMemoryActivityModel#setSelectedProcess(com.nokia.s60tools.analyzetool.engine.statistic.ProcessInfo)
	 */
	public void setSelectedProcess(ProcessInfo processInfo) throws IllegalArgumentException {
		if (this.processList == null || processInfo != null && !this.processList.contains(processInfo)) {
			throw new IllegalArgumentException("Error selecting unknown process");
		}
		selectedProcess = processInfo;
		selectedProcessComputedValues = null;
		fireProcessSelected(processInfo);
	}
	
	/* (non-Javadoc)
	 * @see com.nokia.s60tools.analyzetool.engine.IMemoryActivityModel#getSelectedProcess()
	 */
	public ProcessInfo getSelectedProcess(){
		return selectedProcess;
	}
	
	/**
	 * Notifies listeners that data has changed
	 */
	private void fireProcessSelected(ProcessInfo p) {
		for (IMemoryActivityModelChangeListener listener : listeners) {
			listener.onProcessSelected(p);
		}
		
	}

	/**
	 * Compute the model
	 */
	private void computeValues() {
		computeFirstProcessTime();
		computeLastProcessTime();
		computeFirstMemOpTime();
		computeLastMemOpTime();
		computeHighestCumulatedMemoryAlloc();
	}

	/**
	 * find the start time of the process that started first
	 */
	private void computeFirstProcessTime() {
		Long smallestTime = Long.MAX_VALUE;
		firstProcessTime = 0L;//stays zero if there are no processes

		for (ProcessInfo process : processList) {
			if (process.getStartTime() < smallestTime) {
				smallestTime = process.getStartTime();
				firstProcessTime = smallestTime; 
			}
		}
	}
	
	/**
	 * find end time of the process that ended last
	 */
	private void computeLastProcessTime() {
		Long biggestTime = Long.MIN_VALUE;
		lastProcessTime = 0L; //stays zero if there are no processes
		
		for (ProcessInfo process : processList) {
			Long tmpTime = process.getEndTime();
			if (tmpTime > biggestTime) {
				biggestTime = tmpTime;
				lastProcessTime = tmpTime;
			}
		}
	}

	/**
	 *  find first memory time from all processes
	 */
	private void computeFirstMemOpTime() {
		findFirstMemOp();
	}

	/** find first memory operation in the model */
	private void findFirstMemOp() {
		Long smallestTime = Long.MAX_VALUE;
		for (ProcessInfo process : processList) {
			AbstractList<BaseInfo> allocsAndFrees = process.getAllocsFrees();
			BaseInfo baseInfo = allocsAndFrees.get(0);
			long time = baseInfo.getTime();
			if (time < smallestTime ){
				smallestTime = time;
				firstMemOp = baseInfo;
			}
		}
		if (firstMemOp != null) {
			firstMemOpTime = smallestTime;
		}
	}
	
	/**
	 * find last memory operation time from all processes
	 */
	void computeLastMemOpTime() {
		findLastMemOp();
	}

	/** find last memory operation */
	private void findLastMemOp() {
		Long biggestTime = firstMemOpTime; //Long.MIN_VALUE;
		for (ProcessInfo process : processList) {
			AbstractList<BaseInfo> allocsAndFrees = process.getAllocsFrees();
			BaseInfo baseInfo = allocsAndFrees.get(allocsAndFrees.size() -1);
			long time = baseInfo.getTime();
			if (time > biggestTime ){
				biggestTime = time;
				lastMemOp = baseInfo;
			}
		}
		if (lastMemOp != null) {
			lastMemOpTime = biggestTime;
		}
	}
	
	/** calculate the biggest memory size in all processes */
	private void computeHighestCumulatedMemoryAlloc() {
		for (ProcessInfo process : processList) {
			if (process.getHighestCumulatedMemoryAlloc() > highestCumMemSize) {
				highestCumMemSize = process.getHighestCumulatedMemoryAlloc();
			}
		}
	}
	
	/* (non-Javadoc)
	 * @see IMemoryActivityModel#getProcesses()
	 */
	public AbstractList<ProcessInfo> getProcesses() {
		return processList;
	}

	/* (non-Javadoc)
	 * @see com.nokia.s60tools.analyzetool.engine.IMemoryActivityModel#getFirstMemOpTime()
	 */
	public Long getFirstMemOpTime() {
		return firstMemOpTime;
	}

	/* (non-Javadoc)
	 * @see com.nokia.s60tools.analyzetool.engine.IMemoryActivityModel#getFirstProcessTime()
	 */
	public Long getFirstProcessTime() {
		if (selectedProcess != null){
			if (selectedProcessComputedValues == null){
				selectedProcessComputedValues = new ProcessInfoComputedValues(selectedProcess);
			}
			return selectedProcessComputedValues.getProcessStartTime();
		}
		return firstProcessTime;
	}


	/* (non-Javadoc)
	 * @see com.nokia.s60tools.analyzetool.engine.IMemoryActivityModel#getHighestCumulatedMemoryAlloc()
	 */
	public int getHighestCumulatedMemoryAlloc() {
		if (selectedProcess != null){
			if (selectedProcessComputedValues == null){
				selectedProcessComputedValues = new ProcessInfoComputedValues(selectedProcess);
			}
			return selectedProcessComputedValues.getHighestCumulatedMemorySize();
		}
		return highestCumMemSize;
	}


	/* (non-Javadoc)
	 * @see com.nokia.s60tools.analyzetool.engine.IMemoryActivityModel#getLastMemOpTime()
	 */
	public Long getLastMemOpTime() {
		return lastMemOpTime;
	}

	/* (non-Javadoc)
	 * @see com.nokia.s60tools.analyzetool.engine.IMemoryActivityModel#getLastProcessTime()
	 */
	public Long getLastProcessTime() {
		if (selectedProcess != null){
			if (selectedProcessComputedValues == null){
				selectedProcessComputedValues = new ProcessInfoComputedValues(selectedProcess);
			}
			return selectedProcessComputedValues.getProcessEndTime();
		}
		return lastProcessTime;
	}

	/* (non-Javadoc)
	 * @see com.nokia.s60tools.analyzetool.engine.IMemoryActivityModel#addListener(com.nokia.s60tools.analyzetool.engine.IMemoryActivityModelChangeListener)
	 */
	public void addListener(IMemoryActivityModelChangeListener listener) {
		listeners.add(listener);
	}

	/* (non-Javadoc)
	 * @see com.nokia.s60tools.analyzetool.engine.IMemoryActivityModel#removeListener(com.nokia.s60tools.analyzetool.engine.IMemoryActivityModelChangeListener)
	 */
	public void removeListener(IMemoryActivityModelChangeListener listener) {
		listeners.remove(listener);
	}
	
	/**
	 * 
	 * This class computes values for a given process
	 *
	 */
	private class ProcessInfoComputedValues{
		/** process life cycle start  */
		private long processStartTime;
		
		/** process life cycle end */ 
		private long processEndTime;
		
		/** highest value for cumulative memory allocation during the processes life time*/
		private int highestCumulatedMemorySize;
		
		
		/**
		 * Constructor
		 * @param p the ProcessInfo for this process
		 */
		public ProcessInfoComputedValues(ProcessInfo p) {
			computeValues(p);
		}

		/**
		 * Compute the values for the given process
		 * @param p the ProcessInfo to use
		 */
		private void computeValues(ProcessInfo p){
			//calculate highest cumulative memory value
			highestCumulatedMemorySize = p.getHighestCumulatedMemoryAlloc();
			
			//work out process start time
			Long startTime = p.getStartTime();
			if (startTime == null && p.getAllocsFrees().size() > 0){
				//take the time of the first memory activity
				startTime = p.getAllocsFrees().get(0).getTime();
			}
			if (startTime != null){
				processStartTime = startTime;
			}
			
			//work out process end time
			Long endTime = p.getEndTime();
			if (endTime == null && p.getAllocsFrees().size() > 0){
				//take the time of the last memory activity
				endTime = p.getAllocsFrees().get(p.getAllocsFrees().size()-1).getTime();
			}
			if (endTime != null){
				processEndTime = endTime;
			}
		}

		/**
		 * @return the process start time
		 */
		public long getProcessStartTime() {
			return processStartTime;
		}

		/**
		 * @return the process end time
		 */
		public long getProcessEndTime() {
			return processEndTime;
		}

		/**
		 * @return the highest cumulative memory size for the process
		 */
		public int getHighestCumulatedMemorySize() {
			return highestCumulatedMemorySize;
		}

	}
	/* (non-Javadoc)
	 * @see com.nokia.s60tools.analyzetool.engine.IMemoryActivityModel#setDeferredCallstackReading(boolean)
	 */
	public void setDeferredCallstackReading(boolean deferredCallstackReading) {
		this.deferredCallstackReading = deferredCallstackReading;
	}

	/* (non-Javadoc)
	 * @see com.nokia.s60tools.analyzetool.engine.IMemoryActivityModel#isDeferredCallstackReading()
	 */
	public boolean isDeferredCallstackReading() {
		return deferredCallstackReading;
	}

	/* (non-Javadoc)
	 * @see com.nokia.s60tools.analyzetool.engine.IMemoryActivityModel#getCallstackManager()
	 */
	public ICallstackManager getCallstackManager() {
		return callstackManager;
	}

	/* (non-Javadoc)
	 * @see com.nokia.s60tools.analyzetool.engine.IMemoryActivityModel#setCallstackManager(com.nokia.s60tools.analyzetool.engine.statistic.CallstackManager)
	 */
	public void setCallstackManager(ICallstackManager callstackManager) {
		this.callstackManager = callstackManager;
		
	}
	
	// Other public methods can be added here.
}
