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
 * Trigger Dialog class
 *
 */
package com.nokia.traceviewer.dialog;

import java.util.List;

import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

import com.nokia.traceviewer.TraceViewerHelpContextIDs;
import com.nokia.traceviewer.TraceViewerPlugin;
import com.nokia.traceviewer.dialog.treeitem.TreeItem;
import com.nokia.traceviewer.dialog.treeitem.TreeItemContentProvider;
import com.nokia.traceviewer.dialog.treeitem.TriggerTreeBaseItem;
import com.nokia.traceviewer.dialog.treeitem.TriggerTreeItem;
import com.nokia.traceviewer.dialog.treeitem.TriggerTreeTextItem;
import com.nokia.traceviewer.engine.TraceViewerGlobals;
import com.nokia.traceviewer.engine.preferences.PreferenceConstants;
import com.nokia.traceviewer.engine.preferences.XMLTriggerConfigurationExporter;

/**
 * Trigger Dialog class
 * 
 */
public final class TriggerDialog extends BaseTreeDialog {

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
			.getString("TriggerDialog.DialogName"); //$NON-NLS-1$

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
	public TriggerDialog(Shell parent, TreeItemContentProvider contentProvider,
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
		XMLTriggerConfigurationExporter exporter;

		// Default configuration file
		if (!TraceViewerPlugin.getDefault().getPreferenceStore().getString(
				PreferenceConstants.CONFIGURATION_FILE).equals(
				PreferenceConstants.DEFAULT_CONFIGURATION_FILE)) {
			exporter = new XMLTriggerConfigurationExporter(root,
					TraceViewerPlugin.getDefault().getPreferenceStore()
							.getString(PreferenceConstants.CONFIGURATION_FILE),
					false);
		} else {
			exporter = new XMLTriggerConfigurationExporter(root,
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

		// Save Triggers if something changed
		if (somethingChanged) {
			Object[] arr = viewer.getCheckedElements();

			// Get trigger arrays
			List<TriggerTreeTextItem> startTriggers = TraceViewerGlobals
					.getTraceViewer().getDataProcessorAccess()
					.getTriggerProcessor().getStartTriggers();
			List<TriggerTreeTextItem> stopTriggers = TraceViewerGlobals
					.getTraceViewer().getDataProcessorAccess()
					.getTriggerProcessor().getStopTriggers();
			List<TriggerTreeTextItem> activationTriggers = TraceViewerGlobals
					.getTraceViewer().getDataProcessorAccess()
					.getTriggerProcessor().getActivationTriggers();

			// Clear arrays
			startTriggers.clear();
			stopTriggers.clear();
			activationTriggers.clear();

			for (int i = 0; i < arr.length; i++) {
				// Text rule
				if (((TriggerTreeItem) arr[i]).getRule() == TriggerTreeItem.Rule.TEXT_RULE) {

					// Start Trigger
					if (((TriggerTreeItem) arr[i]).getType() == TriggerTreeItem.Type.STARTTRIGGER) {
						startTriggers.add(((TriggerTreeTextItem) arr[i]));

						// Stop Trigger
					} else if (((TriggerTreeItem) arr[i]).getType() == TriggerTreeItem.Type.STOPTRIGGER) {
						stopTriggers.add(((TriggerTreeTextItem) arr[i]));

						// Activation Trigger
					} else if (((TriggerTreeItem) arr[i]).getType() == TriggerTreeItem.Type.ACTIVATIONTRIGGER) {
						activationTriggers.add(((TriggerTreeTextItem) arr[i]));
					}
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

		// Get trigger arrays
		List<TriggerTreeTextItem> startTriggers = TraceViewerGlobals
				.getTraceViewer().getDataProcessorAccess()
				.getTriggerProcessor().getStartTriggers();
		List<TriggerTreeTextItem> stopTriggers = TraceViewerGlobals
				.getTraceViewer().getDataProcessorAccess()
				.getTriggerProcessor().getStopTriggers();
		List<TriggerTreeTextItem> activationTriggers = TraceViewerGlobals
				.getTraceViewer().getDataProcessorAccess()
				.getTriggerProcessor().getActivationTriggers();

		// Tree has to be re-checked
		for (int i = 0; i < startTriggers.size(); i++) {
			TriggerTreeTextItem item = startTriggers.get(i);
			viewer.setChecked(item, true);
			checkboxStateListener.checkStateChanged(item);
		}
		for (int i = 0; i < stopTriggers.size(); i++) {
			TriggerTreeTextItem item = stopTriggers.get(i);
			viewer.setChecked(item, true);
			checkboxStateListener.checkStateChanged(item);
		}
		for (int i = 0; i < activationTriggers.size(); i++) {
			TriggerTreeTextItem item = activationTriggers.get(i);
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

		// Set help
		PlatformUI.getWorkbench().getHelpSystem().setHelp(getShell(),
				TraceViewerHelpContextIDs.TRIGGERING);
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

	/**
	 * Checks if start triggers are checked
	 * 
	 * @return true if at least one start trigger is checked
	 */
	private boolean startTriggerChecked() {
		boolean startTriggerChecked = false;
		Object[] arr = viewer.getCheckedElements();
		for (int i = 0; i < arr.length; i++) {
			if (((TriggerTreeItem) arr[i]).getType() == TriggerTreeItem.Type.STARTTRIGGER
					&& ((TriggerTreeItem) arr[i]).getRule() != TriggerTreeItem.Rule.GROUP) {
				startTriggerChecked = true;
			}
		}
		return startTriggerChecked;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.traceviewer.dialog.BaseTreeDialog#processAddGroupAction()
	 */
	@Override
	protected void processAddGroupAction() {
		String name = Messages.getString("TriggerDialog.NewGroupText"); //$NON-NLS-1$
		InputDialog dialog = new InputDialog(
				getParentShell(),
				name,
				Messages.getString("TriggerDialog.NewGroupDialogInformation"), name, null); //$NON-NLS-1$
		int ret = dialog.open();
		if (ret == Window.OK) {
			name = dialog.getValue();

			// Get parent node
			Object selection = getSelectedGroup();
			TriggerTreeItem item = new TriggerTreeBaseItem(contentProvider,
					selection, name, TriggerTreeItem.Rule.GROUP,
					TriggerTreeItem.Type.STARTTRIGGER);
			((TriggerTreeItem) selection).addChild(item);
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
		boolean triggerCanBeSet = true;
		// Start trigger checked and traces in file
		if (startTriggerChecked()
				&& TraceViewerGlobals.getTraceViewer().getDataReaderAccess()
						.getCurrentDataReader().getTraceCount() > 0) {

			// Show confirmation message
			String confirmationMsg = Messages
					.getString("TriggerDialog.SetStartTriggerConfirmationMsg"); //$NON-NLS-1$
			boolean ret = TraceViewerGlobals.getTraceViewer().getDialogs()
					.showConfirmationDialog(confirmationMsg);

			// Clicked OK
			if (ret) {
				TraceViewerGlobals.getTraceViewer().clearAllData();
			} else {
				triggerCanBeSet = false;
			}
		}

		if (triggerCanBeSet) {
			saveSettings();

			// Remove pause as data is removed
			if (wasPausedWhenEntered) {
				wasPausedWhenEntered = false;
			}
			// Set the name of the view
			TraceViewerGlobals.getTraceViewer().getView().updateViewName();

			somethingChanged = false;
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
		TriggerTreeItem oldItem = null;
		if (editOldItem) {
			oldItem = (TriggerTreeItem) selection;
		}
		TriggerPropertyDialog dialog = new TriggerPropertyDialog(getShell(),
				oldItem, contentProvider, selection);
		return dialog;
	}
}
