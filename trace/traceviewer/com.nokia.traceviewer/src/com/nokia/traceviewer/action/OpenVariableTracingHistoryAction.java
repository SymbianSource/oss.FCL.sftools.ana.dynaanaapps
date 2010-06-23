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
 * Handler for open variable tracing history command
 *
 */
package com.nokia.traceviewer.action;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;

import com.nokia.traceviewer.TraceViewerHelpContextIDs;
import com.nokia.traceviewer.dialog.VariableTracingHistoryDialog;
import com.nokia.traceviewer.engine.TraceViewerGlobals;
import com.nokia.traceviewer.engine.TraceViewerDialogInterface.Dialog;

/**
 * Handler for open variable tracing history command
 * 
 */
final class OpenVariableTracingHistoryAction extends TraceViewerAction {

	/**
	 * Image for this Action
	 */
	private static ImageDescriptor image;

	/**
	 * Variable Tracing History dialog
	 */
	private VariableTracingHistoryDialog dialog;

	static {
		image = PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(
				ISharedImages.IMG_OBJS_INFO_TSK);
	}

	/**
	 * Constructor
	 */
	OpenVariableTracingHistoryAction() {
		setText(Messages.getString("OpenVariableTracingHistoryAction.Title")); //$NON-NLS-1$
		setToolTipText(Messages
				.getString("OpenVariableTracingHistoryAction.Tooltip")); //$NON-NLS-1$
		setImageDescriptor(image);

		// Set help
		PlatformUI.getWorkbench().getHelpSystem().setHelp(this,
				TraceViewerHelpContextIDs.VARIABLE_TRACING_HISTORY);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.traceviewer.action.TraceViewerAction#doRun()
	 */
	@Override
	protected void doRun() {
		// Create the dialog
		if (dialog == null) {
			dialog = (VariableTracingHistoryDialog) TraceViewerGlobals
					.getTraceViewer().getDialogs().createDialog(
							Dialog.VARIBLETRACINGHISTORY);
			if (dialog != null) {
				dialog.openDialog();
			}
			// Open the dialog
		} else if (!dialog.isOpen()) {
			dialog.openDialog();
			// Set focus to the dialog
		} else {
			dialog.setFocus();
		}
	}
}
