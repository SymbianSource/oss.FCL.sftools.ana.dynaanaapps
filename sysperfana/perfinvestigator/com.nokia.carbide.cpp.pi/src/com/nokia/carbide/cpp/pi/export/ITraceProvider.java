/*
 * Copyright (c) 2010 Nokia Corporation and/or its subsidiary(-ies). 
 * All rights reserved.
 * This component and the accompanying materials are made available
 * under the terms of "Eclipse Public License v1.0"
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
package com.nokia.carbide.cpp.pi.export;

import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import com.nokia.carbide.cpp.internal.pi.plugin.model.ITrace;

/**
 * Interface for binding external trace data providers for Performance
 * Investigator. Only a single trace data provider instance at a time is
 * supported. In case more that a single trace data providers are available the
 * selection among them is undeterministic.
 */
public interface ITraceProvider {

	public final static String DAT_FILE = "dat"; //$NON-NLS-1$
	public final static String BASE_FILE = "base64"; //$NON-NLS-1$
	public final static String BASE_CORRUPTED_FILE = "corrupted_base64"; //$NON-NLS-1$


	/**
	 * Get available list of the plug-ins from the device
	 * 
	 * @param traceClient instance of the ITraceClientNotificationsIf
	 * @param monitor
	 *            instance of the {@link IProgressMonitor}
	 * @return list of the available plug-ins
	 * @throws CoreException
	 */
	public List<ITrace> getAvailableSamplers(ITraceClientNotificationsIf traceClient, IProgressMonitor monitor)
			throws CoreException;


	/**
	 * Gets preference page ID for the user trace source.
	 * 
	 * @return preference page ID for the user trace source
	 */
	public String getTraceSourcePreferencePageId();

	/**
	 * Gets current connections display name.
	 * 
	 * @return current connections display name
	 */
	public String getDisplayNameForCurrentConnection(IProgressMonitor monitor)
			throws CoreException;
	
	/**
	 * Start to trace data from device
	 * 
	 * @param filePrefix profiler data file's prefix
	 * @param traceIds plug-in IDs to trace
	 * @param traceClient instance of the ITraceClientNotificationsIf
	 * @param monitor instance of the {@link IProgressMonitor}
	 * @throws CoreException
	 */
	public void startTrace(String filePrefix, int[] traceIds,
			ITraceClientNotificationsIf traceClient, IProgressMonitor monitor)
			throws CoreException;

	/**
	 * Stop the trace
	 * 
	 * @param forceStop do force stop
	 * @return location where the trace data file is located
	 * @throws CoreException
	 */
	public IPath stopTrace(boolean forceStop) throws CoreException;


	/**
	 * Check is listener activated
	 * 
	 * @return the listener activation status
	 */
	public boolean isListening();
}
