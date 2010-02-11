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
	 * @return
	 */
	public ProcessInfo getSelectedProcess();

	/**
	 * get the start time of the process that started first
	 * @return time
	 */
	public Long getFirstProcessTime();

	/**
	 * get end time of the process that ended last
	 * @return
	 */
	public Long getLastProcessTime();

	/**
	 * get get first point time from all processes
	 * @return time
	 */
	public Long getFirstMemOpTime();

	/**
	 * get last point time from all processes
	 * @return time
	 */
	public Long getLastMemOpTime();

	/**
	 * get highest cumulated memory
	 * @return size
	 */
	public int getHighestCumulatedMemoryAlloc();

	/**
	 * get list of processes
	 * @return processes
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
	
}
