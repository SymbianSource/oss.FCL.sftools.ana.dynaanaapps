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
 * TreeItem Content Provider
 *
 */
package com.nokia.traceviewer.dialog.treeitem;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

/**
 * TreeItem Content Provider
 */
public class TreeItemContentProvider implements ITreeContentProvider,
		TreeItemListener {

	/**
	 * Root item
	 */
	private Object root;

	/**
	 * Viewer to provide content to
	 */
	private Viewer viewer;

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jface.viewers.ITreeContentProvider#getChildren(java.lang.
	 * Object)
	 */
	public Object[] getChildren(Object parentElement) {
		return ((TreeItem) parentElement).getChildren();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jface.viewers.ITreeContentProvider#getParent(java.lang.Object
	 * )
	 */
	public Object getParent(Object element) {
		return ((TreeItem) element).getParent();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jface.viewers.ITreeContentProvider#hasChildren(java.lang.
	 * Object)
	 */
	public boolean hasChildren(Object element) {
		boolean hasChildren = false;
		TreeItem treeItem = ((TreeItem) element);
		if (treeItem.getChildren() != null && treeItem.getChildren().length > 0) {
			hasChildren = true;
		}
		return hasChildren;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jface.viewers.IStructuredContentProvider#getElements(java
	 * .lang.Object)
	 */
	public Object[] getElements(Object inputElement) {
		return getChildren(root);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.IContentProvider#dispose()
	 */
	public void dispose() {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jface.viewers.IContentProvider#inputChanged(org.eclipse.jface
	 * .viewers.Viewer, java.lang.Object, java.lang.Object)
	 */
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		// First time set as root
		this.viewer = viewer;
		this.root = newInput;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.traceviewer.dialog.FilterItemListener#modelChanged()
	 */
	public void modelChanged() {
		if (root != null) {
			viewer.refresh();
		}
	}

}
