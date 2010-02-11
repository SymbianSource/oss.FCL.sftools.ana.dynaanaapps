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

import java.util.ArrayList;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.IExportWizard;
import org.eclipse.ui.IWorkbench;

import com.nokia.carbide.cpp.internal.pi.util.config.gen.PIConfig.ButtonEventProfileType;
import com.nokia.carbide.cpp.pi.button.BupEventMapManager;
import com.nokia.carbide.cpp.pi.button.IBupEventMap;
import com.nokia.carbide.cpp.pi.button.IBupEventMapProfile;
import com.nokia.carbide.cpp.pi.util.GeneralMessages;


public class ExportBupMapWizard extends Wizard implements IExportWizard {

	private ExportBupMapGetXmlTask exportPage;
	
	public ExportBupMapWizard() {
		setWindowTitle(Messages.getString("ExportBupMapWizard.0"));   //$NON-NLS-1$
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.wizard.Wizard#addPages()
	 */
	public void addPages() {
		addPage(exportPage = new ExportBupMapGetXmlTask());
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.wizard.Wizard#performFinish()
	 */
	@Override
	public boolean performFinish() {
		ArrayList<IBupEventMapProfile> workspacePrefProfiles = BupEventMapManager.getInstance().getProfilesFromWorkspacePref();
		if (workspacePrefProfiles.size() <= 0) {
			GeneralMessages.showErrorMessage(Messages.getString("ExportBupMapWizard.1"));   //$NON-NLS-1$
			return false;
		}

		java.io.File file = new java.io.File(exportPage.getExportXml());
		ArrayList<ButtonEventProfileType> profileList = new ArrayList<ButtonEventProfileType>();
		for (IBupEventMapProfile profile : workspacePrefProfiles) {
			IBupEventMap map = BupEventMapManager.getInstance().captureMap(profile);
			profileList.add(map.toEmfModel());
			BupEventMapManager.getInstance().releaseMap(map);
		}
		BupEventMapManager.getInstance().saveMap(file.toURI(), profileList);	

		return true;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchWizard#init(org.eclipse.ui.IWorkbench, org.eclipse.jface.viewers.IStructuredSelection)
	 */
	public void init(IWorkbench arg0, IStructuredSelection arg1) {
	}
}
