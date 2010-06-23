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
* State listener for tree viewer check boxes
*
*/
package com.nokia.tracebuilder.view;

import java.util.Iterator;

import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.ICheckStateListener;

import com.nokia.tracebuilder.engine.CheckListDialogEntry;

/**
 * State listener for tree viewer check boxes
 * 
 */
final class CheckListCheckStateListener implements ICheckStateListener {

	/**
	 * Content provider
	 */
	private final CheckboxTreeViewer viewer;

	/**
	 * Constructor
	 * 
	 * @param viewer
	 *            the viewer
	 */
	CheckListCheckStateListener(CheckboxTreeViewer viewer) {
		this.viewer = viewer;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.ICheckStateListener#
	 *      checkStateChanged(org.eclipse.jface.viewers.CheckStateChangedEvent)
	 */
	public void checkStateChanged(CheckStateChangedEvent event) {
		CheckListDialogEntry entry = (CheckListDialogEntry) event.getElement();
		boolean newState = event.getChecked();
		// View update is not needed with the entry that was clicked by user
		checkEntryAndRelatives(entry, newState, false);
	}

	/**
	 * Changes the state of an entry, its parent and children
	 * 
	 * @param entry
	 *            the entry
	 * @param newState
	 *            the new entry state
	 * @param updateView
	 *            true if view needs to be updated
	 * @return true if entries were changed
	 */
	private boolean checkEntryAndRelatives(CheckListDialogEntry entry,
			boolean newState, boolean updateView) {
		boolean changed = checkEntry(entry, newState, updateView);
		if (changed) {
			viewer.setSubtreeChecked(entry, newState);
			checkChildren(entry, newState);
			checkParent(entry);
		}
		return changed;
	}

	/**
	 * Checks the children of given entry
	 * 
	 * @param entry
	 *            the entry
	 * @param newState
	 *            the new state
	 */
	private void checkChildren(CheckListDialogEntry entry, boolean newState) {
		for (CheckListDialogEntry child : entry) {
			child.setChecked(newState);
			checkChildren(child, newState);
		}
	}

	/**
	 * Checks an entry and unchecks the partially checked flag if it exists
	 * 
	 * @param entry
	 *            the entry
	 * @param newState
	 *            the new entry state
	 * @param updateView
	 *            true if view needs to be updated
	 * @return true if entry was changed
	 */
	private boolean checkEntry(CheckListDialogEntry entry, boolean newState,
			boolean updateView) {
		boolean retval;
		if (entry.isChecked() != newState) {
			entry.setChecked(newState);
			if (entry.isPartiallyChecked()) {
				entry.setPartiallyChecked(false);
				viewer.setGrayChecked(entry, false);
			}
			if (updateView) {
				viewer.setChecked(entry, newState);
			}
			retval = true;
		} else {
			retval = false;
		}
		return retval;
	}

	/**
	 * Recursively checks the parent of the checked element. If all children are
	 * checked, the parent is checked. If all children are unchecked, the parent
	 * is unchecked. If children are partially checked, the parent is grayed.
	 * 
	 * @param entry
	 *            the entry whose parent needs to be checked
	 */
	private void checkParent(CheckListDialogEntry entry) {
		CheckListDialogEntry parent = entry.getParent();
		if (parent != null && parent.hasChildren()) {
			boolean checked = false;
			boolean unchecked = false;
			boolean partiallyChecked = false;
			Iterator<CheckListDialogEntry> children = parent.getChildren();
			while (children.hasNext() && !partiallyChecked) {
				CheckListDialogEntry child = children.next();
				boolean childChecked = child.isChecked();
				boolean childPartial = child.isPartiallyChecked();
				if (childPartial) {
					partiallyChecked = true;
				} else if (childChecked) {
					checked = true;
					if (unchecked) {
						partiallyChecked = true;
					}
				} else {
					unchecked = true;
					if (checked) {
						partiallyChecked = true;
					}
				}
			}
			if (partiallyChecked) {
				if (!parent.isPartiallyChecked()) {
					viewer.setGrayChecked(parent, true);
					parent.setPartiallyChecked(true);
				} else if (!parent.isChecked()) {
					viewer.setChecked(parent, true);
				}
				// Checked flag needs to be set in both cases
				parent.setChecked(true);
			} else if (checked) {
				if (parent.isPartiallyChecked()) {
					viewer.setGrayed(parent, false);
					parent.setPartiallyChecked(false);
				}
				if (!parent.isChecked()) {
					viewer.setChecked(parent, true);
					parent.setChecked(true);
				}
			} else {
				if (parent.isPartiallyChecked()) {
					viewer.setGrayChecked(parent, false);
					parent.setPartiallyChecked(false);
				} else if (parent.isChecked()) {
					viewer.setChecked(parent, false);
				}
				// Checked flag needs to be set in both cases
				parent.setChecked(false);
			}
			checkParent(parent);
		}
	}
}
