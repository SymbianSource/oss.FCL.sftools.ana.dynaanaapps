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
 * Stores HGEN information
 *
 */
public class KernelHandles {

	private String handleName;
	private String handle;
	private String handleType;
	private int attrib;
	
	public void setHandleName(String name)	{
		this.handleName = name;
	}
	public void setHandle(String handle){
		this.handle = handle;
	}
	public void setHandleType(String handleType){
		this.handleType = handleType;
	}
	
	/**
	 * @param status
	 * This method sets the status of this handle to New, Alive or Deleted
	 */
	public void setStatus(String status)
	{
		if(status.equals("[N]+[A]"))
			attrib = CycleData.New;
		else if (status.equals("[A]"))
				attrib = CycleData.Alive;
		else
			attrib = CycleData.Deleted;
	}
	
	public int getStatus(){
		return attrib;
	}
	
	public String getHandleType(){
		return handleType;
	}
	
	public String getHandleName(){
		return handleName;
	}
	public String getHandle() {
		return handle;
	}
}
