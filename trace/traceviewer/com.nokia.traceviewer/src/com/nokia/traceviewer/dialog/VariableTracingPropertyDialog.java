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
 * Variable Tracing Property Dialog class
 *
 */
package com.nokia.traceviewer.dialog;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.nokia.traceviewer.dialog.treeitem.TreeItemListener;
import com.nokia.traceviewer.dialog.treeitem.VariableTracingTreeBaseItem;
import com.nokia.traceviewer.dialog.treeitem.VariableTracingTreeItem;
import com.nokia.traceviewer.dialog.treeitem.VariableTracingTreeTextItem;
import com.nokia.traceviewer.engine.TraceViewerGlobals;

/**
 * Variable Tracing Property Dialog class
 */
public final class VariableTracingPropertyDialog extends BasePropertyDialog {

	/**
	 * Text to show when Text rule is selected
	 */
	private static final String TEXT_RULE_INFORMATION_TEXT = Messages
			.getString("VariableTracingPropertyDialog.TextRuleInfo"); //$NON-NLS-1$

	/**
	 * Default number of history steps to save
	 */
	private static final String DEFAULT_HISTORY_COUNT = "100"; //$NON-NLS-1$

	/**
	 * Old item when editing
	 */
	private VariableTracingTreeItem oldItem;

	/**
	 * Selected item
	 */
	private final VariableTracingTreeItem selectedItem;

	/**
	 * Text field for history text
	 */
	private Text historyText;

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
	 *            Tree item listener
	 * @param selected
	 *            selected object
	 */
	public VariableTracingPropertyDialog(Shell parent,
			VariableTracingTreeItem newItem, TreeItemListener listener,
			Object selected) {
		super(parent, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
		this.newItem = newItem;
		this.selectedItem = (VariableTracingTreeItem) selected;
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
				Messages.getString("VariableTracingPropertyDialog.ShellTitle")); //$NON-NLS-1$
		composite.setLayout(new GridLayout());
		createMainComposite();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.nokia.traceviewer.dialog.BasePropertyDialog#createRightUnderComposite
	 * ()
	 */
	@Override
	protected void createRightUnderComposite() {
		// Right under composite
		GridLayout rightUnderCompositeLayout = new GridLayout();
		rightUnderCompositeLayout.marginHeight = 14;
		rightUnderCompositeLayout.numColumns = 3;
		GridData rightUnderCompositeGridData = new GridData();
		rightUnderCompositeGridData.heightHint = 110;
		rightUnderCompositeGridData.verticalAlignment = GridData.BEGINNING;
		rightUnderCompositeGridData.widthHint = 250;
		rightUnderCompositeGridData.horizontalAlignment = GridData.CENTER;
		rightUnderComposite = new Composite(mainComposite, SWT.BORDER);
		rightUnderComposite.setLayoutData(rightUnderCompositeGridData);
		rightUnderComposite.setLayout(rightUnderCompositeLayout);

		// Text label
		textLabel = new Label(rightUnderComposite, SWT.NONE);
		textLabel.setText(Messages
				.getString("VariableTracingPropertyDialog.TextLabelText")); //$NON-NLS-1$

		// Text text
		GridData textTextGridData = new GridData();
		textTextGridData.widthHint = 150;
		textTextGridData.horizontalSpan = 2;
		textText = new Text(rightUnderComposite, SWT.BORDER);
		textText.setLayoutData(textTextGridData);

		if (oldItem instanceof VariableTracingTreeTextItem) {
			VariableTracingTreeTextItem item = (VariableTracingTreeTextItem) oldItem;
			textText.setText(item.getText());
		}

		// Spacer label
		new Label(rightUnderComposite, SWT.NONE);

		// Match case checkbox
		GridData matchCaseCheckBoxGridData = new GridData();
		matchCaseCheckBoxGridData.horizontalSpan = 2;
		matchCaseCheckBox = new Button(rightUnderComposite, SWT.CHECK);
		matchCaseCheckBox.setText(Messages
				.getString("VariableTracingPropertyDialog.MatchCaseText")); //$NON-NLS-1$
		matchCaseCheckBox.setLayoutData(matchCaseCheckBoxGridData);

		if (oldItem instanceof VariableTracingTreeTextItem) {
			VariableTracingTreeTextItem item = (VariableTracingTreeTextItem) oldItem;
			matchCaseCheckBox.setSelection(item.isMatchCase());
		}

		// History label
		GridData historyLabelGridData = new GridData();
		historyLabelGridData.verticalIndent = 10;
		Label historyLabel = new Label(rightUnderComposite, SWT.NONE);
		historyLabel.setText(Messages
				.getString("VariableTracingPropertyDialog.SaveHistoryText")); //$NON-NLS-1$
		historyLabel.setLayoutData(historyLabelGridData);

		// History text
		historyText = new Text(rightUnderComposite, SWT.BORDER);
		GridData historyTextGridData = new GridData();
		historyTextGridData.widthHint = 20;
		historyTextGridData.verticalIndent = 10;
		historyText.setLayoutData(historyTextGridData);
		historyText.setTextLimit(3);

		if (oldItem instanceof VariableTracingTreeBaseItem) {
			VariableTracingTreeBaseItem item = (VariableTracingTreeBaseItem) oldItem;
			historyText.setText(String.valueOf(item.getHistoryCount()));
		} else {
			historyText.setText(DEFAULT_HISTORY_COUNT);
		}

		// History label 2
		Label historyLabel2 = new Label(rightUnderComposite, SWT.NONE);
		historyLabel2.setText(Messages
				.getString("VariableTracingPropertyDialog.SaveHistoryText2")); //$NON-NLS-1$
		historyLabel2.setLayoutData(historyLabelGridData);
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
		nameLabel.setText(Messages
				.getString("VariableTracingPropertyDialog.RuleNameText")); //$NON-NLS-1$
		// Set data from old item
		if (oldItem != null) {
			nameText.setText(oldItem.getName());
			if (oldItem.getRule() == VariableTracingTreeItem.Rule.TEXT_RULE) {
				selectedRuleLabel.setText(TEXT_RULE_NAME);
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
	 * @see
	 * com.nokia.traceviewer.dialog.BasePropertyDialog#insertDataToTextFields()
	 */
	@Override
	protected void insertDataToTextFields() {
		if (oldItem instanceof VariableTracingTreeTextItem) {
			VariableTracingTreeTextItem item = (VariableTracingTreeTextItem) oldItem;
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
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.traceviewer.dialog.BasePropertyDialog#setListSelection()
	 */
	@Override
	protected void setListSelection() {
		ruleList.add(TEXT_RULE_NAME, TEXT_RULE_INDEX);
		// Set list selection
		if (oldItem != null) {
			if (oldItem.getRule() == VariableTracingTreeItem.Rule.TEXT_RULE) {
				ruleList.setSelection(TEXT_RULE_INDEX);
			}
		} else {
			ruleList.setSelection(TEXT_RULE_INDEX);
		}
	}

	@Override
	protected void okPressed() {
		TraceViewerGlobals.postUiEvent("OkButton", "1"); //$NON-NLS-1$ //$NON-NLS-2$
		// Save the position of this item to set it back after creation
		// and remove the old child
		int pos = 0;
		VariableTracingTreeItem parent = selectedItem;
		if (oldItem != null) {
			pos = getOldPosition();
			parent = (VariableTracingTreeItem) oldItem.getParent();
			parent.removeChild(oldItem);
		}

		// Create new VariableTracingTreeTextItem
		if (ruleList.getSelectionIndex() == TEXT_RULE_INDEX) {
			int history;
			try {
				history = Integer.parseInt(historyText.getText());
			} catch (Exception e) {
				history = 1;
			}
			if (history < 1) {
				history = 1;
			}

			newItem = new VariableTracingTreeTextItem(listener, parent,
					nameText.getText(), VariableTracingTreeItem.Rule.TEXT_RULE,
					textText.getText(), matchCaseCheckBox.getSelection(),
					history);

			// Add mode
			if (parent.equals(selectedItem)) {
				parent.addChild(newItem);

				// Edit mode
			} else {
				parent.addChild(pos, newItem);
			}

		} else {
			// No other rules yet
		}

		super.okPressed();
		TraceViewerGlobals.postUiEvent("OkButton", "0"); //$NON-NLS-1$ //$NON-NLS-2$
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.nokia.traceviewer.dialog.BasePropertyDialog#createActionListeners()
	 */
	@Override
	protected void createActionListeners() {
		super.createActionListeners();
		super.createActionListenersForTextRule();
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
		}
	}

	/**
	 * Gets old position of this item
	 * 
	 * @return old position
	 */
	protected int getOldPosition() {
		VariableTracingTreeItem item = (VariableTracingTreeItem) oldItem
				.getParent();
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
