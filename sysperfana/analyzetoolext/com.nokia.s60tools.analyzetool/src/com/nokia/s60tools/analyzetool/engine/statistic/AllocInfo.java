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

	/**
	 * Constructor
	 * @param memoryAddress memory address for this allocation
	 */
	public AllocInfo(String memoryAddress) {
		super(memoryAddress);
	}

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

	@Override
	public String toString() {
		return String.format("AllocInfo [%s freedBy=[%s]]", super.toString(), freedByToString());
	}
	
	private String freedByToString(){
		if (freedBy == null){
			return "null";
		}
		
		return String.format("addr=0x%08X time=%s", freedBy.getMemoryAddress(), freedBy.getTime());
	}
}
