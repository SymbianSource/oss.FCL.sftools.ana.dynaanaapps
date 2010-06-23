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
 * Component / Group selection dialog
 *
 */
package com.nokia.traceviewer.dialog;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.PlatformUI;

import com.nokia.traceviewer.engine.TraceViewerGlobals;
import com.nokia.traceviewer.engine.activation.TraceActivationComponentItem;
import com.nokia.traceviewer.engine.activation.TraceActivationGroupItem;

/**
 * Component / Group selection dialog
 * 
 */
public final class ComponentSelectionDialog extends BaseDialog {

	/**
	 * Hex prefix
	 */
	private static final String HEX_PREFIX = "0x"; //$NON-NLS-1$

	/**
	 * Group column text
	 */
	private static final String GROUP_COLUMN_TEXT = Messages
			.getString("ComponentSelectionDialog.GroupColumnText"); //$NON-NLS-1$

	/**
	 * Component column text
	 */
	private static final String COMPONENT_COLUMN_TEXT = Messages
			.getString("ComponentSelectionDialog.ComponentColumnText"); //$NON-NLS-1$

	/**
	 * Group shell text
	 */
	private static final String GROUP_SHELL_TEXT = Messages
			.getString("ComponentSelectionDialog.GroupShellText"); //$NON-NLS-1$

	/**
	 * Component shell text
	 */
	private static final String COMPONENT_SHELL_TEXT = Messages
			.getString("ComponentSelectionDialog.ComponentShellText"); //$NON-NLS-1$

	/**
	 * Table where components / groups are shown
	 */
	private Table table;

	/**
	 * Name column for table
	 */
	private TableColumn nameColumn;

	/**
	 * ID column for table
	 */
	private TableColumn idColumn;

	/**
	 * Indicates that should dialog show components or groups
	 */
	private boolean isShowingComponents = true;

	/**
	 * If dialog is showing groups, show them from this component ID
	 */
	private int componentId;

	/**
	 * Selected item ID that can be asked after dialog is closed
	 */
	private String selectedItemId;

	/**
	 * Selected item name that can be asked after dialog is closed
	 */
	private String selectedItemName;

	/**
	 * Constructor
	 */
	public ComponentSelectionDialog() {
		// Pass the default styles here
		super(PlatformUI.getWorkbench().getDisplay().getActiveShell(),
				SWT.DIALOG_TRIM | SWT.RESIZE | SWT.SYSTEM_MODAL);
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
		shellGridLayout.numColumns = 3;
		if (isShowingComponents) {
			getShell().setText(COMPONENT_SHELL_TEXT);
		} else {
			getShell().setText(GROUP_SHELL_TEXT);
		}
		composite.setLayout(shellGridLayout);

		// Table
		GridData listGridData = new GridData();
		listGridData.horizontalAlignment = GridData.FILL;
		listGridData.grabExcessHorizontalSpace = true;
		listGridData.grabExcessVerticalSpace = true;
		listGridData.horizontalSpan = 3;
		listGridData.verticalAlignment = GridData.FILL;
		listGridData.heightHint = 400;
		listGridData.widthHint = 300;
		table = new Table(composite, SWT.BORDER | SWT.FULL_SELECTION);
		table.setHeaderVisible(true);
		table.setLayoutData(listGridData);

		// Table columns
		nameColumn = new TableColumn(table, SWT.NONE);
		if (isShowingComponents) {
			nameColumn.setText(COMPONENT_COLUMN_TEXT);
		} else {
			nameColumn.setText(GROUP_COLUMN_TEXT);
		}
		idColumn = new TableColumn(table, SWT.NONE);
		idColumn.setText("ID"); //$NON-NLS-1$

		// Spacer
		GridData spacerGridData = new GridData();
		spacerGridData.horizontalAlignment = GridData.BEGINNING;
		spacerGridData.grabExcessHorizontalSpace = true;
		spacerGridData.verticalAlignment = GridData.CENTER;
		Label spacerLabel = new Label(composite, SWT.NONE);
		spacerLabel.setText(""); //$NON-NLS-1$
		spacerLabel.setLayoutData(spacerGridData);

		// Fill table
		if (TraceViewerGlobals.getDecodeProvider() != null) {
			fillTable();
		}
	}

	/**
	 * Fills table from decode provider
	 */
	private void fillTable() {
		List<TraceActivationComponentItem> components = TraceViewerGlobals
				.getDecodeProvider().getActivationInformation(false);
		// Show components
		if (isShowingComponents) {
			for (int i = 0; i < components.size(); i++) {
				TableItem item = new TableItem(table, SWT.NONE);
				String id = HEX_PREFIX
						+ Integer.toHexString(components.get(i).getId());
				item.setText(new String[] { components.get(i).getName(), id });
			}

			// Show groups
		} else {
			// Component is defined, show groups from that component only
			if (componentId != NOT_DEFINED) {
				TraceActivationComponentItem component = null;

				// Find the right component
				for (int i = 0; i < components.size(); i++) {
					if (components.get(i).getId() == componentId) {
						component = components.get(i);
						break;
					}
				}

				if (component != null) {

					// Print the groups from the component
					for (int i = 0; i < component.getGroups().size(); i++) {
						TraceActivationGroupItem group = component.getGroups()
								.get(i);
						TableItem item = new TableItem(table, SWT.NONE);
						String id = HEX_PREFIX
								+ Integer.toHexString(group.getId());
						item.setText(new String[] { group.getName(), id });
					}
				}

				// Component is not defined, show all groups
			} else {
				// Put all groups to one arraylist
				ArrayList<TraceActivationGroupItem> allGroups = new ArrayList<TraceActivationGroupItem>();

				// Loop through components
				for (int i = 0; i < components.size(); i++) {
					allGroups.addAll(components.get(i).getGroups());
				}
				// Create listitems from the list
				for (int i = 0; i < allGroups.size(); i++) {
					TableItem item = new TableItem(table, SWT.NONE);
					String id = HEX_PREFIX
							+ Integer.toHexString(allGroups.get(i).getId());
					item
							.setText(new String[] { allGroups.get(i).getName(),
									id });
				}
			}
		}

		nameColumn.pack();
		idColumn.pack();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.dialogs.Dialog#okPressed()
	 */
	@Override
	protected void okPressed() {
		TableItem[] selection = table.getSelection();
		if (selection.length > 0) {
			try {
				// Get the ID
				selectedItemId = selection[0].getText(1);
			} catch (NumberFormatException e) {
				selectedItemId = ""; //$NON-NLS-1$
			}
			// Get the name
			selectedItemName = selection[0].getText(0);
		} else {
			selectedItemId = ""; //$NON-NLS-1$
			selectedItemName = ""; //$NON-NLS-1$
		}
		super.okPressed();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.dialogs.Dialog#cancelPressed()
	 */
	@Override
	protected void cancelPressed() {
		selectedItemId = ""; //$NON-NLS-1$
		selectedItemName = ""; //$NON-NLS-1$
		super.cancelPressed();
	}

	/**
	 * Gets the selected item ID as a hex String
	 * 
	 * @return the selectedItemId
	 */
	public String getSelectedItemId() {
		return selectedItemId;
	}

	/**
	 * Gets selected item name
	 * 
	 * @return the selectedItemName
	 */
	public String getSelectedItemName() {
		return selectedItemName;
	}

	/**
	 * Sets dialog to show either components or groups
	 * 
	 * @param showComponents
	 *            if true, dialog shows components
	 * @param componentId
	 *            if showing groups, show them only from this component
	 */
	public void setDialogToShowComponents(boolean showComponents,
			int componentId) {
		// Remove old items
		if (table != null && !table.isDisposed()) {
			table.removeAll();
		}
		isShowingComponents = showComponents;
		if (!showComponents) {
			this.componentId = componentId;
		} else {
			this.componentId = NOT_DEFINED;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.traceviewer.dialog.BaseDialog#createActionListeners()
	 */
	@Override
	protected void createActionListeners() {
		// No need for action listeners in this dialog
	}

}
