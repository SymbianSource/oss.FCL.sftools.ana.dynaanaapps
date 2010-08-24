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
 * Description:  Definitions for the class CustomErrorDialog
 *
 */

package com.nokia.s60tools.analyzetool.ui;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

import com.nokia.s60tools.analyzetool.AnalyzeToolHelpContextIDs;
import com.nokia.s60tools.analyzetool.global.Util;

public class CustomMessageDialog extends Dialog {

	private String title;
	private String message;
	private int icon;

	public CustomMessageDialog(Shell shell, String title, String message,
			int icon) {
		super(shell);
		this.title = title;
		this.message = message;
		this.icon = icon;
	}

	@Override
	protected void configureShell(Shell shell) {
		shell.setText(title);
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
		imageLabel.setImage(getShell().getDisplay().getSystemImage(icon));

		Composite messageComposite = new Composite(container, SWT.NONE);
		GridLayout gridLayout = new GridLayout();
		gridLayout.verticalSpacing = 0;
		messageComposite.setLayout(gridLayout);

		Label messageLabel = new Label(messageComposite, SWT.NONE);
		messageLabel.setText(message);

		PlatformUI.getWorkbench().getHelpSystem().setHelp(getShell(),
				AnalyzeToolHelpContextIDs.ANALYZE_TROUBLESHOOTING);

		return container;
	}

	@Override
	protected Control createButtonBar(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		layout.horizontalSpacing = 0;
		composite.setLayout(layout);
		composite
				.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
		composite.setFont(parent.getFont());

		Control helpControl = Util.createHelpControl(composite);
		((GridData) helpControl.getLayoutData()).horizontalIndent = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_MARGIN);

		Control buttonSection = super.createButtonBar(composite);
		((GridData) buttonSection.getLayoutData()).grabExcessHorizontalSpace = true;
		return composite;
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL,
				true);
	}
}
