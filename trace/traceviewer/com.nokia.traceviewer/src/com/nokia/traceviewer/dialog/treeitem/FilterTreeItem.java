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
 * Filter tree item interface
 *
 */
package com.nokia.traceviewer.dialog.treeitem;

import com.nokia.traceviewer.engine.dataprocessor.FilterRuleObject;

/**
 * Filter tree item interface
 */
public interface FilterTreeItem extends TreeItem, FilterRuleObject {

	/**
	 * Rule type for filter items
	 */
	public enum Rule {

		/**
		 * Text rule
		 */
		TEXT_RULE,

		/**
		 * Component rule
		 */
		COMPONENT_RULE,

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
