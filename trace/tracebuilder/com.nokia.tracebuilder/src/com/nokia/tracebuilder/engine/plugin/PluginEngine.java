/*
* Copyright (c) 2007 Nokia Corporation and/or its subsidiary(-ies). 
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
* Export plugin manager, which delegates calls to plug-ins
*
*/
package com.nokia.tracebuilder.engine.plugin;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.nokia.tracebuilder.engine.TraceBuilderConfiguration;
import com.nokia.tracebuilder.engine.TraceBuilderEngine;
import com.nokia.tracebuilder.engine.TraceBuilderErrorMessages;
import com.nokia.tracebuilder.engine.TraceBuilderGlobals;
import com.nokia.tracebuilder.engine.TraceBuilderErrorCodes.TraceBuilderErrorCode;
import com.nokia.tracebuilder.model.TraceBuilderException;
import com.nokia.tracebuilder.model.TraceModel;
import com.nokia.tracebuilder.model.TraceObjectPropertyVerifier;
import com.nokia.tracebuilder.plugin.TraceAPIPlugin;
import com.nokia.tracebuilder.plugin.TraceBuilderExport;
import com.nokia.tracebuilder.plugin.TraceBuilderImport;
import com.nokia.tracebuilder.plugin.TraceBuilderPlugin;

/**
 * Plugin engine, which delegates calls to plug-ins
 * 
 */
public final class PluginEngine extends TraceBuilderEngine {

	/**
	 * List of plug-ins
	 */
	private ArrayList<TraceBuilderPlugin> plugins = new ArrayList<TraceBuilderPlugin>();

	/**
	 * Property verifier
	 */
	private PluginTracePropertyVerifier verifier = new PluginTracePropertyVerifier(
			this);

	/**
	 * Trace model
	 */
	private TraceModel model;

	/**
	 * Project open flag
	 */
	private boolean projectOpen;

	/**
	 * Sets the trace model. This is not set in constructor, since plug-in
	 * engine is created before the model
	 * 
	 * @param model
	 *            the trace model
	 */
	public void setModel(TraceModel model) {
		this.model = model;
	}

	/**
	 * Gets the started flag
	 * 
	 * @return true if started, false if not
	 */
	public boolean isProjectOpen() {
		return projectOpen;
	}

	/**
	 * Adds a plugin
	 * 
	 * @param plugin
	 *            the plugin to be added
	 */
	public void add(TraceBuilderPlugin plugin) {
		plugins.add(plugin);
		if (plugin instanceof TraceAPIPlugin) {
			TraceAPIPlugin api = (TraceAPIPlugin) plugin;
			TraceAPIPluginManager manager = model
					.getExtension(TraceAPIPluginManager.class);
			manager.addFormatters(api.getFormatters());
			manager.addParsers(api.getParsers());
		}
	}

	/**
	 * Removes a plugin
	 * 
	 * @param plugin
	 *            the plugin to be removed
	 */
	public void remove(TraceBuilderPlugin plugin) {
		// Formatters / parsers are not removed. Currently this is not a
		// problem since plug-in's are removed only on shutdown
		plugins.remove(plugin);
	}

	/**
	 * Gets the property verifier interface
	 * 
	 * @return the verifier
	 */
	public TraceObjectPropertyVerifier getVerifier() {
		return verifier;
	}

	/**
	 * Checks if there are plug-ins
	 * 
	 * @return true if plug-ins exist
	 */
	public boolean hasPlugins() {
		return !plugins.isEmpty();
	}

	/**
	 * Gets the plug-ins
	 * 
	 * @return the plug-ins
	 */
	Iterator<TraceBuilderPlugin> getPlugins() {
		return plugins.iterator();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.engine.TraceBuilderEngine#projectExporting()
	 */
	@Override
	public void exportProject() {
		if (model.hasTraces()) {
			for (TraceBuilderPlugin plugin : plugins) {
				if (plugin instanceof TraceBuilderExport) {
					try {
						((TraceBuilderExport) plugin).exportTraceProject();
					} catch (TraceBuilderException e) {
						TraceBuilderGlobals.getEvents().postError(e);
					}
				}
			}
		} else {
			TraceBuilderGlobals.getEvents().postInfoMessage(
					TraceBuilderErrorMessages.getErrorMessage(
							TraceBuilderErrorCode.NO_TRACES_TO_EXPORT, null),
					null);
		}
	}

	/**
	 * Gets the list of TraceBuilderImport interfaces
	 * 
	 * @return the import interfaces
	 */
	public List<TraceBuilderImport> getImports() {
		ArrayList<TraceBuilderImport> list = new ArrayList<TraceBuilderImport>();
		for (TraceBuilderPlugin plugin : plugins) {
			if (plugin instanceof TraceBuilderImport) {
				list.add((TraceBuilderImport) plugin);
			}
		}
		return list;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.engine.TraceBuilderEngine#projectOpened()
	 */
	@Override
	public void projectOpened() {
		if (!projectOpen) {
			for (TraceBuilderPlugin plugin : plugins) {
				plugin.traceProjectOpened(model);
			}
			projectOpen = true;
		} else {
			if (TraceBuilderConfiguration.ASSERTIONS_ENABLED) {
				TraceBuilderGlobals.getEvents().postAssertionFailed(
						"PluginEngine.traceProjectOpened", null); //$NON-NLS-1$
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.engine.TraceBuilderEngine#projectClosing()
	 */
	@Override
	public void projectClosed() {
		if (projectOpen) {
			for (TraceBuilderPlugin plugin : plugins) {
				plugin.traceProjectClosed();
			}
			projectOpen = false;
		}
	}

}
