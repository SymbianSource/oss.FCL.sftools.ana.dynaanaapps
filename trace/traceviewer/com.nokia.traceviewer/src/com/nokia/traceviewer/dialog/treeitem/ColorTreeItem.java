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
 * Color Tree item interface
 *
 */
package com.nokia.traceviewer.dialog.treeitem;

import org.eclipse.swt.graphics.Color;

/**
 * Color Tree item interface
 */
public interface ColorTreeItem extends TreeItem {

	/**
	 * Rule type for color items
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

	/**
	 * Gets the foreground color of the rule
	 * 
	 * @return foreground color
	 */
	public Color getForegroundColor();

	/**
	 * Gets the background color of the rule
	 * 
	 * @return background color
	 */
	public Color getBackgroundColor();
}
