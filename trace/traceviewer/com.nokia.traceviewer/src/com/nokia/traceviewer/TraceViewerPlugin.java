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
 * TraceViewer Plugin
 *
 */
package com.nokia.traceviewer;

import java.util.ArrayList;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import com.nokia.traceviewer.engine.DecodeProvider;
import com.nokia.traceviewer.engine.TraceProvider;
import com.nokia.traceviewer.engine.TraceViewerGlobals;
import com.nokia.traceviewer.engine.TrimProvider;
import com.nokia.traceviewer.engine.preferences.PreferenceConstants;

/**
 * The activator class controls the plug-in life cycle
 * 
 */
public class TraceViewerPlugin extends AbstractUIPlugin {

	/**
	 * The plug-in ID
	 */
	public static final String PLUGIN_ID = "com.nokia.traceviewer"; //$NON-NLS-1$

	/**
	 * Trim Provider extension point ID
	 */
	private static final String TRIMPROVIDER_EXTENSION_POINT_ID = "com.nokia.traceviewer.trimprovider"; //$NON-NLS-1$

	/**
	 * Decode Provider extension point ID
	 */
	private static final String DECODEPROVIDER_EXTENSION_POINT_ID = "com.nokia.traceviewer.decodeprovider"; //$NON-NLS-1$

	/**
	 * Trace Provider extension point ID
	 */
	private static final String TRACEPROVIDER_EXTENSION_POINT_ID = "com.nokia.traceviewer.traceprovider"; //$NON-NLS-1$

	/**
	 * The shared instance
	 */
	private static TraceViewerPlugin plugin;

	/**
	 * The constructor
	 */
	public TraceViewerPlugin() {
		// Set this as a plugin
		plugin = this;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext
	 * )
	 */
	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);

		TraceViewerGlobals.start();

		// Insert providers
		insertProviders();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext
	 * )
	 */
	@Override
	public void stop(BundleContext context) throws Exception {
		// Run shutdown
		TraceViewerGlobals.getTraceViewer().shutdown();

		plugin = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 * 
	 * @return the shared instance
	 */
	public static TraceViewerPlugin getDefault() {
		// Returns this plugin
		return plugin;
	}

	/**
	 * Inserts trace and decode providers to the main engine
	 */
	private void insertProviders() {
		try {
			// Trim provider
			IConfigurationElement[] config = Platform.getExtensionRegistry()
					.getConfigurationElementsFor(
							TRIMPROVIDER_EXTENSION_POINT_ID);
			for (IConfigurationElement e : config) {
				Object o = e.createExecutableExtension("class"); //$NON-NLS-1$
				if (o instanceof TrimProvider) {
					TraceViewerGlobals.setTrimProvider((TrimProvider) o);
				}
			}

			// Decode provider
			config = Platform.getExtensionRegistry()
					.getConfigurationElementsFor(
							DECODEPROVIDER_EXTENSION_POINT_ID);
			for (IConfigurationElement e : config) {
				Object o = e.createExecutableExtension("class"); //$NON-NLS-1$
				if (o instanceof DecodeProvider) {
					TraceViewerGlobals.setDecodeProvider((DecodeProvider) o);
				}
			}

			// Trace providers
			config = Platform.getExtensionRegistry()
					.getConfigurationElementsFor(
							TRACEPROVIDER_EXTENSION_POINT_ID);

			// Put providers to ArrayList
			ArrayList<TraceProvider> providers = new ArrayList<TraceProvider>();
			for (int i = 0; i < config.length; i++) {
				Object o = config[i].createExecutableExtension("class"); //$NON-NLS-1$
				if (o instanceof TraceProvider) {
					providers.add((TraceProvider) o);
				}
			}

			// First check if selected is found and select that
			String selectedDataFormat = plugin.getPreferenceStore().getString(
					PreferenceConstants.DATA_FORMAT);
			for (int j = 0; j < providers.size(); j++) {
				TraceProvider provider = providers.get(j);
				if (provider.getName().equals(selectedDataFormat)) {
					TraceViewerGlobals.setTraceProvider(provider, true);
					providers.remove(j);
					break;
				}
			}

			// Then set rest of the providers
			for (int j = 0; j < providers.size(); j++) {
				TraceViewerGlobals.setTraceProvider(providers.get(j), true);
			}
		} catch (Exception ex) {
			System.out.println(ex.getMessage());
		}
	}

}
