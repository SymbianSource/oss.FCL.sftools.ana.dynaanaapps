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
 * Trigger Tree Base item class
 *
 */
package com.nokia.traceviewer.dialog.treeitem;

/**
 * Trigger Tree Base item class
 */
public class TriggerTreeBaseItem extends TreeItemImpl implements
		TriggerTreeItem {

	/**
	 * Rule of the item
	 */
	private final Rule rule;

	/**
	 * Type of the trigger
	 */
	private final Type type;

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
	 * @param type
	 *            type of the trigger
	 */
	public TriggerTreeBaseItem(TreeItemListener listener, Object parent,
			String name, Rule rule, Type type) {
		super(name, parent, listener, (rule == Rule.GROUP));
		this.rule = rule;
		this.type = type;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.traceviewer.dialog.TriggerTreeItem#getRule()
	 */
	public Rule getRule() {
		return rule;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.traceviewer.dialog.TriggerTreeItem#getType()
	 */
	public Type getType() {
		return type;
	}
}
