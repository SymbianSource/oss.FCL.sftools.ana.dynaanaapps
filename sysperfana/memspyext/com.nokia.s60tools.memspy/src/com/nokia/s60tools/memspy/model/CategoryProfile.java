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

import java.util.Iterator;
import java.util.List;

import com.nokia.s60tools.memspy.ui.dialogs.SWMTCategoryEntry;

/**
 * Class for one Category Profile
 */
public class CategoryProfile {

	private String name;
	
	private int categories = 0;

	/**
	 * Create a new Category Profile
	 * @param name Profile name
	 */
	public CategoryProfile(String name) {
		this.setName(name);
	}
	
	/**
	 * 
	 * @param name Profile name
	 * @param categories Categories for profile
	 */
	public CategoryProfile(String name, int categories) {
		this.categories = categories;
		this.setName(name);
	}	

	/**
	 * Get profile name
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Set profile name
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}
	
	/**
	 * Add one or more category to categories
	 * @param categories the categories to set
	 */
	public void addCategory(int category) {
		categories = categories & category;
	}	

	/**
	 * Get categories
	 * @return the categories
	 */
	public int getCategories() {
		return categories;
	}
	
	/**
	 * Get all SWMT categories for this profile
	 * @return the categories
	 */
	public List<SWMTCategoryEntry> getCategoryEntrys() {
		
		SWMTCategorys cat = SWMTCategorys.getInstance();
		List<SWMTCategoryEntry> categoryEntries = cat.getCategoryEntries(getCategories());
		
		return categoryEntries;
	}
	
	/**
	 * Get names of the Categories added to this profile.
	 * @return list of Category names
	 */
	public String [] getCategoryEntryNames(){
		List<SWMTCategoryEntry> categoryEntrys = getCategoryEntrys();
		if(categoryEntrys.isEmpty()){
			return new String[0];
		}
		String[] names = new String[categoryEntrys.size()];
		int i = 0;
		for (Iterator<SWMTCategoryEntry> iterator = categoryEntrys.iterator(); iterator.hasNext();) {
			SWMTCategoryEntry entry = (SWMTCategoryEntry) iterator
					.next();
			names[i] = entry.getCategoryName();
			i++;
		}
		return names;
	}

}
