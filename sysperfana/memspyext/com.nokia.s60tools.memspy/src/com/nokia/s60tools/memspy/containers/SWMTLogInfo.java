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
 * 
 * class SWMTLogInfo
 * SWMTLogInfo class that contains info from one System Wide Memory Tracking log file or directory.
 *
 */
public class SWMTLogInfo {

	/**
	 * Enum about what type the log is
	 */
	public enum SWMTLogType{ DEVICE, FILE };
	private SWMTLogType type;

	// Path of file
	private String path;

	// Date when file has been imported.
	private Date date;
	
	/**
	 * SWMTLogInfo.
	 * constructor.
	 */
	public SWMTLogInfo(){
	}
	
	
	/**
	 * Get the log type
	 * @return type
	 */
	public SWMTLogType getType() {
		return type;
	}
	/**
	 * Set the log type
	 * @param type
	 */
	public void setType(SWMTLogType type) {
		this.type = type;
	}
	/**
	 * Get file path where log file is located
	 * @return path
	 */
	public String getPath() {
		return path;
	}
	/**
	 * Set file path where log file is located
	 * @param path
	 */
	public void setPath(String path) {
		this.path = path;
	}
	/**
	 * Get date when log file is created
	 * @return date
	 */
	public Date getDate() {
		return date;
	}
	/**
	 * Set date when log file is created
	 * @param date
	 */
	public void setDate(Date date) {
		this.date = date;
	}
	
	
	
}
