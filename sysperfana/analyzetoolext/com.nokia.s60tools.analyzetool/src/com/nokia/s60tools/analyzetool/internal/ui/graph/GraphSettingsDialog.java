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
 * Description:  Definitions for the class AnalyzeToolGraph
 *
 */
package com.nokia.s60tools.analyzetool.internal.ui.graph;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

/**
 * Dialog for AnalyzeTool Graph Settings
 */
public class GraphSettingsDialog extends Dialog {
	private static final String DIALOG_TITLE = "Graph Settings";

	// initial values
	private String initialValueThreshold;
	private boolean initialValueAbove;

	/** Composite for entering threshold value */
	private GraphSettingsThresholdComposite thresholdGroup;

	/** the OK button */
	private Button okButton;

	/**
	 * Constructor
	 * 
	 * @param parentShell
	 *            The shell for this dialog to open in
	 * @param oldThreshold
	 *            threshold as previously entered by user
	 * @param oldAbove
	 *            true if previous threshold was filtering "above" the threshold
	 */
	public GraphSettingsDialog(Shell parentShell, String oldThreshold,
			boolean oldAbove) {
		super(parentShell);
		initialValueThreshold = oldThreshold;
		initialValueAbove = oldAbove;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	protected Control createDialogArea(Composite parent) {
		Composite container = (Composite) super.createDialogArea(parent);

		thresholdGroup = new GraphSettingsThresholdComposite(container, this,
				initialValueThreshold, initialValueAbove);
		thresholdGroup.createControl();
		GridDataFactory.fillDefaults().grab(true, true).applyTo(thresholdGroup);

		return container;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jface.window.Window#configureShell(org.eclipse.swt.widgets
	 * .Shell)
	 */
	@Override
	protected void configureShell(Shell shell) {
		super.configureShell(shell);
		shell.setText(DIALOG_TITLE);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jface.dialogs.Dialog#createButtonsForButtonBar(org.eclipse
	 * .swt.widgets.Composite)
	 */
	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		okButton = createButton(parent, IDialogConstants.OK_ID,
				IDialogConstants.OK_LABEL, true);
		createButton(parent, IDialogConstants.CANCEL_ID,
				IDialogConstants.CANCEL_LABEL, false);
	}

	/**
	 * Returns the ok button.
	 * 
	 * @return the ok button
	 */
	Button getOkButton() {
		return okButton;
	}

	/**
	 * Returns the threshold value String as typed by user.
	 * 
	 * @return the input string for threshold value
	 */
	public String getThresholdString() {
		return thresholdGroup.getThresholdString();
	}

	/**
	 * Returns the threshold value in bytes
	 * 
	 * @return the threshold value in bytes
	 */
	public long getThreshold() {
		return thresholdGroup.getThreshold();
	}

	/**
	 * Returns filtering direction for threshold, true for
	 * "above and equals threshold", false for ""below and equals threshold
	 * 
	 * @return
	 */
	public boolean getAbove() {
		return thresholdGroup.getAbove();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.dialogs.Dialog#buttonPressed(int)
	 */
	@Override
	protected void buttonPressed(int buttonId) {
		thresholdGroup.buttonPressed(buttonId);
		super.buttonPressed(buttonId);
	}

}
