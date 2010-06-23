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
 * Base class for own dialogs
 *
 */
package com.nokia.traceviewer.dialog;

import org.eclipse.jface.dialogs.TrayDialog;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

import com.nokia.traceviewer.TraceViewerHelpContextIDs;
import com.nokia.traceviewer.engine.TraceViewerDialog;

/**
 * Base class for own dialogs
 * 
 */
public abstract class BaseDialog extends TrayDialog implements
		TraceViewerDialog {

	/**
	 * Integer value indicating not defined
	 */
	protected static final int NOT_DEFINED = -1;

	/**
	 * Composite in this dialog
	 */
	protected Composite composite;

	/**
	 * Old rectangle used when returning to same position
	 */
	private Rectangle oldRectangle;

	/**
	 * Constructor
	 * 
	 * @param parent
	 *            parent shell
	 * @param style
	 *            style bits
	 */
	public BaseDialog(Shell parent, int style) {
		super(parent);
		setShellStyle(style);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.dialogs.TrayDialog#close()
	 */
	@Override
	public boolean close() {
		saveSettings();
		boolean close = super.close();
		return close;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.window.Window#open()
	 */
	@Override
	public int open() {
		Shell shell = getShell();
		if (shell == null || shell.isDisposed()) {
			shell = null;
			// create the window
			create();
		}

		shell = getShell();

		// Move the dialog to the center of the top level shell.
		Rectangle shellBounds = getParentShell().getBounds();
		Point dialogSize = shell.getSize();
		int middleX = shellBounds.x + ((shellBounds.width - dialogSize.x) / 2);
		int middleY = shellBounds.y + ((shellBounds.height - dialogSize.y) / 2);
		shell.setLocation(middleX, middleY);

		// Restore settings
		restoreSettings();

		return super.open();
	}

	/**
	 * Saves user input settings
	 */
	protected void saveSettings() {
		// Save the old location
		Shell shell = getShell();
		if (shell != null && !shell.isDisposed()) {
			oldRectangle = shell.getBounds();
		}
	}

	/**
	 * Restores user input settings
	 */
	protected void restoreSettings() {
		// Restore the location of the dialog
		Shell shell = getShell();
		if (oldRectangle != null && shell != null) {
			shell.setBounds(oldRectangle);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets
	 * .Composite)
	 */
	@Override
	protected Control createDialogArea(Composite parent) {
		this.composite = (Composite) super.createDialogArea(parent);

		// Set help
		PlatformUI.getWorkbench().getHelpSystem().setHelp(getShell(),
				TraceViewerHelpContextIDs.ACTIONS);

		// Create dialog contents
		createDialogContents();

		// Create action listeners
		createActionListeners();

		return composite;
	}

	/**
	 * This method creates dialog contents. Subclasses must implement.
	 */
	abstract protected void createDialogContents();

	/**
	 * This method creates action listeners for controls in this dialog.
	 * Subclasses must implement.
	 */
	abstract protected void createActionListeners();

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.traceviewer.engine.TraceViewerDialog#openDialog()
	 */
	public void openDialog() {
		open();
	}
}
