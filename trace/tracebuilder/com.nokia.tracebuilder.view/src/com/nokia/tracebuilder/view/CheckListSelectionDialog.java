/*
* Copyright (c) 2007 Nokia Corporation and/or its subsidiary(-ies). 
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
* Check list selection dialog
*
*/
package com.nokia.tracebuilder.view;

import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

import com.nokia.tracebuilder.engine.CheckListDialogEntry;
import com.nokia.tracebuilder.engine.TraceBuilderDialogs.CheckListDialogParameters;

/**
 * Check list selection dialog
 * 
 */
class CheckListSelectionDialog extends TitleAreaDialog {

	/**
	 * Table width
	 */
	private static final int TREE_WIDTH = 400; // CodForChk_Dis_Magic

	/**
	 * Table height
	 */
	private static final int TREE_HEIGHT = 300; // CodForChk_Dis_Magic

	/**
	 * Content provider
	 */
	private IContentProvider contentProvider;

	/**
	 * Label provider
	 */
	private ILabelProvider labelProvider;

	/**
	 * Tree viewer
	 */
	private CheckboxTreeViewer viewer;

	/**
	 * Dialog parameters
	 */
	private CheckListDialogParameters parameters;

	/**
	 * Root selector combo box
	 */
	private Combo rootSelector;

	/**
	 * Constructor
	 * 
	 * @param shell
	 *            the dialog shell
	 * @param parameters
	 *            dialog parameters
	 */
	CheckListSelectionDialog(Shell shell, CheckListDialogParameters parameters) {
		super(shell);
		contentProvider = new CheckListSelectionDialogContentProvider();
		labelProvider = new CheckListSelectionDialogLabelProvider();
		this.parameters = parameters;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.dialogs.TitleAreaDialog#
	 *      createContents(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	protected Control createContents(Composite parent) {
		Control control = super.createContents(parent);
		// Registers the help context to the root control
		PlatformUI.getWorkbench().getHelpSystem().setHelp(control,
				TraceViewHelp.getCheckListDialogContext(parameters.dialogType));
		return control;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.dialogs.TitleAreaDialog#
	 *      createDialogArea(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	protected Control createDialogArea(Composite parent) {
		setTitle(TraceViewMessages.getCheckListTitle(parameters.dialogType));
		setMessage(TraceViewMessages.getCheckListText(parameters.dialogType));
		getShell().setText(
				TraceViewMessages.getCheckListTitle(parameters.dialogType));
		Composite composite = (Composite) super.createDialogArea(parent);
		if (parameters.showRoot) {
			createRootSelector(composite);
		}
		viewer = new CheckboxTreeViewer(composite, SWT.BORDER);
		GridData data = new GridData(GridData.FILL_BOTH);
		data.heightHint = TREE_HEIGHT;
		data.widthHint = TREE_WIDTH;
		viewer.getTree().setLayoutData(data);
		viewer.setContentProvider(contentProvider);
		viewer.setLabelProvider(labelProvider);
		viewer.setInput(parameters.rootItems.get(0));
		((CheckListSelectionDialogContentProvider) contentProvider)
				.initializeCheckStates(parameters.expandLevel);
		return composite;
	}

	/**
	 * Creates the root selector combo box
	 * 
	 * @param composite
	 *            the parent composite
	 */
	private void createRootSelector(Composite composite) {
		rootSelector = new Combo(composite, SWT.BORDER | SWT.READ_ONLY);
		GridData gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		rootSelector.setLayoutData(gridData);
		for (int i = 0; i < parameters.rootItems.size(); i++) {
			rootSelector
					.add(parameters.rootItems.get(i).getObject().toString());
		}
		rootSelector.select(0);
		rootSelector.addSelectionListener(new SelectionAdapter() {

			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.swt.events.SelectionAdapter#
			 *      widgetSelected(org.eclipse.swt.events.SelectionEvent)
			 */
			@Override
			public void widgetSelected(SelectionEvent e) {
				CheckListDialogEntry entry = parameters.rootItems
						.get(rootSelector.getSelectionIndex());
				viewer.setInput(entry);
			}

		});
	}

}