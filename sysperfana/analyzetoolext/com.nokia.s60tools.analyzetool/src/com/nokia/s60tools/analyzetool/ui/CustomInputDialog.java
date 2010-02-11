/*
 * Copyright (c) 2008-2009 Nokia Corporation and/or its subsidiary(-ies).
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
 * Description:  Definitions for the class CustomInputDialog
 *
 */

package com.nokia.s60tools.analyzetool.ui;

import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.window.Window;

import com.nokia.s60tools.analyzetool.Activator;
import com.nokia.s60tools.analyzetool.global.Constants;

/**
 * Provides input dialog Checks that user input does not contain spaces.
 *
 * @author kihe
 *
 */
public class CustomInputDialog implements IInputValidator {
	/** Dialog default value. */
	private final String defaultValue;

	/** Dialog hint text. */
	private final String dialogHintText;

	/** Dialog title. */
	private final String dialogTitle;

	/** User input. */
	private String input = "";

	/**
	 * Constructor.
	 *
	 * @param title
	 *            Dialog title
	 * @param hintText
	 *            Dialog hint text
	 * @param defaultVal
	 *            Dialog default input
	 */
	public CustomInputDialog(final String title, final String hintText,
			final String defaultVal) {
		dialogTitle = title;
		dialogHintText = hintText;
		defaultValue = defaultVal;
	}

	/**
	 * Checks that user entered text does not contain illegal/unwanted
	 * characters.
	 *
	 * @param line
	 *            User entered text
	 * @return True if user entered text does not contain illegal characters
	 *         otherwise False
	 */
	public boolean checkCharacters(final String line) {

		CharSequence[] charTable = { "&", "^", "+", "-", "@", "$", "%", "*",
				"(", ")", "|", "\\", "/", "[", "]", "{", "}", "<", ">", "?",
				";", ":", ",", "\"", "'" };

		// thru illegal/unwanted char table
		for (int i = 0; i < charTable.length; i++) {

			// if user entered text contains illegal/unwanted characters =>
			// return false
			if (line.contains(charTable[i])) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Gets self instance.
	 *
	 * @return Self instance
	 */
	public final CustomInputDialog getParent() {
		return this;
	}

	/**
	 * Returns user input.
	 *
	 * @return User input
	 */
	public final String getUserInput() {
		return input;
	}

	/**
	 * Checks is user given input valid for the AnalyzeTool. If input contains
	 * space/illegal character(s) or it contains more than 50 characters the OK
	 * button becomes disabled and corresponding error message is displayed.
	 *
	 * @see org.eclipse.jface.dialogs.IInputValidator#isValid(java.lang.String)
	 *
	 * @param newText
	 *            User input
	 *
	 * @return String Error message if the user given input is invalid otherwise
	 *         empty string
	 */
	public final String isValid(final String newText) {

		CharSequence space = " ";
		if (("").equals(newText)) {
			return ("");
		} else if (newText.contains(space)) {
			return (Constants.INPUT_NO_SPACES_ALLOWED);
		} else if (newText.length() > Constants.MAX_LENGTH_OF_USER_INPUT) {
			return (Constants.INPUT_TOO_LONG);
		} else if (!checkCharacters(newText)) {
			return (Constants.INPUT_ILLEGAL);
		}
		return null;
	}

	/**
	 * Opens dialog
	 */
	public final void open() {

		Activator.getDefault().getWorkbench().getDisplay().syncExec(
				new Runnable() {
					public void run() {
						InputDialog dlg = new InputDialog(Activator
								.getDefault().getWorkbench().getDisplay()
								.getActiveShell(), dialogTitle, dialogHintText,
								defaultValue, getParent());
						if (dlg.open() == Window.OK) {
							input = dlg.getValue();
						}
					}
				});
	}

}
