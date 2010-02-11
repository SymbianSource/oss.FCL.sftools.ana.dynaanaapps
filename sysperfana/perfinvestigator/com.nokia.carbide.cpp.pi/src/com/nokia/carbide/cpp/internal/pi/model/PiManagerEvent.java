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

import java.util.EventObject;

import com.nokia.carbide.cpp.internal.pi.interfaces.IPiItem;


public class PiManagerEvent extends EventObject
{
	private static final long serialVersionUID = 419533445045955384L;
	private final IPiItem[] added;
	private final IPiItem[] removed;

	public PiManagerEvent(
		PiManager source,
		IPiItem[] itemsAdded,
		IPiItem[] itemsRemoved) {
         
		super(source);
		added = itemsAdded;
		removed = itemsRemoved;
	}
   
	public IPiItem[] getItemsAdded() {
	   return added;
	}
   
	public IPiItem[] getItemsRemoved() {
		return removed;
	}
}
