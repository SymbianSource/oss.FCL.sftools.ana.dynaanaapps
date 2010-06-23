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
 * Trigger Property Dialog class
 *
 */
package com.nokia.traceviewer.dialog;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.nokia.traceviewer.dialog.treeitem.TreeItemListener;
import com.nokia.traceviewer.dialog.treeitem.TriggerTreeItem;
import com.nokia.traceviewer.dialog.treeitem.TriggerTreeTextItem;
import com.nokia.traceviewer.engine.TraceViewerGlobals;

/**
 * Trigger Property Dialog class
 * 
 */
public class TriggerPropertyDialog extends BasePropertyDialog {

	/**
	 * Text to show when Text rule is selected
	 */
	private static final String TEXT_RULE_INFORMATION_TEXT = Messages
			.getString("TriggerPropertyDialog.TextRuleInfo"); //$NON-NLS-1$

	/**
	 * Text to show when Activation rule is selected
	 */
	private static final String ACTIVATION_RULE_INFORMATION_TEXT = Messages
			.getString("TriggerPropertyDialog.ActivationRuleInfo"); //$NON-NLS-1$

	/**
	 * Stop trigger text
	 */
	private static final String STOP_TRIGGER_TEXT = Messages
			.getString("TriggerPropertyDialog.StopTrigger"); //$NON-NLS-1$

	/**
	 * Start trigger text
	 */
	private static final String START_TRIGGER_TEXT = Messages
			.getString("TriggerPropertyDialog.StartTrigger"); //$NON-NLS-1$

	/**
	 * Activation trigger text
	 */
	private static final String ACTIVATION_TRIGGER_TEXT = Messages
			.getString("TriggerPropertyDialog.ActivationTrigger"); //$NON-NLS-1$

	/**
	 * Activation rule index in the type array
	 */
	private static final int ACTIVATION_RULE_INDEX = 2;

	/**
	 * Old item when editing
	 */
	private TriggerTreeItem oldItem;

	/**
	 * Selected item in the tree
	 */
	private final TriggerTreeItem selectedItem;

	/**
	 * Combo box for trigger type
	 */
	private Combo typeCombo;

	/**
	 * Configuration file label
	 */
	private Label configurationFileLabel;

	/**
	 * Activation configuration text field
	 */
	private Text activationConfText;

	/**
	 * Load activation configuration button
	 */
	private Button loadActivationConfButton;

	/**
	 * Configuration file path
	 */
	private String configurationFilePath;

	/**
	 * Configuration name
	 */
	private String configurationName;

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
	public TriggerPropertyDialog(Shell parent, TriggerTreeItem newItem,
			TreeItemListener listener, Object selected) {
		super(parent, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
		this.newItem = newItem;
		this.selectedItem = (TriggerTreeItem) selected;
		this.listener = listener;

		// If edit, save the old item to insert info to fields
		if (newItem != null) {
			oldItem = newItem;
			textFieldTouched = true;
			nameFieldTouched = true;

			if (oldItem instanceof TriggerTreeTextItem) {
				configurationFilePath = ((TriggerTreeTextItem) oldItem)
						.getConfigurationFilePath();
				configurationName = ((TriggerTreeTextItem) oldItem)
						.getConfigurationName();
			}
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
				Messages.getString("TriggerPropertyDialog.TriggerDialogTitle")); //$NON-NLS-1$
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
		// Dispose changeable elements
		if (rightUnderComposite != null) {
			disposeChangeableElements();
		} else {
			// Right under composite
			GridLayout rightUnderCompositeGridLayout = new GridLayout();
			rightUnderCompositeGridLayout.marginHeight = 14;
			rightUnderCompositeGridLayout.numColumns = 3;
			GridData rightUnderCompositeGridData = new GridData();
			rightUnderCompositeGridData.heightHint = 114;
			rightUnderCompositeGridData.verticalAlignment = GridData.BEGINNING;
			rightUnderCompositeGridData.horizontalAlignment = SWT.FILL;
			rightUnderComposite = new Composite(mainComposite, SWT.BORDER);
			rightUnderComposite.setLayoutData(rightUnderCompositeGridData);
			rightUnderComposite.setLayout(rightUnderCompositeGridLayout);

			// Text label
			textLabel = new Label(rightUnderComposite, SWT.NONE);
			textLabel.setText(Messages
					.getString("TriggerPropertyDialog.TextString")); //$NON-NLS-1$

			// Text text
			GridData textTextGridData = new GridData();
			textTextGridData.horizontalIndent = 28;
			textTextGridData.horizontalAlignment = SWT.FILL;
			textTextGridData.horizontalSpan = 2;
			textTextGridData.grabExcessHorizontalSpace = true;
			textText = new Text(rightUnderComposite, SWT.BORDER);
			textText.setLayoutData(textTextGridData);
			if (oldItem instanceof TriggerTreeTextItem) {
				TriggerTreeTextItem item = (TriggerTreeTextItem) oldItem;
				textText.setText(item.getText());
			}

			// Spacer
			spacer = new Label(rightUnderComposite, SWT.NONE);

			// Match case checkbox
			GridData matchCaseCheckBoxGridData = new GridData();
			matchCaseCheckBoxGridData.horizontalIndent = 28;
			matchCaseCheckBoxGridData.horizontalSpan = 2;
			matchCaseCheckBox = new Button(rightUnderComposite, SWT.CHECK);
			matchCaseCheckBox.setText(Messages
					.getString("TriggerPropertyDialog.MatchCaseString")); //$NON-NLS-1$
			matchCaseCheckBox.setLayoutData(matchCaseCheckBoxGridData);

			// Set match case checkbox value
			if (oldItem instanceof TriggerTreeTextItem) {
				TriggerTreeTextItem item = (TriggerTreeTextItem) oldItem;
				matchCaseCheckBox.setSelection(item.isMatchCase());
			}

			// Type label
			Label typeLabel = new Label(rightUnderComposite, SWT.NONE);
			typeLabel.setText(Messages
					.getString("TriggerPropertyDialog.TypeString")); //$NON-NLS-1$
			createTypeCombo();
		}

		// Activation trigger type
		if (typeCombo.getSelectionIndex() == ACTIVATION_RULE_INDEX) {
			// Add new UI component, create action listeners for opening a file
			// Configuration file label
			configurationFileLabel = new Label(rightUnderComposite, SWT.NONE);
			configurationFileLabel.setText(Messages
					.getString("TriggerPropertyDialog.ConfFileString")); //$NON-NLS-1$

			GridData activationConfGridData = new GridData();
			activationConfGridData.horizontalIndent = 28;
			activationConfGridData.horizontalAlignment = SWT.FILL;
			activationConfGridData.grabExcessHorizontalSpace = true;
			activationConfText = new Text(rightUnderComposite, SWT.BORDER);
			activationConfText.setEditable(false);
			activationConfText.setLayoutData(activationConfGridData);
			if (oldItem instanceof TriggerTreeTextItem) {
				TriggerTreeTextItem item = (TriggerTreeTextItem) oldItem;
				activationConfText.setText(item.getConfigurationName());
			}

			GridData loadActivationConfButtonGridData = new GridData();
			loadActivationConfButtonGridData.heightHint = 25;
			loadActivationConfButtonGridData.widthHint = 30;
			loadActivationConfButton = new Button(rightUnderComposite, SWT.PUSH);
			loadActivationConfButton.setText("..."); //$NON-NLS-1$
			loadActivationConfButton
					.setLayoutData(loadActivationConfButtonGridData);
			loadActivationConfButton
					.addSelectionListener(new SelectionAdapter() {
						@Override
						public void widgetSelected(SelectionEvent e) {

							// Open "Load configuration" dialog
							TraceConfigurationLoadDialog dialog = new TraceConfigurationLoadDialog(
									false, configurationFilePath);
							dialog.open();

							configurationFilePath = dialog
									.getLoadedConfigurationFilePath();
							configurationName = dialog
									.getLoadedConfigurationName();
							activationConfText.setText(configurationName);
						}
					});

		} else {
			// Remove extra UI component
			if (configurationFileLabel != null) {
				configurationFileLabel.dispose();
			}
			if (activationConfText != null) {
				activationConfText.dispose();
			}
			if (loadActivationConfButton != null) {
				loadActivationConfButton.dispose();
			}
		}

		// Layout again
		rightUnderComposite.layout();
	}

	/**
	 * This method initializes typeCombo
	 * 
	 */
	private void createTypeCombo() {
		GridData typeComboGridData = new GridData();
		typeComboGridData.horizontalIndent = 28;
		typeComboGridData.horizontalAlignment = SWT.FILL;
		typeComboGridData.horizontalSpan = 2;
		typeComboGridData.grabExcessHorizontalSpace = true;
		typeCombo = new Combo(rightUnderComposite, SWT.READ_ONLY);
		typeCombo.setLayoutData(typeComboGridData);

		// Keep in this order
		typeCombo.add(START_TRIGGER_TEXT);
		typeCombo.add(STOP_TRIGGER_TEXT);
		typeCombo.add(ACTIVATION_TRIGGER_TEXT);

		// Set match case checkbox value
		if (oldItem instanceof TriggerTreeTextItem) {
			TriggerTreeTextItem item = (TriggerTreeTextItem) oldItem;
			typeCombo.setText(typeCombo.getItem(item.getType().ordinal()));
			matchCaseCheckBox.setSelection(item.isMatchCase());
		} else {
			typeCombo.setText(typeCombo.getItem(0));
		}
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
				.getString("TriggerPropertyDialog.RuleNameString")); //$NON-NLS-1$
		// Set data from old item
		if (oldItem != null) {
			nameText.setText(oldItem.getName());
			if (oldItem.getRule() == TriggerTreeItem.Rule.TEXT_RULE) {
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
		if (oldItem instanceof TriggerTreeTextItem) {
			TriggerTreeTextItem item = (TriggerTreeTextItem) oldItem;
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
			if (oldItem.getRule() == TriggerTreeItem.Rule.TEXT_RULE) {
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
		TriggerTreeItem parent = selectedItem;
		if (oldItem != null) {
			pos = getOldPosition();
			parent = (TriggerTreeItem) oldItem.getParent();
			parent.removeChild(oldItem);
		}

		// Create new TriggerTreeTextItem
		if (ruleList.getSelectionIndex() == TEXT_RULE_INDEX) {
			newItem = new TriggerTreeTextItem(
					listener,
					parent,
					nameText.getText(),
					TriggerTreeItem.Rule.TEXT_RULE,
					textText.getText(),
					matchCaseCheckBox.getSelection(),
					TriggerTreeItem.Type.values()[typeCombo.getSelectionIndex()],
					configurationFilePath, configurationName);

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

		// Add selection listener to Trigger type combobox
		typeCombo.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent event) {
				// If type changes, we might need more or less UI components
				createRightUnderComposite();
				setInformationLabel();
			}
		});
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

			// Activation configuration
			if (typeCombo.getSelectionIndex() == ACTIVATION_RULE_INDEX) {
				informationLabel.setText(TEXT_RULE_INFORMATION_TEXT
						+ ACTIVATION_RULE_INFORMATION_TEXT);
			}

		}
	}

	/**
	 * Gets old position of this item
	 * 
	 * @return old position
	 */
	protected int getOldPosition() {
		TriggerTreeItem item = (TriggerTreeItem) oldItem.getParent();
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

	/**
	 * Disposes changeable elements if they exist. Is called when selection is
	 * changed from the trigger type list and some ui components are changed
	 */
	private void disposeChangeableElements() {

	}
}
