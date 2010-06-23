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
 * Handler for trigger command
 *
 */
package com.nokia.traceviewer.action;

import java.net.URL;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.PlatformUI;

import com.nokia.traceviewer.TraceViewerHelpContextIDs;
import com.nokia.traceviewer.TraceViewerPlugin;
import com.nokia.traceviewer.engine.TraceViewerDialog;
import com.nokia.traceviewer.engine.TraceViewerGlobals;

/**
 * Handler for trigger command
 * 
 */
final class TriggerAction extends TraceViewerAction {

	/**
	 * Image for the action
	 */
	private static ImageDescriptor image;

	static {
		URL url = null;
		url = TraceViewerPlugin.getDefault().getBundle().getEntry(
				"/icons/trigger.gif"); //$NON-NLS-1$
		image = ImageDescriptor.createFromURL(url);
	}

	/**
	 * Constructor
	 */
	TriggerAction() {
		setText(Messages.getString("TriggerAction.Title")); //$NON-NLS-1$
		setToolTipText(Messages.getString("TriggerAction.Tooltip")); //$NON-NLS-1$
		setImageDescriptor(image);
		setActionDefinitionId("com.nokia.traceviewer.command.trigger"); //$NON-NLS-1$

		// Set help
		PlatformUI.getWorkbench().getHelpSystem().setHelp(this,
				TraceViewerHelpContextIDs.TRIGGERING);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.traceviewer.action.TraceViewerAction#doRun()
	 */
	@Override
	protected void doRun() {
		TraceViewerGlobals.postUiEvent("TriggerButton", "1"); //$NON-NLS-1$ //$NON-NLS-2$
		TraceViewerDialog dialog = TraceViewerGlobals.getTraceViewer()
				.getDataProcessorAccess().getTriggerProcessor()
				.getTriggerDialog();
		dialog.openDialog();
		TraceViewerGlobals.postUiEvent("TriggerButton", "0"); //$NON-NLS-1$ //$NON-NLS-2$
	}
}
