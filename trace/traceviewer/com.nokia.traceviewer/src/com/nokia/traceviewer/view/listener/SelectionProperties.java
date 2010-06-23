/*
 * Copyright (c) 2007-2010 Nokia Corporation and/or its subsidiary(-ies). 
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
 * Selection Properties holds properties of current selection
 *
 */
package com.nokia.traceviewer.view.listener;

import com.nokia.traceviewer.engine.TraceViewerGlobals;

/**
 * Selection Properties holds properties of current selection
 */
public class SelectionProperties {

	/**
	 * Line number of the first clicked line
	 */
	public static int firstClickedLine = -1;

	/**
	 * Caret offset of the first clicked line
	 */
	public static int firstClickedLineCaretOffset;

	/**
	 * Timestamp of the first clicked line
	 */
	public static String firstClickedTimestamp = ""; //$NON-NLS-1$

	/**
	 * Line number of the last clicked line
	 */
	public static int lastClickedLine = -1;

	/**
	 * Caret offset of the last clicked line
	 */
	public static int lastClickedLineCaretOffset;

	/**
	 * Timestamp of the last clicked line
	 */
	public static String lastClickedTimestamp = ""; //$NON-NLS-1$

	/**
	 * Puts caret to the end when setting the selection to the view
	 */
	public static boolean putCaretToTheEnd;

	/**
	 * Clears selection
	 */
	public static void clear() {
		firstClickedLine = -1;
		lastClickedLine = -1;
		firstClickedLineCaretOffset = 0;
		lastClickedLineCaretOffset = 0;
		firstClickedTimestamp = ""; //$NON-NLS-1$
		lastClickedTimestamp = ""; //$NON-NLS-1$
		putCaretToTheEnd = false;

		// Update trim
		TraceViewerGlobals.getTrimProvider().updateText(""); //$NON-NLS-1$
	}

}
