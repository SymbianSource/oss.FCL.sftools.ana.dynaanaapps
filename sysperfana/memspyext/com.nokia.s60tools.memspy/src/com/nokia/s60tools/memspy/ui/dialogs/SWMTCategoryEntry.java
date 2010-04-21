/*
* Copyright (c) 2006 Nokia Corporation and/or its subsidiary(-ies). 
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
 
package com.nokia.s60tools.memspy.ui.dialogs;

import com.nokia.s60tools.memspy.model.SWMTCategoryConstants;


/**
 * Stores information on a single SIS file entry.
 */
public class SWMTCategoryEntry {
	
	//
	// Column sorting indices for table column sorter
	//
	public static final int NAME_COLUMN_INDEX = 0;
	
	/**
	 * Category id listed in SWMT category constants
	 * @see SWMTCategoryConstants
	 */
	private final int categoryId;
	
	/**
	 * Name of the SWMT category. 
	 */
	private final String categoryName;
		
	/**
	 * Constructor.
	 * @param categoryId path name of the directory SIS file is locating.
	 * @param categoryName name of the SIS file without path.
	 */
	public SWMTCategoryEntry(int categoryId, String categoryName){
		this.categoryId = categoryId;
		this.categoryName = categoryName;		
	}

	/**
	 * Gets path name of the directory SIS file is locating.
	 * @return path name of the directory SIS file is locating.
	 */
	public int getCategoryId() {
		return categoryId;
	}

	/**
	 * Gets name SWMT category. 
	 * @return name SWMT category entry
	 */
	public String getCategoryName() {
		return categoryName;
	}
	
}
