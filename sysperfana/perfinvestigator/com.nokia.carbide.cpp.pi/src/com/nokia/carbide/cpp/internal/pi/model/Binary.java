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

public class Binary implements Serializable
{
  private static final long serialVersionUID = 3278912646658026030L;
	
  public long startAddress;
  public int length;
  public String binaryName;
  public long offsetToCodeStart = 0;
  public String type;
  
  public Binary(String binaryName)
  {
    this.binaryName = binaryName;
  }
  
  public long findOverlapLentghWith(Binary other)
  {
	long cSt = other.startAddress;
	long cEn = other.startAddress+other.length;
	long bSt = this.startAddress;
	long bEn = this.startAddress+this.length;
	long overLap = 0;
	
	if (bSt >= cSt && bSt <= cEn)
	{
		// starts within the current bin
		if (bEn <= cEn)
		{
			// also ends within the current bin
			overLap = this.length;
		}
		else
		{
			// ends outside the current 
			// binary but starts within it
			overLap = cEn-bSt;
		}
		
	}
	else if (bEn >= cSt && bEn <= cEn)
	{
		// ends into the current bin
		// but starts before it
		overLap = bEn - cSt;
	}
	
	else if (bSt <= cSt && bEn >= cSt)
	{
		// starts before and ends after
		// the current bin
		overLap = other.length;
	}
	
	return overLap;
  }

}
