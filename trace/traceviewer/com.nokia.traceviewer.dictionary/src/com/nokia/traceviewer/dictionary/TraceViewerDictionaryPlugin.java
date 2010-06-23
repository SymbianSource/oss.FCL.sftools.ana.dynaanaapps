/*
 * Copyright (c) 2007-2010 Nokia Corporation and/or its subsidiary(-ies). 
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
 * TraceViewer Dictionary Plugin
 *
 */
package com.nokia.traceviewer.dictionary;

import org.eclipse.core.runtime.Plugin;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 * 
 */
public class TraceViewerDictionaryPlugin extends Plugin {

	/**
	 * The plug-in ID
	 */
	public static final String PLUGIN_ID = "com.nokia.traceviewer.dictionary"; //$NON-NLS-1$

	/**
	 * The shared instance
	 */
	private static TraceViewerDictionaryPlugin plugin;

	/**
	 * The constructor
	 */
	public TraceViewerDictionaryPlugin() {
		plugin = this;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.core.runtime.Plugins#start(org.osgi.framework.BundleContext)
	 */
	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.core.runtime.Plugin#stop(org.osgi.framework.BundleContext)
	 */
	@Override
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 * 
	 * @return the shared instance
	 */
	public static TraceViewerDictionaryPlugin getDefault() {
		return plugin;
	}

}
