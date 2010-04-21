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

import org.eclipse.jface.preference.IPreferenceStore;

import com.nokia.s60tools.memspy.plugin.MemSpyPlugin;

/**
 * Helper class to use Dependency Explorer preferences. Use this class for accessing DE preferences 
 * instead of accessing directly through  {@link org.eclipse.jface.util.IPropertyChangeListener.IPreferenceStore}.
 */
public class MemSpyPreferences {
	
	
	/**
	 * Gets SWMT category settings for this session.
	 * @return SWMT category settings for this session
	 */
	public static int getSWMTCategorySetting() {
		
		IPreferenceStore store = MemSpyPlugin.getPrefsStore();
		
		boolean isProfileSelected = store.getBoolean(MemSpyPreferenceConstants.SWMT_CATEGORY_SETTING_PROFILE_SELECTED);
		int value;
		if(isProfileSelected){
			value = getSWMTCategorySettingForProfile();
		}else{
			value = store.getInt(MemSpyPreferenceConstants.SWMT_CATEGORY_SETTINGS_CUSTOM);			
		}
		return value ;
	}
	
	/**
	 * Gets SWMT category settings for Profile selection.
	 * @return SWMT category settings for profile
	 */
	public static int getSWMTCategorySettingForProfile() {		
		IPreferenceStore store = MemSpyPlugin.getPrefsStore();		
		return store.getInt(MemSpyPreferenceConstants.SWMT_CATEGORY_SETTING);
	}	

	/**
	 * Sets SWMT category settings for this session.
	 * @param sessionSpecificSWMTCategorySetting SWMT category settings to set for this session
	 */
	public static void setSWMTCategorySetting(int sessionSpecificSWMTCategorySetting, boolean isProfileSettings) {
		IPreferenceStore store = MemSpyPlugin.getPrefsStore();
		setProfileTrackedCategoriesSelected(isProfileSettings);
		if(isProfileSettings){
			store.setValue(MemSpyPreferenceConstants.SWMT_CATEGORY_SETTING, sessionSpecificSWMTCategorySetting);
		}else{
			store.setValue(MemSpyPreferenceConstants.SWMT_CATEGORY_SETTINGS_CUSTOM, sessionSpecificSWMTCategorySetting);
		}
	}	
	
	
	/**
	 * Sets SWMT category settings for this session.
	 * @param sessionSpecificSWMTCategorySetting SWMT category settings to set for this session
	 */
	public static void setProfileTrackedCategoriesSelected(boolean isAllCategoriesSelected) {		
		IPreferenceStore store = MemSpyPlugin.getPrefsStore();
		store.setValue(MemSpyPreferenceConstants.SWMT_CATEGORY_SETTING_PROFILE_SELECTED, isAllCategoriesSelected);
	}	
	
	/**
	 * Get if All Tracke Categories is selected.
	 * @param <code>true</code> if All is selected <code>false</code> otherwise.
	 */
	public static boolean isProfileTrackedCategoriesSelected() {
		
		IPreferenceStore store = MemSpyPlugin.getPrefsStore();
		boolean isAllSelected = store.getBoolean(MemSpyPreferenceConstants.SWMT_CATEGORY_SETTING_PROFILE_SELECTED);
		return isAllSelected;

	}
	
	/**
	 * Sets the SWMT Heap Dump selection on/off
	 * @param selection
	 */
	public static void setSWMTHeapDumpSelected(boolean selection) {
		IPreferenceStore store = MemSpyPlugin.getPrefsStore();
		store.setValue(MemSpyPreferenceConstants.SWMT_HEAP_DUMP_SELECTED, selection);
	}	
	
	/**
	 * Gets the SWMT Heap Dump selection
	 * @param <code>true</code> if Heap Dumps should receive with SWMT logging, <code>false</code> otherwise.
	 */
	public static boolean isSWMTHeapDumpSelected( ) {
		IPreferenceStore store = MemSpyPlugin.getPrefsStore();
		return store.getBoolean(MemSpyPreferenceConstants.SWMT_HEAP_DUMP_SELECTED);
	}		
	
	/**
	 * Gets SWMT category settings for this session.
	 * @return SWMT HeapNameFilter setting for this session.
	 */
	public static String getSWMTHeapNameFilter() {
		IPreferenceStore store = MemSpyPlugin.getPrefsStore();
		String filter = store.getString(MemSpyPreferenceConstants.SWMT_HEAPFILTER_SETTING);
		return filter;
	}
	
	/**
	 * Sets SWMT HeapNameFilter setting for this session.
	 * @param heapNameFilter SWMT HeapNameFilter settings to set for this session.
	 */
	public static void setSWMTHeapNameFilter(String heapNameFilter) {
		IPreferenceStore store = MemSpyPlugin.getPrefsStore();
		store.setValue(MemSpyPreferenceConstants.SWMT_HEAPFILTER_SETTING, heapNameFilter);		
	}
	
	/**
	 * Sets the user selection if S60 device is closed between cycles
	 * @param selection
	 */
	public static void setCloseSymbianAgentBetweenCycles(boolean selection) {
		IPreferenceStore store = MemSpyPlugin.getPrefsStore();
		store.setValue(MemSpyPreferenceConstants.CLOSE_BETWEEN_CYCLES, selection);
	}	
	
	/**
	 * Gets the user selection if S60 device is closed between cycles
	 * @param <code>true</code> if S60 Device is about to close between cycles, <code>false</code> otherwise.
	 */
	public static boolean isCloseSymbianAgentBetweenCyclesSelected( ) {
		IPreferenceStore store = MemSpyPlugin.getPrefsStore();
		return store.getBoolean(MemSpyPreferenceConstants.CLOSE_BETWEEN_CYCLES);
	}	
	
}
