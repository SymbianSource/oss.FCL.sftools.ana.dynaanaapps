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


package com.nokia.s60tools.memspy.preferences;


import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;

import com.nokia.s60tools.memspy.model.SWMTCategoryConstants;
import com.nokia.s60tools.memspy.plugin.MemSpyPlugin;

/**
 * Class used to initialize default preference values.
 */
public class MemSpyPreferenceInitializer extends AbstractPreferenceInitializer {

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer#initializeDefaultPreferences()
	 */
	public void initializeDefaultPreferences() {
		
		MemSpyPlugin.getPrefsStore().setDefault(MemSpyPreferenceConstants.SWMT_CATEGORY_SETTINGS_CUSTOM, SWMTCategoryConstants.PROFILE_BASIC);
		MemSpyPlugin.getPrefsStore().setDefault(MemSpyPreferenceConstants.SWMT_CATEGORY_SETTING, SWMTCategoryConstants.PROFILE_BASIC);
		MemSpyPlugin.getPrefsStore().setDefault(MemSpyPreferenceConstants.SWMT_CATEGORY_SETTING_PROFILE_SELECTED, true);
		MemSpyPlugin.getPrefsStore().setDefault(MemSpyPreferenceConstants.SWMT_HEAPFILTER_SETTING, "");		
		MemSpyPlugin.getPrefsStore().setDefault(MemSpyPreferenceConstants.SWMT_HEAP_DUMP_SELECTED, false);
		MemSpyPlugin.getPrefsStore().setDefault(MemSpyPreferenceConstants.CLOSE_BETWEEN_CYCLES, true);
		
	}
}

