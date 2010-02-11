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
import org.eclipse.core.runtime.Path;
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
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchWizard;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.model.WorkbenchContentProvider;
import org.eclipse.ui.model.WorkbenchLabelProvider;
import org.eclipse.ui.wizards.newresource.BasicNewProjectResourceWizard;

import com.nokia.carbide.cpp.internal.pi.wizards.ui.util.CarbidePiWizardHelpIds;

public class NewPIWizardPageOutputTask extends NewPIWizardPage implements INewPIWizardSettings {
	
	private final static String DOT_NPI= ".npi"; //$NON-NLS-1$
	private final static String EMPTY_STRING = ""; //$NON-NLS-1$

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
	private Label fileLabel = null;
	private Text outputText = null;
	
	protected NewPIWizardPageOutputTask() {
		super(""); //$NON-NLS-1$
		setTitle(Messages.getString("NewPIWizardPageOutputTask.output.file.and.project")); //$NON-NLS-1$
	    setDescription(Messages.getString("NewPIWizardPageOutputTask.select.folder.or.create.new.project.as.output.file.container")); //$NON-NLS-1$
	}

	public void validatePage() {
		if (outputText.getText().length() < 1 || outputChooserTreeViewer.getTree().getSelection().length < 1) {
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
		fileLabel = new Label (composite, SWT.NONE);
		fileLabel.setText(Messages.getString("NewPIWizardPageOutputTask.output.file.name")); //$NON-NLS-1$
		outputText = new Text(composite, SWT.BORDER);
		outputText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		outputText.addSelectionListener(new SelectionListener () {
			public void widgetDefaultSelected(SelectionEvent arg0) {
			}

			public void widgetSelected(SelectionEvent arg0) {
				validatePage();
			}	
		});
		outputText.addModifyListener(new ModifyListener() {

			public void modifyText(ModifyEvent arg0) {
				NewPIWizardSettings.getInstance().piFileName = outputText.getText();		
				NewPIWizardSettings.getInstance().piFileNameModifiedNanoTime = System.nanoTime();
			}
			
		});
		
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
				outputText.setText(generateNpiFileName(outputText.getText()));
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
					outputText.setText(generateNpiFileName(outputText.getText()));
					validatePage();
				}
			}
			
		});
	}
	
	private boolean isPositiveLong(String x) {
		try {
			if (Long.parseLong(x) >= 0) {
				return true;
			}
		} catch (NumberFormatException e) {
			return false;
		}
		return false;
	}
	
	private String generateNpiFileName(String initialFilename) {
		// First look for the container
		IContainer container = NewPIWizardSettings.getInstance().outputContainer;
		
		TreeSelection selection = (TreeSelection) outputChooserTreeViewer.getSelection();
		if (selection == null)
			return EMPTY_STRING;
		if (selection.getFirstElement() == null)
			return EMPTY_STRING;
		if ((selection.getFirstElement() instanceof IContainer) == false)
			return EMPTY_STRING;
		container = (IContainer) selection.getFirstElement();				
		try {
			// in case user dump a file of the same name without telling eclipse
			container.refreshLocal(1, null);
		} catch (CoreException e) {
			// this is likely harmless
			e.printStackTrace();
		}

		// Then see if we find a matching file name
		initialFilename.trim();
		if (initialFilename.length()==0) {
			// generate from sample file if input file is empty
			initialFilename = NewPIWizardSettings.getInstance().sampleFileName;
		}
		// get just the file name(last part)
		initialFilename = new java.io.File(initialFilename).getName();
		
		String baseName;
		Long suffixNumber = new Long(0);
		
		int dot = initialFilename.lastIndexOf("."); //$NON-NLS-1$
		if (dot > 1) {
			baseName = initialFilename.substring(0, dot); //$NON-NLS-1$
		} else {
			baseName = initialFilename;
		}
		
		if (initialFilename.endsWith(".npi")) {	//$NON-NLS-1$
			// the input is a .npi doesn't exist in container, user should have manually typed it
			if (container.getFile(new Path(initialFilename)).exists() == false) {
				return initialFilename;
			}
			
			// this is probably an ***_<number>.npi we need to increament
			// just suffix _<number> if the name was derived from input sample name
			if (baseName.lastIndexOf("_") > 1 && 	//$NON-NLS-1$
				isPositiveLong(baseName.substring(baseName.lastIndexOf("_") + 1)))	//$NON-NLS-1$
			{
				suffixNumber = Long.parseLong(baseName.substring(baseName.lastIndexOf("_") + 1)); //$NON-NLS-1$
				baseName = baseName.substring(0, baseName.lastIndexOf("_"));	//$NON-NLS-1$
			}
		}
				
		// check existing npi and bump number
		while (container.getFile(new Path(baseName + "_" + suffixNumber.toString() + DOT_NPI)).exists()) { //$NON-NLS-1$
			suffixNumber++;
		}

		return baseName + "_" + suffixNumber.toString() + DOT_NPI; //$NON-NLS-1$
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
		outputText.setText(generateNpiFileName(NewPIWizardSettings.getInstance().piFileName));
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

	public void regenerateOutputIfIntputChanged() {
		// if both time stamps are set and input sample is later than NPI, regenerate it
		if (NewPIWizardSettings.getInstance().sampleFileNameModifiedNanoTime != 0 &&
			NewPIWizardSettings.getInstance().piFileNameModifiedNanoTime != 0 &&
			NewPIWizardSettings.getInstance().sampleFileNameModifiedNanoTime - NewPIWizardSettings.getInstance().piFileNameModifiedNanoTime > 0) {
			outputText.setText(generateNpiFileName(NewPIWizardSettings.getInstance().sampleFileName));
		}
	}
	
	public void setVisible(boolean visable) {
		super.setVisible(visable);
		if (visable) {
			// see if we input changed
			regenerateOutputIfIntputChanged();
			validatePage();
		}
	}

}
