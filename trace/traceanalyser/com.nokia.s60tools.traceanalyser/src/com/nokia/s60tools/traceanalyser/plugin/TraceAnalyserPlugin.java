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



package com.nokia.s60tools.traceanalyser.plugin;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.jobs.ILock;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import com.nokia.s60tools.traceanalyser.model.Engine;
import com.nokia.s60tools.traceanalyser.resources.ImageResourceManager;
import com.nokia.s60tools.traceanalyser.ui.views.MainView;


/**
 * The activator class controls the plug-in life cycle
 */
public class TraceAnalyserPlugin extends AbstractUIPlugin {

	/* Access lock */
	private static ILock accessLock = null;

	/* Install path of Plugin */
	private String pluginInstallPath = "";

	/* Main view of Trace Analyser */
	private static MainView mainView;
	
	/* Engine of Trace Analyser */
	private static Engine engine;
	
	// The plug-in ID
	public static final String PLUGIN_ID = "com.nokia.s60tools.traceanalyser";

	// The shared instance
	private static TraceAnalyserPlugin plugin;
	
	/**
	 * TraceAnalyserPlugin.
	 * The constructor
	 */
	public TraceAnalyserPlugin() {
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;

		String pluginInstallLocation = getPluginInstallPath();

		String imagesPath = getImagesPath(pluginInstallLocation);
		
		// Loading images required by this plug-in
		ImageResourceManager.loadImages(imagesPath);
		
		accessLock = Job.getJobManager().newLock();
		
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		engine.stop();
		plugin = null;
		super.stop(context);
	}

	/**
	 * getDefault.
	 * Returns the shared instance
	 * @return the shared instance
	 */
	public static TraceAnalyserPlugin getDefault() {
		return plugin;
	}

	/**
	 * getImageDescriptor.
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
	 * getPluginInstallPath
	 * @return the path where this plugin is installed
	 */
	public static String getPluginInstallPath() {
		try {
			if ( plugin.pluginInstallPath.equals("") ) { //$NON-NLS-1$
				 // URL to the plugin's root ("/")
				URL relativeURL = plugin.getBundle().getEntry("/"); //$NON-NLS-1$
				//	Converting into local path
				URL localURL = FileLocator.toFileURL(relativeURL);
				//	Getting install location in correct form
				File f = new File(localURL.getPath());
				plugin.pluginInstallPath = f.getAbsolutePath();
			}
			return plugin.pluginInstallPath;
		} catch (Exception e) {
			return ""; //$NON-NLS-1$
		}
	}
	
	/**
	 * getImagesPath.
	 * Gets images path relative to given plugin install path.
	 * @param pluginInstallPath Plugin installation path.
	 * @return Path were image resources are located.
	 * @throws IOException
	 */
	private String getImagesPath(String pluginInstallPath) throws IOException{
		return pluginInstallPath
				+ File.separatorChar + "icons"; //$NON-NLS-1$
	}
	
	/**
	 * startEngine.
	 * Starts Trace Analyser's engine.
	 */
	public static void startEngine(){
		accessLock.acquire();
		if(engine == null){
			engine = new Engine();
		}
		accessLock.release();
	}
	
	/**
	 * getEngine
	 * getter for engine. If engine is not yet started this method starts it.
	 * @return
	 */
	public static Engine getEngine(){
		startEngine();
		return engine;
	}

	/**
	 * getMainView.
	 * @return main view of Trace Analyser
	 */
	public static MainView getMainView() {
		return mainView;
	}

	/**
	 * setMainView.
	 * @param newMainView new main view.
	 */
	public static void setMainView(MainView newMainView) {
		mainView = newMainView;
		if(engine != null){
			engine.setMainView(mainView);
		}
	}


	/**
	 * resets engine
	 */
	public static Engine restartEngine(){
		engine = new Engine();
		return engine;
	}
	
}
