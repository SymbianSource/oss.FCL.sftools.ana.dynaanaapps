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

package com.nokia.carbide.cpp.internal.pi.utils;

public abstract class AlphabeticItem
{
	private String s;
	public int order;
	
	public AlphabeticItem(String s)
	{
		this.s = s;
		this.order = 0;
	}
	
	public AlphabeticItem()
	{
		this.s = ""; //$NON-NLS-1$
		this.order = 0;
	}
	
	public void setAlphabeticOrderString(String order)
	{
		this.s = order;
	}
	
	public int getAlphabeticOrder()
	{
		return this.order;
	}
	
	public void setAlphabeticOrder(int order)
	{
		this.order = order;
	}
		
	public String toString()
	{
		return s;
	}
}
