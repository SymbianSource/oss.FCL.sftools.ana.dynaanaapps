/*
 * Copyright (c) 2010 Nokia Corporation and/or its subsidiary(-ies). 
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

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.wizard.IWizardContainer;
import org.eclipse.swt.widgets.Display;

import com.nokia.carbide.cpp.internal.pi.plugin.model.ITrace;
import com.nokia.carbide.cpp.pi.PiPlugin;
import com.nokia.carbide.cpp.pi.export.ITraceClientNotificationsIf;
import com.nokia.carbide.cpp.pi.wizards.WizardsPlugin;

/**
 * Helper class to handle traces
 */
public class TraceHandler implements ITraceClientNotificationsIf {

	private NewPIWizardPage wizardPage;
	private IWizardContainer wizardContainer;
	private ProfilerActivatorGroup profilerActivatorGroup;

	/**
	 * Constructor
	 * 
	 * @param profilerActivatorGroup
	 *            instance of the ProfilerActivatorGroup
	 */
	public TraceHandler(ProfilerActivatorGroup profilerActivatorGroup) {
		this.profilerActivatorGroup = profilerActivatorGroup;
		this.wizardPage = profilerActivatorGroup.getWizardPage();
		this.wizardContainer = profilerActivatorGroup.getWizardContainer();

	}

	/**
	 * Update current connection
	 * 
	 */
	public void updateCurrenConnection() {
		try {
			IRunnableWithProgress runnableWithProgress = new IRunnableWithProgress() {
				public void run(IProgressMonitor monitor)
						throws InvocationTargetException, InterruptedException {
					monitor.beginTask(Messages.getString("TraceHandler.resolvingCurrentConnection"), //$NON-NLS-1$
							IProgressMonitor.UNKNOWN);
					try {
						String currenConnection = PiPlugin.getTraceProvider()
								.getDisplayNameForCurrentConnection(monitor);
						profilerActivatorGroup
								.setCurrentConnection(currenConnection);
					} catch (CoreException e) {
						throw new InvocationTargetException(e);
					} finally {
						monitor.done();
					}
				}
			};
			wizardContainer.run(true, true, runnableWithProgress);
		} catch (InvocationTargetException e) {
			if(e.getCause() instanceof CoreException){
				updateStatus(((CoreException)e.getCause()).getStatus());
			}else{
				notifyError(e.getMessage());
			}
		} catch (InterruptedException e) {
			notifyError(e.getMessage());
		}
	}

	/**
	 * Fetch available plug-ins list
	 * 
	 */
	public List<ITrace> fetchAvailablePlugins() {
		final List<ITrace> plugins = new ArrayList<ITrace>();
		try {		
			IRunnableWithProgress runnableWithProgress = new IRunnableWithProgress() {
				public void run(IProgressMonitor monitor)
						throws InvocationTargetException, InterruptedException{
					monitor.beginTask(Messages.getString("TraceHandler.fetchingPluginsList"), //$NON-NLS-1$
							IProgressMonitor.UNKNOWN);
					try {				
						plugins.addAll(PiPlugin.getTraceProvider()
								.getAvailableSamplers(TraceHandler.this, monitor));				
					} catch (CoreException e) {
						throw new InvocationTargetException(e);					
					} finally {
						monitor.done();
					}

				}
			};
			wizardContainer.run(true, false, runnableWithProgress);		
			if(wizardPage instanceof INewPIWizardSettings){
				((INewPIWizardSettings)wizardPage).validatePage();
			}
			
			return plugins;
		}catch (InvocationTargetException e) {
			if(e.getCause() instanceof CoreException){
				updateStatus(((CoreException)e.getCause()).getStatus());
			}else{
				notifyError(e.getMessage());	
			}			
		} catch (InterruptedException e) {
			notifyError(e.getMessage());
		}
		return null;
	}

	/**
	 * Start trace
	 * 
	 * @param fileName
	 *            for new profiler data file
	 * @param traceIDs
	 *            selected plug-ins to be profiling
	 */
	public void startTrace(final String fileName, final int[] traceIDs) {

		try {
			IRunnableWithProgress runnableWithProgress = new IRunnableWithProgress() {
				public void run(IProgressMonitor monitor)
						throws InvocationTargetException, InterruptedException {
					monitor.beginTask(Messages.getString("TraceHandler.preparingTracing"), //$NON-NLS-1$
							IProgressMonitor.UNKNOWN);			
					try {	
						
						PiPlugin.getTraceProvider().startTrace(
								fileName,
								traceIDs,
								TraceHandler.this,
								new SubProgressMonitor(monitor,
										IProgressMonitor.UNKNOWN));						
					} catch (CoreException e) {
						throw new InvocationTargetException(e);
					} finally {
						monitor.done();
					}

				}
			};
			wizardContainer.run(true, false, runnableWithProgress);

		} catch (InvocationTargetException e) {
			if(e.getCause() instanceof CoreException){
				updateStatus(((CoreException)e.getCause()).getStatus());
			}else{
				notifyError(e.getMessage());
			}
		} catch (InterruptedException e) {
			notifyError(e.getMessage());
		}
	}

	/**
	 * Stop trace
	 * 
	 * @return
	 */
	public IPath stopTrace() {
		final IPath[] path = new Path[1];
		try {
			IRunnableWithProgress runnableWithProgress = new IRunnableWithProgress() {
				public void run(IProgressMonitor monitor)
						throws InvocationTargetException, InterruptedException {
					monitor.beginTask(Messages.getString("TraceHandler.creatingProfilerDataFile"), //$NON-NLS-1$
							IProgressMonitor.UNKNOWN);
					try {					
						path[0] = PiPlugin.getTraceProvider().stopTrace(false);	
					} catch (CoreException e) {
						throw new InvocationTargetException(e);
					} finally {
						monitor.done();
					}
				}
			};
			wizardContainer.run(true, false, runnableWithProgress);
			return path[0];
		} catch (InvocationTargetException e) {
			if(e.getCause() instanceof CoreException){
				updateStatus(((CoreException)e.getCause()).getStatus());
			}else{
				notifyError(e.getMessage());
			}
			
		} catch (InterruptedException e) {
			notifyError(e.getMessage());
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.nokia.carbide.cpp.pi.export.ITraceClientNotificationsIf#notifyError
	 * (java.lang.String)
	 */
	public void notifyError(String message) {
		updateStatus(createStatus(IStatus.ERROR, message));

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.nokia.carbide.cpp.pi.export.ITraceClientNotificationsIf#notifyInformation
	 * (java.lang.String)
	 */
	public void notifyInformation(String message) {
		updateStatus(createStatus(IStatus.INFO, message));

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.nokia.carbide.cpp.pi.export.ITraceClientNotificationsIf#notifyWarning
	 * (java.lang.String)
	 */
	public void notifyWarning(String message) {
		updateStatus(createStatus(IStatus.WARNING, message));

	}

	/**
	 * Update status for the wizard
	 * 
	 * @param status
	 */
	private void updateStatus(final IStatus status) {	
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {				
				wizardPage.setErrorMessage(null);		
				profilerActivatorGroup.updateButtons();
				if (status.getSeverity() == IStatus.OK) {
					wizardPage.setMessage(status.getMessage());
					wizardPage.setPageComplete(true);
				} else if (status.getSeverity() == IStatus.ERROR) {					
					wizardPage.setErrorMessage(status.getMessage());
					wizardPage.setPageComplete(false);
				} else if (status.getSeverity() == IStatus.INFO) {
					wizardPage.setMessage(status.getMessage());
					wizardPage.setPageComplete(false);
				} else if (status.getSeverity() == IStatus.WARNING) {
					wizardPage.setMessage(status.getMessage());
					wizardPage.setPageComplete(true);
				}else if (status.getSeverity() == IStatus.CANCEL) {
					wizardPage.setMessage(status.getMessage());
					wizardPage.setPageComplete(false);
				}

			}
		});
	}

	/**
	 * Create a status
	 * 
	 * @param severity
	 *            of the status
	 * @param message
	 *            of the status
	 * @return created status
	 */
	private IStatus createStatus(int severity, String message) {
		return new Status(severity, WizardsPlugin.PLUGIN_ID, message);
	}

	/*
	 * (non-Javadoc)
	 * @see com.nokia.carbide.cpp.pi.export.ITraceClientNotificationsIf#updateTraceDataFile(org.eclipse.core.runtime.IPath, long, long)
	 */
	public void updateTraceDataFile(IPath path, long time, long size) {
		profilerActivatorGroup.updateTraceDataFile(path, time, size);
		
	}
}
