/*
 * Copyright (c) 2010 Nokia Corporation and/or its subsidiary(-ies). 
 * All rights reserved.
 * This component and the accompanying materials are made available
 * under the terms of the License "Eclipse Public License v1.0"
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
 */
package com.nokia.carbide.cpp.internal.pi.model;

public interface ICPUScale {
	
	/**
	 * Calculate a percentage value (0 - 1) of the CPU clock at given time
	 * 
	 * @param sampleSynchTime
	 * @return the calculated value
	 */
	public float calculateScale(int sampleSynchTime);
	
	/**
	 * Calculate a percentage value (0 - 1) of the CPU clock at given time period
	 * 
	 * @param startSynchTime
	 * @param endSynchTime
	 * @return the calculated value
	 */
	public float calculateScale(int startSynchTime, int endSynchTime);
	
	/**
	 * Check whether CPU scale supported or not
	 * 
	 * @return <code>true</code> is returned if CPU scale is supported or
	 *         <code>false</code> if is not
	 */
	public boolean isCpuScaleSupported();
}
