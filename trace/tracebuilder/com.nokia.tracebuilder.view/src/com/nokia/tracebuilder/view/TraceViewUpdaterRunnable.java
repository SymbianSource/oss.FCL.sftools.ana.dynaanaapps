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
* A runnable that is passed to Display#syncExec when refreshing the view
*
*/
package com.nokia.tracebuilder.view;

import java.util.ArrayList;
import java.util.Iterator;

import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;

/**
 * A runnable that is passed to Display#syncExec when refreshing the view
 * 
 */
final class TraceViewUpdaterRunnable implements Runnable {

	/**
	 * Tree viewer
	 */
	private TreeViewer viewer;

	/**
	 * Root element of the tree
	 */
	private WrapperBase root;

	/**
	 * List of elements to be refreshed
	 */
	private ArrayList<WrapperBase> updateList;

	/**
	 * Object to be selected after all updates have been run
	 */
	private WrapperBase selection;

	/**
	 * Constructor
	 * 
	 * @param viewer
	 *            tree viewer
	 */
	TraceViewUpdaterRunnable(TreeViewer viewer) {
		this.viewer = viewer;
		updateList = new ArrayList<WrapperBase>();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Runnable#run()
	 */
	public void run() {
		if (!viewer.getControl().isDisposed()) {
			// long time = System.currentTimeMillis();
			Iterator<WrapperBase> itr = updateList.iterator();
			if (itr.hasNext()) {
				WrapperBase wrapper = itr.next();
				if (wrapper == root) {
					viewer.refresh();
				} else {
					viewer.refresh(wrapper);
				}
				itr.remove();
			} else if (selection != null) {
				viewer.setSelection(new StructuredSelection(selection), true);
				selection = null;
			}
		}
	}

	/**
	 * Adds a wrapper to the list of elements to be updated
	 * 
	 * @param wrapper
	 *            the wrapper to be updated
	 */
	void queueUpdate(WrapperBase wrapper) {
		// Adds the element to the update list if it or one of its parents
		// if not already there
		boolean parentFound = false;
		boolean childFound = false;
		Iterator<WrapperBase> itr = updateList.iterator();
		while (itr.hasNext() && !parentFound) {
			WrapperBase existing = itr.next();
			// If a parent is found from the list, the new element is not
			// added to it. If a child is found, it is removed and the new
			// element is added. Loop is not terminated if child is found,
			// since there might be other children to be removed.
			if (!childFound && isParent(existing, wrapper)) {
				parentFound = true;
			} else if (isParent(wrapper, existing)) {
				childFound = true;
				itr.remove();
			}
		}
		// If the wrapper or one of its parents was already in the list, it
		// is not added
		if (!parentFound) {
			updateList.add(wrapper);
		}
	}

	/**
	 * Queues a selection operation. The selection is done after all updates and
	 * if there was an existing selection operation, it is replaced with the new
	 * one.
	 * 
	 * @param wrapper
	 *            the wrapper to be selected
	 */
	void queueSelection(WrapperBase wrapper) {
		selection = wrapper;
	}

	/**
	 * Checks if existing is the same or the parent of wrapper.
	 * 
	 * @param existing
	 *            the possible parent
	 * @param wrapper
	 *            the new wrapper
	 * @return true if existing is parent or the same as wrapper
	 */
	private boolean isParent(WrapperBase existing, WrapperBase wrapper) {
		WrapperBase parent = wrapper;
		boolean found = false;
		while (!found && parent != null) {
			if (parent == existing) {
				found = true;
			} else {
				parent = parent.getParent();
			}
		}
		return found;
	}

	/**
	 * This function is called from the updater background thread and thus the
	 * list is accessed from both threads
	 * 
	 * @return true
	 */
	boolean hasUpdates() {
		return !updateList.isEmpty() || selection != null;
	}

	/**
	 * Sets the root wrapper
	 * 
	 * @param root
	 *            the root wrapper
	 */
	void setRoot(WrapperBase root) {
		this.root = root;
	}
}