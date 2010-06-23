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
 * Variable Tracing tree Text Item class
 *
 */
package com.nokia.traceviewer.dialog.treeitem;

/**
 * Variable Tracing tree Text Item class
 */
public class VariableTracingTreeTextItem extends VariableTracingTreeBaseItem {

	/**
	 * Text of the item
	 */
	private final String text;

	/**
	 * Indicates if the item is case sensitive
	 */
	private final boolean matchCase;

	/**
	 * Constructor
	 * 
	 * @param listener
	 *            TreeItemListener
	 * @param parent
	 *            Parent object
	 * @param name
	 *            Name of the rule
	 * @param rule
	 *            Rule type
	 * @param text
	 *            Text of the item
	 * @param matchCase
	 *            match case boolean
	 * @param historyCount
	 *            history count
	 */
	public VariableTracingTreeTextItem(TreeItemListener listener,
			Object parent, String name, Rule rule, String text,
			boolean matchCase, int historyCount) {
		super(listener, parent, name, rule, historyCount);
		this.text = text;
		this.matchCase = matchCase;
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
}
