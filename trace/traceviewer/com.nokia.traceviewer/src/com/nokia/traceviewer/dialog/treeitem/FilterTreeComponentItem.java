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
 * Filter Tree Component Item
 *
 */
package com.nokia.traceviewer.dialog.treeitem;

import com.nokia.traceviewer.dialog.BasePropertyDialog;
import com.nokia.traceviewer.engine.TraceInformation;
import com.nokia.traceviewer.engine.TraceProperties;

/**
 * Filter Tree Component Item
 */
public class FilterTreeComponentItem extends FilterTreeBaseItem {

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
	 *            rule of the rule
	 * @param componentId
	 *            component Id
	 * @param groupId
	 *            group Id
	 */
	public FilterTreeComponentItem(TreeItemListener listener, Object parent,
			String name, Rule rule, int componentId, int groupId) {
		super(listener, parent, name, rule);
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.nokia.traceviewer.dialog.treeitem.FilterTreeBaseItem#processRule(
	 * com.nokia.traceviewer.engine.TraceProperties)
	 */
	@Override
	public boolean processRule(TraceProperties properties) {
		boolean filterHit = false;
		TraceInformation information = properties.information;

		// Information must be defined
		if (information != null && information.isDefined()) {

			// Component ID matches
			if (componentId == BasePropertyDialog.WILDCARD_INTEGER
					|| componentId == information.getComponentId()) {

				// Group ID matches
				if (groupId == BasePropertyDialog.WILDCARD_INTEGER
						|| groupId == information.getGroupId()) {

					filterHit = true;
				}
			}
		}

		// If logical NOT, change the result to opposite
		if (isLogicalNotRule()) {
			filterHit = !filterHit;
		}

		return filterHit;
	}
}
