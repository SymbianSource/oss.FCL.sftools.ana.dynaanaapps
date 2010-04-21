/*
 * Copyright (c) 2008-2010 Nokia Corporation and/or its subsidiary(-ies).
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
 * Description:  Definitions for the class Activator
 *
 */

package com.nokia.s60tools.analyzetool;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

import com.nokia.s60tools.analyzetool.global.Constants;
import com.nokia.s60tools.analyzetool.ui.IActionListener;

/**
 * The activator class controls the plug-in life cycle
 * @author kihe 
 */
public class Activator extends AbstractUIPlugin {

    /** The plug-in ID. */
	public static final String PLUGIN_ID = Constants.PLUGINID;

	/** The shared instance. */
	private static Activator plugin;

	/** Preference store. */
	private static IPreferenceStore preferenceStore;

	/** Action listener. */
	private static IActionListener actionListener = null;

	/**
	 * The constructor.
	 */
	public Activator() {
		plugin = this;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	@Override
	public final void start(final BundleContext context) {
		try {
			super.start(context);

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	@Override
	public final void stop(final BundleContext context) {
		try {
			plugin = null;
			super.stop(context);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	/**
	 * Returns the shared instance.
	 *
	 * @return the shared instance
	 */
	public static Activator getDefault() {
		return plugin;
	}

	/**
	 * Returns an image descriptor for the image file at the given
	 * plug-in relative path.
	 *
	 * @param path
	 *            the path
	 * @return the image descriptor
	 */
	public static ImageDescriptor getImageDescriptor(final String path) {
		return imageDescriptorFromPlugin(PLUGIN_ID, path);
	}

	/**
	 * Returns AnalyzeTool used preference store.
	 *
	 * @return Preference store
	 */
	public static IPreferenceStore getPreferences() {

		// if preference store not yet created => create it
		if (preferenceStore == null) {
			preferenceStore = getDefault().getPreferenceStore();
		}

		return preferenceStore;
	}

	/**
	 * Add action listener class.
	 *
	 * @param listener
	 *            Action listener
	 */
	public static void setActionListener(final IActionListener listener) {
		actionListener = listener;
	}

	/**
	 * Gets action listener.
	 *
	 * @return ActionListener Action listener
	 */
	public static IActionListener getActionListener() {
		return actionListener;
	}

	/**
	 * Returns a File corresponding to the given bundle relative path.
	 * @param path the bundle relative path to resource to locate
	 * @return the File corresponding to the given bundle relative path, or null
	 * @throws IOException
	 */
	public File locateFileInBundle(final String path) throws IOException {
		Bundle myBundle= getDefault().getBundle();
		IPath ppath= new Path(path);
		ppath= ppath.makeRelative();
		URL[] urls= FileLocator.findEntries(myBundle, ppath);
		if(urls.length != 1) {
			return null;
		}
		return new File(FileLocator.toFileURL(urls[0]).getFile());
	}

	/**
	 * Logs information to the eclipse .log file
	 * @param severity Message severity
	 * @param code Message code
	 * @param message Message content
	 */
	public void logInfo(int severity, int code, String message) {
		IStatus status = new Status(severity, PLUGIN_ID, code, message, (Throwable) null);
		getDefault().getLog().log(status);
	}
	/**
	 * Logs the given message with the given severity and the given exception to
	 * this plug-in's log.
	 * @param aSeverity
	 *            the severity; one of the <strong>IStatus</strong> severity
	 *            constants: OK, ERROR, INFO, WARNING, or CANCEL
	 * @param aMessage
	 *            a human-readable message, localised to the current locale
	 * @param aException
	 *            a low-level exception, or null if not applicable
	 */
	public void log(final int aSeverity, final String aMessage,
			        final Exception aException)
	{
		getLog().log(new Status(aSeverity, PLUGIN_ID, IStatus.OK, aMessage, aException));
	}
	
	/**
	 * Creates and returns an Image for the file at the given plug-in relative
	 * path. The Image is cached in the #ImageRegistry.
	 * @param aPath the plug-in relative path to the image file
	 * @return the requested Image
	 */
	public Image getImage(final String aPath)
	{
		Image cachedImage = getImageRegistry().get(aPath);
		if (null == cachedImage)
		{
			cachedImage = getImageDescriptor(aPath).createImage();
			getImageRegistry().put(aPath, cachedImage);
		}
		return cachedImage;
	}
	
}
