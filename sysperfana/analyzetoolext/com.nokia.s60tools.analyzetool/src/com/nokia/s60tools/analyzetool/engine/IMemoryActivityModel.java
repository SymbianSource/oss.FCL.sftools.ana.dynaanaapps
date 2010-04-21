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
 * Description:  Definitions for the class IMemoryActivityModel
 *
 */
package com.nokia.s60tools.analyzetool.engine;
import java.util.AbstractList;

import com.nokia.s60tools.analyzetool.engine.statistic.ProcessInfo;


/**
 * Interface for the memory model of AnalyzeTool
 */
public interface IMemoryActivityModel {
	
	/**
	 * Sets the data to use. This will cause listeners to be notified.
	 * @param processes The list of processes to set
	 */
	public void addProcesses(AbstractList<ProcessInfo> processes);
	
	/**
	 * Sets the selected ProcessInfo
	 * @param processInfo
	 */
	public void setSelectedProcess(ProcessInfo processInfo);
	
	/**
	 * returns the last selected process
	 * @return Selected process
	 */
	public ProcessInfo getSelectedProcess();

	/**
	 * Get the start time of the process that started first
	 * @return time
	 */
	public Long getFirstProcessTime();

	/**
	 * Get end time of the process that ended last
	 * @return Last process time
	 */
	public Long getLastProcessTime();

	/**
	 * Get time of the first memory operation
	 * @return First memory operation time.
	 */
	public Long getFirstMemOpTime();

	/**
	 * Get last memory operation time from all processes
	 * @return Last memory operation time.
	 */
	public Long getLastMemOpTime();

	/**
	 * get highest cumulated memory
	 * @return size
	 */
	public int getHighestCumulatedMemoryAlloc();

	/**
	 * Get list of processes
	 * @return processes list
	 */
	public AbstractList<ProcessInfo> getProcesses();

	
	/**
	 * @param listener the listener to register
	 */
	void addListener(IMemoryActivityModelChangeListener listener);
	
	/**
	 * @param listener the listener to remove
	 */
	void removeListener(IMemoryActivityModelChangeListener listener);
	
	/**
	 * Returns true if callstack reading from files is done on demand after the
	 * initial parsing phase. This assumes file positions are available from
	 * BaseInfo
	 * @return true for deferred callstack reading, false otherwise
	 */
	public boolean isDeferredCallstackReading();
	
	/**
	 * Indicates whether this model is reading callstacks on demand 
	 * after the initial parsing phase has finished
	 * @param value
	 */
	public void setDeferredCallstackReading(boolean value);
	
	/**
	 * Setter for the ICallstackManager
	 * @param callstackManager the CallstackManager to set
	 */
	public void setCallstackManager(ICallstackManager callstackManager);
	
	/**
	 * Getter for ICallstackManager. 
	 * @return CallstackManager
	 */
	public ICallstackManager getCallstackManager();
		
}
