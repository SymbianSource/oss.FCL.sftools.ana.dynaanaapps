/*
* Copyright (c) 2009 Nokia Corporation and/or its subsidiary(-ies).
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
* Description: 
*
*/
package com.nokia.s60tools.swmtanalyser.data;

/**
 * Stores chunk related data.
 *
 */
public class ChunksData {
	private String processName;
	private String chunkName;
	private String handle;
	private String baseAddr;
	private long size = -1;
	private int attrib;
	private boolean isKernelHandleDeleted;
	
	public ChunksData() {}
	
	public ChunksData(ChunksData target)
	{
		this.chunkName = target.chunkName;
		this.processName = target.processName;
		this.baseAddr = target.baseAddr;
		this.size = target.size;
		this.attrib = target.attrib;
	}
	
	public int getAttrib() {
		return attrib;
	}
	public void setAttrib(int attrib) {
		this.attrib = attrib;
	}
	public String getBaseAddr() {
		return baseAddr;
	}
	public void setBaseAddr(String baseAddr) {
		this.baseAddr = baseAddr;
	}
	public String getChunkName() {
		return chunkName;
	}
	public void setChunkName(String chunkName) {
		this.chunkName = chunkName;
	}
	public String getHandle() {
		return handle;
	}
	public void setHandle(String handle) {
		this.handle = handle;
	}
	public String getProcessName() {
		return processName;
	}
	public void setProcessName(String processName) {
		this.processName = processName;
	}
	public long getSize() {
		return size;
	}
	public void setSize(long size) {
		this.size = size;
	}

	public boolean isKernelHandleDeleted() {
		return isKernelHandleDeleted;
	}

	public void setKernelHandleDeleted(boolean isKernelHandleDeleted) {
		this.isKernelHandleDeleted = isKernelHandleDeleted;
	}
}
