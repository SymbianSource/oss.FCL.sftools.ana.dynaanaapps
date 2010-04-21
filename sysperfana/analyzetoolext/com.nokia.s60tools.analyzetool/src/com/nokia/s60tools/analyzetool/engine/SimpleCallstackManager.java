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
 * This class supports the "old" way of reading callstacks from BaseInfo directly.
 * This requires that callstack information is filled in during the initial parsing phase
 *
 */
public class SimpleCallstackManager implements ICallstackManager{

	/* (non-Javadoc)
	 * @see com.nokia.s60tools.analyzetool.engine.statistic.ICallstackManager#readCallstack(com.nokia.s60tools.analyzetool.engine.statistic.BaseInfo)
	 */
	public List<AllocCallstack> readCallstack(BaseInfo baseInfo)
			throws IOException {
		return baseInfo.getCallstack();
	}

	/* (non-Javadoc)
	 * @see com.nokia.s60tools.analyzetool.engine.statistic.ICallstackManager#hasCallstack(com.nokia.s60tools.analyzetool.engine.statistic.BaseInfo)
	 */
	public boolean hasCallstack(BaseInfo baseInfo) {
		return baseInfo.getCallstack() != null && baseInfo.getCallstack().size() > 0;
	}

}
