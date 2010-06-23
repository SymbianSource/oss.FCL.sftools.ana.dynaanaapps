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
 * Abstract import / export page
 *
 */
package com.nokia.traceviewer.engine.preferences.wizard;

import org.eclipse.jface.viewers.*;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;

/**
 * Abstract superclass of import and export page
 * 
 */
public abstract class AbstractImportExportPage extends WizardPage {

	/**
	 * Path text
	 */
	protected Text pathText;

	/**
	 * Browse button
	 */
	protected Button browseButton;

	/**
	 * Table viewer
	 */
	protected CheckboxTableViewer viewer;

	/**
	 * Constructor
	 * 
	 * @param pageName
	 *            page name
	 */
	public AbstractImportExportPage(String pageName) {
		super(pageName);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets
	 * .Composite)
	 */
	public void createControl(Composite parent) {
		initializeDialogUnits(parent);

		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new GridLayout());
		composite.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_FILL
				| GridData.HORIZONTAL_ALIGN_FILL));
		composite.setSize(composite.computeSize(SWT.DEFAULT, SWT.DEFAULT));
		composite.setFont(parent.getFont());

		setControl(composite);
	}

	/**
	 * Create browse group
	 * 
	 * @param parent
	 *            parent composite
	 * @param labelText
	 *            label text
	 */
	protected void createBrowseGroup(Composite parent, String labelText) {
		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.numColumns = 3;
		composite.setLayout(layout);
		composite.setFont(parent.getFont());
		composite.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL
				| GridData.GRAB_HORIZONTAL));

		// Label
		Label label = new Label(composite, SWT.NONE);
		label.setText(labelText);
		label.setFont(parent.getFont());

		// Path text
		pathText = new Text(composite, SWT.BORDER);
		GridData data = new GridData(GridData.HORIZONTAL_ALIGN_FILL
				| GridData.GRAB_HORIZONTAL);
		data.widthHint = 250;
		pathText.setLayoutData(data);
		pathText.setFont(parent.getFont());
		pathText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				setPageComplete(validatePage(true));
			}
		});

		// Browse button
		browseButton = new Button(composite, SWT.PUSH);
		browseButton.setText(Messages
				.getString("AbstractImportExportPage.BrowseButtonText")); //$NON-NLS-1$
		browseButton
				.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL));
		browseButton.setFont(parent.getFont());
		setButtonLayoutData(browseButton);
	}

	/**
	 * Validates the page
	 * 
	 * @param validateFile
	 *            file path
	 * @return true if page is OK, false otherwise
	 */
	protected abstract boolean validatePage(boolean validateFile);

}