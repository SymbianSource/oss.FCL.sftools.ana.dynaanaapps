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
 * SearchDialogProgressBar updater class
 *
 */
package com.nokia.traceviewer.dialog;

import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.ProgressBar;

import com.nokia.traceviewer.engine.TraceViewerGlobals;

/**
 * Updates search dialogs progressBar
 * 
 */
final class SearchDialogProgressBarUpdater implements Runnable {

	/**
	 * Value of the progressbar
	 */
	private int value;

	/**
	 * Line count label to update
	 */
	private final Label lineCountLabel;

	/**
	 * Constructor
	 * 
	 * @param value
	 *            value of the selection
	 * @param lineCountLabel
	 *            line count label to update
	 */
	SearchDialogProgressBarUpdater(int value, Label lineCountLabel) {
		this.value = value;
		this.lineCountLabel = lineCountLabel;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Runnable#run()
	 */
	public void run() {
		ProgressBar bar = TraceViewerGlobals.getTraceViewer()
				.getDataProcessorAccess().getSearchProcessor()
				.getSearchDialog().getProgressBar();

		if (bar != null && !bar.isDisposed()) {
			int max = 0;

			// Get max value from data reader
			if (TraceViewerGlobals.getTraceViewer().getDataReaderAccess()
					.getCurrentDataReader() != null) {
				max = TraceViewerGlobals.getTraceViewer().getDataReaderAccess()
						.getCurrentDataReader().getTraceCount();
			}
			// Set maximum value to bar
			bar.setMaximum(max);

			// Set selection value
			StringBuffer text = new StringBuffer();
			text.append(Messages
					.getString("SearchDialogProgressBarUpdater.LineText")); //$NON-NLS-1$
			text.append(':');
			text.append(' ');

			if (value <= 0 && max > 0) {
				value = 1;
				text.append(value);
			} else if (value >= max) {
				value = max;
				text.append(value);
			} else if (max == 0) {
				text.append(0);
			} else {
				text.append(value + 1);
			}
			bar.setSelection(value);

			text.append(" / "); //$NON-NLS-1$
			text.append(max);

			// Change line text
			if (!lineCountLabel.isDisposed()) {
				lineCountLabel.setText(text.toString());
			}
		}
	}
}
