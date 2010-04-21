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

import java.io.Serializable;

/**
 * Represents a function that could not be resolved
 *
 */
public class UnresolvedFunction implements IFunction, Serializable {
	
	private static final long serialVersionUID = -3884683701926247700L;

	/** address for this function, usually the program counter value for unresolved functions */
	protected Long address;

	/**
	 * Constructor
	 * @param address address for this function, usually the program counter value
	 */
	public UnresolvedFunction(Long address) {
		this.address = address;
	}

	/* (non-Javadoc)
	 * @see com.nokia.carbide.cpp.internal.pi.model.IFunction#getFunctionBinary()
	 */
	public IBinary getFunctionBinary() {
		return new UnresolvedBinary(address);
	}

	/* (non-Javadoc)
	 * @see com.nokia.carbide.cpp.internal.pi.model.IFunction#getFunctionName()
	 */
	public String getFunctionName() {
		return String.format(Messages.getString("UnresolvedFunction.0"), address); //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see com.nokia.carbide.cpp.internal.pi.model.IFunction#getLength()
	 */
	public long getLength() {
		return 0;
	}

	/* (non-Javadoc)
	 * @see com.nokia.carbide.cpp.internal.pi.model.IFunction#getOffsetFromBinaryStart()
	 */
	public long getOffsetFromBinaryStart() {
		return 0;
	}

	/* (non-Javadoc)
	 * @see com.nokia.carbide.cpp.internal.pi.model.IFunction#getStartAddress()
	 */
	public Long getStartAddress() {
		return address;
	}

}
