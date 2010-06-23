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
 * Handler for deactivating line count rule command
 *
 */
package com.nokia.traceviewer.action;

import java.net.URL;
import java.util.List;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.widgets.Table;
import org.eclipse.ui.PlatformUI;

import com.nokia.traceviewer.TraceViewerHelpContextIDs;
import com.nokia.traceviewer.TraceViewerPlugin;
import com.nokia.traceviewer.dialog.treeitem.LineCountTreeComponentItem;
import com.nokia.traceviewer.dialog.treeitem.LineCountTreeTextItem;
import com.nokia.traceviewer.engine.TraceViewerGlobals;
import com.nokia.traceviewer.engine.dataprocessor.LineCountItem;

/**
 * Handler for deactivating line count rule command
 */
public final class DeactivateLineCountRuleAction extends TraceViewerAction {

	/**
	 * Image for this Action
	 */
	private static ImageDescriptor image;

	/**
	 * Item index in TracePropertyView to be deactivated
	 */
	private int itemIndex;

	/**
	 * Line counting table
	 */
	private final Table lineCountingTable;

	static {
		URL url = null;
		url = TraceViewerPlugin.getDefault().getBundle().getEntry(
				"/icons/count.gif"); //$NON-NLS-1$
		image = ImageDescriptor.createFromURL(url);
	}

	/**
	 * Constructor
	 * 
	 * @param itemIndex
	 *            item index to be edited
	 * @param lineCountingTable
	 */
	public DeactivateLineCountRuleAction(int itemIndex, Table lineCountingTable) {
		setText(Messages
				.getString("DeactivateLineCountRuleAction.DeactivateRuleText")); //$NON-NLS-1$
		setToolTipText(Messages
				.getString("DeactivateLineCountRuleAction.DeactivateRuleToolTip")); //$NON-NLS-1$
		setImageDescriptor(image);

		this.itemIndex = itemIndex;
		this.lineCountingTable = lineCountingTable;

		// Set help
		PlatformUI.getWorkbench().getHelpSystem().setHelp(this,
				TraceViewerHelpContextIDs.ACTIONS);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.traceviewer.action.TraceViewerAction#doRun()
	 */
	@Override
	protected void doRun() {
		// Remove the item from the property view items
		List<LineCountItem> items = TraceViewerGlobals.getTraceViewer()
				.getDataProcessorAccess().getLineCountProcessor()
				.getLineCountItems();
		items.remove(itemIndex);

		// Remove from the actual table
		lineCountingTable.remove(itemIndex);

		// Get rule arrays
		List<LineCountTreeTextItem> textRules = TraceViewerGlobals
				.getTraceViewer().getDataProcessorAccess()
				.getLineCountProcessor().getTextRules();
		List<LineCountTreeComponentItem> componentRules = TraceViewerGlobals
				.getTraceViewer().getDataProcessorAccess()
				.getLineCountProcessor().getComponentRules();

		// Remove from the rule lists
		if (itemIndex < componentRules.size()) {
			componentRules.remove(itemIndex);
		} else {
			itemIndex -= componentRules.size();
			textRules.remove(itemIndex);
		}
	}
}
