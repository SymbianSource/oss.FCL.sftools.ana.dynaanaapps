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

public class Function implements Serializable
{
  private static final long serialVersionUID = -3261268206319782647L;
	
  public Binary functionBinary;
  public Long startAddress;
  public long offsetFromBinaryStart;
  public long length;
  
  public String functionName;

  public Function(String functionName, Long functionStart, String functionBinary)
  {
    this.functionName = functionName;
    this.startAddress = functionStart;
    this.functionBinary = new Binary(functionBinary);
  }
  
  public String toString()
  {
  	return this.functionName+" @"+Long.toHexString(this.startAddress.intValue()); //$NON-NLS-1$
  }

}
