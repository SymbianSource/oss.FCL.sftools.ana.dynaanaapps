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
* List navigator, which is used when a list contains more elements than should be shown at a time
*
*/
package com.nokia.tracebuilder.view;

import com.nokia.tracebuilder.action.TraceViewActionProvider;

/**
 * List navigator, which is used when a list contains more elements than should
 * be shown at a time
 * 
 */
final class ListNavigator extends WrapperBase {

	/**
	 * Action provider
	 */
	private ListNavigatorActionProvider actionProvider;

	/**
	 * Index of the first element currently visible
	 */
	private int startIndex;

	/**
	 * Number of visible elements
	 */
	private int visibleCount;

	/**
	 * Total number of elements
	 */
	private int totalCount;

	/**
	 * Constructor
	 * 
	 * @param parent
	 *            the parent wrapper
	 * @param updater
	 *            the update notifier
	 */
	protected ListNavigator(ListWrapper parent, WrapperUpdater updater) {
		super(parent, updater);
		actionProvider = new ListNavigatorActionProvider(parent);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.view.WrapperBase#getChildren()
	 */
	@Override
	public Object[] getChildren() {
		return new Object[0];
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.view.WrapperBase#hasChildren()
	 */
	@Override
	public boolean hasChildren() {
		return false;
	}

	/**
	 * Gets the start index of visible elements
	 * 
	 * @return the start index
	 */
	int getStartIndex() {
		return startIndex;
	}

	/**
	 * Gets the number of visible elements
	 * 
	 * @return the visible element count
	 */
	int getVisibleCount() {
		return visibleCount;
	}

	/**
	 * Gets the total length of elements
	 * 
	 * @return the total count
	 */
	int getTotalCount() {
		return totalCount;
	}

	/**
	 * Gets the action provider interface
	 * 
	 * @return the action provider
	 */
	public TraceViewActionProvider getActionProvider() {
		return actionProvider;
	}

	/**
	 * Sets the index to currently visible area
	 * 
	 * @param startIndex
	 * @param visibleCount
	 * @param totalCount
	 */
	void setIndex(int startIndex, int visibleCount, int totalCount) {
		this.startIndex = startIndex;
		this.visibleCount = visibleCount;
		this.totalCount = totalCount;
		if (startIndex + visibleCount >= totalCount) {
			actionProvider.setNextEnabled(false);
			actionProvider.setPreviousEnabled(true);
		} else if (startIndex == 0) {
			actionProvider.setNextEnabled(true);
			actionProvider.setPreviousEnabled(false);
		} else {
			actionProvider.setNextEnabled(true);
			actionProvider.setPreviousEnabled(true);
		}
	}

}
