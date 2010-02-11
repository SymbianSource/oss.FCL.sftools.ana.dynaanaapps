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

/**
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
import org.eclipse.core.runtime.NullProgressMonitor;
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

import com.nokia.carbide.cpp.internal.pi.interfaces.ISaveSamples;


public class SaveSamplesWizard extends Wizard implements INewWizard {

	private SaveSamplesPage page;
	private ISelection selection;
	private ISaveSamples saveSamples;
	private String saveSamplesFileContents;
	
	private   boolean writeInProgress = false;
	protected boolean canceled        = false;
	private IFile file;

	/**
	 * Constructor for SampleNewWizard.
	 */
	public SaveSamplesWizard(ISaveSamples saveSamples) {
		super();
		this.saveSamples = saveSamples;
		this.setWindowTitle(Messages.getString("SaveSamplesWizard.SavingSamples")); //$NON-NLS-1$
		setNeedsProgressMonitor(true);
	}
	
	/**
	 * Adding the page to the wizard.
	 */
	public void addPages() {
		this.page = new SaveSamplesPage(this.selection);
		addPage(this.page);
	}

	/**
	 * This method is called when 'Finish' button is pressed in
	 * the wizard. We will create an operation and run it
	 * using the wizard as execution context.
	 */
	public boolean performFinish() {
		final IPath containerName = page.getContainerName();
		String fileName = page.getFileName();

		int dotLoc = fileName.lastIndexOf('.');
		if (dotLoc == -1)
		{
			fileName += ".csv"; //$NON-NLS-1$
		}
		
		final String fileNameFinal = fileName;

		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		final IResource resource = root.findMember(containerName);

		this.saveSamples.clear();
		this.canceled = false;
		this.file = null;

		IRunnableWithProgress op = new IRunnableWithProgress() {
			public void run(IProgressMonitor monitor)
						throws InvocationTargetException, InterruptedException {
				monitor.worked(1);
				try {
					// find the file path in the workspace
					if (!resource.exists() || !(resource instanceof IContainer)) {
						throwCoreException(Messages.getString("SaveSamplesWizard.ProjectFolder") + containerName + Messages.getString("SaveSamplesWizard.doesNotExist"));  //$NON-NLS-1$//$NON-NLS-2$
					}
					
					IContainer container = (IContainer) resource;
					file = container.getFile(new Path(fileNameFinal));

					// save samples, several at a time, to the output file
					writeInProgress = true;
					while (writeInProgress) {
						if (monitor.isCanceled()) {
							canceled = true;
							return;
						}
						writeOneSampleSet(monitor);
					}
					if (monitor.isCanceled()) {
						canceled = true;
						return;
					}

					// open the output file for editing
					monitor.setTaskName(Messages.getString("SaveSamplesWizard.OpeningFileForEditing")); //$NON-NLS-1$
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
				} catch (CoreException e) {
					throw new InvocationTargetException(e);
				}
				if (monitor.isCanceled()) {
					canceled = true;
					return;
				}
			}
		};
		try {
			getContainer().run(true, true, op);
		} catch (InterruptedException e) {
			canceled = true;
		} catch (InvocationTargetException e) {
			Throwable realException = e.getTargetException();
			MessageDialog.openError(getShell(), Messages.getString("SaveSamplesWizard.Error"), realException.getLocalizedMessage()); //$NON-NLS-1$
		}
		
		if (canceled && (file != null)) {
			try {
				file.delete(true, false, new NullProgressMonitor());
			} catch (CoreException coreEx) {
				MessageDialog.openError(getShell(), Messages.getString("SaveSamplesWizard.Error"), coreEx.getLocalizedMessage()); //$NON-NLS-1$
			}
		}

		return true;
	}
		
	private void writeOneSampleSet(IProgressMonitor monitor) throws CoreException {

		InputStream stream;
		
		try {
			if (saveSamples.getIndex() == 0) {
				monitor.setTaskName(Messages.getString("SaveSamplesWizard.WritingSamples")); //$NON-NLS-1$
				stream = openContentStream();
				if (file.exists()) {
					file.setContents(stream, true, true, monitor);
				} else {
					file.create(stream, true, monitor);
				}
				stream.close();
			} else { 
				stream = openContentStream();
				if (stream == null) {
					writeInProgress = false;
					return;
				}

				String taskName = Messages.getString("SaveSamplesWizard.WritingSamples"); //$NON-NLS-1$
				if (saveSamples.getIndex() > 0)
					taskName += Messages.getString("SaveSamplesWizard.alreadyWrtten1") + saveSamples.getIndex() + Messages.getString("SaveSamplesWizard.alreadyWrtten2"); //$NON-NLS-1$ //$NON-NLS-2$
				monitor.setTaskName(taskName);
				file.appendContents(stream, true, false, monitor);
				stream.close();
			}
		} catch (IOException e) {
		}
	}
	
	/**
	 * We will initialize file contents with a sample text.
	 */
	private InputStream openContentStream() {
		Display.getDefault().syncExec(new Runnable() {
			public void run() {
				saveSamplesFileContents = saveSamples.getData();
			}
		});
		if (saveSamplesFileContents == null)
			return null;
		
		return new ByteArrayInputStream(saveSamplesFileContents.getBytes());
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
