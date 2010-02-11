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

import org.eclipse.swt.graphics.Image;

import com.nokia.carbide.cpp.internal.pi.interfaces.IPiItem;


public abstract class PiItemType
	implements Comparable
{
	//// Constant Types ////
	public static final PiItemType UNKNOWN =
		new PiItemType(Messages.getString("PiItemType.unknown"), Messages.getString("PiItemType.unknown"), 0) {   //$NON-NLS-1$ //$NON-NLS-2$
			public Image getImage() {
				return null;
			}
			public IPiItem newFavorite(Object Obj) {
				return null;
			}
			public IPiItem loadFavorite(String info) {
				return null;
			}
			public int compareTo(Object arg0) {
				return 0;
			}
	};
	
	//// Type Lookup ///
	private static final PiItemType[] TYPES = {
		UNKNOWN,
	};
	
	public static PiItemType[] getTypes() {
		return TYPES;
	}
	
	//// Instance Members
	
	private final String id;
	private final String printName;
	private final int ordinal;
	
	private PiItemType(String id, String name, int position) {
		this.id        = id;
		this.ordinal   = position;
		this.printName = name;
	}
	
	public int compareTo(Object arg) {
		return this.ordinal
			- ((PiItemType) arg).ordinal;
	}
	
	public String getID() {
		return id;
	}
	
	public String getName() {
		return printName;
	}
	
	public abstract Image getImage();
	public abstract IPiItem newFavorite(Object obj);
	public abstract IPiItem loadFavorite(String info);
}
