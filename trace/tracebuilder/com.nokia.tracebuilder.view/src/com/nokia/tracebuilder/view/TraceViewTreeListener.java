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
* Selection listener for tree viewer
*
*/
package com.nokia.tracebuilder.view;

import java.util.Iterator;

import org.eclipse.jface.viewers.ITreeViewerListener;
import org.eclipse.jface.viewers.TreeExpansionEvent;
import org.eclipse.jface.viewers.TreeViewer;

import com.nokia.tracebuilder.engine.TraceBuilderConfiguration;

/**
 * Tree expansion listener
 * 
 */
final class TraceViewTreeListener implements ITreeViewerListener {

	/**
	 * The tree viewer
	 */
	private TreeViewer viewer;

	/**
	 * Constructor
	 * 
	 * @param viewer
	 *            the tree viewer
	 */
	TraceViewTreeListener(TreeViewer viewer) {
		this.viewer = viewer;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.ITreeViewerListener#
	 *      treeCollapsed(org.eclipse.jface.viewers.TreeExpansionEvent)
	 */
	public void treeCollapsed(TreeExpansionEvent event) {
		ListWrapper wrapper = (ListWrapper) event.getElement();
		// The visible child wrappers of the collapsed wrapper are marked with
		// the "restore into view" flag. When the wrapper is re-opened, its
		// visible children are also re-opened
		Iterator<WrapperBase> itr = wrapper.getVisibleWrappers();
		while (itr.hasNext()) {
			itr.next().setRestoreIntoView(true);
		}
		// Children of collapsed wrapper are no longer in view
		wrapper.setChildrenInView(false);
		if (TraceBuilderConfiguration.TRACE_VIEW_STATE) {
			((TraceContentProvider) viewer.getContentProvider())
					.dumpToSystemOut(((TraceLabelProvider) viewer
							.getLabelProvider()));
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.ITreeViewerListener#
	 *      treeExpanded(org.eclipse.jface.viewers.TreeExpansionEvent)
	 */
	public void treeExpanded(TreeExpansionEvent event) {
		final ListWrapper wrapper = (ListWrapper) event.getElement();
		if (wrapper instanceof TraceWrapper
				|| wrapper instanceof TraceGroupWrapper) {
			// Collapses other wrappers and marks them as not shown in
			// view -> Optimizes the location list updates
			wrapper.collapseSiblings(viewer);
		}
		// The child wrappers of the expanded wrapper are now in view
		Iterator<WrapperBase> itr = wrapper.getVisibleWrappers();
		while (itr.hasNext()) {
			WrapperBase child = itr.next();
			child.setInView(true);
			// The children of the expanded wrapper are updated since they might
			// have been changed while hidden
			child.getUpdater().queueUpdate(child);
		}
		if (TraceBuilderConfiguration.TRACE_VIEW_STATE) {
			((TraceContentProvider) viewer.getContentProvider())
					.dumpToSystemOut(((TraceLabelProvider) viewer
							.getLabelProvider()));
		}
	}

}