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
 * Ascii log options selection Dialog
 *
 */
package com.nokia.traceviewer.dialog;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

import com.nokia.traceviewer.TraceViewerHelpContextIDs;
import com.nokia.traceviewer.TraceViewerPlugin;
import com.nokia.traceviewer.engine.TraceViewerGlobals;
import com.nokia.traceviewer.engine.preferences.PreferenceConstants;

/**
 * Ascii log options selection Dialog
 */
public final class LogAsciiOptionsSelectionDialog extends BaseDialog {

	/**
	 * Omit timestamps checkbox
	 */
	private Button omitTimestampCheckBox;

	/**
	 * Write machine readable log file checkbox
	 */
	private Button writeMachineReadableLogCheckBox;

	/**
	 * File path
	 */
	private final String filePath;

	/**
	 * Omit timestamp boolean value
	 */
	private static boolean omitTimestamp;

	/**
	 * Write machine readable log boolean value
	 */
	private static boolean writeMachineReadableLog;

	/**
	 * Constructor
	 * 
	 * @param parent
	 *            parent shell
	 * @param filePath
	 *            file path of the ascii log
	 */
	public LogAsciiOptionsSelectionDialog(Shell parent, String filePath) {
		super(parent, SWT.DIALOG_TRIM | SWT.MODELESS | SWT.RESIZE);
		this.filePath = filePath;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.traceviewer.dialog.BaseDialog#createDialogContents()
	 */
	@Override
	protected void createDialogContents() {
		// Shell
		GridLayout shellGridLayout = new GridLayout();
		String shellTitle = Messages
				.getString("LogAsciiOptionsSelectionDialog.ShellTitle"); //$NON-NLS-1$
		getShell().setText(shellTitle);
		composite.setLayout(shellGridLayout);

		// Omit timestamp checkbox
		omitTimestampCheckBox = new Button(composite, SWT.CHECK);
		omitTimestampCheckBox.setText(Messages
				.getString("LogAsciiOptionsSelectionDialog.OmitTimestampText")); //$NON-NLS-1$
		omitTimestampCheckBox
				.setToolTipText(Messages
						.getString("LogAsciiOptionsSelectionDialog.OmitTimestampToolTip")); //$NON-NLS-1$
		omitTimestampCheckBox.setSelection(omitTimestamp);
		omitTimestampCheckBox.setEnabled(!writeMachineReadableLog);

		// Write machine readable log checkbox
		writeMachineReadableLogCheckBox = new Button(composite, SWT.CHECK);
		String text = Messages
				.getString("LogAsciiOptionsSelectionDialog.WriteMachineReadableLogText"); //$NON-NLS-1$
		String tooltip = Messages
				.getString("LogAsciiOptionsSelectionDialog.WriteMachineReadableLogToolTip"); //$NON-NLS-1$
		writeMachineReadableLogCheckBox.setText(text);
		writeMachineReadableLogCheckBox.setToolTipText(tooltip);
		writeMachineReadableLogCheckBox.setSelection(writeMachineReadableLog);
		writeMachineReadableLogCheckBox.setEnabled(!omitTimestamp);

		// Set help
		PlatformUI.getWorkbench().getHelpSystem().setHelp(getShell(),
				TraceViewerHelpContextIDs.LOGGING);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.traceviewer.dialog.BaseDialog#createActionListeners()
	 */
	@Override
	protected void createActionListeners() {
		// Add selection listener to omit timestamp checkbox
		omitTimestampCheckBox.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				boolean selected = omitTimestampCheckBox.getSelection();
				TraceViewerGlobals.postUiEvent("OmitTimestampCheckBoxSelection" //$NON-NLS-1$
						+ selected, "1"); //$NON-NLS-1$

				writeMachineReadableLogCheckBox.setEnabled(!selected);
				if (selected) {
					writeMachineReadableLogCheckBox.setSelection(false);
				}

				TraceViewerGlobals.postUiEvent("OmitTimestampCheckBoxSelection" //$NON-NLS-1$
						+ selected, "0"); //$NON-NLS-1$
			}
		});

		// Add selection listener to write machine readable log checkbox
		writeMachineReadableLogCheckBox
				.addSelectionListener(new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent event) {
						boolean selected = writeMachineReadableLogCheckBox
								.getSelection();
						TraceViewerGlobals.postUiEvent(
								"WriteMachineReadableLogCheckBoxSelection" //$NON-NLS-1$
										+ selected, "1"); //$NON-NLS-1$

						omitTimestampCheckBox.setEnabled(!selected);
						if (selected) {
							omitTimestampCheckBox.setSelection(false);
						}

						TraceViewerGlobals.postUiEvent(
								"WriteMachineReadableLogCheckBoxSelection" //$NON-NLS-1$
										+ selected, "0"); //$NON-NLS-1$
					}
				});
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.dialogs.Dialog#okPressed()
	 */
	@Override
	protected void okPressed() {
		boolean machineReadableSettingChanged = (writeMachineReadableLog != writeMachineReadableLogCheckBox
				.getSelection());
		omitTimestamp = omitTimestampCheckBox.getSelection();
		writeMachineReadableLog = writeMachineReadableLogCheckBox
				.getSelection();

		super.okPressed();

		// If timestamp accuracy is milliseconds, ask if the user wants to
		// change it to microseconds
		if (writeMachineReadableLog
				&& TraceViewerGlobals.getTraceViewer().getDataProcessorAccess()
						.getTimestampParser().isTimestampAccuracyMilliSecs()) {

			// Get change timestamp accuracy confirmation from the user
			String changeAccuracyConfirmation = Messages
					.getString("LogAsciiOptionsSelectionDialog.ChangeTimestampAccuracyMsg"); //$NON-NLS-1$
			boolean changeAccuracy = TraceViewerGlobals.getTraceViewer()
					.getDialogs().showConfirmationDialog(
							changeAccuracyConfirmation);

			// Change the accuracy and save the new value to the preference
			// store
			if (changeAccuracy) {
				TraceViewerGlobals.getTraceViewer().getDataProcessorAccess()
						.getTimestampParser().setTimestampAccuracyMilliSecs(
								false);

				TraceViewerPlugin.getDefault().getPreferenceStore().setValue(
						PreferenceConstants.TIMESTAMP_ACCURACY,
						PreferenceConstants.MICROSECOND_ACCURACY);

			}
		}

		// Start logging
		TraceViewerGlobals.getTraceViewer().getDataProcessorAccess()
				.getLogger().startPlainTextLogging(filePath, omitTimestamp,
						writeMachineReadableLog);

		// Insert CloseAndRestartLogging Action button
		if (writeMachineReadableLog) {
			TraceViewerGlobals.getTraceViewer().getView().getActionFactory()
					.addCloseAndRestartLoggingButton(filePath);
		} else if (machineReadableSettingChanged) {
			TraceViewerGlobals.getTraceViewer().getView().getActionFactory()
					.removeCloseAndRestartLoggingButton();
		}
	}
}
