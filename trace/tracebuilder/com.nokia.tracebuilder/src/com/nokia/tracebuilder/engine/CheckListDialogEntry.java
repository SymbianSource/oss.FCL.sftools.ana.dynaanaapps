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
* Check list dialog entry
*
*/
package com.nokia.tracebuilder.engine;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * Check list dialog entry
 * 
 */
public class CheckListDialogEntry implements Iterable<CheckListDialogEntry> {

	/**
	 * The object
	 */
	private Object object;

	/**
	 * Checked flag for the object
	 */
	private boolean isChecked;

	/**
	 * Children of this entry
	 */
	private List<CheckListDialogEntry> children;

	/**
	 * Parent entry
	 */
	private CheckListDialogEntry parent;

	/**
	 * Partially checked flag is set if some of the children are checked and
	 * some are not
	 */
	private boolean partiallyChecked;

	/**
	 * Gets the parent of this entry
	 * 
	 * @return the parent entry
	 */
	public CheckListDialogEntry getParent() {
		return parent;
	}

	/**
	 * Gets the checked flag
	 * 
	 * @return the flag
	 */
	public boolean isChecked() {
		return isChecked;
	}

	/**
	 * Sets the checked flag
	 * 
	 * @param isChecked
	 *            the new flag value
	 */
	public void setChecked(boolean isChecked) {
		this.isChecked = isChecked;
	}

	/**
	 * Gets the object
	 * 
	 * @return the object
	 */
	public Object getObject() {
		return object;
	}

	/**
	 * Sets the object
	 * 
	 * @param object
	 *            the object to set
	 */
	public void setObject(Object object) {
		this.object = object;
	}

	/**
	 * Adds a child entry
	 * 
	 * @param child
	 *            the child entry to be added
	 */
	public void addChild(CheckListDialogEntry child) {
		if (children == null) {
			children = new ArrayList<CheckListDialogEntry>();
		}
		children.add(child);
		child.parent = this;
	}

	/**
	 * Gets the child entries
	 * 
	 * @return the children
	 */
	public Iterator<CheckListDialogEntry> getChildren() {
		List<CheckListDialogEntry> list;
		if (children != null) {
			list = children;
		} else {
			list = Collections.emptyList();
		}
		return list.iterator();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Iterable#iterator()
	 */
	public Iterator<CheckListDialogEntry> iterator() {
		return getChildren();
	}

	/**
	 * Checks if this entry has children
	 * 
	 * @return true if there are children
	 */
	public boolean hasChildren() {
		return children != null && !children.isEmpty();
	}

	/**
	 * Converts the children to array
	 * 
	 * @return the children as array
	 */
	public Object[] childrenToArray() {
		Object[] retval;
		if (children != null) {
			retval = children.toArray();
		} else {
			retval = new Object[0];
		}
		return retval;
	}

	/**
	 * Gets the number of child elements
	 * 
	 * @return the child count
	 */
	public int getChildCount() {
		return children != null ? children.size() : 0;
	}

	/**
	 * Partially checked flag
	 * 
	 * @return true if some of the children is checked and some are not
	 */
	public boolean isPartiallyChecked() {
		return partiallyChecked;
	}

	/**
	 * Sets the partially checked flag
	 * 
	 * @param checked
	 *            the flag value
	 */
	public void setPartiallyChecked(boolean checked) {
		partiallyChecked = checked;
	}

}