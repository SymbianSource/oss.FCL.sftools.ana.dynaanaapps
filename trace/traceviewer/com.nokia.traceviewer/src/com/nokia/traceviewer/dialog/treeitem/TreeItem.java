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
 * Tree item interface
 *
 */
package com.nokia.traceviewer.dialog.treeitem;

/**
 * Tree item interface
 * 
 */
public interface TreeItem {

	/**
	 * Gets the name of the rule
	 * 
	 * @return the name
	 */
	public String getName();

	/**
	 * Sets the name of the rule
	 * 
	 * @param name
	 *            the name to set
	 */
	public void setName(String name);

	/**
	 * Tells if this TreeItem is group
	 * 
	 * @return true if TreeItem is group
	 */
	public boolean isGroup();

	/**
	 * Gets children from this tree item
	 * 
	 * @return children
	 */
	public Object[] getChildren();

	/**
	 * Set parent
	 * 
	 * @param parent
	 *            the new parent
	 */
	public void setParent(Object parent);

	/**
	 * Get parent
	 * 
	 * @return parent
	 */
	public Object getParent();

	/**
	 * Adds child
	 * 
	 * @param index
	 *            index where to add item
	 * @param treeItem
	 *            new child
	 */
	public void addChild(int index, TreeItem treeItem);

	/**
	 * Adds child
	 * 
	 * @param treeItem
	 *            new child
	 */
	public void addChild(TreeItem treeItem);

	/**
	 * Removes child
	 * 
	 * @param treeItem
	 *            child to be removed
	 */
	public void removeChild(TreeItem treeItem);

}
