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
 * Color Tree Text Item class
 *
 */
package com.nokia.traceviewer.dialog.treeitem;

import org.eclipse.swt.graphics.Color;

/**
 * Color Tree Text Item class
 */
public class ColorTreeTextItem extends ColorTreeBaseItem {

	/**
	 * Text of the item
	 */
	private final String text;

	/**
	 * Match case boolean
	 */
	private final boolean matchCase;

	/**
	 * Text to be compared
	 */
	private final String textToCompare;

	/**
	 * Constructor
	 * 
	 * @param listener
	 *            TreeItem listener
	 * @param parent
	 *            parent object
	 * @param name
	 *            name of the item
	 * @param rule
	 *            rule of the item
	 * @param foreground
	 *            foreground color
	 * @param background
	 *            background color
	 * @param text
	 *            text of the item
	 * @param matchCase
	 *            matching case or not
	 */
	public ColorTreeTextItem(TreeItemListener listener, Object parent,
			String name, Rule rule, Color foreground, Color background,
			String text, boolean matchCase) {
		super(listener, parent, name, rule, foreground, background);
		this.text = text;
		this.matchCase = matchCase;

		// Save text to compare
		if (matchCase || text == null) {
			textToCompare = text;
		} else {
			textToCompare = text.toLowerCase();
		}
	}

	/**
	 * Gets match case status
	 * 
	 * @return the matchCase
	 */
	public boolean isMatchCase() {
		return matchCase;
	}

	/**
	 * Gets text
	 * 
	 * @return the text
	 */
	public String getText() {
		return text;
	}

	/**
	 * Gets text to compare
	 * 
	 * @return the text to compare
	 */
	public String getTextToCompare() {
		return textToCompare;
	}

}
