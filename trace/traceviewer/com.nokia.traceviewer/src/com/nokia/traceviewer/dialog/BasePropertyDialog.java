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
 * Base class for property dialogs
 *
 */
package com.nokia.traceviewer.dialog;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.nokia.traceviewer.dialog.treeitem.TreeItem;
import com.nokia.traceviewer.engine.TraceViewerGlobals;

/**
 * Base class for property dialogs
 * 
 */
public abstract class BasePropertyDialog extends BaseDialog {

	/**
	 * Index of Text Rule in Rules list
	 */
	protected static final int TEXT_RULE_INDEX = 0;

	/**
	 * Index of Component Rule in Rules list
	 */
	protected static final int COMPONENT_RULE_INDEX = 1;

	/**
	 * Wildcard indicating any component / group ID
	 */
	protected static final String WILDCARD_STRING = "*"; //$NON-NLS-1$

	/**
	 * Hex prefix
	 */
	protected static final String HEX_PREFIX = "0x"; //$NON-NLS-1$

	/**
	 * Hex radix
	 */
	protected static final int HEX_RADIX = 16;

	/**
	 * Wildcard integer value indicating any component / group ID
	 */
	public static final int WILDCARD_INTEGER = -1;

	/**
	 * Text rule name
	 */
	protected static final String TEXT_RULE_NAME = Messages
			.getString("BasePropertyDialog.TextRuleName"); //$NON-NLS-1$

	/**
	 * Component rule name
	 */
	protected static final String COMPONENT_RULE_NAME = Messages
			.getString("BasePropertyDialog.ComponentRuleName"); //$NON-NLS-1$

	/**
	 * Main UI Composite
	 */
	protected Composite mainComposite;

	/**
	 * Upper UI Composite
	 */
	protected Composite upperComposite;

	/**
	 * Right under UI Composite
	 */
	protected Composite rightUnderComposite;

	/**
	 * List of available rules
	 */
	protected List ruleList;

	/**
	 * UI Label for name
	 */
	protected Label nameLabel;

	/**
	 * UI Label for rule
	 */
	protected Label ruleLabel;

	/**
	 * UI Label for selected rule
	 */
	protected Label selectedRuleLabel;

	/**
	 * UI Label for information
	 */
	protected Label informationLabel;

	/**
	 * Text field for the name of the rule
	 */
	protected Text nameText;

	/**
	 * Button to change component ID
	 */
	protected Button changeComponentButton;

	/**
	 * Button to change group ID
	 */
	protected Button changeGroupButton;

	/**
	 * Component / Group selection dialog
	 */
	protected ComponentSelectionDialog selDialog;

	/**
	 * Text field for text of the rule when text rule is selected
	 */
	protected Text textText;

	/**
	 * Text field for component ID when component rule is selected
	 */
	protected Text componentTextField;

	/**
	 * UI Label for component name
	 */
	protected Label componentNameLabel;

	/**
	 * Text field for group ID when component rule is selected
	 */
	protected Text groupTextField;

	/**
	 * UI Label for group name
	 */
	protected Label groupNameLabel;

	/**
	 * UI Label for component ID
	 */
	protected Label componentLabel;

	/**
	 * UI Label for group ID
	 */
	protected Label groupLabel;

	/**
	 * UI Label for text
	 */
	protected Label textLabel;

	/**
	 * Matchcase checkbox
	 */
	protected Button matchCaseCheckBox;

	/**
	 * Always selected checkbox
	 */
	protected Button alwaysEnabledCheckBox;

	/**
	 * Spacer label
	 */
	protected Label spacer;

	/**
	 * Indicates if user has touched text field. Used when replicating user
	 * input to another field
	 */
	protected boolean textFieldTouched;

	/**
	 * Indicates if user has touched name field. Used when replicating user
	 * input to another field
	 */
	protected boolean nameFieldTouched;

	/**
	 * New item to be created in this dialog
	 */
	protected TreeItem newItem;

	/**
	 * Constructor
	 * 
	 * @param parent
	 *            parent shell
	 * @param style
	 *            style bits
	 */
	public BasePropertyDialog(Shell parent, int style) {
		super(parent, style);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.traceviewer.dialog.BaseDialog#createActionListeners()
	 */
	@Override
	protected void createActionListeners() {
		// Add selection listener to name field
		nameText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				// Text rule selected
				if (isTextRuleSelected()) {

					// Set the name text to follow text field
					if (!textFieldTouched) {
						textText.setText(nameText.getText());
					}

					if (isTextComponentsFilled()) {
						getButton(IDialogConstants.OK_ID).setEnabled(true);
					} else {
						getButton(IDialogConstants.OK_ID).setEnabled(false);
					}

					// Component rule selected
				} else if (isComponentRuleSelected()) {
					if (isComponentComponentsFilled()) {
						getButton(IDialogConstants.OK_ID).setEnabled(true);
					} else {
						getButton(IDialogConstants.OK_ID).setEnabled(false);
					}
				}
			}
		});

		// Add key listener to name field
		nameText.addKeyListener(new KeyListener() {
			public void keyPressed(KeyEvent e) {
				nameFieldTouched = true;
			}

			public void keyReleased(KeyEvent e) {
			}
		});

		// Add selection listener to rule list
		ruleList.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				TraceViewerGlobals.postUiEvent("RuleListSelection" //$NON-NLS-1$
						+ ruleList.getSelectionIndex(), "1"); //$NON-NLS-1$
				setInformationLabel();
				createRightUnderComposite();
				TraceViewerGlobals.postUiEvent("RuleListSelection" //$NON-NLS-1$
						+ ruleList.getSelectionIndex(), "0"); //$NON-NLS-1$
			}
		});
	}

	/**
	 * Creates action listener when text rule is selected
	 */
	protected void createActionListenersForTextRule() {
		// Add selection listeners when text rule is selected
		if (isTextRuleSelected()) {
			textText.addModifyListener(new ModifyListener() {
				public void modifyText(ModifyEvent e) {
					if (isTextRuleSelected()) {

						// Set the name text to follow text field
						if (!nameFieldTouched) {
							nameText.setText(textText.getText());
						}

						if (isTextComponentsFilled()) {
							getButton(IDialogConstants.OK_ID).setEnabled(true);
						} else {
							getButton(IDialogConstants.OK_ID).setEnabled(false);
						}
					}
				}
			});

			// Add key listener to text field
			textText.addKeyListener(new KeyListener() {
				public void keyPressed(KeyEvent e) {
					textFieldTouched = true;
				}

				public void keyReleased(KeyEvent e) {
				}
			});
		}
	}

	/**
	 * Creates action listeners when component rule is selected
	 */
	protected void createActionListenersForComponentRule() {
		// Add selection listeners when component rule is selected
		if (isComponentRuleSelected()) {
			// Component text field
			componentTextField.addModifyListener(new ModifyListener() {
				public void modifyText(ModifyEvent e) {
					if (isComponentRuleSelected()) {
						if (isComponentComponentsFilled()) {
							getButton(IDialogConstants.OK_ID).setEnabled(true);
						} else {
							getButton(IDialogConstants.OK_ID).setEnabled(false);
						}
					}
				}
			});

			// Group text field
			groupTextField.addModifyListener(new ModifyListener() {
				public void modifyText(ModifyEvent e) {
					if (isComponentRuleSelected()) {
						if (isComponentComponentsFilled()) {
							getButton(IDialogConstants.OK_ID).setEnabled(true);
						} else {
							getButton(IDialogConstants.OK_ID).setEnabled(false);
						}
					}
				}
			});
			// Add selection listener to change component button
			changeComponentButton.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent event) {
					TraceViewerGlobals
							.postUiEvent("ChangeComponentButton", "1"); //$NON-NLS-1$ //$NON-NLS-2$
					if (selDialog == null) {
						selDialog = new ComponentSelectionDialog();
					}
					selDialog.setDialogToShowComponents(true, 0);
					selDialog.open();
					String componentId = selDialog.getSelectedItemId();
					if (componentId != null && !componentId.equals("")) { //$NON-NLS-1$
						componentTextField.setText(componentId);
						String componentName = selDialog.getSelectedItemName();
						componentNameLabel.setText(componentName);
					}
					TraceViewerGlobals
							.postUiEvent("ChangeComponentButton", "0"); //$NON-NLS-1$ //$NON-NLS-2$
				}
			});

			// Add selection listener to change group button
			changeGroupButton.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent event) {
					TraceViewerGlobals.postUiEvent("ChangeGroupButton", "1"); //$NON-NLS-1$ //$NON-NLS-2$
					if (selDialog == null) {
						selDialog = new ComponentSelectionDialog();
					}
					int componentId = NOT_DEFINED;
					try {
						String cid = componentTextField.getText();
						if (cid.length() > HEX_PREFIX.length()
								&& cid.substring(0, HEX_PREFIX.length())
										.equals(HEX_PREFIX)) {
							cid = cid.substring(HEX_PREFIX.length());
						}
						componentId = Integer.parseInt(cid, HEX_RADIX);
					} catch (NumberFormatException e) {
					}
					selDialog.setDialogToShowComponents(false, componentId);
					selDialog.open();
					String groupId = selDialog.getSelectedItemId();
					if (!groupId.equals("")) { //$NON-NLS-1$
						groupTextField.setText(groupId);
						String groupName = selDialog.getSelectedItemName();
						groupNameLabel.setText(groupName);
					}
					TraceViewerGlobals.postUiEvent("ChangeGroupButton", "0"); //$NON-NLS-1$ //$NON-NLS-2$
				}
			});

		}
	}

	/**
	 * Tells that text rule is selected from the view
	 * 
	 * @return true if text rule is selected from the view
	 */
	private boolean isTextRuleSelected() {
		boolean textRuleSelected = false;
		if (textText != null && !textText.isDisposed()) {
			textRuleSelected = true;
		}
		return textRuleSelected;
	}

	/**
	 * Tells that component rule is selected from the view
	 * 
	 * @return true if component rule is selected from the view
	 */
	private boolean isComponentRuleSelected() {
		boolean componentRuleSelected = false;
		if (componentTextField != null && !componentTextField.isDisposed()
				&& groupTextField != null && !groupTextField.isDisposed()) {
			componentRuleSelected = true;
		}
		return componentRuleSelected;
	}

	/**
	 * Checks if all needed components are filled when text rule is selected
	 * 
	 * @return true if all needed components are filled
	 */
	private boolean isTextComponentsFilled() {
		boolean componentsFilled = false;
		if (textText.getText().length() > 0 && nameText.getText().length() > 0) {
			componentsFilled = true;
		}
		return componentsFilled;
	}

	/**
	 * Checks if all needed components are filled when component rule is
	 * selected
	 * 
	 * @return true if all needed components are filled
	 */
	private boolean isComponentComponentsFilled() {
		boolean componentsFilled = false;
		if (componentTextField.getText().length() > 0
				&& groupTextField.getText().length() > 0
				&& nameText.getText().length() > 0) {
			componentsFilled = true;
		}
		return componentsFilled;
	}

	/**
	 * Disposes changeable elements if they exist. Is called when selection is
	 * changed from the selection list and components are changed
	 */
	private void disposeChangeableElements() {
		if (textLabel != null) {
			textLabel.dispose();
		}
		if (textText != null) {
			textText.dispose();
		}
		if (matchCaseCheckBox != null) {
			matchCaseCheckBox.dispose();
		}
		if (spacer != null) {
			spacer.dispose();
		}
		if (componentLabel != null) {
			componentLabel.dispose();
		}
		if (componentTextField != null) {
			componentTextField.dispose();
		}
		if (groupLabel != null) {
			groupLabel.dispose();
		}
		if (groupTextField != null) {
			groupTextField.dispose();
		}
		if (changeComponentButton != null) {
			changeComponentButton.dispose();
		}
		if (changeGroupButton != null) {
			changeGroupButton.dispose();
		}
		if (componentNameLabel != null) {
			componentNameLabel.dispose();
		}
		if (groupNameLabel != null) {
			groupNameLabel.dispose();
		}
	}

	/**
	 * This method creates mainComposite
	 */
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
	 */
	private void createUpperComposite() {
		// Upper composite
		GridData upperCompositeGridData = new GridData();
		upperCompositeGridData.horizontalSpan = 2;
		upperCompositeGridData.grabExcessHorizontalSpace = true;
		upperCompositeGridData.horizontalAlignment = SWT.FILL;
		GridLayout upperCompositeGridLayout = new GridLayout();
		upperCompositeGridLayout.numColumns = 3;
		upperComposite = new Composite(mainComposite, SWT.NONE);
		upperComposite.setLayout(upperCompositeGridLayout);
		upperComposite.setLayoutData(upperCompositeGridData);

		// Name label
		GridData nameLabelGridData = new GridData();
		nameLabelGridData.horizontalIndent = 5;
		nameLabel = new Label(upperComposite, SWT.NONE);
		nameLabel
				.setText(Messages.getString("BasePropertyDialog.RuleNameText")); //$NON-NLS-1$
		nameLabel.setLayoutData(nameLabelGridData);

		// Name text
		GridData nameTextGridData = new GridData();
		nameTextGridData.grabExcessHorizontalSpace = true;
		nameTextGridData.horizontalAlignment = SWT.FILL;
		nameTextGridData.horizontalIndent = 30;
		nameTextGridData.horizontalSpan = 2;
		nameTextGridData.widthHint = 235;
		nameText = new Text(upperComposite, SWT.BORDER);
		nameText.setLayoutData(nameTextGridData);
		nameText.setToolTipText(Messages
				.getString("BasePropertyDialog.NameTexToolTip")); //$NON-NLS-1$

		// Rule label
		GridData ruleLabelGridData = new GridData();
		ruleLabelGridData.horizontalIndent = 5;
		ruleLabel = new Label(upperComposite, SWT.NONE);
		ruleLabel.setText(Messages
				.getString("BasePropertyDialog.SelectedRuleText")); //$NON-NLS-1$
		ruleLabel.setLayoutData(ruleLabelGridData);

		// Selected rule label
		GridData selectedRuleLabelGridData = new GridData();
		selectedRuleLabelGridData.widthHint = 150;
		selectedRuleLabelGridData.horizontalIndent = 30;
		selectedRuleLabelGridData.horizontalSpan = 2;
		selectedRuleLabelGridData.grabExcessHorizontalSpace = true;
		selectedRuleLabelGridData.horizontalAlignment = SWT.FILL;
		selectedRuleLabel = new Label(upperComposite, SWT.NONE);
		selectedRuleLabel.setLayoutData(selectedRuleLabelGridData);
		selectedRuleLabel.setToolTipText(Messages
				.getString("BasePropertyDialog.RuleLabelToolTip")); //$NON-NLS-1$

		// Spacer label
		new Label(upperComposite, SWT.NONE);

		// Insert data to fields
		insertDataToUpperComposite();
	}

	/**
	 * This method initializes leftUnderComposite
	 */
	protected void createLeftUnderComposite() {
		// Left under composite
		GridLayout leftUnderCompositeGridLayout = new GridLayout();
		leftUnderCompositeGridLayout.verticalSpacing = 0;
		leftUnderCompositeGridLayout.marginHeight = 0;
		leftUnderCompositeGridLayout.horizontalSpacing = 0;
		leftUnderCompositeGridLayout.marginWidth = 0;
		GridData leftUnderCompositeGridData = new GridData();
		leftUnderCompositeGridData.heightHint = 190;
		leftUnderCompositeGridData.horizontalIndent = 0;
		leftUnderCompositeGridData.verticalSpan = 2;
		leftUnderCompositeGridData.widthHint = 200;
		Composite leftUnderComposite = new Composite(mainComposite, SWT.BORDER);
		leftUnderComposite.setLayoutData(leftUnderCompositeGridData);
		leftUnderComposite.setLayout(leftUnderCompositeGridLayout);
		leftUnderComposite.setBackground(getShell().getDisplay()
				.getSystemColor(SWT.COLOR_WHITE));

		// Rule list
		GridData ruleListGridData = new GridData();
		ruleListGridData.heightHint = 190;
		ruleListGridData.horizontalAlignment = GridData.CENTER;
		ruleListGridData.verticalAlignment = GridData.CENTER;
		ruleListGridData.horizontalIndent = 15;
		ruleListGridData.verticalIndent = 5;
		ruleListGridData.widthHint = 200;
		ruleList = new List(leftUnderComposite, SWT.NONE);
		ruleList.setLayoutData(ruleListGridData);
		ruleList.setBackground(getShell().getDisplay().getSystemColor(
				SWT.COLOR_WHITE));
		ruleList.setToolTipText(Messages
				.getString("BasePropertyDialog.RuleListToolTip")); //$NON-NLS-1$

		setListSelection();
	}

	/**
	 * This method initializes rightUnderComposite
	 */
	protected void createRightUnderComposite() {
		// Dispose changeable elements
		if (rightUnderComposite != null) {
			disposeChangeableElements();
		} else {
			// Create Right under composite
			GridLayout rightUnderCompositeGridLayout = new GridLayout();
			rightUnderCompositeGridLayout.marginHeight = 12;
			rightUnderCompositeGridLayout.numColumns = 3;
			GridData rightUnderCompositeGridData = new GridData();
			rightUnderCompositeGridData.heightHint = 110;
			rightUnderCompositeGridData.verticalAlignment = GridData.BEGINNING;
			rightUnderCompositeGridData.widthHint = 250;
			rightUnderCompositeGridData.horizontalAlignment = GridData.CENTER;
			rightUnderComposite = new Composite(mainComposite, SWT.BORDER);
			rightUnderComposite.setLayoutData(rightUnderCompositeGridData);
			rightUnderComposite.setLayout(rightUnderCompositeGridLayout);
		}

		// Text rule
		if (ruleList.getSelectionIndex() == TEXT_RULE_INDEX) {
			createTextRuleComposite();
			createActionListenersForTextRule();
		} else if (ruleList.getSelectionIndex() == COMPONENT_RULE_INDEX) {
			createComponentRuleComposite();
			createActionListenersForComponentRule();
		} else {
			createTextRuleComposite();
			createActionListenersForTextRule();
		}

		// Layout again
		rightUnderComposite.layout();
	}

	/**
	 * Creates text rule composite
	 */
	private void createTextRuleComposite() {
		// Text label
		textLabel = new Label(rightUnderComposite, SWT.NONE);
		textLabel.setText(Messages
				.getString("BasePropertyDialog.TextLabelText")); //$NON-NLS-1$

		// Text text
		GridData textTextGridData = new GridData();
		textTextGridData.horizontalIndent = 30;
		textTextGridData.grabExcessHorizontalSpace = true;
		textTextGridData.horizontalAlignment = SWT.FILL;
		textTextGridData.horizontalSpan = 2;
		textText = new Text(rightUnderComposite, SWT.BORDER);
		textText.setLayoutData(textTextGridData);
		textText.setToolTipText(Messages
				.getString("BasePropertyDialog.SearchTooltip")); //$NON-NLS-1$

		// Spacer label
		spacer = new Label(rightUnderComposite, SWT.NONE);

		// Match case checkbox
		GridData matchCaseCheckBoxGridData = new GridData();
		matchCaseCheckBoxGridData.horizontalIndent = 30;
		matchCaseCheckBoxGridData.horizontalSpan = 2;
		matchCaseCheckBox = new Button(rightUnderComposite, SWT.CHECK);
		matchCaseCheckBox.setText(Messages
				.getString("BasePropertyDialog.MatchCaseText")); //$NON-NLS-1$
		matchCaseCheckBox.setToolTipText(Messages
				.getString("BasePropertyDialog.MatchCaseToolTip")); //$NON-NLS-1$
		matchCaseCheckBox.setLayoutData(matchCaseCheckBoxGridData);

		// Insert data to fields
		insertDataToTextFields();
	}

	/**
	 * Create component rule composite
	 */
	private void createComponentRuleComposite() {
		// Component ID Label
		componentLabel = new Label(rightUnderComposite, SWT.NONE);
		componentLabel.setText(Messages
				.getString("BasePropertyDialog.ComponentIDText")); //$NON-NLS-1$

		// Component ID field
		GridData componentTextGridData = new GridData();
		componentTextGridData.horizontalIndent = 5;
		componentTextGridData.horizontalAlignment = SWT.FILL;
		componentTextGridData.grabExcessHorizontalSpace = true;
		componentTextField = new Text(rightUnderComposite, SWT.BORDER);
		componentTextField.setLayoutData(componentTextGridData);
		componentTextField.setToolTipText(Messages
				.getString("BasePropertyDialog.ComponentFieldToolTip")); //$NON-NLS-1$

		// Change component ID button
		GridData changeComponentButtonGridData = new GridData();
		changeComponentButtonGridData.heightHint = 25;
		changeComponentButtonGridData.widthHint = 30;
		changeComponentButton = new Button(rightUnderComposite, SWT.NONE);
		changeComponentButton.setText("..."); //$NON-NLS-1$
		changeComponentButton.setToolTipText(Messages
				.getString("BasePropertyDialog.ComponentBrowseTooltip")); //$NON-NLS-1$
		changeComponentButton.setLayoutData(changeComponentButtonGridData);

		// Component name
		GridData componentNameLabelGridData = new GridData();
		componentNameLabelGridData.horizontalSpan = 3;
		componentNameLabelGridData.horizontalIndent = 5;
		componentNameLabelGridData.widthHint = 230;
		componentNameLabel = new Label(rightUnderComposite, SWT.NONE);
		componentNameLabel.setForeground(getShell().getDisplay()
				.getSystemColor(SWT.COLOR_RED));
		componentNameLabel.setLayoutData(componentNameLabelGridData);

		// Group ID Label
		groupLabel = new Label(rightUnderComposite, SWT.NONE);
		groupLabel
				.setText(Messages.getString("BasePropertyDialog.GroupIdText")); //$NON-NLS-1$

		// Group ID field
		GridData groupTextGridData = new GridData();
		groupTextGridData.horizontalIndent = 5;
		groupTextGridData.horizontalAlignment = SWT.FILL;
		groupTextGridData.grabExcessHorizontalSpace = true;
		groupTextField = new Text(rightUnderComposite, SWT.BORDER);
		groupTextField.setLayoutData(groupTextGridData);
		groupTextField.setToolTipText(Messages
				.getString("BasePropertyDialog.GroupFieldToolTip")); //$NON-NLS-1$

		// Change group ID button
		GridData changeGroupButtonGridData = new GridData();
		changeGroupButtonGridData.heightHint = 25;
		changeGroupButtonGridData.widthHint = 30;
		changeGroupButton = new Button(rightUnderComposite, SWT.NONE);
		changeGroupButton.setText("..."); //$NON-NLS-1$
		changeGroupButton.setToolTipText(Messages
				.getString("BasePropertyDialog.GroupBrowseToolTip")); //$NON-NLS-1$
		changeGroupButton.setLayoutData(changeGroupButtonGridData);

		// Group name
		GridData groupNameLabelGridData = new GridData();
		groupNameLabelGridData.horizontalSpan = 3;
		groupNameLabelGridData.horizontalIndent = 5;
		groupNameLabelGridData.widthHint = 230;
		groupNameLabel = new Label(rightUnderComposite, SWT.NONE);
		groupNameLabel.setForeground(getShell().getDisplay().getSystemColor(
				SWT.COLOR_RED));
		groupNameLabel.setLayoutData(groupNameLabelGridData);

		// Insert data to fields
		insertDataToComponentFields();
	}

	/**
	 * This method initializes rightReallyUnderComposite
	 */
	protected void createRightReallyUnderComposite() {
		// Right really under composite
		GridLayout rightReallyUnderCompositeGridLayout = new GridLayout();
		rightReallyUnderCompositeGridLayout.verticalSpacing = 0;
		rightReallyUnderCompositeGridLayout.marginWidth = 0;
		rightReallyUnderCompositeGridLayout.horizontalSpacing = 0;
		GridData rightReallyUnderCompositeGridData = new GridData();
		rightReallyUnderCompositeGridData.heightHint = 70;
		rightReallyUnderCompositeGridData.horizontalAlignment = GridData.CENTER;
		rightReallyUnderCompositeGridData.verticalAlignment = GridData.CENTER;
		rightReallyUnderCompositeGridData.widthHint = 250;
		Composite rightReallyUnderComposite = new Composite(mainComposite,
				SWT.BORDER);
		rightReallyUnderComposite
				.setLayoutData(rightReallyUnderCompositeGridData);
		rightReallyUnderComposite
				.setLayout(rightReallyUnderCompositeGridLayout);

		// Information label
		GridData informationLabelGridData = new GridData();
		informationLabelGridData.grabExcessVerticalSpace = false;
		informationLabelGridData.heightHint = 69;
		informationLabelGridData.widthHint = 239;
		informationLabelGridData.horizontalIndent = 5;
		informationLabelGridData.grabExcessHorizontalSpace = false;
		informationLabel = new Label(rightReallyUnderComposite, SWT.WRAP);
		informationLabel.setLayoutData(informationLabelGridData);
		setInformationLabel();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jface.dialogs.Dialog#createButtonsForButtonBar(org.eclipse
	 * .swt.widgets.Composite)
	 */
	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		super.createButtonsForButtonBar(parent);

		// If not editing old rule, disable OK button when opening the dialog
		if (newItem == null) {
			getButton(IDialogConstants.OK_ID).setEnabled(false);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.dialogs.Dialog#cancelPressed()
	 */
	@Override
	protected void cancelPressed() {
		newItem = null;
		super.cancelPressed();
	}

	/**
	 * Opens dialog shell
	 * 
	 * @return the new item created
	 */
	public TreeItem openAndGetItem() {
		open();
		return newItem;
	}

	/**
	 * Inserts data to upper composite
	 */
	protected abstract void insertDataToUpperComposite();

	/**
	 * Sets list selection
	 */
	protected abstract void setListSelection();

	/**
	 * Inserts data to text fields
	 */
	protected abstract void insertDataToTextFields();

	/**
	 * Inserts data to component fields
	 */
	protected abstract void insertDataToComponentFields();

	/**
	 * Sets information label
	 */
	protected abstract void setInformationLabel();
}
