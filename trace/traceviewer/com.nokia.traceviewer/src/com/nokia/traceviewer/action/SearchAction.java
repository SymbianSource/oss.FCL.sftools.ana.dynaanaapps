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
 * Handler for search command
 *
 */
package com.nokia.traceviewer.action;

import java.net.URL;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.PlatformUI;

import com.nokia.traceviewer.TraceViewerHelpContextIDs;
import com.nokia.traceviewer.TraceViewerPlugin;
import com.nokia.traceviewer.dialog.SearchDialog;
import com.nokia.traceviewer.engine.TraceViewerGlobals;

/**
 * Handler for search command
 */
final class SearchAction extends TraceViewerAction {

	/**
	 * Image for the action
	 */
	private static ImageDescriptor image;

	static {
		URL url = null;
		url = TraceViewerPlugin.getDefault().getBundle().getEntry(
				"/icons/search.gif"); //$NON-NLS-1$
		image = ImageDescriptor.createFromURL(url);
	}

	/**
	 * Constructor
	 */
	SearchAction() {
		setText(Messages.getString("SearchAction.Title")); //$NON-NLS-1$
		setToolTipText(Messages.getString("SearchAction.Tooltip")); //$NON-NLS-1$
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
		TraceViewerGlobals.postUiEvent("SearchButton", "1"); //$NON-NLS-1$ //$NON-NLS-2$
		SearchDialog dialog = TraceViewerGlobals.getTraceViewer()
				.getDataProcessorAccess().getSearchProcessor()
				.getSearchDialog();
		if (!dialog.isOpen()) {
			dialog.openDialog();
		} else {
			if (!dialog.isVisible()) {
				dialog.openDialog();
			}
			dialog.setFocus();
		}
		TraceViewerGlobals.postUiEvent("SearchButton", "0"); //$NON-NLS-1$ //$NON-NLS-2$
	}
}
