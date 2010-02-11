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
import java.io.IOException;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;

import com.nokia.carbide.cpp.internal.pi.analyser.StreamFileParser;
import com.nokia.carbide.cpp.internal.pi.wizards.ui.util.CarbidePiWizardHelpIds;



public class NewPIWizardPageInputTask
extends NewPIWizardPage
implements INewPIWizardSettings
{
	private final static String UID_PROPERTY= ".uid"; //$NON-NLS-1$
	private final static String DAT_EXTENSION= "dat"; //$NON-NLS-1$
	private boolean visableBefore = false;

	protected NewPIWizardPageInputTask(final NewPIWizard wizard) {
		super(Messages.getString("NewPIWizardPageSampleFile.name")); //$NON-NLS-1$
		setTitle(Messages.getString("NewPIWizardPageSampleFile.title")); //$NON-NLS-1$
	    setDescription(Messages.getString("NewPIWizardPageSampleFile.description")); //$NON-NLS-1$
	}

	// controls
	Composite container;
	private Text sampleFileTexti;
	private Combo sampleFileCombo;

	public void createControl(Composite parent) {
		super.createControl(parent);
		container = new Composite(parent, SWT.NULL);
		container.setData(UID_PROPERTY, "container"); //$NON-NLS-1$
		GridLayout layout = new GridLayout();
		container.setLayout(layout);
		layout.numColumns = 1;
		layout.verticalSpacing = 9;

		Label labelSampleFile = new Label(container, SWT.NONE);
		labelSampleFile.setText(Messages.getString("NewPIWizardPageSampleFile.dat.file.name")); //$NON-NLS-1$
		
		// subcomposite with textbox/button
		Composite subContainer = new Composite(container, SWT.NULL);
		subContainer.setData(UID_PROPERTY, "subContainer"); //$NON-NLS-1$
		GridLayout subLayout = new GridLayout();
		subContainer.setLayout(subLayout);
		subContainer.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		subLayout.numColumns = 2;
		subLayout.verticalSpacing = 9;

		/*
		sampleFileText = new Text(subContainer, SWT.BORDER | SWT.SINGLE);
		sampleFileText.setData(UID_PROPERTY, "sampleFileText"); //$NON-NLS-1$
		sampleFileText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		sampleFileText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				// write back to our status
				NewPIWizardSettings.getInstance().sampleFileName = sampleFileText.getText();
				NewPIWizardSettings.getInstance().sampleFileNameModifiedNanoTime = System.nanoTime();
				validatePage();
			}
		});*/
		
		sampleFileCombo = new Combo(subContainer, SWT.BORDER | SWT.SINGLE);
		sampleFileCombo.setData(UID_PROPERTY, "sampleFileText"); //$NON-NLS-1$
		sampleFileCombo.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		sampleFileCombo.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				// write back to our status
				NewPIWizardSettings.getInstance().sampleFileName = sampleFileCombo.getText();
				NewPIWizardSettings.getInstance().sampleFileNameModifiedNanoTime = System.nanoTime();
				validatePage();
			}
		});

		Button button = new Button(subContainer, SWT.PUSH);
		button.setText(Messages.getString("NewPIWizardPageSampleFile.browse")); //$NON-NLS-1$
		button.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				handleBrowse();
			}
		});
			
		// top tap get focus
		sampleFileCombo.setFocus();


		setControl(container);
		PlatformUI.getWorkbench().getHelpSystem().setHelp(getControl(), CarbidePiWizardHelpIds.PI_IMPORT_WIZARD_INPUT);
	}

	public void validatePage() {
		if (sampleFileCombo.getText() == null || sampleFileCombo.getText().length() < 1) {
			
			updateStatus(null);
			setPageComplete(false);
			return;		// blank should be OK, consistent with Eclipse guideline
		}
		
		int dotLoc = sampleFileCombo.getText().lastIndexOf('.');

		if (dotLoc != -1) {
			String ext = sampleFileCombo.getText().substring(dotLoc + 1);

			if (ext.equalsIgnoreCase(DAT_EXTENSION) == false) {
				updateStatus(Messages.getString("NewPIWizardPageSampleFile.dat.file.extension.must.be.dat")); //$NON-NLS-1$
				return;
			}
		}

		if (!(new File(sampleFileCombo.getText()).exists())) {
			updateStatus(Messages.getString("NewPIWizardPageSampleFile.dat.file.name.does.not.exist")); //$NON-NLS-1$
			return;
		}
		if (!(new File(sampleFileCombo.getText()).isFile())) {

			updateStatus(Messages.getString("NewPIWizardPageSampleFile.dat.must.be.a.file")); //$NON-NLS-1$
			return;		
		}

		try {
			((NewPIWizard)getWizard()).setTraceSet(new StreamFileParser(new File(sampleFileCombo.getText())).allTraceType());

		} catch (IOException e) {
			// just ignore
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
		String[] datExtensions = {"*.dat", //$NON-NLS-1$
//									"*.base64",
									"*.*"}; //$NON-NLS-1$
		String[] datNames = {Messages.getString("NewPIWizardPageSampleFile.sample.filter.name"), //$NON-NLS-1$
//								"Profiler Sample Files (*.base64)",
								Messages.getString("NewPIWizardPageSampleFile.all.filter.name")}; //$NON-NLS-1$
		dialog.setFilterExtensions(datExtensions);
		dialog.setFilterNames(datNames);
		// Try guiding user to path user is trying to fill
		File clueFile = new File(sampleFileCombo.getText());

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
				sampleFileCombo.setText(filePath);

			}
		}		
	}

	public void setupPageFromFromNewPIWizardSettings() {
		
		String[] items = NewPIWizardSettings.getInstance().sampleFileNames;
		if (items != null){
			sampleFileCombo.setItems(items);
			sampleFileCombo.select(0);
		}
	}

	public void setVisible(boolean visable) {
		super.setVisible(visable);
		
		if (visable) {
			// block the text if it's wrong on startup, so user know what's wrong
			if (visableBefore == false && getErrorMessage() != null) {
				sampleFileCombo.select(0);

			}
		}
		visableBefore = visable;
	}
}

