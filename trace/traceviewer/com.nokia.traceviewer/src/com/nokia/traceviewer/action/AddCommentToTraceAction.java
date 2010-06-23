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
 * Handler for add comment to trace command
 *
 */
package com.nokia.traceviewer.action;

import java.net.URL;

import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;

import com.nokia.traceviewer.TraceViewerHelpContextIDs;
import com.nokia.traceviewer.TraceViewerPlugin;
import com.nokia.traceviewer.engine.TraceViewerGlobals;
import com.nokia.traceviewer.view.listener.SelectionProperties;

/**
 * Handler for add comment to trace command
 */
public final class AddCommentToTraceAction extends TraceViewerAction {

	/**
	 * Image for this Action
	 */
	private static ImageDescriptor image;

	/**
	 * Trace number
	 */
	private final int traceNumber;

	static {
		URL url = null;
		url = TraceViewerPlugin.getDefault().getBundle().getEntry(
				"/icons/count.gif"); //$NON-NLS-1$
		image = ImageDescriptor.createFromURL(url);
	}

	/**
	 * Constructor
	 * 
	 * @param traceNumber
	 *            trace number
	 */
	public AddCommentToTraceAction(int traceNumber) {
		this.traceNumber = traceNumber;
		setText(Messages
				.getString("AddCommentToTraceAction.AddCommentToTraceText")); //$NON-NLS-1$
		setToolTipText(Messages
				.getString("AddCommentToTraceAction.AddCommentToTraceToolTip")); //$NON-NLS-1$
		setImageDescriptor(image);

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
		// Show dialog
		InputDialog dialog = new InputDialog(
				Display.getCurrent().getActiveShell(),
				Messages
						.getString("AddCommentToTraceAction.AddCommentShellTitle"), //$NON-NLS-1$
				Messages.getString("AddCommentToTraceAction.AddCommentTipText"), null, new TraceCommentValidator()); //$NON-NLS-1$

		if (dialog.open() == Window.OK) {
			String traceComment = dialog.getValue();

			// First clear selections as it looks stupid when comment is added
			// after the trace
			SelectionProperties.clear();

			// Add to the list
			TraceViewerGlobals.getTraceViewer().getDataProcessorAccess()
					.getTraceCommentHandler().insertComment(traceNumber,
							traceComment);
		}
	}

}
