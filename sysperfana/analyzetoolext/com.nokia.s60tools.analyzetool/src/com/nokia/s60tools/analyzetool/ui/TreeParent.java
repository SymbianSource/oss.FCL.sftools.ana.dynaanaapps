/*
 * Copyright (c) 2008-2010 Nokia Corporation and/or its subsidiary(-ies).
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
 * Description:  Definitions for the class TreeParent
 *
 */

package com.nokia.s60tools.analyzetool.ui;

import java.util.ArrayList;
import java.util.List;

/**
 * Class to implement TreeParent.
 *
 * @author kihe
 *
 */
public class TreeParent extends TreeObject {

	/** Contains list of childrens. */
	private final List<Object> children;

	/**
	 * Constructor.
	 *
	 * @param name
	 *            Current object name
	 */
	public TreeParent(final String name) {
		if (name != null) {
			super.setName(name);
		}
		children = new ArrayList<Object>();
	}

	/**
	 * Adds child.
	 *
	 * @param index
	 *            Index
	 * @param child
	 *            Current parent child
	 */
	public final void addChild(final int index, final TreeObject child) {
		children.add(index, child);
		child.setParent(this);
	}

	/**
	 * Adds child.
	 *
	 * @param child
	 *            Current parent child
	 */
	public final void addChild(final TreeObject child) {
		children.add(child);
		child.setParent(this);
	}

	/**
	 * Gets current parent child objects.
	 *
	 * @return Array of TreeObject
	 */
	public final Object[] getChildren() {
		return children.toArray(new TreeObject[children.size()]);
	}

	/**
	 * Check that does current parent contains child objects.
	 *
	 * @return True if parent contains childrens otherwise false
	 */
	public final boolean hasChildren() {
		return !children.isEmpty();
	}

	/**
	 * Removes child.
	 *
	 * @param child
	 *            Current parent child
	 */
	public final void removeChild(final TreeObject child) {
		children.remove(child);
		child.setParent(null);
	}
}
