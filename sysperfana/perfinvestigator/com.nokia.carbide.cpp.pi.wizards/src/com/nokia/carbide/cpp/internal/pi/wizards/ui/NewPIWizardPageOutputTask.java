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

import java.util.ArrayList;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.DecoratingLabelProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchWizard;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.model.WorkbenchContentProvider;
import org.eclipse.ui.model.WorkbenchLabelProvider;
import org.eclipse.ui.wizards.newresource.BasicNewProjectResourceWizard;

import com.nokia.carbide.cpp.internal.pi.wizards.ui.util.CarbidePiWizardHelpIds;

public class NewPIWizardPageOutputTask extends NewPIWizardPage implements INewPIWizardSettings {

	// Provides all folder in workspace
	private class ProjectContentProvider
	extends WorkbenchContentProvider
	implements ITreeContentProvider
	{
		// local version just to prevent getChildren/hadChild stuck
		// calling each other
		private Object[] localGetChildren(Object arg0) {
			ArrayList<Object> returnList = new ArrayList<Object>();
			Object[] children = super.getChildren(arg0);
			for (Object child: children) {
				if (child instanceof IViewSite || 
					child instanceof IWorkspaceRoot || 
					child instanceof IFolder)
				{
					returnList.add(child);
				} else if (child instanceof IProject) {
					if (((IProject)child).isOpen()) {
						returnList.add(child);
					}
				}
			}
			return returnList.toArray(new Object[returnList.size()]);
		}

		public Object[] getChildren(Object arg0) {
			return localGetChildren(arg0);
		}

		public Object getParent(Object arg0) {
			return super.getParent(arg0);
		}

		public boolean hasChildren(Object arg0) {
			if (localGetChildren(arg0).length > 0) {
				return true;
			}
			return false;
		}

		public Object[] getElements(Object arg0) {
			return super.getElements(arg0);
		}

		public void dispose() {
			super.dispose();
		}

		public void inputChanged(Viewer arg0, Object arg1, Object arg2) {
			super.inputChanged(arg0, arg1, arg2);
		}
	}
	
	// control
	private Composite composite = null;
	private Composite projectComposite = null;
	private Composite buttonComposite = null;
	private TreeViewer outputChooserTreeViewer = null;
	private Button createButton = null;
	
	protected NewPIWizardPageOutputTask() {
		super(""); //$NON-NLS-1$
		setTitle(Messages.getString("NewPIWizardPageOutputTask.output.file.and.project")); //$NON-NLS-1$
	    setDescription(Messages.getString("NewPIWizardPageOutputTask.select.folder.or.create.new.project.as.output.file.container")); //$NON-NLS-1$
	}

	public void validatePage() {
		if (outputChooserTreeViewer.getTree().getSelection().length < 1) {
			setErrorMessage(Messages.getString("NewPIWizardPageOutputTask.choose.output.project")); //$NON-NLS-1$
			setPageComplete(false);
			return;
		}
		setErrorMessage(null);
		setPageComplete(true);
		return;
	}

	public void createControl(Composite parent) {
		super.createControl(parent);
		GridLayout gridLayout1 = new GridLayout();
		gridLayout1.numColumns = 1;
		composite = new Composite(parent, SWT.NONE);
		composite.setLayout(gridLayout1);
		composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		createProjectComposite();
		
		validatePage();

		setControl(composite);
		PlatformUI.getWorkbench().getHelpSystem().setHelp(getControl(), CarbidePiWizardHelpIds.PI_IMPORT_WIZARD_OUTPUT);		
	}
	
	/**
	 * This method initializes projectComposite	
	 *
	 */
	private void createProjectComposite() {
		GridLayout gridLayout2 = new GridLayout();
		gridLayout2.numColumns = 2;
		projectComposite = new Composite(composite, SWT.NONE);
		projectComposite.setLayout(gridLayout2);
		projectComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		outputChooserTreeViewer = new TreeViewer(projectComposite, SWT.BORDER);
		outputChooserTreeViewer.getControl().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		outputChooserTreeViewer.setContentProvider(new ProjectContentProvider());
		outputChooserTreeViewer.setLabelProvider(new DecoratingLabelProvider(
								new WorkbenchLabelProvider(), 
								PlatformUI.getWorkbench().getDecoratorManager().getLabelDecorator()));
		outputChooserTreeViewer.setInput(ResourcesPlugin.getWorkspace().getRoot());
		outputChooserTreeViewer.getTree().addSelectionListener(new SelectionListener() {

			public void widgetDefaultSelected(SelectionEvent arg0) {
			}

			public void widgetSelected(SelectionEvent arg0) {	
				validatePage();
			}
		});
		outputChooserTreeViewer.addSelectionChangedListener(new ISelectionChangedListener() {

			public void selectionChanged(SelectionChangedEvent arg0) {
				TreeSelection selection = (TreeSelection) outputChooserTreeViewer.getSelection();
				if (selection != null) {
					Object selected = selection.getFirstElement();
					if (selected != null) {
						if (selected instanceof IContainer) {
							NewPIWizardSettings.getInstance().outputContainer = (IContainer) selected;
						}
					}
				}
			}
		});

		createButtonComposite();
	}
	
	private void createButtonComposite() {
		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 1;
		buttonComposite = new Composite(projectComposite, SWT.NONE);
		buttonComposite.setLayout(gridLayout);
		buttonComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, true));
		createButton = new Button(buttonComposite, SWT.PUSH);
		createButton.setText(Messages.getString("NewPIWizardPageOutputTask.create.empty.project")); //$NON-NLS-1$
		createButton.addSelectionListener(new SelectionListener() {

			public void widgetDefaultSelected(SelectionEvent arg0) {
			}

			public void widgetSelected(SelectionEvent arg0) {
				// get a standard Eclipse Wizard for creating folder
				IWorkbenchWizard wizard = new BasicNewProjectResourceWizard();
				wizard.init(PlatformUI.getWorkbench(), new TreeSelection());
				WizardDialog dialog = new WizardDialog(getShell(), wizard);
				dialog.open();
				if (outputChooserTreeViewer.getTree().getItemCount() == 1) {					
					validatePage();
				}
			}
			
		});
	}	

	public void setupPageFromFromNewPIWizardSettings() {
		outputChooserTreeViewer.getTree().deselectAll();
		if (NewPIWizardSettings.getInstance().outputContainer == null || 
				NewPIWizardSettings.getInstance().outputContainer.exists() == false ||
				outputChooserTreeViewer.getSelection() == null ||
				outputChooserTreeViewer.getSelection().isEmpty()) {
			// stupid eclipse guideline for default container when we found out how to do it
			NewPIWizardSettings.getInstance().outputContainer = findDefaultContainer();
		}
		if (NewPIWizardSettings.getInstance().outputContainer == null ) {
			// select it if it's the only one output project
			if (outputChooserTreeViewer.getTree().getItemCount() == 1) {
				outputChooserTreeViewer.setSelection(new StructuredSelection(outputChooserTreeViewer.getTree().getItem(0)));
			}
		} else {
			// highlight all items select by expanding to that level
			// restoring from file system persist data could use this
			outputChooserTreeViewer.expandToLevel(NewPIWizardSettings.getInstance().outputContainer, 0);
			outputChooserTreeViewer.setSelection(new StructuredSelection(NewPIWizardSettings.getInstance().outputContainer));
		}
		// setup using last session persisted. If user changed input file, 
		// regenerateOutputIfIntputChanged() will derive the new output name base on input 
		// upon finish, or output page become visible
		validatePage();
	}

	private IContainer findDefaultContainer() {
		IContainer result = null;
		ISelection selection = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getSelectionService().getSelection();
		if (selection instanceof IStructuredSelection) {
			IStructuredSelection structuredSelection = (IStructuredSelection) selection;
			Object firstElement = structuredSelection.getFirstElement();
			if (firstElement instanceof IFile) {
				result = ((IFile)firstElement).getParent();
			} else if (firstElement instanceof IContainer) {
				result = (IContainer) firstElement;
			}
		}
		if (result == null) {
			try {
				IResource[] resources = ResourcesPlugin.getWorkspace().getRoot().members();
				for (IResource resource : resources) {
					if (resource instanceof IContainer) {
						result = (IContainer) resource;
						break;
					}
				}
			} catch (CoreException e) {
				e.printStackTrace();
			}
		}
		return result;
	}
	
	public void setVisible(boolean visable) {
		super.setVisible(visable);
		if (visable) {
			// see if we input changed
			validatePage();
		}
	}

}
