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

import java.io.File;
import java.util.ArrayList;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;

import com.nokia.carbide.cpp.pi.button.BupEventMapManager;
import com.nokia.carbide.cpp.pi.button.ComNokiaCarbidePiButtonHelpIDs;
import com.nokia.carbide.cpp.pi.button.IBupEventMapProfile;


public class ImportBupMapGetXmlTask extends WizardPage {
	private final static String XML_EXTENSION= "xml"; //$NON-NLS-1$
	private boolean visableBefore = false;
	String importXmlPathString = null;
	
	ArrayList<IBupEventMapProfile> overlapList = new ArrayList<IBupEventMapProfile>();
	
	protected ImportBupMapGetXmlTask() {
		super(Messages.getString("ImportBupMapGetXmlTask.selectProfile"));  //$NON-NLS-1$
		setTitle(Messages.getString("ImportBupMapGetXmlTask.selectProfile"));  //$NON-NLS-1$
		setDescription(Messages.getString("ImportBupMapGetXmlTask.importProfileExplained"));  //$NON-NLS-1$
	}

	// controls
	Composite container;
	private Text inputXmlFileText;
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
	 */
	public void createControl(Composite parent) {
		container = new Composite(parent, SWT.NULL);
		GridLayout layout = new GridLayout();
		container.setLayout(layout);
		layout.numColumns = 1;
		layout.verticalSpacing = 9;

		Label labelSampleFile = new Label(container, SWT.NONE);
		labelSampleFile.setText(Messages.getString("ImportBupMapGetXmlTask.importThisProfile"));  //$NON-NLS-1$
		
		// subcomposite with textbox/button
		Composite subContainer = new Composite(container, SWT.NULL);
		GridLayout subLayout = new GridLayout();
		subContainer.setLayout(subLayout);
		subContainer.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		subLayout.numColumns = 2;
		subLayout.verticalSpacing = 9;

		inputXmlFileText = new Text(subContainer, SWT.BORDER | SWT.SINGLE);
		inputXmlFileText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		inputXmlFileText.addModifyListener(new ModifyListener(){
			public void modifyText(ModifyEvent arg0) {
				validatePage();
			}
		});

		Button button = new Button(subContainer, SWT.PUSH);
		button.setText(Messages.getString("ImportBupMapGetXmlTask.keyPressProfile"));  //$NON-NLS-1$
		button.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				handleBrowse();
			}
		});
			
		// top tap get focus
		inputXmlFileText.setFocus();

		setControl(container);
		PlatformUI.getWorkbench().getHelpSystem().setHelp(getControl(), ComNokiaCarbidePiButtonHelpIDs.PI_KEY_MAP_IMPORT_WIZARD_XML);
	}

	public void validatePage() {
		if (inputXmlFileText.getText() == null || inputXmlFileText.getText().length() < 1) {
			updateStatus(null);
			setPageComplete(false);
			return;		// blank should be OK, consistent with Eclipse guideline
		}
		
		int dotLoc = inputXmlFileText.getText().lastIndexOf('.');
		if (dotLoc != -1) {
			String ext = inputXmlFileText.getText().substring(dotLoc + 1);
			if (ext.equalsIgnoreCase(XML_EXTENSION) == false) {
				updateStatus(Messages.getString("ImportBupMapGetXmlTask.mustEndWithXML"));  //$NON-NLS-1$
				return;
			}
		}

		if (!(new File(inputXmlFileText.getText()).exists())) {
			updateStatus(Messages.getString("ImportBupMapGetXmlTask.fileDoesNotExist"));  //$NON-NLS-1$
			return;
		}
		if (!(new File(inputXmlFileText.getText()).isFile())) {
			updateStatus(Messages.getString("ImportBupMapGetXmlTask.enterProfileFileName"));  //$NON-NLS-1$
			return;		
		}
		
		// this file looks good, let's see if it got overlap
		java.io.File file = new java.io.File(inputXmlFileText.getText());
		overlapList = BupEventMapManager.getInstance().getOverLapWithWorkspace(file.toURI());
		importXmlPathString = inputXmlFileText.getText();
		((ImportBupMapWizard)getWizard()).setAllowFinish(overlapList.size() == 0);

		setPageComplete(true);
		updateStatus(null);
	}

	private void updateStatus(String message) {
		// need to enable finish, setPageComplete eventually check global states
		setErrorMessage(message);
		setPageComplete(message == null);
	}
		
	private void handleBrowse() {
		FileDialog dialog = new FileDialog(getShell());
		String[] datExtensions = {"*.xml", //$NON-NLS-1$
									"*.*"}; //$NON-NLS-1$
		String[] datNames = {Messages.getString("ImportBupMapGetXmlTask.profileFiles"),  //$NON-NLS-1$
								Messages.getString("ImportBupMapGetXmlTask.allFiles")};  //$NON-NLS-1$
		dialog.setFilterExtensions(datExtensions);
		dialog.setFilterNames(datNames);
		// Try guiding user to path user is trying to fill
		File clueFile = new File(inputXmlFileText.getText());
		String cluePath = null;
		if (clueFile.isDirectory()) {
			cluePath = clueFile.getAbsolutePath();
		} else {
			cluePath = clueFile.getParent();
		}
		if (cluePath != null) {
			dialog.setFilterPath(cluePath);
		}
		String filePath = dialog.open();

		if (filePath != null) {
			if (filePath.length() > 0) {
				inputXmlFileText.setText(filePath);
			}
		}		
	}

	public void setVisible(boolean visable) {
		super.setVisible(visable);
		
		if (visable) {
			// block the text if it's wrong on startup, so user know what's wrong
			if (visableBefore == false && getErrorMessage() != null) {
				inputXmlFileText.selectAll();
			}
			if (new java.io.File(inputXmlFileText.getText()).exists()) {
				if (overlapList.size() > 0) {
					((ImportBupMapWizard)getWizard()).setAllowFinish(false);
				}
				((ImportBupMapWizard)getWizard()).setAllowFinish(true);
			} else {
				((ImportBupMapWizard)getWizard()).setAllowFinish(false);
			}
		} else {
			((ImportBupMapWizard)getWizard()).setAllowFinish(true);
		}
		visableBefore = visable;
	}

	/**
	 * @return
	 */
	public String[] getOverLapList() {
		ArrayList<String> resultList = new ArrayList<String>();

		for (IBupEventMapProfile overlapProfile : overlapList) {
			resultList.add(overlapProfile.getProfileId());
		}
		
		return resultList.toArray(new String[resultList.size()]);
	}

	/**
	 * @return
	 */
	public String getxImportXml() {
		return importXmlPathString;
	}
}
