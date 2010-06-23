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
 * VariableTracing Tree Base item class
 *
 */
package com.nokia.traceviewer.dialog.treeitem;

/**
 * VariableTracing Tree Base item class
 */
public class VariableTracingTreeBaseItem extends TreeItemImpl implements
		VariableTracingTreeItem {

	/**
	 * Rule of the item
	 */
	private final Rule rule;

	/**
	 * Number of historyItems to save
	 */
	private final int historyCount;

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
	 * @param historyCount
	 *            count for the item
	 */
	public VariableTracingTreeBaseItem(TreeItemListener listener,
			Object parent, String name, Rule rule, int historyCount) {
		super(name, parent, listener, (rule == Rule.GROUP));
		this.rule = rule;
		this.historyCount = historyCount;

		// Always save at least 1
		if (historyCount < 1) {
			historyCount = 1;
		}
	}

	/**
	 * Constructor without history count
	 * 
	 * @param listener
	 *            TreeItemListener
	 * @param parent
	 *            parent object
	 * @param name
	 *            name of the item
	 * @param rule
	 *            rule for the item
	 */
	public VariableTracingTreeBaseItem(TreeItemListener listener,
			Object parent, String name, Rule rule) {
		super(name, parent, listener, (rule == Rule.GROUP));
		this.rule = rule;
		this.historyCount = 1;
	}

	/**
	 * Gets the type of the rule
	 * 
	 * @return the rule
	 */
	public Rule getRule() {
		return rule;
	}

	/**
	 * Gets history count
	 * 
	 * @return the historyCount
	 */
	public int getHistoryCount() {
		return historyCount;
	}
}
