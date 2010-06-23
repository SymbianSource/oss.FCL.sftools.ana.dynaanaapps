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


package com.nokia.s60tools.traceanalyser.parameterrule.plugin;

import org.eclipse.core.runtime.Plugin;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class TraceAnalyserParameterRulePlugin extends Plugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "com.nokia.s60tools.traceanalyser.parameterrule";

	// The shared instance
	private static TraceAnalyserParameterRulePlugin plugin;
	
	/**
	 * TraceAnalyserParameterRulePlugin.
	 * The constructor
	 */
	public TraceAnalyserParameterRulePlugin() {
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.core.runtime.Plugins#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.core.runtime.Plugin#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

	/**
	 * getDefault.
	 * Returns the shared instance
	 * @return the shared instance
	 */
	public static TraceAnalyserParameterRulePlugin getDefault() {
		return plugin;
	}

}
