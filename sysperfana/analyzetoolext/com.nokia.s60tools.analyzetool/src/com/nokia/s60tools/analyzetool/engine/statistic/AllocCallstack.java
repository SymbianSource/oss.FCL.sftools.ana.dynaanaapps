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
 * Description:  Definitions for the class AllocCallstack
 *
 */
package com.nokia.s60tools.analyzetool.engine.statistic;

/**
 * Contains information of allocation callstack item.
 * @author kihe
 *
 */
public class AllocCallstack {

	/** Callstack memory address */
	Long memoryAddress;

	/** Dll name where this callstack belongs*/
	DllLoad dllLoad;

	/**
	 * Returns memory address.
	 * @return Memory address
	 */
	public Long getMemoryAddress() {
		return memoryAddress;
	}

	/**
	 * Sets memory address
	 * @param newMemoryAddress Memory address
	 */
	public void setMemoryAddress(String newMemoryAddress) {
		Long iValue = Long.parseLong(newMemoryAddress,16);
		this.memoryAddress = iValue;
	}

	/**
	 * Set dll load item for current allocation callstack item
	 * @param loadItem Dll load item
	 */
	public void setDllLoad(DllLoad loadItem)
	{
		dllLoad = loadItem;
	}

	/**
	 * Returns Dll load item
	 * @return Dll load item
	 */
	public DllLoad getDllLoad()
	{
		return dllLoad;
	}
}
