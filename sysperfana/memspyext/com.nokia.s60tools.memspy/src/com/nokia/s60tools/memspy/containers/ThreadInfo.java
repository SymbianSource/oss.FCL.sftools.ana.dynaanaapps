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



package com.nokia.s60tools.memspy.containers;

import java.util.Date;




/**
 * threadInfo
 * thread info from one thread
 */
public class ThreadInfo {

	/**
	 * Enum about what type the log is
	 */
	public enum HeapDumpType{ DEVICE, FILE };
	private HeapDumpType type;
	
	// Name of the thread
	private String threadName;
	
	// ID-number of thread
	private String threadID;
	
	// File path of file
	private String threadFilePath;
	
	// Date when file has been imported.
	private Date date;
	
	// Status when heap from thread has been imported,
	// or "-" if it has not been imported.
	private String status;


	/**
	 * ThreadInfo
	 * constructor
	 */
	public ThreadInfo(){
		threadName = "";
		threadID = "";
		threadFilePath = "";
		date = new Date();
		status = "-";
	}
	
	/**
	 * Private constructor for clone method. Status is set as empty.
	 * @param threadName Name of the thread
	 * @param threadID ID-number of thread
	 * @param threadFilePath File path of file
	 * @param date Date when file has been imported.
	 * @param type Enum of which type info.
	 */
	private ThreadInfo(String threadName, String threadID, String threadFilePath, Date date, HeapDumpType type){
		this.threadName = threadName;
		this.threadID = threadID;
		this.threadFilePath = threadFilePath;
		this.date = date;
		this.type = type;

		this.status = "-";
		}
	
	/**
	 * Clone this object.
	 * @return New {@link ThreadInfo} object made from this ThreadInfo. Status is as default value.
	 */
	public ThreadInfo clone() {
		ThreadInfo clone = new ThreadInfo(threadName, threadID, threadFilePath, date, type);
		return clone;
	}
	
	
	/**
	 * Get thread name
	 * @return thread name
	 */
	public String getThreadName() {
		return threadName;
	}

	/**
	 * Set thread name
	 * @param threadName
	 */
	public void setThreadName(String threadName) {
		this.threadName = threadName;
	}

	/**
	 * Get thread id
	 * @return thread id
	 */
	public String getThreadID() {
		return threadID;
	}

	/**
	 * Set thread id
	 * @param threadID
	 */
	public void setThreadID(String threadID) {
		this.threadID = threadID;
	}

	/**
	 * Get date
	 * @return date
	 */
	public Date getDate() {
		return date;
	}

	/**
	 * Set date when log was created
	 * @param date
	 */
	public void setDate(Date date) {
		this.date = date;
	}

	/**
	 * Get absolute file path where log file is located
	 * @return file path
	 */
	public String getThreadFilePath() {
		return threadFilePath;
	}

	/**
	 * Set absolute file path where log file is located
	 * @param threadFilePath
	 */
	public void setThreadFilePath(String threadFilePath) {
		this.threadFilePath = threadFilePath;
	}

	/**
	 * Get the type of this log
	 * @return type
	 */
	public HeapDumpType getType() {
		return type;
	}

	/**
	 * Set the type of this log
	 * @param type
	 */
	public void setType(HeapDumpType type) {
		this.type = type;
	}
	
	/**
	 * Get status
	 * @return status
	 */
	public String getStatus() {
		return status;
	}
	
	/**
	 * Set status
	 * @param status
	 */
	public void setStatus(String status) {
		this.status = status;
	}
}
