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
 * Description:  Definitions for the class WarningDialogWithCheckbox
 *
 */

package com.nokia.s60tools.analyzetool.ui;

import java.text.MessageFormat;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
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

public class WarningDialogWithCheckbox extends Dialog implements Listener {

	private String unknownTag;
	private boolean dontShow;

	private Button showButton;

	public WarningDialogWithCheckbox(Shell shell, String unknownTag) {
		super(shell);
		this.unknownTag = unknownTag;
	}

	@Override
	protected void configureShell(Shell shell) {
		shell.setText("Unknown tag");
		super.configureShell(shell);
	}

	@Override
	protected Control createDialogArea(Composite parent) {

		final Composite container = (Composite) super.createDialogArea(parent);
		container.setLayout(new GridLayout(2, false));

		Composite imageComposite = new Composite(container, SWT.NONE);
		imageComposite.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false,
				false));
		imageComposite.setLayout(new GridLayout());

		Label imageLabel = new Label(imageComposite, SWT.NONE);
		imageLabel.setImage(getShell().getDisplay().getSystemImage(
				SWT.ICON_WARNING));

		Composite warningComposite = new Composite(container, SWT.NONE);
		GridLayout gridLayout = new GridLayout();
		gridLayout.verticalSpacing = 0;
		warningComposite.setLayout(gridLayout);

		Label warningLabel = new Label(warningComposite, SWT.NONE);
		warningLabel
				.setText(MessageFormat
						.format(
								"Trace contains unknown {0} tag. AnalyzeTool Carbide extension might not be up to date.\n\n",
								unknownTag));

		showButton = new Button(warningComposite, SWT.CHECK);
		showButton.setText("Don't show again");
		showButton.addListener(SWT.Selection, this);

		return container;
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL,
				true);
	}

	public void handleEvent(Event event) {
		if (event.widget == showButton) {
			dontShow = true;
		} else {
			dontShow = false;
		}
	}

	public boolean dontShow() {
		return dontShow;
	}
}
