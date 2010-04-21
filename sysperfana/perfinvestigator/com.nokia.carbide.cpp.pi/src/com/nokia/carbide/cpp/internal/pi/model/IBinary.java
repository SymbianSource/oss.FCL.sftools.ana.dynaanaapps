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
 * This interface represents a Binary in Performance Investigator
 */
public interface IBinary {
	/**
	 * Getter for the start address
	 * @return the start address
	 */
	public long getStartAddress();
	
	/**
	 * Getter for the length
	 * @return the length
	 */
	public int getLength();
	
	/**
	 * Getter for the name of the binary
	 * @return the binaryName
	 */
	public String getBinaryName();
	
	/**
	 * Getter for the offset to code start
	 * @return the offset to code start
	 */
	public long getOffsetToCodeStart();
	
	/**
	 * Getter for the type
	 * @return the type
	 */
	public String getType();

}
