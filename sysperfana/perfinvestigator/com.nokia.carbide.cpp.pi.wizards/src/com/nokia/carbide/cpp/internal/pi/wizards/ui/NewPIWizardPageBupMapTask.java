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

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.AbstractTreeViewer;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;

import com.nokia.carbide.cdt.builder.project.ICarbideBuildConfiguration;
import com.nokia.carbide.cpp.internal.pi.button.ui.BupProfileTreeViewer;
import com.nokia.carbide.cpp.internal.pi.wizards.ui.util.CarbidePiWizardHelpIds;
import com.nokia.carbide.cpp.internal.pi.wizards.ui.util.IPkgEntry;
import com.nokia.carbide.cpp.pi.button.BupEventMapManager;
import com.nokia.carbide.cpp.pi.button.IBupEventMapProfile;
import com.nokia.carbide.cpp.sdk.core.ISymbianSDK;


public class NewPIWizardPageBupMapTask extends NewPIWizardPage implements
		INewPIWizardSettings {
	
	private NewPIWizardSettings settings = NewPIWizardSettings.getInstance();

	/**
	 * @param arg0
	 */
	protected NewPIWizardPageBupMapTask() {
		super(Messages.getString("NewPIWizardPageBupMapTask.0")); //$NON-NLS-1$
		setTitle(Messages.getString("NewPIWizardPageBupMapTask.1")); //$NON-NLS-1$
	    setDescription(Messages.getString("NewPIWizardPageBupMapTask.2")); //$NON-NLS-1$
	}
	
	// controls
	Composite container;
	private Label profileLabel = null;
	Composite treeViewerComposite;
	private BupProfileTreeViewer profileTreeViewer = null;
	private Label recommendedLabel = null;
	private Text rationaleText = null;	
	
	public void createControl(Composite parent) {
		super.createControl(parent);
		container = new Composite(parent, SWT.NONE);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(container);
		GridLayoutFactory.fillDefaults().applyTo(container);

		profileLabel = new Label(container, SWT.NONE);
		profileLabel.setText(Messages.getString("NewPIWizardPageBupMapTask.3")); //$NON-NLS-1$
		
		treeViewerComposite = new Composite(container, SWT.NONE);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(treeViewerComposite);
		GridLayoutFactory.fillDefaults().applyTo(treeViewerComposite);
		profileTreeViewer = new BupProfileTreeViewer(treeViewerComposite);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(profileTreeViewer.getTree());
		profileTreeViewer.addPostSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent arg0) {
				validatePage();
				IBupEventMapProfile profile = profileTreeViewer.getSelectedProfile();
				if (profile != null) {
					NewPIWizardSettings.getInstance().keyMapProfile = profile;
				}
			}
		});
		
		recommendedLabel = new Label(container, SWT.NONE);
		recommendedLabel.setText(Messages.getString("NewPIWizardPageBupMapTask.4")); //$NON-NLS-1$
		
		rationaleText = new Text(container, SWT.READ_ONLY);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(rationaleText);
		
		setControl(container);
		PlatformUI.getWorkbench().getHelpSystem().setHelp(getControl(), CarbidePiWizardHelpIds.PI_IMPORT_WIZARD_BUP_MAP);
		
		validatePage();
	}

	/* (non-Javadoc)
	 * @see com.nokia.carbide.cpp.internal.pi.wizards.ui.INewPIWizardSettings#setupPageFromFromNewPIWizardSettings()
	 */
	public void setupPageFromFromNewPIWizardSettings() {
		if (settings.keyMapProfile != null) {
			profileTreeViewer.expandAll();
			profileTreeViewer.setSelection(new StructuredSelection(settings.keyMapProfile));
			profileTreeViewer.reveal(settings.keyMapProfile);
		}
	}

	/* (non-Javadoc)
	 * @see com.nokia.carbide.cpp.internal.pi.wizards.ui.INewPIWizardSettings#validatePage()
	 */
	public void validatePage() {
		IBupEventMapProfile profile = profileTreeViewer.getSelectedProfile();
		if (profile == null) {
			setErrorMessage(Messages.getString("NewPIWizardPageBupMapTask.5")); //$NON-NLS-1$
			setPageComplete(false);
		} else {
			setErrorMessage(null);
			setPageComplete(true);
		}
	}
	
	/*
	 * Build the profile list will descending recommendation
	 * Cust kit => SDK => workspace selection => workspace profiles => builtin profiles
	 * 
	 * If the top tier selection have only one profile, preselect it for user
	 * otherwise use persisted setting.
	 * 
	 * e.g. for preselect
	 * -If we have Cust kit and Cust kit have only one profile, select it
	 * -If we have only one SDK and it only have one profile, select it
	 * 
	 * return if we should preselect for user
	 */
	private boolean buildBupMapProfileList() {
		ArrayList<ICarbideBuildConfiguration> buildConfigList = settings.selectedBuildConfigList;
		ArrayList<IPkgEntry> appFileList = settings.selectedAppFileList;
		ISymbianSDK romSdk = null;
		Set<ISymbianSDK> appSdks = new HashSet<ISymbianSDK>();
		if (settings.haveRomOnly || settings.haveAppRom) {
			romSdk = settings.romSdk;
		}
		
		// update viewer
		if (settings.haveAppRom || settings.haveAppOnly) {		
			for (ICarbideBuildConfiguration config: buildConfigList) {
				appSdks.add(config.getSDK());
			}
			
			for (IPkgEntry pkgEntry : appFileList) {
				appSdks.add(pkgEntry.getSdk());
			}
		}

		BupProfileTreeViewer.BupProfileTreeData data = profileTreeViewer.new BupProfileTreeData(romSdk, appSdks);
		profileTreeViewer.setInput(data);
		profileTreeViewer.expandAll();
		
		// select profile or post rationale
		// ROM CustKit
		if (settings.haveRomOnly || settings.haveAppRom) {
			ArrayList<IBupEventMapProfile> profiles = BupEventMapManager.getInstance().getProfilesFromSDK(settings.romSdk);
			if (profiles.size() == 1) {
				IBupEventMapProfile profile = profiles.get(0);
				rationaleText.setText(Messages.getString("NewPIWizardPageBupMapTask.6") + profile.getProfileId() + Messages.getString("NewPIWizardPageBupMapTask.7") + profile.getSDK().getUniqueId()+ Messages.getString("NewPIWizardPageBupMapTask.8")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				profileTreeViewer.setSelection(new StructuredSelection(profile));
				profileTreeViewer.reveal(profile);
				return true;

			} else {
				if (profiles.size() == 0) {
					if (new File(BupEventMapManager.getInstance().profileLocatationInSDK(settings.romSdk)).exists() == false) {
						rationaleText.setText(Messages.getString("NewPIWizardPageBupMapTask.21") + settings.romSdk.getUniqueId() + Messages.getString("NewPIWizardPageBupMapTask.22")); //$NON-NLS-1$ //$NON-NLS-2$
					} else {
						rationaleText.setText(Messages.getString("NewPIWizardPageBupMapTask.16") + settings.romSdk.getUniqueId() + Messages.getString("NewPIWizardPageBupMapTask.20")); //$NON-NLS-1$ //$NON-NLS-2$
					}
				} else {
					rationaleText.setText(Messages.getString("NewPIWizardPageBupMapTask.9") + settings.romSdk.getUniqueId() +Messages.getString("NewPIWizardPageBupMapTask.10")); //$NON-NLS-1$ //$NON-NLS-2$
				}
			}
		}
		
		if (settings.haveAppRom || settings.haveAppOnly) {
			ArrayList<ISymbianSDK> sdks = new ArrayList<ISymbianSDK>();
			
			for (ICarbideBuildConfiguration config: buildConfigList) {
				sdks.add(config.getSDK());
			}
			
			for (IPkgEntry pkgEntry : appFileList) {
				sdks.add(pkgEntry.getSdk());
			}
			
			if (sdks.size() == 1) {
				ArrayList<IBupEventMapProfile> profiles = BupEventMapManager.getInstance().getProfilesFromSDK(sdks.get(0));
				if (profiles.size() == 1) {
					IBupEventMapProfile profile = profiles.get(0);
					rationaleText.setText(Messages.getString("NewPIWizardPageBupMapTask.11") + profile.getProfileId() + Messages.getString("NewPIWizardPageBupMapTask.12") + profile.getSDK().getUniqueId()+ Messages.getString("NewPIWizardPageBupMapTask.13")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
					profileTreeViewer.setSelection(new StructuredSelection(profile));
					profileTreeViewer.reveal(profile);
					return true;
				} else {
					rationaleText.setText(Messages.getString("NewPIWizardPageBupMapTask.14") + sdks.get(0).getUniqueId()+ Messages.getString("NewPIWizardPageBupMapTask.15")); //$NON-NLS-1$ //$NON-NLS-2$
				}
			} else {
				String sdkList = ""; //$NON-NLS-1$
				for (ISymbianSDK sdk : sdks) {
					sdkList += Messages.getString("NewPIWizardPageBupMapTask.17") + sdk.getUniqueId() + Messages.getString("NewPIWizardPageBupMapTask.18"); //$NON-NLS-1$ //$NON-NLS-2$
				}
				rationaleText.setText(Messages.getString("NewPIWizardPageBupMapTask.19") + sdkList); //$NON-NLS-1$
			}	
		}
		
		IBupEventMapProfile profile = BupEventMapManager.getInstance().getPrefSelectedProfile();
		profileTreeViewer.setSelection(new StructuredSelection(profile));
		profileTreeViewer.reveal(profile);
		
		return false;
	}
	
	public void setVisible(boolean visable) {
		super.setVisible(visable);
		if (visable) {
			buildBupMapProfileList();
			// need this because treeviewer depend on info from previous pages
			setupPageFromFromNewPIWizardSettings();
		}
	}

}
