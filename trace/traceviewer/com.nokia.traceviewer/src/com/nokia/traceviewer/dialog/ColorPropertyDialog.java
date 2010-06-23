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
 * Color Property Dialog class
 *
 */
package com.nokia.traceviewer.dialog;

import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.nokia.traceviewer.dialog.treeitem.ColorTreeComponentItem;
import com.nokia.traceviewer.dialog.treeitem.ColorTreeItem;
import com.nokia.traceviewer.dialog.treeitem.ColorTreeTextItem;
import com.nokia.traceviewer.dialog.treeitem.TreeItemListener;
import com.nokia.traceviewer.engine.TraceViewerGlobals;
import com.nokia.traceviewer.engine.activation.TraceActivationComponentItem;
import com.nokia.traceviewer.engine.activation.TraceActivationGroupItem;

/**
 * Color Property Dialog class
 * 
 */
public class ColorPropertyDialog extends BasePropertyDialog {

	/**
	 * Text to show when Text rule is selected
	 */
	private static final String TEXT_RULE_INFORMATION_TEXT = Messages
			.getString("ColorPropertyDialog.TextRuleInfo"); //$NON-NLS-1$

	/**
	 * Text to show when Component rule is selected
	 */
	private static final String COMPONENT_RULE_INFORMATION_TEXT = Messages
			.getString("ColorPropertyDialog.ComponentRuleInfo1") //$NON-NLS-1$
			+ " " //$NON-NLS-1$
			+ WILDCARD_STRING + " " //$NON-NLS-1$
			+ Messages.getString("ColorPropertyDialog.ComponentRuleInfo2"); //$NON-NLS-1$

	/**
	 * Old item used when editing
	 */
	protected ColorTreeItem oldItem;

	/**
	 * Selected item in tree
	 */
	private final ColorTreeItem selectedItem;

	/**
	 * UI Label for foreground color
	 */
	private Label foregroundColorShowLabel;

	/**
	 * UI Label for background color
	 */
	private Label backGroundColorShowLabel;

	/**
	 * Button to change foreground color
	 */
	private Button foreGroundColorButton;

	/**
	 * Button to change background color
	 */
	private Button backGroundColorButton;

	/**
	 * Foreground color
	 */
	private Color foregroundColor;

	/**
	 * Background color
	 */
	private Color backgroundColor;

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
	public ColorPropertyDialog(Shell parent, ColorTreeItem newItem,
			TreeItemListener listener, Object selected) {
		super(parent, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
		this.newItem = newItem;
		this.selectedItem = (ColorTreeItem) selected;
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
				Messages.getString("ColorPropertyDialog.ColorDefinition")); //$NON-NLS-1$
		createMainComposite();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.nokia.traceviewer.dialog.BasePropertyDialog#createMainComposite()
	 */
	@Override
	protected void createMainComposite() {
		// Main composite
		GridLayout mainCompositeGridLayout = new GridLayout();
		mainCompositeGridLayout.horizontalSpacing = 4;
		mainCompositeGridLayout.verticalSpacing = 4;
		mainCompositeGridLayout.marginWidth = 0;
		mainCompositeGridLayout.marginHeight = 0;
		mainCompositeGridLayout.numColumns = 2;
		GridData mainCompositeGridData = new GridData();
		mainComposite = new Composite(composite, SWT.NONE);
		mainComposite.setLayoutData(mainCompositeGridData);
		mainComposite.setLayout(mainCompositeGridLayout);
		createUpperComposite();
		createLeftUnderComposite();
		createRightUnderComposite();
		createRightReallyUnderComposite();

		nameText.setFocus();
	}

	/**
	 * This method initializes upperComposite
	 * 
	 */
	private void createUpperComposite() {
		// Upper composite
		GridLayout upperCompositeGridLayout = new GridLayout();
		upperCompositeGridLayout.numColumns = 7;
		GridData upperCompositeGridData = new GridData();
		upperCompositeGridData.horizontalSpan = 2;
		upperCompositeGridData.grabExcessHorizontalSpace = true;
		upperCompositeGridData.horizontalAlignment = SWT.FILL;
		upperComposite = new Composite(mainComposite, SWT.NONE);
		upperComposite.setLayout(upperCompositeGridLayout);
		upperComposite.setLayoutData(upperCompositeGridData);

		// Name label
		GridData nameLabelGridData = new GridData();
		nameLabelGridData.horizontalIndent = 5;
		nameLabel = new Label(upperComposite, SWT.NONE);
		nameLabel.setText(Messages
				.getString("ColorPropertyDialog.ColorCodingName")); //$NON-NLS-1$
		nameLabel.setLayoutData(nameLabelGridData);

		// Name text field
		GridData nameTextGridData = new GridData();
		nameTextGridData.grabExcessHorizontalSpace = true;
		nameTextGridData.horizontalAlignment = SWT.FILL;
		nameTextGridData.horizontalIndent = 10;
		nameTextGridData.horizontalSpan = 6;
		nameTextGridData.verticalAlignment = GridData.CENTER;
		nameTextGridData.widthHint = 230;
		nameText = new Text(upperComposite, SWT.BORDER);
		nameText.setLayoutData(nameTextGridData);
		nameText.setToolTipText(Messages
				.getString("ColorPropertyDialog.NameFieldToolTip")); //$NON-NLS-1$

		// Rule label
		GridData ruleLabelGridData = new GridData();
		ruleLabelGridData.horizontalIndent = 5;
		ruleLabel = new Label(upperComposite, SWT.NONE);
		ruleLabel.setText(Messages
				.getString("ColorPropertyDialog.SelectedRule")); //$NON-NLS-1$
		ruleLabel.setLayoutData(ruleLabelGridData);

		// Selected rule label
		GridData selectedRuleLabelGridData = new GridData();
		selectedRuleLabelGridData.widthHint = 150;
		selectedRuleLabelGridData.horizontalIndent = 10;
		selectedRuleLabelGridData.horizontalSpan = 6;
		selectedRuleLabelGridData.grabExcessHorizontalSpace = true;
		selectedRuleLabelGridData.horizontalAlignment = SWT.FILL;
		selectedRuleLabel = new Label(upperComposite, SWT.NONE);
		selectedRuleLabel.setLayoutData(selectedRuleLabelGridData);
		selectedRuleLabel.setToolTipText(Messages
				.getString("ColorPropertyDialog.RuleLabelToolTip")); //$NON-NLS-1$

		// Fore color label
		GridData foreColorLabelGridData = new GridData();
		foreColorLabelGridData.horizontalIndent = 5;
		Label foreColorLabel = new Label(upperComposite, SWT.NONE);
		foreColorLabel.setText(Messages
				.getString("ColorPropertyDialog.ForegroundColor")); //$NON-NLS-1$
		foreColorLabel.setLayoutData(foreColorLabelGridData);

		// Foreground color show label
		GridData foregroundColorShowLabelGridData = new GridData();
		foregroundColorShowLabelGridData.heightHint = 19;
		foregroundColorShowLabelGridData.horizontalIndent = 10;
		foregroundColorShowLabelGridData.widthHint = 33;
		foregroundColorShowLabel = new Label(upperComposite, SWT.BORDER);
		foregroundColorShowLabel.setText(""); //$NON-NLS-1$
		foregroundColorShowLabel
				.setLayoutData(foregroundColorShowLabelGridData);

		// Foreground color button
		GridData foreGroundColorButtonGridData = new GridData();
		foreGroundColorButtonGridData.heightHint = 25;
		foreGroundColorButtonGridData.widthHint = 28;
		foreGroundColorButton = new Button(upperComposite, SWT.NONE);
		foreGroundColorButton.setText("..."); //$NON-NLS-1$
		foreGroundColorButton.setLayoutData(foreGroundColorButtonGridData);

		// Background label
		GridData backGroundLabelGridData = new GridData();
		backGroundLabelGridData.horizontalIndent = 10;
		Label backGroundLabel = new Label(upperComposite, SWT.NONE);
		backGroundLabel.setText(Messages
				.getString("ColorPropertyDialog.BackgroundColor")); //$NON-NLS-1$
		backGroundLabel.setLayoutData(backGroundLabelGridData);

		// Background color show label
		GridData backGroundColorShowLabelGridData = new GridData();
		backGroundColorShowLabelGridData.heightHint = 19;
		backGroundColorShowLabelGridData.widthHint = 33;
		backGroundColorShowLabel = new Label(upperComposite, SWT.BORDER);
		backGroundColorShowLabel.setText(""); //$NON-NLS-1$
		backGroundColorShowLabel
				.setLayoutData(backGroundColorShowLabelGridData);

		// Background color button
		GridData backGroundColorButtonGridData = new GridData();
		backGroundColorButtonGridData.heightHint = 25;
		backGroundColorButtonGridData.widthHint = 28;
		backGroundColorButton = new Button(upperComposite, SWT.NONE);
		backGroundColorButton.setText("..."); //$NON-NLS-1$
		backGroundColorButton.setLayoutData(backGroundColorButtonGridData);

		// Spacer labels
		new Label(upperComposite, SWT.NONE);
		new Label(upperComposite, SWT.NONE);

		// Insert data to fields
		insertDataToUpperComposite();
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

			// Set colors from old item
			foregroundColorShowLabel
					.setBackground(oldItem.getForegroundColor());
			backGroundColorShowLabel
					.setBackground(oldItem.getBackgroundColor());

			// Set selected rule label value
			if (oldItem.getRule() == ColorTreeItem.Rule.TEXT_RULE) {
				selectedRuleLabel.setText(TEXT_RULE_NAME);
			} else if (oldItem.getRule() == ColorTreeItem.Rule.COMPONENT_RULE) {
				selectedRuleLabel.setText(COMPONENT_RULE_NAME);
			}
			// Old item doesn't exist, insert defaults
		} else {
			selectedRuleLabel.setText(TEXT_RULE_NAME);
			foregroundColorShowLabel.setBackground(getShell().getDisplay()
					.getSystemColor(SWT.COLOR_BLUE));
			backGroundColorShowLabel.setBackground(getShell().getDisplay()
					.getSystemColor(SWT.COLOR_WHITE));
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
			if (oldItem.getRule() == ColorTreeItem.Rule.TEXT_RULE) {
				ruleList.setSelection(TEXT_RULE_INDEX);
			} else if (oldItem.getRule() == ColorTreeItem.Rule.COMPONENT_RULE) {
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
		if (oldItem instanceof ColorTreeTextItem) {
			ColorTreeTextItem item = (ColorTreeTextItem) oldItem;
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
		if (oldItem instanceof ColorTreeComponentItem) {
			ColorTreeComponentItem item = (ColorTreeComponentItem) oldItem;
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
		ColorTreeItem parent = selectedItem;
		if (oldItem != null) {
			pos = getOldPosition();
			parent = (ColorTreeItem) oldItem.getParent();
			parent.removeChild(oldItem);
		}

		// Create new ColorTreeTextItem
		if (ruleList.getSelectionIndex() == TEXT_RULE_INDEX) {
			newItem = new ColorTreeTextItem(listener, parent, nameText
					.getText(), ColorTreeItem.Rule.TEXT_RULE,
					foregroundColorShowLabel.getBackground(),
					backGroundColorShowLabel.getBackground(), textText
							.getText(), matchCaseCheckBox.getSelection());

			// Create new ColorTreeComponentItem
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
			newItem = new ColorTreeComponentItem(listener, parent, nameText
					.getText(), ColorTreeItem.Rule.COMPONENT_RULE,
					foregroundColorShowLabel.getBackground(),
					backGroundColorShowLabel.getBackground(), componentId,
					groupId);
		} else {
			// No other rules defined yet
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
	 * com.nokia.traceviewer.dialog.BasePropertyDialog#createActionListeners()
	 */
	@Override
	protected void createActionListeners() {
		super.createActionListeners();

		// Add selection listener to foreground color selection button
		foreGroundColorButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				TraceViewerGlobals.postUiEvent("ChangeForegroundColorButton", //$NON-NLS-1$
						"1"); //$NON-NLS-1$

				// Create the color-change dialog
				org.eclipse.swt.widgets.ColorDialog dlg = new org.eclipse.swt.widgets.ColorDialog(
						getShell());
				dlg.setRGB(foregroundColorShowLabel.getBackground().getRGB());

				// Change the title bar text
				String chooseForeColor = Messages
						.getString("ColorPropertyDialog.ChooseForegroundColor"); //$NON-NLS-1$
				dlg.setText(chooseForeColor);

				// Open the dialog and retrieve the selected color
				RGB rgb = dlg.open();
				if (rgb != null) {
					// Dispose the old color, create the
					// new one, and set into the label

					if (foregroundColor != null) {
						foregroundColor.dispose();
					}
					foregroundColor = new Color(getShell().getDisplay(), rgb);
					foregroundColorShowLabel.setBackground(foregroundColor);
				}
				TraceViewerGlobals.postUiEvent("ChangeForegroundColorButton", //$NON-NLS-1$
						"0"); //$NON-NLS-1$
			}
		});

		// Add selection listener to background color selection button
		backGroundColorButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				TraceViewerGlobals.postUiEvent("ChangeBackgroundColorButton", //$NON-NLS-1$
						"1"); //$NON-NLS-1$
				// Create the color-change dialog
				org.eclipse.swt.widgets.ColorDialog dlg = new org.eclipse.swt.widgets.ColorDialog(
						getShell());
				dlg.setRGB(foregroundColorShowLabel.getBackground().getRGB());

				// Change the title bar text
				String chooseBackColor = Messages
						.getString("ColorPropertyDialog.ChooseBackgroundColor"); //$NON-NLS-1$
				dlg.setText(chooseBackColor);

				// Open the dialog and retrieve the selected color
				RGB rgb = dlg.open();
				if (rgb != null) {
					// Dispose the old color, create the
					// new one, and set into the label

					if (backgroundColor != null) {
						backgroundColor.dispose();
					}
					backgroundColor = new Color(getShell().getDisplay(), rgb);
					backGroundColorShowLabel.setBackground(backgroundColor);
				}
				TraceViewerGlobals.postUiEvent("ChangeBackgroundColorButton", //$NON-NLS-1$
						"0"); //$NON-NLS-1$
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
		ColorTreeItem item = (ColorTreeItem) oldItem.getParent();
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
