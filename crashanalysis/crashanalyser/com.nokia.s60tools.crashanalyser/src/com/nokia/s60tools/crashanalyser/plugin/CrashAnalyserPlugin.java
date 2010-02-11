/*
* Copyright (c) 2008 Nokia Corporation and/or its subsidiary(-ies). 
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

package com.nokia.s60tools.crashanalyser.plugin;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import com.nokia.s60tools.crashanalyser.model.FileOperations;
import com.nokia.s60tools.crashanalyser.model.TraceListener;
import com.nokia.s60tools.crashanalyser.model.EmulatorListener;
import com.nokia.s60tools.crashanalyser.resources.*;
import com.nokia.s60tools.crashanalyser.ui.preferences.*;

/**
 * The activator class controls the plug-in life cycle
 */
public class CrashAnalyserPlugin extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "com.nokia.s60tools.crashanalyser"; //$NON-NLS-1$

	// The shared instance
	private static CrashAnalyserPlugin plugin;
	private static IPreferenceStore prefsStore;
	private static TraceListener traceListener = null;
	private static EmulatorListener emulatorListener = null;
	private String pluginInstallPath = ""; //$NON-NLS-1$
	
	protected static final String DATA_FOLDER = "data\\";	
	
	/**
	 * The constructor
	 */
	public CrashAnalyserPlugin() {
		plugin = this;
	}
	
	/**
	 * Starts/Stops emulator listener according to preferences
	 */
	public static void startEmulatorListener() {
		if (emulatorListener == null)
			emulatorListener = new EmulatorListener();
		
		if (CrashAnalyserPreferences.epocwindListenerOn()) {
			emulatorListener.start();
		} else {
			emulatorListener.stop();
		}
	}
	
	/**
	 * Starts/Stops trace listener according to preferences
	 */
	public static void startTraceListener() {
		if (traceListener == null)
			traceListener = new TraceListener();
		
		traceListener.setDecode(CrashAnalyserPreferences.showVisualizer());
		if (CrashAnalyserPreferences.traceListenerOn()) {
			traceListener.startListening();
		} else {
			traceListener.stopListening();
		}		
	}
	
	/**
	 * Returns the path where this plugin is installed
	 * @return the path where this plugin is installed
	 */
	public static String getPluginInstallPath() {
		try {
			if (plugin.pluginInstallPath.equals("")) { //$NON-NLS-1$
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
	 * Gets images path relative to given plugin install path.
	 * @param pluginInstallPath Plugin installation path.
	 * @return Path were image resources are located.
	 * @throws IOException
	 */
	private String getImagesPath(String pluginInstallPath) throws IOException{
		return pluginInstallPath
				+ File.separatorChar + "icons"; //$NON-NLS-1$
	}
	

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		
		String pluginInstallLocation = getPluginInstallPath();
		String imagesPath = getImagesPath(pluginInstallLocation);
		
		// Loading images required by this plug-in
		ImageResourceManager.loadImages(imagesPath);				
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		if (emulatorListener != null) {
			emulatorListener.stop();
		}
		plugin = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static CrashAnalyserPlugin getDefault() {
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
	 * Returns the PreferenceStore where plugin preferences are stored
	 * 
	 * @return the PreferenceStore where plugin preferences are stored
	 */
	public static IPreferenceStore getCrashAnalyserPrefsStore(){
		if (prefsStore == null){
			prefsStore = getDefault().getPreferenceStore();
		}
		
		return prefsStore;
	}
	
	/**
	 * Returns the location for data-folder which contains all 
	 * error & panic description xml files
	 * @return
	 */
	public static String getDataPath() {
		String path = FileOperations.addSlashToEnd(getPluginInstallPath());
		return path + DATA_FOLDER;
	}		
}
