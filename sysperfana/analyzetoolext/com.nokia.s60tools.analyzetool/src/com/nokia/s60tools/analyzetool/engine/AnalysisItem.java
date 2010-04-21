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
 * Description:  Definitions for the class AnalysisItem
 *
 */

package com.nokia.s60tools.analyzetool.engine;

import java.util.AbstractList;
import java.util.ArrayList;

/**
 * Contains information of one memory leak item. Information is parsed from
 * atool.exe generated XML file so we can assume that all the information is
 * valid and no other checking is needed.
 * 
 * @author kihe
 * 
 */
public class AnalysisItem extends BaseItem {

	/** Call stack addresses. */
	private final AbstractList<CallstackItem> callstackItems;

	/** Size of leak. */
	private int leakSize = 0;

	/** Memory leak time. */
	private String memoryLeakTime;

	/**
	 * Constructor.
	 */
	public AnalysisItem() {
		super();
		callstackItems = new ArrayList<CallstackItem>();
	}

	/**
	 * Adds new Callstack item.
	 * 
	 * @param item
	 *            Callstack item
	 */
	public final void addCallstackItem(final CallstackItem item) {
		this.callstackItems.add(item);
	}

	/**
	 * Check if any stored callstack items has is pinpointed to file and line
	 * number.
	 * 
	 * @return True if any callstack item contains file name and leak line
	 *         number, otherwise False
	 */
	public final boolean containValidCallstack() {
		// thru stored callstack items
		final java.util.Iterator<CallstackItem> iterCallstack = callstackItems
				.iterator();
		while (iterCallstack.hasNext()) {
			// if one item contains valid filename and line number
			// this means that current callstack items can be pinpointed
			final CallstackItem oneItem = iterCallstack.next();
			if (!("").equals(oneItem.getFileName())
					&& oneItem.getLeakLineNumber() != 0) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Gets stored callstack items.
	 * 
	 * @return Callstack items
	 */
	public final AbstractList<CallstackItem> getCallstackItems() {
		return this.callstackItems;
	}

	/**
	 * Gets size of memory leak.
	 * 
	 * @return Memory leak size
	 */
	public final int getLeakSize() {
		return this.leakSize;
	}

	/**
	 * Gets memory leak time.
	 * 
	 * @return Memory leak time
	 */
	public final String getMemoryLeakTime() {
		if (memoryLeakTime == null) {
			return "";
		}
		return this.memoryLeakTime;
	}

	/**
	 * Sets size for the memory leak.
	 * 
	 * @param newSize
	 *            Memory leak size
	 */
	public final void setLeakSize(final int newSize) {
		this.leakSize = newSize;
	}

	/**
	 * Sets memory leak time.
	 * 
	 * @param newMemoryLeakTime
	 *            Memory leak time
	 */
	public final void setMemoryLeakTime(final String newMemoryLeakTime) {
		this.memoryLeakTime = newMemoryLeakTime;

	}
}
