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
 * Filter dialog
 *
 */
package com.nokia.traceviewer.dialog;

import java.net.URL;

import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.ToolItem;

import com.nokia.traceviewer.TraceViewerPlugin;
import com.nokia.traceviewer.dialog.FilterAdvancedDialog.ExitStatus;
import com.nokia.traceviewer.dialog.treeitem.FilterTreeBaseItem;
import com.nokia.traceviewer.dialog.treeitem.FilterTreeItem;
import com.nokia.traceviewer.dialog.treeitem.TreeItem;
import com.nokia.traceviewer.dialog.treeitem.TreeItemContentProvider;
import com.nokia.traceviewer.engine.DataReaderAccess;
import com.nokia.traceviewer.engine.TraceViewerGlobals;
import com.nokia.traceviewer.engine.TraceViewerDialogInterface.Dialog;
import com.nokia.traceviewer.engine.dataprocessor.FilterRuleObject;
import com.nokia.traceviewer.engine.dataprocessor.FilterRuleSet;
import com.nokia.traceviewer.engine.dataprocessor.FilterRuleSet.LogicalOperator;
import com.nokia.traceviewer.engine.preferences.PreferenceConstants;
import com.nokia.traceviewer.engine.preferences.XMLFilterConfigurationExporter;

/**
 * Filter dialog
 * 
 */
public final class FilterDialog extends BaseTreeDialog {

	/**
	 * Processing reason to give to progressBar dialog when filtering
	 */
	private static final String filterProcessReason = Messages
			.getString("FilterDialog.ProcessReason"); //$NON-NLS-1$

	/**
	 * Processing reason to give to progressBar dialog when clearing filters
	 */
	private static final String clearProcessReason = Messages
			.getString("FilterDialog.ClearProcessReason"); //$NON-NLS-1$

	/**
	 * Add item image
	 */
	private static final String itemAddImageLocation = "/icons/filteradd.gif"; //$NON-NLS-1$

	/**
	 * Edit item image
	 */
	private static final String itemEditImageLocation = "/icons/filteredit.gif"; //$NON-NLS-1$

	/**
	 * Remove item image
	 */
	private static final String itemRemoveImageLocation = "/icons/filterremove.gif"; //$NON-NLS-1$

	/**
	 * Advanced item image
	 */
	private static final String advancedImageLocation = "/icons/advancedfilter.gif"; //$NON-NLS-1$

	/**
	 * Name of the dialog
	 */
	private static final String dialogName = Messages
			.getString("FilterDialog.DialogName"); //$NON-NLS-1$

	/**
	 * Indicates that applying the rules caused search dialog to be closed ->
	 * reopen after filter
	 */
	private boolean closedSearchDialog;

	/**
	 * Advanced toolItem to switch to advanced view
	 */
	private ToolItem advancedItem;

	/**
	 * Advanced toolBar image
	 */
	private Image advancedImage;

	/**
	 * Radio buttons to implify if we are showing or hiding traces
	 */
	private Button[] showHideButton = new Button[2];

	/**
	 * Radio buttons to implify if we are ORring or ANDing
	 */
	private Button[] orAndButton = new Button[2];

	/**
	 * Advanced dialog
	 */
	private FilterAdvancedDialog advancedDialog;

	/**
	 * Constructor
	 * 
	 * @param parent
	 *            parent shell
	 * @param contentProvider
	 *            contentprovider for the tree
	 * @param treeRoot
	 *            tree root
	 */
	public FilterDialog(Shell parent, TreeItemContentProvider contentProvider,
			TreeItem treeRoot) {
		super(parent, contentProvider, treeRoot);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.traceviewer.dialog.BaseTreeDialog#export()
	 */
	@Override
	public void export() {

		// Export rules to XML file
		XMLFilterConfigurationExporter exporter;

		// Default configuration file
		if (!TraceViewerPlugin.getDefault().getPreferenceStore().getString(
				PreferenceConstants.CONFIGURATION_FILE).equals(
				PreferenceConstants.DEFAULT_CONFIGURATION_FILE)) {
			exporter = new XMLFilterConfigurationExporter(root,
					TraceViewerPlugin.getDefault().getPreferenceStore()
							.getString(PreferenceConstants.CONFIGURATION_FILE),
					false);
		} else {
			exporter = new XMLFilterConfigurationExporter(root,
					PreferenceConstants.DEFAULT_CONFIGURATION_FILE, true);
		}

		exporter.export();

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.traceviewer.dialog.BaseTreeDialog#saveSettings()
	 */
	@Override
	protected void saveSettings() {
		super.saveSettings();

		FilterRuleSet ruleSet = TraceViewerGlobals.getTraceViewer()
				.getDataProcessorAccess().getFilterProcessor().getFilterRules();

		// Save filters if something changed
		if (somethingChanged) {
			Object[] arr = viewer.getCheckedElements();
			ruleSet.getFilterRules().clear();

			LogicalOperator op;
			if (orAndButton[0].getSelection()) {
				op = LogicalOperator.OR;
			} else {
				op = LogicalOperator.AND;
			}
			FilterRuleSet set = new FilterRuleSet();
			set.setOperator(op);

			for (int i = 0; i < arr.length; i++) {
				// Text rule, add to the end of the list
				if (((FilterTreeItem) arr[i]).getRule() == FilterTreeItem.Rule.TEXT_RULE) {
					set.addObject((FilterRuleObject) arr[i]);
					// Component rule, add to the beginning of the list
				} else if (((FilterTreeItem) arr[i]).getRule() == FilterTreeItem.Rule.COMPONENT_RULE) {
					set.addObject(0, (FilterRuleObject) arr[i]);
				}
			}
			if (set.getFilterRules().size() > 0) {
				ruleSet.addObject(set);
			}

			// Set show / hide and logical operator to processor
			TraceViewerGlobals.getTraceViewer().getDataProcessorAccess()
					.getFilterProcessor().setShowTracesContainingRule(
							showHideButton[0].getSelection());
			TraceViewerGlobals.getTraceViewer().getDataProcessorAccess()
					.getFilterProcessor().setLogicalOrInUse(
							orAndButton[0].getSelection());
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.traceviewer.dialog.BaseTreeDialog#restoreSettings()
	 */
	@Override
	protected void restoreSettings() {
		super.restoreSettings();

		// Get rule array
		FilterRuleSet ruleSet = TraceViewerGlobals.getTraceViewer()
				.getDataProcessorAccess().getFilterProcessor().getFilterRules();

		// Check that the format of the rule sets is correct
		if (!ruleSet.getFilterRules().isEmpty()
				&& ruleSet.getFilterRules().get(0) instanceof FilterRuleSet) {
			FilterRuleSet set = (FilterRuleSet) ruleSet.getFilterRules().get(0);

			// Tree has to be re-checked
			for (int i = 0; i < set.getFilterRules().size(); i++) {
				FilterRuleObject obj = set.getFilterRules().get(i);

				// Check instance type
				if (obj instanceof FilterTreeItem) {
					FilterTreeItem item = (FilterTreeItem) obj;
					viewer.setChecked(item, true);
					checkboxStateListener.checkStateChanged(item);
				}
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.traceviewer.dialog.BaseTreeDialog#createDialogContents()
	 */
	@Override
	protected void createDialogContents() {
		super.doInitialSetup();

		// Shell & composite
		GridLayout shellGridLayout = new GridLayout();
		shellGridLayout.numColumns = 3;
		shellGridLayout.horizontalSpacing = 3;
		getShell().setText(dialogName);
		root.setName(dialogName);
		composite.setLayout(shellGridLayout);
		getShell().setMinimumSize(new Point(300, 450));
		createToolBar();
		super.createTree();

		// Filter settings group
		GridLayout filterSettingsGroupGridLayout = new GridLayout();
		filterSettingsGroupGridLayout.numColumns = 4;
		Group filterSettingsGroup = new Group(composite, SWT.NONE);
		filterSettingsGroup.setText(Messages
				.getString("FilterDialog.SettingsGroupName")); //$NON-NLS-1$
		filterSettingsGroup.setLayout(filterSettingsGroupGridLayout);
		GridData filterSettingsGroupGridData = new GridData();
		filterSettingsGroupGridData.horizontalSpan = 3;
		filterSettingsGroupGridData.horizontalAlignment = SWT.FILL;
		filterSettingsGroup.setLayoutData(filterSettingsGroupGridData);

		// Show hide Button
		GridLayout comp1GridLayout = new GridLayout();
		comp1GridLayout.numColumns = 2;
		comp1GridLayout.verticalSpacing = 0;
		comp1GridLayout.marginHeight = 0;
		Composite comp1 = new Composite(filterSettingsGroup, SWT.NONE);
		comp1.setLayout(comp1GridLayout);
		showHideButton[0] = new Button(comp1, SWT.RADIO);
		showHideButton[0].setText(Messages
				.getString("FilterDialog.ShowButtonText")); //$NON-NLS-1$
		showHideButton[0].setToolTipText(Messages
				.getString("FilterDialog.ShowButtonToolTip")); //$NON-NLS-1$
		showHideButton[1] = new Button(comp1, SWT.RADIO);
		showHideButton[1].setText(Messages
				.getString("FilterDialog.HideButtonText")); //$NON-NLS-1$
		showHideButton[1].setToolTipText(Messages
				.getString("FilterDialog.HideButtonToolTip")); //$NON-NLS-1$
		boolean show = TraceViewerGlobals.getTraceViewer()
				.getDataProcessorAccess().getFilterProcessor()
				.isShowTracesContainingRule();
		showHideButton[0].setSelection(show);
		showHideButton[1].setSelection(!show);

		// Spacer label
		GridData spacerGridData = new GridData();
		spacerGridData.grabExcessHorizontalSpace = true;
		spacerGridData.horizontalAlignment = SWT.FILL;
		Label spacer = new Label(filterSettingsGroup, SWT.NONE);
		spacer.setLayoutData(spacerGridData);

		// Or And Button
		GridLayout comp2GridLayout = new GridLayout();
		comp2GridLayout.numColumns = 2;
		comp2GridLayout.verticalSpacing = 0;
		comp2GridLayout.marginHeight = 0;
		Composite comp2 = new Composite(filterSettingsGroup, SWT.NONE);
		comp2.setLayout(comp2GridLayout);
		orAndButton[0] = new Button(comp2, SWT.RADIO);
		orAndButton[0].setText(Messages.getString("FilterDialog.OrButtonText")); //$NON-NLS-1$
		orAndButton[0].setToolTipText(Messages
				.getString("FilterDialog.ORRuleToolTip")); //$NON-NLS-1$
		orAndButton[0].setSelection(true);
		orAndButton[1] = new Button(comp2, SWT.RADIO);
		orAndButton[1]
				.setText(Messages.getString("FilterDialog.AndButtonText")); //$NON-NLS-1$
		orAndButton[1].setToolTipText(Messages
				.getString("FilterDialog.ANDRuleToolTip")); //$NON-NLS-1$
		boolean logicalOr = TraceViewerGlobals.getTraceViewer()
				.getDataProcessorAccess().getFilterProcessor()
				.isLogicalOrInUse();
		orAndButton[0].setSelection(logicalOr);
		orAndButton[1].setSelection(!logicalOr);

		// Spacer label 2
		GridData spacer2GridData = new GridData();
		spacer2GridData.grabExcessHorizontalSpace = true;
		spacer2GridData.horizontalAlignment = SWT.FILL;
		Label spacer2 = new Label(composite, SWT.NONE);
		spacer2.setLayoutData(spacer2GridData);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.traceviewer.dialog.BaseTreeDialog#createToolBar()
	 */
	@Override
	public void createToolBar() {
		super.createToolBar(itemAddImageLocation, itemEditImageLocation,
				itemRemoveImageLocation);

		// Add advanded filter ToolBar Item
		advancedItem = new ToolItem(toolBar, SWT.PUSH);
		advancedItem.setText(Messages
				.getString("FilterDialog.AdvancedItemText")); //$NON-NLS-1$
		advancedItem.setToolTipText(Messages
				.getString("FilterDialog.AdvancedViewToolTip")); //$NON-NLS-1$
		URL url = TraceViewerPlugin.getDefault().getBundle().getEntry(
				advancedImageLocation);
		advancedImage = ImageDescriptor.createFromURL(url).createImage();
		advancedItem.setImage(advancedImage);
	}

	/**
	 * Opens the search dialog
	 */
	public void openSearchDialog() {
		TraceViewerGlobals.getTraceViewer().getDataProcessorAccess()
				.getSearchProcessor().getSearchDialog().openDialog();
	}

	/**
	 * Closes the search dialog
	 */
	private void closeSearchDialog() {
		// If search dialog is open, close it because data is going to
		// change
		if (TraceViewerGlobals.getTraceViewer().getDataProcessorAccess()
				.getSearchProcessor().disposeSearchDialog()) {
			closedSearchDialog = true;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.traceviewer.dialog.BaseTreeDialog#processAddGroupAction()
	 */
	@Override
	protected void processAddGroupAction() {
		String name = Messages.getString("FilterDialog.NewGroupText"); //$NON-NLS-1$
		InputDialog dialog = new InputDialog(getShell(), name, Messages
				.getString("FilterDialog.NewGroupDialogInfo"), name, null); //$NON-NLS-1$
		int ret = dialog.open();
		if (ret == Window.OK) {
			name = dialog.getValue();
			// Get parent node

			Object selection = getSelectedGroup();
			FilterTreeItem item = new FilterTreeBaseItem(contentProvider,
					selection, name, FilterTreeItem.Rule.GROUP);

			((FilterTreeItem) selection).addChild(item);
			viewer.expandToLevel(item, 0);

			somethingChanged = true;
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.nokia.traceviewer.dialog.BaseTreeDialog#processApplyButtonAction()
	 */
	@Override
	protected void processApplyButtonAction() {
		// If advanced dialog exists, set its rules as not applied
		if (advancedDialog != null) {
			advancedDialog.exitStatus = ExitStatus.NORMAL;
		}

		// Remove advanced filter string from processor
		TraceViewerGlobals.getTraceViewer().getDataProcessorAccess()
				.getFilterProcessor().setAdvancedFilterString(""); //$NON-NLS-1$

		closedSearchDialog = false;
		saveSettings();

		int maxLines = TraceViewerGlobals.getTraceViewer()
				.getDataReaderAccess().getMainDataReader().getTraceCount();

		// Create new file from filters
		if (TraceViewerGlobals.getTraceViewer().getDataProcessorAccess()
				.getFilterProcessor().hasRules()
				&& somethingChanged) {
			applyFilters(maxLines);

			// All filters removed
		} else if (somethingChanged
				&& TraceViewerGlobals.getTraceViewer().getDataReaderAccess()
						.getCurrentDataReader() != TraceViewerGlobals
						.getTraceViewer().getDataReaderAccess()
						.getMainDataReader()) {
			removeFilters(maxLines);

		}

		// Open search dialog if it was closed
		if (closedSearchDialog) {
			openSearchDialog();
		}

		// Insert this to NOT run saveSettings again
		somethingChanged = false;
	}

	/**
	 * Remove filters
	 * 
	 * @param maxLines
	 *            number of traces
	 */
	public void removeFilters(int maxLines) {
		// If still using external filter or has other rules, apply filters
		// instead of removing them
		if (TraceViewerGlobals.getTraceViewer().getDataProcessorAccess()
				.getFilterProcessor().isFiltering()) {
			applyFilters(maxLines);
		} else {
			// Close search dialog if it's necessary
			closeSearchDialog();

			// Empty the tree view of checked items
			if (viewer != null && !viewer.getTree().isDisposed()) {
				viewer.setCheckedElements(new Object[0]);
			}

			DataReaderAccess access = TraceViewerGlobals.getTraceViewer()
					.getDataReaderAccess();

			// If filter data reader exists, close the data reader
			if (access.getCurrentDataReader() != access.getMainDataReader()) {

				access.getCurrentDataReader().shutdown();
			}

			// Delete scroll reader and set main data reader as current data
			// reader
			access.deleteScrollReader();
			access.setCurrentDataReader(access.getMainDataReader());

			// Start reading data from the beginning
			TraceViewerGlobals.getTraceViewer().readDataFileFromBeginning();
			TraceViewerGlobals.getTraceViewer().getView().updateViewName();

			// Hide dialog and open progressbar only if there is at least one
			// trace
			if (maxLines > 0) {
				Shell shell = getShell();
				if (shell != null && !shell.isDisposed()) {
					shell.setVisible(false);
				}
				progressBarDialog = (ProgressBarDialog) TraceViewerGlobals
						.getTraceViewer().getDialogs().createDialog(
								Dialog.PROGRESSBAR);
				progressBarDialog.open(maxLines, clearProcessReason);
			}
		}
	}

	/**
	 * Apply filters
	 * 
	 * @param maxLines
	 *            number of traces
	 */
	public void applyFilters(int maxLines) {
		// Close search dialog if it's necessary
		closeSearchDialog();

		TraceViewerGlobals.getTraceViewer().getDataProcessorAccess()
				.getFilterProcessor().createFilteredFile();

		TraceViewerGlobals.getTraceViewer().getView().updateViewName();

		// Hide dialog and open progressbar only if there is at least one trace
		if (maxLines > 0) {
			Shell shell = getShell();
			if (shell != null && !shell.isDisposed()) {
				shell.setVisible(false);
			}
			progressBarDialog = (ProgressBarDialog) TraceViewerGlobals
					.getTraceViewer().getDialogs().createDialog(
							Dialog.PROGRESSBAR);

			// Open progressbar
			progressBarDialog.open(maxLines, filterProcessReason);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.traceviewer.dialog.BaseDialog#createActionListeners()
	 */
	@Override
	public void createActionListeners() {
		super.createActionListeners();

		// Add selection listener to advanced button
		advancedItem.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				TraceViewerGlobals.postUiEvent("AdvancedButton", "1"); //$NON-NLS-1$ //$NON-NLS-2$
				if (getShell() != null) {
					getShell().close();
				}
				changeToAdvancedView();
				TraceViewerGlobals.postUiEvent("AdvancedButton", "0"); //$NON-NLS-1$ //$NON-NLS-2$
			}
		});

		// Add selection listener to OR radio button
		orAndButton[0].addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				TraceViewerGlobals.postUiEvent("OrRadioButton", "1"); //$NON-NLS-1$ //$NON-NLS-2$
				somethingChanged = true;
				TraceViewerGlobals.postUiEvent("OrRadioButton", "0"); //$NON-NLS-1$ //$NON-NLS-2$
			}
		});

		// Add selection listener to AND radio button
		orAndButton[1].addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				TraceViewerGlobals.postUiEvent("AndRadioButton", "1"); //$NON-NLS-1$ //$NON-NLS-2$
				somethingChanged = true;
				TraceViewerGlobals.postUiEvent("AndRadioButton", "0"); //$NON-NLS-1$ //$NON-NLS-2$
			}
		});

		// Add selection listener to SHOW radio button
		showHideButton[0].addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				TraceViewerGlobals.postUiEvent("ShowRadioButton", "1"); //$NON-NLS-1$ //$NON-NLS-2$
				somethingChanged = true;
				TraceViewerGlobals.postUiEvent("ShowRadioButton", "0"); //$NON-NLS-1$ //$NON-NLS-2$
			}
		});

		// Add selection listener to HIDE radio button
		showHideButton[1].addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				TraceViewerGlobals.postUiEvent("HideRadioButton", "1"); //$NON-NLS-1$ //$NON-NLS-2$
				somethingChanged = true;
				TraceViewerGlobals.postUiEvent("HideRadioButton", "0"); //$NON-NLS-1$ //$NON-NLS-2$
			}
		});
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.traceviewer.dialog.BaseTreeDialog#dispose()
	 */
	@Override
	protected void dispose() {
		super.dispose();
		if (advancedImage != null) {
			advancedImage.dispose();
		}
	}

	/**
	 * Gets advanced dialog
	 * 
	 * @return the advancedDialog
	 */
	public FilterAdvancedDialog getAdvancedDialog() {
		if (advancedDialog == null) {
			advancedDialog = (FilterAdvancedDialog) TraceViewerGlobals
					.getTraceViewer().getDialogs().createDialog(
							Dialog.ADVANCEDFILTER);
		}
		return advancedDialog;
	}

	/**
	 * Change to advanced view
	 */
	private void changeToAdvancedView() {

		// Create if doesn't exits
		advancedDialog = getAdvancedDialog();
		advancedDialog.openDialog();

		// Check if apply button was clicked
		if (advancedDialog.exitStatus == ExitStatus.APPLYBUTTON) {
			TraceViewerGlobals.getTraceViewer().getDataProcessorAccess()
					.getFilterProcessor().setShowTracesContainingRule(
							advancedDialog.isShowTracesContainingRule());
			TraceViewerGlobals.getTraceViewer().getDataProcessorAccess()
					.getFilterProcessor().getFilterRules().getFilterRules()
					.clear();

			int maxLines = TraceViewerGlobals.getTraceViewer()
					.getDataReaderAccess().getMainDataReader().getTraceCount();

			// Check if there are rules set in the advanced dialog
			if (advancedDialog.getRuleSet() != null) {
				TraceViewerGlobals.getTraceViewer().getDataProcessorAccess()
						.getFilterProcessor().getFilterRules().addObject(
								advancedDialog.getRuleSet());

				// Apply filters
				applyFilters(maxLines);

				// Remove filters
			} else {
				removeFilters(maxLines);
			}

			// Open search dialog if it was closed
			if (closedSearchDialog) {
				openSearchDialog();
			}

			// Simple button clicked from advanced view
		} else if (advancedDialog.exitStatus == ExitStatus.SIMPLEBUTTON) {
			open();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.traceviewer.dialog.BaseDialog#open()
	 */
	@Override
	public int open() {
		int ret;

		boolean openAdvanced = false;

		// Check if filter dialog was closed from advanced view
		if (advancedDialog != null) {
			if (advancedDialog.exitStatus == ExitStatus.APPLYBUTTON
					|| advancedDialog.exitStatus == ExitStatus.CANCELBUTTON) {
				openAdvanced = true;
			}
		}

		// Open normal view
		if (!openAdvanced) {
			ret = super.open();

			// Open advanced view
		} else {
			ret = CANCEL;
			changeToAdvancedView();
		}

		return ret;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.nokia.traceviewer.dialog.BaseTreeDialog#getPropertyDialog(java.lang
	 * .Object)
	 */
	@Override
	protected BasePropertyDialog getPropertyDialog(Object selection,
			boolean editOldItem) {
		FilterTreeItem oldItem = null;
		if (editOldItem) {
			oldItem = (FilterTreeItem) selection;
		}
		FilterPropertyDialog dialog = new FilterPropertyDialog(getShell(),
				oldItem, contentProvider, selection, viewer);
		return dialog;
	}

}
