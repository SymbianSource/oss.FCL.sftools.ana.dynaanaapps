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
package com.nokia.s60tools.swmtanalyser;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import com.nokia.s60tools.swmtanalyser.analysers.IAnalyser;
import com.nokia.s60tools.swmtanalyser.analysers.LinearAnalyser;
import com.nokia.s60tools.swmtanalyser.analysers.ThreadDataAnalyser;
import com.nokia.s60tools.util.console.IConsolePrintUtility;

/**
 * The activator class controls the plug-in life cycle
 */
public class SwmtAnalyserPlugin extends AbstractUIPlugin {

	ArrayList<IAnalyser> Analysers = new ArrayList<IAnalyser>();
	
	// The plug-in ID
	public static final String PLUGIN_ID = "com.nokia.s60tools.swmtanalyser";

	// The shared instance
	private static SwmtAnalyserPlugin plugin;
	
	private String pluginInstallPath = "";

	/**
	 * Console for plug-in
	 */
	private IConsolePrintUtility console;
	
	/**
	 * The constructor
	 */
	public SwmtAnalyserPlugin() {
		plugin = this;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		registerAnalyser(new LinearAnalyser());
		registerAnalyser(new ThreadDataAnalyser());
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
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
	public static SwmtAnalyserPlugin getDefault() {
		return plugin;
	}
	
	/**
	 * Returns an image descriptor for the image file at the given
	 * plug-in relative path
	 *
	 * @param path the path
	 * @return the image descriptor
	 */
	public static ImageDescriptor getImageDescriptor(String path) {
		return imageDescriptorFromPlugin(PLUGIN_ID, path);
	}

	/**
	 * @param analyser will be registered for analysis. The tool invokes analysis
	 * on all registered analysers.
	 */
	public void registerAnalyser(IAnalyser analyser)
	{
		if(!Analysers.contains(analyser))
			Analysers.add(analyser);
	}
	
	public IAnalyser [] getRegisteredAnalysers()
	{
		return Analysers.toArray(new IAnalyser[0]);
	}
	
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

	/**
	 * Set the IConsolePrintUtility for plug-in
	 * @param console
	 */
	public void setConsole(IConsolePrintUtility console) {
		this.console = console;
	}
	
	/**
	 * Get the console print utility
	 * @return console
	 */
	public static IConsolePrintUtility getConsole(){
		return getDefault().getConsolePrintUtility();		
	}

	/**
	 * Get the console print utility
	 * @return console
	 */
	private IConsolePrintUtility getConsolePrintUtility(){
		return console;
	}
	
}
