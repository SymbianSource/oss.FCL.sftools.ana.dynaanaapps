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

import com.nokia.s60tools.util.debug.DbgUtility;

/**
 * Lists all the category constants that can be used with SWMT
 */
public class SWMTCategoryConstants {
	public static final int CATEGORY_NONE            = 0x0; // This is not used currently anywhere, but however available for use
	public static final int CATEGORY_FILESERVERCACHE = 0x0001;
	public static final int CATEGORY_BITMAPHANDLES   = 0x0002;
	public static final int CATEGORY_USERHEAP        = 0x0004;
	public static final int CATEGORY_KERNELHEAP      = 0x0008;
	public static final int CATEGORY_LOCALCHUNKS     = 0x0010;
	public static final int CATEGORY_GLOBALCHUNKS    = 0x0020;
	public static final int CATEGORY_RAMDRIVE        = 0x0040;
	public static final int CATEGORY_USERSTACKS      = 0x0080;
	public static final int CATEGORY_GLOBALDATA      = 0x0100;
	public static final int CATEGORY_RAMLOADEDCODE   = 0x0200;
	public static final int CATEGORY_KERNELHANDLES   = 0x0400;
	public static final int CATEGORY_OPENFILES       = 0x0800;
	public static final int CATEGORY_DISKUSAGE       = 0x1000;
	public static final int CATEGORY_SYSTEMMEMORY    = 0x2000;
	public static final int CATEGORY_WINDOWGROUPS    = 0x4000;
	public static final int CATEGORY_ALL             = 0xFFFF; // This is default value in case user has not skipped any categories
	
	/**
	 * Category profile constant for Basic Profile
	 */
	public static final int PROFILE_BASIC = CATEGORY_USERHEAP | CATEGORY_USERSTACKS | CATEGORY_GLOBALDATA | CATEGORY_SYSTEMMEMORY;	
	
	/**
	 * Category profile constant for Ram & Disk Profile
	 */
	public static final int PROFILE_RAM_DISK = CATEGORY_DISKUSAGE | CATEGORY_OPENFILES | CATEGORY_RAMLOADEDCODE | CATEGORY_RAMDRIVE;
	
	/**
	 *  Category profile constant for Ram, Disk & Heap Profile
	 */
	public static final int PROFILE_RAM_DISK_HEAP = PROFILE_RAM_DISK | CATEGORY_USERHEAP | CATEGORY_KERNELHEAP;
	
	/**
	 *  Category profile constant for Ram, Disk, Heap & Handles Profile
	 */
	public static final int PROFILE_RAM_DISK_HEAP_HANDLES = PROFILE_RAM_DISK_HEAP | CATEGORY_BITMAPHANDLES | CATEGORY_FILESERVERCACHE | CATEGORY_SYSTEMMEMORY;	

	/**
	 * Window Groups
	 */
	public static final String WINDOW_GROUPS_TXT = "Window Groups";
	/**
	 * Bitmap Handles
	 */
	public static final String BITMAP_HANDLES_TXT = "Bitmap Handles";
	/**
	 * FileServer Cache
	 */
	public static final String FILE_SERVER_CACHE_TXT = "FileServer Cache";
	/**
	 * RAM Drive
	 */
	public static final String RAM_DRIVE_TXT = "RAM Drive";
	/**
	 * Global Chunks
	 */
	public static final String GLOBAL_CHUNKS_TXT = "Global Chunks";
	/**
	 * Local Chunks
	 */
	public static final String LOCAL_CHUNKS_TXT = "Local Chunks";
	/**
	 * System Memory
	 */
	public static final String SYSTEM_MEMORY_TXT = "System Memory";
	/**
	 * RAM-loaded code
	 */
	public static final String RAM_LOADED_CODE_TXT = "RAM-loaded code";
	/**
	 * Open Files
	 */
	public static final String OPEN_FILES_TXT = "Open Files";
	/**
	 * Disk usage
	 */
	public static final String DISK_USAGE_TXT = "Disk usage";
	/**
	 * Global Data
	 */
	public static final String GLOBAL_DATA_TXT = "Global Data";
	/**
	 * User Stacks
	 */
	public static final String USER_STACKS_TXT = "User Stacks";
	/**
	 * Kernel Heap
	 */
	public static final String KERNEL_HEAP_TXT = "Kernel Heap";
	/**
	 * User Heap
	 */
	public static final String USER_HEAP_TXT = "User Heap";
	

	/**
	 * Debug prints those bits that are set in the category setting
	 * @param categories category settings to be debugged.
	 */
	public static void debugPrintSWMTCategorySetting(int categories){
		if((categories & CATEGORY_FILESERVERCACHE) != 0) DbgUtility.println(DbgUtility.PRIORITY_OPERATION, "- CATEGORY_FILESERVERCACHE"); //$NON-NLS-1$
		if((categories & CATEGORY_BITMAPHANDLES) != 0) DbgUtility.println(DbgUtility.PRIORITY_OPERATION, "- CATEGORY_BITMAPHANDLES"); //$NON-NLS-1$
		if((categories & CATEGORY_USERHEAP) != 0) DbgUtility.println(DbgUtility.PRIORITY_OPERATION, "- CATEGORY_USERHEAP"); //$NON-NLS-1$
		if((categories & CATEGORY_KERNELHEAP) != 0) DbgUtility.println(DbgUtility.PRIORITY_OPERATION, "- CATEGORY_KERNELHEAP"); //$NON-NLS-1$
		if((categories & CATEGORY_LOCALCHUNKS) != 0) DbgUtility.println(DbgUtility.PRIORITY_OPERATION, "- CATEGORY_LOCALCHUNKS"); //$NON-NLS-1$
		if((categories & CATEGORY_GLOBALCHUNKS) != 0) DbgUtility.println(DbgUtility.PRIORITY_OPERATION, "- CATEGORY_GLOBALCHUNKS"); //$NON-NLS-1$
		if((categories & CATEGORY_RAMDRIVE) != 0) DbgUtility.println(DbgUtility.PRIORITY_OPERATION, "- CATEGORY_RAMDRIVE"); //$NON-NLS-1$
		if((categories & CATEGORY_USERSTACKS) != 0) DbgUtility.println(DbgUtility.PRIORITY_OPERATION, "- CATEGORY_USERSTACKS"); //$NON-NLS-1$
		if((categories & CATEGORY_GLOBALDATA) != 0) DbgUtility.println(DbgUtility.PRIORITY_OPERATION, "- CATEGORY_GLOBALDATA"); //$NON-NLS-1$
		if((categories & CATEGORY_RAMLOADEDCODE) != 0) DbgUtility.println(DbgUtility.PRIORITY_OPERATION, "- CATEGORY_RAMLOADEDCODE"); //$NON-NLS-1$
		if((categories & CATEGORY_KERNELHANDLES) != 0) DbgUtility.println(DbgUtility.PRIORITY_OPERATION, "- CATEGORY_KERNELHANDLES"); //$NON-NLS-1$
		if((categories & CATEGORY_OPENFILES) != 0) DbgUtility.println(DbgUtility.PRIORITY_OPERATION, "- CATEGORY_OPENFILES"); //$NON-NLS-1$
		if((categories & CATEGORY_DISKUSAGE) != 0) DbgUtility.println(DbgUtility.PRIORITY_OPERATION, "- CATEGORY_DISKUSAGE"); //$NON-NLS-1$
		if((categories & CATEGORY_SYSTEMMEMORY) != 0) DbgUtility.println(DbgUtility.PRIORITY_OPERATION, "- CATEGORY_SYSTEMMEMORY"); //$NON-NLS-1$
		if((categories & CATEGORY_WINDOWGROUPS) != 0) DbgUtility.println(DbgUtility.PRIORITY_OPERATION, "- CATEGORY_WINDOWGROUPS"); //$NON-NLS-1$		
	}
	
}
