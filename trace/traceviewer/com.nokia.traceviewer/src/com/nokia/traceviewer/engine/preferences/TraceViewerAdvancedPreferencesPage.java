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
 * Advanced preferences page
 *
 */
package com.nokia.traceviewer.engine.preferences;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.PlatformUI;

import com.nokia.traceviewer.TraceViewerHelpContextIDs;
import com.nokia.traceviewer.TraceViewerPlugin;
import com.nokia.traceviewer.engine.TraceViewerGlobals;

/**
 * Advanced preferences page
 * 
 */
public class TraceViewerAdvancedPreferencesPage extends PreferencePage
		implements IWorkbenchPreferencePage {

	/**
	 * Text to show about enabling external filter
	 */
	private static final String EXTERNALFILTER_INFORMATION = Messages
			.getString("TraceViewerAdvancedPreferencesPage.ExternalFilterInformation"); //$NON-NLS-1$

	/**
	 * PreferenceStore holding all preferences
	 */
	private final IPreferenceStore store;

	/**
	 * External filter checkbox
	 */
	private Button externalFilterCheckBox;

	/**
	 * External filter text field
	 */
	private Text externalFilterText;

	/**
	 * External filter browse button
	 */
	private Button externalFilterBrowseButton;

	/**
	 * Constructor
	 */
	public TraceViewerAdvancedPreferencesPage() {
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

		// Set help
		PlatformUI.getWorkbench().getHelpSystem().setHelp(parent,
				TraceViewerHelpContextIDs.ADVANCED_PREFERENCES);

		// Create Top composite in top of the parent composite
		Composite top = new Composite(parent, SWT.LEFT);
		GridData topCompositeGridData = new GridData(SWT.FILL, SWT.FILL, true,
				false);
		top.setLayoutData(topCompositeGridData);
		GridLayout topCompositeGridLayout = new GridLayout();
		topCompositeGridLayout.horizontalSpacing = 0;
		topCompositeGridLayout.verticalSpacing = 0;
		topCompositeGridLayout.marginWidth = 0;
		topCompositeGridLayout.marginHeight = 0;
		top.setLayout(topCompositeGridLayout);

		// Advanced group
		GridLayout advancedGroupGridLayout = new GridLayout();
		advancedGroupGridLayout.numColumns = 3;
		Group advancedGroup = new Group(top, SWT.NONE);
		String advGroupName = Messages
				.getString("TraceViewerAdvancedPreferencesPage.AdvancedGroupName"); //$NON-NLS-1$
		advancedGroup.setText(advGroupName);
		GridData advancedGroupGridData = new GridData(SWT.FILL, SWT.FILL, true,
				false);
		advancedGroup.setLayoutData(advancedGroupGridData);
		advancedGroup.setLayout(advancedGroupGridLayout);

		// External filter checkbox
		String extFilterText = Messages
				.getString("TraceViewerAdvancedPreferencesPage.ExternalFilterText"); //$NON-NLS-1$
		GridData externalFilterGridData = new GridData(SWT.FILL, SWT.FILL,
				true, false);
		externalFilterGridData.horizontalSpan = 3;
		externalFilterCheckBox = new Button(advancedGroup, SWT.CHECK);
		externalFilterCheckBox.setText(extFilterText);
		externalFilterCheckBox.setToolTipText(extFilterText);
		externalFilterCheckBox.setLayoutData(externalFilterGridData);
		externalFilterCheckBox.setSelection(store
				.getBoolean(PreferenceConstants.EXTERNAL_FILTER_CHECKBOX));

		// External filter label
		String commandEditorText = Messages
				.getString("TraceViewerAdvancedPreferencesPage.CommandEditorText"); //$NON-NLS-1$
		Label filterLabel = new Label(advancedGroup, SWT.NONE);
		filterLabel.setText(commandEditorText);

		// External filter text
		GridData externalFilterTextGridData = new GridData();
		externalFilterTextGridData.grabExcessHorizontalSpace = true;
		externalFilterTextGridData.horizontalAlignment = SWT.FILL;
		externalFilterText = new Text(advancedGroup, SWT.BORDER);
		externalFilterText.setText(store
				.getString(PreferenceConstants.EXTERNAL_FILTER_COMMAND));
		externalFilterText.setLayoutData(externalFilterTextGridData);

		// External filter browse button
		GridData externalFilterBrowseButtonGridData = new GridData();
		externalFilterBrowseButtonGridData.widthHint = 75;
		externalFilterBrowseButton = new Button(advancedGroup, SWT.NONE);
		String browseText = Messages
				.getString("TraceViewerAdvancedPreferencesPage.BrowseButtonText"); //$NON-NLS-1$
		externalFilterBrowseButton.setText(browseText);
		String browseToolTip = Messages
				.getString("TraceViewerAdvancedPreferencesPage.BrowseButtonToolTip"); //$NON-NLS-1$
		externalFilterBrowseButton.setToolTipText(browseToolTip);
		externalFilterBrowseButton
				.setLayoutData(externalFilterBrowseButtonGridData);

		// Disable if checkbox is not checked
		if (!externalFilterCheckBox.getSelection()) {
			externalFilterText.setEnabled(false);
			externalFilterBrowseButton.setEnabled(false);
		}

		// Spacer label
		Label spacerLabel = new Label(advancedGroup, SWT.NONE);
		GridData spacerLabelGridData = new GridData();
		spacerLabelGridData.horizontalSpan = 3;
		spacerLabel.setLayoutData(spacerLabelGridData);

		// Information label
		Label label = new Label(advancedGroup, SWT.WRAP);
		label.setText(EXTERNALFILTER_INFORMATION);
		GridData labelGridData = new GridData();
		labelGridData.horizontalSpan = 3;
		label.setLayoutData(labelGridData);

		// Create action listeners
		createActionListeners();

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
	 * @see org.eclipse.jface.preference.PreferencePage#performDefaults()
	 */
	@Override
	protected void performDefaults() {
		if (getControl() != null && !getControl().isDisposed()) {
			externalFilterCheckBox.setSelection(false);
			externalFilterText.setText(""); //$NON-NLS-1$
			super.performDefaults();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.preference.PreferencePage#performOk()
	 */
	@Override
	public boolean performOk() {
		saveSettings();
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

	/**
	 * Saves settings
	 */
	private void saveSettings() {
		if (getControl() != null && !getControl().isDisposed()) {

			// Give FilterProcessor the information about using external filter
			// or
			TraceViewerGlobals.getTraceViewer().getDataProcessorAccess()
					.getFilterProcessor().setUsingExternalFilter(
							externalFilterCheckBox.getSelection());

			store.setValue(PreferenceConstants.EXTERNAL_FILTER_COMMAND,
					externalFilterText.getText());

			store.setValue(PreferenceConstants.EXTERNAL_FILTER_CHECKBOX,
					externalFilterCheckBox.getSelection());
		}
	}

	/**
	 * Creates action listeners
	 */
	private void createActionListeners() {
		// Add listener to checkbox
		externalFilterCheckBox.addSelectionListener(new SelectionAdapter() {

			/*
			 * (non-Javadoc)
			 * 
			 * @see
			 * org.eclipse.swt.events.SelectionAdapter#widgetSelected(org.eclipse
			 * .swt.events.SelectionEvent)
			 */
			@Override
			public void widgetSelected(SelectionEvent e) {
				boolean selected = externalFilterCheckBox.getSelection();
				if (selected) {
					externalFilterText.setEnabled(true);
					externalFilterBrowseButton.setEnabled(true);
				} else {
					externalFilterText.setEnabled(false);
					externalFilterBrowseButton.setEnabled(false);
				}
			}

		});

		// Add listener to Browse button
		externalFilterBrowseButton.addSelectionListener(new SelectionAdapter() {
			/*
			 * (non-Javadoc)
			 * 
			 * @see
			 * org.eclipse.swt.events.SelectionAdapter#widgetSelected(org.eclipse
			 * .swt.events.SelectionEvent)
			 */
			@Override
			public void widgetSelected(SelectionEvent e) {
				Shell activeShell = PlatformUI.getWorkbench().getDisplay()
						.getActiveShell();
				Shell fileDialogShell = new Shell(activeShell);
				FileDialog dlg = new FileDialog(fileDialogShell);

				// Move the dialog to the center of the top level shell.
				Rectangle shellBounds = activeShell.getBounds();
				Point dialogSize = fileDialogShell.getSize();
				int middleX = shellBounds.x
						+ ((shellBounds.width - dialogSize.x) / 2);
				int middleY = shellBounds.y
						+ ((shellBounds.height - dialogSize.y) / 2);
				fileDialogShell.setLocation(middleX, middleY);

				String file = dlg.open();
				externalFilterText.setText(file);
			}
		});
	}
}
