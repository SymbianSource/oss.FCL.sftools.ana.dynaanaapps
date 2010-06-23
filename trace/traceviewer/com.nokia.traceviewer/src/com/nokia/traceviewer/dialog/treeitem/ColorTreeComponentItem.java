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
 * Color Tree Component Item
 *
 */
package com.nokia.traceviewer.dialog.treeitem;

import org.eclipse.swt.graphics.Color;

/**
 * Color Tree Component Item
 */
public class ColorTreeComponentItem extends ColorTreeBaseItem {

	/**
	 * Component ID of this item
	 */
	private final int componentId;

	/**
	 * Group ID of this item
	 */
	private final int groupId;

	/**
	 * Constructor
	 * 
	 * @param listener
	 *            TreeItem listener
	 * @param parent
	 *            parent object
	 * @param name
	 *            name of the rule
	 * @param rule
	 *            Rule of this item
	 * @param foreground
	 *            foreground color for this item
	 * @param background
	 *            background color for this item
	 * @param componentId
	 *            component ID
	 * @param groupId
	 *            group ID
	 */
	public ColorTreeComponentItem(TreeItemListener listener, Object parent,
			String name, Rule rule, Color foreground, Color background,
			int componentId, int groupId) {
		super(listener, parent, name, rule, foreground, background);
		this.componentId = componentId;
		this.groupId = groupId;
	}

	/**
	 * Gets component ID
	 * 
	 * @return the componentId
	 */
	public int getComponentId() {
		return componentId;
	}

	/**
	 * Gets group ID
	 * 
	 * @return the groupId
	 */
	public int getGroupId() {
		return groupId;
	}
}
