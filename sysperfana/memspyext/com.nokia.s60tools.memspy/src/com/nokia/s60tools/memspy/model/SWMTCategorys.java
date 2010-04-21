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
package com.nokia.s60tools.memspy.model;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.nokia.s60tools.memspy.ui.dialogs.SWMTCategoryEntry;

/**
 * Singleton Class for holding SWMT Category profiles
 */
public class SWMTCategorys {
	

	private static SWMTCategorys instance=null;
	
	
	/**
	 * Private constructor
	 */
	private SWMTCategorys(){
		initializeCategoryEntries();
		initializeCategoryProfiles();
	}
		
	

	/**
	 * Stores the category entries.
	 */
	private List<SWMTCategoryEntry> categoryEntries;
		
	private List<CategoryProfile> categoryProfiles;
	
	/**
	 * Get only instance of this object
	 * @return instance
	 */
	public static SWMTCategorys getInstance(){
		if(instance==null){
			instance = new SWMTCategorys();
		}
		return instance;
	}
	
	private void initializeCategoryProfiles() {
		categoryProfiles = new ArrayList<CategoryProfile>();

		CategoryProfile profile0 = new CategoryProfile("Basic", SWMTCategoryConstants.PROFILE_BASIC);
		getCategoryProfiles().add(profile0);		
		
		CategoryProfile profile1 = new CategoryProfile("RAM & Disk Profile", SWMTCategoryConstants.PROFILE_RAM_DISK);
		getCategoryProfiles().add(profile1);
		
		CategoryProfile profile2 = new CategoryProfile("RAM, Disk & Heap Profile", SWMTCategoryConstants.PROFILE_RAM_DISK_HEAP);
		getCategoryProfiles().add(profile2);

		CategoryProfile profile3 = new CategoryProfile("RAM, Disk, Heap & Handles Profile", SWMTCategoryConstants.PROFILE_RAM_DISK_HEAP_HANDLES);
		getCategoryProfiles().add(profile3);

		CategoryProfile profile4 = new CategoryProfile("All", SWMTCategoryConstants.CATEGORY_ALL);
		getCategoryProfiles().add(profile4);		
		
	}


	
    /**
	 * Initializes category entry list.
	 */
	private void initializeCategoryEntries() {
		// Creating entry array
		categoryEntries = new ArrayList< SWMTCategoryEntry>();
		// Initializing value for the array
		categoryEntries.add( new SWMTCategoryEntry(SWMTCategoryConstants.CATEGORY_USERHEAP, SWMTCategoryConstants.USER_HEAP_TXT));
		categoryEntries.add( new SWMTCategoryEntry(SWMTCategoryConstants.CATEGORY_KERNELHEAP, SWMTCategoryConstants.KERNEL_HEAP_TXT));
		categoryEntries.add( new SWMTCategoryEntry(SWMTCategoryConstants.CATEGORY_USERSTACKS, SWMTCategoryConstants.USER_STACKS_TXT));
		categoryEntries.add( new SWMTCategoryEntry(SWMTCategoryConstants.CATEGORY_GLOBALDATA, SWMTCategoryConstants.GLOBAL_DATA_TXT));
		categoryEntries.add( new SWMTCategoryEntry(SWMTCategoryConstants.CATEGORY_DISKUSAGE, SWMTCategoryConstants.DISK_USAGE_TXT));
		categoryEntries.add( new SWMTCategoryEntry(SWMTCategoryConstants.CATEGORY_OPENFILES, SWMTCategoryConstants.OPEN_FILES_TXT));
		categoryEntries.add( new SWMTCategoryEntry(SWMTCategoryConstants.CATEGORY_RAMLOADEDCODE, SWMTCategoryConstants.RAM_LOADED_CODE_TXT));
		categoryEntries.add( new SWMTCategoryEntry(SWMTCategoryConstants.CATEGORY_SYSTEMMEMORY, SWMTCategoryConstants.SYSTEM_MEMORY_TXT));
		categoryEntries.add( new SWMTCategoryEntry(SWMTCategoryConstants.CATEGORY_LOCALCHUNKS, SWMTCategoryConstants.LOCAL_CHUNKS_TXT));
		categoryEntries.add( new SWMTCategoryEntry(SWMTCategoryConstants.CATEGORY_GLOBALCHUNKS, SWMTCategoryConstants.GLOBAL_CHUNKS_TXT));
		categoryEntries.add( new SWMTCategoryEntry(SWMTCategoryConstants.CATEGORY_RAMDRIVE, SWMTCategoryConstants.RAM_DRIVE_TXT));				
		categoryEntries.add( new SWMTCategoryEntry(SWMTCategoryConstants.CATEGORY_FILESERVERCACHE, SWMTCategoryConstants.FILE_SERVER_CACHE_TXT));
		categoryEntries.add( new SWMTCategoryEntry(SWMTCategoryConstants.CATEGORY_BITMAPHANDLES, SWMTCategoryConstants.BITMAP_HANDLES_TXT));
		categoryEntries.add( new SWMTCategoryEntry(SWMTCategoryConstants.CATEGORY_WINDOWGROUPS, SWMTCategoryConstants.WINDOW_GROUPS_TXT));
			
	}
	

	/**
	 * Get all category entries exist
	 * @return the categoryEntries
	 */
	public List<SWMTCategoryEntry> getCategoryEntries() {
		return categoryEntries;
	}
	
	/**
	 * Get a category entry by ID
	 * @return the categoryEntry or <code>null</code> if not found
	 */
	public SWMTCategoryEntry getCategoryEntry(int entryID) {
		
		
		for (Iterator<SWMTCategoryEntry> iterator = categoryEntries.iterator(); iterator.hasNext();) {
			SWMTCategoryEntry entry = (SWMTCategoryEntry) iterator.next();
			if(entry.getCategoryId() == entryID){
				return entry;
			}
		}
		
		return null;
	}	


	/**
	 * Get all category profiles created
	 * @return the categoryProfiles
	 */
	public List<CategoryProfile> getCategoryProfiles() {
		return categoryProfiles;
	}

	/**
	 * Get wanted SWMT Categorys by IDs
	 * @param categories
	 * @return {@link List} of {@link SWMTCategoryEntry}'s
	 */
	public List<SWMTCategoryEntry> getCategoryEntries(int categories) {
		
		List<SWMTCategoryEntry> wantedCategories = new ArrayList<SWMTCategoryEntry>();
		for (Iterator<SWMTCategoryEntry> iterator = categoryEntries.iterator(); iterator.hasNext();) {
			SWMTCategoryEntry entry = (SWMTCategoryEntry) iterator.next();
			if((entry.getCategoryId() & categories) != 0){
				wantedCategories.add(entry);
			}
		}
		return wantedCategories;
	}

	/**
	 * Get profile by it's name
	 * @param profileName
	 * @return profile int or <code>null</code> if not found
	 */
	public CategoryProfile getProfile(String profileName){
				
		for (Iterator<CategoryProfile> iterator = categoryProfiles.iterator(); iterator
				.hasNext();) {
			CategoryProfile profile = (CategoryProfile) iterator.next();
			if(profileName.equals(profile.getName())){
				return profile;
			}
			
		}		
		return null;
	}
	
	

}
