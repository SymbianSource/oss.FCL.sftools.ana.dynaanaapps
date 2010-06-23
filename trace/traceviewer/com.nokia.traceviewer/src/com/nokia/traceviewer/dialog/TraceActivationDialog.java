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
 * Trace Activation dialog
 *
 */
package com.nokia.traceviewer.dialog;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DragDetectEvent;
import org.eclipse.swt.events.DragDetectListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;

import com.nokia.traceviewer.TraceViewerHelpContextIDs;
import com.nokia.traceviewer.action.ReloadDecodeFilesAction;
import com.nokia.traceviewer.engine.TraceViewerGlobals;
import com.nokia.traceviewer.engine.activation.TraceActivationComponentItem;
import com.nokia.traceviewer.engine.activation.TraceActivationGroupItem;
import com.nokia.traceviewer.engine.activation.TraceActivator;

/**
 * Trace activation dialog
 * 
 */
public final class TraceActivationDialog extends BaseDialog {

	/**
	 * Lead zero string
	 */
	private static final String LEAD_ZERO = "0"; //$NON-NLS-1$

	/**
	 * Waiting time for model to be loaded
	 */
	private static final int WAITING_TIME = 50;

	/**
	 * Right mouse button ID
	 */
	private static final int RIGHT_MOUSE_BUTTON = 3;

	/**
	 * Load button ID
	 */
	private static final int LOAD_BUTTON_ID = IDialogConstants.CLIENT_ID + 1;

	/**
	 * Save button ID
	 */
	private static final int SAVE_BUTTON_ID = LOAD_BUTTON_ID + 1;

	/**
	 * Activate button ID
	 */
	private static final int ACTIVATE_BUTTON_ID = SAVE_BUTTON_ID + 1;

	/**
	 * Hex prefix
	 */
	private static final String HEX_PREFIX = "0x"; //$NON-NLS-1$

	/**
	 * String to be shown if same group is both on and off in different
	 * Dictionaries
	 */
	private static final String STATE_BOTH_STRING = Messages
			.getString("TraceActivationDialog.OnOff"); //$NON-NLS-1$

	/**
	 * String to be shown if group is on
	 */
	private static final String STATE_ON_STRING = Messages
			.getString("TraceActivationDialog.On"); //$NON-NLS-1$

	/**
	 * String to be shown if group is off
	 */
	private static final String STATE_OFF_STRING = Messages
			.getString("TraceActivationDialog.Off"); //$NON-NLS-1$

	/**
	 * Trace activation category text
	 */
	private static final String TRACE_ACTIVATION_CATEGORY = com.nokia.traceviewer.dialog.Messages
			.getString("TraceActivationDialog.TraceActivationCategory"); //$NON-NLS-1$

	/**
	 * Color of group which is both on and off
	 */
	private final Color STATE_BOTH_COLOR = getParentShell().getDisplay()
			.getSystemColor(SWT.COLOR_BLACK);

	/**
	 * Color of group which is on
	 */
	private final Color STATE_ON_COLOR = getParentShell().getDisplay()
			.getSystemColor(SWT.COLOR_DARK_GREEN);

	/**
	 * Color of group which is off
	 */
	private final Color STATE_OFF_COLOR = getParentShell().getDisplay()
			.getSystemColor(SWT.COLOR_RED);

	/**
	 * Save dialog
	 */
	private TraceConfigurationSaveDialog saveDialog;

	/**
	 * Load dialog
	 */
	private TraceConfigurationLoadDialog loadDialog;

	/**
	 * Main group
	 */
	private Group mainGroup;

	/**
	 * Open Dictionary file button
	 */
	private Button addDictionaryButton;

	/**
	 * Remove Dictionary file button
	 */
	private Button removeDictionaryButton;

	/**
	 * Text field for Dictionary filter
	 */
	private Text dictionaryFilterText;

	/**
	 * Dictionary table
	 */
	private Table dictionaryTable;

	/**
	 * Group table
	 */
	private Table groupTable;

	/**
	 * Text field for group filter
	 */
	private Text groupFilterText;

	/**
	 * List of all components
	 */
	private List<TraceActivationComponentItem> allComponents;

	/**
	 * List of current components
	 */
	private final List<TraceActivationComponentItem> currentComponents;

	/**
	 * List of changed components. Will be sent when Apply or Ok is clicked
	 */
	private final List<TraceActivationComponentItem> changedComponents;

	/**
	 * Index of the first dictionary selected when dragging with mouse
	 */
	private int firstDictionarySelected;

	/**
	 * Index of the first group selected when dragging with mouse
	 */
	private int firstGroupSelected;

	/**
	 * Tells that model has been changed and components should be loaded again
	 */
	private boolean modelChanged;

	/**
	 * Previously selected Dictionary index
	 */
	private int[] previousSelectedDictionaryIndices = { 0 };

	/**
	 * Saved component filter
	 */
	private String savedComponentFilter;

	/**
	 * Constructor
	 * 
	 * @param parent
	 *            parent shell
	 */
	public TraceActivationDialog(Shell parent) {
		// Pass the default styles here
		super(parent, SWT.DIALOG_TRIM | SWT.MODELESS | SWT.RESIZE);
		allComponents = new ArrayList<TraceActivationComponentItem>();
		currentComponents = new ArrayList<TraceActivationComponentItem>();
		changedComponents = new ArrayList<TraceActivationComponentItem>();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.nokia.traceviewer.dialog.BaseDialog#createDialogArea(org.eclipse.
	 * swt.widgets.Composite)
	 */
	@Override
	protected void createDialogContents() {
		String shellTitle = Messages
				.getString("TraceActivationDialog.ShellTitle"); //$NON-NLS-1$
		getShell().setText(shellTitle);

		// Set the minimum size for the shell
		getShell().setMinimumSize(new Point(600, 400));

		// Set gridlayout to composite
		GridLayout compositeGridLayout = new GridLayout();
		compositeGridLayout.numColumns = 1;
		composite.setLayout(compositeGridLayout);

		// Main group
		GridLayout mainGroupGridLayout = new GridLayout();
		mainGroupGridLayout.numColumns = 5;
		mainGroup = new Group(composite, SWT.NONE);
		mainGroup.setText(shellTitle);
		mainGroup.setLayout(mainGroupGridLayout);
		GridData mainGroupGridData = new GridData(SWT.FILL, SWT.FILL, true,
				true);
		mainGroup.setLayoutData(mainGroupGridData);

		// Create component composite
		createDictionaryComposite();

		// Create group composite
		createGroupComposite();

		// Add dictionary button
		GridData addDictionaryButtonGridData = new GridData();
		addDictionaryButtonGridData.horizontalAlignment = GridData.BEGINNING;
		addDictionaryButtonGridData.widthHint = 110;
		addDictionaryButton = new Button(mainGroup, SWT.NONE);
		addDictionaryButton.setText(Messages
				.getString("TraceActivationDialog.AddDictionaryButton")); //$NON-NLS-1$
		addDictionaryButton.setToolTipText(Messages
				.getString("TraceActivationDialog.AddDictionaryButtonToolTip")); //$NON-NLS-1$
		addDictionaryButton.setLayoutData(addDictionaryButtonGridData);

		// Remove dictionary button
		GridData removeDictionaryButtonGridData = new GridData();
		removeDictionaryButtonGridData.horizontalAlignment = GridData.BEGINNING;
		removeDictionaryButtonGridData.widthHint = 110;
		removeDictionaryButton = new Button(mainGroup, SWT.NONE);
		removeDictionaryButton.setText(Messages
				.getString("TraceActivationDialog.RemoveDictionaryButton")); //$NON-NLS-1$
		removeDictionaryButton
				.setToolTipText(Messages
						.getString("TraceActivationDialog.RemoveDictionaryButtonToolTip")); //$NON-NLS-1$
		removeDictionaryButton.setEnabled(false);
		removeDictionaryButton.setLayoutData(removeDictionaryButtonGridData);

		// Create main composite
		createMainComposite();

		// Get activation information
		getActivationInformation();

		// Set help
		PlatformUI.getWorkbench().getHelpSystem().setHelp(getShell(),
				TraceViewerHelpContextIDs.ACTIVATION_DIALOG);
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
		// Create load button
		Button loadButton = createButton(parent, LOAD_BUTTON_ID, Messages
				.getString("TraceActivationDialog.LoadButtonText"), false); //$NON-NLS-1$
		loadButton.setToolTipText(Messages
				.getString("TraceActivationDialog.LoadButtonToolTip")); //$NON-NLS-1$

		// Create save button
		Button saveButton = createButton(parent, SAVE_BUTTON_ID, Messages
				.getString("TraceActivationDialog.SaveButtonText"), false); //$NON-NLS-1$
		saveButton.setToolTipText(Messages
				.getString("TraceActivationDialog.SaveButtonToolTip")); //$NON-NLS-1$

		// Activate button
		Button activateButton = createButton(
				parent,
				ACTIVATE_BUTTON_ID,
				Messages.getString("TraceActivationDialog.ActivateButtonText"), true); //$NON-NLS-1$
		activateButton.setToolTipText(Messages
				.getString("TraceActivationDialog.ActivateButtonToolTip")); //$NON-NLS-1$

		// Close button
		createButton(parent, IDialogConstants.CLOSE_ID, Messages
				.getString("TraceActivationDialog.CloseButton"), false); //$NON-NLS-1$
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.dialogs.Dialog#buttonPressed(int)
	 */
	@Override
	protected void buttonPressed(int buttonId) {
		switch (buttonId) {

		// Load button
		case LOAD_BUTTON_ID:
			TraceViewerGlobals.postUiEvent("LoadButton", "1"); //$NON-NLS-1$ //$NON-NLS-2$
			if (loadDialog == null) {
				loadDialog = new TraceConfigurationLoadDialog(true, null);
			}
			loadDialog.openDialog(allComponents, changedComponents);
			setGroupsToTable();
			TraceViewerGlobals.postUiEvent("LoadButton", "0"); //$NON-NLS-1$ //$NON-NLS-2$
			break;

		// Save button
		case SAVE_BUTTON_ID:
			TraceViewerGlobals.postUiEvent("SaveButton", "1"); //$NON-NLS-1$ //$NON-NLS-2$
			if (saveDialog == null) {
				saveDialog = new TraceConfigurationSaveDialog();
			}
			saveDialog.openDialog(allComponents);
			TraceViewerGlobals.postUiEvent("SaveButton", "0"); //$NON-NLS-1$ //$NON-NLS-2$
			break;

		// Activate button
		case ACTIVATE_BUTTON_ID:
			TraceViewerGlobals.postUiEvent("ActivateButton", "1"); //$NON-NLS-1$ //$NON-NLS-2$
			sendActivationInformation();
			TraceViewerGlobals.postUiEvent("ActivateButton", "0"); //$NON-NLS-1$ //$NON-NLS-2$
			break;

		// Close button
		case IDialogConstants.CLOSE_ID:
			TraceViewerGlobals.postUiEvent("CloseButton", "1"); //$NON-NLS-1$ //$NON-NLS-2$
			previousSelectedDictionaryIndices = dictionaryTable
					.getSelectionIndices();
			okPressed();
			TraceViewerGlobals.postUiEvent("CloseButton", "0"); //$NON-NLS-1$ //$NON-NLS-2$
			break;
		default:
			System.out.println(Messages
					.getString("TraceActivationDialog.InvalidButton")); //$NON-NLS-1$
		}
	}

	/**
	 * Updates activation information
	 * 
	 * @param added
	 *            if true, activation was added, otherwise it was removed
	 */
	private void updateActivationInformation(boolean added) {
		modelChanged = true;

		// Wait for model to be ready
		if (added) {
			int loopCount = 0;
			int maxLoopCount = 100;
			try {
				Thread.sleep(WAITING_TIME);
				while (!TraceViewerGlobals.getDecodeProvider()
						.isModelLoadedAndValid()
						&& loopCount < maxLoopCount) {

					loopCount++;
					Thread.sleep(WAITING_TIME);
				}
			} catch (InterruptedException e) {
			}
		}

		getActivationInformation();
	}

	/**
	 * Get new activation information from decode provider
	 */
	private void getActivationInformation() {
		if (TraceViewerGlobals.getDecodeProvider() != null) {
			if (modelChanged || allComponents.isEmpty()) {

				List<TraceActivationComponentItem> newComponents = TraceViewerGlobals
						.getDecodeProvider().getActivationInformation(false);
				modelChanged = false;

				// Merge components
				mergeComponents(allComponents, newComponents);
			}
			copyItems(allComponents, currentComponents);

			selectComponentsByFilter(true);
		}
	}

	/**
	 * Merges components from the model
	 * 
	 * @param oldComponents
	 *            old components
	 * @param newComponents
	 *            new components
	 */
	private void mergeComponents(
			List<TraceActivationComponentItem> oldComponents,
			List<TraceActivationComponentItem> newComponents) {

		// Loop through old components
		for (int i = 0; i < oldComponents.size(); i++) {
			TraceActivationComponentItem oldComponent = oldComponents.get(i);

			// Check if the component is found among the new components
			for (int j = 0; j < newComponents.size(); j++) {
				TraceActivationComponentItem newComponent = newComponents
						.get(j);

				if (oldComponent.getId() == newComponent.getId()) {

					// Merge groups
					mergeGroups(oldComponent.getGroups(), newComponent
							.getGroups());
				}
			}
		}

		// Set new components to be the one to be used
		allComponents = newComponents;

		// Sort component list
		sortComponentList(allComponents);

	}

	/**
	 * Merges groups from component
	 * 
	 * @param oldGroups
	 *            old groups
	 * @param newGroups
	 *            new groups
	 */
	private void mergeGroups(List<TraceActivationGroupItem> oldGroups,
			List<TraceActivationGroupItem> newGroups) {

		// Loop through old groups
		for (int i = 0; i < oldGroups.size(); i++) {
			TraceActivationGroupItem oldGroup = oldGroups.get(i);

			// Check if the group if found among the new groups
			for (int j = 0; j < newGroups.size(); j++) {
				TraceActivationGroupItem newGroup = newGroups.get(j);

				if (oldGroup.getId() == newGroup.getId()) {
					newGroup.setActivated(oldGroup.isActivated());
				}
			}
		}
	}

	/**
	 * Sorts component list
	 * 
	 * @param components
	 *            component list
	 */
	private void sortComponentList(
			List<TraceActivationComponentItem> componentList) {

		// Sort component list
		Collections.sort(componentList,
				new Comparator<TraceActivationComponentItem>() {

					/**
					 * Compares two components
					 * 
					 * @param o1
					 *            first component
					 * @param o2
					 *            second component
					 * @return less than zero if first one is first in
					 *         alphabetic order. More than zero if second is
					 *         first.
					 */
					public int compare(TraceActivationComponentItem o1,
							TraceActivationComponentItem o2) {
						int val = o1.getName().toLowerCase().compareTo(
								o2.getName().toLowerCase());
						return val;
					}
				});
	}

	/**
	 * This method initializes main composite
	 * 
	 */
	private void createMainComposite() {
		// Create new composite
		Composite mainComposite = new Composite(composite, SWT.NONE);

		// Set grid data to composite
		GridData mainCompositeGridData = new GridData();
		mainCompositeGridData.horizontalAlignment = GridData.FILL;
		mainCompositeGridData.grabExcessHorizontalSpace = false;
		mainCompositeGridData.grabExcessVerticalSpace = false;
		// mainCompositeGridData.horizontalSpan = 5;
		mainCompositeGridData.verticalAlignment = GridData.FILL;
		mainComposite.setLayout(new GridLayout());

		// Set layout data
		mainComposite.setLayoutData(mainCompositeGridData);
	}

	/**
	 * This method initializes dictionary Composite
	 */
	private void createDictionaryComposite() {
		// Create new composite
		Composite dictionaryComposite = new Composite(mainGroup, SWT.NONE);

		// Set grid data to composite
		GridData componentCompositeGridData = new GridData();
		componentCompositeGridData.grabExcessHorizontalSpace = false;
		componentCompositeGridData.grabExcessVerticalSpace = true;
		componentCompositeGridData.horizontalSpan = 2;
		componentCompositeGridData.horizontalAlignment = GridData.FILL;
		componentCompositeGridData.verticalAlignment = GridData.FILL;
		dictionaryComposite.setLayoutData(componentCompositeGridData);

		// Set grid layout to composite
		GridLayout componentCompositeGridLayout = new GridLayout();
		componentCompositeGridLayout.numColumns = 2;
		dictionaryComposite.setLayout(componentCompositeGridLayout);

		// Create Dictionary table
		dictionaryTable = new Table(dictionaryComposite, SWT.BORDER | SWT.MULTI
				| SWT.FULL_SELECTION);
		dictionaryTable.setHeaderVisible(true);
		dictionaryTable.setLinesVisible(false);

		// Set grid data to Dictionary table
		GridData dictionaryTableGridData = new GridData();
		dictionaryTableGridData.horizontalSpan = 2;
		dictionaryTableGridData.heightHint = 250;
		dictionaryTableGridData.widthHint = 200;
		dictionaryTableGridData.grabExcessHorizontalSpace = true;
		dictionaryTableGridData.grabExcessVerticalSpace = true;
		dictionaryTableGridData.horizontalAlignment = GridData.FILL;
		dictionaryTableGridData.verticalAlignment = GridData.FILL;
		dictionaryTable.setLayoutData(dictionaryTableGridData);

		// Create column for table
		TableColumn column = new TableColumn(dictionaryTable, SWT.NONE);
		column.setText(Messages
				.getString("TraceActivationDialog.DictionaryColumnText")); //$NON-NLS-1$
		column.setToolTipText(Messages
				.getString("TraceActivationDialog.DictionaryColumnToolTip")); //$NON-NLS-1$
		column.setWidth(200);

		// Create filter label
		Label dictionaryFilterLabel = new Label(dictionaryComposite, SWT.NONE);
		dictionaryFilterLabel.setText(Messages
				.getString("TraceActivationDialog.FilterInfoText")); //$NON-NLS-1$

		// Create filter text field
		dictionaryFilterText = new Text(dictionaryComposite, SWT.BORDER);
		GridData componentFilterTextGridData = new GridData(SWT.FILL, SWT.FILL,
				true, false);
		dictionaryFilterText.setLayoutData(componentFilterTextGridData);
		dictionaryFilterText.setToolTipText(Messages
				.getString("TraceActivationDialog.DictionaryFilterToolTip")); //$NON-NLS-1$
		if (savedComponentFilter != null && !savedComponentFilter.equals("")) { //$NON-NLS-1$
			dictionaryFilterText.setText(savedComponentFilter);
		}
	}

	/**
	 * This method initializes group Composite
	 */
	private void createGroupComposite() {
		// Create new composite
		Composite groupComposite = new Composite(mainGroup, SWT.NONE);

		// Set grid data to groupComposite
		GridData groupCompositeGridData = new GridData();
		groupCompositeGridData.horizontalSpan = 3;
		groupCompositeGridData.grabExcessHorizontalSpace = true;
		groupCompositeGridData.grabExcessVerticalSpace = true;
		groupCompositeGridData.horizontalAlignment = GridData.FILL;
		groupCompositeGridData.verticalAlignment = GridData.FILL;
		groupComposite.setLayoutData(groupCompositeGridData);

		// Set grid layout to groupComposite
		GridLayout groupCompositeGridLayout = new GridLayout();
		groupCompositeGridLayout.numColumns = 2;
		groupComposite.setLayout(groupCompositeGridLayout);

		// Create group table
		groupTable = new Table(groupComposite, SWT.BORDER | SWT.FULL_SELECTION
				| SWT.MULTI);
		groupTable.setHeaderVisible(true);
		groupTable.setLinesVisible(false);

		// Set grid data to group table
		GridData groupTableGridData = new GridData();
		groupTableGridData.grabExcessHorizontalSpace = true;
		groupTableGridData.grabExcessVerticalSpace = true;
		groupTableGridData.horizontalAlignment = GridData.FILL;
		groupTableGridData.verticalAlignment = GridData.FILL;
		groupTableGridData.heightHint = 250;
		groupTableGridData.widthHint = 450;
		groupTableGridData.horizontalSpan = 2;
		groupTable.setLayoutData(groupTableGridData);

		// Group ID column
		TableColumn groupIdColumn = new TableColumn(groupTable, SWT.NONE);
		groupIdColumn.setText("ID"); //$NON-NLS-1$
		groupIdColumn.setToolTipText(Messages
				.getString("TraceActivationDialog.IdColumnToolTip")); //$NON-NLS-1$
		groupIdColumn.setWidth(45);

		// Group name column
		TableColumn groupNameColumn = new TableColumn(groupTable, SWT.NONE);
		groupNameColumn.setText(Messages
				.getString("TraceActivationDialog.GroupColumnText")); //$NON-NLS-1$
		groupNameColumn.setToolTipText(Messages
				.getString("TraceActivationDialog.GroupColumnToolTip")); //$NON-NLS-1$
		groupNameColumn.setWidth(355);

		// State column
		TableColumn stateColumn = new TableColumn(groupTable, SWT.NONE);
		stateColumn.setText(Messages
				.getString("TraceActivationDialog.StateColumnText")); //$NON-NLS-1$
		stateColumn.setToolTipText(Messages
				.getString("TraceActivationDialog.StateColumnToolTip")); //$NON-NLS-1$
		stateColumn.setWidth(50);

		// Create group filter label
		Label groupFilterLabel = new Label(groupComposite, SWT.NONE);
		groupFilterLabel.setText(Messages
				.getString("TraceActivationDialog.FilterInfoText")); //$NON-NLS-1$

		// Create group filter text field
		groupFilterText = new Text(groupComposite, SWT.BORDER);
		GridData groupFilterTextGridData = new GridData(SWT.FILL, SWT.CENTER,
				true, false);
		groupFilterText.setToolTipText(Messages
				.getString("TraceActivationDialog.GroupFilterToolTip")); //$NON-NLS-1$
		groupFilterText.setLayoutData(groupFilterTextGridData);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.traceviewer.dialog.BaseDialog#createActionListeners()
	 */
	@Override
	public void createActionListeners() {

		// Add selection listener to component Table
		dictionaryTable.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				TraceViewerGlobals.postUiEvent("ComponentTableSelection" //$NON-NLS-1$
						+ dictionaryTable.getSelectionIndex(), "1"); //$NON-NLS-1$
				setGroupsToTable();
				TraceViewerGlobals.postUiEvent("ComponentTableSelection" //$NON-NLS-1$
						+ dictionaryTable.getSelectionIndex(), "0"); //$NON-NLS-1$
			}
		});

		// Add empty drag listener to remove annoyance
		dictionaryTable.addDragDetectListener(new DragDetectListener() {
			public void dragDetected(DragDetectEvent e) {
			}
		});

		// Add mouse listener to component Table
		dictionaryTable.addMouseListener(new MouseListener() {
			public void mouseDoubleClick(MouseEvent e) {
			}

			public void mouseDown(MouseEvent e) {
				if (e.button == RIGHT_MOUSE_BUTTON) {
					Menu menu = new Menu(dictionaryTable.getShell(), SWT.POP_UP);

					// Activate Dictionary item
					MenuItem activateItem = new MenuItem(menu, SWT.PUSH);
					String activateAllText = Messages
							.getString("TraceActivationDialog.ActivateAllText"); //$NON-NLS-1$
					activateItem.setText(activateAllText);
					if (dictionaryTable.getSelectionCount() > 0) {
						activateItem.setEnabled(true);
					} else {
						activateItem.setEnabled(false);
					}
					activateItem.addSelectionListener(new SelectionAdapter() {

						@Override
						public void widgetSelected(SelectionEvent e) {
							activateSelectedComponents();
						}
					});

					// Deactivate Dictionary item
					MenuItem deactivateItem = new MenuItem(menu, SWT.PUSH);
					String deActivateAllText = Messages
							.getString("TraceActivationDialog.DeActivateAllText"); //$NON-NLS-1$
					deactivateItem.setText(deActivateAllText);
					if (dictionaryTable.getSelectionCount() > 0) {
						deactivateItem.setEnabled(true);
					} else {
						deactivateItem.setEnabled(false);
					}
					deactivateItem.addSelectionListener(new SelectionAdapter() {

						@Override
						public void widgetSelected(SelectionEvent e) {
							deactivateSelectedComponents();
						}
					});

					// Draws pop up menu:
					Point pt = new Point(e.x, e.y);
					pt = dictionaryTable.toDisplay(pt);
					menu.setLocation(pt.x, pt.y);
					menu.setVisible(true);
				} else {
					firstDictionarySelected = dictionaryTable
							.getSelectionIndex();
				}
			}

			public void mouseUp(MouseEvent e) {
			}
		});

		// Add mouse move listener to component Table
		dictionaryTable.addMouseMoveListener(new MouseMoveListener() {
			public void mouseMove(MouseEvent e) {
				if ((e.stateMask & SWT.BUTTON1) != 0) {
					TableItem selected = dictionaryTable.getItem(new Point(e.x,
							e.y));

					int oldSelectionLength = dictionaryTable
							.getSelectionCount();

					if (selected != null) {
						int idx = dictionaryTable.indexOf(selected);

						if (idx < firstDictionarySelected) {
							dictionaryTable.setSelection(idx,
									firstDictionarySelected);
						} else {
							dictionaryTable.setSelection(
									firstDictionarySelected, idx);
						}

						// Selection is null, it means we can select everything
					} else {
						dictionaryTable.setSelection(firstDictionarySelected,
								dictionaryTable.getItemCount() - 1);
					}

					// If selection count changed, insert new groups to the
					// group table
					if (oldSelectionLength != dictionaryTable
							.getSelectionCount()) {
						setGroupsToTable();
					}
				}
			}

		});

		// Add selection listener to group Table
		groupTable.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				TraceViewerGlobals.postUiEvent("GroupTableSelection" //$NON-NLS-1$
						+ groupTable.getSelectionIndex(), "1"); //$NON-NLS-1$
				TraceViewerGlobals.postUiEvent("GroupTableSelection" //$NON-NLS-1$
						+ groupTable.getSelectionIndex(), "0"); //$NON-NLS-1$
			}
		});

		// Add empty drag listener to remove annoyance
		groupTable.addDragDetectListener(new DragDetectListener() {
			public void dragDetected(DragDetectEvent e) {
			}
		});

		// Add mouse listener to group Table
		groupTable.addMouseListener(new MouseListener() {

			@SuppressWarnings("unchecked")
			public void mouseDoubleClick(MouseEvent e) {

				// Get selected groups
				TableItem[] selectedGroupItems = groupTable.getSelection();

				// Save top index to jump back after recreation of group table
				int topIndex = groupTable.getTopIndex();

				for (int i = 0; i < selectedGroupItems.length; i++) {

					// If state is both, activate all
					boolean activateAll = false;
					if (selectedGroupItems[i].getForeground().equals(
							STATE_BOTH_COLOR)) {
						activateAll = true;
					}

					// Activate from items
					ArrayList groups = (ArrayList) selectedGroupItems[i]
							.getData();
					for (int j = 0; j < groups.size(); j++) {
						if (activateAll) {
							((TraceActivationGroupItem) groups.get(j))
									.setActivated(true);
						} else if (((TraceActivationGroupItem) groups.get(j))
								.isActivated()) {
							((TraceActivationGroupItem) groups.get(j))
									.setActivated(false);
						} else {
							((TraceActivationGroupItem) groups.get(j))
									.setActivated(true);
						}

						// Add corresponding component to the changed list
						addToChangedList(((TraceActivationGroupItem) groups
								.get(j)).getParent());

					}
				}
				setGroupsToTable();

				// Restore the top index
				groupTable.setTopIndex(topIndex);
			}

			public void mouseDown(MouseEvent e) {
				if (e.button == RIGHT_MOUSE_BUTTON) {
					Menu menu = new Menu(groupTable.getShell(), SWT.POP_UP);

					// Activate groups item
					MenuItem activateItem = new MenuItem(menu, SWT.PUSH);
					activateItem.setText(Messages
							.getString("TraceActivationDialog.ActivateText")); //$NON-NLS-1$
					if (groupTable.getSelectionCount() > 0) {
						activateItem.setEnabled(true);
					} else {
						activateItem.setEnabled(false);
					}
					activateItem.addSelectionListener(new SelectionAdapter() {

						@Override
						public void widgetSelected(SelectionEvent e) {
							activateSelectedGroups();
						}
					});

					// Deactivate groups item
					MenuItem deactivateItem = new MenuItem(menu, SWT.PUSH);
					deactivateItem.setText(Messages
							.getString("TraceActivationDialog.DeActivateText")); //$NON-NLS-1$
					if (groupTable.getSelectionCount() > 0) {
						deactivateItem.setEnabled(true);
					} else {
						deactivateItem.setEnabled(false);
					}
					deactivateItem.addSelectionListener(new SelectionAdapter() {

						@Override
						public void widgetSelected(SelectionEvent e) {
							deactivateSelectedGroups();
						}
					});

					// Draws pop up menu:
					Point pt = new Point(e.x, e.y);
					pt = groupTable.toDisplay(pt);
					menu.setLocation(pt.x, pt.y);
					menu.setVisible(true);
				} else {
					firstGroupSelected = groupTable.getSelectionIndex();
				}
			}

			public void mouseUp(MouseEvent e) {
			}
		});

		// Add mouse move listener to group Table
		groupTable.addMouseMoveListener(new MouseMoveListener() {
			public void mouseMove(MouseEvent e) {
				if ((e.stateMask & SWT.BUTTON1) != 0) {
					TableItem selected = groupTable
							.getItem(new Point(e.x, e.y));
					if (selected != null) {
						int idx = groupTable.indexOf(selected);
						if (idx < firstGroupSelected) {
							groupTable.setSelection(idx, firstGroupSelected);
						} else {
							groupTable.setSelection(firstGroupSelected, idx);
						}

						// Selected is null, we can select all
					} else {
						groupTable.setSelection(firstGroupSelected, groupTable
								.getItemCount() - 1);
					}
				}
			}

		});

		// Add modify listener to component filter
		dictionaryFilterText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				selectComponentsByFilter(false);
				savedComponentFilter = dictionaryFilterText.getText();
			}
		});

		// Add modify listener to group filter
		groupFilterText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				setGroupsToTable();
				if (groupFilterText.getText().length() > 0) {
					groupTable.selectAll();
				}
			}
		});

		// Add Dictionary button
		addDictionaryButton.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				TraceViewerGlobals.getTraceViewer().getView()
						.getActionFactory().getAppendDecodeFileAction().run();
				updateActivationInformation(true);
			}
		});

		// Remove Dictionary button
		removeDictionaryButton.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				int[] componentIds = dictionaryTable.getSelectionIndices();
				String[] removedFilePaths = new String[componentIds.length];

				// Loop through selected indices
				for (int i = 0; i < componentIds.length; i++) {
					TraceActivationComponentItem comp = currentComponents
							.get(componentIds[i]);
					int cid = comp.getId();
					removedFilePaths[i] = comp.getFilePath();

					// Remove component from the model
					TraceViewerGlobals.getDecodeProvider()
							.removeComponentFromModel(cid);

					// Remove components from changed components list
					for (int j = changedComponents.size() - 1; j >= 0; j--) {
						if (changedComponents.get(j).getId() == cid) {
							changedComponents.remove(j);
						}
					}

				}
				// Tell reload action to update files to be watched
				ReloadDecodeFilesAction action = (ReloadDecodeFilesAction) TraceViewerGlobals
						.getTraceViewer().getView().getActionFactory()
						.getReloadDecodeFilesAction();
				action.updateFilesToBeWatched();

				// Update dialog information
				updateActivationInformation(false);

			}
		});
	}

	/**
	 * Activates selected components
	 */
	private void activateSelectedComponents() {
		TraceViewerGlobals.postUiEvent("ComponentActivateButton", "1"); //$NON-NLS-1$ //$NON-NLS-2$

		// Get selected components
		int[] selected = dictionaryTable.getSelectionIndices();
		for (int i = 0; i < selected.length; i++) {

			// Get all groups
			List<TraceActivationGroupItem> groups = currentComponents.get(
					selected[i]).getGroups();
			for (int j = 0; j < groups.size(); j++) {
				groups.get(j).setActivated(true);

				// Add component to changed list
				addToChangedList(groups.get(j).getParent());
			}
		}
		setGroupsToTable();
		TraceViewerGlobals.postUiEvent("ComponentActivateButton", "0"); //$NON-NLS-1$ //$NON-NLS-2$

	}

	/**
	 * Deactivates selected components
	 */
	private void deactivateSelectedComponents() {
		TraceViewerGlobals.postUiEvent("ComponentDeactivateButton", "1"); //$NON-NLS-1$ //$NON-NLS-2$
		// Get selected components
		int[] selected = dictionaryTable.getSelectionIndices();
		for (int i = 0; i < selected.length; i++) {

			// Get all groups
			List<TraceActivationGroupItem> groups = currentComponents.get(
					selected[i]).getGroups();
			for (int j = 0; j < groups.size(); j++) {
				groups.get(j).setActivated(false);

				// Add component to changed list
				addToChangedList(groups.get(j).getParent());
			}
		}
		setGroupsToTable();
		TraceViewerGlobals.postUiEvent("ComponentDeactivateButton", "0"); //$NON-NLS-1$ //$NON-NLS-2$

	}

	/**
	 * Activates selected groups
	 */
	private void activateSelectedGroups() {
		// Save top index to jump back after recreation of group table
		int topIndex = groupTable.getTopIndex();

		TraceViewerGlobals.postUiEvent("GroupActivateButton", "1"); //$NON-NLS-1$ //$NON-NLS-2$

		// Get selection indices
		int[] selectionIndeces = groupTable.getSelectionIndices();

		// Get selected groups
		TableItem[] selectedGroupItems = groupTable.getSelection();
		for (int i = 0; i < selectedGroupItems.length; i++) {
			// Activate from items
			ArrayList<?> groups = (ArrayList<?>) selectedGroupItems[i]
					.getData();
			for (int j = 0; j < groups.size(); j++) {
				TraceActivationGroupItem item = ((TraceActivationGroupItem) groups
						.get(j));
				item.setActivated(true);

				// Add component to changed list
				addToChangedList(item.getParent());
			}
		}

		setGroupsToTable();
		groupTable.setSelection(selectionIndeces);
		groupTable.setFocus();

		// Restore the top index
		groupTable.setTopIndex(topIndex);

		TraceViewerGlobals.postUiEvent("GroupActivateButton", "0"); //$NON-NLS-1$ //$NON-NLS-2$

	}

	/**
	 * Deactivates selected groups
	 */
	private void deactivateSelectedGroups() {
		// Save top index to jump back after recreation of group table
		int topIndex = groupTable.getTopIndex();

		TraceViewerGlobals.postUiEvent("GroupDeactivateButton", "1"); //$NON-NLS-1$ //$NON-NLS-2$
		// Get selectionIndices
		int[] selectionIndeces = groupTable.getSelectionIndices();

		// Get selected groups
		TableItem[] selectedGroupItems = groupTable.getSelection();
		for (int i = 0; i < selectedGroupItems.length; i++) {
			// Activate from items
			ArrayList<?> groups = (ArrayList<?>) selectedGroupItems[i]
					.getData();
			for (int j = 0; j < groups.size(); j++) {
				TraceActivationGroupItem item = ((TraceActivationGroupItem) groups
						.get(j));
				item.setActivated(false);

				// Add component to changed list
				addToChangedList(item.getParent());
			}
		}

		setGroupsToTable();
		groupTable.setSelection(selectionIndeces);
		groupTable.setFocus();

		// Restore the top index
		groupTable.setTopIndex(topIndex);

		TraceViewerGlobals.postUiEvent("GroupDeactivateButton", "0"); //$NON-NLS-1$ //$NON-NLS-2$

	}

	/**
	 * Sets components to the Dictionaries table
	 * 
	 * @param components
	 *            list of components
	 */
	private void setComponentsToTable(
			List<TraceActivationComponentItem> components) {

		// Empty the table first
		dictionaryTable.removeAll();

		// Loop trough component list and create tableitems
		for (int i = 0; i < components.size(); i++) {
			TableItem item = new TableItem(dictionaryTable, SWT.NONE);
			item.setText(components.get(i).getName());
			item.setData(components.get(i));
		}

		// Select the previous one and set groups from that component
		if (dictionaryTable.getItemCount() >= previousSelectedDictionaryIndices.length) {
			dictionaryTable.setSelection(previousSelectedDictionaryIndices);
			setGroupsToTable();
		} else if (dictionaryTable.getItemCount() > 0) {
			dictionaryTable.setSelection(0);
			setGroupsToTable();
		}
	}

	/**
	 * Sets groups
	 */
	private void setGroupsToTable() {
		groupTable.removeAll();
		int[] selected = dictionaryTable.getSelectionIndices();

		// Create TraceActivationGroupTableItems
		List<TraceActivationGroupTableItem> items = createTraceActivationGroupTableItems(selected);

		// Loop through all table items
		for (int i = 0; i < items.size(); i++) {

			StringBuffer text = new StringBuffer();
			String id = "*"; //$NON-NLS-1$

			// More than one components with the same group
			if (items.get(i).getComponentCount() > 1) {

				String multipleGroupsMsg1 = Messages
						.getString("TraceActivationDialog.MultipleGroupsMessage1"); //$NON-NLS-1$
				String multipleGroupsMsg2 = Messages
						.getString("TraceActivationDialog.MultipleGroupsMessage2"); //$NON-NLS-1$

				// Append texts to the buffer
				text.append(items.get(i).getName());
				text.append(multipleGroupsMsg1);
				text.append(items.get(i).getComponentCount());
				text.append(multipleGroupsMsg2);

				// One component's group with many components selected
			} else if (dictionaryTable.getSelectionCount() > 1) {
				String idStr = Integer.toHexString(items.get(i).getId());
				if (idStr.length() == 1) {
					idStr = LEAD_ZERO + idStr;
				}
				id = HEX_PREFIX + idStr;
				text.append(items.get(i).getName());
				text.append(' ');
				text.append('(');
				text.append(items.get(i).getRealGroups().get(0).getParent()
						.getName());
				text.append(')');

				// One component with one component selected
			} else {
				String idStr = Integer.toHexString(items.get(i).getId());
				if (idStr.length() == 1) {
					idStr = LEAD_ZERO + idStr;
				}
				id = HEX_PREFIX + idStr;
				text.append(items.get(i).getName());
			}

			// Check if this group matches the filter
			if (checkGroupFilter(items.get(i).getName())) {

				TableItem item = new TableItem(groupTable, SWT.NONE);
				item.setText(id);
				item.setText(1, text.toString());
				item.setData(items.get(i).getRealGroups());

				String activated = ""; //$NON-NLS-1$
				Color color;
				if (items.get(i).isDifferentStates()) {
					activated = STATE_BOTH_STRING;
					color = STATE_BOTH_COLOR;
				} else if (items.get(i).isActivated()) {
					activated = STATE_ON_STRING;
					color = STATE_ON_COLOR;
				} else {
					activated = STATE_OFF_STRING;
					color = STATE_OFF_COLOR;
				}

				item.setText(2, activated);
				item.setForeground(color);
			}
		}

		// Set remove Dictionary button status
		if (selected.length > 0) {
			removeDictionaryButton.setEnabled(true);
		} else {
			removeDictionaryButton.setEnabled(false);
		}
	}

	/**
	 * Checks if this group name matches the set group filter
	 * 
	 * @param groupName
	 *            group name
	 * @return true if this group should be visible in the table
	 */
	private boolean checkGroupFilter(String groupName) {
		boolean showGroup = true;
		if (!groupFilterText.getText().equals("")) { //$NON-NLS-1$
			if (groupName.toLowerCase().contains(
					groupFilterText.getText().toLowerCase())
					|| groupFilterText.getText().equals("*")) { //$NON-NLS-1$
			} else {
				showGroup = false;
			}
		}
		return showGroup;
	}

	/**
	 * Creates group table items
	 * 
	 * @param selected
	 *            selected components
	 * @return list of group table items to insert to table
	 */
	private List<TraceActivationGroupTableItem> createTraceActivationGroupTableItems(
			int[] selected) {
		List<TraceActivationGroupTableItem> tableItems = new ArrayList<TraceActivationGroupTableItem>();
		List<TraceActivationGroupItem> realItems = new ArrayList<TraceActivationGroupItem>();
		// Loop through selected components
		for (int i = 0; i < selected.length; i++) {
			// Get groups from this component
			List<TraceActivationGroupItem> groups = currentComponents.get(
					selected[i]).getGroups();
			// Insert all groups
			realItems.addAll(groups);
		}

		// Now we have list of all groups, start inserting to the tableItems
		for (int k = 0; k < realItems.size(); k++) {

			// Check that this groups is not in the array yet
			boolean inArray = false;
			for (int l = 0; l < tableItems.size(); l++) {
				// Is in array
				if (realItems.get(k).getName().equals(
						tableItems.get(l).getName())) {
					tableItems.get(l).setComponentCount(
							tableItems.get(l).getComponentCount() + 1);

					// Two different states
					if (realItems.get(k).isActivated() != tableItems.get(l)
							.isActivated()) {
						tableItems.get(l).setDifferentStates(true);
					}

					tableItems.get(l).getRealGroups().add(realItems.get(k));
					inArray = true;
				}
			}
			// If not in array, put it there
			if (!inArray) {
				TraceActivationGroupTableItem newItem = new TraceActivationGroupTableItem();
				newItem.setComponentCount(1);
				newItem.setDifferentStates(false);
				newItem.setId(realItems.get(k).getId());
				newItem.setName(realItems.get(k).getName());
				newItem.setActivated(realItems.get(k).isActivated());
				newItem.getRealGroups().add(realItems.get(k));
				tableItems.add(newItem);
			}
		}
		return tableItems;
	}

	/**
	 * Selects components by filter
	 * 
	 * @param firstTime
	 *            if true, this is called when opening the dialog and not
	 *            because the filter text changed
	 */
	private void selectComponentsByFilter(boolean firstTime) {

		// Remove old components
		if (!dictionaryFilterText.getText().equals("")) { //$NON-NLS-1$
			dictionaryTable.removeAll();
			currentComponents.clear();
			for (int i = 0; i < allComponents.size(); i++) {
				if (allComponents.get(i).getName().toLowerCase().contains(
						dictionaryFilterText.getText().toLowerCase())
						|| dictionaryFilterText.getText().equals("*")) { //$NON-NLS-1$
					currentComponents.add(allComponents.get(i));
				}
			}
			setComponentsToTable(currentComponents);
			dictionaryTable.selectAll();
		} else {
			// Filter removed, copy all items
			copyItems(allComponents, currentComponents);
			setComponentsToTable(currentComponents);

			if (!firstTime) {
				dictionaryTable.deselectAll();
			}
		}

		setGroupsToTable();
	}

	/**
	 * Copies activation items from list to another. Empties the destination
	 * list before copying.
	 * 
	 * @param fromList
	 *            from array
	 * @param toList
	 *            destination array
	 */
	private void copyItems(List<TraceActivationComponentItem> fromList,
			List<TraceActivationComponentItem> toList) {
		toList.clear();
		for (int i = 0; i < fromList.size(); i++) {
			toList.add(fromList.get(i));
		}

	}

	/**
	 * Adds component item to changed list to wait for be sent as activation
	 * message
	 * 
	 * @param item
	 *            item to be added to list
	 */
	private void addToChangedList(TraceActivationComponentItem item) {

		// Add to changed list if doesn't exist yet
		if (!changedComponents.contains(item)) {

			// If a component with same ID is found, remove old one
			for (int i = 0; i < changedComponents.size(); i++) {
				if (changedComponents.get(i).getId() == item.getId()) {
					changedComponents.remove(i);
					break;
				}
			}

			changedComponents.add(item);
		}
	}

	/**
	 * Sets model changed
	 * 
	 * @param modelChanged
	 *            the modelChanged to set
	 */
	public void setModelChanged(boolean modelChanged) {
		this.modelChanged = modelChanged;

		// Dialog open, update
		if (getShell() != null && !getShell().isDisposed()) {
			getActivationInformation();
		}
	}

	/**
	 * Sends activation information
	 */
	private void sendActivationInformation() {
		if (TraceViewerGlobals.getTraceViewer().getConnection() != null
				&& TraceViewerGlobals.getTraceViewer().getConnection()
						.isConnected()) {

			// Set all selected components to be changed
			// Get selected components and add them to changed list
			int[] selected = dictionaryTable.getSelectionIndices();
			for (int i = 0; i < selected.length; i++) {
				TraceActivationComponentItem component = currentComponents
						.get(selected[i]);
				addToChangedList(component);
			}

			// Activate
			new TraceActivator().activate(changedComponents);

			// No connection, post error to Trace Events
		} else {
			String notOpenMsg = Messages
					.getString("TraceActivationDialog.CannotSendConnectionNotOpen"); //$NON-NLS-1$
			TraceViewerGlobals.postErrorEvent(notOpenMsg,
					TRACE_ACTIVATION_CATEGORY, null);
		}
	}

	/**
	 * Checks if the dialog is open
	 * 
	 * @return true if dialog is open
	 */
	public boolean isOpen() {
		boolean isOpen = false;
		if (getShell() != null && !getShell().isDisposed()) {
			isOpen = true;
		}
		return isOpen;
	}

	/**
	 * Sets focus to this dialog
	 */
	public void setFocus() {
		if (!getShell().isDisposed()) {
			getShell().setFocus();
		}
	}
}
