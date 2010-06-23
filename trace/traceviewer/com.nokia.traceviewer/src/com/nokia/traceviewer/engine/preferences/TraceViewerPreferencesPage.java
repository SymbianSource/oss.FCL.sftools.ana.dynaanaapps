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
 * TraceViewer preferences page
 *
 */
package com.nokia.traceviewer.engine.preferences;

import java.util.List;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.PlatformUI;

import com.nokia.traceviewer.TraceViewerHelpContextIDs;
import com.nokia.traceviewer.TraceViewerPlugin;
import com.nokia.traceviewer.engine.TraceProvider;
import com.nokia.traceviewer.engine.TraceViewerGlobals;

/**
 * TraceViewer preferences page
 * 
 */
public final class TraceViewerPreferencesPage extends PreferencePage implements
		IWorkbenchPreferencePage {

	/**
	 * Preference page ID for opening page directly
	 */
	public static final String PAGE_ID = "com.nokia.traceviewer.preferences.TraceViewerPreferences"; //$NON-NLS-1$

	/**
	 * PreferenceStore holding all preferences
	 */
	private final IPreferenceStore store;

	/**
	 * Radio buttons to implify timestamp accuracy
	 */
	private Button[] timestampAccuracyButton = new Button[2];

	/**
	 * Radio buttons to implify how to show undecoded traces
	 */
	private Button[] showUndecodedTracesAsButton = new Button[3];

	/**
	 * Time from previous trace checkbox
	 */
	private Button timeFromPreviousTraceCheckBox;

	/**
	 * Show trace component and group name in trace checkbox
	 */
	private Button showComponentGroupNameCheckBox;

	/**
	 * Show trace class and function name in trace checkbox
	 */
	private Button showClassFunctionNameCheckBox;

	/**
	 * Automatically reload changed Dictionaries checkbox
	 */
	private Button autoReloadDictionariesCheckBox;

	/**
	 * Show BTrace variables checkbox
	 */
	private Button showBTraceVariablesCheckBox;

	/**
	 * Radio buttons to implify data format
	 */
	private Button[] dataFormatButton;

	/**
	 * Constructor
	 */
	public TraceViewerPreferencesPage() {
		super();
		// Set the preference store for the preference page.
		store = TraceViewerPlugin.getDefault().getPreferenceStore();
		setPreferenceStore(store);
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
	 * @see
	 * org.eclipse.jface.preference.PreferencePage#createContents(org.eclipse
	 * .swt.widgets.Composite)
	 */
	@Override
	protected Control createContents(Composite parent) {

		// Set help
		PlatformUI.getWorkbench().getHelpSystem().setHelp(parent,
				TraceViewerHelpContextIDs.GENERAL_PREFERENCES);

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

		Label generalTextLabel = new Label(top, SWT.NONE);
		String generalTextLabelText = Messages
				.getString("TraceViewerPreferencesPage.GeneralSettingsText"); //$NON-NLS-1$
		generalTextLabel.setText(generalTextLabelText);

		// General group
		Group generalGroup = new Group(top, SWT.NONE);
		String generalGroupText = Messages
				.getString("TraceViewerPreferencesPage.GeneralGroupText"); //$NON-NLS-1$
		generalGroup.setText(generalGroupText);
		GridData generalGroupGridData = new GridData(SWT.FILL, SWT.FILL, true,
				false);
		generalGroup.setLayoutData(generalGroupGridData);
		generalGroup.setLayout(new GridLayout());

		// Show milliseconds from previous trace checkbox
		timeFromPreviousTraceCheckBox = new Button(generalGroup, SWT.CHECK);
		timeFromPreviousTraceCheckBox.setText(Messages
				.getString("TraceViewerPreferencesPage.ShowPreviousTimeText")); //$NON-NLS-1$
		timeFromPreviousTraceCheckBox
				.setToolTipText(Messages
						.getString("TraceViewerPreferencesPage.TimeFromPreviousToolTip")); //$NON-NLS-1$
		timeFromPreviousTraceCheckBox
				.setSelection(store
						.getBoolean(PreferenceConstants.TIME_FROM_PREVIOUS_TRACE_CHECKBOX));

		// Show trace component and group before trace text checkbox
		showComponentGroupNameCheckBox = new Button(generalGroup, SWT.CHECK);
		showComponentGroupNameCheckBox
				.setText(Messages
						.getString("TraceViewerPreferencesPage.ShowComponentGroupName")); //$NON-NLS-1$
		String showComponentGroupToolTip = Messages
				.getString("TraceViewerPreferencesPage.ShowComponentAndGroupToolTip"); //$NON-NLS-1$
		showComponentGroupNameCheckBox
				.setToolTipText(showComponentGroupToolTip);
		showComponentGroupNameCheckBox
				.setSelection(store
						.getBoolean(PreferenceConstants.SHOW_COMPONENT_GROUP_NAME_CHECKBOX));

		// Show trace class and function before trace text checkbox
		showClassFunctionNameCheckBox = new Button(generalGroup, SWT.CHECK);
		showClassFunctionNameCheckBox.setText(Messages
				.getString("TraceViewerPreferencesPage.ShowClassFunctionName")); //$NON-NLS-1$
		String showClassFunctionToolTip = Messages
				.getString("TraceViewerPreferencesPage.ShowClassAndFunctionToolTip"); //$NON-NLS-1$
		showClassFunctionNameCheckBox.setToolTipText(showClassFunctionToolTip);
		showClassFunctionNameCheckBox
				.setSelection(store
						.getBoolean(PreferenceConstants.SHOW_CLASS_FUNCTION_NAME_CHECKBOX));

		// Automatically reload changed Dictionaries checkbox
		autoReloadDictionariesCheckBox = new Button(generalGroup, SWT.CHECK);
		String autoReloadText = Messages
				.getString("TraceViewerPreferencesPage.AutoReloadDictionariesText"); //$NON-NLS-1$
		String autoReloadToolTip = Messages
				.getString("TraceViewerPreferencesPage.AutoReloadDictionariesToolTip"); //$NON-NLS-1$
		autoReloadDictionariesCheckBox.setText(autoReloadText);
		autoReloadDictionariesCheckBox.setToolTipText(autoReloadToolTip);
		autoReloadDictionariesCheckBox
				.setSelection(store
						.getBoolean(PreferenceConstants.AUTO_RELOAD_DICTIONARIES_CHECKBOX));

		// Show BTrace variables in OST traces checkbox
		showBTraceVariablesCheckBox = new Button(generalGroup, SWT.CHECK);
		String btraceVariablesText = Messages
				.getString("TraceViewerPreferencesPage.ShowBTraceVariablesText"); //$NON-NLS-1$
		String btraceVariablesToolTip = Messages
				.getString("TraceViewerPreferencesPage.ShowBTraceVariablesToolTip"); //$NON-NLS-1$
		showBTraceVariablesCheckBox.setText(btraceVariablesText);
		showBTraceVariablesCheckBox.setToolTipText(btraceVariablesToolTip);
		showBTraceVariablesCheckBox
				.setSelection(store
						.getBoolean(PreferenceConstants.SHOW_BTRACE_VARIABLES_CHECKBOX));

		// Timestamp accuracy group
		Group timestampAccuracyGroup = new Group(top, SWT.NONE);
		String timestampAccuracyGroupText = Messages
				.getString("TraceViewerPreferencesPage.TimestampAccuracyText"); //$NON-NLS-1$
		timestampAccuracyGroup.setText(timestampAccuracyGroupText);
		GridData timestampAccuracyGroupGridData = new GridData(SWT.FILL,
				SWT.FILL, true, false);
		timestampAccuracyGroup.setLayoutData(timestampAccuracyGroupGridData);
		timestampAccuracyGroup.setLayout(new GridLayout());

		// Timestamp accuracy milliseconds
		timestampAccuracyButton[0] = new Button(timestampAccuracyGroup,
				SWT.RADIO);
		String milliSecondsText = Messages
				.getString("TraceViewerPreferencesPage.MilliSecondsText"); //$NON-NLS-1$
		timestampAccuracyButton[0].setText(milliSecondsText);

		// Timestamp accuracy microseconds
		timestampAccuracyButton[1] = new Button(timestampAccuracyGroup,
				SWT.RADIO);
		String microSecondsText = Messages
				.getString("TraceViewerPreferencesPage.MicroSecondsText"); //$NON-NLS-1$
		timestampAccuracyButton[1].setText(microSecondsText);

		// Get the current accuracy from PreferenceStore
		String accuracy = store
				.getString(PreferenceConstants.TIMESTAMP_ACCURACY);
		if (accuracy.equals(PreferenceConstants.MILLISECOND_ACCURACY)) {
			timestampAccuracyButton[0].setSelection(true);
			timestampAccuracyButton[1].setSelection(false);
		} else {
			timestampAccuracyButton[0].setSelection(false);
			timestampAccuracyButton[1].setSelection(true);
		}

		// Show undecoded traces as group
		Group showUndecodedTracesGroup = new Group(top, SWT.NONE);
		String showUndecodedTracesAs = Messages
				.getString("TraceViewerPreferencesPage.ShowUndecodedTracesAsText"); //$NON-NLS-1$
		showUndecodedTracesGroup.setText(showUndecodedTracesAs);
		GridData showUndecodedTracesAsGroupGridData = new GridData(SWT.FILL,
				SWT.FILL, true, false);
		showUndecodedTracesGroup
				.setLayoutData(showUndecodedTracesAsGroupGridData);
		showUndecodedTracesGroup.setLayout(new GridLayout());

		// Show undecoded traces as "BINARY TRACE"
		showUndecodedTracesAsButton[0] = new Button(showUndecodedTracesGroup,
				SWT.RADIO);
		String asInfoText = Messages
				.getString("TraceViewerPreferencesPage.ShowUndecodedTracesAsInfoText"); //$NON-NLS-1$
		showUndecodedTracesAsButton[0].setText(asInfoText);

		// Show undecoded traces as "HEX"
		showUndecodedTracesAsButton[1] = new Button(showUndecodedTracesGroup,
				SWT.RADIO);
		String asHexText = Messages
				.getString("TraceViewerPreferencesPage.ShowUndecodedTracesAsHexText"); //$NON-NLS-1$
		showUndecodedTracesAsButton[1].setText(asHexText);

		// Show undecoded traces as "ID numbers"
		showUndecodedTracesAsButton[2] = new Button(showUndecodedTracesGroup,
				SWT.RADIO);
		String asTraceIDsText = Messages
				.getString("TraceViewerPreferencesPage.ShowUndecodedTracesAsIDsText"); //$NON-NLS-1$
		showUndecodedTracesAsButton[2].setText(asTraceIDsText);

		// Get the current "show undecoded traces" value from PreferenceStore
		String showUndecoded = store
				.getString(PreferenceConstants.SHOW_UNDECODED_TRACES_TYPE);
		if (showUndecoded.equals(PreferenceConstants.UNDECODED_INFO_TEXT)) {
			showUndecodedTracesAsButton[0].setSelection(true);
			showUndecodedTracesAsButton[1].setSelection(false);
			showUndecodedTracesAsButton[2].setSelection(false);
		} else if (showUndecoded.equals(PreferenceConstants.UNDECODED_HEX)) {
			showUndecodedTracesAsButton[0].setSelection(false);
			showUndecodedTracesAsButton[1].setSelection(true);
			showUndecodedTracesAsButton[2].setSelection(false);
		} else {
			showUndecodedTracesAsButton[0].setSelection(false);
			showUndecodedTracesAsButton[1].setSelection(false);
			showUndecodedTracesAsButton[2].setSelection(true);
		}

		// Data format editor. Show only if there are more than one
		// TraceProviders registered
		List<TraceProvider> dataFormats = TraceViewerGlobals
				.getListOfTraceProviders();
		if (dataFormats.size() > 1) {

			// Data format group
			String dataFormatText = Messages
					.getString("TraceViewerPreferencesPage.DataFormatText"); //$NON-NLS-1$
			Group dataFormatGroup = new Group(top, SWT.NONE);
			dataFormatGroup.setText(dataFormatText);
			GridData dataFormatGroupGridData = new GridData(SWT.FILL, SWT.FILL,
					true, false);
			dataFormatGroup.setLayoutData(dataFormatGroupGridData);
			dataFormatGroup.setLayout(new GridLayout());

			dataFormatButton = new Button[dataFormats.size()];
			String dataFormat = store
					.getString(PreferenceConstants.DATA_FORMAT);

			// Go through data formats
			for (int i = 0; i < dataFormats.size(); i++) {
				dataFormatButton[i] = new Button(dataFormatGroup, SWT.RADIO);
				String dataFormatName = dataFormats.get(i).getName();
				dataFormatButton[i].setText(dataFormatName);
				if (dataFormat.equals(dataFormatName)) {
					dataFormatButton[i].setSelection(true);
				} else {
					dataFormatButton[i].setSelection(false);
				}
			}
		}

		return top;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.preference.PreferencePage#performDefaults()
	 */
	@Override
	protected void performDefaults() {
		if (getControl() != null && !getControl().isDisposed()) {

			// Timestamp accuracy
			timestampAccuracyButton[0].setSelection(true);
			timestampAccuracyButton[1].setSelection(false);

			timeFromPreviousTraceCheckBox.setSelection(true);

			// Show undecoded traces as
			showUndecodedTracesAsButton[0].setSelection(false);
			showUndecodedTracesAsButton[1].setSelection(false);
			showUndecodedTracesAsButton[2].setSelection(true);

			showClassFunctionNameCheckBox.setSelection(true);
			autoReloadDictionariesCheckBox.setSelection(true);
			showBTraceVariablesCheckBox.setSelection(true);
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

			// Save old values before saving
			boolean updateNeeded = false;
			String oldTimestampAccuracy = store
					.getString(PreferenceConstants.TIMESTAMP_ACCURACY);
			boolean oldTimeFromPrevious = store
					.getBoolean(PreferenceConstants.TIME_FROM_PREVIOUS_TRACE_CHECKBOX);
			String oldShowBinaryAsType = store
					.getString(PreferenceConstants.SHOW_UNDECODED_TRACES_TYPE);
			boolean showComponentGroupNamePrevious = store
					.getBoolean(PreferenceConstants.SHOW_COMPONENT_GROUP_NAME_CHECKBOX);
			boolean showClassNamePrevious = store
					.getBoolean(PreferenceConstants.SHOW_CLASS_FUNCTION_NAME_CHECKBOX);
			boolean showThreadId = store
					.getBoolean(PreferenceConstants.SHOW_BTRACE_VARIABLES_CHECKBOX);

			// Store new values. Timestamp accuracy.
			if (timestampAccuracyButton[0].getSelection()) {
				store.setValue(PreferenceConstants.TIMESTAMP_ACCURACY,
						PreferenceConstants.MILLISECOND_ACCURACY);
			} else {
				store.setValue(PreferenceConstants.TIMESTAMP_ACCURACY,
						PreferenceConstants.MICROSECOND_ACCURACY);
			}

			// Show time from previous trace
			store.setValue(
					PreferenceConstants.TIME_FROM_PREVIOUS_TRACE_CHECKBOX,
					timeFromPreviousTraceCheckBox.getSelection());

			// Store "show undecoded traces" value
			if (showUndecodedTracesAsButton[0].getSelection()) {
				store.setValue(PreferenceConstants.SHOW_UNDECODED_TRACES_TYPE,
						PreferenceConstants.UNDECODED_INFO_TEXT);
			} else if (showUndecodedTracesAsButton[1].getSelection()) {
				store.setValue(PreferenceConstants.SHOW_UNDECODED_TRACES_TYPE,
						PreferenceConstants.UNDECODED_HEX);
			} else {
				store.setValue(PreferenceConstants.SHOW_UNDECODED_TRACES_TYPE,
						PreferenceConstants.UNDECODED_ID_AND_DATA);
			}

			// Show component and group name
			store.setValue(
					PreferenceConstants.SHOW_COMPONENT_GROUP_NAME_CHECKBOX,
					showComponentGroupNameCheckBox.getSelection());

			// Show class and function name
			store.setValue(
					PreferenceConstants.SHOW_CLASS_FUNCTION_NAME_CHECKBOX,
					showClassFunctionNameCheckBox.getSelection());

			// Auto reload changed Dictionaries
			store.setValue(
					PreferenceConstants.AUTO_RELOAD_DICTIONARIES_CHECKBOX,
					autoReloadDictionariesCheckBox.getSelection());

			// Show Thread ID
			store.setValue(PreferenceConstants.SHOW_BTRACE_VARIABLES_CHECKBOX,
					showBTraceVariablesCheckBox.getSelection());

			// Data format
			if (dataFormatButton != null) {
				for (int i = 0; i < dataFormatButton.length; i++) {
					if (dataFormatButton[i].getSelection()) {
						store.setValue(PreferenceConstants.DATA_FORMAT,
								dataFormatButton[i].getText());
					}
				}
			}

			// Check if new values are different from old values and set
			// updateNeeded boolean to true if needed
			if (!store.getString(PreferenceConstants.TIMESTAMP_ACCURACY)
					.equals(oldTimestampAccuracy)) {
				updateNeeded = true;
			} else if (store
					.getBoolean(PreferenceConstants.TIME_FROM_PREVIOUS_TRACE_CHECKBOX) != oldTimeFromPrevious) {
				updateNeeded = true;
			} else if (!store.getString(
					PreferenceConstants.SHOW_UNDECODED_TRACES_TYPE).equals(
					oldShowBinaryAsType)) {
				updateNeeded = true;
			} else if (store
					.getBoolean(PreferenceConstants.SHOW_COMPONENT_GROUP_NAME_CHECKBOX) != showComponentGroupNamePrevious) {
				updateNeeded = true;
			} else if (store
					.getBoolean(PreferenceConstants.SHOW_CLASS_FUNCTION_NAME_CHECKBOX) != showClassNamePrevious) {
				updateNeeded = true;
			} else if (store
					.getBoolean(PreferenceConstants.SHOW_BTRACE_VARIABLES_CHECKBOX) != showThreadId) {
				updateNeeded = true;
			}

			// Update properties
			updateProperties(updateNeeded);
		}
	}

	/**
	 * Updates properties to TraceViewer engine
	 * 
	 * @param updateNeeded
	 *            if true, update to engine is needed
	 */
	private void updateProperties(boolean updateNeeded) {
		// Change the values and refresh the TraceViewer view if update is
		// needed
		if (updateNeeded) {
			// Set timestamp accuracy to timestampParser
			boolean millisec = false;
			if (store.getString(PreferenceConstants.TIMESTAMP_ACCURACY).equals(
					PreferenceConstants.MILLISECOND_ACCURACY)) {
				millisec = true;
			}
			TraceViewerGlobals.getTraceViewer().getDataProcessorAccess()
					.getTimestampParser().setTimestampAccuracyMilliSecs(
							millisec);

			// Set showing time from previous trace to timestampParser
			TraceViewerGlobals.getTraceViewer().getDataProcessorAccess()
					.getTimestampParser().setShowTimeFromPrevious(
							timeFromPreviousTraceCheckBox.getSelection());

			// Set show class and function name before trace text
			if (TraceViewerGlobals.getDecodeProvider() != null) {
				TraceViewerGlobals.getDecodeProvider().setAddPrefixesToTrace(
						showClassFunctionNameCheckBox.getSelection(),
						showComponentGroupNameCheckBox.getSelection());
			}

			// Set show BTrace variables to Decoder
			TraceViewerGlobals.getTraceViewer().getDataProcessorAccess()
					.getDecoder().setShowBTraceVariables(
							showBTraceVariablesCheckBox.getSelection());

			// Update the TraceViewer view
			TraceViewerGlobals.getTraceViewer().getView().refreshCurrentView();

		}

		// Set new TraceProvider. Find it from the list of TraceProviders using
		// the selected name. TraceViewer handles the change. If provider is
		// the same as previously, nothing is done.
		List<TraceProvider> dataFormats = TraceViewerGlobals
				.getListOfTraceProviders();
		if (dataFormatButton != null) {
			for (int i = 0; i < dataFormats.size(); i++) {

				// Compare the name of the Provider to the name saved to the
				// preference store
				if (dataFormats.get(i).getName().equals(
						store.getString(PreferenceConstants.DATA_FORMAT))) {

					// Set new TraceProvider to the TraceViewer
					TraceViewerGlobals.setTraceProvider(dataFormats.get(i),
							false);
					break;
				}
			}
		}
	}
}