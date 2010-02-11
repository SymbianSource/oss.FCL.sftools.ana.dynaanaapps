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

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Link;
import org.eclipse.ui.PlatformUI;

import com.nokia.carbide.cpp.pi.button.BupEventMapManager;
import com.nokia.carbide.cpp.pi.button.ButtonPlugin;
import com.nokia.carbide.cpp.pi.button.ComNokiaCarbidePiButtonHelpIDs;
import com.nokia.carbide.cpp.pi.button.IBupEventMap;
import com.nokia.carbide.cpp.pi.button.IBupEventMapProfile;

public class ButtonTabPage extends Composite {
	
	private ArrayList<IBupEventMapProfile> profiles = new ArrayList<IBupEventMapProfile>();
	
	// Controls
	private Composite content = null;
	private Group profileGroup = null;
	private Combo profileCombo = null;
	private Composite profileButtonComposite = null;
	private Composite importExportButtonComposite= null;
	private Button profileNewButton = null;
	private Button profileRemoveButton = null;
	private Button profileEditButton = null;
	private Link profileImportLink = null;
	private Link profileExportLink = null;
	private Group mappingGroup = null;
	private BupMapTableViewer mappingTableViewer = null;
	
	/**
	 * import XML and merge into workspace pref
	 */
	private void importXML() {
		ImportBupMapWizardDialog dialog = new ImportBupMapWizardDialog(getShell());
		dialog.open();
		refreshCombo();	// modified
	}

	/**
	 * export
	 */
	private void exportXML() {
		ExportBupMapWizardDialog dialog = new ExportBupMapWizardDialog(getShell());
		dialog.open();
	}
	
	public ButtonTabPage(Composite parent) {
		super(parent, SWT.NONE);

		PlatformUI.getWorkbench().getHelpSystem().setHelp(parent, ComNokiaCarbidePiButtonHelpIDs.PI_BUTTON_MAP_PREF_TAB);
		
		GridLayoutFactory layoutExpandBoth = GridLayoutFactory.fillDefaults();
		GridDataFactory gridDataExpandBoth = GridDataFactory.fillDefaults().grab(true, true);
		
		GridLayoutFactory layoutExpandHorizontal = GridLayoutFactory.swtDefaults();
		GridDataFactory gridDataExpandHorizontal = GridDataFactory.swtDefaults().align(SWT.FILL, SWT.CENTER).grab(true, false);

		GridDataFactory gridDataButton = GridDataFactory.swtDefaults();
		
		layoutExpandBoth.applyTo(this);
		gridDataExpandBoth.applyTo(this);
		
		content = new Composite(this, SWT.NONE);
		layoutExpandBoth.applyTo(content);
		gridDataExpandBoth.applyTo(content);
		
		profileGroup = new Group (content, SWT.NONE);
		layoutExpandHorizontal.applyTo(profileGroup);
		gridDataExpandHorizontal.applyTo(profileGroup);
		profileGroup.setText(Messages.getString("ButtonTabPage.manageProfiles")); //$NON-NLS-1$
		
		profileCombo = new Combo (profileGroup, SWT.READ_ONLY);
		layoutExpandHorizontal.applyTo(profileCombo);
		gridDataExpandHorizontal.applyTo(profileCombo);
		profileCombo.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent arg0) {
			}

			public void widgetSelected(SelectionEvent arg0) {
				refreshTableToCombo();
			}
		});
		
		profileButtonComposite = new Composite (profileGroup, SWT.NONE);
		layoutExpandHorizontal.copy().numColumns(3).applyTo(profileButtonComposite);
		gridDataExpandHorizontal.applyTo(profileButtonComposite);
		
		int maxWidth;
		profileNewButton = new Button(profileButtonComposite, SWT.NONE);
		profileNewButton.setText(Messages.getString("ButtonTabPage.duplicate")); //$NON-NLS-1$
		profileNewButton.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent arg0) {
			}

			public void widgetSelected(SelectionEvent arg0) {
				
				BupProfileDuplicateDialog dialog = new BupProfileDuplicateDialog(getShell(), profiles.get(profileCombo.getSelectionIndex()));
				if (dialog.open() == BupProfileDuplicateDialog.OK) { 
					refreshCombo();
					profileCombo.select(BupEventMapManager.getInstance().getProfilesFromWorkspacePref().size() - 1);	// we cheat a bit by knowing addToWorkspace always append to the end
					refreshTableToCombo();	// current profile had been removed;
				}
			}
		});
		maxWidth = profileNewButton.computeSize(SWT.DEFAULT, SWT.DEFAULT).x;
		profileEditButton = new Button(profileButtonComposite, SWT.NONE);
		profileEditButton.setText(Messages.getString("ButtonTabPage.Edit")); //$NON-NLS-1$
		profileEditButton.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent arg0) {
			}
			public void widgetSelected(SelectionEvent arg0) {
				IBupEventMapProfile profile = profiles.get(profileCombo.getSelectionIndex());
				if (profile != null) {
					new BupProfileEditDialog(getShell(), profile).open();
					refreshTableToCombo();	// current profile may had changed
				}
			}
		});
		maxWidth = Math.max(maxWidth, profileEditButton.computeSize(SWT.DEFAULT, SWT.DEFAULT).x);
		profileRemoveButton = new Button(profileButtonComposite, SWT.NONE);
		profileRemoveButton.setText(Messages.getString("ButtonTabPage.remove")); //$NON-NLS-1$
		profileRemoveButton.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent arg0) {
			}

			public void widgetSelected(SelectionEvent arg0) {
				IBupEventMapProfile profile = profiles.get(profileCombo.getSelectionIndex());
				if (profile != null) {
					if(MessageDialog.openQuestion(getShell(), Messages.getString("ButtonTabPage.confirmRemovalHeading"), //$NON-NLS-1$
							Messages.getString("ButtonTabPage.profile") + profile.getProfileId() + Messages.getString("ButtonTabPage.confirmRemoval"))) { //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
						if (BupEventMapManager.getInstance().canRemoveProfile(profile)) { 
							BupEventMapManager.getInstance().removeFromWorkspace(profile);
							refreshCombo();
							refreshTableToCombo();	// current profile had been removed;
						} else {
							MessageDialog.openInformation(getShell(), Messages.getString("ButtonTabPage.cannotRemoveProfile"), //$NON-NLS-1$
									Messages.getString("ButtonTabPage.profile") + profile.getProfileId() + Messages.getString("ButtonTabPage.isStillOpen")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
						}
					}
				}
			}
		});
		maxWidth = Math.max(maxWidth, profileRemoveButton.computeSize(SWT.DEFAULT, SWT.DEFAULT).x);

		importExportButtonComposite = new Composite(content, SWT.NONE);
		layoutExpandHorizontal.copy().numColumns(2).applyTo(importExportButtonComposite);
		gridDataExpandHorizontal.applyTo(importExportButtonComposite);
		
		profileImportLink = new Link (importExportButtonComposite, SWT.NONE);
		profileImportLink.setText(Messages.getString("ButtonTabPage.importHref")); //$NON-NLS-1$
		profileImportLink.addSelectionListener(new SelectionListener() {

			public void widgetDefaultSelected(SelectionEvent arg0) {
			}

			public void widgetSelected(SelectionEvent arg0) {
				importXML();
			}
			
		});
		profileExportLink = new Link (importExportButtonComposite, SWT.NONE);
		profileExportLink.setText(Messages.getString("ButtonTabPage.exportHref")); //$NON-NLS-1$
		profileExportLink.addSelectionListener(new SelectionListener() {

			public void widgetDefaultSelected(SelectionEvent arg0) {
			}

			public void widgetSelected(SelectionEvent arg0) {
				exportXML();
			}
			
		});
		
		gridDataButton.hint(maxWidth, profileNewButton.computeSize(SWT.DEFAULT, SWT.DEFAULT).y).applyTo(profileNewButton);
		gridDataButton.hint(maxWidth, profileRemoveButton.computeSize(SWT.DEFAULT, SWT.DEFAULT).y).applyTo(profileRemoveButton);
		gridDataButton.hint(maxWidth, profileEditButton.computeSize(SWT.DEFAULT, SWT.DEFAULT).y).applyTo(profileEditButton);
		
		mappingGroup = new Group (content, SWT.NONE);
		layoutExpandBoth.applyTo(mappingGroup);
		gridDataExpandBoth.applyTo(mappingGroup);
		mappingGroup.setText(Messages.getString("ButtonTabPage.keyPressMapping")); //$NON-NLS-1$

		Composite compositeToLookBetter = new Composite(mappingGroup, SWT.NONE);
		layoutExpandHorizontal.applyTo(compositeToLookBetter);
		gridDataExpandHorizontal.applyTo(compositeToLookBetter);
		
		mappingTableViewer = new BupMapTableViewer(compositeToLookBetter, false);

		refreshCombo();
	}

	private void refreshTableToCombo() {
		IBupEventMapProfile profile = profiles.get(profileCombo.getSelectionIndex());
		if (profile != null) {
			profileRemoveButton.setEnabled(false);
			profileEditButton.setEnabled(false);
			ArrayList<IBupEventMapProfile> workspacePerfList = BupEventMapManager.getInstance().getProfilesFromWorkspacePref();
			for (IBupEventMapProfile workspacePref : workspacePerfList) {
				if (workspacePref.toString().equals(profile.toString())) {
					// enable edit only when selection is workspace pref
					profileRemoveButton.setEnabled(true);
					profileEditButton.setEnabled(true);
					break;
				}
			}
			IBupEventMap map = BupEventMapManager.getInstance().captureMap(profile);
			mappingTableViewer.loadMap(map);
			BupEventMapManager.getInstance().releaseMap(map);
		}
	}
	
	private void refreshCombo() {		
		profiles.clear();
		ArrayList<IBupEventMapProfile> globalPrefProfile = BupEventMapManager.getInstance().getProfilesFromWorkspacePref();
		profiles.addAll(globalPrefProfile);
		profiles.addAll(BupEventMapManager.getInstance().getProfilesFromBuiltin());
		
		if (globalPrefProfile.size() > 0)
			profileExportLink.setEnabled(true);
		else
			profileExportLink.setEnabled(false);
		
		String [] profileItems = new String [profiles.size()];
		for (int i = 0; i < profiles.size(); i++) {
			profileItems[i] = profiles.get(i).toString();
		}
		
		// try to remember and select the same upon combo reload, select first if
		// nothing was selected before or previous selection is removed
		String savedId = null;
		if (profileCombo.getSelectionIndex() > 0) {
			savedId = profileCombo.getItem(profileCombo.getSelectionIndex());
		} else {
			savedId = BupEventMapManager.getInstance().getDefaultProfile().toString();
		}
		profileCombo.setItems(profileItems);
		
		int selectionIndex = 0;
		if (savedId != null) {
			for (int i = 0; i < profileCombo.getItemCount(); i++) {
				if (profileCombo.getItem(i).equals(savedId)) {
					selectionIndex = i;
					break;
				}
			}
		}
		profileCombo.select(selectionIndex);
		refreshTableToCombo();
	}
	
	public void getStoredPreferenceValues() {
		IPreferenceStore store = ButtonPlugin.getBupPrefsStore();

		String profileID = store.getString(BupPreferenceConstants.KEY_MAP_PROFILE_STRING);
		if (profileID == null || profileID.equals("")) { //$NON-NLS-1$
			profileID = BupEventMapManager.getInstance().getDefaultProfile().toString();
		}
		int selectionIndex = 0;
		if (profileID != null) {
			for (int i = 0; i < profileCombo.getItemCount(); i++) {
				if (profileCombo.getItem(i).equals(profileID)) {
					selectionIndex = i;
				}
			}
		}
		profileCombo.select(selectionIndex);
		refreshTableToCombo();
	}
	
	/**
	 * Set the stored preference settings values of this tab page.
	 */
	public boolean setStoredPreferenceValues(){
		String profileString = profileCombo.getItem(profileCombo.getSelectionIndex());

		IPreferenceStore store = ButtonPlugin.getBupPrefsStore();
		store.setValue(BupPreferenceConstants.KEY_MAP_PROFILE_STRING, profileString);
		
		return true;
	}
}
