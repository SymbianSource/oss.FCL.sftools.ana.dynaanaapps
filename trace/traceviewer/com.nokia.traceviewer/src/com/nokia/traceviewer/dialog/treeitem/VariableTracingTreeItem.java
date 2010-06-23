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
 * Variable Tracing Tree item interface
 *
 */
package com.nokia.traceviewer.dialog.treeitem;

/**
 * Variable Tracing Tree item interface
 */
public interface VariableTracingTreeItem extends TreeItem {

	/**
	 * Rule type for variable tracing items
	 */
	public enum Rule {

		/**
		 * Text rule
		 */
		TEXT_RULE,

		/**
		 * Group rule
		 */
		GROUP;
	}

	/**
	 * Gets the type of the rule
	 * 
	 * @return the rule
	 */
	public Rule getRule();
}
