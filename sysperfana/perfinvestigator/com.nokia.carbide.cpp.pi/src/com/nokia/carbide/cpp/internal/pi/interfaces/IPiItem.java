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

package com.nokia.carbide.cpp.internal.pi.interfaces;

import com.nokia.carbide.cpp.internal.pi.model.PiItemType;


public interface IPiItem
{
	String getName();
	void setName(String newName);
	String getLocation();
	boolean isPIFor(Object obj);
	PiItemType getType();
	String getInfo();
	
	static IPiItem[] NONE = new IPiItem[] {};
}
