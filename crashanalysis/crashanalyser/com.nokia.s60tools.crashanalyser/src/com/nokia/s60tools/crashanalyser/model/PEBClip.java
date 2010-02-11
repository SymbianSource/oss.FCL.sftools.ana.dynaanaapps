/*
* Copyright (c) 2008 Nokia Corporation and/or its subsidiary(-ies). 
* All rights reserved.
* This component and the accompanying materials are made available
* under the terms of "Eclipse Public License v1.0"
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

package com.nokia.s60tools.crashanalyser.model;

import sun.awt.windows.*;
import java.lang.reflect.*;

/**
 * Access the native clipboard, windows only, write only.
 */
public class PEBClip extends WClipboard {
	private final Method m;

	public PEBClip() throws NoSuchMethodException {
		m = WClipboard.class.getDeclaredMethod("publishClipboardData",
				new Class[]  {Long.TYPE, (new byte[0]).getClass()});
		m.setAccessible(true);
	}
	
	public synchronized void setData(long format, byte[] data) throws IllegalAccessException, InvocationTargetException {
		setData(new long[]{format}, new byte[][]{data});
	}

	public synchronized void setData(long[] format, byte[][] data) throws IllegalAccessException, InvocationTargetException {
		openClipboard(this);
		try{
			for(int i=0;i<format.length && i<data.length;i++)
				myPublishClipboardData(format[i], data[i]);
		} finally {
			closeClipboard();
		}
	}

	protected void myPublishClipboardData(long format, byte[] bytes) throws IllegalAccessException, InvocationTargetException{
		m.invoke(this, new Object[]{new Long(format),bytes});
	}
}
