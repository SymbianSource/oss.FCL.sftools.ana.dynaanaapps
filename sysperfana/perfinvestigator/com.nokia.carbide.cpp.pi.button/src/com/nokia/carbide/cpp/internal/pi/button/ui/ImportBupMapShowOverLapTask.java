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

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.PlatformUI;

import com.nokia.carbide.cpp.pi.button.ComNokiaCarbidePiButtonHelpIDs;


public class ImportBupMapShowOverLapTask extends WizardPage {
	
	/**
	 * @param pageName
	 */
	protected ImportBupMapShowOverLapTask() {
		super(Messages.getString("ImportBupMapShowOverLapTask.0"));  //$NON-NLS-1$
		setTitle(Messages.getString("ImportBupMapShowOverLapTask.1"));  //$NON-NLS-1$
		setDescription(Messages.getString("ImportBupMapShowOverLapTask.2"));  //$NON-NLS-1$
	}
	
	// controls
	Composite container;
	Table table;

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
	 */
	public void createControl(Composite parent) {
		GridLayoutFactory layoutExpandBoth = GridLayoutFactory.fillDefaults();
		GridDataFactory gridDataExpandBoth = GridDataFactory.fillDefaults().grab(true, true);
		
		container = new Composite(parent, SWT.NULL);
		layoutExpandBoth.applyTo(container);
		gridDataExpandBoth.applyTo(container);
		
		table = new Table(container, SWT.BORDER | SWT.V_SCROLL);
		layoutExpandBoth.applyTo(table);
		gridDataExpandBoth.applyTo(table);
		
		setControl(container);
		PlatformUI.getWorkbench().getHelpSystem().setHelp(getControl(), ComNokiaCarbidePiButtonHelpIDs.PI_KEY_MAP_IMPORT_WIZARD_OVERLAP);
	}

	/**
	 * @param overLapList
	 */
	public void setOverLapList(String[] overLapList) {
		table.removeAll();
		for (String profile : overLapList) {
			TableItem item = new TableItem(table, SWT.NONE);
			item.setText(profile);
		}
	}
	
	public void setVisible(boolean visable) {
		super.setVisible(visable);
		if (visable) {
			((ImportBupMapWizard)getWizard()).setAllowFinish(true);
		} else {
			((ImportBupMapWizard)getWizard()).setAllowFinish(false);
		}
	}

}
