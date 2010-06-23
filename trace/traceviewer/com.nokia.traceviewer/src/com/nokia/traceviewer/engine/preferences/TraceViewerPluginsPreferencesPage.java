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
 * Preference page for TraceViewer Plugins
 *
 */
package com.nokia.traceviewer.engine.preferences;

import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.PlatformUI;

import com.nokia.traceviewer.TraceViewerHelpContextIDs;

/**
 * Preference page for TraceViewer Plugins
 * 
 */
public class TraceViewerPluginsPreferencesPage extends PreferencePage implements
		IWorkbenchPreferencePage {

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jface.preference.PreferencePage#createContents(org.eclipse
	 * .swt.widgets.Composite)
	 */
	@Override
	protected Control createContents(Composite parent) {

		// Set help
		PlatformUI.getWorkbench().getHelpSystem().setHelp(parent,
				TraceViewerHelpContextIDs.PLUGINS_PREFERENCES);

		noDefaultAndApplyButton();

		// Title label
		Label titleLabel = new Label(parent, SWT.NONE);
		titleLabel.setText(Messages
				.getString("TraceViewerPluginsPreferencesPage.TitleLabel")); //$NON-NLS-1$

		// Spacer
		new Label(parent, SWT.NONE);

		// More information
		Label infoLabel = new Label(parent, SWT.NONE);
		infoLabel.setText(Messages
				.getString("TraceViewerPluginsPreferencesPage.InfoLabel")); //$NON-NLS-1$
		return parent;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
	 */
	public void init(IWorkbench workbench) {
	}
}
