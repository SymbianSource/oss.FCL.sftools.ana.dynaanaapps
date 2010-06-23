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
 * Filter Tree Base item class
 *
 */
package com.nokia.traceviewer.dialog.treeitem;

import com.nokia.traceviewer.engine.TraceProperties;

/**
 * Filter Tree Base item class
 */
public class FilterTreeBaseItem extends TreeItemImpl implements FilterTreeItem {

	/**
	 * Rule of the item
	 */
	private final Rule rule;

	/**
	 * Indicates if this rule is inside logical NOT
	 */
	private boolean notRule;

	/**
	 * Constructor
	 * 
	 * @param listener
	 *            TreeItemListener
	 * @param parent
	 *            parent object
	 * @param name
	 *            name for the item
	 * @param rule
	 *            rule for the item
	 */
	public FilterTreeBaseItem(TreeItemListener listener, Object parent,
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.nokia.traceviewer.engine.dataprocessor.FilterRuleObject#isLogicalNotRule
	 * ()
	 */
	public boolean isLogicalNotRule() {
		return notRule;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.nokia.traceviewer.engine.dataprocessor.FilterRuleObject#setLogicalNotRule
	 * (boolean)
	 */
	public void setLogicalNotRule(boolean notRule) {
		this.notRule = notRule;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.traceviewer.engine.dataprocessor.FilterRuleObject
	 * #processRule(com.nokia.traceviewer.engine.TraceProperties)
	 */
	public boolean processRule(TraceProperties properties) {
		// Method implemented in subclassses
		return false;
	}
}
