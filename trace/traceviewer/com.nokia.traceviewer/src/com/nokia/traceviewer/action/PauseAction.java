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
 * Handler for pause command
 *
 */
package com.nokia.traceviewer.action;

import java.net.URL;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.PlatformUI;

import com.nokia.traceviewer.TraceViewerHelpContextIDs;
import com.nokia.traceviewer.TraceViewerPlugin;
import com.nokia.traceviewer.engine.TraceViewerGlobals;

/**
 * Handler for pause command
 */
public final class PauseAction extends TraceViewerAction {

	/**
	 * Indicated if pause is on or off
	 */
	private boolean paused;

	/**
	 * Image for the action showing pause button
	 */
	private static ImageDescriptor pauseImage;

	/**
	 * Image for the action showing paused button
	 */
	private static ImageDescriptor pausedImage;

	static {
		URL url = null;
		URL url2 = null;
		url = TraceViewerPlugin.getDefault().getBundle().getEntry(
				"/icons/pause.gif"); //$NON-NLS-1$
		url2 = TraceViewerPlugin.getDefault().getBundle().getEntry(
				"/icons/resume.gif"); //$NON-NLS-1$
		pauseImage = ImageDescriptor.createFromURL(url);
		pausedImage = ImageDescriptor.createFromURL(url2);
	}

	/**
	 * Constructor
	 */
	PauseAction() {
		setPauseImage(true);

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
		TraceViewerGlobals.postUiEvent("PauseButton", "1"); //$NON-NLS-1$ //$NON-NLS-2$
		setPauseImage(paused);
		if (TraceViewerGlobals.getTraceViewer().getDataReaderAccess()
				.getMainDataReader() != null) {
			TraceViewerGlobals.getTraceViewer().getDataReaderAccess()
					.getMainDataReader().pause(paused);
		}

		TraceViewerGlobals.getTraceViewer().getView().updateViewName();
		TraceViewerGlobals.postUiEvent("PauseButton", "0"); //$NON-NLS-1$ //$NON-NLS-2$
	}

	/**
	 * Sets pause image
	 * 
	 * @param pause
	 *            true to set pause image
	 */
	public void setPauseImage(boolean pause) {
		if (!pause) {
			paused = true;
			setImageDescriptor(pausedImage);
			setText(Messages.getString("UnPauseAction.Title")); //$NON-NLS-1$
			setToolTipText(Messages.getString("UnPauseAction.Tooltip")); //$NON-NLS-1$
		} else {
			paused = false;
			setImageDescriptor(pauseImage);
			setText(Messages.getString("PauseAction.Title")); //$NON-NLS-1$
			setToolTipText(Messages.getString("PauseAction.Tooltip")); //$NON-NLS-1$
		}
	}

	/**
	 * Sets paused
	 * 
	 * @param paused
	 *            if true, pauses. If false, unpauses
	 */
	public void setPaused(boolean paused) {
		this.paused = !paused;
		doRun();
	}
}
