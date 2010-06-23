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
 * Handler for clear view command
 *
 */
package com.nokia.traceviewer.action;

import java.net.URL;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.ui.PlatformUI;

import com.nokia.traceviewer.TraceViewerHelpContextIDs;
import com.nokia.traceviewer.TraceViewerPlugin;
import com.nokia.traceviewer.engine.TraceViewerGlobals;

/**
 * Handler for clear view command
 * 
 */
final class ClearViewAction extends TraceViewerAction {

	/**
	 * Image for this Action
	 */
	private static ImageDescriptor image;

	static {
		URL url = null;
		url = TraceViewerPlugin.getDefault().getBundle().getEntry(
				"/icons/clear.gif"); //$NON-NLS-1$
		image = ImageDescriptor.createFromURL(url);
	}

	/**
	 * Constructor
	 */
	ClearViewAction() {
		setText(Messages.getString("ClearViewAction.Title")); //$NON-NLS-1$
		setToolTipText(Messages.getString("ClearViewAction.Tooltip")); //$NON-NLS-1$
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
		TraceViewerGlobals.postUiEvent("ClearViewButton", "1"); //$NON-NLS-1$ //$NON-NLS-2$
		BusyIndicator.showWhile(PlatformUI.getWorkbench().getDisplay(),
				new ClearViewRunnable());
		TraceViewerGlobals.postUiEvent("ClearViewButton", "0"); //$NON-NLS-1$ //$NON-NLS-2$
	}
}
