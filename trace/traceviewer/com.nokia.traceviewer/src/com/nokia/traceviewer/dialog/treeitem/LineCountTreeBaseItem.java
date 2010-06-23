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
 * LineCount Tree Base item class
 *
 */
package com.nokia.traceviewer.dialog.treeitem;

/**
 * LineCount Tree Base item class
 */
public class LineCountTreeBaseItem extends TreeItemImpl implements
		LineCountTreeItem {

	/**
	 * Rule of the item
	 */
	private final Rule rule;

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
	 */
	public LineCountTreeBaseItem(TreeItemListener listener, Object parent,
			String name, Rule rule) {
		super(name, parent, listener, (rule == Rule.GROUP));
		this.rule = rule;
	}

	/**
	 * Gets the type of the rule
	 * 
	 * @return the rule
	 */
	public Rule getRule() {
		return rule;
	}
}
