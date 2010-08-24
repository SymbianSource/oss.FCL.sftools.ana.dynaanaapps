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
 * Description:  Definitions for the class PreferenceInitializer
 *
 */

package com.nokia.s60tools.analyzetool.preferences;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;

import com.nokia.s60tools.analyzetool.Activator;
import com.nokia.s60tools.analyzetool.global.Constants;

/**
 * Class to initialize default preference values.
 */
public class PreferenceInitializer extends AbstractPreferenceInitializer {

	/*
	 * @seeorg.eclipse.core.runtime.preferences.AbstractPreferenceInitializer#
	 * initializeDefaultPreferences()
	 */
	@Override
	public void initializeDefaultPreferences() {

		IPreferenceStore store = Activator.getPreferences();

		store.setDefault(Constants.LOGGING_MODE, Constants.LOGGING_EXT_FAST);
		store.setDefault(Constants.REPORT_LEVEL, Constants.REPORT_KNOWN);
		store.setDefault(Constants.USE_INTERNAL, false);
		store
				.setDefault(Constants.ATOOL_FOLDER,
						Constants.DEFAULT_ATOOL_FOLDER);
		store.setDefault(Constants.USER_SELECTED_FOLDER,
				Constants.DEFAULT_ATOOL_FOLDER);
		store.setDefault(Constants.CREATE_STATISTIC, false);
		store.setDefault(Constants.USE_ROM_SYMBOL, false);
		store.setDefault(Constants.CALLSTACK_SIZE, 40);
		store.setDefault(Constants.LOGGING_FAST_ENABLED, false);
	}
}
