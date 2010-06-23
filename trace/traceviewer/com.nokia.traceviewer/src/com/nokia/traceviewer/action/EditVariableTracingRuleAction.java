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
 * Handler for edit variable tracing rule command
 *
 */
package com.nokia.traceviewer.action;

import java.net.URL;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.PlatformUI;

import com.nokia.traceviewer.TraceViewerHelpContextIDs;
import com.nokia.traceviewer.TraceViewerPlugin;
import com.nokia.traceviewer.dialog.VariableTracingDialog;
import com.nokia.traceviewer.engine.TraceViewerGlobals;

/**
 * Handler for edit variable tracing rule command
 */
public final class EditVariableTracingRuleAction extends TraceViewerAction {

	/**
	 * Image for this Action
	 */
	private static ImageDescriptor image;

	/**
	 * Item index in TracePropertyView to be edited
	 */
	private final int itemIndex;

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
	 */
	public EditVariableTracingRuleAction(int itemIndex) {
		setText(Messages.getString("EditVariableTracingRuleAction.Title")); //$NON-NLS-1$
		setToolTipText(Messages
				.getString("EditVariableTracingRuleAction.Tooltip")); //$NON-NLS-1$
		setImageDescriptor(image);

		this.itemIndex = itemIndex;

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
		VariableTracingDialog dialog = TraceViewerGlobals.getTraceViewer()
				.getDataProcessorAccess().getVariableTracingProcessor()
				.getVariableTracingDialog();
		if (dialog != null) {
			dialog.editOrAddItem(itemIndex);
		}
	}
}
