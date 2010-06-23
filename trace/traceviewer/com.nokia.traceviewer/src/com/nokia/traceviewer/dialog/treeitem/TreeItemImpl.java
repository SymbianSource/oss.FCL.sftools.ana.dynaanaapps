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
 * Tree Item Implementation
 *
 */
package com.nokia.traceviewer.dialog.treeitem;

import java.util.ArrayList;
import java.util.List;

/**
 * Tree Item Implementation
 */
public abstract class TreeItemImpl implements TreeItem {

	/**
	 * Children list of this tree item
	 */
	private List<TreeItem> children;

	/**
	 * Parent item of this tree item
	 */
	private Object parent;

	/**
	 * Listener
	 */
	private final TreeItemListener listener;

	/**
	 * Indicates is this item a group item or not
	 */
	private final boolean groupItem;

	/**
	 * Name of the item
	 */
	private String name;

	/**
	 * Constructor
	 * 
	 * @param name
	 *            name of the item
	 * @param parent
	 *            parent object of this TreeItem
	 * @param listener
	 *            TreeItemListener
	 * @param groupItem
	 *            group item indicator
	 */
	public TreeItemImpl(String name, Object parent, TreeItemListener listener,
			boolean groupItem) {
		this.name = name;
		this.parent = parent;
		this.listener = listener;
		this.groupItem = groupItem;
		if (groupItem) {
			children = new ArrayList<TreeItem>();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.traceviewer.dialog.TreeItem#getName()
	 */
	public String getName() {
		return name;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.traceviewer.dialog.TreeItem#setName(java.lang.String)
	 */
	public void setName(String name) {
		this.name = name;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.traceviewer.dialog.TreeItem#getChildren()
	 */
	public Object[] getChildren() {
		Object[] childrenArr = null;
		// Return children if item is group
		if (groupItem) {
			childrenArr = children.toArray();
		} else {
			// Return empty array
			childrenArr = new Object[0];
		}
		return childrenArr;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.nokia.traceviewer.dialog.TreeItem#addChild(com.nokia.traceviewer.
	 * dialog.TreeItem)
	 */
	public void addChild(TreeItem treeItem) {
		addChild(children.size(), treeItem);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.traceviewer.dialog.TreeItem#addChild(int,
	 * com.nokia.traceviewer.dialog.TreeItem)
	 */
	public void addChild(int index, TreeItem treeItem) {
		children.add(index, treeItem);
		listener.modelChanged();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.nokia.traceviewer.dialog.TreeItem#removeChild(com.nokia.traceviewer
	 * .dialog.TreeItem)
	 */
	public void removeChild(TreeItem treeItem) {
		children.remove(treeItem);
		listener.modelChanged();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.traceviewer.dialog.ColorItem#getParent()
	 */
	public Object getParent() {
		return parent;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.traceviewer.dialog.TreeItem#isGroup()
	 */
	public boolean isGroup() {
		return groupItem;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.traceviewer.dialog.TreeItem#setParent(java.lang.Object)
	 */
	public void setParent(Object parent) {
		this.parent = parent;
	}
}
