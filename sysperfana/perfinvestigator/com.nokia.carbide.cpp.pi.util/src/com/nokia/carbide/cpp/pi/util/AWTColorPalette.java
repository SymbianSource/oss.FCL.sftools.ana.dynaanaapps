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

import org.eclipse.swt.graphics.RGB;

/**
 * Class for managing all Color resource for PI
 * 
 */

public class AWTColorPalette {

	private static CacheMap<RGB, java.awt.Color> palette = new CacheMap<RGB, java.awt.Color>();
	
	private static AWTColorPalette colorPalette = null;

	private AWTColorPalette() {
		// private so we can't new ColorPalette
		// user getInstance() to access this class
	}
	
	public static AWTColorPalette getInstance() {
		if (colorPalette == null)
	    {
	    	colorPalette = new AWTColorPalette();
	    }
	    return colorPalette;		
	}

	public static void add(RGB rgb) {
		synchronized(palette) {
			palette.put(new RGB(rgb.red, rgb.green, rgb.blue), new java.awt.Color(rgb.red, rgb.green, rgb.blue));
		}
	}

	public static java.awt.Color getColor(RGB rgb) {
		synchronized(palette) {
			if (palette.get(rgb) == null) {
				add(rgb);
			}
			return (java.awt.Color) palette.get(rgb);
		}
	}

	public static int size() {
		return palette.size();
	}

	public static void dispose() {
		// CacheMap.dispose() is going to free up resource
		palette.dispose();
	}
	
	protected void finalize() {
		palette.dispose();
	}
}
