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

package com.nokia.carbide.cpp.pi.util;

public class PIExceptionRuntime extends RuntimeException{

	/**
	 * 
	 */
	private static final long serialVersionUID = 5209871199262340065L;
	
	public PIExceptionRuntime () {
		super();
	}
	
	public PIExceptionRuntime (String message) {
		super(message);
	}
}
