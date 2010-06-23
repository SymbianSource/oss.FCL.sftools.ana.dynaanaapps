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
 * Connection preferences page
 *
 */
package com.nokia.traceviewer.engine.preferences;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.PlatformUI;

import com.nokia.carbide.remoteconnections.interfaces.IConnection;
import com.nokia.traceviewer.TraceViewerHelpContextIDs;
import com.nokia.traceviewer.TraceViewerPlugin;
import com.nokia.traceviewer.engine.ConnectionHelper;
import com.nokia.traceviewer.internal.api.TraceViewerAPI2Impl;

/**
 * Connection preferences page
 * 
 */
public final class TraceViewerConnectionPreferencesPage extends PreferencePage
		implements IWorkbenchPreferencePage {

	/**
	 * Id for Connection preferences page
	 */
	public static final String PAGE_ID = "com.nokia.traceviewer.preferences.ConnectionPreferences"; //$NON-NLS-1$

	/**
	 * PreferenceStore holding all preferences
	 */
	private final IPreferenceStore store;

	/**
	 * Auto-connect to dynamic connections checkbox
	 */
	private Button autoConnectDynamicConnectionsCheckBox;

	/**
	 * Connection that is used after latest call to <code>saveSettings()</code>
	 * method.
	 */
	private IConnection conn;

	/**
	 * Constructor
	 */
	public TraceViewerConnectionPreferencesPage() {
		super();

		// Set the preference store for the preference page.
		store = TraceViewerPlugin.getDefault().getPreferenceStore();
		setPreferenceStore(store);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jface.preference.PreferencePage#createContents(org.eclipse
	 * .swt.widgets.Composite)
	 */
	@Override
	protected Control createContents(Composite parent) {

		// Create Top composite in top of the parent composite
		Composite top = new Composite(parent, SWT.LEFT);
		GridData topCompositeGridData = new GridData(SWT.FILL, SWT.FILL, true,
				false);
		top.setLayoutData(topCompositeGridData);
		GridLayout topCompositeGridLayout = new GridLayout();
		topCompositeGridLayout.horizontalSpacing = 5;
		topCompositeGridLayout.verticalSpacing = 5;
		topCompositeGridLayout.marginWidth = 0;
		topCompositeGridLayout.marginHeight = 0;
		top.setLayout(topCompositeGridLayout);

		// Create client side UI
		Composite returnedComposite = ConnectionHelper
				.createClientServiceUI(top);

		// If client service UI couldn't be constructed, give an error message
		// to the user
		if (returnedComposite == null) {
			Label errorLabel = new Label(top, SWT.NONE);
			String msg = Messages
					.getString("TraceViewerConnectionPreferencesPage.ServiceUiFailed"); //$NON-NLS-1$ 
			errorLabel.setText(msg);
		} else {

			// Select connection from the UI with ID
			String selectedConnId = store
					.getString(PreferenceConstants.SELECTED_CONNECTION_ID);
			ConnectionHelper.selectConnectionFromUIWithID(selectedConnId);
		}

		// Auto-connect to dynamic connections checkbox
		autoConnectDynamicConnectionsCheckBox = new Button(top, SWT.CHECK);
		String autoConnectText = Messages
				.getString("TraceViewerConnectionPreferencesPage.AutoConnectDynamicText"); //$NON-NLS-1$
		String autoConnectToolTip = Messages
				.getString("TraceViewerConnectionPreferencesPage.AutoConnectDynamicToolTip"); //$NON-NLS-1$
		autoConnectDynamicConnectionsCheckBox.setText(autoConnectText);
		autoConnectDynamicConnectionsCheckBox
				.setToolTipText(autoConnectToolTip);
		autoConnectDynamicConnectionsCheckBox
				.setSelection(store
						.getBoolean(PreferenceConstants.AUTO_CONNECT_DYNAMIC_CONNECTIONS_CHECKBOX));

		// Set help
		PlatformUI.getWorkbench().getHelpSystem().setHelp(parent,
				TraceViewerHelpContextIDs.CONNECTION_PREFERENCES);

		return top;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
	 */
	public void init(IWorkbench workbench) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.preference.PreferencePage#performOk()
	 */
	@Override
	public boolean performOk() {
		saveSettings();
		// Preferences has been saved successfully, notifying listeners about
		// the change. Default implementation of super.performApply() calls back
		// performOK() method so notification to listeners should be only sent
		// in here. In case of Apply and OK sequence, however, the notification
		// is sent twice. Only sent the notification if the connection ID is not
		// the virtual "Current connection"
		if (conn != null
				&& !conn.getIdentifier().equals(
						ConnectionHelper.CURRENT_CONNECTION_ID)) {
			TraceViewerAPI2Impl.notifyConnPrefsChanged(conn);
		}
		return super.performOk();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.preference.PreferencePage#performApply()
	 */
	@Override
	public void performApply() {
		saveSettings();
		super.performApply();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.preference.PreferencePage#performDefaults()
	 */
	@Override
	protected void performDefaults() {
		if (getControl() != null && !getControl().isDisposed()) {
			autoConnectDynamicConnectionsCheckBox.setSelection(false);
		}
		super.performDefaults();
	}

	/**
	 * Saves settings
	 */
	private void saveSettings() {
		if (getControl() != null && !getControl().isDisposed()) {
			conn = ConnectionHelper
					.saveConnectionSettingsToPreferenceStore(false);

			store
					.setValue(
							PreferenceConstants.AUTO_CONNECT_DYNAMIC_CONNECTIONS_CHECKBOX,
							autoConnectDynamicConnectionsCheckBox
									.getSelection());
		}
	}
}
