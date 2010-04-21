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
package com.nokia.s60tools.swmtanalyser.resources;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * Helper class to get the tooltips from the resource file.
 *
 */
public class Tooltips {
	private static final String BUNDLE_NAME = "com.nokia.s60tools.swmtanalyser.resources.tooltip"; //$NON-NLS-1$

	private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle
			.getBundle(BUNDLE_NAME);

	private Tooltips() {
	}

	/**
	 * Get a tooltip by key
	 * @param key
	 * @return tooltip
	 */
	public static String getTooltip(String key) {
		try {
			return RESOURCE_BUNDLE.getString(key);
		} catch (MissingResourceException e) {
			return '!' + key + '!';
		}
	}
}
