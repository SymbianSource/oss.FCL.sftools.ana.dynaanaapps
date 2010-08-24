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
	long memoryAddress;

	/** DLL name where this callstack belongs*/
	DllLoad dllLoad;

	/**
	 * Constructor
	 * @param newMemoryAddress the memory address for this allocation
	 */
	public AllocCallstack(String newMemoryAddress) {
		this.memoryAddress = Long.parseLong(newMemoryAddress,16);
	}

	/**
	 * Returns memory address as decimal value.
	 * @return Memory address
	 */
	public long getMemoryAddress() {
		return memoryAddress;
	}

	/**
	 * Set DLL load item for current allocation callstack item
	 * @param loadItem DLL load item
	 */
	public void setDllLoad(DllLoad loadItem)
	{
		dllLoad = loadItem;
	}

	/**
	 * Returns DLL load item
	 * @return DLL load item
	 */
	public DllLoad getDllLoad()
	{
		return dllLoad;
	}
}
