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
* Configuration listener
*
*/
package com.nokia.tracebuilder.engine.rules;

import com.nokia.tracebuilder.engine.TraceBuilderConfiguration;
import com.nokia.tracebuilder.engine.TraceBuilderConfigurationListener;
import com.nokia.tracebuilder.engine.TraceBuilderGlobals;

/**
 * Configuration listener for rule engine monitors the trace formatting
 * 
 */
final class RulesEngineConfigurationListener implements
		TraceBuilderConfigurationListener {

	/**
	 * Rules engine
	 */
	private RulesEngine engine;

	/**
	 * Constructor
	 * 
	 * @param engine
	 *            the rules engine
	 */
	RulesEngineConfigurationListener(RulesEngine engine) {
		this.engine = engine;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.engine.TraceBuilderConfigurationListener#
	 *      configurationChanged(java.lang.String, java.lang.Object)
	 */
	public void configurationChanged(String property, Object newValue) {
		if (property.equals(TraceBuilderConfiguration.FORMATTER_NAME)) {
			if (TraceBuilderConfiguration.ALLOW_FORMAT_CHANGE) {
				engine.traceAPIChanged((String) newValue);
			}
		} else if (property.equals(TraceBuilderConfiguration.PRINTF_SUPPORT)
				|| property.equals(TraceBuilderConfiguration.PRINTF_EXTENSION)) {
			printfConfigurationChanged();
		}
	}

	/**
	 * Updates the printf configuration
	 */
	private void printfConfigurationChanged() {
		boolean flag = TraceBuilderGlobals.getConfiguration().getFlag(
				TraceBuilderConfiguration.PRINTF_SUPPORT);
		String name = TraceBuilderGlobals.getConfiguration().getText(
				TraceBuilderConfiguration.PRINTF_EXTENSION);
		engine.enablePrintfParser(flag);
		if (!flag) {
			engine.setPrintfMacroExtension(""); //$NON-NLS-1$
		} else {
			engine.setPrintfMacroExtension(name);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.engine.TraceBuilderConfigurationListener#configurationCreated()
	 */
	public void configurationCreated() {
		if (TraceBuilderConfiguration.ALLOW_FORMAT_CHANGE) {
			engine.traceAPIChanged(TraceBuilderGlobals.getConfiguration()
					.getText(TraceBuilderConfiguration.FORMATTER_NAME));
		}
		boolean flag = TraceBuilderGlobals.getConfiguration().getFlag(
				TraceBuilderConfiguration.PRINTF_SUPPORT);
		engine.enablePrintfParser(flag);
		if (flag) {
			engine.setPrintfMacroExtension(TraceBuilderGlobals
					.getConfiguration().getText(
							TraceBuilderConfiguration.PRINTF_EXTENSION));
		}
	}
}
