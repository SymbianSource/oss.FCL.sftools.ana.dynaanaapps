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

public class Function implements IFunction, Serializable
{
  private static final long serialVersionUID = -3261268206319782647L;
	
  private IBinary functionBinary;
  private Long startAddress;
  private long offsetFromBinaryStart;
  private long length;
  
  private String functionName;

  public Function(String functionName, Long functionStart, String functionBinary)
  {
    this.functionName = functionName;
    this.startAddress = functionStart;
    this.functionBinary = new Binary(functionBinary);
  }
	  
	/* (non-Javadoc)
	 * @see com.nokia.carbide.cpp.internal.pi.model.IFunction#getFunctionBinary()
	 */
	public IBinary getFunctionBinary() {
		return functionBinary;
	}
	
	/**
	 * Setter for the binary this function is in  
	 * @param functionBinary the functionBinary to set
	 */
	public void setFunctionBinary(Binary functionBinary) {
		this.functionBinary = functionBinary;
	}

	/* (non-Javadoc)
	 * @see com.nokia.carbide.cpp.internal.pi.model.IFunction#getStartAddress()
	 */
	public Long getStartAddress() {
		return startAddress;
	}

	/**
	 * Setter for start address
	 * @param startAddress the start address to set
	 */
	public void setStartAddress(Long startAddress) {
		this.startAddress = startAddress;
	}

	/* (non-Javadoc)
	 * @see com.nokia.carbide.cpp.internal.pi.model.IFunction#getOffsetFromBinaryStart()
	 */
	public long getOffsetFromBinaryStart() {
		return offsetFromBinaryStart;
	}

	/* (non-Javadoc)
	 * @see com.nokia.carbide.cpp.internal.pi.model.IFunction#getLength()
	 */
	public long getLength() {
		return length;
	}

	/* (non-Javadoc)
	 * @see com.nokia.carbide.cpp.internal.pi.model.IFunction#getFunctionName()
	 */
	public String getFunctionName() {
		return functionName;
	}

	/** Setter for offset from binary start
	 * @param offsetFromBinaryStart the offset to set
	 */
	public void setOffsetFromBinaryStart(long offsetFromBinaryStart) {
		this.offsetFromBinaryStart = offsetFromBinaryStart;
	}

	/**
	 * Setter for the length
	 * @param length the length to set
	 */
	public void setLength(long length) {
		this.length = length;
	}

	@Override
	public String toString() {
		return this.functionName
				+ " @" + Long.toHexString(this.startAddress.intValue()); //$NON-NLS-1$
	}

}
