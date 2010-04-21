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

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.ui.PlatformUI;

import com.nokia.carbide.cpp.internal.pi.wizards.ui.util.CarbidePiWizardHelpIds;

public class NewPIWizardPageInputTask extends NewPIWizardPage implements
		INewPIWizardSettings {
	private FileSelectionGroup fileSelectionGroup;
	private TraceSelectionGroup traceSelectionGroup;

	protected NewPIWizardPageInputTask(final NewPIWizard wizard) {
		super(Messages.getString("NewPIWizardPageSampleFile.title")); //$NON-NLS-1$
		super.setWizard(wizard);
		setTitle(Messages.getString("NewPIWizardPageSampleFile.title")); //$NON-NLS-1$
	    setDescription(Messages.getString("NewPIWizardPageSampleFile.description")); //$NON-NLS-1$
	}

	public void createControl(Composite parent) {
		super.createControl(parent);
		Composite container = new Composite(parent, SWT.NULL);
		GridLayout layout = new GridLayout();
		container.setLayout(layout);
		layout.numColumns = 1;

		//createRadioButtonGroup(container); // not implemented yet
		fileSelectionGroup = new FileSelectionGroup(container, this);
		fileSelectionGroup.setVisible(true);

		traceSelectionGroup = new TraceSelectionGroup(container,this);
		setControl(container);
		PlatformUI.getWorkbench().getHelpSystem().setHelp(getControl(),
				CarbidePiWizardHelpIds.PI_IMPORT_WIZARD_INPUT);
	}

	private void createRadioButtonGroup(Composite parent) {
		// Radio button group
		GridLayout radioButtonGroupGridLayout = new GridLayout();
		Group radioButtonGroup = new Group(parent, SWT.NONE);
		radioButtonGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		radioButtonGroup.setText(Messages.getString("NewPIWizardPageInputTask.profilerDataSourceGroupTitle")); //$NON-NLS-1$
		radioButtonGroup.setLayout(radioButtonGroupGridLayout);
		GridData radioButtonGridData = new GridData(GridData.FILL_HORIZONTAL);
		radioButtonGridData.horizontalSpan = 2;

		// File radio button
		final Button fileRadioButton = new Button(radioButtonGroup, SWT.RADIO);
		fileRadioButton.setText(Messages.getString("NewPIWizardPageInputTask.fromFileSystem")); //$NON-NLS-1$
		fileRadioButton.setLayoutData(radioButtonGridData);
		fileRadioButton.addSelectionListener(new SelectionListener() {

			public void widgetSelected(SelectionEvent e) {
				if (fileRadioButton.getSelection()) {
					fileSelectionGroup.setVisible(true);
				}

			}

			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
		fileRadioButton.setSelection(true);

		// From Device via TraceViewer radio button
		final Button deviceRadioButton = new Button(radioButtonGroup, SWT.RADIO);
		deviceRadioButton
				.setText(Messages.getString("NewPIWizardPageInputTask.fromDevice")); //$NON-NLS-1$
		deviceRadioButton.setLayoutData(radioButtonGridData);
		deviceRadioButton.addSelectionListener(new SelectionListener() {

			public void widgetSelected(SelectionEvent e) {
				if (deviceRadioButton.getSelection()) {
				}
			}

			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
		deviceRadioButton.setSelection(false);

		// not implemented yet
		deviceRadioButton.setEnabled(false);
	}

	public void validatePage() {
		if(traceSelectionGroup != null){
			traceSelectionGroup.updateTraceIds(fileSelectionGroup.getSelectedItem());					
			((NewPIWizard)getWizard()).setProfilerDataFiles(fileSelectionGroup.getProfilerDataFiles()); 
		}
		if(fileSelectionGroup.getProfilerDataFiles().size() > 1){
			setMessage(Messages.getString("NewPIWizardPageInputTask.noteImportMayTakeSeveralMinutes")); //$NON-NLS-1$
		}else{
			setMessage(Messages.getString("NewPIWizardPageSampleFile.description"));
		}
		if(fileSelectionGroup.getProfilerDataFiles().isEmpty()){
			setPageComplete(false);
		} else {	
			setPageComplete(true);
			updateStatus(null);
		}

	}

	private void updateStatus(String message) {
		// need to enable finish, setPageComplete eventually check global states
		setErrorMessage(message);
		setPageComplete(message == null);
	}

	public void setVisible(boolean visable) {
		super.setVisible(visable);
		if (visable) {
			validatePage();
		}
	}

	public void setupPageFromFromNewPIWizardSettings() {		
		// do nothing
	}
}
