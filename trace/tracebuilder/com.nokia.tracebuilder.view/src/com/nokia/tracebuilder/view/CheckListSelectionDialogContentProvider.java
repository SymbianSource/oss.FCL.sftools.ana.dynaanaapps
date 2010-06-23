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
* Trace Builder view implementation
*
*/
package com.nokia.tracebuilder.view;

import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

import com.nokia.tracebuilder.engine.CheckListDialogEntry;

/**
 * Content provider for check list dialog
 * 
 */
class CheckListSelectionDialogContentProvider implements ITreeContentProvider {

	/**
	 * List root
	 */
	private CheckListDialogEntry rootItem;

	/**
	 * Dummy object
	 */
	private Object[] dummy = new Object[0];

	/**
	 * The viewer
	 */
	private CheckboxTreeViewer viewer;

	/**
	 * Constructor
	 */
	CheckListSelectionDialogContentProvider() {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.IStructuredContentProvider#
	 *      getElements(java.lang.Object)
	 */
	public Object[] getElements(Object inputElement) {
		return rootItem.childrenToArray();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.ITreeContentProvider#getChildren(java.lang.Object)
	 */
	public Object[] getChildren(Object parentElement) {
		Object[] retval;
		if (parentElement instanceof CheckListDialogEntry) {
			retval = ((CheckListDialogEntry) parentElement).childrenToArray();
		} else {
			retval = dummy;
		}
		return retval;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.ITreeContentProvider#getParent(java.lang.Object)
	 */
	public Object getParent(Object element) {
		Object retval;
		if (element instanceof CheckListDialogEntry) {
			retval = ((CheckListDialogEntry) element).getParent();
		} else {
			retval = null;
		}
		return retval;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.ITreeContentProvider#hasChildren(java.lang.Object)
	 */
	public boolean hasChildren(Object element) {
		boolean retval;
		if (element instanceof CheckListDialogEntry) {
			retval = ((CheckListDialogEntry) element).hasChildren();
		} else {
			retval = false;
		}
		return retval;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.IContentProvider#dispose()
	 */
	public void dispose() {
		viewer = null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.IContentProvider#
	 *      inputChanged(org.eclipse.jface.viewers.Viewer, java.lang.Object,
	 *      java.lang.Object)
	 */
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		this.viewer = (CheckboxTreeViewer) viewer;
		if (rootItem != null && newInput != null) {
			// Clears the check states of previous root
			setCheckStates(rootItem, false);
		}
		rootItem = (CheckListDialogEntry) newInput;
	}

	/**
	 * Recursively clears all check states
	 * 
	 * @param entry
	 *            the entry to be cleared
	 * @param flag
	 *            new check state
	 */
	private void setCheckStates(CheckListDialogEntry entry, boolean flag) {
		entry.setChecked(flag);
		for (CheckListDialogEntry child : entry) {
			setCheckStates(child, flag);
		}
	}

	/**
	 * Checks all elements if root is checked
	 * 
	 * @param expandLevel
	 *            the number of tree levels to expand
	 */
	void initializeCheckStates(int expandLevel) {
		viewer.addCheckStateListener(new CheckListCheckStateListener(viewer));
		if (rootItem.isChecked()) {
			viewer.expandAll();
			updateViewState(rootItem);
			viewer.collapseAll();
		}
		if (expandLevel >= 0) {
			viewer.expandToLevel(rootItem, expandLevel);
		}
	}

	/**
	 * Updates the view to match the given entry
	 * 
	 * @param entry
	 *            the entry to be checked
	 */
	private void updateViewState(CheckListDialogEntry entry) {
		if (entry.hasChildren()) {
			boolean individualChecksNeeded = false;
			for (CheckListDialogEntry child : entry) {
				if (child.hasChildren() || !child.isChecked()) {
					individualChecksNeeded = true;
					break;
				}
			}
			if (individualChecksNeeded) {
				boolean checked = false;
				boolean unchecked = false;
				for (CheckListDialogEntry child : entry) {
					updateViewState(child);
					if (child.isPartiallyChecked()) {
						unchecked = true;
						checked = true;
					} else if (child.isChecked()) {
						checked = true;
					} else {
						unchecked = true;
					}
				}
				if (checked && unchecked) {
					viewer.setGrayChecked(entry, true);
					entry.setPartiallyChecked(true);
					entry.setChecked(true);
				} else if (checked) {
					viewer.setChecked(entry, true);
					entry.setChecked(true);
				} else {
					// Viewer is already unchecked
					entry.setChecked(false);
				}
			} else {
				// If all children are checked, this is a lot faster
				viewer.setSubtreeChecked(entry, true);
				entry.setChecked(true);
			}
		} else {
			if (entry.isChecked()) {
				viewer.setChecked(entry, true);
			}
		}
	}
}