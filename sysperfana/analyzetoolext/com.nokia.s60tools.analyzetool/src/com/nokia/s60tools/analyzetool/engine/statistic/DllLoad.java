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
 * Description:  Definitions for the class DllLoad
 *
 */

package com.nokia.s60tools.analyzetool.engine.statistic;

/**
 * Contains information of dll loads
 *
 * @author kihe
 *
 */
public class DllLoad {

	/** Dll load end address */
	private Long endAddress;

	/** Time when this dll is loaded. */
	private long loadTime;
	/** Time when this dll is unloaded */
	private long unloadTime = Long.MAX_VALUE;

	/** Name of the dll */
	private String name;

	/** Process ID of the dll load */
	private int processID;

	/** Dll load start address */
	private Long startAddress;

	/**
	 * Returns dll load end address
	 *
	 * @return Dll load end address
	 */
	public Long getEndAddress() {
		return endAddress;
	}

	/**
	 * Returns dll item load time
	 *
	 * @return Time when this dll item is loaded
	 */
	public long getLoadTime() {
		return loadTime;
	}

	/**
	 * Returns unload time of the dll
	 *
	 * @return Time when this dll item is unloaded
	 */
	public long getUnloadTime() {
		return unloadTime;
	}

	/**
	 * Returns name of the dll load
	 *
	 * @return Dll load name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Returns dll load process id
	 *
	 * @return Dll load process id
	 */
	public int getProcessID() {
		return processID;
	}

	/**
	 * Returns dll load start address
	 *
	 * @return Dll load start address
	 */
	public Long getStartAddress() {
		return startAddress;
	}

	/**
	 * Sets dll load end address
	 *
	 * @param newEndAddress
	 *            Dll load end address
	 */
	public void setEndAddress(String newEndAddress) {
		Long lValue = Long.parseLong(newEndAddress, 16);
		this.endAddress = lValue;
	}

	/**
	 * Set load time of this item
	 *
	 * @param newLoadTime
	 *            Time when this dll is loaded.
	 */
	public void setLoadTime(String newLoadTime) {
		Long lValue = Long.parseLong(newLoadTime, 16);
		this.loadTime = lValue;
	}
	/**
	 * Set load time of this item
	 *
	 * @param loadTime
	 *            Time when this dll is loaded.
	 */
	public void setLoadTime(long loadTime) {
		this.loadTime = loadTime;
	}
	/**
	 * Set unload time of this dll
	 *
	 * @param unloadTime
	 *            Time when this dll is unloaded.
	 */
	public void setUnloadTime(long unloadTime) {
		this.unloadTime = unloadTime;
	}

	/**
	 * Sets dll load name
	 *
	 * @param newName
	 *            Dll load name
	 */
	public void setName(String newName) {
		this.name = newName;
	}

	/**
	 * Sets dll load process id
	 *
	 * @param newProcessID
	 *            Dll load process id
	 */
	public void setProcessID(String newProcessID) {
		Integer lValue = Integer.parseInt(newProcessID, 16);
		this.processID = lValue;
	}

	/**
	 * Sets dll load start address
	 *
	 * @param newStartAddress
	 *            Dll load start address
	 */
	public void setStartAddress(String newStartAddress) {
		Long lValue = Long.parseLong(newStartAddress, 16);
		this.startAddress = lValue;
	}

	@Override
	public String toString() {
		return String
				.format(
						"DllLoad [name=%s, processID=%d, startAddress=0x%08X, endAddress=0x%08X, loadTime=%s, unloadTime=%s]",
						name, processID, startAddress, endAddress, loadTime, unloadTime);
	}

}
