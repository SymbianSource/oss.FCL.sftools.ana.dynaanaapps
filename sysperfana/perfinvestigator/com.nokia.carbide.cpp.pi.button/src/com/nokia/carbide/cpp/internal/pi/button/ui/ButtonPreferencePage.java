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
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

public class ButtonPreferencePage extends PreferencePage implements IWorkbenchPreferencePage {
	private static int BUTTON_TAB_INDEX = 0;
	
	
	private Composite content = null;
	private TabFolder buttonTabFolder = null;
	TabItem buttonTab = null;
	ButtonTabPage buttonTabPage = null;
	
	@Override
	protected Control createContents(Composite parent) {
		GridLayoutFactory layoutExpandBoth = GridLayoutFactory.fillDefaults();
		GridDataFactory gridDataExpandBoth = GridDataFactory.fillDefaults().grab(true, true);
		
		content = new Composite(parent, SWT.NONE);
		layoutExpandBoth.applyTo(content);
		gridDataExpandBoth.applyTo(content);
		
		buttonTabFolder = new TabFolder(content, SWT.TOP);
		layoutExpandBoth.applyTo(buttonTabFolder);
		gridDataExpandBoth.applyTo(buttonTabFolder);
		
		buttonTabPage = new ButtonTabPage(buttonTabFolder);
		buttonTab = new TabItem(buttonTabFolder, SWT.NONE, BUTTON_TAB_INDEX);
		buttonTab.setText(Messages.getString("ButtonPreferencePage.0")); //$NON-NLS-1$
		buttonTab.setToolTipText(Messages.getString("ButtonPreferencePage.1")); //$NON-NLS-1$
		buttonTab.setControl(buttonTabPage);
		
		getPageStoredValues();
		return content;
	}

	/**
	 * 
	 */
	private void getPageStoredValues() {
		buttonTabPage.getStoredPreferenceValues();
	}
	
	/**
	 * 
	 */
	private boolean setPageStoredValues() {
		if (buttonTabPage.setStoredPreferenceValues() == false) {
			return false;
		}
		return true;
	}
	
	/**
	 * Things to do when user hit the "OK" button.
	 */
	public boolean performOk() {
		setPageStoredValues();
		cleanUp();
		return super.performOk();
	}

	/**
	 * 
	 */
	private void cleanUp() {
		Runtime.getRuntime().gc();
	}

	/**
	 * Things to do when user hit the "Apply" button.
	 */
	public void performApply() {
		setPageStoredValues();
		cleanUp();
		super.performApply();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
	 */
	public void init(IWorkbench arg0) {
	}


}
