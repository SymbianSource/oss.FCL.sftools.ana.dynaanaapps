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
 * Handler for append decode file command
 *
 */
package com.nokia.traceviewer.action;

import java.net.URL;

import org.eclipse.jface.resource.ImageDescriptor;

import com.nokia.traceviewer.TraceViewerPlugin;
import com.nokia.traceviewer.engine.TraceViewerGlobals;

/**
 * Handler for append decode file command
 * 
 */
public final class AppendDecodeFileAction extends TraceViewerAction {

	/**
	 * Image for this action
	 */
	private static ImageDescriptor image;
	static {
		URL url = null;
		url = TraceViewerPlugin.getDefault().getBundle().getEntry(
				"/icons/opendecodefile.gif"); //$NON-NLS-1$
		image = ImageDescriptor.createFromURL(url);
	}

	/**
	 * Constructor
	 */
	AppendDecodeFileAction() {
		setText(Messages.getString("AppendDecodeFileAction.Title")); //$NON-NLS-1$
		setToolTipText(Messages.getString("AppendDecodeFileAction.Tooltip")); //$NON-NLS-1$
		setImageDescriptor(image);
		setActionDefinitionId("com.nokia.traceviewer.command.append_decode_file"); //$NON-NLS-1$
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.traceviewer.action.TraceViewerAction#doRun()
	 */
	@Override
	protected void doRun() {
		// Pause the datareader if it's not paused already
		boolean wasPausedWhenEntered = TraceViewerGlobals.getTraceViewer()
				.getDataReaderAccess().getMainDataReader().isPaused();

		if (!wasPausedWhenEntered) {
			TraceViewerGlobals.getTraceViewer().getView().getActionFactory()
					.getPauseAction().run();
		}

		// Get files
		String[] files = TraceViewerActionUtils.openFileDialog(
				OpenDecodeFileAction.FILTER_NAMES,
				OpenDecodeFileAction.FILTER_EXTS, null, null, true, true);

		// Get open decode file action
		OpenDecodeFileAction openDecodeFileAction = (OpenDecodeFileAction) TraceViewerGlobals
				.getTraceViewer().getView().getActionFactory()
				.getOpenDecodeFileAction();

		// Load files to model, don't create new model
		openDecodeFileAction.loadFilesToModel(files, false);

		// Unpause
		if (!wasPausedWhenEntered) {
			TraceViewerGlobals.getTraceViewer().getView().getActionFactory()
					.getPauseAction().run();
		}
	}
}
