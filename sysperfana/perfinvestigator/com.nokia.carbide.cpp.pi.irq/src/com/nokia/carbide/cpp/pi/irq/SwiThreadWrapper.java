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

package com.nokia.carbide.cpp.pi.irq;

import java.io.Serializable;

/**
 * Wrapper for swi threads
 */
public class SwiThreadWrapper implements Serializable {
	/**
	 * Serial version uid
	 */
	private static final long serialVersionUID = -377498088520328159L;

	/* address of the thread */
	public Long threadAddress;

	/* name of the thread */
	public String threadName;

	/**
	 * Ddefault constructor
	 */
	public SwiThreadWrapper() {
	}

	/**
	 * Constructor
	 * 
	 * @param threadName
	 *            name of the thread
	 * @param threadAddress
	 *            address of the thread
	 */
	public SwiThreadWrapper(String threadName, Long threadAddress) {
		this.threadName = threadName;
		this.threadAddress = threadAddress;
	}

}