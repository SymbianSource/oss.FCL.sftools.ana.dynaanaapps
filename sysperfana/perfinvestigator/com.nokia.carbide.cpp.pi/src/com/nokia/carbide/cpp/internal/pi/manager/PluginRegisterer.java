/*
 * Copyright (c) 2009 Nokia Corporation and/or its subsidiary(-ies). 
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

package com.nokia.carbide.cpp.internal.pi.manager;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.osgi.framework.Bundle;

import com.nokia.carbide.cpp.internal.pi.interfaces.IReturnPlugin;
import com.nokia.carbide.cpp.internal.pi.plugin.model.AbstractPiPlugin;
import com.nokia.carbide.cpp.pi.PiPlugin;
import com.nokia.carbide.cpp.pi.editors.PIPageEditor;



public class PluginRegisterer
{
	private static final String extensionName = "com.nokia.carbide.cpp.pi.piPluginData"; //$NON-NLS-1$
	
	private static boolean alreadyRegistered = false;

	public PluginRegisterer()
	{
	}
	
	public static void registerAllPlugins()
	{
		if (alreadyRegistered)
			return;
		
		// start all PI plugins that have their dependencies resolved
		if (!PIPageEditor.arePluginsStarted()) {
			PIPageEditor.startPlugins();

			Bundle[] bundles = PiPlugin.getDefault().getBundle().getBundleContext().getBundles();
			for (int i = 0; i < bundles.length; i++) {
				Bundle bundle = bundles[i];
				if (bundle.getSymbolicName().matches(".*com\\.nokia\\.carbide\\.cpp\\.pi\\..*")) { //$NON-NLS-1$
					int state = bundle.getState();
					if (state == Bundle.RESOLVED)
						try {
							bundle.start(Bundle.START_TRANSIENT);
						} catch (Exception ex) {
						}
				}
			}
		}

		IExtensionRegistry registry    = Platform.getExtensionRegistry();
        IExtensionPoint extensionPoint = registry.getExtensionPoint(extensionName);

        if (extensionPoint == null)
        	return;
        
        IExtension[] extensions        = extensionPoint.getExtensions();
        // For each extension ...
    	for (int i = 0; i < extensions.length; i++) {
			IExtension extension             = extensions[i];
			IConfigurationElement[] elements = extension.getConfigurationElements();
            // For each member of the extension ...
			if (elements != null && elements.length >= 1) {
				IConfigurationElement element = elements[0];
				AbstractPiPlugin plugin;
				IReturnPlugin getPlugin;
				try {
					getPlugin = (IReturnPlugin) element.createExecutableExtension("pluginClass");  //$NON-NLS-1$
					plugin = getPlugin.getPlugin();
					PluginRegistry.getInstance().addRegistryEntry(plugin);
				} catch (CoreException e) {
					e.printStackTrace();
				}
			}
		}
    	
    	alreadyRegistered = true;
	}
}
