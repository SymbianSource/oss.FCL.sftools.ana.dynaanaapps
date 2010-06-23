/*
* Copyright (c) 2007 Nokia Corporation and/or its subsidiary(-ies). 
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
* Base class for tree view elements
*
*/
package com.nokia.tracebuilder.view;

import java.util.Iterator;

import org.eclipse.jface.viewers.AbstractTreeViewer;
import org.eclipse.jface.viewers.TreeViewer;

import com.nokia.tracebuilder.engine.TraceBuilderConfiguration;

/**
 * Base class for tree view elements
 * 
 */
abstract class WrapperBase {

	/**
	 * Parent wrapper
	 */
	private WrapperBase parent;

	/**
	 * Visibility flag, used to optimize view updates
	 */
	private boolean isInView;

	/**
	 * Restore visibility flag. When a tree element is expanded, the children of
	 * children that have this flag are marked as visible
	 */
	private boolean restoreIntoView;

	/**
	 * View updater
	 */
	private WrapperUpdater updater;

	/**
	 * Constructor
	 * 
	 * @param parent
	 *            the parent wrapper
	 * @param updater
	 *            the wrapper updater
	 */
	WrapperBase(WrapperBase parent, WrapperUpdater updater) {
		this.parent = parent;
		this.updater = updater;
	}

	/**
	 * Gets the children
	 * 
	 * @return the children of this wrapper
	 */
	abstract Object[] getChildren();

	/**
	 * Checks if this wrapper has children
	 * 
	 * @return true if there are children
	 */
	abstract boolean hasChildren();

	/**
	 * Gets the parent
	 * 
	 * @return parent wrapper
	 */
	WrapperBase getParent() {
		return parent;
	}

	/**
	 * Clears the contents of this wrapper. Wrapper cannot be used after delete
	 */
	void delete() {
		parent = null;
	}

	/**
	 * Sets the shown in view flag
	 * 
	 * @param isInView
	 *            the new flag value
	 */
	void setInView(boolean isInView) {
		this.isInView = isInView;
		if (parent != null && isInView) {
			parent.setChildInView();
		}
	}

	/**
	 * Returns true if this wrapper is shown in the view
	 * 
	 * @return the shown in view flag
	 */
	boolean isInView() {
		return isInView;
	}

	/**
	 * Collapses the sibling wrappers of this wrapper
	 * 
	 * @param viewer
	 *            the tree viewer
	 */
	void collapseSiblings(TreeViewer viewer) {
		Iterator<WrapperBase> itr = ((ListWrapper) getParent())
				.getVisibleWrappers();
		while (itr.hasNext()) {
			WrapperBase child = itr.next();
			// The siblings are shown in view
			child.setInView(true);
			if (child != this && child instanceof ListWrapper) {
				collapseSiblings(viewer, (ListWrapper) child);
			}
		}
	}

	/**
	 * Collapses the sibling wrappers of this wrapper
	 * 
	 * @param viewer
	 *            the viewer
	 * @param child
	 *            the child
	 */
	private void collapseSiblings(TreeViewer viewer, ListWrapper child) {
		// The collapsed wrapper is shown in view, but it
		// children are hidden
		// If a sibling is shown, it is collapsed
		boolean needsCollapse = child.hasChildren()
				&& child.getVisibleWrappers().next().isInView();
		if (needsCollapse) {
			child.setChildrenInView(false);
			viewer.collapseToLevel(child, AbstractTreeViewer.ALL_LEVELS);
		}
	}

	/**
	 * Dumps this object to System.out
	 * 
	 * @param indentLevel
	 *            the indent level
	 * @param provider
	 *            the label provider
	 */
	void dumpToSystemOut(int indentLevel, TraceLabelProvider provider) {
		if (TraceBuilderConfiguration.TRACE_VIEW_STATE) {
			StringBuffer sb = new StringBuffer();
			for (int i = 0; i < indentLevel; i++) {
				sb.append("  "); //$NON-NLS-1$
			}
			sb.append(provider.getText(this));
			System.out.println(sb);
		}
	}

	/**
	 * Gets the view update notifier
	 * 
	 * @return the update notifier
	 */
	WrapperUpdater getUpdater() {
		return updater;
	}

	/**
	 * Recursively flags the parents as in view
	 */
	private void setChildInView() {
		isInView = true;
		if (parent != null) {
			parent.setChildInView();
		}
	}

	/**
	 * Sets restore into view flag. When a tree element is expanded, the child
	 * elements that have this flag are marked as "in view"
	 * 
	 * @param flag
	 *            the new flag value. This changes to true only if this wrapper
	 *            is already visible
	 */
	void setRestoreIntoView(boolean flag) {
		if (flag && isInView()) {
			restoreIntoView = true;
		} else {
			restoreIntoView = false;
		}
	}

	/**
	 * Gets the restore into view flag
	 * 
	 * @return the flag
	 */
	boolean isRestoredIntoView() {
		return restoreIntoView;
	}

}