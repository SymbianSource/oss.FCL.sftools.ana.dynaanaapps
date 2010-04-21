/*
* Copyright (c) 2009 Nokia Corporation and/or its subsidiary(-ies). 
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
* Description:
*
*/ 
package com.nokia.s60tools.swmtanalyser.analysers;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Display;

/**
 * Constants for general Analyser purposes
 */
public class AnalyserConstants {

	/**
	 * Priority types
	 */
	public static enum Priority {NEGLIGIBLE,NORMAL,HIGH,CRITICAL}
	/**
	 * Delta types
	 */
	public static enum DeltaType {SIZE, COUNT}
	/**
	 * Color for Severity Normal
	 */
	public static final Color COLOR_SEVERITY_NORMAL = new Color(Display.getCurrent(),121,255,121);//Green
	/**
	 * Color for Severity High
	 */
	public static final Color COLOR_SEVERITY_HIGH = new Color(Display.getCurrent(),255,255,138);//Yellow
	/**
	 * Color for Severity Critical
	 */
	public static final Color COLOR_SEVERITY_CRITICAL = new Color(Display.getCurrent(),254,106,99);//Red

}
