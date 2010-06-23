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
 * Color Tree Base item class
 *
 */
package com.nokia.traceviewer.dialog.treeitem;

import org.eclipse.swt.graphics.Color;

/**
 * Color Tree Base item class
 */
public class ColorTreeBaseItem extends TreeItemImpl implements ColorTreeItem {

	/**
	 * Rule type
	 */
	private final Rule rule;

	/**
	 * Foreground color of the item
	 */
	private final Color foregroundColor;

	/**
	 * Background color of the item
	 */
	private final Color backgroundColor;

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
	 *            rule for the item
	 * @param foreground
	 *            foreground color
	 * @param background
	 *            background color
	 */
	public ColorTreeBaseItem(TreeItemListener listener, Object parent,
			String name, Rule rule, Color foreground, Color background) {
		super(name, parent, listener, (rule == Rule.GROUP));
		this.rule = rule;
		this.foregroundColor = foreground;
		this.backgroundColor = background;
	}

	/**
	 * Gets the type of the rule
	 * 
	 * @return the rule
	 */
	public Rule getRule() {
		return rule;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.traceviewer.dialog.ColorItem#getBackgroundColor()
	 */
	public Color getBackgroundColor() {
		return backgroundColor;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.traceviewer.dialog.ColorItem#getForegroundColor()
	 */
	public Color getForegroundColor() {
		return foregroundColor;
	}
}
