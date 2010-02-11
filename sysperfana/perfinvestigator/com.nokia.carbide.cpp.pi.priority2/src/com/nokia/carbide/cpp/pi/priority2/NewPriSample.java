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

package com.nokia.carbide.cpp.pi.priority2;

import com.nokia.carbide.cpp.internal.pi.model.GenericSample;


public class NewPriSample extends GenericSample
{
	private static final long serialVersionUID = -4474787388603984159L;
	
	public NewPriThread thread;
	public int priority;
	public int sampleNum;
	
	public NewPriSample(NewPriThread thread, int priority, int sampleNum)
	{
		this.thread = thread;
		this.priority = priority;
		this.sampleNum = sampleNum;
		this.sampleSynchTime = sampleNum;
	}
}
