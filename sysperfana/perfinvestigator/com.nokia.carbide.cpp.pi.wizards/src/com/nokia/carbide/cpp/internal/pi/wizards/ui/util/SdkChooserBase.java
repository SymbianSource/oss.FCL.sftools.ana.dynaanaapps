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

package com.nokia.carbide.cpp.internal.pi.wizards.ui.util;

import java.util.List;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;

import com.nokia.carbide.cpp.sdk.core.ISDKManager;
import com.nokia.carbide.cpp.sdk.core.ISymbianSDK;
import com.nokia.carbide.cpp.sdk.core.SDKCorePlugin;
import com.nokia.carbide.cpp.sdk.ui.shared.AddSDKDialog;

// just gather all composite layout stuff here, so both Dialog
// and Wizard can be the same layout, they should call layout
// in their layout code
// Wizard would setControl(layout(parent)) in createControl
// Dialog would return layout(parent); in layout code e.g. createDialogArea()
public class SdkChooserBase {
	// control
	public Composite composite = null;
	public Label label = null;
	public Composite epocrootComposite = null;
	public Table sdkTable = null;
	public Composite buttonComposite = null;
	public Button addSdkButton = null;
	public Button removeSdkButton = null;
	public Group sdkGroup = null;
	public Label directoryDataLabel = null;
	public Label epocrootLabel = null;
	public Label osVersionDataLabel = null;
	public Label osVersionLabel = null;
	public Label platformDataLabel = null;
	public Label platformLabel = null;

	public Composite layout(Composite parent) {
		SDKCorePlugin.getSDKManager();
		GridLayout gridLayout1 = new GridLayout();
		gridLayout1.numColumns = 1;
		composite = new Composite(parent, SWT.NONE);
		composite.setLayout(gridLayout1);
		composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		label = new Label(composite, SWT.NONE);
		label.setText(Messages.getString("SdkChooserBase.label")); //$NON-NLS-1$
		createEpocrootComposite();
		createSDKGroup();

		return composite;
	}
	
	/**
	 * This method initializes epocrootComposite	
	 *
	 */
	private void createEpocrootComposite() {
		GridLayout gridLayout2 = new GridLayout();
		gridLayout2.numColumns = 2;
		epocrootComposite = new Composite(composite, SWT.NONE);
		epocrootComposite.setLayout(gridLayout2);
		GridData gridData1 = new GridData();
		gridData1.horizontalAlignment = GridData.FILL;
		gridData1.grabExcessHorizontalSpace = true;
		epocrootComposite.setLayoutData(gridData1);
		sdkTable = new Table(epocrootComposite, SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL | SWT.SINGLE);
		sdkTable.setHeaderVisible(false);
		GridData gridData2 = new GridData();
		gridData2.horizontalAlignment = GridData.FILL;
		gridData2.grabExcessHorizontalSpace = true;
		sdkTable.setLayoutData(gridData2);
		sdkTable.setLinesVisible(false);
	
		createButtonComposite();
		
		sdkTable.addSelectionListener(new SelectionListener() {

			public void widgetDefaultSelected(SelectionEvent arg0) {
			}

			public void widgetSelected(SelectionEvent arg0) {
				propagateSdkDetail();
			}
			
		});
	}
	
	private void createButtonComposite() {
		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 1;
		buttonComposite = new Composite(epocrootComposite, SWT.NONE);
		buttonComposite.setLayoutData(new GridData(SWT.BEGINNING, SWT.BEGINNING, false, false));
		buttonComposite.setLayout(gridLayout);
		addSdkButton = new Button (buttonComposite, SWT.NONE);
		addSdkButton.setText(Messages.getString("SdkChooserBase.addsdk.button")); //$NON-NLS-1$
		removeSdkButton = new Button (buttonComposite, SWT.NONE);
		removeSdkButton.setText(Messages.getString("SdkChooserBase.removesdk.button")); //$NON-NLS-1$

		GridData buttonWidthGridData = new GridData();
		buttonWidthGridData.widthHint = Math.max(addSdkButton.computeSize(SWT.DEFAULT, SWT.DEFAULT, true).x, removeSdkButton.computeSize(SWT.DEFAULT, SWT.DEFAULT, true).x);
		addSdkButton.setLayoutData(buttonWidthGridData);
		removeSdkButton.setLayoutData(buttonWidthGridData);

		addSdkButton.addSelectionListener(new SelectionListener() {

			public void widgetDefaultSelected(SelectionEvent arg0) {
			}

			public void widgetSelected(SelectionEvent arg0) {
				handleAddSdk();
			}
			
		});
		removeSdkButton.addSelectionListener(new SelectionListener() {

			public void widgetDefaultSelected(SelectionEvent arg0) {
			}

			public void widgetSelected(SelectionEvent arg0) {
				handleRemoveSdk();
			}
			
		});
		propagateSdkFromManager();
	}
	
	private void createSDKGroup() {
		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 2;
		sdkGroup = new Group(composite, SWT.NONE);
		sdkGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		sdkGroup.setLayout(gridLayout);
		sdkGroup.setText(Messages.getString("SdkChooserBase.sdk.group")); //$NON-NLS-1$
	
		epocrootLabel = new Label(sdkGroup, SWT.NONE);
		epocrootLabel.setText(Messages.getString("SdkChooserBase.epocroot.label")); //$NON-NLS-1$
		directoryDataLabel = new Label(sdkGroup, SWT.WRAP);
		directoryDataLabel.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		directoryDataLabel.setText(Messages.getString("SdkChooserBase.directorydata.label")); //$NON-NLS-1$
		osVersionLabel = new Label(sdkGroup, SWT.NONE);
		osVersionLabel.setText(Messages.getString("SdkChooserBase.osversion.label")); //$NON-NLS-1$
		osVersionDataLabel = new Label(sdkGroup, SWT.WRAP);
		osVersionDataLabel.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		osVersionDataLabel.setText(Messages.getString("SdkChooserBase.osversiondata.label")); //$NON-NLS-1$
		platformLabel = new Label(sdkGroup, SWT.NONE);
		platformLabel.setText(Messages.getString("SdkChooserBase.platform.label")); //$NON-NLS-1$
		platformDataLabel = new Label(sdkGroup, SWT.WRAP);
		platformDataLabel.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		platformDataLabel.setText(Messages.getString("SdkChooserBase.platformdata.label")); //$NON-NLS-1$
		
	}
	
	public void propagateSdkFromManager() {
		List<ISymbianSDK> sdkList = getSDKList();
		sdkTable.removeAll();
		for (ISymbianSDK sdk : sdkList) {
			TableItem item = new TableItem(sdkTable, SWT.NONE);
			item.setText(sdk.getUniqueId());
			item.setData(sdk);
		}
		if (sdkList.size() == 1) {
			sdkTable.select(0);
		}
	}
	
	public void propagateSdkDetail() {
		TableItem[] items = sdkTable.getSelection();
		if (items.length > 0) {
			ISymbianSDK sdk = (ISymbianSDK) items[0].getData();
			if (sdk != null) {
				directoryDataLabel.setText(sdk.getEPOCROOT());
				osVersionDataLabel.setText(sdk.getOSVersion().toString());
				platformDataLabel.setText(sdk.getAvailablePlatforms().toString());
			}
		} else {
			directoryDataLabel.setText(Messages.getString("SdkChooserBase.directorydata.label")); //$NON-NLS-1$
			osVersionDataLabel.setText(Messages.getString("SdkChooserBase.osversiondata.label")); //$NON-NLS-1$
			platformDataLabel.setText(Messages.getString("SdkChooserBase.platformdata.label2"));	 //$NON-NLS-1$
		}
	}
	
	private void handleAddSdk() {
		AddSDKDialog dialog = new AddSDKDialog(composite.getShell());
		if (dialog.open() == AddSDKDialog.OK){
			getSDKManager().updateCarbideSDKCache();
			propagateSdkFromManager();
		}
	}
	
	private void handleRemoveSdk() {
		TableItem[] items = sdkTable.getSelection();
		if (items.length > 0) {
			ISymbianSDK sdk = (ISymbianSDK) items[0].getData();
			if (sdk != null){
				if (MessageDialog.openConfirm(composite.getShell(), Messages.getString("SdkChooserBase.confirm.delete") + sdk.getUniqueId() , Messages.getString("SdkChooserBase.confirm.message"))){ //$NON-NLS-1$ //$NON-NLS-2$
					if (getSDKManager().removeSDK(sdk.getUniqueId())){
						getSDKList().remove(sdk);
						getSDKManager().updateCarbideSDKCache();
						propagateSdkFromManager();
					}
				}
			}			
		}
	}

	public List<ISymbianSDK> getSDKList() {
		return SDKCorePlugin.getSDKManager().getSDKList();
	}

	public ISDKManager getSDKManager() {
		return SDKCorePlugin.getSDKManager();
	}

	public ISymbianSDK getSelectedSdk() {
		TableItem[] items = sdkTable.getSelection();
		if (items.length > 0) {
			return ((ISymbianSDK) items[0].getData());
		}
		return null;
	}

	public void setSelectedSdk(ISymbianSDK romSdk) {
		TableItem[] items = sdkTable.getItems();
		sdkTable.deselectAll();
		for (int i = 0; i < items.length; i++) {
			if ((ISymbianSDK) items[i].getData() == romSdk) {
				sdkTable.select(i);
			}
		}
		propagateSdkDetail();
	}
}
