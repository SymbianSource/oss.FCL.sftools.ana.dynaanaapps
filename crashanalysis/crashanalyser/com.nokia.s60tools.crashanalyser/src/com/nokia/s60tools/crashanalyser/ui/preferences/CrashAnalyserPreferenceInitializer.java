/*
* Copyright (c) 2008 Nokia Corporation and/or its subsidiary(-ies). 
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

package com.nokia.s60tools.crashanalyser.ui.preferences;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;

import com.nokia.s60tools.crashanalyser.plugin.*;

/**
 * Class used to initialize default preference values.
 */
public class CrashAnalyserPreferenceInitializer extends AbstractPreferenceInitializer {

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer#initializeDefaultPreferences()
	 */
	public void initializeDefaultPreferences() {
		IPreferenceStore store = CrashAnalyserPlugin.getCrashAnalyserPrefsStore();
		store.setDefault(CrashAnalyserPreferenceConstants.TRACE_LISTENER, true);
		store.setDefault(CrashAnalyserPreferenceConstants.EPOCWIND_LISTENER, false);
		store.setDefault(CrashAnalyserPreferenceConstants.SHOW_VISUALIZER, true);
	}
}

