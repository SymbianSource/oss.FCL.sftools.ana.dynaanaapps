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
 * Line counting Dialog class
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
import com.nokia.traceviewer.dialog.treeitem.LineCountTreeBaseItem;
import com.nokia.traceviewer.dialog.treeitem.LineCountTreeComponentItem;
import com.nokia.traceviewer.dialog.treeitem.LineCountTreeItem;
import com.nokia.traceviewer.dialog.treeitem.LineCountTreeTextItem;
import com.nokia.traceviewer.dialog.treeitem.TreeItem;
import com.nokia.traceviewer.dialog.treeitem.TreeItemContentProvider;
import com.nokia.traceviewer.engine.TraceViewerGlobals;
import com.nokia.traceviewer.engine.TraceViewerPropertyViewInterface;
import com.nokia.traceviewer.engine.TraceViewerDialogInterface.Dialog;
import com.nokia.traceviewer.engine.dataprocessor.LineCountItem;
import com.nokia.traceviewer.engine.preferences.PreferenceConstants;
import com.nokia.traceviewer.engine.preferences.XMLLineCountConfigurationExporter;

/**
 * Line counting Dialog class
 */
public final class LineCountDialog extends BaseTreeDialog {

	/**
	 * Processing reason to give to progressBar dialog
	 */
	private static final String processReason = Messages
			.getString("LineCountDialog.ProcessReason"); //$NON-NLS-1$

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
	private static final String dialogName = "Line Count Rules"; //$NON-NLS-1$

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
	public LineCountDialog(Shell parent,
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
		XMLLineCountConfigurationExporter exporter;

		// Default configuration file
		if (!TraceViewerPlugin.getDefault().getPreferenceStore().getString(
				PreferenceConstants.CONFIGURATION_FILE).equals(
				PreferenceConstants.DEFAULT_CONFIGURATION_FILE)) {
			exporter = new XMLLineCountConfigurationExporter(root,
					TraceViewerPlugin.getDefault().getPreferenceStore()
							.getString(PreferenceConstants.CONFIGURATION_FILE),
					false);
		} else {
			exporter = new XMLLineCountConfigurationExporter(root,
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
		super.saveSettings();

		// Save LineCountItems if something changed
		if (somethingChanged && !viewer.getControl().isDisposed()) {
			Object[] arr = viewer.getCheckedElements();

			// Get and clear all old rules and items
			List<LineCountItem> lineCountItems = TraceViewerGlobals
					.getTraceViewer().getDataProcessorAccess()
					.getLineCountProcessor().getLineCountItems();
			List<LineCountTreeTextItem> textRules = TraceViewerGlobals
					.getTraceViewer().getDataProcessorAccess()
					.getLineCountProcessor().getTextRules();
			List<LineCountTreeComponentItem> componentRules = TraceViewerGlobals
					.getTraceViewer().getDataProcessorAccess()
					.getLineCountProcessor().getComponentRules();
			lineCountItems.clear();
			textRules.clear();
			componentRules.clear();

			// Save rules
			for (int i = 0; i < arr.length; i++) {
				LineCountTreeItem listItem = (LineCountTreeItem) arr[i];
				// Text rules
				if (listItem.getRule() == LineCountTreeItem.Rule.TEXT_RULE) {
					textRules.add((LineCountTreeTextItem) listItem);
					// Component rules
				} else if (listItem.getRule() == LineCountTreeItem.Rule.COMPONENT_RULE) {
					componentRules.add((LineCountTreeComponentItem) listItem);
				}
			}

			// Insert rules to LineCountItem lists starting from component rules
			for (int i = 0; i < componentRules.size(); i++) {
				LineCountItem item = new LineCountItem(componentRules.get(i)
						.getName(), 0);
				lineCountItems.add(item);
			}
			// Text rules
			for (int i = 0; i < textRules.size(); i++) {
				LineCountItem item = new LineCountItem(textRules.get(i)
						.getName(), 0);
				lineCountItems.add(item);
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

		// Get rule arrays
		List<LineCountTreeTextItem> textRules = TraceViewerGlobals
				.getTraceViewer().getDataProcessorAccess()
				.getLineCountProcessor().getTextRules();
		List<LineCountTreeComponentItem> componentRules = TraceViewerGlobals
				.getTraceViewer().getDataProcessorAccess()
				.getLineCountProcessor().getComponentRules();

		// Tree has to be re-checked
		for (int i = 0; i < textRules.size(); i++) {
			LineCountTreeTextItem item = textRules.get(i);
			viewer.setChecked(item, true);
			checkboxStateListener.checkStateChanged(item);
		}
		for (int i = 0; i < componentRules.size(); i++) {
			LineCountTreeComponentItem item = componentRules.get(i);
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
		String name = Messages.getString("LineCountDialog.NewGroupText"); //$NON-NLS-1$
		InputDialog dialog = new InputDialog(getShell(), name, Messages
				.getString("LineCountDialog.NewGroupDialogInfo"), name, null); //$NON-NLS-1$
		int ret = dialog.open();
		if (ret == Window.OK) {
			name = dialog.getValue();
			// Get parent node

			Object selection = getSelectedGroup();
			LineCountTreeItem item = new LineCountTreeBaseItem(contentProvider,
					selection, name, LineCountTreeItem.Rule.GROUP);
			((LineCountTreeItem) selection).addChild(item);
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

		List<LineCountItem> items = TraceViewerGlobals.getTraceViewer()
				.getDataProcessorAccess().getLineCountProcessor()
				.getLineCountItems();

		// Start counting lines from beginning
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
		LineCountTreeItem oldItem = null;
		if (editOldItem) {
			oldItem = (LineCountTreeItem) selection;
		}
		LineCountPropertyDialog dialog = new LineCountPropertyDialog(
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

			List<LineCountTreeTextItem> textRules = TraceViewerGlobals
					.getTraceViewer().getDataProcessorAccess()
					.getLineCountProcessor().getTextRules();
			List<LineCountTreeComponentItem> componentRules = TraceViewerGlobals
					.getTraceViewer().getDataProcessorAccess()
					.getLineCountProcessor().getComponentRules();
			if (itemIndex < componentRules.size()) {
				viewer.setSelection(new StructuredSelection(componentRules
						.get(itemIndex)));
			} else {
				itemIndex -= componentRules.size();
				viewer.setSelection(new StructuredSelection(textRules
						.get(itemIndex)));
			}

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
