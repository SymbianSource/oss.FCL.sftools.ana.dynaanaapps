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

public abstract class GenericEvent implements Serializable
{
	static final long serialVersionUID = -7891790813095786064L;
	
	public double eventTime;
    
    //time in milliseconds
	public GenericEvent(double time)
	{
		this.eventTime = time;
	}

}
