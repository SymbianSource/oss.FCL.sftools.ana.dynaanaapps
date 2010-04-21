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
 * Description:  Definitions for the class FreeInfo
 *
 */

package com.nokia.s60tools.analyzetool.engine.statistic;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;



/**
 * Constains information of free
 * @author kihe
 *
 */
public class FreeInfo extends BaseInfo {
	
	/**
	 * Constructor
	 * @param memoryAddress memory address for this free
	 */
	public FreeInfo(String memoryAddress) {
		super(memoryAddress);
	}

	AllocInfo[] allocsFreedArr;
	
	/**
	 * Sets all AllocInfos which were freed by this free
	 * @param allocs The AllocInfos to set
	 */
	public void setFreedAllocs(Set<AllocInfo> allocs){
		allocsFreedArr = allocs.toArray(new AllocInfo[allocs.size()]);
	}
	
	/**
	 * Returns a collection of allocs freed by this Free
	 * @return the Collection of AllocInfos freed
	 */
	public Collection<AllocInfo> getFreedAllocs(){
		return allocsFreedArr == null ? Collections.<AllocInfo>emptyList() : Arrays.asList(allocsFreedArr);
	}

	@Override
	public String toString() {
		return String.format("FreeInfo [%s allocsFreedArr=[%s]]", super.toString(), allocsFreedArrToString());
	}

	private String allocsFreedArrToString() {
		if (allocsFreedArr == null){
			return "null";
		}
		
		StringBuilder sb = new StringBuilder();
		boolean addComma = false;
		for (AllocInfo allocInfo : allocsFreedArr) {
			if (addComma){
				sb.append(", ");
			}
			addComma = true;
			sb.append(String.format("addr=0x%08X time=%s", allocInfo.getMemoryAddress(), allocInfo.getTime()));
		}
		return sb.toString();
	}
	
}
