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
 * Base class for tree dialogs
 *
 */
package com.nokia.traceviewer.dialog;

import java.net.URL;

import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSource;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DragSourceListener;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.DropTargetAdapter;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;

import com.nokia.traceviewer.TraceViewerPlugin;
import com.nokia.traceviewer.action.PauseAction;
import com.nokia.traceviewer.dialog.treeitem.TreeCheckboxStateListener;
import com.nokia.traceviewer.dialog.treeitem.TreeItem;
import com.nokia.traceviewer.dialog.treeitem.TreeItemContentProvider;
import com.nokia.traceviewer.dialog.treeitem.TreeItemLabelProvider;
import com.nokia.traceviewer.engine.TraceViewerGlobals;

/**
 * Base class for tree dialogs
 * 
 */
public abstract class BaseTreeDialog extends BaseDialog {

	/**
	 * Tree viewer object
	 */
	protected CheckboxTreeViewer viewer;

	/**
	 * Toolbar where actionbuttons are
	 */
	protected ToolBar toolBar;

	/**
	 * Add group button
	 */
	protected ToolItem addGroupItem;

	/**
	 * Add item button
	 */
	protected ToolItem addItem;

	/**
	 * Edit item button
	 */
	protected ToolItem editItem;

	/**
	 * Remove item button
	 */
	protected ToolItem removeItem;

	/**
	 * Clear all button
	 */
	protected ToolItem clearItem;

	/**
	 * ProgressBar Dialog
	 */
	protected ProgressBarDialog progressBarDialog;

	/**
	 * Content provider
	 */
	protected TreeItemContentProvider contentProvider;

	/**
	 * Checkbox state listener used to check/uncheck many items at once
	 */
	protected TreeCheckboxStateListener checkboxStateListener;

	/**
	 * Tree root
	 */
	protected TreeItem treeRoot;

	/**
	 * Root item in tree
	 */
	protected TreeItem root;

	/**
	 * Selected object when starting drag and drop
	 */
	private TreeItem selectedObject;

	// Images
	/**
	 * Group add image
	 */
	protected Image groupAddImage;

	/**
	 * Item add image
	 */
	protected Image itemAddImage;

	/**
	 * Item edit image
	 */
	protected Image itemEditImage;

	/**
	 * Item remove image
	 */
	protected Image itemRemoveImage;

	/**
	 * Item clear image
	 */
	protected Image clearImage;

	/**
	 * Create group menu item
	 */
	private MenuItem addGroup;

	/**
	 * Add new rule menu item
	 */
	private MenuItem addRule;

	/**
	 * Edit rule menu item
	 */
	private MenuItem editRule;

	/**
	 * Remove rule menu item
	 */
	private MenuItem removeRule;

	/**
	 * Rename configuration menu item
	 */
	private MenuItem renameRule;

	/**
	 * Indicates that something changed while visiting this dialog -> Apply
	 * settings
	 */
	protected boolean somethingChanged = false;

	/**
	 * Checks if DataReader was paused when we entered dialog -> Don't pause
	 * again and don't unpause in exit
	 */
	protected boolean wasPausedWhenEntered = true;

	/**
	 * Tells that we are inserting before item under the mouse cursor when
	 * dragging and dropping items
	 */
	private boolean dndAddBefore;

	/**
	 * Add group image
	 */
	private static final String groupAddImageLocation = "/icons/folderadd.gif"; //$NON-NLS-1$

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
	 * Clear all image
	 */
	private static final String clearImageLocation = "/icons/clear.gif"; //$NON-NLS-1$

	/**
	 * Name of the dialog
	 */
	private static final String dialogName = Messages
			.getString("BaseTreeDialog.DialogName"); //$NON-NLS-1$

	/**
	 * Constructor
	 * 
	 * @param parent
	 *            parent shell
	 * @param contentProvider
	 *            content provider
	 * @param treeRoot
	 *            tree root
	 */
	protected BaseTreeDialog(Shell parent,
			TreeItemContentProvider contentProvider, TreeItem treeRoot) {
		super(parent, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL | SWT.RESIZE);
		this.contentProvider = contentProvider;
		this.treeRoot = treeRoot;
		root = (TreeItem) treeRoot.getChildren()[0];
	}

	/**
	 * Export
	 */
	protected abstract void export();

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.dialogs.TrayDialog#close()
	 */
	@Override
	public boolean close() {
		boolean close = super.close();
		if (!wasPausedWhenEntered) {
			// Unpause
			PauseAction action = (PauseAction) TraceViewerGlobals
					.getTraceViewer().getView().getActionFactory()
					.getPauseAction();
			action.setPaused(false);
		}
		export();
		return close;
	}

	/**
	 * Disposes images when shell is closed
	 */
	protected void dispose() {
		// Dispose images
		if (groupAddImage != null) {
			groupAddImage.dispose();
		}
		if (itemAddImage != null) {
			itemAddImage.dispose();
		}
		if (itemEditImage != null) {
			itemEditImage.dispose();
		}
		if (itemRemoveImage != null) {
			itemRemoveImage.dispose();
		}
		if (clearImage != null) {
			clearImage.dispose();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.traceviewer.dialog.BaseDialog#restoreSettings()
	 */
	@Override
	protected void restoreSettings() {
		// Restore the location of the dialog
		super.restoreSettings();

		// Set clear button enabled if there's more than root
		if (root.getChildren().length > 0) {
			clearItem.setEnabled(true);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.traceviewer.dialog.BaseDialog#createDialogContents()
	 */
	@Override
	protected void createDialogContents() {
		createDialogContents(dialogName);
	}

	/**
	 * Creates dialog contents
	 * 
	 * @param dialogName
	 *            dialog name
	 */
	protected void createDialogContents(String dialogName) {
		doInitialSetup();

		// Shell
		GridLayout shellGridLayout = new GridLayout();
		shellGridLayout.numColumns = 3;
		GridData compositeGridData = new GridData();
		compositeGridData.horizontalAlignment = SWT.FILL;
		compositeGridData.verticalAlignment = SWT.FILL;
		compositeGridData.grabExcessHorizontalSpace = true;
		compositeGridData.grabExcessVerticalSpace = true;
		getShell().setText(dialogName);
		root.setName(dialogName);
		composite.setLayout(shellGridLayout);
		composite.setLayoutData(compositeGridData);

		createToolBar();
		getShell().setMinimumSize(new Point(280, 380));
		createTree();

		// Spacer label
		Label spacer = new Label(composite, SWT.NONE);
		GridData spacerGridData = new GridData(SWT.FILL, SWT.FILL);
		spacerGridData.grabExcessHorizontalSpace = true;
		spacer.setLayoutData(spacerGridData);
	}

	/**
	 * Does initial dialog setup
	 */
	protected void doInitialSetup() {
		// Pause the datareader if it's not paused already
		wasPausedWhenEntered = TraceViewerGlobals.getTraceViewer()
				.getDataReaderAccess().getMainDataReader().isPaused();
		if (!wasPausedWhenEntered) {
			TraceViewerGlobals.getTraceViewer().getView().getActionFactory()
					.getPauseAction().run();
		}
		somethingChanged = false;
	}

	/**
	 * Creates the tree viewer
	 */
	protected void createTree() {
		GridData treeGridData = new GridData();
		treeGridData.horizontalSpan = 3;
		treeGridData.horizontalAlignment = SWT.FILL;
		treeGridData.verticalAlignment = SWT.FILL;
		treeGridData.grabExcessVerticalSpace = true;
		treeGridData.grabExcessHorizontalSpace = true;
		treeGridData.heightHint = 227;
		treeGridData.widthHint = 250;

		// Create Viewer with the checkbox style bit
		viewer = new CheckboxTreeViewer(composite, SWT.CHECK | SWT.BORDER
				| SWT.V_SCROLL | SWT.H_SCROLL);

		viewer.getTree().setLayoutData(treeGridData);
		viewer.setLabelProvider(new TreeItemLabelProvider());
		viewer.setContentProvider(contentProvider);

		// Set root and expand all items
		viewer.setInput(treeRoot);
		viewer.expandAll();

		// Select the root item
		if (viewer.getTree().getItem(0) != null) {
			viewer.getTree().setSelection(viewer.getTree().getItem(0));
		}

		// Create listener for checkboxes
		checkboxStateListener = new TreeCheckboxStateListener(viewer);
		viewer.addCheckStateListener(checkboxStateListener);

		// Drag and Drop support
		createDNDSupport();

		// Context menu
		createContextMenu();

		// Set initial button states
		setButtonStates();
	}

	/**
	 * Creates Drag and Drop support
	 */
	private void createDNDSupport() {
		Transfer[] types = new Transfer[] { TextTransfer.getInstance() };
		int operations = DND.DROP_MOVE;

		// Drag Source definitions
		final DragSource source = new DragSource(viewer.getTree(), operations);
		source.setTransfer(types);

		// Add drag listener
		source.addDragListener(new DragSourceListener() {

			/*
			 * (non-Javadoc)
			 * 
			 * @see
			 * org.eclipse.swt.dnd.DragSourceListener#dragStart(org.eclipse.
			 * swt.dnd.DragSourceEvent)
			 */
			public void dragStart(DragSourceEvent event) {
				// Get selected object
				selectedObject = ((TreeItem) ((IStructuredSelection) viewer
						.getSelection()).getFirstElement());
				if (selectedObject != null) {
					event.doit = true;
				} else {
					event.doit = false;
				}
			}

			/*
			 * (non-Javadoc)
			 * 
			 * @see
			 * org.eclipse.swt.dnd.DragSourceListener#dragSetData(org.eclipse
			 * .swt.dnd.DragSourceEvent)
			 */
			public void dragSetData(DragSourceEvent event) {
				// Have to put some String here
				event.data = "blaa"; //$NON-NLS-1$
			}

			/*
			 * (non-Javadoc)
			 * 
			 * @see
			 * org.eclipse.swt.dnd.DragSourceListener#dragFinished(org.eclipse
			 * .swt.dnd.DragSourceEvent)
			 */
			public void dragFinished(DragSourceEvent event) {
			}
		});

		// Drop Target definitions
		DropTarget target = new DropTarget(viewer.getTree(), operations);
		target.setTransfer(types);

		// Add drop listener
		target.addDropListener(new DropTargetAdapter() {

			/*
			 * (non-Javadoc)
			 * 
			 * @see
			 * org.eclipse.swt.dnd.DropTargetAdapter#dragOver(org.eclipse.swt
			 * .dnd.DropTargetEvent)
			 */
			@Override
			public void dragOver(DropTargetEvent event) {
				event.feedback = DND.FEEDBACK_EXPAND | DND.FEEDBACK_SCROLL;
				if (event.item != null) {
					org.eclipse.swt.widgets.TreeItem item = (org.eclipse.swt.widgets.TreeItem) event.item;
					Point pt = getShell().getDisplay().map(null,
							viewer.getTree(), event.x, event.y);
					Rectangle bounds = item.getBounds();
					TreeItem it = (TreeItem) item.getData();
					dndAddBefore = false;
					if (pt.y < bounds.y + bounds.height / 3) {
						dndAddBefore = true;
						event.feedback = DND.FEEDBACK_INSERT_BEFORE;
					} else if (pt.y > bounds.y + 2 * bounds.height / 3) {
						event.feedback = DND.FEEDBACK_INSERT_AFTER;
					} else if (!it.isGroup()) {
						event.feedback = DND.FEEDBACK_NONE;
					} else {
						event.feedback = DND.FEEDBACK_SELECT;
					}
				}
			}

			/*
			 * (non-Javadoc)
			 * 
			 * @see
			 * org.eclipse.swt.dnd.DropTargetAdapter#drop(org.eclipse.swt.dnd
			 * .DropTargetEvent)
			 */
			@Override
			public void drop(DropTargetEvent event) {

				// Dragging something
				if (selectedObject != null) {

					TreeItem parent = null;

					// Something is selected
					if (event.item != null) {

						// Get parent node under cursor
						parent = (TreeItem) ((org.eclipse.swt.widgets.TreeItem) event.item)
								.getData();

						// if parent is group, add item to end of list
						if (parent.isGroup()) {

							boolean addingOk = true;

							// Moving from the same group
							if (selectedObject.getParent().equals(parent)) {
								parent.removeChild(selectedObject);
								// Moving to another group
							} else {
								// Don't allow moving to itself
								if (!parent.equals(selectedObject)) {

									((TreeItem) selectedObject.getParent())
											.removeChild(selectedObject);
								} else {
									addingOk = false;
								}
							}

							// Add to new parent
							if (addingOk) {
								parent.addChild(selectedObject);
								selectedObject.setParent(parent);
								// If group has children, expand it
								if (selectedObject.isGroup()
										&& selectedObject.getChildren()[0] != null) {
									viewer.expandToLevel(selectedObject
											.getChildren()[0], 0);
								}
							}

							// Parent is not group
						} else {

							// Get parent group of the selected item
							TreeItem realParent = (TreeItem) parent.getParent();

							// Check that we aren't moving folder to a item
							// inside the same folder
							if (!selectedObject.equals(realParent)) {

								// Remove from old parent
								((TreeItem) selectedObject.getParent())
										.removeChild(selectedObject);

								// Get position of parent
								int pos = 0;

								// Go through children to find this one
								Object[] children = ((TreeItem) parent
										.getParent()).getChildren();
								for (int i = 0; i < children.length; i++) {
									if (children[i].equals(parent)) {
										pos = i;
										break;
									}
								}

								// Insert after the item under cursor, add
								// position by one
								if (!dndAddBefore) {
									pos++;
								}

								// Add to new parent
								realParent.addChild(pos, selectedObject);
								selectedObject.setParent(realParent);

								// If group has children, expand it
								if (selectedObject.isGroup()
										&& selectedObject.getChildren()[0] != null) {
									viewer.expandToLevel(selectedObject
											.getChildren()[0], 0);
								}

							}
						}
					} else {
						parent = (TreeItem) getParentGroup();

						// Remove from old parent
						((TreeItem) selectedObject.getParent())
								.removeChild(selectedObject);

						// Add to new parent
						parent.addChild(selectedObject);
						selectedObject.setParent(parent);

						// If group has children, expand it
						if (selectedObject.isGroup()
								&& selectedObject.getChildren()[0] != null) {
							viewer.expandToLevel(
									selectedObject.getChildren()[0], 0);
						}

					}
				}
				// Set button states
				setButtonStates();
			}
		});
	}

	/**
	 * Create toolbar with default image locations
	 */
	protected void createToolBar() {
		createToolBar(itemAddImageLocation, itemEditImageLocation,
				itemRemoveImageLocation);
	}

	/**
	 * Create toolbar with given image locations
	 * 
	 * @param itemAddImageLocation
	 *            add image location
	 * @param itemEditImageLocation
	 *            edit image location
	 * @param itemRemoveImageLocation
	 *            remove image location
	 */
	protected void createToolBar(String itemAddImageLocation,
			String itemEditImageLocation, String itemRemoveImageLocation) {
		URL url = null;
		GridData toolBarGridData = new GridData();
		toolBarGridData.horizontalSpan = 3;
		toolBar = new ToolBar(composite, SWT.NONE);
		toolBar.setLayoutData(toolBarGridData);

		addGroupItem = new ToolItem(toolBar, SWT.PUSH);
		addGroupItem
				.setText(Messages.getString("BaseTreeDialog.GroupItemText")); //$NON-NLS-1$
		addGroupItem.setToolTipText(Messages
				.getString("BaseTreeDialog.AddGroupToolTip")); //$NON-NLS-1$
		url = TraceViewerPlugin.getDefault().getBundle().getEntry(
				groupAddImageLocation);
		groupAddImage = ImageDescriptor.createFromURL(url).createImage();
		addGroupItem.setImage(groupAddImage);

		addItem = new ToolItem(toolBar, SWT.PUSH);
		addItem.setText(Messages.getString("BaseTreeDialog.AddItemText")); //$NON-NLS-1$
		addItem.setToolTipText(Messages
				.getString("BaseTreeDialog.AddItemToolTip")); //$NON-NLS-1$
		url = TraceViewerPlugin.getDefault().getBundle().getEntry(
				itemAddImageLocation);
		itemAddImage = ImageDescriptor.createFromURL(url).createImage();
		addItem.setImage(itemAddImage);

		editItem = new ToolItem(toolBar, SWT.PUSH);
		editItem.setText(Messages.getString("BaseTreeDialog.EditItemText")); //$NON-NLS-1$
		editItem.setToolTipText(Messages
				.getString("BaseTreeDialog.EditItemToolTip")); //$NON-NLS-1$
		url = TraceViewerPlugin.getDefault().getBundle().getEntry(
				itemEditImageLocation);
		itemEditImage = ImageDescriptor.createFromURL(url).createImage();
		editItem.setImage(itemEditImage);
		editItem.setEnabled(false);

		removeItem = new ToolItem(toolBar, SWT.PUSH);
		removeItem.setText(Messages.getString("BaseTreeDialog.RemoveItemText")); //$NON-NLS-1$
		removeItem.setToolTipText(Messages
				.getString("BaseTreeDialog.RemoveItemToolTip")); //$NON-NLS-1$
		url = TraceViewerPlugin.getDefault().getBundle().getEntry(
				itemRemoveImageLocation);
		itemRemoveImage = ImageDescriptor.createFromURL(url).createImage();
		removeItem.setImage(itemRemoveImage);
		removeItem.setEnabled(false);

		clearItem = new ToolItem(toolBar, SWT.PUSH);
		clearItem.setText(Messages.getString("BaseTreeDialog.ClearItemText")); //$NON-NLS-1$
		clearItem.setToolTipText(Messages
				.getString("BaseTreeDialog.ClearItemToolTip")); //$NON-NLS-1$
		url = TraceViewerPlugin.getDefault().getBundle().getEntry(
				clearImageLocation);
		clearImage = ImageDescriptor.createFromURL(url).createImage();
		clearItem.setImage(clearImage);
		clearItem.setEnabled(false);
	}

	/**
	 * Sets button states depending on selected item
	 */
	protected void setButtonStates() {
		// Get selection
		Object selection = ((IStructuredSelection) viewer.getSelection())
				.getFirstElement();

		// If selection is null
		if (selection == null) {
			addItem.setEnabled(false);
			addRule.setEnabled(false);
			editItem.setEnabled(false);
			editRule.setEnabled(false);
			removeItem.setEnabled(false);
			removeRule.setEnabled(false);
			addGroupItem.setEnabled(false);
			addGroup.setEnabled(false);
			renameRule.setEnabled(false);
			// Root node is selected
		} else if (selection.equals(root)) {
			addItem.setEnabled(true);
			addRule.setEnabled(true);
			editItem.setEnabled(false);
			editRule.setEnabled(false);
			removeItem.setEnabled(false);
			removeRule.setEnabled(false);
			addGroupItem.setEnabled(true);
			addGroup.setEnabled(true);
			renameRule.setEnabled(false);
			// Something else than group is selected
		} else if (!((TreeItem) selection).isGroup()) {
			addItem.setEnabled(true);
			addRule.setEnabled(true);
			editItem.setEnabled(true);
			editRule.setEnabled(true);
			removeItem.setEnabled(true);
			removeRule.setEnabled(true);
			addGroupItem.setEnabled(true);
			addGroup.setEnabled(true);
			renameRule.setEnabled(true);
			// Group is selected
		} else if (((TreeItem) selection).isGroup()) {
			addItem.setEnabled(true);
			addRule.setEnabled(true);
			editItem.setEnabled(false);
			editRule.setEnabled(false);
			removeItem.setEnabled(true);
			removeRule.setEnabled(true);
			addGroupItem.setEnabled(true);
			addGroup.setEnabled(true);
			renameRule.setEnabled(true);
		}

		// If root has children, enable clear button
		if (root.getChildren().length > 0) {
			clearItem.setEnabled(true);
		} else {
			clearItem.setEnabled(false);
		}
	}

	/**
	 * Gets progressBarDialog
	 * 
	 * @return progressBarDialog
	 */
	public ProgressBarDialog getProgressBar() {
		return progressBarDialog;
	}

	/**
	 * Sets something changed in this dialog
	 * 
	 * @param somethingChanged
	 *            the somethingChanged to set
	 */
	public void setSomethingChanged(boolean somethingChanged) {
		this.somethingChanged = somethingChanged;
	}

	/**
	 * Returns parent group
	 * 
	 * @return parent group of the selected item
	 */
	private Object getParentGroup() {
		// Get selected object
		Object selection = ((IStructuredSelection) viewer.getSelection())
				.getFirstElement();

		// Get parent of the selected object
		if (selection != null) {
			selection = ((TreeItem) selection).getParent();
		}
		return selection;
	}

	/**
	 * Returns selected group
	 * 
	 * @return selected group
	 */
	protected Object getSelectedGroup() {
		// Get selected object
		Object selection = ((IStructuredSelection) viewer.getSelection())
				.getFirstElement();

		// If selected item is not group, take parent of selected item
		if (selection != null && !((TreeItem) selection).isGroup()) {
			selection = ((TreeItem) selection).getParent();
		}
		return selection;
	}

	/**
	 * Creates context menu
	 */
	private void createContextMenu() {
		// Main menu
		Menu menu = new Menu(composite);

		// Add group item
		addGroup = new MenuItem(menu, SWT.PUSH);
		addGroup.setImage(groupAddImage);
		addGroup
				.setText(Messages.getString("BaseTreeDialog.GroupMenuItemText")); //$NON-NLS-1$

		// Add rule item
		addRule = new MenuItem(menu, SWT.PUSH);
		addRule.setImage(itemAddImage);
		addRule.setText(Messages.getString("BaseTreeDialog.AddRuleText")); //$NON-NLS-1$

		// Edit rule item
		editRule = new MenuItem(menu, SWT.PUSH);
		editRule.setImage(itemEditImage);
		editRule.setText(Messages.getString("BaseTreeDialog.EditRuleText")); //$NON-NLS-1$

		// Remove rule item
		removeRule = new MenuItem(menu, SWT.PUSH);
		removeRule.setImage(itemRemoveImage);
		removeRule.setText(Messages.getString("BaseTreeDialog.RemoveRuleText")); //$NON-NLS-1$

		// Rename rule item
		renameRule = new MenuItem(menu, SWT.PUSH);
		renameRule.setText(Messages.getString("BaseTreeDialog.RenameRuleText")); //$NON-NLS-1$

		// Set menu to tree
		viewer.getTree().setMenu(menu);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.traceviewer.dialog.BaseDialog#createActionListeners()
	 */
	@Override
	public void createActionListeners() {

		// Add selection listener to add group button
		addGroupItem.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				TraceViewerGlobals.postUiEvent("AddGroupButton", "1"); //$NON-NLS-1$ //$NON-NLS-2$
				processAddGroupAction();
				TraceViewerGlobals.postUiEvent("AddGroupButton", "0"); //$NON-NLS-1$ //$NON-NLS-2$
			}
		});

		// Add selection listener to add group menu item
		addGroup.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				TraceViewerGlobals.postUiEvent("AddGroupMenuItem", "1"); //$NON-NLS-1$ //$NON-NLS-2$
				processAddGroupAction();
				TraceViewerGlobals.postUiEvent("AddGroupMenuItem", "0"); //$NON-NLS-1$ //$NON-NLS-2$
			}
		});

		// Add selection listener to Add button
		addItem.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				TraceViewerGlobals.postUiEvent("AddRuleButton", "1"); //$NON-NLS-1$ //$NON-NLS-2$
				processAddItemAction();
				TraceViewerGlobals.postUiEvent("AddRuleButton", "0"); //$NON-NLS-1$ //$NON-NLS-2$
			}
		});

		// Add selection listener to Add menu item
		addRule.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				TraceViewerGlobals.postUiEvent("AddRuleMenuItem", "1"); //$NON-NLS-1$ //$NON-NLS-2$
				processAddItemAction();
				TraceViewerGlobals.postUiEvent("AddRuleMenuItem", "0"); //$NON-NLS-1$ //$NON-NLS-2$
			}
		});

		// Add selection listener to Edit button
		editItem.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				TraceViewerGlobals.postUiEvent("EditRuleButton", "1"); //$NON-NLS-1$ //$NON-NLS-2$
				processEditItemAction();
				TraceViewerGlobals.postUiEvent("EditRuleButton", "0"); //$NON-NLS-1$ //$NON-NLS-2$
			}
		});

		// Add selection listener to Edit menu item
		editRule.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				TraceViewerGlobals.postUiEvent("EditRuleMenuItem", "1"); //$NON-NLS-1$ //$NON-NLS-2$
				processEditItemAction();
				TraceViewerGlobals.postUiEvent("EditRuleMenuItem", "0"); //$NON-NLS-1$ //$NON-NLS-2$
			}
		});

		// Add selection listener to Remove button
		removeItem.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				TraceViewerGlobals.postUiEvent("RemoveRuleButton", "1"); //$NON-NLS-1$ //$NON-NLS-2$
				processRemoveItemAction();
				TraceViewerGlobals.postUiEvent("RemoveRuleButton", "0"); //$NON-NLS-1$ //$NON-NLS-2$
			}
		});

		// Add selection listener to Remove menu item
		removeRule.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				TraceViewerGlobals.postUiEvent("RemoveRuleMenuItem", "1"); //$NON-NLS-1$ //$NON-NLS-2$
				processRemoveItemAction();
				TraceViewerGlobals.postUiEvent("RemoveRuleMenuItem", "0"); //$NON-NLS-1$ //$NON-NLS-2$
			}
		});

		// Add selection listener to Clear button
		clearItem.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				TraceViewerGlobals.postUiEvent("ClearRulesButton", "1"); //$NON-NLS-1$ //$NON-NLS-2$
				MessageBox messageBox = new MessageBox(getShell(),
						SWT.ICON_QUESTION | SWT.YES | SWT.NO);
				messageBox.setMessage(Messages
						.getString("BaseTreeDialog.ClearAllConfirmation")); //$NON-NLS-1$
				messageBox.setText(Messages
						.getString("BaseTreeDialog.ClearAllDialogTitle")); //$NON-NLS-1$
				int response = messageBox.open();
				if (response == SWT.YES) {
					// Clear all
					Object[] children = root.getChildren();

					for (int i = 0; i < children.length; i++) {
						root.removeChild((TreeItem) children[i]);
					}
					viewer.setGrayChecked(root, false);
					setButtonStates();
					somethingChanged = true;
				}
				TraceViewerGlobals.postUiEvent("ClearRulesButton", "0"); //$NON-NLS-1$ //$NON-NLS-2$
			}
		});

		// Add selection listener to Tree
		viewer.getTree().addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				TraceViewerGlobals.postUiEvent("TreeSelection", "1"); //$NON-NLS-1$ //$NON-NLS-2$
				setButtonStates();
				TraceViewerGlobals.postUiEvent("TreeSelection", "0"); //$NON-NLS-1$ //$NON-NLS-2$
			}
		});

		// Add selection listener to Rename menu item
		renameRule.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				TraceViewerGlobals.postUiEvent("RenameMenuItem", "1"); //$NON-NLS-1$ //$NON-NLS-2$
				Object selection = ((IStructuredSelection) viewer
						.getSelection()).getFirstElement();

				// If something is selected, show rename dialog
				if (selection != null) {
					TreeItem selected = (TreeItem) selection;
					String dialogTitle = Messages
							.getString("BaseTreeDialog.RenameDialogTitle"); //$NON-NLS-1$
					String dialogText = Messages
							.getString("BaseTreeDialog.RenameDialogText"); //$NON-NLS-1$
					InputDialog dialog = new InputDialog(getShell(),
							dialogTitle, dialogText, selected.getName(), null);
					int ret = dialog.open();
					if (ret == Window.OK) {
						selected.setName(dialog.getValue());
					}
					viewer.refresh();
				}
				TraceViewerGlobals.postUiEvent("RenameMenuItem", "0"); //$NON-NLS-1$ //$NON-NLS-2$
			}
		});

	}

	/**
	 * Removes selected item from the tree
	 */
	protected void processRemoveItemAction() {
		Object selection = ((IStructuredSelection) viewer.getSelection())
				.getFirstElement();
		if (selection != null && !selection.equals(treeRoot)
				&& !selection.equals(root)) {

			// Remove selection
			Object parent = ((TreeItem) selection).getParent();
			((TreeItem) parent).removeChild((TreeItem) selection);
			checkboxStateListener.checkStateChanged((TreeItem) parent);

			// Select parent from the deleted item
			viewer.setSelection(new StructuredSelection(parent), true);
			viewer.reveal(parent);
		}

		setButtonStates();
		somethingChanged = true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.dialogs.Dialog#okPressed()
	 */
	@Override
	protected void okPressed() {
		processApplyButtonAction();
		super.okPressed();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.dialogs.Dialog#cancelPressed()
	 */
	@Override
	protected void cancelPressed() {
		somethingChanged = false;
		super.cancelPressed();
	}

	/**
	 * Process Add action
	 * 
	 * @return new tree item or null if nothing was created
	 */
	protected TreeItem processAddItemAction() {
		Object selection = getSelectedGroup();
		BasePropertyDialog dialog = getPropertyDialog(selection, false);

		// Open dialog
		TreeItem item = dialog.openAndGetItem();
		if (item != null) {
			viewer.expandToLevel(item, 0);
			viewer.setChecked(item, true);
			checkboxStateListener.checkStateChanged(item);

			setButtonStates();
			somethingChanged = true;
		}

		return item;
	}

	/**
	 * Process Edit action
	 * 
	 * @return modified tree item or null if item was not edited
	 */
	protected TreeItem processEditItemAction() {
		TreeItem item = null;

		// Get selected object
		Object selection = ((IStructuredSelection) viewer.getSelection())
				.getFirstElement();

		if (selection != null) {
			boolean wasChecked = viewer.getChecked(selection);
			BasePropertyDialog dialog = getPropertyDialog(selection, true);
			item = dialog.openAndGetItem();

			if (item != null) {

				// If selection was checked, check it again
				if (wasChecked) {
					viewer.setChecked(item, true);
					checkboxStateListener.checkStateChanged(item);
				}

				viewer.setSelection(new StructuredSelection(item), true);
				viewer.reveal(item);
				setButtonStates();
				somethingChanged = true;
			}
		}

		return item;
	}

	/**
	 * Process Add group action
	 */
	protected abstract void processAddGroupAction();

	/**
	 * Get property dialog for this dialog
	 * 
	 * @param selection
	 *            selected object from the tree
	 * @param editOldItem
	 *            if true, edit old item
	 * @return property dialog
	 */
	protected abstract BasePropertyDialog getPropertyDialog(Object selection,
			boolean editOldItem);

	/**
	 * Process Apply button action
	 */
	protected abstract void processApplyButtonAction();

}
