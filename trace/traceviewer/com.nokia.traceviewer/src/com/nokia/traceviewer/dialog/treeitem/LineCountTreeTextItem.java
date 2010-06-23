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
 * Line count tree Text Item class
 *
 */
package com.nokia.traceviewer.dialog.treeitem;

/**
 * Line count tree Text Item class
 */
public class LineCountTreeTextItem extends LineCountTreeBaseItem {

	/**
	 * Text of the item
	 */
	private final String text;

	/**
	 * Text to be compared
	 */
	private final String textToCompare;

	/**
	 * Indicates if the item case should match
	 */
	private final boolean matchCase;

	/**
	 * Constructor
	 * 
	 * @param listener
	 *            TreeItemListener
	 * @param parent
	 *            parent object
	 * @param name
	 *            name of the item
	 * @param rule
	 *            rule of the item
	 * @param text
	 *            text for this item
	 * @param matchCase
	 *            match case boolean for this item
	 */
	public LineCountTreeTextItem(TreeItemListener listener, Object parent,
			String name, Rule rule, String text, boolean matchCase) {
		super(listener, parent, name, rule);
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
