/*
 * Copyright (c) 2010 Nokia Corporation and/or its subsidiary(-ies).
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
 * Description:  Definitions for the class ReadFile
 *
 */
package com.nokia.s60tools.analyzetool.engine;

import java.io.IOException;
import java.util.List;

import com.nokia.s60tools.analyzetool.engine.statistic.AllocCallstack;
import com.nokia.s60tools.analyzetool.engine.statistic.BaseInfo;

/**
 * Interface for access to callstack. This allows the transparent use of either the old-style saving
 * of callstacks in BaseInfo, or the newer deferred reading of callstacks.
 */
public interface ICallstackManager {
	
	/**
	 * Returns the callstack for the given BaseInfo. Note, this should not return null
	 * if hasCallstack() returns true.
	 * @param baseInfo  the BaseInfo to use
	 * @return the callstack for the given BaseInfo
	 * @throws IOException when problems accessing the .dat file for callstacks
	 */
	public List<AllocCallstack> readCallstack(BaseInfo baseInfo) throws IOException;

	/**
	 * Returns true if the given BaseInfo has a callstack, false otherwise
	 * @param baseInfo the BaseInfo to use
	 * @return true if callstack present
	 */
	public boolean hasCallstack(BaseInfo baseInfo);

}
