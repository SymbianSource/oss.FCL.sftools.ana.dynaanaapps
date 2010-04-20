/*
* Copyright (c) 2008 Nokia Corporation and/or its subsidiary(-ies). 
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
*/

package com.nokia.s60tools.crashanalyser.ui.preferences;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Group;
import com.nokia.s60tools.crashanalyser.plugin.*;
import com.nokia.s60tools.crashanalyser.resources.HelpContextIDs;
import com.nokia.s60tools.crashanalyser.model.*;
import org.eclipse.swt.events.*;

/**
 * Crash Analyser preferences page 
 *
 */
public class CrashAnalyserPreferences extends PreferencePage implements IWorkbenchPreferencePage, 
																		SelectionListener{
	/**
	 * Preference page ID for opening page directly
	 */
	public static final String PAGE_ID = "com.nokia.s60tools.crashanalyser.ui.preferences.CrashAnalyserPreferences"; //$NON-NLS-1$

	private Button buttonListenTraceViewer;
	private Button buttonShowVisualizer;
	private Button buttonEpocwind;
	
	/**
	 * Constructor
	 */
	public CrashAnalyserPreferences() {
		super();
	}
	
	public void init(IWorkbench arg0) {
		// No implementation needed
	}

	@Override
	protected Control createContents(Composite parent) {
		Composite container = new Composite(parent, SWT.NULL);
		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 1;
		container.setLayout(gridLayout);

		Group emulatorGroup = new Group(container, SWT.NONE);
		emulatorGroup.setLayout(gridLayout);
		emulatorGroup.setText("WINSCW Emulator Support");
		emulatorGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		buttonEpocwind = new Button(emulatorGroup, SWT.CHECK);
		buttonEpocwind.setToolTipText("Automatically pick-up panics from epocwind.out while running the Emulator");
		buttonEpocwind.setText("Listen panics from epocwind.out");
		
		Group hardwareGroup = new Group(container, SWT.NONE);
		hardwareGroup.setLayout(gridLayout);		
		hardwareGroup.setText("Target Hardware Support");
		hardwareGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		buttonListenTraceViewer = new Button(hardwareGroup, SWT.CHECK);
		buttonListenTraceViewer.setToolTipText("Listens for MobileCrash files while tracing e.g. with Profiler Activator or Trace Viewer.");
		buttonListenTraceViewer.setText("Listen MobileCrash panics via TraceViewer");
		buttonListenTraceViewer.addSelectionListener(this);
		
		buttonShowVisualizer = new Button(hardwareGroup, SWT.CHECK);
		buttonShowVisualizer.setToolTipText("When a MobileCrash panic is read via TraceViewer, automatically show panic data");
		buttonShowVisualizer.setText("Show Crash Visualiser when crash occurs");
		GridData gd = new GridData();
		gd.horizontalIndent = 20;
		buttonShowVisualizer.setLayoutData(gd);
		
		if (!TraceListener.traceProviderAvailable())
			hardwareGroup.setVisible(false);
		
		getPrefsStoreValues();

		setHelps();
		
		return container;
	}

	private void getPrefsStoreValues(){
		IPreferenceStore store = CrashAnalyserPlugin.getCrashAnalyserPrefsStore();
		boolean listener = store.getBoolean(CrashAnalyserPreferenceConstants.TRACE_LISTENER);
		buttonListenTraceViewer.setSelection(listener);
		
		boolean epocwind = store.getBoolean(CrashAnalyserPreferenceConstants.EPOCWIND_LISTENER);
		buttonEpocwind.setSelection(epocwind);
		
		boolean showVisualizer = store.getBoolean(CrashAnalyserPreferenceConstants.SHOW_VISUALIZER);
		buttonShowVisualizer.setSelection(showVisualizer);
		buttonShowVisualizer.setEnabled(listener);
	}
	
	@Override
	protected void performDefaults() {
		buttonListenTraceViewer.setSelection(true);
		buttonShowVisualizer.setSelection(true);
		buttonShowVisualizer.setEnabled(true);
		buttonEpocwind.setSelection(false);
		super.performDefaults();
	}
	
	@Override
	public boolean performOk() {
		IPreferenceStore store = CrashAnalyserPlugin.getCrashAnalyserPrefsStore();
	
		store.setValue(CrashAnalyserPreferenceConstants.TRACE_LISTENER, buttonListenTraceViewer.getSelection());
		store.setValue(CrashAnalyserPreferenceConstants.EPOCWIND_LISTENER, buttonEpocwind.getSelection());
		store.setValue(CrashAnalyserPreferenceConstants.SHOW_VISUALIZER, buttonShowVisualizer.getSelection());
		
		CrashAnalyserPlugin.startEmulatorListener();
		CrashAnalyserPlugin.startTraceListener();
		
		return super.performOk();
	}
	
	public void widgetDefaultSelected(SelectionEvent arg0) {
		// No implementation needed
	}

	public void widgetSelected(SelectionEvent event) {
		if (buttonListenTraceViewer.getSelection()) {
			buttonShowVisualizer.setEnabled(true);
		} else {
			buttonShowVisualizer.setEnabled(false);
			buttonShowVisualizer.setSelection(false);
		}
	}	
	
	public static boolean traceListenerOn() {
		IPreferenceStore store = CrashAnalyserPlugin.getCrashAnalyserPrefsStore();
		return store.getBoolean(CrashAnalyserPreferenceConstants.TRACE_LISTENER);
	}
	
	public static boolean epocwindListenerOn() {
		IPreferenceStore store = CrashAnalyserPlugin.getCrashAnalyserPrefsStore();
		return store.getBoolean(CrashAnalyserPreferenceConstants.EPOCWIND_LISTENER);
	}
	
	public static boolean showVisualizer() {
		IPreferenceStore store = CrashAnalyserPlugin.getCrashAnalyserPrefsStore();
		return store.getBoolean(CrashAnalyserPreferenceConstants.SHOW_VISUALIZER);
	}
	
	/**
	 * Sets this page's context sensitive helps
	 *
	 */
	protected void setHelps() {
		PlatformUI.getWorkbench().getHelpSystem().setHelp(buttonListenTraceViewer,
				HelpContextIDs.CRASH_ANALYSER_HELP_PREFERENCES);
		PlatformUI.getWorkbench().getHelpSystem().setHelp(buttonEpocwind,
				HelpContextIDs.CRASH_ANALYSER_HELP_PREFERENCES);
		PlatformUI.getWorkbench().getHelpSystem().setHelp(buttonShowVisualizer,
				HelpContextIDs.CRASH_ANALYSER_HELP_PREFERENCES);
	}	
	
}
