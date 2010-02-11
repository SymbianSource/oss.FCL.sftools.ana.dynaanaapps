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
 * Description:  Definitions for the class AdvancedPreferencePage
 *
 */

package com.nokia.s60tools.analyzetool.preferences;

import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

/**
 * This class implements AnalyzeTool advanced preference page.
 *
 * @author kihe
 *
 */
public class AdvancedPreferencePage extends PreferencePage implements
		IWorkbenchPreferencePage {

	/** Label to display info text  */
	private Label infoText;
	

	/**
	 * Constructor.
	 */
	public AdvancedPreferencePage() {
		super();
	}


	/**
	 * Creates this preference page content.
	 *
	 * @param parent
	 *            This preference page parent
	 */
	@Override
	protected final Control createContents(final Composite parent) {

		// create new composite
		final Composite composite = new Composite(parent, SWT.TOP);

		// create griddata for view
		final GridData gridData = new GridData();
		composite.setLayoutData(gridData);
		final GridLayout gridLayout = new GridLayout();
		gridLayout.horizontalSpacing = 0;
		gridLayout.verticalSpacing = 0;
		gridLayout.marginWidth = 0;
		gridLayout.marginHeight = 0;
		gridLayout.numColumns = 1;

		// set layoyt of this view
		composite.setLayout(gridLayout);

		infoText = new Label(composite, INFORMATION);
		infoText.setText("AnalyzeTool rom symbol definition is changed to project related definition. \nSelect project - properties - " +
				"Carbide extensions - AnalyzeTool to define symbol file(s).");

		return composite;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
	 */
	public void init(final IWorkbench workbench) {
		// MethodDeclaration/Block[count(BlockStatement) = 0 and
		// @containsComment = 'false']
	}
}
