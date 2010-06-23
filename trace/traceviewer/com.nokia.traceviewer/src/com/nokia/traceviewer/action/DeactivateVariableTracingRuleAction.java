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
 * Handler for deactivating variable tracing rule command
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
import com.nokia.traceviewer.dialog.treeitem.VariableTracingTreeTextItem;
import com.nokia.traceviewer.engine.TraceViewerGlobals;
import com.nokia.traceviewer.engine.dataprocessor.VariableTracingItem;

/**
 * Handler for deactivating variable tracing rule command
 */
public final class DeactivateVariableTracingRuleAction extends
		TraceViewerAction {

	/**
	 * Image for this Action
	 */
	private static ImageDescriptor image;

	/**
	 * Item index in TracePropertyView to be deactivated
	 */
	private final int itemIndex;

	/**
	 * Variable tracing table
	 */
	private final Table variableTracingTable;

	static {
		URL url = null;
		url = TraceViewerPlugin.getDefault().getBundle().getEntry(
				"/icons/tracevariable.gif"); //$NON-NLS-1$
		image = ImageDescriptor.createFromURL(url);
	}

	/**
	 * Constructor
	 * 
	 * @param itemIndex
	 *            item index to be edited
	 * @param variableTracingTable
	 *            table
	 */
	public DeactivateVariableTracingRuleAction(int itemIndex,
			Table variableTracingTable) {
		setText(Messages
				.getString("DeactivateVariableTracingRuleAction.DeactivateRuleText")); //$NON-NLS-1$
		setToolTipText(Messages
				.getString("DeactivateVariableTracingRuleAction.DeactivateRuleToolTip")); //$NON-NLS-1$
		setImageDescriptor(image);

		this.itemIndex = itemIndex;
		this.variableTracingTable = variableTracingTable;

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
		List<VariableTracingItem> items = TraceViewerGlobals.getTraceViewer()
				.getDataProcessorAccess().getVariableTracingProcessor()
				.getVariableTracingItems();
		items.remove(itemIndex);

		// Remove from the actual table
		variableTracingTable.remove(itemIndex);

		// Remove from the rule list
		List<VariableTracingTreeTextItem> textRules = TraceViewerGlobals
				.getTraceViewer().getDataProcessorAccess()
				.getVariableTracingProcessor().getTextRules();

		textRules.remove(itemIndex);
	}
}
