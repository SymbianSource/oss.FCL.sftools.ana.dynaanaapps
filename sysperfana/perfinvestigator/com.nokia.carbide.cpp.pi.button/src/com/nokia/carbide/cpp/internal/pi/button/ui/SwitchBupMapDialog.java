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
import java.util.HashSet;
import java.util.Set;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;

import com.nokia.carbide.cpp.pi.button.ComNokiaCarbidePiButtonHelpIDs;
import com.nokia.carbide.cpp.pi.button.IBupEventMapProfile;
import com.nokia.carbide.cpp.sdk.core.ISymbianSDK;
import com.nokia.carbide.cpp.sdk.core.SDKCorePlugin;
import com.nokia.carbide.cpp.ui.CarbideUIPlugin;
import com.nokia.carbide.cpp.ui.ICarbideSharedImages;


public class SwitchBupMapDialog extends TitleAreaDialog {
	private IBupEventMapProfile oldProfile;
	private IBupEventMapProfile newProfile;
	private boolean resetAll;
	
	// control
	private Composite composite = null;
	private BupProfileTreeViewer profileTreeViewer = null;
	private Composite currentProfileComposite = null;
	private Label currentProfileLabel = null;
	private Text currenProfileText = null;
	private Button resetAllKeysButton = null;
	
	public SwitchBupMapDialog (Shell parentShell, IBupEventMapProfile formerProfile) {
		super(parentShell);
		oldProfile = formerProfile;
	}
	
	public Control createDialogArea(Composite parent) {
		composite = new Composite(parent, SWT.NONE);
		PlatformUI.getWorkbench().getHelpSystem().setHelp(composite, ComNokiaCarbidePiButtonHelpIDs.PI_KEY_MAP_SWITCH_DIALOG);
		getShell().setText(Messages.getString("SwitchBupMapDialog.switchKeyPressProfile")); //$NON-NLS-1$
		setTitleImage(CarbideUIPlugin.getSharedImages().getImageDescriptor(ICarbideSharedImages.IMG_PI_METER_20_20).createImage());
		setTitle(Messages.getString("SwitchBupMapDialog.remapMessage")); //$NON-NLS-1$
		
		GridLayoutFactory layoutExpandBoth = GridLayoutFactory.fillDefaults();
		GridDataFactory gridDataExpandBoth = GridDataFactory.fillDefaults().grab(true, true);
		
		layoutExpandBoth.applyTo(composite);
		gridDataExpandBoth.applyTo(composite);
		
		profileTreeViewer = new BupProfileTreeViewer(composite);
		gridDataExpandBoth.applyTo(profileTreeViewer.getTree());
		profileTreeViewer.addPostSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent arg0) {
				validatePage();
			}
		});
		
		currentProfileComposite = new Composite(composite, SWT.NONE);
		layoutExpandBoth.copy().numColumns(2).applyTo(currentProfileComposite);
		gridDataExpandBoth.applyTo(currentProfileComposite);
		
		currentProfileLabel = new Label(currentProfileComposite, SWT.NONE);
		currentProfileLabel.setText(Messages.getString("SwitchBupMapDialog.currentProfile")); //$NON-NLS-1$
		
		currenProfileText = new Text(currentProfileComposite, SWT.READ_ONLY);
		
		resetAllKeysButton = new Button(composite, SWT.CHECK);
		resetAllKeysButton.setText(Messages.getString("SwitchBupMapDialog.overwriteAllEventsWithDefault")); //$NON-NLS-1$
		resetAllKeysButton.addSelectionListener(new SelectionListener() {

			public void widgetDefaultSelected(SelectionEvent arg0) {
			}

			public void widgetSelected(SelectionEvent arg0) {
				resetAll = resetAllKeysButton.getSelection();
			}
			
		});
		
		buildBupMapProfileList();
		resetAllKeysButton.setSelection(true);
		resetAllKeysButton.setEnabled(false);
		currenProfileText.setText(Messages.getString("SwitchBupMapDialog.none")); //$NON-NLS-1$
		
		if (oldProfile != null) {
			resetAllKeysButton.setSelection(false);
			resetAllKeysButton.setEnabled(true);
			currenProfileText.setText(oldProfile.toString());
		}
		
		return composite;
	}

	/**
	 * 
	 */
	protected void validatePage() {
		Button buttonSwitch = getButton(IDialogConstants.OK_ID);
		if ((newProfile = profileTreeViewer.getSelectedProfile()) == null) {
			buttonSwitch.setEnabled(false);
		} else {
			buttonSwitch.setEnabled(true);
		}
	}
	
	private void buildBupMapProfileList() {
		Set<ISymbianSDK> sdks = new HashSet<ISymbianSDK>();
		sdks.addAll(SDKCorePlugin.getSDKManager().getSDKList());
		BupProfileTreeViewer.BupProfileTreeData data = profileTreeViewer.new BupProfileTreeData(null, sdks);
		profileTreeViewer.setInput(data);
		profileTreeViewer.expandAll();
	}
	
	ArrayList<Integer> allId = new ArrayList<Integer>();
	protected Button createButton(Composite parent, int id, String label, boolean defaultButton) {
		Button result = super.createButton(parent, id, label, defaultButton);
		if (id == IDialogConstants.OK_ID) {
			result.setText(Messages.getString("SwitchBupMapDialog.switchToNewProfile")); //$NON-NLS-1$
		}
		allId.add(id);
		return result;
	}
	
	protected void createButtonsForButtonBar(Composite parent) {
		super.createButtonsForButtonBar(parent);
		GridDataFactory gridDataButton = GridDataFactory.swtDefaults();
		int maxWidth = 0;
		for (Integer id : allId) {
			Button button = getButton(id);
			maxWidth = Math.max(maxWidth, button.computeSize(SWT.DEFAULT, SWT.DEFAULT).x);
		}
		for (Integer id : allId) {
			Button button = getButton(id);
			gridDataButton.hint(maxWidth, button.computeSize(SWT.DEFAULT, SWT.DEFAULT).y).applyTo(button);
		}
		// now buttons are there, we can  disable key
		getButton(IDialogConstants.OK_ID).setEnabled(false);
	}

	public boolean resetAll() {
		return resetAll;
	}
	
	public IBupEventMapProfile getNewProfile() {
		return newProfile;
	}
}
