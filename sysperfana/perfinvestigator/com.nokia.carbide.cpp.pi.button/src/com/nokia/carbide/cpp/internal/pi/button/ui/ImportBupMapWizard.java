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

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.IImportWizard;
import org.eclipse.ui.IWorkbench;

import com.nokia.carbide.cpp.pi.button.BupEventMapManager;


public class ImportBupMapWizard extends Wizard implements IImportWizard {

	private ImportBupMapGetXmlTask importPage;
	private ImportBupMapShowOverLapTask overlapPage;
	private boolean allowFinish = false;
	
	public ImportBupMapWizard() {
		setWindowTitle(Messages.getString("ImportBupMapWizard.0"));  //$NON-NLS-1$
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.wizard.Wizard#addPages()
	 */
	public void addPages() {
		addPage(importPage = new ImportBupMapGetXmlTask());
		addPage(overlapPage = new ImportBupMapShowOverLapTask());
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.wizard.Wizard#performFinish()
	 */
	@Override
	public boolean performFinish() {
		java.io.File file = new java.io.File(importPage.getxImportXml());
		BupEventMapManager.getInstance().importMergeToWorkspace(file.toURI());
		return true;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchWizard#init(org.eclipse.ui.IWorkbench, org.eclipse.jface.viewers.IStructuredSelection)
	 */
	public void init(IWorkbench arg0, IStructuredSelection arg1) {
	}
	
	public void setAllowFinish(boolean cond) {
		allowFinish = cond;
	}
	
	public boolean canFinish() {
		return allowFinish;
	}
	
	public IWizardPage getNextPage (IWizardPage page) {
		String [] overlaps = importPage.getOverLapList();
		if (page == importPage ) {
			overlapPage.setOverLapList(overlaps);
			if (overlaps.length > 0) {
				return overlapPage;
			}
			else {
				return overlapPage.getNextPage();
			}
		}
		return super.getNextPage(page);
	}

}
