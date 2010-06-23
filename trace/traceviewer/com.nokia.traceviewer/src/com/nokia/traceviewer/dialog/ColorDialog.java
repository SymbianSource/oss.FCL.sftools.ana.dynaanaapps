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
 * Color dialog class
 *
 */
package com.nokia.traceviewer.dialog;

import java.util.List;

import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.widgets.Shell;

import com.nokia.traceviewer.TraceViewerPlugin;
import com.nokia.traceviewer.dialog.treeitem.ColorTreeBaseItem;
import com.nokia.traceviewer.dialog.treeitem.ColorTreeComponentItem;
import com.nokia.traceviewer.dialog.treeitem.ColorTreeItem;
import com.nokia.traceviewer.dialog.treeitem.ColorTreeTextItem;
import com.nokia.traceviewer.dialog.treeitem.TreeItem;
import com.nokia.traceviewer.dialog.treeitem.TreeItemContentProvider;
import com.nokia.traceviewer.engine.TraceViewerGlobals;
import com.nokia.traceviewer.engine.preferences.PreferenceConstants;
import com.nokia.traceviewer.engine.preferences.XMLColorConfigurationExporter;

/**
 * Color dialog class
 */
public final class ColorDialog extends BaseTreeDialog {

	/**
	 * Add item image
	 */
	private static final String itemAddImageLocation = "/icons/coloradd.gif"; //$NON-NLS-1$

	/**
	 * Edit item image
	 */
	private static final String itemEditImageLocation = "/icons/coloredit.gif"; //$NON-NLS-1$

	/**
	 * Remove item image
	 */
	private static final String itemRemoveImageLocation = "/icons/colorremove.gif"; //$NON-NLS-1$

	/**
	 * Dialog name
	 */
	private final String dialogName = Messages
			.getString("ColorDialog.DialogName"); //$NON-NLS-1$

	/**
	 * Constructor
	 * 
	 * @param parent
	 *            parent shell
	 * @param contentProvider
	 *            content provider
	 * @param treeRoot
	 *            root tree item for the dialog
	 */
	public ColorDialog(Shell parent, TreeItemContentProvider contentProvider,
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
		XMLColorConfigurationExporter exporter;

		// Default configuration file
		if (!TraceViewerPlugin.getDefault().getPreferenceStore().getString(
				PreferenceConstants.CONFIGURATION_FILE).equals(
				PreferenceConstants.DEFAULT_CONFIGURATION_FILE)) {
			exporter = new XMLColorConfigurationExporter(root,
					TraceViewerPlugin.getDefault().getPreferenceStore()
							.getString(PreferenceConstants.CONFIGURATION_FILE),
					false);
		} else {
			exporter = new XMLColorConfigurationExporter(root,
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

		// Save rules if something changed
		if (somethingChanged) {
			Object[] arr = viewer.getCheckedElements();

			// Get rule arrays
			List<ColorTreeTextItem> textRules = TraceViewerGlobals
					.getTraceViewer().getDataProcessorAccess().getColorer()
					.getTextRules();
			List<ColorTreeComponentItem> componentRules = TraceViewerGlobals
					.getTraceViewer().getDataProcessorAccess().getColorer()
					.getComponentRules();

			// Clear old rules
			textRules.clear();
			componentRules.clear();

			// Insert new rules
			for (int i = 0; i < arr.length; i++) {
				// Text rule
				if (((ColorTreeItem) arr[i]).getRule() == ColorTreeItem.Rule.TEXT_RULE) {
					textRules.add(((ColorTreeTextItem) arr[i]));
					// Component rule
				} else if (((ColorTreeItem) arr[i]).getRule() == ColorTreeItem.Rule.COMPONENT_RULE) {
					componentRules.add(((ColorTreeComponentItem) arr[i]));
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
		// Get rule arrays
		List<ColorTreeTextItem> textRules = TraceViewerGlobals.getTraceViewer()
				.getDataProcessorAccess().getColorer().getTextRules();
		List<ColorTreeComponentItem> componentRules = TraceViewerGlobals
				.getTraceViewer().getDataProcessorAccess().getColorer()
				.getComponentRules();

		// Tree has to be re-checked
		for (int i = 0; i < textRules.size(); i++) {
			ColorTreeTextItem item = textRules.get(i);
			viewer.setChecked(item, true);
			checkboxStateListener.checkStateChanged(item);
		}
		for (int i = 0; i < componentRules.size(); i++) {
			ColorTreeComponentItem item = componentRules.get(i);
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
		String name = Messages.getString("ColorDialog.NewGroupName"); //$NON-NLS-1$
		InputDialog dialog = new InputDialog(getShell(), name, Messages
				.getString("ColorDialog.NewGroupInputText"), name, null); //$NON-NLS-1$
		int ret = dialog.open();
		if (ret == Window.OK) {
			name = dialog.getValue();

			// Get parent node
			Object selection = getSelectedGroup();
			ColorTreeBaseItem item = new ColorTreeBaseItem(contentProvider,
					selection, name, ColorTreeItem.Rule.GROUP, null, null);
			((TreeItem) selection).addChild(item);
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

		if (somethingChanged) {
			// Clear all old rules
			TraceViewerGlobals.getTraceViewer().getDataProcessorAccess()
					.getColorer().clearRanges();
			TraceViewerGlobals.getTraceViewer().getView().applyColorRules(
					TraceViewerGlobals.getTraceViewer()
							.getDataProcessorAccess().getColorer().getRanges()
							.toArray(new StyleRange[0]));

			// Create new rules
			TraceViewerGlobals.getTraceViewer().getDataProcessorAccess()
					.getColorer().createColorRules();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.nokia.traceviewer.dialog.BaseTreeDialog#processRemoveItemAction()
	 */
	@Override
	protected void processRemoveItemAction() {
		Object selection = ((IStructuredSelection) viewer.getSelection())
				.getFirstElement();
		if (selection != null && selection != treeRoot && selection != root) {
			// Remove selection
			Object parent = ((ColorTreeItem) selection).getParent();
			((ColorTreeItem) parent).removeChild((ColorTreeItem) selection);
			checkboxStateListener.checkStateChanged((ColorTreeItem) parent);

			// Dispose colors
			((ColorTreeItem) selection).getForegroundColor().dispose();
			((ColorTreeItem) selection).getBackgroundColor().dispose();

			// Select parent from the deleted item
			viewer.setSelection(new StructuredSelection(parent), true);
			viewer.reveal(parent);

			somethingChanged = true;
		}

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
		ColorTreeItem oldItem = null;
		if (editOldItem) {
			oldItem = (ColorTreeItem) selection;
		}
		ColorPropertyDialog dialog = new ColorPropertyDialog(getShell(),
				oldItem, contentProvider, selection);
		return dialog;
	}
}
