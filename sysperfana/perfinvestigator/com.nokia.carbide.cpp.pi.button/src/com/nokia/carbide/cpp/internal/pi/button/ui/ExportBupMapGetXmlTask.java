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

import com.nokia.carbide.cpp.pi.button.ComNokiaCarbidePiButtonHelpIDs;


public class ExportBupMapGetXmlTask extends WizardPage {
	String exportXmlPathString = null;
	
	protected ExportBupMapGetXmlTask() {
		super(Messages.getString("ExportBupMapGetXmlTask.selectProfile"));   //$NON-NLS-1$
		setTitle(Messages.getString("ExportBupMapGetXmlTask.selectProfile"));   //$NON-NLS-1$
		setDescription(Messages.getString("ExportBupMapGetXmlTask.exportProfileExplained"));   //$NON-NLS-1$
	}

	// controls
	Composite container;
	private Text exportXmlFileText;
	
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
		labelSampleFile.setText(Messages.getString("ExportBupMapGetXmlTask.exportThisProfile"));   //$NON-NLS-1$
		
		// subcomposite with textbox/button
		Composite subContainer = new Composite(container, SWT.NULL);
		GridLayout subLayout = new GridLayout();
		subContainer.setLayout(subLayout);
		subContainer.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		subLayout.numColumns = 2;
		subLayout.verticalSpacing = 9;

		exportXmlFileText = new Text(subContainer, SWT.BORDER | SWT.SINGLE);
		exportXmlFileText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		exportXmlFileText.addModifyListener(new ModifyListener(){
			public void modifyText(ModifyEvent arg0) {
				validatePage();
			}
		});

		Button button = new Button(subContainer, SWT.PUSH);
		button.setText(Messages.getString("ExportBupMapGetXmlTask.keyPressProfile"));   //$NON-NLS-1$
		button.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				handleBrowse();
			}
		});
			
		// top tap get focus
		exportXmlFileText.setFocus();

		setControl(container);
		PlatformUI.getWorkbench().getHelpSystem().setHelp(getControl(), ComNokiaCarbidePiButtonHelpIDs.PI_KEY_MAP_EXPORT_WIZARD_XML);
	}

	public void validatePage() {
		if (exportXmlFileText.getText() == null || exportXmlFileText.getText().length() < 1) {
			updateStatus(null);
			setPageComplete(false);
			return;		// blank should be OK, consistent with Eclipse guideline
		}
		
		setMessage(null, WARNING);
		String xmlFileString = exportXmlFileText.getText();
		File xmlFile = new File (xmlFileString);
		if (xmlFile != null && !xmlFile.isDirectory()) {
			String parentString = xmlFile.getParent();
			if (parentString != null) {
				File parentFile = new File(parentString);
				if (parentFile.isDirectory() && parentFile.exists()) {
					if (!xmlFileString.endsWith(".xml")) { //$NON-NLS-1$
						updateStatus (Messages.getString("ExportBupMapGetXmlTask.extensionMustBeXML"));   //$NON-NLS-1$
						return;
					}
				} else {
					updateStatus(Messages.getString("ExportBupMapGetXmlTask.directory") + parentString + Messages.getString("ExportBupMapGetXmlTask.doesNotExist"));     //$NON-NLS-1$ //$NON-NLS-2$
					return;
				}
			} else {
				updateStatus(Messages.getString("ExportBupMapGetXmlTask.directory") + xmlFileString + Messages.getString("ExportBupMapGetXmlTask.doesNotExist"));     //$NON-NLS-1$ //$NON-NLS-2$
				return;
			}
		} else {
			updateStatus(Messages.getString("ExportBupMapGetXmlTask.enterFileName"));   //$NON-NLS-1$
			return;
		}

		exportXmlPathString = xmlFileString;
		if (new File(exportXmlPathString).exists()) {
			setMessage(exportXmlPathString + Messages.getString("ExportBupMapGetXmlTask.willBeOverwritten"), WARNING); //$NON-NLS-1$
		}
		
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
		String[] datNames = {Messages.getString("ExportBupMapGetXmlTask.profilerFiles"),   //$NON-NLS-1$
								Messages.getString("ExportBupMapGetXmlTask.allFiles")};   //$NON-NLS-1$
		dialog.setFilterExtensions(datExtensions);
		dialog.setFilterNames(datNames);
		// Try guiding user to path user is trying to fill
		File clueFile = new File(exportXmlFileText.getText());
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
				if (!filePath.endsWith(".xml")) {	//$NON-NLS-1$
					filePath += ".xml";	//$NON-NLS-1$
				}
				exportXmlFileText.setText(filePath);
			}
		}		
	}
	
	public String getExportXml() {
		return exportXmlPathString;
	}
}
