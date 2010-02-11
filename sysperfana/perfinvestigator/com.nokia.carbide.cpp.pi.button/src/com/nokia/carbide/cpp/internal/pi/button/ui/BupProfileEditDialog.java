/*
 * Copyright (c) 2009 Nokia Corporation and/or its subsidiary(-ies). 
 * All rights reserved.
 * This component and the accompanying materials are made available
 * under the terms of the License "Eclipse Public License v1.0"
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
 */

package com.nokia.carbide.cpp.internal.pi.button.ui;

import java.util.Set;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.ui.PlatformUI;

import com.nokia.carbide.cpp.pi.button.BupEventMapManager;
import com.nokia.carbide.cpp.pi.button.ComNokiaCarbidePiButtonHelpIDs;
import com.nokia.carbide.cpp.pi.button.IBupEventMap;
import com.nokia.carbide.cpp.pi.button.IBupEventMapProfile;
import com.nokia.carbide.cpp.ui.CarbideUIPlugin;
import com.nokia.carbide.cpp.ui.ICarbideSharedImages;

public class BupProfileEditDialog extends TitleAreaDialog {
	
	private Composite content = null;
	private Group localeMappingGroup = null;
	private Composite mappingComposite = null;
	private BupMapTableViewer mappingTableViewer = null;
	private Composite buttonComposite = null;
	private Button addButton = null;
	private Button removeButton = null;
	private Button editButton = null;
	private Button clearButton = null;
	
	class BupEditEntry {
		public String enumString = null;
		public String label = null;
		boolean changed = false;
	};

	private IBupEventMapProfile profileForThisEdit = null;
	private IBupEventMap mapForThisEdit = null;
	private ModifyCachedBupEventMap cachedMap = null;
	protected BupProfileEditDialog(Shell shell, IBupEventMapProfile myProfile) {
		super(shell);
		super.setShellStyle(super.getShellStyle() | SWT.RESIZE);
		profileForThisEdit = myProfile;
		mapForThisEdit = BupEventMapManager.getInstance().captureMap(profileForThisEdit);
		cachedMap = new ModifyCachedBupEventMap(mapForThisEdit);
	}
	
	private void cleanUp() {
		BupEventMapManager.getInstance().releaseMap(mapForThisEdit);
	}
	
	public Control createDialogArea(Composite parent) {
		getShell().setText(Messages.getString("BupProfileEditDialog.editingProfile") + profileForThisEdit.getProfileId()); //$NON-NLS-1$
		setTitleImage(CarbideUIPlugin.getSharedImages().getImageDescriptor(ICarbideSharedImages.IMG_PI_METER_20_20).createImage());
		setTitle(Messages.getString("BupProfileEditDialog.profile") + profileForThisEdit.getProfileId()); //$NON-NLS-1$
				
		GridLayoutFactory layoutExpandBoth = GridLayoutFactory.fillDefaults();
		GridDataFactory gridDataExpandBoth = GridDataFactory.fillDefaults().grab(true, true);
				
		GridLayoutFactory gridLayoutButton = GridLayoutFactory.swtDefaults();
		GridDataFactory gridDataButton = GridDataFactory.swtDefaults();
		
		content = new Composite(parent, SWT.NONE);
		layoutExpandBoth.applyTo(content);
		gridDataExpandBoth.applyTo(content);
		
		PlatformUI.getWorkbench().getHelpSystem().setHelp(content, ComNokiaCarbidePiButtonHelpIDs.PI_PROFILE_EDIT_DIALOG);
		
		localeMappingGroup = new Group (content, SWT.NONE);
		layoutExpandBoth.applyTo(localeMappingGroup);
		gridDataExpandBoth.applyTo(localeMappingGroup);
		
		mappingComposite = new Composite (localeMappingGroup, SWT.NONE);
		layoutExpandBoth.copy().numColumns(2).applyTo(mappingComposite);
		gridDataExpandBoth.applyTo(mappingComposite);
		mappingTableViewer = new BupMapTableViewer(mappingComposite, true);
		Table mappingTable = mappingTableViewer.getTable();
		// enable the edit button only when a single file filter is selected
		mappingTable.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent arg0) {
				handleViewerSelection();
			}
		});	

		int maxWidth;
		buttonComposite = new Composite (mappingComposite, SWT.NONE);
		gridLayoutButton.applyTo(buttonComposite);
		gridDataButton.copy().align(SWT.CENTER, SWT.FILL).grab(false, true).applyTo(buttonComposite);
		addButton = new Button (buttonComposite, SWT.NONE);
		addButton.setText(Messages.getString("BupProfileEditDialog.add")); //$NON-NLS-1$
		addButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent arg0) {
				handleAdd();
			}
		});	
		maxWidth = buttonComposite.computeSize(SWT.DEFAULT, SWT.DEFAULT).x;
		editButton = new Button (buttonComposite, SWT.NONE);
		editButton.setText(Messages.getString("BupProfileEditDialog.edit")); //$NON-NLS-1$
		editButton.addSelectionListener(new SelectionAdapter() {	
			public void widgetSelected(SelectionEvent arg0) {
				handleEdit();
			}	
		});
		maxWidth = Math.max(maxWidth, editButton.computeSize(SWT.DEFAULT, SWT.DEFAULT).x);
		removeButton = new Button (buttonComposite, SWT.NONE);
		removeButton.setText(Messages.getString("BupProfileEditDialog.remove")); //$NON-NLS-1$
		removeButton.addSelectionListener(new SelectionAdapter() {	
			public void widgetSelected(SelectionEvent arg0) {
				handleRemove();
			}	
		});
		maxWidth = Math.max(maxWidth, removeButton.computeSize(SWT.DEFAULT, SWT.DEFAULT).x);
		//dummy label as divider
		new Label(buttonComposite, SWT.NONE);
		new Label(buttonComposite, SWT.NONE);
		new Label(buttonComposite, SWT.NONE);
		new Label(buttonComposite, SWT.NONE);
		new Label(buttonComposite, SWT.NONE);
		clearButton = new Button(buttonComposite, SWT.NONE);
		clearButton.setText(Messages.getString("BupProfileEditDialog.clearProfile")); //$NON-NLS-1$
		clearButton.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent arg0) {
			}

			public void widgetSelected(SelectionEvent arg0) {
				if (MessageDialog.openQuestion(getShell(), Messages.getString("BupProfileEditDialog.clearProfile"), Messages.getString("BupProfileEditDialog.clearAllConfirm"))) { //$NON-NLS-1$ //$NON-NLS-2$
					Set<Integer> keySet = cachedMap.getKeyCodeSet();
					Integer [] keyCodes = keySet.toArray(new Integer[keySet.size()]);
					for (Integer keyCode : keyCodes) {
						cachedMap.removeMapping(keyCode);
					}
					refreshTable();
				}
			}
		});
		maxWidth = Math.max(maxWidth, clearButton.computeSize(SWT.DEFAULT, SWT.DEFAULT).x);
		
		gridDataButton.hint(maxWidth, addButton.computeSize(SWT.DEFAULT, SWT.DEFAULT).y).applyTo(addButton);
		gridDataButton.hint(maxWidth, removeButton.computeSize(SWT.DEFAULT, SWT.DEFAULT).y).applyTo(removeButton);
		gridDataButton.hint(maxWidth, editButton.computeSize(SWT.DEFAULT, SWT.DEFAULT).y).applyTo(editButton);
		gridDataButton.hint(maxWidth, clearButton.computeSize(SWT.DEFAULT, SWT.DEFAULT).y).applyTo(clearButton);
		
		initialize();

		return content;
	}

	/**
	 * 
	 */
	protected void handleViewerSelection() {
		boolean enableEdit = (mappingTableViewer.getTable().getSelectionCount() == 1);
		editButton.setEnabled(enableEdit);
		boolean enableRemove = (mappingTableViewer.getTable().getSelectionCount() > 0);
		removeButton.setEnabled(enableRemove);
	}
	
	/**
	 * 
	 */
	protected void handleRemove() {
		ISelection selection = mappingTableViewer.getSelection();
		if (selection instanceof IStructuredSelection) {
			IStructuredSelection ss = (IStructuredSelection) selection;
			Object element = ss.getFirstElement();
			if (element instanceof BupMapTableViewer.BupMapEntry) {
				BupMapTableViewer.BupMapEntry entry = (BupMapTableViewer.BupMapEntry)element;
				cachedMap.removeMapping(entry.keyCode);
				refreshTable();
			}
		}
		removeButton.setEnabled(false);	// selection is gone, let next selection re-enable it
		refreshTable();
	}

	/**
	 * 
	 */
	protected void handleAdd() {
		BupMapAddDialog dialog = new BupMapAddDialog(getShell(), cachedMap, 0, "", ""); //$NON-NLS-1$ //$NON-NLS-2$
		if (dialog.open() == OK) {
			cachedMap.addMapping(dialog.getKeyCode(), dialog.getEnumString(), dialog.getLabel());
		}
		refreshTable();
	}


	/**
	 * 
	 */
	protected void handleEdit() {
		ISelection selection = mappingTableViewer.getSelection();
		if (selection instanceof IStructuredSelection) {
			IStructuredSelection ss = (IStructuredSelection) selection;
			Object element = ss.getFirstElement();
			if (element instanceof BupMapTableViewer.BupMapEntry) {
				BupMapTableViewer.BupMapEntry entry = (BupMapTableViewer.BupMapEntry)element;
				BupMapEditDialog dialog = new BupMapEditDialog(getShell(), entry.keyCode, entry.enumString, entry.label);
				if (dialog.open() == OK) {
					cachedMap.removeMapping(entry.keyCode);
					cachedMap.addMapping(entry.keyCode, dialog.getEnumString(), dialog.getLabel());
				}
			}
		}
		refreshTable();
	}

	private void initialize() {
		// initialize
		editButton.setEnabled(false);
		removeButton.setEnabled(false);
		refreshTable();
	}
	
	public void okPressed() {
		super.okPressed();
		if (cachedMap.haveUncommitedChanges()) {
			cachedMap.commitChanges();
			BupEventMapManager.getInstance().commitEditToWorkspace(mapForThisEdit);
		}
		cleanUp();
	}
	
	public void cancelPressed() {
		if (cachedMap.haveUncommitedChanges()) {
			MessageDialog dialog = new MessageDialog(getShell(), 
					Messages.getString("BupProfileEditDialog.uncommittedChanges"),  //$NON-NLS-1$
					null, 
					Messages.getString("BupProfileEditDialog.saveChanges") + profileForThisEdit.getProfileId() + "?",  //$NON-NLS-1$ //$NON-NLS-2$
					MessageDialog.QUESTION, 
					new String[] { IDialogConstants.YES_LABEL,
					IDialogConstants.NO_LABEL,
					IDialogConstants.CANCEL_LABEL }, 0); // default yes
			switch (dialog.open()) {
			case 0: // yes
				cachedMap.commitChanges();	// fall thru to no
			case 1: // no
				super.cancelPressed();
				cleanUp();
			case 2:	// cancel
			default:
				return;
			}
		}
		super.cancelPressed();
		cleanUp();
	}
	
	// Override OK to look like save
	protected Button createButton(Composite parent, int id, String label, boolean defaultButton) {
		Button myButton = super.createButton(parent, id, label, defaultButton);
		if (id == IDialogConstants.OK_ID) {
			myButton.setText(Messages.getString("BupProfileEditDialog.save")); //$NON-NLS-1$
		}
		return myButton;
	}
	
	private void refreshTable() {
		mappingTableViewer.loadMap(cachedMap);
	}
}
