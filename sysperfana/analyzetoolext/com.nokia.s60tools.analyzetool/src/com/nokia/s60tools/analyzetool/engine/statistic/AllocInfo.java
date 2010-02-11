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
 * Description:  Definitions for the class AllocInfo
 *
 */
package com.nokia.s60tools.analyzetool.engine.statistic;



/**
 * Class holds information of one memory allocation
 * @author kihe
 *
 */
public class AllocInfo extends BaseInfo {

	private FreeInfo freedBy = null;

	/**
	 * set this allocation as freed by the provide FreeInfo
	 * @param info
	 */
	public void setFreedBy(FreeInfo info) {
		this.freedBy  =info;
	}
	
	/**
	 * get the FreeInfo that freed this allocation
	 * @return a FreeInfo or null
	 */
	public FreeInfo getFreedBy() {
		return this.freedBy;
	}

	/**
	 * is this allocation freed
	 * @return boolean true/false
	 */
	public boolean isFreed() {
		return this.freedBy != null;
	}
}
