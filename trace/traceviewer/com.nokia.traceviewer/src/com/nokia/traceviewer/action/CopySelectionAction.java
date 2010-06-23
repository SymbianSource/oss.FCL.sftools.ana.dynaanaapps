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
 * Copy selection command
 *
 */
package com.nokia.traceviewer.action;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;

import com.nokia.traceviewer.TraceViewerHelpContextIDs;
import com.nokia.traceviewer.engine.TraceViewerGlobals;
import com.nokia.traceviewer.view.listener.SelectionProperties;

/**
 * Copy selection command
 * 
 */
final class CopySelectionAction extends TraceViewerAction {

	/**
	 * Data Fetcher class
	 */
	private CopySelectionDataFetcher dataFetcher;

	/**
	 * Image for this action
	 */
	private static ImageDescriptor image;

	/**
	 * Constructor
	 */
	CopySelectionAction() {
		image = PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(
				ISharedImages.IMG_TOOL_COPY);
		setText(Messages.getString("CopySelectionAction.Title")); //$NON-NLS-1$
		setToolTipText(Messages.getString("CopySelectionAction.Tooltip")); //$NON-NLS-1$
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
		int showingFrom = TraceViewerGlobals.getTraceViewer().getView()
				.getShowingTracesFrom();
		StyledText widget = TraceViewerGlobals.getTraceViewer().getView()
				.getViewer().getTextWidget();

		// If the whole selection is in current view, just use widget copy. Also
		// if selected lines are below two blocks, widget copy can be used in
		// any case
		int first = SelectionProperties.firstClickedLine;
		int last = SelectionProperties.lastClickedLine;
		int lineInEnd = showingFrom + widget.getLineCount();

		// If First click and Last click are between current view, use Text
		// Widget Copy
		if (first > showingFrom && first < lineInEnd && last > showingFrom
				&& last < lineInEnd) {
			widget.copy();

			// If we are in the very first block of traces and the first click
			// is in the first row OR the last click hasn't been done at all,
			// use Text Widget Copy
		} else if ((showingFrom == 0 && first == 0 && last < lineInEnd)
				|| (last == -1)) {
			widget.copy();

			// Otherwise, go get the data from the file
		} else {
			if (dataFetcher == null) {
				dataFetcher = new CopySelectionDataFetcher();
			}
			dataFetcher.startGatheringData();
		}
	}
}
