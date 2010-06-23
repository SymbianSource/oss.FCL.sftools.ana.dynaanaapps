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
 * Tree Checkbox State Listener class
 *
 */
package com.nokia.traceviewer.dialog.treeitem;

import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.ICheckStateListener;

import com.nokia.traceviewer.engine.TraceViewerGlobals;

/**
 * Tree Checkbox State Listener class
 */
public class TreeCheckboxStateListener implements ICheckStateListener {

	/**
	 * CheckboxTreeViewer
	 */
	private final CheckboxTreeViewer viewer;

	/**
	 * Constructor
	 * 
	 * @param viewer
	 *            checkboxviewer
	 */
	public TreeCheckboxStateListener(CheckboxTreeViewer viewer) {
		this.viewer = viewer;
	}

	public void checkStateChanged(CheckStateChangedEvent event) {
		// Check and uncheck children
		if (event.getChecked()) {
			viewer.setSubtreeChecked(event.getElement(), true);
		} else {
			viewer.setSubtreeChecked(event.getElement(), false);
		}
		viewer.setGrayed(event.getElement(), false);

		// Set something changed variable to color dialog
		if (event.getElement() instanceof ColorTreeItem) {
			TraceViewerGlobals.getTraceViewer().getDataProcessorAccess()
					.getColorer().getColorDialog().setSomethingChanged(true);

			// Set something changed variable to filter dialog
		} else if (event.getElement() instanceof FilterTreeItem) {
			TraceViewerGlobals.getTraceViewer().getDataProcessorAccess()
					.getFilterProcessor().getFilterDialog()
					.setSomethingChanged(true);

			// Set something changed variable to linecount dialog
		} else if (event.getElement() instanceof LineCountTreeItem) {
			TraceViewerGlobals.getTraceViewer().getDataProcessorAccess()
					.getLineCountProcessor().getLineCountDialog()
					.setSomethingChanged(true);

			// Set something changed variable to variableTracing dialog
		} else if (event.getElement() instanceof VariableTracingTreeItem) {
			TraceViewerGlobals.getTraceViewer().getDataProcessorAccess()
					.getVariableTracingProcessor().getVariableTracingDialog()
					.setSomethingChanged(true);

			// Set something changed variable to trigger dialog
		} else if (event.getElement() instanceof TriggerTreeItem) {
			TraceViewerGlobals.getTraceViewer().getDataProcessorAccess()
					.getTriggerProcessor().getTriggerDialog()
					.setSomethingChanged(true);
		}

		// Check parents if needed
		checkStateChanged((TreeItem) event.getElement());
	}

	/**
	 * Makes needed changes to viewer if checked state was changed manually
	 * 
	 * @param item
	 *            item which has checked state changed
	 */
	public void checkStateChanged(TreeItem item) {
		TreeItem parent = (TreeItem) item.getParent();
		// Item checked
		if (viewer.getChecked(item) && !viewer.getGrayed(item)
				&& parent != null) {

			// If all children checked, check parent also
			if (allChildrenChecked(parent)) {
				viewer.setChecked(parent, true);
				viewer.setGrayed(parent, false);
				// Only grey check
			} else {
				viewer.setGrayChecked(parent, true);
			}

			checkStateChanged(parent);

			// Item unchecked
		} else if (!viewer.getChecked(item) && parent != null) {

			// If any children still checked
			if (anyChildrenChecked(parent)) {
				viewer.setGrayChecked(parent, true);
			} else {
				viewer.setGrayChecked(parent, false);
			}
			checkStateChanged(parent);
		} else if (viewer.getChecked(item) && viewer.getGrayed(item)
				&& parent != null) {

			viewer.setGrayChecked(parent, true);
			checkStateChanged(parent);
		}
	}

	/**
	 * Tells if given item has any children checked
	 * 
	 * @param item
	 *            item to investigate
	 * @return true if this item has any children checked
	 */
	public boolean anyChildrenChecked(TreeItem item) {
		boolean isAnyChecked = false;

		Object[] children = item.getChildren();

		for (int i = 0; i < children.length; i++) {
			if (viewer.getChecked(children[i])) {
				isAnyChecked = true;
				break;
			}
		}
		return isAnyChecked;
	}

	/**
	 * Tells if given item has all children checked
	 * 
	 * @param item
	 *            item to investigate
	 * @return true if this item has all children checked
	 */
	public boolean allChildrenChecked(TreeItem item) {
		boolean isAllChecked = true;

		Object[] children = item.getChildren();

		for (int i = 0; i < children.length; i++) {
			if (!viewer.getChecked(children[i])
					&& !viewer.getGrayed(children[i])) {
				isAllChecked = false;
				break;
			}
		}
		return isAllChecked;
	}
}
