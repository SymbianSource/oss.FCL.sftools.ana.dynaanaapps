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
 * Trace configuration save dialog
 *
 */
package com.nokia.traceviewer.dialog;

import java.io.File;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;

import com.nokia.traceviewer.TraceViewerHelpContextIDs;
import com.nokia.traceviewer.engine.TraceViewerGlobals;
import com.nokia.traceviewer.engine.activation.TraceActivationComponentItem;
import com.nokia.traceviewer.engine.activation.TraceActivationXMLExporter;
import com.nokia.traceviewer.engine.activation.TraceActivationXMLImporter;

/**
 * Trace configuration save dialog
 * 
 */
public final class TraceConfigurationSaveDialog extends BaseDialog {

	/**
	 * Filter names used in open configuration dialog
	 */
	private static final String[] FILTER_NAMES = { Messages
			.getString("TraceConfigurationSaveDialog.FilterNames") }; //$NON-NLS-1$

	/**
	 * Filter extensions used in open configuration dialog
	 */
	private static final String[] FILTER_EXTS = { "*.xml", "*.*" }; //$NON-NLS-1$ //$NON-NLS-2$

	/**
	 * File text field
	 */
	private Text fileText;

	/**
	 * Previous file selected
	 */
	private String previousFile;

	/**
	 * File browse button
	 */
	private Button fileBrowseButton;

	/**
	 * Configuration combo
	 */
	private Combo configurationCombo;

	/**
	 * All components from the Activation dialog
	 */
	private List<TraceActivationComponentItem> allComponents;

	/**
	 * Constructor
	 */
	public TraceConfigurationSaveDialog() {
		super(PlatformUI.getWorkbench().getDisplay().getActiveShell(),
				SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL | SWT.RESIZE);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.traceviewer.dialog.BaseDialog#createDialogContents()
	 */
	@Override
	protected void createDialogContents() {
		// Shell
		GridLayout shellGridLayout = new GridLayout();
		getShell().setText(
				Messages.getString("TraceConfigurationSaveDialog.ShellTitle")); //$NON-NLS-1$
		composite.setLayout(shellGridLayout);

		// File group
		GridLayout fileGroupGridLayout = new GridLayout();
		fileGroupGridLayout.numColumns = 2;
		GridData fileGroupGridData = new GridData(SWT.FILL, SWT.FILL, true,
				true);
		Group fileGroup = new Group(composite, SWT.NONE);
		fileGroup.setText(Messages
				.getString("TraceConfigurationSaveDialog.FileGroupText")); //$NON-NLS-1$
		fileGroup.setLayout(fileGroupGridLayout);
		fileGroup.setLayoutData(fileGroupGridData);

		// Current file Label
		GridData currentFileLabelGridData = new GridData();
		currentFileLabelGridData.horizontalSpan = 2;
		Label currentFileLabel = new Label(fileGroup, SWT.NONE);
		currentFileLabel
				.setText(Messages
						.getString("TraceConfigurationSaveDialog.CurrentFileLabelText")); //$NON-NLS-1$
		currentFileLabel.setLayoutData(currentFileLabelGridData);

		// File Text field
		GridData fileTextGridData = new GridData(SWT.FILL, SWT.FILL, true,
				false);
		fileText = new Text(fileGroup, SWT.BORDER);
		fileText.setLayoutData(fileTextGridData);
		if (previousFile != null) {
			fileText.setText(previousFile);
			fileText.setSelection(fileText.getText().length());
		}

		// Browse button
		GridData fileBrowseButtonGridData = new GridData();
		fileBrowseButtonGridData.widthHint = 75;
		fileBrowseButton = new Button(fileGroup, SWT.PUSH);
		fileBrowseButton.setText(Messages
				.getString("TraceConfigurationSaveDialog.BrowseButtonText")); //$NON-NLS-1$
		fileBrowseButton.setToolTipText(Messages
				.getString("TraceConfigurationSaveDialog.BrowseButtonToolTip")); //$NON-NLS-1$
		fileBrowseButton.setLayoutData(fileBrowseButtonGridData);

		// Configuration group
		GridLayout configurationGroupGridLayout = new GridLayout();
		GridData configurationGroupGridData = new GridData(SWT.FILL, SWT.FILL,
				true, true);
		Group configurationGroup = new Group(composite, SWT.NONE);
		configurationGroup.setText(Messages
				.getString("TraceConfigurationSaveDialog.ConfGroupText")); //$NON-NLS-1$
		configurationGroup.setLayout(configurationGroupGridLayout);
		configurationGroup.setLayoutData(configurationGroupGridData);

		// Current configuration Label
		GridData currentConfigurationLabelGridData = new GridData();
		currentConfigurationLabelGridData.widthHint = 350;
		Label currentConfigurationLabel = new Label(configurationGroup,
				SWT.NONE);
		currentConfigurationLabel.setText(Messages
				.getString("TraceConfigurationSaveDialog.ConfLabelText")); //$NON-NLS-1$
		currentConfigurationLabel
				.setLayoutData(currentConfigurationLabelGridData);

		// Configuration Combo box
		GridData configurationComboGridData = new GridData(SWT.FILL, SWT.FILL,
				true, false);
		configurationCombo = new Combo(configurationGroup, SWT.BORDER);
		configurationCombo.setToolTipText(Messages
				.getString("TraceConfigurationSaveDialog.ConfComboToolTip")); //$NON-NLS-1$
		configurationCombo.setLayoutData(configurationComboGridData);
		if (previousFile == null || previousFile.equals("")) { //$NON-NLS-1$
			configurationCombo.setEnabled(false);
		}

		getConfigurationNamesFromFile();

		// Set help
		PlatformUI.getWorkbench().getHelpSystem().setHelp(getShell(),
				TraceViewerHelpContextIDs.ACTIVATION_CONFIGURATIONS);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.traceviewer.dialog.BaseDialog#createActionListeners()
	 */
	@Override
	protected void createActionListeners() {

		// Add selection listener to file browse button
		fileBrowseButton.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent event) {
				FileDialog dlg = new FileDialog(PlatformUI.getWorkbench()
						.getDisplay().getActiveShell(), SWT.OPEN);
				dlg.setFilterNames(FILTER_NAMES);
				dlg.setFilterExtensions(FILTER_EXTS);
				String fn = dlg.open();
				if (fn != null) {
					if (!fn.endsWith(".xml")) { //$NON-NLS-1$
						fn += ".xml"; //$NON-NLS-1$
					}
					fileText.setText(fn);
					previousFile = fn;
					configurationCombo.setEnabled(true);

					getConfigurationNamesFromFile();
				}
			}
		});

		// Add modify listener to configuration combo box
		configurationCombo.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				checkEnableOkButton();
			}
		});

		// Add modify listener to file text field
		fileText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				if (!fileText.getText().equals("")) { //$NON-NLS-1$
					configurationCombo.setEnabled(true);
				} else {
					configurationCombo.setEnabled(false);
				}
				checkEnableOkButton();
			}
		});

	}

	/**
	 * Gets configuration names from XML file
	 */
	private void getConfigurationNamesFromFile() {
		String filePath = fileText.getText();

		// Load configurations from given file
		TraceActivationXMLImporter importer = new TraceActivationXMLImporter(
				filePath);

		String[] configurationNames = importer.importConfigurationNames();
		if (configurationNames != null && configurationNames.length > 0) {
			configurationCombo.setItems(configurationNames);
			configurationCombo.setText(configurationNames[0]);
		} else {
			configurationCombo.removeAll();
		}
	}

	/**
	 * Saves configuration
	 * 
	 * @return true if saving succeeded
	 */
	private boolean saveConfiguration() {
		boolean success = false;
		String filePath = fileText.getText();
		String confName = configurationCombo.getText();
		File file = new File(filePath);

		// File must be absolute
		if (file.isAbsolute()) {

			if (!confName.equals("")) { //$NON-NLS-1$

				TraceActivationXMLExporter exporter = new TraceActivationXMLExporter(
						allComponents, filePath, confName);
				success = exporter.export();

				// Inform about save succesful
				if (success) {
					String saveMsg = Messages
							.getString("TraceConfigurationSaveDialog.SaveMsg"); //$NON-NLS-1$
					TraceViewerGlobals.getTraceViewer().getDialogs()
							.showInformationMessage(saveMsg);

					// Insert to combo box if not there
					String[] oldConfs = configurationCombo.getItems();
					boolean found = false;
					for (int i = 0; i < oldConfs.length; i++) {
						if (confName.equals(oldConfs[i])) {
							found = true;
						}
					}
					if (!found) {
						configurationCombo.add(confName, 0);
					}
				}
			} else {
				String emptyConfNameMsg = Messages
						.getString("TraceConfigurationSaveDialog.EmptyConfMsg"); //$NON-NLS-1$
				TraceViewerGlobals.getTraceViewer().getDialogs()
						.showInformationMessage(emptyConfNameMsg);
			}
		} else {
			String notValidFileMsg = Messages
					.getString("TraceConfigurationSaveDialog.NotValidFilePath"); //$NON-NLS-1$
			TraceViewerGlobals.getTraceViewer().getDialogs().showErrorMessage(
					notValidFileMsg);
		}
		return success;
	}

	/**
	 * Opens the dialog
	 * 
	 * @param allComponents
	 *            all the components
	 */
	public void openDialog(List<TraceActivationComponentItem> allComponents) {
		this.allComponents = allComponents;
		open();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jface.dialogs.Dialog#createButtonsForButtonBar(org.eclipse
	 * .swt.widgets.Composite)
	 */
	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		super.createButtonsForButtonBar(parent);
		if (configurationCombo == null || configurationCombo.getText() == null
				|| configurationCombo.getText().equals("")) { //$NON-NLS-1$
			getButton(OK).setEnabled(false);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.dialogs.Dialog#okPressed()
	 */
	@Override
	protected void okPressed() {
		if (saveConfiguration()) {
			super.okPressed();
		}
	}

	/**
	 * Check if OK button can be enabled and if yes, do it
	 */
	private void checkEnableOkButton() {
		if (configurationCombo.getText().equals("")) { //$NON-NLS-1$
			getButton(OK).setEnabled(false);

			// Set OK button enabled
		} else if (!fileText.getText().equals("")) { //$NON-NLS-1$
			getButton(OK).setEnabled(true);
		}
	}
}
