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

/*
 *
 * PRI HOMMAT KOMMENTOITU TÄSTÄ FILESTÄ TOISTAISEKSI
 *
 */

public class ProfiledThread extends ProfiledGeneric implements Serializable
{
	private static final long serialVersionUID = 20150633093396772L;

	private char name; //char symbol of the thread

	private int threadId; //thread's real id
 
	public ProfiledThread(int cpuCount, int graphCount)
	{
		super(cpuCount, graphCount);
	}
  
	public void setNameValues(char symbol,String nameString)
	{
		this.name = symbol;
		setNameString(nameString);
	}
 
	//unused?
//	public String toString(int graphIndex)
//	{
//		if (this.isEnabled(graphIndex))
//		{
//			return "true  " + this.getAverageLoadValueString(graphIndex) + getNameString(); //$NON-NLS-1$
//		}
//		else
//		{
//			return "false " + this.getAverageLoadValueString(graphIndex) + getNameString(); //$NON-NLS-1$
//		}
//	}
  
	public Character getName(){
		return new Character(this.name);
	}

	public int getThreadId() 
	{
		return threadId;
	}

	public void setThreadId(int threadId) 
	{
		this.threadId = threadId;
	}
}
