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
 * Variable tracing Dialog class
 *
 */
package com.nokia.traceviewer.dialog;

import java.util.List;

import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Shell;

import com.nokia.traceviewer.TraceViewerPlugin;
import com.nokia.traceviewer.action.TraceViewerActionUtils;
import com.nokia.traceviewer.dialog.treeitem.TreeItem;
import com.nokia.traceviewer.dialog.treeitem.TreeItemContentProvider;
import com.nokia.traceviewer.dialog.treeitem.VariableTracingTreeBaseItem;
import com.nokia.traceviewer.dialog.treeitem.VariableTracingTreeItem;
import com.nokia.traceviewer.dialog.treeitem.VariableTracingTreeTextItem;
import com.nokia.traceviewer.engine.TraceViewerGlobals;
import com.nokia.traceviewer.engine.TraceViewerPropertyViewInterface;
import com.nokia.traceviewer.engine.TraceViewerDialogInterface.Dialog;
import com.nokia.traceviewer.engine.dataprocessor.VariableTracingItem;
import com.nokia.traceviewer.engine.preferences.PreferenceConstants;
import com.nokia.traceviewer.engine.preferences.XMLVariableTracingConfigurationExporter;

/**
 * Variable tracing Dialog class
 */
public final class VariableTracingDialog extends BaseTreeDialog {

	/**
	 * Processing reason to give to progressBar dialog
	 */
	private static final String processReason = Messages
			.getString("VariableTracingDialog.ProcessReason"); //$NON-NLS-1$

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
	 * Dialog name
	 */
	private static final String dialogName = Messages
			.getString("VariableTracingDialog.DialogName"); //$NON-NLS-1$

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
	public VariableTracingDialog(Shell parent,
			TreeItemContentProvider contentProvider, TreeItem treeRoot) {
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
		XMLVariableTracingConfigurationExporter exporter;

		// Default configuration file
		if (!TraceViewerPlugin.getDefault().getPreferenceStore().getString(
				PreferenceConstants.CONFIGURATION_FILE).equals(
				PreferenceConstants.DEFAULT_CONFIGURATION_FILE)) {
			exporter = new XMLVariableTracingConfigurationExporter(root,
					TraceViewerPlugin.getDefault().getPreferenceStore()
							.getString(PreferenceConstants.CONFIGURATION_FILE),
					false);
		} else {
			exporter = new XMLVariableTracingConfigurationExporter(root,
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
	public void saveSettings() {
		// Save the old location
		super.saveSettings();

		// Save VariableTracingItems if something changed
		if (somethingChanged && !viewer.getControl().isDisposed()) {
			Object[] arr = viewer.getCheckedElements();

			// Get arrays and empty them
			List<VariableTracingItem> variableTracingItems = TraceViewerGlobals
					.getTraceViewer().getDataProcessorAccess()
					.getVariableTracingProcessor().getVariableTracingItems();
			List<VariableTracingTreeTextItem> textRules = TraceViewerGlobals
					.getTraceViewer().getDataProcessorAccess()
					.getVariableTracingProcessor().getTextRules();

			variableTracingItems.clear();
			textRules.clear();

			// Create new items
			for (int i = 0; i < arr.length; i++) {
				VariableTracingTreeItem listItem = (VariableTracingTreeItem) arr[i];

				// Text rule
				if (listItem.getRule() == VariableTracingTreeItem.Rule.TEXT_RULE) {
					VariableTracingTreeTextItem listItem2 = (VariableTracingTreeTextItem) listItem;
					textRules.add(listItem2);
					VariableTracingItem item = new VariableTracingItem(
							listItem2.getName(), listItem2.getText(), listItem2
									.isMatchCase(), listItem2.getHistoryCount());
					variableTracingItems.add(item);
				}
			}
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

		// Get text rule array
		List<VariableTracingTreeTextItem> textRules = TraceViewerGlobals
				.getTraceViewer().getDataProcessorAccess()
				.getVariableTracingProcessor().getTextRules();

		// Tree has to be re-checked
		for (int i = 0; i < textRules.size(); i++) {
			VariableTracingTreeItem item = textRules.get(i);
			viewer.setChecked(item, true);
			checkboxStateListener.checkStateChanged(item);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.traceviewer.dialog.BaseDialog#createDialogContents()
	 */
	@Override
	protected void createDialogContents() {
		super.createDialogContents(dialogName);
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
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.traceviewer.dialog.BaseTreeDialog#processAddGroupAction()
	 */
	@Override
	protected void processAddGroupAction() {
		String name = Messages.getString("VariableTracingDialog.NewGroupText"); //$NON-NLS-1$
		InputDialog dialog = new InputDialog(
				getShell(),
				name,
				Messages.getString("VariableTracingDialog.NewGroupDialogInfo"), name, null); //$NON-NLS-1$
		int ret = dialog.open();
		if (ret == Window.OK) {
			name = dialog.getValue();

			// Get parent node
			Object selection = getSelectedGroup();
			VariableTracingTreeItem item = new VariableTracingTreeBaseItem(
					contentProvider, selection, name,
					VariableTracingTreeItem.Rule.GROUP, 0);
			((VariableTracingTreeItem) selection).addChild(item);
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
		saveSettings();

		List<VariableTracingItem> items = TraceViewerGlobals.getTraceViewer()
				.getDataProcessorAccess().getVariableTracingProcessor()
				.getVariableTracingItems();

		// Start variable tracing from beginning
		if (!items.isEmpty() && somethingChanged) {

			TraceViewerPropertyViewInterface propView = TraceViewerGlobals
					.getTraceViewer().getPropertyView();
			if (propView != null && !propView.isDisposed()) {
				propView.clearAll();
				propView.createNewPropertyTableItems();
				// View is not open, empty items from processors
			} else {
				TraceViewerGlobals.getTraceViewer().getDataProcessorAccess()
						.getLineCountProcessor().emptyLineCountItems();

				TraceViewerGlobals.getTraceViewer().getDataProcessorAccess()
						.getVariableTracingProcessor()
						.emptyVariableTracingItems();
			}

			// Set traces not to be shown in the view
			TraceViewerGlobals.getTraceViewer().getDataReaderAccess()
					.getCurrentDataReader().getTraceConfiguration()
					.setShowInView(false);

			int maxLines = TraceViewerGlobals.getTraceViewer()
					.getDataReaderAccess().getCurrentDataReader()
					.getTraceCount();

			TraceViewerGlobals.getTraceViewer().getDataReaderAccess()
					.getCurrentDataReader().setFilePosition(0);

			// Open property view
			TraceViewerActionUtils.openPropertyView();

			// Hide dialog and open progressbar only if there is at least one
			// trace
			if (maxLines > 0) {
				getShell().setVisible(false);
				progressBarDialog = (ProgressBarDialog) TraceViewerGlobals
						.getTraceViewer().getDialogs().createDialog(
								Dialog.PROGRESSBAR);
				progressBarDialog.open(maxLines, processReason);
			}

			// Set traces back to be shown in the view
			TraceViewerGlobals.getTraceViewer().getDataReaderAccess()
					.getCurrentDataReader().getTraceConfiguration()
					.setShowInView(true);

			// Removed all count line rules
		} else if (somethingChanged) {
			TraceViewerPropertyViewInterface view = TraceViewerGlobals
					.getTraceViewer().getPropertyView();

			if (view != null && !view.isDisposed()) {
				view.createNewPropertyTableItems();
			}
		}

		// Insert this to NOT run saveSettings again
		somethingChanged = false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.traceviewer.dialog.BaseTreeDialog#okPressed()
	 */
	@Override
	protected void okPressed() {
		TraceViewerGlobals.postUiEvent("OkButton", "1"); //$NON-NLS-1$ //$NON-NLS-2$
		saveSettings();
		super.okPressed();
		TraceViewerGlobals.postUiEvent("OkButton", "0"); //$NON-NLS-1$ //$NON-NLS-2$
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.nokia.traceviewer.dialog.BaseTreeDialog#getPropertyDialog(java.lang
	 * .Object, boolean)
	 */
	@Override
	protected BasePropertyDialog getPropertyDialog(Object selection,
			boolean editOldItem) {
		VariableTracingTreeItem oldItem = null;
		if (editOldItem) {
			oldItem = (VariableTracingTreeItem) selection;
		}
		VariableTracingPropertyDialog dialog = new VariableTracingPropertyDialog(
				getShell(), oldItem, contentProvider, selection);
		return dialog;
	}

	/**
	 * Edits existing item or adds new one
	 * 
	 * @param itemIndex
	 *            item index to be edited. If -1, create new one
	 */
	public void editOrAddItem(int itemIndex) {
		TreeItem item = null;

		// Create the dialog
		create();

		// Restore selected items to the tree
		restoreSettings();

		// Create new item
		if (itemIndex == -1) {
			item = processAddItemAction();

			// Edit old one
		} else {
			List<VariableTracingTreeTextItem> textRules = TraceViewerGlobals
					.getTraceViewer().getDataProcessorAccess()
					.getVariableTracingProcessor().getTextRules();

			viewer.setSelection(new StructuredSelection(textRules
					.get(itemIndex)));

			item = processEditItemAction();
		}

		if (item != null) {
			// Apply the changes
			somethingChanged = true;
			okPressed();
		} else {
			cancelPressed();
		}
	}
}
