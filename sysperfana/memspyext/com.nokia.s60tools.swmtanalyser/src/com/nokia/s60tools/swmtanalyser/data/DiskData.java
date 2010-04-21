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

public class DiskData {
   private String driveName;
   private long size;
   private long freeSize;
   private int status;
   
   public void setName(String name)
   {
	   this.driveName = name;
   }
   public void setSize(String size)
   {
	   this.size = Long.parseLong(size);
   }
   
   public void setFreeSize(String freeSize)
   {
	   this.freeSize = Long.parseLong(freeSize);
   }
   
   public String getName()
   {
	   return driveName;
   }
   
   /**
	 * @param status
	 * This method sets the status of this handle to New, Alive or Deleted
	 */
	public void setStatus(String status)
	{
		if(status.equals("[N]+[A]"))
			this.status = CycleData.New;
		else if (status.equals("[A]"))
				this.status = CycleData.Alive;
		else
			this.status = CycleData.Deleted;
	}
	public int getStatus()
	{
		return status;
	}
	
	public long getFreeSize()
	{
		return this.freeSize;
	}
	
	public long getSize()
	{
		return this.size;
		
	}
}
