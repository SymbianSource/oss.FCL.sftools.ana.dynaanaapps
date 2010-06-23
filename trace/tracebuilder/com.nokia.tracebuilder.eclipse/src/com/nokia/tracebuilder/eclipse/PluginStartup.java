/*
* Copyright (c) 2007 Nokia Corporation and/or its subsidiary(-ies). 
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
* The plugin startup class
*
*/
package com.nokia.tracebuilder.eclipse;

import org.eclipse.ui.IStartup;
import org.eclipse.ui.PlatformUI;

import com.nokia.tracebuilder.engine.TraceBuilderGlobals;
import com.nokia.tracebuilder.utils.ConsoleFactory;
import com.nokia.tracebuilder.utils.DocumentFactory;

/**
 * The plugin startup class
 * 
 */
public class PluginStartup implements IStartup {

	/**
	 * Trace project monitor
	 */
	private TraceProjectMonitor projectMonitor;

	/**
	 * Runnable which initializes the project opener
	 */
	private final class ProjectOpenerInitRunnable implements Runnable {

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Runnable#run()
		 */
		public void run() {
			if (TraceBuilderGlobals.isViewRegistered()) {
				projectMonitor.startMonitor();
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IStartup#earlyStartup()
	 */
	public void earlyStartup() {
		projectMonitor = new TraceProjectMonitor();
		TraceBuilderGlobals.setProjectMonitor(projectMonitor);
		DocumentFactory.registerDocumentFramework(new WorkbenchEditorMonitor(
				projectMonitor), JFaceDocumentFactory.class);
		ConsoleFactory.registerConsole(EclipseConsole.class);
		PlatformUI.getWorkbench().getDisplay().asyncExec(
				new ProjectOpenerInitRunnable());
	}
}
