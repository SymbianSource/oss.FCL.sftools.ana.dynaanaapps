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
 * This class represents an unresolved binary. Most properties can be generated 
 * on the fly thus potentially saving memory 
 *
 */
public class UnresolvedBinary implements IBinary, Serializable{
	private static final long serialVersionUID = -1872500528710393139L;
	private Long startAddress;

	/**
	 * Constructor
	 * @param startAddress the start address of this binary; mainly used
	 * to generate the name of the unresolved binary
	 */
	public UnresolvedBinary(Long startAddress) {
		super();
		this.startAddress = startAddress;
	}

	/* (non-Javadoc)
	 * @see com.nokia.carbide.cpp.internal.pi.model.IBinary#getBinaryName()
	 */
	public String getBinaryName() {
		return String.format(Messages.getString("UnresolvedBinary.0"), startAddress); //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see com.nokia.carbide.cpp.internal.pi.model.IBinary#getLength()
	 */
	public int getLength() {
		return 0;
	}

	/* (non-Javadoc)
	 * @see com.nokia.carbide.cpp.internal.pi.model.IBinary#getOffsetToCodeStart()
	 */
	public long getOffsetToCodeStart() {
		return 0;
	}

	/* (non-Javadoc)
	 * @see com.nokia.carbide.cpp.internal.pi.model.IBinary#getStartAddress()
	 */
	public long getStartAddress() {
		//we could return an actual address here but for some reason
		//the original code returns zero
		return 0;
		//return startAddress;
	}

	/* (non-Javadoc)
	 * @see com.nokia.carbide.cpp.internal.pi.model.IBinary#getType()
	 */
	public String getType() {
		return null;
	}

}
