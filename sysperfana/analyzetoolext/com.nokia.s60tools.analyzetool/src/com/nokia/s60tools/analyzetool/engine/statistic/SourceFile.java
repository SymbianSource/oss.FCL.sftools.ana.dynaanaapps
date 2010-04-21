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
 * Description:  Definitions for the class SourceFile
 *
 */

package com.nokia.s60tools.analyzetool.engine.statistic;

/**
 * Constains information of found source file.
 * @author kihe
 *
 */
public class SourceFile {

	/** Source file name.*/
	private String fileName;

	/** Source file line number.*/
	private int lineNumber;

	/** How many times this source file is allocated.*/
	private int howManyTimes = 1;

	/** Source file function name. */
	private String functionName;

	/** Summary of allocations size.*/
	private int size;

	/** Allocation time.*/
	private Long time;

	/**
	 * Returns source file name.
	 * @return Source file name
	 */
	public String getFileName() {
		return fileName;
	}

	/**
	 * Sets source file name.
	 * @param newFileName Source file name
	 */
	public void setFileName(String newFileName) {
		this.fileName = newFileName;
	}

	/**
	 * Returns source file line number.
	 * @return Source file line number
	 */
	public int getLineNumber() {
		return lineNumber;
	}

	/**
	 * Sets source file line number
	 * @param newLineNumber Line number
	 */
	public void setLineNumber(int newLineNumber) {
		this.lineNumber = newLineNumber;
	}

	/**
	 * Returns count of how many times this source file is allocated.
	 * @return Count of allocations
	 */
	public int getHowManyTimes() {
		return howManyTimes;
	}

	/**
	 * Sets count of how many times this source file is allocated.
	 * @param count Count of allocations
	 */
	public void setHowManyTimes(int count) {
		this.howManyTimes = count;
	}

	/**
	 * Updates count of allocations
	 */
	public void updateHowManyTimes()
	{
		this.howManyTimes +=1;
	}

	/**
	 * Returns function name.
	 * @return Function name
	 */
	public String getFunctionName() {
		return functionName;
	}

	/**
	 * Sets function name.
	 * @param newFunctionName Function name
	 */
	public void setFunctionName(String newFunctionName) {
		this.functionName = newFunctionName;
	}

	/**
	 * Returns size of allocations.
	 * @return Size of allocations
	 */
	public int getSize() {
		return size;
	}

	/**
	 * Updates allocation count memory usage size.
	 * @param newSize Memory usage size
	 */
	public void updateSize(int newSize){
		this.size += newSize;
	}

	/**
	 * Returns time of allocation.
	 * @return Time of allocation
	 */
	public Long getTime() {
		return time;
	}

	/**
	 * Sets time of allocation
	 * @param newTime Time of allocation
	 */
	public void setTime(Long newTime) {
		this.time = newTime;
	}
}
