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

package com.nokia.carbide.cpp.pi.wizards;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The main plugin class to be used in the desktop.
 */
public class WizardsPlugin extends AbstractUIPlugin {
	public static final String PLUGIN_ID = "com.nokia.carbide.cpp.pi.wizards";	//$NON-NLS-1$
	public static final String PHONENSIS_IMAGE_ID = "phonensis.image";	//$NON-NLS-1$
	public static final String PHONE_IMAGE_ID = "phone.image";	//$NON-NLS-1$
	public static final String SIS_IMAGE_ID = "sis.image";	//$NON-NLS-1$
	public static final String CUSTRACE_IMAGE_ID = "custrace.image";	//$NON-NLS-1$

	//The shared instance.
	private static WizardsPlugin plugin;
	
	/**
	 * The constructor.
	 */
	public WizardsPlugin() {
		plugin = this;
	}

	/**
	 * This method is called upon plug-in activation
	 */
	public void start(final BundleContext context) throws Exception {
		super.start(context);
	}

	/**
	 * This method is called when the plug-in is stopped
	 */
	public void stop(final BundleContext context) throws Exception {
		super.stop(context);
		plugin = null;
	}

	/**
	 * Returns the shared instance.
	 */
	public static WizardsPlugin getDefault() {
		return plugin;
	}
	
	/**
	 * Returns an image descriptor for the image file at the given plug-in
	 * relative path.
	 * 
	 * @param path
	 *            the path
	 * @return the image descriptor
	 */
	public static ImageDescriptor getImageDescriptor(final String path) {
		ImageDescriptor descriptor = getDefault().getImageRegistry()
				.getDescriptor(path);
		if (descriptor == null) {
			descriptor = ImageDescriptor.createFromURL(getDefault().getBundle()
					.getEntry(path));
			getDefault().getImageRegistry().put(path, descriptor);
		}
		return descriptor;
	}
 }
