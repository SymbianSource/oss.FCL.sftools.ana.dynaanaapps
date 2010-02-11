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

package com.nokia.carbide.cpp.internal.pi.wizards.ui;

import java.util.ArrayList;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TrayDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;

import com.nokia.carbide.cpp.internal.pi.wizards.ui.util.IPkgEntry;

public class NewPIWizardRemovePkgDialog extends TrayDialog{
	
	private IPkgEntry[] entryList;
	private IPkgEntry[] removeList;
	
	// control
	private Composite composite = null;
	private Table table = null;
	private Label label = null;
	
	protected NewPIWizardRemovePkgDialog(Shell arg0, IPkgEntry[] pkgFiles) {
		super(arg0);
		entryList = pkgFiles;
	}

	public Control createDialogArea(Composite parent) {
		getShell().setText(Messages.getString("NewPIWizardRemovePkgDialog.shell.title")); //$NON-NLS-1$
		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 1;
		composite = new Composite(parent, SWT.NONE);
		composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		composite.setLayout(gridLayout);
		label = new Label(composite, SWT.NONE);
		label.setText(Messages.getString("NewPIWizardRemovePkgDialog.check.pkg.label")); //$NON-NLS-1$
		table = new Table(composite, SWT.CHECK | SWT.H_SCROLL | SWT.BORDER);
		
		// load settings
		if (entryList != null) {
			for (int i = 0; i < entryList.length; i++) {
				TableItem item = new TableItem(table, SWT.NONE);
				String pkgText = Messages.getString("NewPIWizardRemovePkgDialog.sample.file") + entryList[i].getPkgFile(); //$NON-NLS-1$
				if (entryList[i].getSdk() != null) {
					pkgText += Messages.getString("NewPIWizardRemovePkgDialog.sdk") + entryList[i].getSdk().getUniqueId(); //$NON-NLS-1$
				}
				item.setText(pkgText);
				item.setData(entryList[i]);	// bound the pkgFile to the button, so we can retrieve later
				item.setChecked(false);
			}
		}
		table.addSelectionListener(new SelectionAdapter() {

			public void widgetSelected(SelectionEvent e) {
				TableItem[] items = table.getItems();
				getButton(IDialogConstants.OK_ID).setEnabled(false);
				if (items != null) {
					for (TableItem item : items) {
						if (item.getChecked()) {
							getButton(IDialogConstants.OK_ID).setEnabled(true);
						}
					}
				}
			}
		});
		
		return composite;
	}
	
	public void okPressed() {
		ArrayList<IPkgEntry> removeArrayList = new ArrayList<IPkgEntry>();
		TableItem[] items = table.getItems();
		for (int i = 0; i < items.length; i++) {
			if (items[i].getChecked()) {
				removeArrayList.add((IPkgEntry) items[i].getData());
			}
		}
		removeList = removeArrayList.toArray(new IPkgEntry[removeArrayList.size()]);
		super.okPressed();
	}
	
	public IPkgEntry[] getRemovedList () {
		return removeList;
	}
	
	protected void createButtonsForButtonBar(Composite parent) {
		super.createButtonsForButtonBar(parent);
		// now buttons are there, we can  disable key
		getButton(IDialogConstants.OK_ID).setEnabled(false);
	}
}
