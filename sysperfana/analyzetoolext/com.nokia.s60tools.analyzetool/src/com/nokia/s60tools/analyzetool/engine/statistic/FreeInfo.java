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
 * Description:  Definitions for the class FreeInfo
 *
 */
package com.nokia.s60tools.analyzetool.engine.statistic;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;



/**
 * Constains information of free
 * @author kihe
 *
 */
public class FreeInfo extends BaseInfo {
	Set<AllocInfo> allocsFreed;

	/**
	 * Adds an alloc which has been freed by this Free 
	 * @param alloc the alloc to add
	 */
	public void addFreedAlloc(AllocInfo alloc){
		if (allocsFreed == null){
			allocsFreed = new HashSet<AllocInfo>();
		}
		allocsFreed.add(alloc);
	}
	
	/**
	 * Returns a collection of allocs freed by this Free
	 * @return the Collection of AllocInfos freed
	 */
	public Collection<AllocInfo> getFreedAllocs(){
		return Collections.unmodifiableSet(allocsFreed == null ? new HashSet<AllocInfo>() : allocsFreed);
	}
	
}
