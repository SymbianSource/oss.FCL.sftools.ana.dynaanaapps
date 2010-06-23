/*
 * Copyright (c) 2007-2010 Nokia Corporation and/or its subsidiary(-ies). 
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
 * BTrace information contains BTrace variables
 *
 */
package com.nokia.traceviewer.engine;

/**
 * BTrace information contains BTrace variables
 */
public class BTraceInformation {

	/**
	 * Size of the record in bytes. (Maximum value is KMaxBTraceRecordSize.)
	 */
	private byte recordSize;

	/**
	 * Flags.
	 */
	private byte flags;

	/**
	 * Category. Category value from enum BTrace::TCategory.
	 */
	private byte category;

	/**
	 * Sub-category. The meaning of this is dependant on the value of Category.
	 */
	private byte subCategory;

	/**
	 * Tells if the trace has header 2 present
	 */
	private boolean header2Present;

	/**
	 * Tells if the trace has BTrace timestamp present
	 */
	private boolean timestampPresent;

	/**
	 * Tells if the trace has BTrace timestamp 2 present
	 */
	private boolean timestamp2Present;

	/**
	 * Tells if the trace has Context ID (Thread ID) present
	 */
	private boolean contextIdPresent;

	/**
	 * Tells if the trace has Program counter present
	 */
	private boolean programCounterPresent;

	/**
	 * Tells if the trace has BTrace Extra field present
	 */
	private boolean extraPresent;

	/**
	 * Trace data for this record was truncated to keep the size within the
	 * maximum permissible
	 */
	private boolean traceTruncated;

	/**
	 * Indicates that trace record(s) before this one are missing. This can
	 * happen if the trace buffer was full when a trace output was attempted
	 */
	private boolean traceMissing;

	/**
	 * Tells if the trace is multipart trace. 0 = non multipart, 1 = First part,
	 * 2 = Middle part, 3 = Last part
	 */
	private int multiPartTrace;

	/**
	 * Contains multipart trace parts
	 */
	private MultiPartItem multiPartItem;

	/**
	 * CPU ID
	 */
	private int cpuId = -1;

	/**
	 * Timestamp value
	 */
	private int timestamp;

	/**
	 * Timestamp2 value
	 */
	private int timestamp2;

	/**
	 * Context ID
	 */
	private int contextId;

	/**
	 * Program counter value
	 */
	private int programCounter;

	/**
	 * Extra value
	 */
	private int extraValue;

	/**
	 * Sets record size
	 * 
	 * @param size
	 *            record size
	 */
	public void setRecordSize(byte size) {
		this.recordSize = size;
	}

	/**
	 * Gets this records size
	 * 
	 * @return record size
	 */
	public byte getRecordSize() {
		return recordSize;
	}

	/**
	 * Sets record flags
	 * 
	 * @param flags
	 *            the flags
	 */
	public void setFlags(byte flags) {
		this.flags = flags;
	}

	/**
	 * Gets this records flags
	 * 
	 * @return flags
	 */
	public byte getFlags() {
		return flags;
	}

	/**
	 * Sets record category
	 * 
	 * @param category
	 *            the category
	 */
	public void setCategory(byte category) {
		this.category = category;
	}

	/**
	 * Gets this records category
	 * 
	 * @return category
	 */
	public byte getCategory() {
		return category;
	}

	/**
	 * Sets record sub-category
	 * 
	 * @param subCategory
	 *            the sub category
	 */
	public void setSubCategory(byte subCategory) {
		this.subCategory = subCategory;
	}

	/**
	 * Gets this records sub-category
	 * 
	 * @return sub Category
	 */
	public byte getSubCategory() {
		return subCategory;
	}

	/**
	 * Sets header2 present
	 * 
	 * @param present
	 *            new value of header2 present
	 */
	public void setHeader2Present(boolean present) {
		this.header2Present = present;
	}

	/**
	 * Tells if header2 is present
	 * 
	 * @return true if header2 is present, false otherwise
	 */
	public boolean isHeader2Present() {
		return header2Present;
	}

	/**
	 * Sets timestamp present
	 * 
	 * @param present
	 *            new value of timestamp present
	 */
	public void setTimestampPresent(boolean present) {
		this.timestampPresent = present;
	}

	/**
	 * Tells if timestamp is present
	 * 
	 * @return true if timestamp is present, false otherwise
	 */
	public boolean isTimestampPresent() {
		return timestampPresent;
	}

	/**
	 * Sets timestamp2 present
	 * 
	 * @param present
	 *            new value of timestamp2 present
	 */
	public void setTimestamp2Present(boolean present) {
		this.timestamp2Present = present;
	}

	/**
	 * Tells if timestamp2 is present
	 * 
	 * @return true if timestamp2 is present, false otherwise
	 */
	public boolean isTimestamp2Present() {
		return timestamp2Present;
	}

	/**
	 * Sets context ID present
	 * 
	 * @param present
	 *            new value of context ID present
	 */
	public void setContextIdPresent(boolean present) {
		this.contextIdPresent = present;
	}

	/**
	 * Tells if context ID is present
	 * 
	 * @return true if context ID is present, false otherwise
	 */
	public boolean isContextIdPresent() {
		return contextIdPresent;
	}

	/**
	 * Sets program counter present
	 * 
	 * @param present
	 *            new value of program counter present
	 */
	public void setProgramCounterPresent(boolean present) {
		this.programCounterPresent = present;
	}

	/**
	 * Tells if program counter is present
	 * 
	 * @return true if program counter is present, false otherwise
	 */
	public boolean isProgramCounterPresent() {
		return programCounterPresent;
	}

	/**
	 * Sets extra value present
	 * 
	 * @param present
	 *            new value of extra value present
	 */
	public void setExtraValuePresent(boolean present) {
		this.extraPresent = present;
	}

	/**
	 * Tells if extra value is present
	 * 
	 * @return true if extra value is present, false otherwise
	 */
	public boolean isExtraValuePresent() {
		return extraPresent;
	}

	/**
	 * Gets the CPU ID
	 * 
	 * @return the CPU Id
	 */
	public int getCpuId() {
		return cpuId;
	}

	/**
	 * Sets the CPU ID
	 * 
	 * @param cpuId
	 *            the cpuId to set
	 */
	public void setCpuId(int cpuId) {
		this.cpuId = cpuId;
	}

	/**
	 * Gets the timestamp value
	 * 
	 * @return the timestamp value
	 */
	public int getTimestamp() {
		return timestamp;
	}

	/**
	 * Sets the timestamp value
	 * 
	 * @param timestamp
	 *            timestamp value
	 */
	public void setTimestamp(int timestamp) {
		this.timestamp = timestamp;
	}

	/**
	 * Gets the timestamp2 value
	 * 
	 * @return the timestamp2 value
	 */
	public int getTimestamp2() {
		return timestamp2;
	}

	/**
	 * Sets the timestamp2 value
	 * 
	 * @param timestamp2
	 *            timestamp2 value
	 */
	public void setTimestamp2(int timestamp2) {
		this.timestamp2 = timestamp2;
	}

	/**
	 * Gets the thread ID
	 * 
	 * @return the thread Id
	 */
	public int getThreadId() {
		return contextId;
	}

	/**
	 * Sets the Thread ID
	 * 
	 * @param threadId
	 *            the threadId to set
	 */
	public void setThreadId(int threadId) {
		this.contextId = threadId;
	}

	/**
	 * Gets the program counter
	 * 
	 * @return the program counter
	 */
	public int getProgramCounter() {
		return programCounter;
	}

	/**
	 * Sets the program counter
	 * 
	 * @param programCounter
	 *            the program counter to set
	 */
	public void setProgramCounter(int programCounter) {
		this.programCounter = programCounter;
	}

	/**
	 * Gets the extra value
	 * 
	 * @return the extra value
	 */
	public int getExtraValue() {
		return extraValue;
	}

	/**
	 * Sets the extra value
	 * 
	 * @param extraValue
	 *            the extra value to set
	 */
	public void setExtraValue(int extraValue) {
		this.extraValue = extraValue;
	}

	/**
	 * Sets the multipart value
	 * 
	 * @param multiPartTrace
	 *            the multiPart value to set
	 */
	public void setMultiPart(int multiPartTrace) {
		this.multiPartTrace = multiPartTrace;
	}

	/**
	 * Tells if the trace is multipart trace
	 * 
	 * @return 0 = non multipart, 1 = First part, 2 = Middle part, 3 = Last part
	 */
	public int getMultiPart() {
		return multiPartTrace;
	}

	/**
	 * Sets the multipart trace parts
	 * 
	 * @param multiPartItem
	 *            the multiPart item
	 */
	public void setMultiPartTraceParts(MultiPartItem multiPartItem) {
		this.multiPartItem = multiPartItem;
	}

	/**
	 * Gets the multipart trace parts
	 * 
	 * @return multipart trace parts
	 */
	public MultiPartItem getMultiPartTraceParts() {
		return multiPartItem;
	}

	/**
	 * Sets the truncated value
	 * 
	 * @param truncated
	 *            the truncated value to set
	 */
	public void setTruncated(boolean truncated) {
		this.traceTruncated = truncated;
	}

	/**
	 * Tells if the trace is truncated trace
	 * 
	 * @return true if trace is truncated, false otherwise
	 */
	public boolean isTruncated() {
		return traceTruncated;
	}

	/**
	 * Sets the trace missing value
	 * 
	 * @param missing
	 *            the missing value to set
	 */
	public void setTraceMissing(boolean missing) {
		this.traceMissing = missing;
	}

	/**
	 * Tells if the trace before this one is missing
	 * 
	 * @return true if trace before this one is missing, false otherwise
	 */
	public boolean isTraceMissing() {
		return traceMissing;
	}

	/**
	 * Indicates if this trace has BTrace information that needs to be displayed
	 * in it (meaning Thread ID and CPU ID at this time)
	 * 
	 * @return true if there is BTrace information that needs to be displayed in
	 *         this trace
	 */
	public boolean hasInformation() {
		return (contextId != 0 || cpuId != -1);
	}
}
