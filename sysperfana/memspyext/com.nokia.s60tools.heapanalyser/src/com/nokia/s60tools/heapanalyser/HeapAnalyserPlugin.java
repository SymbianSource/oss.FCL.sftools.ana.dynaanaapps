/*
* Copyright (c) 2009 Nokia Corporation and/or its subsidiary(-ies).
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



package com.nokia.s60tools.heapanalyser;

import java.io.File;
import java.net.URL;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Plugin;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class HeapAnalyserPlugin extends Plugin {

	/**
	 * The plug-in ID for Heap Analyser.
	 */
	public static final String PLUGIN_ID = "com.nokia.s60tools.heapanalyser";

	private String pluginInstallPath = "";

	// The shared instance
	private static HeapAnalyserPlugin plugin;
	
	/**
	 * The constructor
	 */
	public HeapAnalyserPlugin() {
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.core.runtime.Plugins#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.core.runtime.Plugin#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static HeapAnalyserPlugin getDefault() {
		return plugin;
	}

	/**
	 * Returns the path where this plugin is installed
	 * @return the path where this plugin is installed
	 */
	public static String getPluginInstallPath() {
		try {
			if ("".equals(plugin.pluginInstallPath)) {
				 // URL to the plugin's root ("/")
				URL relativeURL = plugin.getBundle().getEntry("/"); 
				//	Converting into local path
				URL localURL = FileLocator.toFileURL(relativeURL);
				//	Getting install location in correct form
				File f = new File(localURL.getPath());
				plugin.pluginInstallPath = f.getAbsolutePath();
			}
			return plugin.pluginInstallPath;
		} catch (Exception e) {
			return "";
		}
	}	
	
}
