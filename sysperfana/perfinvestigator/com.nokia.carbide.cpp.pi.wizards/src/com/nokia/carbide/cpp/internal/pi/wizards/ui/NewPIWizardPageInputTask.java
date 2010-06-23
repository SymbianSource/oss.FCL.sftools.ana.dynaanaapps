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

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

import com.nokia.carbide.cpp.internal.pi.wizards.ui.util.CarbidePiWizardHelpIds;
import com.nokia.carbide.cpp.pi.PiPlugin;

public class NewPIWizardPageInputTask extends NewPIWizardPage implements
		INewPIWizardSettings {
	private FileSelectionGroup fileSelectionGroup;
	private ProfilerActivatorGroup profilerActivatorGroup;
	private Button fileRadioButton;
	private Button deviceRadioButton;
	
	/**
	 *  TraceViewers ID com.nokia.traceviewer.view.TraceViewerView
	 */
	private static final String TRACE_VIEWER_VIEW_ID = "com.nokia.traceviewer.view.TraceViewerView"; //$NON-NLS-1$
	

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

		createRadioButtonGroup(container);
		fileSelectionGroup = new FileSelectionGroup(container, this);	
		if(PiPlugin.isTraceProviderAvailable()){
			profilerActivatorGroup = new ProfilerActivatorGroup(container, this, this, getContainer());
			fileSelectionGroup.setVisible(profilerActivatorGroup);
		}
		
		setControl(container);
		PlatformUI.getWorkbench().getHelpSystem().setHelp(getControl(),
				CarbidePiWizardHelpIds.PI_IMPORT_WIZARD_INPUT);
	}
	
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.DialogPage#performHelp()
	 */
	@Override
	public void performHelp() {
		WizardDialog wizardDialog = (WizardDialog)getContainer();			
		if(wizardDialog.buttonBar != null){			
			PlatformUI.getWorkbench().getHelpSystem().setHelp(wizardDialog.buttonBar,
					CarbidePiWizardHelpIds.PI_IMPORT_WIZARD_INPUT);
		}
	
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
		fileRadioButton = new Button(radioButtonGroup, SWT.RADIO);
		fileRadioButton.setText(Messages.getString("NewPIWizardPageInputTask.fromFileSystem")); //$NON-NLS-1$
		fileRadioButton.setLayoutData(radioButtonGridData);
		fileRadioButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {					
				if(e.widget == fileRadioButton){
					if(!fileSelectionGroup.isVisible()){
						setErrorMessage(null);
						NewPIWizardSettings.getInstance().profilerActivator = false;
						fileSelectionGroup.setVisible(profilerActivatorGroup);
					}
				}
			}
		});
		fileRadioButton.setSelection(true);

		// From Device via TraceViewer radio button
		deviceRadioButton = new Button(radioButtonGroup, SWT.RADIO);
		deviceRadioButton
				.setText(Messages.getString("NewPIWizardPageInputTask.fromDevice")); //$NON-NLS-1$
		deviceRadioButton.setLayoutData(radioButtonGridData);
		deviceRadioButton.addSelectionListener(new SelectionAdapter() {

			public void widgetSelected(SelectionEvent e) {			
				if(e.widget == deviceRadioButton){
					if(!profilerActivatorGroup.isVisible()){
						setErrorMessage(null);
						
						NewPIWizardSettings.getInstance().profilerActivator = true;
						profilerActivatorGroup.setVisible(fileSelectionGroup);
					}
				}
			}
		});
		deviceRadioButton.setSelection(false);
		
		// In case trace plugin is not available, disabling profiler activator part
		if(!PiPlugin.isTraceProviderAvailable()){
			deviceRadioButton.setEnabled(false);
		}
	}

	public void validatePage(){
		IStatus status = null;

		if(fileRadioButton.getSelection()){
			status = fileSelectionGroup.validateContent((NewPIWizard)getWizard());		
		}else{
			status = profilerActivatorGroup.validateContent((NewPIWizard)getWizard());
		}	
		setErrorMessage(null);
		if(status.getSeverity() == Status.OK){
			updateStatus(null);		
		}else if(status.getSeverity() == Status.INFO){		
			setMessage(status.getMessage());
			setPageComplete(false);			
		}else if(status.getSeverity() == Status.WARNING){
			setMessage(status.getMessage());			
			setPageComplete(true);		
		}else{
			updateStatus(status.getMessage());		
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
			showTraceViewer();
			validatePage();
		}
	}

	public void setupPageFromFromNewPIWizardSettings() {		
		// do nothing
	}

	/**
	 * Shows trace viewer plugin's view
	 */
	public void showTraceViewer() {
    	try {
    		IWorkbenchWindow workbenchWindow = PiPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow();
    		if (workbenchWindow == null)
    			return;
    		IWorkbenchPage page = workbenchWindow.getActivePage();
    		// Checking if view is already open
    		IViewReference[] viewRefs = page.getViewReferences();
    		for (int i = 0; i < viewRefs.length; i++) {
				IViewReference reference = viewRefs[i];
				String id = reference.getId();
				if(id.equalsIgnoreCase(TRACE_VIEWER_VIEW_ID)){
					// Found, restoring the view
					IViewPart viewPart = reference.getView(true);
					page.activate(viewPart);
					return;
				}
			}
    		
    		// View was not found, opening it up as a new view.
    		page.showView(TRACE_VIEWER_VIEW_ID);
    	} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.wizard.WizardPage#getNextPage()
	 */
	@Override
	public IWizardPage getNextPage() {
		if(PiPlugin.isTraceProviderAvailable() && PiPlugin.getTraceProvider().isListening()){
			((NewPIWizard)getWizard()).showInformationDialog();
			return this;
		}
		return super.getNextPage();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.wizard.WizardPage#getPreviousPage()
	 */
	@Override
	public IWizardPage getPreviousPage() {
		if(PiPlugin.isTraceProviderAvailable() && PiPlugin.getTraceProvider().isListening()){
			((NewPIWizard)getWizard()).showInformationDialog();
			return this;
		}
		return super.getPreviousPage();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.swt.widgets.Widget#dispose()
	 */
	@Override
	public void dispose() {	
		if(!getWizard().canFinish()){
			handleTemporaryProfilerDataFiles(false);
		}else if(!NewPIWizardSettings.getInstance().profilerActivator){
			handleTemporaryProfilerDataFiles(true);
		}
		
	
		super.dispose();
	}	
		
	public void setButtonGroupEnabled(boolean enabled){
		deviceRadioButton.setEnabled(enabled);
		fileRadioButton.setEnabled(enabled);
	}	
	
	public void handleTemporaryProfilerDataFiles(boolean forceRemove){
		if(PiPlugin.isTraceProviderAvailable()){
			profilerActivatorGroup.handleTemporaryProfilerDataFiles(forceRemove);
		}
	}
}
