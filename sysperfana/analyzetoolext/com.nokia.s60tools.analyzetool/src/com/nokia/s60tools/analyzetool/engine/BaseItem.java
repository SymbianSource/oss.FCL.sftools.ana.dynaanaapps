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
 * Description:  Definitions for the class BaseItem
 *
 */

package com.nokia.s60tools.analyzetool.engine;

/**
 * Base class for {@link AnalysisItem} and {@link CallstackItem} Provides
 * methods to store basic information of one item
 *
 * Information is parsed from atool.exe generated XML file so we can assume that
 * all the information is valid and no other checking is needed.
 *
 * @author kihe
 *
 */
public class BaseItem {

	/** Module name. */
	private String moduleName;

	/** Memory leak time. */
	private String memoryLeakTime;

	/** Memory item memory address. */
	private String memoryAddress;

	/** item ID. */
	private int itemID = 0;

	/**
	 * Gets Callstack item ID.
	 *
	 * @return Callstack item ID
	 */
	public final int getID() {
		return itemID;
	}

	/**
	 * Returns current item memory address.
	 *
	 * @return Memory address
	 */
	public final String getMemoryAddress() {
		if (memoryAddress == null) {
			return "";
		}
		return memoryAddress;
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
	 * Gets module name.
	 *
	 * @return Module name
	 */
	public final String getModuleName() {
		if (moduleName == null) {
			return "";
		}
		return this.moduleName;
	}

	/**
	 * Sets Callstack item ID.
	 *
	 * @param newID
	 *            New callstack item id value
	 */
	public final void setID(final int newID) {
		itemID = newID;
	}

	/**
	 * Set current item memory address.
	 *
	 * @param newMemoryAddress
	 *            Item memory address
	 */
	public final void setMemoryAddress(final String newMemoryAddress) {
		memoryAddress = newMemoryAddress;
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

	/**
	 * Sets module name.
	 *
	 * @param newModuleName
	 *            Module name
	 */
	public final void setModuleName(final String newModuleName) {
		this.moduleName = newModuleName;
	}

	/**
	 * Checks that at least one needed information is available
	 *
	 * @return True if at least one needed information is available otherwise
	 *         False
	 */
	protected boolean checkData() {
		if ((moduleName == null || ("").equals(moduleName))
				&& (memoryLeakTime == null || ("").equals(memoryLeakTime))
				&& (memoryAddress == null || ("").equals(memoryAddress))) {
			return false;
		}
		return true;
	}
}
