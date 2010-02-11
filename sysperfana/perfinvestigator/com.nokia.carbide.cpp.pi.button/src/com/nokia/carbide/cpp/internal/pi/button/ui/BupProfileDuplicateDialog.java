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

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TrayDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.resource.FontRegistry;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;

import com.nokia.carbide.cpp.pi.button.BupEventMapManager;
import com.nokia.carbide.cpp.pi.button.ComNokiaCarbidePiButtonHelpIDs;
import com.nokia.carbide.cpp.pi.button.IBupEventMap;
import com.nokia.carbide.cpp.pi.button.IBupEventMapProfile;

public class BupProfileDuplicateDialog extends TrayDialog {
	private static final FontRegistry fontRegistry = new FontRegistry();

	private String profileId = null;
	private IBupEventMapProfile excludeProfile = null;
	private IBupEventMapProfile selectedProfileHint = null;
	
	// control
	private Composite composite = null;
	
	private Label profileLabel = null;
	private Text profileText = null;
	private Label messageLabel = null;
	private Label duplicateWithLabel = null;
	private Combo profileCombo = null;
	
	public BupProfileDuplicateDialog(Shell shell, IBupEventMapProfile profile) {
		super(shell);
		selectedProfileHint = profile;
	}
	
	public Control createDialogArea(Composite parent) {
		getShell().setText(Messages.getString("BupProfileDuplicateDialog.duplicateProfile"));   //$NON-NLS-1$
		
		composite = new Composite(parent, SWT.NONE);
		GridDataFactory.fillDefaults().applyTo(composite);
		GridLayoutFactory.fillDefaults().margins(5, 5).applyTo(composite);
		
		PlatformUI.getWorkbench().getHelpSystem().setHelp(composite, ComNokiaCarbidePiButtonHelpIDs.PI_PROFILE_ADD_DIALOG);
		
		profileLabel = new Label(composite, SWT.NONE);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(profileLabel);
		profileLabel.setText(Messages.getString("BupProfileDuplicateDialog.enterNewNameHeading"));  //$NON-NLS-1$
		
		profileText = new Text(composite, SWT.BORDER);
		GridDataFactory.fillDefaults().grab(true, true).hint(200, 15).applyTo(profileText);
		
		profileText.setText(""); //$NON-NLS-1$
		profileText.addModifyListener(new ModifyListener(){
			public void modifyText(ModifyEvent arg0) {
				validateKeyCode();
			}
		});
		
		messageLabel = new Label(composite, SWT.NONE);
		messageLabel.setFont(fontRegistry.getBold(""));	//$NON-NLS-1$
		GridDataFactory.fillDefaults().grab(true, true).hint(350, 15).applyTo(messageLabel);
		
		duplicateWithLabel = new Label(composite, SWT.NONE);
		duplicateWithLabel.setText(Messages.getString("BupProfileDuplicateDialog.duplicateWith"));  //$NON-NLS-1$
		
		profileCombo = new Combo(composite, SWT.READ_ONLY);
		profileCombo.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent arg0) {
				validateKeyCode();
			}			
		});


		
		ArrayList<IBupEventMapProfile> myList = new ArrayList<IBupEventMapProfile>();
		ArrayList<IBupEventMapProfile> globalPrefProfile = BupEventMapManager.getInstance().getProfilesFromWorkspacePref();
		myList.addAll(globalPrefProfile);
		myList.addAll(BupEventMapManager.getInstance().getProfilesFromBuiltin());
		
		for (IBupEventMapProfile profile : myList) {
			if (!profile.equals(excludeProfile)) {
				profileCombo.add(profile.toString());
				profileCombo.setData(profile.toString(), profile);
			}
		}
		
		for (int i = 0; i < profileCombo.getItemCount(); i++) {
			if (selectedProfileHint.toString().equals(profileCombo.getItem(i))) {
				profileCombo.select(i);		// select the item from tab page
			}
		}
				
		return composite;
	}
	
	/**
	 * 
	 */
	protected void validateKeyCode() {
		Button buttonOK = getButton(IDialogConstants.OK_ID);
		if (buttonOK == null)
			return;
		
		String message = ""; //$NON-NLS-1$
		if (profileCombo.getSelectionIndex() < 0) {
			message = Messages.getString("BupProfileDuplicateDialog.selectProfileToDuplicate"); //$NON-NLS-1$
			buttonOK.setEnabled(false);
		}
		if (message.equals("")) { //$NON-NLS-1$
			profileId = profileText.getText();
			buttonOK.setEnabled(true);
			if (profileId.equals("")) { //$NON-NLS-1$
				message = ""; //$NON-NLS-1$
				buttonOK.setEnabled(false);
			} else {
				ArrayList<IBupEventMapProfile> workspacePrefs = BupEventMapManager.getInstance().getProfilesFromWorkspacePref();
				for (IBupEventMapProfile pref : workspacePrefs) {
					if (profileId.equals(pref.getProfileId())) {
						message = Messages.getString("BupProfileDuplicateDialog.profileName") + profileId + Messages.getString("BupProfileDuplicateDialog.alreadyExists");   //$NON-NLS-1$ //$NON-NLS-2$
						buttonOK.setEnabled(false);
					}
				}
			}
		}

		messageLabel.setText(message);
		messageLabel.setVisible(!message.equals("")); //$NON-NLS-1$
		messageLabel.setForeground(messageLabel.getDisplay().getSystemColor(SWT.COLOR_RED));
	}
	
	protected void createButtonsForButtonBar(Composite parent) {
		super.createButtonsForButtonBar(parent);
		// now buttons are there, we can  disable key
		getButton(IDialogConstants.OK_ID).setEnabled(false);
	}

	protected Button createButton(Composite parent, int id, String label, boolean defaultButton) {
		Button result = super.createButton(parent, id, label, defaultButton);
		if (id == IDialogConstants.OK_ID) {
			result.setText(Messages.getString("BupProfileDuplicateDialog.duplicate"));  //$NON-NLS-1$
		}
		return result;
	}
	
	public void okPressed() {
		BupEventMapManager.getInstance().addToWorkspace(profileId);
		ArrayList<IBupEventMapProfile> profiles = BupEventMapManager.getInstance().getProfilesFromWorkspacePref();
		IBupEventMapProfile newProfile = null;
		for (IBupEventMapProfile profile : profiles) {
			if (profileId.equals(profile.getProfileId())) {
				newProfile = profile;
				break;
			}
		}
		if (newProfile != null) {
			IBupEventMap newProfileMap = BupEventMapManager.getInstance().captureMap(newProfile);
			ModifyCachedBupEventMap tempMapForWrite = new ModifyCachedBupEventMap(newProfileMap);
			BupEventMapManager.getInstance().releaseMap(newProfileMap);
			IBupEventMapProfile loadedProfile = (IBupEventMapProfile)profileCombo.getData(profileCombo.getItem(profileCombo.getSelectionIndex()));
			IBupEventMap loadedMap = BupEventMapManager.getInstance().captureMap(loadedProfile);
			tempMapForWrite.initializeFromMap(loadedMap);
			BupEventMapManager.getInstance().releaseMap(loadedMap);
			BupEventMapManager.getInstance().commitEditToWorkspace(tempMapForWrite);
		}
		super.okPressed();
	}
}
