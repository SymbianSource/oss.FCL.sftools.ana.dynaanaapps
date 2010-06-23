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
* The main plugin class
*
*/
package com.nokia.tracebuilder;

import org.eclipse.core.runtime.Plugin;
import org.osgi.framework.BundleContext;

import com.nokia.tracebuilder.engine.TraceBuilderGlobals;

/**
 * The main plugin class
 * 
 */
public class TracebuilderPlugin extends Plugin {

	/**
	 * The shared instance.
	 */
	private static TracebuilderPlugin plugin;

	/**
	 * The constructor.
	 */
	public TracebuilderPlugin() {
		plugin = this;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
	 */
	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		TraceBuilderGlobals.start();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
	 */
	@Override
	public void stop(BundleContext context) throws Exception {
		TraceBuilderGlobals.shutdown();
		super.stop(context);
		plugin = null;
	}

	/**
	 * Returns the shared plug-in instance.
	 * 
	 * @return the plug-in instance
	 */
	public static TracebuilderPlugin getDefault() {
		return plugin;
	}
}
