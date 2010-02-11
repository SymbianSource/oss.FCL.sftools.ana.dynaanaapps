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

package com.nokia.carbide.cpp.pi.util;

import org.eclipse.core.runtime.Plugin;
import org.osgi.framework.BundleContext;

/**
 * The main plugin class to be used in the desktop.
 */
public class PIUtilPlugin extends Plugin {

	public static final String PLUGIN_ID = "com.nokia.carbide.cpp.pi.ui"; //$NON-NLS-1$

	//The shared instance.
	private static PIUtilPlugin plugin;

	private static void setPlugin(PIUtilPlugin newPlugin)
	{
		plugin = newPlugin;
	}

	/**
	 * The constructor.
	 */
	public PIUtilPlugin() {
		setPlugin(this);
	}

	/**
	 * This method is called upon plug-in activation
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
	}

	/**
	 * This method is called when the plug-in is stopped
	 */
	public void stop(BundleContext context) throws Exception {
		super.stop(context);
		setPlugin(null);
	}

	/**
	 * Returns the shared instance.
	 */
	public static PIUtilPlugin getDefault() {
		return plugin;
	}
}
