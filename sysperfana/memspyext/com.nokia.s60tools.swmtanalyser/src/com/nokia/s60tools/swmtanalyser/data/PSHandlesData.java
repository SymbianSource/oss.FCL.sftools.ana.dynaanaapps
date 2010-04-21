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

/**
 * Stores HPAS data from the log file.
 *
 */
public class PSHandlesData {
	
	public static String [] keyTypes = new String [] {"EInt", "EByteArray", "EText", "ELargeByteArray","ELargeText", "ETypeLimit", "ETypeMask"};	
	
	private String handleName;
	private String handle;
	private long key;
	private int keyType;
	private long threadId;
	private String threadName;
	private int attrib;
	
	public int getStatus()
	{
		return attrib;
	}
	public String getHandle() {
		return handle;
	}
	public void setHandle(String handle) {
		this.handle = handle;
	}
	public String getHandleName() {
		return handleName;
	}
	public void setHandleName(String handleName) {
		this.handleName = handleName;
	}
	public long getKey() {
		return key;
	}
	public void setKey(long key) {
		this.key = key;
	}
	public int getKeyType() {
		return keyType;
	}
	public void setKeyType(int keyType) {
		this.keyType = keyType;
	}
	public long getThreadId() {
		return threadId;
	}
	public void setThreadId(long threadId) {
		this.threadId = threadId;
	}
	public String getThreadName() {
		return threadName;
	}
	public void setThreadName(String threadName) {
		this.threadName = threadName;
	}
	
	/**
	 * @param status
	 * This method sets the status of this handle to New, Alive or Deleted
	 */
	public void setStatus(String status)
	{
		if(status.equals("[N]+[A]"))
			attrib = CycleData.New;
		else if (status.equals("[A]"))
				attrib = CycleData.Alive;
		else
			attrib = CycleData.Deleted;
	}
}
