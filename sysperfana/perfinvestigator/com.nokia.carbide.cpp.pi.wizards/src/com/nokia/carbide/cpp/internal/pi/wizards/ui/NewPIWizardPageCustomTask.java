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
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;

import com.nokia.carbide.cpp.internal.pi.wizards.ui.util.CarbidePiWizardHelpIds;


public class NewPIWizardPageCustomTask
extends NewPIWizardPage
implements INewPIWizardSettings
{
private final static String UID_PROPERTY= ".uid"; //$NON-NLS-1$
private final static String DAT_EXTENSION= "dat"; //$NON-NLS-1$

protected NewPIWizardPageCustomTask(final NewPIWizard wizard) {
	super(Messages.getString("NewPIWizardPageCustomTask.super.label")); //$NON-NLS-1$
	setTitle(Messages.getString("NewPIWizardPageCustomTask.title")); //$NON-NLS-1$
    setDescription(Messages.getString("NewPIWizardPageCustomTask.description")); //$NON-NLS-1$
}

// controls
Composite container;
Group formatGroup;
Composite optionComposite;
Group exampleGroup;
Group baseGroup;
Group seperatorGroup;
Button buttonValue;
Button buttonName;
Button buttonSemicolon;
Button buttonComma;
Button buttonSpace;

private Text sampleFileText;

public void createControl(Composite parent) {
	super.createControl(parent);
	container = new Composite(parent, SWT.NULL);
	container.setData(UID_PROPERTY, "container"); //$NON-NLS-1$
	GridLayout layout = new GridLayout();
	container.setLayout(layout);
	layout.numColumns = 1;
	layout.verticalSpacing = 9;

	Label labelDetailLabel = new Label(container, SWT.NONE);
	labelDetailLabel.setText(Messages.getString("NewPIWizardPageCustomTask.detail.label")); //$NON-NLS-1$

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

	sampleFileText = new Text(subContainer, SWT.BORDER | SWT.SINGLE);
	sampleFileText.setData(UID_PROPERTY, "sampleFileText"); //$NON-NLS-1$
	sampleFileText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
	sampleFileText.addModifyListener(new ModifyListener() {
		public void modifyText(ModifyEvent e) {
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
	
	// group with the options
	formatGroup = new Group(container, SWT.NONE);
	GridLayout groupLayout = new GridLayout();
	groupLayout.numColumns = 2;
	groupLayout.verticalSpacing = 9;
	formatGroup.setLayout(groupLayout);
	formatGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
	formatGroup.setText(Messages.getString("NewPIWizardPageCustomTask.format.group")); //$NON-NLS-1$
	optionComposite = new Composite (formatGroup, SWT.NONE);
	GridLayout optionLayout = new GridLayout();
	optionLayout.numColumns = 1;
	optionLayout.verticalSpacing = 9;
	optionComposite.setLayout(optionLayout);
	optionComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
	
	exampleGroup = new Group(formatGroup, SWT.NONE);
	GridLayout exampleLayout = new GridLayout();
	exampleLayout.numColumns = 2;
	exampleLayout.verticalSpacing = 9;
	exampleGroup.setLayout(groupLayout);
	exampleGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
	exampleGroup.setText(Messages.getString("NewPIWizardPageCustomTask.example.group")); //$NON-NLS-1$
	Text exampleText = new Text(exampleGroup, SWT.NONE);
	exampleText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
	baseGroup = new Group(optionComposite, SWT.NONE);
	GridLayout baseLayout = new GridLayout();
	baseLayout.numColumns = 1;
	baseLayout.verticalSpacing = 9;
	baseGroup.setLayout(optionLayout);
	baseGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
	baseGroup.setText(Messages.getString("NewPIWizardPageCustomTask.base.group")); //$NON-NLS-1$
	buttonValue = new Button(baseGroup, SWT.RADIO);
	buttonValue.setText(Messages.getString("NewPIWizardPageCustomTask.button.value")); //$NON-NLS-1$
	buttonName = new Button(baseGroup, SWT.RADIO);
	buttonName.setText(Messages.getString("NewPIWizardPageCustomTask.button.name")); //$NON-NLS-1$
	seperatorGroup = new Group(optionComposite, SWT.NONE);
	GridLayout seperatorLayout = new GridLayout();
	seperatorLayout.numColumns = 1;
	seperatorLayout.verticalSpacing = 9;
	seperatorGroup.setLayout(optionLayout);
	seperatorGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
	seperatorGroup.setText(Messages.getString("NewPIWizardPageCustomTask.seperator.group")); //$NON-NLS-1$
	buttonSemicolon = new Button(seperatorGroup, SWT.RADIO);
	buttonSemicolon.setText(Messages.getString("NewPIWizardPageCustomTask.button.semicolon")); //$NON-NLS-1$
	buttonComma = new Button(seperatorGroup, SWT.RADIO);
	buttonComma.setText(Messages.getString("NewPIWizardPageCustomTask.button.comma")); //$NON-NLS-1$
	buttonSpace = new Button(seperatorGroup, SWT.RADIO);
	buttonSpace.setText(Messages.getString("NewPIWizardPageCustomTask.button.space")); //$NON-NLS-1$
	
	// fake default for demo
	sampleFileText.setText(Messages.getString("NewPIWizardPageCustomTask.13")); //$NON-NLS-1$
	buttonValue.setSelection(true);
	buttonName.setSelection(false);
	buttonSemicolon.setSelection(true);
	buttonComma.setSelection(false);
	buttonSpace.setSelection(false);
	exampleText.setText(Messages.getString("NewPIWizardPageCustomTask.14")); //$NON-NLS-1$
	
	
	validatePage();
	setControl(container);
	PlatformUI.getWorkbench().getHelpSystem().setHelp(getControl(), CarbidePiWizardHelpIds.PI_IMPORT_WIZARD_CUSTOM);
}

public void validatePage() {
	if (sampleFileText.getText().length() == 0) {
		updateStatus(Messages.getString("NewPIWizardCustomTask.dat.file.name.must.be.specified")); //$NON-NLS-1$
		return;
	}
	int dotLoc = sampleFileText.getText().lastIndexOf('.');
	if (dotLoc != -1) {
		String ext = sampleFileText.getText().substring(dotLoc + 1);
		if (ext.equalsIgnoreCase(DAT_EXTENSION) == false) {
			updateStatus(Messages.getString("NewPIWizardPageSampleFile.dat.file.extension.must.be.dat")); //$NON-NLS-1$
			return;
		}
	}


	if (!(new File(sampleFileText.getText()).exists())) {
		updateStatus(Messages.getString("NewPIWizardPageSampleFile.dat.file.name.does.not.exist")); //$NON-NLS-1$
		return;
	}
	if (!(new File(sampleFileText.getText()).isFile())) {
		updateStatus(Messages.getString("NewPIWizardPageSampleFile.dat.must.be.a.file")); //$NON-NLS-1$
		return;		
	}

	updateStatus(null);
}

private void updateStatus(String message) {
	// need to enable finish, setPageComplete eventually check global states
	writePageDataToNewPIWizardSettings();
	setErrorMessage(message);
	setPageComplete(message == null);
}
	
private void handleBrowse() {
	FileDialog dialog = new FileDialog(getShell());
	String[] datExtensions = {"*.dat", //$NON-NLS-1$
//								"*.base64",
								"*.*"}; //$NON-NLS-1$
	String[] datNames = {Messages.getString("NewPIWizardPageSampleFile.sample.filter.name"), //$NON-NLS-1$
//							"Profiler Sample Files (*.base64)",
							Messages.getString("NewPIWizardPageSampleFile.all.filter.name")}; //$NON-NLS-1$
	dialog.setFilterExtensions(datExtensions);
	dialog.setFilterNames(datNames);
	// Try guiding user to path user is trying to fill
	File clueFile = new File(sampleFileText.getText());
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
			sampleFileText.setText(filePath);
		}
	}		
}

public void writePageDataToNewPIWizardSettings() {
}

public void setupPageFromFromNewPIWizardSettings() {
}
}

