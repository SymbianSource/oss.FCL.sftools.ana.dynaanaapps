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
 * Variable Tracing History Dialog
 *
 */
package com.nokia.traceviewer.dialog;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.PlatformUI;

import com.nokia.traceviewer.TraceViewerHelpContextIDs;
import com.nokia.traceviewer.action.OpenTraceLocationAction;
import com.nokia.traceviewer.engine.TraceMetaData;
import com.nokia.traceviewer.engine.TraceViewerGlobals;
import com.nokia.traceviewer.engine.dataprocessor.VariableTracingEvent;
import com.nokia.traceviewer.engine.dataprocessor.VariableTracingItem;

/**
 * Variable Tracing History Dialog
 * 
 */
public final class VariableTracingHistoryDialog extends BaseDialog {

	/**
	 * Main UI Composite
	 */
	private Composite mainComposite;

	/**
	 * Top UI Composite
	 */
	private Composite topComposite;

	/**
	 * Table containing all the information
	 */
	private Table table;

	/**
	 * Checkbox for activating trace when doubleclicking
	 */
	private Button activateTraceCheckBox;

	/**
	 * Checkbox for activating codeline when doubleclicking
	 */
	private Button activateCodeLineCheckBox;

	/**
	 * Tells whether we will activate trace when doubleclicking line
	 */
	private boolean activateTrace = true;

	/**
	 * Tells whether we will activate codeline when doubleclicking line
	 */
	private boolean activateCodeLine = true;

	/**
	 * Checks if DataReader was paused when we entered dialog -> Don't pause
	 * again and don't unpause in exit
	 */
	private boolean wasPausedWhenEntered = true;

	/**
	 * Constructor
	 * 
	 * @param parent
	 *            parent shell
	 */
	public VariableTracingHistoryDialog(Shell parent) {
		super(parent, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL | SWT.RESIZE);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.dialogs.TrayDialog#close()
	 */
	@Override
	public boolean close() {
		boolean close = super.close();
		if (!wasPausedWhenEntered) {
			// Unpause
			TraceViewerGlobals.getTraceViewer().getView().getActionFactory()
					.getPauseAction().run();
		}
		return close;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.traceviewer.dialog.BaseDialog#createShell()
	 */
	@Override
	protected void createDialogContents() {
		// Pause the datareader if it's not paused already
		wasPausedWhenEntered = TraceViewerGlobals.getTraceViewer()
				.getDataReaderAccess().getMainDataReader().isPaused();
		if (!wasPausedWhenEntered) {
			TraceViewerGlobals.getTraceViewer().getView().getActionFactory()
					.getPauseAction().run();
		}

		// Contents
		GridLayout gridLayout = new GridLayout();
		getShell().setText(
				Messages.getString("VariableTracingHistoryDialog.ShellTitle")); //$NON-NLS-1$
		composite.setLayout(gridLayout);
		getShell().setMinimumSize(new Point(400, 250));

		// Create main composite
		createMainComposite();

		// Set help
		PlatformUI.getWorkbench().getHelpSystem().setHelp(getShell(),
				TraceViewerHelpContextIDs.VARIABLE_TRACING_HISTORY);
	}

	/**
	 * This method initializes mainComposite
	 * 
	 */
	private void createMainComposite() {
		// Main composite
		GridData mainCompositeGridData = new GridData();
		mainCompositeGridData.horizontalAlignment = GridData.FILL;
		mainCompositeGridData.grabExcessHorizontalSpace = true;
		mainCompositeGridData.grabExcessVerticalSpace = true;
		mainCompositeGridData.verticalAlignment = GridData.FILL;
		mainComposite = new Composite(composite, SWT.NONE);
		mainComposite.setLayout(new GridLayout());
		mainComposite.setLayoutData(mainCompositeGridData);

		// Create top composite
		createTopComposite();

		// Create the table
		createTable();

		// Create bottom composite
		createBottomComposite();
	}

	/**
	 * Creates and initializes table
	 */
	private void createTable() {
		// Layout data for table
		GridData tableGridData = new GridData();
		tableGridData.horizontalAlignment = GridData.FILL;
		tableGridData.verticalAlignment = GridData.FILL;
		tableGridData.grabExcessHorizontalSpace = true;
		tableGridData.grabExcessVerticalSpace = true;
		tableGridData.heightHint = 300;

		// Create table
		table = new Table(mainComposite, SWT.BORDER | SWT.FULL_SELECTION);
		table.setHeaderVisible(true);
		table.setLayoutData(tableGridData);
		table.setLinesVisible(true);

		// Add double click support
		table.addListener(SWT.DefaultSelection, new Listener() {
			public void handleEvent(Event e) {
				handleDoubleClick();
			}
		});

		// Create columns
		TableColumn timestampColumn = new TableColumn(table, SWT.NONE);
		timestampColumn.setWidth(110);
		timestampColumn.setText(Messages
				.getString("VariableTracingHistoryDialog.TimestampColumnName")); //$NON-NLS-1$
		TableColumn valueColumn = new TableColumn(table, SWT.NONE);
		valueColumn.setWidth(405);
		valueColumn.setText(Messages
				.getString("VariableTracingHistoryDialog.ValueColumnName")); //$NON-NLS-1$
		TableColumn lineColumn = new TableColumn(table, SWT.NONE);
		lineColumn.setAlignment(SWT.CENTER);
		lineColumn.setWidth(65);
		lineColumn.setText(Messages
				.getString("VariableTracingHistoryDialog.LineNrText")); //$NON-NLS-1$
		TableColumn hasCodeLineColumn = new TableColumn(table, SWT.NONE);
		hasCodeLineColumn.setAlignment(SWT.CENTER);
		hasCodeLineColumn
				.setText(Messages
						.getString("VariableTracingHistoryDialog.HasCodeLineColumnName")); //$NON-NLS-1$
		hasCodeLineColumn.setWidth(80);

		// Insert data
		insertData();
	}

	/**
	 * Inserts data to table
	 */
	private void insertData() {
		// First get indices selected in the table
		int[] indices = TraceViewerGlobals.getTraceViewer().getPropertyView()
				.getSelectedVariableIndices();

		// Get items from VariableTracingProcessor
		List<VariableTracingItem> variableItems = TraceViewerGlobals
				.getTraceViewer().getDataProcessorAccess()
				.getVariableTracingProcessor().getVariableTracingItems();

		// Create a ArrayList of all events from selected items
		List<VariableTracingEvent> events = new ArrayList<VariableTracingEvent>();
		for (int i = 0; i < indices.length; i++) {
			events.addAll(variableItems.get(indices[i]).getEventList());
		}

		// Sort the list by line numbers
		Collections.sort(events, new Comparator<VariableTracingEvent>() {
			public int compare(VariableTracingEvent o1, VariableTracingEvent o2) {
				int val = o1.getLine();
				int val2 = o2.getLine();
				return val > val2 ? 1 : val < val2 ? -1 : 0;
			}
		});

		// Null own timestamp from TimestampParser
		TraceViewerGlobals.getTraceViewer().getDataProcessorAccess()
				.getTimestampParser().nullPreviousOwnTimestamp();

		// Insert items
		for (int j = 0; j < events.size(); j++) {
			TableItem item = new TableItem(table, SWT.NONE);
			VariableTracingEvent event = events.get(j);
			String hasMetadata = Messages
					.getString("VariableTracingHistoryDialog.YesText"); //$NON-NLS-1$

			// Calculate new timestamp
			String timestamp = TraceViewerGlobals.getTraceViewer()
					.getDataProcessorAccess().getTimestampParser()
					.processTimestampFromPlainText(event.getTimestamp(), false);

			// No metaData
			if (event.getTraceInformation() == null
					|| TraceViewerGlobals.getDecodeProvider().getTraceMetaData(
							event.getTraceInformation()) == null
					|| TraceViewerGlobals.getDecodeProvider().getTraceMetaData(
							event.getTraceInformation()).getLineNumber() == 0) {
				hasMetadata = Messages
						.getString("VariableTracingHistoryDialog.NoText"); //$NON-NLS-1$
			}

			item.setText(new String[] { timestamp,
					(j + 1) + ". " + event.getParent().getName() //$NON-NLS-1$
							+ event.getValue(),
					String.valueOf(event.getLine()), hasMetadata });

			// Attach event to the data part of the TableItem for easy get
			item.setData(event);
		}
	}

	/**
	 * Handles double clicks
	 */
	void handleDoubleClick() {
		VariableTracingEvent event = (VariableTracingEvent) table
				.getSelection()[0].getData();

		// Activate trace
		if (activateTrace) {

			// Set the line highlighted
			TraceViewerGlobals.getTraceViewer().getView().highlightLines(
					event.getLine() - 1, 0, false);
		}

		// Activate code line
		if (activateCodeLine) {
			if (event.getTraceInformation() != null) {
				TraceMetaData metaData = TraceViewerGlobals.getDecodeProvider()
						.getTraceMetaData(event.getTraceInformation());

				if (metaData.getLineNumber() != 0) {
					// Get Action used to open trace line
					OpenTraceLocationAction action = (OpenTraceLocationAction) TraceViewerGlobals
							.getTraceViewer().getView().getActionFactory()
							.getOpenTraceLocationAction();
					action.setMetaData(metaData, true);
					action.run();
				}
			}
		}

	}

	/**
	 * This method initializes topComposite
	 * 
	 */
	private void createTopComposite() {
		// Top composite
		GridData topCompositeGridData = new GridData();
		topCompositeGridData.horizontalAlignment = GridData.FILL;
		topCompositeGridData.grabExcessHorizontalSpace = true;
		topCompositeGridData.heightHint = 90;
		topCompositeGridData.verticalSpan = 2;
		topCompositeGridData.verticalAlignment = GridData.CENTER;
		topComposite = new Composite(mainComposite, SWT.NONE);
		topComposite.setLayout(new GridLayout());
		topComposite.setLayoutData(topCompositeGridData);

		// Create settings group
		createSettingsGroup();
	}

	/**
	 * This method initializes bottomComposite
	 * 
	 */
	private void createBottomComposite() {
		// Bottom composite
		GridData bottomCompositeGridData = new GridData();
		bottomCompositeGridData.horizontalAlignment = GridData.END;
		bottomCompositeGridData.verticalAlignment = GridData.CENTER;
		Composite bottomComposite = new Composite(mainComposite, SWT.NONE);
		bottomComposite.setLayout(new GridLayout());
		bottomComposite.setLayoutData(bottomCompositeGridData);
	}

	/**
	 * This method initializes settingsGroup
	 * 
	 */
	private void createSettingsGroup() {
		// Settings group
		GridLayout settingsGroupGridLayout = new GridLayout();
		settingsGroupGridLayout.numColumns = 2;
		settingsGroupGridLayout.horizontalSpacing = 15;
		Group settingsGroup = new Group(topComposite, SWT.NONE);
		settingsGroup.setText(Messages
				.getString("VariableTracingHistoryDialog.SettingsGroupText")); //$NON-NLS-1$
		settingsGroup.setLayout(settingsGroupGridLayout);

		// Information label
		Label informationLabel = new Label(settingsGroup, SWT.NONE);
		informationLabel
				.setText(Messages
						.getString("VariableTracingHistoryDialog.InformationLabelText")); //$NON-NLS-1$

		// Activate Trace checkbox
		activateTraceCheckBox = new Button(settingsGroup, SWT.CHECK);
		activateTraceCheckBox.setText(Messages
				.getString("VariableTracingHistoryDialog.ActivateTraceText")); //$NON-NLS-1$
		activateTraceCheckBox.setSelection(activateTrace);

		// Filler
		new Label(settingsGroup, SWT.NONE);

		// Activate Codeline checkbox
		activateCodeLineCheckBox = new Button(settingsGroup, SWT.CHECK);
		activateCodeLineCheckBox
				.setText(Messages
						.getString("VariableTracingHistoryDialog.ActivateCodelineText")); //$NON-NLS-1$
		activateCodeLineCheckBox.setSelection(true);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.traceviewer.dialog.BaseDialog#createActionListeners()
	 */
	@Override
	public void createActionListeners() {
		// Add listener to ActivateTrace checkbox
		activateTraceCheckBox.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				activateTrace = activateTraceCheckBox.getSelection();
			}
		});

		// Add listener to ActivateCodeLine checkbox
		activateCodeLineCheckBox.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				activateCodeLine = activateCodeLineCheckBox.getSelection();
			}
		});
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.dialogs.Dialog#cancelPressed()
	 */
	@Override
	protected void cancelPressed() {
		table.removeAll();
		super.cancelPressed();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.dialogs.Dialog#okPressed()
	 */
	@Override
	protected void okPressed() {
		table.removeAll();
		super.okPressed();
	}

	/**
	 * Tells is the search dialog open
	 * 
	 * @return true if dialog is open
	 */
	public boolean isOpen() {
		boolean isOpen = false;
		if (getShell() != null && !getShell().isDisposed()) {
			isOpen = true;
		}
		return isOpen;
	}

	/**
	 * Sets focus
	 */
	public void setFocus() {
		getShell().setFocus();
	}
}
