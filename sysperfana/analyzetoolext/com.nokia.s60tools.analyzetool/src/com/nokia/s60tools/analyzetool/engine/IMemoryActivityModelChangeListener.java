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
 * Description:  Definitions for the class IMemoryActivityModelChangeListener
 *
 */
package com.nokia.s60tools.analyzetool.engine;

import com.nokia.s60tools.analyzetool.engine.statistic.ProcessInfo;

/**
 * 
 * A change listener for {@link IMemoryActivityModel}
 */
public interface IMemoryActivityModelChangeListener {

	/**
	 * callback when data has been added to the model and is ready to be used
	 */
	public void onProcessesAdded();

	/**
	 * callback when a process has been selected
	 * 
	 * @param processId
	 *            The ProcessInfo of the newly selected process
	 */
	public void onProcessSelected(ProcessInfo processId);
}
