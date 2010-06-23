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
 * Access point to the exported interfaces of Trace Viewer engine
 *
 */
package com.nokia.traceviewer.engine;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleManager;
import org.eclipse.ui.console.MessageConsole;
import org.eclipse.ui.console.MessageConsoleStream;

import com.nokia.trace.eventrouter.PropertySource;
import com.nokia.trace.eventrouter.TraceEvent;
import com.nokia.trace.eventrouter.TraceEventRouter;
import com.nokia.traceviewer.TraceViewerPlugin;

/**
 * Access point to the exported interfaces of Trace Viewer engine
 * 
 */
public final class TraceViewerGlobals {

	/**
	 * Action name for UI Event
	 */
	private static final String UI_EVENT_ACTION_NAME = "uievent"; //$NON-NLS-1$

	/**
	 * ID for statisticslogger plugin
	 */
	private static final String STATISTICSLOGGER_ID = "com.nokia.trace.statisticslogger"; //$NON-NLS-1$

	/**
	 * Number of lines in one block.
	 */
	public static final int blockSize = 200;

	/**
	 * If this value is true, debug prints are printed to console
	 */
	private static final boolean debugPrintsEnabled = false;

	/**
	 * Debug print level. None to get nothing
	 */
	private static final DebugLevel debugPrintLevel = DebugLevel.NONE;

	/**
	 * Trace viewer instance
	 */
	private static TraceViewer instance;

	/**
	 * TrimProvider
	 */
	private static TrimProvider trimProvider = new DummyTrim();

	/**
	 * List of TraceProviders
	 */
	private static List<TraceProvider> traceProviders = new ArrayList<TraceProvider>();

	/**
	 * Debug levels
	 * 
	 */
	public enum DebugLevel {

		/**
		 * No prints
		 */
		NONE,

		/**
		 * Simple prints
		 */
		SIMPLE,

		/**
		 * Flow traces
		 */
		FLOW,

		/**
		 * Test traces
		 */
		TEST,

		/**
		 * All traces
		 */
		ALL
	}

	/**
	 * Constructor is hidden
	 */
	private TraceViewerGlobals() {
	}

	/**
	 * Starts Trace Viewer engine.
	 */
	public static void start() {
		if (instance == null) {
			instance = new TraceViewer();
			instance.start();
		}
	}

	/**
	 * Gets the trace viewer interface
	 * 
	 * @return trace viewer
	 */
	public static TraceViewerInterface getTraceViewer() {
		return instance.getTraceViewer();
	}

	/**
	 * Called by the provider plug-in to set the provider
	 * 
	 * @param provider
	 *            the trace provider
	 * @param clearDataBefore
	 *            if true, clear data file before changing the new provider
	 */
	public static void setTraceProvider(TraceProvider provider,
			boolean clearDataBefore) {
		// Add to list if doesn't exist yet
		if (!traceProviders.contains(provider)) {

			// Sort providers to alphabetic order. This is assuming there are
			// only two providers.
			if (!traceProviders.isEmpty()) {
				TraceProvider prevProvider = traceProviders.get(0);
				if (prevProvider.getName().compareTo(provider.getName()) < 0) {
					traceProviders.add(provider);
				} else {
					traceProviders.add(0, provider);
				}
			} else {
				traceProviders.add(provider);
			}
		}

		// Tell the TraceViewer to set TraceProvider into use
		instance.setTraceProvider(provider, clearDataBefore);
	}

	/**
	 * Gets the trace provider
	 * 
	 * @return the trace provider
	 */
	public static TraceProvider getTraceProvider() {
		return instance.getTraceProvider();
	}

	/**
	 * Sets Trim provider
	 * 
	 * @param provider
	 *            new trim provider
	 */
	public static void setTrimProvider(TrimProvider provider) {
		trimProvider = provider;
	}

	/**
	 * Gets the trim provider
	 * 
	 * @return the trimprovider
	 */
	public static TrimProvider getTrimProvider() {
		return trimProvider;
	}

	/**
	 * Gets list of available TraceProviders
	 * 
	 * @return the list of available TraceProviders
	 */
	public static List<TraceProvider> getListOfTraceProviders() {
		return traceProviders;
	}

	/**
	 * Called by the decoder plug-in to set the decoder
	 * 
	 * @param decoder
	 *            the trace decoder
	 */
	public static void setDecodeProvider(DecodeProvider decoder) {
		instance.setDecodeProvider(decoder);
	}

	/**
	 * Gets the decode provider
	 * 
	 * @return the decode provider
	 */
	public static DecodeProvider getDecodeProvider() {
		return instance.getDecodeProvider();
	}

	/**
	 * Posts Error Event
	 * 
	 * @param description
	 *            description of the event
	 * @param category
	 *            category of the event
	 * @param source
	 *            source of the event
	 */
	public static void postErrorEvent(String description, String category,
			Object source) {
		TraceEvent event = new TraceEvent(TraceEvent.ERROR, description);
		event.setCategory(category);

		// If source doesn't exist, insert current time
		if (source == null) {
			source = TraceViewerUtils.constructTimeString();
		}
		event.setSource(source);
		TraceEventRouter.getInstance().postEvent(event);
	}

	/**
	 * Posts Info Event
	 * 
	 * @param description
	 *            description of the event
	 * @param category
	 *            category of the event
	 * @param source
	 *            source of the event
	 */
	public static void postInfoEvent(String description, String category,
			Object source) {
		TraceEvent event = new TraceEvent(TraceEvent.INFO, description);
		event.setCategory(category);

		// If source doesn't exist, insert current time
		if (source == null) {
			source = TraceViewerUtils.constructTimeString();
		}
		event.setSource(source);
		TraceEventRouter.getInstance().postEvent(event);
	}

	/**
	 * Posts UI Event
	 * 
	 * @param name
	 *            name of the event
	 * @param value
	 *            value of the event
	 */
	public static void postUiEvent(String name, String value) {
		TraceEvent event = new TraceEvent(TraceEvent.INFO, "UI Event"); //$NON-NLS-1$
		PropertySource source = new PropertySource(UI_EVENT_ACTION_NAME,
				STATISTICSLOGGER_ID, TraceViewerPlugin.PLUGIN_ID);
		source.getProperties().put(name, value);
		event.setSource(source);
		TraceEventRouter.getInstance().postEvent(event);
	}

	/**
	 * Print debug prints to console or do nothing if debug flag is not enabled
	 * 
	 * @param text
	 *            text to print to console
	 * @param level
	 *            debug print level
	 */
	public static void debug(String text, DebugLevel level) {
		if (debugPrintsEnabled) {
			if (debugPrintLevel.compareTo(level) >= 0) {
				MessageConsole myConsole = findConsole("TraceViewerDebug"); //$NON-NLS-1$
				MessageConsoleStream out = myConsole.newMessageStream();
				out.println(text);
			}
		}
	}

	/**
	 * Find my console
	 * 
	 * @param name
	 *            name for the console
	 * @return my console
	 */
	private static MessageConsole findConsole(String name) {
		ConsolePlugin plugin = ConsolePlugin.getDefault();
		IConsoleManager conMan = plugin.getConsoleManager();
		IConsole[] existing = conMan.getConsoles();
		for (int i = 0; i < existing.length; i++)
			if (name.equals(existing[i].getName()))
				return (MessageConsole) existing[i];
		// no console found, so create a new one
		MessageConsole myConsole = new MessageConsole(name, null);
		conMan.addConsoles(new IConsole[] { myConsole });
		return myConsole;
	}

}
