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
 * Start External Filter Application Action
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
import com.nokia.traceviewer.engine.dataprocessor.ExternalFilterProcessor;
import com.nokia.traceviewer.engine.preferences.PreferenceConstants;

/**
 * Start External Filter Application Action
 */
public class StartExternalFilterAction extends TraceViewerAction {

	/**
	 * Image for the start action
	 */
	private static ImageDescriptor startImage;

	/**
	 * Image for the stop action
	 */
	private static ImageDescriptor stopImage;

	static {
		URL url = null;
		URL url2 = null;
		url = TraceViewerPlugin.getDefault().getBundle().getEntry(
				"/icons/startexternal.gif"); //$NON-NLS-1$
		url2 = TraceViewerPlugin.getDefault().getBundle().getEntry(
				"/icons/stopexternal.gif"); //$NON-NLS-1$
		startImage = ImageDescriptor.createFromURL(url);
		stopImage = ImageDescriptor.createFromURL(url2);
	}

	/**
	 * Constructor
	 */
	public StartExternalFilterAction() {
		setText(Messages.getString("StartExternalFilterAction.Title")); //$NON-NLS-1$
		setToolTipText(Messages.getString("StartExternalFilterAction.Tooltip")); //$NON-NLS-1$
		setImageDescriptor(startImage);

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
		TraceViewerGlobals.postUiEvent("StartExternalFilterButton", "1"); //$NON-NLS-1$ //$NON-NLS-2$

		// If external filter is not defined, open the preference page
		boolean externalFilterIsDefined = TraceViewerPlugin.getDefault()
				.getPreferenceStore().getBoolean(
						PreferenceConstants.EXTERNAL_FILTER_CHECKBOX);

		if (!externalFilterIsDefined) {
			TraceViewerGlobals.getTraceViewer().getDialogs()
					.openPreferencePage(TVPreferencePage.ADVANCED);

			// If defined but not using, set as using
		} else if (!TraceViewerGlobals.getTraceViewer()
				.getDataProcessorAccess().getFilterProcessor()
				.isUsingExternalFilter()) {
			TraceViewerGlobals.getTraceViewer().getDataProcessorAccess()
					.getFilterProcessor().setUsingExternalFilter(true);
		}

		// Check checkbox again
		externalFilterIsDefined = TraceViewerPlugin.getDefault()
				.getPreferenceStore().getBoolean(
						PreferenceConstants.EXTERNAL_FILTER_CHECKBOX);

		// Start external application and show stop image
		if (externalFilterIsDefined
				&& !TraceViewerGlobals.getTraceViewer()
						.getDataProcessorAccess().getFilterProcessor()
						.isUsingExternalFilter()) {
			boolean success = TraceViewerGlobals.getTraceViewer()
					.getDataProcessorAccess().getFilterProcessor()
					.getExternalFilterProcessor().startExternalApplication();

			if (success) {
				setImageDescriptor(stopImage);
				setText(Messages.getString("StopExternalFilterAction.Title")); //$NON-NLS-1$
				setToolTipText(Messages
						.getString("StopExternalFilterAction.Tooltip")); //$NON-NLS-1$
			}
			// Stop external application and show start image
		} else {
			setImageDescriptor(startImage);
			setText(Messages.getString("StartExternalFilterAction.Title")); //$NON-NLS-1$
			setToolTipText(Messages
					.getString("StartExternalFilterAction.Tooltip")); //$NON-NLS-1$

			// Get the processor and stop it if it exists
			ExternalFilterProcessor extProcessor = TraceViewerGlobals
					.getTraceViewer().getDataProcessorAccess()
					.getFilterProcessor().getExternalFilterProcessor();

			if (extProcessor != null) {
				extProcessor.stopExternalApplication();
			}
		}
		TraceViewerGlobals.postUiEvent("StartExternalFilterButton", "0"); //$NON-NLS-1$ //$NON-NLS-2$
	}
}
