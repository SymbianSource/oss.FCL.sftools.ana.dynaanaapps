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
 * Advanced filter dialog
 *
 */
package com.nokia.traceviewer.dialog;

import java.net.URL;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.swt.widgets.Tree;

import com.nokia.traceviewer.TraceViewerPlugin;
import com.nokia.traceviewer.dialog.treeitem.FilterTreeBaseItem;
import com.nokia.traceviewer.dialog.treeitem.FilterTreeComponentItem;
import com.nokia.traceviewer.dialog.treeitem.FilterTreeTextItem;
import com.nokia.traceviewer.dialog.treeitem.TreeItem;
import com.nokia.traceviewer.dialog.treeitem.TreeItemContentProvider;
import com.nokia.traceviewer.dialog.treeitem.TreeItemLabelProvider;
import com.nokia.traceviewer.engine.TraceViewerGlobals;
import com.nokia.traceviewer.engine.dataprocessor.FilterRuleObject;
import com.nokia.traceviewer.engine.dataprocessor.FilterRuleSet;
import com.nokia.traceviewer.engine.dataprocessor.FilterRuleSet.LogicalOperator;

/**
 * Advanced filter dialog
 * 
 */
public final class FilterAdvancedDialog extends BaseDialog {

	/**
	 * Indicates how this dialog was exited
	 */
	public enum ExitStatus {

		/**
		 * Initial state
		 */
		NORMAL,

		/**
		 * Exited through apply button
		 */
		APPLYBUTTON,

		/**
		 * Exited through cancel button
		 */
		CANCELBUTTON,

		/**
		 * Exited through simple button
		 */
		SIMPLEBUTTON;
	}

	/**
	 * Error message to show when there is multiple operators inside same set
	 */
	private static final String MULTIPLE_OPERATORS_ERROR_MSG = Messages
			.getString("FilterAdvancedDialog.MultipleOperatorsErrorMsg"); //$NON-NLS-1$

	/**
	 * Error message to show when there is extra characters inside same set
	 */
	private static final String EXTRA_CHARS_ERROR_MSG = Messages
			.getString("FilterAdvancedDialog.InvalidSyntaxErrorMsg"); //$NON-NLS-1$

	/**
	 * Tree
	 */
	private Tree tree;

	/**
	 * TreeViewer
	 */
	private TreeViewer treeViewer;

	/**
	 * Styled Text rule field
	 */
	private StyledText ruleTextfield;

	/**
	 * Button
	 */
	private Button showButton;

	/**
	 * Content provider
	 */
	private final TreeItemContentProvider contentProvider;

	/**
	 * Tree root
	 */
	private final TreeItem treeRoot;

	/**
	 * Start set tool item
	 */
	private ToolItem startSetToolItem;

	/**
	 * Start set image location
	 */
	private static final String startSetImageLocation = "/icons/startset.gif"; //$NON-NLS-1$

	/**
	 * Start set image
	 */
	private Image startSetImage;

	/**
	 * End set tool item
	 */
	private ToolItem endSetToolItem;

	/**
	 * End set image location
	 */
	private static final String endSetImageLocation = "/icons/endset.gif"; //$NON-NLS-1$

	/**
	 * End set image
	 */
	private Image endSetImage;

	/**
	 * AND tool item
	 */
	private ToolItem andToolItem;

	/**
	 * AND image location
	 */
	private static final String andImageLocation = "/icons/logicaland.gif"; //$NON-NLS-1$

	/**
	 * AND image
	 */
	private Image andImage;

	/**
	 * OR tool item
	 */
	private ToolItem orToolItem;

	/**
	 * OR image location
	 */
	private static final String orImageLocation = "/icons/logicalor.gif"; //$NON-NLS-1$

	/**
	 * OR image
	 */
	private Image orImage;

	/**
	 * NOT tool item
	 */
	private ToolItem notToolItem;

	/**
	 * NOT image location
	 */
	private static final String notImageLocation = "/icons/logicalnot.gif"; //$NON-NLS-1$

	/**
	 * NOT image
	 */
	private Image notImage;

	/**
	 * Back tool item
	 */
	private ToolItem backToolItem;

	/**
	 * Back image location
	 */
	private static final String backImageLocation = "/icons/backarrow.gif"; //$NON-NLS-1$

	/**
	 * Clear tool item
	 */
	private ToolItem clearToolItem;

	/**
	 * Clear image location
	 */
	private static final String clearImageLocation = "/icons/clear.gif"; //$NON-NLS-1$

	/**
	 * Simple tool item
	 */
	private ToolItem simpleToolItem;

	/**
	 * Simple image location
	 */
	private static final String simpleImageLocation = "/icons/simplefilter.gif"; //$NON-NLS-1$

	/**
	 * Simple image
	 */
	private Image simpleImage;

	/**
	 * Indicates to show traces containing the rule. Otherwise they're hidden
	 */
	private boolean showTracesContainingRule = true;

	/**
	 * Rule parser
	 */
	private FilterAdvancedParser ruleParser;

	/**
	 * Filter rule set to create from the rule text
	 */
	private FilterRuleObject ruleSet;

	/**
	 * Indicates that the dialog can be closed
	 */
	private boolean canBeClosed = true;

	/**
	 * Selection menu that opens when user clicks a group in the Tree
	 */
	private Menu menu;

	/**
	 * Indicates the exit status of this dialog
	 */
	public ExitStatus exitStatus;

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
	public FilterAdvancedDialog(Shell parent,
			TreeItemContentProvider contentProvider, TreeItem treeRoot) {
		super(parent, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL | SWT.RESIZE);
		this.contentProvider = contentProvider;
		this.treeRoot = treeRoot;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.traceviewer.dialog.BaseDialog#close()
	 */
	@Override
	public boolean close() {
		// If closing dialog by clicking X, do as in Cancel button
		if (exitStatus == ExitStatus.NORMAL) {
			exitStatus = ExitStatus.CANCELBUTTON;
			saveSettings();
			dispose();
		} else if (exitStatus == ExitStatus.SIMPLEBUTTON) {
			saveSettings();
		}

		return super.close();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.traceviewer.dialog.BaseDialog#createShell()
	 */
	@Override
	protected void createDialogContents() {
		// Shell
		GridLayout shellGridLayout = new GridLayout();
		shellGridLayout.numColumns = 3;
		shellGridLayout.makeColumnsEqualWidth = false;
		getShell().setText("Advanced Filter Rules"); //$NON-NLS-1$
		composite.setLayout(shellGridLayout);
		getShell().setMinimumSize(new Point(435, 515));

		// Tree
		GridData treeGridData = new GridData();
		treeGridData.horizontalAlignment = GridData.FILL;
		treeGridData.grabExcessHorizontalSpace = true;
		treeGridData.grabExcessVerticalSpace = true;
		treeGridData.horizontalSpan = 2;
		treeGridData.verticalAlignment = GridData.FILL;
		treeGridData.widthHint = 250;
		tree = new Tree(composite, SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
		tree.setLayoutData(treeGridData);

		// Tree viewer
		treeViewer = new TreeViewer(tree);
		treeViewer.setLabelProvider(new TreeItemLabelProvider());
		treeViewer.setContentProvider(contentProvider);

		// Set root and expand all items
		treeViewer.setInput(treeRoot);
		treeViewer.expandAll();

		// Select the root item
		if (tree.getItem(0) != null) {
			tree.setSelection(tree.getItem(0));
		}

		// Create toolbar
		createToolBar();

		// Rule text field
		GridData ruleTextFieldGridData = new GridData();
		ruleTextFieldGridData.grabExcessHorizontalSpace = true;
		ruleTextFieldGridData.horizontalAlignment = GridData.FILL;
		ruleTextFieldGridData.verticalAlignment = GridData.FILL;
		ruleTextFieldGridData.heightHint = 70;
		ruleTextFieldGridData.horizontalSpan = 2;
		ruleTextFieldGridData.grabExcessVerticalSpace = false;
		ruleTextFieldGridData.minimumHeight = 100;
		ruleTextFieldGridData.horizontalIndent = 5;
		ruleTextfield = new StyledText(composite, SWT.BORDER | SWT.WRAP
				| SWT.V_SCROLL);
		ruleTextfield.setLayoutData(ruleTextFieldGridData);

		// Create new rule parser to this rule text field
		ruleParser = new FilterAdvancedParser(ruleTextfield, tree.getItems()[0]
				.getItems());
		// Set parser to be the key, mouse and focus listener
		ruleTextfield.addKeyListener(ruleParser);
		ruleTextfield.addMouseListener(ruleParser);
		ruleTextfield.addFocusListener(ruleParser);

		// Set text from previous visit
		ruleTextfield.setText(FilterAdvancedParser.SPACE_STR
				+ TraceViewerGlobals.getTraceViewer().getDataProcessorAccess()
						.getFilterProcessor().getAdvancedFilterString()
				+ FilterAdvancedParser.SPACE_STR);
		ruleTextfield.setCaretOffset(ruleTextfield.getCharCount());

		// Create Group
		createGroup();
	}

	/**
	 * Creates the toolbar containing pushable buttons to the right side of the
	 * dialog.
	 */
	private void createToolBar() {
		// Create the toolBar
		GridData toolBarGridData = new GridData();
		toolBarGridData.horizontalAlignment = GridData.FILL;
		toolBarGridData.grabExcessVerticalSpace = true;
		toolBarGridData.verticalSpan = 2;
		toolBarGridData.verticalAlignment = GridData.FILL;
		ToolBar toolBar = new ToolBar(composite, SWT.VERTICAL);
		toolBar.setLayoutData(toolBarGridData);

		// Create "Start set" item
		startSetToolItem = new ToolItem(toolBar, SWT.PUSH);
		startSetToolItem.setText(Messages
				.getString("FilterAdvancedDialog.StartSetButtonText")); //$NON-NLS-1$
		startSetToolItem.setToolTipText(Messages
				.getString("FilterAdvancedDialog.StartSetToolTip")); //$NON-NLS-1$
		// Set image to the "Start set" item
		URL url = TraceViewerPlugin.getDefault().getBundle().getEntry(
				startSetImageLocation);
		startSetImage = ImageDescriptor.createFromURL(url).createImage();
		startSetToolItem.setImage(startSetImage);

		// Create "End set" item
		endSetToolItem = new ToolItem(toolBar, SWT.PUSH);
		endSetToolItem.setText(Messages
				.getString("FilterAdvancedDialog.EndSetButtonText")); //$NON-NLS-1$
		endSetToolItem.setToolTipText(Messages
				.getString("FilterAdvancedDialog.EndSetToolTip")); //$NON-NLS-1$
		// Set image to the "End set" item
		url = TraceViewerPlugin.getDefault().getBundle().getEntry(
				endSetImageLocation);
		endSetImage = ImageDescriptor.createFromURL(url).createImage();
		endSetToolItem.setImage(endSetImage);

		// Create "And" item
		andToolItem = new ToolItem(toolBar, SWT.PUSH);
		andToolItem.setText(FilterAdvancedParser.AND);
		andToolItem.setToolTipText(Messages
				.getString("FilterAdvancedDialog.ANDToolTip")); //$NON-NLS-1$
		// Set image to the "And" item
		url = TraceViewerPlugin.getDefault().getBundle().getEntry(
				andImageLocation);
		andImage = ImageDescriptor.createFromURL(url).createImage();
		andToolItem.setImage(andImage);

		// Create "Or" item
		orToolItem = new ToolItem(toolBar, SWT.PUSH);
		orToolItem.setText(FilterAdvancedParser.OR);
		orToolItem.setToolTipText(Messages
				.getString("FilterAdvancedDialog.ORToolTip")); //$NON-NLS-1$
		// Set image to the "Or" item
		url = TraceViewerPlugin.getDefault().getBundle().getEntry(
				orImageLocation);
		orImage = ImageDescriptor.createFromURL(url).createImage();
		orToolItem.setImage(orImage);

		// Create "Not" item
		notToolItem = new ToolItem(toolBar, SWT.PUSH);
		notToolItem.setText(FilterAdvancedParser.NOT);
		notToolItem.setToolTipText(Messages
				.getString("FilterAdvancedDialog.NOTToolTip")); //$NON-NLS-1$
		// Set image to the "Not" item
		url = TraceViewerPlugin.getDefault().getBundle().getEntry(
				notImageLocation);
		notImage = ImageDescriptor.createFromURL(url).createImage();
		notToolItem.setImage(notImage);

		// Create "Back" item
		backToolItem = new ToolItem(toolBar, SWT.PUSH);
		backToolItem.setText(Messages
				.getString("FilterAdvancedDialog.BackButtonText")); //$NON-NLS-1$
		backToolItem.setToolTipText(Messages
				.getString("FilterAdvancedDialog.BackToolTip")); //$NON-NLS-1$
		// Set image to the "Back" item
		url = TraceViewerPlugin.getDefault().getBundle().getEntry(
				backImageLocation);
		Image backImage = ImageDescriptor.createFromURL(url).createImage();
		backToolItem.setImage(backImage);

		// Create "Clear" item
		clearToolItem = new ToolItem(toolBar, SWT.PUSH);
		clearToolItem.setText(Messages
				.getString("FilterAdvancedDialog.ClearButtonText")); //$NON-NLS-1$
		clearToolItem.setToolTipText(Messages
				.getString("FilterAdvancedDialog.ClearToolTip")); //$NON-NLS-1$
		// Set image to the "Clear" item
		url = TraceViewerPlugin.getDefault().getBundle().getEntry(
				clearImageLocation);
		Image clearImage = ImageDescriptor.createFromURL(url).createImage();
		clearToolItem.setImage(clearImage);

		// Create "Simple" item
		simpleToolItem = new ToolItem(toolBar, SWT.PUSH);
		simpleToolItem.setText(Messages
				.getString("FilterAdvancedDialog.SimpleButtonText")); //$NON-NLS-1$
		simpleToolItem.setToolTipText(Messages
				.getString("FilterAdvancedDialog.SimpleViewToolTip")); //$NON-NLS-1$
		// Set image to the "Simple" item
		url = TraceViewerPlugin.getDefault().getBundle().getEntry(
				simpleImageLocation);
		simpleImage = ImageDescriptor.createFromURL(url).createImage();
		simpleToolItem.setImage(simpleImage);
	}

	/**
	 * This method initializes group
	 */
	private void createGroup() {
		// Settings group
		GridData settingsGroupGridData = new GridData();
		settingsGroupGridData.horizontalAlignment = GridData.BEGINNING;
		settingsGroupGridData.grabExcessHorizontalSpace = false;
		settingsGroupGridData.grabExcessVerticalSpace = false;
		settingsGroupGridData.verticalAlignment = GridData.FILL;
		GridLayout settingsGroupGridLayout = new GridLayout();
		settingsGroupGridLayout.verticalSpacing = 5;
		settingsGroupGridLayout.marginWidth = 5;
		settingsGroupGridLayout.marginHeight = 5;
		settingsGroupGridLayout.numColumns = 2;
		settingsGroupGridLayout.horizontalSpacing = 5;
		Group settingsGroup = new Group(composite, SWT.NONE);
		settingsGroup.setLayout(settingsGroupGridLayout);
		settingsGroup.setLayoutData(settingsGroupGridData);
		settingsGroup.setText(Messages
				.getString("FilterAdvancedDialog.SettingsGroupName")); //$NON-NLS-1$

		// Show button
		showButton = new Button(settingsGroup, SWT.RADIO);
		showButton.setText(Messages
				.getString("FilterAdvancedDialog.ShowButtonText")); //$NON-NLS-1$
		showButton.setToolTipText(Messages
				.getString("FilterAdvancedDialog.ShowButtonToolTip")); //$NON-NLS-1$
		showButton.setSelection(showTracesContainingRule);

		// Hide button
		Button hideButton = new Button(settingsGroup, SWT.RADIO);
		hideButton.setText(Messages
				.getString("FilterAdvancedDialog.HideButtonText")); //$NON-NLS-1$
		hideButton.setToolTipText(Messages
				.getString("FilterAdvancedDialog.HideButtonToolTip")); //$NON-NLS-1$
		hideButton.setSelection(!showTracesContainingRule);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.traceviewer.dialog.BaseDialog#restoreSettings()
	 */
	@Override
	protected void restoreSettings() {
		super.restoreSettings();
		exitStatus = ExitStatus.NORMAL;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.traceviewer.dialog.BaseDialog#saveSettings()
	 */
	@Override
	protected void saveSettings() {
		super.saveSettings();

		if (exitStatus == ExitStatus.APPLYBUTTON) {
			// Save advanced filter string
			TraceViewerGlobals.getTraceViewer().getDataProcessorAccess()
					.getFilterProcessor().setAdvancedFilterString(
							ruleTextfield.getText());

			showTracesContainingRule = showButton.getSelection();

			// Create the rule set
			String ruleText = ruleTextfield.getText();
			ruleSet = null;
			if (ruleText.length() > 0
					&& ruleParser.containsFilterRule(ruleText, tree.getItems())) {
				ruleSet = createRuleSet(ruleText, false);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.dialogs.Dialog#okPressed()
	 */
	@Override
	protected void okPressed() {
		exitStatus = ExitStatus.APPLYBUTTON;
		boolean valid = checkWrittenRules(ruleTextfield.getText());
		if (valid) {
			saveSettings();
		} else {
			TraceViewerGlobals.getTraceViewer().getDialogs().showErrorMessage(
					EXTRA_CHARS_ERROR_MSG);
			canBeClosed = false;
		}

		if (canBeClosed) {
			super.okPressed();
			dispose();
		} else {
			canBeClosed = true;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.dialogs.Dialog#cancelPressed()
	 */
	@Override
	protected void cancelPressed() {
		exitStatus = ExitStatus.CANCELBUTTON;
		saveSettings();

		super.cancelPressed();

		dispose();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.traceviewer.dialog.BaseDialog#createActionListeners()
	 */
	@Override
	protected void createActionListeners() {

		// Add selection listener to start Set button
		startSetToolItem.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				TraceViewerGlobals.postUiEvent("StartSetButton", "1"); //$NON-NLS-1$ //$NON-NLS-2$
				updateRuleTextField(FilterAdvancedParser.START_BRACKET_STR);
				TraceViewerGlobals.postUiEvent("StartSetButton", "0"); //$NON-NLS-1$ //$NON-NLS-2$
			}
		});

		// Add selection listener to end Set button
		endSetToolItem.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				TraceViewerGlobals.postUiEvent("EndSetButton", "1"); //$NON-NLS-1$ //$NON-NLS-2$
				updateRuleTextField(FilterAdvancedParser.END_BRACKET_STR);
				TraceViewerGlobals.postUiEvent("EndSetButton", "0"); //$NON-NLS-1$ //$NON-NLS-2$
			}
		});

		// Add selection listener to and button
		andToolItem.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				TraceViewerGlobals.postUiEvent("AndButton", "1"); //$NON-NLS-1$ //$NON-NLS-2$
				updateRuleTextField(FilterAdvancedParser.AND);
				TraceViewerGlobals.postUiEvent("AndButton", "0"); //$NON-NLS-1$ //$NON-NLS-2$
			}
		});

		// Add selection listener to or button
		orToolItem.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				TraceViewerGlobals.postUiEvent("OrButton", "1"); //$NON-NLS-1$ //$NON-NLS-2$
				updateRuleTextField(FilterAdvancedParser.OR);
				TraceViewerGlobals.postUiEvent("OrButton", "0"); //$NON-NLS-1$ //$NON-NLS-2$
			}
		});

		// Add selection listener to not button
		notToolItem.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				TraceViewerGlobals.postUiEvent("NotButton", "1"); //$NON-NLS-1$ //$NON-NLS-2$
				updateRuleTextField(FilterAdvancedParser.NOT);
				TraceViewerGlobals.postUiEvent("NotButton", "0"); //$NON-NLS-1$ //$NON-NLS-2$
			}
		});

		// Add selection listener to back button
		backToolItem.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				TraceViewerGlobals.postUiEvent("BackButton", "1"); //$NON-NLS-1$ //$NON-NLS-2$
				removeLastWord();
				TraceViewerGlobals.postUiEvent("BackButton", "0"); //$NON-NLS-1$ //$NON-NLS-2$
			}
		});

		// Add selection listener to clear button
		clearToolItem.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				TraceViewerGlobals.postUiEvent("ClearButton", "1"); //$NON-NLS-1$ //$NON-NLS-2$
				ruleTextfield.setText(""); //$NON-NLS-1$
				setButtonStates();
				TraceViewerGlobals.postUiEvent("ClearButton", "0"); //$NON-NLS-1$ //$NON-NLS-2$
			}
		});

		// Add selection listener to simple button
		simpleToolItem.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				TraceViewerGlobals.postUiEvent("SimpleButton", "1"); //$NON-NLS-1$ //$NON-NLS-2$

				// Save exit status and advanced filter string
				exitStatus = ExitStatus.SIMPLEBUTTON;
				TraceViewerGlobals.getTraceViewer().getDataProcessorAccess()
						.getFilterProcessor().setAdvancedFilterString(
								ruleTextfield.getText());
				close();
				dispose();
				TraceViewerGlobals.postUiEvent("SimpleButton", "0"); //$NON-NLS-1$ //$NON-NLS-2$
			}
		});

		// Add selection listener to Tree
		tree.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent event) {
				TraceViewerGlobals.postUiEvent("TreeSelected", "1"); //$NON-NLS-1$ //$NON-NLS-2$
				// Get selection
				Object selection = ((IStructuredSelection) treeViewer
						.getSelection()).getFirstElement();

				if (selection != null) {
					// If group selected, add all rules to the list
					if (((TreeItem) selection).isGroup()) {
						processGroupSelection();

					} else {
						// Dispose menu if it exists
						if (menu != null && !menu.isDisposed()) {
							menu.dispose();
						}
						updateRuleTextField(tree.getSelection()[0].getText());
					}
				}
				TraceViewerGlobals.postUiEvent("TreeSelected", "0"); //$NON-NLS-1$ //$NON-NLS-2$
			}

			/**
			 * Processes group item selection
			 */
			private void processGroupSelection() {
				// Add menu to Tree
				menu = new Menu(composite);

				MenuItem orItem = new MenuItem(menu, SWT.CASCADE);
				if (orImage != null && !orImage.isDisposed()) {
					orItem.setImage(orImage);
				}
				orItem.setEnabled(ruleParser.canOrBeInserted()
						|| ruleTextfield.getCharCount() < 4);
				orItem.setText(FilterAdvancedParser.OR);

				// Add selection listener to OR button
				orItem.addSelectionListener(new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent event) {
						processSelection(FilterAdvancedParser.OR);
					}
				});

				// And item
				MenuItem andItem = new MenuItem(menu, SWT.CASCADE);
				if (andImage != null && !andImage.isDisposed()) {
					andItem.setImage(andImage);
				}
				andItem.setEnabled(ruleParser.canAndBeInserted()
						|| ruleTextfield.getCharCount() < 3);
				andItem.setText(FilterAdvancedParser.AND);

				// Add selection listener to AND button
				andItem.addSelectionListener(new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent event) {
						processSelection(FilterAdvancedParser.AND);
					}
				});

				tree.setMenu(menu);
				menu.setVisible(true);
			}
		});

		// Validate and set button states
		ruleParser.validate();
		setButtonStates();
	}

	/**
	 * Checks written rules from the rule field
	 * 
	 * @param text
	 *            text to check
	 * @return true if rules are ok, false otherwise
	 */
	public boolean checkWrittenRules(String text) {
		boolean ok = false;
		if (ruleParser != null) {
			ok = ruleParser.checkWrittenRules(text);
		}
		return ok;
	}

	/**
	 * Process selection of AND or OR
	 * 
	 * @param operator
	 *            operator String
	 */
	private void processSelection(String operator) {
		StringBuffer buf = new StringBuffer();

		// Get selection
		Object selection = ((IStructuredSelection) treeViewer.getSelection())
				.getFirstElement();
		if (selection != null) {
			Object[] children = ((TreeItem) selection).getChildren();

			// If previous word is a rule, add the operator
			String prevWord = ruleParser.getPreviousWord(ruleTextfield
					.getText(), ruleTextfield.getText().length() - 1);
			if (ruleParser.containsFilterRule(prevWord, tree.getItems())) {
				buf.append(" " + operator + " "); //$NON-NLS-1$ //$NON-NLS-2$
			}

			// Loop through children and append them to the string buffer
			for (int i = 0; i < children.length; i++) {
				if (!((TreeItem) children[i]).isGroup()) {

					// If previous word is a rule, add the operator
					prevWord = ruleParser.getPreviousWord(buf.toString(), buf
							.length() - 1);
					if (ruleParser
							.containsFilterRule(prevWord, tree.getItems())) {
						buf.append(" " + operator + " "); //$NON-NLS-1$ //$NON-NLS-2$
					}

					buf.append(((TreeItem) children[i]).getName());
					if (i < children.length - 1
							&& !((TreeItem) children[i + 1]).isGroup()) {
						buf.append(" " + operator + " "); //$NON-NLS-1$ //$NON-NLS-2$
					}
				}

			}
			updateRuleTextField(buf.toString());
		}
	}

	/**
	 * Disposes UI resources if they exist. Is called when the shell is closed
	 * or user clicks apply, cancel of simple buttons
	 */
	private void dispose() {
		if (startSetImage != null) {
			startSetImage.dispose();
		}
		if (endSetImage != null) {
			endSetImage.dispose();
		}
		if (orImage != null) {
			orImage.dispose();
		}
		if (andImage != null) {
			andImage.dispose();
		}
		if (notImage != null) {
			notImage.dispose();
		}
		if (simpleImage != null) {
			simpleImage.dispose();
		}
	}

	/**
	 * Updates rule text field
	 * 
	 * @param text
	 *            text to add to text field
	 */
	private void updateRuleTextField(String text) {
		ruleTextfield.insert(FilterAdvancedParser.SPACE_STR + text
				+ FilterAdvancedParser.SPACE_STR);
		ruleTextfield.setCaretOffset(ruleTextfield.getCaretOffset()
				+ text.length() + 2);
		ruleParser.validate();
		setButtonStates();
	}

	/**
	 * Removes last word from the rule text field
	 */
	private void removeLastWord() {

		// Get caret offset and text from the text field
		int caretPos = ruleTextfield.getCaretOffset();
		StringBuffer text = new StringBuffer(ruleTextfield.getText());
		int startPos = 0;
		if (caretPos == ruleTextfield.getCharCount()) {
			caretPos--;
		}

		// Go through characters starting from the last one
		for (int i = caretPos; i > 0; i--) {
			char c = text.charAt(i);

			// When space is found, set start position for deleting
			if (c == FilterAdvancedParser.SPACE && i != caretPos) {
				startPos = i;
				break;
			}
		}

		// Replace the last word from the text field and set button states
		if (startPos < caretPos) {
			ruleTextfield.replaceTextRange(startPos, caretPos - startPos, ""); //$NON-NLS-1$
			setButtonStates();
		}
	}

	/**
	 * Sets button states
	 */
	public void setButtonStates() {

		// Get different statuses from rule parser and set button states
		// according to those
		startSetToolItem.setEnabled(ruleParser.canNewSetBeStarted());
		endSetToolItem.setEnabled(ruleParser.canSetBeEnded());
		Button okButton = getButton(IDialogConstants.OK_ID);
		if (okButton != null) {
			getButton(OK).setEnabled(ruleParser.canApplyBeClicked());
		}
		andToolItem.setEnabled(ruleParser.canAndBeInserted());
		orToolItem.setEnabled(ruleParser.canOrBeInserted());
		backToolItem.setEnabled(ruleTextfield.getCaretOffset() > 1);
		clearToolItem.setEnabled(ruleTextfield.getCharCount() > 1);
	}

	/**
	 * Creates a rule set out of the rule String
	 * 
	 * @param text
	 *            text to create the rule set from
	 * @param notRule
	 *            indicates if a rule is a NOT rule
	 * @return rule object
	 */
	public FilterRuleObject createRuleSet(String text, boolean notRule) {
		// Tells that NOT status has changed
		boolean notHasChanged = false;

		// New Filter rule object to be created
		FilterRuleObject object = null;

		// Trim the text
		text = text.trim();

		// Check if result is a basic rule
		boolean isBasicRule = ruleParser.isBasicRule(text);

		// Basic rule
		if (isBasicRule) {
			// Get the rule from the tree
			object = getRuleWithName(text, treeRoot.getChildren());

			// Rule set
		} else {
			object = new FilterRuleSet();

			// Get and set Logical Operator for this rule set
			LogicalOperator op = ruleParser.getLogicalOperator(text);
			((FilterRuleSet) object).setOperator(op);

			// Both or non operators in the set, show error
			if (op == null) {
				canBeClosed = false;
				TraceViewerGlobals.getTraceViewer().getDialogs()
						.showErrorMessage(MULTIPLE_OPERATORS_ERROR_MSG);
			}

			// Get children and loop through them
			String[] children = ruleParser.getChildren(text);
			for (int i = 0; canBeClosed && children != null
					&& i < children.length; i++) {

				// Check if child is NOT. If yes, it means that the next child
				// will have modified NOT status
				if (children[i].trim().equals(FilterAdvancedParser.NOT)) {
					notHasChanged = true;
				} else {
					FilterRuleObject child;
					if (notHasChanged) {
						child = createRuleSet(children[i], !notRule);
					} else {
						child = createRuleSet(children[i], notRule);
					}

					((FilterRuleSet) object).addObject(child);
					notHasChanged = false;
				}
			}

		}

		// Set NOT rule status
		object.setLogicalNotRule(notRule);
		return object;
	}

	/**
	 * Gets rule with a name
	 * 
	 * @param name
	 *            name text
	 * @param items
	 *            items array
	 * @return true if rule is found from the items
	 */
	private FilterRuleObject getRuleWithName(String name, Object[] items) {
		FilterRuleObject ret = null;
		for (int i = 0; i < items.length; i++) {
			FilterTreeBaseItem item = (FilterTreeBaseItem) items[i];

			// Contains children, recurse
			if ((item.getChildren().length > 0)) {
				ret = getRuleWithName(name, item.getChildren());

				// Check item
			} else {
				if (name.equals(item.getName())) {

					// Create new objects from the existing ones
					// Text item
					if (item instanceof FilterTreeTextItem) {
						FilterTreeTextItem item2 = (FilterTreeTextItem) item;
						ret = new FilterTreeTextItem(null, null, name, item2
								.getRule(), item2.getText(), item2
								.isMatchCase());

						// Component item
					} else if (item instanceof FilterTreeComponentItem) {
						FilterTreeComponentItem item2 = (FilterTreeComponentItem) item;
						ret = new FilterTreeComponentItem(null, null, name,
								item2.getRule(), item2.getComponentId(), item2
										.getGroupId());
					}
					break;
				}
			}
		}
		return ret;
	}

	/**
	 * Checks if traces hitting filter should be displayed or not.
	 * 
	 * @return true if traces hitting filter should be displayed.
	 */
	public boolean isShowTracesContainingRule() {
		return showTracesContainingRule;
	}

	/**
	 * Gets the rule set created
	 * 
	 * @return the ruleSet created
	 */
	public FilterRuleObject getRuleSet() {
		return ruleSet;
	}

}
