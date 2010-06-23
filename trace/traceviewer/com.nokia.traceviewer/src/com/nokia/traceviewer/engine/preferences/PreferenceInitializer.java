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
 * Class used to initialize default preference values.
 *
 */
package com.nokia.traceviewer.engine.preferences;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;

import com.nokia.traceviewer.TraceViewerPlugin;

/**
 * Class used to initialize default preference values.
 */
public class PreferenceInitializer extends AbstractPreferenceInitializer {

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.eclipse.core.runtime.preferences.AbstractPreferenceInitializer#
	 * initializeDefaultPreferences()
	 */
	@Override
	public void initializeDefaultPreferences() {
		IPreferenceStore store = TraceViewerPlugin.getDefault()
				.getPreferenceStore();

		// Configuration file
		store.setDefault(PreferenceConstants.CONFIGURATION_FILE,
				PreferenceConstants.DEFAULT_CONFIGURATION_FILE);

		// Timestamp accuracy
		store.setDefault(PreferenceConstants.TIMESTAMP_ACCURACY,
				PreferenceConstants.MILLISECOND_ACCURACY);

		// Time from previous checkbox
		store.setDefault(PreferenceConstants.TIME_FROM_PREVIOUS_TRACE_CHECKBOX,
				false);

		// Show component and group name before trace text checkbox
		store.setDefault(
				PreferenceConstants.SHOW_COMPONENT_GROUP_NAME_CHECKBOX, true);

		// Show class and function name before trace text checkbox
		store.setDefault(PreferenceConstants.SHOW_CLASS_FUNCTION_NAME_CHECKBOX,
				true);

		// Auto reload changed Dictionaries checkbox
		store.setDefault(PreferenceConstants.AUTO_RELOAD_DICTIONARIES_CHECKBOX,
				true);

		// Show BTrace variables checkbox
		store.setDefault(PreferenceConstants.SHOW_BTRACE_VARIABLES_CHECKBOX,
				true);

		// Show undecoded traces as what type checkbox
		store.setDefault(PreferenceConstants.SHOW_UNDECODED_TRACES_TYPE,
				PreferenceConstants.UNDECODED_ID_AND_DATA);

		// Insert OST as default data format
		store.setDefault(PreferenceConstants.DATA_FORMAT,
				"OST - Open System Trace"); //$NON-NLS-1$

		// Auto connect to dynamic connections
		store.setDefault(
				PreferenceConstants.AUTO_CONNECT_DYNAMIC_CONNECTIONS_CHECKBOX,
				false);
	}
}
