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

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;

/**
 * Class for managing all Color resource for PI
 * 
 */

public final  class ColorPalette {

	private static CacheMap<RGB, Color> palette = new CacheMap<RGB, Color>();
	
	private static ColorPalette colorPalette = null;

	private ColorPalette() {
		// private so we can't new ColorPalette
		// user getInstance() to access this class
	}
	
	public static ColorPalette getInstance() {
		if (colorPalette == null)
	    {
	    	colorPalette = new ColorPalette();
	    }
	    return colorPalette;		
	}

	public static void add(RGB rgb) {
		synchronized(palette) {
			palette.put(new RGB(rgb.red, rgb.green, rgb.blue), new Color(Display.getCurrent(), rgb));
		}
	}

	public static Color getColor(RGB rgb) {
		synchronized(palette) {
			if (palette.get(rgb) == null) {
				add(rgb);
			}
			return (Color) palette.get(rgb);
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
