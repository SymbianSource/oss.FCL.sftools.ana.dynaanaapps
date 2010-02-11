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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWizard;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;

import com.nokia.carbide.cpp.internal.pi.interfaces.ISaveTable;

public class SaveTableWizard extends Wizard implements INewWizard {

	/**
	 * This is a sample new wizard. Its role is to create a new file 
	 * resource in the provided container. If the container resource
	 * (a folder or a project) is selected in the workspace 
	 * when the wizard is opened, it will accept it as the target
	 * container. The wizard creates one file with the extension
	 * "mpe". If a sample multi-page editor (also available
	 * as a template) is registered for the same extension, it will
	 * be able to open it.
	 */

	private SaveTableWizardPage page;
	private ISelection selection;
	private ISaveTable saveTable;
	private String saveTableFileContents;

	/**
	 * Constructor for SampleNewWizard.
	 */
	public SaveTableWizard(ISaveTable saveTable) {
		super();
		this.saveTable = saveTable;
		this.setWindowTitle(Messages.getString("SaveTableWizard.SavingTableData")); //$NON-NLS-1$
		setNeedsProgressMonitor(true);
	}
	
	/**
	 * Adding the page to the wizard.
	 */

	public void addPages() {
		page = new SaveTableWizardPage(selection);
		addPage(page);
	}

	/**
	 * This method is called when 'Finish' button is pressed in
	 * the wizard. We will create an operation and run it
	 * using wizard as execution context.
	 */
	public boolean performFinish() {
		final IPath containerName = page.getContainerName();
		final String fileName = page.getFileName();
		IRunnableWithProgress op = new IRunnableWithProgress() {
			public void run(IProgressMonitor monitor) throws InvocationTargetException {
				try {
					doFinish(containerName, fileName, monitor);
				} catch (CoreException e) {
					throw new InvocationTargetException(e);
				} finally {
					monitor.done();
				}
			}
		};
		try {
			getContainer().run(true, false, op);
		} catch (InterruptedException e) {
			return false;
		} catch (InvocationTargetException e) {
			Throwable realException = e.getTargetException();
			MessageDialog.openError(getShell(), Messages.getString("SaveTableWizard.Error"), realException.getMessage()); //$NON-NLS-1$
			return false;
		}
		return true;
	}
	
	/**
	 * The worker method. It will find the container, create the
	 * file if missing or just replace its contents, and open
	 * the editor on the newly created file.
	 */

	private void doFinish(
		IPath containerName,
		String fileName,
		IProgressMonitor monitor)
		throws CoreException {
		// create a sample file
		monitor.beginTask(Messages.getString("SaveTableWizard.Creating") + fileName, 3); //$NON-NLS-1$
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		IResource resource = root.findMember(containerName);
		if (!resource.exists() || !(resource instanceof IContainer)) {
			throwCoreException(Messages.getString("SaveTableWizard.ProjectFolder") + containerName + Messages.getString("SaveTableWizard.doesNotExist"));  //$NON-NLS-1$//$NON-NLS-2$
		}
		monitor.worked(1);
		monitor.setTaskName(Messages.getString("SaveTableWizard.WritingTableData")); //$NON-NLS-1$
		IContainer container = (IContainer) resource;
		final IFile file = container.getFile(new Path(fileName));
		try {
			InputStream stream = openContentStream();
			if (file.exists()) {
				file.setContents(stream, true, true, monitor);
			} else {
				file.create(stream, true, monitor);
			}
			stream.close();
		} catch (IOException e) {
		}
		monitor.worked(1);
		monitor.setTaskName(Messages.getString("SaveTableWizard.OpeningFileForEditing")); //$NON-NLS-1$
		getShell().getDisplay().asyncExec(new Runnable() {
			public void run() {
				IWorkbenchPage page =
					PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
				try {
					IDE.openEditor(page, file, true);
				} catch (PartInitException e) {
				}
			}
		});
		monitor.worked(1);
	}
	
	/**
	 * We will initialize file contents with a sample text.
	 */
	private InputStream openContentStream() {
		Display.getDefault().syncExec(new Runnable() {
			public void run() {
				saveTableFileContents = saveTable.getData();;
			}
		});
		return new ByteArrayInputStream(saveTableFileContents.getBytes());
	}

	private void throwCoreException(String message) throws CoreException {
		IStatus status =
			new Status(IStatus.ERROR, "junk", IStatus.OK, message, null); //$NON-NLS-1$
		throw new CoreException(status);
	}

	/**
	 * We will accept the selection in the workbench to see if
	 * we can initialize from it.
	 * @see IWorkbenchWizard#init(IWorkbench, IStructuredSelection)
	 */
	public void init(IWorkbench workbench, IStructuredSelection selection) {
		this.selection = selection;
	}
}
