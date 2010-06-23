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
 * Trace configuration load dialog
 *
 */
package com.nokia.traceviewer.dialog;

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
import com.nokia.traceviewer.engine.activation.TraceActivationXMLImporter;

/**
 * Trace configuration load dialog
 * 
 */
public final class TraceConfigurationLoadDialog extends BaseDialog {

	/**
	 * Filter names used in open configuration dialog
	 */
	private static final String[] FILTER_NAMES = { Messages
			.getString("TraceConfigurationLoadDialog.FilterNames") }; //$NON-NLS-1$

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
	private String previousFile = ""; //$NON-NLS-1$

	/**
	 * Previous configuration loaded
	 */
	private String previousConfiguration = ""; //$NON-NLS-1$

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
	 * Changed components from the Activation dialog
	 */
	private List<TraceActivationComponentItem> changedComponents;

	/**
	 * If true, given array already contains the possible components. If false,
	 * components are gathered to the array while importing
	 */
	private boolean arraysAlreadyContainComponents;

	/**
	 * Constructor
	 * 
	 * @param arraysAlreadyContainComponents
	 *            if true, given arrays already contain components and only the
	 *            status of those should be changed
	 * @param previousConfigurationFile
	 *            possible previous configuration file path. Can be null.
	 */
	public TraceConfigurationLoadDialog(boolean arraysAlreadyContainComponents,
			String previousConfigurationFile) {
		super(PlatformUI.getWorkbench().getDisplay().getActiveShell(),
				SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL | SWT.RESIZE);
		this.arraysAlreadyContainComponents = arraysAlreadyContainComponents;
		if (previousConfigurationFile != null) {
			this.previousFile = previousConfigurationFile;
		}
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
				Messages.getString("TraceConfigurationLoadDialog.ShellTitle")); //$NON-NLS-1$
		composite.setLayout(shellGridLayout);

		// File group
		GridLayout fileGroupGridLayout = new GridLayout();
		fileGroupGridLayout.numColumns = 2;
		GridData fileGroupGridData = new GridData(SWT.FILL, SWT.FILL, true,
				true);
		Group fileGroup = new Group(composite, SWT.NONE);
		fileGroup.setText(Messages
				.getString("TraceConfigurationLoadDialog.FileGroupText")); //$NON-NLS-1$
		fileGroup.setLayout(fileGroupGridLayout);
		fileGroup.setLayoutData(fileGroupGridData);

		// Current file Label
		GridData currentFileLabelGridData = new GridData();
		currentFileLabelGridData.horizontalSpan = 2;
		Label currentFileLabel = new Label(fileGroup, SWT.NONE);
		currentFileLabel.setText(Messages
				.getString("TraceConfigurationLoadDialog.FileLabelText")); //$NON-NLS-1$
		currentFileLabel.setLayoutData(currentFileLabelGridData);

		// File Text field
		GridData fileTextGridData = new GridData(SWT.FILL, SWT.FILL, true,
				false);
		fileText = new Text(fileGroup, SWT.BORDER);
		fileText.setEditable(false);
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
				.getString("TraceConfigurationLoadDialog.BrowseButtonText")); //$NON-NLS-1$
		fileBrowseButton.setToolTipText(Messages
				.getString("TraceConfigurationLoadDialog.BrowseButtonToolTip")); //$NON-NLS-1$
		fileBrowseButton.setLayoutData(fileBrowseButtonGridData);

		// Configuration group
		GridLayout configurationGroupGridLayout = new GridLayout();
		GridData configurationGroupGridData = new GridData(SWT.FILL, SWT.FILL,
				true, true);
		Group configurationGroup = new Group(composite, SWT.NONE);
		configurationGroup.setText(Messages
				.getString("TraceConfigurationLoadDialog.ConfGroupText")); //$NON-NLS-1$
		configurationGroup.setLayout(configurationGroupGridLayout);
		configurationGroup.setLayoutData(configurationGroupGridData);

		// Current configuration Label
		GridData currentConfigurationLabelGridData = new GridData();
		currentConfigurationLabelGridData.widthHint = 350;
		Label currentConfigurationLabel = new Label(configurationGroup,
				SWT.NONE);
		currentConfigurationLabel.setText(Messages
				.getString("TraceConfigurationLoadDialog.LoadLabelText")); //$NON-NLS-1$
		currentConfigurationLabel
				.setLayoutData(currentConfigurationLabelGridData);

		// Configuration Combo box
		GridData configurationComboGridData = new GridData(SWT.FILL, SWT.FILL,
				true, false);
		configurationCombo = new Combo(configurationGroup, SWT.BORDER
				| SWT.READ_ONLY);
		configurationCombo.setToolTipText(Messages
				.getString("TraceConfigurationLoadDialog.ConfComboToolTip")); //$NON-NLS-1$
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
	 * Loads configuration
	 */
	private boolean loadConfiguration() {
		boolean succeeded = false;
		String filePath = fileText.getText();
		String confName = configurationCombo.getText();

		if (!confName.equals("")) { //$NON-NLS-1$
			previousConfiguration = confName;

			TraceActivationXMLImporter importer = new TraceActivationXMLImporter(
					filePath);
			boolean success = false;

			// Change the "On / Off" values for already existing components
			if (arraysAlreadyContainComponents) {
				success = importer.importData(allComponents, confName,
						changedComponents);
			}

			if (success) {
				// Inform about load succesful
				String loadMsg = Messages
						.getString("TraceConfigurationLoadDialog.LoadMsg"); //$NON-NLS-1$
				TraceViewerGlobals.getTraceViewer().getDialogs()
						.showInformationMessage(loadMsg);
				succeeded = true;
			}
		} else {
			String emptyConfNameMsg = Messages
					.getString("TraceConfigurationLoadDialog.ConfNameMsg"); //$NON-NLS-1$
			TraceViewerGlobals.getTraceViewer().getDialogs()
					.showInformationMessage(emptyConfNameMsg);
		}

		return succeeded;
	}

	/**
	 * Opens the dialog
	 * 
	 * @param allComponents
	 *            all the components
	 * @param changedComponents
	 *            list of changed components
	 */
	public void openDialog(List<TraceActivationComponentItem> allComponents,
			List<TraceActivationComponentItem> changedComponents) {
		this.allComponents = allComponents;
		this.changedComponents = changedComponents;

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
		boolean succeeded = loadConfiguration();
		if (!succeeded && arraysAlreadyContainComponents) {
			// DO nothing, failed
		} else {
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

	/**
	 * Gets previously loaded file path
	 * 
	 * @return previously loaded file path
	 */
	public String getLoadedConfigurationFilePath() {
		return previousFile;
	}

	/**
	 * Gets previously loaded configuration name
	 * 
	 * @return previously loaded configuration name
	 */
	public String getLoadedConfigurationName() {
		return previousConfiguration;
	}
}
