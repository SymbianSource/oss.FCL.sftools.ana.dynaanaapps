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
 * Decoder class
 *
 */
package com.nokia.traceviewer.engine.dataprocessor;

import com.nokia.traceviewer.TraceViewerPlugin;
import com.nokia.traceviewer.action.OpenDecodeFileAction;
import com.nokia.traceviewer.engine.DecodeProvider;
import com.nokia.traceviewer.engine.TraceProperties;
import com.nokia.traceviewer.engine.TraceViewerGlobals;
import com.nokia.traceviewer.engine.preferences.PreferenceConstants;

/**
 * Decoder class
 * 
 */
public class Decoder implements DataProcessor {

	/**
	 * Interval how often to update progressbar
	 */
	private static final int PROGRESSBAR_UPDATE_INTERVAL = 100;

	/**
	 * Decoder got from decodeProvider
	 */
	private DecodeProvider decoder;

	/**
	 * Offset where decoding of traces after decode file opening is so long that
	 * we can start showing traces
	 */
	private int startShowingTracesAgainFromCount;

	/**
	 * Determines if Btrace variables (Thread ID, CPU ID) should be shown
	 */
	private boolean showBtraceVariables;

	/**
	 * Constructor
	 */
	public Decoder() {

		// Get show BTrace variables option
		showBtraceVariables = TraceViewerPlugin.getDefault()
				.getPreferenceStore().getBoolean(
						PreferenceConstants.SHOW_BTRACE_VARIABLES_CHECKBOX);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.nokia.traceviewer.engine.DataProcessor#processData(com.nokia.traceviewer
	 * .engine.TraceProperties)
	 */
	public void processData(TraceProperties properties) {
		TraceViewerGlobals.debug("processData in Decoder", //$NON-NLS-1$
				TraceViewerGlobals.DebugLevel.TEST);
		if (decoder == null && TraceViewerGlobals.getDecodeProvider() != null) {
			decoder = TraceViewerGlobals.getDecodeProvider();
		}

		// Needs to be binary trace to be decoded
		if (properties.binaryTrace && decoder != null
				&& decoder.isModelLoadedAndValid()) {
			decoder.decodeTrace(properties.byteBuffer, properties);
		}

		// Insert BTrace variables if there is any
		if (showBtraceVariables
				&& properties.bTraceInformation.hasInformation()
				&& properties.traceString != null) {

			StringBuilder tmp = new StringBuilder(properties.traceString
					.length() * 2);

			// CPU ID
			if (properties.bTraceInformation.getCpuId() != -1) {
				tmp.append(Messages.getString("Decoder.CpuIdText")); //$NON-NLS-1$
				tmp.append(properties.bTraceInformation.getCpuId());
				tmp.append(Messages.getString("Decoder.CpuTextDelimeter"));//$NON-NLS-1$
			}

			tmp.append(properties.traceString);

			// Thread ID
			if (properties.bTraceInformation.getThreadId() != 0) {
				tmp.append(Messages.getString("Decoder.ThreadIdText")); //$NON-NLS-1$
				tmp.append(Integer.toHexString(properties.bTraceInformation
						.getThreadId()));
			}
			properties.traceString = tmp.toString();
		}

		// Update progressBar
		if (TraceViewerGlobals.getTraceViewer().getView() != null) {
			updateProgressBar(properties);
		}
	}

	/**
	 * Updates progressBar if needed
	 * 
	 * @param properties
	 *            trace properties
	 */
	private void updateProgressBar(TraceProperties properties) {
		// Update possible progressBar
		OpenDecodeFileAction decodeAction = ((OpenDecodeFileAction) TraceViewerGlobals
				.getTraceViewer().getView().getActionFactory()
				.getOpenDecodeFileAction());
		int traceCount = TraceViewerGlobals.getTraceViewer()
				.getDataReaderAccess().getCurrentDataReader().getTraceCount();

		if (decodeAction.isDecodingTraces()) {
			if (traceCount % PROGRESSBAR_UPDATE_INTERVAL == 0) {
				decodeAction.getProgressBarDialog().updateProgressBar(
						TraceViewerGlobals.getTraceViewer()
								.getDataReaderAccess().getCurrentDataReader()
								.getTraceCount());
			}

			// Start showing traces again
			if (traceCount >= startShowingTracesAgainFromCount) {
				properties.traceConfiguration.setShowInView(true);
				startShowingTracesAgainFromCount = Integer.MAX_VALUE;
			}
		}
	}

	/**
	 * Set startShowingTracesAgainFromCount
	 * 
	 * @param offset
	 *            offset
	 */
	public void setStartShowingTracesAgainOffset(int offset) {
		startShowingTracesAgainFromCount = offset;
	}

	/**
	 * Sets show BTrace variables boolean
	 * 
	 * @param showVariables
	 *            if true, show BTrace variables
	 */
	public void setShowBTraceVariables(boolean showVariables) {
		this.showBtraceVariables = showVariables;
	}

}
