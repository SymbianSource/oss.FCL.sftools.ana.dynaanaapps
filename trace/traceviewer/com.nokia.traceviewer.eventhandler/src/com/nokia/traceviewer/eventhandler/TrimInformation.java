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
 * Trim Information
 *
 */
package com.nokia.traceviewer.eventhandler;

import org.eclipse.jface.action.IContributionManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.menus.WorkbenchWindowControlContribution;

/**
 * Trim Information
 */
public final class TrimInformation extends WorkbenchWindowControlContribution {
	/**
	 * Composite of the trim
	 */
	private static Composite composite;

	/**
	 * Text of the trim
	 */
	private static Label textLabel;

	/**
	 * Text inside the trim
	 */
	private static String textInTheTrim = ""; //$NON-NLS-1$

	/**
	 * Trim parent
	 */
	private static IContributionManager trimParent;

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.menus.AbstractTrimWidget#dispose()
	 */
	@Override
	public void dispose() {
		if (composite != null && !composite.isDisposed()) {
			composite.dispose();
		}
		if (textLabel != null && !textLabel.isDisposed()) {
			textLabel.dispose();
		}
		composite = null;
		textLabel = null;
	}

	/**
	 * Sets text to label
	 * 
	 * @param labelText
	 *            text to insert
	 */
	public static void setTextToLabel(String labelText) {
		textInTheTrim = labelText;

		// Only update the trim parent when there's something in our trim
		if (!textInTheTrim.equals("")) { //$NON-NLS-1$
			trimParent.update(true);

			// Empty, let's not resize the trim parent but only update the text
		} else {
			textLabel.setText(textInTheTrim);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jface.action.ControlContribution#createControl(org.eclipse
	 * .swt.widgets.Composite)
	 */
	@Override
	protected Control createControl(Composite parent) {
		trimParent = getParent();

		// Create a composite to place the label in
		composite = new Composite(parent, SWT.NULL);

		// Give some room around the control
		GridLayout layout = new GridLayout();
		layout.marginHeight = 0;
		composite.setLayout(layout);

		// Create a label for the trim.
		textLabel = new Label(composite, SWT.NULL);
		textLabel
				.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, true));
		textLabel.setText(textInTheTrim);
		return composite;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.action.ContributionItem#isDynamic()
	 */
	@Override
	public boolean isDynamic() {
		return true;
	}
}
