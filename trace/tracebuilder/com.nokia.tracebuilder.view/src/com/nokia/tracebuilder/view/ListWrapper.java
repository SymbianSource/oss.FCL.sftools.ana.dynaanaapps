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
* Base class for wrappers containing a list of other wrappers
*
*/
package com.nokia.tracebuilder.view;

import java.util.ArrayList;
import java.util.Iterator;

import com.nokia.tracebuilder.engine.TraceBuilderConfiguration;

/**
 * Base class for wrappers containing a list of other wrappers
 * 
 */
abstract class ListWrapper extends WrapperBase implements Iterable<WrapperBase> {

	/**
	 * Number of visible elements in the tree viewer
	 */
	private final static int TREE_VIEW_VISIBLE_ELEMENTS = 100; // CodForChk_Dis_Magic

	/**
	 * The full list of wrappers.
	 */
	private ArrayList<WrapperBase> fullList = new ArrayList<WrapperBase>();

	/**
	 * Sublist which is shown in view
	 */
	private ArrayList<WrapperBase> subList = new ArrayList<WrapperBase>();

	/**
	 * Sub-list start index
	 */
	private int subListStartIndex = 0;

	/**
	 * Navigator if there are more children that can be shown in view at a time
	 */
	private ListNavigator navigator;

	/**
	 * Constructor takes the parent as parameter.
	 * 
	 * @param parent
	 *            the parent wrapper
	 * @param updater
	 *            the update notifier
	 */
	ListWrapper(WrapperBase parent, WrapperUpdater updater) {
		super(parent, updater);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.view.WrapperBase#getChildren()
	 */
	@Override
	Object[] getChildren() {
		return subList.toArray();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Iterable#iterator()
	 */
	public Iterator<WrapperBase> iterator() {
		return fullList.iterator();
	}

	/**
	 * Gets the wrappers that are currently visible
	 * 
	 * @return the wrappers
	 */
	Iterator<WrapperBase> getVisibleWrappers() {
		return subList.iterator();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.view.WrapperBase#hasChildren()
	 */
	@Override
	boolean hasChildren() {
		return !fullList.isEmpty();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.view.WrapperBase#delete()
	 */
	@Override
	void delete() {
		for (WrapperBase wrapper : fullList) {
			wrapper.delete();
		}
		fullList.clear();
		subList.clear();
	}

	/**
	 * Adds a child to this model
	 * 
	 * @param child
	 */
	void add(WrapperBase child) {
		fullList.add(child);
		if (getSubListSize() < TREE_VIEW_VISIBLE_ELEMENTS) {
			subList.add(child);
		} else {
			showSubList(fullList.size() - TREE_VIEW_VISIBLE_ELEMENTS);
		}
	}

	/**
	 * Removes the child from the list but does not call delete
	 * 
	 * @param child
	 *            the child to be removed
	 */
	void hide(WrapperBase child) {
		fullList.remove(child);
		subList.remove(child);
	}

	/**
	 * Checks if the wrapper is in this list
	 * 
	 * @param wrapper
	 *            the wrapper
	 * @return true if the wrapper exists, false otherwise
	 */
	boolean contains(WrapperBase wrapper) {
		return fullList.contains(wrapper);
	}

	/**
	 * Resets and removes the child
	 * 
	 * @param child
	 *            the child to be removed
	 */
	void remove(WrapperBase child) {
		child.delete();
		int index = fullList.indexOf(child);
		if (index >= 0) {
			fullList.remove(index);
			// If the item was removed prior to sublist, the sublist index is
			// changed. If item is in sublist, it is removed from it and a
			// replacement is added to the sub-list
			if (index < subListStartIndex) {
				subListStartIndex--;
			} else if (index < subListStartIndex + getSubListSize()) {
				removeFromSubList(child);
			}
		}
	}

	/**
	 * Removes the element from the visible list and replaces it with a new one
	 * 
	 * @param child
	 *            the child element to be removed
	 */
	private void removeFromSubList(WrapperBase child) {
		subList.remove(child);
		if (fullList.size() > getSubListSize()) {
			int end = subListStartIndex + getSubListSize();
			// If the sub-list is at the end, previous element is added to it
			// Otherwise the next element is added prior to the navigator
			if (end == fullList.size()) {
				subList.add(0, fullList.get(subListStartIndex - 1));
				subListStartIndex--;
			} else {
				subList.add(getSubListSize() - 1, fullList.get(end));
			}
		} else if (!subList.isEmpty()) {
			// If the sub-list covers the entire list, the navigator is removed
			// if it exists
			if (subList.get(subList.size() - 1) == navigator) {
				subList.remove(subList.size() - 1);
			}
		}
	}

	/**
	 * Removes and calls delete on all children
	 */
	void clear() {
		for (WrapperBase wrapper : fullList) {
			wrapper.delete();
		}
		fullList.clear();
		subList.clear();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.view.WrapperBase#setInView(boolean)
	 */
	@Override
	void setInView(boolean flag) {
		super.setInView(flag);
		// When an element is hidden, all its children are also hidden
		// When element is shown, its children are not necessarily shown
		if (!flag) {
			setChildrenInView(false);
		} else {
			restoreChildrenIntoView();
		}
	}

	/**
	 * Sets the shown in view flag for child elements that are currently shown
	 * in view
	 * 
	 * @param isInView
	 *            the shown in view flag
	 */
	void setChildrenInView(boolean isInView) {
		for (WrapperBase wrapper : subList) {
			wrapper.setInView(isInView);
		}
	}

	/**
	 * Sets the shown in view flag for child elements that have the
	 * restoreIntoView flag
	 */
	void restoreChildrenIntoView() {
		if (isRestoredIntoView()) {
			super.setInView(true);
			super.setRestoreIntoView(false);
		}
		for (WrapperBase wrapper : subList) {
			if (wrapper instanceof ListWrapper) {
				((ListWrapper) wrapper).restoreChildrenIntoView();
			} else {
				if (wrapper.isRestoredIntoView()) {
					wrapper.setRestoreIntoView(false);
					wrapper.setInView(true);
				}
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.view.WrapperBase#setRestoreIntoView(boolean)
	 */
	@Override
	void setRestoreIntoView(boolean flag) {
		super.setRestoreIntoView(flag);
		for (WrapperBase wrapper : subList) {
			wrapper.setRestoreIntoView(flag);
		}
	}

	/**
	 * Updates the sub-list in view
	 * 
	 * @param start
	 *            the start of sub-list
	 */
	private void showSubList(int start) {
		subList.clear();
		if (start < 0) {
			start = 0;
		} else if (start > fullList.size() - TREE_VIEW_VISIBLE_ELEMENTS) {
			start = fullList.size() - TREE_VIEW_VISIBLE_ELEMENTS;
		}
		subListStartIndex = start;
		int end = subListStartIndex + TREE_VIEW_VISIBLE_ELEMENTS;
		for (int i = 0; i < fullList.size(); i++) {
			if (i >= start && i < end) {
				subList.add(fullList.get(i));
			} else {
				fullList.get(i).setInView(false);
			}
		}
		if (navigator == null) {
			createNavigator();
		}
		navigator
				.setIndex(subListStartIndex, getSubListSize(), fullList.size());
		subList.add(navigator);
	}

	/**
	 * Creates the navigator
	 */
	private void createNavigator() {
		navigator = new ListNavigator(this, getUpdater());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.view.WrapperBase# dumpToSystemOut(int,
	 *      com.nokia.tracebuilder.view.TraceLabelProvider)
	 */
	@Override
	void dumpToSystemOut(int indentLevel, TraceLabelProvider provider) {
		if (TraceBuilderConfiguration.TRACE_VIEW_STATE) {
			super.dumpToSystemOut(indentLevel, provider);
			indentLevel++;
			// This uses the full list -> Verifies that items do not contain
			// visible flag unless they actually are in the sub-list and visible
			for (WrapperBase wrapper : fullList) {
				if (wrapper.isInView()) {
					wrapper.dumpToSystemOut(indentLevel, provider);
				}
			}
		}
	}

	/**
	 * Shows the next elements in the list
	 */
	void showNext() {
		int start = subListStartIndex + TREE_VIEW_VISIBLE_ELEMENTS;
		showSubList(start);
		// showSubList does not change the in-view flag to true, since it is
		// used also with hidden lists
		int size = getSubListSize();
		for (int i = 0; i < size; i++) {
			subList.get(i).setInView(true);
		}
		getUpdater().update(this);
	}

	/**
	 * Shows the previous elements in the list
	 */
	void showPrevious() {
		int start = subListStartIndex - TREE_VIEW_VISIBLE_ELEMENTS;
		showSubList(start);
		// showSubList does not change the in-view flag to true, since it is
		// used also with hidden lists
		int size = getSubListSize();
		for (int i = 0; i < size; i++) {
			subList.get(i).setInView(true);
		}
		getUpdater().update(this);
	}

	/**
	 * Gets the size of the sub-list
	 * 
	 * @return the size
	 */
	private int getSubListSize() {
		int size = subList.size();
		if (size > 0) {
			if (subList.get(size - 1) == navigator) {
				size--;
			}
		}
		return size;
	}

	/**
	 * Moves a child wrapper to the sub-list that is shown in view. Note that
	 * this does not expand the tree to actually show the child
	 * 
	 * @param child
	 *            the child to be revealed
	 * @return the wrapper that needs to be updated
	 */
	WrapperBase moveChildToView(WrapperBase child) {
		WrapperBase retval = null;
		if (!child.isInView()) {
			int index = fullList.indexOf(child);
			if (index < subListStartIndex
					|| index >= subListStartIndex + getSubListSize()) {
				showSubList(index);
				// If the list changes, this wrapper needs to be updated
				retval = this;
			}
		}
		WrapperBase parent = getParent();
		if (parent instanceof ListWrapper) {
			// Delegates the call also to parent lists in case this list is not
			// currently in the view. If a parent list changes, the return value
			// is changed so it gets updated instead of this list
			WrapperBase updatedParent = ((ListWrapper) parent)
					.moveChildToView(this);
			if (updatedParent != null) {
				retval = updatedParent;
			}
		}
		return retval;
	}

}
