/*
 * Copyright (c) 2008-2010 Nokia Corporation and/or its subsidiary(-ies).
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
 * Description:  Definitions for the class OutputModeDialog
 *
 */

package com.nokia.s60tools.analyzetool.ui;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;

import com.nokia.s60tools.analyzetool.Activator;
import com.nokia.s60tools.analyzetool.AnalyzeToolHelpContextIDs;
import com.nokia.s60tools.analyzetool.global.Constants;

public class OutputModeDialog extends Dialog implements Listener {

	private Button traceOutputButton;
	private Button fileOutputButton;

	private Label logPath;
	private Text logPathText;

	private Label logFileName;
	private Text logFileNameText;

	private String outputMode;
	private String path;
	private String fileName;

	public OutputModeDialog(Shell shell) {
		super(shell);
	}

	@Override
	protected void configureShell(Shell shell) {
		shell.setText(Constants.PREFS_LOGGING_MODE_TITLE);
		super.configureShell(shell);
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		final Composite container = (Composite) super.createDialogArea(parent);
		container.setLayout(new GridLayout(1, false));

		traceOutputButton = new Button(container, SWT.RADIO);
		traceOutputButton.setToolTipText(Constants.PREFS_EXT_FAST_TOOLTIP);
		traceOutputButton.setText(Constants.PREFS_EXT_FAST);
		traceOutputButton.addListener(SWT.Selection, this);
		
		fileOutputButton = new Button(container, SWT.RADIO);
		fileOutputButton.setToolTipText(Constants.PREFS_S60_TOOLTIP);
		fileOutputButton.setText(Constants.PREFS_S60);
		fileOutputButton.addListener(SWT.Selection, this);
	
		Composite compS60 = new Composite(container, SWT.NULL);

		final GridLayout layoutS60 = new GridLayout();
		layoutS60.marginLeft = 15;
		layoutS60.numColumns = 2;
		compS60.setLayout(layoutS60);

		logPath = new Label(compS60, SWT.NONE);
		logPath.setToolTipText("Log file path in the device.");
		logPath.setText("Log file path:");

		logPathText = new Text(compS60, SWT.BORDER);
		logPathText.setLayoutData(new GridData(280, SWT.DEFAULT));

		logFileName = new Label(compS60, SWT.NONE);
		logFileName.setToolTipText("Log file name.");
		logFileName.setText("Filename:");

		logFileNameText = new Text(compS60, SWT.BORDER);
		logFileNameText.setLayoutData(new GridData(280, SWT.DEFAULT));

		IPreferenceStore store = Activator.getPreferences();
		String path = store.getString(Constants.DEVICE_LOG_FILE_PATH);

		if (!path.equals("")) {
			logPathText.setText(path);
		} else {
			logPathText.setText("C:\\logs\\atool\\");
		}

		String fileName = store.getString(Constants.DEVICE_LOG_FILE_NAME);

		if (!fileName.equals("")) {
			logFileNameText.setText(fileName);
		} else {
			logFileNameText.setText("%processname%.dat");
		}

		traceOutputButton.setSelection(true);
		logPath.setEnabled(false);
		logPathText.setEnabled(false);
		logFileName.setEnabled(false);
		logFileNameText.setEnabled(false);
		
		return container;
	}

	@Override
	protected void okPressed() {
		if (traceOutputButton.getSelection()) {
			outputMode = Constants.LOGGING_EXT_FAST;
		}

		if (fileOutputButton.getSelection()) {
			outputMode = Constants.LOGGING_S60;
			path = logPathText.getText();
			fileName = logFileNameText.getText();
		}
		super.okPressed();
	}

	public String getOutputMode() {
		return outputMode;
	}

	public String getFileName() {
		return fileName;
	}

	public String getLogPath() {
		return path;
	}

	public void handleEvent(Event event) {
		if (event.widget == traceOutputButton) {
			logPath.setEnabled(false);
			logPathText.setEnabled(false);
			logFileName.setEnabled(false);
			logFileNameText.setEnabled(false);
		} else if (event.widget == fileOutputButton) {
			logPath.setEnabled(true);
			logPathText.setEnabled(true);
			logFileName.setEnabled(true);
			logFileNameText.setEnabled(true);
		}
	}
}
