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
 
 
package com.nokia.s60tools.memspy.common;

/**
 * This class stores product information such as product name, 
 * version, console view name etc.  
 * The idea is to have the product information in one place.
 */
public class ProductInfoRegistry {

	
	/**
	 * @return Returns the Console window name.
	 */
	public static String getConsoleWindowName() {
		return getProductName() + " " + Product.getString("ProductInfoRegistry.Console_Window_Name_Postfix");	 //$NON-NLS-1$ //$NON-NLS-2$
	}
	/**
	 * @return Returns the Product name.
	 */
	public static String getProductName() {
		return Product.getString("ProductInfoRegistry.Product_Name"); //$NON-NLS-1$
	}
	
	/**
	 * @return Returns the images directory.
	 */
	public static String getImagesDirectoryName() {
		return Product.getString("ProductInfoRegistry.Images_Directory");	 //$NON-NLS-1$
	}

	/**
	 * @return Returns the required MemSpy Launcher data version.
	 */
	public static int getRequiredMemSpyLauncherDataVersion() {
		String versionStr = Product.getString("ProductInfoRegistry.Required_MemSpyLauncherS60DataVersion");	 //$NON-NLS-1$
		int version = Integer.parseInt(versionStr);
		return version;
	}

	
}
