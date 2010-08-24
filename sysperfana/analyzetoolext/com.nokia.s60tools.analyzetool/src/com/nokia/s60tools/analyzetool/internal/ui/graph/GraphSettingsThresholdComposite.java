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

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.resource.StringConverter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

/**
 * Composite for entering the graph's threshold value. This is intended to go
 * onto the GraphSettingsDialog.
 */
public class GraphSettingsThresholdComposite extends Composite {
	private static final String B = "b";
	private static final String GB = "gb";
	private static final String BYTE = "byte";
	private static final String BYTES = "bytes";
	private static final String MB = "mb";
	private static final String KB = "kb";
	private static final String DIALOG_MESSAGE = "Only show allocations on the graph which are";
	private static final String ABOVE = "above or equals the threshold";
	private static final String BELOW = "below or equals the threshold";
	private static final String DIALOG_MESSAGE2 = "Examples for valid entries are 20B, 4KB, or 1MB.  Leaving the value empty will clear the threshold.";
	private static final String THRESHOLD_LABEL = "Threshold:";
	private static final String INVALID_INPUT_VALUE = "Invalid input value.";
	private static final String OUT_OF_RANGE_VALUE = "The value is out of range.";

	private GraphSettingsDialog parentDialog;
	private boolean aboveValue;
	private IInputValidator validator;

	/** Threshold Value text entry widget */
	private Text text;
	private String value;
	/** Error message label widget. */
	private Text errorMessageText;
	private Button radioAbove;

	/**
	 * Constructor
	 * 
	 * @param parent
	 *            the parent composite
	 * @param parentDialog
	 *            The calling dialog
	 * @param oldThreshold
	 *            previous threshold value as user entered it
	 * @param above
	 *            true if previous threshold was filtering "above" the threshold
	 */
	public GraphSettingsThresholdComposite(Composite parent,
			GraphSettingsDialog parentDialog, String oldThreshold, boolean above) {
		super(parent, SWT.NONE);
		this.parentDialog = parentDialog;
		value = oldThreshold == null ? "" : oldThreshold;
		aboveValue = above;
		validator = new ThresholdEntryValidator();
	}

	/**
	 * Creates the content of this composite
	 */
	public void createControl() {
		setLayout(GridLayoutFactory.fillDefaults().create());

		Group group = new Group(this, SWT.NONE);
		group.setText("Graph Threshold");
		group.setLayout(GridLayoutFactory.fillDefaults().numColumns(3).margins(
				10, 10).equalWidth(false).create());
		GridDataFactory.fillDefaults().hint(350, SWT.DEFAULT).applyTo(group);

		Label message = new Label(group, SWT.WRAP);
		message.setText(DIALOG_MESSAGE);
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).span(3, 1)
				.grab(true, true).applyTo(message);

		radioAbove = new Button(group, SWT.RADIO);
		radioAbove.setText(ABOVE);
		radioAbove.setSelection(aboveValue);
		GridDataFactory.fillDefaults().span(3, 1).indent(10, 0).applyTo(
				radioAbove);

		Button radioBelow = new Button(group, SWT.RADIO);
		radioBelow.setText(BELOW);
		radioBelow.setSelection(!aboveValue);
		GridDataFactory.fillDefaults().span(3, 1).indent(10, 0).applyTo(
				radioBelow);

		Label thresholdLabel = new Label(group, SWT.NONE);
		thresholdLabel.setText(THRESHOLD_LABEL);
		GridDataFactory.swtDefaults().align(SWT.BEGINNING, SWT.CENTER).grab(
				false, false).indent(0, 10).applyTo(thresholdLabel);

		text = new Text(group, SWT.SINGLE | SWT.BORDER);
		GridDataFactory.fillDefaults().indent(0, 7).align(SWT.BEGINNING,
				SWT.END).hint(80, SWT.DEFAULT).applyTo(text);
		text.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				validateInput();
			}
		});
		text.setText(value);
		text.setFocus();
		text.selectAll();

		errorMessageText = new Text(group, SWT.READ_ONLY | SWT.WRAP);
		GridDataFactory.fillDefaults().grab(true, false).indent(0, 10).applyTo(
				errorMessageText);
		errorMessageText.setBackground(errorMessageText.getDisplay()
				.getSystemColor(SWT.COLOR_WIDGET_BACKGROUND));
		errorMessageText.setForeground(Display.getDefault().getSystemColor(
				SWT.COLOR_RED));

		Label examplesLabel = new Label(group, SWT.WRAP);
		examplesLabel.setText(DIALOG_MESSAGE2);
		GridDataFactory.fillDefaults().span(3, 1).grab(true, false).indent(0,
				10).applyTo(examplesLabel);

		validateInput();
	}

	private void validateInput() {
		setErrorMessage(validator.isValid(text.getText()));
	}

	/**
	 * Sets or clears the error message. If not <code>null</code>, the OK button
	 * is disabled.
	 * 
	 * @param errorMessage
	 *            the error message, or <code>null</code> to clear
	 * @since 3.0
	 */
	public void setErrorMessage(String errorMessage) {
		if (errorMessageText != null && !errorMessageText.isDisposed()) {
			errorMessageText.setText(errorMessage == null ? "" : errorMessage); //$NON-NLS-1$

			// Disable the error message text control if there is no error, or
			// no error text (empty or whitespace only). Hide it also to avoid
			// color change.
			// See https://bugs.eclipse.org/bugs/show_bug.cgi?id=130281
			boolean hasError = errorMessage != null
					&& (StringConverter.removeWhiteSpaces(errorMessage))
							.length() > 0;
			errorMessageText.setEnabled(hasError);
			errorMessageText.setVisible(hasError);
			errorMessageText.getParent().update();

			Control button = parentDialog.getOkButton();
			if (button != null) {
				button.setEnabled(errorMessage == null);
			}
		}
	}

	private static long parseValue(String value) throws NumberFormatException {
		String tmpVal = value.trim().toLowerCase();
		int factor = 1;
		if (tmpVal.endsWith(KB)) {
			tmpVal = tmpVal.substring(0, tmpVal.lastIndexOf(KB)).trim();
			factor = 1024;
		} else if (tmpVal.endsWith(MB)) {
			tmpVal = tmpVal.substring(0, tmpVal.lastIndexOf(MB)).trim();
			factor = 1024 * 1024;
		} else if (tmpVal.endsWith(BYTES)) {
			tmpVal = tmpVal.substring(0, tmpVal.lastIndexOf(BYTES)).trim();
		} else if (tmpVal.endsWith(BYTE)) {
			tmpVal = tmpVal.substring(0, tmpVal.lastIndexOf(BYTE)).trim();
		} else if (tmpVal.endsWith(GB)) {
			tmpVal = tmpVal.substring(0, tmpVal.lastIndexOf(GB)).trim();
			factor = 1024 * 1024 * 1024;
		} else if (tmpVal.endsWith(B)) {
			tmpVal = tmpVal.substring(0, tmpVal.lastIndexOf(B)).trim();
		}
		return Long.parseLong(tmpVal) * factor;
	}

	/**
	 * Returns the threshold value as entered by the user
	 * 
	 * @return the threshold value as entered by the user
	 */
	public String getThresholdString() {
		return value;
	};

	/**
	 * Returns the threshold value in bytes
	 * 
	 * @return the threshold value in bytes
	 */
	public long getThreshold() {
		if (value == null || value.length() == 0) {
			return 0;
		}
		return parseValue(value);
	}

	/**
	 * Returns filtering direction for threshold, true for
	 * "above and equals threshold", false for "below and equals threshold"
	 * 
	 * @return
	 */
	public boolean getAbove() {
		return aboveValue;
	}

	/**
	 * Called from the dialog's buttonPressed() method
	 * 
	 * @param buttonId
	 *            the ID of the button that was pressed
	 */
	public void buttonPressed(int buttonId) {
		value = buttonId == IDialogConstants.OK_ID ? text.getText() : null;
		aboveValue = radioAbove.getSelection();
	}

	/**
	 * This class validates the user-entered threshold value
	 * 
	 */
	class ThresholdEntryValidator implements IInputValidator {

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.eclipse.jface.dialogs.IInputValidator#isValid(java.lang.String)
		 */
		public String isValid(String newText) {
			if (newText.length() == 0) {
				return null;
			}

			try {
				long value = parseValue(newText);
				if (value < 0 || value > 4294967296L) {
					return OUT_OF_RANGE_VALUE;
				}
			} catch (NumberFormatException e) {
				return INVALID_INPUT_VALUE;
			}
			return null;
		}
	}
}
