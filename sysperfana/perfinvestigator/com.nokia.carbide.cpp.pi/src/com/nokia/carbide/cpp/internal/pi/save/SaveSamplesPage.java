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

package com.nokia.carbide.cpp.internal.pi.save;

import java.util.ArrayList;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.dialogs.IDialogPage;
import org.eclipse.jface.viewers.DecoratingLabelProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.jface.wizard.WizardPage;
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

import com.nokia.carbide.cpp.pi.ComNokiaCarbidePiHelpIDs;

/**
 * The SaveSamplesPage wizard page allows setting the containing workspace folder
 * for the saved sample data as well as the file name. The page will only
 * accept a file name without the extension OR with the extension .csv.
 */

public class SaveSamplesPage extends WizardPage {

	// Provides all folders in the workspace
	private class ProjectContentProvider
	extends WorkbenchContentProvider
	implements ITreeContentProvider
	{
		// local version just to prevent getChildren/hadChild getting stuck
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

//	private Text containerText;

	private Text fileText;

	private ISelection selection;
	
	private TreeViewer outputChooserTreeViewer;

	/**
	 * Constructor for SampleNewWizardPage.
	 * 
	 * @param pageName
	 */
	public SaveSamplesPage(ISelection selection) {
		super("wizardPage"); //$NON-NLS-1$
		setTitle(Messages.getString("SaveSamplesPage.CVSFileForData")); //$NON-NLS-1$
		setDescription(Messages.getString("SaveSamplesPage.SpecifyFileWithCSVExtension")); //$NON-NLS-1$
		this.selection = selection;
	}

	/**
	 * @see IDialogPage#createControl(Composite)
	 */
	public void createControl(Composite parent) {
		Composite container = new Composite(parent, SWT.NULL);
		GridLayout layout = new GridLayout();
		container.setLayout(layout);
		layout.numColumns = 1;

		PlatformUI.getWorkbench().getHelpSystem().setHelp(container, ComNokiaCarbidePiHelpIDs.PI_SAVE_SAMPLES_WIZARD_PAGE);
		createProjectComposite(container);
		GridData gd;

		Label fileLabel = new Label(container, SWT.NONE);
		fileLabel.setText(Messages.getString("SaveSamplesPage.OutputFileName")); //$NON-NLS-1$
		fileText = new Text(container, SWT.BORDER | SWT.SINGLE);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		fileText.setLayoutData(gd);
		fileText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				dialogChanged();
			}
		});

		initialize();
		dialogChanged();
		setControl(container);
	}

	/**
	 * Tests if the current workbench selection is a suitable container to use.
	 */

	private void initialize() {
		if (selection != null && selection.isEmpty() == false
				&& selection instanceof IStructuredSelection) {
			IStructuredSelection ssel = (IStructuredSelection) selection;
			if (ssel.size() > 1)
				return;
		}
		fileText.setText("new_file.csv"); //$NON-NLS-1$
	}

	/**
	 * Ensures that both text fields are set.
	 */

	private void dialogChanged() {
		String fileName = getFileName();

		if (outputChooserTreeViewer.getTree().getSelection().length < 1) {
			updateStatus(ERROR, Messages.getString("SaveSamplesPage.ProjectFolderRequired")); //$NON-NLS-1$
			return;
		}

		if (fileName.length() == 0) {
			updateStatus(ERROR, Messages.getString("SaveSamplesPage.FileNameRequired")); //$NON-NLS-1$
			return;
		}
		if (fileName.replace('\\', '/').indexOf('/', 1) > 0) {
			updateStatus(ERROR, Messages.getString("SaveSamplesPage.FileNameMustBeValid")); //$NON-NLS-1$
			return;
		}
		int dotLoc = fileName.lastIndexOf('.');
		if (dotLoc != -1) {
			String ext = fileName.substring(dotLoc + 1);
			if (ext.equalsIgnoreCase("csv") == false) { //$NON-NLS-1$
				updateStatus(ERROR, Messages.getString("SaveSamplesPage.FileExtensionMustBeCSV")); //$NON-NLS-1$
				return;
			}
		}
		
		TreeSelection selection = (TreeSelection) outputChooserTreeViewer.getSelection();
		if (   (selection != null)
			&& (selection.getFirstElement() != null)
			&& (selection.getFirstElement() instanceof IContainer)) {
			IContainer container = (IContainer) selection.getFirstElement();
			String file = fileName;
			
			if (dotLoc == -1)
			{
				file += ".csv"; //$NON-NLS-1$
			}
			if (container.getFile(new Path(file)).exists()) {
				updateStatus(WARNING, Messages.getString("SaveSamplesPage.WarningFileExists")); //$NON-NLS-1$
				return;
			}
		}

		updateStatus(WARNING, null);
	}

	private void updateStatus(int messageType, String message) {
		setMessage(message, messageType);
		setPageComplete(messageType != ERROR);
	}

	public IPath getContainerName() {
		
		TreeSelection selection = (TreeSelection) outputChooserTreeViewer.getSelection();
		if (   (selection != null)
			&& (selection.getFirstElement() != null)
			&& (selection.getFirstElement() instanceof IContainer)) {
			IContainer container = (IContainer) selection.getFirstElement();
			return container.getFullPath();
		}
		return null;
	}

	public String getFileName() {
		return fileText.getText();
	}
	
	/**
	 * This method initializes projectComposite	
	 *
	 */
	private void createProjectComposite(Composite container) {
		GridLayout gridLayout2 = new GridLayout();
		gridLayout2.numColumns = 2;
		Composite projectComposite = new Composite(container, SWT.NONE);
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
				dialogChanged();
			}
		});

		createButtonComposite(projectComposite);
	}
	
	private void createButtonComposite(Composite projectComposite) {
		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 1;
		Composite buttonComposite = new Composite(projectComposite, SWT.NONE);
		buttonComposite.setLayout(gridLayout);
		buttonComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, true));
		Button createButton = new Button(buttonComposite, SWT.PUSH);
		createButton.setText(Messages.getString("SaveSamplesPage.CreateEmptyProject")); //$NON-NLS-1$
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
					dialogChanged();
				}
			}
		});
	}
}
