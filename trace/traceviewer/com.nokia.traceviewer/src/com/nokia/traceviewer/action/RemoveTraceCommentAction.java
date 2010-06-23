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
 * Handler for remove trace comment command
 *
 */
package com.nokia.traceviewer.action;

import java.net.URL;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.PlatformUI;

import com.nokia.traceviewer.TraceViewerHelpContextIDs;
import com.nokia.traceviewer.TraceViewerPlugin;
import com.nokia.traceviewer.engine.TraceViewerGlobals;
import com.nokia.traceviewer.view.listener.SelectionProperties;

/**
 * Handler for remove trace comment command
 */
public final class RemoveTraceCommentAction extends TraceViewerAction {

	/**
	 * Image for this Action
	 */
	private static ImageDescriptor image;

	/**
	 * Trace which comment is to be edited
	 */
	private final int traceNumber;

	static {
		URL url = null;
		url = TraceViewerPlugin.getDefault().getBundle().getEntry(
				"/icons/clear.gif"); //$NON-NLS-1$
		image = ImageDescriptor.createFromURL(url);
	}

	/**
	 * Constructor
	 * 
	 * @param traceNumber
	 *            trace number
	 */
	public RemoveTraceCommentAction(int traceNumber) {
		this.traceNumber = traceNumber;
		setText(Messages
				.getString("RemoveTraceCommentAction.RemoveCommentText")); //$NON-NLS-1$
		setToolTipText(Messages
				.getString("RemoveTraceCommentAction.RemoveCommentToolTip")); //$NON-NLS-1$
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
		// First clear selections as they might get crazy when some text is
		// taken out from the text widget
		SelectionProperties.clear();

		// Then remove the comment from the handler
		TraceViewerGlobals.getTraceViewer().getDataProcessorAccess()
				.getTraceCommentHandler().removeComment(traceNumber);
	}

}
