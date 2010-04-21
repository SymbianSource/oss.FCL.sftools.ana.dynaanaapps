/*
 * Copyright (c) 2009 Nokia Corporation and/or its subsidiary(-ies). 
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

import java.io.Serializable;

public class Binary implements IBinary, Serializable
{
  private static final long serialVersionUID = 3278912646658026030L;
	
  protected long startAddress;
  protected int length;
  protected String binaryName;
  protected long offsetToCodeStart = 0;
  protected String type;
  
  public Binary(String binaryName)
  {
    this.binaryName = binaryName;
  }
  
// un-used in code  
//  public long findOverlapLentghWith(Binary other)
//  {
//	long cSt = other.startAddress;
//	long cEn = other.startAddress+other.length;
//	long bSt = this.startAddress;
//	long bEn = this.startAddress+this.length;
//	long overLap = 0;
//	
//	if (bSt >= cSt && bSt <= cEn)
//	{
//		// starts within the current bin
//		if (bEn <= cEn)
//		{
//			// also ends within the current bin
//			overLap = this.length;
//		}
//		else
//		{
//			// ends outside the current 
//			// binary but starts within it
//			overLap = cEn-bSt;
//		}
//		
//	}
//	else if (bEn >= cSt && bEn <= cEn)
//	{
//		// ends into the current bin
//		// but starts before it
//		overLap = bEn - cSt;
//	}
//	
//	else if (bSt <= cSt && bEn >= cSt)
//	{
//		// starts before and ends after
//		// the current bin
//		overLap = other.length;
//	}
//	
//	return overLap;
//  }

	/* (non-Javadoc)
	 * @see com.nokia.carbide.cpp.internal.pi.model.IBinary#getStartAddress()
	 */
	public long getStartAddress() {
		return startAddress;
	}
	
	/**
	 * Setter for the start address
	 * @param startAddress the start address to set
	 */
	public void setStartAddress(long startAddress) {
		this.startAddress = startAddress;
	}
	
	/* (non-Javadoc)
	 * @see com.nokia.carbide.cpp.internal.pi.model.IBinary#getLength()
	 */
	public int getLength() {
		return length;
	}
	
	/**
	 * Setter for the length
	 * @param length the length to set
	 */
	public void setLength(int length) {
		this.length = length;
	}
	
	/* (non-Javadoc)
	 * @see com.nokia.carbide.cpp.internal.pi.model.IBinary#getBinaryName()
	 */
	public String getBinaryName() {
		return binaryName;
	}
	
	/**
	 * Setter for the name of the binary
	 * @param binaryName the binaryName to set
	 */
	public void setBinaryName(String binaryName) {
		this.binaryName = binaryName;
	}

	/* (non-Javadoc)
	 * @see com.nokia.carbide.cpp.internal.pi.model.IBinary#getOffsetToCodeStart()
	 */
	public long getOffsetToCodeStart() {
		return offsetToCodeStart;
	}
	
	/**
	 * Setter for the offset to code start
	 * @param offsetToCodeStart the offsetToCodeStart to set
	 */
	public void setOffsetToCodeStart(long offsetToCodeStart) {
		this.offsetToCodeStart = offsetToCodeStart;
	}
	
	/* (non-Javadoc)
	 * @see com.nokia.carbide.cpp.internal.pi.model.IBinary#getType()
	 */
	public String getType() {
		return type;
	}
	
	/**
	 * Setter for the type
	 * @param type the type to set
	 */
	public void setType(String type) {
		this.type = type;
	}

}
