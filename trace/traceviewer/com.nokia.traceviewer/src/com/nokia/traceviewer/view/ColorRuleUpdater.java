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
 * Color Rule Updater class
 *
 */
package com.nokia.traceviewer.view;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.TextViewer;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;

import com.nokia.traceviewer.engine.TraceViewerGlobals;

/**
 * Color Rule Updater class
 */
public class ColorRuleUpdater implements Runnable {

	/**
	 * Style ranges
	 */
	private final StyleRange[] ranges;

	/**
	 * Constructor
	 * 
	 * @param ranges
	 *            Style range
	 */
	public ColorRuleUpdater(StyleRange[] ranges) {
		this.ranges = ranges;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Runnable#run()
	 */
	public void run() {

		TextViewer viewer = TraceViewerGlobals.getTraceViewer().getView()
				.getViewer();
		StyledText widget = viewer.getTextWidget();
		IDocument document = viewer.getDocument();

		// Start to replace styleranges
		if (widget != null && document.getLength() > 0 && ranges.length > 0) {
			viewer.getTextWidget().replaceStyleRanges(0, document.getLength(),
					ranges);
			// Delete all styleranges
		} else if (widget != null && document.getLength() > 0
				&& ranges.length == 0) {
			StyleRange range = new StyleRange(0, document.getLength(), null,
					null);
			viewer.getTextWidget().setStyleRange(range);
		} else {
			// Do nothing
		}
	}

}
