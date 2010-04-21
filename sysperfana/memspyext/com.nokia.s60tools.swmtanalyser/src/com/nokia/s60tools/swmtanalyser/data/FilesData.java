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

public class FilesData {
	private String fileName;
	private String threadName;
	private long fileSize;
	private int status;
	
	public void setFileName(String fileName){
		this.fileName = fileName;
	}
	
	public void setFileSize(String fileSize){
		this.fileSize = Long.parseLong(fileSize);
	}
	
	public void setThreadName(String threadName){
		this.threadName = threadName;
	}

	/**
	 * @param status
	 * This method sets the status of this file to New, Alive or Deleted
	 */
	public void setStatus(String status)
	{
		if(status.equals("[N]+[A]"))
			this.status = CycleData.New;
		else if (status.equals("[A]"))
				this.status = CycleData.Alive;
		else
			this.status = CycleData.Deleted;
	}
	
	public int getStatus()
	{
		return status;
	}
	
	public String getThreadName(){
		return threadName;
	}

	public String getFileName() {
		return fileName;
	}

	public long getFileSize() {
		return fileSize;
	}
	
}
