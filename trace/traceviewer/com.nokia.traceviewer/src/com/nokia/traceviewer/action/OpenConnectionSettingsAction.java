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
 * Handler for open connection settings action
 *
 */
package com.nokia.traceviewer.action;

import java.net.URL;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.PlatformUI;

import com.nokia.traceviewer.TraceViewerHelpContextIDs;
import com.nokia.traceviewer.TraceViewerPlugin;
import com.nokia.traceviewer.engine.TraceViewerGlobals;
import com.nokia.traceviewer.engine.TraceViewerDialogInterface.TVPreferencePage;

/**
 * Handler for open connection settings action
 * 
 */
final class OpenConnectionSettingsAction extends TraceViewerAction {

	/**
	 * Image for this Action
	 */
	private static ImageDescriptor image;

	static {
		URL url = null;
		url = TraceViewerPlugin.getDefault().getBundle().getEntry(
				"/icons/connectionsettings.gif"); //$NON-NLS-1$
		image = ImageDescriptor.createFromURL(url);
	}

	/**
	 * Constructor
	 */
	OpenConnectionSettingsAction() {
		setText(Messages.getString("OpenConnectionSettingsAction.Title")); //$NON-NLS-1$
		setToolTipText(Messages
				.getString("OpenConnectionSettingsAction.Tooltip")); //$NON-NLS-1$
		setImageDescriptor(image);

		// Set help
		PlatformUI.getWorkbench().getHelpSystem().setHelp(this,
				TraceViewerHelpContextIDs.CONNECTION_PREFERENCES);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.traceviewer.action.TraceViewerAction#doRun()
	 */
	@Override
	protected void doRun() {
		TraceViewerGlobals.postUiEvent("ConnectionSettingsButton", "1"); //$NON-NLS-1$ //$NON-NLS-2$
		TraceViewerGlobals.getTraceViewer().getDialogs().openPreferencePage(
				TVPreferencePage.CONNECTION);
		TraceViewerGlobals.postUiEvent("ConnectionSettingsButton", "0"); //$NON-NLS-1$ //$NON-NLS-2$
	}
}
