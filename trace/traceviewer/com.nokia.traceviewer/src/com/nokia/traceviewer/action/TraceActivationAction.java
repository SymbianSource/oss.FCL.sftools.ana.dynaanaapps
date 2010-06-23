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
 * Handler for trace activation command
 *
 */
package com.nokia.traceviewer.action;

import java.net.URL;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.PlatformUI;

import com.nokia.traceviewer.TraceViewerHelpContextIDs;
import com.nokia.traceviewer.TraceViewerPlugin;
import com.nokia.traceviewer.dialog.TraceActivationDialog;
import com.nokia.traceviewer.engine.TraceViewerGlobals;
import com.nokia.traceviewer.engine.TraceViewerDialogInterface.Dialog;

/**
 * Handler for trace activation command
 * 
 */
public final class TraceActivationAction extends TraceViewerAction {

	/**
	 * Trace activation dialog
	 */
	private TraceActivationDialog dialog;

	/**
	 * Image for the action
	 */
	private static ImageDescriptor image;

	static {
		URL url = null;
		url = TraceViewerPlugin.getDefault().getBundle().getEntry(
				"/icons/traceactivation.gif"); //$NON-NLS-1$
		image = ImageDescriptor.createFromURL(url);
	}

	/**
	 * Constructor
	 */
	TraceActivationAction() {
		setText(Messages.getString("TraceActivationAction.Title")); //$NON-NLS-1$
		setToolTipText(Messages.getString("TraceActivationAction.Tooltip")); //$NON-NLS-1$
		setImageDescriptor(image);
		setActionDefinitionId("com.nokia.traceviewer.command.trace_activation"); //$NON-NLS-1$

		// Set help
		PlatformUI.getWorkbench().getHelpSystem().setHelp(this,
				TraceViewerHelpContextIDs.ACTIVATION_DIALOG);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.traceviewer.action.TraceViewerAction#doRun()
	 */
	@Override
	protected void doRun() {
		TraceViewerGlobals.postUiEvent("TraceActivationButton", "1"); //$NON-NLS-1$ //$NON-NLS-2$
		if (dialog == null) {
			dialog = (TraceActivationDialog) TraceViewerGlobals
					.getTraceViewer().getDialogs().createDialog(
							Dialog.TRACEACTIVATION);
			dialog.openDialog();
		} else if (!dialog.isOpen()) {
			dialog.openDialog();
		} else {
			dialog.setFocus();
		}
		TraceViewerGlobals.postUiEvent("TraceActivationButton", "0"); //$NON-NLS-1$ //$NON-NLS-2$
	}

	/**
	 * Gets trace activation dialog
	 * 
	 * @return the dialog
	 */
	public TraceActivationDialog getDialog() {
		return dialog;
	}
}
