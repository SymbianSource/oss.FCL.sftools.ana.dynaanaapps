/*
 * Copyright (c) 2008-2009 Nokia Corporation and/or its subsidiary(-ies).
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
 * Description:  Definitions for the class ColorUtil
 *
 */
package com.nokia.s60tools.analyzetool.internal.ui.util;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Display;

/**
 * Utilities for generating the same (unique) colour for a given String every
 * time.
 */
public final class ColorUtil
{
	private static final int[] RGB_WHITE = { 255, 255, 255 };
	private static final int[] RGB_GREY  = { 180, 180, 180 };//light grey
	private static final int[] RGB_BLACK = { 0, 0, 0 };
	private static final int[] RGB_100 = {100, 100, 100};//middle grey
	private static final int[] RGB_170 = {170, 170, 170};//light grey
	private static final int[] RGB_200 = {200, 200, 200};//very light grey

	/** Color representing the colour white */
	public static final Color WHITE =
		new Color(Display.getDefault(), RGB_WHITE[0], RGB_WHITE[1], RGB_WHITE[2]);

	/** Color representing the colour grey */
	public static final Color GREY =
		new Color(Display.getDefault(), RGB_GREY[0], RGB_GREY[1], RGB_GREY[2]);

	/** Color representing the colour black */
	public static final Color BLACK =
		new Color(Display.getDefault(), RGB_BLACK[0], RGB_BLACK[1], RGB_BLACK[2]);
	
	/** Color representing middle grey */
	public static final Color COLOR_100 = new Color(Display.getDefault(), RGB_100[0], RGB_100[1], RGB_100[2]);
	/** Color representing very light grey */
	public static final Color COLOR_200 = new Color(Display.getDefault(), RGB_200[0], RGB_200[1], RGB_200[2]);
	/** Color representing light grey */
	public static final Color COLOR_170 = new Color(Display.getDefault(), RGB_170[0], RGB_170[1], RGB_170[2]);
	
	// Guard against instantiation
	private ColorUtil() {}
}
