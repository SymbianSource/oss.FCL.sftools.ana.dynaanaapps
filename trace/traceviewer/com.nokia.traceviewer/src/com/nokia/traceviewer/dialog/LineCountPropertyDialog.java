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
 * Line Count Property Dialog class
 *
 */
package com.nokia.traceviewer.dialog;

import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Shell;

import com.nokia.traceviewer.dialog.treeitem.LineCountTreeComponentItem;
import com.nokia.traceviewer.dialog.treeitem.LineCountTreeItem;
import com.nokia.traceviewer.dialog.treeitem.LineCountTreeTextItem;
import com.nokia.traceviewer.dialog.treeitem.TreeItemListener;
import com.nokia.traceviewer.engine.TraceViewerGlobals;
import com.nokia.traceviewer.engine.activation.TraceActivationComponentItem;
import com.nokia.traceviewer.engine.activation.TraceActivationGroupItem;

/**
 * Line Count Property Dialog class
 * 
 */
public class LineCountPropertyDialog extends BasePropertyDialog {

	/**
	 * Text to show when Text rule is selected
	 */
	private static final String TEXT_RULE_INFORMATION_TEXT = Messages
			.getString("LineCountPropertyDialog.TextRuleInfo"); //$NON-NLS-1$

	/**
	 * Text to show when Component rule is selected
	 */
	private static final String COMPONENT_RULE_INFORMATION_TEXT = Messages
			.getString("LineCountPropertyDialog.ComponentRuleInfoLine1") //$NON-NLS-1$
			+ WILDCARD_STRING
			+ Messages
					.getString("LineCountPropertyDialog.ComponentRuleInfoLine2"); //$NON-NLS-1$

	/**
	 * Old item used when editing
	 */
	private LineCountTreeItem oldItem;

	/**
	 * Selected item in tree
	 */
	private final LineCountTreeItem selectedItem;

	/**
	 * TreeItem listener
	 */
	private final TreeItemListener listener;

	/**
	 * Constructor
	 * 
	 * @param parent
	 *            Parent shell
	 * @param newItem
	 *            TreeItem to modify, null if creating new one
	 * @param listener
	 *            TreeItem listener
	 * @param selected
	 *            selected item in tree
	 */
	public LineCountPropertyDialog(Shell parent, LineCountTreeItem newItem,
			TreeItemListener listener, Object selected) {
		super(parent, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
		this.newItem = newItem;
		this.selectedItem = (LineCountTreeItem) selected;
		this.listener = listener;

		// If edit, save the old item to insert info to fields
		if (newItem != null) {
			oldItem = newItem;
			textFieldTouched = true;
			nameFieldTouched = true;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.traceviewer.dialog.BaseDialog#createDialogContents()
	 */
	@Override
	protected void createDialogContents() {
		getShell().setText(
				Messages.getString("LineCountPropertyDialog.ShellTitle")); //$NON-NLS-1$
		composite.setLayout(new GridLayout());
		createMainComposite();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.nokia.traceviewer.dialog.BasePropertyDialog#insertDataToUpperComposite
	 * ()
	 */
	@Override
	protected void insertDataToUpperComposite() {
		// Set data from old item
		if (oldItem != null) {
			nameText.setText(oldItem.getName());
			if (oldItem.getRule() == LineCountTreeItem.Rule.TEXT_RULE) {
				selectedRuleLabel.setText(TEXT_RULE_NAME);
			} else if (oldItem.getRule() == LineCountTreeItem.Rule.TEXT_RULE) {
				selectedRuleLabel.setText(COMPONENT_RULE_NAME);
			} else {
				selectedRuleLabel.setText(TEXT_RULE_NAME);
			}
		} else {
			selectedRuleLabel.setText(TEXT_RULE_NAME);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.traceviewer.dialog.BasePropertyDialog#setListSelection()
	 */
	@Override
	protected void setListSelection() {
		ruleList.add(TEXT_RULE_NAME, TEXT_RULE_INDEX);
		ruleList.add(COMPONENT_RULE_NAME, COMPONENT_RULE_INDEX);
		// Set list selection
		if (oldItem != null) {
			if (oldItem.getRule() == LineCountTreeItem.Rule.TEXT_RULE) {
				ruleList.setSelection(TEXT_RULE_INDEX);
			} else if (oldItem.getRule() == LineCountTreeItem.Rule.COMPONENT_RULE) {
				ruleList.setSelection(COMPONENT_RULE_INDEX);
			}
		} else {
			ruleList.setSelection(TEXT_RULE_INDEX);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.nokia.traceviewer.dialog.BasePropertyDialog#insertDataToTextFields()
	 */
	@Override
	protected void insertDataToTextFields() {
		if (oldItem instanceof LineCountTreeTextItem) {
			LineCountTreeTextItem item = (LineCountTreeTextItem) oldItem;
			if (item.getText() != null) {
				textText.setText(item.getText());
			}
			matchCaseCheckBox.setSelection(item.isMatchCase());
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.nokia.traceviewer.dialog.BasePropertyDialog#insertDataToComponentFields
	 * ()
	 */
	@Override
	protected void insertDataToComponentFields() {
		// Get component list
		List<TraceActivationComponentItem> components = null;
		if (TraceViewerGlobals.getDecodeProvider() != null) {
			components = TraceViewerGlobals.getDecodeProvider()
					.getActivationInformation(false);
		}

		// Insert data to fields
		if (oldItem instanceof LineCountTreeComponentItem) {
			LineCountTreeComponentItem item = (LineCountTreeComponentItem) oldItem;
			if (item.getComponentId() == WILDCARD_INTEGER) {
				componentTextField.setText(WILDCARD_STRING);
			} else {
				componentTextField.setText(HEX_PREFIX
						+ Integer.toHexString(item.getComponentId()));
			}
			if (item.getGroupId() == WILDCARD_INTEGER) {
				groupTextField.setText(WILDCARD_STRING);
			} else {
				groupTextField.setText(HEX_PREFIX
						+ Integer.toHexString(item.getGroupId()));
			}

			// Loop through components
			if (components != null) {
				for (int i = 0; i < components.size(); i++) {
					if (components.get(i).getId() == item.getComponentId()) {
						componentNameLabel.setText(components.get(i).getName());
						// Get groups
						List<TraceActivationGroupItem> groups = components.get(
								i).getGroups();
						// Loop through groups
						for (int j = 0; j < groups.size(); j++) {
							if (groups.get(j).getId() == item.getGroupId()) {
								groupNameLabel.setText(groups.get(j).getName());
								break;
							}
						}
						break;
					}
				}
			}
		}

		// Disable change component / group buttons if no components are
		// available
		if (components == null || components.size() <= 0) {
			changeComponentButton.setEnabled(false);
			changeGroupButton.setEnabled(false);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.dialogs.Dialog#okPressed()
	 */
	@Override
	protected void okPressed() {
		TraceViewerGlobals.postUiEvent("OkButton", "1"); //$NON-NLS-1$ //$NON-NLS-2$
		// Save the position of this item to set it back after creation
		// and remove the old child
		int pos = 0;
		LineCountTreeItem parent = selectedItem;
		if (oldItem != null) {
			pos = getOldPosition();
			parent = (LineCountTreeItem) oldItem.getParent();
			parent.removeChild(oldItem);
		}

		// Create new LineCountTreeTextItem
		if (ruleList.getSelectionIndex() == TEXT_RULE_INDEX) {
			newItem = new LineCountTreeTextItem(listener, parent, nameText
					.getText(), LineCountTreeItem.Rule.TEXT_RULE, textText
					.getText(), matchCaseCheckBox.getSelection());

			// Create new LineCountTreeComponentItem
		} else if (ruleList.getSelectionIndex() == COMPONENT_RULE_INDEX) {
			int componentId = WILDCARD_INTEGER;
			int groupId = WILDCARD_INTEGER;

			// Get component ID
			try {
				String cid = componentTextField.getText();
				if (cid.substring(0, HEX_PREFIX.length()).equals(HEX_PREFIX)) {
					cid = cid.substring(HEX_PREFIX.length());
				}
				componentId = Integer.parseInt(cid, HEX_RADIX);
			} catch (Exception e) {
			}

			// Get group ID
			try {
				String gid = groupTextField.getText();
				if (gid.substring(0, HEX_PREFIX.length()).equals(HEX_PREFIX)) {
					gid = gid.substring(HEX_PREFIX.length());
				}
				groupId = Integer.parseInt(gid, HEX_RADIX);
			} catch (Exception e) {
			}

			// Create new item
			newItem = new LineCountTreeComponentItem(listener, parent, nameText
					.getText(), LineCountTreeItem.Rule.COMPONENT_RULE,
					componentId, groupId);
		}

		// Add mode
		if (parent.equals(selectedItem)) {
			parent.addChild(newItem);

			// Edit mode
		} else {
			parent.addChild(pos, newItem);
		}

		super.okPressed();
		TraceViewerGlobals.postUiEvent("OkButton", "0"); //$NON-NLS-1$ //$NON-NLS-2$
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.nokia.traceviewer.dialog.BasePropertyDialog#setInformationLabel()
	 */
	@Override
	protected void setInformationLabel() {
		if (ruleList.getSelectionIndex() == TEXT_RULE_INDEX) {
			informationLabel.setText(TEXT_RULE_INFORMATION_TEXT);
			selectedRuleLabel.setText(TEXT_RULE_NAME);
		} else if (ruleList.getSelectionIndex() == COMPONENT_RULE_INDEX) {
			informationLabel.setText(COMPONENT_RULE_INFORMATION_TEXT);
			selectedRuleLabel.setText(COMPONENT_RULE_NAME);
		}
	}

	/**
	 * Gets old position of this item
	 * 
	 * @return old position
	 */
	protected int getOldPosition() {
		LineCountTreeItem item = (LineCountTreeItem) oldItem.getParent();
		Object[] children = item.getChildren();
		int pos = 0;
		for (int i = 0; i < children.length; i++) {
			if (children[i].equals(oldItem)) {
				pos = i;
				break;
			}
		}
		return pos;
	}

}
