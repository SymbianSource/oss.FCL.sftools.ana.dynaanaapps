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



package com.nokia.s60tools.memspy.plugin;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

import com.nokia.s60tools.memspy.export.ITraceClientNotificationsIf;
import com.nokia.s60tools.memspy.export.ITraceDataProcessor;
import com.nokia.s60tools.memspy.export.ITraceProvider;
import com.nokia.s60tools.memspy.resources.ImageResourceManager;
import com.nokia.s60tools.memspy.util.MemSpyConsole;
import com.nokia.s60tools.util.debug.DbgUtility;


/**
 * The activator class controls the plug-in life cycle.
 */
public class MemSpyPlugin extends AbstractUIPlugin {

	/**
	 * Plug-in ID for Launcher plug-in.
	 */
	public static final String MEMSPY_TRACE_PLUGIN_ID = "com.nokia.s60tools.memspy.trace";//$NON-NLS-1$
	/**
	 * Launcher plug-in binaries directory name.
	 */
	public static final String LAUNCHER_BINARIES_DIR_NAME = "Launcher.binaries";//$NON-NLS-1$
	
	/**
	 * Trace provider extension name.
	 */
	final String EXTENSION_TRACE_PROVIDER = "traceprovider"; //$NON-NLS-1$
	
	/**
	 * Plug-in ID constant.
	 */
	public static final String PLUGIN_ID = "com.nokia.s60tools.memspy";
	
	/**
	 * Member for storing plug-in install path.
	 */
	private String pluginInstallPath = "";
	
	/**
	 * Shared plug-in instance
	 */
	private static MemSpyPlugin plugin;
	
	/**
	 * Storing reference to possibly installed trace provider plug-in. 
	 */
	private static ITraceProvider traceProvider;
	
	/**
	 * Preferences store instance reference.
	 */
	private static IPreferenceStore prefsStore;
	
	/**
	 * SWMT category setting feature is enabled by default, but may be disabled in case device does not support it.
	 */
	boolean isSWMTCategorySettingFeatureEnabled = true;	

	/**
	 * The constructor
	 */
	public MemSpyPlugin() {
		plugin = this;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
		
		String pluginInstallLocation = getPluginInstallPath();
		String imagesPath = getImagesPath(pluginInstallLocation);
		
		// Loading images required by this plug-in
		ImageResourceManager.loadImages(imagesPath);
		
		// Getting installed trace provider plug-in if available
		traceProvider = findTraceProviderExtension();
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
	public static MemSpyPlugin getDefault() {
		return plugin;
	}

	/**
	 * Get plugin installation path
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
	 * This must be called from UI thread. If called
	 * from non-ui thread this returns <code>null</code>.
	 * @return Currently active workbench page.
	 */
	public static IWorkbenchPage getCurrentlyActivePage(){
		return getDefault().getWorkbench().getActiveWorkbenchWindow().getActivePage();
	}
	
	/**
	 * This must be called from UI thread. If called
	 * from non-UI thread this returns <code>null</code>.
	 * @return The shell of the currently active workbench window..
	 */
	public static Shell getCurrentlyActiveWbWindowShell(){
		IWorkbenchPage page = getCurrentlyActivePage();
		if(page != null){
			return page.getWorkbenchWindow().getShell();
		}
		return null;
	}
	
	/**
	 * Checks if SWMT category setting feature is enabled.
	 * @return <code>true</code> if enabled, otherwise <code>false</code>.
	 */
	public boolean isSWMTCategorySettingFeatureEnabled() {
		return isSWMTCategorySettingFeatureEnabled;
	}

	/**
	 * Sets if SWMT category setting feature is enabled.
	 * @param isSWMTCategorySettingFeatureEnabled <code>true</code> if enabled, otherwise <code>false</code>.
	 */
	public void setSWMTCategorySettingFeatureEnabled(
			boolean isSWMTCategorySettingFeatureEnabled) {
		this.isSWMTCategorySettingFeatureEnabled = isSWMTCategorySettingFeatureEnabled;
	}
	
	/**
	 * Gets MemSpy Launcher plugin's binaries directory path
	 * @return path were Launcher binaries are located.
	 */
	public String getMemspyLauncherBinDir(){
				
		Bundle bundle = Platform
			.getBundle(MEMSPY_TRACE_PLUGIN_ID); //$NON-NLS-1$
		
		String launcherBinDir = null;
		try {
			 // URL to the Launcher binaries
			URL relativeURL = bundle.getEntry(LAUNCHER_BINARIES_DIR_NAME); //$NON-NLS-1$
			//	Converting into local path
			URL localURL = FileLocator.toFileURL(relativeURL);
			//	Getting install location in correct form
			File file = new File(localURL.getPath());
			launcherBinDir = file.getAbsolutePath();
			MemSpyConsole.getInstance().println("MemSpyLauncher binaries dir detected: " +launcherBinDir);
			
		} catch (IOException e) {
			MemSpyConsole.getInstance().println("MemSpyLauncher binaries dir detection failed, reason: " +e);
			e.printStackTrace();
		}
		return launcherBinDir;
		
	}	

	/**
	 * Returns PreferenceStore where plugin preferences are stored.
	 * @return PreferenceStore where plugin preferences are stored
	 */
	public static IPreferenceStore getPrefsStore() {
		if (prefsStore == null){
			prefsStore = getDefault().getPreferenceStore();
		}
		
		return prefsStore;
	}

	/**
	 * Gets trace provider interface instance if available.
	 * @return trace provider interface instance or <code>null</code> if not available
	 */
	public static ITraceProvider getTraceProvider(){
		if(isTraceProviderAvailable()){
			return traceProvider;			
		}
		// Otherwise returning a dummy provider instance
		return createDummyTraceProviderInstance();
	}
	
	/**
	 * Creates a dummy trace provider instance.
	 * Client can safely reference to this dummy instance without any <code>NullPointerException</code>
	 * problems etc. However the corresponding functionalities from UI should be disables.
	 * @return dummy trace provider instance.
	 */
	private static ITraceProvider createDummyTraceProviderInstance() {
		// Creating  just a dummy provider instance that does not do anything
		return new ITraceProvider() {
			
			public void stopListenTraceData() {
				// dummy provider does not take any actions
			}
			
			public boolean startListenTraceData(ITraceDataProcessor dataProcessor) {
				// dummy provider does not take any actions
				return false;
			}
			
			public boolean sendStringData(String stringData) {
				// dummy provider does not take any actions
				return false;
			}
			
			public boolean sendIntData(int integerData) {
				// dummy provider does not take any actions
				return false;
			}
			
			public String getTraceSourcePreferencePageId() {
				// dummy provider does not take any actions
				return null;
			}
			
			public String getDisplayNameForCurrentConnection() {
				return "<no trace plug-in installed>";
			}
			
			public void disconnectTraceSource() {
				// dummy provider does not take any actions
			}
			
			public boolean connectTraceSource(
					ITraceClientNotificationsIf traceClient) {
				// dummy provider does not take any actions
				return false;
			}
			
			public boolean activateTrace(String traceGroupID) {
				// dummy provider does not take any actions
				return false;
			}

		};
	}

	/**
	 * Checks if trace provider plug-in is available.
	 * @return <code>true</code> if trace provider interface available, otherwise <code>false</code>
	 */
	public static boolean isTraceProviderAvailable(){
		return (traceProvider != null);
	}
	
	/**
	 * Tries to find trace provider plug-ins. Selecting the first found one.
	 * @return reference to trace provider instance if found, otherwise <code>null</code>
	 */
	ITraceProvider findTraceProviderExtension() {
		try {
			IExtensionRegistry er = Platform.getExtensionRegistry();
			IExtensionPoint ep = er.getExtensionPoint(PLUGIN_ID, EXTENSION_TRACE_PROVIDER);
			String uniqueIdentifier = ep.getUniqueIdentifier();
			DbgUtility.println(DbgUtility.PRIORITY_OPERATION, "Found extension point: " + uniqueIdentifier); //$NON-NLS-1$
			IExtension[] extensions = ep.getExtensions();
			
			// if plug-ins were found.
			if (extensions != null && extensions.length > 0) {
				
				// read all found trace providers
				for (int i = 0; i < extensions.length; i++) {
					IConfigurationElement[] ce = extensions[i].getConfigurationElements();
					if (ce != null && ce.length > 0) {
						try {
							ITraceProvider provider = (ITraceProvider)ce[0].createExecutableExtension("class"); //$NON-NLS-1$ 
							// We support only one trace provider
							if (provider != null) {
								return provider;
							}
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return null;
	}	
	
}
