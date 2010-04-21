/*
 * Copyright (c) 2010 Nokia Corporation and/or its subsidiary(-ies). 
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

/**
 * This interface represents a function in Performance Investigator
 *
 */
public interface IFunction {
	
	
	/**
	 * Getter for the function name
	 * @return the function name
	 */
	public String getFunctionName();
	
	/**
	 * Getter for the Binary that this function exists in
	 * @return the Binary
	 */
	public IBinary getFunctionBinary();
	
	/**
	 * Getter for the start address of this function
	 * @return the start address
	 */
	public Long getStartAddress();
	
	/**
	 * Getter for offset from binary start
	 * @return the offset
	 */
	public long getOffsetFromBinaryStart();

	/** 
	 * Getter for the length
	 * @return the length
	 */
	public long getLength();
	
}
