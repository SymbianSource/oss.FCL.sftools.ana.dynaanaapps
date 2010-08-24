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
 * Description:  Definitions for the class AnalyzeToolPreferencePage
 *
 */

package com.nokia.s60tools.analyzetool.preferences;

import java.io.File;
import java.text.MessageFormat;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.RadioGroupFieldEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.PlatformUI;

import com.nokia.carbide.cpp.epoc.engine.image.ISVGSource;
import com.nokia.s60tools.analyzetool.Activator;
import com.nokia.s60tools.analyzetool.AnalyzeToolHelpContextIDs;
import com.nokia.s60tools.analyzetool.global.Constants;
import com.nokia.s60tools.analyzetool.global.Util;
import com.nokia.s60tools.analyzetool.ui.IActionListener;

/**
 * This class represents a preference page that is contributed to the
 * Preferences dialog. By subclassing <samp>FieldEditorPreferencePage</samp>, we
 * can use the field support built into JFace that allows us to create a page
 * that is small and knows how to save, restore and apply itself.
 * <p>
 * This page is used to modify preferences only. They are stored in the
 * preference store that belongs to the main plug-in class. That way,
 * preferences can be accessed directly via the preference store.
 */

public class AnalyzeToolPreferencePage extends FieldEditorPreferencePage
		implements IWorkbenchPreferencePage, Listener {

	/** Button to ask always. */
	Button askButton = null;

	/** Atool.exe path field. */
	Text atoolDirText = null;

	/** User specified atool.exe path. */
	Label atoolDir = null;

	/** Atool.exe version field. */
	Label atoolVerLabel;

	/** Browse button to active file selection dialog. */
	Button browseButton = null;

	/** Group for callstack size buttons. */
	Group csSizeGroup;

	/** Custom items button */
	Button customButton;

	/** Forty items button */
	Button fortyButton;

	/** Hundred items button */
	Button hundredButton;

	/** Radio group for report level. */
	RadioGroupFieldEditor reportLevels = null;

	/** Button to use S60 file mode. */
	Button s60Button = null;

	/**
	 * Spinner to define custom size of callstack, this item is visible only
	 * when custom button is selected.
	 */
	Spinner spinner;

	/**
	 * Button to use default atool.exe location this means that atool.exe is
	 * executed inside AnalyzeTool jar.
	 */
	Button useDefaultLocation = null;

	/** Use user specified location. */
	Button useUserSpecified = null;

	/** Button to verbose atool.exe output. */
	Button verboseButton = null;

	/** Button to select fast external data gathering mode */
	Button externalFastButton = null;

	/** No items button. */
	Button zeroButton;

	Label logPath;
	Text logPathText;
	Label fileName;
	Text fileNameText;

	/**
	 * Constructor.
	 */
	public AnalyzeToolPreferencePage() {
		super(GRID);
		setPreferenceStore(Activator.getDefault().getPreferenceStore());
		setDescription(Constants.ATOOL_DESC);
	}

	/**
	 * Checks preferences initial values if logging mode is not set to S60
	 * disables S60 data file name selections.
	 */
	public final void checkInitValues() {
		IPreferenceStore store = Activator.getPreferences();

		// get stored atool folder
		String atoolFolder = store.getString(Constants.ATOOL_FOLDER);
		atoolVerLabel.setText(Constants.PREFS_ATOOL_VER_NOT_FOUND);

		// if atool folder is set to point default atool location
		if (atoolFolder.equals(Constants.DEFAULT_ATOOL_FOLDER)) {

			// check that stored atool location exists
			File file = new File(atoolFolder);
			if (file.exists()) { // if exists use this location and update
				// preference page buttons
				useDefaultLocation.setSelection(false);
				store.setValue(Constants.USE_INTERNAL, false);

			} else { // location could not found => use internal atool
				useDefaultLocation.setSelection(true);
				store.setValue(Constants.USE_INTERNAL, true);
			}
		} else {
			boolean useDef = store.getBoolean(Constants.USE_INTERNAL);
			useDefaultLocation.setSelection(useDef);
		}

		// get atool.exe path and set it atool.exe path field
		String atoolPath = store.getString(Constants.USER_SELECTED_FOLDER);
		atoolDirText.setText(atoolPath);

		// update preference page buttons
		handleDefaultLocationChange();

		// get logging mode and update buttons
		String fileMode = store.getString(Constants.LOGGING_MODE);
		setGroupButtons(fileMode);

		logPathText.setText(store.getString(Constants.DEVICE_LOG_FILE_PATH));
		fileNameText.setText(store.getString(Constants.DEVICE_LOG_FILE_NAME));

		logPathText.setText(store.getString(Constants.DEVICE_LOG_FILE_PATH));
		fileNameText.setText(store.getString(Constants.DEVICE_LOG_FILE_NAME));

		verboseButton.setSelection(store.getBoolean(Constants.ATOOL_VERBOSE));

		// get stored callstack size
		int callstackSize = store.getInt(Constants.CALLSTACK_SIZE);
		if (callstackSize == 0) {
			zeroButton.setSelection(true);
			spinner.setEnabled(false);
		} else if (callstackSize == 40) {
			fortyButton.setSelection(true);
			spinner.setEnabled(false);
		} else if (callstackSize == 100) {
			hundredButton.setSelection(true);
			spinner.setEnabled(false);
		} else {
			// if callstack size is set to custom area
			// enable spinner and set stored callstack size
			customButton.setSelection(true);
			spinner.setEnabled(true);
			spinner.setSelection(callstackSize);
		}
	}

	/**
	 * Check if user entered folder location is available.
	 * 
	 * @param folderLocation
	 *            User entered folder location
	 * @return True if folder exists otherwise False
	 */
	public final boolean checkIfFolderExists(final String folderLocation) {
		if (folderLocation.length() > 0) {
			StringBuffer tmpFolderLoc = new StringBuffer();
			tmpFolderLoc.append(folderLocation);
			// folder is not end with backslash => add it
			if (!folderLocation.endsWith("\\")) {
				// folderLocation += "\\";
				tmpFolderLoc.append('\\');
			}
			tmpFolderLoc.append("atool.exe");
			IPath atoolPath = new Path(tmpFolderLoc.toString());

			// if folder does not exists
			if (atoolPath.toFile().exists()) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Creates callstack size group items.
	 */
	private void createCallstackSizeGroup() {
		// create new button group for callstack size
		csSizeGroup = new Group(getFieldEditorParent(), SWT.NULL);
		csSizeGroup.setText("Stored callstack size");

		// set group layout
		final GridLayout layoutCsSize = new GridLayout();
		layoutCsSize.numColumns = 1;
		csSizeGroup.setLayout(layoutCsSize);

		GridData gridData = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		gridData.horizontalSpan = 1;
		csSizeGroup.setLayoutData(gridData);

		// zero size button
		zeroButton = new Button(csSizeGroup, SWT.RADIO);
		zeroButton.setText(Constants.PREFS_ZERO_BUTTON);
		zeroButton.addListener(SWT.Selection, this);

		// forty items button
		fortyButton = new Button(csSizeGroup, SWT.RADIO);
		fortyButton.setText(Constants.PREFS_FORTY_BUTTON);
		fortyButton.addListener(SWT.Selection, this);

		// hundred items button
		hundredButton = new Button(csSizeGroup, SWT.RADIO);
		hundredButton.setText(Constants.PREFS_HUNDRED_BUTTON);
		hundredButton.addListener(SWT.Selection, this);

		// button for define custom size of callstack
		customButton = new Button(csSizeGroup, SWT.RADIO);
		customButton.setText(Constants.PREFS_CUSTOM_BUTTON);
		customButton.addListener(SWT.Selection, this);

		// new composite for spinner item
		Composite customComp = new Composite(csSizeGroup, SWT.NULL);
		final GridLayout customGrid = new GridLayout();
		customGrid.marginLeft = 15;
		customGrid.numColumns = 1;
		customComp.setLayout(customGrid);

		spinner = new Spinner(customComp, SWT.BORDER);
		spinner.setMaximum(255);
	}

	/**
	 * Creates data gathering group items
	 */
	private void createGatheringGroup() {
		// create new button group for logging mode
		Group groupGatheringMode = new Group(getFieldEditorParent(), SWT.NULL);
		groupGatheringMode.setText(Constants.PREFS_LOGGING_MODE_TITLE);

		// set group layout
		final GridLayout layoutLogMode = new GridLayout();
		layoutLogMode.numColumns = 1;
		groupGatheringMode.setLayout(layoutLogMode);

		GridData gridData = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		gridData.horizontalSpan = 1;
		groupGatheringMode.setLayoutData(gridData);

		// External fast mode button
		externalFastButton = new Button(groupGatheringMode, SWT.RADIO);
		externalFastButton.setToolTipText(Constants.PREFS_EXT_FAST_TOOLTIP);
		externalFastButton.setText(Constants.PREFS_EXT_FAST);
		externalFastButton.addListener(SWT.Selection, this);

		// S60 mode button
		s60Button = new Button(groupGatheringMode, SWT.RADIO);
		s60Button.setToolTipText(Constants.PREFS_S60_TOOLTIP);
		s60Button.setText(Constants.PREFS_S60);
		s60Button.addListener(SWT.Selection, this);

		Composite compS60 = new Composite(groupGatheringMode, SWT.NULL);

		final GridLayout layoutS60 = new GridLayout();
		layoutS60.marginLeft = 15;
		layoutS60.numColumns = 2;
		compS60.setLayout(layoutS60);

		// path label
		logPath = new Label(compS60, SWT.NONE);
		logPath.setToolTipText("Log file path in the device.");
		logPath.setText("Log file path:");

		// path field
		logPathText = new Text(compS60, SWT.BORDER);
		logPathText.setLayoutData(new GridData(280, SWT.DEFAULT));
		logPathText.setText("C:\\logs\\atool\\");

		// filename label
		fileName = new Label(compS60, SWT.NONE);
		fileName.setToolTipText("Log file name.");
		fileName.setText("Filename:");

		// filename field
		fileNameText = new Text(compS60, SWT.BORDER);
		fileNameText.setLayoutData(new GridData(280, SWT.DEFAULT));
		fileNameText.setText("%processname%.dat");

		// ask always button
		askButton = new Button(groupGatheringMode, SWT.RADIO);
		askButton.setToolTipText(Constants.PREFS_ASK_ALWAYS_TOOLTIP);
		askButton.setText(Constants.PREFS_ASK_ALWAYS);
		askButton.addListener(SWT.Selection, this);
	}

	/**
	 * Creates the field editors. Field editors are abstractions of the common
	 * GUI blocks needed to manipulate various types of preferences. Each field
	 * editor knows how to save and restore itself.
	 */
	@Override
	public final void createFieldEditors() {

		Composite composite = new Composite(getFieldEditorParent(), SWT.NULL);
		final GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 3;

		// set layoyt of this view
		composite.setLayout(gridLayout);

		// new group for atool.exe specific settings
		Group groupAtool = new Group(getFieldEditorParent(), SWT.NULL);
		groupAtool.setText(Constants.PREFS_ATOOL_GROUP_TITLE);

		// set group layout
		final GridLayout layoutAtool = new GridLayout();
		layoutAtool.numColumns = 1;
		groupAtool.setLayout(layoutAtool);

		// grid data for the atool.exe group
		GridData gridDataAtool = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		gridDataAtool.horizontalSpan = 1;
		groupAtool.setLayoutData(gridDataAtool);

		// create default location button and add listener to it
		useDefaultLocation = new Button(groupAtool, SWT.RADIO);
		useDefaultLocation.setText(Constants.PREFS_USE_INTERNAL_TITLE);
		useDefaultLocation.setToolTipText(Constants.PREFS_USE_INTERNAL_TITLE);
		useDefaultLocation.addListener(SWT.Selection, this);

		// create user specified location button and add listener to it
		useUserSpecified = new Button(groupAtool, SWT.RADIO);
		useUserSpecified.setText(Constants.PREFS_USE_EXTERNAL_TITLE);
		useUserSpecified.setToolTipText(Constants.PREFS_USE_EXTERNAL_TITLE);
		useUserSpecified.addListener(SWT.Selection, this);

		Composite compAtool = new Composite(groupAtool, SWT.NULL);

		final GridLayout layoutAtoolDir = new GridLayout();
		layoutAtoolDir.marginLeft = 15;
		layoutAtoolDir.numColumns = 3;
		compAtool.setLayout(layoutAtoolDir);

		// directory label
		atoolDir = new Label(compAtool, SWT.NONE);
		atoolDir.setToolTipText(Constants.PREFS_SELECT_FOLDER);
		atoolDir.setText(Constants.PREFS_ATOOL_PATH);

		// directory field
		atoolDirText = new Text(compAtool, SWT.BORDER);
		atoolDirText.setEditable(false);
		atoolDirText.setLayoutData(new GridData(200, SWT.DEFAULT));

		// button which opens the folder selection dialog
		browseButton = new Button(compAtool, SWT.NONE);
		browseButton.setToolTipText(Constants.PREFS_SELECT_FOLDER);
		browseButton.setText(Constants.PREFS_BROWSE);
		browseButton.addListener(SWT.Selection, this);

		// verbose atool.exe output
		verboseButton = new Button(groupAtool, SWT.CHECK);
		verboseButton.setText(Constants.PREFS_VERBOSE);
		verboseButton.setToolTipText(Constants.PREFS_VERBOSE_TOOLTIP);

		// create new group for the atool.exe version
		Group groupVersion = new Group(groupAtool, SWT.NONE);

		// set atool.exe version group layout
		GridData gridDataVersion = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		gridDataVersion.horizontalSpan = 1;
		groupVersion.setLayoutData(gridDataVersion);

		// set group layout
		final GridLayout layoutVersion = new GridLayout(4, false);
		groupVersion.setLayout(layoutVersion);

		// atool.exe version label title
		Label versionTextLabel = new Label(groupVersion, SWT.NONE);
		versionTextLabel.setText(Constants.PREFS_ENGINE_VERSION);

		// create atool.exe version field and set layout
		atoolVerLabel = new Label(groupVersion, SWT.NONE);
		GridData vergd13 = new GridData(GridData.FILL_HORIZONTAL);
		vergd13.horizontalSpan = 2;
		atoolVerLabel.setLayoutData(vergd13);

		// create data gathering group
		createGatheringGroup();

		// create callstack size group
		createCallstackSizeGroup();

		// report level related settings
		reportLevels = new RadioGroupFieldEditor(Constants.REPORT_LEVEL,
				Constants.PREFS_REPORT_LEVEL, 1, new String[][] {
						{ Constants.PREFS_SHOW_EVERY, Constants.REPORT_EVERY },
						{ Constants.PREFS_SHOW_KNOWN, Constants.REPORT_KNOWN },
						{ Constants.PREFS_SHOW_TOPMOST,
								Constants.REPORT_TOPMOST } },
				getFieldEditorParent(), true);

		addField(reportLevels);

		checkInitValues();
		PlatformUI.getWorkbench().getHelpSystem().setHelp(super.getControl(),
				AnalyzeToolHelpContextIDs.ANALYZE_TOOL_VIEW_MEM_LEAKS);
	}

	@Override
	protected void checkState() {
		super.checkState();

		String path;

		if (useDefaultLocation.getSelection()) {
			path = null;
		} else {
			path = atoolDirText.getText();
		}

		if (Util.getAtoolVersionNumber(path).equals(
				Constants.PREFS_ATOOL_VER_NOT_FOUND)) {
			setErrorMessage(Constants.PREFS_CLE_NOT_AVAILABLE);
			setValid(false);
		} else if (Util.compareVersionNumber(Util.getAtoolVersionNumber(path),
				Constants.MIN_CLE_SUPPORTED) == Constants.VERSION_NUMBERS_SECOND) {
			setErrorMessage(MessageFormat.format(
					Constants.PREFS_CLE_OLDER_THAN_MIN,
					Constants.MIN_CLE_SUPPORTED));
			setValid(false);
		} else {
			setErrorMessage(null);
			setValid(true);
		}
	}

	/**
	 * Handles atool.exe location selection changes. Update corresponding
	 * buttons states.
	 */
	public final void handleDefaultLocationChange() {
		if (useDefaultLocation.getSelection()) {
			atoolDirText.setEnabled(false);
			browseButton.setEnabled(false);
			atoolDir.setEnabled(false);
			useUserSpecified.setSelection(false);
			updateAtoolVersion(null);
		} else {
			atoolDirText.setEnabled(true);
			browseButton.setEnabled(true);
			atoolDir.setEnabled(true);
			useUserSpecified.setSelection(true);
			updateAtoolVersion(atoolDirText.getText());
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.swt.widgets.Listener#handleEvent(org.eclipse.swt.widgets.
	 * Event)
	 */
	public final void handleEvent(final Event event) {
		if (event.widget == browseButton) {
			openFolderDialog();
		} else if (event.widget == externalFastButton
				|| event.widget == askButton) {
			setFileModeEnabled(false);
		} else if (event.widget == s60Button) {
			setFileModeEnabled(true);
		} else if (event.widget == useDefaultLocation
				|| event.widget == atoolDir) {
			handleDefaultLocationChange();
		} else if (event.widget == zeroButton || event.widget == fortyButton
				|| event.widget == hundredButton) {
			spinner.setEnabled(false);
		} else if (event.widget == customButton) {
			spinner.setEnabled(true);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
	 */
	public void init(final IWorkbench workbench) {
		// MethodDeclaration/Block[count(BlockStatement) = 0 and
		// @containsComment = 'false']
	}

	/**
	 * Opens folder selection dialog.
	 */
	public final void openFolderDialog() {
		DirectoryDialog folderDialog = new DirectoryDialog(atoolDirText
				.getShell(), SWT.OPEN);
		folderDialog.setText(Constants.PREFS_SELECT_DIR);
		folderDialog.setFilterPath(atoolDirText.getText());
		String folderPath = folderDialog.open();
		if (folderPath != null) {
			atoolDirText.setText(folderPath);
			updateAtoolVersion(atoolDirText.getText());
		}
	}

	/**
	 * Perform defaults for AnalyzeTool preferences.
	 */
	@Override
	public final void performDefaults() {
		setGroupButtons(Constants.LOGGING_EXT_FAST);
		atoolDirText.setText(Constants.DEFAULT_ATOOL_FOLDER);
		useDefaultLocation.setSelection(true);
		handleDefaultLocationChange();
		verboseButton.setSelection(false);
		zeroButton.setSelection(false);
		fortyButton.setSelection(true);
		hundredButton.setSelection(false);
		customButton.setSelection(false);
		spinner.setEnabled(false);

		logPathText.setText("C:\\logs\\atool\\");
		fileNameText.setText("%processname%.dat");
	}

	/**
	 * Stores selected values When user press "Ok" or "apply" button this method
	 * is called
	 */
	@Override
	public final boolean performOk() {

		IPreferenceStore store = Activator.getPreferences();

		// use default location is selected
		if (useDefaultLocation.getSelection()) {
			store.setValue(Constants.ATOOL_FOLDER, Util
					.getDefaultAtoolLocation());
			store.setValue(Constants.USE_INTERNAL, true);
		}
		// using user specified atool.exe location and the folder contains
		// atool.exe
		else {
			store.setValue(Constants.ATOOL_FOLDER, atoolDirText.getText());
			store.setValue(Constants.USER_SELECTED_FOLDER, atoolDirText
					.getText());
			store.setValue(Constants.USE_INTERNAL, false);
		}

		// store logging mode
		if (askButton.getSelection()) {
			store.setValue(Constants.LOGGING_MODE,
					Constants.LOGGING_ASK_ALLWAYS);
		} else if (s60Button.getSelection()) {
			store.setValue(Constants.LOGGING_MODE, Constants.LOGGING_S60);
		} else if (externalFastButton.getSelection()) {
			store.setValue(Constants.LOGGING_MODE, Constants.LOGGING_EXT_FAST);
		}
		store.setValue(Constants.DEVICE_LOG_FILE_PATH, logPathText.getText());
		store.setValue(Constants.DEVICE_LOG_FILE_NAME, fileNameText.getText());

		// store value of verbose atool.exe output
		store.setValue(Constants.ATOOL_VERBOSE, verboseButton.getSelection());

		// update preference value
		// this values is used later when UI creates/update toolbar options and
		// when building project with "ask always" option
		if (externalFastButton.isEnabled()) {
			store.setValue(Constants.LOGGING_FAST_ENABLED, true);
		} else {
			store.setValue(Constants.LOGGING_FAST_ENABLED, false);
		}

		// get callstack size
		int size = 0;
		boolean userDefinedCSSize = false;
		if (zeroButton.getSelection() && zeroButton.isEnabled()) {
			userDefinedCSSize = true;
		} else if (fortyButton.getSelection() && fortyButton.isEnabled()) {
			size = 40;
			userDefinedCSSize = true;
		} else if (hundredButton.getSelection() && hundredButton.isEnabled()) {
			size = 100;
			userDefinedCSSize = true;
		} else if (customButton.getSelection() && customButton.isEnabled()) {
			size = spinner.getSelection();
			userDefinedCSSize = true;
		}

		// store callstack size
		store.setValue(Constants.USE_CALLSTACK_SIZE, userDefinedCSSize);
		store.setValue(Constants.CALLSTACK_SIZE, size);

		// update view with new settings
		IActionListener listener = Activator.getActionListener();
		if (listener != null) {
			listener.preferenceChanged();
		}

		// store report detail level prefs
		return super.performOk();
	}

	/**
	 * Sets S60 file options visible.
	 * 
	 * @param enabled
	 *            Is buttons enabled.
	 */
	private void setFileModeEnabled(final boolean enabled) {
		logPath.setEnabled(enabled);
		logPathText.setEnabled(enabled);
		fileName.setEnabled(enabled);
		fileNameText.setEnabled(enabled);
	}

	/**
	 * Changes logging mode buttons.
	 * 
	 * @param mode
	 *            Which mode is used
	 */
	private void setGroupButtons(final String mode) {
		if (mode.equals(Constants.LOGGING_S60)) {
			externalFastButton.setSelection(false);
			s60Button.setSelection(true);
			askButton.setSelection(false);
			setFileModeEnabled(true);
		} else if (mode.equals(Constants.LOGGING_ASK_ALLWAYS)) {
			externalFastButton.setSelection(false);
			s60Button.setSelection(false);
			askButton.setSelection(true);
			setFileModeEnabled(false);
		} else if (mode.equals(Constants.LOGGING_EXT_FAST)) {
			externalFastButton.setSelection(true);
			s60Button.setSelection(false);
			askButton.setSelection(false);
			setFileModeEnabled(false);
		}
		// else {
		// externalButton.setSelection(true);
		// externalFastButton.setSelection(false);
		// s60Button.setSelection(false);
		// askButton.setSelection(false);
		// setFileModeEnabled(false);
		// }
	}

	/**
	 * Update atool.exe version number.
	 * 
	 * @param path
	 *            Atool.exe location
	 */
	private void updateAtoolVersion(final String path) {
		atoolVerLabel.setText(Util.getAtoolVersionNumber(path));
		atoolVerLabel.update();
		checkState();
	}
}